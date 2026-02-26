package org.amapvox.gui;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.vecmath.Point3d;
import org.amapvox.commons.javafx.SelectableMenuButton;
import org.apache.log4j.Logger;

/**
 * FXML Controller class for sensor positions in the canopy tools.
 *
 * @author Philippe Verley
 */
public class SensorPositionsController implements Initializable {

    // logger
    private final Logger LOGGER = Logger.getLogger(SensorPositionsController.class);
    // ressources
    private ResourceBundle resources;
    // voxel file
    File voxelFile;
    // position importer
    private PositionImporterFrameController positionImporterFrameController;

    @FXML
    private Label labelSensorPositions;
    @FXML
    private Button helpButtonSensorPositions;
    @FXML
    private HelpButtonController helpButtonSensorPositionsController;
    @FXML
    private ListView<Point3d> listViewSensorPositions;
    @FXML
    private SelectableMenuButton selectorSensorPositions;
    @FXML
    private Button buttonRemovePosition;

    @Override
    public void initialize(URL url, ResourceBundle resources) {

        this.resources = resources;

        // link list view and selectable menu button
        listViewSensorPositions.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        Util.linkSelectorToList(selectorSensorPositions, listViewSensorPositions);

        // new instance of position importer frame controller
        positionImporterFrameController = PositionImporterFrameController.newInstance();

        // default help message
        setHelpButtonText("help_sensor_positions");
        
        // disable remove button when no sensor positions
        buttonRemovePosition.disableProperty().bind(Bindings.isEmpty(listViewSensorPositions.getItems()));
    }

    public ObservableValue[] getListenedProperties() {
        return new ObservableValue[]{
            listViewSensorPositions.itemsProperty()
        };
    }

    public void setPositions(List<Point3d> positions) {
        listViewSensorPositions.getItems().setAll(positions);
    }

    public List<Point3d> getPositions() {
        return listViewSensorPositions.getItems();
    }

    public void setText(String text) {
        labelSensorPositions.setText(text);
    }

    public void setHelpButtonText(String resourceKey) {

        helpButtonSensorPositions.setOnAction((ActionEvent event) -> {
            helpButtonSensorPositionsController.showHelpDialog(resources.getString(resourceKey));
        });
    }

    public void setVoxelFile(File voxelFile) {
        this.voxelFile = voxelFile;
    }

    @FXML
    private void onActionButtonAddPosition(ActionEvent event) {

        if (null != voxelFile && voxelFile.exists()) {
            positionImporterFrameController.setInitialVoxelFile(voxelFile);
        }

        Stage positionImporterFrame = positionImporterFrameController.getStage();
        positionImporterFrame.show();
        positionImporterFrame.setOnHidden((WindowEvent event1)
                -> {
            listViewSensorPositions.getItems().addAll(positionImporterFrameController.getPositions());
            LOGGER.debug("Position(s) added.");
        });
    }

    @FXML
    private void onActionButtonRemovePosition(ActionEvent event) {

        ObservableList selectedItems = listViewSensorPositions.getSelectionModel().getSelectedItems();
        listViewSensorPositions.getItems().removeAll(selectedItems);
        LOGGER.debug("All view hemispherical photo sensor selected.");
    }

}
