package org.amapvox.voxelisation;

import org.amapvox.commons.raster.asc.Raster;
import org.amapvox.commons.util.Cancellable;
import org.amapvox.commons.util.IteratorWithException;
import org.amapvox.commons.util.TimeCounter;
import org.amapvox.commons.util.filter.Filter;
import org.amapvox.shot.Shot;
import org.amapvox.commons.Voxel;
import org.amapvox.voxelisation.output.OutputVariable;
import org.amapvox.voxelisation.output.PathLengthOutput;
import org.amapvox.commons.raytracing.geometry.LineElement;
import org.amapvox.commons.raytracing.geometry.LineSegment;
import org.amapvox.commons.raytracing.voxel.VoxelManager;
import org.amapvox.commons.raytracing.voxel.VoxelManager.VoxelCrossingContext;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3i;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.amapvox.shot.Echo;
import org.amapvox.shot.weight.EchoWeight;
import org.apache.log4j.Logger;

public class Voxelization extends org.amapvox.commons.util.Process implements Cancellable {

    private final static Logger LOGGER = Logger.getLogger(Voxelization.class);
    private final static double EPSILON = 1e-5;
    private final static double SHOT_MAX_DISTANCE = 10000;

    final private String logHeader;
    private int nMaxCrossedVoxel;

    private boolean cancelled;

    private int nShotProcessed;
    private int nShotDiscarded;
    private int iShot;
    private int nShotOut;

    private VoxelManager voxelManager;
    private VoxelManager subVoxelManager;
    private int subVoxelSplit;
    private boolean subSamplingEnabled;

    private boolean constantBeamSection;

    private final VoxelizationCfg cfg;
    private final Raster dtm;
    private final VoxelSpace vxsp;

    private LaserSpecification laserSpec;

    private final List<Filter<Shot>> shotFilters;
    private final List<Filter<Echo>> echoFilters;
    private final List<EchoWeight> echoWeights;
    private EchoProperties currentEchoProperties;

    private boolean padEnabled;
    private double transmErrFallback;
    private int nTrRecordMax;
    private boolean numEstimTransmEnabled;
    private boolean angleEnabled;
    private boolean meanPathLengthEnabled;
    private boolean pathLengthEnabled;
    private boolean potentialBeamSectionEnabled;
    private boolean attenuation_PL_MLE_enabled;

    private PathLengthOutput pathLengthOutput;

    private boolean averagedLaserDistanceEnabled;
    private boolean pathLengthStDevEnabled;

    private double lambda1;

    public Voxelization(VoxelizationCfg cfg, String logHeader,
            Raster dtm, VoxelSpace vxsp, PathLengthOutput pathLengthOutput) throws Exception {

        // voxelisation parameters
        this.cfg = cfg;

        this.logHeader = logHeader;

        // digital terrain model
        this.dtm = dtm;

        // voxel space
        this.vxsp = vxsp;

        //
        this.pathLengthOutput = pathLengthOutput;

        // user defined shot filters
        shotFilters = cfg.getShotFilters();

        // echo filters
        this.echoFilters = cfg.getEchoFilters();

        // echo weight
        this.echoWeights = cfg.getEchoWeights();
    }

    public String logHeader() {
        return logHeader;
    }

    public void voxelization(IteratorWithException<Shot> it, int nShot) throws Exception {

        fireProgress(logHeader, 0, 100);

        long startTime = System.currentTimeMillis();

        init();

        int length = String.valueOf(nShot).length();
        int modulo = nShot > 0 ? (int) Math.max(Math.pow(10, length) / 100, 1) : 1000000;
        float percent = 0.f;
        while (it.hasNext()) {
            // check for cancellation
            if (isCancelled()) {
                LOGGER.info(logHeader + " cancelled");
                return;
            }
            // progress bar
            if (nShot > 0) {
                percent = 100.f * iShot / nShot;
                fireProgress(logHeader + " " + String.format("%.1f", percent) + "%", iShot, nShot);
            }
            // print progress message
            if ((iShot % modulo) == 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(" Shot ").append(iShot);
                if (nShot > 0) {
                    sb.append(", ").append(String.format("%.1f", percent)).append("%");
                }
                sb.append(" (processed ").append(nShotProcessed);
                sb.append(", discarded ").append(nShotDiscarded);
                sb.append(", out ").append(nShotOut).append(")");
                LOGGER.info(logHeader + " " + sb.toString());
            }
            processOneShot(it.next());
        }

        LOGGER.info(logHeader + " Shots summary: processed " + nShotProcessed + ", discarded " + nShotDiscarded + ", out " + nShotOut);

        fireFinished(TimeCounter.getElapsedTimeInSeconds(startTime));

        LOGGER.info(logHeader + " Voxelisation completed");
    }

    public List<Filter<Shot>> getShotFilters() {
        return shotFilters;
    }

    public List<Filter<Echo>> getEchoFilters() {
        return echoFilters;
    }

    public void processOneShot(Shot shot) throws Exception {

        iShot++;

        if (retainShot(shot)) {

            // set echoes property
            currentEchoProperties = setEchoProperties(shot);

            if (potentialBeamSectionEnabled) {
                // vegetation free shot propagation (as if no vegetation in the scene)
                freePropagation(shot);
            }

            // shot propagation (with light interception in the scene)
            if (propagation(shot, currentEchoProperties)) {
                // increment number of shots processed
                nShotProcessed++;
            } else {
                nShotOut++;
            }
        } else {
            nShotDiscarded++;
        }
    }

    /**
     * Propagate a {@code shot} in the voxel space ignoring the vegetation in
     * order to estimate a potential beam volume. The method is also in charge
     * of initialising the voxels since any voxel that will be crossed during
     * free propagation may be crossed as well during propagation.
     *
     * @param shot
     */
    private void freePropagation(Shot shot) {

        LineElement shotLine = shotLine(shot);

        // first voxel crossed by the shot
        VoxelCrossingContext context = voxelManager.getFirstVoxelV2(shotLine);

        if (null != context) {
            int nCrossedVoxel = 0;
            do {

                // current voxel
                Point3i vcoord = context.indices;

                Voxel voxel;
                synchronized (voxel = vxsp.getVoxel(vcoord.x, vcoord.y, vcoord.z)) {
                    // stop propagation if current voxel is below the ground
                    if (belowGround(voxel)) {
                        break;
                    }

                    // compute beam surface at voxel centre
                    double beamSurface = constantBeamSection
                            ? 1.d
                            : computeBeamSection(shot.origin, vcoord, laserSpec);

                    // increment potential beam volume
                    voxel.potentialBeamSection += beamSurface;
                }

                // get next voxel
                context = voxelManager.CrossVoxel(shotLine, vcoord);

                nCrossedVoxel++;
                // loop until exiting voxel space
            } while (context.indices != null
                    && nCrossedVoxel <= nMaxCrossedVoxel);
        }
    }

    /**
     * Propagate a Shot inside a given voxel and assess the sampling at sub
     * voxel scale.
     *
     * @param segment, the shot portion to propagate
     * @param vcoord
     * @return a BigInteger with sampling intensity at sub voxel scale.
     */
    private BigInteger subPropagation(Shot shot, Point3i vcoord) {

        LineSegment shotLine = shotLine(shot);
        Vector3d normVec = new Vector3d(voxelManager.getInfCorner(vcoord));
        normVec.negate();
        shotLine.translate(normVec);

        BigInteger subsampling = BigInteger.ZERO;

        // first sub voxel intersected by the shot
        VoxelCrossingContext voxelCrossing = subVoxelManager.getFirstVoxelV2(shotLine);
        if (null != voxelCrossing) {
            do {
                // current sub voxel index
                Point3i subvcoord = voxelCrossing.indices;
                // increment sub voxel sampling
                subsampling = subsampling.setBit(subijkToIndex(subvcoord));
                // next subvoxel
                voxelCrossing = subVoxelManager.CrossVoxel(shotLine, subvcoord);
                // loop until exiting voxel
            } while (null != voxelCrossing.indices);
        }

        return subsampling;
    }

    private int subijkToIndex(Point3i vcoord) {
        return vcoord.z + subVoxelSplit * (vcoord.y + subVoxelSplit * vcoord.x);
    }

    private LineSegment shotLine(Shot shot) {
        return new LineSegment(shot.origin, shot.direction, SHOT_MAX_DISTANCE);
    }

    private boolean propagation(Shot shot, EchoProperties echoProperties) throws Exception {

        // create shot line
        LineSegment shotLine = shotLine(shot);

        // first voxel intersected by the shot, refered as voxel0 in further comments
        VoxelCrossingContext voxelCrossing = voxelManager.getFirstVoxelV2(shotLine);

        if (null != voxelCrossing) {
            // initialise entering beam fraction (100%)
            double beamFractionIn = 1.d;
            // look for first echo inside or beyond voxel0 (if any)
            int rank = 0;
            // by default first echo is null
            Echo echo = null;
            if (echoProperties.nEcho > 0) {
                // look for echoes located before voxel0
                while ((rank < echoProperties.nEcho) && (shot.getRange(rank) < voxelCrossing.length)) {
                    // decrement beam fraction entering voxel0
                    beamFractionIn -= echoProperties.bfIntercepted[rank];
                    rank++;
                }

                if (beamFractionIn < EPSILON) {
                    // no light reaches voxel0 => no propagation inside voxel space
                    return false;
                } else if (rank < echoProperties.nEcho) {
                    // there is at list one echo located inside or beyond voxel0
                    echo = shot.echoes[rank];
                }
                // all the echoes are located before voxel0 but beamFractionIn
                // reaching voxel0 not zero => propagation with echo = null
            }

            // loop over the voxels crossed by the shot
            Point3i vcoord;
            boolean interruptPropagation;
            int nCrossedVoxel = 0;
            do {
                // current voxel coordinates
                vcoord = voxelCrossing.indices;
                int i = vcoord.x;
                int j = vcoord.y;
                int k = vcoord.z;

                // stop propagation if current voxel is below the ground
                if (belowGround(vxsp.getVoxel(i, j, k))) {
                    break;
                }

                // distance from shot origin to shot interception point with current voxel
                double dIn = voxelCrossing.length;
                // get next voxel
                voxelCrossing = voxelManager.CrossVoxel(shotLine, vcoord);
                // distance from shot origin to shot interception point with next voxel
                double dOut = voxelCrossing.length;
                // path length within the voxel
                double pathLength = dOut - dIn;

                // reset intercepted beam fraction in current voxel
                double bfIntercepted = 0.d;

                // initializes list of free paths for current voxel
                List<FreepathVoxel> freePaths = new ArrayList<>(shot.getEchoesNumber());

                // handles echoes that fall inside current voxel
                if (null != echo && isEchoInsideVoxel(echo, vcoord)) {
                    // loop over echoes in same voxel
                    while ((null != echo) && isEchoInsideVoxel(echo, vcoord) && (beamFractionIn - bfIntercepted > EPSILON)) {
                        // add free path length associated to current echo (intercepted)
                        double freepathLength = shot.getRange(rank) - dIn;
                        freePaths.add(new FreepathVoxel(freepathLength, echoProperties.bfIntercepted[rank], true, echoProperties.retained[rank]));
                        if (pathLengthOutput.isEnabled()) {
                            pathLengthOutput.addPathLengthRecord(vcoord, (float) freepathLength, (float) pathLength);
                        }
                        // intercepted beam fraction regardless of echo filtering
                        bfIntercepted += echoProperties.bfIntercepted[rank];
                        // next echo 
                        rank++;
                        echo = rank < echoProperties.nEcho ? shot.echoes[rank] : null;
                    }
                }
                // add path length that goes through voxel (not intercepted)
                if (beamFractionIn - bfIntercepted > EPSILON) {
                    freePaths.add(new FreepathVoxel(pathLength, beamFractionIn - bfIntercepted));
                    if (pathLengthOutput.isEnabled()) {
                        pathLengthOutput.addPathLengthRecord(vcoord, (float) pathLength, Float.NaN);
                    }
                }

                // update voxel state variables
                updateVoxel(vcoord, shot, pathLength, freePaths);

                // update variables for numerical estimation of the transmittance
                // (handled separately as it may be removed from future release)
                if (numEstimTransmEnabled) {
                    updateTransmittance(vcoord, shot, pathLength, freePaths);
                }

                // update variables for numerical estimation of the attenuation (G.Vincent)
                if (attenuation_PL_MLE_enabled) {
                    updateAttenuation(vcoord, shot, pathLength, freePaths);
                }

                // clear freepaths (for GC)
                freePaths.clear();

                // increment number of crossed voxels
                nCrossedVoxel++;
                // decrement beamFractionIn for next voxel
                beamFractionIn -= bfIntercepted;
                // check whether the propagation should be interrupted
                //   residual beam fraction is null
                //   reached edge of the voxel space
                //   crossed the whole voxel space in its largest part
                interruptPropagation = (beamFractionIn < EPSILON)
                        || (null == voxelCrossing.indices)
                        || nCrossedVoxel >= nMaxCrossedVoxel;

            } while (!interruptPropagation);
            // shot went through the voxel space
            return true;
        }
        // shot did not go through the voxel space
        return false;
    }

    /**
     * Update voxel state variables based on list of free paths.
     *
     * @param index, the voxel index (i, j, k)
     * @param shot, current shot
     * @param pathLength, potential path length (meter) in current voxel, from
     * entering point to exiting point.
     * @param freepaths, a list of free paths in current voxel
     */
    private void updateVoxel(Point3i index, Shot shot, double pathLength, List<FreepathVoxel> freepaths) {

        int i = index.x;
        int j = index.y;
        int k = index.z;

        // compute beam surface at voxel centre
        double beamSurface = constantBeamSection
                ? 1.d
                : computeBeamSection(shot.origin, index, laserSpec);

        Voxel voxel;
        synchronized (voxel = vxsp.getVoxel(i, j, k)) {
            // increment number of shots crossing current voxel
            voxel.npulse++;
            // initializes entering beam section
            double enteringBeamSection = 0.d;
            // loop over free paths
            for (FreepathVoxel freepath : freepaths) {
                double beamSection = freepath.beamFraction * beamSurface;
                // increment entering beam section in current voxel
                enteringBeamSection += beamSection;
                // compute effective free path
                double effectiveFreepathLength = computeEffectiveFreepath(freepath.length);
                // compute weighted free path
                double weightedEffectiveFreepathLength = beamSection * effectiveFreepathLength;
                // increment effective free path
                voxel.weightedFreepathLength += beamSection * freepath.length;
                // increment weighted effective free path
                voxel.weightedEffectiveFreepathLength += weightedEffectiveFreepathLength;
                // beam fraction hits obstacle and the hit is not discarded
                if (freepath.hit && !freepath.discardedHit) {
                    // increment number of hits
                    voxel.nhit++;
                    // increment intercepted beam section
                    voxel.interceptedBeamSection += beamSection;
                    // increment attenuation bias correction factor (beamSurface * beamFraction * effectiveFreepathLength)
                    voxel.attenuation_FPL_biasCorrection += weightedEffectiveFreepathLength;
                }
            } //  end of loop over free paths
            // increment entering beam section in current voxel
            voxel.enteringBeamSection += enteringBeamSection;
            // increment path length in current voxel
            if (pathLengthEnabled) {
                voxel.pathLength += pathLength;
            }
            // increment pulse angle in current voxel
            if (angleEnabled) {
                voxel.averagedPulseAngle += shot.getAngle();
            }
            // update mean path length and associated standard deviation
            if (pathLengthStDevEnabled) {
                // Welford's method for computing variance
                double oldAveragedLength = voxel.averagedPathLength;
                voxel.averagedPathLength += (pathLength - oldAveragedLength) / voxel.npulse;
                voxel.pathLengthStDev += (pathLength - oldAveragedLength) * (pathLength - voxel.averagedPathLength);
            }
            // increment distance to laser
            if (averagedLaserDistanceEnabled) {
                voxel.averagedLaserDistance += voxelManager.getCenter(index).distance(shot.origin);
            }
            // sub voxel propagation
            if (subSamplingEnabled) {
                voxel.subSampling = voxel.subSampling.or(subPropagation(shot, index));
            }
        }
    }

    private void updateTransmittance(Point3i index, Shot shot, double pathLength, List<FreepathVoxel> freepaths) {

        int i = index.x;
        int j = index.y;
        int k = index.z;

        // compute beam surface at voxel centre
        double beamSurface = constantBeamSection
                ? 1.d
                : computeBeamSection(shot.origin, index, laserSpec);

        // initializes entering beam section
        double enteringBeamSection = 0.d;
        // initializes exiting path length (explanation below)
        double exitingPathLength = 0.d;
        int nExitingPath = 0;
        // loop over free paths
        for (FreepathVoxel freepath : freepaths) {
            double beamSection = freepath.beamFraction * beamSurface;
            // increment entering beam section in current voxel
            enteringBeamSection += beamSection;
            // here we consider the beam fraction that either goes
            // through the whole voxel (no hit) or that is intercepted but
            // discarded (rulled out as wood for instance).
            if (!freepath.hit || freepath.discardedHit) {
                exitingPathLength += freepath.length;
                nExitingPath += 1;
            }
        }
        /**
         * PhV 20201020 - trick for handling free paths that are intercepted but
         * discarded (as wood echo for instance). Basic idea: we handle such
         * beam as a traversing one that goes out of the voxel with path length
         * = distance(voxel entering point, echo). If it is not a last echo
         * (meaning that a beam fraction effectively exits the voxel) then the
         * path length is computed as the average of the "pseudo exiting" path
         * length and the potential path length. If there is no discarded hit or
         * no hit at all, then exiting path length is the potential path length.
         */
        exitingPathLength = (nExitingPath > 0) ? exitingPathLength / nExitingPath : pathLength;

        Voxel voxel;
        synchronized (voxel = vxsp.getVoxel(i, j, k)) {
            // update variables for numerical estimation of the transmittance
            if (voxel.fallbackTransm) {
                int nTrEstim = voxel.trNumBsOut.length;
                for (int itr = 0; itr < nTrEstim; itr += 1) {
                    double tr = transmErrFallback * (itr + 1);
                    voxel.trNumBsOut[itr] += enteringBeamSection * Math.pow(tr, exitingPathLength);
                }
            } else {
                voxel.addTransmittanceRecord(enteringBeamSection, exitingPathLength);
                if (voxel.sizeTransmittanceRecords() >= nTrRecordMax) {
                    // too many records
                    // switch to transmittance fallback mode
                    voxel.fallbackTransm = true;
                    int nTrEstim = (int) (1.d / transmErrFallback) - 1;
                    voxel.trNumBsOut = new double[nTrEstim];
                    for (int itr = 0; itr < nTrEstim; itr += 1) {
                        double tr = transmErrFallback * (itr + 1);
                        voxel.trNumBsOut[itr] += voxel.streamTransmittanceRecords()
                                .mapToDouble(trRecord -> trRecord.bsIn * Math.pow(tr, trRecord.pathLength))
                                .sum();
                    }
                    voxel.clearTransmittanceRecords();
                }
            }
        }

    }

    private void updateAttenuation(Point3i index, Shot shot, double pathLength, List<FreepathVoxel> freepaths) {

        int i = index.x;
        int j = index.y;
        int k = index.z;

        // compute beam surface at voxel centre
        double beamSurface = constantBeamSection
                ? 1.d
                : computeBeamSection(shot.origin, index, laserSpec);

        Voxel voxel;
        synchronized (voxel = vxsp.getVoxel(i, j, k)) {
            // loop over free paths
            freepaths.forEach(freepath -> {
                // beam section
                double beamSection = freepath.beamFraction * beamSurface;
                // distinguish intercepted vs crossing freepath
                if (!freepath.hit || freepath.discardedHit) {
                    // beam that exits the voxel either by crossing the voxel
                    // or through discarded echo.
                    voxel.weightedPathLength += beamSection * freepath.length;
                } else {
                    // intercepted beam
                    voxel.addAttenuationRecord(beamSection, pathLength);
                }
            });
        }
    }

    private boolean isEchoInsideVoxel(Echo echo, Point3i indexVoxel) {

        Point3i indexEcho = findVoxel(echo);

        return indexVoxel.equals(indexEcho);
    }

    /**
     * Compute the average beam section of the shot in given voxel by estimating
     * it at voxel centre.
     *
     * @param origin, the shot origin
     * @param index, the voxel index (i, j, k)
     * @param spec, laser specification (beam divergence and diameter at exit)
     * @return the beam section calculated at voxel centre.
     */
    private double computeBeamSection(Point3d origin, Point3i index, LaserSpecification spec) {

        // distance from shot origin to current voxel center
        Point3d voxelPosition = getPosition(index);
        double distance = voxelPosition.distance(origin);

        // beam surface in current voxel
        return Math.pow((Math.tan(0.5d * spec.getBeamDivergence()) * distance) + 0.5d * spec.getBeamDiameterAtExit(), 2) * Math.PI;
    }

    /**
     * check whether current voxel centre vertical coordinate is below the
     * ground
     */
    private boolean belowGround(Voxel voxel) {
        return (voxel.groundDistance < voxelManager.getVoxelSpace().getVoxelSize().z / 2.0f);
    }

    private EchoProperties setEchoProperties(Shot shot) throws Exception {

        if (laserSpec.isMonoEcho()) {
            // mono echo laser
            EchoProperties echoProperties = new EchoProperties(Math.min(1, shot.getEchoesNumber()));
            if (echoProperties.nEcho > 0) {
                echoProperties.retained[0] = retainEcho(shot.echoes[0]);
                echoProperties.bfIntercepted[0] = 1.d;
            }
            return echoProperties;

        } else {
            // multi echo laser
            EchoProperties echoProperties = new EchoProperties(shot.getEchoesNumber());
            if (echoProperties.nEcho > 0) {
                for (int k = 0; k < echoProperties.nEcho; k++) {
                    // echo filter
                    echoProperties.retained[k] = retainEcho(shot.echoes[k]);
                    // echo weight
                    echoProperties.bfIntercepted[k] = weightEcho(shot.echoes[k]);
                }
            }
            return echoProperties;
        }
    }

    public void init() throws Exception {

        nShotProcessed = 0;
        nShotDiscarded = 0;
        iShot = 0;

        // initialise shot filters
        if (null != shotFilters) {
            for (Filter filter : shotFilters) {
                filter.init();
                LOGGER.info(logHeader + " Initialized shot filter " + filter.getClass().getSimpleName());
            }
        }

        // initialise echo filters
        if (null != echoFilters) {
            for (Filter filter : echoFilters) {
                filter.init();
                LOGGER.info(logHeader + " Initialized echo filter " + filter.getClass().getSimpleName());
            }
        }

        // initialise echo weights
        if (null != echoWeights) {
            for (EchoWeight echoWeight : echoWeights) {
                if (echoWeight.isEnabled()) {
                    echoWeight.init(cfg);
                    LOGGER.info(logHeader + " Initialized echo weight " + echoWeight.getClass().getSimpleName());
                }
            }
        }

        laserSpec = cfg.getLaserSpecification();
        constantBeamSection = laserSpec.getBeamDiameterAtExit() <= 0.d && laserSpec.getBeamDivergence() <= 0.d;

        // enabled output variables
        padEnabled = cfg.isOutputVariableEnabled(OutputVariable.PLANT_AREA_DENSITY);
        numEstimTransmEnabled = padEnabled || cfg.isOutputVariableEnabled(OutputVariable.ESTIMATED_TRANSMITTANCE);
        angleEnabled = padEnabled || cfg.isOutputVariableEnabled(OutputVariable.MEAN_ANGLE);
        meanPathLengthEnabled = cfg.isOutputVariableEnabled(OutputVariable.MEAN_TOTAL_LENGTH);
        pathLengthEnabled = meanPathLengthEnabled || cfg.isOutputVariableEnabled(OutputVariable.TOTAL_LENGTH);
        potentialBeamSectionEnabled = cfg.isOutputVariableEnabled(OutputVariable.POTENTIAL_BEAM_SURFACE);
        attenuation_PL_MLE_enabled = cfg.isOutputVariableEnabled(OutputVariable.ATTENUATION_PPL_MLE);

        // fallback error for the numerical estimation of the transmittance
        transmErrFallback = cfg.getTrNumEstimFallbackError();
        // maximum number of transmittance records to be stored for numerical
        // estimation of the transmittance. If exceeded => fallback mode
        nTrRecordMax = cfg.getNTrRecordMax();
        // nTrRecordMax set to zero means that transmittance computation never switch to fallback mode
        if (nTrRecordMax <= 0) {
            nTrRecordMax = Integer.MAX_VALUE;
        }

        averagedLaserDistanceEnabled = cfg.isOutputVariableEnabled(OutputVariable.DIST_LASER);
        pathLengthStDevEnabled = cfg.isOutputVariableEnabled(OutputVariable.SD_LENGTH);

        // sub voxel space exploration
        subSamplingEnabled = cfg.isOutputVariableEnabled(OutputVariable.EXPLORATION_RATE);
        if (subSamplingEnabled) {
            subVoxelSplit = cfg.getSubVoxelSplit();
        }

        // create voxel space
        createVoxelSpace();
        Point3i nvox = voxelManager.getVoxelSpace().getDimension();
        nMaxCrossedVoxel = (int) Math.ceil(Math.sqrt(nvox.x * nvox.x + nvox.y * nvox.y + nvox.z * nvox.z));

        // unitary extinction coefficient lambda1
        Point3d voxSize = voxelManager.getVoxelSpace().getVoxelSize();
        lambda1 = 0.25d * cfg.getMeanLeafArea() / (voxSize.x * voxSize.y * voxSize.z);
    }

    /**
     * Get position of the center of a voxel
     *
     * @param vcoord, voxel coordinate
     * @return
     */
    public Point3d getPosition(Point3i vcoord) {

        Point3d minCorner = cfg.getMinCorner();
        Point3d voxSize = voxelManager.getVoxelSpace().getVoxelSize();

        double posX = minCorner.x + (voxSize.x / 2.0d) + (vcoord.x * voxSize.x);
        double posY = minCorner.y + (voxSize.y / 2.0d) + (vcoord.y * voxSize.y);
        double posZ = minCorner.z + (voxSize.z / 2.0d) + (vcoord.z * voxSize.z);

        return new Point3d(posX, posY, posZ);
    }

    /**
     * Check whether given 3d point falls right on a voxel facet.
     *
     * @param point3d
     * @return true if the point falls on a voxel facet, false if strictly
     * inside voxel.
     */
    private boolean[] isOnVoxelFacet(Point3d point3d) {

        Point3d size = voxelManager.getVoxelSpace().getVoxelSize();
        boolean[] onFacet = new boolean[3];
        if (voxelManager.isPointInsideBoundingBox(point3d, false)) {
            onFacet[0] = Math.abs(point3d.x % size.x) < EPSILON;
            onFacet[1] = Math.abs(point3d.y % size.y) < EPSILON;
            onFacet[2] = Math.abs(point3d.z % size.z) < EPSILON;
        }
        return onFacet;
    }

    /**
     * Find the voxel that contains the given echo, taking into account specific
     * boundary condition. Boundary condition: for an echo that falls on a facet
     * shared by two voxels AMAPVox assumes it belongs to the voxel in which the
     * optical length is not zero, taking into account the shot trajectory.
     * E.g.: shot going along y = 1, echo y = 3 falls in between voxel 2 and 3.
     * Mathematically speaking it belongs to voxel 3 but in such case the
     * optical length in voxel 3 would be zero so AMAPVox assumes it belongs to
     * voxel 2 with an optical length equal to 1.
     *
     * @param echo
     * @return the index of the voxel containing the echo.
     */
    private Point3i findVoxel(Echo echo) {

        Point3i vcoord = voxelManager.getVoxelIndicesFromPoint(echo.getLocation());
        if (null != vcoord) {
            boolean[] onFacet = isOnVoxelFacet(echo.getLocation());
            if (onFacet[0] && Math.signum(echo.getShot().direction.x) > 0) {
                //LOGGER.info("Special boundary condition on x " + echo.location);
                vcoord.x = (int) Math.max(vcoord.x - 1, 0);
            }
            if (onFacet[1] && Math.signum(echo.getShot().direction.y) > 0) {
                //LOGGER.info("Special boundary condition on y " + echo.location);
                vcoord.y = (int) Math.max(vcoord.y - 1, 0);
            }
            if (onFacet[2] && Math.signum(echo.getShot().direction.z) > 0) {
                //LOGGER.info("Special boundary condition on z " + echo.location);
                vcoord.z = (int) Math.max(vcoord.z - 1, 0);
            }
        }
        return vcoord;
    }

    double weightEcho(Echo echo) {

        double weight = 1.d;

        // multiply weights of every weight function
        if (echo.getRank() >= 0 && echoWeights != null) {
            weight = echoWeights.stream()
                    .mapToDouble(echoWeight -> echoWeight.getWeight(echo))
                    .reduce(1.d, (partialw, w) -> partialw * w);

        }

        return weight;
    }

    boolean retainEcho(Echo echo) throws Exception {

        if (echo.getRank() >= 0 && echoFilters != null) {
            for (Filter<Echo> filter : echoFilters) {
                if (!filter.accept(echo)) {
                    return false;
                }
            }
        }

        // echo retained by every filter
        return true;
    }

    boolean retainShot(Shot shot) throws Exception {

        // discard null shot
        if (null == shot) {
            return false;
        }

        if (null != shotFilters) {
            for (Filter<Shot> filter : shotFilters) {
                // as soon as a filter discard the shot returns false 
                if (!filter.accept(shot)) {
                    return false;
                }
            }
        }

        // all filters retain the shot
        return true;
    }

    boolean isInsideSameVoxel(Echo echo1, Echo echo2) {

        if (echo1 == null || echo2 == null) {
            return false;
        }

        Point3i vcoord1 = findVoxel(echo1);
        Point3i vcoord2 = findVoxel(echo2);

        return vcoord1 != null && vcoord2 != null && vcoord1.equals(vcoord2);
    }

    public void createVoxelSpace() {

        // main voxel space
        voxelManager = new VoxelManager(
                cfg.getMinCorner(), cfg.getMaxCorner(),
                cfg.getDimension());
        // print information on main voxel space
        LOGGER.info(logHeader + " " + voxelManager.getInformations());

        // sub voxel space
        if (subSamplingEnabled) {
            subVoxelManager = new VoxelManager(
                    new Point3d(0.d, 0.d, 0.d),
                    cfg.getVoxelSize(),
                    new Point3i(subVoxelSplit, subVoxelSplit, subVoxelSplit));
        }

    }

    public Raster getDTM() {
        return dtm;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public VoxelizationCfg getVoxelizationCfg() {
        return cfg;
    }

    public VoxelSpace getVoxelSpace() {
        return vxsp;
    }

    public Voxel getVoxel(int i, int j, int k) {
        return vxsp.getVoxel(i, j, k);
    }

    /**
     * Compute optical depth
     *
     * @param freepathLength
     * @return
     */
    private double computeEffectiveFreepath(double freepathLength) {
        return (lambda1 > 0.d)
                ? -Math.log(1.d - lambda1 * freepathLength) / lambda1
                : freepathLength;
    }

    /**
     * Convenience class that gathers together some properties for echoes of
     * same shot.
     */
    private class EchoProperties {

        // number of hits in the shot
        private final int nEcho;
        // whether the echoes are retained
        private final boolean retained[];
        // intercepted beam fraction by each echo
        private final double[] bfIntercepted;

        EchoProperties(int nEchoes) {
            this.nEcho = nEchoes;
            this.retained = new boolean[nEchoes];
            this.bfIntercepted = new double[nEchoes];
        }
    }

    /**
     * Free path limited to a voxel. The free path length in this class cannot
     * exceed the path length in current voxel.
     */
    private class FreepathVoxel {

        /**
         * Free path length inside voxel (capped to voxel path length)
         */
        private final double length;
        /**
         * Entering beam fraction
         */
        private final double beamFraction;
        /**
         * Whether there is a hit in current voxel
         */
        private final boolean hit;
        /**
         * Whether the hit is discarded (ruled out as wood for instance)
         */
        private final boolean discardedHit;

        /**
         * Free path from a voxel perspective.
         *
         * @param length free path length inside voxel
         * @param beamFraction entering beam fraction
         * @param intercepted whether there is a hit inside voxel
         * @param retained whether the hit is retained or discarded
         */
        FreepathVoxel(double length, double beamFraction, boolean intercepted, boolean retained) {
            this.length = length;
            this.beamFraction = beamFraction;
            this.hit = intercepted;
            this.discardedHit = !retained;
        }

        /**
         * Free path going through a voxel.
         *
         * @param length, path length in the voxel
         * @param beamFraction, entering beam fraction (== exiting beam
         * fraction)
         */
        FreepathVoxel(double length, double beamFraction) {
            this(length, beamFraction, false, true);
        }
    }
}
