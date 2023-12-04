package org.amapvox.shot.weight;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import org.amapvox.shot.Echo;
import org.amapvox.shot.Shot;
import org.amapvox.voxelisation.VoxelizationCfg;
import org.apache.log4j.Logger;

/**
 * Using intensity in pre-processing stage to estimate the mean target size
 * given the number of returns and the rank of the return.
 *
 * User provides an array with estimated weights.
 *
 * https://forge.ird.fr/amap/amapvox/-/issues/5
 *
 * @author Philippe Verley
 */
public class RankEchoWeight extends EchoWeight {

    private final static Logger LOGGER = Logger.getLogger(RankEchoWeight.class);

    private double[][] weightTable;

    @Override
    public void init(VoxelizationCfg cfg) throws IOException {

        // echo weighting table with security check to ensure values <= 1
        weightTable = cfg.getEchoesWeightMatrix().getData();
        for (int i = 0; i < weightTable.length; i++) {
            for (int j = 0; j < weightTable[i].length; j++) {
                if (!Double.isNaN(weightTable[i][j])) {
                    BigDecimal w = BigDecimal.valueOf(weightTable[i][j]);
                    if (w.compareTo(BigDecimal.ONE) > 0) {
                        throw new IOException("[Echo weight]" + " Inconsistent echo weighting table with values greater than one. Please fix it in the configuration file.");
                    }
                }
                if (sum(weightTable[i]).compareTo(BigDecimal.ONE) > 0) {
                    throw new IOException("[Echo weight]" + " Inconsistent echo weighting table. Cumulated values at line " + (i + 1) + " greater than one. " + Arrays.toString(weightTable[i]));
                }
            }
        }

    }

    private BigDecimal sum(double[] array) {

        BigDecimal sum = BigDecimal.ZERO;
        for (double d : array) {
            if (!Double.isNaN(d)) {
                sum = sum.add(BigDecimal.valueOf(d));
            }
        }
        return sum;
    }

    @Override
    public void setWeight(Shot shot) {
       // nothing to do
    }

    @Override
    public double getWeight(Echo echo) {

        int nEcho = echo.getShot().getEchoesNumber();

        // discard shot whith more echoes than weights provided in the weighting table
        if (nEcho > weightTable.length) {

            LOGGER.warn(
                    "Shot " + echo.getShot().index
                    + " has been discarded. More echoes ("
                    + nEcho
                    + ") than weights in the echo weighting table ("
                    + weightTable.length + ").");
            return 1.d;
        }

        int rank = echo.getRank();
        return nEcho > 0 ? weightTable[nEcho - 1][rank] : 1.d;
    }

}
