/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.amapvox.gui.viewer3d;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.amapvox.commons.AVoxTask;
import org.amapvox.commons.Configuration;
import org.apache.log4j.Logger;

/**
 *
 * @author pverley
 */
public class Viewer3dTask extends AVoxTask {
    
    private final static Logger LOGGER = Logger.getLogger(Viewer3dTask.class);

    public Viewer3dTask(File file, int ncpu) {
        super(file, ncpu);
    }

    @Override
    protected Class<? extends Configuration> getConfigurationClass() {
        return Viewer3dConfiguration.class;
    }

    @Override
    protected void doInit() throws Exception {
        // nothing to do
    }

    @Override
    public String getName() {
        return "Viewer 3D";
    }

    @Override
    public File[] call() throws Exception {
        
        Viewer3dConfiguration cfg = (Viewer3dConfiguration) getConfiguration();
        
        Map<String, String> parameters = new HashMap<>();

        parameters.put("vox-file", cfg.getVoxelFile().getAbsolutePath());
        parameters.put("vox-var", cfg.getVariableName());

        if (null != cfg.getDtmFile() && cfg.getDtmFile().exists()) {
            parameters.put("dtm-file", cfg.getDtmFile().getAbsolutePath());
            parameters.put("dtm-vop", cfg.getVopMatrix().toString());
            parameters.put("dtm-margin", String.valueOf(cfg.getDtmMargin()));
        }

        Platform.runLater(() -> {
            try {
                new org.amapvox.viewer3d.MainFX().initializeAndShowStage(parameters, new Stage());
            } catch (IOException ex) {
                LOGGER.error(ex);
            }
        });

        return null;
    }

}
