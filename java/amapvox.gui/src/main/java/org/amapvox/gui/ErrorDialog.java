/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * This is an utility class to show Exception to the user by a dialog frame
 * @author Julien Heurtebize
 */
public class ErrorDialog {
    
    public static void show(final Exception e){
        
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText(e.getLocalizedMessage());
                alert.setResizable(true);
                alert.getDialogPane().setPrefSize(500, 200);
                
                TextArea textArea = new TextArea(e.toString());
                textArea.setEditable(false);
                textArea.setWrapText(true);
                
                Label label = new Label("The exception stacktrace was:");

                textArea.setMaxWidth(Double.MAX_VALUE);
                textArea.setMaxHeight(Double.MAX_VALUE);
                GridPane.setVgrow(textArea, Priority.ALWAYS);
                GridPane.setHgrow(textArea, Priority.ALWAYS);

                GridPane expContent = new GridPane();
                expContent.setMaxWidth(Double.MAX_VALUE);
                expContent.add(label, 0, 0);
                expContent.add(textArea, 0, 1);

                // Set expandable Exception into the dialog pane.
                alert.getDialogPane().setExpandableContent(expContent);

                alert.showAndWait();
            }
        });
        
    }
}
