/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.spds;

import org.amapvox.commons.util.io.file.CSVFile;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class OctreeFactory {

    public final static int DEFAULT_MAXIMUM_POINT_NUMBER = 50;

    public static Octree<Point3d> createOctreeFromPointFile(CSVFile file, int maximumPointNumber, boolean sortPoints, Matrix4d transfMatrix) throws Exception {

        List<Point3d> pointList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String line;

            if (file.containsHeader()) {
                reader.readLine();
            }

            for (int i = 0; i < file.getNbOfLinesToSkip(); i++) {
                reader.readLine();
            }

            Map<String, Integer> columnAssignment = file.getColumnAssignment();

            while ((line = reader.readLine()) != null) {

                String[] split = line.split(file.getColumnSeparator());

                Point3d transformedPoint = new Point3d(
                        Float.valueOf(split[columnAssignment.get("X")]),
                        Float.valueOf(split[columnAssignment.get("Y")]),
                        Float.valueOf(split[columnAssignment.get("Z")]));
                transfMatrix.transform(transformedPoint);

                pointList.add(transformedPoint);
            }

            reader.close();

        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }

        Point3d[] points = new Point3d[pointList.size()];

        if (sortPoints) {
            Collections.sort(pointList, new Point3dComparator());
        }

        pointList.toArray(points);

        Octree octree = new Octree(maximumPointNumber);

        octree.setPoints(points);

        return octree;
    }
}
