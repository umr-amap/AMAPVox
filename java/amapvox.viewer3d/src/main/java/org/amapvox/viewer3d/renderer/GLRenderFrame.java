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
package org.amapvox.viewer3d.renderer;

import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Window;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class GLRenderFrame extends GLWindow{
    
    public int width;
    public int height;
    
    private GLRenderFrame(Window w){
        super(w);
    }
    
    public GLRenderFrame(GLCapabilities caps, int posX, int posY, int width, int height, String title){
        
        super(NewtFactory.createWindow(caps));
        setTitle("3D viewer - "+title);
        
        //viewer.setVisible(true); 
        setSize(width, height);
        this.width = width;
        this.height = height;
        setPosition(posX, posY);
        
        /*viewer.setPosition((GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth()/2)-320,
                           (GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight()/2)-240);*/
        //viewer.setAlwaysOnTop(true);
        super.setDefaultCloseOperation(WindowClosingMode.DISPOSE_ON_CLOSE);
    }
    
    public static GLRenderFrame create(GLCapabilities caps, int posX, int posY, int width, int height, String title){
                        
        GLRenderFrame viewer = new GLRenderFrame(NewtFactory.createWindow(caps)); 
        viewer.setTitle("3D viewer - "+title);
        
        //viewer.setVisible(true); 
        viewer.setSize(width, height);
        viewer.width = width;
        viewer.height = height;
        viewer.setPosition(posX, posY);
        
        /*viewer.setPosition((GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth()/2)-320,
                           (GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight()/2)-240);*/
        //viewer.setAlwaysOnTop(true); 
        viewer.setDefaultCloseOperation(WindowClosingMode.DISPOSE_ON_CLOSE);

        return viewer; 
    }

}
