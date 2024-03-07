/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation.output;

import org.amapvox.commons.Voxel;
import org.amapvox.commons.AVoxTask;
import org.amapvox.voxelfile.VoxelFileHeader;
import org.amapvox.voxelisation.VoxelSpace;
import org.amapvox.voxelisation.VoxelizationCfg;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author pverley
 */
public class TxtVoxelFileOutput extends AbstractOutput {

    private final boolean skipEmptyVoxel;

    public TxtVoxelFileOutput(AVoxTask task, VoxelizationCfg cfg, VoxelSpace vxsp, boolean enabled) {
        super(task, cfg, vxsp, enabled);
        this.skipEmptyVoxel = cfg.skipEmptyVoxel();
    }

    @Override
    public File[] write() throws IOException {

        File outputFile = cfg.getOutputFile();
        String fileName = outputFile.getName();
        LOGGER.info("Writing file " + outputFile.getAbsolutePath());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            // header
            writer.write(new VoxelFileHeader(cfg).toString() + "\n");
            
            // variables
            String[] variables = Arrays.asList(OutputVariable.values()).stream()
                    .filter(v -> cfg.isOutputVariableEnabled(v))
                    .map(v -> v.getVariableName())
                    .toArray(String[]::new);
            
            // voxels
            int ivox = 0;
            int nvox = cfg.getDimension().x * cfg.getDimension().y * cfg.getDimension().z;
            for (int i = 0; i < cfg.getDimension().x; i++) {
                for (int j = 0; j < cfg.getDimension().y; j++) {
                    for (int k = 0; k < cfg.getDimension().z; k++) {
                        // task cancelled
                        if (task.isCancelled()) {
                            return null;
                        }
                        // current voxel
                        progress("Writing file " + fileName, ivox++, nvox);
                        Voxel voxel = vxsp.getVoxel(i, j, k);

                        // skip empty voxel
                        if (skipEmptyVoxel && voxel.npulse <= 0) {
                            continue;
                        }

                        // write line
                        writer.write(voxel.variablesToString(variables, cfg.getDecimalFormat()) + "\n");
                    }
                }
            }
        }
        return new File[]{outputFile};
    }

}
