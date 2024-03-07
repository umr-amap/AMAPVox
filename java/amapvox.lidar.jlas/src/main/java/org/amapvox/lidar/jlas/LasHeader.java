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

import org.amapvox.commons.util.ByteConverter;



/**
 * Represents the structure of a las file header version 1.0
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class LasHeader {

    private String fileSignature;
    private long reserved;
    private long projectIdGuidData1;
    private int projectIdGuidData2;
    private int projectIdGuidData3;
    private double projectIdGuidData4;
    private int versionMajor;
    private int versionMinor;
    private String systemIdentifier;
    private String generatingSoftware;
    private short fileCreationDayOfYear;
    private short fileCreationYear;
    private short headerSize;
    private long offsetToPointData;
    private long numberOfVariableLengthRecords;
    private short pointDataFormatID;
    private short pointDataRecordLength;
    private long numberOfPointrecords;
    private long[] numberOfPointsByReturn;
    private double xScaleFactor;
    private double yScaleFactor;
    private double zScaleFactor;
    private double xOffset;
    private double yOffset;
    private double zOffset;
    private double maxX;
    private double minX;
    private double maxY;
    private double minY;
    private double maxZ;
    private double minZ;
    
    public LasHeader(){
        
    }

    public LasHeader(byte versionMajor, byte versionMinor, int numberOfPointrecords, double xScaleFactor, double yScaleFactor, double zScaleFactor, double xOffset, double yOffset, double zOffset, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        
        this.versionMajor = ByteConverter.unsignedByteToShort(versionMajor);
        this.versionMinor = ByteConverter.unsignedByteToShort(versionMinor);
        
        
        this.numberOfPointrecords = ByteConverter.unsignedIntegerToLong(numberOfPointrecords);
        
        this.xScaleFactor = xScaleFactor;
        this.yScaleFactor = yScaleFactor;
        this.zScaleFactor = zScaleFactor;
        
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        
    }
    
    

    public long getReserved() {
        return reserved;
    }

    public void setReserved(long reserved) {
        this.reserved = reserved;
    }

    
    public void setFileSignature(char[] fileSignature) {

        this.fileSignature = String.valueOf(fileSignature);
    }    
    
    public void setProjectIdGuidData1(long projectIdGuidData1) {
        this.projectIdGuidData1 = projectIdGuidData1;
    }

    public void setProjectIdGuidData2(int projectIdGuidData2) {
        this.projectIdGuidData2 = projectIdGuidData2;
    }

    public void setProjectIdGuidData3(int projectIdGuidData3) {
        this.projectIdGuidData3 = projectIdGuidData3;
    }

    public void setProjectIdGuidData4(double projectIdGuidData4) {
        this.projectIdGuidData4 = projectIdGuidData4;
    }

    public void setVersionMajor(byte versionMajor) {
        this.versionMajor = (int) versionMajor;
    }

    public void setVersionMinor(byte versionMinor) {
        this.versionMinor = (int) versionMinor;
    }
    
    /**
     * 
     * @return  <p>The version 1.0 specification assumes that LAS files are exclusively generated 
     * as a result of collection by a hardware sensor. Version 1.1 recognizes that files often result from 
     * extraction, merging or modifying existing data files. </p>
    
     * <table border=1>
     * <caption>System identifier</caption>
     * <tr><th>Generating agent</th> <th>System ID</th></tr>
    
     * <tr><td>Hardware system</td>                          <td>String identifying hardware (e.g. “ALTM 1210” or “ALS50”)</td></tr>
     * <tr><td>Merge of one or more files</td>               <td>“MERGE”</td></tr>
     * <tr><td>Modification of a single file</td>            <td>“MODIFICATION” </td></tr>
     * <tr><td>Extraction from one or more files</td>        <td>“EXTRACTION” </td></tr>
     * <tr><td>Reprojection, rescaling, warping, etc.</td>   <td>“TRANSFORMATION”</td></tr>
     * </table>
     * Some other operation “OTHER” or a string up to 32 characters 
     * identifying the operation 
     */
    
    public String getSystemIdentifier() {
        return systemIdentifier;
    }
    
    public void setGeneratingSoftware(char[] generatingSoftware) {
        this.generatingSoftware = String.valueOf(generatingSoftware);
    }
    
    public void setFileCreationDayOfYear(short fileCreationDayOfYear) {
        this.fileCreationDayOfYear = fileCreationDayOfYear;
    }

    public void setFileCreationYear(short fileCreationYear) {
        this.fileCreationYear = fileCreationYear;
    }

    public void setHeaderSize(short headerSize) {
        this.headerSize = headerSize;
    }


    public void setVersionMajor(int versionMajor) {
        this.versionMajor = versionMajor;
    }
    
    public void setVersionMinor(int versionMinor) {
        this.versionMinor = versionMinor;
    }

    public void setOffsetToPointData(long offsetToPointData) {
        this.offsetToPointData = offsetToPointData;
    }

    public void setNumberOfVariableLengthRecords(long numberOfVariableLengthRecords) {
        this.numberOfVariableLengthRecords = numberOfVariableLengthRecords;
    }

    public void setPointDataFormatID(byte pointDataFormatID) {
        this.pointDataFormatID = (short) pointDataFormatID;
    }

    public void setPointDataRecordLength(short pointDataRecordLength) {
        this.pointDataRecordLength = pointDataRecordLength;
    }

    public void setNumberOfPointsByReturn(long[] numberOfPointsByReturn) {
        this.numberOfPointsByReturn = numberOfPointsByReturn;
    }

    public void setxScaleFactor(double xScaleFactor) {
        this.xScaleFactor = xScaleFactor;
    }

    public void setyScaleFactor(double yScaleFactor) {
        this.yScaleFactor = yScaleFactor;
    }

    public void setzScaleFactor(double zScaleFactor) {
        this.zScaleFactor = zScaleFactor;
    }

    public void setxOffset(double xOffset) {
        this.xOffset = xOffset;
    }

    public void setyOffset(double yOffset) {
        this.yOffset = yOffset;
    }

    public void setzOffset(double zOffset) {
        this.zOffset = zOffset;
    }

    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    public void setMinX(double minX) {
        this.minX = minX;
    }

    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }

    public void setMinY(double minY) {
        this.minY = minY;
    }
    
    public void setMaxZ(double maxZ) {
        this.maxZ = maxZ;
    }

    public void setMinZ(double minZ) {
        this.minZ = minZ;
    }

    public void setNumberOfPointrecords(long numberOfPointrecords) {
        this.numberOfPointrecords = numberOfPointrecords;
    }

    /**
     * 
     * @return The file signature must contain the four characters “LASF”, and it is required by 
     * the LAS specification.
     */
    public String getFileSignature() {
        return fileSignature;
    }

    /**
     * 
     * @return <p>The version number consists of a major and minor field. The major and minor 
     * fields combine to form the number that indicates the format number of the current specification 
     * itself. </p><p>For example, specification number 1.2 (this version) would contain 1 in the major field and 
     * 2 in the minor field.</p>
     */
    public int getVersionMajor() {
        return versionMajor;
    }

    /**
     * 
     * @return <p>The version number consists of a major and minor field. The major and minor 
     * fields combine to form the number that indicates the format number of the current specification 
     * itself. </p><p>For example, specification number 1.2 (this version) would contain 1 in the major field and 
     * 2 in the minor field. </p>
     */
    public int getVersionMinor() {
        return versionMinor;
    }

    public void setSystemIdentifier(char[] systemIdentifier) {
        this.systemIdentifier = String.valueOf(systemIdentifier);
    }
    
    /**
     * 
     * @return <p>This information is ASCII data describing the generating software itself. </p>
     * <p>This field provides a mechanism for specifying which generating software package and version 
     * was used during LAS file creation (e.g. “TerraScan V-10.8”, “REALM V-4.2” and etc.). </p>
     * <p>If the character data is less than 16 characters, the remaining data must be null. </p>
     */
    public String getGeneratingSoftware() {
        return generatingSoftware;
    }
    
    
    /**
     * 
     * @return  The julian day of the year that the data was collected. This field should be 
     * populated by the generating software. 
     */
    public int getFileCreationDayOfYear() {
        return fileCreationDayOfYear;
    }

    /**
     * 
     * @return The year, expressed as a four digit number, in which the file was created. 
     */
    public int getFileCreationYear() {
        return fileCreationYear;
    }

    /**
     * 
     * @return <p>The size, in bytes, of the Public Header Block itself. </p><p>In the event that the header is 
     * extended by a software application through the addition of data at the end of the header, the 
     * Header Size field must be updated with the new header size. </p><p>Extension of the Public Header 
     * Block is discouraged; the Variable Length Records should be used whenever possible to add 
     * custom header data. </p><p>In the event a generating software package adds data to the Public Header 
     * Block, this data must be placed at the end of the structure and the Header Size must be updated 
     * to reflect the new size.</p>
     */
    public int getHeaderSize() {
        return headerSize;
    }

    /**
     * 
     * @return <p>The actual number of bytes from the beginning of the file to the first field 
     * of the first point record data field.</p> <p>This data offset must be updated if any software adds data 
     * from the Public Header Block or adds/removes data to/from the Variable Length Records.</p>
     */
    public long getOffsetToPointData() {
        return offsetToPointData;
    }

    /**
     * 
     * @return <p>This field contains the current number of Variable Length 
     * Records.</p> <p>This number must be updated if the number of Variable Length Records changes at 
     * any time.</p>
     */
    public long getNumberOfVariableLengthRecords() {
        return numberOfVariableLengthRecords;
    }

    /**
     * 
     * @return The point data format ID corresponds to the point data record format 
     * type. LAS 1.2 defines types 0, 1, 2 and 3. 
     */
    public int getPointDataFormatID() {
        return pointDataFormatID;
    }

    /**
     * 
     * @return The size, in bytes, of the Point Data Record. 
     */
    public short getPointDataRecordLength() {
        return pointDataRecordLength;
    }

    /**
     * 
     * @return This field contains the total number of point records within the file. 
     */
    public long getNumberOfPointrecords() {
        return numberOfPointrecords;
    }

    /**
     * 
     * @return <p>This field contains an array of the total point records per return.</p> 
     * <p>The first unsigned long value will be the total number of records from the first return, and the 
     * second contains the total number for return two, and so forth up to five returns.</p>
     */
    public long[] getNumberOfPointsByReturn() {
        return numberOfPointsByReturn;
    }

    /**
     * 
     * @return <p> The scale factor fields contain a double floating point value that is used 
     * to scale the corresponding X, Y, and Z long values within the point records. The corresponding 
     * X, Y, and Z scale factor must be multiplied by the X, Y, or Z point record value to get the actual 
     * X, Y, or Z coordinate.</p> <p>For example, if the X, Y, and Z coordinates are intended to have two 
     * decimal point values, then each scale factor will contain the number 0.01. </p>
     */
    public double getxScaleFactor() {
        return xScaleFactor;
    }

    /**
     * 
     * @return  <p>The scale factor fields contain a double floating point value that is used 
     * to scale the corresponding X, Y, and Z long values within the point records. The corresponding 
     * X, Y, and Z scale factor must be multiplied by the X, Y, or Z point record value to get the actual 
     * X, Y, or Z coordinate.</p> <p>For example, if the X, Y, and Z coordinates are intended to have two 
     * decimal point values, then each scale factor will contain the number 0.01. </p>
     */
    public double getyScaleFactor() {
        return yScaleFactor;
    }

    /**
     * 
     * @return  <p>The scale factor fields contain a double floating point value that is used 
     * to scale the corresponding X, Y, and Z long values within the point records. The corresponding 
     * X, Y, and Z scale factor must be multiplied by the X, Y, or Z point record value to get the actual 
     * X, Y, or Z coordinate.</p> <p>For example, if the X, Y, and Z coordinates are intended to have two 
     * decimal point values, then each scale factor will contain the number 0.01.</p>
     */
    public double getzScaleFactor() {
        return zScaleFactor;
    }

    /**
     * 
     * @return <p>The offset fields should be used to set the overall offset for the point records.</p> 
     * <p>In general these numbers will be zero, but for certain cases the resolution of the point data may 
     * not be large enough for a given projection system.</p> <p>However, it should always be assumed that 
     * these numbers are used. So to scale a given X from the point record, take the point record X 
     * multiplied by the X scale factor, and then add the X offset.</p> 
     * <p>Xcoordinate = (Xrecord * Xscale) + Xoffset</p>
     * <p>Ycoordinate = (Yrecord * Yscale) + Yoffset</p>
     * <p>Zcoordinate = (Zrecord * Zscale) + Zoffset</p> 
     */
    public double getxOffset() {
        return xOffset;
    }

    /**
     * 
     * @return <p>The offset fields should be used to set the overall offset for the point records. </p>
     * <p>In general these numbers will be zero, but for certain cases the resolution of the point data may 
     * not be large enough for a given projection system. However, it should always be assumed that 
     * these numbers are used. So to scale a given X from the point record, take the point record X 
     * multiplied by the X scale factor, and then add the X offset. </p>
     * <p>Xcoordinate = (Xrecord * Xscale) + Xoffset</p>
     * <p>Ycoordinate = (Yrecord * Yscale) + Yoffset</p>
     * <p>Zcoordinate = (Zrecord * Zscale) + Zoffset</p>
     */
    public double getyOffset() {
        return yOffset;
    }

    /**
     * 
     * @return <p>The offset fields should be used to set the overall offset for the point records. </p>
     * <p>In general these numbers will be zero, but for certain cases the resolution of the point data may 
     * not be large enough for a given projection system. However, it should always be assumed that 
     * these numbers are used. So to scale a given X from the point record, take the point record X 
     * multiplied by the X scale factor, and then add the X offset. </p>
     * <p>Xcoordinate = (Xrecord * Xscale) + Xoffset</p>
     * <p>Ycoordinate = (Yrecord * Yscale) + Yoffset</p>
     * <p>Zcoordinate = (Zrecord * Zscale) + Zoffset</p>
     */
    public double getzOffset() {
        return zOffset;
    }

    /**
     * 
     * @return  The max and min data fields are the actual unscaled extents of the LAS 
     * point file data, specified in the coordinate system of the LAS data. 
     */
    public double getMaxX() {
        return maxX;
    }

    /**
     * 
     * @return  The max and min data fields are the actual unscaled extents of the LAS 
     * point file data, specified in the coordinate system of the LAS data. 
     */
    public double getMinX() {
        return minX;
    }

    /**
     * 
     * @return  The max and min data fields are the actual unscaled extents of the LAS 
     * point file data, specified in the coordinate system of the LAS data. 
     */
    public double getMaxY() {
        return maxY;
    }

    /**
     * 
     * @return  The max and min data fields are the actual unscaled extents of the LAS 
     * point file data, specified in the coordinate system of the LAS data. 
     */
    public double getMinY() {
        return minY;
    }

    /**
     * 
     * @return  The max and min data fields are the actual unscaled extents of the LAS 
     * point file data, specified in the coordinate system of the LAS data. 
     */
    public double getMaxZ() {
        return maxZ;
    }

    /**
     * 
     * @return  The max and min data fields are the actual unscaled extents of the LAS 
     * point file data, specified in the coordinate system of the LAS data. 
     */
    public double getMinZ() {
        return minZ;
    }
    
    /**
     * 
     * @return <p>The four fields that comprise a complete Globally Unique Identifier 
     * (GUID) are now reserved for use as a Project Identifier (Project ID). The field remains optional. </p>
     * <p>The time of assignment of the Project ID is at the discretion of processing software. The Project 
     * ID should be the same for all files that are associated with a unique project.</p> By assigning a 
     * Project ID and using a File Source ID (defined above) every file within a project and every point 
     * within a file can be uniquely identified, globally. 
     */
    public long getProjectIdGuidData1() {
        return projectIdGuidData1;
    }

    /**
     * 
     * @return <p>The four fields that comprise a complete Globally Unique Identifier 
     * (GUID) are now reserved for use as a Project Identifier (Project ID). The field remains optional.</p> 
     * <p>The time of assignment of the Project ID is at the discretion of processing software. The Project 
     * ID should be the same for all files that are associated with a unique project. </p>By assigning a 
     * Project ID and using a File Source ID (defined above) every file within a project and every point 
     * within a file can be uniquely identified, globally. 
     */
    public int getProjectIdGuidData2() {
        return projectIdGuidData2;
    }
    
    /**
     * 
     * @return <p>The four fields that comprise a complete Globally Unique Identifier 
     * (GUID) are now reserved for use as a Project Identifier (Project ID). The field remains optional.</p>
     * The time of assignment of the Project ID is at the discretion of processing software. The Project 
     * ID should be the same for all files that are associated with a unique project. <p>By assigning a 
     * Project ID and using a File Source ID (defined above) every file within a project and every point 
     * within a file can be uniquely identified, globally. </p>
     */
    public int getProjectIdGuidData3() {
        return projectIdGuidData3;
    }

    /**
     * 
     * @return <p>The four fields that comprise a complete Globally Unique Identifier 
     * (GUID) are now reserved for use as a Project Identifier (Project ID).The field remains optional. </p>
     * <p>The time of assignment of the Project ID is at the discretion of processing software. The Project 
     * ID should be the same for all files that are associated with a unique project.</p> 
     * <p>By assigning a Project ID and using a File Source ID (defined above) every file within a project and every point 
     * within a file can be uniquely identified, globally. </p>
     */
    public double getProjectIdGuidData4() {
        return projectIdGuidData4;
    }
    
    public int getFileSourceId() {
        return 0;
    }
}


