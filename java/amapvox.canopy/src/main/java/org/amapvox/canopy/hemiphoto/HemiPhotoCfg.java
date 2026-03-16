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

import org.amapvox.commons.Configuration;
import org.amapvox.commons.Matrix;
import org.amapvox.commons.util.io.file.FileManager;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3d;
import org.amapvox.canopy.LeafAngleDistribution;
import org.amapvox.commons.AVoxTask;
import org.amapvox.commons.Release;
import org.amapvox.voxelisation.output.OutputVariable;
import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 *
 * @author pverley
 */
public class HemiPhotoCfg extends Configuration {

    // voxel file
    private File voxelFile;
    // PAD variable
    private String padVariable;
    // leaf angle distribution
    private LeafAngleDistribution.Type leafAngleDistribution;
    // leaf angle distribution parameters
    private double[] leafAngleDistributionParameters = new double[2];
    // sensor positions
    private List<Point3d> sensorPositions;
    // output directory
    private File outputDir;
    // output prefix
    private String outputPrefix;
    // pixel number
    private int pixelNumber = 800;
    // meridian number
    private int meridianNumber = 36;
    // parallel number
    private int parallelNumber = 9;

    public File getVoxelFile() {
        return voxelFile;
    }

    public void setVoxelFile(File voxelFile) {
        this.voxelFile = voxelFile;
    }

    public String getPADVariable() {
        return padVariable;
    }

    public void setPADVariable(String padVariable) {
        this.padVariable = padVariable;
    }

    public List<Point3d> getSensorPositions() {
        return sensorPositions;
    }

    public void setSensorPositions(List<Point3d> sensorPositions) {
        this.sensorPositions = sensorPositions;
    }

    public int getPixelNumber() {
        return pixelNumber;
    }

    public void setPixelNumber(int pixelNumber) {
        this.pixelNumber = pixelNumber;
    }

    public int getMeridianNumber() {
        return meridianNumber;
    }

    public void setMeridianNumber(int azimutsNumber) {
        this.meridianNumber = azimutsNumber;
    }

    public int getParallelNumber() {
        return parallelNumber;
    }

    public void setParallelNumber(int zenithsNumber) {
        this.parallelNumber = zenithsNumber;
    }

    public File getOutputDirectory() {
        return outputDir;
    }

    public void setOutputDirectory(File directory) {
        this.outputDir = directory;
    }

    public String getOutputPrefix() {
        return outputPrefix;
    }

    public void setOutputPrefix(String prefix) {
        this.outputPrefix = prefix;
    }

    /**
     * @return the leafAngleDistribution
     */
    public LeafAngleDistribution.Type getLeafAngleDistribution() {
        return leafAngleDistribution;
    }

    /**
     * @param leafAngleDistribution the leafAngleDistribution to set
     */
    public void setLeafAngleDistribution(LeafAngleDistribution.Type leafAngleDistribution) {
        this.leafAngleDistribution = leafAngleDistribution;
    }

    /**
     * @return the leafAngleDistribution parameters
     */
    public double[] getLeafAngleDistributionParameters() {
        return leafAngleDistributionParameters;
    }

    /**
     * @param leafAngleDistributionParameters
     */
    public void setLeafAngleDistributionParameters(double[] leafAngleDistributionParameters) {
        this.leafAngleDistributionParameters = leafAngleDistributionParameters;
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
    public void readProcessElements(Element processElement) throws IOException {

        Element inputElement = processElement.getChild("input");
        String inputFileSrc = resolve(inputElement.getAttributeValue("src"));

        if (inputFileSrc != null) {
            setVoxelFile(new File(inputFileSrc));
        }

        if (null != inputElement.getAttribute("variable")) {
            setPADVariable(inputElement.getAttributeValue("variable"));
        }

        Element ladElement = processElement.getChild("leaf-angle-distribution");
        if (ladElement != null) {
            setLeafAngleDistribution(LeafAngleDistribution.Type.fromString(ladElement.getAttributeValue("type")));
            double[] ladParams = new double[2];
            String alphaValue = ladElement.getAttributeValue("alpha");
            if (alphaValue != null) {
                ladParams[0] = Double.parseDouble(alphaValue);
            }
            String betaValue = ladElement.getAttributeValue("beta");
            if (betaValue != null) {
                ladParams[0] = Double.parseDouble(betaValue);
            }
            setLeafAngleDistributionParameters(ladParams);
        } else {
            throw new IOException("Cannot find leaf-angle-distribution element");
        }

        List<Point3d> positions = new ArrayList<>();
        Element sensorPositionsElement = processElement.getChild("sensor-positions");
        List<Element> children = sensorPositionsElement.getChildren("position");
        children.forEach((element) -> {
            positions.add(new Point3d(
                    Double.parseDouble(element.getAttributeValue("x")),
                    Double.parseDouble(element.getAttributeValue("y")),
                    Double.parseDouble(element.getAttributeValue("z"))));
        });
        setSensorPositions(positions);

        //output
        Element outputElement = processElement.getChild("output");
        if (null != outputElement) {
            // output directory
            File output = new File(resolve(outputElement.getAttributeValue("src")));
            setOutputDirectory(output);
            // output prefix
            String prefix = outputElement.getAttributeValue("prefix");
            setOutputPrefix(prefix);

            Element parametersElement = outputElement.getChild("parameters");
            if (null != parametersElement) {
                // pixel number
                if (null != parametersElement.getAttribute("pixel-number")) {
                    setPixelNumber(Integer.parseInt(parametersElement.getAttributeValue("pixel-number")));
                }
                // number of meridians
                if (null != parametersElement.getAttribute("meridian-number")) {
                    setMeridianNumber(Integer.parseInt(parametersElement.getAttributeValue("meridian-number")));
                }
                // number of parallels
                if (null != parametersElement.getAttribute("parallel-number")) {
                    setParallelNumber(Integer.parseInt(parametersElement.getAttributeValue("parallel-number")));
                }
            }
        }
    }

    @Override
    public void writeProcessElements(Element processElement) {

        //input
        Element inputElement = new Element("input");
        inputElement.setAttribute("src", getVoxelFile().getAbsolutePath());
        inputElement.setAttribute("variable", getPADVariable());
        processElement.addContent(inputElement);

        // leaf angle distribution
        Element ladElement = new Element("leaf-angle-distribution");
        ladElement.setAttribute("type", getLeafAngleDistribution().toString());
        processElement.addContent(ladElement);

        if (getLeafAngleDistribution() == LeafAngleDistribution.Type.TWO_PARAMETER_BETA
                || getLeafAngleDistribution() == LeafAngleDistribution.Type.ELLIPSOIDAL) {
            ladElement.setAttribute("alpha", String.valueOf(getLeafAngleDistributionParameters()[0]));

            if (getLeafAngleDistribution() == LeafAngleDistribution.Type.TWO_PARAMETER_BETA) {
                ladElement.setAttribute("beta", String.valueOf(getLeafAngleDistributionParameters()[1]));
            }
        }

        // sensor positions
        Element sensorPositionsElement = new Element("sensor-positions");

        getSensorPositions().forEach(position -> {
            Element positionElement = new Element("position");
            positionElement.setAttribute("x", String.valueOf(position.x));
            positionElement.setAttribute("y", String.valueOf(position.y));
            positionElement.setAttribute("z", String.valueOf(position.z));
            sensorPositionsElement.addContent(positionElement);
        });

        processElement.addContent(sensorPositionsElement);

        //common parameters
        Element pixelNumberElement = new Element("pixel-number");
        pixelNumberElement.setAttribute("value", String.valueOf(getPixelNumber()));
        processElement.addContent(pixelNumberElement);

        Element azimutsNumberElement = new Element("azimut-number");
        azimutsNumberElement.setAttribute("value", String.valueOf(getMeridianNumber()));
        processElement.addContent(azimutsNumberElement);

        Element zenithNumberElement = new Element("zenith-number");
        zenithNumberElement.setAttribute("value", String.valueOf(getParallelNumber()));
        processElement.addContent(zenithNumberElement);

        //output
        Element outputElement = new Element("output");
        // output path
        outputElement.setAttribute(new Attribute("src", getOutputDirectory().getAbsolutePath()));
        // output prefix
        outputElement.setAttribute(new Attribute("prefix", getOutputPrefix()));
        // output parameters
        Element parametersElement = new Element("parameters");
        // pixel number
        parametersElement.setAttribute("pixel-number", String.valueOf(getPixelNumber()));
        // number of meridians
        parametersElement.setAttribute("meridian-number", String.valueOf(getMeridianNumber()));
        // number of parallels
        parametersElement.setAttribute("parallel-number", String.valueOf(getParallelNumber()));

        // add elements
        outputElement.addContent(parametersElement);
        processElement.addContent(outputElement);
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
            new Release("2.5.0") {
                @Override
                public void update(Element processElement) {

                    if (Integer.parseInt(processElement.getAttributeValue("type")) == 0) {
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

                    // create output/parameters element
                    Element parametersElement = new Element("parameters");

                    // add pixel-number to output/parameters attribute
                    if (null != processElement.getChild("pixel-number")) {
                        Element pixelNumberElement = processElement.getChild("pixel-number");
                        parametersElement.setAttribute("pixel-number", pixelNumberElement.getAttributeValue("value"));
                    }

                    // add azimut-number to output/paremters attribute
                    // rename it to meridian-number
                    if (null != processElement.getChild("azimut-number")) {
                        Element azimutNumberElement = processElement.getChild("azimut-number");
                        parametersElement.setAttribute("meridian-number", azimutNumberElement.getAttributeValue("value"));
                    }

                    // add zenith-number to output/paremters attribute
                    // rename it to parallel-number
                    if (null != processElement.getChild("zenith-number")) {
                        Element zenithNumberElement = processElement.getChild("zenith-number");
                        parametersElement.setAttribute("parallel-number", zenithNumberElement.getAttributeValue("value"));
                    }

                    // add paremeters element to output element
                    outputElement.addContent(parametersElement);

                    // add output element to process element
                    processElement.addContent(outputElement);

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
