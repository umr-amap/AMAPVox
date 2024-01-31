/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.amapvox.commons.Configuration;
import org.amapvox.gui.MainFrameController.TaskUI;

/**
 * FXML Controller class
 *
 * @author pverley
 */
public class PreferencesFrameController implements Initializable {

    private Stage stage;
    private Parent root;

    private Preferences prefs;

    // FXML imports
    @FXML
    private Slider sliderNCPU;
    @FXML
    Label labelNCPU;
    @FXML
    private VBox vboxActiveTools;
    @FXML
    private VBox vboxInactiveTools;
    @FXML
    private VBox vboxDeprecatedTools;
    @FXML
    private Button btnClear;

    static PreferencesFrameController newInstance() {

        PreferencesFrameController controller = null;

        try {

            FXMLLoader loader = new FXMLLoader(PreferencesFrameController.class.getResource("/org/amapvox/gui/fxml/PreferencesFrame.fxml"));
            Parent root = loader.load();
            controller = loader.getController();
            controller.root = root;

        } catch (IOException ex) {
            Logger.getLogger(PreferencesFrameController.class.getName()).log(Level.SEVERE, "Cannot load PreferencesFrame.fxml", ex);
        }

        return controller;
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        int availableCores = Runtime.getRuntime().availableProcessors();
        sliderNCPU.setMin(1);
        sliderNCPU.setMax(availableCores);
        sliderNCPU.setValue(availableCores - 1);
        labelNCPU.textProperty().bind(sliderNCPU.valueProperty().asString("%02.0f"));
    }

    public void setPreferences(Preferences prefs) {
        this.prefs = prefs;

        prefs.addPreferenceChangeListener(event -> {
            try {
                btnClear.setDisable(prefs.keys().length <= 0);
            } catch (BackingStoreException ex) {
                // just ignore
            }
        });

        // apply prefs
        sliderNCPU.setValue(prefs.getInt("ncpu", Runtime.getRuntime().availableProcessors() - 1));
    }

    void addTasks(TaskUI task, MainFrameController mainController) {

        try {
            Configuration cfg = Configuration.newInstance(task.getClassName());
            CheckBox checkbox = new CheckBox(cfg.getLongName());
            ImageView icon = new ImageView(new Image(MainFrameController.class.getResource(task.getIcon()).toExternalForm()));
            icon.setFitHeight(24);
            icon.setFitWidth(24);
            checkbox.setGraphic(icon);
            checkbox.setSelected(prefs.getBoolean(task.getClassName(), task.getStatus().equals(RepoStatus.ACTIVE)));
            checkbox.setDisable(task.getStatus().equals(RepoStatus.MOVED));
            checkbox.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
                        mainController.updateNewMenuItem(task);
                        prefs.putBoolean(task.getClassName(), new_val);
                    });
            Tooltip tooltip = new Tooltip();
            tooltip.setText(cfg.getDescription());
            Util.hackTooltipStartTiming(tooltip, 0L);
            Tooltip.install(checkbox, tooltip);
            switch (task.getStatus()) {
                case ACTIVE ->
                    vboxActiveTools.getChildren().add(checkbox);
                case INACTIVE ->
                    vboxInactiveTools.getChildren().add(checkbox);
                case MOVED ->
                    vboxDeprecatedTools.getChildren().add(checkbox);
            }
        } catch (Exception ex) {
            Logger.getLogger(PreferencesFrameController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    private void onActionButtonClear(ActionEvent event) throws BackingStoreException {
        prefs.clear();
    }

    @FXML
    private void onActionButtonCancel(ActionEvent event) {

        sliderNCPU.setValue(prefs.getInt("ncpu", Runtime.getRuntime().availableProcessors() - 1));
        stage.close();
    }

    @FXML
    private void onActionButtonOK(ActionEvent event) {

        int ncpu = (int) sliderNCPU.getValue();
        prefs.putInt("ncpu", ncpu);
        stage.close();
    }

    Stage getStage() {
        if (null == stage) {
            stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Preferences");
            stage.initModality(Modality.APPLICATION_MODAL);
        }
        return stage;
    }
}
