/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;

/**
 * FXML Controller class
 *
 * @author Julien Heurtebize
 */
public class EditableListViewController<T> implements Initializable {

    @FXML
    private HBox hboxButtons;
    @FXML
    private ListView<T> listViewItems;
    @FXML
    private MenuItem buttonSelectAll;
    @FXML
    private MenuItem buttonSelectNone;
    @FXML
    private Button buttonRemoveItemFromListView;
    @FXML
    private Button buttonAddItemToListView;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }

    public ListView<T> getListViewItems() {
        return listViewItems;
    }

    public HBox getHboxButtons() {
        return hboxButtons;
    }   

    public MenuItem getButtonSelectAll() {
        return buttonSelectAll;
    }

    public MenuItem getButtonSelectNone() {
        return buttonSelectNone;
    }

    public Button getButtonRemoveItemFromListView() {
        return buttonRemoveItemFromListView;
    }

    public Button getButtonAddItemToListView() {
        return buttonAddItemToListView;
    }

    @FXML
    private void onActionMenuItemSelectAll(ActionEvent event) {
        listViewItems.getSelectionModel().selectAll();
    }

    @FXML
    private void onActionMenuItemSelectNone(ActionEvent event) {
        listViewItems.getSelectionModel().clearSelection();
    }

    @FXML
    private void onActionButtonRemoveItemFromListView(ActionEvent event) {
        ObservableList<T> selectedItems = listViewItems.getSelectionModel().getSelectedItems();
        listViewItems.getItems().removeAll(selectedItems);
    }

}
