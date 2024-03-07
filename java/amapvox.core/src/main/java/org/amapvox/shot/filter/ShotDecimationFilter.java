/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.shot.filter;

import org.amapvox.commons.util.filter.Filter;
import org.amapvox.shot.Shot;
import java.util.Random;

/**
 * This filter decimates a fraction of the shots randomly given a decimation
 * rate.
 *
 * @author Philippe Verley
 */
public class ShotDecimationFilter implements Filter<Shot> {

    private final float decimationFactor;
    private final Random rd;

    public ShotDecimationFilter(float decimationFactor) {
        this.decimationFactor = decimationFactor;
        rd = new Random();
    }

    @Override
    public void init() throws Exception {
        // nothing to do
    }

    @Override
    public boolean accept(Shot shot) {
        return rd.nextFloat() >= decimationFactor;
    }

    public float getDecimationFactor() {
        return decimationFactor;
    }
}
