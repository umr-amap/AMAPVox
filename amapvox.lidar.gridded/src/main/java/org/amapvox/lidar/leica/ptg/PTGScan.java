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

import org.amapvox.commons.util.io.LittleEndianUtility;
import org.amapvox.lidar.gridded.GriddedPointScan;
import org.amapvox.lidar.gridded.LDoublePoint;
import org.amapvox.lidar.gridded.LEmptyPoint;
import org.amapvox.lidar.gridded.LFloatPoint;
import org.amapvox.lidar.gridded.LPoint;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Matrix4d;
import org.amapvox.lidar.leica.ptx.PTXScan;

/**
 * <p>
 * This class is dedicated to handle PTG binary scan file, a Leica gridded point
 * format (see <a href= "http://www.xdesy.de/freeware/PTG-DLL/PTG-1.0.pdf">
 * specification</a>)</p>
 * <p>
 * It provides a simple iterator to get points from the file.</p>
 * As the ptg scan file is a gridded point format, you can select the row,
 * columns you want to read with the following methods :
 * <ul>
 * <li>{@link #setAzimuthIndex(int) setUpColumnToRead(int columnIndex)},</li>
 * <li>{@link #setAzimuthRange(int, int) setUpColumnsToRead(int startColumnIndex, int endColumnIndex)},</li>
 * <li>{@link #setZenithIndex(int) setUpRowToRead(int rowIndex)},</li>
 * <li>{@link #setZenithRange(int, int) setUpRowsToRead(int startRowIndex, int endRowIndex)}</li>
 * </ul>
 *
 * @author Julien Heurtebize
 */
public class PTGScan extends GriddedPointScan {

    private long nbByteRead;
    private long headerByteLength;
    private long[] offsets = null;
    private long offsetSize;

    private int row = -1;
    private int col = -1;
    private boolean[] validPoints;

    // whether PTX scan has been cached in heap memory
    final private AtomicBoolean cached = new AtomicBoolean(false);

    // LPoint array, without empty point
    private LPoint[][] points;

    public PTGScan() {
        header = new PTGHeader();
        returnMissingPoint = true;
    }

    /**
     * Open a ptg binary scan file.This method read header and stores it as a
     * {@link PTGHeader PTGHeader} object. You can get it with the method
     *
     * @param file
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Override
    public void openScanFile(File file) throws FileNotFoundException, IOException {

        this.nbByteRead = 0;
        this.offsetSize = 0;
        this.headerByteLength = 0;

        this.file = file;

        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {

            /**
             * *read PTG format signature**
             */
            char ptgSignatureByte1 = (char) dis.readByte();
            char ptgSignatureByte2 = (char) dis.readByte();
            char ptgSignatureByte3 = (char) dis.readByte();
            char ptgSignatureByte4 = (char) dis.readByte();

            nbByteRead += 4;

            if (ptgSignatureByte1 != 'P' || ptgSignatureByte2 != 'T' || ptgSignatureByte3 != 'G' || ptgSignatureByte4 != '\0') {
                throw new IOException("Bad format");
            }

            /**
             * *read magic number**
             */
            byte magicNumberByte1 = dis.readByte();
            byte magicNumberByte2 = dis.readByte();
            byte magicNumberByte3 = dis.readByte();
            byte magicNumberByte4 = dis.readByte();

            nbByteRead += 4;

            if ((magicNumberByte1 & 0xc7) != 0xc7
                    || (magicNumberByte2 & 0xa3) != 0xa3
                    || (magicNumberByte3 & 0x8f) != 0x8f
                    || (magicNumberByte4 & 0x92) != 0x92) {
                throw new IOException("Bad format");
            }

            /**
             * *read keys**
             */
            boolean hasKeys = true;

            while (hasKeys) {

                //read string key
                String key = getNextString(dis);

                switch (key) {
                    case "%%header_begin":
                        break;
                    case "%%version":
                        ((PTGHeader) header).setVersion(getNextInt(dis));
                        break;
                    case "%%sw_name":
                        ((PTGHeader) header).setScanWorldName(getNextString(dis));
                        break;
                    case "%%scan_name":
                        ((PTGHeader) header).setScanName(getNextString(dis));
                        break;
                    case "%%scanner_name":
                        ((PTGHeader) header).setScannerName(getNextString(dis));
                        break;
                    case "%%scanner_model":
                        ((PTGHeader) header).setScannerModel(getNextString(dis));
                        break;
                    case "%%scanner_ip_addr":
                        ((PTGHeader) header).setIpAddress(getNextString(dis));
                        break;
                    case "%%creation_date":
                        ((PTGHeader) header).setDate(getNextString(dis));
                        break;
                    case "%%creation_time":
                        ((PTGHeader) header).setTime(getNextString(dis));
                        break;
                    case "%%texte_*":
                        ((PTGHeader) header).setTexte(getNextString(dis));
                        break;
                    case "%%text_*":
                        ((PTGHeader) header).setText(getNextString(dis));
                        break;
                    case "%%cols":
                        ((PTGHeader) header).setNAzimuth(getNextInt(dis));
                        break;
                    case "%%rows":
                        ((PTGHeader) header).setNZenith(getNextInt(dis));
                        break;
                    case "%%rows_total":
                        ((PTGHeader) header).setRowsTotal(getNextInt(dis));
                        break;
                    case "%%azim_min":
                        ((PTGHeader) header).setMinAzimuthAngle(getNextDouble(dis));
                        break;
                    case "%%azim_max":
                        ((PTGHeader) header).setMaxAzimuthAngle(getNextDouble(dis));
                        break;
                    case "%%elev_min":
                        ((PTGHeader) header).setMinElevationAngle(getNextDouble(dis));
                        break;
                    case "%%elev_max":
                        ((PTGHeader) header).setMaxElevationAngle(getNextDouble(dis));
                        break;
                    case "%%transform":

                        double[] mat = new double[16];

                        for (int i = 0; i < 16; i++) {
                            mat[i] = getNextDouble(dis);
                        }

                        Matrix4d transfMatrix = new Matrix4d();
                        transfMatrix.set(new double[]{
                            mat[0], mat[4], mat[8], mat[12],
                            mat[1], mat[5], mat[9], mat[13],
                            mat[2], mat[6], mat[10], mat[14],
                            mat[3], mat[7], mat[11], mat[15]
                        });

                        header.setTransfMatrix(transfMatrix);

                        break;
                    case "%%properties":

                        byte propertiesByte1 = dis.readByte();
                        byte propertiesByte2 = dis.readByte(); //useless
                        byte propertiesByte3 = dis.readByte(); //useless
                        byte propertiesByte4 = dis.readByte(); //useless

                        nbByteRead += 4;

                        int bit1 = ((propertiesByte1 & 0x1) == 0x1) ? 1 : 0;
                        int bit2 = ((propertiesByte1 & 0x2) == 0x2) ? 1 : 0;
                        int bit3 = ((propertiesByte1 & 0x4) == 0x4) ? 1 : 0;
                        int bit4 = ((propertiesByte1 & 0x8) == 0x8) ? 1 : 0;

                        header.setPointInFloatFormat(bit1 == 1);
                        header.setPointInDoubleFormat(bit2 == 1);
                        header.setPointContainsIntensity(bit3 == 1);
                        header.setPointContainsRGB(bit4 == 1);

                        break;
                    case "%%header_end":
                        hasKeys = false;
                        break;
                }
            }

            header.updatePointSize();
            headerByteLength = nbByteRead;

            resetAzimuthRange();
            resetZenithRange();
        }
    }

    private String getNextString(DataInputStream dis) throws IOException {

        int length = dis.readUnsignedByte() + dis.readUnsignedByte() + dis.readUnsignedByte() + dis.readUnsignedByte();
        nbByteRead += 4;

        char[] keyCharacters = new char[length];
        for (int i = 0; i < length; i++) {
            keyCharacters[i] = (char) dis.readByte();
        }

        nbByteRead += length;

        return String.valueOf(keyCharacters).trim();
    }

    private int getNextInt(DataInputStream dis) throws IOException {

        byte b1 = dis.readByte();
        byte b2 = dis.readByte();
        byte b3 = dis.readByte();
        byte b4 = dis.readByte();

        int result = LittleEndianUtility.bytesToInt(b1, b2, b3, b4);

        nbByteRead += 4;

        return result;
    }

    private long getNextLong(DataInputStream dis) throws IOException {

        long result = LittleEndianUtility.tolong(new byte[]{dis.readByte(),
            dis.readByte(),
            dis.readByte(),
            dis.readByte(),
            dis.readByte(),
            dis.readByte(),
            dis.readByte(),
            dis.readByte()});

        nbByteRead += 8;

        return result;
    }

    private double getNextDouble(DataInputStream dis) throws IOException {

        double result = LittleEndianUtility.toDouble(dis.readByte(),
                dis.readByte(),
                dis.readByte(),
                dis.readByte(),
                dis.readByte(),
                dis.readByte(),
                dis.readByte(),
                dis.readByte());

        nbByteRead += 8;

        return result;
    }

    private float getNextFloat(DataInputStream dis) throws IOException {

        float result = LittleEndianUtility.toFloat(dis.readByte(),
                dis.readByte(),
                dis.readByte(),
                dis.readByte());

        nbByteRead += 4;

        return result;
    }

    private void readColumnsOffsets() throws FileNotFoundException, IOException {

        offsetSize = 0;

        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {

            dis.mark(Integer.MAX_VALUE);
            dis.skipBytes((int) headerByteLength);

            offsets = new long[header.getNZenith()];

            //get columns offsets list
            for (int i = 0; i < header.getNZenith(); i++) {

                offsets[i] = getNextLong(dis);
                offsetSize += 8;
            }
        }
    }

    private void skipHeader(DataInputStream dis) throws IOException {
        dis.skipBytes((int) headerByteLength);
        nbByteRead = headerByteLength;
    }

    private void skipOffsets(DataInputStream dis) throws IOException {
        dis.skipBytes((int) offsetSize);
        nbByteRead = (int) (headerByteLength + offsetSize);
    }

    private void skipMetadata(DataInputStream dis) throws IOException {
        skipHeader(dis);
        skipOffsets(dis);
    }

    public long getNbByteRead() {
        return nbByteRead;
    }

    @Override
    public PTGHeader getHeader() {
        return (PTGHeader) header;
    }

    synchronized private void cacheScan(File file) throws FileNotFoundException, IOException {

        if (cached.get()) {
            return;
        }

        points = new LPoint[header.getNAzimuth()][header.getNZenith()];

        readColumnsOffsets();

        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            dis.mark(Integer.MAX_VALUE);
            skipMetadata(dis);
            incrementColumnIndex(dis);
            do {
                incrementRowIndex(dis);
                if (validPoints[row]) {
                    LPoint point;
                    if (header.isPointInDoubleFormat()) {
                        double x = getNextDouble(dis);
                        double y = getNextDouble(dis);
                        double z = getNextDouble(dis);

                        point = new LDoublePoint();
                        ((LDoublePoint) point).x = x;
                        ((LDoublePoint) point).y = y;
                        ((LDoublePoint) point).z = z;
                    } else {
                        float x = getNextFloat(dis);
                        float y = getNextFloat(dis);
                        float z = getNextFloat(dis);

                        point = new LFloatPoint();
                        ((LFloatPoint) point).x = x;
                        ((LFloatPoint) point).y = y;
                        ((LFloatPoint) point).z = z;
                    }

                    if (header.isPointContainsIntensity()) {
                        float intensity = getNextFloat(dis);
                        point.intensity = intensity;
                    }

                    if (header.isPointContainsRGB()) {
                        int red = dis.readUnsignedByte() + 128;
                        int green = dis.readUnsignedByte() + 128;
                        int blue = dis.readUnsignedByte() + 128;

                        nbByteRead += 3;

                        point.red = red;
                        point.green = green;
                        point.blue = blue;
                    }

                    point.azimuthIndex = col;
                    point.zenithIndex = row;

                    points[col][row] = point;

                } else {
                    //skipBytes(dis, header.getPointSize());
                }
            } while (col < header.getNAzimuth());
            cached.set(true);
        }
    }

    private void incrementColumnIndex(DataInputStream dis) {

        try {
            col++;

            if (col < header.getNAzimuth()) {

                if (nbByteRead != offsets[col]) {
                    dis.reset();
                    dis.skipBytes((int) offsets[col]); //seek to column position
                    nbByteRead = offsets[col];
                }

                int nbValidityBytes = (int) Math.ceil(header.getNZenith()/ 8.0);
                validPoints = new boolean[nbValidityBytes * 8];

                for (int j = 0, count = 0; j < nbValidityBytes; j++, count += 8) {

                    byte value = dis.readByte();
                    nbByteRead++;
                    validPoints[count + 7] = ((value & 0b00000001) == 0b00000001);
                    validPoints[count + 6] = ((value & 0b00000010) == 0b00000010);
                    validPoints[count + 5] = ((value & 0b00000100) == 0b00000100);
                    validPoints[count + 4] = ((value & 0b00001000) == 0b00001000);
                    validPoints[count + 3] = ((value & 0b00010000) == 0b00010000);
                    validPoints[count + 2] = ((value & 0b00100000) == 0b00100000);
                    validPoints[count + 1] = ((value & 0b01000000) == 0b01000000);
                    validPoints[count + 0] = ((value & 0b10000000) == 0b10000000);
                }
            }

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void incrementRowIndex(DataInputStream dis) throws IOException {

        row++;

        //if last processed row was the last row then increment column and reinitialize row index
        if (row == validPoints.length) {
            incrementColumnIndex(dis);
            row = 0;
        }
    }

    private void skipBytes(DataInputStream dis, long nbBytes) throws IOException {
        dis.skipBytes((int) nbBytes);
        nbByteRead += nbBytes;
    }

    /**
     * Returns an iterator to get points from the scan file as a {@link LPoint}
     * As the ptg scan file is a gridded point format, you can select the row,
     * columns you want to read with the following methods :
     * <ul>
     * <li>{@link #setAzimuthIndex(int) setUpColumnToRead(int columnIndex)},</li>
     * <li>{@link #setAzimuthRange(int, int) setUpColumnsToRead(int startColumnIndex, int endColumnIndex)},</li>
     * <li>{@link #setZenithIndex(int) setUpRowToRead(int rowIndex)},</li>
     * <li>{@link #setZenithRange(int, int) setUpRowsToRead(int startRowIndex, int endRowIndex)}</li>
     * </ul>
     * Those methods should be called before to get the iterator.
     *
     * @return A {@link LPoint} point returned by the iterator.
     */
    @Override
    public Iterator<LPoint> iterator() {

        if (!cached.get()) {
            try {
                cacheScan(file);
            } catch (IOException ex) {
                Logger.getLogger(PTXScan.class.getName()).log(Level.SEVERE, "Error at column " + col + ", row " + row, ex);
                throw new RuntimeException(ex);
            }
        }
        return new CachedPTGScanIterator();
    }

    private class CachedPTGScanIterator implements Iterator<LPoint> {

        final private int size, nrow, ncol;
        private int cursor;

        CachedPTGScanIterator() {
            nrow = endZenithIndex - startZenithIndex + 1;
            ncol = endAzimuthIndex - startAzimuthIndex + 1;
            size = nrow * ncol;
        }

        @Override
        public boolean hasNext() {
            return cursor != size;
        }

        @Override
        public LPoint next() {

            int i = cursor;
            if (i >= size) {
                throw new NoSuchElementException();
            }
            int col = i / nrow;
            int row = i - col * nrow;
            col += startAzimuthIndex;
            row += startZenithIndex;
            cursor = i + 1;
            return (null != points[col][row])
                    ? points[col][row]
                    : new LEmptyPoint(col, row);
        }
    }
}
