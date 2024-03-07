/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation;

import org.amapvox.commons.raster.asc.Raster;
import org.amapvox.commons.util.CallableTaskAdapter;
import org.amapvox.commons.Voxel;
import org.amapvox.commons.AVoxTask;
import org.amapvox.commons.FinalCounter;
import org.amapvox.voxelisation.las.LasVoxelization;
import org.amapvox.voxelisation.gridded.PTGVoxelization;
import org.amapvox.voxelisation.gridded.PTXVoxelization;
import org.amapvox.voxelisation.txt.ShotVoxelization;
import org.amapvox.voxelisation.output.AbstractOutput;
import org.amapvox.voxelisation.output.OutputVariable;
import org.amapvox.voxelisation.output.PathLengthOutput;
import org.amapvox.voxelisation.output.VoxelFileOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import org.apache.log4j.Logger;

/**
 *
 * @author pverley
 */
public class VoxelizationTask extends AVoxTask {

    private final static Logger LOGGER = Logger.getLogger(VoxelizationTask.class);

    private final List<AbstractVoxelization> tasks;
    private DigitalTerrainModelManager dtmManager;
    private VoxelSpace vxsp;

    private double transmErr;
    private double transmErrFallback;
    private double maxAttenuation;
    private double attenuationError;
    private final static double EPSILON = 1e-5;
    // outputs
    protected List<AbstractOutput> outputs;
    private PathLengthOutput pathLengthOutput;

    public VoxelizationTask(File file, int ncpu) {
        super(file, ncpu);
        tasks = new ArrayList<>();
    }

    @Override
    public String getName() {
        return "Voxelisation";
    }

    @Override
    protected Class<VoxelizationCfg> getConfigurationClass() {
        return VoxelizationCfg.class;
    }

    @Override
    protected void doInit() throws Exception {

        VoxelizationCfg mainCfg = (VoxelizationCfg) getConfiguration();
        tasks.clear();

        // create voxelisation class
        Class<?> voxelisationClass;
        switch (mainCfg.getLidarType()) {
            case RXP:
            case RSP:
                voxelisationClass = RXPVoxelization.class;
                break;
            case PTG:
                voxelisationClass = PTGVoxelization.class;
                break;
            case PTX:
                voxelisationClass = PTXVoxelization.class;
                break;
            case XYB:
                voxelisationClass = XYBVoxelization.class;
                break;
            case SHT:
                voxelisationClass = ShotVoxelization.class;
                break;
            case LAS:
            case LAZ:
                voxelisationClass = LasVoxelization.class;
                break;
            default:
                throw new IllegalArgumentException("Wrong input file type");
        }

        // load DTM
        if (null != mainCfg.getDTMFile()) {
            dtmManager = new DigitalTerrainModelManager(
                    mainCfg.getDTMFile(),
                    mainCfg.isDTMUseVopMatrix() ? mainCfg.getVopMatrix() : null
            );
            LOGGER.info("Loading DTM file " + dtmManager.getFile());
            dtmManager.init();
        }

        // create voxel space
        vxsp = new VoxelSpace(
                mainCfg.getDimension(),
                mainCfg.getMinCorner(),
                mainCfg.getVoxelSize(),
                getDTM());

        // error for the numerical estimation of the transmittance
        transmErr = mainCfg.getTrNumEstimError();
        // fallback error for the numerical estimation of the transmittance
        transmErrFallback = mainCfg.getTrNumEstimFallbackError();
        // maximal attenuation
        maxAttenuation = mainCfg.getMaxAttenuation();
        attenuationError = mainCfg.getAttenuationError();
        // outputs
        outputs = new ArrayList<>();
        // voxels output
        outputs.add(new VoxelFileOutput(this, mainCfg, vxsp, mainCfg.isVoxelOutputEnabled()));
        // path length output
        pathLengthOutput = new PathLengthOutput(this, mainCfg, vxsp, mainCfg.isPathLengthEnabled());
        if (pathLengthOutput.isEnabled()) {
            pathLengthOutput.init();
        }
        outputs.add(pathLengthOutput);

        // loop over single scans
        FinalCounter nDone = new FinalCounter(0);
        int nScan = mainCfg.getLidarScans().size();
        if (0 == nScan) {
            throw new IOException("There is not any LiDAR scan declared in the configuration file (input>scans>scan element).");
        }

        for (int iscan = 0; iscan < nScan; iscan++) {
            // add current scan voxelization to the list of configurations
            Class<?>[] types = new Class[]{VoxelizationTask.class, VoxelizationCfg.class, int.class};
            AbstractVoxelization voxelisation = (AbstractVoxelization) voxelisationClass
                    .getConstructor(types)
                    .newInstance(this, mainCfg, iscan);
            voxelisation.init();
            voxelisation.addCallableTaskListener(new CallableTaskAdapter() {
                @Override
                public void onSucceeded() {
                    nDone.increment();
                    String msg = "Voxelization, scan " + nDone.getValue() + "/" + nScan;
                    VoxelizationTask.this.fireProgress(msg, nDone.getValue(), nScan);
                }
            });
            //voxelisation.addProcessingListener(this);
            tasks.add(voxelisation);
        }
    }

    public Raster getDTM() {
        return null != dtmManager ? dtmManager.getDTM() : null;
    }

    public VoxelSpace getVoxelSpace() {
        return vxsp;
    }

    public PathLengthOutput getPathLengthOutput() {
        return pathLengthOutput;
    }

    @Override
    public File[] call() throws Exception {

        LOGGER.info("Voxelization started");

        // list of output files
        ArrayList<File> outputFiles = new ArrayList<>();

        if (!tasks.isEmpty()) {
            try {
                // special case single task
                if (tasks.size() == 1) {
                    tasks.get(0).call();
                } else {
                    // run batch of voxelisation tasks
                    ExecutorService exec = Executors.newFixedThreadPool(Math.min(getNCPU(), tasks.size()));
                    List<Future<Object>> results = exec.invokeAll(tasks);
                    for (Future<Object> result : results) {
                        result.get();
                    }
                }

                if (isCancelled()) {
                    // returns what has been done so far
                    return outputFiles.toArray(new File[outputFiles.size()]);
                }

                // post processing 
                postProcess();

                if (isCancelled()) {
                    // returns what has been done so far
                    return outputFiles.toArray(new File[outputFiles.size()]);
                }

                // write outputs
                for (AbstractOutput output : outputs) {
                    if (output.isEnabled()) {
                        outputFiles.addAll(Arrays.asList(output.write()));
                    }
                }

                LOGGER.info("Voxelization completed (" + tasks.size() + " scans)");

            } catch (InterruptedException | NullPointerException ex) {
                fireCancelled();
                throw ex;
            }
        }

        return outputFiles.toArray(new File[outputFiles.size()]);
    }

    @Override
    public void setCancelled(boolean cancelled) {
        // propagate cancellation to sub tasks
        tasks.forEach((task) -> {
            task.setCancelled(cancelled);
        });
        super.setCancelled(cancelled);
    }

    public void postProcess() {

        LOGGER.info("[Voxelization] Post-processing, please wait...");

        VoxelizationCfg cfg = (VoxelizationCfg) getConfiguration();

        // enabled output variables
        boolean numEstimTransmEnabled = cfg.isOutputVariableEnabled(OutputVariable.ESTIMATED_TRANSMITTANCE);
        boolean angleEnabled = cfg.isOutputVariableEnabled(OutputVariable.MEAN_ANGLE);
        boolean meanPathLengthEnabled = cfg.isOutputVariableEnabled(OutputVariable.MEAN_TOTAL_LENGTH);
        boolean attenuation_FPL_MLE_enabled = cfg.isOutputVariableEnabled(OutputVariable.ATTENUATION_FPL_BIASED_MLE)
                || cfg.isOutputVariableEnabled(OutputVariable.ATTENUATION_FPL_BIAS_CORRECTION)
                || cfg.isOutputVariableEnabled(OutputVariable.ATTENUATION_FPL_UNBIASED_MLE);
        boolean attenuation_PL_MLE_enabled = cfg.isOutputVariableEnabled(OutputVariable.ATTENUATION_PPL_MLE);
        boolean averagedLaserDistanceEnabled = cfg.isOutputVariableEnabled(OutputVariable.DIST_LASER);
        boolean pathLengthStDevEnabled = cfg.isOutputVariableEnabled(OutputVariable.SD_LENGTH);
        boolean subSamplingEnabled = cfg.isOutputVariableEnabled(OutputVariable.EXPLORATION_RATE);

        int nFallback = 0;
        int nvoxel = cfg.getDimension().x * cfg.getDimension().y * cfg.getDimension().z;
        int ivoxel = 1;
        double subSamplingMax = Math.pow(cfg.getSubVoxelSplit(), 3);
        for (int i = 0; i < cfg.getDimension().x; i++) {
            for (int j = 0; j < cfg.getDimension().y; j++) {
                for (int k = 0; k < cfg.getDimension().z; k++) {
                    if (isCancelled()) {
                        LOGGER.info("[Voxelization] Post-processing cancelled");
                        return;
                    }
                    fireProgress("[Voxelization] Post-processing...", ivoxel++, nvoxel);
                    Voxel voxel = vxsp.getVoxel(i, j, k);
                    // empty voxel, no post-processing
                    if (null == voxel) {
                        continue;
                    }
                    // unsampled voxel, no post-processing
                    // (may have a non zero potential beam surface though, so it
                    // may not be empty)
                    if (voxel.npulse == 0) {
                        // set to NaN some of the variables
                        voxel.empty();
                        continue;
                    }

                    // mean angle
                    if (angleEnabled) {
                        voxel.averagedPulseAngle = voxel.averagedPulseAngle / voxel.npulse;
                    }

                    // mean path length
                    if (meanPathLengthEnabled) {
                        voxel.averagedPathLength = voxel.pathLength / voxel.npulse;
                    }

                    if (pathLengthStDevEnabled) {
                        // phv 20190617 Welford’s method for computing variance (and sd)
                        voxel.pathLengthStDev = voxel.npulse > 1
                                ? Math.sqrt(voxel.pathLengthStDev / (voxel.npulse - 1))
                                : 0.d;
                    }

                    // numerical estimation of the transmittance
                    if (numEstimTransmEnabled) {
                        voxel.transmittance = estimateTransmittance(voxel);
                        if (voxel.fallbackTransm) {
                            nFallback += 1;
                        }
                    }

                    // averaged distance to laser
                    if (averagedLaserDistanceEnabled) {
                        voxel.averagedLaserDistance = voxel.averagedLaserDistance / voxel.npulse;
                    }

                    // attenuation estimator (F. Pimont's method)
                    if (attenuation_FPL_MLE_enabled) {
                        if (voxel.weightedEffectiveFreepathLength > 0.d) {
                            voxel.attenuation_FPL_biasCorrection *= (voxel.enteringBeamSection / Math.pow(voxel.weightedEffectiveFreepathLength, 2));
                            voxel.attenuation_FPL_biasCorrection /= voxel.npulse; //FP FIX
                            voxel.attenuation_FPL_biasedMLE = voxel.interceptedBeamSection / voxel.weightedEffectiveFreepathLength;
                            voxel.attenuation_FPL_unbiasedMLE = voxel.attenuation_FPL_biasedMLE - voxel.attenuation_FPL_biasCorrection;
                        } else {
                            voxel.attenuation_FPL_biasCorrection = Double.NaN;
                            voxel.attenuation_FPL_biasedMLE = Double.NaN;
                        }
                    }

                    // attenuation estimator (G. Vincent's method)
                    if (attenuation_PL_MLE_enabled) {
                        voxel.attenuation_PPL_MLE = estimateAttenuation(voxel);
                    }

                    // sub voxel sampling
                    if (subSamplingEnabled) {
                        voxel.explorationRate = voxel.subSampling.bitCount() / (double) subSamplingMax;
                    }
                }
            }
        }

        if (nFallback > 0) {
            StringBuilder sb = new StringBuilder();
            float nVoxel = cfg.getDimension().x * cfg.getDimension().y * cfg.getDimension().z;
            float fraction = 100.f * nFallback / nVoxel;
            sb.append("Transmittance computation switched to fallback mode (lower precision) for ")
                    .append(nFallback).append("/").append((int) nVoxel).append(" voxels")
                    .append(" (~").append(String.format("%.1f", fraction)).append("%)");
            LOGGER.warn("[Voxelization] " + sb.toString());
        }
    }

    private double estimateAttenuation(Voxel voxel) {

        // unsampled voxel
        if ((voxel.enteringBeamSection == 0)) {
            return Double.NaN;
        }

        // no light interception, attenuation = 0
        if (voxel.interceptedBeamSection == 0) {
            return 0.d;
        }

        double k = Double.NaN;
        double kMin = 0.d;
        double kMax = maxAttenuation;
        double incr = (kMax - kMin) / 10;

        double err = Double.MAX_VALUE;
        int n = 0;
        boolean makingProgress = true;
        while ((err > attenuationError) && makingProgress && (n < 200)) {
            makingProgress = false;
            for (double kTmp = kMin; kTmp <= kMax; kTmp += incr) {
                double errTmp = errAttenuationEstimate(kTmp, voxel.weightedPathLength, voxel.streamAttenuationRecords());
                n++;
                if (errTmp < err) {
                    err = errTmp;
                    k = kTmp;
                    makingProgress = true;
                }
            }
            kMin = Math.max(k - incr, kMin);
            kMax = Math.min(k + incr, kMax);
            incr /= 10.d;
        }
        return k;
    }

    private static double errAttenuationEstimate(double k, double wPathLength, Stream<Voxel.FreepathRecord> attenuationRecords) {

        double wInterceptedPathLength = attenuationRecords.mapToDouble(
                attenuationRecord -> {
                    double exp = Math.exp(-k * attenuationRecord.pathLength);
                    return exp != 1.d
                            ? (attenuationRecord.bsIn * attenuationRecord.pathLength) * exp / (1.d - exp)
                            : 0.d;
                }).sum();

        return Math.abs(wInterceptedPathLength - wPathLength);
    }

    private double estimateTransmittance(Voxel voxel) {

        double bsOut = voxel.enteringBeamSection - voxel.interceptedBeamSection;

        // unsampled voxel
        if ((voxel.enteringBeamSection == 0)) {
            return Double.NaN;
        }

        // no light interception, transmittance = 1
        if (voxel.interceptedBeamSection == 0) {
            return 1.d;
        }

        // no light transmitted, transmittance = 0
        if (Math.abs(bsOut) < EPSILON) {
            return 0.d;
        }

        double tr = Double.NaN;

        if (voxel.fallbackTransm) {
            // fallback computation of the transmittance
            double err = Double.MAX_VALUE;
            int nTrEstim = voxel.trNumBsOut.length;
            for (int i = 0; i < nTrEstim; i++) {
                double errTmp = Math.abs(voxel.trNumBsOut[i] - bsOut);
                if (errTmp < err) {
                    tr = (i + 1) * transmErrFallback;
                    err = errTmp;
                }
            }
        } else {
            double trMin = 0.d;
            double trMax = 1.d;
            double incr = 0.1;

            double err = Double.MAX_VALUE;
            int n = 0;
            while (err > transmErr && n < 200) {
                for (double trTmp = trMin; trTmp <= trMax; trTmp += incr) {
                    double errTmp = errTransmittanceEstimate(trTmp, bsOut, voxel.streamTransmittanceRecords());
                    n++;
                    if (errTmp < err) {
                        err = errTmp;
                        tr = trTmp;
                    }
                }
                trMin = Math.max(tr - incr, 0.d);
                trMax = Math.min(tr + incr, 1.d);
                incr /= 10.d;
            }
        }

        return tr;
    }

    private static double errTransmittanceEstimate(double tr, double bsOut, Stream<Voxel.FreepathRecord> trRecords) {

        double cumBsIn = trRecords
                .mapToDouble(trRecord -> trRecord.bsIn * Math.pow(tr, trRecord.pathLength))
                .sum();

        return Math.abs(cumBsIn - bsOut) / bsOut;
    }
}
