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
 * Represents the structure of a las file header version 1.1
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */

public class LasHeader11 extends LasHeader {

    private int fileSourceId;

    /**
     * 
     * @return <p>This field is a value between 1 and 65,535, inclusive. A value of zero (0) is interpreted to 
    mean that an ID has not been assigned. In this case, processing software is free to assign any valid number.</p>
    <p>Note that this scheme allows a LIDAR project to contain up to 65,535 unique 
    sources. A source can be considered an original flight line or it can be the result of merge and/or 
    extract operations. </p>
     */
    @Override
    public int getFileSourceId() {
        return fileSourceId;
    }

    /**
     * 
     * @return  Day, expressed as an unsigned short, on which this file was created. 
    Day is computed as the Greenwich Mean Time (GMT) day. January 1 is considered day 1. 
     */
    @Override
    public int getFileCreationDayOfYear() {
        return super.getFileCreationDayOfYear();
    }

    public void setFileSourceId(int fileSourceId) {
        this.fileSourceId = fileSourceId;
    }


}
