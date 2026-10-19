/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui.configuration;

import org.amapvox.commons.math.util.SphericalCoordinates;
import org.amapvox.gui.Util;
import org.amapvox.canopy.analyzer.CanopyAnalyzerCfg;
import org.amapvox.canopy.analyzer.LAI2200;
import org.amapvox.canopy.analyzer.CanopyAnalyzerSensor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import javafx.stage.FileChooser;
import org.amapvox.canopy.analyzer.CanopyAnalyzerSensor.Direction;
import org.amapvox.gui.CanopyToolsIOFrameController;
import org.amapvox.gui.HelpButtonController;
import org.amapvox.gui.SensorPositionsController;
import org.apache.log4j.Logger;
import org.controlsfx.validation.ValidationSupport;

/**
 * FXML Controller class
 *
 * @author pverley
 */
public class CanopyAnalyzerFrameController extends ConfigurationController {

    // logger
    private final Logger LOGGER = Logger.getLogger(CanopyAnalyzerFrameController.class);

    // validation support
    private ValidationSupport canopyAnalyzerValidationSupport;

    // FXML imports
    @FXML
    private CanopyToolsIOFrameController canopyToolsIOFrameController;
    @FXML
    private SensorPositionsController sensorPositionsController;
    @FXML
    private ComboBox<CanopyAnalyzerCfg.CanopyAnalyzer> comboboxCanopyAnalyzerSensor;
    @FXML
    private ComboBox<Integer> comboboxCanopyAnalyzerDirectionCount;
    @FXML
    private ToggleButton toggleButtonCanopyAnalyzerRingMask1;
    @FXML
    private ToggleButton toggleButtonCanopyAnalyzerRingMask2;
    @FXML
    private ToggleButton toggleButtonCanopyAnalyzerRingMask3;
    @FXML
    private ToggleButton toggleButtonCanopyAnalyzerRingMask4;
    @FXML
    private ToggleButton toggleButtonCanopyAnalyzerRingMask5;
    // help buttons
    @FXML
    private Button helpButtonCanopyAnalzerDevice;
    @FXML
    private HelpButtonController helpButtonCanopyAnalzerDeviceController;
    @FXML
    private Button helpButtonCanopyAnalzerDirections;
    @FXML
    private HelpButtonController helpButtonCanopyAnalzerDirectionsController;
    @FXML
    private Button helpButtonRingMask;
    @FXML
    private HelpButtonController helpButtonRingMaskController;

    @Override
    public void initComponents(ResourceBundle rb) {
        
        comboboxCanopyAnalyzerSensor.getItems().setAll(CanopyAnalyzerCfg.CanopyAnalyzer.values());
        comboboxCanopyAnalyzerSensor.getSelectionModel().select(CanopyAnalyzerCfg.CanopyAnalyzer.LAI2200);

        comboboxCanopyAnalyzerDirectionCount.getItems().setAll(500, 4000, 10000);
        comboboxCanopyAnalyzerDirectionCount.getSelectionModel().selectFirst();

        helpButtonCanopyAnalzerDevice.setOnAction((ActionEvent event) -> {
            helpButtonCanopyAnalzerDeviceController.showHelpDialog(rb.getString("help_canopyanalyzer_device"));
        });

        helpButtonCanopyAnalzerDirections.setOnAction((ActionEvent event) -> {
            helpButtonCanopyAnalzerDirectionsController.showHelpDialog(rb.getString("help_canopyanalyzer_directions"));
        });

        helpButtonRingMask.setOnAction((ActionEvent event) -> {
            helpButtonRingMaskController.showHelpDialog(rb.getString("help_canopyanalyzer_ringmask"));
        });
    }

    @Override
    void initValidationSupport() {

        //lai2200 simulations validation support
        canopyAnalyzerValidationSupport = new ValidationSupport();

        canopyToolsIOFrameController.registerValidators();
    }

    @Override
    ObservableValue[] getListenedProperties() {

        List<ObservableValue> properties = new ArrayList();

        properties.addAll(Arrays.asList(
                new ObservableValue[]{
                    comboboxCanopyAnalyzerSensor.getSelectionModel().selectedItemProperty(),
                    comboboxCanopyAnalyzerDirectionCount.getSelectionModel().selectedItemProperty(),
                    toggleButtonCanopyAnalyzerRingMask1.selectedProperty(),
                    toggleButtonCanopyAnalyzerRingMask2.selectedProperty(),
                    toggleButtonCanopyAnalyzerRingMask3.selectedProperty(),
                    toggleButtonCanopyAnalyzerRingMask4.selectedProperty(),
                    toggleButtonCanopyAnalyzerRingMask5.selectedProperty()}));

        properties.addAll(Arrays.asList(canopyToolsIOFrameController.getListenedProperties()));
        properties.addAll(Arrays.asList(sensorPositionsController.getListenedProperties()));

        return properties.toArray(ObservableValue[]::new);
    }

    @Override
    public void saveConfiguration(File file) throws Exception {

        CanopyAnalyzerCfg cfg = new CanopyAnalyzerCfg();

        // validation support
        StringBuilder sb = new StringBuilder();
        if (canopyAnalyzerValidationSupport.isInvalid()) {
            canopyAnalyzerValidationSupport.initInitialDecoration();
            canopyAnalyzerValidationSupport.getValidationResult().getErrors().forEach(error -> sb.append("> ").append(error.getText()).append('\n'));
        }
        ValidationSupport voxelFileValidationSuuport = canopyToolsIOFrameController.getValidationSupport();
        if (voxelFileValidationSuuport.isInvalid()) {
            voxelFileValidationSuuport.initInitialDecoration();
            voxelFileValidationSuuport.getValidationResult().getErrors().forEach(error -> sb.append("> ").append(error.getText()).append('\n'));
        }
        if (!sb.toString().isEmpty()) {
            throw new IOException(sb.toString());
        }

        cfg.setDirectionCount(comboboxCanopyAnalyzerDirectionCount.getSelectionModel().getSelectedItem());

        cfg.setCanopyAnalyzer(comboboxCanopyAnalyzerSensor.getSelectionModel().getSelectedItem());

        cfg.setMasks(new boolean[]{
            toggleButtonCanopyAnalyzerRingMask1.isSelected(),
            toggleButtonCanopyAnalyzerRingMask2.isSelected(),
            toggleButtonCanopyAnalyzerRingMask3.isSelected(),
            toggleButtonCanopyAnalyzerRingMask4.isSelected(),
            toggleButtonCanopyAnalyzerRingMask5.isSelected()
        });

        cfg.setVoxelFile(canopyToolsIOFrameController.getVoxelFile());
        cfg.setPADVariable(canopyToolsIOFrameController.getPADVariable());
        cfg.setLeafAngleDistribution(canopyToolsIOFrameController.getLeafAngleDistribution());
        cfg.setLeafAngleDistributionParameters(canopyToolsIOFrameController.getLeafAngleDistributionParameters());

        cfg.setOutputDirectory(canopyToolsIOFrameController.getOutputDir());
        cfg.setOutputPrefix(canopyToolsIOFrameController.getOutputPrefix());

        if (comboboxCanopyAnalyzerDirectionCount.isEditable()) {
            cfg.setDirectionCount(Integer.parseInt(comboboxCanopyAnalyzerDirectionCount.getEditor().getText()));
        } else {
            cfg.setDirectionCount(comboboxCanopyAnalyzerDirectionCount.getSelectionModel().getSelectedItem());
        }

        cfg.setSensorPositions(sensorPositionsController.getPositions());

        cfg.write(file);
    }

    @Override
    public void loadConfiguration(File file) throws Exception {

        CanopyAnalyzerCfg cfg = new CanopyAnalyzerCfg();
        cfg.read(file);

        canopyToolsIOFrameController.setVoxelFile(cfg.getVoxelFile(), cfg.getPADVariable());
        canopyToolsIOFrameController.setLeafAngleDistribution(cfg.getLeafAngleDistribution());
        canopyToolsIOFrameController.setLeafAngleDistributionParameters(cfg.getLeafAngleDistributionParameters());
        canopyToolsIOFrameController.setOutputDir(cfg.getOutputDirectory());
        canopyToolsIOFrameController.setOutputPrefix(cfg.getOutputPrefix());

        sensorPositionsController.setPositions(cfg.getSensorPositions());

        comboboxCanopyAnalyzerSensor.getSelectionModel().select(cfg.getCanopyAnalyzer());
       
        comboboxCanopyAnalyzerDirectionCount.getSelectionModel().select(Integer.valueOf(cfg.getDirectionCount()));

        boolean[] masks = cfg.getMasks();
        if (masks != null && masks.length == 5) {
            toggleButtonCanopyAnalyzerRingMask1.setSelected(masks[0]);
            toggleButtonCanopyAnalyzerRingMask2.setSelected(masks[1]);
            toggleButtonCanopyAnalyzerRingMask3.setSelected(masks[2]);
            toggleButtonCanopyAnalyzerRingMask4.setSelected(masks[3]);
            toggleButtonCanopyAnalyzerRingMask5.setSelected(masks[4]);
        }
    }

    @FXML
    private void onActionButtonSaveCanopyAnalyzerDirections(ActionEvent event) {

        ChoiceDialog<String> choiceDialog = new ChoiceDialog<>();
        choiceDialog.getItems().addAll("OBJ", "CSV (spherical coordinates)", "CSV (cartesian coordinates)");
        choiceDialog.setSelectedItem("OBJ");

        choiceDialog.setTitle("Output format");
        choiceDialog.setContentText("Choose the output format");

        Optional<String> result = choiceDialog.showAndWait();

        if (result.isPresent()) {

            String format = result.get();

            boolean csv = (format.equals("CSV (spherical coordinates)") || format.equals("CSV (cartesian coordinates)"));

            boolean cartesian = format.equals("CSV (cartesian coordinates)") && csv;

            FileChooser fc = new FileChooser();
            File selectedFile = fc.showSaveDialog(null);

            if (selectedFile != null) {

                CanopyAnalyzerSensor canopyAnalyzer = new LAI2200(new boolean[]{
                    false, false, false, false, false
                });

                Direction[] directions = canopyAnalyzer.computeDirections(comboboxCanopyAnalyzerDirectionCount.getSelectionModel().getSelectedItem());
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {

                    if (csv) {
                        if (cartesian) {
                            writer.write("x y z ring band\n");
                        } else {
                            writer.write("azimut elevation ring band\n");
                        }
                    }

                    for (Direction direction : directions) {

                        if (csv) {
                            if (cartesian) {
                                writer.write(direction.x + " " + direction.y + " " + direction.z + " " + direction.ringIndex + " " + direction.bandIndex + "\n");
                            } else {
                                SphericalCoordinates sc = SphericalCoordinates.fromCartesian(direction.x, direction.y, direction.z);
                                writer.write(sc.getAzimut() + " " + sc.getZenith() + " " + direction.ringIndex + " " + direction.bandIndex + "\n");
                            }
                        } else {
                            writer.write("v " + direction.x + " " + direction.y + " " + direction.z + "\n");
                        }
                    }

                } catch (IOException ex) {
                    Util.showErrorDialog(null, ex, "[Canopy Analyzer]");
                }

                LOGGER.info("[Canopy analyzer] Directions saved to " + selectedFile);
            }
        }

    }
}
