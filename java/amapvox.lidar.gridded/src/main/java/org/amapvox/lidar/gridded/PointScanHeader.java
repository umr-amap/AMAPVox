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
package org.amapvox.lidar.gridded;

import javax.vecmath.Matrix4d;

/**
 * Basic header for a gridded point scan.
 *
 * @author Julien Heurtebize
 */
public class PointScanHeader {

    /**
     * Number of zenithal pulses in the scan
     */
    private int nzenith;

    /**
     * Number of azimuthal pulses in the scan
     */
    private int nazimuth;

    /**
     * 4x4 transformation matrix, the default value is identity.
     * <p>
     * It can be another value after the scan have been registered</p>
     */
    private Matrix4d transfMatrix;

    /**
     * Are points stored in float format (4 bytes)?
     */
    private boolean pointInFloatFormat;

    /**
     * Are points stored in double format (8 bytes)?
     */
    private boolean pointInDoubleFormat;

    /**
     * Are points have intensity value?
     */
    private boolean pointContainsIntensity;

    /**
     * Are points have color values (red-green-blue)?
     */
    private boolean pointContainsRGB;

    /**
     * Size of a point in bytes, including positions and other attributes
     */
    private int pointSize;

    public PointScanHeader() {
        transfMatrix = new Matrix4d();
        transfMatrix.setIdentity();
    }

    /**
     * Get number of zenithal pulses in the scan. Vertical sweep.
     *
     * @return The number of pulses in the zenithal sweep.
     */
    public int getNZenith() {
        return nzenith;
    }

    /**
     * Set number of zenithal pulses in the scan. Vertical sweep.
     *
     * @param nzenith The number of pulses in the zenithal sweep.
     */
    public void setNZenith(int nzenith) {
        this.nzenith = nzenith;
    }

    /**
     * Get number of azimuthal pulses in the scan. Horizontal rotation.
     *
     * @return The number of pulses in the azimuthal rotation.
     */
    public int getNAzimuth() {
        return nazimuth;
    }

    /**
     * Set number of azimuthal pulses in the scan. Horizontal rotation.
     *
     * @param nazimuth The number of pulses in the azimuthal rotation.
     */
    public void setNAzimuth(int nazimuth) {
        this.nazimuth = nazimuth;
    }

    /**
     * Get the 4x4 transformation matrix of the scan {@link #transfMatrix}
     *
     * @return The transformation matrix.
     */
    public Matrix4d getTransfMatrix() {
        return transfMatrix;
    }

    /**
     * Set the 4x4 transformation matrix of the scan {@link #transfMatrix}
     *
     * @param transfMatrix The transformation matrix.
     */
    public void setTransfMatrix(Matrix4d transfMatrix) {
        this.transfMatrix = transfMatrix;
    }

    /**
     * Is point in float format {@link #pointInFloatFormat}
     *
     * @return true if point is in float format, false otherwise
     */
    public boolean isPointInFloatFormat() {
        return pointInFloatFormat;
    }

    /**
     * Set point format storage to float {@link #pointInFloatFormat}
     *
     * @param pointInFloatFormat
     */
    public void setPointInFloatFormat(boolean pointInFloatFormat) {
        this.pointInFloatFormat = pointInFloatFormat;
    }

    /**
     * Is point in double format {@link #pointInDoubleFormat}
     *
     * @return true if point is in double format, false otherwise
     */
    public boolean isPointInDoubleFormat() {
        return pointInDoubleFormat;
    }

    /**
     * Set point format storage to double {@link #pointInDoubleFormat}
     *
     * @param pointInDoubleFormat
     */
    public void setPointInDoubleFormat(boolean pointInDoubleFormat) {
        this.pointInDoubleFormat = pointInDoubleFormat;
    }

    /**
     * Does point contain intensity attribute ? {@link #pointContainsIntensity}
     *
     * @return true if point contains intensity value, false otherwise
     */
    public boolean isPointContainsIntensity() {
        return pointContainsIntensity;
    }

    /**
     * Set whether point format contains intensity attribute.
     *
     * @param pointContainsIntensity
     */
    public void setPointContainsIntensity(boolean pointContainsIntensity) {
        this.pointContainsIntensity = pointContainsIntensity;
    }

    /**
     * Does point contain RGB attribute ? {@link #pointContainsRGB}
     *
     * @return true if point contains RGB value, false otherwise
     */
    public boolean isPointContainsRGB() {
        return pointContainsRGB;
    }

    /**
     * Set whether point format contains RGB attribute
     *
     * @param pointContainsRGB
     */
    public void setPointContainsRGB(boolean pointContainsRGB) {
        this.pointContainsRGB = pointContainsRGB;
    }

    /**
     * Get size of a point ({@link #pointSize})
     *
     * @return The size (in bytes) of a point
     */
    public int getPointSize() {
        return pointSize;
    }

    /**
     * Update point size based on current defined attributes and storage format
     */
    public void updatePointSize() {

        pointSize = 0;

        if (isPointContainsIntensity()) {
            pointSize += 4;
        }

        if (isPointContainsRGB()) {
            pointSize += 3;
        }

        if (pointInDoubleFormat) {
            pointSize += 24;
        } else {
            pointSize += 12;
        }
    }

    @Override
    public String toString() {

        return "Transformation matrix:\n" + transfMatrix.toString()
                + "Number of pulses in vertical sweep:\t\t" + nzenith + "\n"
                + "Number of pulses in horizontal rotation:\t\t" + nazimuth + "\n"
                + "Point storage format:\t" + ((isPointInDoubleFormat()) ? "Double" : "Float") + "\n"
                + "Contains intensity:\t" + ((isPointContainsIntensity()) ? "Yes" : "No") + "\n"
                + "Contains RGB:\t\t" + ((isPointContainsRGB()) ? "Yes" : "No");
    }
}
