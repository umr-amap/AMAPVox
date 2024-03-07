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

import java.math.BigInteger;

/**
 * Represents the structure of a las file header version 1.4
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class LasHeader14 extends LasHeader13 {
    
    private BigInteger StartOfFirstExtendedVariableLengthRecord;
    private long NumberOfExtendedVariableLengthRecords;
    private BigInteger extendedNumberOfPointRecords;
    private BigInteger[] extendedNumberOfPointsByReturn;

    public long getNumberOfExtendedVariableLengthRecords() {
        return NumberOfExtendedVariableLengthRecords;
    }

    public void setNumberOfExtendedVariableLengthRecords(long NumberOfExtendedVariableLengthRecords) {
        this.NumberOfExtendedVariableLengthRecords = NumberOfExtendedVariableLengthRecords;
    }

    public BigInteger getStartOfFirstExtendedVariableLengthRecord() {
        return StartOfFirstExtendedVariableLengthRecord;
    }

    public void setStartOfFirstExtendedVariableLengthRecord(BigInteger StartOfFirstExtendedVariableLengthRecord) {
        this.StartOfFirstExtendedVariableLengthRecord = StartOfFirstExtendedVariableLengthRecord;
    }

    public BigInteger getExtendedNumberOfPointRecords() {
        return extendedNumberOfPointRecords;
    }

    public void setExtendedNumberOfPointRecords(BigInteger extendedNumberOfPointRecords) {
        this.extendedNumberOfPointRecords = extendedNumberOfPointRecords;
    }

    public BigInteger[] getExtendedNumberOfPointsByReturn() {
        return extendedNumberOfPointsByReturn;
    }

    public void setExtendedNumberOfPointsByReturn(BigInteger[] extendedNumberOfPointsByReturn) {
        this.extendedNumberOfPointsByReturn = extendedNumberOfPointsByReturn;
    }
    
}