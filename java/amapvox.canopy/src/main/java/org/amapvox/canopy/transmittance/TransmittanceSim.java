/**
 *
 */
package org.amapvox.canopy.transmittance;

import org.amapvox.canopy.DirectionalTransmittance;
import org.amapvox.canopy.LeafAngleDistribution;
import org.amapvox.canopy.mmr.IncidentRadiation;
import org.amapvox.canopy.mmr.SolarRadiation;
import org.amapvox.canopy.mmr.Turtle;
import org.amapvox.canopy.util.Colouring;
import org.amapvox.canopy.util.Time;
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
import java.util.Calendar;
import javax.vecmath.Point3d;
import org.apache.log4j.Logger;
import org.amapvox.commons.AVoxTask;
import org.amapvox.commons.raytracing.geometry.Transformations;
import org.amapvox.commons.raytracing.voxel.VoxelSpace;
import org.amapvox.voxelisation.output.OutputVariable;

/**
 * @author dauzat
 *
 */
public class TransmittanceSim extends AVoxTask {

    private final static Logger LOGGER = Logger.getLogger(TransmittanceSim.class);

    private DirectionalTransmittance direcTransmittance;
    private double[][] transmissionPeriod;
    private int nbPeriods;
    private Turtle turtle;
    private List<File> outputFiles;

    private List<Point3d> positions;
    private List<IncidentRadiation> solRad;

    private TransmittanceParameters parameters;
    private VoxelSpace voxelSpace;

    private final String logHeader = "[Transmittance light map]";

    public TransmittanceSim(File file, int ncpu) {
        super(file, ncpu);
    }

    @Override
    public String getName() {
        return "Transmittance light map";
    }

    @Override
    protected Class<TransmittanceCfg> getConfigurationClass() {
        return TransmittanceCfg.class;
    }

    @Override
    protected void doInit() throws Exception {

        parameters = ((TransmittanceCfg) getConfiguration()).getParameters();
        turtle = new Turtle(parameters.getDirectionsNumber(), parameters.getDirectionsRotation());
        LOGGER.info(logHeader + " Turtle built with " + turtle.getNbDirections() + " directions");
        outputFiles = new ArrayList<>();
    }

    @Override
    public File[] call() throws Exception {
        process();

        if (isCancelled()) {
            return null;
        }

        if (parameters.isGenerateTextFile()) {
            writeTransmittance();
        }

        if (parameters.isGenerateBitmapFile()) {
            writeBitmaps();
        }

        return outputFiles.toArray(new File[outputFiles.size()]);
    }

    public void process() throws IOException, Exception {

        LOGGER.info(logHeader + " Processing " + parameters.getInputFile().getName());

        direcTransmittance = new DirectionalTransmittance(
                parameters.getInputFile(),
                OutputVariable.PLANT_AREA_DENSITY.getShortName(),
                LeafAngleDistribution.Type.SPHERIC);
        direcTransmittance.setToric(parameters.isToricity());

        voxelSpace = direcTransmittance.getVoxelSpace();

        getSensorPositions();

        solRad = new ArrayList<>();

        List<SimulationPeriod> simulationPeriods = parameters.getSimulationPeriods();

        for (SimulationPeriod period : simulationPeriods) {

            Calendar c1 = period.getPeriod().startDate;
            Calendar c2 = period.getPeriod().endDate;

            Time time1 = new Time(c1.get(Calendar.YEAR), c1.get(Calendar.DAY_OF_YEAR), c1.get(Calendar.HOUR_OF_DAY), c1.get(Calendar.MINUTE));
            Time time2 = new Time(c2.get(Calendar.YEAR), c2.get(Calendar.DAY_OF_YEAR), c2.get(Calendar.HOUR_OF_DAY), c2.get(Calendar.MINUTE));

            solRad.add(SolarRadiation.globalTurtleIntegrate(turtle, (float) Math.toRadians(parameters.getLatitudeInDegrees()), period.getClearnessCoefficient(), time1, time2));
        }

        transmissionPeriod = new double[positions.size()][solRad.size()];
        for (int i = 0; i < positions.size(); i++) {
            for (int m = 0; m < solRad.size(); m++) {
                transmissionPeriod[i][m] = 0;
            }
        }

        nbPeriods = solRad.size();

        // TRANSMITTANCE
        LOGGER.info(logHeader + " Computation of transmittance");

        int positionID = 0;
        double transmitted;

        IncidentRadiation ir = solRad.get(0);

        float rotation = parameters.getDirectionsRotation();
        Transformations tr = new Transformations();
        // Note: "rotation" is negate because the convention of "Transformations" is clockwise
        tr.setRotationAroundZ(Math.toRadians(-rotation));

        int count = 0;

        for (Point3d position : positions) {

            fireProgress("Compute transmittance", positionID, positions.size());

            for (int t = 0; t < turtle.getNbDirections(); t++) {

                if (isCancelled()) {
                    return;
                }

                Vector3d dir = new Vector3d(ir.directions[t]);
                tr.apply(dir);
                dir.normalize();

                transmitted = direcTransmittance.directionalTransmittance(position, dir);

                if (!Double.isNaN(transmitted)) {

                    for (int m = 0; m < solRad.size(); m++) {
                        ir = solRad.get(m);

                        //transmittance for the direction
                        double transmittance = transmitted * ir.directionalGlobals[t];

                        transmissionPeriod[positionID][m] += transmittance;
                    }
                }
            }

            for (int m = 0; m < solRad.size(); m++) {
                ir = solRad.get(m);
                transmissionPeriod[positionID][m] /= ir.global;
            }

            positionID++;
            count++;

            if (count == 1000) {
                LOGGER.info(logHeader + " " + positionID + "/" + positions.size());
                count = 0;
            }
        }

    }

    public void writeBitmaps() throws IOException {

        int zoom = 1;
        Point3d resolution = direcTransmittance.getVoxelSize();
        //float zoom = (1*direcTransmittance.getInfos().getResolution());

        int nbXPixels = (int) (voxelSpace.getDimension().x * resolution.x);
        int nbYPixels = (int) (voxelSpace.getDimension().y * resolution.y);

        if (!parameters.getBitmapFolder().exists()) {
            parameters.getBitmapFolder().mkdirs();
        }

        for (int k = 0; k < nbPeriods; k++) {

            SimulationPeriod period = parameters.getSimulationPeriods().get(k);

            String periodString = period.getPeriod().toString().replaceAll(" ", "_");
            periodString = periodString.replaceAll("/", "-");
            periodString = periodString.replaceAll(":", "");

            File outputFile = new File(parameters.getBitmapFolder() + File.separator + periodString + "_" + period.getClearnessCoefficient() + ".bmp");
            outputFiles.add(outputFile);

            LOGGER.info(logHeader + " Writing file " + outputFile);

            BufferedImage bimg = new BufferedImage(nbXPixels, nbYPixels, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = bimg.createGraphics();

            // background
            g.setColor(new Color(80, 30, 0));
            g.fillRect(0, 0, nbXPixels, nbYPixels);

            for (int p = 0; p < positions.size(); p++) {

                int i = (int) ((positions.get(p).x - voxelSpace.getMin().x) /*/ voxSpace.getVoxelSize().x*/);
                int j = (int) ((positions.get(p).y - voxelSpace.getMin().y)/* / voxSpace.getVoxelSize().y*/);

                float col = (float) (transmissionPeriod[p][k]/* / 0.1*/);
                col = Math.min(col, 1);
                Color c = Colouring.rainbow(col);
                g.setColor(c);
                int jj = nbYPixels - j - 1;
                g.fillRect((int) (i * zoom), (int) (jj * zoom), zoom, zoom);
            }

            try {
                ImageIO.write(bimg, "bmp", outputFile);
            } catch (IOException ex) {
                throw ex;
            }
        }

    }

    private void getSensorPositions() {

        if (parameters.getPositions() != null) {
            positions = parameters.getPositions();
            LOGGER.info(logHeader + " number of positions " + positions.size());
        }
    }

    public void writeTransmittance() throws IOException {

        parameters.getTextFile().getParentFile().mkdirs();

        LOGGER.info(logHeader + " Writing file " + parameters.getTextFile().getAbsolutePath());

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(parameters.getTextFile()))) {

            String metadata = "Voxel space\n"
                    + "  min corner:\t" + voxelSpace.getMin() + "\n"
                    + "  max corner:\t" + voxelSpace.getMax() + "\n"
                    + "  dimension:\t" + voxelSpace.getDimension() + "\n\n"
                    + "latitude (degrees)\t" + parameters.getLatitudeInDegrees() + "\n\n";

            bw.write(metadata);

            String header = "X\tY\tZ\t";
            String periodsInfos = "";
            String periodsInfosHeader = "period ID\tstart\tend\tclearness\tglobalMJ\tdirectMJ\tdiffuseMJ";

            int count = 1;
            for (SimulationPeriod period : parameters.getSimulationPeriods()) {

                String periodName = "Period " + count;
                header += periodName + "\t";
                periodsInfos += periodName + "\t" + Period.getDate(period.getPeriod().startDate) + "\t" + Period.getDate(period.getPeriod().endDate) + "\t" + period.getClearnessCoefficient()
                        + "\t" + solRad.get(count - 1).global + "\t" + solRad.get(count - 1).direct + "\t" + solRad.get(count - 1).diffuse + "\n";

                count++;
            }

            bw.write(periodsInfosHeader + "\n"
                    + periodsInfos + "\n");

            bw.write(header + "\n");

            float mean[] = new float[transmissionPeriod[0].length];

            int i = 0;
            for (Point3d position : positions) {

                bw.write(position.x + "\t" + position.y + "\t" + position.z);

                for (int m = 0; m < transmissionPeriod[i].length; m++) {
                    bw.write("\t" + transmissionPeriod[i][m]);
                    mean[m] += transmissionPeriod[i][m];
                }

                bw.write("\n");
                i++;
            }

            float yearlyMean = 0;
            bw.write("\nPERIOD\tMEAN");
            for (int m = 0; m < transmissionPeriod[0].length; m++) {
                mean[m] /= (float) positions.size();
                bw.write("\t" + mean[m]);
                yearlyMean += mean[m];
            }
            yearlyMean /= transmissionPeriod[0].length;
            bw.write("\nTOTAL\tMEAN\t" + yearlyMean);
            bw.write("\n");

            outputFiles.add(parameters.getTextFile());
        } catch (IOException ex) {
            throw ex;
        }
    }
}
