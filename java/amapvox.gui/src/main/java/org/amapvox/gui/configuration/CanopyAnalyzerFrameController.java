/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui.configuration;

import org.amapvox.commons.javafx.SelectableMenuButton;
import org.amapvox.commons.javafx.io.FileChooserContext;
import org.amapvox.commons.math.util.SphericalCoordinates;
import org.amapvox.commons.Configuration;
import org.amapvox.gui.CheckMissingVoxelController;
import org.amapvox.gui.PositionImporterFrameController;
import org.amapvox.gui.Util;
import org.amapvox.gui.Validators;
import org.amapvox.gui.ViewCapsSetupFrameController;
import org.amapvox.canopy.lai2xxx.CanopyAnalyzerCfg;
import org.amapvox.canopy.transmittance.TransmittanceCfg;
import org.amapvox.canopy.transmittance.TransmittanceParameters;
import org.amapvox.canopy.lai2xxx.LAI2200;
import org.amapvox.canopy.lai2xxx.LAI2xxx;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;
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
    // file choosers
    private FileChooserContext fileChooserOpenCanopyAnalyserInputFile;
    private FileChooserContext fileChooserSaveCanopyAnalyserOutputFile;
    //
    private PositionImporterFrameController positionImporterFrameController;
    //
    private ViewCapsSetupFrameController viewCapsSetupFrameController;
    // validation support
    private ValidationSupport lai2xxxSimValidationSupport;
    // FXML imports
    @FXML
    private TextField textfieldVoxelFilePathCanopyAnalyzer;
    @FXML
    private CheckMissingVoxelController checkMissingVoxelCanopyController;
    @FXML
    private ComboBox<Integer> comboboxChooseCanopyAnalyzerSampling;
    @FXML
    private TextField textFieldViewCapAngleCanopyAnalyzer;
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
    @FXML
    private ListView<Point3d> listViewCanopyAnalyzerSensorPositions;
    @FXML
    private SelectableMenuButton selectorCanopy;
    @FXML
    private CheckBox checkboxGenerateCanopyAnalyzerTextFile;
    @FXML
    private CheckBox checkboxGenerateLAI2xxxFormat;
    @FXML
    private TextField textfieldOutputCanopyAnalyzerTextFile;
    @FXML
    private ToggleButton toggleButtonLAI2000Choice;
    @FXML
    private ToggleButton toggleButtonLAI2200Choice;

    @Override
    public void initComponents(ResourceBundle rb) {

        fileChooserSaveCanopyAnalyserOutputFile = new FileChooserContext();
        fileChooserOpenCanopyAnalyserInputFile = new FileChooserContext();
        listViewCanopyAnalyzerSensorPositions.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        Util.linkSelectorToList(selectorCanopy, listViewCanopyAnalyzerSensorPositions);

        ToggleGroup virtualMeasuresChoiceGroup = new ToggleGroup();
        toggleButtonLAI2000Choice.setToggleGroup(virtualMeasuresChoiceGroup);
        toggleButtonLAI2200Choice.setToggleGroup(virtualMeasuresChoiceGroup);

        comboboxChooseCanopyAnalyzerSampling.getItems().setAll(500, 4000, 10000);
        comboboxChooseCanopyAnalyzerSampling.getSelectionModel().selectFirst();

        textfieldVoxelFilePathCanopyAnalyzer.textProperty().addListener(checkMissingVoxelCanopyController);

        Util.setDragGestureEvents(textfieldOutputCanopyAnalyzerTextFile);
        Util.setDragGestureEvents(textfieldVoxelFilePathCanopyAnalyzer, Util.isVoxelFile, Util.doNothing);

        viewCapsSetupFrameController = ViewCapsSetupFrameController.newInstance();

        positionImporterFrameController = PositionImporterFrameController.newInstance();

        //lai2200 simulations validation support
        lai2xxxSimValidationSupport = new ValidationSupport();
        lai2xxxSimValidationSupport.registerValidator(textfieldVoxelFilePathCanopyAnalyzer, true, Validators.fileExistValidator("Voxel file"));
        lai2xxxSimValidationSupport.registerValidator(textfieldOutputCanopyAnalyzerTextFile, true, Validators.fileValidityValidator("Output file"));

    }

    @Override
    void initValidationSupport() {
    }

    @Override
    ObservableValue[] getListenedProperties() {
        return new ObservableValue[]{
            textfieldVoxelFilePathCanopyAnalyzer.textProperty(),
            checkboxGenerateCanopyAnalyzerTextFile.selectedProperty(),
            checkboxGenerateLAI2xxxFormat.selectedProperty(),
            textfieldOutputCanopyAnalyzerTextFile.textProperty(),
            toggleButtonLAI2000Choice.selectedProperty(),
            toggleButtonLAI2200Choice.selectedProperty(),
            comboboxChooseCanopyAnalyzerSampling.getSelectionModel().selectedItemProperty(),
            textFieldViewCapAngleCanopyAnalyzer.textProperty(),
            toggleButtonCanopyAnalyzerRingMask1.selectedProperty(),
            toggleButtonCanopyAnalyzerRingMask2.selectedProperty(),
            toggleButtonCanopyAnalyzerRingMask3.selectedProperty(),
            toggleButtonCanopyAnalyzerRingMask4.selectedProperty(),
            toggleButtonCanopyAnalyzerRingMask5.selectedProperty(),
            listViewCanopyAnalyzerSensorPositions.itemsProperty(),};
    }

    @Override
    public void saveConfiguration(File file) throws Exception {

        TransmittanceParameters transmParameters = new TransmittanceParameters();

        transmParameters.setDirectionsNumber(comboboxChooseCanopyAnalyzerSampling.getSelectionModel().getSelectedItem());

        if (toggleButtonLAI2000Choice.isSelected()) {
            transmParameters.setMode(TransmittanceParameters.Mode.LAI2000);
        } else {
            transmParameters.setMode(TransmittanceParameters.Mode.LAI2200);
        }

        transmParameters.setMasks(new boolean[]{
            toggleButtonCanopyAnalyzerRingMask1.isSelected(),
            toggleButtonCanopyAnalyzerRingMask2.isSelected(),
            toggleButtonCanopyAnalyzerRingMask3.isSelected(),
            toggleButtonCanopyAnalyzerRingMask4.isSelected(),
            toggleButtonCanopyAnalyzerRingMask5.isSelected()
        });

        transmParameters.setGenerateLAI2xxxTypeFormat(checkboxGenerateLAI2xxxFormat.isSelected());

        transmParameters.setInputFile(new File(textfieldVoxelFilePathCanopyAnalyzer.getText()));
        transmParameters.setGenerateTextFile(checkboxGenerateCanopyAnalyzerTextFile.isSelected());

        if (checkboxGenerateCanopyAnalyzerTextFile.isSelected()) {
            transmParameters.setTextFile(new File(textfieldOutputCanopyAnalyzerTextFile.getText()));
        }
        if (comboboxChooseCanopyAnalyzerSampling.isEditable()) {
            transmParameters.setDirectionsNumber(Integer.valueOf(comboboxChooseCanopyAnalyzerSampling.getEditor().getText()));
        } else {
            transmParameters.setDirectionsNumber(comboboxChooseCanopyAnalyzerSampling.getSelectionModel().getSelectedItem());
        }

        transmParameters.setPositions(listViewCanopyAnalyzerSensorPositions.getItems());

        CanopyAnalyzerCfg cfg = new CanopyAnalyzerCfg();
        cfg.setParameters(transmParameters);
        cfg.write(file);
    }

    @Override
    public void loadConfiguration(File file) throws Exception {

        Configuration laiCfg = new TransmittanceCfg();
        laiCfg.read(file);
        TransmittanceParameters laiParams = ((TransmittanceCfg) laiCfg).getParameters();

        textfieldVoxelFilePathCanopyAnalyzer.setText(laiParams.getInputFile().getAbsolutePath());

        if (laiParams.getMode().equals(TransmittanceParameters.Mode.LAI2000)) {
            toggleButtonLAI2000Choice.setSelected(true);
        } else if (laiParams.getMode().equals(TransmittanceParameters.Mode.LAI2200)) {
            toggleButtonLAI2200Choice.setSelected(true);
        }
        comboboxChooseCanopyAnalyzerSampling.getSelectionModel().select(Integer.valueOf(laiParams.getDirectionsNumber()));
        boolean[] masks = laiParams.getMasks();
        if (masks != null && masks.length == 5) {
            toggleButtonCanopyAnalyzerRingMask1.setSelected(masks[0]);
            toggleButtonCanopyAnalyzerRingMask2.setSelected(masks[1]);
            toggleButtonCanopyAnalyzerRingMask3.setSelected(masks[2]);
            toggleButtonCanopyAnalyzerRingMask4.setSelected(masks[3]);
            toggleButtonCanopyAnalyzerRingMask5.setSelected(masks[4]);
        }
        checkboxGenerateLAI2xxxFormat.setSelected(laiParams.isGenerateLAI2xxxTypeFormat());
        List<Point3d> positions = laiParams.getPositions();
        if (positions != null) {
            listViewCanopyAnalyzerSensorPositions.getItems().setAll(positions);
        }
        checkboxGenerateCanopyAnalyzerTextFile.setSelected(laiParams.isGenerateTextFile());
        if (laiParams.isGenerateTextFile() && laiParams.getTextFile() != null) {
            textfieldOutputCanopyAnalyzerTextFile.setText(laiParams.getTextFile().getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonOpenVoxelFileCanopyAnalyzer(ActionEvent event) {

        File selectedFile = fileChooserOpenCanopyAnalyserInputFile.showOpenDialog(null);

        if (selectedFile != null) {
            textfieldVoxelFilePathCanopyAnalyzer.setText(selectedFile.getAbsolutePath());
            LOGGER.info("Canopy analyser input file opened.");
        }
    }

    @FXML
    private void onActionButtonRemovePositionCanopyAnalyzer(ActionEvent event) {
        ObservableList<?> selectedItems = listViewCanopyAnalyzerSensorPositions.getSelectionModel().getSelectedItems();
        listViewCanopyAnalyzerSensorPositions.getItems().removeAll(selectedItems);
        LOGGER.info("All canopy analyser sensors position removed.");
    }

    @FXML
    private void onActionButtonAddPositionCanopyAnalyzer(ActionEvent event) {

        if (!textfieldVoxelFilePathCanopyAnalyzer.getText().isEmpty()) {
            File voxelFile = new File(textfieldVoxelFilePathCanopyAnalyzer.getText());
            if (voxelFile.exists()) {
                positionImporterFrameController.setInitialVoxelFile(voxelFile);
            }
        }

        Stage positionImporterFrame = positionImporterFrameController.getStage();
        positionImporterFrame.show();
        positionImporterFrame.setOnHidden((WindowEvent event1)
                -> {
            listViewCanopyAnalyzerSensorPositions.getItems().addAll(positionImporterFrameController.getPositions());
            LOGGER.info("Canopy analizer position(s) added.");
        });
    }

    @FXML
    private void onActionButtonOpenOutputCanopyAnalyzerTextFile(ActionEvent event) {

        File selectedFile = fileChooserSaveCanopyAnalyserOutputFile.showSaveDialog(null);

        if (selectedFile != null) {

            textfieldOutputCanopyAnalyzerTextFile.setText(selectedFile.getAbsolutePath());
            LOGGER.info("Canopy analyser text file opened.");
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

                LAI2xxx lAi2xxx = new LAI2200(comboboxChooseCanopyAnalyzerSampling.getSelectionModel().getSelectedItem(),
                        LAI2xxx.ViewCap.CAP_360, new boolean[]{
                            false, false, false, false, false
                        });

                lAi2xxx.computeDirections();
                Vector3f[] directions = lAi2xxx.getDirections();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {

                    if (csv) {
                        if (cartesian) {
                            writer.write("X_cartesian Y_cartesian Z_cartesian\n");
                        } else {
                            writer.write("azimut elevation\n");
                        }
                    }

                    for (Vector3f direction : directions) {

                        if (csv) {
                            if (cartesian) {
                                writer.write(direction.x + " " + direction.y + " " + direction.z + "\n");
                            } else {
                                SphericalCoordinates sc = SphericalCoordinates.fromCartesian(direction);
                                writer.write(sc.getAzimut() + " " + sc.getZenith() + "\n");
                            }
                        } else {
                            writer.write("v " + direction.x + " " + direction.y + " " + direction.z + "\n");
                        }
                    }

                } catch (IOException ex) {
                    Util.showErrorDialog(null, ex, "[Canopy Analyzer]");
                }

                LOGGER.info("Canopy analyzer directions saved.");
            }
        }

    }

    @FXML
    private void onActionButtonSetupViewCap(ActionEvent event) {

        if (toggleButtonLAI2000Choice.isSelected()) {
            viewCapsSetupFrameController.setViewCapAngles(ViewCapsSetupFrameController.ViewCaps.LAI_2000);
        } else if (toggleButtonLAI2200Choice.isSelected()) {
            viewCapsSetupFrameController.setViewCapAngles(ViewCapsSetupFrameController.ViewCaps.LAI_2200);
        }

        Stage viewCapsSetupFrame = viewCapsSetupFrameController.getStage();
        viewCapsSetupFrame.setOnHidden((WindowEvent event1)
                -> {
            if (viewCapsSetupFrameController.isConfirmed()) {
                textFieldViewCapAngleCanopyAnalyzer.setText(df.format(viewCapsSetupFrameController.getAngle()));
            }
        });

        viewCapsSetupFrame.show();

    }

}
