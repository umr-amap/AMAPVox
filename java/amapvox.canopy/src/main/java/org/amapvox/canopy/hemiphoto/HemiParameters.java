/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.canopy.hemiphoto;

import java.io.File;
import java.util.List;
import javax.vecmath.Point3d;
import org.amapvox.canopy.LeafAngleDistribution;

/**
 *
 * @author calcul
 */
public class HemiParameters {
    
    //PAD mode
    private File voxelFile;
    private String padVariable;
    private LeafAngleDistribution.Type leafAngleDistribution;
    private double[] leafAngleDistributionParameters = new double[2];
    private List<Point3d> sensorPositions;
    
    //common parameters
    private int pixelNumber;
    private int azimutsNumber = 36;
    private int zenithsNumber = 9;
    
    //output
    private File outputDir;
    private String outputPrefix;

    public File getVoxelFile() {
        return voxelFile;
    }

    public void setVoxelFile(File voxelFile) {
        this.voxelFile = voxelFile;
    }
    
    public String getPADVariable() {
        return padVariable;
    }
    
    public void setPADVariable(String padVariable) {
        this.padVariable = padVariable;
    }

    public List<Point3d> getSensorPositions() {
        return sensorPositions;
    }

    public void setSensorPositions(List<Point3d> sensorPositions) {
        this.sensorPositions = sensorPositions;
    }

    public int getPixelNumber() {
        return pixelNumber;
    }

    public void setPixelNumber(int pixelNumber) {
        this.pixelNumber = pixelNumber;
    }

    public int getAzimutsNumber() {
        return azimutsNumber;
    }

    public void setAzimutsNumber(int azimutsNumber) {
        this.azimutsNumber = azimutsNumber;
    }

    public int getZenithsNumber() {
        return zenithsNumber;
    }

    public void setZenithsNumber(int zenithsNumber) {
        this.zenithsNumber = zenithsNumber;
    }

    public File getOutputDirectory() {
        return outputDir;
    }

    public void setOutputDirectory(File directory) {
        this.outputDir = directory;
    }
    
    public String getOutputPrefix() {
        return outputPrefix;
    }
    
    public void setOutputPrefix(String prefix) {
        this.outputPrefix = prefix;
    }
    
    /**
     * @return the leafAngleDistribution
     */
    public LeafAngleDistribution.Type getLeafAngleDistribution() {
        return leafAngleDistribution;
    }

    /**
     * @param leafAngleDistribution the leafAngleDistribution to set
     */
    public void setLeafAngleDistribution(LeafAngleDistribution.Type leafAngleDistribution) {
        this.leafAngleDistribution = leafAngleDistribution;
    }
    
    /**
     * @return the leafAngleDistribution parameters
     */
    public double[] getLeafAngleDistributionParameters() {
        return leafAngleDistributionParameters;
    }

    /**
     * @param leafAngleDistributionParameters
     */
    public void setLeafAngleDistributionParameters(double[] leafAngleDistributionParameters) {
        this.leafAngleDistributionParameters = leafAngleDistributionParameters;
    }
}
