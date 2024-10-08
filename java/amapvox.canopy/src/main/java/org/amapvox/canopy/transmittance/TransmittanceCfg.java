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
package org.amapvox.canopy.transmittance;

import org.amapvox.commons.Configuration;
import static org.amapvox.canopy.transmittance.TransmittanceParameters.Mode.LAI2000;
import static org.amapvox.canopy.transmittance.TransmittanceParameters.Mode.LAI2200;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.vecmath.Point3d;
import org.amapvox.canopy.LeafAngleDistribution;
import org.amapvox.commons.AVoxTask;
import org.amapvox.commons.Release;
import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 *
 * @author Julien Heurtebize
 */
public class TransmittanceCfg extends Configuration {

    private TransmittanceParameters parameters;
    final public static SimpleDateFormat PERIOD_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public TransmittanceCfg(String type, String longName, String description, String[] deprecatedNames) {
        super(type, longName, description, deprecatedNames);
        parameters = new TransmittanceParameters();
    }

    public TransmittanceCfg() {
        super("TRANSMITTANCE", "Transmittance Map",
                "Generates a bitmap/text file of the transmittance light out of a voxel file, sensors and sun parameters.");
        parameters = new TransmittanceParameters();
    }

    @Override
    public Class<? extends AVoxTask> getTaskClass() {
        return TransmittanceSim.class;
    }

    public void setParameters(TransmittanceParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public void readProcessElements(Element processElement) throws IOException {

        Element inputFileElement = processElement.getChild("input_file");
        String inputFileSrc = resolve(inputFileElement.getAttributeValue("src"));

        if (inputFileSrc != null) {
            parameters.setVoxelFile(new File(inputFileSrc));
        }

        if (null != inputFileElement.getAttribute("variable")) {
            parameters.setPADVariable(inputFileElement.getAttributeValue("variable"));
        }

        Element ladElement = processElement.getChild("leaf-angle-distribution");
        if (ladElement != null) {
            parameters.setLeafAngleDistribution(LeafAngleDistribution.Type.fromString(ladElement.getAttributeValue("type")));
            double[] ladParams = new double[2];
            String alphaValue = ladElement.getAttributeValue("alpha");
            if (alphaValue != null) {
                ladParams[0] = Double.parseDouble(alphaValue);
            }
            String betaValue = ladElement.getAttributeValue("beta");
            if (betaValue != null) {
                ladParams[0] = Double.parseDouble(betaValue);
            }
            parameters.setLeafAngleDistributionParameters(ladParams);
        } else {
            throw new IOException("Cannot find leaf-angle-distribution element");
        }

        Element outputFilesElement = processElement.getChild("output_files");
        Element outputTextFileElement = outputFilesElement.getChild("output_text_file");

        if (outputTextFileElement != null) {
            boolean generateOutputTextFile = Boolean.parseBoolean(outputTextFileElement.getAttributeValue("generate"));
            parameters.setGenerateTextFile(generateOutputTextFile);

            if (generateOutputTextFile) {

                String outputTextFileSrc = resolve(outputTextFileElement.getAttributeValue("src"));
                if (outputTextFileSrc != null) {
                    parameters.setTextFile(new File(outputTextFileSrc));

                    if (parameters.getMode() == LAI2000 || parameters.getMode() == LAI2200) {

                        try {
                            parameters.setGenerateLAI2xxxTypeFormat(Boolean.parseBoolean(outputTextFileElement.getAttributeValue("lai2xxx-type")));
                        } catch (Exception e) {
                        }
                    }
                }

            }
        }

        Element outputBitmapFileElement = outputFilesElement.getChild("output_bitmap_file");

        if (outputBitmapFileElement != null) {
            boolean generateOutputBitmapFile = Boolean.parseBoolean(outputBitmapFileElement.getAttributeValue("generate"));
            parameters.setGenerateBitmapFile(generateOutputBitmapFile);

            if (generateOutputBitmapFile) {

                String outputBitmapFileSrc = resolve(outputBitmapFileElement.getAttributeValue("src"));
                if (outputBitmapFileSrc != null) {
                    parameters.setBitmapFile(new File(outputBitmapFileSrc));
                }

            }
        }

        Element directionsNumberElement = processElement.getChild("directions-number");
        if (directionsNumberElement != null) {
            String directionsNumberValue = directionsNumberElement.getAttributeValue("value");
            if (directionsNumberValue != null) {
                parameters.setDirectionsNumber(Integer.parseInt(directionsNumberValue));
            }
        }

        Element directionsRotationElement = processElement.getChild("directions-rotation");
        if (directionsRotationElement != null) {
            String directionsRotationValue = directionsRotationElement.getAttributeValue("value");
            if (directionsRotationValue != null) {
                parameters.setDirectionsRotation(Float.parseFloat(directionsRotationValue));
            }
        }

        Element toricityElement = processElement.getChild("toricity");
        if (toricityElement != null) {
            String toricityValue = toricityElement.getAttributeValue("enable");
            if (toricityValue != null) {
                parameters.setToricity(Boolean.parseBoolean(toricityValue));
            }
        }

        Element scannerPositionsElement = processElement.getChild("scanners-positions");

        if (scannerPositionsElement != null) {

            List<Element> childrens = scannerPositionsElement.getChildren("position");
            if (childrens != null) {
                List<Point3d> positions = new ArrayList<>();

                for (Element children : childrens) {
                    positions.add(new Point3d(Double.parseDouble(children.getAttributeValue("x")),
                            Double.parseDouble(children.getAttributeValue("y")),
                            Double.parseDouble(children.getAttributeValue("z"))));
                }

                parameters.setPositions(positions);
            }
        }
        if (null == parameters.getPositions() || parameters.getPositions().isEmpty()) {
            throw new IOException("Scanner positions are missing.");
        }

        String latitude = processElement.getChild("latitude").getAttributeValue("value");
        if (latitude != null) {
            parameters.setLatitudeInDegrees(Float.parseFloat(latitude));
        }

        Element simulationPeriodsElement = processElement.getChild("simulation-periods");
        if (simulationPeriodsElement != null) {
            List<Element> periodElements = simulationPeriodsElement.getChildren("period");

            if (periodElements != null) {

                List<SimulationPeriod> simulationPeriods = new ArrayList<>();

                for (Element periodElement : periodElements) {

                    String startingDateStr = periodElement.getAttributeValue("from");
                    String endingDateStr = periodElement.getAttributeValue("to");
                    String clearness = periodElement.getAttributeValue("clearness");

                    Period period = new Period();
                    try {
                        Date startingDate = PERIOD_DATE_FORMAT.parse(startingDateStr);
                        Date endingDate = PERIOD_DATE_FORMAT.parse(endingDateStr);

                        Calendar c1 = Calendar.getInstance();
                        c1.setTime(startingDate);

                        Calendar c2 = Calendar.getInstance();
                        c2.setTime(endingDate);

                        period.startDate = c1;
                        period.endDate = c2;

                        SimulationPeriod simulationPeriod = new SimulationPeriod(period, Float.parseFloat(clearness));
                        simulationPeriods.add(simulationPeriod);

                    } catch (ParseException ex) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Failed to parse either start date (");
                        sb.append(startingDateStr).append(") or end date (");
                        sb.append(endingDateStr).append(") from simulation period. ");
                        sb.append("Should be ").append(PERIOD_DATE_FORMAT.getDateFormatSymbols().toString());
                        throw new IOException(sb.toString(), ex);
                    }

                }

                parameters.setSimulationPeriods(simulationPeriods);
            }
        }

        boolean[] ringMasks = new boolean[5];

        Element ringMasksElement = processElement.getChild("ring-masks");
        if (ringMasksElement != null) {
            ringMasks[0] = Boolean.parseBoolean(ringMasksElement.getAttributeValue("mask-ring-1"));
            ringMasks[1] = Boolean.parseBoolean(ringMasksElement.getAttributeValue("mask-ring-2"));
            ringMasks[2] = Boolean.parseBoolean(ringMasksElement.getAttributeValue("mask-ring-3"));
            ringMasks[3] = Boolean.parseBoolean(ringMasksElement.getAttributeValue("mask-ring-4"));
            ringMasks[4] = Boolean.parseBoolean(ringMasksElement.getAttributeValue("mask-ring-5"));
        }

        parameters.setMasks(ringMasks);
    }

    @Override
    public void writeProcessElements(Element processElement) {

        //input
        Element inputFileElement = new Element("input_file");
        inputFileElement.setAttribute(new Attribute("type", "VOX"));
        inputFileElement.setAttribute(new Attribute("src", parameters.getVoxelFile().getAbsolutePath()));
        inputFileElement.setAttribute("variable", parameters.getPADVariable());
        processElement.addContent(inputFileElement);

        // leaf angle distribution
        Element ladElement = new Element("leaf-angle-distribution");
        ladElement.setAttribute("type", parameters.getLeafAngleDistribution().toString());
        processElement.addContent(ladElement);

        if (parameters.getLeafAngleDistribution() == LeafAngleDistribution.Type.TWO_PARAMETER_BETA
                || parameters.getLeafAngleDistribution() == LeafAngleDistribution.Type.ELLIPSOIDAL) {
            ladElement.setAttribute("alpha", String.valueOf(parameters.getLeafAngleDistributionParameters()[0]));

            if (parameters.getLeafAngleDistribution() == LeafAngleDistribution.Type.TWO_PARAMETER_BETA) {
                ladElement.setAttribute("beta", String.valueOf(parameters.getLeafAngleDistributionParameters()[1]));
            }
        }

        //outputs
        Element outputFilesElement = new Element("output_files");
        Element outputTextFileElement = new Element("output_text_file");
        outputTextFileElement.setAttribute("generate", String.valueOf(parameters.isGenerateTextFile()));

        if (parameters.isGenerateTextFile() && parameters.getTextFile() != null) {
            outputTextFileElement.setAttribute("src", parameters.getTextFile().getAbsolutePath());

            if (parameters.getMode() == LAI2000 || parameters.getMode() == LAI2200) {
                outputTextFileElement.setAttribute("lai2xxx-type", String.valueOf(parameters.isGenerateLAI2xxxTypeFormat()));
            }
        }

        outputFilesElement.addContent(outputTextFileElement);

        Element outputBitmapFileElement = new Element("output_bitmap_file");
        outputBitmapFileElement.setAttribute("generate", String.valueOf(parameters.isGenerateBitmapFile()));

        if (parameters.isGenerateBitmapFile() && parameters.getBitmapFolder() != null) {
            outputBitmapFileElement.setAttribute("src", parameters.getBitmapFolder().getAbsolutePath());
        }

        outputFilesElement.addContent(outputBitmapFileElement);

        processElement.addContent(outputFilesElement);

        //directions
        processElement.addContent(new Element("directions-number").setAttribute("value", String.valueOf(parameters.getDirectionsNumber())));
        processElement.addContent(new Element("directions-rotation").setAttribute("value", String.valueOf(parameters.getDirectionsRotation())));
        processElement.addContent(new Element("toricity").setAttribute("enable", String.valueOf(parameters.isToricity())));

        //scanners positions
        Element scannersPositionsElement = new Element("scanners-positions");

        if (parameters.getPositions() != null) {

            List<Point3d> positions = parameters.getPositions();

            for (Point3d position : positions) {
                Element positionElement = new Element("position");
                positionElement.setAttribute("x", String.valueOf(position.x));
                positionElement.setAttribute("y", String.valueOf(position.y));
                positionElement.setAttribute("z", String.valueOf(position.z));
                scannersPositionsElement.addContent(positionElement);
            }
        }

        processElement.addContent(scannersPositionsElement);

        //latitude (radians)
        Element latitudeElement = new Element("latitude").setAttribute("value", String.valueOf(parameters.getLatitudeInDegrees()));
        processElement.addContent(latitudeElement);

        //simulation periods
        Element simulationPeriodsElement = new Element("simulation-periods");

        List<SimulationPeriod> simulationPeriods = parameters.getSimulationPeriods();

        for (SimulationPeriod period : simulationPeriods) {
            Element periodElement = new Element("period");
            periodElement.setAttribute("from", PERIOD_DATE_FORMAT.format(period.getPeriod().startDate.getTime()));
            periodElement.setAttribute("to", PERIOD_DATE_FORMAT.format(period.getPeriod().endDate.getTime()));
            periodElement.setAttribute("clearness", String.valueOf(period.getClearnessCoefficient()));
            simulationPeriodsElement.addContent(periodElement);
        }

        processElement.addContent(simulationPeriodsElement);

        if (parameters.getMode() == LAI2000 || parameters.getMode() == LAI2200) {

            Element ringMasksElement = new Element("ring-masks");

            boolean[] masks = parameters.getMasks();
            if (masks == null) {
                masks = new boolean[5];
            }

            ringMasksElement.setAttribute("mask-ring-1", String.valueOf(masks[0]));
            ringMasksElement.setAttribute("mask-ring-2", String.valueOf(masks[1]));
            ringMasksElement.setAttribute("mask-ring-3", String.valueOf(masks[2]));
            ringMasksElement.setAttribute("mask-ring-4", String.valueOf(masks[3]));
            ringMasksElement.setAttribute("mask-ring-5", String.valueOf(masks[4]));

            processElement.addContent(ringMasksElement);
        }
    }

    public TransmittanceParameters getParameters() {
        return parameters;
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
            }};
    }
}
