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
package org.amapvox.lidar.gridded;

/**
 * A basic lidar point which belongs to a gridded scan (has a column and a row index)
 * and can contains intensity and rgb attributes.
 * @author Julien Heurtebize
 */
public abstract class LPoint {
    
    /**
     * Intensity value from 0 to 1.
     */
    public float intensity = Float.NaN;

    /**
     * Red color value from 0 to 255.
     */
    public int red = -1;

    /**
     * Green color value from 0 to 255.
     */
    public int green = -1;

    /**
     * Blue color value from 0 to 255.
     */
    public int blue = -1;
    
    /**
     * Row index in the grid.
     */
    public int rowIndex;

    /**
     * Column index in the grid.
     */
    public int columnIndex;

    /**
     * Is the point valid or not (is the lidar shot got an answer) ?
     */
    public boolean valid = true;

}
