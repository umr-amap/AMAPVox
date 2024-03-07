/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.javafx;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 *
 * @author http://stackoverflow.com/users/1844265/roland
 */
public class PannableCanvas extends Pane {

    DoubleProperty myScale = new SimpleDoubleProperty(1.0);

    public PannableCanvas() {
        super.setPrefSize(1200, 1200);
        setStyle("-fx-background-color: rgba(1, 1, 1, 0.0);");

        // add scale transform
        scaleXProperty().bind(myScale);
        scaleYProperty().bind(myScale);
    }

    /**
     * Add a grid to the canvas, send it to back
     * @param startPointX
     * @param startPointY
     * @param resolution grid spacing
     * @param height
     * @param width
     */
    public void addGrid(int startPointX, int startPointY, int width, int height, int resolution) {

        double w = getBoundsInLocal().getWidth();
        double h = getBoundsInLocal().getHeight();

        // add grid
        Canvas grid = new Canvas(w, h);
        grid.setTranslateY(startPointY);

        // don't catch mouse events
        grid.setMouseTransparent(true);

        GraphicsContext gc = grid.getGraphicsContext2D();

        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);

        // draw grid lines
        
        for(int i=0; i <= width; i++) {
            gc.strokeLine(startPointX+(i*resolution), 0, startPointX+(i*resolution), 0+height);
        }
        
        for(int i=0; i <= height; i++) {
            gc.strokeLine(startPointX, 0+(i*resolution), startPointX+width, 0+(i*resolution));
        }

        getChildren().add( grid);

        grid.toBack();
    }

    public double getScale() {
        return myScale.get();
    }

    public void setScale( double scale) {
        myScale.set(scale);
    }

    public void setPivot( double x, double y) {
        setTranslateX(getTranslateX()-x);
        setTranslateY(getTranslateY()-y);
    }
}