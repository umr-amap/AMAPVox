/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui.configuration;

import org.amapvox.gui.DateChooserFrameController;
import org.amapvox.gui.TextFieldUtil;
import org.amapvox.gui.Validators;
import org.amapvox.canopy.lightmap.SimulationPeriod;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.amapvox.canopy.lightmap.LightMapCfg;
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
public class LightMapFrameController extends ConfigurationController {

    final Logger LOGGER = Logger.getLogger(LightMapFrameController.class);

    // validation support
    private ValidationSupport lightMapValidationSupport;

    //
    private DateChooserFrameController dateChooserFrameController;
    private ObservableList<SimulationPeriod> simulationPeriods;
    private final SimpleBooleanProperty simulationPeriodsProperty = new SimpleBooleanProperty();

    // FXML import
    @FXML
    private CanopyToolsIOFrameController canopyToolsIOFrameController;
    @FXML
    private SensorPositionsController sensorPositionsController;
    @FXML
    private ComboBox<Integer> comboboxDirectionCount;
    @FXML
    private Button buttonHelpDirectionCount;
    @FXML
    private HelpButtonController buttonHelpDirectionCountController;
    @FXML
    private TextField textfieldLatitudeRadians;
     @FXML
    private Button buttonHelpLatitude;
    @FXML
    private HelpButtonController buttonHelpLatitudeController;
    @FXML
    private TableView<SimulationPeriod> tableViewSimulationPeriods;
     @FXML
    private Button buttonHelpSimulationPeriods;
    @FXML
    private HelpButtonController buttonHelpSimulationPeriodsController;
    @FXML
    private TableColumn<SimulationPeriod, String> tableColumnPeriod;
    @FXML
    private TableColumn<SimulationPeriod, String> tableColumnClearness;
    @FXML
    private TextField textfieldDirectionRotation;
     @FXML
    private Button buttonHelpDirectionRotation;
    @FXML
    private HelpButtonController buttonHelpDirectionRotationController;
    @FXML
    private CheckBox checkboxToricity;
     @FXML
    private Button buttonHelpToricity;
    @FXML
    private HelpButtonController buttonHelpToricityController;

    @Override
    public void initComponents(ResourceBundle rb) {

        dateChooserFrameController = DateChooserFrameController.newInstance();

        textfieldLatitudeRadians.setTextFormatter(TextFieldUtil.createFloatTextFormatter(0.f));
        simulationPeriods = FXCollections.observableArrayList();

        tableViewSimulationPeriods.setItems(simulationPeriods);
        tableViewSimulationPeriods.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableViewSimulationPeriods.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        comboboxDirectionCount.getItems().addAll(1, 6, 16, 46, 136, 406);
        comboboxDirectionCount.getSelectionModel().select(4);

        textfieldDirectionRotation.setTextFormatter(TextFieldUtil.createFloatTextFormatter(0.f));

        tableColumnPeriod.setCellValueFactory((TableColumn.CellDataFeatures<SimulationPeriod, String> param) -> new SimpleStringProperty(param.getValue().toString()));

        tableColumnClearness.setCellValueFactory((TableColumn.CellDataFeatures<SimulationPeriod, String> param) -> new SimpleStringProperty(df.format(param.getValue().getClearnessCoefficient())));
        
        buttonHelpDirectionCount.setOnAction((ActionEvent event) -> {
            buttonHelpDirectionCountController.showHelpDialog(rb.getString("help_lightmap_direction_count"));
        });
        
        buttonHelpDirectionRotation.setOnAction((ActionEvent event) -> {
            buttonHelpDirectionRotationController.showHelpDialog(rb.getString("help_lightmap_direction_rotation"));
        });
        
        buttonHelpToricity.setOnAction((ActionEvent event) -> {
            buttonHelpToricityController.showHelpDialog(rb.getString("help_lightmap_toricity"));
        });
        
        buttonHelpLatitude.setOnAction((ActionEvent event) -> {
            buttonHelpLatitudeController.showHelpDialog(rb.getString("help_lightmap_latitude"));
        });
        
        buttonHelpSimulationPeriods.setOnAction((ActionEvent event) -> {
            buttonHelpSimulationPeriodsController.showHelpDialog(rb.getString("help_lightmap_simulation_periods"));
        });
        
    }

    @Override
    void initValidationSupport() {

        lightMapValidationSupport = new ValidationSupport();

        lightMapValidationSupport.registerValidator(textfieldDirectionRotation, true, Validators.fieldDoubleValidator);

        lightMapValidationSupport.registerValidator(textfieldLatitudeRadians, true, Validators.fieldDoubleValidator);

        canopyToolsIOFrameController.registerValidators();
    }

    @Override
    ObservableValue[] getListenedProperties() {
        List<ObservableValue> properties = new ArrayList();

        properties.addAll(Arrays.asList(
                new ObservableValue[]{
                    comboboxDirectionCount.getSelectionModel().selectedItemProperty(),
                    textfieldDirectionRotation.textProperty(),
                    checkboxToricity.selectedProperty(),
                    textfieldLatitudeRadians.textProperty(),
                    simulationPeriodsProperty
                }
        ));

        properties.addAll(Arrays.asList(canopyToolsIOFrameController.getListenedProperties()));
        properties.addAll(Arrays.asList(sensorPositionsController.getListenedProperties()));

        return properties.toArray(ObservableValue[]::new);
    }

    @Override
    public void saveConfiguration(File file) throws Exception {
        
        LightMapCfg cfg = new LightMapCfg();

        // validation support
        StringBuilder sb = new StringBuilder();
        if (lightMapValidationSupport.isInvalid()) {
            lightMapValidationSupport.initInitialDecoration();
            lightMapValidationSupport.getValidationResult().getErrors().forEach(error -> sb.append("> ").append(error.getText()).append('\n'));
        }
        ValidationSupport voxelFileValidationSuuport = canopyToolsIOFrameController.getValidationSupport();
        if (voxelFileValidationSuuport.isInvalid()) {
            voxelFileValidationSuuport.initInitialDecoration();
            voxelFileValidationSuuport.getValidationResult().getErrors().forEach(error -> sb.append("> ").append(error.getText()).append('\n'));
        }
        if (!sb.toString().isEmpty()) {
            throw new IOException(sb.toString());
        }

        cfg.setVoxelFile(canopyToolsIOFrameController.getVoxelFile());
        cfg.setPADVariable(canopyToolsIOFrameController.getPADVariable());
        cfg.setLeafAngleDistribution(canopyToolsIOFrameController.getLeafAngleDistribution());
        cfg.setLeafAngleDistributionParameters(canopyToolsIOFrameController.getLeafAngleDistributionParameters());
        
        cfg.setOutputDirectory(canopyToolsIOFrameController.getOutputDir());
        cfg.setOutputPrefix(canopyToolsIOFrameController.getOutputPrefix());
        
        cfg.setSensorPositions(sensorPositionsController.getPositions());

        cfg.setDirectionCount(comboboxDirectionCount.getSelectionModel().getSelectedItem());
        cfg.setDirectionRotation(Float.parseFloat(textfieldDirectionRotation.getText()));

        cfg.setLatitudeInDegrees(Float.parseFloat(textfieldLatitudeRadians.getText()));

        cfg.setSimulationPeriods(tableViewSimulationPeriods.getItems());

        cfg.write(file);
    }

    @Override
    public void loadConfiguration(File file) throws Exception {

        LightMapCfg cfg = new LightMapCfg();
        cfg.read(file);

        canopyToolsIOFrameController.setVoxelFile(cfg.getVoxelFile(), cfg.getPADVariable());
        canopyToolsIOFrameController.setLeafAngleDistribution(cfg.getLeafAngleDistribution());
        canopyToolsIOFrameController.setLeafAngleDistributionParameters(cfg.getLeafAngleDistributionParameters());
        
        canopyToolsIOFrameController.setOutputDir(cfg.getOutputDirectory());
        canopyToolsIOFrameController.setOutputPrefix(cfg.getOutputPrefix());
        
        sensorPositionsController.setPositions(cfg.getSensorPositions());

        comboboxDirectionCount.getSelectionModel().select(Integer.valueOf(cfg.getDirectionCount()));
        textfieldDirectionRotation.setText(df.format(cfg.getDirectionRotation()));
        checkboxToricity.setSelected(cfg.isToricity());

        textfieldLatitudeRadians.setText(df.format(cfg.getLatitudeInDegrees()));

        simulationPeriods.clear();
        List<SimulationPeriod> periods = cfg.getSimulationPeriods();
        if (periods != null) {
            simulationPeriods.addAll(periods);
        }
    }

    @FXML
    private void onActionMenuItemSelectAllPeriods(ActionEvent event) {
        tableViewSimulationPeriods.getSelectionModel().selectAll();
        LOGGER.info("All periods seleted.");
    }

    @FXML
    private void onActionMenuItemUnselectAllPeriods(ActionEvent event) {
        tableViewSimulationPeriods.getSelectionModel().clearSelection();
        LOGGER.info("All periods unselected.");
    }

    @FXML
    private void onActionButtonRemovePeriodFromPeriodList(ActionEvent event) {

        tableViewSimulationPeriods.getItems().removeAll(tableViewSimulationPeriods.getSelectionModel().getSelectedItems());
        simulationPeriodsProperty.set(!simulationPeriodsProperty.get());
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
                    simulationPeriods.add(period);
                    simulationPeriodsProperty.set(!simulationPeriodsProperty.get());
                }
            }
        });
        dateChooserFrame.show();
    }

}
