/*
 * Copyright (C) 2016 UMR AMAP (botAnique et Modélisation de l'Architecture des Plantes et des végétations.
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
package org.amapvox.lidar.riegl;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * This class is an utility to open and read rxp Riegl proprietary format</p>
 * <p>
 * The idea of this reader is to get an iterator on those files and getting shot
 * + echos (with reflectance)</p>
 * <p>
 * This class call a native JNI library using Riegl RivLib library.</p>
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class RxpExtraction implements Iterable<RxpShot>, AutoCloseable {

    private static native long createConnection();

    private static native void deleteConnection(long pointer);

    private static native int openConnection(long pointer, String file_name, int[] shotTypes);

    private static native void closeConnection(long pointer);

    private static native RxpShot getNextShot(long pointer);

    private static native boolean hasShot(long pointer);
    
    private static native long tellg(long pointer);

    public final static int REFLECTANCE = 2;
    public final static int DEVIATION = 3;
    public final static int AMPLITUDE = 4;
    public final static int TIME = 5;

    private long rxpPointer;
    private int currentShotID = -1;
    
    private File file;

    static {
        
        try {
            RiVLibLoader.loadLibrary();
        } catch (IOException ex) {
            Logger.getLogger(RxpExtraction.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }

    /**
     * Open a RXP file and instantiate a pointer
     *
     * @param file Rxp file to read
     * @param shotTypes Echoes attributes to import, variable parameters, can be {@link #REFLECTANCE},
     * {@link #DEVIATION}, {@link #AMPLITUDE}, {@link #TIME}. Import only
     * attributes you need.
     * 
     * @return -1 if an exception has occured, 0 if everything if fine and other
     * value for unknown exception
     * @throws IOException if path of the file is invalid
     * @throws Exception if an unknown exception occured when trying to open the
     * file
     */
    public final int open(File file, int... shotTypes) throws IOException, Exception {

        rxpPointer = createConnection();
        this.file = file;

        switch ((int) rxpPointer) {
            case -1:
                break;
            case -2:
                break;
            default:
                int result = RxpExtraction.openConnection(rxpPointer, file.getAbsolutePath(), shotTypes);

                switch (result) {
                    case -1:
                        throw new IOException("Rxp file " + file.getAbsolutePath() + " cannot be open");

                    case 0:
                        //logger.info("Rxp file " + file.getAbsolutePath() + " is open");
                        break;

                    default:
                        throw new Exception("Rxp file " + file.getAbsolutePath() + " reading error");
                }

                return result;
        }

        return -1;

    }

    /**
     * Close RXP file and delete pointer
     */
    @Override
    public void close() {
        closeConnection(rxpPointer);
        deleteConnection(rxpPointer);
    }
    
    /**
     * Return the number of bytes read during last successful read.
     * 
     * @return number of bytes read so far in RXP file.
     */
    public long progress() {
        return tellg(rxpPointer);
    }
    
    public File getFile() {
        return this.file;
    }

    @Override
    public Iterator<RxpShot> iterator() {

        final long finalRxpPointer = rxpPointer;

        Iterator<RxpShot> it = new Iterator<RxpShot>() {

            private RxpShot shot = null;
            private boolean hasNextCalled = false;
            public int nbShotsFailed = 0;

            private RxpShot nextShot() {

                if (hasShot(finalRxpPointer)) {

                    Object o = getNextShot(finalRxpPointer);

                    if (o == null) {
                        return null;
                    } else {
                        if (o instanceof RxpShot) {
                            shot = (org.amapvox.lidar.riegl.RxpShot) o;

                        } else {
                            nbShotsFailed++;
                            return nextShot();
                        }
                    }
                    return shot;

                } else {
                    return null;
                }
            }

            @Override
            public boolean hasNext() {

                if (!hasNextCalled) {
                    hasNextCalled = true;

                    shot = nextShot();
                }

                return shot != null;
            }

            @Override
            public RxpShot next() {
                currentShotID++;

                if (hasNextCalled) {
                    hasNextCalled = false;
                    return shot;
                } else {
                    return nextShot();
                }

            }
        };

        return it;
    }

    public int getShotID() {
        return currentShotID;
    }
}
