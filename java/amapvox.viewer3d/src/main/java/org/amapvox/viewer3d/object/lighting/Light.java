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
package org.amapvox.viewer3d.object.lighting;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Light {
    
    public Point3f position;
    public Vector3f ambient;
    public Vector3f diffuse;
    public Vector3f specular;
    
    public Light(){
        
        ambient = new Vector3f(1.0f, 1.0f, 1.0f);
        diffuse = new Vector3f(1.0f, 1.0f, 1.0f);
        specular = new Vector3f(1.0f, 1.0f, 1.0f);
        position = new Point3f(0, 0, 100);
    }
}
