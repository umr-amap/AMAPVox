
/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */
package org.amapvox.gui;

import org.amapvox.commons.javafx.io.TextFileParserFrameController;
import org.amapvox.commons.util.filter.Filter;
import org.amapvox.commons.util.io.file.CSVFile;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class PointcloudFilterController extends HBox implements Initializable {

    final Logger LOGGER = Logger.getLogger(PointcloudFilterController.class);

    @FXML
    private TextField textfieldPointCloudPath;

    @FXML
    private Label labelPointCloudErrorMarginValue;

    @FXML
    private TextField textfieldPointCloudErrorMargin;

    @FXML
    private Label labelPointCloudPath;

    @FXML
    private Button buttonOpenPointCloudFile;

    @FXML
    private ComboBox<Filter.Behavior> comboboxPointCloudFilteringType;

    @FXML
    private Button buttonRemovePointCloudFilter;

    @FXML
    private CheckBox checkboxApplyVopMatrix;

    final private VBox parent;

    final private Stage stage;

    private CSVFile csvFile;

    private File initialDirectory;

    public PointcloudFilterController(VBox parent, Stage stage, File file) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/PointcloudFilter.fxml"));
            /**
             * The constructor is leaking 'this' but it seems the way to do it
             * according to Oracle documentation.
             * https://docs.oracle.com/javafx/2/fxml_get_started/custom_control.htm
             */
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        this.parent = parent;
        this.stage = stage;
        this.initialDirectory = file;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        textfieldPointCloudPath.setOnDragOver(DragAndDropHelper.dragOverEvent);
        textfieldPointCloudPath.setOnDragDropped((DragEvent event)
                -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if ((db.getFiles().size() == 1) && db.hasFiles()) {
                success = true;
                db.getFiles()
                        .stream()
                        .filter(file -> (file != null))
                        .forEach(file -> {
                            textfieldPointCloudPath.setText(file.getAbsolutePath());
                        });
            }
            event.setDropCompleted(success);
            event.consume();
        });

        comboboxPointCloudFilteringType.getItems().addAll(Filter.Behavior.values());
        comboboxPointCloudFilteringType.getSelectionModel().selectFirst();

        // close button
        buttonRemovePointCloudFilter.setOnAction((event) -> {
            Platform.runLater(() -> {
                parent.getChildren().remove(this);
            });
        });

        // open button
        FileChooser fileChooserOpenPointCloudFile = new FileChooser();
        fileChooserOpenPointCloudFile.setTitle("Choose point cloud file");
        fileChooserOpenPointCloudFile.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("TXT Files", "*.txt"));
        buttonOpenPointCloudFile.setOnAction((ActionEvent event) -> {
            if (initialDirectory != null) {
                fileChooserOpenPointCloudFile.setInitialDirectory(initialDirectory);
            }
            File selectedFile = fileChooserOpenPointCloudFile.showOpenDialog(stage);
            if (selectedFile != null) {
                TextFileParserFrameController textFileParserFrameController = TextFileParserFrameController.newInstance();
                initialDirectory = selectedFile.getParentFile();
                textFileParserFrameController.setColumnAssignment(true);
                textFileParserFrameController.setColumnAssignmentValues("Ignore", "X", "Y", "Z");
                textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(0, 1);
                textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(1, 2);
                textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(2, 3);
                textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(3, 4);
                textFileParserFrameController.setHeaderExtractionEnabled(true);
                textFileParserFrameController.setSeparator(",");
                try {
                    textFileParserFrameController.setTextFile(selectedFile);
                } catch (IOException ex) {
                    Util.showErrorDialog(stage, ex, "[Voxelization]");
                    return;
                }
                Stage textFileParserFrame = textFileParserFrameController.getStage();
                textFileParserFrame.setOnHidden((WindowEvent event1) -> {
                    CSVFile file = new CSVFile(selectedFile.getAbsolutePath());
                    file.setColumnSeparator(textFileParserFrameController.getSeparator());
                    file.setColumnAssignment(textFileParserFrameController.getAssignedColumnsItemsMap());
                    file.setNbOfLinesToRead(textFileParserFrameController.getNumberOfLines());
                    file.setNbOfLinesToSkip(textFileParserFrameController.getSkipLinesNumber());
                    file.setContainsHeader(textFileParserFrameController.getHeaderIndex() != -1);
                    file.setHeaderIndex(textFileParserFrameController.getHeaderIndex());
                    setCSVFile(file);
                });
                textFileParserFrame.show();

            }
        });
    }

    public void setCSVFile(CSVFile file) {

        if (file != null) {
            csvFile = file;
            textfieldPointCloudPath.setText(csvFile.getAbsolutePath());
        }
    }

    public CSVFile getCsvFile() {
        return csvFile;
    }

    public float getMarginOfError() {
        return Float.valueOf(textfieldPointCloudErrorMargin.getText());
    }

    public void setMarginOfError(float error) {
        textfieldPointCloudErrorMargin.setText(Float.toString(error));
    }

    public Filter.Behavior getBehavior() {
        return comboboxPointCloudFilteringType.getSelectionModel().getSelectedItem();
    }

    public void setBehavior(Filter.Behavior behavior) {
        comboboxPointCloudFilteringType.getSelectionModel().select(behavior);
    }

    public void disableContent(boolean disable) {

        setDisable(disable);
        labelPointCloudPath.setDisable(disable);
        textfieldPointCloudPath.setDisable(disable);
        buttonOpenPointCloudFile.setDisable(disable);
        buttonRemovePointCloudFilter.setDisable(disable);
        textfieldPointCloudErrorMargin.setDisable(disable);
        textfieldPointCloudPath.setDisable(disable);
        comboboxPointCloudFilteringType.setDisable(disable);
        labelPointCloudErrorMarginValue.setDisable(disable);
        //checkboxApplyVopMatrix.setDisable(disable);
    }

    public boolean isApplyVOPMatrix() {
        return checkboxApplyVopMatrix.isSelected();
    }

    public void setApplyVOPMatrix(boolean selected) {
        checkboxApplyVopMatrix.setSelected(selected);
    }

}
