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
package org.amapapvox.lidar.txt;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Shot{
    
    private int id;
    
    private double xOrigin;
    private double yOrigin;
    private double zOrigin;
    
    private double xDirection;
    private double yDirection;
    private double zDirection;
    
    private Echo[] echoes;

    public Shot(int id, double xOrigin, double yOrigin, double zOrigin, double xDirection, double yDirection, double zDirection, Echo... echoes) {
        this.id = id;
        this.xOrigin = xOrigin;
        this.yOrigin = yOrigin;
        this.zOrigin = zOrigin;
        this.xDirection = xDirection;
        this.yDirection = yDirection;
        this.zDirection = zDirection;
        this.echoes = echoes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Echo[] getEchoes() {
        return echoes;
    }
    
    public void setOrigin(double x, double y, double z){
        this.xOrigin = x;
        this.yOrigin = y;
        this.zOrigin = z;
    }

    public void setXOrigin(double xOrigin) {
        this.xOrigin = xOrigin;
    }

    public double getXOrigin() {
        return xOrigin;
    }

    public void setYOrigin(double yOrigin) {
        this.yOrigin = yOrigin;
    }

    public double getYOrigin() {
        return yOrigin;
    }

    public void setZOrigin(double zOrigin) {
        this.zOrigin = zOrigin;
    }

    public double getZOrigin() {
        return zOrigin;
    }
    
    public void setDirection(double x, double y, double z){
        this.xDirection = x;
        this.yDirection = y;
        this.zDirection = z;
    }

    public double getXDirection() {
        return xDirection;
    }

    public void setXDirection(double xDirection) {
        this.xDirection = xDirection;
    }

    public double getYDirection() {
        return yDirection;
    }

    public void setYDirection(double yDirection) {
        this.yDirection = yDirection;
    }

    public double getZDirection() {
        return zDirection;
    }

    public void setZDirection(double zDirection) {
        this.zDirection = zDirection;
    }

    public int getNbEchoes() {
        return echoes.length;
    }
}
