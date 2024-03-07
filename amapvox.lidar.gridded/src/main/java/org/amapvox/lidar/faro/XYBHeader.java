/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in
the editor.
 */
package org.amapvox.lidar.faro;

import org.amapvox.lidar.gridded.PointScanHeader;
import javax.vecmath.Point3d;

/**
 *
 * @author pverley
 */
public class XYBHeader extends PointScanHeader {

    /**
     * Scanner position
     */
    private Point3d scannerPosition;
    /**
     * Size of header expressed in number of bytes
     */
    private int nByte;

    /**
     * @return the scannerPosition
     */
    public Point3d getScannerPosition() {
        return scannerPosition;
    }

    /**
     * @param scannerPosition the scannerPosition to set
     */
    public void setScannerPosition(Point3d scannerPosition) {
        this.scannerPosition = scannerPosition;
    }

    /**
     * Size of header, in number of bytes
     *
     * @return the size of the header in number of bytes
     */
    public int getSize() {
        return nByte;
    }
    
    public void setSize(int nByte) {
        this.nByte = nByte;
    }

}
