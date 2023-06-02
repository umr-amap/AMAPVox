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

    public PTGScanTest() throws Exception {

    }

    @Before
    public void init() throws URISyntaxException, Exception {
        pTGScan = new PTGScan();
        pTGScan.openScanFile(new File(PTGScanTest.class.getResource("/leica_scans-2.PTG").toURI()));
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

        assertEquals(3498, header.getNZenith());
        assertEquals(3696, header.getNAzimuth());
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

        for (LPoint point : pTGScan) {
            assertNotNull(point);
            if (point instanceof LEmptyPoint) {
                emptypoint++;
            }
            npoint++;
        }

        // total number of points
        assertEquals(npoint,
                pTGScan.getHeader().getNZenith() * pTGScan.getHeader().getNAzimuth());
        // same as above but hard-coded
        assertEquals(npoint, 12928608);
        
        // empty shots
        assertEquals(emptypoint, 10201699);
    }

    @Test
    public void testIteratorFiltering() throws IOException, URISyntaxException {

        int indexRowToRead = 0;
        pTGScan.setZenithIndex(indexRowToRead);

        Iterator<LPoint> iterator = pTGScan.iterator();

        while (iterator.hasNext()) {

            LPoint point = iterator.next();
            assertNotNull(point);

            assertEquals(indexRowToRead, point.azimuthIndex);
        }

        pTGScan.resetZenithRange();

        int indexColumnToRead = pTGScan.getHeader().getNZenith() - 1;
        pTGScan.setAzimuthIndex(indexColumnToRead);

        iterator = pTGScan.iterator();

        while (iterator.hasNext()) {

            LPoint point = iterator.next();
            assertNotNull(point);

            assertEquals(indexColumnToRead, point.zenithIndex);
        }

        pTGScan.resetAzimuthRange();

        int minIndexColumnToRead = pTGScan.getHeader().getNZenith() - 51;
        int maxIndexColumnToRead = pTGScan.getHeader().getNZenith() - 1;

        pTGScan.setAzimuthRange(minIndexColumnToRead, maxIndexColumnToRead);

        iterator = pTGScan.iterator();

        while (iterator.hasNext()) {

            LPoint point = iterator.next();
            assertNotNull(point);

            assertTrue(point.zenithIndex >= minIndexColumnToRead && point.zenithIndex <= maxIndexColumnToRead);
        }

        pTGScan.resetAzimuthRange();

        int minIndexRowToRead = 50;
        int maxIndexRowToRead = 100;
        pTGScan.setZenithRange(minIndexRowToRead, maxIndexRowToRead);

        iterator = pTGScan.iterator();

        while (iterator.hasNext()) {

            LPoint point = iterator.next();
            assertNotNull(point);

            assertTrue(point.azimuthIndex >= minIndexRowToRead && point.azimuthIndex <= maxIndexRowToRead);
        }

    }

    @Test
    public void testComputeAngles() {
        
        pTGScan.computeMinMaxAngles();

        assertEquals(1.9210892540308797, pTGScan.getZenithMax(), 0.0000001);
        assertEquals(0.07353449476314654, pTGScan.getZenithMin(), 0.0000001);
        assertEquals(1.4557245551731213, pTGScan.getAzimMax(), 0.0000001);
        assertEquals(-0.2926585159330574, pTGScan.getAzimMin(), 0.0000001);

        assertEquals(4.999665630844091E-4, pTGScan.getAzimuthalStepAngle(), 0.0000001);
        assertEquals(5.000148198288859E-4, pTGScan.getZenithalStepAngle(), 0.0000001);
    }
}
