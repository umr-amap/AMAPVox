/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.javafx;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

/**
 *
 * @author Julien Heurtebize
 */
public class Timeline extends AnchorPane{
    
    private final NumberAxis numberAxis;
    private final Spinner spinner;
    
    private final StackPane stackPane;
    private final ScrollPane scrollPane;
    private final VBox vBox;
    
    private final Cursor cursor= new Cursor();
    //public final SimpleDoubleProperty selectedTick = new SimpleDoubleProperty(0);
    
    private final static int MAXIMUM_VISIBLE_TICK_UNIT = 11;
    
    private final double effectiveStart;
    private final double effectiveEnd;
    private final double effectiveStep;
    
    public class Cursor extends Rectangle{

        public final SimpleDoubleProperty selectedTick = new SimpleDoubleProperty(0);

        public Cursor() {

            super(0, 0, 2, 150);

            setStroke(new Color(1, 0, 0, 1));
            setMouseTransparent(true);

            selectedTick.set(0);
        }

        /**
         * <p>Get the cursor tick position</p>
         * Example : 0 if selected tick is 0, 40.1 if the cursor is over the 40.1 tick.
         * @return 
         */
        public double getTimeCursorPosition(){

            return selectedTick.get();
        }

        public void setTimeCursorPosition(double tick){

            double cursorPos = getTickPosition(tick);
            selectedTick.set(tick);

            cursor.setTranslateX(cursorPos);
        }

    }

    public Timeline(double start, double end, double step) {
        
        effectiveStart = start;
        effectiveEnd = end;
        effectiveStep = step;
        
        double tickNumber = ((end - start) / step) + 1;
        if(tickNumber > MAXIMUM_VISIBLE_TICK_UNIT){
            step = getOptimalStep(start, end);
        }
        
        numberAxis = new NumberAxis(start, end, step);
        
        numberAxis.setAnimated(false);
        
        numberAxis.setTickLength(150);
        numberAxis.setMinorTickVisible(false);
                
        spinner = new Spinner(start, end, start);        
        
        
        stackPane = new StackPane(numberAxis, cursor);
        stackPane.setAlignment(Pos.TOP_LEFT);
        stackPane.prefHeightProperty().bind(numberAxis.heightProperty());
        
        GridPane gridPane = new GridPane();
        RowConstraints row = new RowConstraints();
        row.setPercentHeight(100);
        row.setFillHeight(true);
        row.setValignment(VPos.CENTER);
        gridPane.getRowConstraints().add(row);
        
        AnchorPane.setTopAnchor(gridPane, 0.0);
        AnchorPane.setLeftAnchor(gridPane, 0.0);
        AnchorPane.setRightAnchor(gridPane, 0.0);

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(100);
        col.setFillWidth(false);
        col.setHalignment(HPos.CENTER);
        
        gridPane.getColumnConstraints().add(col);
        gridPane.add(stackPane, 0, 0);
        
        scrollPane = new ScrollPane(gridPane);
        scrollPane.setFitToHeight(true);
        
        scrollPane.setPrefHeight(210);
        //stackPane.prefHeightProperty().bind(numberAxis.heightProperty());        
        //scrollPane.prefHeightProperty().bind(numberAxis.heightProperty());
        
        gridPane.minWidthProperty().bind(scrollPane.widthProperty());
        stackPane.minWidthProperty().bind(scrollPane.widthProperty());
        
        vBox = new VBox(scrollPane, spinner);
        vBox.setFillWidth(true);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10);
        
        numberAxis.setTickLabelFormatter(null);
        
        Text text = new Text("2016/30/09\n" +"    16:32");
        double width = text.getBoundsInLocal().getWidth();
        double width2 = text.getBoundsInParent().getWidth();
        /*numberAxis.setTickLabelFormatter(new StringConverter<Number>() {
        @Override
        public String toString(Number object) {
        if(object.doubleValue() == 50){
        return "2016/30/09\n" +"    16:32";
        }
        return "";
        }
        @Override
        public Number fromString(String string) {
        return new Double(0);
        }
        });*/
        
        
        spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(start, end, start, effectiveStep));
        
        spinner.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                double value = (Double)newValue;
                
                cursor.setTimeCursorPosition(value);
            }
        });
        
        scrollPane.hvalueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                
                double doubleValue = newValue.doubleValue();
                
                if((doubleValue >= 1 || doubleValue <= 0) 
                        && numberAxis.getUpperBound() < effectiveEnd && numberAxis.getLowerBound() > effectiveStart){
                
                double newLowerBound;
                double newUpperBound;
                
                double scrollPaneViewportWidth = scrollPane.getViewportBounds().getWidth();
                double numberAxisWidth = numberAxis.getWidth();
                
                double diff = 0.5 - doubleValue; //if diff > 0, scrollpane was dragged to left, otherwise to right
                                
                double delta = (-diff * numberAxisWidth) / scrollPaneViewportWidth;
                
                newLowerBound = numberAxis.getLowerBound()+delta;
                newUpperBound = numberAxis.getUpperBound()+delta;
                
                if(newLowerBound >= effectiveStart && newUpperBound <= effectiveEnd){
                    numberAxis.setLowerBound(newLowerBound);
                    numberAxis.setUpperBound(newUpperBound);
                }else{
                    numberAxis.setLowerBound(effectiveStart);
                    numberAxis.setUpperBound(effectiveEnd);
                }
                
                if(diff > 0){
                    //stackPane.setPrefWidth(stackPane.getWidth()-delta);
                }else{
                    //stackPane.setPrefWidth(stackPane.getWidth()+delta);
                }
                
                    scrollPane.setHvalue(0.5);
                    
                    double newOffset = 0.5 * scrollPaneViewportWidth;
                    
                    if(doubleValue >= 1){
                        delta = (newOffset * numberAxisWidth) / scrollPaneViewportWidth;
                    }else{
                        delta = (-newOffset * numberAxisWidth) / scrollPaneViewportWidth;
                    }
                    
                    newLowerBound = numberAxis.getLowerBound()-delta;
                    newUpperBound = numberAxis.getUpperBound()-delta;
                    
                    if(newLowerBound >= effectiveStart && newUpperBound <= effectiveEnd){
                        numberAxis.setLowerBound(newLowerBound);
                        numberAxis.setUpperBound(newUpperBound);
                    }else{
                        numberAxis.setLowerBound(effectiveStart);
                        numberAxis.setUpperBound(effectiveEnd);
                    }
                    
                }
            }
        });
        
        stackPane.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println(event.getSceneX()+"\t"+event.getSceneY());
            }
        });
        
        
        numberAxis.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                
                //scrollPane.setHvalue(0.5);
                
                //scrollPane.setFitToWidth(false);
                
                double delta = getSpaceBetweenSuccessiveTicks();
                
                double newLowerBound;
                double newUpperBound;
                //double newWidth;
                
                if(event.getDeltaY() > 0){
                    //newWidth = stackPane.getWidth()+delta;
                    newLowerBound = numberAxis.getLowerBound()+effectiveStep;
                    newUpperBound = numberAxis.getUpperBound()-effectiveStep;
                    
                }else{
                    //newWidth = stackPane.getWidth()-delta;
                    newLowerBound = numberAxis.getLowerBound()-effectiveStep;
                    newUpperBound = numberAxis.getUpperBound()+effectiveStep;
                }
                
                if(newLowerBound >= effectiveStart && newUpperBound <= effectiveEnd){
                    numberAxis.setLowerBound(newLowerBound);
                    numberAxis.setUpperBound(newUpperBound);
                    
                }else{ //snap bounds to initial min and max
                    numberAxis.setLowerBound(effectiveStart);
                    numberAxis.setUpperBound(effectiveEnd);                    
                    //stackPane.setPrefWidth(getSpaceBetweenTwoTicks(effectiveStart, effectiveEnd));
                    
                    //scrollPane.setHvalue(0.5);
                }
                
                double newStep = getOptimalStep(numberAxis.getLowerBound(), numberAxis.getUpperBound());
                    
                if(newStep >= effectiveStep){
                    numberAxis.setTickUnit(newStep);
                }else{
                    numberAxis.setTickUnit(effectiveStep);
                }
                
            }
        });
        
        numberAxis.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                
                
            }
        });
        
        numberAxis.setOnMouseDragOver(new EventHandler<MouseDragEvent>() {
            @Override
            public void handle(MouseDragEvent event) {
                scrollPane.setPannable(false);
            }
        });
        
        numberAxis.setOnMouseDragReleased(new EventHandler<MouseDragEvent>() {
            @Override
            public void handle(MouseDragEvent event) {
                scrollPane.setPannable(true);
            }
        });
        
        EventHandler eh = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                                
                Point2D sceneToLocal = numberAxis.sceneToLocal(event.getSceneX(), event.getSceneY());
                
                double nearestTick = getNearestTick(sceneToLocal.getX());
                
                if(nearestTick >= numberAxis.getLowerBound() && nearestTick <= numberAxis.getUpperBound()){
                    spinner.getValueFactory().setValue((double)nearestTick);
                
                    //snap cursor to the nearest tick                
                    cursor.setTimeCursorPosition(nearestTick);
                }
            }
        };
        
        numberAxis.setOnMouseDragged(eh);
        numberAxis.setOnMouseClicked(eh);
        
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        
        AnchorPane.setTopAnchor(vBox, 0.0);
        AnchorPane.setLeftAnchor(vBox, 0.0);
        AnchorPane.setRightAnchor(vBox, 0.0);
        
        super.getChildren().add(vBox);
    }
    
    private double getTick(double posX){
        
        double tick = numberAxis.getLowerBound() + ((posX - numberAxis.getTranslateX()) * getAxisWidth()) / numberAxis.getWidth();
        
        return tick;
    }
    
    private double getNearestTick(double posX){
        
        double nearestTick = Math.round(getTick(posX) / effectiveStep)*effectiveStep;
        
        return nearestTick;
    }
    
    private double getTickPosition(double tick){
        
        double cursorPos = (((tick - numberAxis.getLowerBound() ) * numberAxis.getWidth()) / getAxisWidth() );
        
        return cursorPos;
    }
    
    private double getSpaceBetweenTwoTicks(double tick1, double tick2){
        
        double pos1 = getTickPosition(tick1);
        double pos2 = getTickPosition(tick2);
        
        return pos2 - pos1;
    }
    
    private double getSpaceBetweenSuccessiveTicks(){
        
        double pos1 = getTickPosition(numberAxis.getLowerBound());
        double pos2 = getTickPosition(numberAxis.getLowerBound()+numberAxis.getTickUnit());
        
        return pos2 - pos1;
    }
    
    private double getOptimalStep(double start, double end){
        
        double step = (end - start ) / (MAXIMUM_VISIBLE_TICK_UNIT - 1);
        return step;
    }
    
    private double getAxisWidth(){
        return numberAxis.getUpperBound() - numberAxis.getLowerBound();
    }

    public Cursor getTimeCursor() {
        return cursor;
    }
}
