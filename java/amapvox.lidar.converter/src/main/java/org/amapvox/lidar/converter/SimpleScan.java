/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.converter;

import javax.vecmath.Matrix4d;
import java.io.File;

/**
 *
 * @author calcul
 */
public class SimpleScan {

    public final File file;
    public Matrix4d popMatrix;
    public Matrix4d sopMatrix;

    public SimpleScan(File file) {
        this.file = file;
        popMatrix = new Matrix4d();
        popMatrix.setIdentity();
        sopMatrix = new Matrix4d();
        sopMatrix.setIdentity();
    }

    public SimpleScan(File file, Matrix4d popMatrix, Matrix4d sopMatrix) {
        this.file = file;
        this.popMatrix = popMatrix;
        this.sopMatrix = sopMatrix;
    }

    @Override
    public String toString() {
        return file.getAbsolutePath();
    }
}
