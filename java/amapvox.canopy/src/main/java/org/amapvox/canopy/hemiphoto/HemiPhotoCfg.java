/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */
package org.amapvox.canopy.hemiphoto;

import org.amapvox.commons.Matrix;
import org.amapvox.commons.util.io.file.FileManager;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.amapvox.canopy.CanopyCfg;
import org.amapvox.commons.AVoxTask;
import org.amapvox.commons.Release;
import org.amapvox.voxelisation.output.OutputVariable;
import org.jdom2.Element;

/**
 *
 * @author pverley
 */
public class HemiPhotoCfg extends CanopyCfg {

    // number of pixels
    private int pixelCount = 800;
    // number of meridians
    private int meridianCount = 36;
    // number of parallels
    private int parallelCount = 9;

    public int getPixelCount() {
        return pixelCount;
    }

    public void setPixelCount(int pixelCount) {
        this.pixelCount = pixelCount;
    }

    public int getMeridianCount() {
        return meridianCount;
    }

    public void setMeridianCount(int meridianCount) {
        this.meridianCount = meridianCount;
    }

    public int getParallelCount() {
        return parallelCount;
    }

    public void setParallelCount(int parallelCount) {
        this.parallelCount = parallelCount;
    }

    public HemiPhotoCfg() {
        super("HEMI_PHOTO", "Hemispheral Photography",
                "Generates hemispherical photography from a voxel file or a single lidar scan.");
    }

    @Override
    public Class<? extends AVoxTask> getTaskClass() {
        return HemiPhotoTask.class;
    }

    @Override
    public void readParameters(Element parametersElement) throws IOException {

        // pixel count
        if (null != parametersElement.getAttribute("pixel-count")) {
            setPixelCount(Integer.parseInt(parametersElement.getAttributeValue("pixel-count")));
        }
        // meridian count
        if (null != parametersElement.getAttribute("meridian-count")) {
            setMeridianCount(Integer.parseInt(parametersElement.getAttributeValue("meridian-count")));
        }
        // parallel count
        if (null != parametersElement.getAttribute("parallel-count")) {
            setParallelCount(Integer.parseInt(parametersElement.getAttributeValue("parallel-count")));
        }

    }

    @Override
    public void writeParameters(Element parametersElement) {

        // number of pixels
        parametersElement.setAttribute("pixel-count", String.valueOf(getPixelCount()));
        // number of meridians
        parametersElement.setAttribute("meridian-count", String.valueOf(getMeridianCount()));
        // number of parallels
        parametersElement.setAttribute("parallel-count", String.valueOf(getParallelCount()));
    }

    @Override
    public Release[] getReleases() {

        return new Release[]{
            // 2022-01-12
            new Release("1.9.3") {
                @Override
                public void update(Element processElement) {

                    String processTypeValue = processElement.getAttributeValue("type");
                    if ("0".equals(processTypeValue)) {
                        Element inputFilesElement = processElement.getChild("input_files");
                        // renamed input_files into scans
                        inputFilesElement.setName("scans");
                        List<Element> scansElement = inputFilesElement.getChildren("scan");
                        scansElement.forEach(scanElement -> {
                            // delete input_scan/src attr to scan/src attr
                            scanElement.setAttribute("src", scanElement.getChild("input_file").getAttributeValue("src"));
                            // delete input_file element
                            scanElement.removeChild("input_file");
                            // update scan sop matrix
                            Element matrixElement = scanElement.getChild("SOP");
                            Matrix matrix = Matrix.valueOf(matrixElement.getText());
                            matrix.setId("sop");
                            scanElement.removeChild("SOP");
                            scanElement.addContent(matrix.toElement());
                        });

                    }
                }
            },
            // 2023-03-23
            new Release("2.0.1") {
                @Override
                public void update(Element processElement) {

                    Element inputFileElement = processElement.getChild("input_file");
                    if (null != inputFileElement) {
                        inputFileElement.setAttribute("variable", OutputVariable.PLANT_AREA_DENSITY.getShortName());
                    }
                }
            },
            // 2026-10
            new Release("2.5.0") {
                @Override
                public void update(Element processElement) {

                    if (null != processElement.getAttribute("type")
                            && Integer.parseInt(processElement.getAttributeValue("type")) == 0) {
                        throw new UnsupportedOperationException("Hemispherical photographs from RSP/RXP scans is deprecated since v2.5.0");
                    }

                    // rename sensor-position element into sensor-positions
                    if (null != processElement.getChild("sensor-position")) {
                        Element sensorPositionElement = processElement.getChild("sensor-position");
                        Element sensorPositionsElement = new Element("sensor-positions");
                        Element positionElement = new Element("position");
                        positionElement.setAttribute("x", sensorPositionElement.getAttributeValue("x"));
                        positionElement.setAttribute("y", sensorPositionElement.getAttributeValue("y"));
                        positionElement.setAttribute("z", sensorPositionElement.getAttributeValue("z"));
                        sensorPositionsElement.addContent(positionElement);
                        processElement.addContent(sensorPositionsElement);
                        processElement.removeContent(sensorPositionElement);
                    }

                    // remove process element mode attribute
                    processElement.removeAttribute("mode");

                    // remove process element type attribute
                    processElement.removeAttribute("type");

                    // rename input_file element to <input>
                    Element inputFileElement = processElement.getChild("input_file");
                    inputFileElement.setName("input");

                    // remove input element type attribute
                    inputFileElement.removeAttribute("type");

                    // rename output_file element to output
                    Element outputFilesElement = processElement.getChild("output_files");
                    Element outputElement = new Element("output");

                    // set output element src attribute
                    String outputDir = outputFilesElement.getChild("output_bitmap_file").getAttributeValue("src");
                    outputElement.setAttribute("src", outputDir);

                    // set output element prefix attribute
                    File voxelFile = new File(inputFileElement.getAttributeValue("src"));
                    String prefix = voxelFile.isFile()
                            ? FileManager.removeFileExtension(voxelFile.getName())
                            : "hemiphoto";
                    outputElement.setAttribute("prefix", prefix);

                    // add paremeters element to output element
                    processElement.addContent(outputElement);

                    // create parameters element
                    Element parametersElement = new Element("parameters");

                    // add pixel-number to output/parameters attribute
                    if (null != processElement.getChild("pixel-number")) {
                        Element pixelNumberElement = processElement.getChild("pixel-number");
                        parametersElement.setAttribute("pixel-count", pixelNumberElement.getAttributeValue("value"));
                    }

                    // add azimut-number to output/paremters attribute
                    // rename it to meridian-count
                    if (null != processElement.getChild("azimut-number")) {
                        Element azimutNumberElement = processElement.getChild("azimut-number");
                        parametersElement.setAttribute("meridian-count", azimutNumberElement.getAttributeValue("value"));
                    }

                    // add zenith-number to output/paremters attribute
                    // rename it to parallel-count
                    if (null != processElement.getChild("zenith-number")) {
                        Element zenithNumberElement = processElement.getChild("zenith-number");
                        parametersElement.setAttribute("parallel-count", zenithNumberElement.getAttributeValue("value"));
                    }

                    // add paremeters element to process element
                    processElement.addContent(parametersElement);

                    // remove pixel-number element
                    processElement.removeChild("pixel-number");

                    // remove pixel-number element
                    processElement.removeChild("azimut-number");

                    // remove zenith-number element
                    processElement.removeChild("zenith-number");

                    // remove output files element
                    processElement.removeChild("output_files");
                }
            }
        };
    }
}
