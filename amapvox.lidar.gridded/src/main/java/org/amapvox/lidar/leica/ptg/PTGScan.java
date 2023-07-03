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
import org.amapvox.lidar.gridded.LFloatPoint;
import org.amapvox.lidar.gridded.LPoint;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.vecmath.Matrix4d;

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

    private long nByteRead;
    private int headerSize;
    private int columnPositionSize;

    public PTGScan(File file) {
        super(file);
        header = new PTGHeader();
        returnMissingPoint = true;
    }

    @Override
    /**
     * Open a ptg binary scan file.This method read header and stores it as a
     * {@link PTGHeader PTGHeader} object. You can get it with the method
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void readHeader() throws FileNotFoundException, IOException {

        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(getFile())))) {
            // reset number of bytes read
            nByteRead = 0;

            /**
             * *read PTG format signature**
             */
            char ptgSignatureByte1 = (char) dis.readByte();
            char ptgSignatureByte2 = (char) dis.readByte();
            char ptgSignatureByte3 = (char) dis.readByte();
            char ptgSignatureByte4 = (char) dis.readByte();

            nByteRead += 4;

            if (ptgSignatureByte1 != 'P' || ptgSignatureByte2 != 'T' || ptgSignatureByte3 != 'G' || ptgSignatureByte4 != '\0') {
                throw new IOException("PTG file " + getFile().getName() + " File type tag ‘P’, ‘T’, ‘G’, ‘\\0’ excepected in header");
            }

            /**
             * *read magic number**
             */
            byte magicNumberByte1 = dis.readByte();
            byte magicNumberByte2 = dis.readByte();
            byte magicNumberByte3 = dis.readByte();
            byte magicNumberByte4 = dis.readByte();

            nByteRead += 4;

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

                        nByteRead += 4;

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
            headerSize = (int) nByteRead;
            columnPositionSize = 8 * header.getNAzimuth();

            resetRange();
        }
    }

    private String getNextString(DataInputStream dis) throws IOException {

        int length = dis.readUnsignedByte() + dis.readUnsignedByte() + dis.readUnsignedByte() + dis.readUnsignedByte();
        nByteRead += 4;

        char[] keyCharacters = new char[length];
        for (int i = 0; i < length; i++) {
            keyCharacters[i] = (char) dis.readByte();
        }

        nByteRead += length;

        return String.valueOf(keyCharacters).trim();
    }

    private int getNextInt(DataInputStream dis) throws IOException {

        byte b1 = dis.readByte();
        byte b2 = dis.readByte();
        byte b3 = dis.readByte();
        byte b4 = dis.readByte();

        int result = LittleEndianUtility.bytesToInt(b1, b2, b3, b4);

        nByteRead += 4;

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

        nByteRead += 8;

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

        nByteRead += 8;

        return result;
    }

    private float getNextFloat(DataInputStream dis) throws IOException {

        float result = LittleEndianUtility.toFloat(dis.readByte(),
                dis.readByte(),
                dis.readByte(),
                dis.readByte());

        nByteRead += 4;

        return result;
    }

    // column offset to be able to jump directly to colum start
    // not used since we read the whole file at once
    private void readColumnsOffsets() throws FileNotFoundException, IOException {

        columnPositionSize = 0;

        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(getFile())))) {

            dis.mark(Integer.MAX_VALUE);
            dis.skipBytes((int) headerSize);

            long[] offsets = new long[header.getNAzimuth()];

            //get columns offsets list
            for (int i = 0; i < header.getNAzimuth(); i++) {
                offsets[i] = getNextLong(dis);
                columnPositionSize += 8;
            }
        }
    }

    @Override
    public PTGHeader getHeader() {
        return (PTGHeader) header;
    }

    @Override
    public void readPointCloud() throws FileNotFoundException, IOException {

        points = new LPoint[header.getNAzimuth()][header.getNZenith()];

        // use col and row variables to stick to LEICA Cyclone PTG File Format Specification
        int col = -1;
        int row = -1;
        // open data stream
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(getFile())))) {
            // reset number of bytes
            nByteRead = 0;
            // skip header
            dis.skipBytes(headerSize);
            nByteRead += headerSize;
            // skip column positions
            dis.skipBytes(columnPositionSize);
            nByteRead += columnPositionSize;
            // loop over columns
            for (col = 0; col < header.getNAzimuth(); col++) {
                // read row validity bits 
                boolean[] validPoints = readValidityBits(dis);
                // loop over rows
                for (row = 0; row < header.getNZenith(); row++) {
                    // read valid points
                    if (validPoints[row]) {
                        LPoint point;
                        // point xyz coordinate
                        if (header.isPointInDoubleFormat()) {
                            // xyz as double
                            double x = getNextDouble(dis);
                            double y = getNextDouble(dis);
                            double z = getNextDouble(dis);

                            point = new LDoublePoint();
                            ((LDoublePoint) point).x = x;
                            ((LDoublePoint) point).y = y;
                            ((LDoublePoint) point).z = z;
                        } else {
                            // xyz as float
                            float x = getNextFloat(dis);
                            float y = getNextFloat(dis);
                            float z = getNextFloat(dis);

                            point = new LFloatPoint();
                            ((LFloatPoint) point).x = x;
                            ((LFloatPoint) point).y = y;
                            ((LFloatPoint) point).z = z;
                        }
                        // point intensity
                        if (header.isPointContainsIntensity()) {
                            float intensity = getNextFloat(dis);
                            point.intensity = intensity;
                        }
                        // point RGB
                        if (header.isPointContainsRGB()) {
                            int red = dis.readUnsignedByte() + 128;
                            int green = dis.readUnsignedByte() + 128;
                            int blue = dis.readUnsignedByte() + 128;
                            nByteRead += 3;
                            point.red = red;
                            point.green = green;
                            point.blue = blue;
                        }
                        // point grid coordinate
                        point.azimuthIndex = col;
                        point.zenithIndex = row;
                        // set point in array
                        points[col][row] = point;
                    }
                }
            }
        } catch (IOException ex) {
            throw new IOException("Error reading point cloud " + getFile().getName() + " col " + col + " row " + row, ex);
        }
    }

    private boolean[] readValidityBits(DataInputStream dis) throws IOException {

        int nbValidityBytes = (int) Math.ceil(header.getNZenith() / 8.0);
        boolean[] validPoints = new boolean[nbValidityBytes * 8];

        for (int j = 0, count = 0; j < nbValidityBytes; j++, count += 8) {

            byte value = dis.readByte();
            nByteRead++;
            validPoints[count + 7] = ((value & 0b00000001) == 0b00000001);
            validPoints[count + 6] = ((value & 0b00000010) == 0b00000010);
            validPoints[count + 5] = ((value & 0b00000100) == 0b00000100);
            validPoints[count + 4] = ((value & 0b00001000) == 0b00001000);
            validPoints[count + 3] = ((value & 0b00010000) == 0b00010000);
            validPoints[count + 2] = ((value & 0b00100000) == 0b00100000);
            validPoints[count + 1] = ((value & 0b01000000) == 0b01000000);
            validPoints[count + 0] = ((value & 0b10000000) == 0b10000000);
        }
        
        return validPoints;
    }
}
