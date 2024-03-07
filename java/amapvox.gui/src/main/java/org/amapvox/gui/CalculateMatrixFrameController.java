package org.amapvox.gui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.amapvox.commons.math.util.MatrixUtility;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

/**
 * FXML Controller class
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class CalculateMatrixFrameController implements Initializable {
    
    private Stage stage;
    
    private Matrix4d matrix;
    
    @FXML
    private Button buttonAccept;
    @FXML
    private TextField textFieldPoint1X;
    @FXML
    private TextField textFieldPoint1Y;
    @FXML
    private TextField textFieldPoint1Z;
    @FXML
    private TextField textFieldPoint2X;
    @FXML
    private TextField textFieldPoint2Y;
    @FXML
    private TextField textFieldPoint2Z;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
    
    public void setStage(Stage stage){
        this.stage = stage;
    }

    public Matrix4d getMatrix() {
        return matrix;
    }

    @FXML
    private void onActionButtonAccept(ActionEvent event) {
        
        Vector3d point1 = new Vector3d(Double.valueOf(textFieldPoint1X.getText()), Double.valueOf(textFieldPoint1Y.getText()), Double.valueOf(textFieldPoint1Z.getText()));
        Vector3d point2 = new Vector3d(Double.valueOf(textFieldPoint2X.getText()), Double.valueOf(textFieldPoint2Y.getText()), Double.valueOf(textFieldPoint2Z.getText()));
        matrix = MatrixUtility.getMatrixTransformation(point1, point2);
        
        stage.close();
    }
    
}
