/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation.gridded;

import org.amapvox.voxelisation.AbstractVoxelization;
import org.amapvox.commons.util.IteratorWithException;
import org.amapvox.shot.Shot;
import org.amapvox.voxelisation.VoxelizationTask;
import org.amapvox.voxelisation.VoxelizationCfg;
import org.amapvox.lidar.gridded.GriddedPointScan;
import org.amapvox.lidar.gridded.LPointShotExtractor;
import org.amapvox.lidar.gridded.LShot;
import java.util.Iterator;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.apache.log4j.Logger;

/**
 *
 * @author pverley
 */
public abstract class GriddedScanVoxelization extends AbstractVoxelization {

    private final Logger LOGGER = Logger.getLogger(GriddedScanVoxelization.class);

    public GriddedScanVoxelization(VoxelizationTask task, VoxelizationCfg cfg, int iscan) {
        super(task, cfg, iscan);
    }
    
    abstract GriddedPointScan newGriddedScan();

    @Override
    public Object call() throws Exception {

        LOGGER.info(logHeader() + " Point cloud extraction started");

        GriddedPointScan gpScan = newGriddedScan();
        gpScan.openScanFile(getLidarScan().getFile());

        Iterator<LShot> iterator = new LPointShotExtractor(gpScan).iterator();

        IteratorWithException<Shot> it = new IteratorWithException<>() {

            private int index = 0;

            @Override
            public boolean hasNext() throws Exception {
                return iterator.hasNext();
            }

            @Override
            public Shot next() throws Exception {
                LShot shot = iterator.next();
                if (shot != null) {
                    Point3d location = new Point3d(shot.origin);
                    transformation.transform(location);
                    Vector3d direction = shot.direction;
                    transformation.transform(direction);
                    return new Shot(index++, location, direction, shot.ranges);
                } else {
                    return null;
                }
            }
        };

        voxelization.voxelization(it, gpScan.getEndRowIndex() * gpScan.getEndColumnIndex());

        fireSucceeded();

        return null;

    }

    
}
