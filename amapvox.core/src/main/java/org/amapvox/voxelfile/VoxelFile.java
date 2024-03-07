/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelfile;

import org.amapvox.commons.Voxel;
import java.util.List;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

/**
 *
 * @author calcul
 */
public class VoxelFile {

    private final VoxelFileHeader header;
    public List voxels;

    public VoxelFile(VoxelFileHeader header) {
        this.header = header;
    }

    public VoxelFileHeader getHeader() {
        return header;
    }

    private int ijkToIndex(int i, int j, int k) {

        return (i * header.getDimension().y * header.getDimension().z) + (j * header.getDimension().z) + k;
    }

    public Point3i coordinatesToIndex(Point3d coordinates) {

        // shift to scene Min
        Point3d pt = new Point3d(coordinates);
        pt.sub(header.getMinCorner());

        if ((pt.z < 0) || (pt.z >= (header.getMaxCorner().z - header.getMinCorner().z))) {

            return null;
        }

        pt.x /= header.getVoxelSize().x;
        pt.y /= header.getVoxelSize().y;
        pt.z /= header.getVoxelSize().z;

        Point3i indices = new Point3i();

        // voxel indexes
        indices.x = (int) Math.floor((double) (pt.x % header.getDimension().x));
        if (indices.x < 0) {
            indices.x += header.getDimension().x;
        }
        indices.y = (int) Math.floor((double) (pt.y % header.getDimension().y));
        if (indices.y < 0) {
            indices.y += header.getDimension().y;
        }
        indices.z = (int) Math.min(pt.z, header.getDimension().z - 1);

        return indices;
    }

    public Object getVoxel(int i, int j, int k) {

        if (i > header.getDimension().x - 1 || j > header.getDimension().y - 1 || k > header.getDimension().z - 1) {
            return null;
        }

        int index = ijkToIndex(i, j, k);

        if (index > voxels.size() - 1) {
            return null;
        }

        return voxels.get(index);
    }

    public Voxel[][] getLayerX(int xIndex) {

        if (xIndex < 0 || xIndex >= header.getDimension().x) {
            return null;
        }

        Voxel[][] result = new Voxel[header.getDimension().y][header.getDimension().z];

        for (int j = 0; j < header.getDimension().y; j++) {
            for (int k = 0; k < header.getDimension().z; k++) {
                result[j][k] = (Voxel) getVoxel(xIndex, j, k);
            }
        }

        return result;
    }

    public Voxel[][] getLayerY(int yIndex) {

        if (yIndex < 0 || yIndex >= header.getDimension().y) {
            return null;
        }

        Voxel[][] result = new Voxel[header.getDimension().x][header.getDimension().z];

        for (int i = 0; i < header.getDimension().x; i++) {
            for (int k = 0; k < header.getDimension().z; k++) {
                result[i][k] = (Voxel) getVoxel(i, yIndex, k);
            }
        }

        return result;
    }

    public Voxel[][] getLayerZ(int zIndex) {

        if (zIndex < 0 || zIndex >= header.getDimension().z) {
            return null;
        }

        Voxel[][] result = new Voxel[header.getDimension().x][header.getDimension().y];

        for (int i = 0; i < header.getDimension().x; i++) {
            for (int j = 0; j < header.getDimension().y; j++) {
                result[i][j] = (Voxel) getVoxel(i, j, zIndex);
            }
        }

        return result;
    }

}
