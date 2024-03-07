/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui.chart;

import java.awt.Color;

/**
 *
 * @author Julien Heurtebize
 */
public class SeriesParameters {
    
    private String label;
    private Color color;
    
    public SeriesParameters(String label) {
        this.label = label;
        this.color = Color.BLACK;
    }

    public SeriesParameters(String label, Color color) {
        this.label = label;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public String getLabel() {
        return label;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
