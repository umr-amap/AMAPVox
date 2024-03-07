/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui;

import org.amapvox.commons.util.LineCount;
import org.amapvox.voxelfile.VoxelFileHeader;
import org.amapvox.voxelfile.VoxelFileReader;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 *
 * @author pverley
 */
public class CheckMissingVoxelController implements Initializable, ChangeListener<String> {

    @FXML
    private HBox hboxMissingVoxel;
    @FXML
    private Label labelMessage;
    @FXML
    private Label labelCheckResult;
    @FXML
    ProgressIndicator progressIndicator;

    private Service<Boolean> service;
    private File voxFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        hboxMissingVoxel.setManaged(false);
        hboxMissingVoxel.setVisible(false);

        ImageView errorImageView = new ImageView(new Image("fxml/icons/error.png"));
        errorImageView.setPreserveRatio(true);
        errorImageView.setFitHeight(16);

        ImageView validImageView = new ImageView(new Image("fxml/icons/valid.png"));
        validImageView.setPreserveRatio(true);
        validImageView.setFitHeight(16);

        ImageView warningImageView = new ImageView(new Image("fxml/icons/warning.png"));
        warningImageView.setPreserveRatio(true);
        warningImageView.setFitHeight(16);

        // new check missing voxel service
        service = new Service() {
            @Override
            protected Task createTask() {
                return new CheckMissingVoxelTask();
            }
        };
        // property binding
        progressIndicator.visibleProperty().bind(service.runningProperty());
        progressIndicator.managedProperty().bind(service.runningProperty());
        // on succeeded
        service.setOnSucceeded(workerStateEvent -> {
            boolean missingVoxel = service.getValue();
            labelMessage.setText(missingVoxel
                    ? "Missing voxels in " + voxFile.getName() + ". Post-processing analysis might be inconsistent."
                    : "No missing voxels in " + voxFile.getName() + ". You can carry-on with post-processing analysis.");
            labelCheckResult.setManaged(true);
            labelCheckResult.setVisible(true);
            labelCheckResult.setGraphic(missingVoxel ? warningImageView : validImageView);
        });
        // on failed
        service.setOnFailed(workerStateEvent -> {
            labelCheckResult.setManaged(true);
            labelCheckResult.setVisible(true);
            labelCheckResult.setGraphic(errorImageView);
            labelMessage.setText("Failed to check for missing voxels in " + voxFile.getName() + " (" + service.getException().getMessage() + ")");
        });
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (null != newValue && !newValue.isEmpty()) {
            voxFile = new File(newValue);
            labelMessage.setText("Looking for missing voxels in " + voxFile.getName());
            hboxMissingVoxel.setManaged(true);
            hboxMissingVoxel.setVisible(true);
            labelCheckResult.setManaged(false);
            labelCheckResult.setVisible(false);
            service.restart();
        } else {
            hboxMissingVoxel.setManaged(false);
            hboxMissingVoxel.setVisible(false);
        }
    }

    class CheckMissingVoxelTask extends Task<Boolean> {

        @Override
        protected Boolean call() throws Exception {
            //Thread.sleep(2000);
            VoxelFileHeader header = new VoxelFileReader(voxFile).getHeader();
            int nvoxel = header.getDimension().x * header.getDimension().y * header.getDimension().z;
            int nline = LineCount.count(voxFile.getAbsolutePath());
            // coarse check
            return nvoxel != (nline - header.getNLine());
        }
    }

}
