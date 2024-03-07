/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui.configuration;

import org.amapvox.commons.javafx.io.FileChooserContext;
import org.amapvox.gui.DragAndDropHelper;
import org.amapvox.gui.TextFieldUtil;
import org.amapvox.gui.Util;
import org.amapvox.gui.Validators;
import org.amapvox.voxelfile.VoxelFileHeader;
import org.amapvox.voxelisation.output.OutputVariable;
import org.amapvox.voxelisation.postproc.ObjExporterCfg;
import org.amapvox.voxelfile.VoxelFileReader;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;
import org.controlsfx.validation.ValidationSupport;

/**
 * FXML Controller class
 *
 * @author Julien Heurtebize
 */
public class ObjExporterFrameController extends ConfigurationController {

    private final static Logger LOGGER = Logger.getLogger(ObjExporterFrameController.class);

    private final FileChooserContext fcVoxelFile = new FileChooserContext();
    private final FileChooserContext fcOutputFile = new FileChooserContext();

    private ValidationSupport objExportValidationSupport;

    @FXML
    private TextField textfieldInputVoxelFile;
    @FXML
    private TextField textfieldOutputFile;
    @FXML
    private HBox hboxOutputFile;

    @FXML
    private CheckBox checkboxMaterial;
    @FXML
    private ComboBox<String> comboboxAttribute;
    @FXML
    private ComboBox<String> comboboxGradient;
    @FXML
    private HBox hboxMaterial;
    @FXML
    private VBox vboxMaterialParameters;

    @FXML
    private CheckBox checkboxSizeFunctionOfPAD;
    @FXML
    private VBox vboxSizeFunctionofPAD;
    @FXML
    private HBox hboxSizeFunctionofPAD;
    @FXML
    private TextField textfieldPADMax;
    @FXML
    private TextField textfieldAlpha;
    //
    private final String TITLE = "AMAPVox: VoxToObj export tool";

    @Override
    public void initComponents(ResourceBundle rb) {

        textfieldInputVoxelFile.setOnDragOver(DragAndDropHelper.dragOverEvent);
        textfieldInputVoxelFile.setOnDragDropped((DragEvent event)
                -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles() && db.getFiles().size() == 1) {
                success = true;
                db.getFiles().stream().filter(file -> (file != null)).forEach(file -> {
                    try {
                        setVoxelFile(file);
                    } catch (Exception ex) {
                        LOGGER.error("Failed to load voxel file", ex);
                    }
                });
            }
            event.setDropCompleted(success);
            event.consume();
        });

        textfieldPADMax.setTextFormatter(TextFieldUtil.createFloatTextFormatter(5.f, TextFieldUtil.Sign.POSITIVE));
        textfieldAlpha.setTextFormatter(TextFieldUtil.createFloatTextFormatter(1.f / 3.f, TextFieldUtil.Sign.BOTH));

        hboxOutputFile.disableProperty().bind(Bindings.isEmpty(textfieldInputVoxelFile.textProperty()));

        comboboxGradient.getItems().addAll(Util.AVAILABLE_GRADIENT_COLOR_NAMES);
        comboboxGradient.getSelectionModel().selectFirst();
//        comboboxAttribute.getSelectionModel().selectFirst();

        vboxSizeFunctionofPAD.disableProperty().bind(Bindings.isEmpty(textfieldInputVoxelFile.textProperty()));
        hboxSizeFunctionofPAD.disableProperty().bind(checkboxSizeFunctionOfPAD.selectedProperty().not());

        hboxMaterial.disableProperty().bind(Bindings.isEmpty(textfieldInputVoxelFile.textProperty()));
        vboxMaterialParameters.disableProperty().bind(checkboxMaterial.selectedProperty().not());
    }

    @Override
    void initValidationSupport() {

        objExportValidationSupport = new ValidationSupport();
        objExportValidationSupport.registerValidator(textfieldInputVoxelFile, false, Validators.fileExistValidator("Voxel file"));
    }

    @Override
    ObservableValue[] getListenedProperties() {
        return new ObservableValue[]{
            textfieldInputVoxelFile.textProperty(),
            textfieldOutputFile.textProperty(),
            checkboxMaterial.selectedProperty(),
            comboboxAttribute.getSelectionModel().selectedItemProperty(),
            comboboxGradient.getSelectionModel().selectedItemProperty(),
            checkboxSizeFunctionOfPAD.selectedProperty(),
            textfieldPADMax.textProperty(),
            textfieldAlpha.textProperty()
        };
    }

    @FXML
    private void onActionButtonOpenVoxelFile(ActionEvent event) throws Exception {

        if (!textfieldInputVoxelFile.getText().isEmpty()) {
            File voxFile = new File(textfieldInputVoxelFile.getText());
            fcVoxelFile.fc.setInitialDirectory(voxFile.getParentFile());
        }
        File selectedFile = fcVoxelFile.showOpenDialog(getStage());

        try {
            setVoxelFile(selectedFile);
        } catch (Exception ex) {
            Util.showErrorDialog(null, ex, "[OBJ Export]");
        }
    }

    @FXML
    private void onActionButtonOpenOutputFile(ActionEvent event) throws Exception {

        if (!textfieldInputVoxelFile.getText().isEmpty()) {
            File voxFile = new File(textfieldInputVoxelFile.getText());
            fcOutputFile.fc.setInitialDirectory(voxFile.getParentFile());
            fcOutputFile.fc.setInitialFileName(replaceExtension(voxFile.getName(), "obj"));
        }
        File outputFile = fcOutputFile.showSaveDialog(getStage());

        if (null != outputFile) {
            textfieldOutputFile.setText(outputFile.getAbsolutePath());
        }
    }

    private String replaceExtension(String file, String extension) {

        int extensionBeginIndex = file.lastIndexOf(".");
        return file.substring(0, extensionBeginIndex) + "." + extension;
    }

    private void setVoxelFile(File voxelFile) throws Exception {

        VoxelFileHeader header = new VoxelFileReader(voxelFile).getHeader();
        // look for mandatory PAD variable
        String[] columns = header.getColumnNames();
        int noVegetationColumn = -1;
        int padColumn = -1;
        for (int ic = 0; ic < columns.length; ic++) {
            try {
                OutputVariable variable = OutputVariable.find(columns[ic]);
                if (variable.equals(OutputVariable.PLANT_AREA_DENSITY)) {
                    padColumn = ic;
                }
                if (variable.equals(OutputVariable.PLANT_AREA_DENSITY)
                        || variable.equals(OutputVariable.ATTENUATION_FPL_BIASED_MLE)
                        || variable.equals(OutputVariable.ATTENUATION_FPL_UNBIASED_MLE)
                        || variable.equals(OutputVariable.ATTENUATION_PPL_MLE)) {
                    noVegetationColumn = ic;
                    break;
                }
            } catch (NullPointerException ex) {
            }
        }
        if (noVegetationColumn < 0) {
            throw new IOException("Cannot export to OBJ. Output variable attenuation or PAD is missing.");
        }

        textfieldInputVoxelFile.setText(voxelFile.getAbsolutePath());
        textfieldOutputFile.setText(replaceExtension(textfieldInputVoxelFile.getText(), "obj"));

        if (padColumn < 0) {
            checkboxSizeFunctionOfPAD.setSelected(false);
        }
        vboxSizeFunctionofPAD.disableProperty().unbind();
        vboxSizeFunctionofPAD.setDisable(padColumn < 0);

        comboboxAttribute.getItems().setAll(columns);
        comboboxAttribute.getSelectionModel().selectFirst();
        textfieldPADMax.setText(df.format(5.f));
    }

    @Override
    public void saveConfiguration(File file) throws Exception {

        StringBuilder sb = new StringBuilder();
        if (objExportValidationSupport.isInvalid()) {
            objExportValidationSupport.initInitialDecoration();

            objExportValidationSupport.getValidationResult().getErrors().forEach(error -> sb.append("> ").append(error.getText()).append('\n'));
            if (!sb.toString().isEmpty()) {
                throw new IOException(sb.toString());
            }
        }

        ObjExporterCfg cfg = new ObjExporterCfg();

        cfg.setInputFile(new File(textfieldInputVoxelFile.getText()));
        cfg.setOutputFile(new File(textfieldOutputFile.getText()));

        cfg.setVoxelSizeFunctionEnabled(checkboxSizeFunctionOfPAD.isSelected());
        cfg.setMaxPAD(Float.valueOf(textfieldPADMax.getText()));
        cfg.setAlpha(Float.valueOf(textfieldAlpha.getText()));

        cfg.setMaterialEnabled(checkboxMaterial.isSelected());
        cfg.setOutputVariable(comboboxAttribute.getSelectionModel().getSelectedItem());
        cfg.setGradientName(comboboxGradient.getSelectionModel().getSelectedItem());

        cfg.write(file);
    }

    @Override
    public void loadConfiguration(File file) throws Exception {

        ObjExporterCfg cfg = new ObjExporterCfg();
        cfg.read(file);

        try {
            setVoxelFile(cfg.getInputFile());
            textfieldOutputFile.setText(cfg.getOutputFile().getAbsolutePath());

            checkboxSizeFunctionOfPAD.setSelected(cfg.isVoxelSizeFunctionEnabled());
            textfieldPADMax.setText(df.format(cfg.getMaxPAD()));
            textfieldAlpha.setText(df.format(cfg.getAlpha()));

            checkboxMaterial.setSelected(cfg.isMaterialEnabled());
            comboboxAttribute.getSelectionModel().select(cfg.getOutputVariable());
            comboboxGradient.getSelectionModel().select(cfg.getGradientName());
        } catch (Exception ex) {
            Util.showErrorDialog(null, ex, "[OBJ Export]");
        }
    }

}
