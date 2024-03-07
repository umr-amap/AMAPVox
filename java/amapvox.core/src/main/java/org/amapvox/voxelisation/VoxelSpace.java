/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation;

import org.amapvox.commons.raster.asc.Raster;
import org.amapvox.commons.Voxel;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

/**
 *
 * @author pverley
 */
public class VoxelSpace {

    private final Point3i dim;
    private final Point3d min, voxelSize;
    private final Raster dtm;
    private final Voxel[] voxels;

    public VoxelSpace(Point3i dim, Point3d min, Point3d voxelSize, Raster dtm) {

        this.dim = dim;
        this.min = min;
        this.voxelSize = voxelSize;
        this.dtm = dtm;
        // initialize voxel space
        voxels = new Voxel[dim.x * dim.y * dim.z];
    }

    private Voxel initVoxel(int i, int j, int k) {

        Voxel voxel = new Voxel(i, j, k);
        voxel.groundDistance = (float) computeGroundDistance(i, j, k);
        return voxel;
    }

    public Voxel getVoxel(int i, int j, int k, boolean create) {

        int index = i + (j + k * dim.y) * dim.x;
        synchronized (voxels) {
            if (null == voxels[index] && create) {
                voxels[index] = initVoxel(i, j, k);
            }
        }
        return voxels[index];

    }

    public Voxel getVoxel(int i, int j, int k) {
        return getVoxel(i, j, k, true);
    }

    private double computeGroundDistance(int i, int j, int k) {

        Point3d position = getPosition(new Point3i(i, j, k));

        if (null != dtm) {
            float dtmHeightXY = dtm.getSimpleHeight((float) position.x, (float) position.y);
            return (Float.isNaN(dtmHeightXY)
                    ? position.z
                    : position.z - dtmHeightXY);
        } else {
            return position.z - min.z;
        }
    }

    /**
     * Get position of the center of a voxel
     *
     * @param vcoord, voxel coordinate
     * @return
     */
    public Point3d getPosition(Point3i vcoord) {

        double posX = min.x + (voxelSize.x / 2.0d) + (vcoord.x * voxelSize.x);
        double posY = min.y + (voxelSize.y / 2.0d) + (vcoord.y * voxelSize.y);
        double posZ = min.z + (voxelSize.z / 2.0d) + (vcoord.z * voxelSize.z);

        return new Point3d(posX, posY, posZ);
    }

}
