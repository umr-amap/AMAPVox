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

import org.amapvox.viewer3d.input.InputKeyListener;
import org.amapvox.viewer3d.input.InputMouseAdapter;

/**
 * Abstract class that describes user input behavior
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public abstract class EventManager {
        
    protected final InputMouseAdapter mouse;
    protected final InputKeyListener keyboard;
    
    protected float mouseScrollSensitivity = 5.0f;
    
    public EventManager(InputMouseAdapter inputMouseAdapter, InputKeyListener inputKeyListener) {
        
        this.mouse = inputMouseAdapter;
        this.keyboard = inputKeyListener;
    }
    
    /**
     * update events
     */
    public abstract void updateEvents();

    public InputMouseAdapter getMouse() {
        return mouse;
    }

    public InputKeyListener getKeyboard() {
        return keyboard;
    }

    public float getMouseScrollSensitivity() {
        return mouseScrollSensitivity;
    }

    public void setMouseScrollSensitivity(float mouseScrollSensitivity) {
        this.mouseScrollSensitivity = mouseScrollSensitivity;
    }
}
