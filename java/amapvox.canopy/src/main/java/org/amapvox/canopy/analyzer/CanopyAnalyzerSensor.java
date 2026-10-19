/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */
package org.amapvox.canopy.analyzer;

import org.amapvox.commons.math.util.SphericalCoordinates;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;
import org.amapvox.canopy.analyzer.Ring.ZenithalBand;
import org.apache.log4j.Logger;

/**
 * Simulates LAI2000 and LAI2200 sensors.
 *
 * https://www.licor.com/support/LAI-2200C/topics/theory.html
 * 
 * @author Julien Heurtebize, Philippe Verley
 */
public abstract class CanopyAnalyzerSensor {

    protected final static Logger logger = Logger.getLogger(CanopyAnalyzerSensor.class);

    protected int positionNumber;

    protected final Ring[] rings;

    protected int[] ringOffsetID;

    protected float[] avgTransByRing;
    protected float[] meanContactNumber;
    protected float[] gapsByRing;

    protected float[] contactNumberByRing;
    protected float[][] contactNumberPerRingAndPos;

    // standard deviation of the contact numbers for each ring
    protected float[] stdevByRing;
    protected float sel;

    protected float DIFN;

    // apparent clumping factor for each ring
    protected float[] acfsByRing;

    public float[][] transmittances;

    //test
    public float[][] normalizedTransmittances;
    public float[][] pathLengths;
    protected int[][] countByPositionAndRing2;

    protected float[] byPosition_LAI;
    //lai for all positions
    protected float LAI; 
    protected float acf;

    protected int[][] countPerPositionAndRing;

    /**
     * Abstract function that writes LAI output.
     *
     * @param outputFile the output file
     */
    public abstract void writeOutput(File outputFile);

    /**
     * Create a LAI2000-2200 device
     *
     * @param rings
     */
    protected CanopyAnalyzerSensor(Ring... rings) {

        /**
         * *Sum of non masked rings weighting factor should be equals to 1 and
         * sum of non masked rings weighting factor prime should be equals to
         * 0.5**
         */
        float residualWeightingFac = 0;
        float residualWeightingFacPrime = 0;

        int nbMaskedRings = 0;

        for (Ring ring : rings) {
            if (ring.isMasked()) {
                residualWeightingFac += ring.getWeightingFactor();
                residualWeightingFacPrime += ring.getWeightingFactorPrime();
                nbMaskedRings++;
            }
        }

        int nbNotMaskedRings = rings.length - nbMaskedRings;
        if (residualWeightingFac > 0) {

            float fac = residualWeightingFac / nbNotMaskedRings;
            float facPrime = residualWeightingFacPrime / nbNotMaskedRings;

            for (Ring ring : rings) {
                if (!ring.isMasked()) {
                    ring.setWeightingFactor(ring.getWeightingFactor() + fac);
                    ring.setWeightingFactorPrime(ring.getWeightingFactorPrime() + facPrime);
                }
            }
        }

        this.avgTransByRing = new float[rings.length];
        this.gapsByRing = new float[rings.length];
        this.meanContactNumber = new float[rings.length];

        this.rings = rings;
    }

    public enum ViewCap {

        CAP_360(360),
        CAP_270(270),
        CAP_180(180),
        CAP_90(90),
        CAP_45(45);

        private final float viewCap;

        private ViewCap(float angle) {
            this.viewCap = angle;
        }

        public float getViewCap() {
            return viewCap;
        }
    }

    public void initPositions(int positionNumber) {

        this.positionNumber = positionNumber;

        transmittances = new float[rings.length][positionNumber];
        countPerPositionAndRing = new int[positionNumber][rings.length];

        //test
//        normalizedTransmittances = new float[rings.length][positionNumber];
//        pathLengths = new float[rings.length][positionNumber];
//        countByPositionAndRing2 = new int[positionNumber][rings.length];
    }

    public void addNormalizedTransmittance(int ringID, int position, float transmittance, float pathLength) {

        if (ringID < rings.length && position < positionNumber) {
            normalizedTransmittances[ringID][position] += transmittance;
            countByPositionAndRing2[position][ringID]++;
            pathLengths[ringID][position] += pathLength;
        }
    }

    public void addTransmittance(int ringID, int position, float transmittance) {

        if (ringID < rings.length && position < positionNumber) {

            if (!Float.isNaN(transmittance)/* && transmittance != 0*/) {

                if (transmittance == 0) {
                    transmittance = 0.0000000001f;
                }
                transmittances[ringID][position] += transmittance;
                countPerPositionAndRing[position][ringID]++;
            }
        }
    }

    /**
     * Computes directional light paths for simulating LAI-2200/LAI-2000
     * sensors. Directions are distributed across the hemisphere, with counts
     * proportional to each ring's solid angle.
     *
     * @param directionCount Total number of light paths to sample the
     * hemisphere.
     * @return Array of direction vectors (x, y, z, ring.band ID as float) in
     * Cartesian coordinates.
     */
    public Direction[] computeDirections(int directionCount) {

        // Calculate total solid angle of all rings (hemisphere coverage)
        float totalSolidAngle = (float) Arrays.stream(rings)
                .mapToDouble(ring -> ring.getSolidAngle())
                .sum();

        // Directions per ring if uniformly distributed (baseline for hybrid allocation)
        int uniformRingDirectionCount = directionCount / rings.length;

        List<Direction> directions = new ArrayList<>();

        ringOffsetID = new int[rings.length];

        // Loop over sensor rings
        int ringIndex = 0;
        for (Ring ring : rings) {

            ringOffsetID[ringIndex] = directions.size();

            // Fraction of the hemisphere's solid angle covered by this ring
            float ringSolidAngleFraction = (ring.getSolidAngle() / totalSolidAngle);

            // Allocate directions to this ring
            // Average of uniform and solid-angle-proportional distributions
            int ringDirectionCount = (int) (0.5 * (uniformRingDirectionCount + Math.ceil(ringSolidAngleFraction * directionCount)));

            // Divide the ring into zenithal bands for finer sampling
            ZenithalBand[] zenithalBands = ring.createZenithalBands(ringDirectionCount);

            float azimuth;
            float azimuthOffset = 0.f;

            // Loop over zenithal bands within the rings
            int bandIndex = 0;
            for (ZenithalBand band : zenithalBands) {
                if (band.getAzimuthDirectionCount() != 0) {
                    // Azimuthal step size (degrees) for even spacing around the 360° circle
                    float azimuthStep = 360.f / band.getAzimuthDirectionCount();
                    azimuth = azimuthOffset;

                    // Generate directions at the band's fixed zenith angle
                    float zenithAngle = band.getZenithAngle();
                    for (int azimuthIndex = 0; azimuthIndex < band.getAzimuthDirectionCount(); azimuthIndex++) {
                        Point3d cartesian = SphericalCoordinates.toCartesian(
                                Math.toRadians(azimuth),
                                Math.toRadians(zenithAngle));
                        directions.add(new Direction(
                                (float) cartesian.x, (float) cartesian.y, (float) cartesian.z,
                                ringIndex, bandIndex));
                        azimuth += azimuthStep;
                    }
                    // Offset next band's starting azimuth by half a step to avoid alignment
                    azimuthOffset += (azimuthStep / 2.f);
                }
                bandIndex++;
            }
            ringIndex++;
        }

        return directions.toArray(Direction[]::new);
    }
    
    public int getRingNumber() {
        return rings.length;
    }

    public float computeLAIForOneMeasure(double[] contactNumberPerRing) {

        float lai = 0.0f;
        for (int i = 0; i < 5; i++) {
            lai += contactNumberPerRing[i] * rings[i].getWeightingFactor();
        }
        lai *= 2;
        return lai;
    }

    public float computeContactNumber(float transmittance, int ringID) {

        float contactNumber = (float) (-Math.log(transmittance) / rings[ringID].getDist());
        return contactNumber;
    }

    public float computeSkyDIFN(float[][] aboveReadingsPerRingAndPos, float[] gapsByRing) {

        int nbAboveReadings = aboveReadingsPerRingAndPos[0].length;

        float[] aboveReadingsPerRing = new float[rings.length];
        float numerateur = 0, denominateur = 0;

        for (int i = 0; i < rings.length; i++) {

            for (int j = 0; j < nbAboveReadings; j++) {
                aboveReadingsPerRing[i] += aboveReadingsPerRingAndPos[i][j];
            }

            aboveReadingsPerRing[i] /= nbAboveReadings;

            numerateur += (aboveReadingsPerRing[i] * gapsByRing[i] * rings[i].getWeightingFactorPrime());
            denominateur += (aboveReadingsPerRing[i] * rings[i].getWeightingFactorPrime());

        }

        float skyDIFN = numerateur / denominateur;

        return skyDIFN;

    }

    public void computeValues() {

        acfsByRing = new float[rings.length];
        contactNumberByRing = new float[rings.length]; //Ki
        avgTransByRing = new float[rings.length];
        gapsByRing = new float[rings.length];
        byPosition_LAI = new float[positionNumber];
        contactNumberPerRingAndPos = new float[rings.length][positionNumber];

        logger.info("Computation of AVGTRANS, ACFS, CNTCT#, GAPS, DIFN ...");

        float lai;

        for (int j = 0; j < positionNumber; j++) {

            lai = 0;

            for (int i = 0; i < rings.length; i++) {

                transmittances[i][j] /= (float) countPerPositionAndRing[j][i];

                float contactNumber = computeContactNumber(transmittances[i][j], i);
                contactNumberPerRingAndPos[i][j] = contactNumber;

                contactNumberByRing[i] += contactNumber;

                avgTransByRing[i] += transmittances[i][j];

                double gap = Math.log(transmittances[i][j]);
                gapsByRing[i] += gap;

                if (!rings[i].isMasked()) {
                    lai += contactNumber * rings[i].getWeightingFactor();
                }
            }

            lai *= 2;
            byPosition_LAI[j] = lai;

            this.LAI += lai;
        }

        DIFN = 0;

        for (int i = 0; i < rings.length; i++) {

            contactNumberByRing[i] /= positionNumber;

            avgTransByRing[i] /= positionNumber;

            gapsByRing[i] /= positionNumber;

            acfsByRing[i] = (float) (Math.log(avgTransByRing[i])) / gapsByRing[i];

            gapsByRing[i] = (float) Math.exp(gapsByRing[i]);

            DIFN += gapsByRing[i] * rings[i].getWeightingFactorPrime();
        }

        LAI /= positionNumber;
        DIFN *= 2;

        //compute acf
        logger.info("Computation of ACF...");

        float numerateur = 0;

        for (int i = 0; i < rings.length; i++) {
            numerateur += -(Math.log(avgTransByRing[i]) / rings[i].getDist()) * rings[i].getWeightingFactor();
        }

        numerateur *= 2;

        acf = numerateur / LAI;

        logger.info("Computation of STDEV, SEL...");

        stdevByRing = new float[rings.length];

        for (int i = 0; i < rings.length; i++) {
            for (int j = 0; j < positionNumber; j++) {

                stdevByRing[i] += Math.pow(contactNumberPerRingAndPos[i][j] - contactNumberByRing[i], 2);

            }
        }

        for (int i = 0; i < rings.length; i++) {
            stdevByRing[i] = (float) Math.sqrt((1 / (float) (positionNumber - 1)) * stdevByRing[i]);
        }

        //compute sel
        sel = 0;

        for (int j = 0; j < positionNumber; j++) {

            sel += ((byPosition_LAI[j] * byPosition_LAI[j]) - (LAI * LAI));
        }

        sel /= positionNumber;
        sel = (float) Math.sqrt(sel / positionNumber);
    }

    public int getRingIDFromDirectionID(int directionID) {

        for (int i = 0; i < ringOffsetID.length; i++) {

            if (i + 1 < ringOffsetID.length) {
                if (directionID >= ringOffsetID[i] && directionID < ringOffsetID[i + 1]) {
                    return i;
                }
            } else {
                return i;
            }
        }

        return -1;
    }

    public float[] getByPosition_LAI() {
        return byPosition_LAI;
    }

    public class Direction extends Vector3f {

        Direction(float x, float y, float z, int ringIndex, int bandIndex) {
            super(x, y, z);
            this.ringIndex = ringIndex;
            this.bandIndex = bandIndex;
        }

        public final int ringIndex;
        public final int bandIndex;
    }
}
