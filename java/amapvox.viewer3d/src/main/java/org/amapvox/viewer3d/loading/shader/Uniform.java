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

import com.jogamp.opengl.GL3;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Julien Heurtebize
 */
public abstract class Uniform {
    
    protected final String name;
    protected final List<Shader> owners;
    protected boolean dirty; //if true it means no shaders has updated the uniform
    
    public Uniform(String name){
        
        owners = new ArrayList<>();
        
        this.name = name;
    }
    
    public Uniform(String name, Shader shader){
        
        owners = new ArrayList<>();
        
        this.name = name;
        
        owners.add(shader);
        
    }
    
    public void addOwner(Shader shader){
        owners.add(shader);
    }
    
    public void notifyOwners(){
        
        if(!owners.isEmpty()){
            
            dirty = false;
            
            for (Shader owner : owners) {
                owner.notifyDirty(this);
            }
            
        }else{
            dirty = true;
        }
        
    }

    public String getName() {
        return name;
    }

    public boolean isDirty() {
        return dirty;
    }
    
    public abstract void update(GL3 gl, int location); 
}
