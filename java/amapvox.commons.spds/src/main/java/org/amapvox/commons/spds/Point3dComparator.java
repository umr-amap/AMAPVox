/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.spds;

import java.util.Comparator;
import javax.vecmath.Point3d;

/**
 *
 * @author pverley
 */
public class Point3dComparator implements Comparator<Point3d> {

    @Override
    public int compare(Point3d p1, Point3d p2) {
        
        if (p1.x != p2.x) {
            return Double.compare(p1.x, p2.x);
        } else if (p1.y != p2.y) {
            return Double.compare(p1.y, p2.y);
        } else if (p1.z != p2.z) {
            return Double.compare(p1.z, p2.z);
        }
        return 0;
    }

}
