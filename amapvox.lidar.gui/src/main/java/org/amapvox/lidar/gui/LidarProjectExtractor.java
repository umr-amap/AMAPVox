/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.gui;

import org.amapvox.commons.util.IteratorWithException;
import org.amapvox.commons.util.ProcessingAdapter;
import org.amapvox.lidar.commons.LidarProjectReader;
import org.amapvox.lidar.commons.LidarScan;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Julien Heurtebize
 */
public abstract class LidarProjectExtractor {

    private final static Logger LOGGER = Logger.getLogger(LidarProjectExtractor.class.getCanonicalName());

    protected LidarProjectExtractorController controller;
    protected Stage stage;

    private Service<List<LidarScan>> service;

    abstract public LidarProjectReader getReader(File file) throws FileNotFoundException, IOException;

    /**
     * Extract single scans from LiDAR project and add them to table view.
     *
     * @param file
     * @throws Exception
     */
    public void read(File file) throws Exception {

        stage.setTitle(file.getAbsolutePath());
        controller.getRoot().getChildren().clear();
        controller.getRoot().setExpanded(true);
        controller.setDisable(true);

        LidarProjectReader reader = getReader(file);

        service = new Service() {
            @Override
            protected Task createTask() {

                return new Task<List<LidarScan>>() {
                    @Override
                    protected List<LidarScan> call() throws Exception {
                        updateMessage("Reading PTX file " + file);
                        reader.addProcessingListener(new ProcessingAdapter() {
                            @Override
                            public void processingStepProgress(String progressMsg, long progress, long max) {
                                updateMessage(progressMsg);
                                updateProgress(progress, max);
                            }
                        });

                        List<LidarScan> scans = new ArrayList();

                        IteratorWithException<LidarScan> it = reader.iterator();
                        int nscan = 0;
                        while (it.hasNext()) {
                            LidarScan scan = it.next();
                            scans.add(scan);
                            nscan++;
                            Platform.runLater(() -> {
                                CheckBoxTreeItem<LidarScan> item = new CheckBoxTreeItem(scan);
                                item.setSelected(true);
                                item.setExpanded(true);
                                controller.getRoot().getChildren().add(item);
                            });
                        }
                        updateMessage("Extracted " + nscan + " scan(s)");
                        updateProgress(0, 0);
                        return scans;
                    }
                };
            }
        };

        service.setOnCancelled(workerStateEvent -> {
            reader.setCancelled(true);
        });

        service.setOnSucceeded(workerStateEvent -> {
            onSucceeded(service.getValue());
        });

        controller.setService(service);
        service.start();
    }

    public void onSucceeded(List<LidarScan> scans) {

        if (!scans.isEmpty()) {
            controller.setScanSelected(true);
            if (scans.size() > 1) {
                controller.setDisable(false);
            }
        }
    }

    public LidarProjectExtractor() {

        try {
            FXMLLoader loader = new FXMLLoader(LidarProjectExtractor.class.getResource("fxml/LidarProjectExtractor.fxml"));
            Parent root = loader.load();
            stage = new Stage();
            controller = loader.getController();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            controller.setStage(stage);

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Cannot load LidarProjectExtractor.fxml", ex);
        }
    }

    public LidarProjectExtractorController getController() {
        return controller;
    }

    public Stage getFrame() {
        return stage;
    }

}
