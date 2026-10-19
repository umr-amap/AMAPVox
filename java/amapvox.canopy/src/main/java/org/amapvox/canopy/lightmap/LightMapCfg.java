package org.amapvox.canopy.lightmap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
public class LightMapCfg extends CanopyCfg {

    private int directionCount;

    private float directionRotation;

    private boolean toricity;

    private float latitudeInDegrees;

    private List<SimulationPeriod> simulationPeriods;

    public LightMapCfg() {
        super("LIGHTMAP", "Canopy Light Availability Map",
                "Generates a bitmap/text file of the transmittance light out of a voxel file, sensors and sun parameters.",
                new String[]{"TRANSMITTANCE"});
        simulationPeriods = new ArrayList<>();

    }

    public int getDirectionCount() {
        return directionCount;
    }

    public void setDirectionCount(int directionCount) {
        this.directionCount = directionCount;
    }

    public float getDirectionRotation() {
        return directionRotation;
    }

    public void setDirectionRotation(float directionRotation) {
        this.directionRotation = directionRotation;
    }

    public boolean isToricity() {
        return toricity;
    }

    public void setToricity(boolean toricity) {
        this.toricity = toricity;
    }

    public float getLatitudeInDegrees() {
        return latitudeInDegrees;
    }

    public void setLatitudeInDegrees(float latitudeInDegrees) {
        this.latitudeInDegrees = latitudeInDegrees;
    }

    public List<SimulationPeriod> getSimulationPeriods() {
        return simulationPeriods;
    }

    public void setSimulationPeriods(List<SimulationPeriod> simulationPeriods) {
        this.simulationPeriods = simulationPeriods;
    }

    @Override
    public Class<? extends AVoxTask> getTaskClass() {
        return LightMapTask.class;
    }

    @Override
    public void readParameters(Element parametersElement) throws IOException, JDOMException {

        setDirectionCount(Integer.parseInt(parametersElement.getAttributeValue("direction-count")));

        setDirectionRotation(Float.parseFloat(parametersElement.getAttributeValue("direction-rotation")));

        setToricity(Boolean.parseBoolean(parametersElement.getAttributeValue("toricity")));

        setLatitudeInDegrees(Float.parseFloat(parametersElement.getAttributeValue("latitude")));

        Element simulationPeriodsElement = parametersElement.getChild("simulation-periods");
        if (simulationPeriodsElement != null) {
            List<Element> periodElements = simulationPeriodsElement.getChildren("period");

            if (periodElements != null) {

                for (Element periodElement : periodElements) {

                    String startingDateStr = periodElement.getAttributeValue("from");
                    String endingDateStr = periodElement.getAttributeValue("to");
                    String clearness = periodElement.getAttributeValue("clearness");

                    SimulationPeriod simulationPeriod = SimulationPeriod.parse(
                            startingDateStr,
                            endingDateStr,
                            clearness);
                    simulationPeriods.add(simulationPeriod);
                }
            }
        }
    }

    @Override
    public void writeParameters(Element parametersElement) {

        parametersElement.setAttribute("direction-count", String.valueOf(getDirectionCount()));
        parametersElement.setAttribute("direction-rotation", String.valueOf(getDirectionRotation()));
        parametersElement.setAttribute("toricity", String.valueOf(isToricity()));
        parametersElement.setAttribute("latitude", String.valueOf(getLatitudeInDegrees()));

        //simulation periods
        Element simulationPeriodsElement = new Element("simulation-periods");
        for (SimulationPeriod period : simulationPeriods) {
            Element periodElement = new Element("period");
            periodElement.setAttribute("from", period.startDateToString());
            periodElement.setAttribute("to", period.endDateToString());
            periodElement.setAttribute("clearness", String.valueOf(period.getClearnessCoefficient()));
            simulationPeriodsElement.addContent(periodElement);
        }
        parametersElement.addContent(simulationPeriodsElement);
    }

    @Override
    public Release[] getReleases() {
        return new Release[]{
            // 2024-09-03
            new Release("2.3.0") {
                @Override
                public void update(Element processElement) {

                    // add default spherical leaf angle distribution element
                    Element leafAngleDistributionElement = new Element("leaf-angle-distribution");
                    leafAngleDistributionElement.setAttribute("type", LeafAngleDistribution.Type.SPHERIC.toString());
                    processElement.addContent(leafAngleDistributionElement);
                }
            },
            new Release("2.5.0") {
                @Override
                public void update(Element processElement) {

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

                    // rename scanners-positions into sensor-positions
                    if (null != processElement.getChild("scanners-positions")) {
                        processElement.getChild("scanners-positions").setName("sensor-positions");
                    }

                    // Create parameters Element
                    Element parametersElement = new Element("parameters");

                    // move directions-number element as parameters directions-number attribute
                    String directionsNumberValue = String.valueOf(136);
                    Element directionsNumberElement = processElement.getChild("directions-number");
                    if (directionsNumberElement != null) {
                        if (null != directionsNumberElement.getAttribute("value")) {
                            directionsNumberValue = directionsNumberElement.getAttributeValue("value");
                        }
                    }
                    processElement.removeChild("directions-number");
                    parametersElement.setAttribute("direction-count", directionsNumberValue);

                    // move directions-rotation element as parameters directions-rotation attribute
                    String directionsRotationValue = String.valueOf(0.f);
                    Element directionsRotationElement = processElement.getChild("directions-rotation");
                    if (directionsRotationElement != null) {
                        if (null != directionsRotationElement.getAttribute("value")) {
                            directionsRotationValue = directionsRotationElement.getAttributeValue("value");
                        }
                    }
                    processElement.removeChild("directions-rotation");
                    parametersElement.setAttribute("direction-rotation", directionsRotationValue);

                    // move toricity element as parameters toricity attribute
                    String toricityValue = String.valueOf(false);
                    Element toricityElement = processElement.getChild("toricity");
                    if (toricityElement != null) {
                        if (null != toricityElement.getAttribute("enable")) {
                            toricityValue = toricityElement.getAttributeValue("enable");
                        }
                    }
                    processElement.removeChild("toricity");
                    parametersElement.setAttribute("toricity", toricityValue);

                    // move latitude element as parameters latitude attribute
                    String latitudeValue = String.valueOf(0.f);
                    Element latitudeElement = processElement.getChild("latitude");
                    if (null != latitudeElement) {
                        if (null != latitudeElement.getAttribute("value")) {
                            latitudeValue = latitudeElement.getAttributeValue("value");
                        }
                    }
                    processElement.removeChild("latitude");
                    parametersElement.setAttribute("latitude", latitudeValue);

                    // move simulation-periods element to parametersElement
                    if (null != processElement.getChild("simulation-periods")) {
                        Element simulationPeriodsElement = processElement.getChild("simulation-periods").detach();
                        parametersElement.addContent(simulationPeriodsElement);
                        processElement.removeChild("simulation-periods");
                    }

                    // add parameters element
                    processElement.addContent(parametersElement);
                }
            }
        };
    }

}
