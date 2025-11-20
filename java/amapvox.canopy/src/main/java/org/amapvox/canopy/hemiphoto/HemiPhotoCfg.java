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

    //PAD mode
    private File voxelFile;
    private String padVariable;
    private LeafAngleDistribution.Type leafAngleDistribution;
    private double[] leafAngleDistributionParameters = new double[2];
    private List<Point3d> sensorPositions;
    
    //common parameters
    private int pixelNumber;
    private int azimutsNumber = 36;
    private int zenithsNumber = 9;
    
    //output
    private File outputDir;
    private String outputPrefix;

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

    public int getAzimutsNumber() {
        return azimutsNumber;
    }

    public void setAzimutsNumber(int azimutsNumber) {
        this.azimutsNumber = azimutsNumber;
    }

    public int getZenithsNumber() {
        return zenithsNumber;
    }

    public void setZenithsNumber(int zenithsNumber) {
        this.zenithsNumber = zenithsNumber;
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
        super("HEMI_PHOTO", "Hemispheral Photograph",
                "Generates hemispherical photography from a voxel file or a single lidar scan.");
    }

    @Override
    public Class<? extends AVoxTask> getTaskClass() {
        return HemiPhotoTask.class;
    }

    @Override
    public void readProcessElements(Element processElement) throws IOException {

        Element inputFileElement = processElement.getChild("input_file");
        String inputFileSrc = resolve(inputFileElement.getAttributeValue("src"));

        if (inputFileSrc != null) {
            setVoxelFile(new File(inputFileSrc));
        }

        if (null != inputFileElement.getAttribute("variable")) {
            setPADVariable(inputFileElement.getAttributeValue("variable"));
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

        Element sensorPositionElement = processElement.getChild("sensor-position");

        if (sensorPositionElement != null) { //work around for old config

            positions.add(new Point3d(Double.parseDouble(sensorPositionElement.getAttributeValue("x")),
                    Double.parseDouble(sensorPositionElement.getAttributeValue("y")),
                    Double.parseDouble(sensorPositionElement.getAttributeValue("z"))));

            setSensorPositions(positions);

        } else {

            Element sensorPositionsElement = processElement.getChild("sensor-positions");
            List<Element> children = sensorPositionsElement.getChildren("position");
            children.forEach((element) -> {
                positions.add(new Point3d(
                        Double.parseDouble(element.getAttributeValue("x")),
                        Double.parseDouble(element.getAttributeValue("y")),
                        Double.parseDouble(element.getAttributeValue("z"))));
            });

            setSensorPositions(positions);
        }

        //common parameters
        Element pixelNumberElement = processElement.getChild("pixel-number");
        if (null != pixelNumberElement) {
            setPixelNumber(Integer.parseInt(pixelNumberElement.getAttributeValue("value")));
        } else {
            throw new IOException("Cannot find pixel-number element");
        }

        Element azimutsNumberElement = processElement.getChild("azimut-number");
        if (null != azimutsNumberElement) {
            setAzimutsNumber(Integer.parseInt(azimutsNumberElement.getAttributeValue("value")));
        } else {
            throw new IOException("Cannot find azimut-number element");
        }

        Element zenithNumberElement = processElement.getChild("zenith-number");
        if (null != zenithNumberElement) {
            setZenithsNumber(Integer.parseInt(zenithNumberElement.getAttributeValue("value")));
        } else {
            throw new IOException("Cannot find zenith-number element");
        }

        //outputs
        Element outputElement = processElement.getChild("output");
        if (null != outputElement) {
            // output directory
            File outputDir = new File(resolve(outputElement.getAttributeValue("src")));
            setOutputDirectory(outputDir);
            // output prefix
            String prefix = outputElement.getAttributeValue("prefix");
            setOutputPrefix(prefix);
        }

    }

    @Override
    public void writeProcessElements(Element processElement) {

        //input
        Element inputFileElement = new Element("input_file");
        inputFileElement.setAttribute("type", "VOX");
        inputFileElement.setAttribute("src", getVoxelFile().getAbsolutePath());
        inputFileElement.setAttribute("variable", getPADVariable());
        processElement.addContent(inputFileElement);

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
        azimutsNumberElement.setAttribute("value", String.valueOf(getAzimutsNumber()));
        processElement.addContent(azimutsNumberElement);

        Element zenithNumberElement = new Element("zenith-number");
        zenithNumberElement.setAttribute("value", String.valueOf(getZenithsNumber()));
        processElement.addContent(zenithNumberElement);

        //outputs
        Element outputElement = new Element("output");
        // output path
        outputElement.setAttribute(new Attribute("src", getOutputDirectory().getAbsolutePath()));
        // output prefix
        outputElement.setAttribute(new Attribute("prefix", getOutputPrefix()));
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
            }
        };
    }
}
