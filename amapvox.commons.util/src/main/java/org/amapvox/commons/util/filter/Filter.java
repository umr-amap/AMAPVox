/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util.filter;

/**
 *
 * @author pverley
 * @param <T>
 */
public interface Filter<T> {
    
    
    public void init() throws Exception;
    
    public boolean accept(T object) throws Exception;
    
    public enum Behavior {
        RETAIN,
        DISCARD;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
