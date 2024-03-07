/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation.gridded;

import java.io.File;
import org.amapvox.voxelisation.VoxelizationTask;
import org.amapvox.voxelisation.VoxelizationCfg;
import org.amapvox.lidar.gridded.GriddedPointScan;
import org.amapvox.lidar.leica.ptg.PTGScan;

/**
 *
 * @author calcul
 */
public class PTGVoxelization extends GriddedScanVoxelization {

    public PTGVoxelization(VoxelizationTask task, VoxelizationCfg cfg, int iscan) {
        super(task, cfg, iscan);
    }

    @Override
    public String getName() {
        return "Voxelisation (PTG)";
    }

    @Override
    GriddedPointScan newGriddedScan(File file) {
        return new PTGScan(file);
    }

}
