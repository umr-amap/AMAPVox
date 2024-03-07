/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author pverley
 */
public class SaveOnCloseFrameController implements Initializable {

    private Stage stage;

    private MainFrameController mainController;

    private boolean cancelled = false;

    @FXML
    private ListView<CfgFile> listViewModifiedFiles;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnDiscard;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        listViewModifiedFiles.setCellFactory((ListView<CfgFile> list) -> new ListCell<CfgFile>() {

            @Override
            protected void updateItem(CfgFile file, boolean empty) {
                super.updateItem(file, empty);
                final String text = (file == null || empty) ? null : file.getName();
                setText(text);
            }
        });

        listViewModifiedFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Bindings.isEmpty(listViewModifiedFiles.getItems()).addListener(
                (obs, wasEmpty, isNowEmpty) -> {
                    if (!wasEmpty && isNowEmpty) {
                        stage.hide();
                    }
                }
        );

        btnSave.disableProperty().bind(Bindings.isEmpty(listViewModifiedFiles.getSelectionModel().getSelectedItems()));
        btnDiscard.disableProperty().bind(Bindings.isEmpty(listViewModifiedFiles.getSelectionModel().getSelectedItems()));
    }

    public void setStage(Stage stage) {
        this.stage = stage;

        this.stage.setOnCloseRequest(windowEvent -> {
            windowEvent.consume();
            onActionButtonCancel(null);
        });
    }
    
    public void setMainController(MainFrameController controller) {
        this.mainController = controller;
    }

    public void addFile(CfgFile file) {

        if (!listViewModifiedFiles.getItems().contains(file)) {
            listViewModifiedFiles.getItems().add(file);
        }
    }

    public void removeFile(CfgFile file) {
        listViewModifiedFiles.getItems().remove(file);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    @FXML
    private void onActionButtonSave(ActionEvent event) {

        List<CfgFile> list = listViewModifiedFiles.getSelectionModel().getSelectedItems();
        list.forEach(file -> {
            mainController.saveTask(file);
            listViewModifiedFiles.getItems().remove(file);
        });
    }

    @FXML
    private void onActionButtonSaveAll(ActionEvent event) {

        listViewModifiedFiles.getItems().forEach(file -> {
            mainController.saveTask(file);
        });
        listViewModifiedFiles.getItems().clear();
    }

    @FXML
    private void onActionButtonDiscard(ActionEvent event) {

        List<CfgFile> list = listViewModifiedFiles.getSelectionModel().getSelectedItems();
        listViewModifiedFiles.getItems().removeAll(list);
    }

    @FXML
    private void onActionButtonDiscardAll(ActionEvent event) {
        listViewModifiedFiles.getItems().clear();
    }

    @FXML
    private void onActionButtonCancel(ActionEvent event) {
        cancelled = true;
        stage.hide();
    }

}
