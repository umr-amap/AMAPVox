/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util.filter;

/**
 * This class represent a filter and can is compound of a variable, a condition
 * and a value
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class FloatFilter implements Filter<Float> {

    public final static int NOT_EQUAL = 0;

    public final static int EQUAL = 1;

    public final static int LESS_THAN = 2;

    public final static int LESS_THAN_OR_EQUAL = 3;

    public final static int GREATER_THAN = 4;

    public final static int GREATER_THAN_OR_EQUAL = 5;

    private final String variable;
    private final float value;
    private final int condition;

    /**
     *
     * @param variable The variable name
     * @param value The value to test
     * @param condition The condition to compare the variable to value
     */
    public FloatFilter(String variable, float value, int condition) {
        this.variable = variable;
        this.value = value;
        this.condition = condition;
    }

    /**
     *
     * @return
     */
    public String getVariable() {
        return variable;
    }

    /**
     *
     * @return
     */
    public double getValue() {
        return value;
    }

    /**
     *
     * @return
     */
    public int getCondition() {
        return condition;
    }

    /**
     *
     * @return the condition as a string
     */
    public String getConditionString() {

        switch (condition) {

            case FloatFilter.EQUAL:
                return "==";
            case FloatFilter.NOT_EQUAL:
                return "!=";
            case FloatFilter.LESS_THAN:
                return "<";
            case FloatFilter.LESS_THAN_OR_EQUAL:
                return "<=";
            case FloatFilter.GREATER_THAN:
                return ">";
            case FloatFilter.GREATER_THAN_OR_EQUAL:
                return ">=";
            default:
                return "";
        }
    }

    @Override
    public void init() {
        // nothing to do
    }

    @Override
    public boolean accept(Float value) {

        int comp = Float.compare(value, this.value);
        switch (condition) {
            case FloatFilter.EQUAL:
                return comp == 0;
            case FloatFilter.GREATER_THAN:
                return comp > 0;
            case FloatFilter.GREATER_THAN_OR_EQUAL:
                return comp >= 0;
            case FloatFilter.LESS_THAN:
                return comp < 0;
            case FloatFilter.LESS_THAN_OR_EQUAL:
                return comp <= 0;
            case FloatFilter.NOT_EQUAL:
                return comp != 0;
            default:
                return false;
        }
    }

    /**
     *
     * @param condition the condition as a string Possible values ("!=" , "==" ,
     * "&lt;", "&gt;" "&gt;=", "&lt;=")
     * @return the integer value of the condition
     */
    public static int getConditionFromString(String condition) {

        switch (condition) {

            case "==":
                return FloatFilter.EQUAL;
            case "!=":
                return FloatFilter.NOT_EQUAL;
            case "<":
                return FloatFilter.LESS_THAN;
            case "<=":
                return FloatFilter.LESS_THAN_OR_EQUAL;
            case ">":
                return FloatFilter.GREATER_THAN;
            case ">=":
                return FloatFilter.GREATER_THAN_OR_EQUAL;
            default:
                return -1;
        }
    }

    /**
     *
     * @param filterString string to be convert to Filter object <br>
     * @return
     */
    public static FloatFilter getFilterFromString(String filterString) {

        String[] split = filterString.split(" ");

        if (split.length != 3) {
            return null;
        }

        FloatFilter filter = new FloatFilter(split[0], Float.valueOf(split[2]), getConditionFromString(split[1]));

        return filter;
    }

    @Override
    public String toString() {
        return variable + "\t" + getConditionString() + "\t" + value;
    }

}
