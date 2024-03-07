/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation.postproc;

import org.amapvox.commons.util.ColorGradient;
import org.amapvox.commons.math.util.Statistic;
import org.amapvox.voxelfile.VoxelFileVoxel;
import org.amapvox.voxelfile.VoxelFileHeader;
import org.amapvox.commons.AVoxTask;
import org.amapvox.voxelfile.VoxelFileReader;
import org.amapvox.voxelisation.output.OutputVariable;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import org.apache.log4j.Logger;

/**
 *
 * @author pverley
 */
public class ObjExporter extends AVoxTask {

    private final static Logger LOGGER = Logger.getLogger(ObjExporter.class);

    private List<Point3d> cubeVertices;
    private List<Point3i> faces;
    private Map<String, Point3f> materials;
    private List<String> materialsKeys;

    public ObjExporter(File file, int ncpu) {
        super(file, ncpu);
    }

    @Override
    protected Class<ObjExporterCfg> getConfigurationClass() {
        return ObjExporterCfg.class;
    }

    @Override
    protected void doInit() throws Exception {
        // nothing to do
    }

    @Override
    public String getName() {
        return "Obj Export";
    }

    @Override
    public File[] call() throws Exception {

        ObjExporterCfg objCfg = (ObjExporterCfg) getConfiguration();

        cubeVertices = new ArrayList<>();
        faces = new ArrayList<>();

        File voxFile = objCfg.getInputFile();
        LOGGER.info("Exporting VOX file " + voxFile.getName() + " to OBJ file " + objCfg.getOutputFile().getName());
        VoxelFileReader reader = new VoxelFileReader(voxFile);
        VoxelFileHeader header = reader.getHeader();
        Point3i split = header.getDimension();
        int nprogress = split.x * split.y * split.z;
        int ipgrogress = 1;

        String[] columns = reader.getHeader().getColumnNames();
        int noVegetationColumn = -1;
        for (int ic = 0; ic < columns.length; ic++) {
            try {
                OutputVariable variable = OutputVariable.find(columns[ic]);
                if (variable.equals(OutputVariable.PLANT_AREA_DENSITY)
                        || variable.equals(OutputVariable.ATTENUATION_FPL_BIASED_MLE)
                        || variable.equals(OutputVariable.ATTENUATION_FPL_UNBIASED_MLE)
                        || variable.equals(OutputVariable.ATTENUATION_PPL_MLE)) {
                    noVegetationColumn = ic - 3;
                    break;
                }
            } catch (NullPointerException ex) {
            }
        }
        if (noVegetationColumn < 0) {
            throw new IOException("Cannot export to OBJ. Output variable attenuation or PAD is missing.");
        }

        int variableColumn = -4;
        for (int ic = 0; ic < columns.length; ic++) {
            if (columns[ic].equalsIgnoreCase(objCfg.getOutputVariable())) {
                variableColumn = ic - 3;
                break;
            }
        }
        if (objCfg.isMaterialEnabled() && variableColumn < -3) {
            throw new IOException("Cannot export to OBJ. Output variable " + objCfg.getOutputVariable() + " is missing.");
        }

        ColorGradient gradient = new ColorGradient(0, 0);

        if (objCfg.isMaterialEnabled()) {
            //find min and max
            Iterator<VoxelFileVoxel> iterator = reader.iterator();
            Statistic attributeStat = new Statistic();

            while (iterator.hasNext()) {
                //  progress ipgrogress++/nprogress
                fireProgress("Reading voxel file...", ipgrogress++, nprogress);

                VoxelFileVoxel voxel = iterator.next();

                switch (variableColumn) {
                    case -3:
                        attributeStat.addValue(voxel.i);
                        break;
                    case -2:
                        attributeStat.addValue(voxel.j);
                        break;
                    case -1:
                        attributeStat.addValue(voxel.k);
                        break;
                    default:
                        attributeStat.addValue(Float.valueOf(voxel.variables[variableColumn]));
                        break;
                }

            }

            gradient = new ColorGradient((float) attributeStat.getMinValue(), (float) attributeStat.getMaxValue());
            gradient.setGradientColor(objCfg.getColors());

            materials = new HashMap<>();
            materialsKeys = new ArrayList<>();
        }

        Iterator<VoxelFileVoxel> iterator = reader.iterator();

        ipgrogress = 1;
        float maxPAD = objCfg.getMaxPAD();
        float alpha = objCfg.getAlpha();
        while (iterator.hasNext()) {
            // progress ipgrogress++ / nprogress;
            fireProgress("Reading voxel file...", ipgrogress++, nprogress);

            VoxelFileVoxel voxel = iterator.next();

            float attntOrPad = Float.valueOf(voxel.variables[noVegetationColumn]);
            if (!Float.isNaN(attntOrPad) && attntOrPad != 0) {

                Point3d position = getPosition(new Point3i(voxel.i, voxel.j, voxel.k), header);

                if (objCfg.isMaterialEnabled()) {

                    Color c;

                    switch (variableColumn) {
                        case -3:
                            c = gradient.getColor(voxel.i);
                            break;
                        case -2:
                            c = gradient.getColor(voxel.j);
                            break;
                        case -1:
                            c = gradient.getColor(voxel.k);
                            break;
                        default:
                            c = gradient.getColor(Float.valueOf(voxel.variables[variableColumn]));
                            break;
                    }

                    float red = c.getRed() / 255.0f;
                    float green = c.getGreen() / 255.0f;
                    float blue = c.getBlue() / 255.0f;

                    String key = String.valueOf(red) + "_" + String.valueOf(green) + "_" + String.valueOf(blue);
                    materials.put(key, new Point3f(red, green, blue));
                    materialsKeys.add(key);
                }

                Point3d voxelSize;
                if (objCfg.isVoxelSizeFunctionEnabled()) {
                    voxelSize = header.getVoxelSize();
                    voxelSize.scale((float) Math.pow(attntOrPad / maxPAD, alpha));
                } else {
                    voxelSize = header.getVoxelSize();
                }

                createCube(voxelSize, position);
            }

        }

        File materialFile = objCfg.getMaterialFile();

        nprogress = cubeVertices.size() + faces.size();
        double iprogress = 1;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(objCfg.getOutputFile()))) {

            if (objCfg.isMaterialEnabled()) {
                writer.write("mtllib " + materialFile.getName() + "\n");
            }

            for (Point3d point : cubeVertices) {
                // progress iprogress++ / nprogress
                fireProgress("Writing OBJ file...", ipgrogress++, nprogress);
                // write vertex
                writer.write("v " + point.x + " " + point.y + " " + point.z + "\n");
            }

            int count = 0;
            int matIndex = 0;

            for (Point3i face : faces) {
                // progress iprogress++ / nprogress;
                fireProgress("Writing OBJ file...", ipgrogress++, nprogress);

                if (objCfg.isMaterialEnabled()) {
                    if (count == 0) {

                        writer.write("usemtl Material." + getValueIndex(materialsKeys.get(matIndex)) + "\n");
                        matIndex++;
                        count = 12;
                    }

                    count--;
                }

                writer.write("f " + face.x + " " + face.y + " " + face.z + "\n");
            }
        }

        if (objCfg.isMaterialEnabled()) {

            try (BufferedWriter materialWriter = new BufferedWriter(new FileWriter(materialFile))) {

                int count = 0;
                Iterator<Map.Entry<String, Point3f>> itMtl = materials.entrySet().iterator();
                while (itMtl.hasNext()) {

                    Map.Entry<String, Point3f> next = itMtl.next();
                    Point3f material = next.getValue();

                    materialWriter.write("newmtl Material." + count + "\n"
                            + "Ns 96.078431\n"
                            + "Ka 0 0 0\n"
                            + "Kd " + material.x + " " + material.y + " " + material.z + "\n"
                            + "Ks 0.500000 0.500000 0.500000\n"
                            + "Ni 1.000000\n"
                            + "d 1.000000\n"
                            + "illum 2\n");

                    count++;
                }
            }
        }

        return (objCfg.isMaterialEnabled())
                ? new File[]{objCfg.getOutputFile(), objCfg.getMaterialFile()}
                : new File[]{objCfg.getOutputFile()};
    }

    private void createCube(Point3d size, Point3d translation) {

        cubeVertices.add(new Point3d(size.x / 2.d + translation.x, size.y / 2.d + translation.y, -size.z / 2.d + translation.z));
        cubeVertices.add(new Point3d(size.x / 2.d + translation.x, -size.y / 2.d + translation.y, -size.z / 2.d + translation.z));
        cubeVertices.add(new Point3d(-size.x / 2.d + translation.x, -size.y / 2.d + translation.y, -size.z / 2.d + translation.z));
        cubeVertices.add(new Point3d(-size.x / 2.d + translation.x, size.y / 2.d + translation.y, -size.z / 2.d + translation.z));
        cubeVertices.add(new Point3d(size.x / 2.d + translation.x, size.y / 2.d + translation.y, size.z / 2.d + translation.z));
        cubeVertices.add(new Point3d(size.x / 2.d + translation.x, -size.y / 2.d + translation.y, size.z / 2.d + translation.z));
        cubeVertices.add(new Point3d(-size.x / 2.d + translation.x, -size.y / 2.d + translation.y, size.z / 2.d + translation.z));
        cubeVertices.add(new Point3d(-size.x / 2.d + translation.x, size.y / 2.d + translation.y, size.z / 2.d + translation.z));

        int currentOffset = cubeVertices.size() - 7;

        faces.add(new Point3i(currentOffset + 0, currentOffset + 1, currentOffset + 2));
        faces.add(new Point3i(currentOffset + 4, currentOffset + 7, currentOffset + 6));
        faces.add(new Point3i(currentOffset + 0, currentOffset + 4, currentOffset + 5));
        faces.add(new Point3i(currentOffset + 1, currentOffset + 5, currentOffset + 6));
        faces.add(new Point3i(currentOffset + 2, currentOffset + 6, currentOffset + 7));
        faces.add(new Point3i(currentOffset + 4, currentOffset + 0, currentOffset + 3));
        faces.add(new Point3i(currentOffset + 3, currentOffset + 0, currentOffset + 2));
        faces.add(new Point3i(currentOffset + 5, currentOffset + 4, currentOffset + 6));
        faces.add(new Point3i(currentOffset + 1, currentOffset + 0, currentOffset + 5));
        faces.add(new Point3i(currentOffset + 2, currentOffset + 1, currentOffset + 6));
        faces.add(new Point3i(currentOffset + 3, currentOffset + 2, currentOffset + 7));
        faces.add(new Point3i(currentOffset + 7, currentOffset + 4, currentOffset + 3));

    }

    private Point3d getPosition(Point3i index, VoxelFileHeader header) {

        double posX = header.getMinCorner().x + (header.getVoxelSize().x / 2.0d) + (index.x * header.getVoxelSize().x);
        double posY = header.getMinCorner().y + (header.getVoxelSize().y / 2.0d) + (index.y * header.getVoxelSize().y);
        double posZ = header.getMinCorner().z + (header.getVoxelSize().z / 2.0d) + (index.z * header.getVoxelSize().z);

        return new Point3d(posX, posY, posZ);
    }

    private int getValueIndex(String key) {

        Iterator<Map.Entry<String, Point3f>> iterator1 = materials.entrySet().iterator();

        int count = 0;

        while (iterator1.hasNext()) {
            Map.Entry<String, Point3f> next = iterator1.next();
            if (next.getKey().equals(key)) {
                return count;
            }

            count++;
        }

        return -1;
    }
}
