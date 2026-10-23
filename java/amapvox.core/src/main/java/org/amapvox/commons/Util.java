/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons;

import org.amapvox.commons.math.geometry.BoundingBox3D;
import org.amapvox.commons.spds.Octree;
import org.amapvox.commons.spds.OctreeFactory;
import org.amapvox.commons.util.io.file.CSVFile;
import com.github.mreutegg.laszip4j.LASHeader;
import com.github.mreutegg.laszip4j.LASReader;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import org.amapvox.commons.math.util.MatrixUtility;
import org.amapvox.commons.util.io.file.FileManager;
import org.amapvox.lidar.commons.LidarScan;
import org.amapvox.lidar.faro.XYBIterator;
import org.amapvox.voxelisation.VoxelizationCfg;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize
 */
public class Util {

    private final static Logger LOGGER = Logger.getLogger(Util.class);

    public static String getVersion() {

        String buildVersion = "UNDEF";

        try {
            buildVersion = ResourceBundle.getBundle("version").getString("amapvox.version");
        } catch (Exception ex) {
        }

        return buildVersion;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static Octree loadOctree(CSVFile pointcloudFile, Matrix4d vopMatrix) {

        Octree octree = null;

        if (pointcloudFile != null) {

            try {
                LOGGER.info("Loading point cloud file...");
                octree = OctreeFactory.createOctreeFromPointFile(pointcloudFile, OctreeFactory.DEFAULT_MAXIMUM_POINT_NUMBER, false, vopMatrix);
                octree.build();
                LOGGER.info("Point cloud file loaded");

            } catch (Exception ex) {
                LOGGER.error(ex);
            }
        }

        return octree;
    }

    private static List<Point3d> getScanPositions(List<LidarScan> scans, VoxelizationCfg.LidarType lidarType) {

        return switch (lidarType) {
            case XYB -> {
                yield scans.stream()
                .map((LidarScan scan) -> {
                    Point3d scannerPosition = new Point3d();
                    try {
                        scannerPosition = new XYBIterator(scan.getFile()).getHeader().getScannerPosition();
                    } catch (IOException ex) {
                        System.getLogger(Util.class.getName()).log(System.Logger.Level.WARNING, "Failed to read scanner position from XYB file {0}", scan.getFile());
                    }
                    return scannerPosition;
                }).toList();
            }
            case RXP, RSP, PTX, PTG -> {
                yield scans.stream()
                .map(scan -> {
                    Matrix4d sopMatrix = scan.getMatrix();
                    return new Point3d(sopMatrix.m03, sopMatrix.m13, sopMatrix.m23);
                })
                // return as a list of scan positions
                .toList();
            }
            case LAS, LAZ -> {
                yield scans.stream()
                .map((LidarScan scan) -> {
                    LASReader lasReader = new LASReader(scan.getFile());
                    LASHeader header = lasReader.getHeader();
                    return List.of(
                            new Point3d(header.getMinX(), header.getMinY(), header.getMinZ()),
                            new Point3d(header.getMaxX(), header.getMaxY(), header.getMaxZ()));
                })
                .flatMap(List::stream)
                .toList();
            }
            default ->
                List.of(new Point3d());
        };
    }

    public static BoundingBox3D getBoundingBox(List<LidarScan> scans, Matrix4d popMatrix, Matrix4d vopMatrix) {

        String extension = FileManager.getExtension(scans.getFirst().getFile()).substring(1).toUpperCase();
        VoxelizationCfg.LidarType lidarType = VoxelizationCfg.LidarType.valueOf(extension);

        Point3d[] scanPositions = getScanPositions(scans, lidarType).stream()
                .peek(scanPosition -> {
                    Matrix4d transformationMatrix = MatrixUtility.identity4d();
                    if (vopMatrix != null) {
                        transformationMatrix.mul(vopMatrix);
                    }
                    if (popMatrix != null) {
                        transformationMatrix.mul(popMatrix);
                    }
                    transformationMatrix.transform(scanPosition);
                })
                // return as an array of scan positions
                .toArray(Point3d[]::new);

        // get bounding box from scan positions
        BoundingBox3D boundingBox = BoundingBox3D.from(scanPositions);

        // default 10m buffer except for LAS/LAZ scan
        double buffer = switch (lidarType) {
            case LAZ, LAS -> {
                yield boundingBox.min.distance(boundingBox.max) < 10.0 ? 10.d : 0.d;
            }
            default -> {
                yield 10.d;
            }
        };

        if (buffer > 0.d) {
            // add buffer
            Point3d buffer3d = new Point3d(buffer, buffer, buffer);
            boundingBox.max.add(buffer3d);
            boundingBox.min.sub(buffer3d);
        }

        // rounding
        boundingBox.round();
        
        // z >= 0
        boundingBox.min.z = Math.max(boundingBox.min.z, 0.d);

        return boundingBox;
    }
}
