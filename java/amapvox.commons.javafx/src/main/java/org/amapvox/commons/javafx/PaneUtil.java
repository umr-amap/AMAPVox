/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.javafx;

import javafx.scene.Node;
import javafx.scene.layout.GridPane;

/**
 *
 * @author Julien Heurtebize
 */
public class PaneUtil {
    
    public static Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            
            try{
                if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                    return node;
                }
            }catch(Exception ex){}
            
        }
        return null;
    }
}
