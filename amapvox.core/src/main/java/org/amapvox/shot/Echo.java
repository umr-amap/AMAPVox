package org.amapvox.shot;

import java.util.HashMap;
import javax.vecmath.Point3d;

/**
 *
 * @author pverley
 */
public class Echo {

    // echo rank
    private final int rank;
    // echo location
    private final Point3d location;
    // pointer to shot
    private final Shot shot;
    // echo integer properties
    private final HashMap<String, Integer> intProperties;
    // echo float properties
    private final HashMap<String, Float> floatProperties;

    public Echo(Shot shot, int rank, Point3d location) {
        this.rank = rank;
        this.location = location;
        this.shot = shot;
        intProperties = new HashMap<>();
        floatProperties = new HashMap<>();
    }
    
    public int getRank() {
        return rank;
    }
    
    public Point3d getLocation() {
        return location;
    }
    
    public Shot getShot() {
        return shot;
    }
    
    public Integer getInteger(String name) {
        return intProperties.get(name);
    }
    
    public Float getFloat(String name) {
        return floatProperties.get(name);
    }
    
    public void addInteger(String name, int value) {
        intProperties.put(name, value);
    }
    
    public void addFloat(String name, float value) {
        floatProperties.put(name, value);
    }

}
