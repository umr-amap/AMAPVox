/*
 * Copyright (C) 2017 UMR AMAP (botAnique et Modélisation de l'Architecture des Plantes et des végétations.
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
package fr.umramap.lidar.riegl;

import java.io.File;
import java.util.Iterator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Julien Heurtebize
 */
public class RxpExtractionTest {
    
    private final static File RXP_FILE = new File(RxpExtractionTest.class.getResource("/rxp/750209_181255.mon.rxp").getFile());
    
    public RxpExtractionTest() {
    }

    /**
     * Test of openRxpFile method, of class RxpExtraction.
     */
//    @Test
//    public void testOpenRxpFile() throws Exception {
//        System.out.println("openRxpFile");
      
//
//        RxpExtraction instance = new RxpExtraction();
//        int expResult = 0;
//        int result = instance.openRxpFile(RXP_FILE);
//        assertEquals(expResult, result);
//        
//        instance.close();
//    }

    /**
     * Test of close method, of class RxpExtraction.
     */
//    @Test
//    public void testClose() {
//        System.out.println("close");
//        RxpExtraction instance = new RxpExtraction();
//        instance.close();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of iterator method, of class RxpExtraction.
     */
//    @Test
//    public void testIterator() throws Exception  {
//        System.out.println("iterator");
//        RxpExtraction instance = new RxpExtraction();
//        instance.openRxpFile(RXP_FILE);
//        
//        Iterator<Shot> iterator = instance.iterator();
//        
//        while(iterator.hasNext()){
//            Shot next = iterator.next();
//            assertNotNull(next);
//        }
//        
//        instance.close();
//    }
    
}
