package org.amapvox.lidar.laszip;


import com.sun.jna.Platform;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author pverley
 */
public class LASzipLoader {
    
    public static String getLibrary() throws IOException {

        String osArch = System.getProperty("os.arch");
        String osName = System.getProperty("os.name").toLowerCase();
        
        // 64-Bit only
        if (!Platform.is64Bit()) {
            throw new UnsupportedOperationException("LASzip library has only been compiled for 64-Bit architectures.");
        }

        // library path
        String libPath;
        if (Platform.isWindows()) {
            libPath = "LASzip64.dll";
        } else if (Platform.isLinux()) {
            libPath = "liblaszip64.so";
        } else {
            throw new UnsupportedOperationException("LASzip library, platform " + osName + ":" + osArch + " not supported.");
        }

        // input stream
        InputStream is = LASzipLoader.class.getResource(libPath).openStream();
        if (null == is) {
            throw new NullPointerException("LASzip library not found " + libPath);
        }

        // copy library to temporary file
        Path file = Files.createTempFile("laszip-", ".tmp");
        Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);

        // delete tmp file
        file.toFile().deleteOnExit();
        
        return file.toString();
    }
    
}
