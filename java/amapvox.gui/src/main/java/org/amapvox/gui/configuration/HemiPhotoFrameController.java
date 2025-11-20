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
import org.amapvox.canopy.hemiphoto.HemiPhotoCfg;
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
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.vecmath.Point3d;
import org.amapvox.gui.HelpButtonController;
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
    // directory chooser
    private DirectoryChooser directoryChooserHemiPhotoOutputDirectory;
    // position importer
    private PositionImporterFrameController positionImporterFrameController;

    // FXML imports
    @FXML
    private VoxelFileCanopyController voxelFileCanopyController;
    @FXML
    private TextField textfieldHemiPhotoOutputDirectory;
    @FXML
    private TextField textfieldHemiPhotoOutputPrefix;
    @FXML
    private ListView<Point3d> listViewHemiPhotoSensorPositions;
    @FXML
    private SelectableMenuButton selectorHemiPhotoSensor;
    @FXML
    private TextField textfieldPixelNumber;
    @FXML
    private Button buttonHelpPixelNumber;
    @FXML
    private HelpButtonController buttonHelpPixelNumberController;
    @FXML
    private TextField textfieldAzimuthNumber;
    @FXML
    private Button buttonHelpAzimuthNumber;
    @FXML
    private HelpButtonController buttonHelpAzimuthNumberController;
    @FXML
    private TextField textfieldZenithNumber;
    @FXML
    private Button buttonHelpZenithNumber;
    @FXML
    private HelpButtonController buttonHelpZenithNumberController;

    @Override
    public void initComponents(ResourceBundle rb) {

        textfieldPixelNumber.setTextFormatter(TextFieldUtil.createIntegerTextFormatter(800, TextFieldUtil.Sign.POSITIVE));
        textfieldAzimuthNumber.setTextFormatter(TextFieldUtil.createIntegerTextFormatter(36, TextFieldUtil.Sign.POSITIVE));
        textfieldZenithNumber.setTextFormatter(TextFieldUtil.createIntegerTextFormatter(9, TextFieldUtil.Sign.POSITIVE));

        listViewHemiPhotoSensorPositions.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        Util.linkSelectorToList(selectorHemiPhotoSensor, listViewHemiPhotoSensorPositions);

        directoryChooserHemiPhotoOutputDirectory = new DirectoryChooser();
        directoryChooserHemiPhotoOutputDirectory.setTitle("Choose output directory");

        positionImporterFrameController = PositionImporterFrameController.newInstance();

        buttonHelpPixelNumber.setOnAction((ActionEvent event) -> {
            buttonHelpPixelNumberController.showHelpDialog(rb.getString("help_hemiphoto_pixel_number"));
        });
        
        buttonHelpAzimuthNumber.setOnAction((ActionEvent event) -> {
            buttonHelpAzimuthNumberController.showHelpDialog(rb.getString("help_hemiphoto_azimuth_number"));
        });
        
        buttonHelpZenithNumber.setOnAction((ActionEvent event) -> {
            buttonHelpZenithNumberController.showHelpDialog(rb.getString("help_hemiphoto_zenith_number"));
        });
    }

    @Override
    ObservableValue[] getListenedProperties() {

        List<ObservableValue> properties = new ArrayList();

        properties.addAll(Arrays.asList(
                new ObservableValue[]{
                    listViewHemiPhotoSensorPositions.itemsProperty(),
                    textfieldHemiPhotoOutputDirectory.textProperty(),
                    textfieldHemiPhotoOutputPrefix.textProperty(),
                    textfieldPixelNumber.textProperty(),
                    textfieldAzimuthNumber.textProperty(),
                    textfieldZenithNumber.textProperty()
                }));

        properties.addAll(Arrays.asList(voxelFileCanopyController.getListenedProperties()));

        return properties.toArray(ObservableValue[]::new);
    }

    @Override
    void initValidationSupport() {

        hemiPhotoSimValidationSupport = new ValidationSupport();
        hemiPhotoSimValidationSupport.registerValidator(textfieldPixelNumber, true, Validators.fieldIntegerValidator);
        hemiPhotoSimValidationSupport.registerValidator(textfieldAzimuthNumber, true, Validators.fieldIntegerValidator);
        hemiPhotoSimValidationSupport.registerValidator(textfieldZenithNumber, true, Validators.fieldIntegerValidator);
        voxelFileCanopyController.registerValidators();
    }

    @Override
    public void loadConfiguration(File file) throws Exception {

        HemiPhotoCfg hemiPhotoCfg = new HemiPhotoCfg();
        hemiPhotoCfg.read(file);

        voxelFileCanopyController.setVoxelFile(hemiPhotoCfg.getVoxelFile(), hemiPhotoCfg.getPADVariable());
        voxelFileCanopyController.setLeafAngleDistribution(hemiPhotoCfg.getLeafAngleDistribution());
        voxelFileCanopyController.setLeafAngleDistributionParameters(hemiPhotoCfg.getLeafAngleDistributionParameters());
        listViewHemiPhotoSensorPositions.getItems().setAll(hemiPhotoCfg.getSensorPositions());

        textfieldPixelNumber.setText(String.valueOf(hemiPhotoCfg.getPixelNumber()));
        textfieldAzimuthNumber.setText(String.valueOf(hemiPhotoCfg.getAzimutsNumber()));
        textfieldZenithNumber.setText(String.valueOf(hemiPhotoCfg.getZenithsNumber()));
        textfieldHemiPhotoOutputDirectory.setText(hemiPhotoCfg.getOutputDirectory().getAbsolutePath());
        textfieldHemiPhotoOutputPrefix.setText(hemiPhotoCfg.getOutputPrefix());

    }

    @FXML
    private void onActionButtonOpenHemiPhotoOutputDirectory(ActionEvent event) {

        File selectedFile = directoryChooserHemiPhotoOutputDirectory.showDialog(null);

        if (selectedFile != null) {
            textfieldHemiPhotoOutputDirectory.setText(selectedFile.getAbsolutePath());
            LOGGER.debug("Hemispherical Photo output directory choosed");
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

        HemiPhotoCfg hemiPhotoCfg = new HemiPhotoCfg();

        hemiPhotoCfg.setPixelNumber(Integer.parseInt(textfieldPixelNumber.getText()));
        hemiPhotoCfg.setAzimutsNumber(Integer.parseInt(textfieldAzimuthNumber.getText()));
        hemiPhotoCfg.setZenithsNumber(Integer.parseInt(textfieldZenithNumber.getText()));

        hemiPhotoCfg.setVoxelFile(voxelFileCanopyController.getVoxelFile());
        hemiPhotoCfg.setPADVariable(voxelFileCanopyController.getPADVariable());
        hemiPhotoCfg.setLeafAngleDistribution(voxelFileCanopyController.getLeafAngleDistribution());
        hemiPhotoCfg.setLeafAngleDistributionParameters(voxelFileCanopyController.getLeafAngleDistributionParameters());
        hemiPhotoCfg.setSensorPositions(listViewHemiPhotoSensorPositions.getItems());

        File outputDirectory = new File(textfieldHemiPhotoOutputDirectory.getText());
        hemiPhotoCfg.setOutputDirectory(outputDirectory);
        hemiPhotoCfg.setOutputPrefix(textfieldHemiPhotoOutputPrefix.getText());

        hemiPhotoCfg.write(file);
    }
}
