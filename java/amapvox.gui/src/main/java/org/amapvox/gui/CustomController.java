/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui;

import javafx.stage.Stage;

/**
 *
 * @author calcul
 */
public abstract class CustomController {
    
    protected Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
}
