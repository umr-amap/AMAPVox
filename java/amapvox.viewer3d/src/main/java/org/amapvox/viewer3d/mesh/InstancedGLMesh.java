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

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class InstancedGLMesh extends GLMesh{
    
    public FloatBuffer instancePositionsBuffer;
    public FloatBuffer instanceColorsBuffer;
    private int instanceNumber;
    
    
    public InstancedGLMesh(GLMesh glMesh, int instanceNumber){
        
        super();
        this.instanceNumber = instanceNumber;
        this.vertexBuffer = glMesh.vertexBuffer;
        this.indexBuffer = glMesh.indexBuffer;
        this.colorBuffer = glMesh.colorBuffer;
        this.normalBuffer = glMesh.normalBuffer;
        this.vertexCount = glMesh.vertexCount;
    }

    @Override
    public void draw(GL3 gl, DrawType drawType) {
        gl.glDrawElementsInstanced(drawType.get(), vertexCount, GL3.GL_UNSIGNED_INT, 0, instanceNumber);
    }

    @Override
    public void initBuffers(GL3 gl, long maximumTotalBufferSize) {
        
        if(getVboId() <= 0 || getIboId() <= 0){
            initVBOAndIBO(gl);
        }
        
        
        FloatBuffer[] floatBuffers = new FloatBuffer[]{vertexBuffer};
        
        bindBuffer(gl);
        
        totalBuffersSize = maximumTotalBufferSize;
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, totalBuffersSize, null, GL3.GL_STATIC_DRAW);
        
        for (FloatBuffer buffer : floatBuffers) {
            addSubBuffer(gl, buffer);
        }
        
        sendIBOData(gl);
        
        unbindBuffer(gl);
    }

    public int getInstanceNumber() {
        return instanceNumber;
    }

    public void setInstanceNumber(int instanceNumber) {
        this.instanceNumber = instanceNumber;
    }
    
}
