package org.amapvox.gui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javax.vecmath.Point3f;

/**
 * FXML Controller class
 *
 * @author Julien Heurtebize
 */
public class VoxelSpacePanelController implements Initializable {

    private ChangeListener changeListener;
    private Point3f resolution = new Point3f(1.f, 1.f, 1.f);
    
    @FXML
    private TextField textfieldXNumber;
    @FXML
    private TextField textfieldYNumber;
    @FXML
    private TextField textfieldZNumber;
    @FXML
    private TextField textfieldEnterXMin;
    @FXML
    private TextField textfieldEnterXMax;
    @FXML
    private TextField textfieldEnterYMin;
    @FXML
    private TextField textfieldEnterYMax;
    @FXML
    private TextField textfieldEnterZMin;
    @FXML
    private TextField textfieldEnterZMax;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        changeListener = new ChangeListener<Object>() {

            @Override
            public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {

                try {

                    //float resolution = Float.valueOf(textFieldResolution.getText());

                    double minPointX = Double.valueOf(textfieldEnterXMin.getText());
                    double maxPointX = Double.valueOf(textfieldEnterXMax.getText());
                    int voxelNumberX = (int) Math.ceil((maxPointX - minPointX) / resolution.x);
                    textfieldXNumber.setText(String.valueOf(voxelNumberX));

                    double minPointY = Double.valueOf(textfieldEnterYMin.getText());
                    double maxPointY = Double.valueOf(textfieldEnterYMax.getText());
                    int voxelNumberY = (int) Math.ceil((maxPointY - minPointY) / resolution.y);
                    textfieldYNumber.setText(String.valueOf(voxelNumberY));

                    double minPointZ = Double.valueOf(textfieldEnterZMin.getText());
                    double maxPointZ = Double.valueOf(textfieldEnterZMax.getText());
                    int voxelNumberZ = (int) Math.ceil((maxPointZ - minPointZ) / resolution.z);
                    textfieldZNumber.setText(String.valueOf(voxelNumberZ));

                } catch (Exception e) {

                }

            }
        };

        textfieldEnterXMin.textProperty().addListener(changeListener);
        textfieldEnterYMin.textProperty().addListener(changeListener);
        textfieldEnterZMin.textProperty().addListener(changeListener);

        textfieldEnterXMax.textProperty().addListener(changeListener);
        textfieldEnterYMax.textProperty().addListener(changeListener);
        textfieldEnterZMax.textProperty().addListener(changeListener);
    }

    public ChangeListener getChangeListener() {
        return changeListener;
    }

    public Point3f getResolution() {
        return resolution;
    }

    public void setResolution(Point3f resolution) {
        this.resolution = resolution;
    }

    public TextField getTextFieldXNumber() {
        return textfieldXNumber;
    }

    public TextField getTextFieldYNumber() {
        return textfieldYNumber;
    }

    public TextField getTextFieldZNumber() {
        return textfieldZNumber;
    }

    public TextField getTextFieldEnterXMin() {
        return textfieldEnterXMin;
    }

    public TextField getTextFieldEnterXMax() {
        return textfieldEnterXMax;
    }

    public TextField getTextFieldEnterYMin() {
        return textfieldEnterYMin;
    }

    public TextField getTextFieldEnterYMax() {
        return textfieldEnterYMax;
    }

    public TextField getTextFieldEnterZMin() {
        return textfieldEnterZMin;
    }

    public TextField getTextFieldEnterZMax() {
        return textfieldEnterZMax;
    }
}
