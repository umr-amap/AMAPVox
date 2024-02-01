/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui.configuration;

import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.stage.Stage;

/**
 *
 * @author pverley
 */
abstract public class ConfigurationController implements Initializable {

    private final BooleanProperty changedProperty = new SimpleBooleanProperty(false);

    private final UIChangeListener uiChangeListener = new UIChangeListener();

    private boolean loaded = false;

    final protected DecimalFormat df;

    private Stage stage;

    abstract void saveConfiguration(File file) throws Exception;

    abstract void loadConfiguration(File file) throws Exception;

    /**
     * Initializes the components.
     *
     * @param rb, the resource bundle
     */
    abstract void initComponents(ResourceBundle rb);

    /**
     * Initializes validation support.
     */
    abstract void initValidationSupport();

    abstract ObservableValue[] getListenedProperties();

    private boolean changeListenerAdded = false;

    ConfigurationController() {
        DecimalFormatSymbols symb = new DecimalFormatSymbols();
        symb.setDecimalSeparator('.');
        df = new DecimalFormat("#####.######", symb);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        initComponents(rb);
        initValidationSupport();

        if (!changeListenerAdded) {
            for (ObservableValue value : getListenedProperties()) {
                value.addListener(uiChangeListener);
            }
            changeListenerAdded = true;
        }
    }
    
    final public ChangeListener getUIChangeListener() {
        return uiChangeListener;
    }

    final public void save(File file, boolean reset) throws Exception {
        saveConfiguration(file);
        if (reset) {
            resetChangeProperty();
        }
    }

    final public void save(File file) throws Exception {
        save(file, true);
    }

    final public void load(File file) throws Exception {
        loadConfiguration(file);
        resetChangeProperty();
        loaded = true;
    }

    final public void unload() {
        resetChangeProperty();
        loaded = false;
    }

    final public boolean isLoaded() {
        return loaded;
    }

    final public ReadOnlyBooleanProperty changedProperty() {
        return changedProperty;
    }

    private void resetChangeProperty() {
        changedProperty.set(false);
    }

    private class UIChangeListener implements ChangeListener {

        @Override
        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            if (oldValue != newValue) {
                changedProperty.set(true);
            }
        }
    }

}
