package org.amapvox.canopy.analyzer;

import org.amapvox.canopy.DirectionalTransmittance;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.amapvox.canopy.analyzer.CanopyAnalyzerSensor.Direction;
import org.apache.log4j.Logger;
import org.amapvox.commons.AVoxTask;
import org.amapvox.commons.math.util.Statistic;

/**
 *
 * @author Julien
 */
public class CanopyAnalyzerTask extends AVoxTask {

    private final static Logger LOGGER = Logger.getLogger(CanopyAnalyzerTask.class);

    private final String logHeader = "[Canopy analyzer]";

    private CanopyAnalyzerCfg cfg;

    public CanopyAnalyzerTask(File file, int ncpu) {
        super(file, ncpu);
    }

    @Override
    protected Class<CanopyAnalyzerCfg> getConfigurationClass() {
        return CanopyAnalyzerCfg.class;
    }

    @Override
    public String getName() {
        return "Canopy analyzer (LAI2000/2200)";
    }

    @Override
    protected void doInit() throws Exception {

        cfg = ((CanopyAnalyzerCfg) getConfiguration());
    }

    @Override
    public File[] call() throws Exception {

        LOGGER.info(logHeader + " Processing " + cfg.getVoxelFile().getName());

        DirectionalTransmittance directionalTransmittance = new DirectionalTransmittance(
                cfg.getVoxelFile(),
                cfg.getPADVariable(),
                cfg.getLeafAngleDistribution(),
                cfg.getLeafAngleDistributionParameters());
        
        Statistic transmittedStatistic = new Statistic();

        // create canopy analyzer sensor
        CanopyAnalyzerSensor canopyAnalyzer = (cfg.getCanopyAnalyzer() == CanopyAnalyzerCfg.CanopyAnalyzer.LAI2000)
                ? new LAI2000(cfg.getMasks())
                : new LAI2200(cfg.getMasks());
        
        LOGGER.info(logHeader + " Computing directions...");
        Direction[] directions = canopyAnalyzer.computeDirections(cfg.getDirectionCount());

        // TRANSMITTANCE
        LOGGER.info(logHeader + " Computing transmittance...");

        int positionCount = cfg.getSensorPositions().size();
        canopyAnalyzer.initPositions(positionCount);
        int positionIndex = 0;
        double transmitted;

        for (Point3d position : cfg.getSensorPositions()) {

            for (Direction direction : directions) {

                if (isCancelled()) {
                    return null;
                }

                Vector3d dir = new Vector3d(direction);
                dir.normalize();

                transmitted = directionalTransmittance.computeTransmittance(position, dir);
                transmittedStatistic.addValue(transmitted);

                if (!Double.isNaN(transmitted)) {
                    canopyAnalyzer.addTransmittance(direction.ringIndex, positionIndex, (float) transmitted);
                }
            }
            positionIndex++;

            if (positionIndex % 1000 == 0) {
                LOGGER.info(logHeader + " Position " + positionIndex + "/" + positionCount);
            }
        }

        if (transmittedStatistic.getNbNaNValues() > 0) {
            LOGGER.warn(logHeader + " Some rays crossed NA voxels, count: " + transmittedStatistic.getNbNaNValues());
        }

        // compute sensor aggregated values
        canopyAnalyzer.computeValues();

        // create output dir
        if (!cfg.getOutputDirectory().exists()) {
            cfg.getOutputDirectory().mkdirs();
        }

        // write in LAI sensor like format
        String sensorFilename = cfg.getOutputPrefix()
                + "."
                + cfg.getCanopyAnalyzer().toString().toLowerCase();
        File sensorFile = new File(cfg.getOutputDirectory(), sensorFilename);
        LOGGER.info(logHeader + " Writing file " + sensorFile);
        canopyAnalyzer.writeOutput(sensorFile);

        // write in raw text format
        String rawFilename = cfg.getOutputPrefix()
                + "_"
                + cfg.getCanopyAnalyzer().toString().toLowerCase()
                + ".txt";
        File rawFile = new File(cfg.getOutputDirectory(), rawFilename);
        LOGGER.info(logHeader + " Writing file " + rawFile);
        writeTransmittance(rawFile, canopyAnalyzer);

        return new File[]{rawFile, sensorFile};
    }

    private void writeTransmittance(File file, CanopyAnalyzerSensor sensor) throws IOException {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            // header
            bw.write("posX\tposY\tposZ\tLAI\tGAP[1]\tGAP[2]\tGAP[3]\tGAP[4]\tGAP[5]\n");
            // loop over positions
            int i = 0;
            for (Point3d position : cfg.getSensorPositions()) {
                StringBuilder line = new StringBuilder();
                // append position
                line.append(position.x).append("\t");
                line.append(position.y).append("\t");
                line.append(position.z).append("\t");
                line.append(sensor.getByPosition_LAI()[i]);
                // append transmittance for every ring
                for (int r = 0; r < sensor.getRingNumber(); r++) {
                    line.append("\t").append(sensor.transmittances[r][i]);
                }
                // line break
                line.append("\n");
                // write line
                bw.write(line.toString());
                i++;
            }
        }
    }
}
