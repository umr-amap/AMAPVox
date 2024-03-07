/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.leica.ptx;

import org.amapvox.lidar.commons.LidarScan;
import java.io.File;
import javax.vecmath.Matrix4d;

/**
 *
 * @author calcul
 */
public class PTXLidarScan extends LidarScan {

    private final PTXScan scan;
    private final int scanIndex;

    public PTXLidarScan(File file, Matrix4d matrix, PTXScan scan, int scanIndex) {
        super(file, matrix, file.getName() + "-scan-" + scanIndex);
        this.scan = scan;
        this.scanIndex = scanIndex;
    }

    public PTXScan getScan() {
        return scan;
    }

    public int getScanIndex() {
        return scanIndex;
    }

}
