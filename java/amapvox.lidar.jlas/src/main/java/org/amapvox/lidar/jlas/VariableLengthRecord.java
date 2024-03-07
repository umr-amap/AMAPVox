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
public class VariableLengthRecord {
    
    private int reserved;
    private String userID;
    private int recordID;
    private int recordLengthAfterHeader;
    private String description;

    public void setReserved(int reserved) {
        this.reserved = reserved;
    }

    public void setUserID(char[] userID) {
        this.userID = String.valueOf(userID);
    }

    public void setRecordID(int recordID) {
        this.recordID = recordID;
    }

    public void setRecordLengthAfterHeader(int recordLengthAfterHeader) {
        this.recordLengthAfterHeader = recordLengthAfterHeader;
    }

    public void setDescription(char[] description) {
        this.description = String.valueOf(description);
    }

    public int getReserved() {
        return reserved;
    }

    public String getUserID() {
        return userID;
    }

    public int getRecordID() {
        return recordID;
    }

    public int getRecordLengthAfterHeader() {
        return recordLengthAfterHeader;
    }

    public String getDescription() {
        return description;
    }
}
