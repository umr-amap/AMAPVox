/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.faro;

import org.amapvox.lidar.gridded.GriddedPointScan;
import org.amapvox.lidar.gridded.LPoint;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author pverley
 */
public class XYBScan extends GriddedPointScan {
    
    public XYBScan(File file) {
        super(file);
        this.returnMissingPoint = false;
    }
    
    @Override
    public void open() throws IOException {
        readHeader();
    }
    
    @Override
    public void readHeader() throws FileNotFoundException, IOException {
        
        XYBIterator xybIterator = new XYBIterator(getFile());
        this.header = xybIterator.getHeader();
    }

    @Override
    public void readPointCloud() throws FileNotFoundException, IOException {
        // nothing to do, file is read on the fly whith iterator
    }

    @Override
    public Iterator<LPoint> iterator() {

        try {
            return new XYBIterator(getFile());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
