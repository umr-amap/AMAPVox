package org.amapvox.gui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import org.amapvox.commons.util.filter.FloatFilter;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class FilterFrameController implements Initializable {

    private Stage stage;
    private Parent root;
    private boolean requestAdd;

    @FXML
    private ComboBox<String> comboboxVariable;
    @FXML
    private ComboBox<String> comboboxInequality;
    @FXML
    private TextField textfieldValue;
    @FXML
    private Button buttonAdd;

    public static FilterFrameController newInstance() {

        FilterFrameController controller = null;

        try {

            FXMLLoader loader = new FXMLLoader(FilterFrameController.class.getResource("/org/amapvox/gui/fxml/FilterFrame.fxml"));
            Parent root = loader.load();
            controller = loader.getController();
            controller.root = root;

        } catch (IOException ex) {
            Logger.getLogger(FilterFrameController.class.getName()).log(Level.SEVERE, "Cannot load FilterFrame.fxml", ex);
        }

        return controller;
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        textfieldValue.setOnDragOver(DragAndDropHelper.dragOverEvent);
        textfieldValue.setOnDragDropped((DragEvent event)
                -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles() && db.getFiles().size() == 1) {
                success = true;
                db.getFiles().forEach(file -> {
                    if (file != null) {
                        textfieldValue.setText(file.getAbsolutePath());
                    }
                });
            }
            event.setDropCompleted(success);
            event.consume();
        });

        comboboxInequality.getItems().addAll("!=", "==", "<", "<=", ">", ">=");

        comboboxInequality.getSelectionModel().selectFirst();
        comboboxVariable.getSelectionModel().selectFirst();

        buttonAdd.disableProperty().bind(comboboxVariable.getSelectionModel().selectedItemProperty().isNull()
                .or(comboboxInequality.getSelectionModel().selectedItemProperty().isNull())
                .or(textfieldValue.textProperty().isEmpty()));
    }

    public Stage getStage() {
        if (null == stage) {
            stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Filter");
            stage.initModality(Modality.APPLICATION_MODAL);
        }
        return stage;
    }

    public void setFilters(String... items) {
        comboboxVariable.getItems().setAll(items);
        comboboxVariable.getSelectionModel().selectFirst();
    }

    public FloatFilter getFilter() {

        if (textfieldValue.getText().isEmpty() || comboboxVariable.getSelectionModel().getSelectedIndex() < 0) {
            return null;
        }

        FloatFilter filter = new FloatFilter(comboboxVariable.getSelectionModel().getSelectedItem(),
                Float.valueOf(textfieldValue.getText()), comboboxInequality.getSelectionModel().getSelectedIndex());

        return filter;
    }

    @FXML
    private void onActionButtonAdd(ActionEvent event) {
        requestAdd = true;
        stage.close();
        requestAdd = false;
    }

    public boolean isRequestAdd() {
        return requestAdd;
    }
}
