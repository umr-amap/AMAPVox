/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.math.geometry;

import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3f;

/**
 *
 * @author calcul
 */
public class AABB {

    private final Point3f[] points;
    private final int[][] faces;

    public AABB(BoundingBox3F boundingBox) {

        this.points = new Point3f[8];

        this.points[0] = new Point3f(boundingBox.min.x, boundingBox.min.y, boundingBox.min.z);
        this.points[1] = new Point3f(boundingBox.min.x, boundingBox.min.y, boundingBox.max.z);
        this.points[2] = new Point3f(boundingBox.min.x, boundingBox.max.y, boundingBox.min.z);
        this.points[3] = new Point3f(boundingBox.min.x, boundingBox.max.y, boundingBox.max.z);
        this.points[4] = new Point3f(boundingBox.max.x, boundingBox.min.y, boundingBox.min.z);
        this.points[5] = new Point3f(boundingBox.max.x, boundingBox.min.y, boundingBox.max.z);
        this.points[6] = new Point3f(boundingBox.max.x, boundingBox.max.y, boundingBox.min.z);
        this.points[7] = new Point3f(boundingBox.max.x, boundingBox.max.y, boundingBox.max.z);

        faces = new int[6][];

        faces[0] = new int[]{0, 1, 4, 5}; //front face
        faces[1] = new int[]{2, 3, 6, 7}; //back face
        faces[2] = new int[]{4, 5, 6, 7}; //right face
        faces[3] = new int[]{0, 1, 2, 3}; //left face
        faces[4] = new int[]{1, 3, 5, 7}; //top face
        faces[5] = new int[]{0, 2, 4, 6}; //bottom face
    }

    //check if an element is inside the given range
    private boolean intoRange(float rangeElement1, float rangeElement2, float value) {

        if (rangeElement1 > rangeElement2) {
            return (value >= rangeElement2 && value <= rangeElement1);
        } else {
            return (value <= rangeElement2 && value >= rangeElement1);
        }
    }

    public List<Point3f> getIntersectionWithPlane(Plane plane) {

        //test plane intersection with each face of AABB
        List<Point3f> intersections = new ArrayList<>();

        for (int[] face : faces) {

            for (int i = 0; i < 4; i++) {

                int indice = i;

                Point3f point1 = points[face[indice]];

                if (indice + 1 == 4) {
                    indice = 0;
                }

                Point3f point2 = points[face[indice + 1]];

                if (point1.x != point2.x) { // x est la variable

                    float y = point1.y;
                    float z = point1.z;

                    float x = plane.getXFromYZ(y, z);
                    if (intoRange(point1.x, point2.x, x)) { //intersection
                        intersections.add(new Point3f(x, y, z));
                    }

                } else if (point1.y != point2.y) { // y est la variable

                    float x = point1.x;
                    float z = point1.z;

                    float y = plane.getYFromXZ(x, z);
                    if (intoRange(point1.y, point2.y, y)) { //intersection
                        intersections.add(new Point3f(x, y, z));
                    }

                } else if (point1.z != point2.z) {

                    float x = point1.x;
                    float y = point1.y;

                    float z = plane.getZFromXY(x, y);
                    if (intoRange(point1.z, point2.z, z)) { //intersection
                        intersections.add(new Point3f(x, y, z));
                    }
                }
            }
        }

        return intersections;
    }

    public Point3f getNearestPoint(Point3f point) {

        float minDistance = 999999999;

        int indice = -1;

        for (int i = 0; i < points.length; i++) {

            Point3f corner = points[i];

            float currentDistance = corner.distance(point);

            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                indice = i;
            }
        }

        return points[indice];
    }
}
