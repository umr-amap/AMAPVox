/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui.configuration;

import org.amapvox.lidar.riegl.RSPReader;
import org.amapvox.commons.javafx.SelectableMenuButton;
import org.amapvox.commons.javafx.SelectableTitledPane;
import org.amapvox.commons.javafx.io.TextFileParserFrameController;
import org.amapvox.commons.javafx.matrix.TransformationFrameController;
import org.amapvox.commons.math.geometry.BoundingBox3D;
import org.amapvox.commons.math.util.MatrixFileParser;
import org.amapvox.commons.spds.PointCloud;
import org.amapvox.commons.util.filter.Filter;
import org.amapvox.commons.util.filter.FloatFilter;
import org.amapvox.commons.util.io.file.CSVFile;
import org.amapvox.commons.util.io.file.FileManager;
import org.amapvox.commons.Matrix;
import org.amapvox.gui.FilterFrameController;
import org.amapvox.gui.HelpButtonController;
import org.amapvox.lidar.gui.LidarProjectExtractor;
import org.amapvox.lidar.gui.MultiScanProjectExtractor;
import org.amapvox.lidar.gui.PTXProjectExtractor;
import org.amapvox.gui.PointcloudFilterController;
import org.amapvox.lidar.gui.RiscanProjectExtractor;
import org.amapvox.gui.TextFieldUtil;
import org.amapvox.gui.TransformationMatrixController;
import org.amapvox.gui.Util;
import org.amapvox.gui.Validators;
import org.amapvox.gui.VoxelSpacePanelController;
import org.amapvox.shot.Shot;
import org.amapvox.shot.filter.ClassifiedPointFilter;
import org.amapvox.shot.filter.DigitalTerrainModelFilter;
import org.amapvox.shot.filter.EchoAttributeFilter;
import org.amapvox.shot.filter.EchoRangeFilter;
import org.amapvox.shot.filter.EchoRankFilter;
import org.amapvox.shot.filter.PointcloudFilter;
import org.amapvox.shot.filter.ShotAttributeFilter;
import org.amapvox.shot.filter.ShotDecimationFilter;
import org.amapvox.voxelisation.LaserSpecification;
import org.amapvox.voxelisation.txt.TxtShotWriter;
import org.amapvox.voxelisation.las.PointsToShot;
import org.amapvox.voxelisation.VoxelizationCfg;
import org.amapvox.voxelisation.output.OutputVariable;
import org.amapvox.lidar.commons.LidarScan;
import org.amapvox.lidar.commons.MultiScanProjectReader;
import org.amapvox.lidar.faro.XYBReader;
import org.amapvox.lidar.leica.ptg.PTGReader;
import org.amapvox.lidar.las.Classification;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import org.apache.log4j.Logger;
import org.controlsfx.dialog.ProgressDialog;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

/**
 * FXML Controller class
 *
 * @author pverley
 */
public class VoxelizationFrameController extends ConfigurationController {

    final Logger logger = Logger.getLogger(VoxelizationFrameController.class);

    /**
     * Validation supports
     */
    private ValidationSupport voxSpaceValidationSupport;
    private ValidationSupport inputValidationSupport;
    private ValidationSupport outputValidationSupport;

    /**
     * Transformation variables
     */
    private Matrix4d popMatrix;
    private Matrix4d sopMatrix;
    private Matrix4d vopMatrix;
    private Matrix4d resultMatrix;
    private FileChooser fileChooserOpenPopMatrixFile;
    private FileChooser fileChooserOpenSopMatrixFile;
    private FileChooser fileChooserOpenVopMatrixFile;
    private File lastFCOpenPopMatrixFile;
    private File lastFCOpenSopMatrixFile;
    private TransformationFrameController transformationFrameController;

    /**
     * Filter variables
     */
    private FileChooser fileChooserOpenDTMFile;
    private FileChooser fileChooserOpenPointCloudFile;
    private File lastFCOpenDTMFile;
    private FilterFrameController filterFrameController;
    private final SimpleBooleanProperty shotFilterProperty = new SimpleBooleanProperty();
    private final SimpleBooleanProperty echoFilterProperty = new SimpleBooleanProperty();

    /**
     * IO variables
     */
    private List<LidarScan> scanItems;
    private final SimpleBooleanProperty lidarScansProperty = new SimpleBooleanProperty();
    private File lastScanFile;
    private FileChooser fileChooserScan;
    private File lastOutputFile;
    private FileChooser fileChooserOutputFile;

    /**
     * LAS variables
     */
    private File lastTrajectoryFile;
    private CSVFile trajectoryFile;
    private FileChooser fileChooserTrajectory;
    private TextFileParserFrameController trajectoryController;

    @Override
    public void initComponents(ResourceBundle rb) {

        // components
        initInputPane(rb);
        initOutputPane(rb);
        initTransformationPane(rb);
        initVoxelSpacePane(rb);
        initFilterPane(rb);
        initWeightingPane(rb);
        initScannerPane(rb);
        initLeafPane(rb);

        initDragGestures();
    }

    @Override
    ObservableValue[] getListenedProperties() {

        List<ObservableValue> properties = new ArrayList();

        properties.addAll(vboxOutputVariables.getChildren().stream()
                .filter(node -> node instanceof SelectableTitledPane)
                .map(node -> ((SelectableTitledPane) node).selectedProperty())
                .collect(Collectors.toList()));

        properties.addAll(listviewClassifications.getItems()
                .stream()
                .map(checkbox -> checkbox.selectedProperty())
                .collect(Collectors.toList()));

        properties.addAll(Arrays.asList(
                new ObservableValue[]{
                    // Input
                    lidarScansProperty,
                    rdbtnLasTrajectory.selectedProperty(),
                    textFieldLasTrajectoryFile.textProperty(),
                    rdbtnLasPosition.selectedProperty(),
                    textFieldLasPositionX.textProperty(),
                    textFieldLasPositionY.textProperty(),
                    textFieldLasPositionZ.textProperty(),
                    checkboxLasConsistency.selectedProperty(),
                    textfieldMaxDeviation.textProperty(),
                    rdbtnLasConsistencyWarn.selectedProperty(),
                    rdbtnLasConsistencySilent.selectedProperty(),
                    checkboxLasCollinearity.selectedProperty(),
                    rdbtnLasCollinearityWarn.selectedProperty(),
                    rdbtnLasCollinearitySilent.selectedProperty(),
                    // Output
                    textFieldOutputFile.textProperty(),
                    checkboxVoxelOutput.selectedProperty(),
                    checkboxSkipEmptyVoxel.selectedProperty(),
                    spinnerFractionDigits.valueProperty(),
                    comboboxVoxOutputFormat.getSelectionModel().selectedItemProperty(),
                    textFieldSubVoxel.textProperty(),
                    spinnerTrNumError.valueProperty(),
                    spinnerTrNumFallbackError.valueProperty(),
                    textFieldTrNumNRecordMax.textProperty(),
                    textFieldMaxAttenuation.textProperty(),
                    spinnerAttenuationError.valueProperty(),
                    // Transformation
                    checkboxUsePopMatrix.selectedProperty(),
                    checkboxUseSopMatrix.selectedProperty(),
                    checkboxUseVopMatrix.selectedProperty(),
                    //          @TODO matrix changes
                    // Voxel Space
                    checkBoxCubicVoxel.selectedProperty(),
                    textFieldVoxelSizeX.textProperty(),
                    textFieldVoxelSizeY.textProperty(),
                    textFieldVoxelSizeZ.textProperty(),
                    voxelSpaceController.getTextFieldXNumber().textProperty(),
                    voxelSpaceController.getTextFieldYNumber().textProperty(),
                    voxelSpaceController.getTextFieldZNumber().textProperty(),
                    voxelSpaceController.getTextFieldEnterXMin().textProperty(),
                    voxelSpaceController.getTextFieldEnterYMin().textProperty(),
                    voxelSpaceController.getTextFieldEnterZMin().textProperty(),
                    voxelSpaceController.getTextFieldEnterXMax().textProperty(),
                    voxelSpaceController.getTextFieldEnterYMax().textProperty(),
                    voxelSpaceController.getTextFieldEnterZMax().textProperty(),
                    // Filters
                    checkboxUseDTMFilter.selectedProperty(),
                    textfieldDTMPath.textProperty(),
                    textfieldDTMValue.textProperty(),
                    checkboxDTMVOPMatrix.selectedProperty(),
                    checkboxShotDecimation.selectedProperty(),
                    textfieldDecimationFactor.textProperty(),
                    checkboxBlankEchoDiscarded.selectedProperty(),
                    rdbtnShotConsistencyWarn.selectedProperty(),
                    rdbtnShotConsistencySilent.selectedProperty(),
                    checkboxShotAttributeFilter.selectedProperty(),
                    shotFilterProperty,
                    checkboxEmptyShotsFilter.selectedProperty(),
                    checkboxUsePointcloudFilter.selectedProperty(),
                    //          @TODO pointcloud filter
                    checkboxEchoFilterByAttributes.selectedProperty(),
                    echoFilterProperty,
                    checkboxEchoFilterByClass.selectedProperty(),
                    // WEIGHTING
                    checkboxWeightingByRank.selectedProperty(),
                    textAreaWeighting.textProperty(),
                    // LASER
                    comboboxLaserSpecification.getSelectionModel().selectedItemProperty(),
                    checkboxCustomLaserSpecification.selectedProperty(),
                    textFieldBeamDiameterAtExit.textProperty(),
                    textFieldBeamDivergence.textProperty(),
                    checkboxMonoEcho.selectedProperty(),
                    // LEAF
                    textFieldLeafArea.textProperty()
                }));

        return properties.toArray(new ObservableValue[properties.size()]);
    }

    private void initInputPane(ResourceBundle rb) {

        toggleGroupLasPosition = new ToggleGroup();
        rdbtnLasTrajectory.setToggleGroup(toggleGroupLasPosition);
        rdbtnLasPosition.setToggleGroup(toggleGroupLasPosition);
        buttonHelpScans.setOnAction((ActionEvent event) -> {
            buttonHelpScansController.showHelpDialog(rb.getString("help_scans"));
        });
        buttonHelpScannerPosition.setOnAction((ActionEvent event) -> {
            buttonHelpScannerPositionController.showHelpDialog(rb.getString("help_scanner_position"));
        });
        textFieldLasPositionX.setTextFormatter(TextFieldUtil.createFloatTextFormatter(0.f, TextFieldUtil.Sign.BOTH));
        textFieldLasPositionY.setTextFormatter(TextFieldUtil.createFloatTextFormatter(0.f, TextFieldUtil.Sign.BOTH));
        textFieldLasPositionZ.setTextFormatter(TextFieldUtil.createFloatTextFormatter(0.f, TextFieldUtil.Sign.BOTH));

        // Las consistency & collinearity checks
        textfieldMaxDeviation.setTextFormatter(TextFieldUtil.createFloatTextFormatter(0.f, TextFieldUtil.Sign.POSITIVE));
        textfieldMaxDeviation.disableProperty().bind(checkboxLasCollinearity.selectedProperty().not());
        buttonHelpLasConsistency.setOnAction((ActionEvent event)
                -> {
            buttonHelpLasConsistencyController.showHelpDialog(rb.getString("help_las_consistency"));
        });
        buttonHelpLasCollinearity.setOnAction((ActionEvent event)
                -> {
            buttonHelpLasCollinearityController.showHelpDialog(rb.getString("help_las_collinearity"));
        });
        toggleGroupLasConsistency = new ToggleGroup();
        rdbtnLasConsistencyWarn.setToggleGroup(toggleGroupLasConsistency);
        rdbtnLasConsistencySilent.setToggleGroup(toggleGroupLasConsistency);
        toggleGroupLasCollinearity = new ToggleGroup();
        rdbtnLasCollinearityWarn.setToggleGroup(toggleGroupLasCollinearity);
        rdbtnLasCollinearitySilent.setToggleGroup(toggleGroupLasCollinearity);

        fileChooserScan = new FileChooser();
        fileChooserScan.setTitle("Open input file");
        fileChooserScan.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("RXP Files (*.rxp)", "*.rxp"),
                new FileChooser.ExtensionFilter("RSP Files", "*.rsp"),
                new FileChooser.ExtensionFilter("PTX files", "*.ptx"),
                new FileChooser.ExtensionFilter("PTG files", "*.ptg"),
                new FileChooser.ExtensionFilter("XYB files", "*.xyb"),
                new FileChooser.ExtensionFilter("Shot files (*.sht)", "*.sht"),
                new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"),
                new FileChooser.ExtensionFilter("Las Files (*.las or *.laz)", "*.las", "*.laz"));

        fileChooserTrajectory = new FileChooser();
        fileChooserTrajectory.setTitle("Open trajectory file");
        fileChooserTrajectory.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));

        fileChooserOpenDTMFile = new FileChooser();
        fileChooserOpenDTMFile.setTitle("Choose DTM file");
        fileChooserOpenDTMFile.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("DTM Files (*.asc)", "*.asc"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        buttonHelpDTM.setOnAction((ActionEvent event)
                -> {
            buttonHelpDTMController.showHelpDialog(rb.getString("help_dtm"));
        });

        trajectoryController = TextFileParserFrameController.newInstance();

    }

    private void initOutputPane(ResourceBundle resourceBundle) {

        BooleanBinding voxelOutputDisabled = checkboxVoxelOutput.selectedProperty().not();
        spinnerFractionDigits.disableProperty().bind(voxelOutputDisabled);
        comboboxVoxOutputFormat.disableProperty().bind(voxelOutputDisabled);

        // generate output variable controler
        for (OutputVariable output : OutputVariable.values()) {
            if (output.isCoordinateVariable() || output.isDeprecated()) {
                continue;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Short name: ").append(output.getShortName());
            sb.append(", units: ").append(output.getUnits());

            Node content = null;
            switch (output) {
                case EXPLORATION_RATE:
                    vboxOutputVariables.getChildren().remove(hboxSubVoxel);
                    content = hboxSubVoxel;
                    break;
                case ESTIMATED_TRANSMITTANCE:
                    vboxOutputVariables.getChildren().remove(vboxTrNumEstim);
                    content = vboxTrNumEstim;
                    break;
                case ATTENUATION_PPL_MLE:
                    vboxOutputVariables.getChildren().remove(vboxAttenuation);
                    content = vboxAttenuation;
                    break;
            }
            SelectableTitledPane stp = new SelectableTitledPane(output.getLongName(), content, new Insets(0, 0, 5, -20));
            stp.setTooltip(new Tooltip(sb.toString()));
            vboxOutputVariables.getChildren().add(stp);
        }
        // selector
        selectorOutputVariables.setOnActionAll(event -> setOutputVariablesSelected(true));
        selectorOutputVariables.setOnActionNone(event -> setOutputVariablesSelected(false));
        selectorOutputVariables.addMenuItem("Default", event -> Arrays.asList(OutputVariable.values()).forEach(output -> getOutputCheckBox(output).setSelected(output.isEnabledByDefault())));
        ChangeListener<Boolean> selectedListener = (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            int nselected = (int) Arrays.asList(OutputVariable.values()).stream()
                    .filter(output -> getOutputCheckBox(output).isSelected())
                    .count();
            int noutput = vboxOutputVariables.getChildren().size();
            if (0 == nselected) {
                selectorOutputVariables.setIndeterminate(false);
                selectorOutputVariables.setSelected(false);
            } else if (nselected < noutput) {
                selectorOutputVariables.setIndeterminate(true);
            } else {
                selectorOutputVariables.setIndeterminate(false);
                selectorOutputVariables.setSelected(true);
            }
        };
        vboxOutputVariables.getChildren().stream()
                .filter(node -> (node instanceof SelectableTitledPane))
                .forEach(node -> {
                    ((SelectableTitledPane) node).selectedProperty().addListener(selectedListener);
                });
        selectorOutputVariables.getMenuItem("Default").fire();

        comboboxVoxOutputFormat.getItems().setAll(VoxelizationCfg.VoxelsFormat.values());
        comboboxVoxOutputFormat.getSelectionModel().select(VoxelizationCfg.VoxelsFormat.VOXEL);

        spinnerFractionDigits.disableProperty().bind(voxelOutputDisabled.or(comboboxVoxOutputFormat.getSelectionModel().selectedItemProperty().isNotEqualTo(VoxelizationCfg.VoxelsFormat.VOXEL)));
        SpinnerValueFactory<Integer> fractionDigitsFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 16, 7, 1);
        spinnerFractionDigits.setValueFactory(fractionDigitsFactory);

        helpButtonFractionDigits.setOnAction((ActionEvent event)
                -> {
            helpButtonFractionDigitsController.showHelpDialog(resourceBundle.getString("help_fraction_digits"));
        });

        helpButtonOutputFormat.setOnAction((ActionEvent event)
                -> {
            helpButtonOutputFormatController.showHelpDialog(resourceBundle.getString("help_output_format"));
        });
        helpButtonSkipEmptyVoxel.setOnAction((ActionEvent event)
                -> {
            helpButtonSkipEmptyVoxelController.showHelpDialog(resourceBundle.getString("help_skip_empty_voxel"));
        });
        checkboxSkipEmptyVoxel.disableProperty().bind(voxelOutputDisabled.or(comboboxVoxOutputFormat.getSelectionModel().selectedItemProperty().isNotEqualTo(VoxelizationCfg.VoxelsFormat.VOXEL)));

        // Numerical estimation of the transmittance
        // general help
        helpButtonTrNumEstim.setOnAction((ActionEvent event)
                -> {
            helpButtonTrNumEstimController.showHelpDialog(resourceBundle.getString("help_transmittance_numerical_estimation"));
        });
        // error spinner
        spinnerTrNumError.disableProperty().bind(getOutputCheckBox(OutputVariable.ESTIMATED_TRANSMITTANCE).selectedProperty().not());
        SpinnerValueFactory<Integer> spinnerFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 14, 7);
        spinnerTrNumError.setValueFactory(spinnerFactory);
        helpButtonTrNumError.setOnAction((ActionEvent event) -> {
            helpButtonTrNumErrorController.showHelpDialog(resourceBundle.getString("help_transmittance_error"));
        });
        // fallback error spinner
        spinnerTrNumFallbackError.disableProperty().bind(getOutputCheckBox(OutputVariable.ESTIMATED_TRANSMITTANCE).selectedProperty().not());
        spinnerFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 3, 2);
        spinnerTrNumFallbackError.setValueFactory(spinnerFactory);
        helpButtonTrNumFallbackError.setOnAction((ActionEvent event) -> {
            helpButtonTrNumFallbackErrorController.showHelpDialog(resourceBundle.getString("help_transmittance_fallback_error"));
        });
        // transmittance max records
        textFieldTrNumNRecordMax.disableProperty().bind(getOutputCheckBox(OutputVariable.ESTIMATED_TRANSMITTANCE).selectedProperty().not());
        textFieldTrNumNRecordMax.setTextFormatter(TextFieldUtil.createIntegerTextFormatter(0, TextFieldUtil.Sign.POSITIVE));
        helpButtonTrNumNRecordMax.setOnAction((ActionEvent event) -> {
            helpButtonTrNumNRecordMaxController.showHelpDialog(resourceBundle.getString("help_transmittance_nrecordmax"));
        });

        // Numerical estimation of the attenuation
        textFieldMaxAttenuation.disableProperty().bind(getOutputCheckBox(OutputVariable.ATTENUATION_PPL_MLE).selectedProperty().not());
        textFieldMaxAttenuation.setTextFormatter(TextFieldUtil.createFloatTextFormatter(20.f, TextFieldUtil.Sign.POSITIVE));
        helpButtonMaxAttenuation.setOnAction((ActionEvent event) -> {
            helpButtonMaxAttenuationController.showHelpDialog(resourceBundle.getString("help_max_attenuation"));
        });
        spinnerAttenuationError.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 14, 7));
        helpButtonAttenuationError.setOnAction((ActionEvent event) -> {
            helpButtonAttenuationErrorController.showHelpDialog(resourceBundle.getString("help_attenuation_error"));
        });

        // output variables
        helpButtonOutputVariables.setOnAction((ActionEvent event)
                -> {
            helpButtonOutputVariablesController.showHelpDialog(resourceBundle.getString("help_output_variables"));
        });

        helpButtonAutoBBox.setOnAction((ActionEvent event)
                -> {
            helpButtonAutoBBoxController.showHelpDialog(resourceBundle.getString("help_bbox"));
        });

        buttonHelpEmptyShotsFilter.setOnAction((ActionEvent event)
                -> {
            buttonHelpEmptyShotsFilterController.showHelpDialog(resourceBundle.getString("help_empty_shots_filter"));
        });

        // sub voxel division
        textFieldSubVoxel.setTextFormatter(TextFieldUtil.createIntegerTextFormatter(2, TextFieldUtil.Sign.POSITIVE));
        textFieldSubVoxel.disableProperty().bind(getOutputCheckBox(OutputVariable.EXPLORATION_RATE).selectedProperty().not());
        helpButtonSubVoxel.setOnAction((ActionEvent event)
                -> {
            helpButtonSubVoxelController.showHelpDialog(resourceBundle.getString("help_subvoxel_division"));
        });

        hboxAutomaticBBox.disableProperty().bind(
                labelLidarType.textProperty().isNotEqualTo(VoxelizationCfg.LidarType.LAS.name())
                        .and(labelLidarType.textProperty().isNotEqualTo(VoxelizationCfg.LidarType.LAZ.name())));

        listviewLidarScans.disableProperty().bind(Bindings.isEmpty(listviewLidarScans.getItems()));
        listviewLidarScans.setCellFactory((ListView<LidarScan> p) -> new ListCell<LidarScan>() {
            @Override
            protected void updateItem(LidarScan value, boolean empty) {
                super.updateItem(value, empty);
                if (null != value) {
                    setText(value.getFile().getAbsolutePath());
                }
            }
        });

        fileChooserOutputFile = new FileChooser();
        fileChooserOutputFile.getExtensionFilters().addAll(
                new ExtensionFilter("Voxel files", "*.vox"),
                new ExtensionFilter("NetCDF files", "*.nc"),
                new ExtensionFilter("All Files", "*.*"));
        fileChooserOutputFile.setTitle("Choose output file");
    }

    private void initTransformationPane(ResourceBundle resourceBundle) {

        transformationFrameController = TransformationFrameController.newInstance();

        fileChooserOpenPopMatrixFile = new FileChooser();
        fileChooserOpenPopMatrixFile.setTitle("Choose matrix file");
        fileChooserOpenPopMatrixFile.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Text Files  (*.txt)", "*.txt"));

        fileChooserOpenSopMatrixFile = new FileChooser();
        fileChooserOpenSopMatrixFile.setTitle("Choose matrix file");
        fileChooserOpenSopMatrixFile.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));

        fileChooserOpenVopMatrixFile = new FileChooser();
        fileChooserOpenVopMatrixFile.setTitle("Choose matrix file");
        fileChooserOpenVopMatrixFile.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));

        onActionButtonResetToIdentity(null);

        vopMatrixController.setDisable(true);
        checkboxUseVopMatrix.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
                -> {
            buttonSetVOPMatrix.setDisable(!newValue);
            vopMatrixController.setDisable(!newValue);
        });

        popMatrixController.setDisable(true);
        checkboxUsePopMatrix.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
                -> {
            checkBoxUseDefaultPopMatrix.setDisable(!newValue);
            buttonOpenPopMatrixFile.setDisable(!newValue);
            popMatrixController.setDisable(!newValue);
        });

        sopMatrixController.setDisable(true);
        checkboxUseSopMatrix.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
                -> {
            checkBoxUseDefaultSopMatrix.setDisable(!newValue);
            buttonOpenSopMatrixFile.setDisable(!newValue);
            comboboxSingleScan.setDisable(!newValue);
            sopMatrixController.setDisable(!newValue);
            if (!newValue) {
                comboboxSingleScan.getSelectionModel().clearSelection();
            }
        });

        comboboxSingleScan.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends LidarScan> observable, LidarScan oldValue, LidarScan newValue)
                -> {
            if (newValue != null) {
                sopMatrix = newValue.getMatrix();
                updateResultMatrix();
            }
        });
        comboboxSingleScan.itemsProperty().bind(listviewLidarScans.itemsProperty());

        disablePopMatrixChoice(false);
        disableSopMatrixChoice(false);
    }

    private void initVoxelSpacePane(ResourceBundle resourceBundle) {

        voxelSpaceController.getTextFieldEnterXMin().setTextFormatter(TextFieldUtil.createFloatTextFormatter(0.f));
        voxelSpaceController.getTextFieldEnterXMax().setTextFormatter(TextFieldUtil.createFloatTextFormatter(0.f));
        voxelSpaceController.getTextFieldEnterYMin().setTextFormatter(TextFieldUtil.createFloatTextFormatter(0.f));
        voxelSpaceController.getTextFieldEnterYMax().setTextFormatter(TextFieldUtil.createFloatTextFormatter(0.f));
        voxelSpaceController.getTextFieldEnterZMin().setTextFormatter(TextFieldUtil.createFloatTextFormatter(0.f));
        voxelSpaceController.getTextFieldEnterZMax().setTextFormatter(TextFieldUtil.createFloatTextFormatter(0.f));
        voxelSpaceController.getTextFieldXNumber().setTextFormatter(TextFieldUtil.createIntegerTextFormatter(0, TextFieldUtil.Sign.POSITIVE));
        voxelSpaceController.getTextFieldYNumber().setTextFormatter(TextFieldUtil.createIntegerTextFormatter(0, TextFieldUtil.Sign.POSITIVE));
        voxelSpaceController.getTextFieldZNumber().setTextFormatter(TextFieldUtil.createIntegerTextFormatter(0, TextFieldUtil.Sign.POSITIVE));
        // voxel size vx
        textFieldVoxelSizeX.setTextFormatter(TextFieldUtil.createFloatTextFormatter(1.f, TextFieldUtil.Sign.POSITIVE));
        textFieldVoxelSizeX.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (null != newValue && !newValue.isEmpty()) {
                float vx = Float.valueOf(newValue);
                voxelSpaceController.setResolution(new Point3f(
                        vx,
                        checkBoxCubicVoxel.isSelected() ? vx : Float.valueOf(textFieldVoxelSizeY.getText()),
                        checkBoxCubicVoxel.isSelected() ? vx : Float.valueOf(textFieldVoxelSizeZ.getText())
                ));
            }
        });
        textFieldVoxelSizeX.textProperty().addListener(voxelSpaceController.getChangeListener());
        // voxel size vy
        textFieldVoxelSizeY.setTextFormatter(TextFieldUtil.createFloatTextFormatter(1.f, TextFieldUtil.Sign.POSITIVE));
        textFieldVoxelSizeY.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (null != newValue && !newValue.isEmpty()) {
                voxelSpaceController.setResolution(new Point3f(
                        Float.valueOf(textFieldVoxelSizeX.getText()),
                        Float.valueOf(newValue),
                        Float.valueOf(textFieldVoxelSizeZ.getText())
                ));
            }
        });
        textFieldVoxelSizeY.textProperty().addListener(voxelSpaceController.getChangeListener());
        // voxel size vz
        textFieldVoxelSizeZ.setTextFormatter(TextFieldUtil.createFloatTextFormatter(1.f, TextFieldUtil.Sign.POSITIVE));
        textFieldVoxelSizeZ.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (null != newValue && !newValue.isEmpty()) {
                voxelSpaceController.setResolution(new Point3f(
                        Float.valueOf(textFieldVoxelSizeX.getText()),
                        Float.valueOf(textFieldVoxelSizeY.getText()),
                        Float.valueOf(newValue)));
            }
        });
        textFieldVoxelSizeZ.textProperty().addListener(voxelSpaceController.getChangeListener());

        helpButtonCubicVoxel.setOnAction((ActionEvent event) -> {
            helpButtonCubicVoxelController.showHelpDialog(resourceBundle.getString("help_cubic_voxel"));
        });
        onActionCheckBoxCubicVoxel(null);
    }

    private void initFilterPane(ResourceBundle resourceBundle) {

        fileChooserOpenPointCloudFile = new FileChooser();
        fileChooserOpenPointCloudFile.setTitle("Choose point cloud file");
        fileChooserOpenPointCloudFile.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("TXT Files", "*.txt"));

        textfieldDTMValue.setTextFormatter(TextFieldUtil.createFloatTextFormatter(1.f, TextFieldUtil.Sign.POSITIVE));
        textfieldDTMValue.disableProperty().bind(checkboxUseDTMFilter.selectedProperty().not());
        labelDTMValue.disableProperty().bind(checkboxUseDTMFilter.selectedProperty().not());

        checkboxEmptyShotsFilter.disableProperty().bind(labelLidarType.textProperty().isNotEqualTo(VoxelizationCfg.LidarType.RXP.name()));

        BooleanBinding pcfBindig = checkboxUsePointcloudFilter.selectedProperty().not();
        hBoxPointCloudFiltering.disableProperty().bind(pcfBindig);
        checkboxUsePointcloudFilter.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
                -> {
            vBoxPointCloudFiltering.getChildren().stream()
                    .filter(node -> node instanceof PointcloudFilterController)
                    .map(PointcloudFilterController.class::cast)
                    .forEach(pcf -> pcf.disableContent(!newValue));
        });

        // Shot decimation filter
        textfieldDecimationFactor.disableProperty().bind(checkboxShotDecimation.selectedProperty().not());
        textfieldDecimationFactor.setTextFormatter(TextFieldUtil.createRatioTextFormatter(2, 0.f, false));
        buttonHelpShotDecimation.setOnAction((ActionEvent event)
                -> {
            buttonHelpShotDecimationController.showHelpDialog(resourceBundle.getString("help_shot_decimation"));
        });

        // Shot consistency filter
        buttonHelpShotConsistency.setOnAction((ActionEvent event)
                -> {
            buttonHelpShotConsistencyController.showHelpDialog(resourceBundle.getString("help_shot_consistency"));
        });
        toggleGroupShotConsistency = new ToggleGroup();
        rdbtnShotConsistencyWarn.setToggleGroup(toggleGroupShotConsistency);
        rdbtnShotConsistencySilent.setToggleGroup(toggleGroupShotConsistency);

        // Shot attribute filter
        vBoxShotAttributeFilter.disableProperty().bind(checkboxShotAttributeFilter.selectedProperty().not());
        buttonHelpShotAttributeFilter.setOnAction((ActionEvent event)
                -> {
            buttonHelpShotAttributeFilterController.showHelpDialog(resourceBundle.getString("help_shot_angle_filter"));
        });

        addPointcloudFilter(null);

        initEchoFiltering();

        // dtm filter
        buttonHelpDTMFilter.setOnAction((ActionEvent event)
                -> {
            buttonHelpDTMFilterController.showHelpDialog(resourceBundle.getString("help_dtm_filter"));
        });

        filterFrameController = FilterFrameController.newInstance();
    }

    private void initWeightingPane(ResourceBundle resourceBundle) {

        BooleanBinding binding = labelLidarType.textProperty().isEqualTo("LAS").or(labelLidarType.textProperty().isEqualTo("LAZ"));

        checkboxEchoFilterByClass.disableProperty().bind(binding.not());
        vboxEchoFilterByClass.disableProperty().bind(checkboxEchoFilterByClass.selectedProperty().not());
        buttonHelpEchoFilterByAttributes.setOnAction((ActionEvent event)
                -> {
            buttonHelpEchoFilterByAttributesController.showHelpDialog(resourceBundle.getString("help_byattribute_echo_filter"));
        });

        checkboxEchoFilterByAttributes.disableProperty().bind(binding.not());
        vboxEchoFilterByAttribute.disableProperty().bind(checkboxEchoFilterByAttributes.selectedProperty().not());
        buttonHelpByClassEchoFilter.setOnAction((ActionEvent event)
                -> {
            buttonHelpByClassEchoFilterController.showHelpDialog(resourceBundle.getString("help_byclass_echo_filter"));
        });

        vboxWeightingByRank.disableProperty().bind(checkboxWeightingByRank.selectedProperty().not());
        checkboxWeightingByRank.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
                -> {
            if (newValue && textAreaWeighting.getText().isEmpty()) {
                textAreaWeighting.setText(VoxelizationCfg.DEFAULT_ECHOES_WEIGHT.toExternalString());
            }
        });
        helpButtonWeightingByRank.setOnAction((ActionEvent event)
                -> {
            helpButtonWeightingByRankController.showHelpDialog(resourceBundle.getString("help_weighting_rank"));
        });

    }

    private void initScannerPane(ResourceBundle resourceBundle) {

        comboboxLaserSpecification.getItems().addAll(LaserSpecification.getPresets());

        comboboxLaserSpecification.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends LaserSpecification> observable, LaserSpecification oldValue, LaserSpecification newValue)
                -> {
            textFieldBeamDiameterAtExit.setText(df.format(newValue.getBeamDiameterAtExit()));
            textFieldBeamDivergence.setText(df.format(newValue.getBeamDivergence()));
            checkboxMonoEcho.setSelected(newValue.isMonoEcho());
        });

        comboboxLaserSpecification.getSelectionModel().select(LaserSpecification.LMS_Q560);

        comboboxLaserSpecification.disableProperty().bind(checkboxCustomLaserSpecification.selectedProperty());
        textFieldBeamDiameterAtExit.disableProperty().bind(checkboxCustomLaserSpecification.selectedProperty().not());
        textFieldBeamDivergence.disableProperty().bind(checkboxCustomLaserSpecification.selectedProperty().not());
        checkboxMonoEcho.disableProperty().bind(checkboxCustomLaserSpecification.selectedProperty().not());

        textFieldBeamDiameterAtExit.setTextFormatter(TextFieldUtil.createFloatTextFormatter(0.f, TextFieldUtil.Sign.POSITIVE));
        textFieldBeamDivergence.setTextFormatter(TextFieldUtil.createFloatTextFormatter(0.f, TextFieldUtil.Sign.POSITIVE));

    }

    private void initLeafPane(ResourceBundle resourceBundle) {

        // mean leaf size
        textFieldLeafArea.setTextFormatter(TextFieldUtil.createFloatTextFormatter(0.01f, TextFieldUtil.Sign.POSITIVE));
        BooleanBinding selected = getOutputCheckBox(OutputVariable.ATTENUATION_FPL_BIASED_MLE).selectedProperty()
                .or(getOutputCheckBox(OutputVariable.ATTENUATION_FPL_BIAS_CORRECTION).selectedProperty())
                .or(getOutputCheckBox(OutputVariable.ATTENUATION_FPL_UNBIASED_MLE).selectedProperty())
                .or(getOutputCheckBox(OutputVariable.WEIGHTED_EFFECTIVE_FREEPATH).selectedProperty());
        textFieldLeafArea.disableProperty().bind(selected.not());
        helpButtonLeafArea.setOnAction((ActionEvent event)
                -> {
            helpButtonLeafAreaController.showHelpDialog(resourceBundle.getString("help_leaf_area"));
        });
    }

    @Override
    void initValidationSupport() {

        //voxelization fields validation
        voxSpaceValidationSupport = new ValidationSupport();

        voxSpaceValidationSupport.registerValidator(voxelSpaceController.getTextFieldEnterXMin(), false, Validators.fieldDoubleValidator);
        voxSpaceValidationSupport.registerValidator(voxelSpaceController.getTextFieldEnterYMin(), false, Validators.fieldDoubleValidator);
        voxSpaceValidationSupport.registerValidator(voxelSpaceController.getTextFieldEnterZMin(), false, Validators.fieldDoubleValidator);
        voxSpaceValidationSupport.registerValidator(voxelSpaceController.getTextFieldEnterXMax(), false, Validators.fieldDoubleValidator);
        voxSpaceValidationSupport.registerValidator(voxelSpaceController.getTextFieldEnterYMax(), false, Validators.fieldDoubleValidator);
        voxSpaceValidationSupport.registerValidator(voxelSpaceController.getTextFieldEnterZMax(), false, Validators.fieldDoubleValidator);
        voxSpaceValidationSupport.registerValidator(textFieldVoxelSizeX, false, Validators.fieldNonZeroDecimalValidator);
        voxSpaceValidationSupport.registerValidator(textFieldVoxelSizeY, false, Validators.fieldNonZeroDecimalValidator);
        voxSpaceValidationSupport.registerValidator(textFieldVoxelSizeZ, false, Validators.fieldNonZeroDecimalValidator);

        getOutputCheckBox(OutputVariable.EXPLORATION_RATE).selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                voxSpaceValidationSupport.registerValidator(textFieldSubVoxel, true, Validator.createPredicateValidator(isIntegerBiggerThanOne(), "Integer strictly greater than one."));
            } else {
                voxSpaceValidationSupport.registerValidator(textFieldSubVoxel, false, Validators.unregisterValidator);
            }
        });
        checkboxUseDTMFilter.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                voxSpaceValidationSupport.registerValidator(textfieldDTMPath, false, Validators.fileExistValidator("DTM file"));
            } else {
                voxSpaceValidationSupport.registerValidator(textfieldDTMPath, false, Validators.unregisterValidator);
            }
        });

        // input validation support
        inputValidationSupport = new ValidationSupport();
//        labelLidarType.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue)
//                -> {
//            if (newValue.equals(LidarType.LAS.name()) || newValue.equals(LidarType.LAZ.name())) {
//                inputValidationSupport.registerValidator(textFieldLasTrajectoryFile, false, Validators.unregisterValidator);
//            } else {
//                inputValidationSupport.registerValidator(textFieldLasTrajectoryFile, false, Validators.fileExistValidator("LAS trajectory file"));
//
//                 }
//        });

        // output validation support
        outputValidationSupport = new ValidationSupport();
        outputValidationSupport.registerValidator(textFieldOutputFile, Validators.fileValidityValidator("Voxelspace file"));

    }

    private void checkInputSVoxelizationParametersValidity() throws IOException {

        StringBuilder sb = new StringBuilder();

        if (voxSpaceValidationSupport.isInvalid()) {
            voxSpaceValidationSupport.initInitialDecoration();
            voxSpaceValidationSupport.getValidationResult().getErrors().forEach(error -> sb.append("> ").append(error.getText()).append('\n'));
        }

        if (inputValidationSupport.isInvalid()) {
            inputValidationSupport.initInitialDecoration();
            inputValidationSupport.getValidationResult().getErrors().forEach(error -> sb.append("> ").append(error.getText()).append('\n'));
        }

        if (outputValidationSupport.isInvalid()) {
            outputValidationSupport.initInitialDecoration();
            outputValidationSupport.getValidationResult().getErrors().forEach(error -> sb.append("> ").append(error.getText()).append('\n'));
        }

        if (!sb.toString().isEmpty()) {
            throw new IOException(sb.toString());
        }

    }

    @Override
    public void saveConfiguration(File file) throws Exception {

        checkInputSVoxelizationParametersValidity();

        // new configuration
        VoxelizationCfg cfg = new VoxelizationCfg();

        // lidar type
        VoxelizationCfg.LidarType lidarType = VoxelizationCfg.LidarType.valueOf(labelLidarType.getText());
        cfg.setLidarType(lidarType);

        // lidar scans
        cfg.setLidarScans(listviewLidarScans.getItems());

        // dtm
        if (!textfieldDTMPath.getText().isEmpty()) {
            cfg.setDTMFile(new File(textfieldDTMPath.getText()));
        }
        cfg.setDTMUseVopMatrix(checkboxDTMVOPMatrix.isSelected());

        switch (lidarType) {
            case LAS:
            case LAZ:
                // trajectory
                if (rdbtnLasTrajectory.isSelected() && trajectoryFile != null) {
                    CSVFile trajFile = new CSVFile(textFieldLasTrajectoryFile.getText());
                    trajFile.setColumnAssignment(trajectoryFile.getColumnAssignment());
                    trajFile.setColumnSeparator(trajectoryFile.getColumnSeparator());
                    trajFile.setContainsHeader(trajectoryFile.containsHeader());
                    trajFile.setHeaderIndex(trajectoryFile.getHeaderIndex());
                    trajFile.setNbOfLinesToRead(trajectoryFile.getNbOfLinesToRead());
                    trajFile.setNbOfLinesToSkip(trajectoryFile.getNbOfLinesToSkip());
                    cfg.setTrajectoryFile(trajFile);
                }
                // scanner position
                if (rdbtnLasPosition.isSelected()) {
                    cfg.setScannerPosition(new Point3d(
                            Double.valueOf(textFieldLasPositionX.getText()),
                            Double.valueOf(textFieldLasPositionY.getText()),
                            Double.valueOf(textFieldLasPositionZ.getText())));
                }
                // consistency checks
                cfg.setEchoConsistencyCheckEnabled(checkboxLasConsistency.isSelected());
                cfg.setEchoConsistencyWarningEnabled(rdbtnLasConsistencyWarn.isSelected());
                cfg.setCollinearityCheckEnabled(checkboxLasCollinearity.isSelected());
                cfg.setCollinearityWarningEnabled(rdbtnLasCollinearityWarn.isSelected());
                cfg.setCollinearityMaxDeviation(Double.valueOf(textfieldMaxDeviation.getText()));
                break;
            case RXP:
            case RSP:
                // false empty shot filter
                cfg.setEnableEmptyShotsFiltering(checkboxEmptyShotsFilter.isSelected());
                break;

        }

        // output
        cfg.setOutputFile(new File(textFieldOutputFile.getText()));
        cfg.setVoxelsFormat(comboboxVoxOutputFormat.getValue());
        cfg.setFractionDigits(spinnerFractionDigits.getValue());

        // voxel parameters
        setVoxelParametersFromUI(cfg);

        // transformation
        cfg.setUsePopMatrix(checkboxUsePopMatrix.isSelected());
        cfg.setUseSopMatrix(checkboxUseSopMatrix.isSelected());
        cfg.setUseVopMatrix(checkboxUseVopMatrix.isSelected());
        cfg.setPopMatrix(popMatrix);
        cfg.setVopMatrix(vopMatrix);

        // echo classification filter
        if (checkboxEchoFilterByClass.isSelected()) {
            cfg.addEchoFilter(new ClassifiedPointFilter(getListOfClassificationPointToDiscard()));
        }
        if (checkboxUseDTMFilter.isSelected()) {
            cfg.addEchoFilter(new DigitalTerrainModelFilter(Float.valueOf(textfieldDTMValue.getText())));
        }

        List<Filter<Shot>> shotFilters = new ArrayList();
        // shot attribute filter
        if (checkboxShotAttributeFilter.isSelected()) {
            listviewShotFilters.getItems().forEach(filter -> shotFilters.add(new ShotAttributeFilter(filter)));
        }
        // shot decimation
        if (checkboxShotDecimation.isSelected()) {
            shotFilters.add(new ShotDecimationFilter(Float.valueOf(textfieldDecimationFactor.getText())));
        }
        // shot integrity
        shotFilters.add(new EchoRangeFilter(checkboxBlankEchoDiscarded.isSelected(), rdbtnShotConsistencyWarn.isSelected()));

        // add filters
        cfg.setShotFilters(shotFilters);

        // pointcloud filter
        if (checkboxUsePointcloudFilter.isSelected()) {
            vBoxPointCloudFiltering.getChildrenUnmodifiable().stream()
                    .filter(node -> node instanceof PointcloudFilterController)
                    .forEach(node -> {
                        PointcloudFilterController controller = (PointcloudFilterController) node;
                        Matrix4d identity = new Matrix4d();
                        identity.setIdentity();
                        cfg.addEchoFilter(new PointcloudFilter(controller.getCsvFile(),
                                controller.getMarginOfError(),
                                controller.getBehavior(),
                                (controller.isApplyVOPMatrix() && (null != vopMatrix)) ? vopMatrix : identity));
                    });
        }

        // echo filtering by attribute
        listviewEchoFilters.getItems().forEach(filter -> cfg.addEchoFilter(new EchoAttributeFilter(filter)));

        cfg.write(file);

    }

    private void setVoxelParametersFromUI(VoxelizationCfg cfg) {

        cfg.setMinCorner(new Point3d(
                Double.valueOf(voxelSpaceController.getTextFieldEnterXMin().getText()),
                Double.valueOf(voxelSpaceController.getTextFieldEnterYMin().getText()),
                Double.valueOf(voxelSpaceController.getTextFieldEnterZMin().getText())));

        cfg.setMaxCorner(new Point3d(
                Double.valueOf(voxelSpaceController.getTextFieldEnterXMax().getText()),
                Double.valueOf(voxelSpaceController.getTextFieldEnterYMax().getText()),
                Double.valueOf(voxelSpaceController.getTextFieldEnterZMax().getText())));

        cfg.setDimension(new Point3i(
                Integer.valueOf(voxelSpaceController.getTextFieldXNumber().getText()),
                Integer.valueOf(voxelSpaceController.getTextFieldYNumber().getText()),
                Integer.valueOf(voxelSpaceController.getTextFieldZNumber().getText())));

        cfg.setVoxelSize(new Point3d(
                Double.valueOf(textFieldVoxelSizeX.getText()),
                Double.valueOf(textFieldVoxelSizeY.getText()),
                Double.valueOf(textFieldVoxelSizeZ.getText())
        ));

        // sub voxel division
        cfg.setSubVoxelSplit(Integer.valueOf(textFieldSubVoxel.getText()));

        // echo weighting
        if (checkboxWeightingByRank.isSelected()) {
            cfg.setEchoesWeightMatrix(
                    Matrix.valueOf(textAreaWeighting.getText()));
        }

        if (checkboxCustomLaserSpecification.isSelected()) {
            try {
                cfg.setLaserSpecification(new LaserSpecification("custom", Double.valueOf(textFieldBeamDiameterAtExit.getText()), Double.valueOf(textFieldBeamDivergence.getText()), checkboxMonoEcho.isSelected()));
            } catch (NumberFormatException ex) {
                Util.showErrorDialog(getStage(),
                        new Exception("Cannot parse laser specification values.", ex), "[Voxelization]");
            }

        } else {
            cfg.setLaserSpecification(comboboxLaserSpecification.getSelectionModel().getSelectedItem());
        }

        // mean leaf size
        cfg.setMeanLeafArea(Double.valueOf(textFieldLeafArea.getText()));

        // output variables
        for (OutputVariable output : OutputVariable.values()) {
            cfg.setOutputVariableEnabled(output, getOutputCheckBox(output).isSelected());
        }

        // voxel output enabled
        cfg.setVoxelOutputEnabled(checkboxVoxelOutput.isSelected());

        // skip empty voxels in output file
        cfg.setSkipEmptyVoxel(checkboxSkipEmptyVoxel.isSelected());

        // parameters for numerical estimation of the transmittance
        int ndigit = (int) spinnerTrNumError.getValue();
        cfg.setTrNumEstimError(Math.pow(10, -ndigit));
        ndigit = (int) spinnerTrNumFallbackError.getValue();
        cfg.setTrNumEstimFallbackError(Math.pow(10, -ndigit));
        cfg.setNTrRecordMax(Integer.valueOf(textFieldTrNumNRecordMax.getText()));

        // maximal attenuation
        cfg.setMaxAttenuation(Double.valueOf(textFieldMaxAttenuation.getText()));
        ndigit = (int) spinnerAttenuationError.getValue();
        cfg.setAttenuationError(Math.pow(10, -ndigit));
    }

    private List<Integer> getListOfClassificationPointToDiscard() {

        return listviewClassifications.getItems().stream()
                .filter(checkBox -> !checkBox.isSelected())
                .map(checkBox -> Integer.valueOf(checkBox.getText().substring(0, checkBox.getText().indexOf("-") - 1)))
                .collect(Collectors.toList());
    }

    @Override
    public void loadConfiguration(File file) throws Exception {

        VoxelizationCfg cfg = new VoxelizationCfg();
        cfg.read(file);

        // dtm
        if (null != cfg.getDTMFile()) {
            textfieldDTMPath.setText(cfg.getDTMFile().getAbsolutePath());
            checkboxDTMVOPMatrix.setSelected(cfg.isDTMUseVopMatrix());
        }

        // voxel output enabled
        checkboxVoxelOutput.setSelected(cfg.isVoxelOutputEnabled());
        // voxel file output format
        comboboxVoxOutputFormat.getSelectionModel().select(cfg.getVoxelsFormat());
        // number of fraction digits
        spinnerFractionDigits.getValueFactory().setValue(cfg.getDecimalFormat().getMaximumFractionDigits());
        // skip empty voxel
        checkboxSkipEmptyVoxel.setSelected(cfg.skipEmptyVoxel());
        LaserSpecification laserSpecification = cfg.getLaserSpecification();
        if (laserSpecification != null) {
            if (laserSpecification.getName().equals("custom")) {
                checkboxCustomLaserSpecification.setSelected(true);
                textFieldBeamDiameterAtExit.setText(df.format(laserSpecification.getBeamDiameterAtExit()));
                textFieldBeamDivergence.setText(df.format(laserSpecification.getBeamDivergence()));
                checkboxMonoEcho.setSelected(laserSpecification.isMonoEcho());
            } else {
                checkboxCustomLaserSpecification.setSelected(false);
                // look at laser specification presets
                for (LaserSpecification laserSpec : LaserSpecification.getPresets()) {
                    if (laserSpec.isValidName(laserSpecification.getName())) {
                        comboboxLaserSpecification.getSelectionModel().select(laserSpec);
                        break;
                    }
                }
            }
        }
        textFieldVoxelSizeX.setText(df.format(cfg.getVoxelSize().x));
        if (cfg.getVoxelSize().x == cfg.getVoxelSize().y
                && cfg.getVoxelSize().x == cfg.getVoxelSize().z) {
            // cubic voxel
            checkBoxCubicVoxel.fire();
        } else {
            // cuboid voxel
            textFieldVoxelSizeY.setText(df.format(cfg.getVoxelSize().y));
            textFieldVoxelSizeZ.setText(df.format(cfg.getVoxelSize().z));
        }

        // sub voxel division
        textFieldSubVoxel.setText(String.valueOf(cfg.getSubVoxelSplit()));

        checkboxUsePopMatrix.setSelected(cfg.isUsePopMatrix());
        checkboxUseSopMatrix.setSelected(cfg.isUseSopMatrix());
        checkboxUseVopMatrix.setSelected(cfg.isUseVopMatrix());
        // echo filters
        clearPointcloudFiltersPane();
        checkboxEchoFilterByClass.setSelected(false);
        checkboxUsePointcloudFilter.setSelected(false);
        listviewEchoFilters.getItems().clear();
        cfg.getEchoFilters().forEach(filter -> {
            if (filter instanceof EchoAttributeFilter) {
                EchoAttributeFilter f = (EchoAttributeFilter) filter;
                listviewEchoFilters.getItems().add(f.getFilter());
            } else if (filter instanceof ClassifiedPointFilter) {
                checkboxEchoFilterByClass.setSelected(true);
                ((ClassifiedPointFilter) filter).getClasses()
                        .forEach(iclass -> listviewClassifications.getItems().get(iclass).setSelected(false));
            } else if (filter instanceof PointcloudFilter) {
                checkboxUsePointcloudFilter.setSelected(true);
                addPointcloudFilter((PointcloudFilter) filter);
            } else if (filter instanceof DigitalTerrainModelFilter) {
                checkboxUseDTMFilter.setSelected(true);
                textfieldDTMValue.setText(df.format(((DigitalTerrainModelFilter) filter).getMinDistance()));
            }
        });

        // pop
        popMatrix = cfg.getPopMatrix();
        if (popMatrix == null) {
            popMatrix = new Matrix4d();
            popMatrix.setIdentity();
        }
        // vop
        vopMatrix = cfg.getVopMatrix();
        if (vopMatrix == null) {
            vopMatrix = new Matrix4d();
            vopMatrix.setIdentity();
        }
        // sop
        sopMatrix = new Matrix4d();
        sopMatrix.setIdentity();

        updateResultMatrix();
        List<Filter<Shot>> shotFilters = cfg.getShotFilters();
        if (shotFilters != null) {
            listviewShotFilters.getItems().clear();
            checkboxShotAttributeFilter.setSelected(false);
            checkboxShotDecimation.setSelected(false);

            shotFilters.forEach(filter -> {
                if (filter instanceof ShotAttributeFilter) {
                    ShotAttributeFilter f = (ShotAttributeFilter) filter;
                    listviewShotFilters.getItems().add(f.getFilter());
                } else if (filter instanceof ShotDecimationFilter) {
                    ShotDecimationFilter f = (ShotDecimationFilter) filter;
                    checkboxShotDecimation.setSelected(true);
                    textfieldDecimationFactor.setText(df.format(f.getDecimationFactor()));
                } else if (filter instanceof EchoRangeFilter) {
                    EchoRangeFilter f = (EchoRangeFilter) filter;
                    checkboxBlankEchoDiscarded.setSelected(f.isBlankEchoDiscarded());
                    toggleGroupShotConsistency.selectToggle(f.isWarningEnabled() ? rdbtnShotConsistencyWarn : rdbtnShotConsistencySilent);
                }
            });
            if (!listviewShotFilters.getItems().isEmpty()) {
                checkboxShotAttributeFilter.setSelected(true);
            }
        }
        if (null == cfg.getEchoesWeightMatrix()) {
            checkboxWeightingByRank.setSelected(false);
        } else {
            checkboxWeightingByRank.setSelected(true);
            textAreaWeighting.setText(
                    cfg
                            .getEchoesWeightMatrix().toExternalString());
        }

        textFieldLeafArea.setText(df.format(cfg.getMeanLeafArea()));
        spinnerTrNumError.getValueFactory().setValue((int) Math.abs(Math.log10(cfg.getTrNumEstimError())));
        spinnerTrNumFallbackError.getValueFactory().setValue((int) Math.abs(Math.log10(cfg.getTrNumEstimFallbackError())));
        textFieldTrNumNRecordMax.setText(String.valueOf(cfg.getNTrRecordMax()));
        textFieldMaxAttenuation.setText(BigDecimal.valueOf(cfg.getMaxAttenuation()).toPlainString());
        spinnerAttenuationError.getValueFactory().setValue((int) Math.abs(Math.log10(cfg.getAttenuationError())));

        labelLidarType.setText(cfg.getLidarType().name());

        if (cfg.getLidarType() == VoxelizationCfg.LidarType.LAS
                || cfg.getLidarType() == VoxelizationCfg.LidarType.LAZ) {
            if (null != cfg.getTrajectoryFile()) {
                textFieldLasTrajectoryFile.setText(cfg.getTrajectoryFile().getAbsolutePath());
                rdbtnLasTrajectory.setSelected(true);
            }
            trajectoryFile = cfg.getTrajectoryFile();
            checkboxLasConsistency.setSelected(cfg.isEchoConsistencyCheckEnabled());
            toggleGroupLasConsistency.selectToggle(cfg.isEchoConsistencyWarningEnabled()
                    ? rdbtnLasConsistencyWarn
                    : rdbtnLasConsistencySilent);
            checkboxLasCollinearity.setSelected(cfg.isCollinearityCheckEnabled());
            toggleGroupLasCollinearity.selectToggle(cfg.isCollinearityWarningEnabled()
                    ? rdbtnLasCollinearityWarn
                    : rdbtnLasCollinearitySilent);
            textfieldMaxDeviation.setText(BigDecimal.valueOf(cfg.getCollinearityMaxDeviation()).toPlainString());

        }
        if (null != cfg.getScannerPosition()) {
            textFieldLasPositionX.setText(String.valueOf(cfg.getScannerPosition().x));
            textFieldLasPositionY.setText(String.valueOf(cfg.getScannerPosition().y));
            textFieldLasPositionZ.setText(String.valueOf(cfg.getScannerPosition().z));
            rdbtnLasPosition.setSelected(true);
        }

        List<LidarScan> scans = cfg.getLidarScans();
        if (scans != null) {
            scanItems = scans;
            listviewLidarScans.getItems().setAll(scanItems);
        }

        // false empty shot filter
        checkboxEmptyShotsFilter.setSelected(cfg.isEnableEmptyShotsFiltering());

        textFieldOutputFile.setText(cfg.getOutputFile().getAbsolutePath());

        voxelSpaceController.getTextFieldEnterXMin()
                .setText(df.format(cfg.getMinCorner().x));
        voxelSpaceController.getTextFieldEnterYMin()
                .setText(df.format(cfg.getMinCorner().y));
        voxelSpaceController.getTextFieldEnterZMin()
                .setText(df.format(cfg.getMinCorner().z));

        voxelSpaceController.getTextFieldEnterXMax()
                .setText(df.format(cfg.getMaxCorner().x));
        voxelSpaceController.getTextFieldEnterYMax()
                .setText(df.format(cfg.getMaxCorner().y));
        voxelSpaceController.getTextFieldEnterZMax()
                .setText(df.format(cfg.getMaxCorner().z));

        voxelSpaceController.getTextFieldXNumber()
                .setText(df.format(cfg.getDimension().x));
        voxelSpaceController.getTextFieldYNumber()
                .setText(df.format(cfg.getDimension().y));
        voxelSpaceController.getTextFieldZNumber()
                .setText(df.format(cfg.getDimension().z));

        // output variables
        for (OutputVariable output
                : OutputVariable.values()) {
            getOutputCheckBox(output).setSelected(cfg.isOutputVariableEnabled(output));
        }

    }

    private void initDragGestures() {

        // textfield with file path
        Util.setDragGestureEvents(listviewLidarScans, null);
        Util.setDragGestureEvents(textFieldLasTrajectoryFile, file -> trajectoryFileChoosed(file));
        Util.setDragGestureEvents(textFieldOutputFile);
        Util.setDragGestureEvents(textfieldDTMPath);

    }

    private SelectableTitledPane getOutputCheckBox(OutputVariable output) {
        for (Node node : vboxOutputVariables.getChildren()) {
            if (node instanceof SelectableTitledPane) {
                SelectableTitledPane stp = (SelectableTitledPane) node;
                if (stp.getText().startsWith(output.getLongName())) {
                    return (SelectableTitledPane) node;
                }
            }
        }
        SelectableTitledPane stp = new SelectableTitledPane("", null, null);
        stp.setSelected(false);
        return stp;
    }

    private void guessLasBoundingBox(File file, boolean quick) {

        if (!Files.exists(file.toPath(), LinkOption.NOFOLLOW_LINKS)) {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("File not found");
            alert.setContentText("The file " + file.getAbsolutePath() + " cannot be found.");

            alert.showAndWait();

        } else if (FileManager.getExtension(file).equals(".las") || FileManager.getExtension(file).equals(".laz")) {

            Matrix4d identityMatrix = new Matrix4d();
            identityMatrix.setIdentity();

            ProgressDialog d;

            Service<Void> service = new Service<Void>() {

                @Override
                protected Task<Void> createTask() {

                    return new Task<Void>() {
                        @Override
                        protected Void call() throws InterruptedException {

                            final BoundingBox3D boundingBox = org.amapvox.commons.Util.getBoundingBoxOfPoints(file, resultMatrix, quick, getListOfClassificationPointToDiscard());

                            Point3d minPoint = boundingBox.min;
                            Point3d maxPoint = boundingBox.max;

                            Platform.runLater(()
                                    -> {
                                voxelSpaceController.getTextFieldEnterXMin().setText(df.format(minPoint.x));
                                voxelSpaceController.getTextFieldEnterYMin().setText(df.format(minPoint.y));
                                voxelSpaceController.getTextFieldEnterZMin().setText(df.format(minPoint.z));

                                voxelSpaceController.getTextFieldEnterXMax().setText(df.format(maxPoint.x));
                                voxelSpaceController.getTextFieldEnterYMax().setText(df.format(maxPoint.y));
                                voxelSpaceController.getTextFieldEnterZMax().setText(df.format(maxPoint.z));
                            });

                            return null;
                        }
                    };

                }
            ;
            };
                
                d = new ProgressDialog(service);
            d.initOwner(getStage());
            d.setHeaderText("Please wait...");
            d.setResizable(true);

            d.show();

            service.start();

        }

    }

    private Predicate<String> isIntegerBiggerThanOne() {
        return (String p) -> {
            try {
                return Integer.valueOf(p) > 1;
            } catch (NumberFormatException ex) {
            }
            return false;
        };
    }

    private void setOutputVariablesSelected(boolean selected) {

        vboxOutputVariables.getChildren().stream()
                .filter(node -> node instanceof SelectableTitledPane)
                .forEach(node -> ((SelectableTitledPane) node).setSelected(selected));
    }

    private void resetMatrices() {

        popMatrix = new Matrix4d();
        popMatrix.setIdentity();
        sopMatrix = new Matrix4d();
        sopMatrix.setIdentity();
        vopMatrix = new Matrix4d();
        vopMatrix.setIdentity();
        resultMatrix = new Matrix4d();
        resultMatrix.setIdentity();
    }

    private void disableSopMatrixChoice(boolean value) {

        checkboxUseSopMatrix.setSelected(!value);
        checkboxUseSopMatrix.setDisable(value);
    }

    private void disablePopMatrixChoice(boolean value) {

        checkboxUsePopMatrix.setSelected(!value);
        checkboxUsePopMatrix.setDisable(value);
    }

    private void updateResultMatrix() {

        resultMatrix = new Matrix4d();
        resultMatrix.setIdentity();

        if (checkboxUseVopMatrix.isSelected() && vopMatrix != null) {
            resultMatrix.mul(vopMatrix);
            vopMatrixController.setMatrix(vopMatrix);
        }

        if (checkboxUsePopMatrix.isSelected() && popMatrix != null) {
            resultMatrix.mul(popMatrix);
            popMatrixController.setMatrix(popMatrix);
        }

        if (checkboxUseSopMatrix.isSelected() && sopMatrix != null) {
            resultMatrix.mul(sopMatrix);
            sopMatrixController.setMatrix(sopMatrix);
        }

        transformationMatrixController.setMatrix(resultMatrix);
    }

    private void addPointcloudFilter(PointcloudFilter f) {

        File initialDirectory = null != lastScanFile
                ? lastScanFile.getParentFile() : null;
        PointcloudFilterController pcfpc = new PointcloudFilterController(
                vBoxPointCloudFiltering,
                getStage(),
                initialDirectory);
        pcfpc.disableContent(!checkboxUsePointcloudFilter.isSelected());
        if (null != f) {
            pcfpc.setCSVFile(f.getPointcloudFile());
            pcfpc.setMarginOfError(f.getPointcloudErrorMargin());
            pcfpc.setBehavior(f.behavior());
            pcfpc.setApplyVOPMatrix(f.isApplyVOPMatrix());
        }
        vBoxPointCloudFiltering.getChildren().add(pcfpc);
    }

    private void clearPointcloudFiltersPane() {

        vBoxPointCloudFiltering.getChildren().clear();
    }

    private CheckBox createSelectedCheckbox(String text) {

        CheckBox c = new CheckBox(text);
        c.setSelected(true);
        return c;
    }

    private void initEchoFiltering() {

        listviewClassifications.getItems().addAll(
                createSelectedCheckbox(Classification.CREATED_NEVER_CLASSIFIED.getValue() + " - "
                        + Classification.CREATED_NEVER_CLASSIFIED.getDescription()),
                createSelectedCheckbox(Classification.UNCLASSIFIED.getValue() + " - "
                        + Classification.UNCLASSIFIED.getDescription()),
                new CheckBox(Classification.GROUND.getValue() + " - "
                        + //by default unselected, ground point will be removed
                        Classification.GROUND.getDescription()),
                createSelectedCheckbox(Classification.LOW_VEGETATION.getValue() + " - "
                        + Classification.LOW_VEGETATION.getDescription()),
                createSelectedCheckbox(Classification.MEDIUM_VEGETATION.getValue() + " - "
                        + Classification.MEDIUM_VEGETATION.getDescription()),
                createSelectedCheckbox(Classification.HIGH_VEGETATION.getValue() + " - "
                        + Classification.HIGH_VEGETATION.getDescription()),
                createSelectedCheckbox(Classification.BUILDING.getValue() + " - "
                        + Classification.BUILDING.getDescription()),
                createSelectedCheckbox(Classification.LOW_POINT.getValue() + " - "
                        + Classification.LOW_POINT.getDescription()),
                createSelectedCheckbox(Classification.MODEL_KEY_POINT.getValue() + " - "
                        + Classification.MODEL_KEY_POINT.getDescription()),
                createSelectedCheckbox(Classification.WATER.getValue() + " - "
                        + Classification.WATER.getDescription()),
                createSelectedCheckbox(Classification.RESERVED_10.getValue() + " - "
                        + Classification.RESERVED_10.getDescription()),
                createSelectedCheckbox(Classification.RESERVED_11.getValue() + " - "
                        + Classification.RESERVED_11.getDescription()),
                createSelectedCheckbox(Classification.OVERLAP_POINTS.getValue() + " - "
                        + Classification.OVERLAP_POINTS.getDescription()));
    }

    private void updateLastFCOpenFiles(File file) {

        lastScanFile = file;
        lastTrajectoryFile = file;
        lastOutputFile = file;
        lastFCOpenPopMatrixFile = file;
        lastFCOpenSopMatrixFile = file;
        lastFCOpenDTMFile = file;
        transformationFrameController.setLastMatrixFile(file);
    }

    public void showMatrixFormatErrorDialog() {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Impossible to parse matrix file");
        alert.setContentText("Matrix file has to look like this: \n\n\t1.0 0.0 0.0 0.0\n\t0.0 1.0 0.0 0.0\n\t0.0 0.0 1.0 0.0\n\t0.0 0.0 0.0 1.0\n");

        alert.showAndWait();
    }

    private void trajectoryFileChoosed(File selectedFile) {

        trajectoryController.setColumnAssignment(true);
        trajectoryController.setColumnAssignmentValues("Ignore", "Easting", "Northing", "Elevation", "Time");

        trajectoryController.setColumnAssignmentDefaultSelectedIndex(0, 1);
        trajectoryController.setColumnAssignmentDefaultSelectedIndex(1, 2);
        trajectoryController.setColumnAssignmentDefaultSelectedIndex(2, 3);
        trajectoryController.setColumnAssignmentDefaultSelectedIndex(3, 4);

        if (trajectoryFile != null) {
            trajectoryController.setHeaderExtractionEnabled(trajectoryFile.containsHeader());
            trajectoryController.setSeparator(trajectoryFile.getColumnSeparator());
            trajectoryFile.setColumnAssignment(trajectoryFile.getColumnAssignment());
        } else {
            trajectoryController.setHeaderExtractionEnabled(true);
            trajectoryController.setSeparator(",");
        }

        try {
            trajectoryController.setTextFile(selectedFile);
        } catch (IOException ex) {
            Util.showErrorDialog(getStage(), ex, "[Voxelization]");
            return;
        }

        Stage trajectoryStage = trajectoryController.getStage();
        trajectoryStage.setOnHidden((WindowEvent event)
                -> {
            trajectoryFile = new CSVFile(selectedFile.getAbsolutePath());
            trajectoryFile.setColumnSeparator(trajectoryController.getSeparator());
            trajectoryFile.setColumnAssignment(trajectoryController.getAssignedColumnsItemsMap());
            trajectoryFile.setNbOfLinesToRead(trajectoryController.getNumberOfLines());
            trajectoryFile.setNbOfLinesToSkip(trajectoryController.getSkipLinesNumber());
            trajectoryFile.setContainsHeader(trajectoryController.getHeaderIndex() != -1);
            trajectoryFile.setHeaderIndex(trajectoryController.getHeaderIndex());

            textFieldLasTrajectoryFile.setText(selectedFile.getAbsolutePath());
        });
        trajectoryStage.show();
    }

    private void scanFileChoosed(File selectedFile) {

        String extension = FileManager.getExtension(selectedFile).substring(1).toUpperCase();
        VoxelizationCfg.LidarType lidarType = VoxelizationCfg.LidarType.valueOf(extension);
        lastScanFile = selectedFile;
        LidarProjectExtractor lidarProjectExtractor;

        if (MultiScanProjectReader.isValid(selectedFile)) {
            try {
                switch (lidarType) {
                    case PTG:
                        lidarProjectExtractor = new MultiScanProjectExtractor(new PTGReader());
                        break;
                    case XYB:
                        lidarProjectExtractor = new MultiScanProjectExtractor(new XYBReader());
                        break;
                    default:
                        throw new IOException("Unsupported file format for multi-scan project");
                }

                lidarProjectExtractor.read(selectedFile);
                lidarProjectExtractor.getFrame().show();

                lidarProjectExtractor.getFrame().setOnHidden((WindowEvent event) -> {
                    final List<LidarScan> selectedScans = lidarProjectExtractor.getController().getSelectedScans();
                    if (null != selectedScans && !selectedScans.isEmpty()) {
                        scanItems = new ArrayList(selectedScans);
                        updateResultMatrix();
                        listviewLidarScans.getItems().setAll(scanItems);
                    }
                });
            } catch (Exception ex) {
                Util.showErrorDialog(getStage(), ex, "[Voxelization]");
            }
        } else {

            switch (lidarType) {

                case RXP:
                case LAS:
                case LAZ:
                case SHT:
                    Matrix4d identity = new Matrix4d();
                    identity.setIdentity();
                    scanItems = new ArrayList();
                    scanItems.add(new LidarScan(selectedFile, identity));
                    listviewLidarScans.getItems().setAll(scanItems);
                    break;

                case RSP:
                    try {
                    RSPReader rsp = new RSPReader(selectedFile);
                    lidarProjectExtractor = new RiscanProjectExtractor();
                    lidarProjectExtractor.read(selectedFile);
                    lidarProjectExtractor.getFrame().show();

                    lidarProjectExtractor.getFrame().setOnHidden((WindowEvent event) -> {
                        final List<LidarScan> selectedScans = lidarProjectExtractor.getController().getSelectedScans();
                        if (null != selectedScans && !selectedScans.isEmpty()) {
                            scanItems = new ArrayList(selectedScans);
                            popMatrix = new Matrix4d(rsp.getPopMatrix());
                            updateResultMatrix();
                            listviewLidarScans.getItems().setAll(scanItems);
                        }
                    });
                } catch (Exception ex) {
                    Util.showErrorDialog(getStage(), ex, "[Voxelization]");
                }
                break;

                case PTG:
                    try {
                    scanItems = new ArrayList();
                    PTGReader ptgReader = new PTGReader();
                    scanItems.add(ptgReader.toLidarScan(selectedFile));
                    listviewLidarScans.getItems().setAll(scanItems);
                } catch (IOException ex) {
                    Util.showErrorDialog(getStage(), ex, "[Voxelization]");
                }
                break;

                case XYB:
                    try {
                    scanItems = new ArrayList();
                    XYBReader ptgReader = new XYBReader();
                    scanItems.add(ptgReader.toLidarScan(selectedFile));
                    listviewLidarScans.getItems().setAll(scanItems);
                } catch (IOException ex) {
                    Util.showErrorDialog(getStage(), ex, "[Voxelization]");
                }
                break;

                case PTX:
                    try {
                    lidarProjectExtractor = new PTXProjectExtractor();
                    lidarProjectExtractor.read(selectedFile);
                    lidarProjectExtractor.getFrame().show();

                    lidarProjectExtractor.getFrame().setOnHidden((WindowEvent event) -> {
                        final List<LidarScan> selectedScans = lidarProjectExtractor.getController().getSelectedScans();
                        if (null != selectedScans && !selectedScans.isEmpty()) {
                            scanItems = new ArrayList(selectedScans);
                            updateResultMatrix();
                            listviewLidarScans.getItems().setAll(scanItems);
                        }
                    });
                } catch (Exception ex) {
                    Util.showErrorDialog(getStage(), ex, "[Voxelization]");
                }
                break;
            }
        }
        lidarScansProperty.setValue(!lidarScansProperty.get());
        labelLidarType.setText(extension);
    }

////////////////////////////////////////////////////////////////////////////////
// FXML / FXML / FXML / FXML / FXML / FXML / FXML / FXML / FXML / FXML / FXML //
////////////////////////////////////////////////////////////////////////////////    
//////////////////
// FXML variables
/////////////////
//
    //////////////
    // INPUT PANE
    /////////////
    //
    @FXML
    private Button buttonHelpScans;
    @FXML
    private HelpButtonController buttonHelpScansController;
    @FXML
    private ListView<LidarScan> listviewLidarScans;
    @FXML
    private Label labelLidarType;

    // LAS trajectory and position
    @FXML
    private HBox hboxLasTrajectory;
    @FXML
    private Button buttonHelpScannerPosition;
    @FXML
    private HelpButtonController buttonHelpScannerPositionController;
    @FXML
    private TextField textFieldLasTrajectoryFile;
    @FXML
    private RadioButton rdbtnLasTrajectory;
    @FXML
    private RadioButton rdbtnLasPosition;
    private ToggleGroup toggleGroupLasPosition;
    @FXML
    private TextField textFieldLasPositionX;
    @FXML
    private TextField textFieldLasPositionY;
    @FXML
    private TextField textFieldLasPositionZ;

    // LAS consistency
    @FXML
    private HBox hboxLasConsistency;
    @FXML
    private CheckBox checkboxLasConsistency;
    @FXML
    private TextField textfieldMaxDeviation;
    @FXML
    private Button buttonHelpLasConsistency;
    @FXML
    private HelpButtonController buttonHelpLasConsistencyController;
    @FXML
    private RadioButton rdbtnLasConsistencyWarn;
    @FXML
    private RadioButton rdbtnLasConsistencySilent;
    private ToggleGroup toggleGroupLasConsistency;

    // LAS collinearity
    @FXML
    private HBox hboxLasCollinearity;
    @FXML
    private CheckBox checkboxLasCollinearity;
    @FXML
    private Button buttonHelpLasCollinearity;
    @FXML
    private HelpButtonController buttonHelpLasCollinearityController;
    @FXML
    private RadioButton rdbtnLasCollinearityWarn;
    @FXML
    private RadioButton rdbtnLasCollinearitySilent;
    private ToggleGroup toggleGroupLasCollinearity;

    // DTM filter
    @FXML
    private Button buttonHelpDTM;
    @FXML
    private HelpButtonController buttonHelpDTMController;
    @FXML
    private TextField textfieldDTMPath;
    @FXML
    private CheckBox checkboxDTMVOPMatrix;

    ///////////////
    // OUTPUT PANE
    //////////////
    //
    @FXML
    private TextField textFieldOutputFile;

    // Output skip empty voxel
    @FXML
    private CheckBox checkboxVoxelOutput;
    @FXML
    private CheckBox checkboxSkipEmptyVoxel;
    @FXML
    private HelpButtonController helpButtonSkipEmptyVoxelController;
    @FXML
    private Button helpButtonSkipEmptyVoxel;

    // Output fraction digits
    @FXML
    private Spinner<Integer> spinnerFractionDigits;
    @FXML
    private Button helpButtonFractionDigits;
    @FXML
    private HelpButtonController helpButtonFractionDigitsController;

    // Output format
    @FXML
    private HBox hboxOutputFormat;
    @FXML
    private ComboBox<VoxelizationCfg.VoxelsFormat> comboboxVoxOutputFormat;
    @FXML
    private HelpButtonController helpButtonOutputFormatController;
    @FXML
    private Button helpButtonOutputFormat;

    // Output variables
    @FXML
    private SelectableMenuButton selectorOutputVariables;
    @FXML
    private Button helpButtonOutputVariables;
    @FXML
    private HelpButtonController helpButtonOutputVariablesController;
    @FXML
    private VBox vboxOutputVariables;

    // Output, variables parameters
    // subvoxel exploration rate
    @FXML
    private HBox hboxSubVoxel;
    @FXML
    private TextField textFieldSubVoxel;
    @FXML
    private Button helpButtonSubVoxel;
    @FXML
    private HelpButtonController helpButtonSubVoxelController;
    // transmittance estimation
    @FXML
    private VBox vboxTrNumEstim;
    @FXML
    private Button helpButtonTrNumEstim;
    @FXML
    private HelpButtonController helpButtonTrNumEstimController;
    @FXML
    private Spinner spinnerTrNumError;
    @FXML
    private Button helpButtonTrNumError;
    @FXML
    private HelpButtonController helpButtonTrNumErrorController;
    @FXML
    private Spinner spinnerTrNumFallbackError;
    @FXML
    private Button helpButtonTrNumFallbackError;
    @FXML
    private HelpButtonController helpButtonTrNumFallbackErrorController;
    @FXML
    private TextField textFieldTrNumNRecordMax;
    @FXML
    private Button helpButtonTrNumNRecordMax;
    @FXML
    private HelpButtonController helpButtonTrNumNRecordMaxController;
    // attenuation
    @FXML
    private VBox vboxAttenuation;
    @FXML
    private TextField textFieldMaxAttenuation;
    @FXML
    private Button helpButtonMaxAttenuation;
    @FXML
    private HelpButtonController helpButtonMaxAttenuationController;
    @FXML
    private Spinner spinnerAttenuationError;
    @FXML
    private Button helpButtonAttenuationError;
    @FXML
    private HelpButtonController helpButtonAttenuationErrorController;

    ///////////////////////
    // TRANSFORMATION PANE
    //////////////////////
    //
    @FXML
    private TransformationMatrixController transformationMatrixController;
    // POP matrix
    @FXML
    private CheckBox checkboxUsePopMatrix;
    @FXML
    private TransformationMatrixController popMatrixController;
    @FXML
    private CheckBox checkBoxUseDefaultPopMatrix;
    @FXML
    private Button buttonOpenPopMatrixFile;
    // SOP matrix
    @FXML
    private CheckBox checkboxUseSopMatrix;
    @FXML
    private TransformationMatrixController sopMatrixController;
    @FXML
    private CheckBox checkBoxUseDefaultSopMatrix;
    @FXML
    private Button buttonOpenSopMatrixFile;
    @FXML
    private ComboBox<LidarScan> comboboxSingleScan;
    // VOP matrix
    @FXML
    private CheckBox checkboxUseVopMatrix;
    @FXML
    private TransformationMatrixController vopMatrixController;
    @FXML
    private Button buttonSetVOPMatrix;

    ////////////////////
    // VOXEL SPACE PANE
    ///////////////////
    //
    // Voxel size
    @FXML
    private CheckBox checkBoxCubicVoxel;
    @FXML
    private Button helpButtonCubicVoxel;
    @FXML
    private HelpButtonController helpButtonCubicVoxelController;
    // Voxel space dimension
    @FXML
    private TextField textFieldVoxelSizeX;
    @FXML
    private Label labelVoxelSizeX;
    @FXML
    private TextField textFieldVoxelSizeY;
    @FXML
    private Label labelVoxelSizeY;
    @FXML
    private TextField textFieldVoxelSizeZ;
    @FXML
    private Label labelVoxelSizeZ;
    // Bounding box
    @FXML
    private HBox hboxAutomaticBBox;
    @FXML
    private Button helpButtonAutoBBox;
    @FXML
    private HelpButtonController helpButtonAutoBBoxController;
    @FXML
    private VoxelSpacePanelController voxelSpaceController;

    ///////////////
    // FILTER PANE
    //////////////
    //
    // DTM filter
    @FXML
    private Button buttonHelpDTMFilter;
    @FXML
    private HelpButtonController buttonHelpDTMFilterController;
    @FXML
    private CheckBox checkboxUseDTMFilter;
    @FXML
    private Label labelDTMValue;
    @FXML
    private TextField textfieldDTMValue;

    // Shot filter
    @FXML
    private CheckBox checkboxShotDecimation;
    @FXML
    private Button buttonHelpShotDecimation;
    @FXML
    private HelpButtonController buttonHelpShotDecimationController;
    @FXML
    private TextField textfieldDecimationFactor;

    // Shot consistency
    @FXML
    private Button buttonHelpShotConsistency;
    @FXML
    private HelpButtonController buttonHelpShotConsistencyController;
    @FXML
    private RadioButton rdbtnShotConsistencyWarn;
    @FXML
    private RadioButton rdbtnShotConsistencySilent;
    ToggleGroup toggleGroupShotConsistency;
    @FXML
    private CheckBox checkboxBlankEchoDiscarded;

    // Shot attribute filter
    @FXML
    private CheckBox checkboxShotAttributeFilter;
    @FXML
    private Button buttonHelpShotAttributeFilter;
    @FXML
    private HelpButtonController buttonHelpShotAttributeFilterController;
    @FXML
    private VBox vBoxShotAttributeFilter;
    @FXML
    private ListView<FloatFilter> listviewShotFilters;

    // False empty shots
    @FXML
    private CheckBox checkboxEmptyShotsFilter;
    @FXML
    private Button buttonHelpEmptyShotsFilter;
    @FXML
    private HelpButtonController buttonHelpEmptyShotsFilterController;

    // Point cloud filter
    @FXML
    private CheckBox checkboxUsePointcloudFilter;
    @FXML
    private HBox hBoxPointCloudFiltering;
    @FXML
    private VBox vBoxPointCloudFiltering;


    // Echo filter by attribute
    @FXML
    private CheckBox checkboxEchoFilterByAttributes;
    @FXML
    private Button buttonHelpEchoFilterByAttributes;
    @FXML
    private HelpButtonController buttonHelpEchoFilterByAttributesController;
    @FXML
    private VBox vboxEchoFilterByAttribute;
    @FXML
    private ListView<FloatFilter> listviewEchoFilters;

    // Echo filter by class
    @FXML
    private CheckBox checkboxEchoFilterByClass;
    @FXML
    private Button buttonHelpByClassEchoFilter;
    @FXML
    private HelpButtonController buttonHelpByClassEchoFilterController;
    @FXML
    private VBox vboxEchoFilterByClass;
    @FXML
    private ListView<CheckBox> listviewClassifications;

    //////////////////
    // WEIGHTING PANE
    /////////////////
    //
    // Weighting by rank
    @FXML
    private CheckBox checkboxWeightingByRank;
    @FXML
    private Button helpButtonWeightingByRank;
    @FXML
    private HelpButtonController helpButtonWeightingByRankController;
    @FXML
    private VBox vboxWeightingByRank;
    @FXML
    private TextArea textAreaWeighting;

    ////////////////
    // SCANNER PANE
    ///////////////
    //
    @FXML
    private ComboBox<LaserSpecification> comboboxLaserSpecification;
    @FXML
    private CheckBox checkboxCustomLaserSpecification;
    @FXML
    private TextField textFieldBeamDiameterAtExit;
    @FXML
    private TextField textFieldBeamDivergence;
    @FXML
    private CheckBox checkboxMonoEcho;

    /////////////
    // LEAF PANE
    ////////////
    //
    // Leaf area
    @FXML
    private TextField textFieldLeafArea;
    @FXML
    private Button helpButtonLeafArea;
    @FXML
    private HelpButtonController helpButtonLeafAreaController;

//////////////////////////////////////////
// FXML functions (alphabetically sorted)   
/////////////////////////////////////////
//
    @FXML
    private void onActionButtonAddEchoFilter(ActionEvent event) {

        Stage filterFrame = filterFrameController.getStage();
        filterFrameController.setFilters("Reflectance", "Amplitude", "Deviation");
        filterFrame.setOnHidden((WindowEvent event1) -> {
            if (filterFrameController.getFilter() != null && filterFrameController.isRequestAdd()) {
                listviewEchoFilters.getItems().addAll(filterFrameController.getFilter());
                echoFilterProperty.setValue(!echoFilterProperty.get());
            }
        });
        filterFrame.show();
    }

    @FXML
    private void onActionButtonAddPointcloudFilter(ActionEvent event) {

        addPointcloudFilter(null);
    }

    @FXML
    private void onActionButtonAddShotFilter(ActionEvent event) {

        Stage filterFrame = filterFrameController.getStage();
        filterFrameController.setFilters("Angle");
        filterFrame.setOnHidden((WindowEvent event1) -> {
            if (filterFrameController.getFilter() != null) {
                listviewShotFilters.getItems().add(filterFrameController.getFilter());
                shotFilterProperty.setValue(!shotFilterProperty.get());
            }
        });
        filterFrame.show();
    }

    @FXML
    private void onActionButtonAutomatic(ActionEvent event) {

        guessLasBoundingBox(scanItems.get(0).getFile(), true);
    }

    @FXML
    private void onActionButtonAutomaticDeepSearch(ActionEvent event) {
        guessLasBoundingBox(scanItems.get(0).getFile(), false);
    }

    @FXML
    private void onActionButtonExportALSLidarShots(ActionEvent event) {

        File alsFile = scanItems.get(0).getFile();

        if (!alsFile.exists()) {
            Util.showErrorDialog(getStage(), new Exception("File does not exist."), "[ALS export]");
            return;
        } else if (!alsFile.isFile()) {
            Util.showErrorDialog(getStage(), new Exception("Input is not a file."), "[ALS export]");
            return;
        } else if (trajectoryFile == null || !trajectoryFile.exists() || !trajectoryFile.isFile()) {
            Util.showErrorDialog(getStage(), new Exception("Invalid trajectory file."), "[ALS export]");
            return;
        }

        FileChooser fc = new FileChooser();
        File selectedFile = fc.showSaveDialog(getStage());

        if (selectedFile == null) {
            return;
        }

        while (!selectedFile.getName().endsWith(".sht")) {

            fc.setInitialFileName(selectedFile.getName() + ".sht");
            fc.setInitialDirectory(new File(selectedFile.getParent()));
            selectedFile = fc.showSaveDialog(getStage());

            if (selectedFile == null) {
                return;
            }
        }

        PointsToShot pts = new PointsToShot(trajectoryFile, null, alsFile, vopMatrix,
                checkboxLasConsistency.isSelected(), rdbtnLasConsistencyWarn.isSelected(),
                checkboxLasCollinearity.isSelected(), rdbtnLasCollinearityWarn.isSelected(),
                Double.valueOf(textfieldMaxDeviation.getText()));
        try {
            pts.init();
            logger.info("Point to shot initialized.");
            new TxtShotWriter(pts).write(selectedFile);
            logger.info("Point to shot saved.");
        } catch (Exception ex) {
            Util.showErrorDialog(getStage(), ex, "[ALS export]");
        }
    }

    @FXML
    private void onActionButtonFillDefaultWeight(ActionEvent event) {
        textAreaWeighting.setText(VoxelizationCfg.DEFAULT_ECHOES_WEIGHT.toExternalString());
    }

    @FXML
    private void onActionButtonGetBoundingBox(ActionEvent event) {

        Matrix4d identity = new Matrix4d();
        identity.setIdentity();

        final Matrix4d transfMatrix = (vopMatrix == null && checkboxUseVopMatrix.isSelected()) ? identity : vopMatrix;

        ObservableList<Node> children = vBoxPointCloudFiltering.getChildren();

        // retrieve list of pointcloud filter controllers
        List<PointcloudFilterController> tempList = children.stream()
                .filter(n -> n instanceof PointcloudFilterController)
                .map(PointcloudFilterController.class::cast)
                .collect(Collectors.toList());

        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws InterruptedException {

                        final BoundingBox3D boundingBox = new BoundingBox3D();

                        int count = 0;

                        for (PointcloudFilterController pane : tempList) {

                            if (pane.getBehavior().equals(Filter.Behavior.RETAIN)) {

                                CSVFile file = pane.getCsvFile();

                                if (Files.exists(file.toPath()) && file.isFile()) {

                                    PointCloud pc = new PointCloud();
                                    try {
                                        pc.readFromFile(file, transfMatrix);
                                    } catch (IOException ex) {

                                    }

                                    BoundingBox3D boundingBox2;
                                    if (count == 0) {
                                        boundingBox2 = pc.getBoundingBox();
                                        boundingBox.min = boundingBox2.min;
                                        boundingBox.max = boundingBox2.max;

                                    } else {
                                        boundingBox2 = pc.getBoundingBox();
                                        boundingBox.keepLargest(boundingBox2);
                                    }
                                    count++;
                                }
                            }
                        }

                        Platform.runLater(()
                                -> {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Information");
                            alert.setHeaderText("Bounding box:");
                            alert.setContentText("Minimum: " + "x: " + boundingBox.min.x + " y: " + boundingBox.min.y + " z: " + boundingBox.min.z + "\n"
                                    + "Maximum: " + "x: " + boundingBox.max.x + " y: " + boundingBox.max.y + " z: " + boundingBox.max.z + "\n\n"
                                    + "Use for voxel space bounding-box?");

                            alert.initModality(Modality.NONE);
                            Optional<ButtonType> answer = alert.showAndWait();
                            if (answer.get() == ButtonType.OK) {

                                voxelSpaceController.getTextFieldEnterXMin().setText(df.format(boundingBox.min.x));
                                voxelSpaceController.getTextFieldEnterYMin().setText(df.format(boundingBox.min.y));
                                voxelSpaceController.getTextFieldEnterZMin().setText(df.format(boundingBox.min.z));

                                voxelSpaceController.getTextFieldEnterXMax().setText(df.format(boundingBox.max.x));
                                voxelSpaceController.getTextFieldEnterYMax().setText(df.format(boundingBox.max.y));
                                voxelSpaceController.getTextFieldEnterZMax().setText(df.format(boundingBox.max.z));
                            }
                        });

                        return null;
                    }
                };
            }
        };

        ProgressDialog d = new ProgressDialog(service);
        d.initOwner(getStage());
        d.show();

        service.start();

    }

    @FXML
    private void onActionButtonOpenDTMFile(ActionEvent event) {

        if (lastFCOpenDTMFile != null) {
            fileChooserOpenDTMFile.setInitialDirectory(lastFCOpenDTMFile.getParentFile());
        } else {
            File f = new File(textfieldDTMPath.getText());
            if (Files.exists(f.toPath())) {
                fileChooserOpenDTMFile.setInitialDirectory(f.getParentFile());
            }
        }

        File selectedFile = fileChooserOpenDTMFile.showOpenDialog(getStage());
        if (selectedFile != null) {
            textfieldDTMPath.setText(selectedFile.getAbsolutePath());
            lastFCOpenDTMFile = selectedFile;
        }
    }

    @FXML
    private void onActionButtonSetScans(ActionEvent event) {

        if (lastScanFile != null) {
            fileChooserScan.setInitialDirectory(lastScanFile.getParentFile());
        }

        File selectedFile = fileChooserScan.showOpenDialog(getStage());
        if (selectedFile != null) {
            scanFileChoosed(selectedFile);
            updateLastFCOpenFiles(selectedFile);
        }
    }

    @FXML
    private void onActionButtonOpenOutputFile(ActionEvent event) {

        if (lastOutputFile != null) {
            fileChooserOutputFile.setInitialDirectory(lastOutputFile.getParentFile());
        }

        File selectedPath = fileChooserOutputFile.showSaveDialog(getStage());
        if (selectedPath != null) {
            lastOutputFile = selectedPath;
            textFieldOutputFile.setText(selectedPath.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonOpenPopMatrixFile(ActionEvent event) {

        if (lastFCOpenPopMatrixFile != null) {
            fileChooserOpenPopMatrixFile.setInitialDirectory(lastFCOpenPopMatrixFile.getParentFile());
        }

        File selectedFile = fileChooserOpenPopMatrixFile.showOpenDialog(getStage());
        if (selectedFile != null) {

            String extension = FileManager.getExtension(selectedFile);
            Matrix4d mat = null;

            switch (extension) {
                case ".rsp":
                    try {
                    RSPReader tempRsp = new RSPReader(selectedFile);
                    mat = new Matrix4d(tempRsp.getPopMatrix());
                } catch (IOException ex) {
                    logger.error("Cannot read rsp project file", ex);
                }
                break;
                default:
                    try {
                    mat = MatrixFileParser.getMatrixFromFile(selectedFile);
                } catch (IOException ex) {
                    logger.error("Cannot read matrix file", ex);
                }
            }

            if (mat != null) {
                popMatrix = mat;
            } else {
                showMatrixFormatErrorDialog();
            }

            updateResultMatrix();

            lastFCOpenPopMatrixFile = selectedFile;
        }
    }

    @FXML
    private void onActionButtonOpenSopMatrixFile(ActionEvent event) {

        if (lastFCOpenSopMatrixFile != null) {
            fileChooserOpenSopMatrixFile.setInitialDirectory(lastFCOpenSopMatrixFile.getParentFile());
        }

        File selectedFile = fileChooserOpenSopMatrixFile.showOpenDialog(getStage());
        if (selectedFile != null) {

            lastFCOpenSopMatrixFile = selectedFile;

            String extension = FileManager.getExtension(selectedFile);
            Matrix4d mat;

            switch (extension) {
                case ".rsp":

                    try {
                    RSPReader tempRsp = new RSPReader(selectedFile);
                    //scan unique
//                    if (comboboxModeTLS.getSelectionModel().getSelectedIndex() == 0) {
//
//                        File scanFile;
//                        if (textFieldInputFileTLS.getText().equals("")) {
//                            scanFile = null;
//                        } else {
//                            scanFile = new File(textFieldInputFileTLS.getText());
//                        }
//
//                        if (scanFile != null && Files.exists(scanFile.toPath())) {
//                            LidarScan rxpScan = tempRsp.getScan(scanFile.getName());
//                            if (rxpScan != null) {
//                                sopMatrix = new Matrix4d(rxpScan.getMatrix());
//                            } else {
//                                Alert alert = new Alert(Alert.AlertType.ERROR);
//                                alert.setTitle("Error");
//                                alert.setHeaderText("Cannot get sop matrix from rsp file");
//                                alert.setContentText("Check rsp file!");
//
//                                alert.showAndWait();
//                            }
//                        } else {
//                            Alert alert = new Alert(Alert.AlertType.ERROR);
//                            alert.setTitle("Error");
//                            alert.setHeaderText("Cannot get sop matrix from rsp file");
//                            alert.setContentText("TLS input file should be a valid rxp file!");
//
//                            alert.showAndWait();
//                        }
//                    }

                } catch (IOException ex) {
                    logger.error("Cannot read rsp project file", ex);
                }

                break;
                default:
                    try {
                    mat = MatrixFileParser.getMatrixFromFile(selectedFile);
                    if (mat != null) {
                        sopMatrix = mat;
                    } else {
                        showMatrixFormatErrorDialog();
                    }
                } catch (IOException ex) {
                    logger.error("Cannot read matrix file", ex);
                }
            }

            updateResultMatrix();
        }

    }

    @FXML
    private void onActionButtonOpenTrajectoryFileALS(ActionEvent event) {

        if (lastTrajectoryFile != null) {
            fileChooserTrajectory.setInitialDirectory(lastTrajectoryFile.getParentFile());
        }

        File selectedFile = fileChooserTrajectory.showOpenDialog(getStage());
        if (selectedFile != null) {
            updateLastFCOpenFiles(selectedFile);
            trajectoryFileChoosed(selectedFile);
        }
    }

    @FXML
    private void onActionButtonRemoveEchoFilter(ActionEvent event) {
        ObservableList<FloatFilter> selectedItems = listviewEchoFilters.getSelectionModel().getSelectedItems();
        listviewEchoFilters.getItems().removeAll(selectedItems);
        echoFilterProperty.setValue(!echoFilterProperty.get());
    }

    @FXML
    private void onActionButtonRemoveShotFilter(ActionEvent event) {
        ObservableList<FloatFilter> selectedItems = listviewShotFilters.getSelectionModel().getSelectedItems();
        listviewShotFilters.getItems().removeAll(selectedItems);
        shotFilterProperty.setValue(!shotFilterProperty.get());
    }

    @FXML
    private void onActionButtonResetToIdentity(ActionEvent event) {
        resetMatrices();
        updateResultMatrix();
    }

    @FXML
    private void onActionButtonSetVOPMatrix(ActionEvent event) {

        transformationFrameController.reset();
        transformationFrameController.fillMatrix(vopMatrix);

        Stage transformationStage = transformationFrameController.getStage();
        transformationStage.setOnHidden((WindowEvent event1)
                -> {
            if (transformationFrameController.isConfirmed()) {

                vopMatrix = transformationFrameController.getMatrix();

                if (vopMatrix == null) {
                    vopMatrix = new Matrix4d();
                    vopMatrix.setIdentity();
                }

                updateResultMatrix();
            }
        });
        transformationStage.show();
    }

    @FXML
    private void onActionCheckBoxCubicVoxel(ActionEvent event) {

        boolean cubicVoxelEnabled = checkBoxCubicVoxel.isSelected();

        textFieldVoxelSizeY.setDisable(cubicVoxelEnabled);
        textFieldVoxelSizeZ.setDisable(cubicVoxelEnabled);

        if (cubicVoxelEnabled) {
            textFieldVoxelSizeY.textProperty().bind(textFieldVoxelSizeX.textProperty());
            textFieldVoxelSizeZ.textProperty().bind(textFieldVoxelSizeX.textProperty());
        } else {
            textFieldVoxelSizeY.textProperty().unbind();
            textFieldVoxelSizeZ.textProperty().unbind();
        }
    }

    @FXML
    private void onActionUpdateTransformationMatrix(ActionEvent event) {
        updateResultMatrix();
    }

}
