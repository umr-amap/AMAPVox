/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.amapvox.gui.viewer3d;

import java.io.File;
import java.io.IOException;
import javax.vecmath.Matrix4d;
import org.amapvox.commons.AVoxTask;
import org.amapvox.commons.Configuration;
import org.amapvox.commons.Matrix;
import org.amapvox.commons.Release;
import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 *
 * @author pverley
 */
public class Viewer3dConfiguration extends Configuration {

    private File voxelFile;
    private String variableName;

    private File dtmFile;
    private Matrix4d vopMatrix;
    private int dtmMargin = 0;

    public Viewer3dConfiguration() {
        super("VIEWER3D", "Viewer 3D",
                "3D visualization of a voxel file with OpenGL");
        vopMatrix = new Matrix4d();
        vopMatrix.setIdentity();
    }

    @Override
    public Class<? extends AVoxTask> getTaskClass() {
        return Viewer3dTask.class;
    }

    @Override
    public void readProcessElements(Element processElement) throws JDOMException, IOException {

        Element inputElement = processElement.getChild("input");
        voxelFile = new File(inputElement.getAttributeValue("src"));
        variableName = inputElement.getAttributeValue("variable");

        Element dtmElement = processElement.getChild("dtm");
        if (!dtmElement.getAttributeValue("src").isEmpty()) {
            dtmFile = new File(dtmElement.getAttributeValue("src"));
        }
        vopMatrix = Matrix.valueOf(dtmElement.getChild("matrix")).toMatrix4d();
        dtmMargin = Integer.parseInt(dtmElement.getAttributeValue("margin"));
    }

    @Override
    public void writeProcessElements(Element processElement) throws JDOMException, IOException {

        Element inputElement = new Element("input");
        inputElement.setAttribute("src", voxelFile.getAbsolutePath());
        inputElement.setAttribute("variable", variableName);
        processElement.addContent(inputElement);

        Element dtmElement = new Element("dtm");
        dtmElement.setAttribute("src", null != dtmFile ? dtmFile.getAbsolutePath() : "");
        dtmElement.setAttribute("margin", String.valueOf(dtmMargin));
        Matrix matrix = Matrix.valueOf(vopMatrix);
        matrix.setId("vop");
        dtmElement.addContent(matrix.toElement());
        processElement.addContent(dtmElement);
    }

    /**
     * @return the voxelFile
     */
    public File getVoxelFile() {
        return voxelFile;
    }

    /**
     * @param voxelFile the voxelFile to set
     */
    public void setVoxelFile(File voxelFile) {
        this.voxelFile = voxelFile;
    }

    /**
     * @return the variableName
     */
    public String getVariableName() {
        return variableName;
    }

    /**
     * @param variableName the variableName to set
     */
    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    /**
     * @return the dtmFile
     */
    public File getDtmFile() {
        return dtmFile;
    }

    /**
     * @param dtmFile the dtmFile to set
     */
    public void setDtmFile(File dtmFile) {
        this.dtmFile = dtmFile;
    }

    /**
     * @return the vopMatrix
     */
    public Matrix4d getVopMatrix() {
        return vopMatrix;
    }

    /**
     * @param vopMatrix the vopMatrix to set
     */
    public void setVopMatrix(Matrix4d vopMatrix) {
        this.vopMatrix = vopMatrix;
    }

    /**
     * @return the dtmMargin
     */
    public int getDtmMargin() {
        return dtmMargin;
    }

    /**
     * @param dtmMargin the dtmMargin to set
     */
    public void setDtmMargin(int dtmMargin) {
        this.dtmMargin = dtmMargin;
    }

    @Override
    public Release[] getReleases() {
        return null;
    }

}
