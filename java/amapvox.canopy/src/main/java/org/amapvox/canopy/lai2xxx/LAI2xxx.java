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
package org.amapvox.canopy.lai2xxx;

import org.amapvox.commons.math.util.SphericalCoordinates;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;
import org.apache.log4j.Logger;

/**
 * <p>
 * This class compute lai2000-2200 values based on the lai-2200 manual</p>
 *
 * @see
 * <a href=http://www.licor.co.za/manuals/LAI-2200_Manual.pdf>http://www.licor.co.za/manuals/LAI-2200_Manual.pdf</a>
 * @author Julien Heurtebize
 */
public abstract class LAI2xxx {

    protected final static Logger logger = Logger.getLogger(LAI2xxx.class);

    /**
     *
     */
    protected int directionNumber;
    protected int positionNumber;

    /**
     *
     */
    protected final Ring[] rings;

    /**
     *
     */
    protected Vector3f[] directions;
    protected int[] ringOffsetID;

    private int[] shotNumberByRing;
    protected float[] avgTransByRing;
    protected float[] meanContactNumber;
    protected float[] gapsByRing;

    protected float[] contactNumberByRing;
    protected float[][] contactNumberPerRingAndPos;

    /**
     * standard deviation of the contact numbers for each ring
     */
    protected float[] stdevByRing;
    protected float sel;

    protected float DIFN;

    /**
     * apparent clumping factor for each ring
     */
    protected float[] acfsByRing;

    public float[][] transmittances;

    //test
    public float[][] normalizedTransmittances;
    public float[][] pathLengths;
    protected int[][] countByPositionAndRing2;

    protected float[] byPosition_LAI;
    protected float LAI; //lai for all positions
    protected float acf;

    protected ViewCap viewCap;

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
     * @param shotNumber
     * @param viewCap
     * @param rings
     */
    protected LAI2xxx(int shotNumber, ViewCap viewCap, Ring... rings) {

        /**
         * *Sum of non masked rings weighting factor should be equals to 1 and
         * sum of non masked rings weighting factor prime should be equals to 0.5**
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

        this.directionNumber = shotNumber;
        this.viewCap = viewCap;
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
     *
     */
    public void computeDirections() {

        //somme des angles solides
        float solidAngleSum = 0;

        for (Ring ring : rings) {
            solidAngleSum += ring.getSolidAngle();
        }

        //nombre de tirs par ring
        shotNumberByRing = new int[rings.length];

        for (int i = 0; i < rings.length; i++) {

            //pourcentage d'angle solide
            float solidAnglePercentage = (rings[i].getSolidAngle() / solidAngleSum);

            shotNumberByRing[i] = (int) Math.ceil(solidAnglePercentage * directionNumber);
        }

        int nbDirectionForOneRing = (directionNumber / 5);

        for (int i = 0; i < rings.length; i++) {

            shotNumberByRing[i] = (shotNumberByRing[i] + nbDirectionForOneRing) / 2;
            rings[i].setNbDirections(shotNumberByRing[i]);
            logger.info("Nb shots ring " + (i + 1) + " = " + shotNumberByRing[i]);
        }

//        int nbSubRings=3;
//        switch(directionNumber){
//            case 500:
//                nbSubRings = 4;
//                break;
//            case 4000:
//                nbSubRings = 8;
//                break;
//            case 10000:
//                nbSubRings = 12;
//                break;
//            default:
//                if(directionNumber > 4000){
//                    nbSubRings = 6;
//                }else if(directionNumber < 500){
//                    nbSubRings = 3;
//                }
//                
//        }
        //paramètres définissant le balayage incrémentielle d'un angle donné
        RingInformation[] ringInformations = new RingInformation[rings.length];

        for (int i = 0; i < rings.length; i++) {

            Ring ring = rings[i];

            ringInformations[i] = new RingInformation(
                    ring.getLowerZenithalAngle(),
                    ring.getUpperZenithalAngle(),
                    360 / (float) shotNumberByRing[i], shotNumberByRing[i], rings[i].getSolidAngle());

            /*ringInformations[i] = new RingInformation(
                    ring.getUpperZenithalAngle() + (ring.getLowerZenithalAngle() - ring.getUpperZenithalAngle()) * 0.25f,
                    ring.getUpperZenithalAngle() + (ring.getLowerZenithalAngle() - ring.getUpperZenithalAngle()) * 0.75f,
                    360/(float)shotNumberByRing[i]);*/
        }

        List<Vector3f> directionList = new ArrayList<>();
//        List<Float> azimuthAnglesList = new ArrayList<>();
//        List<Float> elevationAnglesList = new ArrayList<>();

        //view cap: calcul des bornes azimutales
//        float viewCapAngle = viewCap.getViewCap();
//        float minAzimuthAngle = 180 + ((360 - viewCapAngle)/2.0f);
//        float maxAzimuthAngle = 180 - ((360 - viewCapAngle)/2.0f);
        ringOffsetID = new int[rings.length];

        //pour chaque plage angulaire
        for (int i = 0; i < rings.length; i++) {

            ringOffsetID[i] = directionList.size();

            RingInformation ringInformation = ringInformations[i];

            float azimuthalAngle;
            float elevationAngle;
            float azimuthalOffset = 0.f;

            for (int j = 0; j < ringInformation.nbSubRings; j++) {

                if (ringInformation.subRingsSamplingRate[j] != 0) {

                    float azimuthalStep = 360 / (float) ringInformation.subRingsSamplingRate[j];

                    azimuthalAngle = azimuthalOffset;

                    for (int s = 0; s < ringInformation.subRingsSamplingRate[j]; s++) {

                        elevationAngle = ringInformation.subRingsAngles[j];
//                        azimuthAnglesList.add(azimuthalAngle);
//                        elevationAnglesList.add(elevationAngle);

                        Point3d cc = SphericalCoordinates.toCartesian(Math.toRadians(azimuthalAngle), Math.toRadians(elevationAngle));
                        directionList.add(new Vector3f(cc));

                        azimuthalAngle += azimuthalStep;
                    }

                    azimuthalOffset += (azimuthalStep / 2.0f);
                }

            }

//            boolean lowAngle = false;
//            
//            
//            
//            //pour tous les tirs d'une plage angulaire
//            for (int s=0;s<shotNumberByRing[i];s++){
//                
//                //view cap filtrage
//                if(azimuthalAngle < minAzimuthAngle && azimuthalAngle > maxAzimuthAngle){
//                    //on filtre
//                }else{
//                    
//                    if(lowAngle){ //alternate lower angle and upper angle
//                        elevationAngle = ringInformation.getLowerShotAngle();
//                        lowAngle = false;
//                    }else{
//                        elevationAngle = ringInformation.getUpperShotAngle();
//                        lowAngle = true;
//                    }
//                    
//                    azimuthAnglesList.add(azimuthalAngle);
//                    elevationAnglesList.add(elevationAngle);
//
//                    SphericalCoordinates sphericalCoordinates = new SphericalCoordinates(
//                            (float)Math.toRadians(azimuthalAngle), (float)Math.toRadians(elevationAngle));
//
//                    directionList.add(sphericalCoordinates.toCartesian());
//                }
//                
//                azimuthalAngle += ringInformations[i].getAzimuthalStepAngle();
//                
//            }
        }

        directions = new Vector3f[directionList.size()];

        for (int i = 0; i < directionList.size(); i++) {
            directions[i] = directionList.get(i);
        }

//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/home/calcul/Documents/Julien/Test_transmittance_marilyne/directions.obj")));
//            
//            float length = 100;
//            for(Vector3f direction : directions){
//                
//                float x1 = 0, y1 = 0, z1 = 0;
//                
//                float x2 = x1+direction.x*length;
//                float y2 = y1+direction.y*length;
//                float z2 = z1+direction.z*length;
//                
//                writer.write("v "+ x1+" "+y1+" "+z1+"\n");
//                writer.write("v "+ x2+" "+y2+" "+z2+"\n");
//            }
//            
//            writer.close();
//        } catch (IOException ex) {
//            java.util.logging.Logger.getLogger(LAI2xxx.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    public Vector3f[] getDirections() {
        return directions;
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

    private class RingInformation {

        /**
         * angle tirs bas
         */
        private final float lowerShotAngle;

        /**
         * angle tirs haut
         */
        private final float upperShotAngle;

        /**
         * pas angulaire azimuthal
         */
        private final float azimuthalStepAngle;

        public final float[] subRingsAngles;
        public final float[] subRingsSolidAngles;
        public final int[] subRingsSamplingRate;
        public int nbSubRings = 0;

        public RingInformation(float lowerShotAngle, float upperShotAngle, float azimuthalStepAngle, int nbShots, float solidAngle) {

            float zenitalStep = (float) (Math.toRadians(lowerShotAngle) - Math.toRadians(upperShotAngle));
            float petitOmega = solidAngle / (float) nbShots;
            float twoAlpha = (float) Math.sqrt(petitOmega);
            //float alpha = (float) Math.acos(1-(petitOmega/(2*Math.PI)));
            nbSubRings = (int) (((zenitalStep) / (twoAlpha)) + 0.5);

            //float alpha = (float) Math.acos(1- ((solidAngle/(float)nbShots)/(2*Math.PI)));
            //nbSubRings = (int) (((Math.toRadians(lowerShotAngle) - Math.toRadians(upperShotAngle)) / (2 * alpha))+0.5);
            this.lowerShotAngle = lowerShotAngle;
            this.upperShotAngle = upperShotAngle;
            this.azimuthalStepAngle = azimuthalStepAngle;
            this.subRingsAngles = new float[nbSubRings];
            this.subRingsSolidAngles = new float[nbSubRings];
            this.subRingsSamplingRate = new int[nbSubRings];

            float oldUpperSubRingAngle = upperShotAngle;
            float step = (lowerShotAngle - upperShotAngle) / nbSubRings;

            for (int i = 0; i < subRingsAngles.length; i++) {

                float upperSubRingAngle = oldUpperSubRingAngle;

                float lowerSubRingAngle = upperSubRingAngle + step;
                oldUpperSubRingAngle = lowerSubRingAngle;

                subRingsAngles[i] = (lowerSubRingAngle + upperSubRingAngle) / 2;
                subRingsSolidAngles[i] = (float) (2 * Math.PI * (Math.cos(Math.toRadians(upperSubRingAngle)) - Math.cos(Math.toRadians(lowerSubRingAngle))));
                subRingsSamplingRate[i] = (int) (((subRingsSolidAngles[i] / solidAngle) * nbShots) + 0.5);
            }
        }

        public float getLowerShotAngle() {
            return lowerShotAngle;
        }

        public float getUpperShotAngle() {
            return upperShotAngle;
        }

        public float getAzimuthalStepAngle() {
            return azimuthalStepAngle;
        }
    }

    public Ring getRing(int ringID) {
        return rings[ringID];
    }

    public float[] getByPosition_LAI() {
        return byPosition_LAI;
    }

    public float[] getGapsByRing() {
        return gapsByRing;
    }

    public void setQuiet(boolean quiet) {

        if (quiet) {
            logger.setLevel(org.apache.log4j.Level.ERROR);
        } else {
            logger.setLevel(org.apache.log4j.Level.INFO);
        }
    }

//    //test
//    public float[][] getNormalizedTransmittances() {
//        return normalizedTransmittances;
//    }
//    //test
//    public float[][] getPathLengths() {
//        return pathLengths;
//    }
}
