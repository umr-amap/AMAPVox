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
package org.amapvox.viewer3d.event;

import com.jogamp.newt.event.KeyEvent;
import org.amapvox.viewer3d.input.InputKeyListener;
import org.amapvox.viewer3d.input.InputMouseAdapter;
import org.amapvox.viewer3d.input.InputMouseAdapter.Button;
import org.amapvox.viewer3d.renderer.JoglListener;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * Describes user input behavior
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class BasicEvent extends EventManager{
    
    private final JoglListener joglContext;
    
    private int mouseX;
    private int mouseY;
    
    private int mouseXOldLocation;
    private int mouseYOldLocation;
    
    public BasicEvent(JoglListener context, InputMouseAdapter inputMouseAdapter, InputKeyListener inputKeyListener){
        
        super(inputMouseAdapter, inputKeyListener);
        
        this.joglContext = context;
    }
    
    @Override
    public void updateEvents(){
                
        mouseXOldLocation = mouseX;
        mouseYOldLocation = mouseY;
        
        mouseX = mouse.getXLoc();
        mouseY = mouse.getYLoc();
        
        int deltaX = mouseX - mouseXOldLocation;
        int deltaY = mouseY - mouseYOldLocation;
        
        if(mouse.isButtonClicked(Button.MIDDLE)){
            
            joglContext.updateMousePicker(mouse.getXLoc(), mouse.getYLoc());
        }
        
        
        if(mouse.isButtonDown(Button.LEFT) && mouse.isDragged()){
            
            if(deltaX != 0 || deltaY != 0){
                joglContext.getScene().getCamera().rotateFromOrientationV2(deltaX*0.5f, deltaY*0.5f);
            }
        }
        
        //translate the world
        if(mouse.isButtonDown(Button.RIGHT) && mouse.isDragged()){
                       
            if(deltaX != 0 || deltaY != 0){
                if(Math.abs(deltaX) > 200 || Math.abs(deltaY) > 200){
                    System.out.println(deltaX+"\t"+deltaY);
                }
                joglContext.getScene().getCamera().translateV2(new Vector3f(deltaX*0.5f, deltaY*0.5f, 0.0f));
                
            }
        }
        
        if(mouse.isWheelRotateUp() || mouse.isWheelRotateDown()){
            
            float distanceToTarget = joglContext.getScene().getCamera().getDistanceToTarget();
            joglContext.getScene().getCamera().translate(new Vector3f(0.0f, 0.0f, (distanceToTarget/5.0f)*mouse.getWheelRotationValue()/**mouseScrollSensitivity*/));
        }
        
        if(keyboard.isKeyDown(KeyEvent.VK_RIGHT)){
            
            joglContext.getScene().getCamera().translate(new Vector3f(4.0f, 0.0f, 0.0f));
        }
        
        if(keyboard.isKeyDown(KeyEvent.VK_LEFT)){
            
            joglContext.getScene().getCamera().translate(new Vector3f(-4.0f, 0.0f, 0.0f));
        }
        
        if(keyboard.isKeyDown(KeyEvent.VK_UP)){
            
            joglContext.getScene().getCamera().translate(new Vector3f(0.0f, 4.0f, 0.0f));
        }
        
        if(keyboard.isKeyDown(KeyEvent.VK_DOWN)){
            
            joglContext.getScene().getCamera().translate(new Vector3f(0.0f, -4.0f, 0.0f));
        }
        
        if(keyboard.isKeyDown(KeyEvent.VK_D)){
            
            Point3f currentLightPosition = joglContext.getScene().getLightPosition();
            joglContext.getScene().setLightPosition(new Point3f(currentLightPosition.x, currentLightPosition.y+1, currentLightPosition.z));
        }
        
        if(keyboard.isKeyDown(KeyEvent.VK_Q)){
            
            Point3f currentLightPosition = joglContext.getScene().getLightPosition();
            joglContext.getScene().setLightPosition(new Point3f(currentLightPosition.x, currentLightPosition.y-1, currentLightPosition.z));
        }
        
        if(keyboard.isKeyDown(KeyEvent.VK_Z)){
            
            Point3f currentLightPosition = joglContext.getScene().getLightPosition();
            joglContext.getScene().setLightPosition(new Point3f(currentLightPosition.x, currentLightPosition.y, currentLightPosition.z+1));
        }
        
        if(keyboard.isKeyDown(KeyEvent.VK_S)){
            
            Point3f currentLightPosition = joglContext.getScene().getLightPosition();
            joglContext.getScene().setLightPosition(new Point3f(currentLightPosition.x, currentLightPosition.y, currentLightPosition.z-1));
        }
        
        if(keyboard.isKeyClicked(KeyEvent.VK_AMPERSAND) || keyboard.isKeyClicked(KeyEvent.VK_NUMPAD1) ){
            
            if(keyboard.isControlDown()){
                joglContext.getScene().getCamera().setViewToBack();
            }else{
                joglContext.getScene().getCamera().setViewToFront();
            }
            
            resetMouseLocation();
            joglContext.refresh();
        }
        
        if(keyboard.isKeyClicked(KeyEvent.VK_QUOTEDBL) || keyboard.isKeyClicked(KeyEvent.VK_NUMPAD3) ){
            
            if(keyboard.isControlDown()){
                joglContext.getScene().getCamera().setViewToLeft();
            }else{
                joglContext.getScene().getCamera().setViewToRight();
            }
            
            resetMouseLocation();
            joglContext.refresh();
        }
        
        if(keyboard.isKeyClicked(KeyEvent.VK_NUMPAD7) ){
            
            if(keyboard.isControlDown()){
                joglContext.getScene().getCamera().setViewToBottom();
                
            }else{
                joglContext.getScene().getCamera().setViewToTop();
            }
            
            resetMouseLocation();
            joglContext.refresh();
        }
        
        if(keyboard.isKeyClicked(KeyEvent.VK_LEFT_PARENTHESIS) || keyboard.isKeyClicked(KeyEvent.VK_NUMPAD5) ){
            joglContext.getScene().getCamera().switchPerspective();
            joglContext.updateCamera();
            resetMouseLocation();
            joglContext.refresh();
        }
    }
    
    private void resetMouseLocation(){
        
        mouseXOldLocation = mouse.getXLoc();
        mouseYOldLocation = mouse.getYLoc();
    }
}
