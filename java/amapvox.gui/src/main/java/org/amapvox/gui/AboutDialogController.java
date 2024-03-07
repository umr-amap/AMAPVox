/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui;

import java.net.URL;
import java.time.Year;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

/**
 *
 * @author pverley
 */
public class AboutDialogController implements Initializable {

    private Stage stage;

    @FXML
    private TextFlow description;
    @FXML
    private TextFlow properties;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Text tx1 = new Text("LiDAR data voxelization platform");
        tx1.setStyle("-fx-font-weight: bold; -fx-font-size: 1.5em");

        Text tx2 = new Text("\n\n");

        Text tx3 = new Text("Description: ");
        tx3.setStyle("-fx-font-weight: bold");

        StringBuilder sb = new StringBuilder();
        sb.append("AMAPvox tracks every laser pulse through ")
                .append("3D grid (voxelized space) and computes the local ")
                .append("transmittance or local attenuation per voxel.")
                .append("\n")
                .append("Aside R package provides visualization, utility and ")
                .append("validation tools for the voxelized space.")
                .append("\n");
        Text tx4 = new Text(sb.toString());

        Text tx5 = new Text("License: ");
        tx5.setStyle("-fx-font-weight: bold");

        Text tx6 = new Text("CeCILL ≥ 2\n");

        Text tx7 = new Text("Copyright ©: ");
        tx7.setStyle("-fx-font-weight: bold");

        sb = new StringBuilder();
        sb.append("ird.fr 2015-")
                .append(Year.now().getValue())
                .append("\n");
        Text tx8 = new Text(sb.toString());

        Text tx9 = new Text("URL: ");
        tx9.setStyle("-fx-font-weight: bold");

        Text tx10 = new Text("https://amapvox.org");

        description.getChildren().addAll(tx1, tx2, tx3, tx4, tx5, tx6, tx7, tx8, tx9, tx10);

        Text text1 = new Text("AMAPVox: ");
        text1.setStyle("-fx-font-weight: bold");
        Text text2 = new Text(org.amapvox.commons.Util.getVersion() + "\n");

        Text text3 = new Text("Java: ");
        text3.setStyle("-fx-font-weight: bold");

        sb = new StringBuilder();
        sb.append(System.getProperty("java.version")).append("; ")
                .append(System.getProperty("java.vm.name")).append(" ")
                .append(System.getProperty("java.vm.version")).append("\n");
        Text text4 = new Text(sb.toString());

        Text text5 = new Text("Runtime: ");
        text5.setStyle("-fx-font-weight: bold");

        sb = new StringBuilder();
        sb.append(System.getProperty("java.runtime.name")).append(" ")
                .append(System.getProperty("java.runtime.version")).append("\n");
        Text text6 = new Text(sb.toString());

        Text text7 = new Text("JavaFX: ");
        text7.setStyle("-fx-font-weight: bold");

        Text text8 = new Text(System.getProperty("javafx.version") + "\n");

        Text text9 = new Text("System: ");
        text9.setStyle("-fx-font-weight: bold");

        sb = new StringBuilder();
        sb.append(System.getProperty("os.name")).append(" version ")
                .append(System.getProperty("os.version")).append(" on ")
                .append(System.getProperty("os.arch"));
        Text text10 = new Text(sb.toString());

        properties.getChildren().addAll(text1, text2, text3, text4, text5, text6, text7, text8, text9, text10);
    }

    @FXML
    private void onActionButtonClose(ActionEvent event) {
        stage.close();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

}
