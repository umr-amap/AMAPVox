/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation.txt;

import org.amapvox.voxelisation.AbstractVoxelization;
import org.amapvox.voxelisation.VoxelizationTask;
import org.amapvox.voxelisation.VoxelizationCfg;

/**
 *
 * @author pverley
 */
public class ShotVoxelization extends AbstractVoxelization {

    public ShotVoxelization(VoxelizationTask task, VoxelizationCfg cfg, int iscan) {
        super(task, cfg, iscan);
    }

    @Override
    public String getName() {
        return "Voxelisation (SHT)";
    }

    @Override
    public Object call() throws Exception {

        TxtShotReader alsShotReader = new TxtShotReader(getLidarScan().getFile());
        voxelization.voxelization(alsShotReader, alsShotReader.getNShot());
        return null;
    }

}
