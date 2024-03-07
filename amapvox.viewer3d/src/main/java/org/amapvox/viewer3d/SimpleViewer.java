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
package org.amapvox.viewer3d;

import com.jogamp.nativewindow.util.Point;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;
import org.amapvox.viewer3d.event.BasicEvent;
import org.amapvox.viewer3d.event.EventManager;
import org.amapvox.viewer3d.input.InputKeyListener;
import org.amapvox.viewer3d.input.InputMouseAdapter;
import org.amapvox.viewer3d.renderer.GLRenderFrame;
import org.amapvox.viewer3d.renderer.MinimalWindowAdapter;
import org.amapvox.viewer3d.renderer.JoglListener;
import org.amapvox.viewer3d.renderer.MinimalKeyAdapter;
import org.amapvox.viewer3d.renderer.MinimalMouseAdapter;
import org.amapvox.viewer3d.renderer.RenderListener;

/**
 * 
 * @author Julien Heurtebize
 */
public class SimpleViewer {

    private final GLRenderFrame renderFrame;
    private final JoglListener joglContext;
    private final FPSAnimator animator;  
    
    private boolean focused;
    
    private final int width;
    private final int height;
    
    private boolean dynamicDraw = false;
    private final MinimalMouseAdapter minimalMouseAdapter;
    private final EventManager minimalEventMgr;
    
    private final BasicEvent basicEvents;
    
    public SimpleViewer(int posX, int posY, int width, int height, String title) throws GLException {
        
        GLProfile glp = GLProfile.getMaximum(false);
        GLCapabilities caps = new GLCapabilities(glp);
        caps.setDoubleBuffered(true);

        this.width = width;
        this.height = height;

        renderFrame = new GLRenderFrame(caps, posX, posY, width, height, title);
        //renderFrame = GLRenderFrame.create(caps, posX, posY, width, height, title);
        animator = new FPSAnimator(60);

        /* From doc : 
        An Animator can be attached to one or more GLAutoDrawables to drive their display() methods in a loop.
        The Animator class creates a background thread in which the calls to display() are performed.
        After each drawable has been redrawn, a brief pause is performed to avoid swamping the CPU,
        unless setRunAsFastAsPossible(boolean) has been called.
        */
        animator.add(renderFrame);

        joglContext = new JoglListener(animator);
        joglContext.getScene().setWidth(width);
        joglContext.getScene().setHeight(height);

        renderFrame.addGLEventListener(joglContext);
        renderFrame.addWindowListener(new MinimalWindowAdapter(animator));


        InputKeyListener inputKeyListener = new InputKeyListener(); 
        InputMouseAdapter inputMouseAdapter = new InputMouseAdapter();
        
        minimalEventMgr = new EventManager(inputMouseAdapter, inputKeyListener) {
            @Override
            public void updateEvents() {
                
                if(!animator.isPaused() && !joglContext.isDynamicDraw() &&
                        !mouse.isButtonDown(InputMouseAdapter.Button.LEFT) && !mouse.isButtonDown(InputMouseAdapter.Button.RIGHT)){

                    //this function cost time, it should not be called at each updateEvents method call
                    animator.pause();
                }
            }
        };
        
        addEventListener(minimalEventMgr);
        
        basicEvents = new BasicEvent(joglContext, inputMouseAdapter, inputKeyListener);
        addEventListener(basicEvents);
        
        //basic input adapters for waking up animator if necessary
        MinimalKeyAdapter minimalKeyAdapter = new MinimalKeyAdapter(animator);
        renderFrame.addKeyListener(minimalKeyAdapter);

        minimalMouseAdapter = new MinimalMouseAdapter(animator, dynamicDraw);
        renderFrame.addMouseListener(minimalMouseAdapter);
    }
    
    public final void addEventListener(EventManager eventManager){
        
        renderFrame.addKeyListener(eventManager.getKeyboard());
        renderFrame.addMouseListener(eventManager.getMouse());
        joglContext.addEventListener(eventManager);
    }
    
    /**
     * Remove the default action listener who is handling mouse and keyboard events.
     * You can call this method if you want to custom events.
     */
    public void removeDefaultEventManager(){
        renderFrame.removeKeyListener(basicEvents.getKeyboard());
        renderFrame.removeMouseListener(basicEvents.getMouse());
        joglContext.removeEventListener(basicEvents);
    }
    
    public org.amapvox.viewer3d.object.scene.Scene getScene(){
        return getJoglContext().getScene();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public GLRenderFrame getRenderFrame() {
        return renderFrame;
    }
    
    
    public Point getPosition(){
        try{
            Point locationOnScreen = renderFrame.getLocationOnScreen(null);
            return new Point(locationOnScreen.getX(), locationOnScreen.getY());
        }catch(RuntimeException ex){
            return new Point(0, 0);
        }
    }
    
    public void show(){
        //this.setOnTop();
        animator.start();
        renderFrame.setVisible(true);
    }
    
    public void close(){
        if(renderFrame.isVisible()){
            //renderFrame.setVisible(false);
            renderFrame.destroy();
        }
    }
    
    public void setOnTop(){
        renderFrame.setAlwaysOnTop(true);
        renderFrame.setAlwaysOnTop(false);
    }

    public JoglListener getJoglContext() {
        return joglContext;
    }

    public FPSAnimator getAnimator() {
        return animator;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setIsFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean isDynamicDraw() {
        return dynamicDraw;
    }

    public void setDynamicDraw(boolean dynamicDraw) {
        this.dynamicDraw = dynamicDraw;
        joglContext.setDynamicDraw(dynamicDraw);
        minimalMouseAdapter.setDynamicDraw(dynamicDraw);
    }

    public EventManager getEventManager() {
        return basicEvents;
    }

    public void takeScreenshot(RenderListener listener){
        joglContext.setTakeScreenShot(true, listener);
        joglContext.refresh();
    }
}
