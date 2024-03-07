package org.amapvox.commons;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 * @author Philippe Verley
 */
public class Voxel {

    /**
     * x-axis voxel center coordinate
     */
    public int i;

    /**
     * y-axis voxel center coordinate
     */
    public int j;
    /**
     * z-axis voxel center coordinate
     */
    public int k;
    /**
     * number of pulses going through the voxel (sampling intensity)
     */
    public int npulse;
    /**
     * number of hits
     */
    public int nhit;
    /**
     * sum of free paths
     */
    public double pathLength;
    /**
     * distance from voxel center to ground level
     */
    public float groundDistance;
    /**
     * averaged free path (sum free paths / number of pulses)
     */
    public double averagedPathLength;
    /**
     * numerically estimated transmittance
     */
    public double transmittance;
    /**
     * averaged pulse angle
     */
    public double averagedPulseAngle;
    /**
     * entering beam section (fraction of pulse * cross section of pulse at
     * voxel center)
     */
    public double enteringBeamSection;
    /**
     * intercepted beam section (fraction of pulse * cross section of pulse at
     * voxel center)
     */
    public double interceptedBeamSection;
    /**
     * potential beam section (fraction of pulse * cross section of pulse at
     * voxel center)
     */
    public double potentialBeamSection;

    // transmittance
    public double[] trNumBsOut;
    // variable for numerical estimation of the transmittance
    final private List<FreepathRecord> transmittanceRecords;
    public boolean fallbackTransm = false;

    /**
     * averaged distance to laser
     */
    public double averagedLaserDistance;
    /**
     * free path length standard deviation
     */
    public double pathLengthStDev;
    /**
     * attenuation biased maximum likelihood estimator based on free path length
     * (F.Pimont)
     */
    public double attenuation_FPL_biasedMLE;
    /**
     * Bias correction factor for Pimont's attenuation estimator
     */
    public double attenuation_FPL_biasCorrection;
    /*
     * attenuation unbiased maximum likelihood estimator based on free path length
     * unbiased_k = biased_k - bias_corr
     */
    public double attenuation_FPL_unbiasedMLE;
    /*
     * effective weighted freepath sum (denominator of Pimont's biased attenuation estimator)
     */
    public double weightedEffectiveFreepathLength;
    public double weightedFreepathLength;

    // sampling intensity at sub voxel scale
    public BigInteger subSampling;
    /**
     * sub voxel exploration rate
     */
    public double explorationRate;
    /**
     * Attenuation maximum likelihood estimator based on potential path length
     * (G.Vincent)
     */
    public double attenuation_PPL_MLE;
    // variable for numerical estimation of the attenuation (G.Vincent)
    final private List<FreepathRecord> attenuationRecords;
    public double weightedPathLength;

    /**
     *
     * @param i
     * @param j
     * @param k
     */
    public Voxel(int i, int j, int k) {

        this.i = i;
        this.j = j;
        this.k = k;
        transmittanceRecords = Collections.synchronizedList(new ArrayList<>());
        attenuationRecords = Collections.synchronizedList(new ArrayList<>());
        subSampling = BigInteger.ZERO;
    }

    /**
     * Set some variables to NaN (zero could be ambiguous as it does not convey
     * the same meaning than NaN).
     */
    public void empty() {

        averagedPulseAngle = Double.NaN;
        transmittance = Double.NaN;
        averagedLaserDistance = Double.NaN;
        attenuation_FPL_biasCorrection = Double.NaN;
        attenuation_FPL_biasedMLE = Double.NaN;
        attenuation_FPL_unbiasedMLE = Double.NaN;
        attenuation_PPL_MLE = Double.NaN;
        explorationRate = Double.NaN;
        subSampling = BigInteger.ZERO;
    }

    /**
     * Set variables such as if no vegetation in voxel.
     */
    public void clean() {

        nhit = 0;
        interceptedBeamSection = 0.d;
        transmittance = 1.d;
        attenuation_FPL_biasCorrection = 0.d;
        attenuation_FPL_biasedMLE = 0.d;
        attenuation_FPL_unbiasedMLE = 0.d;
        attenuation_PPL_MLE = 0.d;
    }

    public String variablesToString(String[] names, DecimalFormat dnf) {

        // voxel attributes to String
        StringBuilder voxelSB = new StringBuilder();
        for (String name : names) {
            try {
                Field field = Voxel.class.getField(name);
                switch (field.getType().getName()) {
                    case "double":
                    case "float":
                        double value = field.getDouble(this);
                        voxelSB.append(Double.isNaN(value) ? Double.NaN : dnf.format(value));
                        break;
                    case "int":
                        voxelSB.append(field.getInt(this));
                        break;
                    case "java.lang.String":
                        voxelSB.append((String) field.get(this));
                        break;
//                    case "java.math.BigInteger":
//                        voxelSB.append(((BigInteger) field.get(this)).toString(36));
//                        break;
                }
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                voxelSB.append(Double.NaN);
            }
            voxelSB.append(" ");
        }

        return voxelSB.toString().trim();
    }

    public Stream<FreepathRecord> streamTransmittanceRecords() {
        return transmittanceRecords.stream();
    }
    
    public int sizeTransmittanceRecords() {
        return transmittanceRecords.size();
    }
    
    public void clearTransmittanceRecords() {
        transmittanceRecords.clear();
    }

    public void addTransmittanceRecord(double bsIn, double pathLength) {
        transmittanceRecords.add(new FreepathRecord(bsIn, pathLength));
    }

    public Stream<FreepathRecord> streamAttenuationRecords() {
        return attenuationRecords.stream();
    }

    public void addAttenuationRecord(double bsIn, double pathLength) {
        attenuationRecords.add(new FreepathRecord(bsIn, pathLength));
    }

    public void setFieldValue(Class< ? extends Voxel> c, String fieldName, Object object, String value)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        Field f = c.getField(fieldName);
        Class<?> type = f.getType();

        switch (type.getName()) {
            case "double":
                f.setDouble(object, Double.valueOf(value));
                break;
            case "float":
                f.setFloat(object, (float) Float.valueOf(value));
                break;
            case "int":
                f.setInt(object, Integer.valueOf(value));
                break;
            case "java.math.BigInteger":
                f.set(object, new BigInteger(value, 36));
        }
    }

    public double getFieldValue(Class< ? extends Voxel> c, String fieldName, Object o)
            throws SecurityException, NoSuchFieldException, IllegalAccessException {

        return c.getField(fieldName).getDouble(o);
    }

    public class FreepathRecord {

        public final double bsIn;
        public final double pathLength;

        private FreepathRecord(double bsIn, double pathLength) {
            this.bsIn = bsIn;
            this.pathLength = pathLength;
        }
    }
}
