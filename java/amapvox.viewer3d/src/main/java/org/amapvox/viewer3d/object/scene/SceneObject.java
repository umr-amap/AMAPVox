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
import org.amapvox.commons.math.geometry.BoundingBox3D;
import org.amapvox.commons.math.util.MatrixUtility;
import org.amapvox.viewer3d.loading.shader.Shader;
import org.amapvox.viewer3d.loading.shader.UniformMat4F;
import org.amapvox.viewer3d.loading.texture.Texture;
import org.amapvox.viewer3d.mesh.GLMesh;
import org.amapvox.viewer3d.mesh.GLMesh.DrawType;
import java.nio.FloatBuffer;
import javax.swing.event.EventListenerList;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public abstract class SceneObject{
    
    //public Mesh mesh ;
    protected GLMesh mesh;
    protected int vaoId = -1, textureId = -1;
    protected DrawType drawType;
    public boolean isAlphaRequired;
    public boolean depthTest = true;
    public Texture texture;
    protected Shader shader;
    protected final Point3f gravityCenter;
    protected Matrix4f transformation;
    private UniformMat4F transfoUniform = new UniformMat4F("transformation");
    private int id;
    protected boolean colorNeedUpdate = false;
    private String name = "";
    
    protected boolean mousePickable;
    private boolean selected;
    private boolean visible = true;
    
    private final EventListenerList listeners;

    public SceneObject(){
        vaoId = -1;
        transformation = MatrixUtility.identity4f();
        //setPosition(new Point3f());
        listeners = new EventListenerList();
        visible = true;
        gravityCenter = new Point3f();
    }
    
    public SceneObject(GLMesh mesh, boolean isAlphaRequired){
        
        this.mesh = mesh;
        this.drawType = DrawType.TRIANGLES;
        this.isAlphaRequired = isAlphaRequired;
        this.gravityCenter = mesh.getGravityCenter();
        vaoId = -1;
        transformation = MatrixUtility.identity4f();
        //setPosition(new Point3f());
        listeners = new EventListenerList();
        visible = true;
    }
    
    public void resetIds(){
        vaoId = -1;
        mesh.resetIds();
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getShaderId() {
        return shader.getProgramId();
    }

    public int getTextureId() {
        return textureId;
    }
    

    public int getId() {
        return id;
    }

    public int getVaoId() {
        return vaoId;
    }

    public void setDrawType(DrawType drawType) {
        this.drawType = drawType;
    }

    public DrawType getDrawType() {
        return drawType;
    }

    public Shader getShader() {
        return shader;
    }

    public void setShader(Shader shader) {
        this.shader = shader;
        transfoUniform.addOwner(this.shader);
    }
    
    public void attachTexture(int textureId){
        
        this.textureId = textureId;
    }
    
    public void attachTexture(Texture texture){
        
        this.texture = texture;
        textureId = texture.getId();
    }

    public Point3f getGravityCenter() {
        return gravityCenter;
    }

    public void setGravityCenter(Point3f position) {
        this.gravityCenter.x = position.x;
        this.gravityCenter.y = position.y;
        this.gravityCenter.z = position.z;
    }
    
    public void setMousePickable(boolean isPickable){
        
        this.mousePickable = isPickable;
    }

    public boolean isMousePickable() {
        return mousePickable;
    }

    public GLMesh getMesh() {
        return mesh;
    }
    
    public void setMesh(GLMesh mesh){
        this.mesh = mesh;
        this.resetIds();
    }
    
    public void addSceneObjectListener(SceneObjectListener listener){
        listeners.add(SceneObjectListener.class, listener);
    }
    
    public void removeSceneObjectListener(SceneObjectListener listener){
        listeners.remove(SceneObjectListener.class, listener);
    }
    
    public void fireClicked(MousePicker mousePicker, Point3d intersection){
        
        for(SceneObjectListener listener : listeners.getListeners(SceneObjectListener.class)){
            
            listener.clicked(this, mousePicker, intersection);
        }
    }
    
    public void setPosition(Point3f position){
        
        transformation.setElement(0, 3, position.x);
        transformation.setElement(1, 3, position.y);
        transformation.setElement(2, 3, position.z);
        
        Matrix4f transposed = new Matrix4f();
        transposed.transpose(transformation);
        transfoUniform.setValue(transposed);
    }
    
    public void setTransformation(Matrix4f transformation){
        
        this.transformation = new Matrix4f(transformation);
        
        Matrix4f transposed = new Matrix4f();
        transposed.transpose(transformation);
        transfoUniform.setValue(transposed);
    }
    
    public BoundingBox3D getBoundingBox(){
        
        Point3f min = new Point3f(
                (float)mesh.getxValues().getMinValue(),
                (float)mesh.getyValues().getMinValue(),
                (float)mesh.getzValues().getMinValue()
        );
        transformation.transform(min);
       
        Point3f max = new Point3f(
                (float)mesh.getxValues().getMaxValue(),
                (float)mesh.getyValues().getMaxValue(),
                (float)mesh.getzValues().getMaxValue()
        );
        transformation.transform(max);

        BoundingBox3D bb = new BoundingBox3D(
                new Point3d(min),
                new Point3d(max));
        
        return bb;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public abstract void initBuffers(GL3 gl);
    
    public abstract void updateBuffers(GL3 gl, int index, FloatBuffer buffer);
    
    public abstract void initVao(GL3 gl);
    
    public abstract void draw(GL3 gl);
    
    public abstract Object doPicking(MousePicker mousePicker);
}
