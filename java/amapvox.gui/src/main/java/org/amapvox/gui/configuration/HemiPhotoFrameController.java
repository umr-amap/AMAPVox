/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui.configuration;

import org.amapvox.gui.TextFieldUtil;
import org.amapvox.gui.Validators;
import org.amapvox.canopy.hemiphoto.HemiPhotoCfg;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import org.amapvox.gui.HelpButtonController;
import org.amapvox.gui.SensorPositionsController;
import org.amapvox.gui.CanopyToolsIOFrameController;
import org.apache.log4j.Logger;
import org.controlsfx.validation.ValidationSupport;

/**
 * FXML Controller class
 *
 * @author pverley
 */
public class HemiPhotoFrameController extends ConfigurationController {

    // logger
    private final Logger LOGGER = Logger.getLogger(HemiPhotoFrameController.class);
    // validation support
    private ValidationSupport hemiPhotoValidationSupport;
    // directory chooser
    private DirectoryChooser directoryChooserHemiPhotoOutputDirectory;

    // FXML imports
    @FXML
    private CanopyToolsIOFrameController canopyToolsIOFrameController;
    @FXML
    private SensorPositionsController sensorPositionsController;
    @FXML
    private TextField textfieldPixelCount;
    @FXML
    private Button buttonHelpPixelCount;
    @FXML
    private HelpButtonController buttonHelpPixelCountController;
    @FXML
    private TextField textfieldMeridianCount;
    @FXML
    private Button buttonHelpMeridianCount;
    @FXML
    private HelpButtonController buttonHelpMeridianCountController;
    @FXML
    private TextField textfieldParallelCount;
    @FXML
    private Button buttonHelpParallelCount;
    @FXML
    private HelpButtonController buttonHelpParallelCountController;

    @Override
    public void initComponents(ResourceBundle rb) {

        textfieldPixelCount.setTextFormatter(TextFieldUtil.createIntegerTextFormatter(800, TextFieldUtil.Sign.POSITIVE));
        textfieldMeridianCount.setTextFormatter(TextFieldUtil.createIntegerTextFormatter(36, TextFieldUtil.Sign.POSITIVE));
        textfieldParallelCount.setTextFormatter(TextFieldUtil.createIntegerTextFormatter(9, TextFieldUtil.Sign.POSITIVE));

        sensorPositionsController.setText("Positions:");
        sensorPositionsController.setHelpButtonText("help_hemiphoto_sensor_positions");

        directoryChooserHemiPhotoOutputDirectory = new DirectoryChooser();
        directoryChooserHemiPhotoOutputDirectory.setTitle("Choose output directory");

        buttonHelpPixelCount.setOnAction((ActionEvent event) -> {
            buttonHelpPixelCountController.showHelpDialog(rb.getString("help_hemiphoto_pixel_count"));
        });

        buttonHelpMeridianCount.setOnAction((ActionEvent event) -> {
            buttonHelpMeridianCountController.showHelpDialog(rb.getString("help_hemiphoto_meridian_count"));
        });

        buttonHelpParallelCount.setOnAction((ActionEvent event) -> {
            buttonHelpParallelCountController.showHelpDialog(rb.getString("help_hemiphoto_parallel_count"));
        });
    }

    @Override
    ObservableValue[] getListenedProperties() {

        List<ObservableValue> properties = new ArrayList();

        properties.addAll(Arrays.asList(
                new ObservableValue[]{
                    textfieldPixelCount.textProperty(),
                    textfieldMeridianCount.textProperty(),
                    textfieldParallelCount.textProperty()
                }));

        properties.addAll(Arrays.asList(canopyToolsIOFrameController.getListenedProperties()));
        properties.addAll(Arrays.asList(sensorPositionsController.getListenedProperties()));

        return properties.toArray(ObservableValue[]::new);
    }

    @Override
    void initValidationSupport() {

        hemiPhotoValidationSupport = new ValidationSupport();
        hemiPhotoValidationSupport.registerValidator(textfieldPixelCount, true, Validators.fieldIntegerValidator);
        hemiPhotoValidationSupport.registerValidator(textfieldMeridianCount, true, Validators.fieldIntegerValidator);
        hemiPhotoValidationSupport.registerValidator(textfieldParallelCount, true, Validators.fieldIntegerValidator);
        canopyToolsIOFrameController.registerValidators();
    }

    @Override
    public void loadConfiguration(File file) throws Exception {

        HemiPhotoCfg cfg = new HemiPhotoCfg();
        cfg.read(file);

        canopyToolsIOFrameController.setVoxelFile(cfg.getVoxelFile(), cfg.getPADVariable());
        canopyToolsIOFrameController.setLeafAngleDistribution(cfg.getLeafAngleDistribution());
        canopyToolsIOFrameController.setLeafAngleDistributionParameters(cfg.getLeafAngleDistributionParameters());
        canopyToolsIOFrameController.setOutputDir(cfg.getOutputDirectory());
        canopyToolsIOFrameController.setOutputPrefix(cfg.getOutputPrefix());
        
        sensorPositionsController.setPositions(cfg.getSensorPositions());

        textfieldPixelCount.setText(String.valueOf(cfg.getPixelCount()));
        textfieldMeridianCount.setText(String.valueOf(cfg.getMeridianCount()));
        textfieldParallelCount.setText(String.valueOf(cfg.getParallelCount()));

    }

    @Override
    public void saveConfiguration(File file) throws Exception {

        // validation support
        StringBuilder sb = new StringBuilder();
        if (hemiPhotoValidationSupport.isInvalid()) {
            hemiPhotoValidationSupport.initInitialDecoration();
            hemiPhotoValidationSupport.getValidationResult().getErrors().forEach(error -> sb.append("> ").append(error.getText()).append('\n'));
        }
        ValidationSupport ioValidationSupport = canopyToolsIOFrameController.getValidationSupport();
        if (ioValidationSupport.isInvalid()) {
            ioValidationSupport.initInitialDecoration();
            ioValidationSupport.getValidationResult().getErrors().forEach(error -> sb.append("> ").append(error.getText()).append('\n'));
        }
        if (!sb.toString().isEmpty()) {
            throw new IOException(sb.toString());
        }

        HemiPhotoCfg cfg = new HemiPhotoCfg();

        cfg.setPixelCount(Integer.parseInt(textfieldPixelCount.getText()));
        cfg.setMeridianCount(Integer.parseInt(textfieldMeridianCount.getText()));
        cfg.setParallelCount(Integer.parseInt(textfieldParallelCount.getText()));

        cfg.setVoxelFile(canopyToolsIOFrameController.getVoxelFile());
        cfg.setPADVariable(canopyToolsIOFrameController.getPADVariable());
        cfg.setLeafAngleDistribution(canopyToolsIOFrameController.getLeafAngleDistribution());
        cfg.setLeafAngleDistributionParameters(canopyToolsIOFrameController.getLeafAngleDistributionParameters());
        

        cfg.setOutputDirectory(canopyToolsIOFrameController.getOutputDir());
        cfg.setOutputPrefix(canopyToolsIOFrameController.getOutputPrefix());
        
        cfg.setSensorPositions(sensorPositionsController.getPositions());

        cfg.write(file);
    }
}
