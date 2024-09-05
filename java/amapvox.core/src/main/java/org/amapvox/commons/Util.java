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
import com.github.mreutegg.laszip4j.LASPoint;
import com.github.mreutegg.laszip4j.LASReader;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;
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

        LASReader lasReader = new LASReader(file);
        LASHeader header = lasReader.getHeader();
        return new BoundingBox3D(
                new Point3d(header.getMinX(), header.getMinY(), header.getMinZ()),
                new Point3d(header.getMaxX(), header.getMaxY(), header.getMaxZ()));
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
     * @param transformationMatrix
     * @param quick don't use classification filters
     * @param classificationsToDiscard list of point classification to skip
     * during getting bounding box process
     * @return
     */
    public static BoundingBox3D getBoundingBoxOfPoints(File pointFile, Matrix4d transformationMatrix, boolean quick, List<Integer> classificationsToDiscard) {

        BoundingBox3D boundingBox = new BoundingBox3D();

        Matrix4d identityMatrix = new Matrix4d();
        identityMatrix.setIdentity();

        if (transformationMatrix.equals(identityMatrix) && quick) {

            LASReader lasReader = new LASReader(pointFile);
            LASHeader header = lasReader.getHeader();
            boundingBox.min = new Point3d(header.getMinX(), header.getMinY(), header.getMinZ());
            boundingBox.max = new Point3d(header.getMaxX(), header.getMaxY(), header.getMaxZ());

        } else {

            int count = 0;
            double xMin = 0, yMin = 0, zMin = 0;
            double xMax = 0, yMax = 0, zMax = 0;

            LASReader lasReader = new LASReader(pointFile);
            LASHeader lasHeader = lasReader.getHeader();
            // scale factors and offsets
            double x_offset = lasHeader.getXOffset();
            double x_scale_factor = lasHeader.getXScaleFactor();
            double y_offset = lasHeader.getYOffset();
            double y_scale_factor = lasHeader.getYScaleFactor();
            double z_offset = lasHeader.getZOffset();
            double z_scale_factor = lasHeader.getZScaleFactor();
            for (LASPoint p : lasReader.getPoints()) {

                if (!classificationsToDiscard.contains((int) p.getClassification())) {
                    //skip those
                    Point3d pt = new Point3d(
                            (p.getX() * x_scale_factor) + x_offset,
                            (p.getY() * y_scale_factor) + y_offset,
                            (p.getZ() * z_scale_factor) + z_offset);
                    transformationMatrix.transform(pt);

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
        }

        return boundingBox;
    }
}
