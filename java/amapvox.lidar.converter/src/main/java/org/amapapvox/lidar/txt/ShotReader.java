/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapapvox.lidar.txt;

import org.amapvox.commons.util.IterableWithException;
import org.amapvox.commons.util.IteratorWithException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Julien Heurtebize
 */
public class ShotReader implements IterableWithException<Shot>{

    private final File file;
    
    private final ShotFileContext context;
    
    public ShotReader(File file) throws Exception{
        
        this.file = file;
        
        try(BufferedReader reader = new BufferedReader(new FileReader(file))){
            
            String columnTypes = reader.readLine();
            String[] typesSplit = columnTypes.split(" ");
            
            if(typesSplit.length < 8){
                throw new Exception("Invalid header !");
            }
            
            String columnNames = reader.readLine();
            
            String[] namesSplit = columnNames.split(" ");
            
            if(namesSplit.length < 8){
                throw new Exception("Invalid header !");
            }else if(typesSplit.length != namesSplit.length){
                throw new Exception("Invalid header !");
            }
            
            Column[] extraColumns = new Column[typesSplit.length - 8];
            
            for (int i = 8, j = 0; i < typesSplit.length; i++, j++) {
                
                String columnType = typesSplit[i];
                String columnName = namesSplit[i];
                
                extraColumns[j] = new Column(columnName, Column.Type.fromString(columnType));
            }
            
            for (int i = 0; i < namesSplit.length; i++) {
                
                String columnName = namesSplit[i];
                
                switch(i){
                    case 0:
                        if(!columnName.equals("shotID")){
                            throw new Exception("shotID is not the 1st attribute !");
                        }
                        break;
                    case 1:
                        if(!columnName.equals("xOrigin")){
                            throw new Exception("xOrigin is not the 2nd attribute !");
                        }
                        break;
                    case 2:
                        if(!columnName.equals("yOrigin")){
                            throw new Exception("yOrigin is not the 3rd attribute !");
                        }
                        break;
                    case 3:
                        if(!columnName.equals("zOrigin")){
                            throw new Exception("zOrigin is not the 4th attribute !");
                        }
                        break;
                    case 4:
                        if(!columnName.equals("xDirection")){
                            throw new Exception("xDirection is not the 5th attribute !");
                        }
                        break;
                    case 5:
                        if(!columnName.equals("yDirection")){
                            throw new Exception("yDirection is not the 6th attribute !");
                        }
                        break;
                    case 6:
                        if(!columnName.equals("zDirection")){
                            throw new Exception("zDirection is not the 7th attribute !");
                        }
                        break;
                    case 7:
                        if(!columnName.equals("range")){
                            throw new Exception("range is not the 8th attribute !");
                        }
                        break;
                }
            }
            
            context = new ShotFileContext(extraColumns);
            
        } catch (FileNotFoundException ex) {
            throw ex;
        }
    }
    
    private class ShotLine{
        public int shotID;

        public double xOrigin;
        public double yOrigin;
        public double zOrigin;

        public double xDirection;
        public double yDirection;
        public double zDirection;

        public ShotLine(int shotID,
                        double xOrigin, double yOrigin, double zOrigin,
                        double xDirection, double yDirection, double zDirection) {
            this.shotID = shotID;
            this.xOrigin = xOrigin;
            this.yOrigin = yOrigin;
            this.zOrigin = zOrigin;
            this.xDirection = xDirection;
            this.yDirection = yDirection;
            this.zDirection = zDirection;
        }
    }
    
    private class NonEmptyShotLine extends ShotLine{
        
        
        public double range;
        public Object[] attributes;

        public NonEmptyShotLine(int shotID,
                                double xOrigin, double yOrigin, double zOrigin,
                                double xDirection, double yDirection, double zDirection,
                                double range, Object... attributes) {
            super(shotID, xOrigin, yOrigin, zOrigin, xDirection, yDirection, zDirection);
            
            this.range = range;
            this.attributes = attributes;
        }
        
    }
    
    

    @Override
    public IteratorWithException<Shot> iterator() throws Exception{
        
        BufferedReader reader = new BufferedReader(new FileReader(file));
        
        //skip headers
        reader.readLine();
        reader.readLine();
        
        return new IteratorWithException<Shot>() {
            
            String currentLine;
            int lineIndex = 0;
            boolean hasNextCalled;
            boolean newShot = true;
            List<NonEmptyShotLine> tempLines;
            int lastShotID = -1;
            String[] tempSplit = null;
            Shot currentShot = null;
            
            @Override
            public boolean hasNext() throws Exception {
                
                if(!hasNextCalled){
                    hasNextCalled = true;
                    currentShot = getNextShot();
                }
                
                return currentShot != null;
            }
            
            private String getNextLine() throws Exception{
                try {
                    lineIndex++;
                    return reader.readLine();
                } catch (IOException ex) {
                    throw new Exception("Cannot read line "+lineIndex, ex);
                }
            }
            
            private Shot buildNonEmptyShot(List<NonEmptyShotLine> lines){
                
                NonEmptyShotLine firstLine = lines.get(0);
                
                Echo[] echoes = new Echo[lines.size()];
                
                for(int i = 0;i<lines.size();i++){
                    
                    NonEmptyShotLine line = lines.get(i);
                    echoes[i] = new Echo(line.range,  line.attributes);
                }
                
                Shot shot = new Shot(
                        firstLine.shotID,
                        firstLine.xOrigin, firstLine.yOrigin, firstLine.zOrigin,
                        firstLine.xDirection, firstLine.yDirection, firstLine.zDirection,
                        echoes);
                
                return shot;
            }
            
            private Shot getNextShot() throws Exception {
                
                String[] split;
                
                if(tempSplit != null){
                    split = new String[tempSplit.length];
                    System.arraycopy(tempSplit, 0, split, 0, tempSplit.length);
                    tempSplit = null;
                }else{
                    String line = getNextLine();
                    if(line == null){
                        if(tempLines != null && !tempLines.isEmpty()){
                            Shot buildNonEmptyShot = buildNonEmptyShot(tempLines);
                            tempLines = null;
                            return buildNonEmptyShot;
                        }else{
                            return null;
                        }
                    }

                    split = line.split(" ");
                }
                
                //parse split array
                if (split.length < 7) {
                    throw new Exception("Invalid column number !");
                } else{
                    
                    int shotID = Integer.valueOf(split[0]);
                    
                    if(shotID != lastShotID){ //new shot
                        if(tempLines != null && !tempLines.isEmpty()){
                            Shot buildNonEmptyShot = buildNonEmptyShot(tempLines);
                            tempLines = null;
                            tempSplit = split;
                            return buildNonEmptyShot;
                        }
                    }

                    double xOrigin = Double.valueOf(split[1]);
                    double yOrigin = Double.valueOf(split[2]);
                    double zOrigin = Double.valueOf(split[3]);

                    double xDirection = Double.valueOf(split[4]);
                    double yDirection = Double.valueOf(split[5]);
                    double zDirection = Double.valueOf(split[6]);
                    
                    if (split.length == 7) { //empty shot
                        
                        lastShotID = shotID;
                        return new Shot(shotID, xOrigin, yOrigin, zOrigin, xDirection, yDirection, zDirection);
                        
                    }else{ //non empty shot, possibly other lines
                        
                        double range = Double.valueOf(split[7]);
                        
                        if(tempLines == null){
                            tempLines = new ArrayList<>();
                        }
                        
                        Object[] objects = new Object[split.length-8];
                        for (int i = 8, j = 0; i < split.length; i++, j++) {
                            
                            Column.Type columnType = context.getColumnType(i);
                            switch (columnType) {
                                case BOOLEAN:
                                    objects[j] = Boolean.valueOf(split[i]);
                                    break;
                                case DOUBLE:
                                    objects[j] = Double.valueOf(split[i]);
                                    break;
                                case FLOAT:
                                    objects[j] = Float.valueOf(split[i]);
                                    break;
                                case INTEGER:
                                    objects[j] = Integer.valueOf(split[i]);
                                    break;
                                case LONG:
                                    objects[j] = Long.valueOf(split[i]);
                                    break;
                                case STRING:
                                default:
                                    objects[j] = split[i];
                                    break;
                            }
                            
                        }
                        
                        tempLines.add(new NonEmptyShotLine(shotID, xOrigin, yOrigin, zOrigin, xDirection, yDirection, zDirection, range, objects));
                        lastShotID = shotID;
                        
                        return getNextShot();
                    }
                }
            }

            @Override
            public Shot next() throws Exception {

                if(hasNextCalled){
                    hasNextCalled = false;
                    return currentShot;
                }else{
                    return getNextShot();
                }                
            }
        };        
    }

    public ShotFileContext getContext() {
        return context;
    }
}
