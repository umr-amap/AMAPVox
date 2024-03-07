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
package org.amapvox.viewer3d.mesh;

import com.jogamp.opengl.GL3;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Julien Heurtebize
 */
public class PointCloudGLMesh extends GLMesh{

    @Override
    public void draw(GL3 gl, DrawType drawType) {
        gl.glDrawArrays(GL3.GL_POINTS, 0, vertexCount);
    }

    @Override
    public void initBuffers(GL3 gl, long maximumTotalBufferSize) {
        
        initVBOAndIBO(gl);
        
        bindBuffer(gl);
        
        List<FloatBuffer> floatBuffers = new ArrayList<>();
        floatBuffers.add(vertexBuffer);
        if(colorBuffer != null){
            floatBuffers.add(colorBuffer);
        }
        
        if(maximumTotalBufferSize == DEFAULT_SIZE){
            for (FloatBuffer buffer : floatBuffers) {
                
                if(buffer != null){
                    totalBuffersSize += buffer.capacity()*FLOAT_SIZE;
                }
                
            }
        }else{
            totalBuffersSize = maximumTotalBufferSize;
        }
        
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, totalBuffersSize, null, GL3.GL_STATIC_DRAW);
        
        for (FloatBuffer buffer : floatBuffers) {
            addSubBuffer(gl, buffer);
        }
        
        unbindBuffer(gl);
    }
    
}
