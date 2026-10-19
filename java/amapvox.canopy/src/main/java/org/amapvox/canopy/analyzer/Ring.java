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

/**
 *
 * @author Julien Heurtebize
 */
public class Ring {

    private final float lowerZenithAngle;
    private final float upperZenithAngle;
    private final float viewAngle;
    private final float width;
    private float weightingFactor;
    private float weightingFactorPrime;
    private final float solidAngle;
    private int nbDirections;
    private float sumTrans;
    private float acfs;
    private float cntct;
    private float stdev;
    private float dist;
    private float gap;
    private final boolean masked;

    public Ring(float lowerZenithalAngle, float upperZenithalAngle, float viewAngle, boolean masked, float weightingFactor, float weightingFactorPrime) {

        this.lowerZenithAngle = lowerZenithalAngle;
        this.upperZenithAngle = upperZenithalAngle;
        this.masked = masked;

        //this.viewAngle = (int)((lowerZenithalAngle+upperZenithalAngle) / 2.0f);
        this.viewAngle = viewAngle;

        this.width = (float) (Math.toRadians(lowerZenithalAngle) - Math.toRadians(upperZenithalAngle));
        this.weightingFactor = weightingFactor;
        this.weightingFactorPrime = weightingFactorPrime;
        this.dist = (float) (1 / Math.cos(Math.toRadians(this.viewAngle)));

        // Compute ring solid angle
        solidAngle = (float) (2 * Math.PI * (Math.cos(Math.toRadians(upperZenithalAngle)) - Math.cos(Math.toRadians(lowerZenithalAngle))));
    }

    public float getLowerZenithalAngle() {
        return lowerZenithAngle;
    }

    public float getUpperZenithalAngle() {
        return upperZenithAngle;
    }

    public float getSolidAngle() {
        return solidAngle;
    }

    public float getViewAngle() {
        return viewAngle;
    }

    public float getAvgtrans() {

        return sumTrans / nbDirections;
    }

    public float getAcfs() {
        return acfs;
    }

    public float getCntct() {
        return cntct;
    }

    public float getStdev() {
        return stdev;
    }

    public float getDist() {
        return dist;
    }

    public float getGap() {
        return gap;
    }

    public void setTrans(float transmittance) {

        if (!Float.isNaN(transmittance)) {
            this.sumTrans += transmittance;
        }
    }

    public void setAcfs(float acfs) {
        this.acfs = acfs;
    }

    public void setCntct(float cntct) {
        this.cntct = cntct;
    }

    public void setStdev(float stdev) {
        this.stdev = stdev;
    }

    public void setDist(float dist) {
        this.dist = dist;
    }

    public void setGap(float gap) {
        this.gap = gap;
    }

    public void setNbDirections(int nbDirections) {
        this.nbDirections = nbDirections;
    }

    public float getWidth() {
        return width;
    }

    public float getWeightingFactor() {
        return weightingFactor;
    }

    public boolean isMasked() {
        return masked;
    }

    public void setWeightingFactor(float weightingFactor) {
        this.weightingFactor = weightingFactor;
    }

    public float getWeightingFactorPrime() {
        return weightingFactorPrime;
    }

    public void setWeightingFactorPrime(float weightingFactorPrime) {
        this.weightingFactorPrime = weightingFactorPrime;
    }

    /**
     * Divides a ring into zenithal bands with directions distributed
     * proportionally to their solid angles.
     *
     * @param ringDirectionCount Total directions allocated to this ring.
     *
     * @return Array of zenithal bands, each with its central zenith angle and
     * azimuthal direction count.
     */
    public ZenithalBand[] createZenithalBands(int ringDirectionCount) {

        // Zenithal width of this ring
        double zenitalStep = Math.toRadians(lowerZenithAngle) - Math.toRadians(upperZenithAngle);
        // Solid angle per direction (ring's total solid angle divided by its direction count)
        double omega = solidAngle / (float) ringDirectionCount;
        // Approximate apex angle (2α) for a cone with solid angle ω: 2α ≈ √ω (small-angle approximation)
        // omega = 2 * PI * (1 - cos(alpha)) 
        // alpha = acos(1 - (omega / (2 * PI)));
        // for alpha very small, cos series expansion gives 1 - cos(alpha) ~= alpha^2 / 2
        // hence alpha ~= sqrt(omega / PI) ~= sqrt(omega) / 0.5 whith 1 / sqrt(PI) ~= 0.5
        double twoAlpha = Math.sqrt(omega);
        // Number of zenithal bands for even zenithal sampling of the ring solid angle
        int bandCount = (int) Math.ceil(zenitalStep / twoAlpha);

        ZenithalBand[] zenithalBands = new ZenithalBand[bandCount];

        float previousUpperZenithAngle = upperZenithAngle;
        float step = (lowerZenithAngle - upperZenithAngle) / bandCount;

        for (int bandIndex = 0; bandIndex < bandCount; bandIndex++) {

            // compute central subring angle
            float bandUpperZenithAngle = previousUpperZenithAngle;
            float bandLowerZenithAngle = bandUpperZenithAngle + step;
            previousUpperZenithAngle = bandLowerZenithAngle;
            float bandZenithAngle = (bandLowerZenithAngle + bandUpperZenithAngle) / 2;

            // compute this band solid angle
            double bandSolidAngle = (2 * Math.PI * (Math.cos(Math.toRadians(bandUpperZenithAngle)) - Math.cos(Math.toRadians(bandLowerZenithAngle))));

            // distribute azimuthal directions proportionaly to this band solid angle
            int bandAzimuthDirectionCount = (int) (((bandSolidAngle / solidAngle) * ringDirectionCount) + 0.5);

            zenithalBands[bandIndex] = new ZenithalBand(bandZenithAngle, bandAzimuthDirectionCount);
        }

        return zenithalBands;
    }

    /**
     * Represents a zenithal band within a ring, with a fixed zenith angle and a
     * proportional number of azimuthal directions for ray casting.
     * 
     * @author Philippe Verley
     */
    public class ZenithalBand {

        private final float zenithAngle;

        private final int azimuthDirectionCount;

        public ZenithalBand(float zenithAngle, int azimuthDirectionCount) {
            this.zenithAngle = zenithAngle;
            this.azimuthDirectionCount = azimuthDirectionCount;
        }

        /**
         * @return the band zenith angle
         */
        public float getZenithAngle() {
            return zenithAngle;
        }

        /**
         * @return the number of directions for azimuthal
         */
        public int getAzimuthDirectionCount() {
            return azimuthDirectionCount;
        }
    }
}
