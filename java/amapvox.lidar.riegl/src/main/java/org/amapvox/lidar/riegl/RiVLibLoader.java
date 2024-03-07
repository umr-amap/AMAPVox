/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.riegl;

import com.sun.jna.Platform;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Load RiVLib dynamic library (.so, .dll, .dynlib). Extracts the library from
 * the JAR, copies it to a tmp folder and loads it. Throws error message for
 * unsupported architectures.
 *
 * @author Philippe Verley (philippe.verley@ird.fr)
 */
public class RiVLibLoader {

    /**
     * For Unix-like system, looks for libRiVLibJNI.so For Windows system, looks
     * for RiVLibJNI.dll
     */
    private final static String RIVLIB_LIBRARY_NAME = "RiVLibJNI";

    public static void loadLibrary() throws IOException {

        String osArch = System.getProperty("os.arch");
        String osName = System.getProperty("os.name").toLowerCase();
        
        // 64-Bit only
        if (!Platform.is64Bit()) {
            throw new UnsupportedOperationException("RiVLib library has only been compiled for 64-Bit architectures.");
        }
        
        // library path
        String libPath;
        if (Platform.isWindows()) {
            libPath = RIVLIB_LIBRARY_NAME + ".dll";
        } else if (Platform.isLinux()) {
            libPath = "lib" + RIVLIB_LIBRARY_NAME + ".so";
        } else {
            throw new UnsupportedOperationException("RiVLib library, platform " + osName + ":" + osArch + " not supported.");
        }

        // input stream
        InputStream is = RiVLibLoader.class.getResource(libPath).openStream();
        if (null == is) {
            throw new NullPointerException("RiVLib library not found " + libPath);
        }

        // copy library to temporary file
        Path file = Files.createTempFile(RIVLIB_LIBRARY_NAME + "-", ".tmp");
        Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);

        // load library from temp location
        System.load(file.toString());

        // delete tmp file
        file.toFile().deleteOnExit();
    }

}
