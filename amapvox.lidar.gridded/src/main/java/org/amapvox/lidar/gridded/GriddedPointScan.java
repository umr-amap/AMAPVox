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
    protected int startZenithIndex;

    /**
     * Last zenithal index to read in the scan. Vertical sweep.
     */
    protected int endZenithIndex;

    /**
     * First azimuthal index to read in the scan. Horizontal rotation.
     */
    protected int startAzimuthIndex;

    /**
     * Last azimuthal index to read in the scan. Horizontal rotation.
     */
    protected int endAzimuthIndex;

    private double[] averagedAzimuth;
    private double[] averagedZenith;

    /**
     * Minimum azimuthal angle in radians. Azimuth of first non empty vertical
     * sweep.
     */
    private double azim_min = Double.NaN;

    /**
     * Maximum azimuthal angle in radians. Azimuth of last non empty vertical
     * sweep.
     */
    private double azim_max = Double.NaN;

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
     * Minimum zenithal angle in radians. Zenith of first non empty horizontal
     * rotation.
     */
    private double zenith_min = Double.NaN;

    /**
     * Maximum zenithal angle in radians. Zenith of last non empty horizontal
     * rotation.
     */
    private double zenith_max = Double.NaN;

    /**
     * Index to identify empty vertical sweep at the beginning and the end of
     * the horizontal rotation.
     */
    private int indexMinAzimAngle = -1;
    private int indexMaxAzimAngle = -1;

    /**
     * Index to identity empty horizontal slices at the beginning and the end of
     * the vertical sweep.
     */
    private int indexMinZenithAngle = -1;
    private int indexMaxZenithAngle = -1;

    /**
     * Should iterator return missing points ? (shot without return)
     */
    protected boolean returnMissingPoint;

    /**
     * Bonded gridded point scan file.
     */
    protected File file;

    public GriddedPointScan() {
        header = new PointScanHeader();
    }

    public abstract void openScanFile(File file) throws FileNotFoundException, IOException, Exception;

    @Override
    public abstract Iterator<LPoint> iterator();

    /**
     * Compute minimum and maximum azimuthal and zenithal angles of the scan.
     */
    public void computeMinMaxAngles() {

        //compute min & max azimutal angle
        resetZenithRange();
        resetAzimuthRange();
        
        resetZenithRange();

        int i;
        averagedAzimuth = new double[header.getNAzimuth()];
        for (i = 0; i < header.getNAzimuth(); i++) {

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
                    azimuthStatistics.addValue(sc.getTheta());
                }
            }
            averagedAzimuth[i] = azimuthStatistics.getNbValues() > 0
                    ? azimuthStatistics.getMean()
                    : Double.NaN;

//            System.out.println("index azimuth " + i + " averaged azimuth " + averagedAzimuth[i]);
        }

        azim_min = Float.NaN;
        for (i = 0; i < averagedAzimuth.length; i++) {
            // min
            if (!Double.isNaN(averagedAzimuth[i])) {
                azim_min = averagedAzimuth[i];
                indexMinAzimAngle = i;
                break;
            }

        }
        azim_max = Float.NaN;
        for (i = averagedAzimuth.length - 1; i > 0; i--) {
            // max
            if (!Double.isNaN(averagedAzimuth[i])) {
                azim_max = averagedAzimuth[i];
                indexMaxAzimAngle = i;
                break;
            }
        }

        // compute min & max zenithal angle
        resetAzimuthRange();
        averagedZenith = new double[header.getNZenith()];
        for (i = 0; i < header.getNZenith(); i++) {

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

        zenith_min = Float.NaN;
        for (i = 0; i < averagedZenith.length; i++) {
            // min
            if (!Double.isNaN(averagedZenith[i])) {
                zenith_min = averagedZenith[i];
                indexMinZenithAngle = i;
                break;
            }

        }
        zenith_max = Float.NaN;
        for (i = averagedZenith.length - 1; i > 0; i--) {
            // max
            if (!Double.isNaN(averagedZenith[i])) {
                zenith_max = averagedZenith[i];
                indexMaxZenithAngle = i;
                break;
            }
        }

        resetZenithRange();
    }

    /**
     * Compute azimutal step angle from the extremums angles.
     * <p>
     * If the extremums are unknown, then the method
     * {@link #computeMinMaxAngles() computeExtremumsAngles()} will be
     * called.</p>
     */
    protected void computeAzimutalStepAngle() {

        if (Double.isNaN(azim_min) || Double.isNaN(azim_max)) {
            computeMinMaxAngles();
        }

        //azimutalStepAngle = (Math.abs(azim_min)-Math.abs(azim_max))/(double)(colIndexAzimMax - colIndexAzimMin);
        double fov = azim_min - azim_max;
        if (Math.abs(azim_max - azim_min) < 0.1) {
            fov += (Math.PI * 2);
        }

        azim_delta = fov / (double) (indexMaxAzimAngle - indexMinAzimAngle);
    }

    /**
     * Compute zenithal step angle from the extremums angles.
     * <p>
     * If the extremums are unknown, then the method
     * {@link #computeMinMaxAngles() computeExtremumsAngles()} will be
     * called.</p>
     */
    protected void computeZenithalStepAngle() {

        if (Double.isNaN(zenith_min) || Double.isNaN(zenith_max)) {
            computeMinMaxAngles();
        }

        double fov = zenith_min - zenith_max;
        zenith_delta = fov / ((double) indexMaxZenithAngle - indexMinZenithAngle);
    }

    /**
     * Reset the zenithal range to default values.
     * <p>
     * The values can have been modified by a call to the following methods
     * :</p>
     * <ul>
     * <li>{@link #setZenithIndex(int) setZenithIndex(int)}</li>
     * <li>{@link #setZenithRange(int, int) setZenithRange(int, int)}</li>
     * </ul>
     */
    public void resetZenithRange() {
        setZenithRange(0, header.getNZenith() - 1);
    }

    /**
     * Reset the azimuthal range to default values.
     * <p>
     * The values can have been modified by a call to the following methods
     * :</p>
     * <ul>
     * <li>{@link #setAzimuthIndex(int) setAzimuthIndex(int)}</li>
     * <li>{@link #setAzimuthRange(int, int) setAzimuthRange(int, int)}</li>
     * </ul>
     */
    public void resetAzimuthRange() {

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
        this.startAzimuthIndex = azimuthIndex;
        this.endAzimuthIndex = azimuthIndex;
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
        this.startZenithIndex = zenithIndex;
        this.endZenithIndex = zenithIndex;
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
        this.startZenithIndex = zenithIndex1;
        this.endZenithIndex = zenithIndex2;
    }

    /**
     * Are invalid points returned ?
     * <p>
     * An invalid point is a point without a position because the laser shot
     * didn't get a return.</p>
     *
     * @return true if invalid points are returned, false otherwise
     */
    public boolean isReturnInvalidPoint() {
        return returnMissingPoint;
    }

    /**
     * Set if invalid points are returned or not.
     * <p>
     * An invalid point is a point without a position because the laser shot
     * didn't get a return.</p>
     *
     * @param returnInvalidPoint true if invalid point should be returned, false
     * otherwise
     */
    public void setReturnInvalidPoint(boolean returnInvalidPoint) {
        this.returnMissingPoint = returnInvalidPoint;
    }

    public double[] getAveragedZenith() {
        return averagedZenith;
    }

    public double[] getAveragedAzimuth() {
        return averagedAzimuth;
    }

    /**
     * Get azimuthal index corresponding to the minimal azimuthal angle.
     * <p>
     * The minimum azimuthal angle is obtained from the first non empty vertical
     * sweep.</p>
     *
     * @return an azimuthal index corresponding to the minimal azimuthal angle.
     */
    public int getIndexAzimMin() {
        return indexMinAzimAngle;
    }

    /**
     * Get zenithal index corresponding to the minimal zenithal angle.
     * <p>
     * The minimum zenithal angle is obtained from the first non empty
     * horizontal rotation.</p>
     *
     * @return a zenithal index corresponding to the minimal zenithal angle.
     */
    public int getIndexZenithMin() {
        return indexMinZenithAngle;
    }

    /**
     * Get zenithal index corresponding to the maximum zenithal angle.
     * <p>
     * The maximum zenithal angle is obtained from the last non empty horizontal
     * rotation.</p>
     *
     * @return a zenithal index corresponding to the maximum zenithal angle.
     */
    public int getIndexZenithMax() {
        return indexMaxZenithAngle;
    }

    /**
     * Get azimuthal index corresponding to the maximum azimuthal angle.
     * <p>
     * The maximum azimuthal angle is obtained from the last non empty vertical
     * sweep.</p>
     *
     * @return an azimuthal index corresponding to the maximum azimuthal angle.
     */
    public int getIndexAzimMax() {
        return indexMaxAzimAngle;
    }

    /**
     * Get the first zenith index to read from the file. The default value is 0.
     *
     * @return the start zenith index
     */
    public int getStartZenithIndex() {
        return startZenithIndex;
    }

    /**
     * Get the first azimuth index to read from the file. The default value is
     * 0.
     *
     * @return the first zenith index
     */
    public int getStartAzimuthIndex() {
        return startAzimuthIndex;
    }

    /**
     * Get the last zenith index to read from the file. The default value is
     * (number of horizontal rotations) - 1.
     *
     * @return the last zenith index
     */
    public int getEndZenithIndex() {
        return endZenithIndex;
    }

    /**
     * Get the last azimuth index to read from the file. The default value is
     * (number of vertical sweeps) - 1.
     *
     * @return the last azimuth index
     */
    public int getEndAzimuthIndex() {
        return endAzimuthIndex;
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
     * Get the minimum azimuthal angle.
     * <p>
     * If min/max angles are unknown, then the method
     * {@link #computeMinMaxAngles()} will be called.</p>
     *
     * @return The minimum azimuthal angle in radians
     */
    public double getAzimMin() {

        if (Double.isNaN(azim_min)) {
            computeMinMaxAngles();
        }

        return azim_min;
    }

    /**
     * Get the maximum azimuthal angle.
     * <p>
     * If min/max angles are unknown, then the method
     * {@link #computeMinMaxAngles()} will be called.</p>
     *
     * @return The maximum azimuthal angle in radians
     */
    public double getAzimMax() {

        if (Double.isNaN(azim_max)) {
            computeMinMaxAngles();
        }

        return azim_max;
    }

    /**
     * Get the minimum zenithal angle.
     * <p>
     * If min/max angles are unknown, then the method
     * {@link #computeMinMaxAngles()} will be called.</p>
     *
     * @return The minimum zenithal angle in radians
     */
    public double getZenithMin() {

        if (Double.isNaN(zenith_min)) {
            computeMinMaxAngles();
        }
        return zenith_min;
    }

    /**
     * Get the maximum zenithal angle.
     * <p>
     * If min/max angles are unknown, then the method
     * {@link #computeMinMaxAngles()} will be called.</p>
     *
     * @return The maximum zenithal angle in radians
     */
    public double getZenithMax() {
        return zenith_max;
    }

    /**
     * Get the azimuthal step angle.
     * <p>
     * If min/max angles are unknown, then the method
     * {@link #computeMinMaxAngles()} will be called.</p>
     *
     * @return The azimuthal step angle in radians
     */
    public double getAzimuthalStepAngle() {

        if (Double.isNaN(azim_delta)) {
            computeAzimutalStepAngle();
        }

        return azim_delta;
    }

    /**
     * Get the zenithal step angle.
     * <p>
     * If min/max angles are unknown, then the method
     * {@link #computeMinMaxAngles()} will be called.</p> F
     *
     * @return The zenithal step angle in radians
     */
    public double getZenithalStepAngle() {

        if (Double.isNaN(zenith_delta)) {
            computeZenithalStepAngle();
        }

        return zenith_delta;
    }

//    public int getCurrentColIndex() {
//        return currentColIndex;
//    }
}
