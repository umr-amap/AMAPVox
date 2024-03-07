/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.viewer3d;

import java.awt.Color;
import javax.vecmath.Point3i;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class VoxelObject {

    private final Point3i index;
    private final float[] attributes;
    
    private float value;
    private boolean hidden;
    private Color color;

    public VoxelObject(Point3i index, String[] variables, float alpha) {

        this.index = index;

        this.color = new Color(0, 0, 0, 1.0f);
        this.attributes = new float[variables.length + 3];
        attributes[0] = index.x;
        attributes[1] = index.y;
        attributes[2] = index.z;
        for (int l = 0; l < variables.length; l++) {
            try {
                attributes[l + 3] = Float.valueOf(variables[l]);
            } catch (NumberFormatException ex) {
                attributes[l] = Float.NaN;
            }
        }
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public float getAttribute(int i) {
        return attributes[i];
    }

    public Point3i getIndex() {
        return index;
    }

    public void setAlpha(int alpha) {
        this.color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public void setColor(int red, int green, int blue) {
        this.color = new Color(red, green, blue, color.getAlpha());
    }

    public float getAlpha() {
        return color.getAlpha() / 255.0f;
    }

    public float getRed() {
        return color.getRed() / 255.0f;
    }

    public float getGreen() {
        return color.getGreen() / 255.0f;
    }

    public float getBlue() {
        return color.getBlue() / 255.0f;
    }
}
