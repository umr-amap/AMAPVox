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
package org.amapvox.lidar.leica.ptx;

import org.amapvox.lidar.gridded.PointScanHeader;
import javax.vecmath.Point3d;

/**
 * This class represents the header of a scan into a ptx file (a ptx file may
 * have multiple scans).
 *
 * @author Julien Heurtebize
 */
public class PTXHeader extends PointScanHeader {

    private Point3d scannerRegisteredPosition;
    private Point3d scannerRegisteredAxisX;
    private Point3d scannerRegisteredAxisY;
    private Point3d scannerRegisteredAxisZ;

    /**
     * Get the scanner registered position.
     *
     * @return The scanner registered position as a 3d point
     */
    public Point3d getScannerRegisteredPosition() {
        return scannerRegisteredPosition;
    }

    /**
     * Set the scanner registered position.
     *
     * @param scannerRegisteredPosition The scanner registered position as a 3d
     * point
     */
    public void setScannerRegisteredPosition(Point3d scannerRegisteredPosition) {
        this.scannerRegisteredPosition = scannerRegisteredPosition;
    }

    /**
     * Get the scanner registered axis X
     *
     * @return The scanner registered axis X as a 3d point
     */
    public Point3d getScannerRegisteredAxisX() {
        return scannerRegisteredAxisX;
    }

    /**
     * Set the scanner registered axis X
     *
     * @param scannerRegisteredAxisX The scanner registered axis X as a 3d point
     */
    public void setScannerRegisteredAxisX(Point3d scannerRegisteredAxisX) {
        this.scannerRegisteredAxisX = scannerRegisteredAxisX;
    }

    /**
     * Get the scanner registered axis Y
     *
     * @return The scanner registered axis Y as a 3d point
     */
    public Point3d getScannerRegisteredAxisY() {
        return scannerRegisteredAxisY;
    }

    /**
     * Set the scanner registered axis Y
     *
     * @param scannerRegisteredAxisY The scanner registered axis Y as a 3d point
     */
    public void setScannerRegisteredAxisY(Point3d scannerRegisteredAxisY) {
        this.scannerRegisteredAxisY = scannerRegisteredAxisY;
    }

    /**
     * Get the scanner registered axis Z
     *
     * @return The scanner registered axis Z as a 3d point
     */
    public Point3d getScannerRegisteredAxisZ() {
        return scannerRegisteredAxisZ;
    }

    /**
     * Set the scanner registered axis Z
     *
     * @param scannerRegisteredAxisZ The scanner registered axis Z as a 3d point
     */
    public void setScannerRegisteredAxisZ(Point3d scannerRegisteredAxisZ) {
        this.scannerRegisteredAxisZ = scannerRegisteredAxisZ;
    }
}
