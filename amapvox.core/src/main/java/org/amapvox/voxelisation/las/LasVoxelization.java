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
        PointsToShot alsShotBuilder = new PointsToShot(
                cfg.getTrajectoryFile(), cfg.getScannerPosition(),
                getLidarScan().getFile(), vop,
                cfg.isEchoConsistencyCheckEnabled(), cfg.isEchoConsistencyWarningEnabled(),
                cfg.isCollinearityCheckEnabled(), cfg.isCollinearityWarningEnabled(),
                cfg.getCollinearityMaxDeviation());

        // add listener to monitor progress of the ALS shot builder
        alsShotBuilder.addProcessingListener(parent);
        // initializes the ALS shot builder
        alsShotBuilder.init();

        // voxelisation
        voxelization.voxelization(alsShotBuilder.iterator(), alsShotBuilder.getNShot());
        return null;
    }
}
