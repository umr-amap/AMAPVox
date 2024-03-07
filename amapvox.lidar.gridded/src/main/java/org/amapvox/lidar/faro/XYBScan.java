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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pverley
 */
public class XYBScan extends GriddedPointScan {
    
    public XYBScan() {
        this.returnInvalidPoint = false;
    }

    @Override
    public void openScanFile(File file) throws FileNotFoundException, IOException {
        this.file = file;
        try (XYBIterator xybIterator = new XYBIterator(this.file)) {
            this.header = xybIterator.getHeader();
            resetColumnLimits();
            resetRowLimits();
        } catch (Exception ex) {
            Logger.getLogger(XYBScan.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Iterator<LPoint> iterator() {

        try {
            return new XYBIterator(this.file);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
