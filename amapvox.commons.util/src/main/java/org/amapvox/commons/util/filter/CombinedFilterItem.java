/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util.filter;

import static org.amapvox.commons.util.filter.CombinedFloatFilter.AND;

/**
 *
 * @author calcul
 */
public class CombinedFilterItem extends CombinedFloatFilter {

    private final boolean display;
    private final String scalarField;

    public CombinedFilterItem(String scalarField, boolean display, FloatFilter filter1, FloatFilter filter2, int type) {
        super(filter1, filter2, type);

        this.display = display;
        this.scalarField = scalarField;
    }

    @Override
    public String toString() {
        
        if(filter2 == null){
            return (display ? "Display" : "Don't display") + " " + scalarField + " " + ("equals to ") +filter1.getValue();
        }else{
            return (display ? "Display" : "Don't display") + " " + scalarField + " " + filter1.getConditionString() + " " + filter1.getValue() + " and " +
                filter2.getConditionString() + " " + filter2.getValue();
        }

        /*return (display ? "Display" : "Don't display") + " "
                + scalarField + " " + filter2 == null ? ("equals to ") +
                filter1.getValue() : 
                filter1.getConditionString() + " " + filter1.getValue() + " and " +
                filter2.getConditionString() + " " + filter2.getValue();*/
    }

    public static void main(String[] args) {

        CombinedFilterItem item = new CombinedFilterItem("pad", true, new FloatFilter("x", 5, FloatFilter.EQUAL), null, AND);

        System.out.println(item.toString());
    }

    public boolean isDisplay() {
        return display;
    }

    public String getScalarField() {
        return scalarField;
    }
}


