package org.amapvox.shot.weight;

import java.io.IOException;
import java.util.Arrays;
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
    private boolean normalized;

    public RelativeEchoWeight(boolean enabled) {
        super(enabled);
    }

    @Override
    public void init(VoxelizationCfg cfg) throws IOException {

        attribute = cfg.getRelativeEchoWeightVariable();
        normalized = cfg.isEchoWeightNormalized();
    }

    @Override
    public void setWeight(Shot shot) {

        // echo weights from attribute
        weight = new double[shot.echoes.length];
        for (int k = 0; k < weight.length; k++) {
            weight[k] = shot.echoes[k].getFloat(attribute);
        }
        
        if (normalized) {
            double sumValues = Arrays.stream(weight).sum();
            for (int k = 0; k < weight.length; k++) {
                weight[k] /= sumValues;
            }
        }
    }

    @Override
    public double getWeight(Echo echo) {
        return weight[echo.getRank()];
    }

}
