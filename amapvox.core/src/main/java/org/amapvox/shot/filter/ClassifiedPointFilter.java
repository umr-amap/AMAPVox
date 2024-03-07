/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.shot.filter;

import org.amapvox.commons.util.filter.Filter;
import org.amapvox.voxelisation.las.LasShot;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pverley
 */
public class ClassifiedPointFilter implements Filter<LasShot.Echo> {

    private final List<Integer> classifiedPointsToDiscard;

    public ClassifiedPointFilter(List<Integer> classifiedPointsToDiscard) {

        this.classifiedPointsToDiscard = new ArrayList<>();
        if (null != classifiedPointsToDiscard) {
            this.classifiedPointsToDiscard.addAll(classifiedPointsToDiscard);
        }
    }

    public List<Integer> getClasses() {
        return classifiedPointsToDiscard;
    }

    @Override
    public void init() {
        // nothing to do
    }

    @Override
    public boolean accept(LasShot.Echo echo) {
        LasShot shot = (LasShot) echo.shot;
        return shot.classifications != null && !classifiedPointsToDiscard.contains(shot.classifications[echo.rank]);
    }

}
