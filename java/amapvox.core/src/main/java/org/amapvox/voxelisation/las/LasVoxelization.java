/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation.las;

import org.amapvox.voxelisation.AbstractVoxelization;
import org.amapvox.commons.math.util.MatrixUtility;
import org.amapvox.voxelisation.VoxelizationTask;
import org.amapvox.voxelisation.VoxelizationCfg;
import javax.vecmath.Matrix4d;

/**
 *
 * @author pverley
 */
public class LasVoxelization extends AbstractVoxelization {

    public LasVoxelization(VoxelizationTask task, VoxelizationCfg cfg, int iscan) {
        super(task, cfg, iscan);
    }

    @Override
    public String getName() {
        return "Voxelisation (LAS/LAZ)";
    }

    @Override
    public Object call() throws Exception {

        // vop matrix
        Matrix4d vop = (null == cfg.getVopMatrix())
                ? MatrixUtility.identity4d()
                : new Matrix4d(cfg.getVopMatrix());

        // LAS shot builder
        double lasTimeMin = cfg.isTimeRangeEnabled() ? cfg.getLasTimeMin() : 0.d;
        double lasTimeMax = cfg.isTimeRangeEnabled() ? cfg.getLasTimeMax() : Double.MAX_VALUE;
        LasShotExtractor lasShotExtractor = new LasShotExtractor(
                cfg.getTrajectoryFile(), cfg.getScannerPosition(),
                getLidarScan().getFile(), vop,
                cfg.isEchoConsistencyCheckEnabled(), cfg.isEchoConsistencyWarningEnabled(),
                cfg.isCollinearityCheckEnabled(), cfg.isCollinearityWarningEnabled(),
                cfg.getCollinearityMaxDeviation(),
                lasTimeMin, lasTimeMax);

        // add listener to monitor progress of the ALS shot builder
        lasShotExtractor.addProcessingListener(parent);
        // initializes the ALS shot builder
        lasShotExtractor.init();

        // voxelisation
        voxelization.voxelization(lasShotExtractor.iterator(), lasShotExtractor.getNShot());
        return null;
    }
}
