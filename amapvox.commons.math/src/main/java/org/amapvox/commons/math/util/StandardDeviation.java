/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.math.util;

import java.util.ArrayList;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class StandardDeviation {
    
    private float average;
    private float sum1, sum2;
    private float count;
    private final ArrayList<Float> valuesList;
    
    public StandardDeviation(){
        average = 0;
        sum1 = 0;
        count = 0;
        valuesList= new ArrayList<>();
    }

    public float getAverage() {
        return average;
    }
    
    public void addValue(float value){
        
        if(!Float.isNaN(value)){
            sum1+=value;
            valuesList.add(value);
            count++;
        }
    }
    
    public float getStandardDeviation(){
        
        average = sum1/(float)count;
        sum2 = 0;
        
        for (Float value : valuesList) {
            
            sum2 += Math.pow(value - average, 2);
        }
        
        return (float)(Math.sqrt((1/(float)count)* sum2));
    }
    
    public float getFromFloatArray(float[] values){
        
        //average
        sum1 = 0;
        count = 0;
        
        for(int i=0;i<values.length;i++){
            
            if(!Float.isNaN(values[i])){
                sum1+=values[i];
                count++;
            }
        }
        
        average = sum1/(float)count;
        
        sum2 = 0;
        count = 0;
        
        for(int i=0;i<values.length;i++){
            
            float val = (float) Math.pow(values[i]-average, 2);
            if(!Float.isNaN(val)){
                sum2+=Math.pow(values[i]-average, 2);
                count++;
            }
            
        }
        
        return (float)(Math.sqrt((1/(float)count)* sum2));
    }
}
