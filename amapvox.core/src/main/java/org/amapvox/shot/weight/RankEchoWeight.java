package org.amapvox.shot.weight;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import org.amapvox.commons.Matrix;
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
    
    /**
     * Default echo weights, evenly distributed.
     */
    public final static Matrix DEFAULT_WEIGHT = new Matrix(
            new double[][]{
                {1.d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
                {0.5d, 0.5d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
                {1 / 3.d, 1 / 3.d, 1 / 3.d, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
                {0.25d, 0.25d, 0.25d, 0.25d, Double.NaN, Double.NaN, Double.NaN},
                {0.2d, 0.2d, 0.2d, 0.2d, 0.2d, Double.NaN, Double.NaN},
                {1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d, Double.NaN},
                {1 / 7.d, 1 / 7.d, 1 / 7.d, 1 / 7.d, 1 / 7.d, 1 / 7.d, 1 / 7.d}}
    );

    private double[][] weightTable;

    public RankEchoWeight(boolean enabled) {
        super(enabled);
    }

    @Override
    public void init(VoxelizationCfg cfg) throws IOException {

        // echo weighting table with security check to ensure values <= 1
        weightTable = cfg.getRankEchoWeightMatrix().getData();
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
