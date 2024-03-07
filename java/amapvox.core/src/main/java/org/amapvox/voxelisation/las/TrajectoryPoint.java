/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation.las;

import javax.vecmath.Point3d;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class TrajectoryPoint extends Point3d {
    
    public Point3d point;
    public double time;
    
    public TrajectoryPoint(double x, double y, double z, double t) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.time = t;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Time ").append((float) time).append(" ");
        sb.append("Easting ").append((float) x).append(" ");
        sb.append("Northing ").append((float) y).append(" ");
        sb.append("Elevation ").append((float) z);
        return sb.toString();
    }   
}
