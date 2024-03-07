/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.laszip;

import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author pverley
 */
public class LASzipApiTest {
    
    private final static Logger LOGGER = Logger.getLogger(LASzipApiTest.class.getCanonicalName());
    private final static Level LOG_LEVEL = Level.FINE;
    private final boolean JNA_LOGS = false;
    
    private final static String LAS_FILE = LASzipApiTest.class.getResource("/las/5points_14.las").getFile();
    private final static String LAZ_FILE = LASzipApiTest.class.getResource("/laz/5points_14.laz").getFile();
    
    @Test
    public void testLAS() {
        testLASzipApi(LAS_FILE);
    }

    @Test
    public void testLAZ() {
        testLASzipApi(LAZ_FILE);
    }
    
    private void testLASzipApi(String file) {
        
        try {

            // additional JNA logging messages
            System.setProperty("jna.debug_load", String.valueOf(JNA_LOGS));

            // loading LASzip dll
            if (LASzipApi.laszip_load_dll_from_path(LASzipLoader.getLibrary()) != 0) {
                Assert.fail("DLL ERROR: loading LASzip DLL");
            }
            LOGGER.log(LOG_LEVEL, "LASzip DLL loaded {0}", LASzipLoader.getLibrary());
            
            ByteByReference major = new ByteByReference();
            ByteByReference minor = new ByteByReference();
            ShortByReference revision = new ShortByReference();
            IntByReference build = new IntByReference();
            
            if (LASzipApi.laszip_get_version(major, minor, revision, build) != 0) {
                Assert.fail("DLL ERROR: getting LASzip DLL version number");
            }
            LOGGER.log(LOG_LEVEL, "LAZzip version {0}.{1}.{2}.{3}", new Object[]{
                major.getValue(), minor.getValue(), revision.getValue(), build.getValue()
            });

            // create the reader
            PointerByReference laszip_reader = new PointerByReference();
            if (LASzipApi.laszip_create(laszip_reader) != 0) {
                Assert.fail("DLL ERROR: creating laszip reader\n");
            }
            LOGGER.log(LOG_LEVEL, "LASZIP reader created");

            // open the reader
//            if (!Files.exists(Path.of(file))) { // java 11
            if (!new File(file).exists()) { // java 8
                Assert.fail("File not found: " + file);
            }
            LOGGER.log(LOG_LEVEL, "LAS/LAZ file {0}", file);
            
            IntByReference is_compressed = new IntByReference();
            
            if (LASzipApi.laszip_open_reader(laszip_reader.getValue(), new File(file).toString(), is_compressed) != 0) {
                Assert.fail("DLL ERROR: opening laszip reader\n");
            }
            LOGGER.log(LOG_LEVEL, "LASZIP reader opened. Compressed? {0}", is_compressed.getValue());

            // read header
            PointerByReference header_ptr = new PointerByReference();
            if (LASzipApi.laszip_get_header_pointer(laszip_reader.getValue(), header_ptr) != 0) {
                Assert.fail("DLL ERROR: getting header pointer from laszip reader\n");
            }
            
            LASHeader header = LASHeader.read(header_ptr.getValue());
            LOGGER.log(LOG_LEVEL, header.getClass().getCanonicalName());
            LOGGER.log(LOG_LEVEL, header.toString());

            // npoints
            long npoint = header.getNPoint();
            LOGGER.log(LOG_LEVEL, "Number of points? {0}", npoint);

            // point pointer
            PointerByReference point_laszip_ptr = new PointerByReference();
            if (LASzipApi.laszip_get_point_pointer(laszip_reader.getValue(), point_laszip_ptr) != 0) {
                Assert.fail("DLL ERROR: getting point pointer from laszip reader\n");
            }

            // read some points
            int p = 0;
            while (p < npoint) {
                
                if (LASzipApi.laszip_read_point(laszip_reader.getValue()) != 0) {
                    Assert.fail("DLL ERROR: reading point" + p);
                }

                // transform laszip point into jlas point
                LASPoint point = new LASPoint();
                LASzipApi.jlas_copy_point(point_laszip_ptr.getValue(), point);
                Assert.assertNotNull(point);
                p++;
            }
            LOGGER.log(LOG_LEVEL, "Read successfully {0} points", p);

            // close the reader
            if (LASzipApi.laszip_close_reader(laszip_reader.getValue()) != 0) {
                Assert.fail("DLL ERROR: closing laszip reader\n");
            }
            LOGGER.log(LOG_LEVEL, "LASZIP reader closed");

            // destroy the reader
            if (LASzipApi.laszip_destroy(laszip_reader.getValue()) != 0) {
                Assert.fail("DLL ERROR: destroying laszip reader\n");
            }
            LOGGER.log(LOG_LEVEL, "LASZIP reader destroyed");

            // unload the dll
            if (LASzipApi.laszip_unload_dll() != 0) {
                Assert.fail("DLL ERROR: unloading LASzip DLL\n");
            }
            LOGGER.log(LOG_LEVEL, "LASZIP DLL unloaded");
            
        } catch (IOException ex) {
            Logger.getLogger(LASzipApiTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
