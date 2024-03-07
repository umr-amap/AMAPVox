/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation.las;

import org.amapvox.shot.Shot;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author Julien Heurtebize
 */
public class LasShot extends Shot {

    public float intensities[];
    public int classifications[];
    public double time;

    public LasShot(int index, Point3d origin, Vector3d direction, double[] ranges) {
        super(index, origin, direction, ranges);
    }

    public LasShot(int index, Point3d origin, Vector3d direction, double[] ranges, int[] classifications, float[] intensities) {
        super(index, origin, direction, ranges);
        this.classifications = classifications;
        this.intensities = intensities;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(super.toString());
        if (getEchoesNumber() > 0) {
            str.append("\n  Echo classes ");
            for (int k = 0; k < getEchoesNumber(); k++) {
                str.append(classifications[k]).append(" ");
            }
        }
        return str.toString();
    }
}
