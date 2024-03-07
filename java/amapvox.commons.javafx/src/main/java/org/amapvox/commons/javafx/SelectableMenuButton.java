/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.javafx;

import javafx.beans.property.BooleanProperty;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;

/**
 *
 * @author pverley
 */
public class SelectableMenuButton extends SplitMenuButton {

    private final CheckBox checkBox = new CheckBox();
    private final MenuItem ALL = new MenuItem("All");
    private final MenuItem NONE = new MenuItem("None");

    public SelectableMenuButton() {
        setGraphic(checkBox);
        checkBox.disableProperty().bind(disableProperty());
            addMenuItem(ALL);
            addMenuItem(NONE);
        checkBox.addEventHandler(ActionEvent.ACTION, event -> {
            if (checkBox.isSelected()) {
                ALL.fire();
            } else {
                NONE.fire();
        }
        });
        ALL.addEventHandler(ActionEvent.ACTION, event -> checkBox.setSelected(true));
        NONE.addEventHandler(ActionEvent.ACTION, event -> checkBox.setSelected(false));
    }

    private void addMenuItem(MenuItem item) {
        getItems().add(item);
        item.setStyle("-fx-padding: 0 5 0 25;");
    }

    public boolean addMenuItem(String text, EventHandler<ActionEvent> value) {
        if (null != getMenuItem(text)) {
            // already an item with same name
            return false;
        } else {
            // add item
            MenuItem item = new MenuItem(text);
            addMenuItem(item);
            item.setOnAction(value);
            return true;
        }
    }
    
    public void setOnActionAll(EventHandler<ActionEvent> value) {
        ALL.setOnAction(value);
    }
    
    public void setOnActionNone(EventHandler<ActionEvent> value) {
        NONE.setOnAction(value);
    }

    public MenuItem getMenuItem(String text) {

        FilteredList<MenuItem> items = getItems().filtered(item -> item.getText().equalsIgnoreCase(text));
        // 
        return items.isEmpty() ? null : items.get(0);
    }

    public void removeMenuItem(String text) {
        getItems().removeIf(item -> item.getText().equalsIgnoreCase(text));
    }

    public BooleanProperty selectedProperty() {
        return checkBox.selectedProperty();
    }

    public boolean isSelected() {
        return checkBox.isSelected();
    }

    public void setSelected(boolean value) {
        checkBox.setSelected(value);
    }

    public BooleanProperty indeterminateProperty() {
        return checkBox.indeterminateProperty();
    }

    public boolean isIndeterminate() {
        return checkBox.isIndeterminate();
    }

    public void setIndeterminate(boolean value) {
        checkBox.setIndeterminate(value);
    }

}
