/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation.gridded;

import org.amapvox.voxelisation.VoxelizationTask;
import org.amapvox.voxelisation.VoxelizationCfg;
import org.amapvox.lidar.leica.ptx.PTXLidarScan;
import org.amapvox.lidar.gridded.GriddedPointScan;

/**
 *
 * @author Philippe Verley
 */
public class PTXVoxelization extends GriddedScanVoxelization {

    public PTXVoxelization(VoxelizationTask task, VoxelizationCfg cfg, int iscan) {
        super(task, cfg, iscan);
    }

    @Override
    public String getName() {
        return "Voxelisation (PTX)";
    }

    @Override
    GriddedPointScan newGriddedScan() {
        return ((PTXLidarScan) getLidarScan()).getScan();
    }
}
