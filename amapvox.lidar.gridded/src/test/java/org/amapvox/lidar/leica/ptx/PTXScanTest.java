/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.leica.ptx;

import org.amapvox.lidar.gridded.LDoublePoint;
import org.amapvox.lidar.gridded.LPoint;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
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
public class PTXScanTest {

    private PTXScan pTXScan;

    private final static double DELTA = 0.0000001;

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws URISyntaxException, IOException {
        
        PTXHeader header = new PTXHeader();
        header.setNZenith(2);
        header.setNAzimuth(5);
        header.setPointContainsIntensity(true);
        header.setPointContainsRGB(false);
        header.setPointInDoubleFormat(true);

        pTXScan = new PTXScan(new File(PTXScanTest.class.getResource("/scan_test.ptx").toURI()), header, 10);
        pTXScan.open();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testIteratorFiltering() {

        pTXScan.reset();

        pTXScan.setAzimuthIndex(3);
        pTXScan.setZenithIndex(1);

        Iterator<LPoint> iterator = pTXScan.iterator();

        int count = 0;

        while (iterator.hasNext()) {

            LPoint point = iterator.next();
            LDoublePoint dPoint = (LDoublePoint) point;

            if (count == 0) {
                assertEquals(dPoint.x, 8.0, 0);
                assertEquals(dPoint.y, 3.0, 0);
                assertEquals(dPoint.z, 8.0, 0);
                assertEquals(dPoint.intensity, 0.8, DELTA);
            }

            count++;

        }

        assertEquals(1, count);
    }

    @Test
    public void testIteratorNoFiltering() {

        Iterator<LPoint> iterator = pTXScan.iterator();

        int count = 0;
        while (iterator.hasNext()) {

            LPoint point = iterator.next();
            LDoublePoint dPoint = (LDoublePoint) point;

            switch (count) {
                case 0:
                    assertEquals(dPoint.x, 1.0, 0);
                    assertEquals(dPoint.y, 10.0, 0);
                    assertEquals(dPoint.z, 1.0, 0);
                    assertEquals(dPoint.intensity, 0.1, DELTA);
                    break;
                case 1:
                    assertEquals(dPoint.x, 2.0, 0);
                    assertEquals(dPoint.y, 9.0, 0);
                    assertEquals(dPoint.z, 2.0, 0);
                    assertEquals(dPoint.intensity, 0.2, DELTA);
                    break;
                case 2:
                    assertEquals(dPoint.x, 3.0, 0);
                    assertEquals(dPoint.y, 8.0, 0);
                    assertEquals(dPoint.z, 3.0, 0);
                    assertEquals(dPoint.intensity, 0.3, DELTA);
                    break;
                case 3:
                    assertEquals(dPoint.x, 4.0, 0);
                    assertEquals(dPoint.y, 7.0, 0);
                    assertEquals(dPoint.z, 4.0, 0);
                    assertEquals(dPoint.intensity, 0.4, DELTA);
                    break;
                case 4:
                    assertEquals(dPoint.x, 5.0, 0);
                    assertEquals(dPoint.y, 6.0, 0);
                    assertEquals(dPoint.z, 5.0, 0);
                    assertEquals(dPoint.intensity, 0.5, DELTA);
                    break;
                case 5:
                    assertEquals(dPoint.x, 6.0, 0);
                    assertEquals(dPoint.y, 5.0, 0);
                    assertEquals(dPoint.z, 6.0, 0);
                    assertEquals(dPoint.intensity, 0.6, DELTA);
                    break;
                case 6:
                    assertEquals(dPoint.x, 7.0, 0);
                    assertEquals(dPoint.y, 4.0, 0);
                    assertEquals(dPoint.z, 7.0, 0);
                    assertEquals(dPoint.intensity, 0.7, DELTA);
                    break;
                case 7:
                    assertEquals(dPoint.x, 8.0, 0);
                    assertEquals(dPoint.y, 3.0, 0);
                    assertEquals(dPoint.z, 8.0, 0);
                    assertEquals(dPoint.intensity, 0.8, DELTA);
                    break;
                case 8:
                    assertEquals(dPoint.x, 9.0, 0);
                    assertEquals(dPoint.y, 2.0, 0);
                    assertEquals(dPoint.z, 9.0, 0);
                    assertEquals(dPoint.intensity, 0.9, DELTA);
                    break;
                case 9:
                    assertEquals(dPoint.x, 10.0, 0);
                    assertEquals(dPoint.y, 1.0, 0);
                    assertEquals(dPoint.z, 10.0, 0);
                    assertEquals(dPoint.intensity, 1.0, DELTA);
                    break;
            }

            count++;

        }
    }
}
