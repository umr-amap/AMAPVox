/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation;

import org.amapvox.commons.util.IteratorWithException;
import org.amapvox.shot.Shot;
import org.amapvox.lidar.faro.XYBIterator;
import org.amapvox.lidar.faro.XYBScan;
import org.amapvox.lidar.gridded.LDoublePoint;
import org.amapvox.lidar.gridded.LPoint;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author pverley
 */
public class XYBVoxelization extends AbstractVoxelization {

    public XYBVoxelization(VoxelizationTask task, VoxelizationCfg cfg, int iscan) {
        super(task, cfg, iscan);
    }

    @Override
    public String getName() {
        return "Voxelisation (XYB)";
    }

    @Override
    public Object call() throws Exception {

        // XYB scan
        XYBScan xybScan = new XYBScan();
        xybScan.openScanFile(getLidarScan().getFile());

        // XYB iterator
        XYBIterator xybIterator = (XYBIterator) xybScan.iterator();
        Point3d origin = xybIterator.getHeader().getScannerPosition();
        Point3d t_origin = new Point3d(origin);
        transformation.transform(t_origin);

        // Shot iterator
        IteratorWithException<Shot> shotIterator = new IteratorWithException<>() {

            private int index = 0;

            @Override
            public boolean hasNext() {
                return xybIterator.hasNext();
            }

            @Override
            public Shot next() throws Exception {

                LPoint point = xybIterator.next();
                Point3d hit = new Point3d(
                        ((LDoublePoint) point).x,
                        ((LDoublePoint) point).y,
                        ((LDoublePoint) point).z);
                // distance origin/hit
                double[] ranges = new double[]{origin.distance(hit)};
                // shot direction
                Vector3d direction = new Vector3d(hit);
                direction.sub(origin);
                direction.normalize();
                transformation.transform(direction);
                return new Shot(index++, t_origin, direction, ranges);
            }
        };

        int nshot = xybIterator.getHeader().getNZenith() * xybIterator.getHeader().getNAzimuth();
        voxelization.voxelization(shotIterator, nshot);

        fireSucceeded();

        return null;
    }
}
