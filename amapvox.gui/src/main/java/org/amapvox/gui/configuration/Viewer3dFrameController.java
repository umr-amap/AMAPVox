package org.amapvox.gui.configuration;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import org.amapvox.commons.math.util.MatrixFileParser;
import org.amapvox.voxelfile.VoxelFileHeader;
import org.amapvox.gui.DialogHelper;
import org.amapvox.commons.javafx.io.FileChooserContext;
import org.amapvox.gui.DragAndDropHelper;
import org.amapvox.voxelfile.VoxelFileReader;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javax.vecmath.Matrix4d;
import org.amapvox.gui.TransformationMatrixController;
import org.amapvox.gui.Validators;
import org.amapvox.gui.viewer3d.Viewer3dConfiguration;
import org.apache.log4j.Logger;
import org.controlsfx.validation.ValidationSupport;

/**
 * FXML Controller class
 *
 * @author calcul
 */
public class Viewer3dFrameController extends ConfigurationController {

    private final static Logger LOGGER = Logger.getLogger(Viewer3dFrameController.class);

    private FileChooserContext fileChooserOpenVoxelFile;
    private File currentVoxelFile;
    private FileChooserContext fileChooserOpenDTMFile;
    private FileChooserContext fileChooserOpenMatrixFile;

    private Matrix4d rasterTransfMatrix;

    private ValidationSupport viewer3dValidationSupport;

    @FXML
    private TextField textFieldVoxelFile;

    @FXML
    private ComboBox<String> comboboxAttributeToView;
    @FXML
    private VBox vboxRasterProperties;
    @FXML
    private HBox hboxRasterFile;
    @FXML
    private HBox hboxAttributeToView;
    @FXML
    private TextField textfieldRasterFilePath;
    @FXML
    private CheckBox checkboxUseTransformationMatrix;
    @FXML
    private Button buttonSetTransformationMatrix;
    @FXML
    private Button buttonResetTransformationMatrix;
    @FXML
    private TransformationMatrixController transformationMatrixController;
    @FXML
    private CheckBox checkboxFitRasterToVoxelSpace;
    @FXML
    private TextField textfieldRasterFittingMargin;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    void initComponents(ResourceBundle rb) {

        textFieldVoxelFile.setOnDragOver(DragAndDropHelper.dragOverEvent);
        textFieldVoxelFile.setOnDragDropped((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles() && db.getFiles().size() == 1) {
                success = true;
                db.getFiles().stream()
                        .filter(Objects::nonNull)
                        .forEach(file -> {
                            if (VoxelFileReader.isValid(file)) {
                                textFieldVoxelFile.setText(file.getAbsolutePath());
                                updateCurrentVoxelFile(file);
                                hboxAttributeToView.setDisable(false);
                            } else {
                                LOGGER.warn("Failed to drop " + file.getName() + ". Not a valid vox file.");
                            }
                        });
            }
            event.setDropCompleted(success);
            event.consume();
        });

        textfieldRasterFilePath.setOnDragOver(DragAndDropHelper.dragOverEvent);
        textfieldRasterFilePath.setOnDragDropped((DragEvent event)
                -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles() && db.getFiles().size() == 1) {
                success = true;
                for (File file : db.getFiles()) {
                    if (file != null) {
                        textfieldRasterFilePath.setText(file.getAbsolutePath());
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        fileChooserOpenVoxelFile = new FileChooserContext();
        fileChooserOpenVoxelFile.fc.setTitle("Choose Voxel file");
        fileChooserOpenVoxelFile.fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*"),
                new FileChooser.ExtensionFilter("Voxel Files", "*.vox"));

        fileChooserOpenDTMFile = new FileChooserContext();
        fileChooserOpenDTMFile.fc.setTitle("Choose DTM file");
        fileChooserOpenDTMFile.fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*"),
                new FileChooser.ExtensionFilter("DTM Files", "*.asc"));

        fileChooserOpenMatrixFile = new FileChooserContext();

        rasterTransfMatrix = new Matrix4d();
        rasterTransfMatrix.setIdentity();

        checkboxUseTransformationMatrix.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            buttonSetTransformationMatrix.setDisable(!newValue);
            buttonResetTransformationMatrix.setDisable(!newValue);
            transformationMatrixController.setDisable(!newValue);
        });
    }

    private void updateCurrentVoxelFile(File voxelFile) {

        this.currentVoxelFile = voxelFile;

        if (!VoxelFileReader.isValid(voxelFile)) {
            return;
        }

        try {
            VoxelFileHeader header = new VoxelFileReader(voxelFile).getHeader();
            String[] parameters = header.getColumnNames();

            for (int i = 0; i < parameters.length; i++) {

                parameters[i] = parameters[i].replaceAll(" ", "");
                parameters[i] = parameters[i].replaceAll("#", "");
            }

            comboboxAttributeToView.getItems().clear();
            comboboxAttributeToView.getItems().addAll(parameters);

            if (parameters.length > 3) {
                comboboxAttributeToView.getSelectionModel().select(3);
            }

        } catch (Exception ex) {
            LOGGER.error("Cannot read voxel file", ex);
        }
    }

    @FXML
    private void onActionButtonOpenVoxelFile(ActionEvent event) {

        if (currentVoxelFile != null) {
            fileChooserOpenVoxelFile.fc.setInitialDirectory(currentVoxelFile.getParentFile());
        }

        File selectedFile = fileChooserOpenVoxelFile.showOpenDialog(getStage());

        if (selectedFile != null) {
            textFieldVoxelFile.setText(selectedFile.getAbsolutePath());
            updateCurrentVoxelFile(selectedFile);
            hboxAttributeToView.setDisable(false);
        } else {
            vboxRasterProperties.setDisable(true);
            hboxAttributeToView.setDisable(true);
        }
    }

    @FXML
    private void onActionButtonOpenRasterFile(ActionEvent event) {

        if (currentVoxelFile != null) {
            fileChooserOpenDTMFile.fc.setInitialDirectory(currentVoxelFile.getParentFile());
        }

        File selectedFile = fileChooserOpenDTMFile.showOpenDialog(getStage());

        if (selectedFile != null) {
            textfieldRasterFilePath.setText(selectedFile.getAbsolutePath());
            vboxRasterProperties.setDisable(false);
        } else {
            vboxRasterProperties.setDisable(true);
        }
    }

    @FXML
    private void onActionButtonSetTransformationMatrix(ActionEvent event) {

        fileChooserOpenMatrixFile.fc.setInitialDirectory(currentVoxelFile.getParentFile());

        File selectedFile = fileChooserOpenMatrixFile.showOpenDialog(getStage());

        if (selectedFile != null) {

            Matrix4d mat;
            try {
                mat = MatrixFileParser.getMatrixFromFile(selectedFile);
                if (mat != null) {

                    rasterTransfMatrix = MatrixFileParser.getMatrixFromFile(selectedFile);
                    if (rasterTransfMatrix == null) {
                        rasterTransfMatrix = new Matrix4d();
                        rasterTransfMatrix.setIdentity();
                    }
                    transformationMatrixController.setMatrix(rasterTransfMatrix);

                } else {
                    DialogHelper.showErrorDialog(getStage(), new Exception("bad format"));
                }

            } catch (IOException ex) {
                LOGGER.error("Cannot read matrix file", ex);
            }

        }
    }

    @FXML
    private void onActionButtonResetTransformationMatrix(ActionEvent event) {

        rasterTransfMatrix.setIdentity();
        transformationMatrixController.setMatrix(rasterTransfMatrix);
    }

    @Override
    void saveConfiguration(File file) throws Exception {

        StringBuilder sb = new StringBuilder();

        if (viewer3dValidationSupport.isInvalid()) {
            viewer3dValidationSupport.initInitialDecoration();
            viewer3dValidationSupport.getValidationResult().getErrors().forEach(error -> sb.append("> ").append(error.getText()).append('\n'));
        }

        if (!sb.toString().isEmpty()) {
            throw new IOException(sb.toString());
        }

        Viewer3dConfiguration cfg = new Viewer3dConfiguration();

        cfg.setVoxelFile(currentVoxelFile);
        cfg.setVariableName(comboboxAttributeToView.getSelectionModel().getSelectedItem());
        File dtmFile = new File(textfieldRasterFilePath.getText());
        if (dtmFile.exists()) {
            cfg.setDtmFile(dtmFile);
        }
        Matrix4d vopMatrix = new Matrix4d(rasterTransfMatrix);
        if (!checkboxUseTransformationMatrix.isSelected()) {
            vopMatrix.setIdentity();
        }
        cfg.setVopMatrix(vopMatrix);
        int fitMargin = checkboxFitRasterToVoxelSpace.isSelected()
                ? Integer.parseInt(textfieldRasterFittingMargin.getText())
                : -1;
        cfg.setDtmMargin(fitMargin);

        cfg.write(file);
    }

    @Override
    void loadConfiguration(File file) throws Exception {

        Viewer3dConfiguration cfg = new Viewer3dConfiguration();
        cfg.read(file);

        textFieldVoxelFile.setText(cfg.getVoxelFile().getAbsolutePath());
        updateCurrentVoxelFile(cfg.getVoxelFile());

        hboxAttributeToView.setDisable(false);
        comboboxAttributeToView.getSelectionModel().select(cfg.getVariableName());

        if (null != cfg.getDtmFile()) {
            textfieldRasterFilePath.setText(cfg.getDtmFile().getAbsolutePath());
            vboxRasterProperties.setDisable(false);
        }

        rasterTransfMatrix = new Matrix4d(cfg.getVopMatrix());
        transformationMatrixController.setMatrix(rasterTransfMatrix);
        Matrix4d identity = new Matrix4d();
        identity.setIdentity();
        checkboxUseTransformationMatrix.setSelected(!rasterTransfMatrix.equals(identity));

        textfieldRasterFittingMargin.setText(String.valueOf(cfg.getDtmMargin()));
        checkboxFitRasterToVoxelSpace.setSelected(cfg.getDtmMargin() >= 0);
    }

    @Override
    void initValidationSupport() {

        viewer3dValidationSupport = new ValidationSupport();
        viewer3dValidationSupport.registerValidator(textFieldVoxelFile, true, Validators.fileExistValidator("Voxel file"));
        if (textfieldRasterFilePath.getText().isEmpty()) {
            viewer3dValidationSupport.registerValidator(textfieldRasterFilePath, false, Validators.unregisterValidator);
        } else {
            viewer3dValidationSupport.registerValidator(textfieldRasterFilePath, false, Validators.fileExistValidator("DTM file"));
        }
        viewer3dValidationSupport.registerValidator(textfieldRasterFittingMargin, true, Validators.fieldIntegerValidator);
    }

    @Override
    ObservableValue[] getListenedProperties() {

        return new ObservableValue[]{
            textFieldVoxelFile.textProperty(),
            comboboxAttributeToView.getSelectionModel().selectedIndexProperty(),
            textfieldRasterFilePath.textProperty(),
            checkboxUseTransformationMatrix.selectedProperty(),
            checkboxFitRasterToVoxelSpace.selectedProperty(),
            textfieldRasterFittingMargin.textProperty()
        };
    }
}
