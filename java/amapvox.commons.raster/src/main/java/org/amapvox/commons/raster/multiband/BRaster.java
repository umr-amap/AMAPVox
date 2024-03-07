/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */

package org.amapvox.commons.raster.multiband;

import static org.amapvox.commons.raster.multiband.BCommon.getBooleanBitsArray;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author calcul
 */


public abstract class BRaster {
        
    protected BHeader header;
    protected File outputFile;
    protected List<Band> bands;
    
    public abstract File writeImage() throws IOException, FileNotFoundException;
    
    public BRaster(File outputFile, BHeader header){
        bands = new ArrayList<>();
        this.header = header;
        this.outputFile = outputFile;
        
        if(!outputFile.getName().endsWith(".bsq")){
            this.outputFile = new File(outputFile.getAbsolutePath()+".bsq");
        }
        
        for(int i=0;i<header.getNbands();i++){
            bands.add(new Band(header.getNcols(), header.getNrows(), header.getNbits()));
        }
    }
    
    public void setOutputFile(File outputFile){
        
        if(!outputFile.getName().endsWith(".bsq")){
            outputFile = new File(outputFile.getAbsolutePath()+".bsq");
        }
        
        this.outputFile = outputFile;
    }
    
    public File writeHeader() throws IOException{
        
        String rasterPath = outputFile.getAbsolutePath();
        File headerFile = new File(rasterPath.substring(0, rasterPath.length()-4)+".hdr");
        
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(headerFile))) {
            writer.write(header.toString());
        } catch (IOException ex) {
            throw new IOException("Cannot write header file "+headerFile.getAbsolutePath(), ex);
        }
        
        return headerFile;
    }
    
    public void setPixel(int posX, int posY, int bandID, long value) throws Exception{
        
        String binaryString = Long.toBinaryString(value);
        byte[] bval = new BigInteger(binaryString, 2).toByteArray();
        
        byte b0 = 0x0, b1 = 0x0, b2 = 0x0, b3 = 0x0;
        if (bval.length > 0) {
            b0 = bval[0];
        }
        if (bval.length > 1) {
            b1 = bval[1];
        }
        if (bval.length > 2) {
            b2 = bval[2];
        }
        if (bval.length > 3) {
            b3 = bval[3];
        }
        
        setPixel(posX, posY, bandID, b3, b2, b1, b0);
    }
    
    
    public void setPixel(int posX, int posY, int bandID, byte... bytes) throws Exception{
        
        if(bandID > bands.size()){
            throw new Exception("Cannot set pixel to band "+bandID+" ; band doesn't exist");
        }
        
        if(header.getByteNumber() != bytes.length){
            throw new Exception("Cannot set pixel to band, bits number from header and byte array length doesn't match, header byte number: "+
                    header.getByteNumber()+", byte array length: "+bytes.length);
        }
        
        boolean[] bits = new boolean[header.getNbits().getNumberOfBits()];
        
        int[] unsignedByte = new int[bytes.length];
        
        for(int i = 0 ; i<unsignedByte.length ; i++){
            unsignedByte[i] = bytes[i]&0xff;
        }
        
        switch(header.getNbits()){
            case N_BITS_1: //black or white
                //bits[0] = (color.getRed()/255) != 0;
                break;
            case N_BITS_4: //16 differents colors
                break;
            case N_BITS_8: //256 differents colors
                bits = getBooleanBitsArray(unsignedByte[0], 8);
                break;
            case N_BITS_16: //65536 colors
                
                break;
            case N_BITS_32:
                
                boolean[] bitsRed = getBooleanBitsArray(unsignedByte[0], 8);
                boolean[] bitsGreen = getBooleanBitsArray(unsignedByte[1], 8);
                boolean[] bitsBlue = getBooleanBitsArray(unsignedByte[2], 8);
                boolean[] bitsAlpha = getBooleanBitsArray(unsignedByte[3], 8);
                
                concatenateArrays(bits, bitsRed, bitsGreen, bitsBlue, bitsAlpha);
                
                break;
        }
        
        
        
        bands.get(bandID).setPixel(posX, posY, bits);
    }
    
    private static void concatenateArrays(boolean[] dest, boolean[]... srcs){
        
        int count = 0;
        for(int j=0;j<srcs.length;j++){
            for(int k=0;k<srcs[j].length;k++){
                dest[count] = srcs[j][k];
                count++;
            }
        }
    }
    
    
    
    public abstract void writeColorFile();
    public abstract void writeStatisticsFile();
    
}
