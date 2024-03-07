package org.amapvox.canopy.util;

import java.awt.Color;

import javax.vecmath.Point3f;
import javax.vecmath.Point3i;

/**
 * Class used to calculate rainbow colors
 *
 * @author J. Dauzat - April 2012
 */
public class Colouring {

    public static Color grey(float value) {
        int rgb = (int) (value * 255);
        return new Color(rgb, rgb, rgb);
    }

    public static Color blue(float value) {
        int b = (int) (value * 255);
        return new Color(0, 0, b);
    }

    /**
     * @param value in range (0-1)
     * @return Color from blue to green and green to red for increasing value
     */
    public static Color rainbow(float value) {
        Point3f rgb = rainbowRGB(value);

        if (rgb.x < 0 || rgb.x > 255) {
            System.out.println("Colouring: value: " + value + " red: " + rgb.x);
        }
        if (rgb.y < 0 || rgb.y > 255) {
            System.out.println("Colouring: value: " + value + " green: " + rgb.y);
        }
        if (rgb.z < 0 || rgb.z > 255) {
            System.out.println("Colouring: value: " + value + " blue: " + rgb.z);
        }

        return new Color((int) rgb.x, (int) rgb.y, (int) rgb.z);
    }

    /**
     * @param value in range (0-1)
     * @return rgb components (in range 0-255 for each) of
     * {@link #rainbow(float)}
     */
    public static Point3f rainbowRGB(float value) {
        
        float v = Math.min(1, value);

        int m4 = 1020; // 255 * 4
        if (v < 0) {
            return new Point3f(0, 0, 0);
        } else if (v <= 0.25f) {
            int red = 0;
            int green = (int) (m4 * v);
            int blue = 255;
            return new Point3f(red, green, blue);
        } else if (v <= 0.50f) {
            int red = 0;
            int green = 255;
            int blue = (int) (m4 * (0.5 - v));
            return new Point3f(red, green, blue);
        } else if (v <= 0.75f) {
            int red = (int) (m4 * (v - 0.5));
            int green = 255;
            int blue = 0;
            return new Point3f(red, green, blue);
        } else {
            int red = 255;
            int green = (int) (m4 * (1 - v));
            int blue = 0;
            return new Point3f(red, green, blue);
        }
    }

    /**
     * @param rainbowColor color (see {@link #rainbow(float)})
     * @return value in range (0,1)
     */
    public static float getValue(Color rainbowColor) {
        int r = rainbowColor.getRed();
        int g = rainbowColor.getGreen();
        int b = rainbowColor.getBlue();

        return getValue(r, g, b);
    }

    public static float getValue(float r, float g, float b) {
        Point3i rgb = new Point3i((int) r, (int) g, (int) b);
        int m4 = 1020; // 255 * 4
        float val = 0;

        if (rgb.z == 255) {
            return ((float) rgb.y / m4);
        }
        if (rgb.x == 0) {
            return (0.5f - ((float) rgb.y / m4));
        }
        if (rgb.y == 255) {
            return (0.5f + ((float) rgb.x / m4));
        }
        if (rgb.x == 255) {
            return 1f - ((float) rgb.y / m4);
        }

        return val;
    }
}
