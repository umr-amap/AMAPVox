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
package org.amapvox.lidar.jlas;

import org.amapvox.commons.util.NativeLoader;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * This class is devoted to read a LASzip file (*.laz), it allows to get the header, <br>
 * in a simple basic format (V1.0) and get an iterator on the points of the file.<br>
 * It uses a native library compiled for 64 bits systems, Linux and Windows.
 * 
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class LazReader implements Iterable<LasPoint>{
    
    private final static String NATIVE_LIBRARY_NAME = "LasZipLibrary";
    
//    private native void afficherBonjour();
    private native long instantiateLasZip();
    private native void deleteLasZip(long pointer);
    private native int open(long pointer, String file_name);
//    private native void readAllPoints(long pointer);
    private native LasPoint getNextPoint(long pointer);
    private native LasHeader getBasicHeader(long pointer);
    
    private long lasZipPointer;
    private LasHeader header;
    
    /**
     * Instantiate the laz reader
     */
    public LazReader(){
        
    }
    
    static {
        
        NativeLoader loader = new NativeLoader();
        try {
            loader.loadLibrary(NATIVE_LIBRARY_NAME, LazReader.class);
        } catch (IOException ex) {
            System.err.println("Cannot load "+NATIVE_LIBRARY_NAME+" library, cause : " + ex.getMessage());
        }
        
    }
    
    /**
     * Open laz file, instantiate a pointer on this file
     * @param file Laz file to read
     * @throws IOException throws an IOException happen when path of the file is invalid
     * @throws Exception This error can occured when trying to open the file or read the header
     */
    public void openLazFile(File file) throws IOException, Exception{
        
        try{
            lasZipPointer = instantiateLasZip();
        }catch(Exception e){
            throw new Exception("Cannot initialize laszip pointer", e);
        }
        
        
        int result = open(lasZipPointer, file.getAbsolutePath());
            
        switch(result){
            case -1:
                throw new IOException("Laz file "+file.getAbsolutePath()+" cannot be open");
            case 0:
                break;

            default:
                throw new Exception("Laz file "+file.getAbsolutePath()+" reading error");
        }
        
        try{
            header = getBasicHeader(lasZipPointer);
        }catch(Exception e){
            throw new Exception("Cannot get laz header", e);
        }
        
        if(header == null){
            throw new Exception("Cannot get laz header");
        }
        
    }
    
    /**
     * Close laz file, destroy the pointer
     */
    public void close(){
        deleteLasZip(lasZipPointer);
    }

    /**
     *
     * @return The header of laz file, the basic version of it (V1.0)
     */
    public LasHeader getHeader() {
        return header;
    }

    /**
     * Iterates through the points of the las file. 
     * Points are not kept in memory.
     * @return 
     */
    @Override
    public Iterator<LasPoint> iterator() {
        
        long size = header.getNumberOfPointrecords();
        
        Iterator<LasPoint> it = new Iterator<LasPoint>() {
            
            long count = 0;
            
            @Override
            public boolean hasNext() {
                return count<size;
            }

            @Override
            public LasPoint next() {
                count++;
                return getNextPoint(lasZipPointer);
            }
        };
        
        return it;
    }
}
