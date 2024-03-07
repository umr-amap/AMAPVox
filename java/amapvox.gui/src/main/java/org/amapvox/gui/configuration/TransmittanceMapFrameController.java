/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui.configuration;

import org.amapvox.commons.javafx.SelectableMenuButton;
import org.amapvox.commons.Configuration;
import org.amapvox.gui.CheckMissingVoxelController;
import org.amapvox.gui.DateChooserFrameController;
import org.amapvox.gui.PositionImporterFrameController;
import org.amapvox.gui.TextFieldUtil;
import org.amapvox.gui.Util;
import org.amapvox.gui.Validators;
import org.amapvox.canopy.transmittance.SimulationPeriod;
import org.amapvox.canopy.transmittance.TransmittanceCfg;
import org.amapvox.canopy.transmittance.TransmittanceParameters;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.vecmath.Point3d;
import org.apache.log4j.Logger;
import org.controlsfx.validation.ValidationSupport;

/**
 * FXML Controller class
 *
 * @author pverley
 */
public class TransmittanceMapFrameController extends ConfigurationController {

    final Logger logger = Logger.getLogger(TransmittanceMapFrameController.class);

    // validation support
    private ValidationSupport transLightMapValidationSupport;
    //
    private PositionImporterFrameController positionImporterFrameController;
    //
    private DateChooserFrameController dateChooserFrameController;
    private ObservableList<SimulationPeriod> data;
    // file choosers
    private File lastFCOpenVoxelFile;
    private File lastFCSaveTransmittanceTextFile;
    private File lastDCSaveTransmittanceBitmapFile;
    private FileChooser fileChooserSaveTransmittanceTextFile;
    private DirectoryChooser directoryChooserSaveTransmittanceBitmapFile;

    // FXML import
    @FXML
    private TextField textfieldVoxelFilePathTransmittance;
    @FXML
    private CheckMissingVoxelController checkMissingVoxelTransmittanceController;
    @FXML
    private TextField textfieldOutputTextFilePath;
    @FXML
    private ComboBox<Integer> comboboxChooseDirectionsNumber;
    @FXML
    private TextField textfieldLatitudeRadians;
    @FXML
    private TextField textfieldOutputBitmapFilePath;
    @FXML
    private CheckBox checkboxGenerateTextFile;
    @FXML
    private CheckBox checkboxGenerateBitmapFile;
    @FXML
    private ListView<Point3d> listViewTransmittanceMapSensorPositions;
    @FXML
    private SelectableMenuButton selectorTransmittanceSensor;
    @FXML
    private HBox hboxGenerateTextFile;
    @FXML
    private TableView<SimulationPeriod> tableViewSimulationPeriods;
    @FXML
    private TableColumn<SimulationPeriod, String> tableColumnPeriod;
    @FXML
    private TableColumn<SimulationPeriod, String> tableColumnClearness;
    @FXML
    private TextField textfieldDirectionRotationTransmittanceMap;
    @FXML
    private CheckBox checkboxTransmittanceMapToricity;
    @FXML
    private HBox hboxGenerateBitmapFiles;

    @Override
    public void initComponents(ResourceBundle rb) {

        fileChooserSaveTransmittanceTextFile = new FileChooser();
        fileChooserSaveTransmittanceTextFile.setTitle("Save text file");

        directoryChooserSaveTransmittanceBitmapFile = new DirectoryChooser();
        directoryChooserSaveTransmittanceBitmapFile.setTitle("Choose output directory");

        dateChooserFrameController = DateChooserFrameController.newInstance();

        positionImporterFrameController = PositionImporterFrameController.newInstance();

        textfieldLatitudeRadians.setTextFormatter(TextFieldUtil.createFloatTextFormatter(0.f));
        data = FXCollections.observableArrayList();

        tableViewSimulationPeriods.setItems(data);
        tableViewSimulationPeriods.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableViewSimulationPeriods.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        comboboxChooseDirectionsNumber.getItems().addAll(1, 6, 16, 46, 136, 406);
        comboboxChooseDirectionsNumber.getSelectionModel().select(4);

        textfieldDirectionRotationTransmittanceMap.setTextFormatter(TextFieldUtil.createFloatTextFormatter(0.f));

        tableColumnPeriod.setCellValueFactory((TableColumn.CellDataFeatures<SimulationPeriod, String> param) -> new SimpleStringProperty(param.getValue().getPeriod().toString()));

        tableColumnClearness.setCellValueFactory((TableColumn.CellDataFeatures<SimulationPeriod, String> param) -> new SimpleStringProperty(df.format(param.getValue().getClearnessCoefficient())));

        hboxGenerateBitmapFiles.disableProperty().bind(checkboxGenerateBitmapFile.selectedProperty().not());
        hboxGenerateTextFile.disableProperty().bind(checkboxGenerateTextFile.selectedProperty().not());

        textfieldVoxelFilePathTransmittance.textProperty().addListener(checkMissingVoxelTransmittanceController);

        listViewTransmittanceMapSensorPositions.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        Util.linkSelectorToList(selectorTransmittanceSensor, listViewTransmittanceMapSensorPositions);

        Util.setDragGestureEvents(textfieldOutputTextFilePath);
        Util.setDragGestureEvents(textfieldOutputBitmapFilePath);
        Util.setDragGestureEvents(textfieldVoxelFilePathTransmittance, Util.isVoxelFile, Util.doNothing);

        // validation support
    }

    @Override
    void initValidationSupport() {

        transLightMapValidationSupport = new ValidationSupport();
        transLightMapValidationSupport.registerValidator(textfieldVoxelFilePathTransmittance, true, Validators.fileExistValidator("Voxel file"));

        transLightMapValidationSupport.registerValidator(textfieldDirectionRotationTransmittanceMap, true, Validators.fieldDoubleValidator);

        checkboxGenerateBitmapFile.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
                -> {
            if (newValue) {
                transLightMapValidationSupport.registerValidator(textfieldOutputBitmapFilePath, true, Validators.directoryValidator("Bitmap output folder"));
            } else {
                //unregister the validator
                transLightMapValidationSupport.registerValidator(textfieldOutputBitmapFilePath, false, Validators.unregisterValidator);
            }
        });

        checkboxGenerateTextFile.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
                -> {
            if (newValue) {
                transLightMapValidationSupport.registerValidator(textfieldOutputTextFilePath, true, Validators.fileValidityValidator("Output file"));
            } else {
                //unregister the validator
                transLightMapValidationSupport.registerValidator(textfieldOutputTextFilePath, false, Validators.unregisterValidator);
            }
        });

        ObservableList<Point3d> content = FXCollections.observableArrayList();
        listViewTransmittanceMapSensorPositions.setItems(content);

        transLightMapValidationSupport.registerValidator(textfieldLatitudeRadians, true, Validators.fieldDoubleValidator);
        //transLightMapValidationSupport.registerValidator(listViewTransmittanceMapSensorPositions, true, emptyListValidator);
        //transLightMapValidationSupport.registerValidator(tableViewSimulationPeriods, true, emptyTableValidator);
    }

    @Override
    ObservableValue[] getListenedProperties() {
        return new ObservableValue[]{
            textfieldVoxelFilePathTransmittance.textProperty(),
            checkboxGenerateTextFile.selectedProperty(),
            textfieldOutputTextFilePath.textProperty(),
            checkboxGenerateBitmapFile.selectedProperty(),
            textfieldOutputBitmapFilePath.textProperty(),
            listViewTransmittanceMapSensorPositions.itemsProperty(),
            comboboxChooseDirectionsNumber.getSelectionModel().selectedItemProperty(),
            textfieldDirectionRotationTransmittanceMap.textProperty(),
            checkboxTransmittanceMapToricity.selectedProperty(),
            textfieldLatitudeRadians.selectedTextProperty(),
            tableViewSimulationPeriods.itemsProperty()
        };
    }

    @Override
    public void saveConfiguration(File file) throws Exception {

        TransmittanceParameters transmParameters = new TransmittanceParameters();

        transmParameters.setInputFile(new File(textfieldVoxelFilePathTransmittance.getText()));
        transmParameters.setGenerateBitmapFile(checkboxGenerateBitmapFile.isSelected());
        transmParameters.setGenerateTextFile(checkboxGenerateTextFile.isSelected());
        transmParameters.setToricity(checkboxTransmittanceMapToricity.isSelected());

        if (checkboxGenerateBitmapFile.isSelected()) {
            transmParameters.setBitmapFile(new File(textfieldOutputBitmapFilePath.getText()));
        }

        if (checkboxGenerateTextFile.isSelected()) {
            transmParameters.setTextFile(new File(textfieldOutputTextFilePath.getText()));
        }

        transmParameters.setDirectionsNumber(comboboxChooseDirectionsNumber.getSelectionModel().getSelectedItem());
        transmParameters.setDirectionsRotation(Float.valueOf(textfieldDirectionRotationTransmittanceMap.getText()));

        transmParameters.setLatitudeInDegrees(Float.valueOf(textfieldLatitudeRadians.getText()));

        transmParameters.setSimulationPeriods(tableViewSimulationPeriods.getItems());

        transmParameters.setPositions(listViewTransmittanceMapSensorPositions.getItems());

        TransmittanceCfg cfg = new TransmittanceCfg();
        cfg.setParameters(transmParameters);
        cfg.write(file);
    }

    @Override
    public void loadConfiguration(File file) throws Exception {

        Configuration trCfg = new TransmittanceCfg();
        trCfg.read(file);
        TransmittanceParameters trParams = ((TransmittanceCfg) trCfg).getParameters();

        textfieldVoxelFilePathTransmittance.setText(trParams.getInputFile().getAbsolutePath());

        comboboxChooseDirectionsNumber.getSelectionModel().select(Integer.valueOf(trParams.getDirectionsNumber()));
        textfieldDirectionRotationTransmittanceMap.setText(df.format(trParams.getDirectionsRotation()));
        checkboxTransmittanceMapToricity.setSelected(trParams.isToricity());

        checkboxGenerateBitmapFile.setSelected(trParams.isGenerateBitmapFile());

        if (trParams.isGenerateBitmapFile() && trParams.getBitmapFolder() != null) {
            textfieldOutputBitmapFilePath.setText(trParams.getBitmapFolder().getAbsolutePath());
        }

        checkboxGenerateTextFile.setSelected(trParams.isGenerateTextFile());

        if (trParams.isGenerateTextFile() && trParams.getTextFile() != null) {
            textfieldOutputTextFilePath.setText(trParams.getTextFile().getAbsolutePath());
        }

        if (trParams.getPositions() != null) {
            listViewTransmittanceMapSensorPositions.getItems().setAll(trParams.getPositions());
        }

        textfieldLatitudeRadians.setText(df.format(trParams.getLatitudeInDegrees()));

        data.clear();

        List<SimulationPeriod> simulationPeriods = trParams.getSimulationPeriods();

        if (simulationPeriods != null) {
            data.addAll(simulationPeriods);
        }
    }

    @FXML
    private void onActionButtonOpenVoxelFileTransmittance(ActionEvent event) {

        if (lastFCOpenVoxelFile != null) {
            Util.FILE_CHOOSER_VOXELFILE.setInitialDirectory(lastFCOpenVoxelFile.getParentFile());
        }

        File selectedFile = Util.FILE_CHOOSER_VOXELFILE.showOpenDialog(null);

        if (selectedFile != null) {

            lastFCOpenVoxelFile = selectedFile;
            textfieldVoxelFilePathTransmittance.setText(selectedFile.getAbsolutePath());
            logger.info("Transmittance voxel file opened.");
        }

    }

    @FXML
    private void onActionButtonOpenOutputTextFile(ActionEvent event) {

        if (lastFCSaveTransmittanceTextFile != null) {
            fileChooserSaveTransmittanceTextFile.setInitialFileName(lastFCSaveTransmittanceTextFile.getName());
            fileChooserSaveTransmittanceTextFile.setInitialDirectory(lastFCSaveTransmittanceTextFile.getParentFile());
        }

        File selectedFile = fileChooserSaveTransmittanceTextFile.showSaveDialog(null);

        if (selectedFile != null) {

            lastFCSaveTransmittanceTextFile = selectedFile;
            textfieldOutputTextFilePath.setText(selectedFile.getAbsolutePath());
            logger.info("Output text file opened.");
        }
    }

    @FXML
    private void onActionButtonOpenOutputBitmapFile(ActionEvent event) {

        if (lastDCSaveTransmittanceBitmapFile != null) {
            directoryChooserSaveTransmittanceBitmapFile.setInitialDirectory(lastDCSaveTransmittanceBitmapFile);
        }

        File selectedFile = directoryChooserSaveTransmittanceBitmapFile.showDialog(null);

        if (selectedFile != null) {

            lastDCSaveTransmittanceBitmapFile = selectedFile;
            textfieldOutputBitmapFilePath.setText(selectedFile.getAbsolutePath());
            logger.info("Output bitman file opened.");
        }
    }

    @FXML
    private void onActionButtonRemovePositionTransmittanceMap(ActionEvent event) {

        ObservableList<Point3d> selectedItems = listViewTransmittanceMapSensorPositions.getSelectionModel().getSelectedItems();

        if (selectedItems.size() == listViewTransmittanceMapSensorPositions.getItems().size()) {
            listViewTransmittanceMapSensorPositions.getItems().clear();
            logger.info("Transmittance position(s) removed.");
        } else {
            listViewTransmittanceMapSensorPositions.getItems().removeAll(selectedItems);
            logger.info("Transmittance position(s) removed.");
        }
    }

    @FXML
    private void onActionButtonAddPositionTransmittanceMap(ActionEvent event) {

        if (!textfieldVoxelFilePathTransmittance.getText().isEmpty()) {
            File voxelFile = new File(textfieldVoxelFilePathTransmittance.getText());
            if (voxelFile.exists()) {
                positionImporterFrameController.setInitialVoxelFile(voxelFile);
            }
        }

        Stage positionImporterFrame = positionImporterFrameController.getStage();
        positionImporterFrame.show();
        positionImporterFrame.setOnHidden((WindowEvent event1)
                -> {
            listViewTransmittanceMapSensorPositions.getItems().addAll(positionImporterFrameController.getPositions());
            logger.info("Transmittance position(s) added.");
        });
    }

    @FXML
    private void onActionMenuItemSelectAllPeriods(ActionEvent event) {
        tableViewSimulationPeriods.getSelectionModel().selectAll();
        logger.info("All periods seleted.");
    }

    @FXML
    private void onActionMenuItemUnselectAllPeriods(ActionEvent event) {
        tableViewSimulationPeriods.getSelectionModel().clearSelection();
        logger.info("All periods unselected.");
    }

    @FXML
    private void onActionButtonRemovePeriodFromPeriodList(ActionEvent event) {

        tableViewSimulationPeriods.getItems().removeAll(tableViewSimulationPeriods.getSelectionModel().getSelectedItems());
        logger.info("Period(s) removed.");
    }

    @FXML
    private void onActionButtonAddPeriodToPeriodList(ActionEvent event) {

        dateChooserFrameController.reset();
        Stage dateChooserFrame = dateChooserFrameController.getStage();
        dateChooserFrame.setOnHidden((WindowEvent event1)
                -> {
            if (dateChooserFrameController.isConfirmed()) {

                SimulationPeriod period = dateChooserFrameController.getDateRange();

                if (period != null) {
                    data.add(period);
                }
            }
        });
        dateChooserFrame.show();
    }

}
