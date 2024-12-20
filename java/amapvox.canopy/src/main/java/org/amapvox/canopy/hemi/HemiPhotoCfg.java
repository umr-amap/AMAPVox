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
package org.amapvox.canopy.hemi;

import org.amapvox.commons.Configuration;
import org.amapvox.commons.Matrix;
import org.amapvox.lidar.commons.LidarScan;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Matrix4d;
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

    private HemiParameters parameters = new HemiParameters();

    public HemiPhotoCfg() {
        super("HEMI_PHOTO", "Hemispheral Photograph",
                "Generates hemispherical photography from a voxel file or a single lidar scan.");
    }

    @Override
    public Class<? extends AVoxTask> getTaskClass() {
        return HemiScanView.class;
    }

    public void setParameters(HemiParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public void readProcessElements(Element processElement) throws IOException {

        String processTypeValue = processElement.getAttributeValue("type");

        switch (processTypeValue) {
            case "0" -> //ECHOS
                parameters.setMode(HemiParameters.Mode.ECHOS);
            case "1" -> //PAD
                parameters.setMode(HemiParameters.Mode.PAD);
        }

        if (parameters.getMode() == HemiParameters.Mode.ECHOS) {

            Element inputFilesElement = processElement.getChild("scans");
            List<Element> scanElements = inputFilesElement.getChildren("scan");

            List<LidarScan> scans = new ArrayList<>(scanElements.size());

            scanElements.forEach((scanElement) -> {
                String inputFileSrc = resolve(scanElement.getAttributeValue("src"));
                Element matrixElement = scanElement.getChild("matrix");
                Matrix4d sopMatrix = Matrix.valueOf(matrixElement).toMatrix4d();
                if (inputFileSrc != null) {
                    File f = new File(inputFileSrc);
                    scans.add(new LidarScan(f, sopMatrix));
                }
            });

            parameters.setRxpScansList(scans);

        } else if (parameters.getMode() == HemiParameters.Mode.PAD) {

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

            List<Point3d> positions = new ArrayList<>();

            Element sensorPositionElement = processElement.getChild("sensor-position");

            if (sensorPositionElement != null) { //work around for old config

                positions.add(new Point3d(Double.parseDouble(sensorPositionElement.getAttributeValue("x")),
                        Double.parseDouble(sensorPositionElement.getAttributeValue("y")),
                        Double.parseDouble(sensorPositionElement.getAttributeValue("z"))));

                parameters.setSensorPositions(positions);

            } else {

                Element sensorPositionsElement = processElement.getChild("sensor-positions");
                List<Element> children = sensorPositionsElement.getChildren("position");
                children.forEach((element) -> {
                    positions.add(new Point3d(
                            Double.parseDouble(element.getAttributeValue("x")),
                            Double.parseDouble(element.getAttributeValue("y")),
                            Double.parseDouble(element.getAttributeValue("z"))));
                });

                parameters.setSensorPositions(positions);
            }

        }

        //common parameters
        Element pixelNumberElement = processElement.getChild("pixel-number");
        if (null != pixelNumberElement) {
            parameters.setPixelNumber(Integer.parseInt(pixelNumberElement.getAttributeValue("value")));
        } else {
            throw new IOException("Cannot find pixel-number element");
        }

        Element azimutsNumberElement = processElement.getChild("azimut-number");
        if (null != azimutsNumberElement) {
            parameters.setAzimutsNumber(Integer.parseInt(azimutsNumberElement.getAttributeValue("value")));
        } else {
            throw new IOException("Cannot find azimut-number element");
        }

        Element zenithNumberElement = processElement.getChild("zenith-number");
        if (null != zenithNumberElement) {
            parameters.setZenithsNumber(Integer.parseInt(zenithNumberElement.getAttributeValue("value")));
        } else {
            throw new IOException("Cannot find zenith-number element");
        }

        //outputs
        Element outputFilesElement = processElement.getChild("output_files");
        Element outputTextFileElement = outputFilesElement.getChild("output_text_file");

        if (outputTextFileElement != null) {
            boolean generateOutputTextFile = Boolean.parseBoolean(outputTextFileElement.getAttributeValue("generate"));
            parameters.setGenerateTextFile(generateOutputTextFile);

            if (generateOutputTextFile) {

                String outputTextFileSrc = resolve(outputTextFileElement.getAttributeValue("src"));
                if (outputTextFileSrc != null) {
                    parameters.setOutputTextFile(new File(outputTextFileSrc));
                }

            }
        }

        Element outputBitmapFileElement = outputFilesElement.getChild("output_bitmap_file");

        if (outputBitmapFileElement != null) {
            boolean generateOutputBitmapFile = Boolean.parseBoolean(outputBitmapFileElement.getAttributeValue("generate"));
            parameters.setGenerateBitmapFile(generateOutputBitmapFile);

            if (generateOutputBitmapFile) {

                String outputBitmapFileSrc = resolve(outputBitmapFileElement.getAttributeValue("src"));
                int bitmapMode = Integer.parseInt(outputBitmapFileElement.getAttributeValue("mode"));

                switch (bitmapMode) {
                    case 0 -> parameters.setBitmapMode(HemiParameters.BitmapMode.PIXEL);
                    case 1 -> parameters.setBitmapMode(HemiParameters.BitmapMode.COLOR);
                }

                if (outputBitmapFileSrc != null) {
                    parameters.setOutputBitmapFile(new File(outputBitmapFileSrc));
                }

            }
        }

    }

    @Override
    public void writeProcessElements(Element processElement) {

        HemiParameters.Mode mode = parameters.getMode();
        processElement.setAttribute(new Attribute("type", String.valueOf(mode.getMode())));

        if (mode == HemiParameters.Mode.ECHOS) {

            //input
            Element scansElement = new Element("scans");
            List<LidarScan> rxpScansList = parameters.getRxpScansList();

            rxpScansList.forEach(scan -> {
                Element scanElement = new Element("scan");
                scanElement.setAttribute("src", scan.getFile().getAbsolutePath());
                Matrix matrix = Matrix.valueOf(scan.getMatrix());
                matrix.setId("sop");
                scanElement.addContent(matrix.toElement());
                scansElement.addContent(scanElement);
            });

            processElement.addContent(scansElement);

        } else if (mode == HemiParameters.Mode.PAD) {

            //input
            Element inputFileElement = new Element("input_file");
            inputFileElement.setAttribute("type", "VOX");
            inputFileElement.setAttribute("src", parameters.getVoxelFile().getAbsolutePath());
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

            // sensor positions
            Element sensorPositionsElement = new Element("sensor-positions");

            parameters.getSensorPositions().forEach(position -> {
                Element positionElement = new Element("position");
                positionElement.setAttribute("x", String.valueOf(position.x));
                positionElement.setAttribute("y", String.valueOf(position.y));
                positionElement.setAttribute("z", String.valueOf(position.z));
                sensorPositionsElement.addContent(positionElement);
            });

            processElement.addContent(sensorPositionsElement);
        }

        //common parameters
        Element pixelNumberElement = new Element("pixel-number");
        pixelNumberElement.setAttribute("value", String.valueOf(parameters.getPixelNumber()));
        processElement.addContent(pixelNumberElement);

        Element azimutsNumberElement = new Element("azimut-number");
        azimutsNumberElement.setAttribute("value", String.valueOf(parameters.getAzimutsNumber()));
        processElement.addContent(azimutsNumberElement);

        Element zenithNumberElement = new Element("zenith-number");
        zenithNumberElement.setAttribute("value", String.valueOf(parameters.getZenithsNumber()));
        processElement.addContent(zenithNumberElement);

        //outputs
        Element outputFilesElement = new Element("output_files");
        Element outputTextFileElement = new Element("output_text_file");
        outputTextFileElement.setAttribute("generate", String.valueOf(parameters.isGenerateTextFile()));

        if (parameters.isGenerateTextFile() && parameters.getOutputTextFile() != null) {
            outputTextFileElement.setAttribute("src", parameters.getOutputTextFile().getAbsolutePath());
        }

        outputFilesElement.addContent(outputTextFileElement);

        Element outputBitmapFileElement = new Element("output_bitmap_file");
        outputBitmapFileElement.setAttribute("generate", String.valueOf(parameters.isGenerateBitmapFile()));

        if (parameters.isGenerateBitmapFile() && parameters.getOutputBitmapFile() != null) {
            outputBitmapFileElement.setAttribute("src", parameters.getOutputBitmapFile().getAbsolutePath());
            outputBitmapFileElement.setAttribute("mode", String.valueOf(parameters.getBitmapMode().getMode()));
        }

        outputFilesElement.addContent(outputBitmapFileElement);

        processElement.addContent(outputFilesElement);
    }

    public HemiParameters getParameters() {
        return parameters;
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
