/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.javafx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TitledPane;
import javafx.scene.control.skin.TitledPaneSkin;

/**
 *
 * @author pverley
 */
public class SelectableTitledPane extends TitledPane {

    private final CheckBox checkBox;

    public SelectableTitledPane(String title, Node content, Insets titlePadding) {
        super(title, content);
        checkBox = new CheckBox(title);
//    checkBox.selectedProperty().bindBidirectional(this.expandedProperty());
        checkBox.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            setExpanded(newValue);
        });
        setExpanded(false);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setGraphic(checkBox);
        setSkin(new TitledPaneSkin(this));
        if (null == content) {
            lookup(".arrow").setVisible(false);
        }
        if (null != titlePadding) {
            String insets = titlePadding.getTop() + " " + titlePadding.getRight() + " " + titlePadding.getBottom() + " " + titlePadding.getLeft();
            lookup(".title").setStyle("-fx-padding: " + insets + ";" + "-fx-background-color: null;");
        } else {
            lookup(".title").setStyle("-fx-background-color: null;");
        }
//    lookup(".content").setStyle("-fx-background-color: null; -fx-padding:  0.2em 0.2em 0.2em 1.316667em;");
    }

    public BooleanProperty selectedProperty() {
        return checkBox.selectedProperty();
    }

    public boolean isSelected() {
        return checkBox.isSelected();
    }

    public void setSelected(boolean selected) {
        checkBox.setSelected(selected);
    }
}
