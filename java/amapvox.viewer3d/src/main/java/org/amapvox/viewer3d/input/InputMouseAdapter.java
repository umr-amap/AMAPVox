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

import com.jogamp.newt.event.MouseAdapter;

/**
 * Class to handle mouse states
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class InputMouseAdapter extends MouseAdapter {

    //private final EventManager listener;
    private final boolean[] mouseButtonDown;
    private final boolean[] mouseButtonClicked;

    private boolean moved;
    private int xLoc;
    private int yLoc;
    private int oldXLoc;
    private int oldYLoc;

    private boolean wheelRotateUp;
    private boolean wheelRotateDown;
    private float wheelRotationValue;

    private boolean dragged;

    public enum Button {

        LEFT((short) 1),
        MIDDLE((short) 2),
        RIGHT((short) 3);

        private final short id;

        private Button(short id) {
            this.id = id;
        }
    }

    public InputMouseAdapter(/*EventManager listener*/) {

        mouseButtonDown = new boolean[256];
        mouseButtonClicked = new boolean[256];
        //this.listener = listener;
    }

    @Override
    public void mousePressed(com.jogamp.newt.event.MouseEvent me) {

        mouseButtonDown[me.getButton()] = true;

        /*if(me.getButton() == MouseEvent.BUTTON1){
            
            listener.leftMousePressed = true;
        }
        
        if(me.getButton() == MouseEvent.BUTTON3){
            
            listener.rightMousePressed = true;
        }
        
        if(me.getButton() == MouseEvent.BUTTON2){
            
            listener.middleMousePressed = true;
        }*/
    }

    @Override
    public void mouseReleased(com.jogamp.newt.event.MouseEvent me) {

        if (me.isAutoRepeat()) {
            return;
        }

        mouseButtonDown[me.getButton()] = false;
        mouseButtonClicked[me.getButton()] = true;

        /*if(me.getButton() == MouseEvent.BUTTON1){
            
            listener.leftMousePressed = false;
        }
        
        if(me.getButton() == MouseEvent.BUTTON3){
            
            listener.rightMousePressed = false;
        }
        
        if(me.getButton() == MouseEvent.BUTTON2){
            
            listener.middleMousePressed = false;
        }*/
    }

    @Override
    public void mouseMoved(com.jogamp.newt.event.MouseEvent me) {

        setMouseXCurrentLocation(me.getX());
        setMouseYCurrentLocation(me.getY());
        
        dragged = false;
        /*listener.mouseMoved = true;
        listener.setMouseXCurrentLocation(me.getX());
        listener.setMouseYCurrentLocation(me.getY());*/

    }

    @Override
    public void mouseDragged(com.jogamp.newt.event.MouseEvent me) {

        setMouseXCurrentLocation(me.getX());
        setMouseYCurrentLocation(me.getY());
        
        dragged = true;
        /*switch(me.getButton()){
            
            case MouseEvent.BUTTON1:
                
                listener.setMouseXCurrentLocation(me.getX());
                listener.setMouseYCurrentLocation(me.getY());

                lastPositionX = listener.getMouseXCurrentLocation();
                lastPositionY = listener.getMouseYCurrentLocation();
                
                break;
                
            case MouseEvent.BUTTON3:
                
                listener.setMouseXCurrentLocation(me.getX());
                listener.setMouseYCurrentLocation(me.getY());

                lastPositionX = listener.getMouseXCurrentLocation();
                lastPositionY = listener.getMouseYCurrentLocation();
                
                break;
        }*/
    }

    @Override
    public void mouseWheelMoved(com.jogamp.newt.event.MouseEvent me) {

        float[] rotation = me.getRotation();
        float verticalRotation = rotation[1];

        if (verticalRotation > 0.0f) {

            wheelRotateUp = true;
            wheelRotationValue = verticalRotation;
            //listener.mouseWheelRotateUp = true;

        } else if (verticalRotation < 0.0f) {

            wheelRotateDown = true;
            wheelRotationValue = verticalRotation;
            //listener.mouseWheelRotateDown = true;
        }

    }

    public boolean isButtonDown(Button button) {

        if (button.id < 256) {
            return mouseButtonDown[button.id];
        } else {
            return false;
        }
    }
    
    public boolean isButtonClicked(Button button) {

        if (button.id < 256) {
            boolean clicked = mouseButtonClicked[button.id];
            mouseButtonClicked[button.id] = false;
            return clicked;
        } else {
            return false;
        }
    }

    public boolean isMoved() {
        boolean mouseMovedOld = moved;
        moved = false;
        return mouseMovedOld;
    }

    public int getXLoc() {
        return xLoc;
    }

    public int getYLoc() {
        return yLoc;
    }

    public boolean isWheelRotateUp() {

        boolean mouseRotate = wheelRotateUp;
        wheelRotateUp = false;
        return mouseRotate;
    }

    public boolean isWheelRotateDown() {
        boolean mouseRotate = wheelRotateDown;
        wheelRotateDown = false;
        return mouseRotate;
    }

    public float getWheelRotationValue() {
        return wheelRotationValue;
    }

    private void setMouseXCurrentLocation(int mouseXCurrentLocation) {
        oldXLoc = this.xLoc;
        this.xLoc = mouseXCurrentLocation;
        moved = true;
    }

    private void setMouseYCurrentLocation(int mouseYCurrentLocation) {
        oldYLoc = this.yLoc;
        this.yLoc = mouseYCurrentLocation;
        moved = true;
    }

    public int getOldXLoc() {
        return oldXLoc;
    }

    public int getOldYLoc() {
        return oldYLoc;
    }

    public void resetOldLoc() {
        oldXLoc = this.xLoc;
        oldYLoc = this.yLoc;
    }

    public boolean isDragged() {
        boolean mouseDraggedOld = dragged;
        dragged = false;
        return mouseDraggedOld;
    }

    public boolean[] getMouseButtonDown() {
        return mouseButtonDown;
    }

}
