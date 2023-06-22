/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.leica.ptg;

import org.amapvox.lidar.gridded.LPoint;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;
import javax.vecmath.Matrix4d;
import org.amapvox.lidar.gridded.LEmptyPoint;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author calcul
 */
public class PTGScanTest {

    private PTGScan pTGScan;

    @Before
    public void init() throws URISyntaxException, Exception {
        pTGScan = new PTGScan(new File(PTGScanTest.class.getResource("/leica_scans-2.PTG").toURI()));
        pTGScan.open();
    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetHeader() {

        PTGHeader header = pTGScan.getHeader();

        assertEquals("2015/11/13", header.getDate());
        assertEquals("ScanWorld [Registration 1]", header.getScanWorldName());
        assertNull(header.getIpAddress());
        assertEquals("Scan-1", header.getScanName());
        assertEquals("ScanStation P30/P40", header.getScannerModel());
        assertEquals("ScanStationP40", header.getScannerName());
        assertEquals("16:22:10", header.getTime());
        assertEquals("", header.getText());
        assertEquals("", header.getTexte());
        assertTrue(header.isPointContainsIntensity());
        assertTrue(header.isPointInFloatFormat());
        assertFalse(header.isPointInDoubleFormat());
        assertFalse(header.isPointContainsRGB());

        Matrix4d transfMatrix = header.getTransfMatrix();
        assertArrayEquals(new double[]{0.1868399899901585, -0.9823903593482977, 0.0, -0.8699481165511749,
            0.9823903593482977, 0.1868399899901585, 0.0, -14.452411519374708,
            0.0, 0.0, 1.0, 0.5802979320287703,
            0.0, 0.0, 0.0, 1.0},
                new double[]{
                    transfMatrix.getM00(), transfMatrix.getM01(), transfMatrix.getM02(), transfMatrix.getM03(),
                    transfMatrix.getM10(), transfMatrix.getM11(), transfMatrix.getM12(), transfMatrix.getM13(),
                    transfMatrix.getM20(), transfMatrix.getM21(), transfMatrix.getM22(), transfMatrix.getM23(),
                    transfMatrix.getM30(), transfMatrix.getM31(), transfMatrix.getM32(), transfMatrix.getM33()},
                0);

        assertEquals(3498, header.getNAzimuth());
        assertEquals(3696, header.getNZenith());
        assertEquals(16, header.getPointSize());
        assertEquals(0, header.getRowsTotal());
        assertEquals(1, header.getVersion());
        assertEquals(0, header.getMaxAzimuthAngle(), 0);
        assertEquals(0, header.getMinAzimuthAngle(), 0);
        assertEquals(0, header.getMaxElevationAngle(), 0);
        assertEquals(0, header.getMinElevationAngle(), 0);
    }

    @Test
    public void testIteratorNoFiltering() throws IOException, URISyntaxException {

        long npoint = 0;
        long emptypoint = 0;

        System.out.println("testIteratorNoFiltering");
        for (LPoint point : pTGScan) {
            assertNotNull(point);
            if (point instanceof LEmptyPoint) {
                emptypoint++;
            }
            npoint++;
        }

        // total number of points
        assertEquals(pTGScan.getHeader().getNZenith() * pTGScan.getHeader().getNAzimuth(), npoint);
        // same as above but hard-coded
        assertEquals(12928608, npoint);

        // empty shots
        assertEquals(10201699, emptypoint);
    }

    @Test
    public void testIteratorFiltering() throws IOException, URISyntaxException {

        pTGScan.setZenithIndex(0);

        Iterator<LPoint> iterator = pTGScan.iterator();

        while (iterator.hasNext()) {

            LPoint point = iterator.next();
            assertNotNull(point);

            assertEquals(0, point.zenithIndex);
        }

        pTGScan.reset();

        int indexAzimuth = pTGScan.getHeader().getNAzimuth() - 1;
        pTGScan.setAzimuthIndex(indexAzimuth);

        iterator = pTGScan.iterator();

        while (iterator.hasNext()) {

            LPoint point = iterator.next();
            assertNotNull(point);

            assertEquals(indexAzimuth, point.azimuthIndex);
        }

        pTGScan.reset();

        int minIndezZenith = pTGScan.getHeader().getNZenith() - 51;
        int maxIndexZenith = pTGScan.getHeader().getNZenith() - 1;

        pTGScan.setZenithRange(minIndezZenith, maxIndexZenith);

        iterator = pTGScan.iterator();

        while (iterator.hasNext()) {

            LPoint point = iterator.next();
            assertNotNull(point);

            assertTrue(point.zenithIndex >= minIndezZenith && point.zenithIndex <= maxIndexZenith);
        }

        pTGScan.reset();

        int minIndexAzim = 50;
        int maxIndexAzim = 100;
        pTGScan.setAzimuthRange(minIndexAzim, maxIndexAzim);

        iterator = pTGScan.iterator();

        while (iterator.hasNext()) {

            LPoint point = iterator.next();
            assertNotNull(point);

            assertTrue(point.azimuthIndex >= minIndexAzim && point.azimuthIndex <= maxIndexAzim);
        }

        pTGScan.reset();

    }

    @Test
    public void testComputeAngles() {

        double a1 = Arrays.stream(pTGScan.getAveragedAzimuth())
                .filter(az -> !Double.isNaN(az))
                .findFirst().getAsDouble();
        assertEquals(1.4557245551731213, a1, 0.0000001);

        double a2 = Double.NaN;
        for (int i = pTGScan.getAveragedAzimuth().length - 1; i > 0; i--) {
            // max
            if (!Double.isNaN(pTGScan.getAveragedAzimuth()[i])) {
                a2 = pTGScan.getAveragedAzimuth()[i];
                break;
            }
        }
        assertEquals(-0.2926585159330574, a2, 0.0000001);

        double zn1 = Arrays.stream(pTGScan.getAveragedZenith())
                .filter(zn -> !Double.isNaN(zn))
                .findFirst().getAsDouble();
        assertEquals(1.9210892540308797, zn1, 0.0000001);
        
        double zn2 = Double.NaN;
        for (int i = pTGScan.getAveragedZenith().length - 1; i > 0; i--) {
            // max
            if (!Double.isNaN(pTGScan.getAveragedZenith()[i])) {
                zn2 = pTGScan.getAveragedZenith()[i];
                break;
            }
        }
        assertEquals(0.07353449476314654, zn2, 0.0000001);
        
        assertEquals(5E-4, pTGScan.getAzimuthalStepAngle(), 1E-6);
        assertEquals(5E-4, pTGScan.getZenithalStepAngle(), 1E-6);
    }
}
