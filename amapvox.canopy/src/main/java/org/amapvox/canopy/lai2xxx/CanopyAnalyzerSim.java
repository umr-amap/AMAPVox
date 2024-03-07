/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.canopy.lai2xxx;

import org.amapvox.commons.math.util.SphericalCoordinates;
import org.amapvox.canopy.DirectionalTransmittance;
import org.amapvox.canopy.LeafAngleDistribution;
import org.amapvox.canopy.transmittance.TransmittanceCfg;
import org.amapvox.canopy.transmittance.TransmittanceParameters;
import static org.amapvox.canopy.lai2xxx.LAI2xxx.ViewCap.CAP_360;
import org.amapvox.voxelfile.VoxelFileReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.amapvox.commons.AVoxTask;
import org.amapvox.voxelfile.VoxelFileHeader;
import org.amapvox.voxelfile.VoxelFileVoxel;
import org.amapvox.voxelisation.output.OutputVariable;
import org.amapvox.commons.math.util.Statistic;
import org.amapvox.commons.raytracing.geometry.LineElement;
import org.amapvox.commons.raytracing.geometry.LineSegment;
import org.amapvox.commons.raytracing.voxel.VoxelManager;
import org.amapvox.commons.raytracing.voxel.VoxelManager.Topology;

/**
 *
 * @author Julien
 */
public class CanopyAnalyzerSim extends AVoxTask {

    private final static Logger LOGGER = Logger.getLogger(CanopyAnalyzerSim.class);

    private LAI2xxx lai2xxx;
    private TransmittanceParameters parameters;

    private DirectionalTransmittance direcTransmittance;
    private List<Point3d> positions;

    private Vector3f[] directions;

    private Statistic transmittedStatistic;

    //temporaire, pour test
    private final static boolean TRANSMITTANCE_NORMALISEE = true;

    private class CAVoxel {

        double plantAreaDensity;
        float groundDistance;
        int k;
    }

    public CanopyAnalyzerSim(File file, int ncpu) {
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

        parameters = ((TransmittanceCfg) getConfiguration()).getParameters();
        transmittedStatistic = new Statistic();

        if (parameters.getMode() == TransmittanceParameters.Mode.LAI2000) {
            lai2xxx = new LAI2000(parameters.getDirectionsNumber(), CAP_360, parameters.getMasks());
        } else {
            lai2xxx = new LAI2200(parameters.getDirectionsNumber(), CAP_360, parameters.getMasks());
        }

        LOGGER.info("Computing directions...");

        lai2xxx.computeDirections();

        directions = lai2xxx.getDirections();
    }

    @Override
    public File[] call() throws Exception {

        if (!TRANSMITTANCE_NORMALISEE) {
            LOGGER.info("===== " + parameters.getInputFile().getAbsolutePath() + " =====");

            if (direcTransmittance == null) {
                direcTransmittance = new DirectionalTransmittance(parameters.getInputFile(), LeafAngleDistribution.Type.SPHERIC);
            }

            positions = parameters.getPositions();

            // TRANSMITTANCE
            LOGGER.info("Computation of transmittance");

            lai2xxx.initPositions(positions.size());

            int positionID = 0;
            double transmitted;

            for (Point3d position : positions) {

                for (int t = 0; t < directions.length; t++) {

                    if (isCancelled()) {
                        return null;
                    }

                    Vector3d dir = new Vector3d(directions[t]);
                    dir.normalize();

                    transmitted = direcTransmittance.directionalTransmittance(position, dir);
                    transmittedStatistic.addValue(transmitted);

                    if (!Double.isNaN(transmitted)) {

                        int ring = lai2xxx.getRingIDFromDirectionID(t);
                        lai2xxx.addTransmittance(ring, positionID, (float) transmitted);
                    }

                }

                positionID++;

                if (positionID % 1000 == 0) {
                    LOGGER.info(positionID + "/" + positions.size());
                }
            }

            if (transmittedStatistic.getNbNaNValues() > 0) {
                LOGGER.warn("Some rays crossed NA voxels, count: " + transmittedStatistic.getNbNaNValues());
            }

            if (parameters.isGenerateTextFile()) {
                writeTransmittance();
                LOGGER.info("File " + parameters.getTextFile().getAbsolutePath() + " was written.");
            }

            LOGGER.info("Simulation is finished.");
        } else {
            //*******début du test
            //lecture du fichier voxel
            VoxelFileReader reader = new VoxelFileReader(parameters.getInputFile());
            VoxelFileHeader header = reader.getHeader();

            Iterator<VoxelFileVoxel> iterator = reader.iterator();
            CAVoxel voxels[][][] = new CAVoxel[header.getDimension().x][header.getDimension().y][header.getDimension().z];
            int padColumn = reader.findColumn(OutputVariable.PLANT_AREA_DENSITY);
            if (padColumn < 0) {
                throw new IOException("[canopy analyzer] Output variable \"plant area density\" is missing");
            }
            int groundDistanceColumn = reader.findColumn(OutputVariable.GROUND_DISTANCE);
            if (groundDistanceColumn < 0) {
                throw new IOException("[canopy analyzer] Output variable \"ground distance\" is missing");
            }

            //conversion de la liste de voxels en tableau 3d
            while (iterator.hasNext()) {
                VoxelFileVoxel voxel = iterator.next();
                voxels[voxel.i][voxel.j][voxel.k] = new CAVoxel();
                voxels[voxel.i][voxel.j][voxel.k].k = voxel.k;
                voxels[voxel.i][voxel.j][voxel.k].plantAreaDensity = Float.valueOf(voxel.variables[padColumn]);
                voxels[voxel.i][voxel.j][voxel.k].groundDistance = Float.valueOf(voxel.variables[groundDistanceColumn]);
            }

            //création d'un nouveau VoxelManager avec les paramètres du fichier voxel
            VoxelManager vm = new VoxelManager(
                    header.getMinCorner(), header.getMaxCorner(),
                    header.getDimension(), Topology.NON_TORIC_FINITE_BOX_TOPOLOGY);

            List<Double[]> l = new ArrayList<>();


            /*try (BufferedReader reader = new BufferedReader(new FileReader(new File("/media/forestview01/partageLidar/FTH2014_LAI2200/data/LAI_P9_M_fusion_new.txt")))) {
                reader.readLine();

                String line;

                while((line = reader.readLine()) != null){
                    String[] split = line.split("\t");
                    l.add(new Double[]{Double.valueOf(split[4]), Double.valueOf(split[5]), Double.valueOf(split[6]), Double.valueOf(split[7]), Double.valueOf(split[8])});
                }
            }*/

 /*Statistic[][] tranStattistics = new Statistic[l.size()][5];
            for(int i=0;i<tranStattistics.length;i++){
                for(int j=0;j<5;j++){
                    tranStattistics[i][j] = new Statistic();
                }
            }*/
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(parameters.getTextFile().getAbsolutePath() + "_test.txt")));
            writer.write("position.ID" + " " + "position.x" + " " + "position.y" + " " + "position.z" + " " + "ring" + " " + "pathLength" + " " + "transmittance" + " " + "isOut" + " " + "azimut" + " " + "elevation" + " " + "cross_NA" + "\n");

            //*******fin du test
            LOGGER.info("===== " + parameters.getInputFile().getAbsolutePath() + " =====");

            direcTransmittance = new DirectionalTransmittance(parameters.getInputFile(), LeafAngleDistribution.Type.SPHERIC);

            positions = parameters.getPositions();

            // TRANSMITTANCE
            LOGGER.info("Computation of transmittance");

            lai2xxx.initPositions(positions.size());

            int positionID = 0;
            double transmitted;

            Statistic NaNCounter = new Statistic();

            for (Point3d position : positions) {

                for (int t = 0; t < directions.length; t++) {

                    if (isCancelled()) {
                        return null;
                    }

                    Vector3d dir = new Vector3d(directions[t]);
                    dir.normalize();

                    transmitted = direcTransmittance.directionalTransmittance(position, dir);
//                    System.out.println("Position " + position + " direction " + directions[t] +"  transmittance " + transmitted);

                    int ring = lai2xxx.getRingIDFromDirectionID(t);

                    //test
                    LineElement lineElement = new LineSegment(position, new Vector3d(dir), 99999999);
                    //distance cumulée
                    double distance = 0;

                    //dernière distance valide (sortie de canopée)
                    double lastValidDistance = 0;

                    //get the first voxel cross by the line
                    VoxelManager.VoxelCrossingContext context = vm.getFirstVoxel(lineElement);

                    double distanceToHit = lineElement.getLength();
                    boolean gotOneNaN = false;

                    boolean wasOutside = false;

                    SphericalCoordinates sc = SphericalCoordinates.fromCartesian(dir);

                    while ((context != null) && (context.indices != null)) {

                        if (isCancelled()) {
                            return null;
                        }

                        //current voxel
                        Point3i indices = context.indices;
                        CAVoxel voxel = voxels[indices.x][indices.y][indices.z];

                        if (null != voxel && voxel.groundDistance < 0.0f) {
                            break;
                        }

                        if (null == voxel || Double.isNaN(voxel.plantAreaDensity)) {
                            gotOneNaN = true;
                            break;
                        }

                        //distance from the last origin to the point in which the ray enter the voxel
                        double d1 = context.length;

                        context = vm.CrossVoxel(lineElement, indices);

                        if (context != null && context.indices == null) {
                            if (voxel.k == header.getDimension().z - 1) {
                                wasOutside = false;
                            } else {
                                wasOutside = true;
                            }
                        }

                        //distance from the last origin to the point in which the ray exit the voxel
                        double d2 = context.length;

                        if (d2 < distanceToHit) {

                            distance += (d2 - d1);

                        } else if (d1 >= distanceToHit) {

                        } else {
                            distance += (d2 - d1);
                        }

                        if (voxel.plantAreaDensity > 0) {
                            lastValidDistance = distance;
                        }
                    }

                    double pathLength = lastValidDistance;

                    if (Double.isNaN(transmitted)) {
                        gotOneNaN = true;
                    }

                    //test
                    if (!gotOneNaN && pathLength != 0) {

                        NaNCounter.addValue(transmitted);

                        //lai2xxx.addNormalizedTransmittance(ring, positionID, (float) (Math.pow(transmitted, 1/pathLength)), (float) pathLength);
                        lai2xxx.addTransmittance(ring, positionID, (float) transmitted);

                        //tranStattistics[positionID][ring].addValue((Math.pow(l.get(positionID)[ring], 1/pathLength)));
                    } else {
                        NaNCounter.addValue(Double.NaN);
                    }

                    writer.write(positionID + " " + position.x + " " + position.y + " " + position.z + " " + (ring + 1) + " " + pathLength + " " + transmitted + " " + wasOutside + " " + (float) Math.toDegrees(sc.getAzimut()) + " " + (float) Math.toDegrees(sc.getZenith()) + " " + gotOneNaN + "\n");

                    //lai2xxx.addTransmittance(ring, positionID, (float) transmitted);
                }

                positionID++;

                if (positionID % 1000 == 0) {
                    LOGGER.info(positionID + "/" + positions.size());
                }
            }

            //test
            System.out.println("Nb values : " + NaNCounter.getNbValues());
            System.out.println("Nb NaN values : " + NaNCounter.getNbNaNValues());

            writer.close();

            //        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/media/forestview01/partageLidar/FTH2014_LAI2200/data/tests/normalisation_mesure/methode2/transmittances.txt")))) {
            //            
            //            for(int i=0;i<tranStattistics.length;i++){
            //                
            //                String line = i+"";
            //                
            //                for(int j=0;j<5;j++){
            //                    line += "\t"+tranStattistics[i][j].getMean();
            //                }
            //                
            //                writer.write(line+"\n");
            //                
            //            }
            //        }
            if (parameters.isGenerateTextFile()) {

                if (isCancelled()) {
                    return null;
                }

                writeTransmittance();
                LOGGER.info("File " + parameters.getTextFile().getAbsolutePath() + " was written.");
            }

            LOGGER.info("Simulation is finished.");
        }

        return parameters.isGenerateTextFile() ? new File[]{parameters.getTextFile()} : null;
    }

    public void writeTransmittance() throws IOException {

        if (parameters.isGenerateLAI2xxxTypeFormat()) {
            lai2xxx.writeOutput(parameters.getTextFile());
        } else {

            lai2xxx.computeValues();

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(parameters.getTextFile()))) {

                //bw.write("posX\tposY\tposZ\tLAI\tGAP[1]\tGAP[2]\tGAP[3]\tGAP[4]\tGAP[5]\n");
                //test
                bw.write("posX\tposY\tposZ\tLAI\tGAP[1]\tGAP[2]\tGAP[3]\tGAP[4]\tGAP[5]"/*
                        + "\tGAP[1]_normalized\tGAP[2]_normalized\tGAP[3]_normalized\tGAP[4]_normalized\tGAP[5]_normalized"
                        + "\tGAP[1]_mean_pathLength\tGAP[2]_mean_pathLength\tGAP[3]_mean_pathLength\tGAP[4]_mean_pathLength\tGAP[5]_mean_pathLength*/ + "\n");

                for (int i = 0; i < positions.size(); i++) {

                    Point3d position = positions.get(i);

                    String line = position.x + "\t" + position.y + "\t" + position.z + "\t" + lai2xxx.getByPosition_LAI()[i];

                    for (int r = 0; r < lai2xxx.getRingNumber(); r++) {
                        line += "\t" + lai2xxx.transmittances[r][i];
                    }
//                    
//                    //test
//                    for(int r=0;r<lai2xxx.getRingNumber();r++){
//                        line += "\t"+lai2xxx.getNormalizedTransmittances()[r][i];
//                    }
//                    
//                    //test
//                    for(int r=0;r<lai2xxx.getRingNumber();r++){
//                        line += "\t"+lai2xxx.getPathLengths()[r][i];
//                    }

                    bw.write(line + "\n");

                }
            } catch (IOException ex) {
                throw ex;
            }

        }
    }

    public LAI2xxx getLai2xxx() {
        return lai2xxx;
    }

    public void setQuiet(boolean quiet) {

        if (quiet) {
            LOGGER.setLevel(Level.ERROR);
        } else {
            LOGGER.setLevel(Level.INFO);
        }
    }

}
