package org.amapvox.commons.util;

import java.awt.Color;

/**
 * This class can be used to generate gradient from a color to another or to simulate a color ramp
 * @author took from jeeb\\lib\\util
 */
public class ColorGradient {

    /**
     * Produces a gradient using the University of Minnesota's school colors,
     * from maroon (low) to gold (high)
     */
    public final static Color[] GRADIENT_MAROON_TO_GOLD = createGradient(
                    new Color(0xA0, 0x00, 0x00), new Color(0xFF, 0xFF, 0x00), 500);

    /**
     * Produces a gradient from blue (low) to red (high)
     */
    public final static Color[] GRADIENT_BLUE_TO_RED = createGradient(
                    Color.BLUE, Color.RED, 500);

    /**
     * Produces a gradient from black (low) to white (high)
     */
    public final static Color[] GRADIENT_BLACK_TO_WHITE = createGradient(
                    Color.BLACK, Color.WHITE, 500);

    /**
     * Produces a gradient from white (low) to red (high)
     */
    public final static Color[] GRADIENT_WHITE_TO_BLACK = createGradient(
                    Color.WHITE, Color.BLACK, 500);

    /**
     * Produces a gradient from white (low) to red (high)
     */
    public final static Color[] GRADIENT_WHITE_TO_RED = createGradient(
                    Color.WHITE, Color.RED, 500);

    /**
     * Produces a gradient from red (low) to green (high)
     */
    public final static Color[] GRADIENT_RED_TO_GREEN = createGradient(
                    Color.RED, Color.GREEN, 500);

    /**
     * Produces a gradient through green, yellow, orange, red
     */
    public final static Color[] GRADIENT_GREEN_YELLOW_ORANGE_RED = createMultiGradient(
                    new Color[] { Color.green, Color.yellow, Color.orange, Color.red },
                    500);

    /**
     * Produces a gradient through the rainbow: violet, blue, green, yellow,
     * orange, red
     */
    public final static Color[] GRADIENT_RAINBOW = createMultiGradient(
                    new Color[] { new Color(181, 32, 255), Color.blue, Color.green,
                                    Color.yellow, Color.orange, Color.red }, 500);
    
    public final static Color[] GRADIENT_RAINBOW2 = createMultiGradient(
                    new Color[] { Color.blue, Color.green,
                                    Color.yellow, Color.orange, Color.red, Color.pink}, 500);
    
    public final static Color[] GRADIENT_RAINBOW3 = createMultiGradient(
                    new Color[] { Color.blue, Color.cyan, Color.green,
                                    Color.yellow, Color.orange, Color.red }, 500);

    /**
     * Produces a gradient for hot things (black, red, orange, yellow, white)
     */
    public final static Color[] GRADIENT_HOT = createMultiGradient(new Color[] {
                    Color.black, new Color(87, 0, 0), Color.red, Color.orange,
                    Color.yellow, Color.white }, 500);

    /**
     * Produces a different gradient for hot things (black, brown, orange,
     * white)
     */
    public final static Color[] GRADIENT_HEAT = createMultiGradient(
                    new Color[] { Color.black, new Color(105, 0, 0),
                                    new Color(192, 23, 0), new Color(255, 150, 38), Color.white },
                    500);

    /**
     * Produces a gradient through red, orange, yellow
     */
    public final static Color[] GRADIENT_ROY = createMultiGradient(new Color[] {
                    Color.red, Color.orange, Color.yellow }, 500);

    
    
    public final static Color[] GRADIENT_TREE = createMultiGradient(
                    new Color[] {  new Color(125, 129, 0), new Color(90, 164, 0), new Color(173, 81, 0)}, 500);
    
    
    
    private float minValue = Float.MIN_VALUE;
    private float maxValue = Float.MAX_VALUE;
    private Color[] gradientColor;
    
    /**
     *
     * @param min
     * @param max
     */
    public ColorGradient (float min, float max) {
    	minValue = min;
    	maxValue = max;
    	gradientColor = GRADIENT_RAINBOW;
    }
    
    /**
     *
     * @param minValue
     */
    public void setMinValue (float minValue) {
		this.minValue = minValue;
	}
	
    /**
     *
     * @param maxValue
     */
    public void setMaxValue (float maxValue) {
		this.maxValue = maxValue;
	}
    
    /**
     *
     * @param value
     * @return
     */
    public Color getColor(float value) {
    	
        float colorIdx = ((value - minValue) / (maxValue-minValue));  
    	
    	int index = (int) ((gradientColor.length-1)*colorIdx); 
    	if(index<0) {
    		index = 0;
    	}else if (index>=gradientColor.length) {
    		index = gradientColor.length-1;
    	}
    	return gradientColor[index];
    }
    
    /**
     *
     * @param gradientColor
     */
    public void setGradientColor(Color[] gradientColor) {
        this.gradientColor = gradientColor;
    }
    
    
    /**
     * Creates an array of Color objects for use as a gradient, using a linear
     * interpolation between the two specified colors.
     *
     * @param one
     *            Color used for the bottom of the gradient
     * @param two
     *            Color used for the top of the gradient
     * @param numSteps
     *            The number of steps in the gradient. 250 is a good number.
     * @return 
     */
    public static Color[] createGradient(Color one, Color two, int numSteps) {
            int r1 = one.getRed();
            int g1 = one.getGreen();
            int b1 = one.getBlue();

            int r2 = two.getRed();
            int g2 = two.getGreen();
            int b2 = two.getBlue();

            int newR = 0;
            int newG = 0;
            int newB = 0;

            Color[] gradient = new Color[numSteps];
            double iNorm;
            for (int i = 0; i < numSteps; i++) {
                    iNorm = i / (double) numSteps; // a normalized [0:1] variable
                    newR = (int) (r1 + iNorm * (r2 - r1));
                    newG = (int) (g1 + iNorm * (g2 - g1));
                    newB = (int) (b1 + iNorm * (b2 - b1));
                    gradient[i] = new Color(newR, newG, newB);
            }

            return gradient;
    }

    /**
     * Creates an array of Color objects for use as a gradient, using an array
     * of Color objects. It uses a linear interpolation between each pair of
     * points.
     *
     * @param colors
     *            An array of Color objects used for the gradient. The Color at
     *            index 0 will be the lowest color.
     * @param numSteps
     *            The number of steps in the gradient. 250 is a good number.
     * @return 
     */
    public static Color[] createMultiGradient(Color[] colors, int numSteps) {
            // we assume a linear gradient, with equal spacing between colors
            // The final gradient will be made up of n 'sections', where n =
            // colors.length - 1
            int numSections = colors.length - 1;
            int gradientIndex = 0; // points to the next open spot in the final
                                                            // gradient
            Color[] gradient = new Color[numSteps];
            Color[] temp;

            if (numSections <= 0) {
                    throw new IllegalArgumentException(
                                    "You must pass in at least 2 colors in the array!");
            }

            for (int section = 0; section < numSections; section++) {
                    // we divide the gradient into (n - 1) sections, and do a regular
                    // gradient for each
                    temp = createGradient(colors[section], colors[section + 1],
                                    numSteps / numSections);
                    for (int i = 0; i < temp.length; i++) {
                            // copy the sub-gradient into the overall gradient
                            gradient[gradientIndex++] = temp[i];
                    }
            }

            if (gradientIndex < numSteps) {
                    // The rounding didn't work out in our favor, and there is at least
                    // one unfilled slot in the gradient[] array.
                    // We can just copy the final color there
                    for (/* nothing to initialize */; gradientIndex < numSteps; gradientIndex++) {
                            gradient[gradientIndex] = colors[colors.length - 1];
                    }
            }

            return gradient;
    }



}
