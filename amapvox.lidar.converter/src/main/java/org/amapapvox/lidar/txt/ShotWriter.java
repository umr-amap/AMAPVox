/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapapvox.lidar.txt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 *
 * @author Julien Heurtebize
 */
public class ShotWriter {

    private final ShotFileContext context;
    private final BufferedWriter writer;
    private final DecimalFormat formatter = new DecimalFormat("###.#######################");
    
    public ShotWriter(ShotFileContext context, File file) throws IOException {
        
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        
        formatter.setDecimalFormatSymbols(symbols);
        this.context = context;
        writer = new BufferedWriter(new FileWriter(file));
        
        String attributesTypesLine = "INTEGER DOUBLE DOUBLE DOUBLE DOUBLE DOUBLE DOUBLE DOUBLE";
        String attributesLine = "shotID xOrigin yOrigin zOrigin xDirection yDirection zDirection range";
        
        Column[] extraColumns = context.getExtraColumns();
        
        for(Column extraColumn : extraColumns){
            attributesTypesLine += " " + extraColumn.getType().toString();
            attributesLine += " " + extraColumn.getName();
        }
        
        writer.write(attributesTypesLine + "\n");
        writer.write(attributesLine + "\n");
    }
    
    public void write(Shot shot) throws IOException{
        
        if(shot.getNbEchoes() > 0){
            
            for (Echo echo : shot.getEchoes()) {
                
                String line = 
                        shot.getId()+" "+
                        formatter.format(shot.getXOrigin())+" "+formatter.format(shot.getYOrigin())+" "+formatter.format(shot.getZOrigin())+" "+
                        formatter.format(shot.getXDirection())+" "+formatter.format(shot.getYDirection())+" "+formatter.format(shot.getZDirection())+" "+
                        formatter.format(echo.getRange());
                
                for(Object object : echo.getObjects()){
                    line += " "+object;
                }
                
                writer.write(line+"\n");
            }
        }else{
            writer.write(shot.getId()+" "+
                        formatter.format(shot.getXOrigin())+" "+formatter.format(shot.getYOrigin())+" "+formatter.format(shot.getZOrigin())+" "+
                        formatter.format(shot.getXDirection())+" "+formatter.format(shot.getYDirection())+" "+formatter.format(shot.getZDirection())+"\n");
        }
        
    }
    
    public void close() throws IOException{
        writer.close();
    }
}
