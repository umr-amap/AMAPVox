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
import org.amapvox.lidar.gridded.LEmptyPoint;
import org.amapvox.lidar.gridded.LPoint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * <li>{@link #setUpColumnToRead(int) setUpColumnToRead(int columnIndex)},</li>
 * <li>{@link #setUpColumnsToRead(int, int) setUpColumnsToRead(int startColumnIndex, int endColumnIndex)},</li>
 * <li>{@link #setUpRowToRead(int) setUpRowToRead(int rowIndex)},</li>
 * <li>{@link #setUpRowsToRead(int, int) setUpRowsToRead(int startRowIndex, int endRowIndex)}</li>
 * </ul>
 * <p>
 * The ptx scan file may contains multiple scans</p>
 *
 * @author Julien Heurtebize
 */
public class PTXScan extends GriddedPointScan {

    private long currentLineIndex;

    private static final int HEADER_SIZE = 10;

    /**
     * The line indice representing offset to point data record of the scan.
     */
    public long offset;

    /**
     * The number of points of the scan, computed as (columns number) * (rows
     * number), which includes invalid points.
     */
    public long nbPoints;

    /**
     * Initialize a new PTXScan, meaning a single scan into the file
     *
     * @param file The ptx file
     * @param header Header of the specific scan
     * @param offset Offset to point data record into the file
     */
    public PTXScan(File file, PTXHeader header, long offset) {

        super();

        this.file = file;
        this.header = header;
        this.offset = offset;
        this.nbPoints = header.getNumCols() * header.getNumRows();

        startRowIndex = 0;
        endRowIndex = header.getNumRows() - 1;

        startColumnIndex = 0;
        endColumnIndex = header.getNumCols() - 1;
    }

    private void skipLines(BufferedReader reader, long nbLinesToSkip) throws IOException {

        int nbLinesSkipped = 0;

        while (nbLinesSkipped < nbLinesToSkip) {

            reader.readLine();
            nbLinesSkipped++;
        }
    }

    /**
     * Returns an iterator to get points from the scan file as a
     * {@link org.amapvox.lidar.gridded.LPoint} As the ptx scan file is a
     * gridded point format, you can select the row, columns you want to read
     * with the following methods :
     * <ul>
     * <li>{@link #setUpColumnToRead(int) setUpColumnToRead(int columnIndex)},</li>
     * <li>{@link #setUpColumnsToRead(int, int) setUpColumnsToRead(int startColumnIndex, int endColumnIndex)},</li>
     * <li>{@link #setUpRowToRead(int) setUpRowToRead(int rowIndex)},</li>
     * <li>{@link #setUpRowsToRead(int, int) setUpRowsToRead(int startRowIndex, int endRowIndex)}</li>
     * </ul>
     * All those methods should be called before to get the iterator.
     *
     * @return A {@link LPoint} point returned by the iterator.
     */
    @Override
    public Iterator<LPoint> iterator() {

        Iterator it = null;
        final BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(file));

            it = new Iterator<LPoint>() {

                LPoint currentPoint;
                long totalLinesRead = 0;

                int currentColumnIndex = 0;
                int currentRowIndex = -1;

                final int finalColumnIndex = endColumnIndex; //end column index is included
                final int finalRowIndex = endRowIndex; //end row index is included

                boolean initialized = false;
                boolean isFinish = false;

                private String readLine(BufferedReader reader) throws IOException {

                    String line = reader.readLine();

                    currentRowIndex++;
                    totalLinesRead++;

                    if (currentRowIndex >= header.getNumRows()) {
                        currentRowIndex = 0;
                        currentColumnIndex++;
                    }

                    return line;
                }

                @Override
                public boolean hasNext() {

                    try {

                        LPoint point;

                        if (!initialized) {

                            skipLines(reader, offset);

                            initialized = true;
                        }

                        if (totalLinesRead == nbPoints) {

                            reader.close();
                            return false;
                        }

                        String currentLine;

                        do {
                            if (isFinish) {
                                reader.close();
                                return false;
                            }

                            currentLine = readLine(reader);

                            if (currentLine == null) {
                                reader.close();
                                return false;
                            }

                            if (currentColumnIndex >= finalColumnIndex && (currentRowIndex) >= finalRowIndex) {
                                isFinish = true;
                            }

                        } while ((currentRowIndex) < startRowIndex
                                || (currentRowIndex) > finalRowIndex
                                || currentColumnIndex < startColumnIndex);

                        String[] split = currentLine.split(" ");

                        point = new LDoublePoint();

                        ((LDoublePoint) point).x = Double.valueOf(split[0]);
                        ((LDoublePoint) point).y = Double.valueOf(split[1]);
                        ((LDoublePoint) point).z = Double.valueOf(split[2]);

                        if (((LDoublePoint) point).x == 0 && ((LDoublePoint) point).y == 0 && ((LDoublePoint) point).z == 0) {

                            if (!returnInvalidPoint) {
                                hasNext();
                            } else {

                                point = new LEmptyPoint();

                                point.rowIndex = currentRowIndex;
                                point.columnIndex = currentColumnIndex;

                                point.valid = false;

                                currentPoint = point;
                            }
                        } else {
                            point.valid = true;

                            point.rowIndex = currentRowIndex;
                            point.columnIndex = currentColumnIndex;

                            if (split.length > 3) {

                                point.intensity = Float.valueOf(split[3]);

                                if (split.length > 6) {
                                    point.red = Integer.valueOf(split[4]);
                                    point.green = Integer.valueOf(split[5]);
                                    point.blue = Integer.valueOf(split[6]);
                                }
                            }

                            currentPoint = point;
                        }

                    } catch (IOException | ArrayIndexOutOfBoundsException | NumberFormatException e) {
                        try {
                            reader.close();
                        } catch (IOException ex) {
                            Logger.getLogger(PTXScan.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        return false;
                    }

                    return true;
                }

                @Override
                public LPoint next() {
                    return currentPoint;
                }
            };

        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }

        return it;
    }

    @Override
    public void openScanFile(File file) throws FileNotFoundException, IOException, Exception {

        //test file existence
        FileReader reader = new FileReader(file);
        reader.close();
    }

    @Override
    public PTXHeader getHeader() {
        return (PTXHeader) header;
    }

}
