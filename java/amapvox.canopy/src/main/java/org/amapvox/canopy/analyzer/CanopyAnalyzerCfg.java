/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.canopy.analyzer;

import java.io.File;
import java.io.IOException;
import org.amapvox.canopy.CanopyCfg;
import org.amapvox.canopy.LeafAngleDistribution;
import org.amapvox.commons.AVoxTask;
import org.amapvox.commons.Release;
import org.amapvox.commons.util.io.file.FileManager;
import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 *
 * @author pverley
 */
public class CanopyAnalyzerCfg extends CanopyCfg {

    private int directionCount;

    private CanopyAnalyzer canopyAnalyzer;

    public enum CanopyAnalyzer {
        LAI2000,
        LAI2200;
    }

    private boolean[] masks; //ring masks

    public CanopyAnalyzerCfg() {
        super("CANOPY_ANALYZER", "Canopy Analyzer (LAI2000/2200)",
                "Simulates LAI2000/2200 sensors from a voxel file and sensor parameters.",
                new String[]{"LAI2000", "LAI2200"});
    }

    public int getDirectionCount() {
        return directionCount;
    }

    public void setDirectionCount(int directionCount) {
        this.directionCount = directionCount;
    }

    public CanopyAnalyzer getCanopyAnalyzer() {
        return canopyAnalyzer;
    }

    public void setCanopyAnalyzer(CanopyAnalyzer mode) {
        this.canopyAnalyzer = mode;
    }

    public boolean[] getMasks() {
        return masks;
    }

    public void setMasks(boolean[] masks) {
        this.masks = masks;
    }

    @Override
    public Class<? extends AVoxTask> getTaskClass() {
        return CanopyAnalyzerTask.class;
    }

    @Override
    public void readParameters(Element parametersElement) throws IOException, JDOMException {

        // device 
        canopyAnalyzer = CanopyAnalyzer.valueOf(parametersElement.getAttributeValue("device"));

        // direction count
        setDirectionCount(Integer.parseInt(parametersElement.getAttributeValue("direction-count")));

        // ring mask
        boolean[] ringMasks = new boolean[5]; // false by default (no mask)
        for (int i = 0; i < 5; i++) {
            String mask = "mask-ring-" + (i + 1);
            if (null != parametersElement.getAttribute(mask)) {
                ringMasks[i] = Boolean.parseBoolean(parametersElement.getAttributeValue(mask));
            }
        }
        setMasks(ringMasks);
    }

    @Override
    public void writeParameters(Element parametersElement) {

        parametersElement.setAttribute("device", canopyAnalyzer.toString());
        parametersElement.setAttribute("direction-count", String.valueOf(directionCount));
        parametersElement.setAttribute("mask-ring-1", String.valueOf(masks[0]));
        parametersElement.setAttribute("mask-ring-2", String.valueOf(masks[1]));
        parametersElement.setAttribute("mask-ring-3", String.valueOf(masks[2]));
        parametersElement.setAttribute("mask-ring-4", String.valueOf(masks[3]));
        parametersElement.setAttribute("mask-ring-5", String.valueOf(masks[4]));
    }

    @Override
    public Release[] getReleases() {
        return new Release[]{
            // 2024-09-03
            new Release("2.3.0") {
                @Override
                public void update(Element processElement) {

                    // add default spherical leaf angle distribution element
                    if (null == processElement.getChild("leaf-angle-distribution")) {
                        Element leafAngleDistributionElement = new Element("leaf-angle-distribution");
                        leafAngleDistributionElement.setAttribute("type", LeafAngleDistribution.Type.SPHERIC.toString());
                        processElement.addContent(leafAngleDistributionElement);
                    }
                }
            },
            //2026-10
            new Release("2.5.0") {
                @Override
                public void update(Element processElement) {

                    // rename scanners-positions into sensor-positions
                    if (null != processElement.getChild("scanners-positions")) {
                        processElement.getChild("scanners-positions").setName("sensor-positions");
                    }

                    // rename ring-masks into parameters
                    if (null != processElement.getChild("ring-masks")) {
                        processElement.getChild("ring-masks").setName("parameters");
                    } else {
                        Element parametersElement = new Element("parameters");
                        parametersElement.setAttribute("mask-ring-1", String.valueOf(false));
                        parametersElement.setAttribute("mask-ring-2", String.valueOf(false));
                        parametersElement.setAttribute("mask-ring-3", String.valueOf(false));
                        parametersElement.setAttribute("mask-ring-4", String.valueOf(false));
                        parametersElement.setAttribute("mask-ring-5", String.valueOf(false));
                        processElement.addContent(parametersElement);
                    }

                    // add sensor in parameters (2200 by default)
                    Element parametersElement = processElement.getChild("parameters");
                    parametersElement.setAttribute("device", CanopyAnalyzer.LAI2200.toString());

                    // direction count (4000 by default)
                    int dirCount = 4000;
                    Element directionsNumberElement = processElement.getChild("directions-number");
                    if (directionsNumberElement != null) {
                        String directionsNumberValue = directionsNumberElement.getAttributeValue("value");
                        if (directionsNumberValue != null) {
                            dirCount = Integer.parseInt(directionsNumberValue);
                        }
                    }
                    parametersElement.setAttribute("direction-count", String.valueOf(dirCount));

                    // remove useless elements
                    processElement.removeChild("directions-number");
                    processElement.removeChild("directions-rotation");
                    processElement.removeChild("toricity");
                    processElement.removeChild("simulation-periods");
                    processElement.removeChild("latitude");

                    // rename input_file element to <input>
                    Element inputFileElement = processElement.getChild("input_file");
                    inputFileElement.setName("input");

                    // remove input element type attribute
                    inputFileElement.removeAttribute("type");
          
                    // create new output element
                    Element outputElement = new Element("output");

                    // set output element src attribute
                    Element outputFilesElement = processElement.getChild("output_files");
                    File outputTextFile = new File(outputFilesElement.getChild("output_text_file").getAttributeValue("src"));
                    outputElement.setAttribute("src", outputTextFile.getParent());

                    // set output element prefix attribute
                    File voxelFile = new File(inputFileElement.getAttributeValue("src"));
                    String prefix = FileManager.removeFileExtension(voxelFile.getName());
                    outputElement.setAttribute("prefix", prefix);

                    // add output element to output element
                    processElement.addContent(outputElement);
                    
                    // remove deprecated output_files element
                    processElement.removeChild("output_files");
                }
            }};
    }

}
