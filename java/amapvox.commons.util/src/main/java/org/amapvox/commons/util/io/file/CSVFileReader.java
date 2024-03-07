/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util.io.file;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Julien Heurtebize
 */
public class CSVFileReader{

    private final CSVFile csvFile;
    private BufferedReader reader;
    private long nbLinesRead;
    private long nbLinesSkipped;
    private String header;

    public CSVFileReader(CSVFile csvFile) {
        
        this.csvFile = csvFile;
    }
    
    public void open() throws FileNotFoundException{
        
        reader = new BufferedReader(new FileReader(csvFile));
        nbLinesRead = 0;
        nbLinesSkipped = 0;
        
        //read header first if the header is the first line
        if(!csvFile.isSkippedBeforeHeader() && csvFile.containsHeader()){
            try{
                header = reader.readLine();
            }catch(Exception e){}
        }
        
        //skip as many lines as specified in the CSVFile object
        while(nbLinesSkipped < csvFile.getNbOfLinesToSkip()){
            try{
                reader.readLine();
                nbLinesSkipped++;
            }catch(Exception e){
                return;
            }
        }
        
        if(csvFile.isSkippedBeforeHeader() && csvFile.containsHeader()){
            try{
                header = reader.readLine();
            }catch(Exception e){}
        }
    }
    
    public void close() throws IOException{
        
        if(reader != null){
            reader.close();
        }
    }
    
    /**
     * 
     * @return The line as a {@link CSVLine} or null if the reader reached the end of the file.
     * @throws java.io.IOException
     * @throws IllegalArgumentException Happens when the column type is not the good one.
     */
    public CSVLine readLine() throws IOException, IllegalArgumentException{
        
        if(nbLinesRead == csvFile.getNbOfLinesToRead()){
            return null;
        }
        
        String line = reader.readLine();
        nbLinesRead++;
        
        if(line == null){
            return null;
        }else{
            
            Map<String, CSVColumn> columnAssignment = csvFile.getCsvColumns();
            
            String[] elements = line.split(csvFile.getColumnSeparator());
            
            Iterator<Map.Entry<String, CSVColumn>> iterator = columnAssignment.entrySet().iterator();
            
            CSVLine csvLine = new CSVLine();
            
            while(iterator.hasNext()){
            
                Map.Entry<String, CSVColumn> entry = iterator.next();
                String key = entry.getKey();
                
                CSVColumn column = entry.getValue();
                Integer index = column.getIndex();
                
                Class type = column.getType();
                String value = elements[index];
                
                Object object = convertString(value, type);
                        
                csvLine.setAttribute(key, object);
            }            
            
            return csvLine;
        }
    }
    
    private <T> T convertString(String value, Class c) throws IllegalArgumentException{
        
        if (String.class == c) {
            return (T) value;
        }
        if (Float.class == c || Float.TYPE == c) {
            return (T) Float.valueOf(value);
        }
        if (Double.class == c || Double.TYPE == c) {
            return (T) Double.valueOf(value);
        }
        if (Boolean.class == c || Boolean.TYPE == c) {
            return (T) Boolean.valueOf(value);
        }
        if (Byte.class == c || Byte.TYPE == c) {
            return (T) Byte.valueOf(value);
        }
        if (Short.class == c || Short.TYPE == c) {
            return (T) Short.valueOf(value);
        }
        if (Integer.class == c || Integer.TYPE == c) {
            return (T) Integer.valueOf(value);
        }
        if (Long.class == c || Long.TYPE == c) {
            return (T) Long.valueOf(value);
        }
        
        return (T)value;
    }

    public String getHeader() {
        
        return header;
    }
    
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        
        CSVFile csvFile = new CSVFile("/home/julien/Documents/AVstudio/app/sunrapp/data/Meteo/Fichiers horizon/dtmh_42.654_2.589.hor");
        csvFile.setColumn("azimut", 0, Double.class);
        csvFile.setColumn("elevation", 1, Double.class);
        csvFile.setColumnSeparator(" ");
        csvFile.setNbOfLinesToSkip(5);
        csvFile.setNbOfLinesToRead(5);
        csvFile.setContainsHeader(false);
        csvFile.setSkippedBeforeHeader(true);
        
        CSVFileReader reader = new CSVFileReader(csvFile);
        
        reader.open();
        
        CSVLine line;
        
        while ((line = reader.readLine()) != null) {
            
            double azimut = line.getAttribute("azimut");
            azimut += 180;
            
            double elevation = line.getAttribute("elevation");
            
            System.out.println(azimut + "\t" + elevation);
        }
        
        reader.close();
        
        
//        CSVFile csvFile = new CSVFile("/home/julien/Documents/tmp/csv_test.txt");
//        csvFile.setColumnSeparator(" ");
//        csvFile.setContainsHeader(false);
//        
//        csvFile.setColumn("colonne 1", 0, Integer.class);
//        csvFile.setColumn("colonne 2", 1, String.class);
//        csvFile.setColumn("colonne 3", 2, Float.class);
//        csvFile.setColumn("colonne 4", 3, Boolean.class);
//        
//        CSVFileReader reader = new CSVFileReader(csvFile);
//        
//        reader.open();
//        
//        CSVLine line;
//        
//        while((line = reader.readLine()) != null){
//            int colonne1 = line.getAttribute("colonne 1");
//            String colonne2 = line.getAttribute("colonne 2");
//            Float colonne3 = line.getAttribute("colonne 3");
//            Boolean colonne4 = line.getAttribute("colonne 4");
//            
//            System.out.println(colonne1 +" "+colonne2+" "+colonne3+" "+colonne4);
//        }
//        
//        reader.close();
    }
    
    
}
