package org.amapvox.shot.weight;

import java.io.IOException;
import org.amapvox.shot.Echo;
import org.amapvox.shot.Shot;
import org.amapvox.voxelisation.VoxelizationCfg;

/**
 * Use relative intensity per pulse (or any other attribute relevant for the
 * simulation) to weight echos.
 *
 * weight(echo) = intensity(echo) / sum(intensity(pulse))
 *
 * @author pverley
 */
public class RelativeEchoWeight extends EchoWeight {

    private String attribute;
    private double[] weight;

    public RelativeEchoWeight(boolean enabled) {
        super(enabled);
    }

    @Override
    public void init(VoxelizationCfg cfg) throws IOException {

        attribute = cfg.getRelativeEchoWeightVariable();
    }

    @Override
    public void setWeight(Shot shot) {

        // echo weights from relative intensity
        float[] values = new float[shot.echoes.length];
        double sumValues = 0.d;
        for (int k = 0; k < values.length; k++) {
            values[k] = shot.echoes[k].getFloat(attribute);
            sumValues += values[k];
        }
        weight = new double[values.length];
        for (int k = 0; k < values.length; k++) {
            weight[k] = values[k] / sumValues;
        }
    }

    @Override
    public double getWeight(Echo echo) {
        return weight[echo.getRank()];
    }

}
