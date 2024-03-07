/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.math.geometry;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 *
 * @author calcul
 */
public class Plane {
    
    private final float a;
    private final float b;
    private final float c;
    private final float d;
    
    private final Point3f point;
    
    public Plane(Vector3f u, Vector3f v, Point3f A){
        
        Vector3f normal = new Vector3f();
        normal.cross(u, v);
        normal.normalize();
        a = normal.x;
        b = normal.y;
        c = normal.z;
        d = -1.f * (new Vector3f(A.x, A.y, A.z).dot(normal));
        this.point = A;
    }
    
    public float getZFromXY(float x, float y){
        
        float z = ((-a*x)-(b*y)-d) / c;
        
        if(Float.isInfinite(z)){
            z = Float.NaN;
        }else if(Float.isNaN(z)){
            z = Float.POSITIVE_INFINITY;
        }
        
        return z;
    }
    
    public float getXFromYZ(float y, float z){
        return ((-b*y)-(c*z)-d) / a;
    }
    
    public float getYFromXZ(float x, float z){
        return ((-a*x)-(c*z)-d) / b;
    }
    
    public Vector3f getNormale(){
        return new Vector3f(a, b, c);
    }

    public Point3f getPoint() {
        return point;
    }
    
    public static void main(String[] args){
        
        Plane p = new Plane(new Vector3f(-17.21f, 8.95f, 0f), new Vector3f(-17.73f, -13.51f, 0f), new Point3f(0, 0, 4));
        float x = p.getXFromYZ(3, 4);
        float z = p.getZFromXY(3, 4);
        System.out.println(x);
    }
}
