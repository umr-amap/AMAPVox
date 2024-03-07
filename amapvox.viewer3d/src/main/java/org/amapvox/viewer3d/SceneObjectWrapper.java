/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.viewer3d;

import org.amapvox.viewer3d.object.scene.SceneObject;
import java.io.File;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javax.vecmath.Matrix4d;

/**
 *
 * @author calcul
 */
public class SceneObjectWrapper extends VBox{
        
        private SceneObject sceneObject;
        private final Label label;
        private final ProgressBar progressBar;
        private final Label progressInfo;
        private final CheckBox checkbox;
        private final HBox hbox;
        
        public final BooleanProperty selectedProperty;
        
        
        private Matrix4d transfMatrix;
        
        public SceneObjectWrapper(String name, ProgressBar progressBar) {
            
            label = new Label(name);
            this.progressBar = progressBar;
            this.progressInfo = new Label();
            checkbox = new CheckBox();
            checkbox.setSelected(true);
            hbox = new HBox();
            hbox.getChildren().add(checkbox);
            hbox.getChildren().add(label);
            
            super.setSpacing(5.0);
            super.getChildren().add(hbox);
            super.getChildren().add(new HBox(5, this.progressBar, this.progressInfo));
            
            selectedProperty = checkbox.selectedProperty();
        }

        public SceneObjectWrapper(File file, ProgressBar progressBar) {
            
            label = new Label(file.getName());
            this.progressBar = progressBar;
            this.progressInfo = new Label();
            checkbox = new CheckBox();
            checkbox.setSelected(true);
            hbox = new HBox();
            hbox.getChildren().add(checkbox);
            hbox.getChildren().add(label);
            
            super.setSpacing(5.0);
            super.getChildren().add(hbox);
            super.getChildren().add(new HBox(5, this.progressBar, this.progressInfo));
            
            //addColumn(0, labelWrapper);
            //addColumn(1, progressBarWrapper);
            
            /*ColumnConstraints columnConstraints1 = new ColumnConstraints();
            columnConstraints1.setPercentWidth(50);
            
            ColumnConstraints columnConstraints2 = new ColumnConstraints();
            columnConstraints1.setPercentWidth(50);
            
            this.getColumnConstraints().addAll(columnConstraints1, columnConstraints2);*/
            
            //this.prefWidthProperty().bind(listviewTreeSceneObjects.widthProperty());
            
            selectedProperty = checkbox.selectedProperty();
        }

        public Label getLabel() {
            return label;
        }

        public ProgressBar getProgressBar() {
            return progressBar;
        }

        public Label getProgressInfo() {
            return progressInfo;
        }

        public SceneObject getSceneObject() {
            return sceneObject;
        }

        public void setSceneObject(SceneObject sceneObject) {
            this.sceneObject = sceneObject;
        }

        public Matrix4d getTransfMatrix() {
            return transfMatrix;
        }

        public void setTransfMatrix(Matrix4d transfMatrix) {
            this.transfMatrix = transfMatrix;
        }
        
        public boolean isSelected(){
            return checkbox.isSelected();
        }
        
        public void setSelected(boolean selected){
            checkbox.setSelected(selected);
        }
        
    }
