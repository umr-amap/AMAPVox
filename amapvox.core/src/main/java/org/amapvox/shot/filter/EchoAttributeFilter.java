/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.shot.filter;

import org.amapvox.commons.util.filter.Filter;
import org.amapvox.commons.util.filter.FloatFilter;
import org.amapvox.shot.Shot;

/**
 * Filter an echo based attribute values, not operational yet.
 * Refactoring of RxpEchoFilter.java and EchoFilter.java
 * 
 * @author Philippe Verley
 */
public class EchoAttributeFilter implements Filter<Shot.Echo> {

    private final FloatFilter filter;

    public EchoAttributeFilter(FloatFilter filter) {
        this.filter = filter;
    }

    public FloatFilter getFilter() {
        return filter;
    }
    
    @Override
    public void init() {
        // nothing to do
    }

    @Override
    public boolean accept(Shot.Echo echo) {

//        Shot shot = echo.shot;
//        switch (filter.getVariable()) {
//            case "Reflectance":
//                if (shot.reflectances != null) {
//                    return filter.accept(shot.reflectances[echo.rank]);
//                }
//            case "Amplitude":
//                if (shot.amplitudes != null) {
//                    return filter.accept(shot.amplitudes[echo.rank]);
//                }
//            case "Deviation":
//
//                if (shot.deviations != null) {
//                    return filter.accept(shot.deviations[echo.rank]);
//                }
//        }
        return true;
    }

}
