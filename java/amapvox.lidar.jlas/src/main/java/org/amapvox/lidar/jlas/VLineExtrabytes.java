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
package org.amapvox.lidar.jlas;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class VLineExtrabytes{
    
    private int amplitude;
    private int reflectance;
    private int deviation;
    
    public VLineExtrabytes(byte[] bytes) {
        
        if(bytes.length == 3){
            amplitude = bytes[0];
            reflectance = bytes[1];
            deviation = bytes[2];
        }
    }

    public int getAmplitude() {
        return amplitude;
    }

    public int getReflectance() {
        return reflectance;
    }

    public int getDeviation() {
        return deviation;
    }
}
