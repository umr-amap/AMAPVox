/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons;

import org.amapvox.commons.math.geometry.BoundingBox3D;
import org.amapvox.commons.spds.Octree;
import org.amapvox.commons.spds.OctreeFactory;
import org.amapvox.commons.util.IteratorWithException;
import org.amapvox.commons.util.io.file.CSVFile;
import org.amapvox.lidar.laszip.LASHeader;
import org.amapvox.lidar.laszip.LASPoint;
import org.amapvox.lidar.laszip.LASReader;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
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

    public static BoundingBox3D getALSMinAndMax(File file) {

        try (LASReader lasReader = new LASReader(file)) {
            LASHeader header = lasReader.getHeader();

            return new BoundingBox3D(
                    new Point3d(header.min_x, header.min_y, header.min_z),
                    new Point3d(header.max_x, header.max_y, header.max_z));

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
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

    /**
     *
     * @param pointFile
     * @param resultMatrix
     * @param quick don't use classification filters
     * @param classificationsToDiscard list of point classification to skip
     * during getting bounding box process
     * @return
     */
    public static BoundingBox3D getBoundingBoxOfPoints(File pointFile, Matrix4d resultMatrix, boolean quick, List<Integer> classificationsToDiscard) {

        BoundingBox3D boundingBox = new BoundingBox3D();

        Matrix4d identityMatrix = new Matrix4d();
        identityMatrix.setIdentity();

        if (resultMatrix.equals(identityMatrix) && quick) {

            boundingBox = getALSMinAndMax(pointFile);

        } else {

            int count = 0;
            double xMin = 0, yMin = 0, zMin = 0;
            double xMax = 0, yMax = 0, zMax = 0;

            LASHeader lasHeader;
            try (LASReader lasReader = new LASReader(pointFile)) {
                lasHeader = lasReader.getHeader();
                IteratorWithException<LASPoint> iterator = lasReader.iterator();
                while (iterator.hasNext()) {

                    LASPoint point = iterator.next();

                    if (!classificationsToDiscard.contains((int) point.classification)) {
                        //skip those
                        Point3d pt = new Point3d(
                                (point.x * lasHeader.x_scale_factor) + lasHeader.x_offset,
                                (point.y * lasHeader.y_scale_factor) + lasHeader.y_offset,
                                (point.z * lasHeader.z_scale_factor) + lasHeader.z_offset);
                        resultMatrix.transform(pt);

                        if (count != 0) {

                            if (pt.x < xMin) {
                                xMin = pt.x;
                            } else if (pt.x > xMax) {
                                xMax = pt.x;
                            }

                            if (pt.y < yMin) {
                                yMin = pt.y;
                            } else if (pt.y > yMax) {
                                yMax = pt.y;
                            }

                            if (pt.z < zMin) {
                                zMin = pt.z;
                            } else if (pt.z > zMax) {
                                zMax = pt.z;
                            }

                        } else {

                            xMin = pt.x;
                            yMin = pt.y;
                            zMin = pt.z;

                            xMax = pt.x;
                            yMax = pt.y;
                            zMax = pt.z;

                            count++;
                        }
                    }
                }

                boundingBox.min = new Point3d(xMin, yMin, zMin);
                boundingBox.max = new Point3d(xMax, yMax, zMax);
            } catch (IOException ex) {
                LOGGER.error(ex);
                java.util.logging.Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                LOGGER.error(ex);
            }
        }

        return boundingBox;
    }
}
