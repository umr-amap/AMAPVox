/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.laszip;

import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author pverley
 */
public class LASPoint extends Structure {

    public int x, y, z;
    public short intensity;
    public byte return_number;
    public byte number_of_returns;
    public byte classification;
    public double gps_time;
    public short[] rgb = new short[4];

    @Override
    protected List<String> getFieldOrder() {

        return Arrays.asList(new String[]{
            "x",
            "y",
            "z",
            "intensity",
            "return_number",
            "number_of_returns",
            "classification",
            "gps_time",
            "rgb"
        });
    }

    @Override
    public String toString() {

        StringBuilder s = new StringBuilder();
        s.append("x ").append(x);
        s.append(" y ").append(y);
        s.append(" z ").append(z);
        s.append(" i ").append(intensity);
        s.append(" r ").append(return_number);
        s.append(" n ").append(number_of_returns);
        s.append(" c ").append(classification);
        s.append(" t ").append(gps_time);
        return s.toString();
    }

}
