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
package org.amapvox.lidar.gridded;

import org.amapvox.commons.math.util.Statistic;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinates;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * <p>
 * This abstract class can be used by subclasses who need to handle gridded
 * point scan files. It is assumed that the scanner performs alternatively (i)
 * vertical sweeps with regular zenith increment, and (ii) horizontal rotations
 * with regular azimuthal increment.
 * </p>
 * <p>
 * The subclass should implements iterator and file opening.
 * </p>
 *
 * @author Julien Heurtebize
 */
public abstract class GriddedPointScan implements Iterable<LPoint> {

    protected PointScanHeader header;

    /**
     * First zenithal index to read in the scan. Vertical sweep.
     */
    protected int startZenithIndex = -1;

    /**
     * Last zenithal index to read in the scan. Vertical sweep.
     */
    protected int endZenithIndex = -1;

    /**
     * First azimuthal index to read in the scan. Horizontal rotation.
     */
    protected int startAzimuthIndex = -1;

    /**
     * Last azimuthal index to read in the scan. Horizontal rotation.
     */
    protected int endAzimuthIndex = -1;

    /**
     * Averaged azimuthal angle for every vertical sweep.
     */
    private double[] averagedAzimuth;
    /**
     * Averaged zenithal angle for every horizontal rotation.
     */
    private double[] averagedZenith;

    /**
     * Delta azimuthal angle in radians. Averaged azimuthal angle between two
     * vertical sweeps.
     */
    private double azim_delta = Double.NaN;

    /**
     * Delta zenithal step angle in radians. Averaged zenithal angle between two
     * horizontal rotations.
     */
    private double zenith_delta = Double.NaN;
    /**
     * Should iterator return missing points ? (shot without return)
     */
    protected boolean returnMissingPoint;

    // LPoint array, without empty point
    protected LPoint[][] points;

    /**
     * Bonded gridded point scan file.
     */
    final private File file;

    public GriddedPointScan(File file) {
        this.file = file;
        header = new PointScanHeader();
    }

    public abstract void readHeader() throws FileNotFoundException, IOException;

    public abstract void readPointCloud() throws FileNotFoundException, IOException;

    /**
     * Returns an iterator to get points from the scan file as a
     * {@link org.amapvox.lidar.gridded.LPoint}
     *
     * @return A {@link LPoint} point returned by the iterator.
     */
    @Override
    public Iterator<LPoint> iterator() {
        return new GriddedScanIterator();
    }

    synchronized public void open() throws FileNotFoundException, IOException {

        readHeader();
        readPointCloud();
        reset();
        initZenith();
        reset();
        initAzimuth();
        reset();
    }

    private void initZenith() {

        // averaged zenith angle for every horizontal rotation
        averagedZenith = new double[header.getNZenith()];
        for (int i = 0; i < header.getNZenith(); i++) {

            setZenithIndex(i);

            Statistic zenithStatistics = new Statistic();
            for (LPoint point : this) {
                if (point.valid) {
                    SphericalCoordinates sc;
                    if (header.isPointInFloatFormat()) {
                        LFloatPoint floatPoint = (LFloatPoint) point;
                        sc = new SphericalCoordinates(new Vector3D(floatPoint.x, floatPoint.y, floatPoint.z).normalize());
                    } else {
                        LDoublePoint doublePoint = (LDoublePoint) point;
                        sc = new SphericalCoordinates(new Vector3D(doublePoint.x, doublePoint.y, doublePoint.z).normalize());
                    }
                    zenithStatistics.addValue(sc.getPhi());
                }
            }
            averagedZenith[i] = zenithStatistics.getNbValues() > 0
                    ? zenithStatistics.getMean()
                    : Double.NaN;

//            System.out.println("zenith index " + i + " averaged zenith " + averagedZenith[i]);
        }

        // zenithal angle delta
        Statistic deltaZenithStatistics = new Statistic();
        for (int i = 0; i < (averagedZenith.length - 1); i++) {
            if (!Double.isNaN(averagedZenith[i]) && !Double.isNaN(averagedZenith[i + 1])) {
                deltaZenithStatistics.addValue(averagedZenith[i] - averagedZenith[i + 1]);
            }
        }

        if (deltaZenithStatistics.getNbValues() >= 10) {
            // arbitrarily get averaged delta zenith for series greater than 10 values
            zenith_delta = deltaZenithStatistics.getMean();
        } else {
            // computes delta zenith with min & max zenithal angles
            // zenith of first non empty horizontal rotation
            double z1 = Double.NaN;
            int iz1 = -1;
            for (int i = 0; i < averagedZenith.length; i++) {
                // min
                if (!Double.isNaN(averagedZenith[i])) {
                    z1 = averagedZenith[i];
                    iz1 = i;
                    break;
                }

            }
            // zenith of last non empty horizontal rotation
            double z2 = Float.NaN;
            int iz2 = -1;
            for (int i = averagedZenith.length - 1; i > 0; i--) {
                // max
                if (!Double.isNaN(averagedZenith[i])) {
                    z2 = averagedZenith[i];
                    iz2 = i;
                    break;
                }
            }
            double fov = z1 - z2;
            zenith_delta = fov / ((double) iz2 - iz1);
        }
    }

    private void initAzimuth() {

        averagedAzimuth = new double[header.getNAzimuth()];
        for (int i = 0; i < header.getNAzimuth(); i++) {

            setAzimuthIndex(i);

            Statistic azimuthStatistics = new Statistic();
            for (LPoint point : this) {
                if (point.valid) {
                    SphericalCoordinates sc;
                    if (header.isPointInFloatFormat()) {
                        LFloatPoint floatPoint = (LFloatPoint) point;
                        sc = new SphericalCoordinates(new Vector3D(floatPoint.x, floatPoint.y, floatPoint.z).normalize());
                    } else {
                        LDoublePoint doublePoint = (LDoublePoint) point;
                        sc = new SphericalCoordinates(new Vector3D(doublePoint.x, doublePoint.y, doublePoint.z).normalize());
                    }
                    double azimuth = Math.abs(sc.getTheta() +  Math.PI) < 0.1  ? sc.getTheta() + 2 * Math.PI : sc.getTheta();
                    azimuthStatistics.addValue(azimuth);
                }
            }

            averagedAzimuth[i] = azimuthStatistics.getNbValues() > 0
                    ? azimuthStatistics.getMean()
                    : Double.NaN;

//            System.out.println("index azimuth " + i + " averaged azimuth " + averagedAzimuth[i]);
        }

        // 
        Statistic deltaAzimStatistics = new Statistic();
        for (int i = 0; i < (averagedAzimuth.length - 1); i++) {
            if (!Double.isNaN(averagedAzimuth[i]) && !Double.isNaN(averagedAzimuth[i + 1])) {
                deltaAzimStatistics.addValue(averagedAzimuth[i] - averagedAzimuth[i + 1]);
            }
        }

        if (deltaAzimStatistics.getNbValues() >= 10) {
            azim_delta = deltaAzimStatistics.getMean();
        } else {

            double a1 = Double.NaN;
            int ia1 = -1;
            for (int i = 0; i < averagedAzimuth.length; i++) {
                // min
                if (!Double.isNaN(averagedAzimuth[i])) {
                    a1 = averagedAzimuth[i];
                    ia1 = i;
                    break;
                }

            }
            double a2 = Double.NaN;
            int ia2 = -1;
            for (int i = averagedAzimuth.length - 1; i > 0; i--) {
                // max
                if (!Double.isNaN(averagedAzimuth[i])) {
                    a2 = averagedAzimuth[i];
                    ia2 = i;
                    break;
                }
            }
            double fov = a1 - a2;
            if (Math.abs(a2 - a1) < 0.1) {
                fov += (Math.PI * 2);
            }
            azim_delta = fov / (double) (ia2 - ia1);
        }
    }

    /**
     * Reset the zenithal and azimuthal ranges to default values.
     */
    public void reset() {

        setZenithRange(0, header.getNZenith() - 1);
        setAzimuthRange(0, header.getNAzimuth() - 1);
    }

    /**
     * Set the azimuth index to read from the file. Others vertical sweeps will
     * be ignored.
     * <p>
     * You should invoke this method before you get the iterator.</p>
     *
     * @param azimuthIndex The azimuth index to read
     */
    public void setAzimuthIndex(int azimuthIndex) {
        setAzimuthRange(azimuthIndex, azimuthIndex);
    }

    /**
     * Set the range (inclusion) of azimuth index to read from the file.
     * Vertical sweeps outside the range will be ignored.
     * <p>
     * You should invoke this method before you get the iterator.</p>
     *
     * @param azimuthIndex1 The first azimuth index of the range
     * @param azimuthIndex2 The last azimuth index of the range
     */
    public void setAzimuthRange(int azimuthIndex1, int azimuthIndex2) {

        if (azimuthIndex1 < 0 || azimuthIndex1 >= header.getNAzimuth()) {
            throw new IllegalArgumentException("azimuth index out of range exception (" + azimuthIndex1 + " not in [" + 0 + " " + header.getNAzimuth() + "[)");
        }

        if (azimuthIndex2 < 0 || azimuthIndex2 >= header.getNAzimuth()) {
            throw new IllegalArgumentException("azimuth index out of range exception (" + azimuthIndex2 + " not in [" + 0 + " " + header.getNAzimuth() + "[)");
        }

        if (azimuthIndex1 > azimuthIndex2) {
            throw new IllegalArgumentException("azimuth index 1 must be smaller than azimuth index 2");
        }

        this.startAzimuthIndex = azimuthIndex1;
        this.endAzimuthIndex = azimuthIndex2;
    }

    /**
     * Set the zenith index to read from the file. Other horizontal slices will
     * be ignored.
     * <p>
     * You should invoke this method before you get the iterator.</p>
     *
     * @param zenithIndex The zenith index to read
     */
    public void setZenithIndex(int zenithIndex) {

        setZenithRange(zenithIndex, zenithIndex);
    }

    /**
     * Set the range (inclusion) of zenithal index to read from the file.
     * Horizontal slices outside the zenithal range will be ignored.
     * <p>
     * You should invoke this method before you get the iterator.</p>
     *
     * @param zenithIndex1 The first zenithal index of the range
     * @param zenithIndex2 The last zenithal index of the range
     */
    public void setZenithRange(int zenithIndex1, int zenithIndex2) {

        if (zenithIndex1 < 0 || zenithIndex1 >= header.getNZenith()) {
            throw new IllegalArgumentException("zenith index out of range exception (" + zenithIndex1 + " not in [" + 0 + " " + header.getNZenith() + "[)");
        }

        if (zenithIndex2 < 0 || zenithIndex2 >= header.getNZenith()) {
            throw new IllegalArgumentException("zenith index out of range exception (" + zenithIndex2 + " not in [" + 0 + " " + header.getNZenith() + "[)");
        }

        if (zenithIndex1 > zenithIndex2) {
            throw new IllegalArgumentException("zenith index 1 must be smaller than zenith index 2");
        }

        this.startZenithIndex = zenithIndex1;
        this.endZenithIndex = zenithIndex2;
    }

    /**
     * Are missing points returned ?
     * <p>
     * A missing point is a point without a position because the laser shot
     * didn't get a return.</p>
     *
     * @return true if missing points are returned, false otherwise
     */
    public boolean isReturnMissingPoint() {
        return returnMissingPoint;
    }

    /**
     * Set if invalid points are returned or not.
     * <p>
     * A missing point is a point without a position because the laser shot
     * didn't get a return.</p>
     *
     * @param returnMissingPoint true if missing point should be returned, false
     * otherwise
     */
    public void setReturnMisingPoint(boolean returnMissingPoint) {
        this.returnMissingPoint = returnMissingPoint;
    }

    public double[] getAveragedZenith() {
        return averagedZenith;
    }

    public double[] getAveragedAzimuth() {
        return averagedAzimuth;
    }

    /**
     * Get the bonded scan file.
     *
     * @return The bonded scan file.
     */
    public File getFile() {
        return file;
    }

    public PointScanHeader getHeader() {
        return header;
    }

    /**
     * Get the azimuthal step angle.
     *
     * @return The azimuthal step angle in radians
     */
    public double getAzimuthalStepAngle() {

        return azim_delta;
    }

    /**
     * Get the zenithal step angle.
     *
     * @return The zenithal step angle in radians
     */
    public double getZenithalStepAngle() {

        return zenith_delta;
    }

    private class GriddedScanIterator implements Iterator<LPoint> {

        final private int size, nrow, ncol;
        private int cursor;

        GriddedScanIterator() {
            nrow = endZenithIndex - startZenithIndex + 1;
            ncol = endAzimuthIndex - startAzimuthIndex + 1;
            size = nrow * ncol;
        }

        @Override
        public boolean hasNext() {
            return cursor != size;
        }

        @Override
        public LPoint next() {

            int i = cursor;
            if (i >= size) {
                throw new NoSuchElementException();
            }
            int col = i / nrow;
            int row = i - col * nrow;
            col += startAzimuthIndex;
            row += startZenithIndex;
            cursor = i + 1;
            return (null != points[col][row])
                    ? points[col][row]
                    : new LEmptyPoint(col, row);
        }
    }

}
