/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.math.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.vecmath.Matrix4d;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class MatrixFileParser {
    
    private static ArrayList<String> readAllLines(File file) throws FileNotFoundException, IOException{
        
        ArrayList<String> lines = new ArrayList<>();
        
        try {

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                
                String line;
                
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
            
        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
        
        return lines;
    }
    
    public static float[][] getPonderationMatrixFromFile(File matrixFile) throws IOException{
        
        float[][] ponderationMatrix = new float[7][7];
        
        ArrayList<String> lines;
        
        lines = readAllLines(matrixFile);
        String s = "";
                
        for(String line : lines){
            s += line;
        }
        
        s = s.replaceAll("\n", ",");
        s = s.replaceAll(" ", ",");
        s = s.replaceAll("\t", ",");
        
        String[] elements = s.split(",");
        
        int i = 0;
        int j = 0;
        try{
            for(String element : elements){
                if(!element.isEmpty()){
                    ponderationMatrix[j][i] = Float.valueOf(element);

                    if(i%6 == 0 && i!=0){
                        j++;
                        i = 0; 
                    }else{
                        i++;
                    }
                }
            }
            
            return ponderationMatrix;
            
        }catch(Exception e){
            
        }
        
        return null;
    }
    
    public static Matrix4d getMatrixFromFile(File matrixFile) throws IOException{
        
        ArrayList<String> lines = readAllLines(matrixFile);
        
        if(lines.size() != 4){
            return null;
        }
        
        Matrix4d matrix = new Matrix4d();
        
        for(int j=0;j<lines.size();j++){
            
            StringTokenizer parser = new StringTokenizer(lines.get(j));
            int count = 0;
            while(parser.hasMoreTokens()){
                String val = parser.nextToken();
                double v1 = Double.valueOf(val);
                matrix.setElement(j, count, v1);
                count++;
            }
        }
        
        return matrix;
    }
}
