/*
 * Copyright (C) 2016 UMR AMAP (botAnique et Modélisation de l'Architecture des Plantes et des végétations.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.amapvox.lidar.jlas;

import org.amapvox.commons.util.ByteConverter;

/**
 * Represents the structure of a las point, with the following basics
 * informations: x, y, z, echo range, echo number, recorded time (gps),
 * intensity, classification.
 * <p>
 * Warning : This class is used by the native library and should not be moved to
 * another package.</p>
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
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

    public LasPoint(int x, int y, int z, byte returnNumber, byte numberOfReturns, int intensity, byte classification, double gpsTime) {

        this.x = x;
        this.y = y;
        this.z = z;

        this.i = ByteConverter.unsignedShortToInteger(intensity);
        this.r = ByteConverter.unsignedByteToShort(returnNumber);
        this.n = ByteConverter.unsignedByteToShort(numberOfReturns);

        this.classification = classification & 0x1f; //récupération des 5 lower bits
        this.t = gpsTime;
    }

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
