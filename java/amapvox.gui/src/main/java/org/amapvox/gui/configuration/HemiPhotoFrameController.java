/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui.configuration;

import org.amapvox.commons.javafx.SelectableMenuButton;
import org.amapvox.commons.javafx.io.FileChooserContext;
import org.amapvox.gui.FilterFrameController;
import org.amapvox.gui.HelpButtonController;
import org.amapvox.lidar.gui.LidarProjectExtractor;
import org.amapvox.gui.PositionImporterFrameController;
import org.amapvox.lidar.gui.RiscanProjectExtractor;
import org.amapvox.gui.TextFieldUtil;
import org.amapvox.gui.Util;
import org.amapvox.gui.Validators;
import org.amapvox.canopy.hemi.HemiParameters;
import org.amapvox.canopy.hemi.HemiPhotoCfg;
import org.amapvox.lidar.commons.LidarScan;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ContextMenuEvent;
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
    private FileChooserContext fileChooserSaveHemiPhotoOutputTextFile;
    private FileChooserContext fileChooserSaveHemiPhotoOutputBitmapFile;
    private DirectoryChooser directoryChooserSaveHemiPhotoOutputBitmapFile;
    private DirectoryChooser directoryChooserSaveHemiPhotoOutputTextFile;
    // position importer
    private PositionImporterFrameController positionImporterFrameController;
    // filter frame
    private FilterFrameController filterFrameController;

    // FXML imports
    @FXML
    private VoxelFileCanopyController voxelFileCanopyController;
    @FXML
    private RadioButton rdbtnFromScans;
    @FXML
    private RadioButton rdbtnFromPAD;
    @FXML
    private Button helpButtonHemiPhoto;
    @FXML
    private HelpButtonController helpButtonHemiPhotoController;
    @FXML
    private CheckBox checkboxGenerateSectorsTextFileHemiPhoto;
    @FXML
    private ComboBox<String> comboboxHemiPhotoBitmapOutputMode;
    @FXML
    private CheckBox checkboxHemiPhotoGenerateBitmapFile;
    @FXML
    private TextField textfieldHemiPhotoOutputTextFile;
    @FXML
    private TextField textfieldHemiPhotoOutputBitmapFile;
    @FXML
    private TitledPane titledPaneHemiFromScans;
    @FXML
    private TitledPane titledPaneHemiFromPAD;
    @FXML
    private ListView<LidarScan> listViewHemiPhotoScans;
    @FXML
    private SelectableMenuButton selectorHemiPhotoScans;
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

        ToggleGroup hemiPhotoMode = new ToggleGroup();
        rdbtnFromScans.setToggleGroup(hemiPhotoMode);
        rdbtnFromPAD.setToggleGroup(hemiPhotoMode);

        helpButtonHemiPhoto.setOnAction((ActionEvent event)
                -> {
            helpButtonHemiPhotoController.showHelpDialog(rb.getString("help_hemiphoto"));
        });

        comboboxHemiPhotoBitmapOutputMode.getItems().addAll("Pixel", "Color");
        comboboxHemiPhotoBitmapOutputMode.getSelectionModel().selectFirst();

        textfieldPixelNumber.setTextFormatter(TextFieldUtil.createIntegerTextFormatter(800, TextFieldUtil.Sign.POSITIVE));
        textfieldAzimutsNumber.setTextFormatter(TextFieldUtil.createIntegerTextFormatter(36, TextFieldUtil.Sign.POSITIVE));
        textfieldZenithsNumber.setTextFormatter(TextFieldUtil.createIntegerTextFormatter(9, TextFieldUtil.Sign.POSITIVE));

        listViewHemiPhotoSensorPositions.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        Util.linkSelectorToList(selectorHemiPhotoSensor, listViewHemiPhotoSensorPositions);

        filterFrameController = FilterFrameController.newInstance();

        ContextMenu contextMenuLidarScanEdit = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction((ActionEvent event)
                -> {
            filterFrameController.setFilters("Reflectance", "Deviation", "Amplitude");
            Stage filterFrame = filterFrameController.getStage();
            filterFrame.show();
            filterFrame.setOnHidden((WindowEvent event1)
                    -> {
                if (filterFrameController.getFilter() != null) {
                    listViewHemiPhotoScans.getSelectionModel().getSelectedItems().forEach(scan -> scan.addFilter(filterFrameController.getFilter()));
                }
            });
        });
        contextMenuLidarScanEdit.getItems().add(editItem);

        listViewHemiPhotoScans.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listViewHemiPhotoScans.setOnContextMenuRequested((ContextMenuEvent event)
                -> {
            contextMenuLidarScanEdit.show(listViewHemiPhotoScans, event.getScreenX(), event.getScreenY());
        });
        Util.linkSelectorToList(selectorHemiPhotoScans, listViewHemiPhotoScans);

        Util.setDragGestureEvents(textfieldHemiPhotoOutputBitmapFile);
        Util.setDragGestureEvents(textfieldHemiPhotoOutputTextFile);

        fileChooserSaveHemiPhotoOutputBitmapFile = new FileChooserContext("*.png");
        fileChooserSaveHemiPhotoOutputBitmapFile.fc.setTitle("Save bitmap file");

        directoryChooserSaveHemiPhotoOutputBitmapFile = new DirectoryChooser();
        directoryChooserSaveHemiPhotoOutputBitmapFile.setTitle("Choose bitmap files output directory");

        directoryChooserSaveHemiPhotoOutputTextFile = new DirectoryChooser();
        directoryChooserSaveHemiPhotoOutputTextFile.setTitle("Choose text files output directory");

        fileChooserSaveHemiPhotoOutputTextFile = new FileChooserContext();

        positionImporterFrameController = PositionImporterFrameController.newInstance();
    }

    @Override
    ObservableValue[] getListenedProperties() {

        List<ObservableValue> properties = new ArrayList();

        properties.addAll(Arrays.asList(
                new ObservableValue[]{
                    rdbtnFromScans.selectedProperty(),
                    rdbtnFromPAD.selectedProperty(),
                    listViewHemiPhotoScans.itemsProperty(),
                    listViewHemiPhotoSensorPositions.itemsProperty(),
                    checkboxGenerateSectorsTextFileHemiPhoto.selectedProperty(),
                    textfieldHemiPhotoOutputTextFile.textProperty(),
                    checkboxHemiPhotoGenerateBitmapFile.selectedProperty(),
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

        //hemi photo simulation
        hemiPhotoSimValidationSupport = new ValidationSupport();
        hemiPhotoSimValidationSupport.registerValidator(textfieldPixelNumber, true, Validators.fieldIntegerValidator);
        hemiPhotoSimValidationSupport.registerValidator(textfieldAzimutsNumber, true, Validators.fieldIntegerValidator);
        hemiPhotoSimValidationSupport.registerValidator(textfieldZenithsNumber, true, Validators.fieldIntegerValidator);
        /*hemiPhotoSimValidationSupport.registerValidator(textfieldHemiPhotoOutputBitmapFile, true, Validators.fileValidityValidator);
        
        checkboxHemiPhotoGenerateBitmapFile.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    hemiPhotoSimValidationSupport.registerValidator(textfieldHemiPhotoOutputBitmapFile, true, Validators.fileValidityValidator);
                }else{
                    //unregister the validator
                    hemiPhotoSimValidationSupport.registerValidator(textfieldHemiPhotoOutputBitmapFile, false, Validators.unregisterValidator);
                }
            }
        });*/

        rdbtnFromScans.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
                -> {
            if (newValue) {
                //unregister the validators
                voxelFileCanopyController.unregisterValidators();
            }
        });

        rdbtnFromPAD.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
                -> {
            if (newValue) {
                voxelFileCanopyController.registerValidators();
            }
        });
    }

    @Override
    public void loadConfiguration(File file) throws Exception {

        HemiPhotoCfg hemiCfg = new HemiPhotoCfg();
        hemiCfg.read(file);
        HemiParameters hemiParameters = hemiCfg.getParameters();
        rdbtnFromScans.setSelected(hemiParameters.getMode() == HemiParameters.Mode.ECHOS);
        rdbtnFromPAD.setSelected(hemiParameters.getMode() == HemiParameters.Mode.PAD);
        titledPaneHemiFromScans.setExpanded(hemiParameters.getMode() == HemiParameters.Mode.ECHOS);
        titledPaneHemiFromPAD.setExpanded(hemiParameters.getMode() == HemiParameters.Mode.PAD);
        switch (hemiParameters.getMode()) {
            case ECHOS ->
                listViewHemiPhotoScans.getItems().setAll(hemiParameters.getRxpScansList());
            case PAD -> {
                voxelFileCanopyController.setVoxelFile(hemiParameters.getVoxelFile(), hemiParameters.getPADVariable());
                voxelFileCanopyController.setLeafAngleDistribution(hemiParameters.getLeafAngleDistribution());
                voxelFileCanopyController.setLeafAngleDistributionParameters(hemiParameters.getLeafAngleDistributionParameters());
                listViewHemiPhotoSensorPositions.getItems().setAll(hemiParameters.getSensorPositions());
            }
        }
        textfieldPixelNumber.setText(String.valueOf(hemiParameters.getPixelNumber()));
        textfieldAzimutsNumber.setText(String.valueOf(hemiParameters.getAzimutsNumber()));
        textfieldZenithsNumber.setText(String.valueOf(hemiParameters.getZenithsNumber()));
        checkboxGenerateSectorsTextFileHemiPhoto.setSelected(hemiParameters.isGenerateTextFile());
        if (hemiParameters.isGenerateTextFile()) {
            textfieldHemiPhotoOutputTextFile.setText(hemiParameters.getOutputTextFile().getAbsolutePath());
        }
        checkboxHemiPhotoGenerateBitmapFile.setSelected(hemiParameters.isGenerateBitmapFile());
        if (hemiParameters.isGenerateBitmapFile()) {
            comboboxHemiPhotoBitmapOutputMode.getSelectionModel().select(hemiParameters.getBitmapMode().getMode());
            textfieldHemiPhotoOutputBitmapFile.setText(hemiParameters.getOutputBitmapFile().getAbsolutePath());

        }

    }

    @FXML
    private void onActionRdBtnFromScans(ActionEvent event) {
        titledPaneHemiFromScans.setExpanded(true);
        titledPaneHemiFromPAD.setExpanded(false);
    }

    @FXML
    private void onActionRdBtnFromPAD(ActionEvent event) {
        titledPaneHemiFromScans.setExpanded(false);
        titledPaneHemiFromPAD.setExpanded(true);
    }

    @FXML
    private void onActionButtonOpenHemiPhotoOutputTextFile(ActionEvent event) {

        if (rdbtnFromScans.isSelected()) {
            File selectedFile = fileChooserSaveHemiPhotoOutputTextFile.showSaveDialog(null);

            if (selectedFile != null) {
                textfieldHemiPhotoOutputTextFile.setText(selectedFile.getAbsolutePath());
                LOGGER.debug("Hemispherical photo output text file opened.");
            }
        } else {
            File selectedFile = directoryChooserSaveHemiPhotoOutputTextFile.showDialog(null);

            if (selectedFile != null) {
                textfieldHemiPhotoOutputTextFile.setText(selectedFile.getAbsolutePath());
                LOGGER.debug("Hemispherical photo output text file opened.");
            }
        }
    }

    @FXML
    private void onActionButtonOpenHemiPhotoOutputBitmapFile(ActionEvent event) {

        if (rdbtnFromScans.isSelected()) {
            File selectedFile = fileChooserSaveHemiPhotoOutputBitmapFile.showSaveDialog(null);

            if (selectedFile != null) {
                textfieldHemiPhotoOutputBitmapFile.setText(selectedFile.getAbsolutePath());
                LOGGER.debug("Hemispherical photo output bitmap file opened.");
            }
        } else {
            File selectedFile = directoryChooserSaveHemiPhotoOutputBitmapFile.showDialog(null);

            if (selectedFile != null) {
                textfieldHemiPhotoOutputBitmapFile.setText(selectedFile.getAbsolutePath());
                LOGGER.debug("Hemispherical photo output bitmap file opened.");
            }
        }
    }

    @FXML
    private void onActionButtonRemoveScanFromHemiPhotoListView(ActionEvent event) {

        ObservableList<LidarScan> selectedItems = listViewHemiPhotoScans.getSelectionModel().getSelectedItems();
        listViewHemiPhotoScans.getItems().removeAll(selectedItems);
        LOGGER.debug("Scan(s) from hemispherical photo(s) removed.");
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

    @FXML
    private void onActionButtonOpenRspProject(ActionEvent event) {

        File selectedFile = Util.FILE_CHOOSER_TLS.showOpenDialog(null);

        if (selectedFile != null) {

            onTLSInputFileFileChoosed(selectedFile);
            LOGGER.info("RSP project file opened.");
        }
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

        int selectedMode = rdbtnFromScans.isSelected() ? 0 : 1;

        switch (selectedMode) {
            case 0 -> {
                hemiParameters.setMode(HemiParameters.Mode.ECHOS);
                hemiParameters.setRxpScansList(listViewHemiPhotoScans.getItems());
            }
            case 1 -> {
                hemiParameters.setMode(HemiParameters.Mode.PAD);
                hemiParameters.setVoxelFile(voxelFileCanopyController.getVoxelFile());
                hemiParameters.setPADVariable(voxelFileCanopyController.getPADVariable());
                hemiParameters.setLeafAngleDistribution(voxelFileCanopyController.getLeafAngleDistribution());
                hemiParameters.setLeafAngleDistributionParameters(voxelFileCanopyController.getLeafAngleDistributionParameters());
                hemiParameters.setSensorPositions(listViewHemiPhotoSensorPositions.getItems());
            }
        }

        hemiParameters.setGenerateBitmapFile(checkboxHemiPhotoGenerateBitmapFile.isSelected());

        if (checkboxHemiPhotoGenerateBitmapFile.isSelected()) {

            File outputBitmapFile = new File(textfieldHemiPhotoOutputBitmapFile.getText());
            if (selectedMode == 1 && !outputBitmapFile.isDirectory()) {
                throw new Exception("The selected output bitmap directory is not a directory !");
            } else if (selectedMode == 0 && outputBitmapFile.isDirectory()) {
                throw new Exception("The selected output bitmap file is not a file!");
            }

            hemiParameters.setOutputBitmapFile(outputBitmapFile);

            int selectedIndex = comboboxHemiPhotoBitmapOutputMode.getSelectionModel().getSelectedIndex();
            switch (selectedIndex) {
                case 0 ->
                    hemiParameters.setBitmapMode(HemiParameters.BitmapMode.PIXEL);
                case 1 ->
                    hemiParameters.setBitmapMode(HemiParameters.BitmapMode.COLOR);
            }

        }

        hemiParameters.setGenerateTextFile(checkboxGenerateSectorsTextFileHemiPhoto.isSelected());

        if (checkboxGenerateSectorsTextFileHemiPhoto.isSelected()) {

            File outputTextFile = new File(textfieldHemiPhotoOutputTextFile.getText());
            if (selectedMode == 1 && !outputTextFile.isDirectory()) {
                throw new Exception("The selected output text directory is not a directory !");
            } else if (selectedMode == 0 && outputTextFile.isDirectory()) {
                throw new Exception("The selected output text file is not a file!");
            }

            hemiParameters.setOutputTextFile(outputTextFile);
        }

        HemiPhotoCfg hemiPhotoCfg = new HemiPhotoCfg();
        hemiPhotoCfg.setParameters(hemiParameters);
        hemiPhotoCfg.write(file);
    }

    private void onTLSInputFileFileChoosed(File selectedFile) {

        try {
            LidarProjectExtractor lidarProjectExtractor = new RiscanProjectExtractor();
            lidarProjectExtractor.read(selectedFile);
            lidarProjectExtractor.getFrame().show();

            lidarProjectExtractor.getFrame().setOnHidden((WindowEvent event) -> {
                lidarProjectExtractor.getController().getSelectedScans()
                        .forEach(scan -> listViewHemiPhotoScans.getItems().add(scan));
            });
        } catch (Exception ex) {
            Util.showErrorDialog(null, ex, "[Hemispherical photograph]");
        }
    }

}
