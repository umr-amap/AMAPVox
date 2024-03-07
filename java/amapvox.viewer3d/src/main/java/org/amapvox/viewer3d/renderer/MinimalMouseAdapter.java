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

import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.opengl.util.FPSAnimator;

/**
 * Handle 3d view non-dynamic draw
 * @author Julien Heurtebize
 */
public class MinimalMouseAdapter extends MouseAdapter{

    private final FPSAnimator animator;
    private boolean dynamicDraw;
    
    public MinimalMouseAdapter(FPSAnimator animator, boolean dynamicDraw) {
        this.animator = animator;
        this.dynamicDraw = dynamicDraw;
    }
    
    @Override
    public void mouseClicked(MouseEvent me) {
        
        if(animator.isPaused()){
            animator.resume();
        }
        
        if(me.getButton() == MouseEvent.BUTTON2){
            
            if(animator.isPaused()){
                animator.resume();
            }else{
                if(!dynamicDraw){
                    animator.pause();
                }
            }
            
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
         if(animator.isPaused()){
            animator.resume();
        }
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        
        if(!animator.isPaused() && !dynamicDraw){
            animator.pause();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(animator.isPaused()){
            animator.resume();
        }
    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {
        if(animator.isPaused()){
            animator.resume();
        }
    }

    public boolean isDynamicDraw() {
        return dynamicDraw;
    }

    public void setDynamicDraw(boolean dynamicDraw) {
        this.dynamicDraw = dynamicDraw;
    }
    
}
