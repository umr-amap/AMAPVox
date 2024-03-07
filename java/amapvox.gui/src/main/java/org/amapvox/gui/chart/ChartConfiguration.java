/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.amapvox.gui.chart;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.amapvox.commons.AVoxTask;
import org.amapvox.commons.Configuration;
import org.amapvox.commons.Release;
import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 *
 * @author pverley
 */
public class ChartConfiguration extends Configuration {

    private final List<VoxelFileChart> listVoxelFileChart = new ArrayList();
    private String variableName;
    private VoxelsToChart.LayerReference layerReference = VoxelsToChart.LayerReference.FROM_ABOVE_GROUND;
    private boolean split = false;
    private VoxelsToChart.QuadratAxis splitAxis = VoxelsToChart.QuadratAxis.Y_AXIS;
    private int splitCount = 1;
    private int splitLength = -1;
    private int maxChartNumberInARow = 6;

    public ChartConfiguration() {
        super("CHART", "Vertical profile",
                "Create charts of vertical profiles");
    }

    @Override
    public Class<? extends AVoxTask> getTaskClass() {
        return ChartTask.class;
    }

    @Override
    public void readProcessElements(Element processElement) throws JDOMException, IOException {

        Element voxelFilesElement = processElement.getChild("voxel_files");
        voxelFilesElement.getChildren("voxel_file").forEach(voxelFileElement -> {
            addVoxelFileChart(new VoxelFileChart(
                    new File(voxelFileElement.getAttributeValue("src")),
                    voxelFileElement.getAttributeValue("label"),
                    Color.decode(voxelFileElement.getAttributeValue("color"))));
        });

        Element variableELement = processElement.getChild("variable");
        setVariableName(variableELement.getAttributeValue("name"));

        Element heightElement = processElement.getChild("height");
        setLayerReference(VoxelsToChart.LayerReference.valueOf(heightElement.getAttributeValue("from").toUpperCase()));

        Element splittingElement = processElement.getChild("splitting");
        setSplit(Boolean.parseBoolean(splittingElement.getAttributeValue("enabled")));
        setSplitAxis(VoxelsToChart.QuadratAxis.valueOf(splittingElement.getAttributeValue("axis")));
        setSplitCount(Integer.parseInt(splittingElement.getAttributeValue("count")));
        setSplitLength(Integer.parseInt(splittingElement.getAttributeValue("length")));

        Element maxChartElement = processElement.getChild("chart_in_row");
        setMaxChartNumberInARow(Integer.parseInt(maxChartElement.getAttributeValue("max")));
    }

    @Override
    public void writeProcessElements(Element processElement) throws JDOMException, IOException {

        // add voxel files
        Element voxelFilesElement = new Element("voxel_files");
        getListVoxelFileChart().forEach(vfc -> {
            Element voxelFile = new Element("voxel_file");
            voxelFile.setAttribute("src", vfc.file.getAbsolutePath());
            voxelFile.setAttribute("color", String.valueOf(vfc.getSeriesParameters().getColor().getRGB()));
            voxelFile.setAttribute("label", vfc.getSeriesParameters().getLabel());
            voxelFilesElement.addContent(voxelFile);
        });
        processElement.addContent(voxelFilesElement);

        // variable 
        Element variableELement = new Element("variable");
        variableELement.setAttribute("name", getVariableName());
        processElement.addContent(variableELement);

        Element heightElement = new Element("height");
        heightElement.setAttribute("from", getLayerReference().name());
        processElement.addContent(heightElement);

        Element splittingElement = new Element("splitting");
        splittingElement.setAttribute("enabled", String.valueOf(isSplit()));
        splittingElement.setAttribute("axis", getSplitAxis().name());
        splittingElement.setAttribute("count", String.valueOf(getSplitCount()));
        splittingElement.setAttribute("length", String.valueOf(getSplitLength()));
        processElement.addContent(splittingElement);

        Element maxChartElement = new Element("chart_in_row");
        maxChartElement.setAttribute("max", String.valueOf(getMaxChartNumberInARow()));
        processElement.addContent(maxChartElement);
    }

    /**
     * @return the listVoxelFileChart
     */
    public List<VoxelFileChart> getListVoxelFileChart() {
        return listVoxelFileChart;
    }

    public void addVoxelFileChart(VoxelFileChart vfc) {
        listVoxelFileChart.add(vfc);
    }

    /**
     * @return the variableName
     */
    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    /**
     * @return the heightFrom
     */
    public VoxelsToChart.LayerReference getLayerReference() {
        return layerReference;
    }

    /**
     * @return the split
     */
    public boolean isSplit() {
        return split;
    }

    /**
     * @return the splitAxis
     */
    public VoxelsToChart.QuadratAxis getSplitAxis() {
        return splitAxis;
    }

    /**
     * @return the splitCount
     */
    public int getSplitCount() {
        return splitCount;
    }

    /**
     * @return the splitLength
     */
    public int getSplitLength() {
        return splitLength;
    }

    /**
     * @return the maxChartNumberInARow
     */
    public int getMaxChartNumberInARow() {
        return maxChartNumberInARow;
    }

    /**
     * @param layerReference the layerReference to set
     */
    public void setLayerReference(VoxelsToChart.LayerReference layerReference) {
        this.layerReference = layerReference;
    }

    /**
     * @param split the split to set
     */
    public void setSplit(boolean split) {
        this.split = split;
    }

    /**
     * @param splitAxis the splitAxis to set
     */
    public void setSplitAxis(VoxelsToChart.QuadratAxis splitAxis) {
        this.splitAxis = splitAxis;
    }

    /**
     * @param splitCount the splitCount to set
     */
    public void setSplitCount(int splitCount) {
        this.splitCount = splitCount;
    }

    /**
     * @param splitLength the splitLength to set
     */
    public void setSplitLength(int splitLength) {
        this.splitLength = splitLength;
    }

    /**
     * @param maxChartNumberInARow the maxChartNumberInARow to set
     */
    public void setMaxChartNumberInARow(int maxChartNumberInARow) {
        this.maxChartNumberInARow = maxChartNumberInARow;
    }

    @Override
    public Release[] getReleases() {
        return null;
    }
}
