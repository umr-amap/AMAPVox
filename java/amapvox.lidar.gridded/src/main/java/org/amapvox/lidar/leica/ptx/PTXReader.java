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

import org.amapvox.commons.util.IteratorWithException;
import org.amapvox.commons.util.TimeCounter;
import org.amapvox.lidar.commons.LidarProjectReader;
import org.amapvox.lidar.commons.LidarScan;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

/**
 *
 * @author Julien Heurtebize, Philippe Verley
 */
public class PTXReader extends LidarProjectReader {

    private long iline;
    private boolean cancelled = false;

    public PTXReader(File file) throws FileNotFoundException, IOException {
        super(file);
    }

    @Override
    public IteratorWithException<LidarScan> iterator() throws FileNotFoundException, IOException {
        iline = 0L;
        return new PTXScanIterator(new FileReader(getFile()));
    }

    private class PTXScanIterator implements IteratorWithException<LidarScan> {

        final BufferedReader reader;
        private int iscan;
        private PTXScan nextScan;

        PTXScanIterator(FileReader freader) {
            this.reader = new BufferedReader(freader);
            nextScan = readSingleScanHeader(this.reader);
        }

        @Override
        public boolean hasNext() throws Exception {
            return !cancelled && nextScan != null;
        }

        @Override
        public LidarScan next() throws Exception {

            PTXScan scan = nextScan;
            if (null != scan) {
                // move to next scan
                long npoint = scan.getHeader().getNZenith() * scan.getHeader().getNAzimuth();
                long ilineSkipped = 0;
                while (!cancelled
                        && (ilineSkipped < npoint)
                        && (null != reader.readLine())) {
                    fireProgress("Loading PTX scan " + iscan, ilineSkipped, npoint);
                    ilineSkipped += 1L;
                }
                iscan++;
                iline += npoint;
                // read next scan header
                nextScan = readSingleScanHeader(reader);
                // end of PTX file
                if (null == nextScan) {
                    reader.close();
                }
            }
            // return current scan
            return (null != scan)
                    ? new PTXLidarScan(scan.getFile(), new Matrix4d(scan.getHeader().getTransfMatrix()), scan, iscan)
                    : null;
        }
    }

    private PTXScan readSingleScanHeader(BufferedReader reader) {

        try {
            PTXHeader header = new PTXHeader();

            header.setNAzimuth(Integer.parseInt(getNextLine(reader)));
            header.setNZenith(Integer.parseInt(getNextLine(reader)));
            header.setPointInDoubleFormat(true);

            String[] registeredPos = getNextLine(reader).split(" ");
            header.setScannerRegisteredPosition(new Point3d(Double.parseDouble(registeredPos[0]),
                    Double.parseDouble(registeredPos[1]),
                    Double.parseDouble(registeredPos[2])));

            String[] registeredAxisX = getNextLine(reader).split(" ");
            header.setScannerRegisteredAxisX(new Point3d(Double.parseDouble(registeredAxisX[0]),
                    Double.parseDouble(registeredAxisX[1]),
                    Double.parseDouble(registeredAxisX[2])));

            String[] registeredAxisY = getNextLine(reader).split(" ");
            header.setScannerRegisteredAxisY(new Point3d(Double.parseDouble(registeredAxisY[0]),
                    Double.parseDouble(registeredAxisY[1]),
                    Double.parseDouble(registeredAxisY[2])));

            String[] registeredAxisZ = getNextLine(reader).split(" ");
            header.setScannerRegisteredAxisZ(new Point3d(Double.parseDouble(registeredAxisZ[0]),
                    Double.parseDouble(registeredAxisZ[1]),
                    Double.parseDouble(registeredAxisZ[2])));

            String[] transfMatrixRow0 = getNextLine(reader).split(" ");
            String[] transfMatrixRow1 = getNextLine(reader).split(" ");
            String[] transfMatrixRow2 = getNextLine(reader).split(" ");
            String[] transfMatrixRow3 = getNextLine(reader).split(" ");

            double m00 = Double.parseDouble(transfMatrixRow0[0]);
            double m01 = Double.parseDouble(transfMatrixRow1[0]);
            double m02 = Double.parseDouble(transfMatrixRow2[0]);
            double m03 = Double.parseDouble(transfMatrixRow3[0]);
            double m10 = Double.parseDouble(transfMatrixRow0[1]);
            double m11 = Double.parseDouble(transfMatrixRow1[1]);
            double m12 = Double.parseDouble(transfMatrixRow2[1]);
            double m13 = Double.parseDouble(transfMatrixRow3[1]);
            double m20 = Double.parseDouble(transfMatrixRow0[2]);
            double m21 = Double.parseDouble(transfMatrixRow1[2]);
            double m22 = Double.parseDouble(transfMatrixRow2[2]);
            double m23 = Double.parseDouble(transfMatrixRow3[2]);
            double m30 = Double.parseDouble(transfMatrixRow0[3]);
            double m31 = Double.parseDouble(transfMatrixRow1[3]);
            double m32 = Double.parseDouble(transfMatrixRow2[3]);
            double m33 = Double.parseDouble(transfMatrixRow3[3]);

            Matrix4d transfMatrix = new Matrix4d();
            transfMatrix.set(new double[]{
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33
            });

            header.setTransfMatrix(transfMatrix);

            if (header.getNZenith() * header.getNAzimuth() != 0) {

                //read first point
                reader.mark(1000);

                String firstPoint = reader.readLine();
                if (firstPoint != null) {
                    String[] split = firstPoint.split(" ");

                    if (split.length > 3) {

                        header.setPointContainsIntensity(true);

                        if (split.length > 6) {
                            header.setPointContainsRGB(true);
                        }
                    }
                }

                reader.reset();
            }

            return new PTXScan(getFile(), header, iline);

        } catch (IOException | NumberFormatException ex) {
            Logger.getLogger(PTXReader.class.getCanonicalName()).log(Level.WARNING, "Error around line " + iline, ex);
        }
        return null;
    }

    private String getNextLine(BufferedReader reader) throws IOException {

        String line = reader.readLine();

        if (line != null) {
            iline++;
        }

        return line;
    }

    public static void main(String[] args) {

        try {
            long startTime = System.currentTimeMillis();
            PTXReader reader = new PTXReader(new File("/data/lidar/tls/leica/bouamir/Bouamir_P16.ptx"));

            IteratorWithException<LidarScan> it = reader.iterator();
            while (it.hasNext()) {
                System.out.println(it.next());
            }
            System.out.println("Time to read scans header : " + TimeCounter.getElapsedTimeInSeconds(startTime) + " s");

        } catch (Exception e) {
            System.err.println("Error occured");
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
