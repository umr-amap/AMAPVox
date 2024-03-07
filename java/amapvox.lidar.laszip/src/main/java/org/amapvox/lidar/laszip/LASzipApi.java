/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.laszip;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;

/**
 *
 * @author pverley
 */
public class LASzipApi {
    
    public static native int jlas_copy_point(Pointer point_in, LASPoint point_out);
    
    public static native int laszip_load_dll();
    
    public static native int laszip_load_dll_from_path(String full_path);
    
    public static native int laszip_unload_dll();
    
    public static native int laszip_get_version(ByteByReference major,
            ByteByReference minor,
            ShortByReference revision,
            IntByReference build);
    
    public static native int laszip_create(PointerByReference pointer);
    
    public static native int laszip_destroy(Pointer pointer);
    
    public static native int laszip_open_reader(Pointer pointer, String uri, IntByReference is_compressed);
    
    public static native int laszip_close_reader(Pointer pointer);
    
    public static native int laszip_clean(PointerByReference pointer);
    
    public static native int laszip_get_header_pointer(Pointer pointer, PointerByReference header);
    
    public static native int laszip_get_point_pointer(Pointer pointer, PointerByReference point);
    
    public static native int laszip_read_point(Pointer pointer);
    
    static {
        Native.register("JLasApi64");
    }
    
}
