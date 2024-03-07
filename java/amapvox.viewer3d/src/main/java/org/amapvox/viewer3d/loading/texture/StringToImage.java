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
package org.amapvox.viewer3d.loading.texture;

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author Julien Heurtebize
 */
public class StringToImage {
    
    private Font font;
    private Font defaultFont;
    private Color backgroundColor;
    private Color textColor;
    private Graphics2D graphics;
    private BufferedImage image;
    private int width;
    private int height;
    private boolean adaptableFontSize;
    private Canvas canvas;
    private String text;
    private int xCoordinate;
    

    public static int getMaxFittingFontSize(Graphics g, Font font, String string, int nbLines, int width, int height) {
        
        int minSize = 0;
        int maxSize = 288;
        int curSize = font.getSize();

        while (maxSize - minSize > 2) {
            FontMetrics fm = g.getFontMetrics(new Font(font.getName(), font.getStyle(), curSize));
            int fontWidth = fm.stringWidth(string);
            int fontHeight = fm.getLeading() + fm.getMaxAscent() + fm.getMaxDescent();

            if ((fontWidth > width) || ((fontHeight*nbLines) > height)) {
                maxSize = curSize;
                curSize = (maxSize + minSize) / 2;
            } else {
                minSize = curSize;
                curSize = (minSize + maxSize) / 2;
            }
        }

        return curSize;
    }
    
    public StringToImage(int width, int height) {
        
        this.textColor = new Color(0, 0, 0, 255);
        this.backgroundColor = new Color(0, 0, 0, 0);
        this.width = width;
        this.height = height;
        
        
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        graphics = (Graphics2D)image.createGraphics();

        setFont(new Font("Courier", Font.BOLD, 40));

        canvas = new Canvas();
    }
    
    public BufferedImage buildImage(){
        
        //fill with opaque color
        graphics.setPaint(new Color(255, 255, 255, 255));
        graphics.fillRect(0, 0, width, height);

        //fill with transparency (or not)
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
        graphics.setPaint(backgroundColor);
        graphics.fillRect(0, 0, width, height);
        
        //draw strings
        FontMetrics fontMetrics = canvas.getFontMetrics(font);
        Rectangle2D stringBounds = fontMetrics.getStringBounds("Hello world", graphics);
        
        int textHeight = (int) stringBounds.getHeight();
        int startY = textHeight;
        
        String[] lines = text.split("\n");
        
        graphics.setPaint(textColor);
        
        for(String line : lines){
            graphics.drawString(line, xCoordinate, startY);
            startY += textHeight;
        }
        
        return image;
    }
    
    public void setFont(Font font){
        this.font = font;
        defaultFont = new Font(font.getName(), font.getStyle(), font.getSize());
    }
    
    public void setBackgroundColor(Color color){
        this.backgroundColor = color;
    }
    
    public void setTextColor(Color textColor){
        this.textColor = textColor;
    }
    
    public void setText(String text, int xCoordinate, int yCoordinate){
        
        this.text = text;
        this.xCoordinate = xCoordinate;
        
        graphics.setBackground(backgroundColor);
        
        String[] lines = text.split("\n");
        
        String largestLine = "";
        
        for(String line : lines){
            if(line.length() > largestLine.length()){
                largestLine = line;
            }
        }
        
        if(adaptableFontSize){
            
            int maxFittingFontSize = getMaxFittingFontSize(graphics, defaultFont, largestLine, lines.length, width, height);

            font = new Font(font.getName(), font.getStyle(), maxFittingFontSize);
            
        }
        
        
        
        graphics.setFont(font);
    }

    /**
     * Is the font size re-computed to fit the size of the texture.
     * @return 
     */
    public boolean isAdaptableFontSize() {
        return adaptableFontSize;
    }

    /**
     * Re-compute the font size to fit the size of the texture.
     * @param adaptableFontSize 
     */
    public void setAdaptableFontSize(boolean adaptableFontSize) {
        this.adaptableFontSize = adaptableFontSize;
    }
    
}
