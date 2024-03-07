/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.math.geometry;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author calcul
 */
public class Distance {

    public static float getPointLineDistance(Point3d point, Point3d lineStart, Point3d lineEnd) {

        //compute direction vector of the line
        Vector3d direction = new Vector3d(lineEnd.x - lineStart.x, lineEnd.y - lineStart.y, lineEnd.z - lineStart.z);

        //apply formula
        Vector3d BA = new Vector3d(lineStart.x - point.x, lineStart.y - point.y, lineStart.z - point.z);

        Vector3d result = new Vector3d();
        result.cross(BA, direction);
        return (float) (result.length() / direction.length());
    }
}
