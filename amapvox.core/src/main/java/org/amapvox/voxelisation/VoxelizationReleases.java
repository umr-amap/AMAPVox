/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation;

import org.amapvox.commons.*;
import org.amapvox.commons.util.filter.Filter;
import org.amapvox.shot.filter.ClassifiedPointFilter;
import org.amapvox.shot.filter.DigitalTerrainModelFilter;
import org.amapvox.shot.filter.EchoAttributeFilter;
import org.amapvox.shot.filter.EchoRangeFilter;
import org.amapvox.shot.filter.EchoRankFilter;
import org.amapvox.shot.filter.PointcloudFilter;
import org.amapvox.voxelisation.VoxelizationCfg.LidarType;
import org.amapvox.voxelisation.output.OutputVariable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;

/**
 *
 * @author pverley
 */
public class VoxelizationReleases {

    final public static Release[] ALL = new Release[]{
        // 2018-11-07
        new Release("1.1.0") {
            @Override
            public void update(Element processElement) {

                // new echo weights parameters
                Element ponderationElement = processElement.getChild("ponderation");
                if (null != ponderationElement && null != ponderationElement.getAttribute("mode")) {
                    int mode = Integer.valueOf(ponderationElement.getAttributeValue("mode"));
                    ponderationElement.removeAttribute("mode");
                    ponderationElement.setAttribute(new Attribute("byrank", String.valueOf(mode > 0)));
                    ponderationElement.setAttribute(new Attribute("byfile", String.valueOf(false)));
                }
            }
        },
        // 2018-11-12
        new Release("1.1.1") {
            @Override
            public void update(Element processElement) {

                // deprecated elements transmittance mode and path-length mode
                processElement.removeChild("transmittance");
                processElement.removeChild("path-length");
            }
        },
        // 2019-01-22
        new Release("1.1.5") {
            @Override
            public void update(Element processElement) {

                // new transmittance calculation algorithm
                if (null == processElement.getChild("transmittance")) {
                    Element transmittanceElement = new Element("transmittance");
                    transmittanceElement.setAttribute(new Attribute("transmittanceNumericallyEstimated", String.valueOf(false)));
                    transmittanceElement.setAttribute(new Attribute("error", String.valueOf(0.0001f)));
                    processElement.addContent(transmittanceElement);
                }
            }
        },
        // 2019-02-14
        new Release("1.2.0") {
            @Override
            public void update(Element processElement) {

                // new attribute mono-echo (true/false) in the laser specification
                Element laserSpecElement = processElement.getChild("laser-specification");
                if (null != laserSpecElement) {
                    String laserSpecName = laserSpecElement.getAttributeValue("name");
                    boolean monoEchoSet = false;
                    for (LaserSpecification laserSpec : LaserSpecification.getPresets()) {
                        if (laserSpec.isValidName(laserSpecName)) {
                            laserSpecElement.setAttribute(new Attribute("mono-echo", String.valueOf(laserSpec.isMonoEcho())));
                            monoEchoSet = true;
                            break;
                        }
                    }
                    if (!monoEchoSet) {
                        Logger.getLogger(Release.class).warn("Your laser specification does not match any known specification. AMAPVox cannot guess whether the laser is either mono or multi echo. New parameter <laser-specification mono-echo=\"true|false\" cannot be automatically updated. You will have to set it manually.");
                    }
                }
            }
        },
        // 2019-05-28
        new Release("1.2.9") {
            @Override
            public void update(Element processElement) {

                // no need to include coordinate output variables in configuration file
                // included automatically
                Element outputVariablesElement = processElement.getChild("output_variables");
                if (null != outputVariablesElement) {
                    outputVariablesElement.removeAttribute("i_index");
                    outputVariablesElement.removeAttribute("j_index");
                    outputVariablesElement.removeAttribute("k_index");
                }
            }
        },
        // 2019-05-29
        new Release("1.2.10") {
            @Override
            public void update(Element processElement) {

                // delete deprecated element export-shot-segment
                processElement.removeChild("export-shot-segment");
            }
        },
        // 2019-06-19
        new Release("1.2.15") {
            @Override
            public void update(Element processElement) {

                // make sure output_variables element exists
                if (null == processElement.getChild("output_variables")) {
                    processElement.addContent(new Element("output_variables"));
                }

                // added attribute  output_variables/attenuation_coeff
                processElement.getChild("output_variables").setAttribute(new Attribute("attenuation_coeff", String.valueOf(false)));
            }
        },
        // 2019-07-08
        new Release("1.2.20") {
            @Override
            public void update(Element processElement) {

                // make sure filters element exists
                if (null == processElement.getChild("filters")) {
                    processElement.addContent(new Element("filters"));
                }
                // make sure shot-filters element exists
                if (null == processElement.getChild("filters").getChild("shot-filters")) {
                    processElement.getChild("filters").addContent(new Element("shot-filters"));
                }
                // check existence of shot integrity filter
                if (processElement.getChild("filters").getChild("shot-filters")
                        .getChildren("filter").stream()
                        .noneMatch(filter -> null != filter.getAttribute("blank-echo-discarded"))) {
                    // create shot integrity filter
                    Element filterElement = new Element("filter");
                    filterElement.setAttribute("blank-echo-discarded", String.valueOf(false));
                    // add shot integrity filter
                    processElement.getChild("filters").getChild("shot-filters").addContent(filterElement);
                    // warn the user that such addition may change the results
                    Logger.getLogger(Release.class).warn("Added new shot consistency filter that will discard shots with inconsistent echo ranges. This new filter may change previous outputs.");
                }
            }
        },
        // 2019-11-15
        new Release("1.4.2") {
            @Override
            public void update(Element processElement) {

                // new transmittance parameters
                if (null != processElement.getChild("transmittance")) {
                    // delete deprecated attribute transmittanceNumericallyEstimated
                    processElement.getChild("transmittance").removeAttribute("transmittanceNumericallyEstimated");
                    // error attribute keeps same meaning
                    // fallback-error and nrecordmax will take default values
                }
            }
        },
        // 2019-12-09
        new Release("1.4.3") {
            @Override
            public void update(Element processElement) {

                // make sure output_variables element exists
                if (null == processElement.getChild("output_variables")) {
                    processElement.addContent(new Element("output_variables"));
                }

                // removed attribute output_variables/approximated_transmittance
                Element outputs = processElement.getChild("output_variables");
                outputs.removeAttribute(outputs.getAttribute("approximated_transmittance"));

                // renamed attribute output_variables/attenuation_biasCorr into attenuationBiasCorrection
                if (null != outputs.getAttribute("attenuation_biasCorr")) {
                    boolean enabled = Boolean.valueOf(outputs.getAttributeValue("attenuation_biasCorr"));
                    outputs.removeAttribute(outputs.getAttribute("attenuation_biasCorr"));
                    outputs.setAttribute(new Attribute("attenuationBiasCorrection", String.valueOf(enabled)));
                }

                // added attribute  output_variables/weighted_freepath
                outputs.setAttribute(new Attribute("weighted_freepath", String.valueOf(false)));
            }
        },
        // 2020-02-17
        new Release("1.5.1") {
            @Override
            public void update(Element processElement) {

                Element output = processElement.getChild("output_file");
                if (null != output) {
                    // replace output format integer code by name
                    int format = -1;
                    try {
                        format = output.getAttribute("format").getIntValue();
                    } catch (DataConversionException ex) {
                        // ignore error and just keep zero as default value
                    }
                    switch (format) {
                        case 0:
                            output.setAttribute("format", "NONE");
                            break;
                        case 1:
                        default:
                            output.setAttribute("format", "VOXEL");
                            break;
                        case 2:
                            output.setAttribute("format", "RASTER");
                            break;
                        case 3:
                            output.setAttribute("format", "NETCDF");
                            break;
                    }

                    // add skip-empty-voxel attribute, false by default for backward compatibility
                    output.setAttribute("skip-empty-voxel", Boolean.FALSE.toString());
                }

                // renamed ponderation bu echo_weighting
                if (null != processElement.getChild("ponderation")) {
                    processElement.getChild("ponderation").setName("echo_weighting");
                }
                Element weighting = processElement.getChild("echo_weighting");
                if (null != weighting.getChild("matrix")) {
                    weighting.getChild("matrix").setAttribute("type_id", "weighting");
                }
            }
        },
        // 2020-04-16
        new Release("1.5.4") {
            @Override
            public void update(Element processElement) {

                // add classname and enabled attribute to every echo-filter
                if (null != processElement.getChild("filters")
                        && null != processElement.getChild("filters").getChild("echo-filters")) {
                    Element echoFilters = processElement.getChild("filters").getChild("echo-filters");
                    for (Element filter : echoFilters.getChildren()) {
                        // some configuration contains empty filter children that must be deleted
                        if (filter.getAttributes().isEmpty()) {
                            echoFilters.removeContent(filter);
                            continue;
                        }
                        if (null != filter.getAttribute("variable")) {
                            // EchoAttributeFilter
                            filter.setAttribute("classname", EchoAttributeFilter.class.getCanonicalName());
                        } else if (filter.getAttributes().size() == 2 && null != filter.getAttribute("src")) {
                            // EchoRankFilter
                            filter.setAttribute("classname", EchoRankFilter.class.getCanonicalName());
                        }
                        filter.setAttribute("enabled", Boolean.toString(true));
                    }
                }

                // remove empty shot filters
                // add enabled attribute
                // add classname attribute
                if (null != processElement.getChild("filters")
                        && null != processElement.getChild("filters").getChild("shot-filters")) {
                    Element shotFilters = processElement.getChild("filters").getChild("shot-filters");
                    for (Element filter : shotFilters.getChildren()) {
                        // some old configuration contains empty filter children that must be deleted
                        if (filter.getAttributes().isEmpty()) {
                            shotFilters.removeContent(filter);
                            continue;
                        }
                        if (null == filter.getAttribute("enabled")) {
                            filter.setAttribute("enabled", Boolean.toString(true));
                        }
                        if (null != filter.getAttribute("blank-echo-discarded")) {
                            // EchoRangeFilter
                            filter.setAttribute("classname", EchoRangeFilter.class.getCanonicalName());
                        }
                    }
                }

                // move classification filters to echo filters
                if (null != processElement.getChild("filters")
                        && null != processElement.getChild("filters").getChild("point-filters")) {
                    Element filter = processElement.getChild("filters").getChild("point-filters").detach();
                    filter.setName("filter");
                    filter.setAttribute("classname", ClassifiedPointFilter.class.getCanonicalName());
                    if (null == processElement.getChild("filters").getChild("echo-filters")) {
                        processElement.getChild("filters").addContent(new Element("echo-filters"));
                    }
                    processElement.getChild("filters").getChild("echo-filters").addContent(filter);
                    // remove point-filters
                    processElement.getChild("filters").removeChild("point-filters");
                }

                // move false empty shot filter to shot-filters
                if (null != processElement.getChild("filter-empty-shots")) {
                    Element filter = new Element("filter");
                    filter.setAttribute("classname", RXPFalseEmptyShotRemover.class.getCanonicalName());
                    filter.setAttribute("enabled", processElement.getChild("filter-empty-shots").getAttributeValue("enable")); // beware enabled/enable
                    // remove filter-empty-shots element
                    processElement.removeChild("filter-empty-shots");
                    // make sure filters element exists
                    if (null == processElement.getChild("filters")) {
                        processElement.addContent(new Element("filters"));
                    }
                    // make sure shot-filters element exists
                    if (null == processElement.getChild("filters").getChild("shot-filters")) {
                        processElement.getChild("filters").addContent(new Element("shot-filters"));
                    }
                    // add false empty shot filter
                    processElement.getChild("filters").getChild("shot-filters").addContent(filter);
                }

                // move dtm-filter to filters element
                if (null != processElement.getChild("dtm-filter")) {
                    Element dtmFilter = processElement.getChild("dtm-filter").detach();
                    // make sure filters element exists
                    if (null == processElement.getChild("filters")) {
                        processElement.addContent(new Element("filters"));
                    }
                    processElement.getChild("filters").addContent(dtmFilter);
                    // remove dtm-filter from process element
                    processElement.removeChild("dtm-filter");
                }

                // move point-cloud filter to echo-filters
                Element pointcloudFilters = processElement.getChild("pointcloud-filters");
                if (null != pointcloudFilters) {
                    boolean pcfEnabled = Boolean.valueOf(pointcloudFilters.getAttributeValue("enabled"));
                    List<Element> childrens = pointcloudFilters.getChildren("pointcloud-filter");
                    if (childrens != null) {
                        // make sure filters element exists
                        if (null == processElement.getChild("filters")) {
                            processElement.addContent(new Element("filters"));
                        }
                        Element filtersElement = processElement.getChild("filters");
                        // make sure echo-filters element exists
                        if (null == filtersElement.getChild("echo-filters")) {
                            filtersElement.addContent(new Element("echo-filters"));
                        }
                        Element echoFiltersElement = filtersElement.getChild("echo-filters");
                        childrens.forEach(e -> {
                            Element pcf = e.detach();
                            pcf.setName("filter");
                            // new classname attribute
                            pcf.setAttribute("classname", PointcloudFilter.class.getCanonicalName());
                            // new enabled attribute
                            pcf.setAttribute("enabled", String.valueOf(pcfEnabled));
                            // renamed operation-type attribute into behavior
                            String operationType = pcf.getAttributeValue("operation-type");
                            Filter.Behavior behavior = operationType.equals("Keep")
                                    ? Filter.Behavior.RETAIN : Filter.Behavior.DISCARD;
                            pcf.removeAttribute("operation-type");
                            pcf.setAttribute("behavior", behavior.toString());
                            // add filter
                            echoFiltersElement.addContent(pcf);
                        });
                    }
                    // remove point-cloud filters element
                    processElement.removeChild("pointcloud-filters");
                }
            }
        },
        // 2020-06-05
        new Release("1.5.6") {
            @Override
            public void update(Element processElement) {

                // renamed ground-energy attribute
                if (null != processElement.getChild("ground-energy")) {
                    String enabled = processElement.getChild("ground-energy").getAttributeValue("generate");
                    processElement.getChild("ground-energy").removeAttribute("generate");
                    processElement.getChild("ground-energy").setAttribute(new Attribute("enabled", enabled));
                }

                // gather together all output related elements under output element
                if (null == processElement.getChild("output")) {
                    processElement.addContent(new Element("output"));
                }
                Element outputElement = processElement.getChild("output");
                // voxel_file element
                if (null != processElement.getChild("output_file")) {
                    Element outputFileElement = processElement.getChild("output_file").detach();
                    outputFileElement.setName("voxel_file");
                    outputElement.addContent(outputFileElement);
                }
                // variables element
                if (null != processElement.getChild("output_variables")) {
                    Element outputVariablesElement = processElement.getChild("output_variables").detach();
                    if (null != outputElement.getChild("variables")) {
                        // variables element already exists
                        // merely update attributes
                        outputVariablesElement.getAttributes().forEach(attribute -> {
                            if (!outputElement.getChild("variables").getAttributes().contains(attribute)) {
                                outputElement.getChild("variables").setAttribute(attribute.getName(), attribute.getValue());
                            }
                        });
                    } else {
                        // variables element does not exist, create it
                        outputVariablesElement.setName("variables");
                        outputElement.addContent(outputVariablesElement);
                    }
                }
                if (null != processElement.getChild("decimal-format")) {
                    Element decimalFormatElement = processElement.getChild("decimal-format").detach();
                    outputElement.addContent(decimalFormatElement);
                }
                if (null != processElement.getChild("merging")) {
                    Element mergingElement = processElement.getChild("merging").detach();
                    outputElement.addContent(mergingElement);
                }
                if (null != processElement.getChild("ground-energy")) {
                    Element groundEnergyElement = processElement.getChild("ground-energy").detach();
                    outputElement.addContent(groundEnergyElement);
                }
                if (null != processElement.getChild("correct-NaNs")) {
                    Element correctNansElement = processElement.getChild("correct-NaNs").detach();
                    outputElement.addContent(correctNansElement);
                }

                // new element for path length export inside output element
                if (null == outputElement.getChild("pathlength")) {
                    Element pathLengthElement = new Element("pathlength");
                    pathLengthElement.setAttribute(new Attribute("enabled", Boolean.FALSE.toString()));
                    pathLengthElement.setAttribute(new Attribute("array-max-size", String.valueOf(100)));
                    outputElement.addContent(pathLengthElement);
                }

                // gather together all input related elements under input element
                if (null == processElement.getChild("input")) {
                    processElement.addContent(new Element("input"));
                }
                Element inputElement = processElement.getChild("input");
                // main_file element
                if (null != processElement.getChild("input_file")) {
                    Element inputFileElement = processElement.getChild("input_file").detach();
                    inputFileElement.setName("main_file");
                    inputElement.addContent(inputFileElement);
                }
                // files element (renamed to scans)
                if (null != processElement.getChild("files")) {
                    Element filesElement = processElement.getChild("files").detach();
                    filesElement.setName("scans");
                    filesElement.getChildren("file").forEach((fileElement) -> {
                        fileElement.setName("scan");
                    });
                    inputElement.addContent(filesElement);
                }
                // trajectory element
                if (null != processElement.getChild("trajectory")) {
                    Element trajectoryElement = processElement.getChild("trajectory").detach();
                    inputElement.addContent(trajectoryElement);
                }
                // als-consistency element
                if (null != processElement.getChild("als-consistency")) {
                    Element alsConsistencyElement = processElement.getChild("als-consistency").detach();
                    inputElement.addContent(alsConsistencyElement);
                }
            }
        },
        // 2020-11-24
        new Release("1.7.1") {
            @Override
            public void update(Element processElement) {

                Element outputElement = processElement.getChild("output");
                if (null != outputElement) {
                    // rename some output variables
                    Element variables = outputElement.getChild("variables");
                    if (null != variables) {
                        if (null != variables.getAttribute("attenuation_coeff")) {
                            variables.getAttribute("attenuation_coeff").setName("attenuation_fpl_biased_mle");
                        }
                        if (null != variables.getAttribute("attenuation_bias_corr")) {
                            variables.getAttribute("attenuation_bias_corr").setName("attenuation_fpl_bias_correction");
                        }
                        if (null != variables.getAttribute("weighted_freepath")) {
                            variables.getAttribute("weighted_freepath").setName("weighted_effective_freepath");
                        }
                    }
                    // update output path
                    Element voxelFileElement = outputElement.getChild("voxel_file");
                    if (voxelFileElement != null) {
                        File outputFile = new File(voxelFileElement.getAttributeValue("src"));
                        if (outputFile.isFile()
                                || (!outputFile.exists() && outputFile.getName().endsWith(".vox"))) {
                            voxelFileElement.setAttribute("src", outputFile.getParent());
                        }
                    }
                }
            }
        },
        // 2020-12-15
        new Release("1.7.3") {

            @Override
            public void update(Element processElement) {

                Element inputElement = processElement.getChild("input");
                if (null != inputElement) {
                    Element mainFileElement = inputElement.getChild("main_file");
                    if (null != mainFileElement) {
                        // add type attribute inside input element
                        // <input type=LidarType> 
                        int inputType = Integer.valueOf(mainFileElement.getAttributeValue("type"));
                        LidarType lidarType;
                        switch (inputType) {
                            case 0:
                                lidarType = LidarType.LAS;
                                break;
                            case 1:
                                lidarType = LidarType.LAZ;
                                break;
                            case 2:
                                lidarType = LidarType.SHT;
                                break;
                            case 4:
                                lidarType = LidarType.RXP;
                                break;
                            case 5:
                                lidarType = LidarType.RSP;
                                break;
                            case 7:
                                lidarType = LidarType.PTX;
                                break;
                            case 8:
                                lidarType = LidarType.PTG;
                                break;
                            case 9:
                                lidarType = LidarType.XYB;
                                break;
                            default:
                                Logger.getLogger(Release.class).warn("Unsupported/deprecated voxelisation type (=" + inputType + "). AMAPVox arbitrarily sets it to LAS but the updated configuration will likely fail.");
                                lidarType = LidarType.LAS;
                        }
                        // add lidar type attribute to input element
                        inputElement.setAttribute("type", lidarType.name());
                        // remove input type attribute from main_file element
                        mainFileElement.removeAttribute("type");
                        // rename main_file element into project element
                        mainFileElement.setName("project");

                        // add scan element for single scan project
                        if (null == inputElement.getChild("scans")) {
                            inputElement.addContent(new Element("scans"));
                            Element scansElement = inputElement.getChild("scans");
                            if (null == scansElement.getChildren("scan") || scansElement.getChildren("scan").isEmpty()) {
                                Element scanElement = new Element("scan");
                                scanElement.setAttribute("src", mainFileElement.getAttributeValue("src"));
                                Matrix4d identity = new Matrix4d();
                                identity.setIdentity();
                                scanElement.addContent(new Element("matrix").setText(identity.toString()));
                                scansElement.addContent(scanElement);
                            }
                        }
                    }
                }

                // delete process type attribute
                if (null != processElement.getAttribute("type")) {
                    processElement.removeAttribute("type");
                }
            }
        },
        // 2021-06-17
        new Release("1.7.5") {

            @Override
            public void update(Element processElement) {

                Element voxelSpaceElement = processElement.getChild("voxelspace");

                // replaced xmin, xmin, zmin attributes by single min attribute
                Point3d min = new Point3d(
                        Double.valueOf(voxelSpaceElement.getAttributeValue("xmin")),
                        Double.valueOf(voxelSpaceElement.getAttributeValue("ymin")),
                        Double.valueOf(voxelSpaceElement.getAttributeValue("zmin"))
                );
                voxelSpaceElement.removeAttribute("xmin");
                voxelSpaceElement.removeAttribute("ymin");
                voxelSpaceElement.removeAttribute("zmin");
                voxelSpaceElement.setAttribute("min", min.toString());

                // replaced xmax, xmax, zmax attributes by single max attribute
                Point3d max = new Point3d(
                        Double.valueOf(voxelSpaceElement.getAttributeValue("xmax")),
                        Double.valueOf(voxelSpaceElement.getAttributeValue("ymax")),
                        Double.valueOf(voxelSpaceElement.getAttributeValue("zmax"))
                );
                voxelSpaceElement.removeAttribute("xmax");
                voxelSpaceElement.removeAttribute("ymax");
                voxelSpaceElement.removeAttribute("zmax");
                voxelSpaceElement.setAttribute("max", max.toString());

                // replaced splitX, splitY, splitZ attributes by single split attribute
                Point3i split = new Point3i(
                        Integer.valueOf(voxelSpaceElement.getAttributeValue("splitX")),
                        Integer.valueOf(voxelSpaceElement.getAttributeValue("splitY")),
                        Integer.valueOf(voxelSpaceElement.getAttributeValue("splitZ"))
                );
                voxelSpaceElement.removeAttribute("splitX");
                voxelSpaceElement.removeAttribute("splitY");
                voxelSpaceElement.removeAttribute("splitZ");
                voxelSpaceElement.setAttribute("split", split.toString());

                // updated resolution attribute with 3 dimensions
                float resolution = Float.valueOf(voxelSpaceElement.getAttributeValue("resolution"));
                voxelSpaceElement.setAttribute("resolution", new Point3d(resolution, resolution, resolution).toString());
            }
        },
        // 2022-01-12
        new Release("1.9.3") {
            @Override
            public void update(Element processElement) {

                // update scan sop matrix
                Element inputElement = processElement.getChild("input");
                if (null != inputElement) {
                    Element scansElement = inputElement.getChild("scans");
                    if (null != scansElement) {
                        List<Element> scanList = scansElement.getChildren("scan");
                        scanList.forEach((scanElement) -> {
                            Matrix matrix = Matrix.valueOf(scanElement.getChild("matrix"));
                            matrix.setId("sop");
                            scanElement.removeChild("matrix");
                            scanElement.addContent(matrix.toElement());
                        });
                    }
                }

                // update echo weighting matrix
                Element echoWeightinhgElement = processElement.getChild("echo_weighting");
                if (echoWeightinhgElement != null) {
                    Element matrixElement = echoWeightinhgElement.getChild("matrix");
                    if (null != matrixElement) {
                        Matrix matrix = Matrix.valueOf(matrixElement);
                        matrix.setId("echo-weights");
                        echoWeightinhgElement.removeChild("matrix");
                        echoWeightinhgElement.addContent(matrix.toElement());
                    }
                }

                // update transformation matrices
                Element transformationElement = processElement.getChild("transformation");
                if (null != transformationElement) {
                    List<Element> deprecatedMatrixList = transformationElement.getChildren("matrix");
                    List<Element> updatedMatrixList = new ArrayList<>(deprecatedMatrixList.size());
                    deprecatedMatrixList.forEach((matrixElement) -> {
                        Matrix matrix = Matrix.valueOf(matrixElement);
                        matrix.setId(matrixElement.getAttributeValue("type_id"));
                        updatedMatrixList.add(matrix.toElement());
                    });
                    transformationElement.removeChildren("matrix");
                    transformationElement.addContent(updatedMatrixList);
                }
            }
        },
        // 2022-03-07   
        new Release("1.10.0") {
            @Override
            public void update(Element processElement) {

                // replaces output variables attributes by a list of variable elements
                // every output variable will be listed
                Element outputElement = processElement.getChild("output");
                if (null != outputElement) {
                    Element variablesElement = outputElement.getChild("variables");
                    // list variables and set 'enabled' status
                    HashMap<OutputVariable, Boolean> variables = new HashMap<>();
                    for (OutputVariable variable : OutputVariable.values()) {
                        variables.put(variable, variable.isEnabledByDefault());
                    }
                    variablesElement.getAttributes().forEach(attribute -> {
                        OutputVariable variable = OutputVariable.valueOf(attribute.getName().toUpperCase());
                        variables.put(variable, Boolean.valueOf(attribute.getValue()));
                    });
                    // replace attributes by list of elements
                    for (OutputVariable variable : OutputVariable.values()) {
                        if (!(variable.isCoordinateVariable())) {
                            Element variableElement = new Element("variable");
                            variableElement.setAttribute("name", variable.name().toLowerCase());
                            variableElement.setAttribute("enabled", String.valueOf(variables.get(variable)));
                            variablesElement.addContent(variableElement);
                        }
                    }
                    // clear variables attributes
                    String[] attributes = variablesElement.getAttributes()
                            .stream()
                            .map(attribute -> attribute.getName())
                            .toArray(String[]::new);
                    for (String attribute : attributes) {
                        variablesElement.removeAttribute(attribute);
                    }

                    for (Element variableElement : variablesElement.getChildren("variable")) {
                        OutputVariable variable = OutputVariable.valueOf(variableElement.getAttributeValue("name").toUpperCase());
                        switch (variable) {
                            case ESTIMATED_TRANSMITTANCE:
                                Element transmittanceElement = processElement.getChild("transmittance");
                                if (null != transmittanceElement) {
                                    transmittanceElement.detach();
                                    transmittanceElement.setName("parameters");
                                    variableElement.addContent(transmittanceElement);
                                    processElement.removeChild(transmittanceElement.getName());
                                }
                                break;
                            case PLANT_AREA_DENSITY:
                                Element limitsElement = processElement.getChild("limits");
                                if (limitsElement != null) {
                                    List<Element> limitChildrensElement = limitsElement.getChildren("limit");
                                    if (limitChildrensElement != null) {
                                        if (limitChildrensElement.size() > 0) {
                                            Element padElement = new Element("parameters");
                                            padElement.setAttribute("max-pad", limitChildrensElement.get(0).getAttributeValue("max"));
                                            variableElement.addContent(padElement);
                                        }
                                    }
                                    processElement.removeContent(limitsElement);
                                }
                                break;
                            case ATTENUATION_PPL_MLE:
                                Element attenuationElement = processElement.getChild("attenuation");
                                if (null != attenuationElement) {
                                    attenuationElement.detach();
                                    attenuationElement.setName("parameters");
                                    variableElement.addContent(attenuationElement);
                                    processElement.removeChild(attenuationElement.getName());
                                }
                                break;
                            case EXPLORATION_RATE:
                                Element voxelspaceElement = processElement.getChild("voxelspace");
                                Element subvoxelElement = new Element("parameters");
                                if (null != voxelspaceElement.getAttribute("subvoxel")) {
                                    subvoxelElement.setAttribute("subvoxel", voxelspaceElement.getAttributeValue("subvoxel"));
                                    voxelspaceElement.removeAttribute("subvoxel");
                                } else {
                                    // default value if subvoxel attribute was not previously set
                                    subvoxelElement.setAttribute("subvoxel", "2");
                                }
                                variableElement.addContent(subvoxelElement);
                                break;
                        }
                    }

                    // renamed voxel_file into voxels
                    Element voxelsElement = outputElement.getChild("voxel_file");
                    voxelsElement.setName("voxels");
                    // move src attribute to output element
                    outputElement.setAttribute("src", voxelsElement.getAttributeValue("src"));
                    voxelsElement.removeAttribute("src");
                    // add enabled attribute to voxels element
                    boolean enabled = null != voxelsElement.getAttribute("format")
                            ? !voxelsElement.getAttributeValue("format").equalsIgnoreCase("none")
                            : true;
                    voxelsElement.setAttribute("enabled", String.valueOf(enabled));

                    // moved decimal-format#fraction-digits attribute to voxels element
                    Element decimalFormatElement = outputElement.getChild("decimal-format");
                    if (null != decimalFormatElement) {
                        voxelsElement.setAttribute("fraction-digits", decimalFormatElement.getAttributeValue("fraction-digits"));
                        outputElement.removeChild("decimal-format");
                    }

                    // moved variables element inside voxels element
                    voxelsElement.addContent(variablesElement.detach());
                    outputElement.removeChild("variables");
                }

                // renamed dtm-filter shot-filter and echo-filters element into filter element
                // moved them directly under filters element
                Element filtersElement = processElement.getChild("filters");
                if (null != filtersElement) {
                    // dtm filter
                    Element dtmFilterElement = filtersElement.getChild("dtm-filter");
                    if (null != dtmFilterElement) {
                        dtmFilterElement.setName("filter");
                        dtmFilterElement.setAttribute("classname", DigitalTerrainModelFilter.class.getCanonicalName());
                        // move dtm file attributes to dtm element in input element
                        Element dtmElement = new Element("dtm");
                        dtmElement.setAttribute("src", null != dtmFilterElement.getAttribute("src") ? dtmFilterElement.getAttributeValue("src") : "");
                        dtmElement.setAttribute("use-vop", null != dtmFilterElement.getAttribute("use-vop") ? dtmFilterElement.getAttributeValue("use-vop") : String.valueOf(true));
                        processElement.getChild("input").addContent(dtmElement);
                        // delete deprecated elements in dtm filter element
                        if (null != dtmFilterElement.getAttribute("src")) {
                            dtmFilterElement.removeAttribute("src");
                        }
                        if (null != dtmFilterElement.getAttribute("use-vop")) {
                            dtmFilterElement.removeAttribute("use-vop");
                        }
                    }

                    // shot filters
                    Element shotfiltersElement = filtersElement.getChild("shot-filters");
                    if (null != shotfiltersElement) {
                        shotfiltersElement.getChildren().forEach(filterElement -> {
                            filterElement.detach();
                            filtersElement.addContent(filterElement);
                        });
                        filtersElement.removeContent(shotfiltersElement);
                    }

                    // echo filters
                    Element echoFiltersElement = filtersElement.getChild("echo-filters");
                    if (null != echoFiltersElement) {
                        echoFiltersElement.getChildren().forEach(filterElement -> {
                            filterElement.detach();
                            filtersElement.addContent(filterElement);
                        });
                        filtersElement.removeContent(echoFiltersElement);
                    }

                    // move filter attributes other than enabled and classname to parameters element
                    filtersElement.getChildren().forEach(filterElement -> {
                        Element parametersElement = new Element("parameters");
                        List<String> attributes = new ArrayList<>();
                        filterElement.getAttributes().stream()
                                .filter(attribute -> !(attribute.getName().equalsIgnoreCase("classname") || attribute.getName().equalsIgnoreCase("enabled")))
                                .forEach(attribute -> {
                                    parametersElement.setAttribute(attribute.getName(), attribute.getValue());
                                    attributes.add(attribute.getName());
                                });
                        if (!parametersElement.getAttributes().isEmpty()) {
                            filterElement.addContent(parametersElement);
                            attributes.forEach(attribute -> filterElement.removeAttribute(attribute));
                        }
                    });
                }

                // renamed echo_weighting by echo-weighting
                if (null != processElement.getChild("echo_weighting")) {
                    Element echoWeightingElement = processElement.getChild("echo_weighting");
                    echoWeightingElement.setName("echo-weighting");
                    // renamed weight_file by weight-file
                    if (null != echoWeightingElement.getChild("weight_file")) {
                        echoWeightingElement.getChild("weight_file").setName("weight-file");
                    }
                }
            }
        },
        // 2022-04-08
        new Release("1.10.1") {
            @Override
            public void update(Element processElement) {

                Element outputElement = processElement.getChild("output");
                if (null != outputElement) {
                    // output path replaced by output file
                    // arbitrarily name voxel space file voxelization.vox
                    if (null != outputElement.getAttribute("src")) {
                        outputElement.setAttribute("src", new File(outputElement.getAttributeValue("src"), "voxelization.vox").toString());
                    }
                }
            }
        },
        // 2022-04-22
        new Release("1.10.2") {
            @Override
            public void update(Element processElement) {

                // remove deprecated output variables
                // PLANT_AREA_DENSITY
                removeDeprecatedOutputVariables(processElement);

                Element outputElement = processElement.getChild("output");
                if (null != outputElement) {
                    // remove deprecated merging element
                    outputElement.removeChild("merging");
                }

                // remove deprecated leaf-angle-distribution element
                processElement.removeChild("leaf-angle-distribution");
            }
        },
        // 2022-05-04
        new Release("1.10.3") {
            @Override
            public void update(Element processElement) {

                // remove deprecated output variables
                // SUBSAMPLING
                removeDeprecatedOutputVariables(processElement);
            }
        },
        // 2023-12-14
        new Release("2.1.0") {
            @Override
            public void update(Element processElement) {

                // new way of inputing echo weight functions
                Element echoWeightingElement = processElement.getChild("echo-weighting");
                Element echoWeightsElement = new Element("echo-weights");
                if (null != echoWeightingElement) {
                    // rank echo-weight
                    boolean enabled = Boolean.parseBoolean(echoWeightingElement.getAttributeValue("byrank"));
                    if (enabled) {
                        Element rankEchoWeightElement = new Element("echo-weight");
                        rankEchoWeightElement.setAttribute("enabled", Boolean.TRUE.toString());
                        rankEchoWeightElement.setAttribute("classname", org.amapvox.shot.weight.RankEchoWeight.class.getCanonicalName());
                        Element matrixElement = echoWeightingElement.getChild("matrix").detach();
                        Element parametersElement = new Element("paremeters");
                        parametersElement.addContent(matrixElement);
                        rankEchoWeightElement.addContent(parametersElement);
                        echoWeightsElement.addContent(rankEchoWeightElement);
                    }
                    
                    // shot echo weight
                    enabled = Boolean.parseBoolean(echoWeightingElement.getAttributeValue("byfile"));
                    if (enabled) {
                        Element shotEchoWeight = new Element("echo-weight");
                        shotEchoWeight.setAttribute("enabled", Boolean.TRUE.toString());
                        shotEchoWeight.setAttribute("classname", org.amapvox.shot.weight.ShotEchoWeight.class.getCanonicalName());
                        Element weightFileElement = echoWeightingElement.getChild("weight-file");
                        String file = weightFileElement.getAttributeValue("src");
                        Element parametersElement = new Element("paremeters");
                        parametersElement.setAttribute("src", file);
                        shotEchoWeight.addContent(parametersElement);
                        echoWeightsElement.addContent(shotEchoWeight);
                    }
                    // add new <echo-weights> element
                    processElement.addContent(echoWeightsElement);
                    // delete deprecated <echo-weighting> element
                    processElement.removeChild("echo-weighting");
                }
            }
        }
    };

    // remove deprecated output variables for version >= 1.10.2
    private static void removeDeprecatedOutputVariables(Element processElement) {

        Element outputElement = processElement.getChild("output");
        if (null != outputElement) {
            // remove deprecated output variables
            Element variablesElement = outputElement.getChild("voxels").getChild("variables");
            List<Element> deprecatedVariables = variablesElement.getChildren().stream()
                    .filter(e -> {
                        try {
                            return OutputVariable.valueOf(e.getAttributeValue("name").toUpperCase()).isDeprecated();
                        } catch (Exception ex) {
                            // variable not recognized, assuming it is deprecated
                            return true;
                        }
                    }).collect(Collectors.toList());
            deprecatedVariables.forEach(e -> variablesElement.removeContent(e));
        }
    }

    private enum InputType {

        LAS_FILE(0),
        LAZ_FILE(1),
        SHOTS_FILE(2),
        POINTS_FILE(3),
        RXP_SCAN(4),
        RSP_PROJECT(5),
        VOXEL_FILE(6),
        PTX_PROJECT(7),
        PTG_PROJECT(8),
        XYB_PROJECT(9);

        public int type;

        private InputType(int type) {
            this.type = type;
        }
    }

}
