
/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */

package org.amapvox.commons.math.geometry;

import javax.vecmath.Point3d;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class BoundingBox3D {
    
    public Point3d min;
    public Point3d max;
    
    public BoundingBox3D(){
        min = new Point3d();
        max = new Point3d();
    }

    public BoundingBox3D(Point3d min, Point3d max) {
        this.min = min;
        this.max = max;
    }
    
    public void keepLargest(BoundingBox3D boundingBox3D){
        
        double xMin, yMin, zMin;
        double xMax, yMax, zMax;
        
        if(boundingBox3D.min.x < min.x){
            xMin = boundingBox3D.min.x;
        }else{
            xMin = min.x;
        }
        
        if(boundingBox3D.min.y < min.y){
            yMin = boundingBox3D.min.y;
        }else{
            yMin = min.y;
        }
        
        if(boundingBox3D.min.z < min.z){
            zMin = boundingBox3D.min.z;
        }else{
            zMin = min.z;
        }
        
        if(boundingBox3D.max.x > max.x){
            xMax = boundingBox3D.max.x;
        }else{
            xMax = max.x;
        }
        
        if(boundingBox3D.max.y > max.y){
            yMax = boundingBox3D.max.y;
        }else{
            yMax = max.y;
        }
        
        if(boundingBox3D.max.z > max.z){
            zMax = boundingBox3D.max.z;
        }else{
            zMax = max.z;
        }
        
        this.min = new Point3d(xMin, yMin, zMin);
        this.max = new Point3d(xMax, yMax, zMax);
    }
}
