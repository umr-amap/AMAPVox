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

import org.amapvox.lidar.gridded.GriddedPointScan;
import org.amapvox.lidar.gridded.LDoublePoint;
import org.amapvox.lidar.gridded.LPoint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * <p>
 * This class is dedicated to handle PTX ascii scan file, a Leica gridded point
 * format (see
 * <a href= "http://www.geodetawlkp.nazwa.pl/instrukcje/leica_hds/Baza_wiedzy_HDS/Cyclone/Cyclone_pointcloud_export_format_-_Description_of_ASCII_.ptx_format.pdf">
 * specification</a>)</p>
 * <p>
 * It provides a simple iterator to get points from the file.</p>
 * As the ptx scan file is a gridded point format, you can select the row,
 * columns you want to read with the following methods :
 * <ul>
 * <li>{@link #setAzimuthIndex(int) setUpColumnToRead(int columnIndex)},</li>
 * <li>{@link #setAzimuthRange(int, int) setUpColumnsToRead(int startColumnIndex, int endColumnIndex)},</li>
 * <li>{@link #setZenithIndex(int) setUpRowToRead(int rowIndex)},</li>
 * <li>{@link #setZenithRange(int, int) setUpRowsToRead(int startRowIndex, int endRowIndex)}</li>
 * </ul>
 * <p>
 * The ptx scan file may contains multiple scans</p>
 *
 * @author Julien Heurtebize
 */
public class PTXScan extends GriddedPointScan {

    // offset to scan point data in PTX file
    final private long offset;

    /**
     * Initialize a new PTXScan, meaning a single scan into the file
     *
     * @param file The PTX file
     * @param header Header of the specific scan
     * @param offset Offset to point data record into the file
     */
    public PTXScan(File file, PTXHeader header, long offset) {

        super(file);

        this.header = header;
        this.offset = offset;
        this.returnMissingPoint = true;

        points = new LPoint[header.getNAzimuth()][header.getNZenith()];

        startZenithIndex = 0;
        endZenithIndex = header.getNAzimuth() - 1;

        startAzimuthIndex = 0;
        endAzimuthIndex = header.getNZenith() - 1;
    }

    public long getOffset() {
        return offset;
    }

    private void skipLines(BufferedReader reader, long nbLinesToSkip) throws IOException {

        int nbLinesSkipped = 0;

        while (nbLinesSkipped < nbLinesToSkip) {

            reader.readLine();
            nbLinesSkipped++;
        }
    }

    @Override
    public void readHeader() throws FileNotFoundException, IOException {
        // does nothing, header is passed in constructor
    }

    @Override
    public void readPointCloud() throws FileNotFoundException, IOException {

        try (BufferedReader reader = new BufferedReader(new FileReader(getFile()))) {
            // skip previous scans and current scan header
            skipLines(reader, offset);
            // init line, row and col index
            int nline = header.getNZenith() * header.getNAzimuth();
            int iline = 0;
            int iazimuth = 0;
            int izenith = -1;
            // loop over lines
            String line;
            while ((line = reader.readLine()) != null && iline < nline) {
                izenith++;
                iline++;
                if (izenith >= header.getNZenith()) {
                    izenith = 0;
                    iazimuth++;
                }
                String[] split = line.split(" ");
                LDoublePoint point = new LDoublePoint();
                point.x = Double.parseDouble(split[0]);
                point.y = Double.parseDouble(split[1]);
                point.z = Double.parseDouble(split[2]);
                point.valid = !(point.x == 0 && point.y == 0 && point.z == 0);
                // only fill valid points
                if (point.valid) {
                    point.azimuthIndex = iazimuth;
                    point.zenithIndex = izenith;
                    if (split.length > 3) {
                        point.intensity = Float.parseFloat(split[3]);
                        if (split.length > 6) {
                            point.red = Integer.parseInt(split[4]);
                            point.green = Integer.parseInt(split[5]);
                            point.blue = Integer.parseInt(split[6]);
                        }
                    }
                    points[iazimuth][izenith] = point;
                }
            }
        }
    }

    @Override
    public PTXHeader getHeader() {
        return (PTXHeader) header;
    }
}
