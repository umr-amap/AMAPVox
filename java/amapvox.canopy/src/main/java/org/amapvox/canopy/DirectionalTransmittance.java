/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.canopy;

import org.amapvox.voxelfile.VoxelFileHeader;
import org.amapvox.voxelfile.VoxelFileReader;
import org.amapvox.voxelfile.VoxelFileVoxel;
import org.amapvox.voxelisation.output.OutputVariable;
import org.amapvox.commons.raytracing.voxel.VoxelManager.Topology;
import org.amapvox.commons.raytracing.voxel.VoxelSpace;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;
import org.apache.commons.math3.util.FastMath;
import org.apache.log4j.Logger;

/**
 * Get ray transmittance from a position to a direction into a sampled voxel
 * space.
 *
 * @author main :Jean Dauzat ; co: Julien Heurtebize
 */
public class DirectionalTransmittance {

    private final static Logger LOGGER = Logger.getLogger(DirectionalTransmittance.class);

    private final Point3d min;
    private final Point3d max;

    private final Point3d voxelSize;
    private final Point3i dimension;

    private final VoxelSpace voxelSpace;

    private final DTVoxel voxels[][][];

    private final GTheta direcTrans;

    private boolean toric = false;
    private final static double EPSILON = 0.001;

    private class DTVoxel {

        double pad;
        float groundDistance;
    }

    /**
     *
     * @param inputFile voxel file
     * @param lad
     * @param ladParams
     * @throws Exception
     */
    public DirectionalTransmittance(File inputFile, String padVariable, LeafAngleDistribution.Type lad, double... ladParams) throws Exception {

        VoxelFileReader reader = new VoxelFileReader(inputFile);
        VoxelFileHeader header = reader.getHeader();

        min = header.getMinCorner();
        max = header.getMaxCorner();
        dimension = header.getDimension();

        voxelSize = new Point3d();
        voxelSize.x = (max.x - min.x) / (double) dimension.x;
        voxelSize.y = (max.y - min.y) / (double) dimension.y;
        voxelSize.z = (max.z - min.z) / (double) dimension.z;

        LOGGER.debug(header.toString() + "\n");

        direcTrans = new GTheta(new LeafAngleDistribution(lad, ladParams));
        direcTrans.buildTable(180);

        voxelSpace = new VoxelSpace(min, max, dimension, Topology.NON_TORIC_FINITE_BOX_TOPOLOGY);

        //createVoxelTable();
        voxels = new DTVoxel[dimension.x][dimension.y][dimension.z];

        Iterator<VoxelFileVoxel> iterator = reader.iterator();

        OutputVariable pad = OutputVariable.find(padVariable);
        int padColumn = reader.findColumn(pad);
        if (padColumn < 0) {
            throw new IOException("[directional transmittance] Output variable \"plant area density\" (" + padVariable + ") is missing");
        }
        int groundDistanceColumn = reader.findColumn(OutputVariable.GROUND_DISTANCE);
        if (groundDistanceColumn < 0) {
            throw new IOException("[directional transmittance] Output variable \"ground distance\" is missing");
        }

        while (iterator.hasNext()) {
            VoxelFileVoxel voxel = iterator.next();
            if (voxel != null) {
                voxels[voxel.i][voxel.j][voxel.k] = new DTVoxel();
                voxels[voxel.i][voxel.j][voxel.k].pad = Float.valueOf(voxel.variables[padColumn]);
                voxels[voxel.i][voxel.j][voxel.k].groundDistance = Float.valueOf(voxel.variables[groundDistanceColumn]);
            }
        }
    }

    private List<Double> distToVoxelWallsV2(Point3d origin, Vector3d direction) {

        // point where the ray exits from the top of the bounding box
        double distToTop = (max.z - origin.z) / direction.z;

        List<Double> distances = new ArrayList<>();

        distances.add(distToTop);

        // voxel walls in X
        if (direction.x != 0) {
            double deltaX = min.x - origin.x;
            deltaX -= voxelSize.x * ((int) (deltaX / voxelSize.x));

            if (direction.x > 0) {
                deltaX = voxelSize.x - deltaX;
            }

            double dist = Math.abs(deltaX / direction.x);
            distances.add(dist);

            double dX = Math.abs(voxelSize.x / direction.x);
            while (dist < distToTop) {
                //current distance
                dist += dX;
                distances.add(dist);
            }
        }

        // voyel walls in Y
        if (direction.y != 0) {
            double deltaY = min.y - origin.y;
            deltaY -= voxelSize.y * ((int) (deltaY / voxelSize.y));

            if (direction.y > 0) {
                deltaY = voxelSize.y - deltaY;
            }

            double dist = Math.abs(deltaY / direction.y);
            distances.add(dist);

            double dY = Math.abs(voxelSize.y / direction.y);
            while (dist < distToTop) {
                //current distance
                dist += dY;
                distances.add(dist);
            }
        }

        // vozel walls in Z
        if (direction.z != 0) {
            double deltaZ = min.z - origin.z;
            deltaZ -= voxelSize.z * ((int) (deltaZ / voxelSize.z));

            if (direction.z > 0) {
                deltaZ = voxelSize.z - deltaZ;
            }

            double dist = Math.abs(deltaZ / direction.z);
            distances.add(dist);

            double dZ = Math.abs(voxelSize.z / direction.z);
            while (dist < distToTop) {
                //current distance
                dist += dZ;
                distances.add(dist);
            }
        }

        Collections.sort(distances);

        return distances;
    }

    private List<Double> distToVoxelWalls(Point3d origin, Vector3d direction) {

        // point where the ray exits from the top of the bounding box
        Point3d exit = new Point3d(direction);
        double dist = (max.z - origin.z) / direction.z;
        exit.scale(dist);
        exit.add(origin);

        Point3i originVoxel = new Point3i((int) ((origin.x - min.x) / voxelSize.x), (int) ((origin.y - min.y) / voxelSize.y), (int) ((origin.z - min.z) / voxelSize.z));
        Point3i exitVoxel = new Point3i((int) ((exit.x - min.x) / voxelSize.x), (int) ((exit.y - min.y) / voxelSize.y), (int) ((exit.z - min.z) / voxelSize.z));

        List<Double> distances = new ArrayList<>();

        Vector3d oe = new Vector3d(exit);
        oe.sub(origin);
        distances.add(0.0);
        distances.add(oe.length());

        // voxel walls in X
        int minX = Math.min(originVoxel.x, exitVoxel.x);
        int maxX = Math.max(originVoxel.x, exitVoxel.x);
        for (int m = minX; m < maxX; m++) {
            double dx = (m + 1) * voxelSize.x;
            dx += min.x - origin.x;
            dx /= direction.x;
            distances.add(dx);
        }

        // voxel walls in Y
        int minY = Math.min(originVoxel.y, exitVoxel.y);
        int maxY = Math.max(originVoxel.y, exitVoxel.y);
        for (int m = minY; m < maxY; m++) {
            double dy = (m + 1) * voxelSize.y;
            dy += min.y - origin.y;
            dy /= direction.y;
            distances.add(dy);
        }

        // voxel walls in Z
        int minZ = Math.min(originVoxel.z, exitVoxel.z);
        int maxZ = Math.max(originVoxel.z, exitVoxel.z);
        for (int m = minZ; m < maxZ; m++) {
            double dz = (m + 1) * voxelSize.z;
            dz += min.z - origin.z;
            dz /= direction.z;
            distances.add(dz);
        }

        Collections.sort(distances);

        return distances;
    }

    public double directionalTransmittance(Point3d origin, Vector3d direction) {

        List<Double> distances = distToVoxelWallsV2(origin, direction);

        //we can optimize this by storing the angle value to avoid repeating this for each position
        double directionAngle = FastMath.toDegrees(FastMath.acos(direction.z));

        double dMoy;
        Point3d pMoy;

        double d1 = 0;
        double transmitted = 1;
        for (Double d2 : distances) {
            double pathLength = d2 - d1;
            dMoy = (d1 + d2) / 2.0;
            pMoy = new Point3d(direction);
            pMoy.scale(dMoy);
            pMoy.add(origin);
            pMoy.sub(min);

            int i = (int) Math.floor(pMoy.x / voxelSize.x);
            int j = (int) Math.floor(pMoy.y / voxelSize.y);
            int k = (int) Math.floor(pMoy.z / voxelSize.z);

            if (i < 0 || j < 0 || k < 0 || i >= dimension.x || j >= dimension.y || k >= dimension.z) {

                if (toric) {

                    while (i < 0) {
                        i += dimension.x;
                    }
                    while (j < 0) {
                        j += dimension.y;
                    }
                    while (i >= dimension.x) {
                        i -= dimension.x;
                    }
                    while (j >= dimension.y) {
                        j -= dimension.y;
                    }

                    if (k < 0 || k >= dimension.z) {
                        break;
                    }

                } else {
                    break;
                }
            }

            // crossing unsampled voxel, transmittance cannot be estimated
            if (null == voxels[i][j][k] || Double.isNaN(voxels[i][j][k].pad)) {
                return Double.NaN;
            }

            // Test if current voxel is below the ground level
            if (voxels[i][j][k].groundDistance <= 0.f) {
                transmitted = 0;
            } else {

                double coefficientGTheta = direcTrans.getGThetaFromAngle(directionAngle, true);

                //input transmittance
                //double transmittedBefore = transmitted;
                transmitted *= Math.exp(-coefficientGTheta * voxels[i][j][k].pad * pathLength);

                //output transmittance
                //double transmittedAfter = transmitted;
                //intercepted transmittance
                //double interceptedTrans = transmittedAfter - transmittedBefore;
                //transmitted *= Math.exp(-0.5 * voxels[i][j][k].padBV * pathLength)/*(default coeff)*/;
            }

            if (transmitted <= EPSILON && toric) {
                break;
            }

            d1 = d2;
        }

        return transmitted;
    }

    public Point3d getVoxelSize() {
        return voxelSize;
    }

    public VoxelSpace getVoxelSpace() {
        return voxelSpace;
    }

    public boolean isToric() {
        return toric;
    }

    public void setToric(boolean toric) {
        this.toric = toric;
    }
}
