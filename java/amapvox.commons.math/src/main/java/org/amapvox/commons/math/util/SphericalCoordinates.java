/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */
package org.amapvox.commons.math.util;

import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple3f;

/**
 *
 * @author calcul
 */
public class SphericalCoordinates {

    private final double azimut;
    private final double zenith;
    private final double radius;
    
    public SphericalCoordinates(double azimut, double zenith) {
        this(azimut, zenith, 1.d);
    }

    public SphericalCoordinates(double azimut, double zenith, double radius) {
        this.azimut = azimut;
        this.zenith = zenith;
        this.radius = radius;
    }

    public final static Point3d toCartesian(double azimut, double zenith, double radius) {

        return new Point3d(
                radius * Math.sin(zenith) * Math.cos(azimut),
                radius * Math.sin(zenith) * Math.sin(azimut),
                radius * Math.cos(zenith));
    }
    
    public final static Point3d toCartesian(double azimut, double zenith) {
        return toCartesian(azimut, zenith, 1.d);
    }
    
    public static final SphericalCoordinates fromCartesian(double x, double y, double z) {
        
        double radius = Math.sqrt((x * x) + (y * y) + (z * z));
        double azimut = Math.atan2(y, x);
        double zenith = Math.acos(z / radius);
        return new SphericalCoordinates(azimut, zenith, radius);
    }
    
    public static final SphericalCoordinates fromCartesian(Tuple3d point) {
        return fromCartesian(point.x, point.y, point.z);
    }
    
    public static final SphericalCoordinates fromCartesian(Tuple3f point) {
        return fromCartesian(point.x, point.y, point.z);
    }

    public double getAzimut() {
        return azimut;
    }

    public double getZenith() {
        return zenith;
    }

    public double getElevation() {
        return Math.PI / 2.0 - zenith;
    }

    public double getRadius() {
        return radius;
    }
}
