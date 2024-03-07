/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util.io.file;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CSVFile class defines how a CSV file should be read (header, separator, etc...)
 * @author Julien Heurtebize
 */
public class CSVFile extends File{
    
    private boolean containsHeader;
    private long headerIndex;
    private String columnSeparator;
    private long nbOfLinesToSkip;
    private long nbOfLinesToRead;
    private boolean skippedBeforeHeader;
    private Map<String, Integer> columnAssignment;
    private Map<String, Class> columnTypesAssignment;
    private Map<String, CSVColumn> csvColumns;
        
    public CSVFile(File file) {
        
        super(file.getAbsolutePath());
        init();
    }
            
    public CSVFile(String pathname) {
        
        super(pathname);
        init();
    }
    
     private void init(){
        columnSeparator = ",";
        containsHeader = true;
        headerIndex = 0;
        nbOfLinesToSkip = 1;
        nbOfLinesToRead = Long.MAX_VALUE;
        columnAssignment = new HashMap<>();
        columnTypesAssignment = new HashMap<>();
        csvColumns = new HashMap<>();
    }

    /**
     * 
     * @return true if the file contains a header, false otherwise
     */
    public boolean containsHeader() {
        return containsHeader;
    }

    public void setContainsHeader(boolean containsHeader) {
        this.containsHeader = containsHeader;
    }

    public String getColumnSeparator() {
        return columnSeparator;
    }

    public void setColumnSeparator(String columnSeparator) {
        this.columnSeparator = columnSeparator;
    }

    public long getHeaderIndex() {
        return headerIndex;
    }

    public void setHeaderIndex(long headerIndex) {
        this.headerIndex = headerIndex;
    }

    /**
     * Get the number of lines to skip, the header line is not taken into account.
     * @return The number of lines to skip as a long
     */
    public long getNbOfLinesToSkip() {
        return nbOfLinesToSkip;
    }

    /**
     * Set the number of lines to skip, the header line must not be taken into account.
     * @param nbOfLinesToSkip 
     */
    public void setNbOfLinesToSkip(long nbOfLinesToSkip) {
        this.nbOfLinesToSkip = nbOfLinesToSkip;
    }
    
    /**
     * Configure if the lines to be skipped are before the header or after.
     * @param before true for before, false for after.
     */
    public void setSkippedBeforeHeader(boolean before){
        skippedBeforeHeader = before;
    }

    /**
     * Get if the lines to skip are before the header or after.
     * @return true for before, false for after.
     */
    public boolean isSkippedBeforeHeader() {
        return skippedBeforeHeader;
    }

    /**
     * Get the number of lines to read from the file.
     * <p>The number of read line is calculated from the beginning which is the number of line to be ignored + header (optional).</p>
     * That is if we want to read 10 lines of a file but if this one has 5 lines to be ignored (discarded) and one header,
     * the number of line will be incremented from the seventh read line.
     * @return The number of lines to read as a long.
     */
    public long getNbOfLinesToRead() {
        return nbOfLinesToRead;
    }

    /**
     * Set the number of lines to read from the file.
     * <p>The number of read line must be calculated from the beginning which is the number of line to be ignored + header (optional).</p>
     * That is if we want to read 10 lines of a file but if this one has 5 lines to be ignored (discarded) and one header,
     * the number of line will be incremented from the seventh read line.
     * @param nbOfLinesToRead The number of lines to read as a long.
     */
    public void setNbOfLinesToRead(long nbOfLinesToRead) {
        this.nbOfLinesToRead = nbOfLinesToRead;
    }

    public Map<String, Integer> getColumnAssignment() {
        return columnAssignment;
    }

    public Map<String, CSVColumn> getCsvColumns() {
        return csvColumns;
    }

    public void setColumnAssignment(Map<String, Integer> columnAssignment) {
        this.columnAssignment = columnAssignment;
    }
    
    public void setColumnAssignmentV2(Map<String, CSVColumn> columnAssignment) {
        this.csvColumns = columnAssignment;
    }
    
    /**
     * Assign index and type to a column.
     * @param name Name of the column
     * @param index Index of the column in the file
     * @param type Type of values contained in the column (optional, can be null)
     */
    public void setColumn(String name, Integer index, Class type) {
        columnAssignment.put(name, index);
        
        if(type != null){
            columnTypesAssignment.put(name, type);
        }
        
        csvColumns.put(name, new CSVColumn(name, index, type));
    }
    
    public CSVColumn getColumn(String name){
        return csvColumns.get(name);
    }

    public Map<String, Class> getColumnTypes() {
        return columnTypesAssignment;
    }

    public void setColumnTypes(Map<String, Class> columnTypes) {
        this.columnTypesAssignment = columnTypes;
    }
}
