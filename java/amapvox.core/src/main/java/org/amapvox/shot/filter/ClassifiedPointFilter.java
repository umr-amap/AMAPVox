/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.shot.filter;

import org.amapvox.commons.util.filter.Filter;
import java.util.ArrayList;
import java.util.List;
import org.amapvox.shot.Echo;

/**
 *
 * @author pverley
 */
public class ClassifiedPointFilter implements Filter<Echo> {

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
    public boolean accept(Echo echo) {
        
        Integer classification = echo.getInteger("classification");
            
        return (null == classification) || (!classifiedPointsToDiscard.contains(classification));
    }

}
