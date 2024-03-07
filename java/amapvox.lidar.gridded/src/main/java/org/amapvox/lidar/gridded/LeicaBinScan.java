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
package org.amapvox.lidar.gridded;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

/**
 * 
 * @author Julien Heurtebize
 */
public class LeicaBinScan extends GriddedPointScan{

    public LeicaBinScan(File file) {
        super(file);
    }
    
    
//    private long currentLineIndex;
//    
//    private static final int HEADER_SIZE = 10;
//
//    /**
//     * The line indice representing offset to point data record of the scan.
//     */
//    public long offset;
//
//    /**
//     * The number of points of the scan, computed as (columns number) * (rows number), which includes invalid points.
//     */
//    public long nbPoints;
//    
//    public static void main(String[] args) throws IOException {
//        
//        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(new File("/media/forestview01/BDLidar/TLS/Test_scanners/Vezenobres/Leica C10/raw data/Station-002/SW-002/SCANS/Scan-0003.bin"))));
//        
//        for(int i = 0;i< 272;i++){
//            dis.readByte();
//            nbBytesRead++;
//        }
//        
//        /*byte byte1 = dis.readByte();
//        byte byte2 = dis.readByte();
//        byte byte3 = dis.readByte();
//        byte byte4 = dis.readByte();*/
//        
//        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/home/julien/Bureau/tmp.txt")));
//        
//        try{
//            int count = 0;
//            while(true){
//                
//                if(count == 4712){
//                    getNextFloat(dis);
//                    count = 0;
//                }
//                
//                float intensity = getNextFloat(dis);
//                
//                if(intensity < 0){
//                    System.out.println("test");
//                }
//                
//                float unknow = getNextFloat(dis);
//                
//                float x = getNextFloat(dis);
//                float y = getNextFloat(dis);
//                float z = getNextFloat(dis);
//
//                writer.write(x+" "+y+" "+z+" "+intensity+" "+unknow+"\n");
//                //System.out.println(intensity+"\t"+x+"\t"+y+"\t"+z);
//                count++;
//            }
//        }catch(Exception ex){
//            dis.close();
//            writer.close();
//        }   
//        
//        writer.close();
//        
//    }
//    
//    private float getNextFloat(DataInputStream dis) throws IOException{
//        
//        float result = LittleEndianUtility.toFloat(dis.readByte(),
//                                                    dis.readByte(),
//                                                    dis.readByte(),
//                                                    dis.readByte());
//        
//        nbByteRead+=4;
//        return result;
//    }
//    
//    private double getNextDouble(DataInputStream dis) throws IOException{
//        
//        double result = LittleEndianUtility.toDouble(dis.readByte(),
//                                                    dis.readByte(),
//                                                    dis.readByte(),
//                                                    dis.readByte(),
//                                                    dis.readByte(),
//                                                    dis.readByte(),
//                                                    dis.readByte(),
//                                                    dis.readByte());
//        nbByteRead+=8;
//        return result;
//    }
//    
//    private void skipLines(BufferedReader reader, long nbLinesToSkip) throws IOException{
//        
//        int nbLinesSkipped = 0;
//
//        while(nbLinesSkipped < nbLinesToSkip){
//
//            reader.readLine();
//            nbLinesSkipped++;
//        }
//    }
//
//    @Override
//    public Iterator<LPoint> iterator() {
//        
//        Iterator it = null;
//        final DataInputStream dis;
//        
//        try{
//            dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
//            
//            it = new Iterator<LPoint> () {
//                
//                LPoint currentPoint;
//                long totalLinesRead = 0;
//                
//                int currentColumnIndex = 0;
//                int currentRowIndex = -1;
//                
//                final int finalColumnIndex = endColumnIndex; //end column index is included
//                final int finalRowIndex = endRowIndex; //end row index is included
//                
//                boolean initialized = false;
//                boolean isFinish = false;
//                
//                private String readLine(BufferedReader reader) throws IOException{
//                    
//                    String line = reader.readLine();
//                    
//                    currentRowIndex++;
//                    totalLinesRead++;
//                    
//                    if(currentRowIndex >= header.getNumRows()){
//                        currentRowIndex = 0;
//                        currentColumnIndex++;
//                    }
//                    
//                    return line;
//                }
//                
//                
//                @Override
//                public boolean hasNext() {
//                    
//                    try {
//                        
//                        LPoint point;
//                        
//                        if(!initialized){
//                            
//                            skipLines(dis, offset);
//                            
//                            initialized = true;
//                        }
//                        
//                        if (totalLinesRead == nbPoints) {
//                            
//                            dis.close();
//                            return false;
//                        }
//                        
//                        String currentLine;
//                        
//                        do{
//                            if(isFinish){
//                                dis.close();
//                                return false;
//                            }
//
//                            currentLine = readLine(dis);
//                            
//                            if(currentLine == null){
//                                dis.close();
//                                return false;
//                            }
//                            
//                            if(currentColumnIndex >= finalColumnIndex && (currentRowIndex) >= finalRowIndex){
//                                isFinish = true;
//                            }
//                            
//                        }while((currentRowIndex) < startRowIndex 
//                                || (currentRowIndex) > finalRowIndex
//                                || currentColumnIndex < startColumnIndex);
//                        
//                        
//                        String[] split = currentLine.split(" ");
//
//                        point = new LDoublePoint();
//
//                        ((LDoublePoint)point).x = Double.valueOf(split[0]);
//                        ((LDoublePoint)point).y = Double.valueOf(split[1]);
//                        ((LDoublePoint)point).z = Double.valueOf(split[2]);
//
//                        if(((LDoublePoint)point).x == 0 && ((LDoublePoint)point).y == 0 && ((LDoublePoint)point).z == 0){
//                            
//                            if(!returnInvalidPoint){
//                                hasNext();
//                            }else{
//                                
//                                point = new LEmptyPoint();
//                                
//                                point.rowIndex = currentRowIndex;
//                                point.columnIndex = currentColumnIndex;
//                                
//                                point.valid = false;
//
//                                currentPoint = point;
//                            }
//                        }else{
//                            point.valid = true;
//                            
//                            point.rowIndex = currentRowIndex;
//                            point.columnIndex = currentColumnIndex;
//                            
//                            if(split.length > 3){
//
//                                point.intensity = Float.valueOf(split[3]);
//
//                                if(split.length > 6){
//                                    point.red = Integer.valueOf(split[4]);
//                                    point.green = Integer.valueOf(split[5]);
//                                    point.blue = Integer.valueOf(split[6]);
//                                }
//                            }
//
//                            currentPoint = point;
//                        }
//                        
//                    } catch (IOException | ArrayIndexOutOfBoundsException | NumberFormatException e) {
//                        try {
//                            dis.close();
//                        } catch (IOException ex) {
//                            Logger.getLogger(PTXScan.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                        return false;
//                    }
//                    
//                    return true;
//                }
//
//                @Override
//                public LPoint next() {
//                    return currentPoint;
//                }
//            };
//            
//        } catch (FileNotFoundException ex) {
//            throw new RuntimeException(ex);
//        }
//        
//        return it;
//    }
//
//    @Override
//    public void openScanFile(File file) throws FileNotFoundException, IOException, Exception {
//        
//        //test file existence
//        FileReader reader = new FileReader(file);
//        reader.close();
//    }

    @Override
    public void readHeader() throws FileNotFoundException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void readPointCloud() throws FileNotFoundException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<LPoint> iterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
