/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.spds;

import javax.vecmath.Point3d;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Sphere {
    
    private Point3d center;
    private float radius;
    
    public Sphere(){
        
        this.radius = 1.0f;
        this.center = new Point3d();
    }
    
    public Sphere(float radius){
        
        this.radius = radius;
        this.center = new Point3d();
    }
    
    public Sphere(Point3d center){
        
        this.radius = 1.0f;
        this.center = center;
    }
    
    public Sphere(Point3d center, float radius){
        
        this.radius = radius;
        this.center = center;
    }

    public Point3d getCenter() {
        return center;
    }

    public void setCenter(Point3d center) {
        this.center = center;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
    
    public double distanceTo(Point3d target){
        return center.distance(target);
    }
}
