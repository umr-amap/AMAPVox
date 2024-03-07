/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util;

import java.util.Arrays;

/**
 *
 * @author pverley
 */
public class ArrayUtils {

    public static double[] parseDoubleArray(String s) throws NumberFormatException {

        String str = s.trim();

        // remove leading & trainling parenthesis or brackets
        if ((str.startsWith("(") && str.endsWith(")")) || ((str.startsWith("[") && str.endsWith("]")))) {
            str = str.substring(1, str.length() - 1);
        }

        // split, trim and convert to double array
        return Arrays.stream(str.split(guessSeparator(str)))
                .map(String::trim)
                .mapToDouble(Double::valueOf)
                .toArray();
    }
    
    public static int[] parseIntArray(String s) throws NumberFormatException {

        String str = s.trim();

        // remove leading & trainling parenthesis or brackets
        if ((str.startsWith("(") && str.endsWith(")")) || ((str.startsWith("[") && str.endsWith("]")))) {
            str = str.substring(1, str.length() - 1);
        }

        // split, trim and convert to double array
        return Arrays.stream(str.split(guessSeparator(str)))
                .map(String::trim)
                .mapToInt(Integer::valueOf)
                .toArray();
    }

    /**
     * Guess separator in a string representation of numeric array.
     * Default separator is COMA.
     */
    private static String guessSeparator(String s) {

        // guess separator
        String[] separators = new String[]{",", " ", ";", "\t", ":"};
        for (String guess : separators) {
            if (s.contains(guess) && s.split(guess).length >= 1) {
                return guess;
            }
        }
        return separators[0];
    }

}
