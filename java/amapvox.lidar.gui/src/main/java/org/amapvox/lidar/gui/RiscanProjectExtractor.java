/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.gui;

import org.amapvox.lidar.commons.LidarScan;
import org.amapvox.lidar.commons.LidarProjectReader;
import org.amapvox.lidar.riegl.RSPReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import javafx.scene.control.CheckBoxTreeItem;

/**
 *
 * @author calcul
 */
public class RiscanProjectExtractor extends LidarProjectExtractor {

    private void setSelected(boolean selected, String s) {

        controller.getRoot().getChildren().stream()
                .forEach(treeItem -> ((CheckBoxTreeItem) treeItem).setSelected(treeItem.getValue().getFile().getName().contains(s) ? selected : !selected));
    }

    @Override
    public void onSucceeded(List<LidarScan> scans) {
        
        // append FULL & MON menu item to selection menu button
        controller.getSelectMenuButton().addMenuItem("FULL", event -> setSelected(false, "mon"));
        controller.getSelectMenuButton().addMenuItem("MON", event -> setSelected(true, "mon"));

        super.onSucceeded(scans);

        boolean fullAndMonDefined = scans.stream().anyMatch(scan -> scan.getFile().getName().contains("mon"))
                && !scans.stream().allMatch(scan -> scan.getFile().getName().contains("mon"));

        if (fullAndMonDefined) {
            controller.getSelectMenuButton().getMenuItem("FULL").setDisable(false);
            controller.getSelectMenuButton().getMenuItem("MON").setDisable(false);
            // perform first click to disable MON file
            controller.getSelectMenuButton().getMenuItem("FULL").fire();
        } else {
            controller.getSelectMenuButton().getMenuItem("FULL").setDisable(true);
            controller.getSelectMenuButton().getMenuItem("MON").setDisable(true);
        }
    }

    @Override
    public LidarProjectReader getReader(File file) throws FileNotFoundException, IOException {
        return new RSPReader(file);
    }
}
