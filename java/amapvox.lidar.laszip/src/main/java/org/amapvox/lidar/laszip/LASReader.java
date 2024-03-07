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
import org.amapvox.commons.util.IterableWithException;
import org.amapvox.commons.util.IteratorWithException;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pverley
 */
public class LASReader implements Closeable, IterableWithException<LASPoint> {

    private final File file;
    private final PointerByReference laszip_reader = new PointerByReference();
    private LASHeader header;
    // static counter for loading/unloading laszip library
    private final static AtomicInteger LASZIP_COUNT = new AtomicInteger(0);
    // logger
    private final static Logger LOGGER = Logger.getLogger(LASReader.class.getCanonicalName());

    public LASReader(File file) throws FileNotFoundException, IOException {

        this.file = file;
        if (file == null) {
            throw new NullPointerException();
        }
        if (!file.canRead()) {
            throw new FileNotFoundException("Invalid file path");
        }

        open();
        readHeader();
    }

    public String getLASzipVersion() throws IOException {

        ByteByReference major = new ByteByReference();
        ByteByReference minor = new ByteByReference();
        ShortByReference revision = new ShortByReference();
        IntByReference build = new IntByReference();

        if (LASzipApi.laszip_get_version(major, minor, revision, build) != 0) {
            throw new IOException("DLL ERROR: getting LASzip DLL version number");
        }

        return ("LASzip "
                + major.getValue() + "."
                + minor.getValue() + "."
                + revision.getValue() + "."
                + build.getValue());
    }

    private void open() throws IOException {

        // load the dll if not loaded already
        synchronized (LASZIP_COUNT) {
            if (LASZIP_COUNT.incrementAndGet() == 1) {
                if (LASzipApi.laszip_load_dll_from_path(LASzipLoader.getLibrary()) != 0) {
                    throw new IOException("DLL ERROR: loading LASzip DLL");
                }
                LOGGER.log(Level.FINE, "LASzip DLL loaded (count = {0})", LASZIP_COUNT.get());
            } else {
                LOGGER.log(Level.FINE, "LASzip DLL load request ignored (count = {0})", LASZIP_COUNT.get());
            }
        }

        // create the reader
        if (LASzipApi.laszip_create(laszip_reader) != 0) {
            throw new IOException("DLL ERROR: creating laszip reader\n");
        }

        // open the reader
        IntByReference is_compressed = new IntByReference();
        if (LASzipApi.laszip_open_reader(laszip_reader.getValue(), file.getAbsolutePath(), is_compressed) != 0) {
            throw new IOException("DLL ERROR: opening laszip reader\n");
        }
    }

    private void readHeader() throws IOException {

        // read header
        PointerByReference header_ptr = new PointerByReference();
        if (LASzipApi.laszip_get_header_pointer(laszip_reader.getValue(), header_ptr) != 0) {
            throw new IOException("DLL ERROR: getting header pointer from laszip reader\n");
        }

        // cast it to Java class
        header = LASHeader.read(header_ptr.getValue());
    }

    public LASHeader getHeader() {
        return header;
    }

    @Override
    public void close() throws IOException {

        // close the reader
        if (LASzipApi.laszip_close_reader(laszip_reader.getValue()) != 0) {
            throw new IOException("DLL ERROR: closing laszip reader\n");
        }

        // destroy the reader
        if (LASzipApi.laszip_destroy(laszip_reader.getValue()) != 0) {
            throw new IOException("DLL ERROR: destroying laszip reader\n");
        }

        // unload dll unless still used by other instance
        synchronized (LASZIP_COUNT) {
            if (LASZIP_COUNT.decrementAndGet() == 0) {
                if (LASzipApi.laszip_unload_dll() != 0) {
                    throw new IOException("DLL ERROR: unloading LASzip DLL\n");
                }
                LOGGER.log(Level.FINE, "LASzip DLL unloaded (count = {0})", LASZIP_COUNT.get());
            } else {
                LOGGER.log(Level.FINE, "LASzip DLL unload request ignored (count = {0})", LASZIP_COUNT.get());
            }
        }
    }

    @Override
    public IteratorWithException<LASPoint> iterator() throws IOException {

        long n = header.getNPoint();

        // point pointer
        PointerByReference point_laszip_ptr = new PointerByReference();
        if (LASzipApi.laszip_get_point_pointer(laszip_reader.getValue(), point_laszip_ptr) != 0) {
            throw new IOException("DLL ERROR: getting point pointer from laszip reader\n");
        }

        IteratorWithException<LASPoint> it = new IteratorWithException() {

            long p = 0;

            @Override
            public boolean hasNext() {
                return p < n;
            }

            @Override
            public LASPoint next() throws IOException {

                // read laszip point
                if (LASzipApi.laszip_read_point(laszip_reader.getValue()) != 0) {
                    throw new IOException("DLL ERROR: reading point" + p);
                }

                // transform laszip point into jlas point
                LASPoint point = new LASPoint();
                LASzipApi.jlas_copy_point(point_laszip_ptr.getValue(), point);

                // increment counter
                p++;

                return point;
            }
        };

        return it;
    }
}
