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
package org.amapvox.lidar.jlas;

import org.amapvox.lidar.jlas.LasHeader;
import org.amapvox.lidar.jlas.LasHeader11;
import org.amapvox.lidar.jlas.LasHeader12;
import org.amapvox.lidar.jlas.LasHeader13;
import org.amapvox.commons.util.io.LittleEndianUtility;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is devoted to read a LASer (*.las) file.
 * It allows to get the file header and get an iterator on the points of the file.<br><br><br>
 * 
 * @see <a href="http://www.asprs.org/Committee-General/LASer-LAS-File-Format-Exchange-Activities.html">ASPRS Specification</a>
 * 
 * 
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class LasReader implements Iterable<PointDataRecordFormat> {
    
    private File file;
    private ArrayList<VariableLengthRecord> variableLengthRecords;
    private LasHeader header;

    /**
     * Get a list of the variable length records.
     * @see <a href="http://www.asprs.org/Committee-General/LASer-LAS-File-Format-Exchange-Activities.html">ASPRS Specification</a>
     * @return A list of variable length records.
     */
    public ArrayList<VariableLengthRecord> getVariableLengthRecords() {
        return variableLengthRecords;
    }

    /**
     * Get the las file header.
     * <p>Before, a call to the method {@link #open(java.io.File) } is required.</p>
     * @return The las header.
     */
    public LasHeader getHeader() {
        return header;
    }
    
    
    private LasHeader readHeader10(DataInputStream dis, LasHeader header) throws IOException {

        char bytefs1 = (char) dis.readByte();
        char bytefs2 = (char) dis.readByte();
        char bytefs3 = (char) dis.readByte();
        char bytefs4 = (char) dis.readByte();
        char[] fileSignature = new char[]{bytefs1, bytefs2, bytefs3, bytefs4};
        header.setFileSignature(fileSignature);

        int byteGE = dis.readByte() + dis.readByte() + dis.readByte() + dis.readByte();
        header.setReserved(byteGE);

        long pigd1 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setProjectIdGuidData1(pigd1);

        int pigd2 = dis.readByte() + dis.readByte();
        header.setProjectIdGuidData2(pigd2);

        int pigd3 = dis.readByte() + dis.readByte();
        header.setProjectIdGuidData3(pigd3);

        double pigd4 = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setProjectIdGuidData4(pigd4);

        byte vM = dis.readByte();
        header.setVersionMajor(vM);

        byte vm = dis.readByte();
        header.setVersionMinor(vm);

        char[] si = new char[]{(char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte()};

        header.setSystemIdentifier(si);

        char[] gs = new char[]{(char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte()};

        header.setGeneratingSoftware(gs);

        short fcdoy = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setFileCreationDayOfYear(fcdoy);

        short fcy = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setFileCreationYear(fcy);

        short hs = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setHeaderSize(hs);

        long otpd = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setOffsetToPointData(otpd);

        long novlr = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setNumberOfVariableLengthRecords(novlr);

        byte pdfID = dis.readByte();
        header.setPointDataFormatID(pdfID);

        short pdrl = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setPointDataRecordLength(pdrl);

        long nopr = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setNumberOfPointrecords(nopr);

        long nopbyr1 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr2 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr3 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr4 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr5 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});

        long[] nopbyr = new long[]{nopbyr1, nopbyr2, nopbyr3, nopbyr4, nopbyr5};

        header.setNumberOfPointsByReturn(nopbyr);

        double sxf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setxScaleFactor(sxf);

        double syf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setyScaleFactor(syf);

        double szf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setzScaleFactor(szf);

        double xoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setxOffset(xoff);

        double yoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setyOffset(yoff);

        double zoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setzOffset(zoff);

        double maxX = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxX(maxX);

        double minX = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinX(minX);

        double maxY = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxY(maxY);

        double minY = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinY(minY);

        double maxZ = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxZ(maxZ);

        double minZ = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinZ(minZ);

        return header;
    }

    private LasHeader readHeader11(DataInputStream dis, LasHeader11 header) throws IOException {

        char bytefs1 = (char) dis.readByte();
        char bytefs2 = (char) dis.readByte();
        char bytefs3 = (char) dis.readByte();
        char bytefs4 = (char) dis.readByte();
        char[] fileSignature = new char[]{bytefs1, bytefs2, bytefs3, bytefs4};
        header.setFileSignature(fileSignature);

        int byteFsi = dis.readByte() + dis.readByte();
        header.setFileSourceId(byteFsi);

        int byteGE = dis.readByte() + dis.readByte();
        header.setReserved(byteGE);

        long pigd1 = dis.readByte() + dis.readByte() + dis.readByte() + dis.readByte();
        header.setProjectIdGuidData1(pigd1);

        int pigd2 = dis.readByte() + dis.readByte();
        header.setProjectIdGuidData2(pigd2);

        int pigd3 = dis.readByte() + dis.readByte();
        header.setProjectIdGuidData3(pigd3);

        double pigd4 = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setProjectIdGuidData4(pigd4);

        byte vM = dis.readByte();
        header.setVersionMajor(vM);

        byte vm = dis.readByte();
        header.setVersionMinor(vm);

        char[] si = new char[]{(char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte()};

        header.setSystemIdentifier(si);

        char[] gs = new char[]{(char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte()};

        header.setGeneratingSoftware(gs);

        short fcdoy = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setFileCreationDayOfYear(fcdoy);

        short fcy = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setFileCreationYear(fcy);

        short hs = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setHeaderSize(hs);

        long otpd = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setOffsetToPointData(otpd);

        long novlr = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setNumberOfVariableLengthRecords(novlr);

        byte pdfID = dis.readByte();
        header.setPointDataFormatID(pdfID);

        short pdrl = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setPointDataRecordLength(pdrl);

        long nopr = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setNumberOfPointrecords(nopr);

        long nopbyr1 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr2 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr3 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr4 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr5 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});

        long[] nopbyr = new long[]{nopbyr1, nopbyr2, nopbyr3, nopbyr4, nopbyr5};

        header.setNumberOfPointsByReturn(nopbyr);

        double sxf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setxScaleFactor(sxf);

        double syf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setyScaleFactor(syf);

        double szf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setzScaleFactor(szf);

        double xoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setxOffset(xoff);

        double yoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setyOffset(yoff);

        double zoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setzOffset(zoff);

        double maxX = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxX(maxX);

        double minX = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinX(minX);

        double maxY = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxY(maxY);

        double minY = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinY(minY);

        double maxZ = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxZ(maxZ);

        double minZ = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinZ(minZ);

        return header;
    }

    private LasHeader readHeader12(DataInputStream dis, LasHeader12 header) throws IOException {

        char bytefs1 = (char) dis.readByte();
        char bytefs2 = (char) dis.readByte();
        char bytefs3 = (char) dis.readByte();
        char bytefs4 = (char) dis.readByte();
        char[] fileSignature = new char[]{bytefs1, bytefs2, bytefs3, bytefs4};
        header.setFileSignature(fileSignature);

        int byteFsi = dis.readByte() + dis.readByte();
        header.setFileSourceId(byteFsi);

        int byteGE = dis.readByte() + dis.readByte();
        header.setGlobalEncoding(byteGE);

        long pigd1 = dis.readByte() + dis.readByte() + dis.readByte() + dis.readByte();
        header.setProjectIdGuidData1(pigd1);

        int pigd2 = dis.readByte() + dis.readByte();
        header.setProjectIdGuidData2(pigd2);

        int pigd3 = dis.readByte() + dis.readByte();
        header.setProjectIdGuidData3(pigd3);

        double pigd4 = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setProjectIdGuidData4(pigd4);

        byte vM = dis.readByte();
        header.setVersionMajor(vM);

        byte vm = dis.readByte();
        header.setVersionMinor(vm);

        char[] si = new char[]{(char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte()};

        header.setSystemIdentifier(si);

        char[] gs = new char[]{(char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte()};

        header.setGeneratingSoftware(gs);

        short fcdoy = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setFileCreationDayOfYear(fcdoy);

        short fcy = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setFileCreationYear(fcy);

        short hs = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setHeaderSize(hs);

        long otpd = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setOffsetToPointData(otpd);

        long novlr = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setNumberOfVariableLengthRecords(novlr);

        byte pdfID = dis.readByte();
        header.setPointDataFormatID(pdfID);

        short pdrl = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setPointDataRecordLength(pdrl);

        long nopr = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setNumberOfPointrecords(nopr);

        long nopbyr1 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr2 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr3 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr4 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr5 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});

        long[] nopbyr = new long[]{nopbyr1, nopbyr2, nopbyr3, nopbyr4, nopbyr5};

        header.setNumberOfPointsByReturn(nopbyr);

        double sxf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setxScaleFactor(sxf);

        double syf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setyScaleFactor(syf);

        double szf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setzScaleFactor(szf);

        double xoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setxOffset(xoff);

        double yoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setyOffset(yoff);

        double zoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setzOffset(zoff);

        double maxX = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxX(maxX);

        double minX = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinX(minX);

        double maxY = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxY(maxY);

        double minY = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinY(minY);

        double maxZ = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxZ(maxZ);

        double minZ = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinZ(minZ);

        return header;
    }

    private LasHeader readHeader13(DataInputStream dis, LasHeader13 header) throws IOException {

        char bytefs1 = (char) dis.readByte();
        char bytefs2 = (char) dis.readByte();
        char bytefs3 = (char) dis.readByte();
        char bytefs4 = (char) dis.readByte();
        char[] fileSignature = new char[]{bytefs1, bytefs2, bytefs3, bytefs4};
        header.setFileSignature(fileSignature);

        int byteFsi = dis.readByte() + dis.readByte();
        header.setFileSourceId(byteFsi);

        int byteGE = dis.readByte() + dis.readByte();
        header.setGlobalEncoding(byteGE);

        long pigd1 = dis.readByte() + dis.readByte() + dis.readByte() + dis.readByte();
        header.setProjectIdGuidData1(pigd1);

        int pigd2 = dis.readByte() + dis.readByte();
        header.setProjectIdGuidData2(pigd2);

        int pigd3 = dis.readByte() + dis.readByte();
        header.setProjectIdGuidData3(pigd3);

        double pigd4 = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setProjectIdGuidData4(pigd4);

        byte vM = dis.readByte();
        header.setVersionMajor(vM);

        byte vm = dis.readByte();
        header.setVersionMinor(vm);

        char[] si = new char[]{(char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte()};

        header.setSystemIdentifier(si);

        char[] gs = new char[]{(char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte()};

        header.setGeneratingSoftware(gs);

        short fcdoy = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setFileCreationDayOfYear(fcdoy);

        short fcy = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setFileCreationYear(fcy);

        short hs = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setHeaderSize(hs);

        long otpd = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setOffsetToPointData(otpd);

        long novlr = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setNumberOfVariableLengthRecords(novlr);

        byte pdfID = dis.readByte();
        header.setPointDataFormatID(pdfID);

        short pdrl = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setPointDataRecordLength(pdrl);

        long nopr = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setNumberOfPointrecords(nopr);

        long nopbyr1 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr2 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr3 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr4 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr5 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});

        long[] nopbyr = new long[]{nopbyr1, nopbyr2, nopbyr3, nopbyr4, nopbyr5};

        header.setNumberOfPointsByReturn(nopbyr);

        double sxf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setxScaleFactor(sxf);

        double syf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setyScaleFactor(syf);

        double szf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setzScaleFactor(szf);

        double xoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setxOffset(xoff);

        double yoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setyOffset(yoff);

        double zoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setzOffset(zoff);

        double maxX = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxX(maxX);

        double minX = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinX(minX);

        double maxY = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxY(maxY);

        double minY = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinY(minY);

        double maxZ = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxZ(maxZ);

        double minZ = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinZ(minZ);

        BigInteger startOfWaveformDataPacketRecord = LittleEndianUtility.toBigInteger(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());

        header.setStartOfWaveformDataPacketRecord(startOfWaveformDataPacketRecord);

        return header;
    }

    /**
     * Read the header of the given las file.
     * @param file a las file
     * @return The header of the las file.
     * @throws IOException
     * @throws UnsupportedOperationException 
     */
    public LasHeader readHeader(File file) throws IOException, UnsupportedOperationException {

        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {

            /**
             * *read the file version at first**
             */
            dis.mark(30);
            dis.skipBytes(24);
            byte vM = dis.readByte();
            byte vm = dis.readByte();
            dis.reset();

            LasHeader header = null;
            String errorMsg = "";

            if (vM == 1) {
                switch (vm) {
                    case 0:
                        header = new LasHeader();
                        header = readHeader10(dis, header);
                        break;
                    case 1:
                        header = new LasHeader11();
                        header = readHeader11(dis, (LasHeader11) header);
                        break;
                    case 2:
                        header = new LasHeader12();
                        header = readHeader12(dis, (LasHeader12) header);
                        break;
                    case 3:
                        header = new LasHeader13();
                        header = readHeader13(dis, (LasHeader13) header);
                        break;
                    case 4:
                        //header = new LasHeader14();
                        errorMsg = "1.4 format not supported yet";
                        //throw new Exception("1.4 format not supported yet");
                        //header = readHeader14(dis, (LasHeader14)header);
                }
            }
            
            if(header == null){
                throw new UnsupportedOperationException(errorMsg);
            }

            return header;

        } catch (IOException ex) {
            throw ex;
        }

    }

    private static ArrayList<VariableLengthRecord> readVariableLengthRecords(File file, int start, long end, long variableNumber) throws IOException {

        ArrayList<VariableLengthRecord> variableLengthRecords = new ArrayList<>();

        if (variableNumber > 0) {

            try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {

                dis.skipBytes(start);

                for (long i = 0; i < variableNumber; i++) {

                    VariableLengthRecord vlr = new VariableLengthRecord();

                    short reserved = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
                    vlr.setReserved(reserved);

                    char[] userID = new char[]{(char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
                        (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
                        (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
                        (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte()};

                    vlr.setUserID(userID);

                    short recordID = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
                    vlr.setRecordID(recordID);

                    short rlah = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
                    vlr.setRecordLengthAfterHeader(rlah);

                    char[] description = new char[]{(char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
                        (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
                        (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
                        (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
                        (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
                        (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
                        (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
                        (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte()};

                    vlr.setDescription(description);

                    dis.skipBytes(rlah);
                    variableLengthRecords.add(vlr);
                }

            } catch (IOException ex) {
                throw ex;
            } catch (Exception ex) {
                throw ex;
            }
        }

        return variableLengthRecords;
    }
    
    /**
     * Open the given las file, read its header and the variable length records.
     * @param file a las file
     * @throws IOException
     * @throws Exception 
     */
    public void open(File file) throws IOException, Exception{
        
        LasReader reader = new LasReader();
        this.file = file;
        header = reader.readHeader(file);
        variableLengthRecords = readVariableLengthRecords(file, header.getHeaderSize(), header.getOffsetToPointData(), header.getNumberOfVariableLengthRecords());
    }
    

    /**
     * Iterates through the points of the las file. 
     * Points are not kept in memory.
     * @return 
     */
    @Override
    public Iterator<PointDataRecordFormat> iterator(){

        final DataInputStream dis;
        final int offset = header.getPointDataRecordLength();
        final long size = header.getNumberOfPointrecords();
        final long start = header.getOffsetToPointData();
        final int pointFormatID = header.getPointDataFormatID();
        Iterator<PointDataRecordFormat> it;
        
        try{
            dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            
            dis.skip(start);

            it = new Iterator<PointDataRecordFormat>() {
            
            int count = 0;
            
            @Override
            public boolean hasNext() {
                
                boolean test = count < size;
                
                if(!test){
                    try {
                        dis.close();
                    } catch (IOException ex) {
                        Logger.getLogger(LasReader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                return test;
            }

            @Override
            public PointDataRecordFormat next(){

                PointDataRecordFormat pdr = null;

                switch (pointFormatID) {

                    case 0:
                        pdr = new PointDataRecordFormat();
                        break;
                    case 1:
                        pdr = new PointDataRecordFormat1();
                        break;
                    case 2:
                        pdr = new PointDataRecordFormat2();
                        break;
                    case 3:
                        pdr = new PointDataRecordFormat3();
                        break;
                }
                int x;
                try {
                    x = LittleEndianUtility.bytesToInt(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
                    
                    pdr.setX(x);
                    int y = LittleEndianUtility.bytesToInt(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
                    pdr.setY(y);
                    int z = LittleEndianUtility.bytesToInt(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
                    pdr.setZ(z);
                    int intensity = LittleEndianUtility.bytesToShortInt(dis.readByte(), dis.readByte());
                    pdr.setIntensity(intensity);
                    byte b = dis.readByte();
                    int bit0 = (b >> 0) & 1;
                    int bit1 = (b >> 1) & 1;
                    int bit2 = (b >> 2) & 1;
                    int bit3 = (b >> 3) & 1;
                    int bit4 = (b >> 4) & 1;
                    int bit5 = (b >> 5) & 1;
                    int bit6 = (b >> 6) & 1;
                    int bit7 = (b >> 7) & 1;
                    int returnNumber = Integer.parseInt(String.valueOf(bit2) + String.valueOf(bit1) + String.valueOf(bit0), 2);
                    pdr.setReturnNumber((short) returnNumber);
                    int numberOfReturns = Integer.parseInt(String.valueOf(bit5) + String.valueOf(bit4) + String.valueOf(bit3), 2);
                    pdr.setNumberOfReturns((short) numberOfReturns);
                    if (bit6 == 0) {
                        pdr.setScanDirectionFlag(false);
                    } else {
                        pdr.setScanDirectionFlag(true);
                    }
                    if (bit7 == 0) {
                        pdr.setEdgeOfFlightLine(false);
                    } else {
                        pdr.setEdgeOfFlightLine(true);
                    }
                    b = dis.readByte();
                    /*classification bits*/
                    bit0 = (b >> 0) & 1;
                    bit1 = (b >> 1) & 1;
                    bit2 = (b >> 2) & 1;
                    bit3 = (b >> 3) & 1;
                    bit4 = (b >> 4) & 1;
                    /*synthetic*/
                    bit5 = (b >> 5) & 1;
                    /*key-point*/
                    bit6 = (b >> 6) & 1;
                    /*Withheld*/
                    bit7 = (b >> 7) & 1;
                    short classification = (short) Integer.parseInt(
                            String.valueOf(bit4)
                            + String.valueOf(bit3)
                            + String.valueOf(bit2)
                            + String.valueOf(bit1)
                            + String.valueOf(bit0), 2);
                    pdr.setClassification(classification);
                    boolean synthetic = (bit5 != 0);
                    pdr.setSynthetic(synthetic);
                    boolean keyPoint = (bit6 != 0);
                    pdr.setKeyPoint(keyPoint);
                    boolean withheld = (bit7 != 0);
                    pdr.setWithheld(withheld);
                    int sar = dis.readByte();
                    pdr.setScanAngleRank(sar);
                    int usrData = dis.readUnsignedByte();
                    pdr.setUserData(usrData);
                    int pointSrcID = dis.readByte() + dis.readByte();
                    pdr.setPointSourceID(pointSrcID);
                    double gpsTime;
                    int red, green, blue;
                    short length = 0;
                    short difference = 0;
                    switch (pointFormatID) {

                        case 0:
                            length = PointDataRecordFormat.LENGTH;
                            break;
                        case 1:
                            gpsTime = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                                    dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
                            ((PointDataRecordFormat1) pdr).setGpsTime(gpsTime);

                            length = PointDataRecordFormat1.LENGTH;

                            break;

                        case 2:
                            red = dis.readUnsignedByte() + dis.readUnsignedByte();
                            ((PointDataRecordFormat3) pdr).setRed(red);
                            green = dis.readUnsignedByte() + dis.readUnsignedByte();
                            ((PointDataRecordFormat3) pdr).setGreen(green);
                            blue = dis.readUnsignedByte() + dis.readUnsignedByte();
                            ((PointDataRecordFormat3) pdr).setBlue(blue);

                            length = PointDataRecordFormat2.LENGTH;

                            break;
                        case 3:
                            gpsTime = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                                    dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
                            ((PointDataRecordFormat3) pdr).setGpsTime(gpsTime);
                            red = dis.readUnsignedByte() + dis.readUnsignedByte();
                            ((PointDataRecordFormat3) pdr).setRed(red);
                            green = dis.readUnsignedByte() + dis.readUnsignedByte();
                            ((PointDataRecordFormat3) pdr).setGreen(green);
                            blue = dis.readUnsignedByte() + dis.readUnsignedByte();
                            ((PointDataRecordFormat3) pdr).setBlue(blue);

                            length = PointDataRecordFormat3.LENGTH;

                            break;
                    }
                    difference = (short) (offset - length);
                    if (difference != 0) {

                        byte[] bytes = new byte[difference];

                        for (short j = 0; j < difference; j++) {

                            bytes[j] = dis.readByte();
                        }
                        switch (difference) {
                            case 2:
                                pdr.setExtrabytes((Extrabytes) new QLineExtrabytes(bytes));
                                break;
                            case 3:
                                pdr.setExtrabytes((Extrabytes) new VLineExtrabytes(bytes));
                                break;
                        }
                    }
                
                } catch (IOException ex) {
                    System.err.println("error");
                }
                
                count++;
                
                return pdr;
            }
        };
        
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
        return it;
    }
}
