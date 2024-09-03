/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author calcul
 */
public class HelpButtonController implements Initializable {
    
    final Logger LOGGER = Logger.getLogger(HelpButtonController.class);

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void showHelpDialog(String message) {
        
        LOGGER.info("[help] " + message);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setResizable(true);

        alert.setHeaderText("Help");
        alert.setContentText(message);

        alert.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));

        alert.show();
    }

}
