package org.amapvox.canopy.mmr;

import org.amapvox.canopy.util.Circle;
import org.amapvox.commons.math.util.SphericalCoordinates;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * Sun position and splitting of direct radiation in turtle sectors // TODO move
 * in turtle methods
 *
 * @author J. Dauzat - May 2012
 */
public class Sun {

    /**
     * Sun zenith angle in radians
     */
    public float zenith;
    /**
     * PI/2 - zenith
     */
    public float elevation;
    /**
     * Sun azimuth in radians
     */
    public float azimuth;
    public Vector3f direction;

    /**
     * Constructor
     */
    public Sun() {
        direction = new Vector3f(new Point3f());
    }

    public static double sunsetHourAngle(float latitude, float declination) {
        double sunsetHourAngle = -Math.tan(latitude) * Math.tan(declination);
        sunsetHourAngle = Math.max(-1, sunsetHourAngle);
        sunsetHourAngle = Math.min(1, sunsetHourAngle);
        sunsetHourAngle = Math.acos(sunsetHourAngle);

        return sunsetHourAngle;
    }

    /**
     * Sun declination as a function of day
     *
     * @param doy: Day Of Year
     * @return sun declination [radian]
     */
    public static float declination(int doy) {
        double a, declin;
        a = Math.toRadians(0.9683 * doy - 78.00878);
        declin = (23.4856 * Math.sin(a));
        declin = Math.toRadians(declin);

        return (float) (declin);
    }

    /**
     * Calculates the True Solar Time
     *
     * @param doy Day Of Year
     * @return True Solar Time [decimal hour]
     */
    public static float timeEquation(int doy) {
        double a1 = Math.toRadians((1.00554 * doy) - 6.28306);
        double a2 = Math.toRadians((1.93946 * doy) + 23.35089);
        double tst = -7.67825 * Math.sin(a1) - 10.09176 * Math.sin(a2);

        return (float) (tst / 60);
    }

    /**
     * Computes sun elevation and azimuth
     *
     * @param latitudeRadian (in radians)
     * @param doy (day of year)
     * @param hourDecimal hourDecimal
     * @return boolean (true:day/false:night)
     */
    public boolean position(float latitudeRadian, int doy, float hourDecimal) {
        double declin = declination(doy);
        double et = timeEquation(doy);

        double ah = Math.toRadians((hourDecimal + et - 12) * 15);

        double amuzero = (Math.sin(latitudeRadian) * Math.sin(declin)) + (Math.cos(latitudeRadian) * Math.cos(declin) * Math.cos(ah));
        double el = Math.asin(amuzero);
        double cos_sun_el = Math.cos(el);

        double az = 0;
        double caz = 0;

        if (cos_sun_el != 0.) {
            az = Math.cos(declin) * Math.sin(ah) / cos_sun_el;
            caz = -Math.cos(latitudeRadian) * Math.sin(declin) + Math.sin(latitudeRadian) * Math.cos(declin) * Math.cos(ah);
            caz /= cos_sun_el;
        }
        az = Math.min(az, 0.9999);
        az = Math.max(az, -0.9999);
        az = Math.asin(az);
        if (caz <= 0.) {
            az = Math.PI - az;
        } else if (az < 0) {
            az += 2. * Math.PI;
        }
        az += Math.PI;
        if (az > 2. * Math.PI) {
            az -= 2. * Math.PI;
        }

        double zn = (Math.PI / 2.) - el;

        zenith = (float) zn;
        elevation = (float) el;
        azimuth = (float) ((Math.PI * 2) - az);

        direction = new Vector3f(SphericalCoordinates.toCartesian(azimuth, zenith));
        
        return zn <= Math.PI / 2.;
    }

    /**
     * Calculates the hours of sunrise and sunset for the given day and latitude
     *
     * @param latit latitude
     * @param DOY day of year
     * @return sunrise and sunset
     */
    public static Point2f sunriseSunsetHours(float latit, int DOY) {
        float declin = declination(DOY);

        // hour angle
        double cosha = -Math.tan(declin) * Math.tan(latit);
        cosha = Math.min(cosha, 1);
        cosha = Math.max(cosha, -1);
        float ha = (float) Math.acos(cosha);

        // Universal time
        float ut = (float) (ha / Math.toRadians(15));
        float tst = timeEquation(DOY);

        float sunRise = 12 - tst - ut;
        float sunSet = 12 - tst + ut;

        return new Point2f(sunRise, sunSet);
    }

    public static float[] directInTurtle(float direct, Vector3f sunDirection, Turtle turtle) { // TODO to be moved in Turtle class
        float[] weights = new float[turtle.directions.length];

        if (sunDirection.z < 0) {
            return weights;
        }

        float weightSum = 0;
        // plane angle corresponding to sector solid angle of 2PI/nb sectors
        // Note: solid angle W = 2PI (1-cos(angle/2)) => cos(angle/2)= (46-1) / 46
        float turtleSectorRadius = (float) Math.acos((turtle.getNbDirections() - 1) / (float) turtle.getNbDirections());
        float sunHaloRadius = turtleSectorRadius; // TODO create method setsunHaloRadius
        sunHaloRadius /= 2;
        // System.out.println ("turtle radius= "+Math.toDegrees(turtleSectorRadius));
        for (int dir = 0; dir < turtle.directions.length; dir++) {
            float angle = sunDirection.dot(turtle.directions[dir]);
            angle = (float) Math.acos(angle);
            weights[dir] = Circle.lumenArea(angle, turtleSectorRadius, sunHaloRadius);

            weightSum += weights[dir];
        }
        for (int dir = 0; dir < turtle.directions.length; dir++) {
            
            if(weightSum == 0){
                weights[dir] = 0;
            }else{
                weights[dir] *= direct / weightSum;
                weights[dir] += 0.0f;
            }
            
        }

        return weights;
    }

    public static void sunPathImage(int doy, float latitude, float decimalHour1, float decimalHour2) {

        int nbPixels = 600;
        int border = 30;
        float center = nbPixels / 2;
        float radius = center - border;

        float latitudeRadian = (float) Math.toRadians(latitude);

        BufferedImage bimg = new BufferedImage(nbPixels, nbPixels, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bimg.createGraphics();

        // background
        g.setColor(new Color(80, 30, 0));
        g.fillRect(0, 0, nbPixels, nbPixels);

        // blue sky vault
        g.setColor(new Color(150, 220, 255));
        g.fillOval((int) (center - radius), (int) (center - radius), (int) (2 * radius), (int) (2 * radius));

        // parallels
        g.setColor(new Color(220, 240, 255));
        float rad = radius / 6;
        for (int i = 1; i <= 6; i++) {
            g.drawOval((int) (center - rad * i), (int) (center - rad * i), (int) (2 * rad * i), (int) (2 * rad * i));
        }
        // meridians
        for (int i = 1; i <= 6; i++) {
            double azimuth = i * (Math.PI / 6);
            double x = radius * Math.sin(azimuth);
            double y = radius * Math.cos(azimuth);
            g.drawLine((int) (center - x), (int) (center - y), (int) (center + x), (int) (center + y));
        }

        // cardinal points
        g.drawString("N", (int) center, border / 2);
        g.drawString("S", (int) center, nbPixels - (border / 2));
        g.drawString("E", nbPixels - (border / 2), center);
        g.drawString("W", border / 2, center);

        // sun position
        Sun sun = new Sun();
        int sunRadius = nbPixels / 100;
//		for (float h= 0; h<24; h+=0.5) {
        for (float h = decimalHour1; h < decimalHour2; h += 0.5) {
            boolean vis = sun.position(latitudeRadian, doy, h);
            System.out.println(Math.toDegrees(sun.azimuth) + "\t");
            double z = radius * sun.zenith / (Math.PI / 2);
            double x = -Math.sin(sun.azimuth) * z;
            double y = Math.cos(sun.azimuth) * z;
            x += center;
            y += center;

            if (vis) {
                g.setColor(new Color(255, 225, 100));
                g.fillOval((int) (x - sunRadius), (int) (nbPixels - (y - sunRadius) - 1), 2 * sunRadius, 2 * sunRadius);

                if (h % 1 == 0f) {
                    g.setColor(new Color(0, 0, 0));
                    g.drawString(String.valueOf((int) h) + "h", (int) (x - sunRadius), (int) (nbPixels - (y - sunRadius) - 1));
                }
            }
        }

        //ImageView view= new ImageView (bimg);
        //view.view(new JDialog());
    }

    public static void main(String[] args) {

        int DOY = 90;
        int latitude = 45;
        float latitudeRadian = (float) Math.toRadians(latitude);

        float decimalHour1 = 0;
        float decimalHour2 = 24;

        sunPathImage(DOY, latitudeRadian, decimalHour1, decimalHour2);

    }

}
