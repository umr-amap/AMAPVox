/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class can be used to load dynamic library(.so, .dll).
 * This get the library from a stream, usually into the .jar archive, 
 * extract it in a temp folder (can change according to the os) and load it.
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class NativeLoader {


    public NativeLoader() {
    }

    public void loadLibrary(String library, Class c) throws IOException {
        try {
            String path = saveLibrary(library, c);
            //logger.info(path);
            
            System.load(path);
        } catch (IOException e) {
            throw e;
        } catch (SecurityException e) {
            throw e;
        }catch (Exception e) {
            throw e;
        }
    }


    private String getOSSpecificLibraryName(String library, boolean includePath) {
        String osArch = System.getProperty("os.arch");
        String osName = System.getProperty("os.name").toLowerCase();
        String name;
        String path;

        if (osName.startsWith("win")) {
            if (osArch.equalsIgnoreCase("x86")) {
                name = library + ".dll";
                path = "win-x86/";
            } else {
                name = library + ".dll";
                path = "winx64/";
            }
        } else if (osName.startsWith("linux")) {
            if (osArch.equalsIgnoreCase("amd64")) {
                name = library + ".so";
                path = "linux_x86_64/";
            } else if (osArch.equalsIgnoreCase("ia64")) {
                name = library + ".so";
                path = "linux-ia64/";
            } else if (osArch.equalsIgnoreCase("i386")) {
                name = library + ".so";
                path = "linux-x86/";
            } else {
                throw new UnsupportedOperationException("Platform " + osName + ":" + osArch + " not supported");
            }
        } else {
            throw new UnsupportedOperationException("Platform " + osName + ":" + osArch + " not supported");
        }

        return includePath ? path + name : name;
    }

    private String saveLibrary(String library, Class c) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        try {
            String libraryName = getOSSpecificLibraryName(library, true);
            in = c.getClassLoader().getResourceAsStream("lib/" + libraryName);
            String tmpDirName = System.getProperty("java.io.tmpdir");
            File tmpDir = new File(tmpDirName);
            if (!tmpDir.exists()) {
                tmpDir.mkdir();
            }
            File file = File.createTempFile(library + "-", ".tmp", tmpDir);
            // Clean up the file when exiting
            file.deleteOnExit();
            out = new FileOutputStream(file);

            int cnt;
            byte buf[] = new byte[16 * 1024];
            // copy until done.
            while ((cnt = in.read(buf)) >= 1) {
                out.write(buf, 0, cnt);
            }
            //logger.info("Saved libfile: " + file.getAbsoluteFile());
            return file.getAbsolutePath();
            
        } catch(Exception e){
            throw e;
        }finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                    throw ignore;
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignore) {
                    throw ignore;
                }
            }
        }
    }
}