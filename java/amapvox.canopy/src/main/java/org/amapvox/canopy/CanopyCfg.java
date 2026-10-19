package org.amapvox.canopy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3d;
import org.amapvox.commons.Configuration;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 *
 * @author pverley
 */
public abstract class CanopyCfg extends Configuration {

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

    abstract public void readParameters(final Element parametersElement) throws JDOMException, IOException;

    abstract public void writeParameters(final Element parametersElement) throws JDOMException, IOException;

    public CanopyCfg(String type, String longName, String description, String[] deprecatedNames) {
        super(type, longName, description, deprecatedNames);
    }

    public CanopyCfg(String type, String longName, String description) {
        super(type, longName, description, new String[0], false);
    }

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

    public List<Point3d> getSensorPositions() {
        return sensorPositions;
    }

    public void setSensorPositions(List<Point3d> sensorPositions) {
        this.sensorPositions = sensorPositions;
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

    @Override
    public void readProcessElements(Element processElement) throws JDOMException, IOException {

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
        if (positions.isEmpty()) {
            throw new IOException("Scanner positions are missing.");
        }
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
        }

        // read canopy tools specific parameters
        Element parametersElement = processElement.getChild("parameters");
        if (null != parametersElement) {
            readParameters(parametersElement);
        } else {
            throw new IOException("Parameters element missing.");
        }
    }

    @Override
    public void writeProcessElements(Element processElement) throws JDOMException, IOException {

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

        //output
        Element outputElement = new Element("output");
        // output path
        outputElement.setAttribute(new Attribute("src", getOutputDirectory().getAbsolutePath()));
        // output prefix
        outputElement.setAttribute(new Attribute("prefix", getOutputPrefix()));

        processElement.addContent(outputElement);

        // write canopy tools specific parameters
        Element parametersElement = new Element("parameters");
        writeParameters(parametersElement);
        processElement.addContent(parametersElement);
    }

}
