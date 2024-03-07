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
     * Number of columns in the scan
     */
    protected int numCols;

    /**
     * Number of rows in the scan
     */
    protected int numRows;

    /**
     * 4x4 transformation matrix, the default value is identity,
     * <p>
     * It can be another value after the scan have been registered</p>
     */
    protected Matrix4d transfMatrix;

    /**
     * Are points stored in float format (4 bytes)?
     */
    protected boolean pointInFloatFormat;

    /**
     * Are points stored in double format (8 bytes)?
     */
    protected boolean pointInDoubleFormat;

    /**
     * Are points have intensity value?
     */
    protected boolean pointContainsIntensity;

    /**
     * Are points have color values (red-green-blue)?
     */
    protected boolean pointContainsRGB;

    /**
     * Size of a point in bytes, including positions and other attributes
     */
    protected int pointSize;

    public PointScanHeader() {
        transfMatrix = new Matrix4d();
        transfMatrix.setIdentity();
    }

    /**
     * Get number of columns of the scan.
     *
     * @return The number of columns.
     */
    public int getNumCols() {
        return numCols;
    }

    /**
     * Set number of columns of the scan.
     *
     * @param numCols The number of columns
     */
    public void setNumCols(int numCols) {
        this.numCols = numCols;
    }

    /**
     * Get number of rows of the scan
     *
     * @return The number of rows
     */
    public int getNumRows() {
        return numRows;
    }

    /**
     * Set number of rows of the scan
     *
     * @param numRows The number of rows
     */
    public void setNumRows(int numRows) {
        this.numRows = numRows;
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
     * Is point is in float format {@link #pointInFloatFormat}
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
     * Is point is in double format {@link #pointInDoubleFormat}
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
     * Is point contains intensity attribute ? {@link #pointContainsIntensity}
     *
     * @return true if point contains intensity value, false otherwise
     */
    public boolean isPointContainsIntensity() {
        return pointContainsIntensity;
    }

    /**
     * Set that point format contains intensity attribute
     *
     * @param pointContainsIntensity
     */
    public void setPointContainsIntensity(boolean pointContainsIntensity) {
        this.pointContainsIntensity = pointContainsIntensity;
    }

    /**
     * Is point contains rgb attribute ? {@link #pointContainsRGB}
     *
     * @return true if point contains rgb value, false otherwise
     */
    public boolean isPointContainsRGB() {
        return pointContainsRGB;
    }

    /**
     * Set that point format contains rgb attribute
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
     * Update point size based on current defined atributes and storage format
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
                + "Column number:\t\t" + numCols + "\n"
                + "Row number:\t\t" + numRows + "\n"
                + "Point storage format:\t" + ((isPointInDoubleFormat()) ? "Double" : "Float") + "\n"
                + "Contains intensity:\t" + ((isPointContainsIntensity()) ? "Yes" : "No") + "\n"
                + "Contains RGB:\t\t" + ((isPointContainsRGB()) ? "Yes" : "No");
    }
}
