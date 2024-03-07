/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util.image;

import org.amapvox.commons.util.ColorGradient;
import org.amapvox.commons.util.DecimalScientificFormat;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class ScaleGradient {
    
    /**
     * Gradient orientation, can be either HORIZONTAL or VERTICAL
     */
    public enum Orientation{
        
        /**
         * horizontal drawing
         */
        HORIZONTAL(1),

        /**
         * vertical drawing
         */
        VERTICAL(2);
        
        private int value;

        private Orientation(int value) {
            this.value = value;
        }
    }
    
    /**
     * @see fr.umramap.commons.util.ColorGradient
     * @see fr.amap.commons.util.image.ScaleGradient.Orientation
     * @param gradientColor array of color values
     * @param min min value
     * @param max max value
     * @param width output image width
     * @param height output image height
     * @param orientation gradient orientation
     * @return a buffered image filled with gradient
     */
    public static BufferedImage generateColorGradientImage(Color[] gradientColor, float min, float max, int width, int height, Orientation orientation){
        
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        ColorGradient gradient = new ColorGradient(min, max);
        gradient.setGradientColor(gradientColor);
        
        switch(orientation){
            case VERTICAL:
                for (int i = 0; i < width; i++) {
                    for (int j = 0, k = height-1; j < height; j++,  k--) {
                        float value = (((max-min)*j)/height);
                        Color color = gradient.getColor(value);
                        bi.setRGB(i, k, color.getRGB());
                    }
                }

                break;
            case HORIZONTAL:
                
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        //float value = (((max-min)*j)/width);
                        float value = (j/(float)width)*(max-min)+min;
                        Color color = gradient.getColor(value);
                        bi.setRGB(j, i, color.getRGB());
                    }
                }

                break;
        }
        
        return bi;
    }
    
    /**
     * @see fr.umramap.commons.util.ColorGradient
     * @see fr.amap.commons.util.image.ScaleGradient.Orientation
     * @param gradientColor array of color values
     * @param minValue min scale value
     * @param maxValue max scale value
     * @param width output image width
     * @param height output image height
     * @param orientation gradient orientation
     * @param majorTickNumber major tick number to draw into image, major ticks have values associated
     * @param minorTickNumber minor tick number to draw into image between major ticks
     * @return a buffered image filled with gradient, with ticks and values (with intermediates values) drawn into
     */
    public static BufferedImage createColorScaleBufferedImage(Color[] gradientColor, float minValue, float maxValue, int width, int height, Orientation orientation, int majorTickNumber, int minorTickNumber){
        
        if(majorTickNumber < 2){
            majorTickNumber = 2;
        }
        
        BufferedImage image = generateColorGradientImage(gradientColor, minValue, maxValue, width, height, orientation);
        
        DecimalScientificFormat format = new DecimalScientificFormat();
        
        int borderX = 60;
        int borderY = 30;
        
        //calcul de la marge
        Font font = new Font("Comic Sans MS",Font.PLAIN,20);
        Canvas c = new Canvas();
        FontMetrics fm = c.getFontMetrics(font);
        int minValueTextWidth = fm.stringWidth(format.format(minValue));
        int maxValueTextWidth = fm.stringWidth(format.format(maxValue));
        
        borderX = Integer.max(minValueTextWidth, maxValueTextWidth);
        
        /***Génération de l'image avec BufferedImage***/
        BufferedImage imageWithTextcaption = new BufferedImage(image.getWidth()+borderX, image.getHeight()+borderY, image.getType());
        Graphics2D graphics = (Graphics2D)imageWithTextcaption.createGraphics();
        
        
        //FontMetrics fm = graphics.getFontMetrics();
        graphics.setFont(font);  
        
        int leftXMargin = borderX/2;
        graphics.drawImage(image, leftXMargin, 0, null);
        graphics.setPaint(Color.BLACK);
        
        
        //calcul des valeurs intermédiaires
        float step = (maxValue - minValue)/(float)(majorTickNumber-1);
        
        float[] tickValues = new float[majorTickNumber];
        tickValues[0] = minValue;
        tickValues[majorTickNumber-1] = maxValue;
        
        float currentValue = minValue+step;
        for(int i=1;i<majorTickNumber-1;i++){
            
            tickValues[i] = currentValue;
            currentValue += step;
        }
            
        //création du texte associé aux valeurs des ticks
        int y = imageWithTextcaption.getHeight();
        
        //génération des ticks (sous forme de lignes)
        float majorTickSpace = (image.getWidth())/(float)(majorTickNumber-1);
        
        float currentTickRectXOffset = 0 + leftXMargin;
        
        int majorTickWidth = 1;
        int majorTickHeight = (int) (image.getHeight()*0.75f);
        int majorTickPosY = image.getHeight() - majorTickHeight;
        
        int minorTickWidth = 1;
        int minorTickHeight = (int) (image.getHeight()*0.5f);
        int minorTickPosY = image.getHeight() - minorTickHeight;
        
        float minorTickSpace = (majorTickSpace) / (float)(minorTickNumber+1);
        
        for(int i=0;i<majorTickNumber;i++){
            
            graphics.fillRect((int)currentTickRectXOffset, majorTickPosY, majorTickWidth, majorTickHeight);
            
            if(i<majorTickNumber -1){
                
                for(int j=0;j<minorTickNumber;j++){

                    currentTickRectXOffset += minorTickSpace;
                    graphics.fillRect((int)currentTickRectXOffset, minorTickPosY, minorTickWidth, minorTickHeight);
                }

                currentTickRectXOffset += minorTickSpace;
            }
            
        }
        
        //scale border
        graphics.drawRect(leftXMargin, 0, image.getWidth(), image.getHeight());
        
        //scale labels (values)
        currentTickRectXOffset = 0 + leftXMargin;
        
        if(minValue == maxValue){ //display on value at center
                        
            String text = format.format(minValue);
            int textWidth = fm.stringWidth(text);
            int x = (int) ((currentTickRectXOffset+(width/2.0)) - ((int) (textWidth/2.0f)));

            graphics.drawString(text, x, y);
            
        }else{
            
            for(int i=0;i<majorTickNumber;i++){
            
                String text = format.format(tickValues[i]);
                int textWidth = fm.stringWidth(text);
                int x = (int) (currentTickRectXOffset - ((int) (textWidth/2.0f)));

                graphics.drawString(text, x, y);

                currentTickRectXOffset += majorTickSpace;
            }
        }
        
        return imageWithTextcaption;
    }
}
