package org.amapvox.canopy.lightmap;

import org.amapvox.canopy.DirectionalTransmittance;
import org.amapvox.canopy.mmr.IncidentRadiation;
import org.amapvox.canopy.mmr.SolarRadiation;
import org.amapvox.canopy.mmr.Turtle;
import org.amapvox.canopy.util.Colouring;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.vecmath.Vector3d;
import javax.vecmath.Point3d;
import org.apache.log4j.Logger;
import org.amapvox.commons.AVoxTask;
import org.amapvox.commons.raytracing.geometry.Transformations;
import org.amapvox.commons.raytracing.voxel.VoxelSpace;

/**
 * @author dauzat
 *
 */
public class LightMapTask extends AVoxTask {

    private final static Logger LOGGER = Logger.getLogger(LightMapTask.class);

    private LightMapCfg cfg;

    private DirectionalTransmittance direcTransmittance;
    private double[][] transmissionPeriod;
    private Turtle turtle;

    private List<Point3d> positions;
    private List<IncidentRadiation> solarRadiationPeriods;

    private VoxelSpace voxelSpace;

    private final String logHeader = "[Canopy light map]";

    public LightMapTask(File file, int ncpu) {
        super(file, ncpu);
    }

    @Override
    public String getName() {
        return "Canopy light availability map";
    }

    @Override
    protected Class<LightMapCfg> getConfigurationClass() {
        return LightMapCfg.class;
    }

    @Override
    protected void doInit() throws Exception {

        cfg = ((LightMapCfg) getConfiguration());
        turtle = new Turtle(cfg.getDirectionCount());
        LOGGER.info(logHeader + " Turtle built with " + turtle.getDirectionCount() + " directions");
    }

    @Override
    public File[] call() throws Exception {

        LOGGER.info(logHeader + " Processing " + cfg.getVoxelFile().getName());

        direcTransmittance = new DirectionalTransmittance(
                cfg.getVoxelFile(),
                cfg.getPADVariable(),
                cfg.getLeafAngleDistribution(),
                cfg.getLeafAngleDistributionParameters());
        direcTransmittance.setToric(cfg.isToricity());

        voxelSpace = direcTransmittance.getVoxelSpace();

        positions = cfg.getSensorPositions();

        solarRadiationPeriods = new ArrayList<>();

        List<SimulationPeriod> simulationPeriods = cfg.getSimulationPeriods();

        for (SimulationPeriod period : simulationPeriods) {

            solarRadiationPeriods.add(SolarRadiation.globalTurtleIntegrate(
                    turtle, 
                    (float) Math.toRadians(cfg.getLatitudeInDegrees()), 
                    period.getClearnessCoefficient(), 
                    period.startDateToTime(), 
                    period.endDateToTime()));
        }

        transmissionPeriod = new double[positions.size()][solarRadiationPeriods.size()];
        for (int i = 0; i < positions.size(); i++) {
            for (int m = 0; m < solarRadiationPeriods.size(); m++) {
                transmissionPeriod[i][m] = 0;
            }
        }

        // TRANSMITTANCE
        LOGGER.info(logHeader + " Computation of transmittance");

        double transmitted;

        IncidentRadiation incidentRadiation = solarRadiationPeriods.get(0);

        float rotationAngle = cfg.getDirectionRotation();
        Transformations rotation = new Transformations();
        // Apply minus rotation because the convention of Transformations is clockwise
        rotation.setRotationAroundZ(Math.toRadians(-rotationAngle));

        int count = 0;

        int positionIndex = 0;
        int positionCount = positions.size();
        for (Point3d position : positions) {

            fireProgress("Compute transmittance", positionIndex, positionCount);

            for (int directionIndex = 0; directionIndex < turtle.getDirectionCount(); directionIndex++) {

                if (isCancelled()) {
                    return null;
                }

                Vector3d direction = new Vector3d(incidentRadiation.directions[directionIndex]);
                rotation.apply(direction);
                direction.normalize();

                transmitted = direcTransmittance.computeTransmittance(position, direction);

                if (!Double.isNaN(transmitted)) {

                    for (int m = 0; m < solarRadiationPeriods.size(); m++) {
                        incidentRadiation = solarRadiationPeriods.get(m);

                        //transmittance for the direction
                        double transmittance = transmitted * incidentRadiation.directionalGlobals[directionIndex];

                        transmissionPeriod[positionIndex][m] += transmittance;
                    }
                }
            }

            for (int solarRadiationIndex = 0; solarRadiationIndex < solarRadiationPeriods.size(); solarRadiationIndex++) {
                incidentRadiation = solarRadiationPeriods.get(solarRadiationIndex);
                transmissionPeriod[positionIndex][solarRadiationIndex] /= incidentRadiation.global;
            }

            positionIndex++;
            count++;

            if (count == 1000) {
                LOGGER.info(logHeader + " Position " + positionIndex + "/" + positionCount);
                count = 0;
            }
        }

        // create output directory
        if (!cfg.getOutputDirectory().exists()) {
            cfg.getOutputDirectory().mkdirs();
        }

        List<File> outputFiles = new ArrayList<>();

        // write bitmaps
        int periodCount = solarRadiationPeriods.size();
        for (int periodIndex = 0; periodIndex < periodCount; periodIndex++) {
            SimulationPeriod period = cfg.getSimulationPeriods().get(periodIndex);
            String periodString = period.toString().replaceAll(" ", "_");
            periodString = periodString.replaceAll("/", "-");
            periodString = periodString.replaceAll(":", "");
            String outputFilename = cfg.getOutputPrefix()
                    + "_" + periodString
                    + "_" + period.getClearnessCoefficient() + ".bmp";
            File outputFile = new File(cfg.getOutputDirectory(), outputFilename);
            writeBitmaps(periodIndex, outputFile);
            outputFiles.add(outputFile);
        }

        // write text file
        String outputFilename = cfg.getOutputPrefix() + "_lightmap.txt";
        File outputFile = new File(cfg.getOutputDirectory(), outputFilename);
        writeTransmittance(outputFile);
        outputFiles.add(outputFile);

        return outputFiles.toArray(File[]::new);
    }

    public File writeBitmaps(int periodIndex, File outputFile) throws IOException {

        int zoom = 1;
        Point3d resolution = direcTransmittance.getVoxelSize();
        //float zoom = (1*direcTransmittance.getInfos().getResolution());

        int nbXPixels = (int) (voxelSpace.getDimension().x * resolution.x);
        int nbYPixels = (int) (voxelSpace.getDimension().y * resolution.y);

        LOGGER.info(logHeader + " Writing file " + outputFile);

        BufferedImage bimg = new BufferedImage(nbXPixels, nbYPixels, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bimg.createGraphics();

        // background
        g.setColor(new Color(80, 30, 0));
        g.fillRect(0, 0, nbXPixels, nbYPixels);

        int positionIndex = 0;
        for (Point3d position : positions) {

            int i = (int) ((position.x - voxelSpace.getMin().x));
            int j = (int) ((position.y - voxelSpace.getMin().y));

            float col = (float) (transmissionPeriod[positionIndex][periodIndex]);
            col = Math.min(col, 1);
            Color c = Colouring.rainbow(col);
            g.setColor(c);
            int jj = nbYPixels - j - 1;
            g.fillRect((int) (i * zoom), (int) (jj * zoom), zoom, zoom);
            positionIndex++;
        }

        ImageIO.write(bimg, "bmp", outputFile);

        return outputFile;
    }

    public void writeTransmittance(File outputFile) throws IOException {

        LOGGER.info(logHeader + " Writing file " + outputFile);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {

            StringBuilder metadata = new StringBuilder();
            metadata.append("Voxel space").append("\n");
            metadata.append("  min corner:").append("\t").append(voxelSpace.getMin()).append("\n");
            metadata.append("  max corner:").append("\t").append(voxelSpace.getMax()).append("\n");
            metadata.append("  dimension:").append("\t").append(voxelSpace.getDimension()).append("\n");
            metadata.append("\n");
            metadata.append("  latitude (degrees)").append("\t").append(cfg.getLatitudeInDegrees()).append("\n");
            bw.write(metadata + "\n");

            StringBuilder header = new StringBuilder();
            header.append("X\tY\tZ\t");
            StringBuilder periodsInfos = new StringBuilder();
            String periodsInfosHeader = "period ID\tstart\tend\tclearness\tglobalMJ\tdirectMJ\tdiffuseMJ";
            bw.write(periodsInfosHeader + "\n");

            for (int periodIndex = 0; periodIndex < cfg.getSimulationPeriods().size(); periodIndex++) {
                String periodName = "Period " + (periodIndex + 1);
                header.append(periodName).append("\t");
                periodsInfos.append(periodName);

                SimulationPeriod period = cfg.getSimulationPeriods().get(periodIndex);
                periodsInfos.append("\t").append(period.startDateToString());
                periodsInfos.append("\t").append(period.endDateToString());
                periodsInfos.append("\t").append(period.getClearnessCoefficient());

                IncidentRadiation solarRadiation = solarRadiationPeriods.get(periodIndex);
                periodsInfos.append("\t").append(solarRadiation.global);
                periodsInfos.append("\t").append(solarRadiation.direct);
                periodsInfos.append("\t").append(solarRadiation.diffuse);
                periodsInfos.append("\t").append("\n");
            }
            bw.write(periodsInfos + "\n");
            bw.write(header + "\n");

            float meanTransmittance[] = new float[transmissionPeriod[0].length];

            int positionIndex = 0;
            for (Point3d position : positions) {
                bw.write(position.x + "\t" + position.y + "\t" + position.z);
                int periodIndex = 0;
                for (double transmittance : transmissionPeriod[positionIndex]) {
                    bw.write("\t" + transmittance);
                    meanTransmittance[periodIndex] += transmittance;
                }
                bw.write("\n");
                positionIndex++;
            }

            float yearlyMean = 0;
            bw.write("\nPERIOD\tMEAN");
            int periodCount = solarRadiationPeriods.size();
            int positionCount = positions.size();
            for (int m = 0; m < periodCount; m++) {
                meanTransmittance[m] /= positionCount;
                bw.write("\t" + meanTransmittance[m]);
                yearlyMean += meanTransmittance[m];
            }
            yearlyMean /= periodCount;
            bw.write("\nTOTAL\tMEAN\t" + yearlyMean);
            bw.write("\n");
        }
    }
}
