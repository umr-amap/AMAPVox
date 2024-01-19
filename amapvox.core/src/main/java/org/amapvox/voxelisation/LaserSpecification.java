/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Julien Heurtebize
 */
public class LaserSpecification {

    public final static LaserSpecification LMS_Q560 = new LaserSpecification("LMS_Q560", 0.0003d, 0.0005d);
    public final static LaserSpecification LMS_Q780 = new LaserSpecification("LMS_Q780", 0.005d, 0.00025d);
    public final static LaserSpecification VZ_400 = new LaserSpecification("VZ_400/VZ_400i", new String[] {"VZ_400"}, 0.007d, 0.00035d);
    public final static LaserSpecification LEICA_SCANSTATION_P30_40 = new LaserSpecification("LEICA_SCANSTATION_P30_40", 0.0035d, 0.00023d);
    public final static LaserSpecification LEICA_SCANSTATION_C10 = new LaserSpecification("LEICA_SCANSTATION_C10", 0.004d, 0.0001d);
    public final static LaserSpecification FARO_FOCUS_X330 = new LaserSpecification("FARO_FOCUS_X330", 0.0025d, 0.00019d);
    public final static LaserSpecification MINIVUX1UAV = new LaserSpecification("miniVUX-1UAV", 0.0145, 0.00105d);
    public final static LaserSpecification UNITARY_BEAM_SECTION = new LaserSpecification("Unitary beam section", 0.d, 0.d);
    
    private final double beamDiameterAtExit;
    private final double beamDivergence;
    private final String name;
    private final String[] deprecatedNames;

    public LaserSpecification(String name, String[] deprecatedNames, double beamDiameterAtExit, double beamDivergence) {
        this.beamDiameterAtExit = beamDiameterAtExit;
        this.beamDivergence = beamDivergence;
        this.name = name;
        this.deprecatedNames = deprecatedNames;
    }

    public LaserSpecification(String name, double beamDiameterAtExit, double beamDivergence) {
        this(name, null, beamDiameterAtExit, beamDivergence);
    }

    public LaserSpecification(double beamDiameterAtExit, double beamDivergence) {
        this("custom", null, beamDiameterAtExit, beamDivergence);
    }

    public double getBeamDiameterAtExit() {
        return beamDiameterAtExit;
    }

    public double getBeamDivergence() {
        return beamDivergence;
    }

    public String getName() {
        return name;
    }
    
    public boolean isValidName(String aName) {
        boolean oldName = false;
        if (null != deprecatedNames) {
            oldName = Arrays.asList(deprecatedNames).stream().anyMatch( dName -> dName.equalsIgnoreCase(aName));
        }
        return oldName || aName.equalsIgnoreCase(name);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Laser ").append(name).append("\n");
        str.append("  beam diameter at exit (meter) ").append((float) beamDiameterAtExit).append("\n");
        str.append("  beam divergence (radian) ").append((float) beamDivergence).append("\n");
        return str.toString();
    }

    public static List<LaserSpecification> getPresets() {

        List<LaserSpecification> presets = new ArrayList<>();

        Field[] declaredFields = LaserSpecification.class.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.getType() == LaserSpecification.class) {
                try {
                    presets.add((LaserSpecification) field.get(null));
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                }
            }
        }

        return presets;
    }

}
