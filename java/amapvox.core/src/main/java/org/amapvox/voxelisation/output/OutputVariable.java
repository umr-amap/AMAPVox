/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation.output;

import java.util.Arrays;
import ucar.ma2.DataType;

/**
 *
 * @author pverley
 */
public enum OutputVariable {

    I_INDEX("i", new String[]{"i"}, "i index", DataType.INT, "scalar", true, true, false),
    J_INDEX("j", new String[]{"j"}, "j index", DataType.INT, "scalar", true, true, false),
    K_INDEX("k", new String[]{"k"}, "k index", DataType.INT, "scalar", true, true, false),
    GROUND_DISTANCE("groundDistance", new String[]{"ground_distance"}, "Ground distance", DataType.FLOAT, "m"),
    NUMBER_OF_ECHOES("nhit", new String[]{"nbEchos"}, "Number of hits", DataType.INT, "scalar"),
    NUMBER_OF_SHOTS("npulse", new String[]{"nbSampling"}, "Number of pulses", DataType.INT, "scalar"),
    TOTAL_LENGTH("pathLength", new String[]{"lgTotal"}, "Cumulated path length", DataType.FLOAT, "m"),
    MEAN_TOTAL_LENGTH("averagedPathLength", new String[]{"lMeanTotal"}, "Mean path length", DataType.FLOAT, "m"),
    SD_LENGTH("pathLengthStDev", new String[]{"sdLength"}, "Path length standard deviation", DataType.FLOAT, "m2", false, false, false),
    MEAN_ANGLE("averagedPulseAngle", new String[]{"angleMean"}, "Mean pulse angle", DataType.FLOAT, "degree"),
    POTENTIAL_BEAM_SURFACE("potentialBeamSection", new String[]{"bsPotential", "bvPotential"}, "Potential beam surface", DataType.FLOAT, "m2"),
    ENTERING_BEAM_SURFACE("enteringBeamSection", new String[]{"bsEntering", "bvEntering"}, "Entering beam surface", DataType.FLOAT, "m2"),
    INTERCEPTED_BEAM_SURFACE("interceptedBeamSection", new String[]{"bsIntercepted", "bvIntercepted"}, "Intercepted beam surface", DataType.FLOAT, "m2"),
    ESTIMATED_TRANSMITTANCE("transmittance", new String[]{"transmittance"}, "Estimated transmittance", DataType.FLOAT, "dimensionless", false, false, false),
    ATTENUATION_FPL_BIASED_MLE("attenuation_FPL_biasedMLE", new String[]{"attenuation_FPL_biasedMLE", "attenuation"}, "Attenuation biased maximum likelihood estimator based on free path length(FPL MLE, F. Pimont)", DataType.FLOAT, "dimensionless"),
    ATTENUATION_FPL_BIAS_CORRECTION("attenuation_FPL_biasCorrection", new String[]{"attenuation_FPL_biasCorrection", "attenuationBiasCorrection", "attenuation_biasCorr"}, "Bias correction factor for attenuation maximum likelihood estimator based on free path length (FPL MLE, F. Pimont)", DataType.FLOAT, "dimensionless"),
    ATTENUATION_FPL_UNBIASED_MLE("attenuation_FPL_unbiasedMLE", new String[]{"attenuation_FPL_unbiasedMLE"}, "Attenuation unbiased maximum likelihood estimator based on free path length (FPL MLE, F. Pimont)", DataType.FLOAT, "dimensionless"),
    WEIGHTED_EFFECTIVE_FREEPATH("weightedEffectiveFreepathLength", new String[]{"weightedEffectiveFreepathLength"}, "Sum of effective free path length weighted by beam section", DataType.FLOAT, "m3"),
    WEIGHTED_FREEPATH("weightedFreepathLength", new String[]{"weightedFreepathLength"}, "Sum of free path length weighted by beam section", DataType.FLOAT, "m3"),
    ATTENUATION_PPL_MLE("attenuation_PPL_MLE", new String[]{"attenuation_PPL_MLE"}, "Attenuation maximum likelihood estimator based on potential path length (PPL MLE, G. Vincent)", DataType.FLOAT, "dimensionless"),
    DIST_LASER("averagedLaserDistance", new String[]{"distLaser"}, "Mean distance to laser", DataType.FLOAT, "meter"),
    EXPLORATION_RATE("explorationRate", new String[]{"explorationRate"}, "Subvoxel exploration rate", DataType.FLOAT, "dimensionless", false, false, false),
    // deprecated variables
    PLANT_AREA_DENSITY("plantAreaDensity", new String[]{"PadBVTotal"}, "Plant Area Density", DataType.FLOAT, "dimensionless", false, false, true),
    SUBSAMPLING("subSampling", new String[]{"subSampling"}, "Sampling intensity at sub voxel scale", DataType.STRING, "binary", false, false, true);

    private final String[] shortNames;
    private final String longName;
    private final String variableName;
    private final DataType type;
    private final String units;
    private final boolean coordinateVariable;
    private final boolean enabledByDefault;
    private final boolean deprecated;

    private OutputVariable(String variableName, String[] shortNames, String longName, DataType type, String unit, boolean coordinateVariable, boolean enabledByDefault, boolean deprecated) {
        this.variableName = variableName;
        this.shortNames = shortNames;
        this.longName = longName;
        this.type = type;
        this.units = unit;
        this.coordinateVariable = coordinateVariable;
        this.enabledByDefault = enabledByDefault;
        this.deprecated = deprecated;
    }

    private OutputVariable(String variableName, String[] shortNames, String longName, DataType type, String unit) {
        this(variableName, shortNames, longName, type, unit, false, true, false);
    }

    public String getVariableName() {
        return variableName;
    }

    public String getShortName() {
        return shortNames[0];
    }

    /**
     * @return the longName
     */
    public String getLongName() {
        return longName;
    }

    /**
     * @return the type
     */
    public DataType getType() {
        return type;
    }

    /**
     * @return the unit
     */
    public String getUnits() {
        return units;
    }

    public boolean isCoordinateVariable() {
        return coordinateVariable;
    }

    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }
    
    public boolean isDeprecated() {
        return deprecated;
    }

    static public OutputVariable find(String variable) {

        String var = variable.trim();
        for (OutputVariable ovar : OutputVariable.values()) {
            for (String shortName : ovar.shortNames) {
                if (variable.trim().equalsIgnoreCase(shortName)) {
                    return ovar;
                }
            }
        }
        throw new NullPointerException("Undefined output variable " + variable + ". Must be one of " + Arrays.toString(OutputVariable.values()));
    }

    public boolean isValidShortName(String aShortName) {

        for (String shortName : shortNames) {
            if (aShortName.equalsIgnoreCase(shortName)) {
                return true;
            }
        }
        return false;
    }

}
