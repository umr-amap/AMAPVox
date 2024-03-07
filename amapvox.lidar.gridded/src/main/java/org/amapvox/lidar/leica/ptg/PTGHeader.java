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
package org.amapvox.lidar.leica.ptg;

import org.amapvox.lidar.gridded.PointScanHeader;

/**
 * This class represents the header of a PTG binary scan file, a Leica gridded point format.
 * (see <a href= "http://www.xdesy.de/freeware/PTG-DLL/PTG-1.0.pdf"> specification</a>)
 * 
 * @author Julien Heurtebize
 */
public class PTGHeader extends PointScanHeader{
    
    private int version;
    private String scanWorldName;
    private String scanName;
    private String scannerName;
    private String scannerModel;
    private String ipAddress;
    private String date; //as y/m/d
    private String time; //as hh:mm:ss
    private String texte = ""; //ignored
    private String text = ""; //ignored
    
    private int rowsTotal;
    private double minAzimuthAngle;
    private double maxAzimuthAngle;
    private double minElevationAngle;
    private double maxElevationAngle;
    

    public PTGHeader() {
        super();
    }

    /**
     * Get the file format version
     * @return the version as an integer
     */
    public int getVersion() {
        return version;
    }

    /**
     * Set the file format version
     * @param version the version as an integer
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * Set the name of the scanworld
     * @return The scanworld name
     */
    public String getScanWorldName() {
        return scanWorldName;
    }

    /**
     * Get the name of the scanworld
     * @param scanWorldName The scanworld name
     */
    public void setScanWorldName(String scanWorldName) {
        this.scanWorldName = scanWorldName;
    }

    /**
     * Get the name of the scan
     * @return scan's name
     */
    public String getScanName() {
        return scanName;
    }

    /**
     * Set the name of the scan
     * @param scanName
     */
    public void setScanName(String scanName) {
        this.scanName = scanName;
    }

    /**
     * Get the name of the scanner
     * @return The scanner's name
     */
    public String getScannerName() {
        return scannerName;
    }

    /**
     * Set the name of the scanner
     * @param scannerName The scanner's name
     */
    public void setScannerName(String scannerName) {
        this.scannerName = scannerName;
    }

    /**
     * Get the model of the scanner
     * @return The scanner's model
     */
    public String getScannerModel() {
        return scannerModel;
    }

    /**
     * Set the model of the scanner
     * @param scannerModel The scanner's model
     */
    public void setScannerModel(String scannerModel) {
        this.scannerModel = scannerModel;
    }

    /**
     * Get the IP address
     * @return The ip's address
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Set the IP address
     * @param ipAddress The ip's address
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Get the creation date as yyyy/mm/dd
     * @return The acquisition's date
     */
    public String getDate() {
        return date;
    }

    /**
     * Set the creation date as yyyy/mm/dd
     * @param date The acquisition's date
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Get the creation time as hh:mm:ss
     * @return The creation time
     */
    public String getTime() {
        return time;
    }

    /**
     * Set the creation time as hh:mm:ss
     * @param time The creation time
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * Get texte attribute(s) (to be ignored)
     * @return The texte attribute(s), separated by ";"
     */
    public String getTexte() {
        return texte;
    }

    /**
     * Set texte attribute (to be ignored), this attribute may occur multiple times, 
     * so it will be added to the current string and separated by ";"
     * @param texte The texte attribute to add
     */
    public void setTexte(String texte) {
        this.texte += ";"+texte;
    }

    /**
     * Get text attribute(s) (to be ignored)
     * @return The text attribute(s), separated by ";"
     */
    public String getText() {
        return text;
    }

    /**
     * Set text attribute (to be ignored), this attribute may occur multiple times, 
     * so it will be added to the current string and separated by ";"
     * @param text The text attribute to add
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Get rows_total attribute (to be ignored)
     * @return The rows_total attribute
     */
    public int getRowsTotal() {
        return rowsTotal;
    }

    /**
     * Set rows_total attribute (to be ignored)
     * @param rowsTotal The rows_total attribute
     */
    public void setRowsTotal(int rowsTotal) {
        this.rowsTotal = rowsTotal;
    }

    /**
     * Get minimum azimuth angle (this value may not be set)
     * @return The minimum azimuth angle
     */
    public double getMinAzimuthAngle() {
        return minAzimuthAngle;
    }

    /**
     * Set minimum azimuth angle
     * @param minAzimuthAngle The minimum azimuth angle
     */
    public void setMinAzimuthAngle(double minAzimuthAngle) {
        this.minAzimuthAngle = minAzimuthAngle;
    }

    /**
     * Get maximum azimuth angle (this value may not be set)
     * @return The maximum azimuth angle
     */
    public double getMaxAzimuthAngle() {
        return maxAzimuthAngle;
    }

    /**
     * Set maximum azimuth angle
     * @param maxAzimuthAngle The maximum azimuth angle
     */
    public void setMaxAzimuthAngle(double maxAzimuthAngle) {
        this.maxAzimuthAngle = maxAzimuthAngle;
    }

    /**
     * Get minimum zenithal angle (this value may not be set)
     * @return The minimum zenithal angle
     */
    public double getMinElevationAngle() {
        return minElevationAngle;
    }

    /**
     * Set minimum zenithal angle
     * @param minElevationAngle The minimum zenithal angle
     */
    public void setMinElevationAngle(double minElevationAngle) {
        this.minElevationAngle = minElevationAngle;
    }

    /**
     * Get maximum zenithal angle (this value may not be set)
     * @return The maximum zenithal angle
     */
    public double getMaxElevationAngle() {
        return maxElevationAngle;
    }

    /**
     * Set maximum zenithal angle
     * @param maxElevationAngle The maximum zenithal angle
     */
    public void setMaxElevationAngle(double maxElevationAngle) {
        this.maxElevationAngle = maxElevationAngle;
    }
    
    @Override
    public String toString(){
        
        return "Format version :\t"+version+"\n"+
                "Scan world name :\t"+scanWorldName+"\n"+
                "Scan name :\t\t"+scanName+"\n"+
                "Scanner name :\t\t"+scannerName+"\n"+
                "Scanner model :\t\t"+scannerModel+"\n"+
                "Scanner ip address :\t"+ipAddress+"\n"+
                "Date :\t\t\t"+date+"\n"+
                "Time :\t\t\t"+time+"\n"+
                "Min azimuth angle :\t"+minAzimuthAngle+"\n"+
                "Max azimuth angle :\t"+maxAzimuthAngle+"\n"+
                "Min elevation angle :\t"+minElevationAngle+"\n"+
                "Max elevation angle :\t"+maxElevationAngle+"\n"+
                "Transformation matrix :\t"+transfMatrix.toString()+"\n"+
                "Column number :\t\t"+numCols+"\n"+
                "Row number :\t\t"+numRows+"\n"+
                "Point storage format :\t"+((isPointInDoubleFormat()) ? "Double" : "Float")+"\n"+
                "Contains intensity :\t"+((isPointContainsIntensity()) ? "Yes" : "No")+"\n"+
                "Contains RGB :\t\t"+((isPointContainsRGB()) ? "Yes" : "No");
    }
}
