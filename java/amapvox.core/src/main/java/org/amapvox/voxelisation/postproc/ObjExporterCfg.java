/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation.postproc;

import org.amapvox.commons.util.ColorGradient;
import org.amapvox.commons.Configuration;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import org.amapvox.commons.AVoxTask;
import org.amapvox.commons.Release;
import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 *
 * @author pverley
 */
public class ObjExporterCfg extends Configuration {

    private final static Logger LOGGER = Logger.getLogger(ObjExporterCfg.class);

    private File inputFile;
    private File outputFile;
    // voxel size function of PAD
    private boolean voxelSizeFunctionEnabled = false;
    private float maxPAD;
    private float alpha;
    // material
    private boolean materialEnabled = false;
    private String outputVariable = "";
    private String gradientName = "";

    public ObjExporterCfg() {
        super("OBJ_EXPORT", "OBJ Export",
                "Exports the voxel space into a Wawefront *.obj file.");
    }

    @Override
    public Class<? extends AVoxTask> getTaskClass() {
        return ObjExporter.class;
    }

    @Override
    public void readProcessElements(Element processElement) throws JDOMException, IOException {

        inputFile = new File(resolve(processElement.getChild("input_file").getAttributeValue("src")));
        outputFile = new File(resolve(processElement.getChild("output_file").getAttributeValue("src")));

        voxelSizeFunctionEnabled = Boolean.valueOf(processElement.getChild("voxel_size_function").getAttributeValue("enabled"));
        maxPAD = Float.valueOf(processElement.getChild("voxel_size_function").getAttributeValue("max_pad"));
        alpha = Float.valueOf(processElement.getChild("voxel_size_function").getAttributeValue("alpha"));

        materialEnabled = Boolean.valueOf(processElement.getChild("material").getAttributeValue("enabled"));
        outputVariable = processElement.getChild("material").getAttributeValue("variable");
        gradientName = processElement.getChild("material").getAttributeValue("gradient").toUpperCase();
    }

    @Override
    public void writeProcessElements(Element processElement) throws JDOMException, IOException {

        Element outputFileElement = new Element("output_file");
        outputFileElement.setAttribute(new Attribute("src", outputFile.getAbsolutePath()));
        processElement.addContent(outputFileElement);

        Element inputFileElement = new Element("input_file");
        inputFileElement.setAttribute(new Attribute("src", inputFile.getAbsolutePath()));
        processElement.addContent(inputFileElement);

        Element voxelSizeElement = new Element("voxel_size_function");
        voxelSizeElement.setAttribute("enabled", String.valueOf(voxelSizeFunctionEnabled));
        voxelSizeElement.setAttribute("max_pad", String.valueOf(maxPAD));
        voxelSizeElement.setAttribute("alpha", String.valueOf(alpha));
        processElement.addContent(voxelSizeElement);

        Element materialElement = new Element("material");
        materialElement.setAttribute("enabled", String.valueOf(materialEnabled));
        materialElement.setAttribute("variable", outputVariable);
        materialElement.setAttribute("gradient", gradientName);
        processElement.addContent(materialElement);
    }

    public boolean isVoxelSizeFunctionEnabled() {
        return this.voxelSizeFunctionEnabled;
    }

    public void setVoxelSizeFunctionEnabled(boolean enabled) {
        this.voxelSizeFunctionEnabled = enabled;
    }

    public float getMaxPAD() {
        return this.maxPAD;
    }

    public void setMaxPAD(float maxPAD) {
        this.maxPAD = maxPAD;
    }

    public float getAlpha() {
        return this.alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public boolean isMaterialEnabled() {
        return this.materialEnabled;
    }

    public void setMaterialEnabled(boolean enabled) {
        this.materialEnabled = enabled;
    }

    public String getOutputVariable() {
        return this.outputVariable;
    }

    public void setOutputVariable(String variable) {
        this.outputVariable = variable;
    }

    public String getGradientName() {
        return this.gradientName;
    }

    public void setGradientName(String gradientName) {
        this.gradientName = gradientName;
    }

    public Color[] getColors() {
        return getColors(gradientName);
    }

    private Color[] getColors(String gradientName) {

        Class c = ColorGradient.class;
        Field[] fields = c.getFields();

        for (Field field : fields) {

            String type = field.getType().getSimpleName();
            String name = field.getName();
            if (type.equals("Color[]") && name.equals(gradientName)) {
                try {
                    return (Color[]) field.get(c);
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    //java.util.logging.Logger.getLogger(ObjExporterCfg.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        // default gradient
        return ColorGradient.GRADIENT_BLUE_TO_RED;
    }

    public File getInputFile() {
        return inputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public File getMaterialFile() {
        String filename = outputFile.getName();
        int extensionBeginIndex = filename.lastIndexOf(".");
        return new File(outputFile.getParent(), filename.substring(0, extensionBeginIndex) + ".mtl");
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public Release[] getReleases() {
        return null;
    }

}
