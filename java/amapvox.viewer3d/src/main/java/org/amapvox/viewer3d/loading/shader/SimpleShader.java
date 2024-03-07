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
package org.amapvox.viewer3d.loading.shader;

import javax.vecmath.Vector3f;

/**
 *
 * @author Julien Heurtebize
 */
public class SimpleShader extends Shader{
    
    private final Uniform3F colorUniform;
    
    public SimpleShader(){
        
        super();
        
        setVertexShaderCode(loadCodeFromInputStream(getStream("/org/amapvox/viewer3d/shaders/SimpleVertexShader.txt")));
        setFragmentShaderCode(loadCodeFromInputStream(getStream("/org/amapvox/viewer3d/shaders/SimpleFragmentShader.txt")));
        
        colorUniform = new Uniform3F("color");
    }
    
    public void setColor(Vector3f color){
        colorUniform.setValue(color);
        notifyDirty(colorUniform);
    }
}
