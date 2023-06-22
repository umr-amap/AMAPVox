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

    private SimpleSphericalCoordinates[][] angles;

    /*this class contains azimuth and zenith information,
    light version of SphericalCoordinates */
    private class SimpleSphericalCoordinates {

        final private double azimuth;
        final private double zenith;

        SimpleSphericalCoordinates(double azimuth, double zenith) {
            this.azimuth = azimuth;
            this.zenith = zenith;
        }
    }

    public GriddedScanShotExtractor(GriddedPointScan scan, Matrix4d transformation) throws Exception {
        this.scan = scan;
        this.transformation = transformation;
    }

    public void init() throws Exception {
        if (scan.isReturnMissingPoint()) {
            fillMissingShot();
        }
    }

    private void fillMissingShot() throws Exception {

        Logger.getLogger(GriddedScanShotExtractor.class.getName()).info("Computing missing shots...");

        int nzenith = scan.getHeader().getNZenith();
        int nazimuth = scan.getHeader().getNAzimuth();
        angles = new SimpleSphericalCoordinates[nazimuth][nzenith];

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

                angles[point.azimuthIndex][point.zenithIndex] = new SimpleSphericalCoordinates(sc.getTheta(), sc.getPhi());
            }
        }

        // compute missing shot angles in a separate array
        SimpleSphericalCoordinates[][] filledAngles = new SimpleSphericalCoordinates[nazimuth][nzenith];

        for (int iazim = 0; iazim < nazimuth; iazim++) {
            for (int izenith = 0; izenith < nzenith; izenith++) {

                if (angles[iazim][izenith] != null) {
                    continue;
                }

                double azimuth = Double.NaN;
                double zenith = Double.NaN;

                if (!Double.isNaN(scan.getAveragedAzimuth()[iazim])) {
                    // look for closest non null azimuth value on same vertical sweep
                    for (int up = izenith - 1, down = izenith + 1; up >= 0 || down < nzenith; up--, down++) {
                        if (up >= 0 && angles[iazim][up] != null) {
                            azimuth = angles[iazim][up].azimuth;
                            break;
                        }
                        if (down < nzenith && angles[iazim][down] != null) {
                            azimuth = angles[iazim][down].azimuth;
                            break;
                        }
                    }
                    // fallback: get averaged azimuthal angle 
                    if (Double.isNaN(azimuth)) {
                        azimuth = scan.getAveragedAzimuth()[iazim];
                    }
                } else {
                    // look for closest non null averaged azimuth value on neighbooring vertical sweeps and interpolate
                    for (int fw = iazim + 1, bw = iazim - 1; fw < nazimuth || bw >= 0; fw++, bw--) {
                        if (fw < nazimuth) {
                            double azimFw = scan.getAveragedAzimuth()[fw];
                            if (!Double.isNaN(azimFw)) {
                                azimuth = (azimFw - ((fw - iazim) * scan.getAzimuthalStepAngle()));
                                break;
                            }
                        }
                        if (bw >= 0) {
                            double azimBw = scan.getAveragedAzimuth()[bw];
                            if (!Double.isNaN(azimBw)) {
                                azimuth = (azimBw + ((iazim - bw) * scan.getAzimuthalStepAngle()));
                                break;
                            }
                        }
                    }
                }

                if (!Double.isNaN(scan.getAveragedZenith()[izenith])) {
                    // look for closest non null zenith value on same horizontal rotation
                    for (int fw = iazim + 1, bw = iazim - 1; fw < nazimuth || bw >= 0; fw++, bw--) {
                        if (fw < nazimuth && angles[fw][izenith] != null) {
                            zenith = angles[fw][izenith].zenith;
                            break;
                        }
                        if (bw >= 0 && angles[bw][izenith] != null) {
                            zenith = angles[bw][izenith].zenith;
                            break;
                        }
                    }
                    // fallback: get averaged zenithal angle
                    if (Double.isNaN(zenith)) {
                        zenith = scan.getAveragedZenith()[izenith];
                    }
                } else {
                    // look for closest non null averaged zenith value on neighbooring horizontal rotations and interpolate
                    for (int up = izenith + 1, down = izenith - 1; up < nzenith || down >= 0; up++, down--) {
                        if (up < nzenith) {
                            double zenithUp = scan.getAveragedZenith()[up];
                            if (!Double.isNaN(zenithUp)) {
                                zenith = (zenithUp - ((up - izenith) * scan.getZenithalStepAngle()));
                                break;
                            }
                        }
                        if (down >= 0) {
                            double zenithDown = scan.getAveragedZenith()[down];
                            if (!Double.isNaN(zenithDown)) {
                                zenith = (zenithDown + ((izenith - down) * scan.getZenithalStepAngle()));
                                break;
                            }
                        }
                    }
                }

                if (!Double.isNaN(azimuth) && !Double.isNaN(zenith)) {
                    filledAngles[iazim][izenith] = new SimpleSphericalCoordinates(azimuth, zenith);
                }
            }
        }

        // fill missing shot angles
        int nfill = 0;
        for (int iazim = 0; iazim < nazimuth; iazim++) {
            for (int izenith = 0; izenith < nzenith; izenith++) {
                if (null == angles[iazim][izenith] && null != filledAngles[iazim][izenith]) {
                    angles[iazim][izenith] = new SimpleSphericalCoordinates(
                            filledAngles[iazim][izenith].azimuth,
                            filledAngles[iazim][izenith].zenith);
                    nfill++;
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
                    double range = direction.length();

                    direction.normalize();
                    transformation.transform(direction);

                    shot = new Shot(index++, origin, direction, new double[]{range});
                    if (!Float.isNaN(point.intensity)) {
                        shot.getEcho(0).addFloat("intensity", point.intensity);
                    }
                    if (point.red >= 0) {
                        shot.getEcho(0).addInteger("color", new Color(point.red, point.green, point.blue).getRGB());
                    }
                    return shot;
                } else if (scan.isReturnMissingPoint()) {

                    Point3d origin = new Point3d(0.d, 0.d, 0.d);
                    transformation.transform(origin);

                    SphericalCoordinates sc = new SphericalCoordinates(1, angles[point.azimuthIndex][point.zenithIndex].azimuth, angles[point.azimuthIndex][point.zenithIndex].zenith);
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
