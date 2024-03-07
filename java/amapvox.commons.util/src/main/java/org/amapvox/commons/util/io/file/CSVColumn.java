/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util.io.file;

/**
 * Represents a column in a CSV file.
 * @author Julien Heurtebize
 */
public class CSVColumn {
    
    private final int index;
    private final String name;
    private final Class type;

    /**
     * 
     * @param name Name of the column.
     * @param index Index of the column in the CSV file
     * @param type Type of column's values.
     */
    public CSVColumn(String name, int index, Class type) {
        this.name = name;
        this.index = index;
        this.type = type;
    }

    /**
     * Get the name of the column.
     * @return 
     */
    public String getName() {
        return name;
    }

    /**
     * Get the type of column's values.
     * @return A {@link Class} object.
     */
    public Class getType() {
        return type;
    }

    /**
     * Get the index of the column in the CSV file.
     * @return 
     */
    public int getIndex() {
        return index;
    }
}
