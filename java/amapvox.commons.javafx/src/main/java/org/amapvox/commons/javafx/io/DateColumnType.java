/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.javafx.io;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 *
 * @author Julien Heurtebize
 */
public class DateColumnType{
    
    private String pattern;
    private Locale locale;
    private final SimpleDateFormat sdf;

    public DateColumnType(String pattern, Locale locale) {
        sdf = new SimpleDateFormat(pattern, locale);
    }

    public Locale getLocale() {
        return locale;
    }

    public String getPattern() {
        return pattern;
    }

    public SimpleDateFormat getFormatter() {
        return sdf;
    }
}
