/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util;

import java.text.DecimalFormat;
import java.text.FieldPosition;

/**
 *
 * @author calcul
 */
    
public class DecimalScientificFormat extends DecimalFormat {
    private static final DecimalFormat DF = new DecimalFormat("#0.00##");
    private static final DecimalFormat SF = new DecimalFormat("0.###E0");

    @Override
    public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
        String decimalFormat = DF.format(number);
        return (0.0001 != number && DF.format(0.0001).equals(decimalFormat)) ? SF.format(number, result, fieldPosition) : result.append(decimalFormat);
    }
}
