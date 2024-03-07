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

package org.amapvox.commons.raster.multiband;

import org.amapvox.commons.raster.multiband.BCommon.NumberOfBits;
import static org.amapvox.commons.raster.multiband.BCommon.encodebool;



/**
 *
 * @author calcul
 */


public class Band {
    
    
    private final boolean[][][] data;
    private final int width;
    private final int height;
    private final short numberOfBits;
    
    public Band(int width, int height, NumberOfBits numberOfBits){
        
        this.numberOfBits = numberOfBits.getNumberOfBits();
        this.width = width;
        this.height = height;
        
        data = new boolean[height][width][this.numberOfBits];
    }
    
    public void setPixel(int posX, int posY, boolean[] color){
        
        if(posX < 0 || posX > width-1 || posY < 0 || posY > height-1){
            
            //logger.warn("Cannot set pixel to position "+posX+","+posY +" ; band size (width/height): "+width+"/"+height);
            return;
        }
        
        data[posY][posX] = color;
    }
    
    public byte[] getRow(int rowID){
        
        byte[] row = new byte[(width*numberOfBits)/8];
        
        int count = 0;
        int currentNumberOfBits = 0;
        boolean[] byteOfBoolean = new boolean[8];
        
        for (boolean[] bits : data[rowID]) {
                        
            for(boolean bit : bits){
                                
                byteOfBoolean[currentNumberOfBits] = bit;
                
                currentNumberOfBits++;
                
                if(currentNumberOfBits == 8){
                    
                    row[count] = encodebool(byteOfBoolean);
                    count++;
                    byteOfBoolean = new boolean[8];
                    currentNumberOfBits = 0;
                }
            }
        }
        
        return row;
    }
}
