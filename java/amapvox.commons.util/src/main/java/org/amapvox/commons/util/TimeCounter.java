/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class TimeCounter {
    
    public static String getElapsedStringTimeInSeconds(long startTime){
        
        return String.valueOf((float)(Math.round((System.currentTimeMillis() - startTime)*(Math.pow(10, -3))*100)/100))+" s";
    }
    
    public static float getElapsedTimeInSeconds(long startTime){
        
        return (float)(Math.round((System.currentTimeMillis() - startTime)*(Math.pow(10, -3))*100)/100);
    }
}
