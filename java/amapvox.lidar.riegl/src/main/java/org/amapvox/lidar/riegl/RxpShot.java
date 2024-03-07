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
package org.amapvox.lidar.riegl;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class RxpShot {

    public int nEcho;
    public double time;
    public Point3d origin;
    public Vector3d direction;
    public double ranges[] = null;
    public float intensities[];
    public int classifications[];

    /**
     * This is the ratio of the received power to the power that would be
     * received from a white diffuse target at the same distance expressed in
     * dB. The reflectance represents a range independent property of the
     * target. The surface normal of this target is assumed to be in parallel to
     * the laser beam direction.
     */
    public float reflectances[];
    public float deviations[];
    public float amplitudes[];
    public double times[];

    /**
     * Optional echoes attributes
     */
    public double[][] echoesAttributes;

    public double angle;

    public RxpShot(int nEcho, Point3d origin, Vector3d direction, double[] ranges, int[] classifications, float[] intensities) {

        this.origin = origin;
        this.nEcho = nEcho;
        this.direction = direction;
        this.ranges = ranges;
        this.classifications = classifications;
        this.intensities = intensities;
    }

    public RxpShot(int nbEchos, Point3d origin, Vector3d direction, double[] ranges) {
        this(nbEchos, origin, direction, ranges, null, null);
    }

    public RxpShot(int nEcho, double time,
            double originX, double originY, double originZ,
            double directionX, double directionY, double directionZ,
            double[] ranges) {

        this.nEcho = nEcho;
        this.time = time;

        this.origin = new Point3d(originX, originY, originZ);
        this.direction = new Vector3d(directionX, directionY, directionZ);

        this.ranges = ranges;
    }

    /**
     * Copy constructor.
     *
     * @param shot The shot object to make a copy from.
     */
    public RxpShot(RxpShot shot) {

        this.origin = new Point3d(shot.origin);
        this.direction = new Vector3d(shot.direction);
        this.angle = shot.angle;
        this.nEcho = shot.nEcho;
        this.time = shot.time;

        if (shot.ranges != null) {
            this.ranges = new double[shot.ranges.length];
            System.arraycopy(shot.ranges, 0, this.ranges, 0, this.ranges.length);
        }

        if (shot.times != null) {
            this.times = new double[shot.times.length];
            System.arraycopy(shot.times, 0, this.times, 0, this.times.length);
        }

        if (shot.amplitudes != null) {
            this.amplitudes = new float[shot.amplitudes.length];
            System.arraycopy(shot.amplitudes, 0, this.amplitudes, 0, this.amplitudes.length);
        }

        if (shot.classifications != null) {
            this.classifications = new int[shot.classifications.length];
            System.arraycopy(shot.classifications, 0, this.classifications, 0, this.classifications.length);
        }

        if (shot.deviations != null) {
            this.deviations = new float[shot.deviations.length];
            System.arraycopy(shot.deviations, 0, this.deviations, 0, this.deviations.length);
        }

        if (shot.intensities != null) {
            this.intensities = new float[shot.intensities.length];
            System.arraycopy(shot.intensities, 0, this.intensities, 0, this.intensities.length);
        }

        if (shot.reflectances != null) {
            this.reflectances = new float[shot.reflectances.length];
            System.arraycopy(shot.reflectances, 0, this.reflectances, 0, this.reflectances.length);
        }
    }

    public void setReflectances(float[] reflectances) {
        this.reflectances = reflectances;
    }

    public void setDeviations(float[] deviations) {
        this.deviations = deviations;
    }

    public void setAmplitudes(float[] amplitudes) {
        this.amplitudes = amplitudes;
    }

    public void setIntensities(float[] intensities) {
        this.intensities = intensities;
    }

    public void setTimes(double[] times) {
        this.times = times;
    }

    public void setOriginAndDirection(Point3d origin, Vector3d direction) {

        this.origin = new Point3d(origin);
        this.direction = new Vector3d(direction);

        calculateAngle();
    }

    public void calculateAngle() {
        this.angle = Math.toDegrees(Math.acos(direction.z));
    }

}
