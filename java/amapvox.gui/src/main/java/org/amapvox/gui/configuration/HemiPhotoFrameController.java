/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui.configuration;

import org.amapvox.commons.javafx.SelectableMenuButton;
import org.amapvox.gui.PositionImporterFrameController;
import org.amapvox.gui.TextFieldUtil;
import org.amapvox.gui.Util;
import org.amapvox.gui.Validators;
import org.amapvox.canopy.hemi.HemiParameters;
import org.amapvox.canopy.hemi.HemiPhotoCfg;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.vecmath.Point3d;
import org.amapvox.gui.VoxelFileCanopyController;
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
    private ValidationSupport hemiPhotoSimValidationSupport;
    // file chooser
    private DirectoryChooser directoryChooserSaveHemiPhotoOutputBitmapFile;
    private DirectoryChooser directoryChooserSaveHemiPhotoOutputTextFile;
    // position importer
    private PositionImporterFrameController positionImporterFrameController;

    // FXML imports
    @FXML
    private VoxelFileCanopyController voxelFileCanopyController;
    @FXML
    private TextField textfieldHemiPhotoOutputTextFile;
    @FXML
    private TextField textfieldHemiPhotoOutputBitmapFile;
    @FXML
    private ListView<Point3d> listViewHemiPhotoSensorPositions;
    @FXML
    private SelectableMenuButton selectorHemiPhotoSensor;
    @FXML
    private TextField textfieldPixelNumber;
    @FXML
    private TextField textfieldAzimutsNumber;
    @FXML
    private TextField textfieldZenithsNumber;

    @Override
    public void initComponents(ResourceBundle rb) {

        textfieldPixelNumber.setTextFormatter(TextFieldUtil.createIntegerTextFormatter(800, TextFieldUtil.Sign.POSITIVE));
        textfieldAzimutsNumber.setTextFormatter(TextFieldUtil.createIntegerTextFormatter(36, TextFieldUtil.Sign.POSITIVE));
        textfieldZenithsNumber.setTextFormatter(TextFieldUtil.createIntegerTextFormatter(9, TextFieldUtil.Sign.POSITIVE));

        listViewHemiPhotoSensorPositions.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        Util.linkSelectorToList(selectorHemiPhotoSensor, listViewHemiPhotoSensorPositions);

        Util.setDragGestureEvents(textfieldHemiPhotoOutputBitmapFile);
        Util.setDragGestureEvents(textfieldHemiPhotoOutputTextFile);

        directoryChooserSaveHemiPhotoOutputBitmapFile = new DirectoryChooser();
        directoryChooserSaveHemiPhotoOutputBitmapFile.setTitle("Choose bitmap files output directory");

        directoryChooserSaveHemiPhotoOutputTextFile = new DirectoryChooser();
        directoryChooserSaveHemiPhotoOutputTextFile.setTitle("Choose text files output directory");

        positionImporterFrameController = PositionImporterFrameController.newInstance();
    }

    @Override
    ObservableValue[] getListenedProperties() {

        List<ObservableValue> properties = new ArrayList();

        properties.addAll(Arrays.asList(
                new ObservableValue[]{
                    listViewHemiPhotoSensorPositions.itemsProperty(),
                    textfieldHemiPhotoOutputTextFile.textProperty(),
                    textfieldHemiPhotoOutputBitmapFile.textProperty(),
                    textfieldPixelNumber.textProperty(),
                    textfieldAzimutsNumber.textProperty(),
                    textfieldZenithsNumber.textProperty()
                }));

        properties.addAll(Arrays.asList(voxelFileCanopyController.getListenedProperties()));

        return properties.toArray(ObservableValue[]::new);
    }

    @Override
    void initValidationSupport() {

        hemiPhotoSimValidationSupport = new ValidationSupport();
        hemiPhotoSimValidationSupport.registerValidator(textfieldPixelNumber, true, Validators.fieldIntegerValidator);
        hemiPhotoSimValidationSupport.registerValidator(textfieldAzimutsNumber, true, Validators.fieldIntegerValidator);
        hemiPhotoSimValidationSupport.registerValidator(textfieldZenithsNumber, true, Validators.fieldIntegerValidator);
        voxelFileCanopyController.registerValidators();
    }

    @Override
    public void loadConfiguration(File file) throws Exception {

        HemiPhotoCfg hemiCfg = new HemiPhotoCfg();
        hemiCfg.read(file);
        HemiParameters hemiParameters = hemiCfg.getParameters();

        voxelFileCanopyController.setVoxelFile(hemiParameters.getVoxelFile(), hemiParameters.getPADVariable());
        voxelFileCanopyController.setLeafAngleDistribution(hemiParameters.getLeafAngleDistribution());
        voxelFileCanopyController.setLeafAngleDistributionParameters(hemiParameters.getLeafAngleDistributionParameters());
        listViewHemiPhotoSensorPositions.getItems().setAll(hemiParameters.getSensorPositions());

        textfieldPixelNumber.setText(String.valueOf(hemiParameters.getPixelNumber()));
        textfieldAzimutsNumber.setText(String.valueOf(hemiParameters.getAzimutsNumber()));
        textfieldZenithsNumber.setText(String.valueOf(hemiParameters.getZenithsNumber()));
        textfieldHemiPhotoOutputTextFile.setText(hemiParameters.getOutputTextFile().getAbsolutePath());
        textfieldHemiPhotoOutputBitmapFile.setText(hemiParameters.getOutputBitmapFile().getAbsolutePath());

    }

    @FXML
    private void onActionButtonOpenHemiPhotoOutputTextFile(ActionEvent event) {

        File selectedFile = directoryChooserSaveHemiPhotoOutputTextFile.showDialog(null);

        if (selectedFile != null) {
            textfieldHemiPhotoOutputTextFile.setText(selectedFile.getAbsolutePath());
            LOGGER.debug("Hemispherical photo output text file opened.");
        }
    }

    @FXML
    private void onActionButtonOpenHemiPhotoOutputBitmapFile(ActionEvent event) {

        File selectedFile = directoryChooserSaveHemiPhotoOutputBitmapFile.showDialog(null);

        if (selectedFile != null) {
            textfieldHemiPhotoOutputBitmapFile.setText(selectedFile.getAbsolutePath());
            LOGGER.debug("Hemispherical photo output bitmap file opened.");
        }

    }

    @FXML
    private void onActionButtonRemovePositionHemiPhoto(ActionEvent event) {

        ObservableList selectedItems = listViewHemiPhotoSensorPositions.getSelectionModel().getSelectedItems();
        listViewHemiPhotoSensorPositions.getItems().removeAll(selectedItems);
        LOGGER.debug("All view hemispherical photo sensor selected.");
    }

    @FXML
    private void onActionButtonAddPositionHemiPhoto(ActionEvent event) {

        File voxelFile = voxelFileCanopyController.getVoxelFile();
        if (null != voxelFile && voxelFile.exists()) {
            positionImporterFrameController.setInitialVoxelFile(voxelFile);
        }

        Stage positionImporterFrame = positionImporterFrameController.getStage();
        positionImporterFrame.show();
        positionImporterFrame.setOnHidden((WindowEvent event1)
                -> {
            listViewHemiPhotoSensorPositions.getItems().addAll(positionImporterFrameController.getPositions());
            LOGGER.debug("Hemispherical photo position(s) added.");
        });
    }

    @Override
    public void saveConfiguration(File file) throws Exception {

        // validation support
        StringBuilder sb = new StringBuilder();
        if (hemiPhotoSimValidationSupport.isInvalid()) {
            hemiPhotoSimValidationSupport.initInitialDecoration();
            hemiPhotoSimValidationSupport.getValidationResult().getErrors().forEach(error -> sb.append("> ").append(error.getText()).append('\n'));
        }
        ValidationSupport voxelFileValidationSuuport = voxelFileCanopyController.getValidationSupport();
        if (voxelFileValidationSuuport.isInvalid()) {
            voxelFileValidationSuuport.initInitialDecoration();
            voxelFileValidationSuuport.getValidationResult().getErrors().forEach(error -> sb.append("> ").append(error.getText()).append('\n'));
        }
        if (!sb.toString().isEmpty()) {
            throw new IOException(sb.toString());
        }

        HemiParameters hemiParameters = new HemiParameters();

        hemiParameters.setPixelNumber(Integer.parseInt(textfieldPixelNumber.getText()));
        hemiParameters.setAzimutsNumber(Integer.parseInt(textfieldAzimutsNumber.getText()));
        hemiParameters.setZenithsNumber(Integer.parseInt(textfieldZenithsNumber.getText()));

        hemiParameters.setVoxelFile(voxelFileCanopyController.getVoxelFile());
        hemiParameters.setPADVariable(voxelFileCanopyController.getPADVariable());
        hemiParameters.setLeafAngleDistribution(voxelFileCanopyController.getLeafAngleDistribution());
        hemiParameters.setLeafAngleDistributionParameters(voxelFileCanopyController.getLeafAngleDistributionParameters());
        hemiParameters.setSensorPositions(listViewHemiPhotoSensorPositions.getItems());

        File outputBitmapFile = new File(textfieldHemiPhotoOutputBitmapFile.getText());
        hemiParameters.setOutputBitmapFile(outputBitmapFile);

        File outputTextFile = new File(textfieldHemiPhotoOutputTextFile.getText());
        hemiParameters.setOutputTextFile(outputTextFile);

        HemiPhotoCfg hemiPhotoCfg = new HemiPhotoCfg();
        hemiPhotoCfg.setParameters(hemiParameters);
        hemiPhotoCfg.write(file);
    }
}
