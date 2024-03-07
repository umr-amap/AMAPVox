/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.viewer3d;

import javafx.scene.control.TreeCell;

/**
 *
 * @author calcul
 */
public class SceneObjectTreeCell extends TreeCell<SceneObjectWrapper>{
    
    public SceneObjectTreeCell() {
    }
    
    @Override
        public void updateItem(SceneObjectWrapper item, boolean empty) {
            
            super.updateItem(item, empty);
            
            
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if(item == null){
                    setText("Scene");
                    setGraphic(null);
                }else{
                    
                    disableProperty().bind(item.getProgressBar().progressProperty().lessThan(1));
                    setGraphic(item);
                    setText("");
                }
            }
        }
}
