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
import org.apache.log4j.Logger;

/**
 *
 * @author pverley
 */
public abstract class AbstractOutput {
    
    protected final static Logger LOGGER = Logger.getLogger(AbstractOutput.class);
    
    protected final AVoxTask task;
    protected final VoxelizationCfg cfg;
    protected final VoxelSpace vxsp;
    private final boolean enabled;
    
    AbstractOutput(AVoxTask task, VoxelizationCfg cfg, VoxelSpace vxsp, boolean enabled) {
        this.task = task;
        this.cfg = cfg;
        this.vxsp = vxsp;
        this.enabled = enabled;
    }
    
    void progress(String progressMsg, long progress, long max) {
        task.fireProgress(progressMsg, progress, max);
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    abstract public File[] write() throws IOException;
}
