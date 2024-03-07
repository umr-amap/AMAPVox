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

/**
 *
 * @author calcul
 */


public class Ring {
    
    private final float lowerZenithalAngle;
    private final float upperZenithalAngle;
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
    private boolean masked;
    

    public Ring(float lowerZenithalAngle, float upperZenithalAngle, float viewAngle, boolean masked, float weightingFactor, float weightingFactorPrime) {
        
        this.lowerZenithalAngle = lowerZenithalAngle;
        this.upperZenithalAngle = upperZenithalAngle;
        this.masked = masked;
        
        //this.viewAngle = (int)((lowerZenithalAngle+upperZenithalAngle) / 2.0f);
        this.viewAngle = viewAngle;
        
        this.width = (float) (Math.toRadians(lowerZenithalAngle) - Math.toRadians(upperZenithalAngle));
        this.weightingFactor = weightingFactor;
        this.weightingFactorPrime = weightingFactorPrime;
        this.dist = (float) (1/Math.cos(Math.toRadians(this.viewAngle)));
        
        //calcul de l'angle solide
        solidAngle = (float) (2* Math.PI * (Math.cos(Math.toRadians(upperZenithalAngle)) - Math.cos(Math.toRadians(lowerZenithalAngle))));
    }   

    public float getLowerZenithalAngle() {
        return lowerZenithalAngle;
    }

    public float getUpperZenithalAngle() {
        return upperZenithalAngle;
    }    

    public float getSolidAngle() {
        return solidAngle;
    }

    public float getViewAngle() {
        return viewAngle;
    }

    public float getAvgtrans() {
        
        return sumTrans/nbDirections;
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
        
        if(!Float.isNaN(transmittance)){
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
}
