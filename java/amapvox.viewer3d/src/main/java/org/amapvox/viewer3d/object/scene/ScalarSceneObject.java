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

import org.amapvox.viewer3d.mesh.GLMesh;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Julien Heurtebize
 */
public class ScalarSceneObject extends SimpleSceneObject{
    
    protected final Map<String, ScalarField> scalarFieldsList;
    protected String currentAttribut;
    
    public ScalarSceneObject() {
        this.scalarFieldsList = new HashMap<>();
    }
    
    public ScalarSceneObject(GLMesh mesh, boolean isAlphaRequired) {
        
        super(mesh, isAlphaRequired);
        
        this.scalarFieldsList = new HashMap<>();
        
    }
    
    protected final void init(){
                
        Iterator<Map.Entry<String, ScalarField>> iterator = scalarFieldsList.entrySet().iterator();
        
        while(iterator.hasNext()){
            
            iterator.next().getValue().buildHistogram();
        }
        
        ScalarField scalarField = scalarFieldsList.entrySet().iterator().next().getValue();
        
        currentAttribut = scalarField.getName();
        
    }
    
    public void switchToNextColor(){
        
        Iterator<Map.Entry<String, ScalarField>> iterator = scalarFieldsList.entrySet().iterator();
        
        while(iterator.hasNext()){
            
            String key = iterator.next().getKey();
            if(key.equals(currentAttribut)){
                
                if(iterator.hasNext()){
                    switchColor(iterator.next().getKey());
                }else{
                    switchColor(scalarFieldsList.entrySet().iterator().next().getKey());
                }
            }
        }
    }
    
    public void switchColor(String colorAttributIndex){
        
        
        if(mesh == null){
            
            //mesh = new PointCloudGLMesh();
            //initMesh();
        }
        
        if(scalarFieldsList.containsKey(colorAttributIndex)){
            
            ScalarField scalarField = scalarFieldsList.get(colorAttributIndex);
            
            currentAttribut = scalarField.getName();
            updateColor();
        }
    }
    
    public float[] updateColor(ScalarField scalarField){
        
        int nbValues;
        float[] colorDataArray;
        
        if(scalarField.hasColorGradient){
            
            nbValues = scalarField.getNbValues()*3;
            colorDataArray = new float[nbValues];
            
            for(int i=0, j=0;i<scalarField.getNbValues();i++, j+=3){

                colorDataArray[j] = scalarField.getColor(i).getRed()/255.0f;
                colorDataArray[j+1] = scalarField.getColor(i).getGreen()/255.0f;
                colorDataArray[j+2] = scalarField.getColor(i).getBlue()/255.0f;
            }
        }else{
            
            nbValues = scalarField.getNbValues();
            colorDataArray = new float[nbValues];
            
            for(int i=0;i<scalarField.getNbValues();i++){
                colorDataArray[i] = scalarField.getValue(i)/255.0f;
            }
        }
        
        return colorDataArray;
    }
    
    public void updateColor(){
        
        ScalarField scalarField = scalarFieldsList.get(currentAttribut);
        
        int nbValues;
        float[] colorDataArray;
        
        if(scalarField.hasColorGradient){
            
            nbValues = scalarField.getNbValues()*3;
            colorDataArray = new float[nbValues];
            
            for(int i=0, j=0;i<scalarField.getNbValues();i++, j+=3){

                colorDataArray[j] = scalarField.getColor(i).getRed()/255.0f;
                colorDataArray[j+1] = scalarField.getColor(i).getGreen()/255.0f;
                colorDataArray[j+2] = scalarField.getColor(i).getBlue()/255.0f;
            }
        }else{
            
            nbValues = scalarField.getNbValues();
            colorDataArray = new float[nbValues];
            
            for(int i=0;i<scalarField.getNbValues();i++){
                colorDataArray[i] = scalarField.getValue(i)/255.0f;
            }
        }

        mesh.setColorData(colorDataArray);
        colorNeedUpdate = true;
    }
    
    public Map<String, ScalarField> getScalarFieldsList() {
        return scalarFieldsList;
    }
    
    public void addValue(String index, float value){
        
        if(!scalarFieldsList.containsKey(index)){
            
            scalarFieldsList.put(index, new ScalarField(index));
        }
        
        scalarFieldsList.get(index).addValue(value);
    }
    
    public void addValue(String index, float value, boolean gradient){
        
        
        if(!scalarFieldsList.containsKey(index)){
            ScalarField scalarField = new ScalarField(index);
            scalarField.hasColorGradient = gradient;
            scalarFieldsList.put(index, scalarField);
        }
        
        scalarFieldsList.get(index).addValue(value);
    }
}
