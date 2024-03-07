/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.raster.asc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class AsciiGridHelper {

    
    public static Raster readFromAscFile(File ascFile) throws Exception{
        
        final String pathFile = ascFile.getAbsolutePath();
        
        
        String line;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(ascFile))) {
            
            int nbCols = Integer.valueOf(reader.readLine().split(" ", 2)[1].trim());
            int nbRows = Integer.valueOf(reader.readLine().split(" ", 2)[1].trim());
            float xLeftCorner = Float.valueOf(reader.readLine().split(" ", 2)[1].trim());
            float yLeftCorner = Float.valueOf(reader.readLine().split(" ", 2)[1].trim());
            float step = Float.valueOf(reader.readLine().split(" ", 2)[1].trim());
            float noDataValue = Float.valueOf(reader.readLine().split(" ", 2)[1].trim());
            
            float[][] zArray = new float[nbCols][nbRows];
            
            float z;
            
            int yIndex = 0;
            
            while((line = reader.readLine()) != null){
                
                line = line.trim();
                String[] values = line.split(" ", -1);
                if(values.length != nbCols){
                    throw new Exception("nb columns different from ncols header value");
                }
                
                for(int xIndex=0;xIndex<values.length;xIndex++){
                    
                    z = Float.valueOf(values[xIndex]);
                    if(z == noDataValue){
                        z = Float.NaN;
                    }
                    zArray[xIndex][yIndex] = z;
                    
                }
                
                yIndex++;
            }
            
            Raster terrain = new Raster(pathFile, zArray, xLeftCorner, yLeftCorner, step, nbCols, nbRows);
        
            return terrain;
            
        } catch (IOException ex) {
            throw ex;
        }
    }
    
    public static void write(File output, Raster raster, boolean invertY) throws IOException{
        write(output, raster.getzArray(),
                raster.getColNumber(), raster.getRowNumber(), 
                raster.getxLeftLowerCorner(), raster.getyLeftLowerCorner(),
                raster.getCellSize(), invertY);
    }
    
    /**
     * Write the raster in ascii grid format (*.asc)
     * @param output Output file
     * @param zArray 2d (x,y) array containing z values
     * @param colNumber number of columns
     * @param rowNumber number of rows
     * @param xLeftLowerCorner lower left x corner
     * @param yLeftLowerCorner lower left y corner
     * @param cellSize size of a cell
     * @param invertY
     * @throws IOException Throws an IOException when output path is invalid or other
     */
    public static void write(File output, float[][] zArray, int colNumber, int rowNumber, float xLeftLowerCorner, float yLeftLowerCorner, float cellSize, boolean invertY) throws IOException{
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))){
            
            float noDataValue = -9999.000000f;
            writer.write("ncols "+colNumber+"\n");
            writer.write("nrows "+rowNumber+"\n");
            writer.write("xllcorner "+xLeftLowerCorner+"\n");
            writer.write("yllcorner "+yLeftLowerCorner+"\n");
            writer.write("cellsize "+cellSize+"\n");
            writer.write("nodata_value "+noDataValue+"\n");
            
            for(int j = 0;j<rowNumber;j++){
            
                StringBuilder stringBuilder = new StringBuilder();

                for(int i = 0 ; i<colNumber ; i++){

                    int yIndex;
                    if(invertY){
                        yIndex = rowNumber - j - 1;
                    }else{
                        yIndex = j;
                    }
                    
                    if (Float.isNaN(zArray[i][yIndex])) {
                        stringBuilder.append(noDataValue);
                    } else {
                        stringBuilder.append(zArray[i][yIndex]);
                    }
                    
                    stringBuilder.append(" ");
                }

                writer.write(stringBuilder.toString()+"\n");
            }
            
            
        } catch (IOException ex) {
            throw new IOException("Cannot write dtm file "+output.getAbsolutePath(), ex);
        }
    }
   
}
