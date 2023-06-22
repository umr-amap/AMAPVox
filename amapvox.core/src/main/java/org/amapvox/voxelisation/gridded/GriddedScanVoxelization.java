/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation.gridded;

import java.io.File;
import org.amapvox.voxelisation.AbstractVoxelization;
import org.amapvox.commons.util.IteratorWithException;
import org.amapvox.shot.Shot;
import org.amapvox.voxelisation.VoxelizationTask;
import org.amapvox.voxelisation.VoxelizationCfg;
import org.amapvox.lidar.gridded.GriddedPointScan;
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

    abstract GriddedPointScan newGriddedScan(File file);

    @Override
    public Object call() throws Exception {

        LOGGER.info(logHeader() + " Point cloud extraction started");

        GriddedPointScan gpScan = newGriddedScan(getLidarScan().getFile());
        gpScan.open();
        
        GriddedScanShotExtractor shotExtractor = new GriddedScanShotExtractor(gpScan, transformation);
        shotExtractor.init();

        IteratorWithException<Shot> it = shotExtractor.iterator();

        voxelization.voxelization(it, gpScan.getHeader().getNZenith() * gpScan.getHeader().getNAzimuth());

        fireSucceeded();

        return null;

    }

}
