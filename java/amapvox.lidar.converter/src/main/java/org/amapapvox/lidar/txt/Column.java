/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapapvox.lidar.txt;

/**
 *
 * @author Julien Heurtebize
 */
public class Column {
    
    public enum Type{
        
        INTEGER("INTEGER"),
        LONG("LONG"),
        FLOAT("FLOAT"),
        DOUBLE("DOUBLE"),
        BOOLEAN("BOOLEAN"),
        STRING("STRING");
        
        private final String name;
        
        private Type(String name){
            this.name = name;
        }
        
        public static Type fromString(String name){
            
            switch(name){
                case "INTEGER":
                    return INTEGER;
                case "LONG":
                    return LONG;
                case "FLOAT":
                    return FLOAT;
                case "DOUBLE":
                    return DOUBLE;
                case "BOOLEAN":
                    return BOOLEAN;
                case "STRING":
                    return STRING;
                default:
                    return null;
            }
        }

        @Override
        public String toString() {
            return name;
        }
    }
    
    private final String name;
    private final Type type;

    public Column(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}
