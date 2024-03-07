/*
 * Copyright (C) 2016 UMR AMAP (botAnique et Modélisation de l'Architecture des Plantes et des végétations.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.amapvox.viewer3d.object.scene;

import org.amapvox.commons.util.ColorGradient;
import org.amapvox.commons.math.util.Statistic;
import gnu.trove.list.array.TFloatArrayList;
import java.awt.Color;
import org.apache.commons.math3.stat.Frequency;

/**
 *
 * @author Julien Heurtebize
 */
public class ScalarField {
    
    private final Statistic statistic;
    private ColorGradient colorGradient;
    private final TFloatArrayList values;
    private final String name;
    
    private final Frequency f;
    public long[] histogramFrequencyCount;
    public double[] histogramValue;
    
    public boolean hasColorGradient;
            
    public ScalarField(String name) {
        
        this.name = name;
        values = new TFloatArrayList();
        statistic = new Statistic();
        colorGradient = new ColorGradient(0, 0);
        hasColorGradient = true;
        f = new Frequency();
    }
    
    public void addValue(float value){
        values.add(value);
        statistic.addValue(value);
    }
    
    public float getValue(int index){
        
        return values.get(index);
    }
    
    public Color getColor(int index){
        
        colorGradient.setMinValue((float) statistic.getMinValue());
        colorGradient.setMaxValue((float) statistic.getMaxValue());
        
        return colorGradient.getColor(values.get(index));
    }

    public void setColorGradient(ColorGradient colorGradient) {
        this.colorGradient = colorGradient;
    }
    
    public void setGradientColor(Color[] color) {
        this.colorGradient.setGradientColor(color);
    }
    
    public int getNbValues(){
        return values.size();
    }

    public String getName() {
        return name;
    }

    public Statistic getStatistic() {
        return statistic;
    }
    
    public void buildHistogram(){
        
        for(int i=0;i< values.size();i++){
            float value = values.get(i);
            f.addValue(Double.valueOf(value).longValue());
        }
        
        double minValue = statistic.getMinValue();
        double maxValue = statistic.getMaxValue();
        
        double width = maxValue - minValue;
        double step = width / 18.0d;
        
        histogramValue = new double[19];
        histogramFrequencyCount = new long[19];
        
        int i=0;
        
        try{
            for(double d = minValue ; d <= maxValue ; d += step){
            
            if(i < histogramValue.length){
                    histogramFrequencyCount[i] = f.getCumFreq(Double.valueOf(d + step).longValue()) - f.getCumFreq(Double.valueOf(d).longValue());
                    histogramValue[i] = d;
                }

                i++;
            }
        }catch(Exception e){
            
        }
        
    }
    
    
}
