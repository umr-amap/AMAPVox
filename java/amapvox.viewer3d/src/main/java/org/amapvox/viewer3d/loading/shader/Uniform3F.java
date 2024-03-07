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
import javax.vecmath.Vector3f;

/**
 *
 * @author Julien Heurtebize
 */
public class Uniform3F extends Uniform{

    private Vector3f value;
    
    public Uniform3F(String name) {
        super(name);
    }

    public Uniform3F(String name, Shader shader) {
        super(name, shader);
    }

    @Override
    public void update(GL3 gl, int location) {
        
        gl.glUniform3f(location, value.x, value.y, value.z);
    }

    public void setValue(Vector3f value) {
        this.value = value;
        notifyOwners();
    }
    
}
