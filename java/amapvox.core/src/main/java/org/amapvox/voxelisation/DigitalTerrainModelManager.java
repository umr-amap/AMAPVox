/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation;

import org.amapvox.commons.raster.asc.AsciiGridHelper;
import org.amapvox.commons.raster.asc.Raster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.vecmath.Matrix4d;

/**
 *
 * @author pverley
 */
public class DigitalTerrainModelManager {

    private final File file;
    private final Matrix4d vopMatrix;

    private Raster dtm;

    public DigitalTerrainModelManager(File file, Matrix4d vopMatrix) {

        this.file = file;
        if (null != vopMatrix) {
            this.vopMatrix = vopMatrix;
        } else {
            this.vopMatrix = new Matrix4d();
            this.vopMatrix.setIdentity();
        }
    }

    public void init() throws IOException {

        if (null == file || !file.exists()) {
            throw new FileNotFoundException("DTM file not found, " + file);
        }

        try {
            dtm = AsciiGridHelper.readFromAscFile(file);
            dtm.setTransformationMatrix(vopMatrix);
        } catch (Exception ex) {
            StringBuilder sb = new StringBuilder();
            sb.append("Failed to load DTM file ");
            sb.append(file.getAbsoluteFile());
            sb.append('\n');
            sb.append("It must be an ASCII file (*.asc)");
            throw new IOException(sb.toString(), ex);
        }
    }
    
    public String getFile() {
        return file.getAbsolutePath();
    }

    public Raster getDTM() {
        return dtm;
    }
}
