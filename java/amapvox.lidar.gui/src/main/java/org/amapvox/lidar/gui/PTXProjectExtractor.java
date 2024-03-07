/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.gui;

import org.amapvox.lidar.commons.LidarProjectReader;
import org.amapvox.lidar.leica.ptx.PTXReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author calcul
 */
public class PTXProjectExtractor extends LidarProjectExtractor {

    @Override
    public LidarProjectReader getReader(File file) throws FileNotFoundException, IOException {
        return new PTXReader(file);
    }
}
