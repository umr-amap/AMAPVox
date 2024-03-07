/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.canopy.lai2xxx;

import org.amapvox.canopy.transmittance.TransmittanceCfg;
import org.amapvox.commons.AVoxTask;

/**
 *
 * @author pverley
 */
public class CanopyAnalyzerCfg extends TransmittanceCfg {

    public CanopyAnalyzerCfg() {
        super("CANOPY_ANALYZER", "Canopy Analyzer (LAI2000/2200)",
                "Simulates LAI2000/2200 sensors from a voxel file and sensor parameters.",
                new String[]{"LAI2000", "LAI2200"});
    }

    @Override
    public Class<? extends AVoxTask> getTaskClass() {
        return CanopyAnalyzerSim.class;
    }

}
