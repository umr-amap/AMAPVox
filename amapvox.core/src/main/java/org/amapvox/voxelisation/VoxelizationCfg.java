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
package org.amapvox.voxelisation;

import org.amapvox.commons.math.util.MatrixUtility;
import org.amapvox.commons.util.ArrayUtils;
import org.amapvox.commons.util.filter.Filter;
import org.amapvox.commons.Configuration;
import org.amapvox.commons.util.filter.FloatFilter;
import org.amapvox.commons.util.io.file.CSVFile;
import org.amapvox.commons.Matrix;
import org.amapvox.shot.Shot;
import org.amapvox.shot.filter.ClassifiedPointFilter;
import org.amapvox.shot.filter.DigitalTerrainModelFilter;
import org.amapvox.shot.filter.EchoAttributeFilter;
import org.amapvox.shot.filter.EchoRankFilter;
import org.amapvox.shot.filter.PointcloudFilter;
import org.amapvox.shot.filter.ShotAttributeFilter;
import org.amapvox.shot.filter.EchoRangeFilter;
import org.amapvox.shot.filter.ShotDecimationFilter;
import org.amapvox.commons.Util;
import org.amapvox.voxelisation.output.OutputVariable;
import org.amapvox.lidar.commons.LidarScan;
import org.amapvox.lidar.leica.ptx.PTXHeader;
import org.amapvox.lidar.leica.ptx.PTXLidarScan;
import org.amapvox.lidar.leica.ptx.PTXScan;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import org.amapvox.commons.AVoxTask;
import org.amapvox.commons.Release;
import org.amapvox.shot.Echo;
import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author calcul
 */
public class VoxelizationCfg extends Configuration {

    public enum VoxelsFormat {
        VOXEL,
        NETCDF;
    }

    public enum LidarType {
        RXP, RSP, PTX, PTG, XYB, LAS, LAZ, SHT;
    }

    public final static Matrix DEFAULT_ECHOES_WEIGHT = new Matrix(
            new double[][]{
                {1.d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
                {0.5d, 0.5d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
                {1 / 3.d, 1 / 3.d, 1 / 3.d, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
                {0.25d, 0.25d, 0.25d, 0.25d, Double.NaN, Double.NaN, Double.NaN},
                {0.2d, 0.2d, 0.2d, 0.2d, 0.2d, Double.NaN, Double.NaN},
                {1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d, Double.NaN},
                {1 / 7.d, 1 / 7.d, 1 / 7.d, 1 / 7.d, 1 / 7.d, 1 / 7.d, 1 / 7.d}}
    );

    private final static Logger LOGGER = Logger.getLogger(VoxelizationCfg.class);

    protected File dtmFile;
    protected File outputFile;
    protected VoxelsFormat voxelsFormat = VoxelsFormat.VOXEL;
    protected boolean usePopMatrix;
    protected boolean useSopMatrix;
    protected boolean useVopMatrix;
    protected boolean dtmUseVopMatrix;
    protected Matrix4d popMatrix;
    protected Matrix4d vopMatrix;
    protected List<Filter<Shot>> shotFilters;
    protected List<Filter<Echo>> echoFilters;
    protected DecimalFormat decimalFormat;
    private List<LidarScan> lidarScans;
    protected LidarType lidarType;

    // RXP/RSP only
    private boolean enableEmptyShotsFiltering;

    // LAS/LAZ only
    private CSVFile trajectoryFile;
    private Point3d scannerPosition;
    private boolean echoConsistencyCheckEnabled;
    private boolean echoConsistencyWarningEnabled;
    private boolean collinearityCheckEnabled;
    private boolean collinearityWarningEnabled;
    private double collinearityMaxDeviation;

    // Voxelization parameters
    //voxel space parameters
    private Point3d minCorner;
    private Point3d maxCorner;
    private Point3i dimension;
    private Point3d voxelSize;

    // transmittance parameters
    private double trNumEstimError = 1e-7d;
    private double trNumEstimFallbackError = 0.01d;
    private int nTrRecordMax = 0;

    // maximal attenuation
    private double maxAttenuation = 20.f;
    private double attenuationError = 1e-7d;

    private Matrix echoesWeightMatrix;
    private File echoWeightsFile;
    private LidarScan lidarScan;

    private LaserSpecification laserSpecification = null;

    private final HashMap<OutputVariable, Boolean> outputVariablesEnabled;

    // default mean single leaf area set to 10cm * 10cm = 0.01m^2
    private double meanLeafArea = 0.01;

    // sub voxel split (for computing exploration rate)
    private int subVoxelSplit = 4;

    private boolean voxelOutputEnabled = true;

    // skip empty voxel
    private boolean skipEmptyVoxel;

    // path length export
    private boolean pathLengthEnabled = false;
    private int pathLengthMaxSize = 100;

    public VoxelizationCfg() {
        super("VOXELIZATION", "Voxelization",
                "Tracks every laser pulse through 3D grid (voxel space) and computes the local transmittance or local attenuation per voxel.",
                new String[]{"VOXELISATION"});
        shotFilters = new ArrayList<>();
        echoFilters = new ArrayList<>();
        decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        decimalFormat.applyPattern("#0.############");
        decimalFormat.setGroupingUsed(false);
        popMatrix = new Matrix4d();
        popMatrix.setIdentity();
        vopMatrix = new Matrix4d();
        vopMatrix.setIdentity();
        outputVariablesEnabled = new HashMap<>();
    }

    @Override
    public Release[] getReleases() {
        return VoxelizationReleases.ALL;
    }

    @Override
    public Class<? extends AVoxTask> getTaskClass() {
        return VoxelizationTask.class;
    }

    public static LidarType readInputType(File file) {

        try {
            SAXBuilder sxb = new SAXBuilder();
            Document document = sxb.build(file);
            Element root = document.getRootElement();

            Element processElement = root.getChild("process");

            Element inputElement = processElement.getChild("input");
            if (null != inputElement && (null != inputElement.getAttribute("type"))) {
                return LidarType.valueOf(inputElement.getAttributeValue("type").toUpperCase());
            }
        } catch (IllegalArgumentException | JDOMException | IOException ex) {
            LOGGER.warn("Unsupported/deprecated lidar type. Must be one of " + Arrays.toString(LidarType.values()), ex);
        }

        return null;
    }

    @Override
    public void readProcessElements(Element processElement) throws IOException {

        shotFilters.clear();
        echoFilters.clear();

        Element inputElement = processElement.getChild("input");
        if (null != inputElement) {
            // lidar type
            try {
                lidarType = LidarType.valueOf(inputElement.getAttributeValue("type").toUpperCase());
            } catch (NullPointerException | IllegalArgumentException ex) {
                LOGGER.warn("Unsupported/deprecated/missing lidar type. Must be one of " + Arrays.toString(LidarType.values()), ex);
                throw new IOException(ex);
            }

            // lidar scans
            Element scansElement = inputElement.getChild("scans");
            List<Element> scanElements = scansElement.getChildren("scan");
            lidarScans = new ArrayList<>();
            int count = 0;
            for (Element scanElement : scanElements) {
                Matrix4d mat = new Matrix4d();
                mat.setIdentity();
                if (null != scanElement.getChild("matrix")) {
                    mat = Matrix.valueOf(scanElement.getChild("matrix")).toMatrix4d();
                }
                File f = new File(resolve(scanElement.getAttributeValue("src")));

                if (lidarType == LidarType.PTX) {
                    long offset = Long.valueOf(scanElement.getAttributeValue("offset"));
                    int numRows = Integer.valueOf(scanElement.getAttributeValue("numRows"));
                    int numCols = Integer.valueOf(scanElement.getAttributeValue("numCols"));
                    PTXHeader header = new PTXHeader();
                    header.setNumRows(numRows);
                    header.setNumCols(numCols);
                    header.setPointInDoubleFormat(true);
                    PTXScan scan = new PTXScan(f, header, offset);
                    lidarScans.add(new PTXLidarScan(f, mat, scan, count++));
                } else {
                    lidarScans.add(new LidarScan(f, mat));
                }
            }

            // scanner position
            if (null != inputElement.getChild("trajectory")) {
                Element trajectoryElement = inputElement.getChild("trajectory");
                if (!trajectoryElement.getAttributeValue("src").equalsIgnoreCase("null")) {
                    trajectoryFile = new CSVFile(resolve(trajectoryElement.getAttributeValue("src")));
                    trajectoryFile.setColumnSeparator(null != trajectoryElement.getAttribute("column-separator")
                            ? trajectoryElement.getAttributeValue("column-separator")
                            : " ");
                    trajectoryFile.setContainsHeader(null != trajectoryElement.getAttribute("has-header")
                            ? Boolean.valueOf(trajectoryElement.getAttributeValue("has-header"))
                            : true);
                    trajectoryFile.setNbOfLinesToRead(null != trajectoryElement.getAttribute("nb-of-lines-to-read")
                            ? Long.valueOf(trajectoryElement.getAttributeValue("nb-of-lines-to-read"))
                            : Integer.MAX_VALUE);
                    trajectoryFile.setNbOfLinesToSkip(null != trajectoryElement.getAttribute("nb-of-lines-to-skip")
                            ? Long.valueOf(trajectoryElement.getAttributeValue("nb-of-lines-to-skip"))
                            : 0L);
                    String columnAssignment = (null != trajectoryElement.getAttribute("column-assignment"))
                            ? trajectoryElement.getAttributeValue("column-assignment")
                            : "Elevation=0,Northing=1,Time=2,Easting=3";
                    Map<String, Integer> colMap = new HashMap<>();
                    String[] split = columnAssignment.split(",");
                    for (String s : split) {
                        int indexOfSep = s.indexOf("=");
                        String key = s.substring(0, indexOfSep);
                        String value = s.substring(indexOfSep + 1, s.length());
                        colMap.put(key, Integer.valueOf(value));
                    }
                    trajectoryFile.setColumnAssignment(colMap);
                }
            }

            if (null != inputElement.getChild("scanner")) {
                String position = inputElement.getChild("scanner").getAttributeValue("position");
                if (!position.equalsIgnoreCase("null")) {
                    setScannerPosition(new Point3d(
                            ArrayUtils.parseDoubleArray(position)));
                }
            }

            if (null != inputElement.getChild("als-consistency")) {
                Element consistencyElement = inputElement.getChild("als-consistency");
                echoConsistencyCheckEnabled = Boolean.valueOf(consistencyElement.getAttributeValue("echoes"));
                echoConsistencyWarningEnabled = Boolean.valueOf(consistencyElement.getAttributeValue("echoes-warn"));
                collinearityCheckEnabled = Boolean.valueOf(consistencyElement.getAttributeValue("collinearity"));
                collinearityWarningEnabled = Boolean.valueOf(consistencyElement.getAttributeValue("collinearity-warn"));
                collinearityMaxDeviation = Double.valueOf(consistencyElement.getAttributeValue("max-deviation"));
            } else {
                // set to false for backward compatibility
                echoConsistencyCheckEnabled = false;
                echoConsistencyWarningEnabled = false;
                collinearityCheckEnabled = false;
                collinearityWarningEnabled = false;
                collinearityMaxDeviation = 5.d;
            }

            Element dtmElement = inputElement.getChild("dtm");
            if (null != dtmElement) {
                if (null != dtmElement.getAttribute("src") && !dtmElement.getAttributeValue("src").isEmpty()) {
                    dtmFile = new File(dtmElement.getAttributeValue("src"));
                }
                dtmUseVopMatrix = Boolean.valueOf(dtmElement.getAttributeValue("use-vop"));
            }

        } else {
            throw new IOException("Cannot find input element");
        }

        // output element
        Element outputElement = processElement.getChild("output");
        if (null != outputElement) {
            // output path
            outputFile = new File(resolve(outputElement.getAttributeValue("src")));

            // voxel_file element
            Element voxelsElement = outputElement.getChild("voxels");
            if (voxelsElement != null) {
                // voxel output enabled
                setVoxelOutputEnabled(
                        null != voxelsElement.getAttribute("enabled")
                        ? Boolean.valueOf(voxelsElement.getAttributeValue("enabled"))
                        : true
                );
                // voxel output format
                String format = null != voxelsElement.getAttribute("format")
                        ? voxelsElement.getAttributeValue("format").trim()
                        : VoxelsFormat.VOXEL.toString();
                for (VoxelsFormat voxformat : VoxelsFormat.values()) {
                    if (format.equalsIgnoreCase(voxformat.name())) {
                        voxelsFormat = voxformat;
                        break;
                    }
                }
                // skip empty voxels
                setSkipEmptyVoxel(null != voxelsElement.getAttribute("skip-empty-voxel")
                        ? Boolean.valueOf(voxelsElement.getAttributeValue("skip-empty-voxel"))
                        : false);
                // number of fraction digits
                if (null != voxelsElement.getAttribute("fraction-digits")) {
                    decimalFormat.setMaximumFractionDigits(Integer.valueOf(voxelsElement.getAttributeValue("fraction-digits")));
                }
                setFractionDigits(decimalFormat.getMaximumFractionDigits());

                // variables element
                Element variablesElement = voxelsElement.getChild("variables");
                if (variablesElement != null) {

                    List<Element> variableElements = variablesElement.getChildren("variable");
                    variableElements.forEach((variableElement) -> {
                        try {
                            OutputVariable variable = OutputVariable.valueOf(variableElement.getAttributeValue("name").toUpperCase());
                            setOutputVariableEnabled(variable, Boolean.valueOf(variableElement.getAttributeValue("enabled")));
                            Element parametersElement = variableElement.getChild("parameters");
                            if (null != parametersElement) {
                                // output variable specific parameters
                                switch (variable) {
                                    case ESTIMATED_TRANSMITTANCE:
                                        // check whether attributes exist for backward compatibility
                                        if (null != parametersElement.getAttribute("error")) {
                                            setTrNumEstimError(Double.valueOf(parametersElement.getAttributeValue("error")));
                                        }
                                        if (null != parametersElement.getAttribute("fallback-error")) {
                                            setTrNumEstimFallbackError(Double.valueOf(parametersElement.getAttributeValue("fallback-error")));
                                        }
                                        if (null != parametersElement.getAttribute("nrecordmax")) {
                                            setNTrRecordMax(Integer.valueOf(parametersElement.getAttributeValue("nrecordmax")));
                                        }

                                        break;
                                    case ATTENUATION_PPL_MLE:
                                        // maximal attenuation
                                        if (null != parametersElement.getAttribute("maximal-attenuation")) {
                                            setMaxAttenuation(Double.valueOf(parametersElement.getAttributeValue("maximal-attenuation")));
                                        }
                                        // attenuation error
                                        if (null != parametersElement.getAttribute("error")) {
                                            setAttenuationError(Double.valueOf(parametersElement.getAttributeValue("error")));
                                        }
                                        break;
                                    case EXPLORATION_RATE:
                                        if (null != parametersElement.getAttribute("subvoxel")) {
                                            setSubVoxelSplit(Integer.valueOf(parametersElement.getAttributeValue("subvoxel")));
                                        }
                                        break;
                                }
                            }
                        } catch (java.lang.IllegalArgumentException ex) {
                            // the configuration file contains an output variable name that is not listed in Voxel.java
                            LOGGER.warn("Output variable attribute " + variableElement.getAttributeValue("name") + " does not match any known variables in Voxel.java");
                        }
                    });
                }

            } else {
                throw new IOException("Cannot find output/voxels element");
            }

            // path length export element
            Element pathLengthElement = outputElement.getChild("pathlength");
            if (null != pathLengthElement) {
                setPathLengthEnabled(Boolean.valueOf(pathLengthElement.getAttributeValue("enabled")));
                setPathLengthMaxSize(Integer.valueOf(pathLengthElement.getAttributeValue("array-max-size")));
            }

        } else {
            throw new IOException("Cannot find output element");
        }

        Element voxelSpaceElement = processElement.getChild("voxelspace");

        if (voxelSpaceElement != null) {
            setMinCorner(new Point3d(
                    ArrayUtils.parseDoubleArray(voxelSpaceElement.getAttributeValue("min"))));
            setMaxCorner(new Point3d(
                    ArrayUtils.parseDoubleArray(voxelSpaceElement.getAttributeValue("max"))));
            setDimension(new Point3i(
                    ArrayUtils.parseIntArray(voxelSpaceElement.getAttributeValue("split"))));
            setVoxelSize(new Point3d(
                    ArrayUtils.parseDoubleArray(voxelSpaceElement.getAttributeValue("resolution"))));

        } else {
            //logger.info("Cannot find bounding-box element");
        }

        Element echoWeightinhgElement = processElement.getChild("echo-weighting");

        if (echoWeightinhgElement != null) {

            // weighting by rank
            if (Boolean.valueOf(echoWeightinhgElement.getAttributeValue("byrank"))) {
                Element matrixElement = echoWeightinhgElement.getChild("matrix");
                Matrix matrix = Matrix.valueOf(matrixElement.getText());
                setEchoesWeightMatrix(matrix);
            } else {
                setEchoesWeightMatrix(null);
            }

            // weighting from external CSV file
            if (Boolean.valueOf(echoWeightinhgElement.getAttributeValue("byfile"))) {
                Element weightFileElement = echoWeightinhgElement.getChild("weight-file");
                String weightFile = resolve(weightFileElement.getAttributeValue("src"));
                setEchoWeightsFile(new File(resolve(weightFile)));
            } else {
                setEchoWeightsFile(null);
            }
        }

        Element transformationElement = processElement.getChild("transformation");

        if (transformationElement != null) {
            usePopMatrix = Boolean.valueOf(transformationElement.getAttributeValue("use-pop"));
            useSopMatrix = Boolean.valueOf(transformationElement.getAttributeValue("use-sop"));
            useVopMatrix = Boolean.valueOf(transformationElement.getAttributeValue("use-vop"));
            List<Element> matrixList = transformationElement.getChildren("matrix");
            for (Element e : matrixList) {
                Matrix matrix = Matrix.valueOf(e);
                switch (matrix.getId()) {
                    case "pop":
                        popMatrix = matrix.toMatrix4d();
                        break;
                    case "vop":
                        vopMatrix = matrix.toMatrix4d();
                        break;
                }
            }
        }

        Element filtersElement = processElement.getChild("filters");

        if (filtersElement != null) {
            List<Element> filterElements = filtersElement.getChildren("filter");
            for (Element filterElement : filterElements) {
                // get class name
                String classname = filterElement.getAttributeValue("classname");
                // enabled
                if (!Boolean.valueOf(filterElement.getAttributeValue("enabled"))) {
                    continue;
                }
                Element parametersElement = filterElement.getChild("parameters");
                if (classname.equalsIgnoreCase(ShotAttributeFilter.class.getCanonicalName())) {
                    // shot attribute filter
                    String variable = parametersElement.getAttributeValue("variable");
                    String inequality = parametersElement.getAttributeValue("inequality");
                    String value = parametersElement.getAttributeValue("value");
                    shotFilters.add(new ShotAttributeFilter(new FloatFilter(variable, Float.valueOf(value), FloatFilter.getConditionFromString(inequality))));
                } else if (classname.equalsIgnoreCase(ShotDecimationFilter.class.getCanonicalName())) {
                    // shot decimation
                    float decimationFactor = Float.valueOf(parametersElement.getAttributeValue("decimation-factor"));
                    shotFilters.add(new ShotDecimationFilter(decimationFactor));
                } else if (classname.equalsIgnoreCase(EchoRangeFilter.class.getCanonicalName())) {
                    // echo range filter
                    boolean blankEchoDiscarded = Boolean.valueOf(parametersElement.getAttributeValue("blank-echo-discarded"));
                    boolean warningEnabled = (null != parametersElement.getAttribute("warning-enabled"))
                            ? Boolean.valueOf(parametersElement.getAttributeValue("warning-enabled"))
                            : true;
                    shotFilters.add(new EchoRangeFilter(blankEchoDiscarded, warningEnabled));
                } else if (classname.equalsIgnoreCase(EchoAttributeFilter.class.getCanonicalName())) {
                    // echo attribute filter
                    String variable = parametersElement.getAttributeValue("variable");
                    String inequality = parametersElement.getAttributeValue("inequality");
                    String value = parametersElement.getAttributeValue("value");
                    echoFilters.add(new EchoAttributeFilter(new FloatFilter(variable, Float.valueOf(value), FloatFilter.getConditionFromString(inequality))));
                } else if (classname.equalsIgnoreCase(EchoRankFilter.class.getCanonicalName())) {
                    // echo rank filter
                    String src = resolve(parametersElement.getAttributeValue("src"));
                    String behavior = parametersElement.getAttributeValue("behavior").toUpperCase();
                    echoFilters.add(new EchoRankFilter(src, EchoRankFilter.Behavior.valueOf(behavior)));
                } else if (classname.equalsIgnoreCase(PointcloudFilter.class.getCanonicalName())) {
                    // point cloud filter
                    CSVFile file = new CSVFile(resolve(parametersElement.getAttributeValue("src")));
                    try {
                        String columnSeparator = parametersElement.getAttributeValue("column-separator");
                        String hasHeader = parametersElement.getAttributeValue("has-header");
                        String nbOfLinesToRead = parametersElement.getAttributeValue("nb-of-lines-to-read");
                        String nbOfLinesToSkip = parametersElement.getAttributeValue("nb-of-lines-to-skip");
                        String columnAssignment = parametersElement.getAttributeValue("column-assignment");

                        file.setColumnSeparator(columnSeparator);
                        file.setContainsHeader(Boolean.valueOf(hasHeader));
                        file.setNbOfLinesToRead(Long.valueOf(nbOfLinesToRead));
                        file.setNbOfLinesToSkip(Long.valueOf(nbOfLinesToSkip));

                        Map<String, Integer> colMap = new HashMap<>();
                        String[] split = columnAssignment.split(",");
                        for (String s : split) {
                            int indexOfSep = s.indexOf("=");
                            String key = s.substring(0, indexOfSep);
                            String value = s.substring(indexOfSep + 1, s.length());
                            colMap.put(key, Integer.valueOf(value));
                        }
                        file.setColumnAssignment(colMap);

                    } catch (Exception ex) {
                        LOGGER.warn("Pointcloud filter " + file.getName() + ". Using default CSV preferences.");
                    }

                    String behavior = parametersElement.getAttributeValue("behavior").toUpperCase();
                    boolean applyVOP = null != parametersElement.getAttribute("apply-vop")
                            ? Boolean.valueOf(parametersElement.getAttributeValue("apply-vop"))
                            : true;
                    Matrix4d vop = applyVOP && (null != vopMatrix)
                            ? new Matrix4d(vopMatrix)
                            : MatrixUtility.identity4d();
                    echoFilters.add(new PointcloudFilter(file,
                            Float.valueOf(parametersElement.getAttributeValue("error-margin")),
                            EchoRankFilter.Behavior.valueOf(behavior), vop));
                } else if (classname.equalsIgnoreCase(DigitalTerrainModelFilter.class.getCanonicalName())) {
                    // digital terrain model filter
                    echoFilters.add(new DigitalTerrainModelFilter(Float.valueOf(parametersElement.getAttributeValue("height-min"))));
                } else if (classname.equalsIgnoreCase(RXPFalseEmptyShotRemover.class.getCanonicalName())) {
                    enableEmptyShotsFiltering = true;
                } else if (classname.equalsIgnoreCase(ClassifiedPointFilter.class.getCanonicalName())) {
                    String classifications = parametersElement.getAttributeValue("classifications");
                    if (classifications != null && !classifications.isEmpty()) {
                        List<Integer> classifiedPointsToDiscard = new ArrayList<>();
                        String[] classificationsArray = classifications.split(" ");
                        for (String s : classificationsArray) {
                            classifiedPointsToDiscard.add(Integer.valueOf(s));
                        }
                        echoFilters.add(new ClassifiedPointFilter(classifiedPointsToDiscard));
                    }
                }
            }
        }

        Element laserSpecElement = processElement.getChild("laser-specification");

        if (laserSpecElement != null) {
            // laser-specification element exist in XML configuration file
            String laserSpecName = laserSpecElement.getAttributeValue("name");
            double beamDivergence = Double.valueOf(laserSpecElement.getAttributeValue("beam-divergence"));
            double beamDiameterAtExit = Double.valueOf(laserSpecElement.getAttributeValue("beam-diameter-at-exit"));
            boolean monoEcho = true;
            if (null != laserSpecElement.getAttribute("mono-echo")) {
                // AMAPVox > 1.0.#
                monoEcho = Boolean.valueOf(laserSpecElement.getAttributeValue("mono-echo"));
            } else {
                // AMAPVox 1.0.#, the mono-echo attribute did not exist yet
                // check if the laser specification is among the presets
                // and set the mono echo property accordingly
                boolean monoEchoSet = false;
                for (LaserSpecification laserSpec : LaserSpecification.getPresets()) {
                    if (laserSpec.isValidName(laserSpecName)) {
                        monoEcho = laserSpec.isMonoEcho();
                        monoEchoSet = true;
                        break;
                    }
                }
                // laser specification not found amon presets, better not make
                // any guess and throw error message
                if (!monoEchoSet) {
                    StringBuilder msg = new StringBuilder();
                    msg.append("Could not find mono-echo attribute in the laser specification. ");
                    msg.append("Edit manually the XML configuration file and add missing attribute <laser-specification mono-echo=\"true|false\"");
                    LOGGER.error(msg.toString());
                    throw new IOException("laser specification mono-echo attribute missing");
                }
            }

            // check with preset values
            for (LaserSpecification laserSpec : LaserSpecification.getPresets()) {
                if (laserSpec.isValidName(laserSpecName)) {
                    if (beamDiameterAtExit != laserSpec.getBeamDiameterAtExit()) {
                        LOGGER.warn("Laser " + laserSpecName + " beam diameter at exit " + beamDiameterAtExit + " differs from preset value " + laserSpec.getBeamDiameterAtExit());
                    }
                    if (beamDivergence != laserSpec.getBeamDivergence()) {
                        LOGGER.warn("Laser " + laserSpecName + " beam divergence " + beamDivergence + " differs from preset value " + laserSpec.getBeamDivergence());
                    }
                    if (monoEcho != laserSpec.isMonoEcho()) {
                        LOGGER.warn("Laser " + laserSpecName + " mono echo " + monoEcho + " differs from preset value " + laserSpec.isMonoEcho());
                    }
                    break;
                }
            }

            setLaserSpecification(new LaserSpecification(laserSpecName, beamDiameterAtExit, beamDivergence, monoEcho));
        } else {
            // laser-specification element does not exist in XML configuration file
            setLaserSpecification(LaserSpecification.UNITARY_BEAM_SECTION_MONO_ECHO);
            // inform user that a default value has been set
            LOGGER.warn("Could not find laser specification element in configuration file. Default specification:\n" + getLaserSpecification().toString());
        }

        // single leaf area
        Element leafAreaElement = processElement.getChild("single-leaf-area");
        if (null != leafAreaElement) {
            setMeanLeafArea(Double.valueOf(leafAreaElement.getAttributeValue("value")));
        }
    }

    @Override
    public void writeProcessElements(Element processElement
    ) {

        Element inputElement = new Element("input");
        inputElement.setAttribute(new Attribute("type", String.valueOf(lidarType)));
        processElement.addContent(inputElement);

        // lidar scans
        Element scansElement = new Element("scans");
        if (lidarScans != null) {
            for (LidarScan scan : lidarScans) {
                Element scanElement = new Element("scan");
                scanElement.setAttribute("src", scan.getFile().getAbsolutePath());

                if (lidarType == LidarType.PTX) {
                    scanElement.setAttribute("offset", String.valueOf(((PTXLidarScan) scan).getScan().getOffset()));
                    scanElement.setAttribute("numRows", String.valueOf(((PTXLidarScan) scan).getScan().getHeader().getNumRows()));
                    scanElement.setAttribute("numCols", String.valueOf(((PTXLidarScan) scan).getScan().getHeader().getNumCols()));
                }
                Matrix sop = Matrix.valueOf(scan.getMatrix());
                sop.setId("sop");
                scanElement.addContent(sop.toElement());
                scansElement.addContent(scanElement);
            }
        }
        inputElement.addContent(scansElement);

        // scanner position (LAS/LAZ)
        if (lidarType == LidarType.LAS || lidarType == LidarType.LAZ) {

            // trajectory element
            Element trajectoryFileElement = new Element("trajectory");
            if (null != trajectoryFile) {
                trajectoryFileElement.setAttribute(new Attribute("src", trajectoryFile.getAbsolutePath()));
                trajectoryFileElement.setAttribute(new Attribute("src", trajectoryFile.getAbsolutePath()));
                trajectoryFileElement.setAttribute(new Attribute("column-separator", trajectoryFile.getColumnSeparator()));
                trajectoryFileElement.setAttribute(new Attribute("header-index", String.valueOf(trajectoryFile.getHeaderIndex())));
                trajectoryFileElement.setAttribute(new Attribute("has-header", String.valueOf(trajectoryFile.containsHeader())));
                trajectoryFileElement.setAttribute(new Attribute("nb-of-lines-to-read", String.valueOf(trajectoryFile.getNbOfLinesToRead())));
                trajectoryFileElement.setAttribute(new Attribute("nb-of-lines-to-skip", String.valueOf(trajectoryFile.getNbOfLinesToSkip())));

                Map<String, Integer> columnAssignment = trajectoryFile.getColumnAssignment();
                Iterator<Map.Entry<String, Integer>> iterator = columnAssignment.entrySet().iterator();
                String colAssignment = new String();

                while (iterator.hasNext()) {
                    Map.Entry<String, Integer> entry = iterator.next();
                    colAssignment += entry.getKey() + "=" + entry.getValue() + ",";
                }
                trajectoryFileElement.setAttribute(new Attribute("column-assignment", colAssignment));
            } else {
                trajectoryFileElement.setAttribute(new Attribute("src", "null"));
            }
            // add trajectory element
            inputElement.addContent(trajectoryFileElement);

            // scanner position
            Element scannerPositionElement = new Element("scanner");
            scannerPositionElement.setAttribute("position", String.valueOf(scannerPosition));
            inputElement.addContent(scannerPositionElement);

            // echoes and collinearity checks
            Element consistencyElement = new Element("als-consistency");
            consistencyElement.setAttribute(new Attribute("echoes", String.valueOf(echoConsistencyCheckEnabled)));
            consistencyElement.setAttribute(new Attribute("echoes-warn", String.valueOf(echoConsistencyWarningEnabled)));
            consistencyElement.setAttribute(new Attribute("collinearity", String.valueOf(collinearityCheckEnabled)));
            consistencyElement.setAttribute(new Attribute("collinearity-warn", String.valueOf(collinearityWarningEnabled)));
            consistencyElement.setAttribute(new Attribute("max-deviation", String.valueOf(collinearityMaxDeviation)));
            inputElement.addContent(consistencyElement);
        }

        // dtm
        Element dtmElement = new Element("dtm");
        dtmElement.setAttribute("src", null != dtmFile ? dtmFile.getAbsolutePath() : "");
        dtmElement.setAttribute("use-vop", String.valueOf(dtmUseVopMatrix));
        inputElement.addContent(dtmElement);

        // output element
        Element outputElement = new Element("output");
        // output path
        outputElement.setAttribute(new Attribute("src", outputFile.getAbsolutePath()));
        processElement.addContent(outputElement);

        // voxel file element
        Element voxelsElement = new Element("voxels");
        voxelsElement.setAttribute(new Attribute("enabled", String.valueOf(isVoxelOutputEnabled())));
        voxelsElement.setAttribute(new Attribute("format", String.valueOf(voxelsFormat)));
        voxelsElement.setAttribute(new Attribute("skip-empty-voxel", String.valueOf(skipEmptyVoxel())));
        voxelsElement.setAttribute("fraction-digits", String.valueOf(decimalFormat.getMaximumFractionDigits()));

        // variables element
        Element variablesElement = new Element("variables");
        for (OutputVariable variable : OutputVariable.values()) {
            if (!(variable.isCoordinateVariable() || variable.isDeprecated())) {
                Element variableElement = new Element("variable");
                variableElement.setAttribute("name", variable.name().toLowerCase());
                variableElement.setAttribute("enabled", String.valueOf(isOutputVariableEnabled(variable)));
                Element parametersElement = new Element("parameters");
                switch (variable) {
                    case ESTIMATED_TRANSMITTANCE:
                        parametersElement.setAttribute(new Attribute("error", String.valueOf(getTrNumEstimError())));
                        parametersElement.setAttribute(new Attribute("fallback-error", String.valueOf(getTrNumEstimFallbackError())));
                        parametersElement.setAttribute(new Attribute("nrecordmax", String.valueOf(getNTrRecordMax())));
                        break;
                    case ATTENUATION_PPL_MLE:
                        parametersElement.setAttribute("maximal-attenuation", String.valueOf(getMaxAttenuation()));
                        parametersElement.setAttribute("error", String.valueOf(getAttenuationError()));
                        break;
                    case EXPLORATION_RATE:
                        parametersElement.setAttribute("subvoxel", String.valueOf(getSubVoxelSplit()));
                        break;
                }
                // add specific parameters element
                if (!parametersElement.getAttributes().isEmpty()) {
                    variableElement.addContent(parametersElement);
                }
                // add current variable element
                variablesElement.addContent(variableElement);
            }
        }
        voxelsElement.addContent(variablesElement);
        outputElement.addContent(voxelsElement);

        // path length export
        Element pathLengthElement = new Element("pathlength");
        pathLengthElement.setAttribute(new Attribute("enabled", String.valueOf(isPathLengthEnabled())));
        pathLengthElement.setAttribute(new Attribute("array-max-size", String.valueOf(getPathLengthMaxSize())));
        outputElement.addContent(pathLengthElement);

        Element voxelSpaceElement = new Element("voxelspace");
        voxelSpaceElement.setAttribute("min", String.valueOf(getMinCorner()));
        voxelSpaceElement.setAttribute("max", String.valueOf(getMaxCorner()));
        voxelSpaceElement.setAttribute("split", String.valueOf(getDimension()));
        voxelSpaceElement.setAttribute("resolution", String.valueOf(getVoxelSize()));
        voxelSpaceElement.setAttribute("subvoxel", String.valueOf(getSubVoxelSplit()));
        processElement.addContent(voxelSpaceElement);

        // echo weighting by rank
        Element echoWeightingElement = new Element("echo-weighting");
        echoWeightingElement.setAttribute(new Attribute("byrank", String.valueOf(null != getEchoesWeightMatrix())));
        if (null != getEchoesWeightMatrix()) {
            Matrix matrix = getEchoesWeightMatrix();
            matrix.setId("echo-weights");
            echoWeightingElement.addContent(matrix.toElement());
        }
        // echo weighting by file
        echoWeightingElement.setAttribute(new Attribute("byfile", String.valueOf(null != getEchoWeightsFile())));
        if (null != getEchoWeightsFile()) {
            Element weightFileElement = new Element("weight-file");
            weightFileElement.setAttribute("src", getEchoWeightsFile().getAbsolutePath());
            echoWeightingElement.addContent(weightFileElement);
        }

        processElement.addContent(echoWeightingElement);

        /**
         * *LASER SPECIFICATION**
         */
        Element laserSpecElement = new Element("laser-specification");
        laserSpecElement.setAttribute("name", getLaserSpecification().getName());
        laserSpecElement.setAttribute("beam-diameter-at-exit", String.valueOf(getLaserSpecification().getBeamDiameterAtExit()));
        laserSpecElement.setAttribute("beam-divergence", String.valueOf(getLaserSpecification().getBeamDivergence()));
        laserSpecElement.setAttribute("mono-echo", String.valueOf(getLaserSpecification().isMonoEcho()));
        processElement.addContent(laserSpecElement);

        /**
         * *TRANSFORMATION**
         */
        Element transformationElement = new Element("transformation");

        transformationElement.setAttribute(new Attribute("use-pop", String.valueOf(usePopMatrix)));
        transformationElement.setAttribute(new Attribute("use-sop", String.valueOf(useSopMatrix)));
        transformationElement.setAttribute(new Attribute("use-vop", String.valueOf(useVopMatrix)));

        Matrix matrix = Matrix.valueOf(popMatrix);
        matrix.setId("pop");
        transformationElement.addContent(matrix.toElement());

        matrix = Matrix.valueOf(vopMatrix);
        matrix.setId("vop");
        transformationElement.addContent(matrix.toElement());

        processElement.addContent(transformationElement);

        /**
         * FILTERS
         */
        Element filtersElement = new Element("filters");

        if (shotFilters != null && !shotFilters.isEmpty()) {

            for (Filter filter : shotFilters) {
                Element filterElement = new Element("filter");
                Element parametersElement = new Element("parameters");
                if (filter instanceof ShotAttributeFilter) {
                    FloatFilter f = ((ShotAttributeFilter) filter).getFilter();
                    parametersElement.setAttribute("variable", f.getVariable());
                    parametersElement.setAttribute("inequality", f.getConditionString());
                    parametersElement.setAttribute("value", String.valueOf(f.getValue()));
                } else if (filter instanceof ShotDecimationFilter) {
                    ShotDecimationFilter f = (ShotDecimationFilter) filter;
                    parametersElement.setAttribute("decimation-factor", String.valueOf(f.getDecimationFactor()));
                } else if (filter instanceof EchoRangeFilter) {
                    EchoRangeFilter f = (EchoRangeFilter) filter;
                    parametersElement.setAttribute("blank-echo-discarded", String.valueOf(f.isBlankEchoDiscarded()));
                    parametersElement.setAttribute("warning-enabled", String.valueOf(f.isWarningEnabled()));
                }
                if (!parametersElement.getAttributes().isEmpty()) {
                    filterElement.addContent(parametersElement);
                }
                filterElement.setAttribute("classname", filter.getClass().getCanonicalName());
                filterElement.setAttribute("enabled", Boolean.toString(true));
                filtersElement.addContent(filterElement);
            }
        }

        if (echoFilters != null && !echoFilters.isEmpty()) {
            for (Filter filter : echoFilters) {
                Element filterElement = new Element("filter");
                Element parametersElement = new Element("parameters");
                if (filter instanceof EchoAttributeFilter) {
                    FloatFilter f = ((EchoAttributeFilter) filter).getFilter();
                    parametersElement.setAttribute("variable", f.getVariable());
                    parametersElement.setAttribute("inequality", f.getConditionString());
                    parametersElement.setAttribute("value", String.valueOf(f.getValue()));
                } else if (filter instanceof EchoRankFilter) {
                    EchoRankFilter f = (EchoRankFilter) filter;
                    parametersElement.setAttribute("src", f.getFile().getAbsolutePath());
                    parametersElement.setAttribute("behavior", f.behavior().toString());
                } else if (filter instanceof PointcloudFilter) {
                    PointcloudFilter f = (PointcloudFilter) filter;
                    parametersElement.setAttribute(new Attribute("src", f.getPointcloudFile().getAbsolutePath()));
                    parametersElement.setAttribute(new Attribute("error-margin", String.valueOf(f.getPointcloudErrorMargin())));
                    parametersElement.setAttribute(new Attribute("apply-vop", String.valueOf(f.isApplyVOPMatrix())));
                    parametersElement.setAttribute(new Attribute("behavior", f.behavior().toString()));
                    parametersElement.setAttribute(new Attribute("column-separator", f.getPointcloudFile().getColumnSeparator()));
                    parametersElement.setAttribute(new Attribute("has-header", String.valueOf(f.getPointcloudFile().containsHeader())));
                    parametersElement.setAttribute(new Attribute("nb-of-lines-to-read", String.valueOf(f.getPointcloudFile().getNbOfLinesToRead())));
                    parametersElement.setAttribute(new Attribute("nb-of-lines-to-skip", String.valueOf(f.getPointcloudFile().getNbOfLinesToSkip())));
                    Map<String, Integer> columnAssignment = f.getPointcloudFile().getColumnAssignment();
                    Iterator<Map.Entry<String, Integer>> iterator = columnAssignment.entrySet().iterator();
                    String colAssignment = new String();
                    while (iterator.hasNext()) {
                        Map.Entry<String, Integer> entry = iterator.next();
                        colAssignment += entry.getKey() + "=" + entry.getValue() + ",";
                    }
                    parametersElement.setAttribute(new Attribute("column-assignment", colAssignment));
                } else if (filter instanceof DigitalTerrainModelFilter) {
                    parametersElement.setAttribute(new Attribute("height-min", String.valueOf(((DigitalTerrainModelFilter) filter).getMinDistance())));
                } else if (filter instanceof ClassifiedPointFilter) {
                    String classifications = ((ClassifiedPointFilter) filter).getClasses().stream()
                            .map(iclass -> String.valueOf(iclass) + " ")
                            .reduce("", String::concat).trim();
                    parametersElement.setAttribute("classifications", classifications.trim());
                }
                filterElement.setAttribute("enabled", Boolean.toString(true));
                filterElement.setAttribute("classname", filter.getClass().getCanonicalName());
                if (!parametersElement.getAttributes().isEmpty()) {
                    filterElement.addContent(parametersElement);
                }
                filtersElement.addContent(filterElement);
            }
        }

        /**
         * *EMPTY shots filtering**
         */
        Element emptyShotFiltering = new Element("filter");
        emptyShotFiltering.setAttribute("enabled", String.valueOf(enableEmptyShotsFiltering));
        emptyShotFiltering.setAttribute("classname", RXPFalseEmptyShotRemover.class.getCanonicalName());
        filtersElement.addContent(emptyShotFiltering);

        processElement.addContent(filtersElement);

        /**
         * LEAF properties
         */
        // single leaf area
        Element leafAreaElement = new Element("single-leaf-area");
        leafAreaElement.setAttribute("value", String.valueOf(getMeanLeafArea()));
        processElement.addContent(leafAreaElement);
    }

    public LidarType getLidarType() {
        return lidarType;
    }

    public void setLidarType(LidarType lidarType) {
        this.lidarType = lidarType;
    }

    public File getDTMFile() {
        return dtmFile;
    }

    public void setDTMFile(File file) {
        this.dtmFile = file;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public VoxelsFormat getVoxelsFormat() {
        return voxelsFormat;
    }

    public void setVoxelsFormat(VoxelsFormat voxelsFormat) {
        this.voxelsFormat = voxelsFormat;
    }

    public boolean isUsePopMatrix() {
        return usePopMatrix;
    }

    public void setUsePopMatrix(boolean usePopMatrix) {
        this.usePopMatrix = usePopMatrix;
    }

    public boolean isUseSopMatrix() {
        return useSopMatrix;
    }

    public void setUseSopMatrix(boolean useSopMatrix) {
        this.useSopMatrix = useSopMatrix;
    }

    public boolean isUseVopMatrix() {
        return useVopMatrix;
    }

    public void setUseVopMatrix(boolean useVopMatrix) {
        this.useVopMatrix = useVopMatrix;
    }

    public boolean isDTMUseVopMatrix() {
        return dtmUseVopMatrix;
    }

    public void setDTMUseVopMatrix(boolean useVopMatrix) {
        this.dtmUseVopMatrix = useVopMatrix;
    }

    public Matrix4d getPopMatrix() {
        return popMatrix;
    }

    public void setPopMatrix(Matrix4d popMatrix) {
        this.popMatrix = popMatrix;
    }

    public Matrix4d getVopMatrix() {
        return vopMatrix;
    }

    public void setVopMatrix(Matrix4d vopMatrix) {
        this.vopMatrix = vopMatrix;
    }

    public List<Filter<Shot>> getShotFilters() {
        return shotFilters;
    }

    public void addEchoFilter(Filter<Echo> filter) {
        this.echoFilters.add(filter);
    }

    public boolean removeEchoFilter(Filter<Echo> filter) {
        return this.echoFilters.remove(filter);
    }

    public void setShotFilters(List<Filter<Shot>> shotFilters) {
        this.shotFilters = shotFilters;
    }

    public List<Filter<Echo>> getEchoFilters() {
        return echoFilters;
    }

    public DecimalFormat getDecimalFormat() {
        return decimalFormat;
    }

    public void setFractionDigits(int ndigit) {
        decimalFormat.setMaximumFractionDigits(ndigit);
    }

    public List<LidarScan> getLidarScans() {
        return lidarScans;
    }

    public void setLidarScans(List<LidarScan> matricesAndFiles) {
        this.lidarScans = matricesAndFiles;
    }

    public boolean isEnableEmptyShotsFiltering() {
        return enableEmptyShotsFiltering;
    }

    public void setEnableEmptyShotsFiltering(boolean enableEmptyShotsFiltering) {
        this.enableEmptyShotsFiltering = enableEmptyShotsFiltering;
    }

    public CSVFile getTrajectoryFile() {
        return trajectoryFile;
    }

    public void setTrajectoryFile(CSVFile trajectoryFile) {
        this.trajectoryFile = trajectoryFile;
    }

    public Point3d getScannerPosition() {
        return scannerPosition;
    }

    public void setScannerPosition(Point3d position) {
        this.scannerPosition = new Point3d(position);
    }

    /**
     * @return the echoConsistencyCheckEnabled
     */
    public boolean isEchoConsistencyCheckEnabled() {
        return echoConsistencyCheckEnabled;
    }

    /**
     * @param echoConsistencyCheckEnabled the echoConsistencyCheckEnabled to set
     */
    public void setEchoConsistencyCheckEnabled(boolean echoConsistencyCheckEnabled) {
        this.echoConsistencyCheckEnabled = echoConsistencyCheckEnabled;
    }

    /**
     * @return the echoConsistencyWarningEnabled
     */
    public boolean isEchoConsistencyWarningEnabled() {
        return echoConsistencyWarningEnabled;
    }

    /**
     * @param echoConsistencyWarningEnabled the echoConsistencyWarningEnabled to
     * set
     */
    public void setEchoConsistencyWarningEnabled(boolean echoConsistencyWarningEnabled) {
        this.echoConsistencyWarningEnabled = echoConsistencyWarningEnabled;
    }

    /**
     * @return the collinearityCheckEnabled
     */
    public boolean isCollinearityCheckEnabled() {
        return collinearityCheckEnabled;
    }

    /**
     * @param collinearityCheckEnabled the collinearityCheckEnabled to set
     */
    public void setCollinearityCheckEnabled(boolean collinearityCheckEnabled) {
        this.collinearityCheckEnabled = collinearityCheckEnabled;
    }

    /**
     * @return the collinearityWarningEnabled
     */
    public boolean isCollinearityWarningEnabled() {
        return collinearityWarningEnabled;
    }

    /**
     * @param collinearityWarningEnabled the collinearityWarningEnabled to set
     */
    public void setCollinearityWarningEnabled(boolean collinearityWarningEnabled) {
        this.collinearityWarningEnabled = collinearityWarningEnabled;
    }

    /**
     * @return the collinearityMaxDeviation
     */
    public double getCollinearityMaxDeviation() {
        return collinearityMaxDeviation;
    }

    /**
     * @param collinearityMaxDeviation the collinearityMaxDeviation to set
     */
    public void setCollinearityMaxDeviation(double collinearityMaxDeviation) {
        this.collinearityMaxDeviation = collinearityMaxDeviation;
    }

    public LaserSpecification getLaserSpecification() {
        return laserSpecification;
    }

    public void setLaserSpecification(LaserSpecification laserSpecification) {
        this.laserSpecification = laserSpecification;
    }

    /**
     *
     * @return Echoes weigting parameters
     */
    public Matrix getEchoesWeightMatrix() {
        return echoesWeightMatrix;
    }

    /**
     *
     * @param echoesWeightMatrix Echoes weighting parameters
     */
    public void setEchoesWeightMatrix(Matrix echoesWeightMatrix) {
        this.echoesWeightMatrix = echoesWeightMatrix;
    }

    /**
     *
     * @return Echoes weighting parameters
     */
    public File getEchoWeightsFile() {
        return echoWeightsFile;
    }

    /**
     *
     * @param file path of the echo weights file
     */
    public void setEchoWeightsFile(File file) {
        this.echoWeightsFile = file;
    }

    public LidarScan getLidarScan() {
        return lidarScan;
    }

    public void setLidarScan(LidarScan lidarScan) {
        this.lidarScan = lidarScan;
    }

    public double getTrNumEstimError() {
        return this.trNumEstimError;
    }

    public void setTrNumEstimError(double error) {
        this.trNumEstimError = error;
    }

    public double getTrNumEstimFallbackError() {
        return this.trNumEstimFallbackError;
    }

    public void setTrNumEstimFallbackError(double error) {
        this.trNumEstimFallbackError = error;
    }

    public int getNTrRecordMax() {
        return this.nTrRecordMax;
    }

    public void setNTrRecordMax(int nTrRecordsMax) {
        this.nTrRecordMax = nTrRecordsMax;
    }

    public void setOutputVariableEnabled(OutputVariable variable, boolean enabled) {
        outputVariablesEnabled.put(variable, enabled);
    }

    public boolean isOutputVariableEnabled(OutputVariable variable) {
        return outputVariablesEnabled.containsKey(variable)
                ? outputVariablesEnabled.get(variable)
                : variable.isEnabledByDefault();
    }

    public void setMeanLeafArea(double leafArea) {
        this.meanLeafArea = leafArea;
    }

    public double getMeanLeafArea() {
        return meanLeafArea;
    }

    public void setSubVoxelSplit(int subVoxelSplit) {
        this.subVoxelSplit = subVoxelSplit;
    }

    public int getSubVoxelSplit() {
        return subVoxelSplit;
    }

    public boolean isVoxelOutputEnabled() {
        return voxelOutputEnabled;
    }

    public void setVoxelOutputEnabled(boolean enabled) {
        this.voxelOutputEnabled = enabled;
    }

    public boolean skipEmptyVoxel() {
        return this.skipEmptyVoxel;
    }

    public void setSkipEmptyVoxel(boolean skip) {
        this.skipEmptyVoxel = skip;
    }

    /**
     * @return the pathLengthEnabled
     */
    public boolean isPathLengthEnabled() {
        return pathLengthEnabled;
    }

    /**
     * @param pathLengthEnabled the pathLengthEnabled to set
     */
    public void setPathLengthEnabled(boolean pathLengthEnabled) {
        this.pathLengthEnabled = pathLengthEnabled;
    }

    /**
     * @return the pathLengthMaxSize
     */
    public int getPathLengthMaxSize() {
        return pathLengthMaxSize;
    }

    /**
     * @param pathLengthMaxSize the pathLengthMaxSize to set
     */
    public void setPathLengthMaxSize(int pathLengthMaxSize) {
        this.pathLengthMaxSize = pathLengthMaxSize;
    }

    /**
     * @return the maxAttenuation
     */
    public double getMaxAttenuation() {
        return maxAttenuation;
    }

    /**
     * @param maxAttenuation the maxAttenuation to set
     */
    public void setMaxAttenuation(double maxAttenuation) {
        this.maxAttenuation = maxAttenuation;
    }

    /**
     * @return the attenuationError
     */
    public double getAttenuationError() {
        return attenuationError;
    }

    /**
     * @param attenuationError the attenuationError to set
     */
    public void setAttenuationError(double attenuationError) {
        this.attenuationError = attenuationError;
    }

    /**
     * @return the minCorner
     */
    public Point3d getMinCorner() {
        return minCorner;
    }

    /**
     * @param minCorner the minCorner to set
     */
    public void setMinCorner(Point3d minCorner) {
        this.minCorner = minCorner;
    }

    /**
     * @return the maxCorner
     */
    public Point3d getMaxCorner() {
        return maxCorner;
    }

    /**
     * @param maxCorner the maxCorner to set
     */
    public void setMaxCorner(Point3d maxCorner) {
        this.maxCorner = maxCorner;
    }

    /**
     * @return the dimension
     */
    public Point3i getDimension() {
        return dimension;
    }

    /**
     * @param dimension the dimension to set
     */
    public void setDimension(Point3i dimension) {
        this.dimension = dimension;
    }

    /**
     * @return the voxelSize
     */
    public Point3d getVoxelSize() {
        return voxelSize;
    }

    /**
     * @param voxelSize the voxelSize to set
     */
    public void setVoxelSize(Point3d voxelSize) {
        this.voxelSize = voxelSize;
    }

    /**
     * Voxelization parameters to Properties. May not keep it or use it though.
     *
     * @return parameters as a list of properties
     */
    private Properties toProperties() {

        Properties properties = new Properties();
        properties.put("input.type", getLidarType().name());
        properties.put("input.scan.file", getLidarScan().getFile().getAbsolutePath());
        properties.put("input.scan.sop.matrix", Matrix.valueOf(getLidarScan().getMatrix()).toString());
        properties.put("input.scan.name", getLidarScan().getName());
        properties.put("input.dtm.file", getDTMFile().getAbsolutePath());
        properties.put("input.dtm.vop.enabled", String.valueOf(isDTMUseVopMatrix()));
        properties.put("voxelspace.min", getMinCorner().toString());
        properties.put("voxelspace.max", getMaxCorner().toString());
        properties.put("voxelspace.dimension", getDimension().toString());
        properties.put("voxelspace.voxelsize", getVoxelSize().toString());
        properties.put("transformation.pop.matrix", Matrix.valueOf(getPopMatrix()).toString());
        properties.put("transformation.vop.matrix", Matrix.valueOf(getVopMatrix()).toString());
        properties.put("laser.specification.name", getLaserSpecification().getName());
        properties.put("laser.specification.diameter", String.valueOf(getLaserSpecification().getBeamDiameterAtExit()));
        properties.put("laser.specification.divergence", String.valueOf(getLaserSpecification().getBeamDivergence()));
        properties.put("laser.specification.mono", String.valueOf(getLaserSpecification().isMonoEcho()));
        properties.put("echo.weights.matrix", getEchoesWeightMatrix().toString());
        properties.put("output.fraction.digits", String.valueOf(getDecimalFormat().getMaximumFractionDigits()));
        properties.put("leaf.area", String.valueOf(meanLeafArea));
        properties.put("build.version", Util.getVersion());
        properties.put("output.colnames", Arrays.asList(OutputVariable.values()).stream()
                .filter(v -> isOutputVariableEnabled(v))
                .map(v -> v.getShortName())
                .collect(Collectors.joining(" ")));
        return properties;
    }
}
