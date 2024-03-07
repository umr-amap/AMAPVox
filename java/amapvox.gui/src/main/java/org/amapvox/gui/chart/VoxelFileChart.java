/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui.chart;

import java.awt.Color;
import org.amapvox.voxelfile.VoxelFileReader;
import java.io.File;

/**
 *
 * @author calcul
 */
public class VoxelFileChart{
        
    public File file;
    public VoxelFileReader reader;
    public boolean loaded;
    
    private final SeriesParameters seriesParameters;

    public VoxelFileChart(File file, String label, Color color) {
        this.file = file;
        this.loaded = false;
        this.seriesParameters = new SeriesParameters(label, color);
    }

    public SeriesParameters getSeriesParameters() {
        return seriesParameters;
    }

    @Override
    public String toString(){
        return file.toString();
    }
}