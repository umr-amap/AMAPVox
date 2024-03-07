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
import org.amapvox.commons.math.geometry.Intersection;
import org.amapvox.commons.math.util.MatrixUtility;
import org.amapvox.viewer3d.loading.shader.AxisShader;
import org.amapvox.viewer3d.loading.shader.ColorShader;
import org.amapvox.viewer3d.loading.shader.InstanceLightedShader;
import org.amapvox.viewer3d.loading.shader.InstanceShader;
import org.amapvox.viewer3d.loading.shader.PhongShader;
import org.amapvox.viewer3d.loading.shader.Shader;
import org.amapvox.viewer3d.loading.shader.SimpleShader;
import org.amapvox.viewer3d.loading.shader.TextureShader;
import org.amapvox.viewer3d.loading.shader.Uniform;
import org.amapvox.viewer3d.loading.shader.Uniform1I;
import org.amapvox.viewer3d.loading.shader.Uniform3F;
import org.amapvox.viewer3d.loading.shader.UniformMat4F;
import org.amapvox.viewer3d.loading.texture.Texture;
import org.amapvox.viewer3d.object.camera.TrackballCamera;
import org.amapvox.viewer3d.object.lighting.Light;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Scene {
    
    private final static Logger LOGGER = Logger.getLogger(Scene.class.getCanonicalName());
    /*
        contient une liste de shaders, une liste de buffers, une liste de lumières et un VoxelSpace
        ??optimisation 
    */
    
    public final CopyOnWriteArrayList<SceneObject> objectsList;
    public final CopyOnWriteArrayList<SceneObject> hudList;
    public final List<Shader> shadersList;
    public final List<Texture> textureList;
    
    private TrackballCamera camera;
    private Light light;
    
    private MousePicker mousePicker;
    private boolean mousePickerIsDirty; //the mouse picker has been updated
    
    public boolean canDraw;
    
    private int width;
    private int height;
    
    //default shaders
    public AxisShader noTranslationShader = new AxisShader();
    public Shader instanceLightedShader = new InstanceLightedShader();
    public InstanceShader instanceShader = new InstanceShader();
    public TextureShader texturedShader = new TextureShader();
    public TextureShader labelShader = new TextureShader();
    public SimpleShader simpleShader = new SimpleShader();
    public ColorShader colorShader = new ColorShader();
    public PhongShader phongShader = new PhongShader();
    
    //global uniforms, can be used inside shaders files
    public UniformMat4F viewMatrixUniform = new UniformMat4F("viewMatrix");
    public UniformMat4F projMatrixUniform = new UniformMat4F("projMatrix");
    public UniformMat4F normalMatrixUniform;
    public UniformMat4F transformationUniform = new UniformMat4F("transformation");
    public Uniform3F lightPositionUniform = new Uniform3F("lightPosition");
    public Uniform3F lambientUniform = new Uniform3F("lambient");
    public Uniform3F ldiffuseUniform = new Uniform3F("ldiffuse");
    public Uniform3F lspecularUniform = new Uniform3F("lspecular");
    public UniformMat4F projMatrixOrthoUniform = new UniformMat4F("projMatrixOrtho");
    public UniformMat4F viewMatrixOrthoUniform = new UniformMat4F("viewMatrixOrtho");
    public Uniform1I textureUniform = new Uniform1I("texture");
    
    private final Map<String, Uniform> uniforms = new HashMap<>();
    
    public Scene(){
        
        objectsList = new CopyOnWriteArrayList<>();
        hudList = new CopyOnWriteArrayList<>();
        shadersList = new ArrayList<>();
        textureList = new ArrayList<>();
        canDraw = false;
        light = new Light();
        camera = new TrackballCamera();
        mousePicker = new MousePicker(camera);
        
        uniforms.put(viewMatrixUniform.getName(), viewMatrixUniform);
        uniforms.put(projMatrixUniform.getName(), projMatrixUniform);
        uniforms.put(transformationUniform.getName(), transformationUniform);
        uniforms.put(projMatrixOrthoUniform.getName(), projMatrixOrthoUniform);
        uniforms.put(viewMatrixOrthoUniform.getName(), viewMatrixOrthoUniform);
        uniforms.put(textureUniform.getName(), textureUniform);
        uniforms.put(lambientUniform.getName(), lambientUniform);
        uniforms.put(ldiffuseUniform.getName(), ldiffuseUniform);
        uniforms.put(lspecularUniform.getName(), lspecularUniform);
        uniforms.put(lightPositionUniform.getName(), lightPositionUniform);
        
    }
    
    private void initUniforms(){
        
        for(Shader shader : shadersList){
            initUniforms(shader);
        }
    }
    
    private void initUniforms(Shader shader){
        
        Iterator<Entry<String, Integer>> iterator2 = shader.uniformMap.entrySet().iterator();
            
        while(iterator2.hasNext()){ //pour chaque uniform d'un shader
            Entry<String, Integer> uniformEntry = iterator2.next();

            //on ajoute la variable uniform à la map uniforms
            if(uniforms.containsKey(uniformEntry.getKey())){
                uniforms.get(uniformEntry.getKey()).addOwner(shader);

                //handle the case when uniform has been updated before shader has been initialized
                if(uniforms.get(uniformEntry.getKey()).isDirty()){
                    shader.notifyDirty(uniforms.get(uniformEntry.getKey()));
                }
            }
        }
    }
    
    private void notifyShaderDirtyUniforms(Shader shader){
        
        Iterator<Entry<String, Integer>> iterator2 = shader.uniformMap.entrySet().iterator();
            
        while(iterator2.hasNext()){ //pour chaque uniform d'un shader
            Entry<String, Integer> uniformEntry = iterator2.next();

            //on ajoute la variable uniform à la map uniforms
            if(uniforms.containsKey(uniformEntry.getKey())){
                shader.notifyDirty(uniforms.get(uniformEntry.getKey()));
            }
        }
    }
    
    public void init(GL3 gl){
        
        try {
            
            for(Shader shader : shadersList){
                shader.init(gl);
            }
            
            noTranslationShader.init(gl);
            instanceLightedShader.init(gl);
            instanceShader.init(gl);
            texturedShader.init(gl);
            labelShader.init(gl);
            phongShader.init(gl);
            simpleShader.init(gl);
            colorShader.init(gl);
            
            addShader(noTranslationShader);
            addShader(instanceLightedShader);
            addShader(instanceShader);
            addShader(texturedShader);
            addShader(simpleShader);
            addShader(phongShader);
            addShader(labelShader);
            addShader(colorShader);
            
            initUniforms(); //assign owners to uniforms (shaders using the uniforms)
            
            projMatrixOrthoUniform.setValue(MatrixUtility.ortho(0, width, 0, height, -10, 1000));
            viewMatrixOrthoUniform.setValue(MatrixUtility.lookAt(new Vector3f(0,0,0), new Vector3f(0,0,0), new Vector3f(0,1,0)));
            transformationUniform.setValue(MatrixUtility.identity4f());
            
            textureUniform.setValue(0);
            
            //binding de la caméra avec les variables uniforms des shaders
            camera.addPropertyChangeListener("projMatrix", new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    projMatrixUniform.setValue((Matrix4f) evt.getNewValue());
                    mousePicker.setProjectionMatrix((Matrix4f) evt.getNewValue());
                }
            });
            
            camera.addPropertyChangeListener("viewMatrix", new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    viewMatrixUniform.setValue((Matrix4f) evt.getNewValue());
                    mousePicker.setViewMatrix((Matrix4f) evt.getNewValue());
                }
            });
            
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Cannot generate shader", ex);
        }
        
        for(Texture texture : textureList){
            try {
                texture.init(gl);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Cannot generate texture", ex);
            }
        }
            
        for(SceneObject sceneObject : objectsList){
            
            if(sceneObject.getShaderId() != -1 && sceneObject.getMesh() != null){
                
                if(sceneObject.getMesh().getVboId() == -1){
                    sceneObject.initBuffers(gl);
                }
                
                if(sceneObject.getVaoId() == -1){
                    sceneObject.initVao(gl);
                }
                
                sceneObject.setId(objectsList.size());
            }            
        }
        
        for(SceneObject sceneObject : hudList){
            
            if(sceneObject.getShaderId() != -1 && sceneObject.getMesh() != null){
                
                if(sceneObject.getMesh().getVboId() == -1){
                    sceneObject.initBuffers(gl);
                }
                
                if(sceneObject.getVaoId() == -1){
                    sceneObject.initVao(gl);
                }
                
                sceneObject.setId(objectsList.size()+hudList.size());
            }
        }
        
        setLightAmbientValue(getLight().ambient);
        setLightDiffuseValue(getLight().diffuse);
        setLightSpecularValue(getLight().specular);
        
    }
    
    public void addSceneObjectAsHud(SceneObject sceneObject){
        
        hudList.add(sceneObject);
        if(sceneObject.texture != null){
            addTexture(sceneObject.texture);
        }
    }
    
    public void addSceneObject(SceneObject sceneObject){
        
        if(objectsList.isEmpty()){
            
            if(camera.getPivot() == null){
                camera.setPivot(sceneObject);
            }
            
            Vector3f camLoc = camera.getLocation();
            if(camLoc.x == 0 && camLoc.y == 0 && camLoc.z == 0){
                
                if(sceneObject.getGravityCenter() != null){
                    camera.setLocation(new Vector3f(sceneObject.getGravityCenter().x + 20,
                            sceneObject.getGravityCenter().y + 20, sceneObject.getGravityCenter().z + 10));
                }
            }
            
            if(light.position.x == 0 && light.position.y == 0 && light.position.z == 100){
                
                if(sceneObject.getGravityCenter() != null){
                    camLoc = camera.getLocation();
                    light.position = new Point3f(camLoc.x , camLoc.y, camLoc.z);
                }
            }
        }
        
        if(sceneObject.getShader() == null){
            sceneObject.setShader(new SimpleShader());
        }
        
        objectsList.add(sceneObject);
        sceneObject.setId(objectsList.size());
        
        if(sceneObject.texture != null){
            addTexture(sceneObject.texture);
        }
    }
    
    public SceneObject getSceneObject(String name){
        
        int i=0;
        
        for(SceneObject object : objectsList){
            
            if(object.getName().equals(name)){
                return objectsList.get(i);
            }
            
            i++;
        }
        
        i=0;
        
        for(SceneObject object : hudList){
            
            if(object.getName().equals(name)){
                return hudList.get(i);
            }
            
            i++;
        }
        
        return null;
    }
    
    public SceneObject getSceneObject(SceneObject sceneObject){
        
        int i=0;
        
        for(SceneObject object : objectsList){
            
            if(object.equals(sceneObject)){
                return objectsList.get(i);
            }
            
            i++;
        }
        
        i=0;
        
        for(SceneObject object : hudList){
            
            if(object.equals(sceneObject)){
                return hudList.get(i);
            }
            
            i++;
        }
        
        return null;
    }
    
    public void removeSceneObject(SceneObject sceneObject){
        
        objectsList.remove(sceneObject);
    }
    
    public SceneObject getFirstSceneObject(){
        
        if(objectsList.size() > 0){
            return objectsList.get(0);
        }
        
        return null;
    }
    
    public CopyOnWriteArrayList<SceneObject> getSceneObjects(){
        return objectsList;
    }
    
    public void updateMousePicker(float mouseX, float mouseY, int startX, int startY, float viewportWidth, float viewportHeight){
        mousePicker.update(mouseX, mouseY, startX, startY, viewportWidth, viewportHeight);
        mousePickerIsDirty = true;
    }
    
    public void addShader(Shader shader) {
        
        shadersList.add(shader);
    }
    
    public void addTexture(Texture texture){
        textureList.add(texture);
    }
    
    public void draw(final GL3 gl){
        
        camera.updateViewMatrix();
        
        //update picking
        if(mousePickerIsDirty){
            
            //test for intersection (simple method, need to use an octree later)
            for(SceneObject object : objectsList){
                
                if(object.isMousePickable()){
                    Point3f startPoint = mousePicker.getPointOnray(1);
                    Point3f endPoint = mousePicker.getPointOnray(1000);

                    Point3d intersection = Intersection.getIntersectionLineBoundingBox(new Point3d(startPoint.x, startPoint.y, startPoint.z),
                            new Point3d(endPoint.x, endPoint.y, endPoint.z),
                            object.getBoundingBox());

                    if(intersection != null){
                        object.fireClicked(mousePicker, intersection);
                    }
                }
                
            }
            
            mousePickerIsDirty = false;
        }
        
        //add possible new shaders
        for(SceneObject object : objectsList){
            
            if(object.getShaderId() == -1){
                Shader shader = object.getShader();
                shader.init(gl);
                initUniforms(shader);
                addShader(shader);
                notifyShaderDirtyUniforms(shader);
            }
            
            if(object.vaoId == -1){
                object.initBuffers(gl);
                object.initVao(gl);
            }
        }
        
        for(SceneObject object : hudList){
            
            if(object.getShaderId() == -1){
                Shader shader = object.getShader();
                shader.init(gl);
                initUniforms(shader);
                addShader(shader);
                notifyShaderDirtyUniforms(shader);
            }
            
            if(object.vaoId == -1){
                object.initBuffers(gl);
                object.initVao(gl);
            }
        }
        
        //update shader variables
        for(Shader shader : shadersList){
            shader.updateProgram(gl);
        }
        
        //update textures
        for(Texture texture : textureList){
            if(texture.isDirty()){
                try {
                    texture.update(gl);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed to update texture", ex);
                }
            }
        }

        /***draw scene objects***/

        gl.glEnable(GL3.GL_BLEND);
            
        for(SceneObject object : objectsList){
            
            if(object.isVisible()){
                if(!object.depthTest){
                    gl.glClear(GL3.GL_DEPTH_BUFFER_BIT);
                }

                gl.glUseProgram(object.getShaderId());
                    object.draw(gl);
                gl.glUseProgram(0);
            }
        } 
        
        for(SceneObject object : hudList){
            
            if(object.isVisible()){
                
                if(object.vaoId == -1){
                    object.initBuffers(gl);
                    object.initVao(gl);
                }

                if(!object.depthTest){
                    gl.glClear(GL3.GL_DEPTH_BUFFER_BIT);
                }

                gl.glUseProgram(object.getShaderId());
                    object.draw(gl);
                gl.glUseProgram(0);
            }
            
        }
    }

    public Light getLight() {
        return light;
    }

    public void setLight(Light light) {
        this.light = light;
    }
    
    public void setLightAmbientValue(Vector3f ambient){
        light.ambient = ambient;
        lambientUniform.setValue(ambient);
    }
    
    public void setLightDiffuseValue(Vector3f diffuse){
        light.diffuse = diffuse;
        ldiffuseUniform.setValue(diffuse);
    }
    
    public void setLightSpecularValue(Vector3f specular){
        light.specular = specular;
        lspecularUniform.setValue(specular);
    }
    
    public Point3f getLightPosition() {
        return light.position;
    }

    public void setLightPosition(Point3f position) {
        this.light.position = position;
        lightPositionUniform.setValue(new Vector3f(position.x, position.y, position.z));
    }
    

    public void setCamera(TrackballCamera camera) {
        this.camera = camera;
        mousePicker = new MousePicker(camera);
    }

    public TrackballCamera getCamera() {
        return camera;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public MousePicker getMousePicker() {
        return mousePicker;
    }
}
