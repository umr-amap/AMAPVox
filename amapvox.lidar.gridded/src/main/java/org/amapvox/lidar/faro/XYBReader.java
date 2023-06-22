/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.faro;

import org.amapvox.lidar.commons.LidarScan;
import org.amapvox.lidar.commons.LidarScanReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.vecmath.Matrix4d;

/**
 *
 * @author pverley
 */
public class XYBReader implements LidarScanReader {

    @Override
    public LidarScan toLidarScan(File file) throws FileNotFoundException, IOException {
        XYBScan scan = new XYBScan(file);
        scan.readHeader();
        return new LidarScan(scan.getFile(), new Matrix4d(scan.getHeader().getTransfMatrix()));

    }

}
