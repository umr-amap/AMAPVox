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
package org.amapvox.lidar.jlas;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class PointDataRecordFormat {
    
    public static enum Classification {

        CREATED_NEVER_CLASSIFIED((short)0,  "Created, never classified"),
        UNCLASSIFIED((short)1,  "Unclassified"),
        GROUND((short)2,  "Ground"),
        LOW_VEGETATION((short)3,  "Low vegetation"),
        MEDIUM_VEGETATION((short)4,  "Medium vegetation"),
        HIGH_VEGETATION((short)5,  "High vegetation"),
        BUILDING((short)6,  "Building"),
        LOW_POINT((short)7,  "Low point (noise)"),
        MODEL_KEY_POINT((short)8,  "Model key-point (mass point)"),
        WATER((short)9,  "Water"),
        RESERVED_10((short)10,  "Reserved for ASPRS Definition"),
        RESERVED_11((short)11,  "Reserved for ASPRS Definition"),
        OVERLAP_POINTS((short)12,  "Overlap Points");
        
        private short value;
        private String description;

        private Classification(short value, String description) {
            this.value = value;
            this.description = description;
        }

        public short getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }        
}
    
    public static short LENGTH = 20;
            
    private int x;
    private int y;
    private int z;
    private int intensity;
   
    
    private short returnNumber;
    private short numberOfReturns;
    private boolean scanDirectionFlag;
    private boolean edgeOfFlightLine;
    private short classification;
    private int scanAngleRank;
    private int userData;
    private int pointSourceID;
    private double gpsTime;
    private boolean synthetic;
    private boolean keyPoint;
    private boolean withheld;
    
    private boolean hasVLineExtrabytes = false;
    private boolean hasQLineExtrabytes = false;
    
    private Extrabytes extrabytes;

    public void setExtrabytes(Extrabytes extrabytes) {
        if(extrabytes instanceof VLineExtrabytes){
            hasVLineExtrabytes = true;
        }else if(extrabytes instanceof QLineExtrabytes){
            hasQLineExtrabytes = false;
        }
        this.extrabytes = extrabytes;
    }

    public QLineExtrabytes getQLineExtrabytes() {
        if(!hasQLineExtrabytes){
            return null;
        }
        return (QLineExtrabytes)extrabytes;
    }
    
    public VLineExtrabytes getVLineExtrabytes() {
        if(!hasVLineExtrabytes){
            return null;
        }
        return (VLineExtrabytes)extrabytes;
    }

    public boolean isHasVLineExtrabytes() {
        return hasVLineExtrabytes;
    }

    public boolean isHasQLineExtrabytes() {
        return hasQLineExtrabytes;
    }
    

    public boolean isSynthetic() {
        return synthetic;
    }

    public void setSynthetic(boolean synthetic) {
        this.synthetic = synthetic;
    }

    public boolean isKeyPoint() {
        return keyPoint;
    }

    public void setKeyPoint(boolean keyPoint) {
        this.keyPoint = keyPoint;
    }

    public boolean isWithheld() {
        return withheld;
    }

    public void setWithheld(boolean withheld) {
        this.withheld = withheld;
    }
    
    public void setGpsTime(double gpsTime) {
        this.gpsTime = gpsTime;
    }

    public double getGpsTime() {
        return gpsTime;
    }
    
    
    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }

    public void setReturnNumber(short returnNumber) {
        this.returnNumber = returnNumber;
    }

    public void setNumberOfReturns(short numberOfReturns) {
        this.numberOfReturns = numberOfReturns;
    }

    public void setScanDirectionFlag(boolean scanDirectionFlag) {
        this.scanDirectionFlag = scanDirectionFlag;
    }

    public void setEdgeOfFlightLine(boolean edgeOfFlightLine) {
        this.edgeOfFlightLine = edgeOfFlightLine;
    }

    public void setClassification(short classification) {
        this.classification = classification;
    }

    public void setScanAngleRank(int scanAngleRank) {
        this.scanAngleRank = scanAngleRank;
    }

    public void setUserData(int userData) {
        this.userData = userData;
    }

    public void setPointSourceID(int pointSourceID) {
        this.pointSourceID = pointSourceID;
    }
    
    
    public short getClassification() {
        return classification;
    }

    public boolean isEdgeOfFlightLine() {
        return edgeOfFlightLine;
    }

    public int getIntensity() {
        return intensity;
    }

    public short getNumberOfReturns() {
        return numberOfReturns;
    }

    public int getPointSourceID() {
        return pointSourceID;
    }

    public short getReturnNumber() {
        return returnNumber;
    }

    public int getScanAngleRank() {
        return scanAngleRank;
    }

    public boolean isScanDirectionFlag() {
        return scanDirectionFlag;
    }

    public int getUserData() {
        return userData;
    }

    public long getX() {
        return x;
    }

    public long getY() {
        return y;
    }

    public long getZ() {
        return z;
    }
    
}