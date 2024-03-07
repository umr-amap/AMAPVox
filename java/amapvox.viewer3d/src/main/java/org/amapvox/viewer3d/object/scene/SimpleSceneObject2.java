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

import com.jogamp.opengl.GL3;
import org.amapvox.viewer3d.mesh.GLMesh;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author Julien Heurtebize
 */
public class SimpleSceneObject2 extends SceneObject{
    
    public SimpleSceneObject2(GLMesh mesh, boolean isAlphaRequired){
        
        super(mesh, isAlphaRequired);
    }
    
    @Override
    public void initBuffers(GL3 gl){
        
        mesh.initBuffers(gl, GLMesh.DEFAULT_SIZE);
    }
    
    @Override
    public void initVao(GL3 gl){
        
        //generate vao
        IntBuffer tmp = IntBuffer.allocate(1);
        gl.glGenVertexArrays(1, tmp);
        vaoId = tmp.get(0);
        
        gl.glBindVertexArray(vaoId);
        
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, mesh.getVboId());
                gl.glEnableVertexAttribArray(shader.attributeMap.get("position"));
                gl.glVertexAttribPointer(shader.attributeMap.get("position"), 3, GL3.GL_FLOAT, false, 0, 0);
                 
            gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, mesh.getIboId());
            
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
            
        gl.glBindVertexArray(0);
    }
    
    @Override
    public void draw(GL3 gl){
        
        gl.glBindVertexArray(vaoId);
        mesh.draw(gl, drawType);
        gl.glBindVertexArray(0);
    }

    @Override
    public void updateBuffers(GL3 gl, int index, FloatBuffer buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String doPicking(MousePicker mousePicker) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}