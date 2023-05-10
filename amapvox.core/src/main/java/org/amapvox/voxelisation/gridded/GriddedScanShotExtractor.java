/*
 * Copyright (C) 2016 UMR AMAP (botAnique et Modélisation de l'Architecture des Plantes et des végétations.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.amapvox.voxelisation.gridded;

import java.awt.Color;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.amapvox.commons.util.IterableWithException;
import org.amapvox.commons.util.IteratorWithException;
import org.amapvox.lidar.gridded.GriddedPointScan;
import org.amapvox.lidar.gridded.LDoublePoint;
import org.amapvox.lidar.gridded.LFloatPoint;
import org.amapvox.lidar.gridded.LPoint;
import org.amapvox.shot.Shot;
import org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinates;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 *
 * @author Julien Heurtebize
 */
public class GriddedScanShotExtractor implements IterableWithException<Shot> {

    private final GriddedPointScan scan;
    private final Matrix4d transformation;

    private boolean[] azimuts;
    private boolean[] zenithals;

    private SimpleSpherCoords[][] angles;

    /*this class contains azimut and zenith information,
    this is not a doublon with the SphericalCoordinates class, this is a light version*/
    private class SimpleSpherCoords {

        public double azimut;
        public double zenith;

        public SimpleSpherCoords() {

        }

        public SimpleSpherCoords(double azimut, double elevation) {
            this.azimut = azimut;
            this.zenith = elevation;
        }

    }

    public GriddedScanShotExtractor(GriddedPointScan scan, Matrix4d transformation) throws Exception {
        this.scan = scan;
        this.transformation = transformation;
        if (scan.isReturnInvalidPoint()) {
            fillNoHit();
        }
    }

    private void fillNoHit() throws Exception {
        
        Logger.getLogger(GriddedScanShotExtractor.class.getName()).info("Computing missing shots...");

        scan.computeExtremumsAngles();
        
        scan.openScanFile(scan.getFile());

        angles = new SimpleSpherCoords[this.scan.getHeader().getNumRows()][this.scan.getHeader().getNumCols()];

        azimuts = new boolean[this.scan.getHeader().getNumCols()];
        zenithals = new boolean[this.scan.getHeader().getNumRows()];

        for (LPoint point : scan) {
            if (point.valid) {

                double x, y, z;

                if (scan.getHeader().isPointInDoubleFormat()) {
                    x = ((LDoublePoint) point).x;
                    y = ((LDoublePoint) point).y;
                    z = ((LDoublePoint) point).z;
                } else {
                    x = ((LFloatPoint) point).x;
                    y = ((LFloatPoint) point).y;
                    z = ((LFloatPoint) point).z;
                }

                Vector3d dir = new Vector3d(x, y, z);
                dir.normalize();

                SphericalCoordinates sc = new SphericalCoordinates(new Vector3D(dir.x, dir.y, dir.z));

                angles[point.rowIndex][point.columnIndex] = new SimpleSpherCoords();
                angles[point.rowIndex][point.columnIndex].azimut = sc.getTheta();
                angles[point.rowIndex][point.columnIndex].zenith = sc.getPhi();

                azimuts[point.columnIndex] = true;
                zenithals[point.rowIndex] = true;
            }
        }

        int lastValidRowIndex = -1;
        int lastValidColumnIndex = -1;
        int nfill = 0;

        for (int row = 0; row < angles.length; row++) {

            for (int column = 0; column < angles[0].length; column++) {

                if (angles[row][column] == null) {

                    double azimut = Double.NaN;
                    double zenithal = Double.NaN;

                    if (azimuts[column]) {
                        for (int i = row + 1, j = row - 1; i < angles.length || j >= 0; i++, j--) {

                            if (i < angles.length && angles[i][column] != null) {
                                azimut = angles[i][column].azimut;
                                azimuts[column] = true;
                                break;
                            }

                            if (j >= 0 && angles[j][column] != null) {
                                azimut = angles[j][column].azimut;
                                azimuts[column] = true;
                                break;
                            }
                        }
                    }

                    if (azimuts[column]) {

                        for (int i = row + 1, j = row - 1; i < angles.length || j >= 0; i++, j--) {

                            if (i < angles.length && angles[i][column] != null) {
                                zenithal = (angles[i][column].zenith + ((i - row) * scan.getElevationStepAngle()));
                                azimuts[column] = true;
                                break;
                            }

                            if (j >= 0 && angles[j][column] != null) {
                                zenithal = (angles[j][column].zenith - ((row - j) * scan.getElevationStepAngle()));
                                azimuts[column] = true;
                                break;
                            }
                        }
                    }

                    /*if(zenithals[row]){
                        
                        for(int i=column+1, j=column-1;i<angles[0].length || j>=0;i++, j--){
                        
                            if(i<angles[0].length && angles[row][i] != null){
                                zenithal = angles[row][i].zenith;
                                zenithals[row] = true;
                                break;
                            }

                            if(j >=0 && angles[row][j] != null){
                                zenithal = angles[row][j].zenith;
                                zenithals[row] = true;
                                break;
                            }
                        }
                    }*/
                    if (Double.isNaN(azimut)) {
                        azimut = (scan.getAzim_min() - ((column - scan.getColIndexAzimMin()) * scan.getAzimutalStepAngle()));
                    }

                    if (Double.isNaN(zenithal)) {
                        if (lastValidRowIndex != -1) {
                            zenithal = (angles[lastValidRowIndex][lastValidColumnIndex].zenith - ((row - lastValidRowIndex) * scan.getElevationStepAngle()));
                        } else {
                            zenithal = (scan.getElev_min() - ((row - scan.getRowIndexElevMin()) * scan.getElevationStepAngle()));
                        }

                    }

                    angles[row][column] = new SimpleSpherCoords(azimut, zenithal);
                    nfill++;

                } else {
                    lastValidRowIndex = row;
                    lastValidColumnIndex = column;
                }
            }
        }
        Logger.getLogger(GriddedScanShotExtractor.class.getName()).log(Level.INFO, "Filled {0} missing shots", nfill);

    }

    @Override
    public IteratorWithException<Shot> iterator() {
        
        final Iterator<LPoint> pointIterator = scan.iterator();

        IteratorWithException<Shot> it = new IteratorWithException<Shot>() {

            private int index = 0;

            @Override
            public boolean hasNext() {
                return pointIterator.hasNext();
            }

            @Override
            public Shot next() throws Exception {

                Shot shot;

                LPoint point = pointIterator.next();

                if (point.valid) {

                    double xDirection, yDirection, zDirection;
                    if (scan.getHeader().isPointInDoubleFormat()) {
                        xDirection = ((LDoublePoint) point).x;
                        yDirection = ((LDoublePoint) point).y;
                        zDirection = ((LDoublePoint) point).z;
                    } else {
                        xDirection = ((LFloatPoint) point).x;
                        yDirection = ((LFloatPoint) point).y;
                        zDirection = ((LFloatPoint) point).z;

                        //System.out.println(xDirection+"\t"+yDirection+"\t"+zDirection);
                    }

                    Point3d origin = new Point3d(0.d, 0.d, 0.d);
                    transformation.transform(origin);
                    
                    Vector3d direction = new Vector3d(xDirection, yDirection, zDirection);
                    direction.normalize();
                    transformation.transform(direction);

                    double range = Math.sqrt((xDirection * xDirection) + (yDirection * yDirection) + (zDirection * zDirection));

                    shot = new Shot(index++, origin, direction, new double[]{range});
                    if (!Float.isNaN(point.intensity)) {
                        shot.getEcho(0).addFloat("intensity", point.intensity);
                    }
                    if (point.red >= 0) {
                        shot.getEcho(0).addInteger("color", new Color(point.red, point.green, point.blue).getRGB());
                    }
                    return shot;
                } else if (scan.isReturnInvalidPoint()) {
                    
                    Point3d origin = new Point3d(0.d, 0.d, 0.d);
                    transformation.transform(origin);
                    
                    SphericalCoordinates sc = new SphericalCoordinates(1, angles[point.rowIndex][point.columnIndex].azimut, angles[point.rowIndex][point.columnIndex].zenith);
                    //SphericalCoordinates sc = angles[point.rowIndex][point.columnIndex];
                    Vector3d direction = new Vector3d(sc.getCartesian().getX(), sc.getCartesian().getY(), sc.getCartesian().getZ());
                    direction.normalize();
                    transformation.transform(direction);

                    shot = new Shot(index++, origin, direction, new double[]{});
                    return shot;
                }
                return null;
            }
        };

        return it;
    }

}
