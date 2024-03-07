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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author calcul
 */


public class BSQ extends BRaster{

    public BSQ(File outputFile, BHeader header) {
        super(outputFile, header);
    }

    @Override
    public File writeImage() throws FileNotFoundException, IOException {
        
        try (DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))){
            
            for(Band band : bands){
                
                for(int y=0;y<header.getNrows();y++){
                    byte[] bytes = band.getRow(y);
                    writer.write(bytes);
                }
            }
            
        }catch (FileNotFoundException ex) {
            throw new FileNotFoundException("File "+outputFile.getAbsolutePath()+" not found");
        } catch (IOException ex) {
            throw new IOException("An error occured during writing of file "+outputFile.getAbsolutePath(), ex);
        }
        
        return outputFile;
    }

    @Override
    public void writeColorFile() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void writeStatisticsFile() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
