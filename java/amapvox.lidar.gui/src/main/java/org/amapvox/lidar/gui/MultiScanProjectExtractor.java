/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.gui;

import org.amapvox.lidar.commons.LidarProjectReader;
import org.amapvox.lidar.commons.LidarScanReader;
import org.amapvox.lidar.commons.MultiScanProjectReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author pverley
 */
public class MultiScanProjectExtractor extends LidarProjectExtractor {

    private final String fileExt;
    private final LidarScanReader scanReader;

    public MultiScanProjectExtractor(String fileExt, LidarScanReader scanReader) {
        this.fileExt = fileExt;
        this.scanReader = scanReader;
    }

    @Override
    public LidarProjectReader getReader(File file) throws FileNotFoundException, IOException {
        return new MultiScanProjectReader(file, fileExt, scanReader);
    }

}
