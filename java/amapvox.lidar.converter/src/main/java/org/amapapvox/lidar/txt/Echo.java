/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapapvox.lidar.txt;

/**
 *
 * @author Julien Heurtebize
 */
public class Echo {
    
    private double range;
    private Object[] objects;

    public Echo(double range, Object... objects) {
        this.range = range;
        this.objects = objects;
    }

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }
    
    public double getXPosition(Shot shot){
        return shot.getXOrigin() + shot.getXDirection() * range;
    }
    
    public double getYPosition(Shot shot){
        return shot.getYOrigin() + shot.getYDirection() * range;
    }
    
    public double getZPosition(Shot shot){
        return shot.getZOrigin() + shot.getZDirection() * range;
    }

    public Object[] getObjects() {
        return objects;
    }
    
    public <T> T getAttribute(String name, ShotFileContext context){
        
        Integer index = context.getColumnIndex(name);
        
        Object o = objects[8 - index];
        return (T)o;
    }
}
