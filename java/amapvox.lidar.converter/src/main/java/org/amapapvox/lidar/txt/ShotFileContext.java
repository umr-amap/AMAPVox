/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapapvox.lidar.txt;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Julien Heurtebize
 */
public class ShotFileContext {
    
    private final Map<String, Integer> userAttributesByName;
    private final Map<Integer, String> userAttributesByIndex;
    private final Map<Integer, Column.Type> userAttributesTypesByIndex;
    private final Map<String, Column.Type> userAttributesTypesByName;
    
    private Column[] extraColumns;
    
    public ShotFileContext(Column... extraColumns){
             
        this.extraColumns = extraColumns;
        
        userAttributesByName = new HashMap<>();
        userAttributesByIndex = new HashMap<>();
        userAttributesTypesByIndex = new HashMap<>();
        userAttributesTypesByName = new HashMap<>();
        
        int columnIndex = 8;
        for(Column column : extraColumns){
            userAttributesByName.put(column.getName(), columnIndex);
            userAttributesByIndex.put(columnIndex, column.getName());
            userAttributesTypesByName.put(column.getName(), column.getType());
            userAttributesTypesByIndex.put(columnIndex, column.getType());
            columnIndex++;
        }
    }

    
    public int getColumnIndex(String name){
        return userAttributesByName.get(name);
    }
    
    public String getColumnName(int index){
        return userAttributesByIndex.get(index);
    }
    
    public Column.Type getColumnType(int index){
        return userAttributesTypesByIndex.get(index);
    }
    
    public Column.Type getColumnType(String name){
        return userAttributesTypesByName.get(name);
    }

    public Column[] getExtraColumns() {
        return extraColumns;
    }
}
