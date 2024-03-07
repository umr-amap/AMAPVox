package org.amapvox.gui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
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
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author calcul
 */
public class ViewCapsSetupFrameController implements Initializable {
    
    private float[] defaultAngles = ViewCaps.LAI_2200.getAngles();
    private int currentIndex = 1;
    private float currentAngle;
    
    private boolean confirmed;
    private Stage stage;
    private Parent root;
    
    @FXML
    private Arc arcViewCap;
    @FXML
    private Label labelAngleValue;
    @FXML
    private Circle circleViewCapFull;    
    
    public enum ViewCaps{
        
        LAI_2000(360, 10, 45, 180),
        LAI_2200(360, 10, 45, 90, 180, 270);
        
        private final float[] angles;
        
        private ViewCaps(float... angles){
            this.angles = angles;
        }
        
        public float[] getAngles(){
            return angles;
        }
    }
    
    public static ViewCapsSetupFrameController newInstance() {

        ViewCapsSetupFrameController controller = null;

        try {

            FXMLLoader loader = new FXMLLoader(ViewCapsSetupFrameController.class.getResource("/org/amapvox/gui/fxml/ViewCapsSetupFrame.fxml"));
            Parent root = loader.load();
            controller = loader.getController();
            controller.root = root;

        } catch (IOException ex) {
            Logger.getLogger(ViewCapsSetupFrameController.class.getName()).log(Level.SEVERE, "Failed to load ViewCapsSetupFrame.fxml", ex);
        }

        return controller;
    }
    
    public Stage getStage() {
        if (null == stage) {
            stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("View cap");
            stage.initModality(Modality.APPLICATION_MODAL);
        }
        return stage;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
//        //ce code récupère la fenêtre (stage) et initialise celle-ci
//        rootPane.sceneProperty().addListener(new ChangeListener<Scene>() {
//
//            @Override
//            public void changed(ObservableValue<? extends Scene> observable, Scene oldScene, Scene newScene) {
//                
//                if (oldScene == null && newScene != null) {
//                    newScene.windowProperty().addListener(new ChangeListener<Window>() {
//
//                        @Override
//                        public void changed(ObservableValue<? extends Window> observable, Window oldWindow, Window newWindow) {
//                            
//                            if (oldWindow == null && newWindow != null) {
//                                
//                                stage = (Stage) newWindow;
//                                stage.sizeToScene();
//                                stage.setTitle("View cap");
//                                
//                                stage.setMaxWidth(rootPane.getPrefWidth());
//                                stage.setMaxHeight(rootPane.getPrefHeight());
//                            }
//                        }
//                    });
//                }
//            }
//        });
        
        setViewCapAngles(ViewCaps.LAI_2200);
        setViewCap(360.0f);
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public void reset(){
        
        confirmed = false;
    }
    
    public void setViewCapAngles(ViewCaps viewCaps){
        defaultAngles = viewCaps.getAngles();
    }
    
    private void setViewCap(float angle){
        
        currentAngle= angle;
        float startAngle = 90 - (angle / 2.0f);
        if(startAngle < 0){
            startAngle += 360;
        }
        
        arcViewCap.setStartAngle(startAngle);
        arcViewCap.setLength(currentAngle);
        
        if(angle == 360){
            circleViewCapFull.setVisible(true);
        }else{
            circleViewCapFull.setVisible(false);
        }
        
        labelAngleValue.setText(String.valueOf(angle));
    }
    
    public float getAngle(){
        return currentAngle;
    }

    @FXML
    private void onMouseClickedOnArcViewCapListener(MouseEvent event) {
        
        setViewCap(defaultAngles[currentIndex]);
        currentIndex++;
        
        if(currentIndex >= defaultAngles.length){
            currentIndex = 0;
        }
    }

    @FXML
    private void onActionButtonConfirmViewCap(ActionEvent event) {
        
        confirmed = true;
        stage.close();
    }
    
}
