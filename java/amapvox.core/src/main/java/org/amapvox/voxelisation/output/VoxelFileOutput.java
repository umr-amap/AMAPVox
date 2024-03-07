/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation.output;

import org.amapvox.commons.AVoxTask;
import org.amapvox.voxelisation.VoxelSpace;
import org.amapvox.voxelisation.VoxelizationCfg;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author pverley
 */
public class VoxelFileOutput extends AbstractOutput {

    public VoxelFileOutput(AVoxTask task, VoxelizationCfg cfg, VoxelSpace vxsp, boolean enabled) {
        super(task, cfg, vxsp, enabled);
    }

    @Override
    public File[] write() throws IOException {

        switch (cfg.getVoxelsFormat()) {
            case VOXEL:
                return new TxtVoxelFileOutput(task, cfg, vxsp, isEnabled()).write();
            case NETCDF:
                return new NetCDFVoxelFileOutput(task, cfg, vxsp, isEnabled()).write();
        }
        return null;
    }

}
