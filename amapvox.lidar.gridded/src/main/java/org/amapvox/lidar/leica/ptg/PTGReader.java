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
package org.amapvox.lidar.leica.ptg;

import org.amapvox.lidar.commons.LidarScan;
import org.amapvox.lidar.commons.LidarScanReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.vecmath.Matrix4d;

/**
 * <p>
 * This class is dedicated to handle PTG files, a Leica gridded point format
 * (see <a href= "http://www.xdesy.de/freeware/PTG-DLL/PTG-1.0.pdf">
 * specification</a>)</p>
 * <p>
 * You can use this class to get the scan list from the ascii ptg file or use it
 * to determines if the input file is the ascii file or a binary file. If you
 * already know that the type of the input file is a binary scan file, you can
 * use the class {@link PTGScan PTGScan}.</p>
 *
 * @author Julien Heurtebize
 */
public class PTGReader implements LidarScanReader {

    @Override
    public LidarScan toLidarScan(File file) throws FileNotFoundException, IOException {

        PTGScan scan = new PTGScan(file);
        scan.readHeader();
        return new LidarScan(scan.getFile(), new Matrix4d(scan.getHeader().getTransfMatrix()));
    }
}
