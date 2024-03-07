/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.amapvox.gui.chart;

import java.io.File;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import org.amapvox.commons.AVoxTask;
import org.amapvox.commons.Configuration;
import org.amapvox.commons.javafx.chart.ChartViewer;
import org.jfree.chart.JFreeChart;

/**
 *
 * @author pverley
 */
public class ChartTask extends AVoxTask {

    public ChartTask(File file, int ncpu) {
        super(file, ncpu);
    }

    @Override
    protected Class<? extends Configuration> getConfigurationClass() {
        return ChartConfiguration.class;
    }

    @Override
    protected void doInit() throws Exception {
    }

    @Override
    public String getName() {
        return "Verticale profile chart";
    }

    @Override
    public File[] call() throws Exception {

        ChartConfiguration cfg = (ChartConfiguration) getConfiguration();

        Platform.runLater(() -> {

            int maxChartNumberInARow = Math.max(1, cfg.getMaxChartNumberInARow());

            ChartViewer chartViewer = new ChartViewer("Charts", 270, 600, maxChartNumberInARow);

            cfg.getListVoxelFileChart().forEach(voxelFileChart -> voxelFileChart.loaded = true);

            VoxelFileChart[] voxelFileChartArray = new VoxelFileChart[cfg.getListVoxelFileChart().size()];
            cfg.getListVoxelFileChart().toArray(voxelFileChartArray);

            VoxelsToChart voxelsToChart = new VoxelsToChart(voxelFileChartArray);

            if (cfg.isSplit()) {
                voxelsToChart.configureQuadrats(cfg.getSplitAxis(), cfg.getSplitCount(), cfg.getSplitLength());
            } else {
                voxelsToChart.configureQuadrats(VoxelsToChart.QuadratAxis.Y_AXIS, 1, -1);
            }

            // chart from variable profile
            JFreeChart[] charts = voxelsToChart.getAttributProfileCharts(
                    cfg.getVariableName(), cfg.getLayerReference());

            if (charts != null) {
                int chartWidth = 200;

                int nChartRow = Math.min(charts.length, maxChartNumberInARow);
                int stageWidth = nChartRow * chartWidth;
                Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
                double screenWidth = primaryScreenBounds.getWidth();
                if (stageWidth > screenWidth) {
                    chartViewer.getStage().setWidth(screenWidth);
                } else {
                    chartViewer.getStage().setWidth(stageWidth);
                }

                for (JFreeChart chart : charts) {
                    chartViewer.insertChart(chart);
                }
                chartViewer.show();

            }
        });
        return null;
    }

}
