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
package org.amapvox.viewer3d.input;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;


/**
 * Class to handle keys states
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class InputKeyListener implements KeyListener{
    
    //private final EventManager listener;
    private final boolean[] keyStates;
    private final boolean[] keysClicked;
    private boolean controlDown;
    private boolean altDown;
    private boolean altGraphDown;
    private boolean shiftDown;
    
    public InputKeyListener(/*EventManager listener*/){
        
        //this.listener = listener;
        this.keyStates = new boolean[256];
        this.keysClicked = new boolean[256];
    }
    
    private void updateSpecialKeys(KeyEvent ke){
        
        controlDown = ke.isControlDown();
        shiftDown = ke.isShiftDown();
        altDown = ke.isAltDown();
        altGraphDown = ke.isAltGraphDown();
    }
    
    @Override
    public void keyPressed(KeyEvent ke) {
        
        //listener.ctrlPressed = ke.isControlDown();
        
        keyStates[ke.getKeyCode()] = true;
        
        updateSpecialKeys(ke);
        
        /*switch(ke.getKeyCode()){
            
            case KeyEvent.VK_LEFT:
                listener.leftKeyPressed = true;
                break;
            case KeyEvent.VK_RIGHT:
                listener.rightKeyPressed = true;
                break;
            case KeyEvent.VK_UP:
                listener.upKeyPressed = true;
                break;
            case KeyEvent.VK_DOWN:
                listener.downKeyPressed = true;
                break;
            case KeyEvent.VK_ENTER:
                listener.spaceKeyPressed = true;
                break;
            case KeyEvent.VK_ESCAPE:
                listener.escapeKeyPressed = true;
                break;
            case KeyEvent.VK_Z:
                listener.zKeyPressed = true;
                break;
            case KeyEvent.VK_S:
                listener.sKeyPressed = true;
                break;
            case KeyEvent.VK_Q:
                listener.qKeyPressed = true;
                break;
            case KeyEvent.VK_D:
                listener.dKeyPressed = true;
                break;
            case KeyEvent.VK_ADD:
            case KeyEvent.VK_EQUALS:    
                listener.plusKeyPressed = true;
                break;
            case KeyEvent.VK_SUBTRACT:
            case KeyEvent.VK_MINUS:
                listener.minusKeyPressed = true;
                break;
            case KeyEvent.VK_NUMPAD1:
            case KeyEvent.VK_AMPERSAND:
                listener.number1KeyPressed = true;
                break;
            case KeyEvent.VK_NUMPAD3:
            case KeyEvent.VK_QUOTEDBL:
                listener.number3KeyPressed = true;
                break;
            case KeyEvent.VK_NUMPAD7:
            case 232:
                listener.number7KeyPressed = true;
                break;
            case KeyEvent.VK_NUMPAD5:
            case KeyEvent.VK_LEFT_PARENTHESIS:
                listener.number5KeyPressed = true;
                break;
        }*/
    }

    @Override
    public void keyReleased(KeyEvent ke) {
        
        if(  ke.isAutoRepeat() ) {
            return;
        }
        
        updateSpecialKeys(ke);
        
        keyStates[ke.getKeyCode()] = false;
        keysClicked[ke.getKeyCode()] = true;
        
        /*listener.ctrlPressed = ke.isControlDown();
        
        switch(ke.getKeyCode()){
            
            case KeyEvent.VK_LEFT:
                listener.leftKeyPressed = false;
                break;
            case KeyEvent.VK_RIGHT:
                listener.rightKeyPressed = false;
                break;
            case KeyEvent.VK_UP:
                listener.upKeyPressed = false;
                break;
            case KeyEvent.VK_DOWN:
                listener.downKeyPressed = false;
                break;
            case KeyEvent.VK_ENTER:
                listener.spaceKeyPressed = false;
                break;
            case KeyEvent.VK_Z:
                listener.zKeyPressed = false;
                break;
            case KeyEvent.VK_S:
                listener.sKeyPressed = false;
                break;
            case KeyEvent.VK_Q:
                listener.qKeyPressed = false;
                break;
            case KeyEvent.VK_D:
                listener.dKeyPressed = false;
                break;
            case KeyEvent.VK_ADD:
            case KeyEvent.VK_EQUALS:  
                listener.plusKeyPressed = false;
                break;
            case KeyEvent.VK_SUBTRACT:
            case KeyEvent.VK_MINUS:
                listener.minusKeyPressed = false;
                break;
            case KeyEvent.VK_NUMPAD1:
            case KeyEvent.VK_AMPERSAND:
                listener.number1KeyPressed = false;
                break;
            case KeyEvent.VK_NUMPAD3:
            case KeyEvent.VK_QUOTEDBL:
                listener.number3KeyPressed = false;
                break;
            case KeyEvent.VK_NUMPAD7:
            case 232:
                listener.number7KeyPressed = false;
                break;
            case KeyEvent.VK_NUMPAD5:
            case KeyEvent.VK_LEFT_PARENTHESIS:
                listener.number5KeyPressed = false;
                break;
        }*/
    }

    public boolean isKeyDown(short key) {
        
        if(key < keyStates.length){
            return keyStates[key];
        }else{
            return false;
        }
    }
    
    public boolean isKeyClicked(short key) {
        
        if (key < 256) {
            boolean clicked = keysClicked[key];
            keysClicked[key] = false;
            return clicked;
        } else {
            return false;
        }
    }

    public boolean isControlDown() {
        return controlDown;
    }

    public boolean isAltDown() {
        return altDown;
    }

    public boolean isAltGraphDown() {
        return altGraphDown;
    }

    public boolean isShiftDown() {
        return shiftDown;
    }
    
}
