package org.amapvox.commons.javafx;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TitledPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author calcul
 */
public class EditableListViewController extends TitledPane {

    @FXML
    private ListView<File> listViewTaskList;
    @FXML
    private Button buttonMoveItemUp;
    @FXML
    private Button buttonMoveItemDown;
    @FXML
    private MenuItem menuitemSelectAll;
    @FXML
    private MenuItem menuitemSelectNone;
    @FXML
    private Button buttonRemoveItemFromList;
    @FXML
    private Button buttonAddItemToList;
    
    private FileChooser fileChooser;

    /**
     * Initializes the controller class.
     */
    public EditableListViewController() {
        
         FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/EditableListView.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        
        fileChooser = new FileChooser();
        
        listViewTaskList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        menuitemSelectAll.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent event) {
                 listViewTaskList.getSelectionModel().selectAll();
             }
         });
        
        menuitemSelectNone.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent event) {
                 listViewTaskList.getSelectionModel().clearSelection();
             }
         });
        
        buttonMoveItemUp.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent event) {
                 
                ObservableList<File> items = listViewTaskList.getItems();
                List<File> copy = new ArrayList<>();
                
                for(File item : items){
                    copy.add(item);
                }
                
                List<Integer> selectedIndices = new ArrayList<>();
                for(Integer i : listViewTaskList.getSelectionModel().getSelectedIndices()){
                    selectedIndices.add(i);
                }
                
                List<Integer> targetIndices = new ArrayList<>();
                
                for(Integer selectedIndice : selectedIndices){
                    
                    if(selectedIndice != 0){
                        int draggedIdx = selectedIndice;
                        int targetIndex = draggedIdx - 1;
                        
                        targetIndices.add(targetIndex);

                        File item = copy.get(draggedIdx);
                        File item2 = copy.get(targetIndex);

                        //swap
                        items.set(draggedIdx, item2);
                        items.set(targetIndex, item);

                        listViewTaskList.getSelectionModel().clearSelection(draggedIdx);
                        
                    }
                    
                }
                
                List<File> itemscopy = new ArrayList<>(listViewTaskList.getItems());
                listViewTaskList.getItems().setAll(itemscopy);
                
                for(int i=0;i<targetIndices.size();i++){
                    listViewTaskList.getSelectionModel().select(targetIndices.get(i));
                }
                
             }
         });
        
        buttonAddItemToList.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent event) {
                 
                List<File> selectedFiles = fileChooser.showOpenMultipleDialog(new Stage());

                if(selectedFiles != null){
                    listViewTaskList.getItems().addAll(selectedFiles);
                }
                 
             }
         });
        
    }

    public ListView<?> getListViewTaskList() {
        return listViewTaskList;
    }

    public Button getButtonMoveItemUp() {
        return buttonMoveItemUp;
    }

    public Button getButtonMoveItemDown() {
        return buttonMoveItemDown;
    }

    public MenuItem getMenuitemSelectAll() {
        return menuitemSelectAll;
    }

    public MenuItem getMenuitemSelectNone() {
        return menuitemSelectNone;
    }

    public Button getButtonRemoveItemFromList() {
        return buttonRemoveItemFromList;
    }

    public Button getButtonAddItemToList() {
        return buttonAddItemToList;
    }    
}
