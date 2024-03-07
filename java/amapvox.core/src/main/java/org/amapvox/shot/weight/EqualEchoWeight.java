package org.amapvox.shot.weight;

import java.io.IOException;
import org.amapvox.shot.Echo;
import org.amapvox.shot.Shot;
import org.amapvox.voxelisation.VoxelizationCfg;

/**
 * The weighting function assumes that the size of a target is inversely
 * proportional to the number of returns per emitted pulse.
 *
 * E.g. in case three returns are recorded, each is supposed to account for
 * one-third of the pulse footprint size at the target distance.
 *
 * https://forge.ird.fr/amap/amapvox/-/issues/5
 *
 * @author Philippe Verley
 */
public class EqualEchoWeight extends EchoWeight {

    private double weight;

    public EqualEchoWeight(boolean enabled) {
        super(enabled);
    }

    @Override
    public void setWeight(Shot shot) {
        weight = 1.d / shot.getEchoesNumber();
    }

    @Override
    public double getWeight(Echo echo) {
        return weight;
    }

    @Override
    public void init(VoxelizationCfg cfg) throws IOException {
        // default weight set to 1
        weight = 1.d;
    }

}
