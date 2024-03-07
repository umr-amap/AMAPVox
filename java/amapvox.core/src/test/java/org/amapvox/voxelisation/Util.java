/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * @author pverley
 */
public class Util {

    public static boolean equal(double v1, double v2) {
        return new BigDecimal(v1).setScale(6, RoundingMode.HALF_UP)
                .compareTo(new BigDecimal(v2).setScale(6, RoundingMode.HALF_UP)) == 0;
    }

}
