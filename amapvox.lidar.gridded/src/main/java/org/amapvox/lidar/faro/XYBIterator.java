/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.faro;

import org.amapvox.lidar.gridded.LDoublePoint;
import org.amapvox.lidar.gridded.LPoint;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Point3d;
import org.apache.poi.util.LittleEndianInputStream;

/**
 * Iterator for FARO XYB pointcloud file. XYB is similar to XYZ ASCII file but
 * in binary format. Little endian convention.
 * <p>
 * Example of XYB header: #SCENE XYZ binary format v1.0 ScanPosition 0.00000000
 * 0.00000000 0.00000000 Rows 10142 Cols 4268 0b00000000 0b00000000 0b00000000
 * 0b00000000 (4 zeroes as byte)
 * <p>
 * The rest of the XYB file is a sequence of hit points (x, y, z, r) with
 * precision double, double, double, unsigned short.
 *
 * @author Philippe Verley (philippe.verley@ird.fr)
 */
public class XYBIterator implements Iterator<LPoint>, AutoCloseable {

    private final File file;
    private XYBHeader header;
    private final LittleEndianInputStream leis;

    public XYBIterator(File file) throws IOException {
        this.file = file;
        open();
        leis = new LittleEndianInputStream(new FileInputStream(this.file));
        leis.skipFully(header.getSize());
    }

    @Override
    public boolean hasNext() {
        return leis.available() > 0;
    }

    @Override
    public LPoint next() {

        if (leis.available() > 0) {
            LDoublePoint point = new LDoublePoint();
            point.x = leis.readDouble();
            point.y = leis.readDouble();
            point.z = leis.readDouble();
            if (!isFinite(point)) {
                point.valid = false;
            }
            leis.readUShort();
            return point;
        }

        return null;
    }

    @Override
    public void close() throws Exception {
        leis.close();
    }

    private boolean isFinite(LDoublePoint point) {
        return Double.isFinite(point.x)
                && Double.isFinite(point.y)
                && Double.isFinite(point.z);
    }

    /**
     * Open the XYB file and read the header.
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void open() throws FileNotFoundException, IOException {

        // read header
        int nByteHeader = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            // read header
            header = new XYBHeader();
            header.setPointContainsIntensity(true);
            header.setPointContainsRGB(false);
            header.setPointInDoubleFormat(true);
            header.setPointInFloatFormat(false);
            header.updatePointSize();
            String str;
            // 1s line, comment to be ignored
            str = br.readLine();
            nByteHeader += str.length() + 1; // +1 for carriage return (\r or \n)
            // 2nd line, ScanPosition
            str = br.readLine();
            nByteHeader += str.length() + 1;
            String[] strScanPosition = clean(str).split(" ");
            header.setScannerPosition(new Point3d(
                    Double.valueOf(strScanPosition[1]),
                    Double.valueOf(strScanPosition[2]),
                    Double.valueOf(strScanPosition[3])
            )); // 3rd line, number of rows
            str = br.readLine();
            nByteHeader += str.length() + 1;
            String[] strRows = clean(str).split(" ");
            header.setNAzimuth(Integer.valueOf(strRows[1]));
            // 4th line, number of colums
            str = br.readLine();
            nByteHeader += str.length() + 1;
            String[] strCols = clean(str).split(" ");
            header.setNZenith(Integer.valueOf(strCols[1]));
            // 5th line, 4 zero bytes (000000 000000 000000 000000)
            nByteHeader += 4;
            // set header size
            header.setSize(nByteHeader);
        }
    }
    
    private String clean(String str) {
        return str.trim().replaceAll(" +", " ");
    }

    /**
     * XYB header
     *
     * @return XYB header
     */
    public XYBHeader getHeader() {
        return header;
    }

    private void toTextFile(File textFile) throws FileNotFoundException, IOException {

        try (LittleEndianInputStream oleis = new LittleEndianInputStream(new FileInputStream(file))) {
            oleis.skipFully(header.getSize());
            DecimalFormat dmf = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
            dmf.applyPattern("#0.###");
            dmf.setGroupingUsed(false);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(textFile))) {
                while (oleis.available() > 0) {
                    double x = oleis.readDouble();
                    double y = oleis.readDouble();
                    double z = oleis.readDouble();
                    oleis.readUShort();
                    writer.write(dmf.format(x) + " " + dmf.format(y) + " " + dmf.format(z) + "\n");
                }
            }
        }
    }

    public static void main(String... args) {

        try (XYBIterator xybr = new XYBIterator(new File("/data/lidar/tls/valsain/Valsain_89_000.xyb"))) {
            System.out.println(xybr.getHeader());
            int npoint = 0;
            while (xybr.hasNext()) {
                xybr.next();
                npoint++;
            }
            System.out.println("npoint " + npoint);
        } catch (Exception ex) {
            Logger.getLogger(XYBIterator.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
