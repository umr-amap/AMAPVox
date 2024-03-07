/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation.las;

/**
 *
 * @author pverley
 */
public class LasPoint implements Comparable<LasPoint> {

    public final static int CLASSIFICATION_CREATED_NEVER_CLASSIFIED = 0;
    public final static int CLASSIFICATION_UNCLASSIFIED = 1;
    public final static int CLASSIFICATION_GROUND = 2;
    public final static int CLASSIFICATION_LOW_VEGETATION = 3;
    public final static int CLASSIFICATION_MEDIUM_VEGETATION = 4;
    public final static int CLASSIFICATION_HIGH_VEGETATION = 5;
    public final static int CLASSIFICATION_BUILDING = 6;
    public final static int CLASSIFICATION_LOW_POINT = 7;
    public final static int CLASSIFICATION_MODEL_KEY_POINT = 8;
    public final static int CLASSIFICATION_WATER = 9;

    /**
     * las point location
     */
    public double x;
    public double y;
    public double z;

    /**
     * echo range
     */
    public int r;

    /**
     * echo number
     */
    public int n;

    /**
     * recorded time
     */
    public double t;

    /**
     * intensity
     */
    public int i;

    /**
     * classification (ground = 2, unclassified = 1)
     */
    public int classification;

    public boolean flawed = false;

    public LasPoint(double x, double y, double z, int r, int n, int i, short classification, double t) {

        this.x = x;
        this.y = y;
        this.z = z;

        this.r = r;
        this.n = n;
        this.classification = classification;
        this.i = i;
        this.t = t;
    }

    /**
     * Compare gps time values
     *
     * @param point point to compare
     */
    @Override
    public int compareTo(LasPoint point) {

        if (point.t > this.t) {
            return -1;
        } else if (point.t < this.t) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("LAS point x ").append((float) x)
                .append(" y ").append((float) y)
                .append(" z ").append((float) z)
                .append(" rank ").append(r)
                .append(" nEcho ").append(n)
                .append(" time ").append(t)
                .append(" classification ").append(classification);
        return sb.toString();
    }
}
