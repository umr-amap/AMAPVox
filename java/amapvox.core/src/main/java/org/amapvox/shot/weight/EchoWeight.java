package org.amapvox.shot.weight;

import java.io.IOException;
import org.amapvox.shot.Echo;
import org.amapvox.shot.Shot;
import org.amapvox.voxelisation.VoxelizationCfg;

/**
 * Abstract class for defining echo weight functions.
 * 
 * @author Philippe Verley
 */
public abstract class EchoWeight {
    
    private boolean enabled;
    
    public EchoWeight(boolean enabled) {
        this.enabled = enabled;
    }
    
    abstract public void init(VoxelizationCfg cfg) throws IOException;

    abstract public void setWeight(Shot shot);

    abstract public double getWeight(Echo echo);
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
