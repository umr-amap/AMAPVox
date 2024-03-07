/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.amapvox.gui;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author pverley
 */
public class CfgFile {

    private File file;
    private String name;
    private boolean deprecated;
    private final BooleanProperty savedProperty = new SimpleBooleanProperty(false);

    public CfgFile() {
    }

    public CfgFile(File file) {
        this.file = file;
        this.name = file.getName();
        savedProperty.setValue(true);
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    final public ReadOnlyBooleanProperty savedProperty() {
        return savedProperty;
    }

    public void setSaved() {
        savedProperty.setValue(true);
    }
    
    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }
    
    public boolean isDeprecated() {
        return deprecated;
    }

    public static CfgFile create(int index) {

        try {
            CfgFile cfgFile = new CfgFile();
            cfgFile.file = File.createTempFile("amapvox-", ".xml");
            cfgFile.name = "Untitled " + index;
            return cfgFile;
        } catch (IOException ex) {
            Logger.getLogger(CfgFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.file);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CfgFile other = (CfgFile) obj;
        return Objects.equals(this.file, other.file);
    }
    

}
