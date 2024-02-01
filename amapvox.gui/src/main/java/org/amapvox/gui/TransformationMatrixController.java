package org.amapvox.gui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javax.vecmath.Matrix4d;

/**
 * FXML Controller class
 *
 * @author pverley
 */
public class TransformationMatrixController implements Initializable {

    @FXML
    private Label labelM00;
    @FXML
    private Label labelM10;
    @FXML
    private Label labelM20;
    @FXML
    private Label labelM30;
    @FXML
    private Label labelM01;
    @FXML
    private Label labelM11;
    @FXML
    private Label labelM21;
    @FXML
    private Label labelM31;
    @FXML
    private Label labelM02;
    @FXML
    private Label labelM12;
    @FXML
    private Label labelM22;
    @FXML
    private Label labelM32;
    @FXML
    private Label labelM03;
    @FXML
    private Label labelM13;
    @FXML
    private Label labelM23;
    @FXML
    private Label labelM33;

    private final SimpleBooleanProperty matrixChangedProperty = new SimpleBooleanProperty();

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Matrix4d identity = new Matrix4d();
        identity.setIdentity();
        setMatrix(identity);
    }
    
    public ReadOnlyBooleanProperty changedProperty() {
        return matrixChangedProperty;
    }

    public void setMatrix(Matrix4d matrix) {
        labelM00.setText(String.format("%f", matrix.m00));
        labelM00.setTooltip(new Tooltip(String.valueOf(matrix.m00)));
        labelM01.setText(String.format("%f", matrix.m01));
        labelM01.setTooltip(new Tooltip(String.valueOf(matrix.m01)));
        labelM02.setText(String.format("%f", matrix.m02));
        labelM02.setTooltip(new Tooltip(String.valueOf(matrix.m02)));
        labelM03.setText(String.format("%f", matrix.m03));
        labelM03.setTooltip(new Tooltip(String.valueOf(matrix.m03)));
        labelM10.setText(String.format("%f", matrix.m10));
        labelM10.setTooltip(new Tooltip(String.valueOf(matrix.m10)));
        labelM11.setText(String.format("%f", matrix.m11));
        labelM11.setTooltip(new Tooltip(String.valueOf(matrix.m11)));
        labelM12.setText(String.format("%f", matrix.m12));
        labelM12.setTooltip(new Tooltip(String.valueOf(matrix.m12)));
        labelM13.setText(String.format("%f", matrix.m13));
        labelM13.setTooltip(new Tooltip(String.valueOf(matrix.m13)));
        labelM20.setText(String.format("%f", matrix.m20));
        labelM20.setTooltip(new Tooltip(String.valueOf(matrix.m20)));
        labelM21.setText(String.format("%f", matrix.m21));
        labelM21.setTooltip(new Tooltip(String.valueOf(matrix.m21)));
        labelM22.setText(String.format("%f", matrix.m22));
        labelM22.setTooltip(new Tooltip(String.valueOf(matrix.m22)));
        labelM23.setText(String.format("%f", matrix.m23));
        labelM23.setTooltip(new Tooltip(String.valueOf(matrix.m23)));
        labelM30.setText(String.format("%f", matrix.m30));
        labelM30.setTooltip(new Tooltip(String.valueOf(matrix.m30)));
        labelM31.setText(String.format("%f", matrix.m31));
        labelM31.setTooltip(new Tooltip(String.valueOf(matrix.m31)));
        labelM32.setText(String.format("%f", matrix.m32));
        labelM32.setTooltip(new Tooltip(String.valueOf(matrix.m32)));
        labelM33.setText(String.format("%f", matrix.m33));
        labelM33.setTooltip(new Tooltip(String.valueOf(matrix.m33)));
        matrixChangedProperty.setValue(!matrixChangedProperty.get());
    }

    public void setDisable(boolean value) {

        String white = "-fx-background-color: white; -fx-text-fill: black;";
        String grey = "-fx-background-color: #F4F4F4; -fx-text-fill: lightgrey;";

        labelM00.setStyle(value ? grey : white);
        labelM01.setStyle(value ? grey : white);
        labelM02.setStyle(value ? grey : white);
        labelM03.setStyle(value ? grey : white);
        labelM10.setStyle(value ? grey : white);
        labelM11.setStyle(value ? grey : white);
        labelM12.setStyle(value ? grey : white);
        labelM13.setStyle(value ? grey : white);
        labelM20.setStyle(value ? grey : white);
        labelM21.setStyle(value ? grey : white);
        labelM22.setStyle(value ? grey : white);
        labelM23.setStyle(value ? grey : white);
        labelM30.setStyle(value ? grey : white);
        labelM31.setStyle(value ? grey : white);
        labelM32.setStyle(value ? grey : white);
        labelM33.setStyle(value ? grey : white);
    }

}
