/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui;

import org.amapvox.commons.javafx.io.FileChooserContext;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Julien Heurtebize
 */
public class FileChooserTextFieldController<T> implements Initializable {

    @FXML
    private Label label;
    @FXML
    private TextField textFieldFile;
    @FXML
    private Button buttonOpenFile;
    
    private T selectedObject;
    
    private final FileChooserContext fileChooser = new FileChooserContext();
    private final Stage stage = new Stage();
    
    public enum Mode{
        
        SAVE((short)0),
        OPEN((short)1),
        OPEN_MULTIPLE((short)2);
        
        private short mode;
        
        private Mode(short mode){
            this.mode = mode;
        }
    }
    
    private Mode mode = Mode.OPEN;

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Label getLabel() {
        return label;
    }
    
    public void setSelectedObject(T o){
        textFieldFile.setText(o.toString());
        this.selectedObject = o;
    }

    public TextField getTextFieldFile() {
        return textFieldFile;
    }

    public Button getButtonOpenFile() {
        return buttonOpenFile;
    }

    public FileChooserContext getFileChooser() {
        return fileChooser;
    }

    public T getSelectedObject() {
        return selectedObject;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        textFieldFile.setOnDragOver(DragAndDropHelper.dragOverEvent);
        textFieldFile.setOnDragDropped((DragEvent event)
                -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles() && db.getFiles().size() == 1) {
                success = true;
                for (File file : db.getFiles()) {
                    if (file != null) {
                        textFieldFile.setText(file.getAbsolutePath());
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }    

    @FXML
    private void onActionButtonOpenFile(ActionEvent event) {
        
        switch (mode) {
            case OPEN:
                File selectedInputFile = fileChooser.showOpenDialog(stage);
                
                if(selectedInputFile != null){
                    textFieldFile.setText(selectedInputFile.getAbsolutePath());
                }
                
                break;
            case OPEN_MULTIPLE:
                List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
                
                if(selectedFiles != null){
                    StringBuilder sb = new StringBuilder();
                    for (File selectedFile : selectedFiles) {
                        sb.append(selectedFile.getAbsolutePath()).append(";");
                    }
                    textFieldFile.setText(sb.toString());
                }
                
                break;
            case SAVE:
                File selectedOutputFile = fileChooser.showSaveDialog(stage);
                
                if(selectedOutputFile != null){
                    textFieldFile.setText(selectedOutputFile.getAbsolutePath());
                }
                break;
            default:
                break;
        }
        
    }
}
