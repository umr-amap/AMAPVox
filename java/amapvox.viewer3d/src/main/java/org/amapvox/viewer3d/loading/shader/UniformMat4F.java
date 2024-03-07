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

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import org.amapvox.commons.math.util.MatrixUtility;
import java.nio.FloatBuffer;
import javax.vecmath.Matrix4f;

/**
 *
 * @author Julien Heurtebize
 */
public class UniformMat4F extends Uniform{

    private FloatBuffer value;
    
    public UniformMat4F(String name) {
        super(name);
    }
    
    public UniformMat4F(String name, Shader shader) {
        super(name, shader);
    }

    @Override
    public void update(GL3 gl, int location) {
        
        gl.glUniformMatrix4fv(location, 1, false, value);
    }

    public void setValue(Matrix4f value) {
        
        this.value = Buffers.newDirectFloatBuffer(MatrixUtility.toFloatArray(value));
        notifyOwners();
    }
    
}
