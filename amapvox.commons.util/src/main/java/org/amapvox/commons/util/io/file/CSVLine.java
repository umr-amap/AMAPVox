/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util.io.file;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a line in a CSV file.
 * @author Julien Heurtebize
 */
public class CSVLine {

    private final Map<String, Object> map ;
    
    public CSVLine() {
        map = new HashMap<>();
    }
    
    /**
     * Add a column to the line with the specified name and value.
     * @param <T> 
     * @param key Name of the column.
     * @param value Value of the field.
     */
    public <T> void setAttribute(String key, T value){
        map.put(key, value);
    }
    
    /**
     * Get a column with the specified name.
     * @param <T>
     * @param key Name of the column.
     * @return The value of the field.
     */
    public <T> T getAttribute(String key){
        
        return (T)map.get(key);
    }
    
    
}
