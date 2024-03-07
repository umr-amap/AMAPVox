package org.amapvox.shot.weight;

import java.io.IOException;
import org.amapvox.shot.Echo;
import org.amapvox.shot.Shot;
import org.amapvox.voxelisation.VoxelizationCfg;

/**
 * Return strongest echo only and discard others. Strongest as maximal intensity
 * return or maximal reflectance return. Nonetheless any available variable can
 * be selected.
 *
 * @author Philippe Verley
 */
public class StrongestEchoWeight extends EchoWeight {

    private String attribute;
    private int[] weight;

    public StrongestEchoWeight(boolean enabled) {
        super(enabled);
    }

    @Override
    public void init(VoxelizationCfg cfg) throws IOException {

        attribute = cfg.getStrongestEchoWeightVariable();
    }

    @Override
    public void setWeight(Shot shot) {

        // get echo attributes (usually intensity of reflectance
        float[] values = new float[shot.echoes.length];
        for (int k = 0; k < values.length; k++) {
            values[k] = shot.echoes[k].getFloat(attribute);
        }

        // every weight set to zero
        weight = new int[values.length];

        // set strongest echo weight to one
        weight[indexMax(values)] = 1;
    }

    private int indexMax(float[] values) {
        int index = -1;
        float max = -1.f * Float.MAX_VALUE;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
                index = i;
            }
        }
        return index;
    }

    @Override
    public double getWeight(Echo echo) {
        return weight[echo.getRank()];
    }

}
