/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author calcul
 */
public class AttributsImporterFrameController extends CustomController implements Initializable {
    
    @FXML
    private VBox vBoxAttributsLayout;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        vBoxAttributsLayout.setSpacing(20);
    }    
    
    public void setAttributsList(String... attributesList){
        
        vBoxAttributsLayout.getChildren().clear();
        
        for(String s : attributesList){
            
            CheckBox cb = new CheckBox(s);
            cb.setSelected(true);
            vBoxAttributsLayout.getChildren().add(cb);
        }
    }
    
    public void setAttributsList(List<String> attributsList){
        
        vBoxAttributsLayout.getChildren().clear();
        
        for(String s : attributsList){
            
            CheckBox cb = new CheckBox(s);
            cb.setSelected(true);
            vBoxAttributsLayout.getChildren().add(cb);
        }
    }
    
    public List<String> getSelectedAttributs(){
        
        List<String> selectedAttributs = new ArrayList<>();
        
        ObservableList<Node> children = vBoxAttributsLayout.getChildren();
        
        for(Node node : children){
            if(((CheckBox)node).isSelected()){
                selectedAttributs.add(((CheckBox)node).getText());
            }
        }
        
        return selectedAttributs;
    }

    @FXML
    private void onActionButtonImport(ActionEvent event) {
        
        if(stage != null){
            stage.close();
        }
    }
}
