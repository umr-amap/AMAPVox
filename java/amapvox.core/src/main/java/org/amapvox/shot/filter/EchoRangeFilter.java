/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.shot.filter;

import org.amapvox.commons.util.filter.Filter;
import org.amapvox.shot.Shot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.DoubleStream;
import org.apache.log4j.Logger;

/**
 * This filter checks the integrity of the echoes distances. First it makes sure
 * there are not any blank echo (range == 0) interlaid in between valid echoes
 * and it also checks that the distances are monotonically increasing.
 *
 * @author Philippe Verley (philippe.verley@ird.fr)
 */
public class EchoRangeFilter implements Filter<Shot> {

    private final static Logger LOGGER = Logger.getLogger(EchoRangeFilter.class);
    private final static double EPSILON = 1E-7;
    private final boolean blankEchoDiscarded;
    private final boolean warningEnabled;

    public EchoRangeFilter(boolean blankEchoDiscarded, boolean warningEnabled) {
        this.blankEchoDiscarded = blankEchoDiscarded;
        this.warningEnabled = warningEnabled;
    }
    
    public boolean isWarningEnabled() {
        return warningEnabled;
    }

    @Override
    public void init() throws Exception {
        // nothing to do
    }

    /**
     * Check shot integrity by making sure there are not any blank echoes that
     * are neither leading nor trailing echoes and by checking that echo
     * distances are monotonically increasing. Valid echo distances: [2, 3] [0,
     * 0, 2, 3, 0] [2, 3, 0] [0, 2, 3]. Invalid echo ranges: [0, 3, 2] [2, 0, 3]
     * [0, 2, 0, 3, 0].
     *
     * @param shot
     * @return true if the shot shows consistent echo distances.
     * @throws Exception
     */
    @Override
    public boolean accept(Shot shot) throws Exception {

        if (null != shot.getRanges()) {

            // reject shot with blank echo
            if (blankEchoDiscarded && DoubleStream.of(shot.getRanges()).anyMatch(range -> range <= EPSILON)) {
                if (warningEnabled) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Inconsistant shot (contains blank echo).").append('\n');
                    sb.append(shot).append('\n');
                    sb.append("Shot discarded.");
                    LOGGER.warn(sb.toString());
                }
                return false;
            }

            // accept leading & trailing blank echoes that are not a problem for the voxelisation
            Double[] ranges = rangesWithoutLeadingTrailingZeros(shot);
            for (int k = 0; k < ranges.length; k++) {
                // check whether there are any blank echo
                // check that echoes are monotonically increasing
                if (ranges[k] < EPSILON || ((k < ranges.length - 1) && (ranges[k] >= ranges[k + 1]))) {
                    if (warningEnabled) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Inconsistant echo ranges (either interlaid blank echo or distances not monotonically increasing).").append('\n');
                        sb.append(shot).append('\n');
                        sb.append("Shot discarded.");
                        LOGGER.warn(sb.toString());
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private Double[] rangesWithoutLeadingTrailingZeros(Shot shot) {

        if (null != shot.getRanges()) {
            List<Double> r1 = new ArrayList<>();
            boolean leadingZeroFinished = false;
            for (int k = 0; k < shot.getRanges().length; k++) {
                if (!leadingZeroFinished && shot.getRange(k) > EPSILON) {
                    leadingZeroFinished = true;
                }
                if (leadingZeroFinished) {
                    r1.add(shot.getRange(k));
                }
            }
            boolean trailingZeroFinished = false;
            ArrayList<Double> r2 = new ArrayList<>();
            for (int k = r1.size() - 1; k > 0; k--) {
                if (!trailingZeroFinished && r1.get(k) > EPSILON) {
                    trailingZeroFinished = true;
                }
                if (trailingZeroFinished) {
                    r2.add(r1.get(k));
                }
            }
            Collections.reverse(r2);
            return r2.toArray(new Double[r2.size()]);
        }
        return null;
    }

    public boolean isBlankEchoDiscarded() {
        return this.blankEchoDiscarded;
    }
}
