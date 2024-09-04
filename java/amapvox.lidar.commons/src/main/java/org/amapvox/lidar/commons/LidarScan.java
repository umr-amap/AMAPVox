/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.commons;

import org.amapvox.commons.util.filter.FloatFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Matrix4d;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class LidarScan {

    private final File file;
    private final Matrix4d matrix;
    private final List<FloatFilter> filters;
    private final String name;

    public LidarScan(File file, Matrix4d matrix, String name) {
        this.file = file;
        this.matrix = matrix;
        this.name = name;
        filters = new ArrayList();
    }
    
    public LidarScan(File file, Matrix4d matrix) {
        this(file, matrix, file.getName());
    }

    @Override
    public String toString() {
        return name + " (" + file.getAbsolutePath() + ")";
    }
    
    public void addFilter(FloatFilter filter) {
        filters.add(filter);
    }
     
    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public Matrix4d getMatrix() {
        return matrix;
    }
}
