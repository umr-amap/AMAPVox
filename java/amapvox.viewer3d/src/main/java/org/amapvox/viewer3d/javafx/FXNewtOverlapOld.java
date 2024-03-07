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
package org.amapvox.viewer3d.javafx;

import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.opengl.GLWindow;
import org.amapvox.viewer3d.SimpleViewer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Set a {@link GLWindow} to overlap a javafx node.
 * @author Julien Heurtebize
 */
public class FXNewtOverlapOld {
    
    public static void link (final Stage stage, final SimpleViewer viewer3D, final Node node) {
        
        final GLWindow gLWindow = viewer3D.getRenderFrame();
        
        gLWindow.setUndecorated(true);
        
        //initialize
        //gLWindow.setPosition((int)stage.getX(), gLWindow.getY());
        //gLWindow.setAlwaysOnTop(true);
        
        stage.xProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                Bounds bounds = node.getBoundsInLocal();
                Bounds localToScreen = node.localToScreen(bounds);
                gLWindow.setSize((int)localToScreen.getWidth(), (int)localToScreen.getHeight());
                gLWindow.setPosition((int)localToScreen.getMinX(), (int)localToScreen.getMinY());
                gLWindow.setVisible(false);
            }
        });
        
        stage.yProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                Bounds bounds = node.getBoundsInLocal();
                Bounds localToScreen = node.localToScreen(bounds);
                gLWindow.setSize((int)localToScreen.getWidth(), (int)localToScreen.getHeight());
                gLWindow.setPosition((int)localToScreen.getMinX(), (int)localToScreen.getMinY());
                gLWindow.setVisible(false);
            }
        });
        
        stage.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(!newValue){
                    gLWindow.setAlwaysOnTop(true);
                    gLWindow.setAlwaysOnTop(false);
                    //show(gLWindow, node);
                }else{
                    show(gLWindow, node);
                    gLWindow.setAlwaysOnTop(true);
                }
            }
        });
        
        
        
        node.boundsInLocalProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
                
                //viewer3D.setDynamicDraw(false);
                Bounds localToScreen = node.localToScreen(newValue);
                gLWindow.setPosition((int)localToScreen.getMinX(), (int)localToScreen.getMinY());
                gLWindow.setSize((int)newValue.getWidth(), (int)newValue.getHeight());
                
            }
        });
        
        gLWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowResized(com.jogamp.newt.event.WindowEvent e) {
                gLWindow.setVisible(true);
            }

            @Override
            public void windowMoved(com.jogamp.newt.event.WindowEvent e) {
                gLWindow.setVisible(true);
            }
            
            @Override
            public void windowDestroyed(com.jogamp.newt.event.WindowEvent e) {
                stage.close();
            }
        });
        
        stage.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                show(gLWindow, node);
            }
        });
        
        stage.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                show(gLWindow, node);
            }
        });
        
        stage.addEventFilter(WindowEvent.WINDOW_HIDDEN, new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                gLWindow.setVisible(false);
            }
        });
        
        stage.maximizedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    
                    //fix for bug https://bugs.openjdk.java.net/browse/JDK-8087997
                    stage.setIconified(false);
                    show(gLWindow, node);
                    
                }else{
                    gLWindow.setVisible(false);
                }
            }
        });
        
        
        stage.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                
                if(newValue){
                    //fix for bug https://bugs.openjdk.java.net/browse/JDK-8087997
                    stage.setMaximized(false);
                    
                    gLWindow.setVisible(false);
                }else{
                    //fix for bug https://bugs.openjdk.java.net/browse/JDK-8087997
                    stage.setMaximized(true);
                    
                    show(gLWindow, node);
                }
                
            }
        });
    }
    
    private static void show(GLWindow gLWindow, Node node){
        Bounds bounds = node.getBoundsInLocal();
        Bounds localToScreen = node.localToScreen(bounds);
        gLWindow.setPosition((int)localToScreen.getMinX(), (int)localToScreen.getMinY());
        gLWindow.setSize((int)localToScreen.getWidth(), (int)localToScreen.getHeight());
    }
}
