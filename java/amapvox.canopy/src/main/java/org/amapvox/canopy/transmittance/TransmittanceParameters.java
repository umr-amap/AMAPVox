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

package org.amapvox.canopy.transmittance;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

/**
 *
 * @author Julien Heurtebize
 */


public class TransmittanceParameters {
    
    private File inputFile;
    private int directionsNumber;
    private float directionsRotation;
    private boolean toricity;
    
    //scanner positions
    private boolean useScanPositionsFile;
    private Point3f centerPoint;
    private float width;
    private float step;
    private List<Point3d> positions;
    
    private float latitudeInDegrees;
    
    private List<SimulationPeriod> simulationPeriods;
    private boolean generateTextFile;
    private boolean generateBitmapFile;
    private File textFile;
    private File bitmapFolder;
    
    private Mode mode;
    
    //lai2xxx specific
    private boolean[] masks; //ring masks
    private boolean generateLAI2xxxTypeFormat;
    
    public enum Mode{
        
        TRANSMITTANCE,
        LAI2000,
        LAI2200;
    } 

    public TransmittanceParameters(){
        simulationPeriods = new ArrayList<>();
        mode = Mode.TRANSMITTANCE;
        masks = new boolean[]{false, false, false, false, false};
    }
    
    public File getInputFile() {
        return inputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public int getDirectionsNumber() {
        return directionsNumber;
    }

    public void setDirectionsNumber(int directionsNumber) {
        this.directionsNumber = directionsNumber;
    }

    public boolean isUseScanPositionsFile() {
        return useScanPositionsFile;
    }

    public void setUseScanPositionsFile(boolean useScanPositionsFile) {
        this.useScanPositionsFile = useScanPositionsFile;
    }

    public Point3f getCenterPoint() {
        return centerPoint;
    }

    public void setCenterPoint(Point3f centerPoint) {
        this.centerPoint = centerPoint;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getStep() {
        return step;
    }

    public void setStep(float step) {
        this.step = step;
    }

    public List<Point3d> getPositions() {
        return positions;
    }

    public void setPositions(List<Point3d> positions) {
        this.positions = positions;
    }

    public float getLatitudeInDegrees() {
        return latitudeInDegrees;
    }

    public void setLatitudeInDegrees(float latitudeInDegrees) {
        this.latitudeInDegrees = latitudeInDegrees;
    }

    public List<SimulationPeriod> getSimulationPeriods() {
        return simulationPeriods;
    }

    public void setSimulationPeriods(List<SimulationPeriod> simulationPeriods) {
        this.simulationPeriods = simulationPeriods;
    }

    public boolean isGenerateTextFile() {
        return generateTextFile;
    }

    public void setGenerateTextFile(boolean generateTextFile) {
        this.generateTextFile = generateTextFile;
    }

    public boolean isGenerateBitmapFile() {
        return generateBitmapFile;
    }

    public void setGenerateBitmapFile(boolean generateBitmapFile) {
        this.generateBitmapFile = generateBitmapFile;
    }

    public File getTextFile() {
        return textFile;
    }

    public void setTextFile(File textFile) {
        this.textFile = textFile;
    }

    public File getBitmapFolder() {
        return bitmapFolder;
    }

    public void setBitmapFile(File bitmapFolder) {
        this.bitmapFolder = bitmapFolder;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public boolean[] getMasks() {
        return masks;
    }

    public void setMasks(boolean[] masks) {
        this.masks = masks;
    }   

    public boolean isGenerateLAI2xxxTypeFormat() {
        return generateLAI2xxxTypeFormat;
    }

    public void setGenerateLAI2xxxTypeFormat(boolean generateLAI2xxxTypeFormat) {
        this.generateLAI2xxxTypeFormat = generateLAI2xxxTypeFormat;
    }

    public float getDirectionsRotation() {
        return directionsRotation;
    }

    public void setDirectionsRotation(float directionsRotation) {
        this.directionsRotation = directionsRotation;
    }

    public boolean isToricity() {
        return toricity;
    }

    public void setToricity(boolean toricity) {
        this.toricity = toricity;
    }
}
