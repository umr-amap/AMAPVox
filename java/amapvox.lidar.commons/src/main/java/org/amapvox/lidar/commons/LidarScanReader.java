/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.commons;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author pverley
 */
public interface LidarScanReader {

    public LidarScan toLidarScan(File file) throws FileNotFoundException, IOException;

}
