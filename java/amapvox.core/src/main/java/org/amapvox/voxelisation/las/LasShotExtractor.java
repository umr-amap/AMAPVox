/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation.las;

import com.github.mreutegg.laszip4j.LASExtraBytesDescription;
import org.amapvox.commons.util.io.file.FileManager;
import org.amapvox.commons.util.Process;
import org.amapvox.commons.util.io.file.CSVFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.amapvox.commons.util.Cancellable;
import org.amapvox.commons.util.IterableWithException;
import org.amapvox.commons.util.IteratorWithException;
import org.amapvox.shot.Shot;
import com.github.mreutegg.laszip4j.LASHeader;
import com.github.mreutegg.laszip4j.LASPoint;
import com.github.mreutegg.laszip4j.LASReader;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.apache.log4j.Logger;

/**
 * This class merge trajectory file with point file (LAS, LAZ)
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 * @author Philippe Verley
 */
public class LasShotExtractor extends Process implements IterableWithException<Shot>, Cancellable {

    // LAS points
    private final File inputFile;
    private List<LasPoint> lasPoints;

    // trajectory
    private final CSVFile trajectoryFile;
    private List<TrajectoryPoint> trajectory;

    // scanner position
    private final Point3d scannerPosition;

    // VOP matrix
    private final Matrix4d vopMatrix;

    private final double collinearityError;

    private final static int SHOT_BUFFER = 100;

    // whether the task is cancelled
    private boolean cancelled;

    private final boolean echoConsistencyCheckEnabled;
    private final boolean echoConsistencyWarningEnabled;
    private final boolean collinearityCheckEnabled;
    private final boolean collinearityWarningEnabled;

    //
    private final double timeMin, timeMax;
    private double trajTimeMin = 0.d, trajTimeMax = Double.MAX_VALUE;

    private int nshot;

    private String relativeEchoWeightVariable;

    // logger
    private final static Logger LOGGER = Logger.getLogger(LasShotExtractor.class);

    public LasShotExtractor(CSVFile trajectoryFile, Point3d scannerPosition,
            File inputFile, Matrix4d vopMatrix,
            boolean echoConsistencyCheckEnabled, boolean echoConsistencyWarningEnabled,
            boolean collinearityCheckEnabled, boolean collinearityWarningEnabled,
            double maxDeviation,
            double timeMin, double timeMax,
            String relativeEchoWeightVariable) {

        this.trajectoryFile = trajectoryFile;
        this.scannerPosition = scannerPosition;
        this.vopMatrix = vopMatrix;
        this.inputFile = inputFile;

        this.echoConsistencyCheckEnabled = echoConsistencyCheckEnabled;
        this.echoConsistencyWarningEnabled = echoConsistencyWarningEnabled;
        this.collinearityCheckEnabled = collinearityCheckEnabled;
        this.collinearityWarningEnabled = collinearityWarningEnabled;
        this.collinearityError = Math.abs(1.d - Math.abs(Math.cos(Math.toRadians(maxDeviation))));

        this.timeMin = timeMin;
        this.timeMax = timeMax;

        this.relativeEchoWeightVariable = relativeEchoWeightVariable;
    }

    public LasShotExtractor(CSVFile trajectoryFile, Point3d scannerPosition,
            File inputFile, Matrix4d vopMatrix,
            boolean echoConsistencyCheckEnabled, boolean echoConsistencyWarningEnabled,
            boolean collinearityCheckEnabled, boolean collinearityWarningEnabled,
            double maxDeviation) {

        this(trajectoryFile, scannerPosition,
                inputFile, vopMatrix,
                echoConsistencyCheckEnabled, echoConsistencyWarningEnabled,
                collinearityCheckEnabled, collinearityWarningEnabled,
                maxDeviation,
                0.d, Double.MAX_VALUE,
                null);
    }

    public int getNShot() {
        return nshot;
    }

    public void init() throws Exception {

        if (null != trajectoryFile) {
            // read trajectory file
            trajectory = readTrajectory(trajectoryFile);
            trajTimeMin = trajectory.get(0).time;
            trajTimeMax = trajectory.get(trajectory.size() - 1).time;
        }

        // read LAS / LAZ points
        List<LasPoint> rawLasPoints = readLasPoints(inputFile);

        // perform checks
        int nflawed = 0;
        List<LasPoint> points;
        int count = 0;
        int npoint = rawLasPoints.size();
        nshot = 0;
        int nshotFlawed = 0;
        LOGGER.info((echoConsistencyCheckEnabled | collinearityCheckEnabled)
                ? "Checking LAS points consistency"
                : "Counting number of shots");
        while (null != (points = nextLasPoints(count, rawLasPoints))) {
            if (echoConsistencyCheckEnabled | collinearityCheckEnabled) {
                fireProgress("Checking LAS points consistency", count, npoint);
            } else {
                fireProgress("Counting number of shots", count, npoint);
            }
            if (cancelled) {
                break;
            }
            if ((echoConsistencyCheckEnabled && !checkEchoConsistency(points, echoConsistencyWarningEnabled))
                    || (collinearityCheckEnabled && !checkCollinearity(points, collinearityError))) {
                // flawed LAS points
                points.forEach(point -> point.flawed = true);
                nflawed += points.size();
                nshotFlawed++;
            }
            nshot++;
            count += points.size();
        }

        if (nflawed > 0) {
            float percent = 100.f * nflawed / npoint;
            LOGGER.warn("Discarded " + nflawed + " flawed LAS points out of " + npoint + " (" + String.format("%.2f", percent) + "%)");
            LOGGER.info("Removing flawed LAS points...");
            lasPoints = new ArrayList<>(count - nflawed);
            lasPoints = rawLasPoints.parallelStream().filter(point -> !point.flawed).collect(Collectors.toList());
            nshot -= nshotFlawed;
        } else {
            lasPoints = new ArrayList<>(rawLasPoints);
        }

        // trim trajectory
        if (null != trajectory) {
            // reduce trajectory points to LAS points time range
            int imin = nearestTrajectoryPoint(lasPoints.get(0).t, Search.MIN);
            int imax = nearestTrajectoryPoint(lasPoints.get(lasPoints.size() - 1).t, Search.MAX);
            trajectory = trajectory.subList(imin, imax + 1);
        }
    }

    private List<LasPoint> readLasPoints(final File file) throws Exception {

        // read LAS points
        List<LasPoint> points = new ArrayList<>();
        LASHeader header;

        int count = 0;
        int countTimeRangeDiscarded = 0;
        int countTrajectoryDiscarded = 0;
        long nPoint;
        double tmin = Double.MAX_VALUE, tmax = 0.d;

        LOGGER.info("Reading LAS file " + file.getName());
        // open LAS reader
        LASReader lasReader = new LASReader(file);
        // read LAS header
        header = lasReader.getHeader();
        // number of LAS points
        nPoint = Math.max(header.getLegacyNumberOfPointRecords(), header.getNumberOfPointRecords());
        // scale factors and offsets
        double x_offset = header.getXOffset();
        double x_scale_factor = header.getXScaleFactor();
        double y_offset = header.getYOffset();
        double y_scale_factor = header.getYScaleFactor();
        double z_offset = header.getZOffset();
        double z_scale_factor = header.getZScaleFactor();
        // look for extrabytes variable for relative echo weight
        LASExtraBytesDescription extraBytesDescription = null;
        LOGGER.info("Relative echo weight variable `" + relativeEchoWeightVariable + "`");
        if (!relativeEchoWeightVariable.equalsIgnoreCase("intensity")) {
            try {
                extraBytesDescription = header.getExtraBytesDescription(relativeEchoWeightVariable);
            } catch (NullPointerException ex) {
                LOGGER.warn("LAS extra bytes variable `" + relativeEchoWeightVariable + "` not found in LAS file. Relative echo weight function will use `Intensity` variable instead");
            }
        }

        // loop over LAS points
        for (LASPoint p : lasReader.getPoints()) {
            fireProgress("Reading LAS file " + file.getName(), count++, nPoint);
            if (isCancelled()) {
                return null;
            }
            tmin = Math.min(p.getGPSTime(), tmin);
            tmax = Math.max(p.getGPSTime(), tmax);
            // time range filtering
            if (p.getGPSTime() < timeMin | p.getGPSTime() > timeMax) {
                countTimeRangeDiscarded++;
                continue;
            }
            // trajectory filtering
            if (p.getGPSTime() < trajTimeMin | p.getGPSTime() > trajTimeMax) {
                countTrajectoryDiscarded++;
                continue;
            }
            Vector3d location = new Vector3d(
                    (p.getX() * x_scale_factor) + x_offset,
                    (p.getY() * y_scale_factor) + y_offset,
                    (p.getZ() * z_scale_factor) + z_offset);
            float weight = (null != extraBytesDescription)
                    ? p.getExtraBytes(extraBytesDescription).getValue().floatValue()
                    : p.getIntensity();
            LasPoint point = new LasPoint(
                    location.x, location.y, location.z,
                    p.getReturnNumber(), p.getNumberOfReturns(),
                    p.getClassification(),
                    p.getGPSTime(),
                    weight);

            points.add(point);
        }
        if (countTimeRangeDiscarded > 0) {
            int percent = (int) Math.ceil(100.f * countTrajectoryDiscarded / count);
            LOGGER.info("LAS points outside time range: " + countTimeRangeDiscarded + " / " + count + " (~" + percent + "%)");
        }
        if (countTimeRangeDiscarded >= count) {
            StringBuilder msg = new StringBuilder();
            msg.append("All LAS points have been discarded from time range. LAS time range: ");
            msg.append((long) tmin).append(", ").append((long) tmax);
            msg.append("; time filter: ").append((long) timeMin).append(", ").append((long) timeMax);
            throw new IOException(msg.toString());
        }
        if (countTrajectoryDiscarded > 0) {
            int percent = (int) Math.ceil(100.f * countTrajectoryDiscarded / count);
            StringBuilder sb = new StringBuilder();
            sb.append("LAS points outside trajectory time range (");
            sb.append(countTrajectoryDiscarded).append(" / ").append(count);
            sb.append(", ~").append(percent).append("%)");
            sb.append(" have been discarded.\n");
            sb.append("\t").append("LAS points time span [").append(tmin);
            sb.append(" ").append(tmax).append("]\n");
            sb.append("\t").append("Trajectory time span [").append(trajTimeMin);
            sb.append(" ").append(trajTimeMax).append("]");

            LOGGER.warn(sb);
        }

        // sort LAS point by time stamp
        LOGGER.info("Sorting LAS points");
        fireProgress("Sorting LAS points", 50, 100);
        Collections.sort(points, (LasPoint lp1, LasPoint lp2) -> {
            int tcomp = Double.compare(lp1.t, lp2.t);
            return tcomp == 0 ? Integer.compare(lp1.r, lp2.r) : tcomp;
        });
        fireProgress("Sorting LAS points", 100, 100);

        double minTime = points.get(0).t;
        double maxTime = points.get(points.size() - 1).t;

        if (minTime >= maxTime) {
            throw new IOException("LAS/LAZ file contains inconsistent time information, minimum time " + minTime + " >= maximum time " + maxTime);
        }

        return points;
    }

    private List<TrajectoryPoint> readTrajectory(final CSVFile file) throws Exception {

        LOGGER.info("Reading trajectory file " + file.getName());
        List<TrajectoryPoint> points = new ArrayList<>();

        // number of trajectory points
        int nPoint = FileManager.getLineNumber(file.getAbsolutePath());
        int count = 0;

        BufferedReader reader = new BufferedReader(new FileReader(file));
        // skip header
        if (file.containsHeader()) {
            reader.readLine();
            count++;
        }

        // skip additional lines
        for (long l = 0; l < file.getNbOfLinesToSkip(); l++) {
            reader.readLine();
            count++;
        }

        // assign column index
        Map<String, Integer> columnAssignment = file.getColumnAssignment();
        // time column
        Integer timeIndex = columnAssignment.get("Time");
        if (timeIndex == null) {
            timeIndex = 3;
        }
        // easting column
        Integer eastingIndex = columnAssignment.get("Easting");
        if (eastingIndex == null) {
            eastingIndex = 0;
        }
        // northing column
        Integer northingIndex = columnAssignment.get("Northing");
        if (northingIndex == null) {
            northingIndex = 1;
        }
        // elevation column
        Integer elevationIndex = columnAssignment.get("Elevation");
        if (elevationIndex == null) {
            elevationIndex = 2;
        }

        String line;
        // parse lines
        while ((line = reader.readLine()) != null) {
            fireProgress("Reading trajectory file " + file.getName(), count++, nPoint);
            String[] lineSplit = line.split(file.getColumnSeparator());
            if (lineSplit.length < 4) {
                StringBuilder msg = new StringBuilder();
                msg.append("Line ").append(count).append(" from trajectory file has less than four columns. The point is discarded.");
                msg.append('\n').append(line);
                LOGGER.warn(msg);
                continue;
            }
            try {
                double easting = Double.parseDouble(lineSplit[eastingIndex]);
                double northing = Double.parseDouble(lineSplit[northingIndex]);
                double elevation = Double.parseDouble(lineSplit[elevationIndex]);
                double time = Double.parseDouble(lineSplit[timeIndex]);
                points.add(new TrajectoryPoint(easting, northing, elevation, time));
            } catch (NumberFormatException ex) {
                StringBuilder msg = new StringBuilder();
                msg.append("Failed to parse line ").append(count).append(" from trajectory file (number format error). The point is discarded.");
                msg.append('\n').append(line);
                LOGGER.warn(msg);
            }
        }

        LOGGER.info("Sorting and trimming trajectory points");
        fireProgress("Sorting trajectory points", 50, 100);
        // sort points by time stamp
        Collections.sort(points, (TrajectoryPoint p1, TrajectoryPoint p2) -> Double.compare(p1.time, p2.time));
        fireProgress("Sorting trajectory points", 100, 100);

        return points;
    }

    private synchronized Shot pointsToShot(final List<LasPoint> points, final int shotIndex) {

        if (points != null) {

            // take any point from the shot, apply transformation
            LasPoint lasPoint = points.get(0);
            Point3d point = new Point3d(lasPoint.x, lasPoint.y, lasPoint.z);
            vopMatrix.transform(point);
            // shot origin, apply transformation
            Point3d origin = (null != trajectory) ? findOrigin(lasPoint) : new Point3d(scannerPosition);
            vopMatrix.transform(origin);

            // shot direction
            Vector3d direction = new Vector3d(point);
            direction.sub(origin);
            direction.normalize();
            // number of echoes
            int nEcho = lasPoint.n;
            // echoes attributes
            double[] ranges = new double[nEcho];
            int[] classifications = new int[nEcho];
            float[] weights = new float[nEcho];
            points.forEach(pt -> {
                // range (distance from source)
                Point3d pt3d = new Point3d(pt.x, pt.y, pt.z);
                vopMatrix.transform(pt3d);
                ranges[pt.r - 1] = origin.distance(pt3d);
                // classification
                classifications[pt.r - 1] = pt.classification;
                weights[pt.r - 1] = pt.weight;
            });

            // create new shot
            Shot shot = new Shot(
                    shotIndex,
                    origin, direction,
                    ranges);

            // add specific properties
            for (int r = 0; r < nEcho; r++) {
                shot.getEcho(r).addInteger("classification", classifications[r]);
                shot.getEcho(r).addFloat(relativeEchoWeightVariable, weights[r]);
            }

            return shot;
        }

        return null;
    }

    /**
     * Returns a subset of LAS points with the same GPS time from the set of LAS
     * points, starting from given index.
     *
     * @param fromIndex, the index of the first Las point of the subset.
     * @return the subset of LAS points that have the same GPS time than LAS
     * point at index {@code fromIndex}.
     */
    private List<LasPoint> nextLasPoints(final int fromIndex, final List<LasPoint> points) {

        if (fromIndex > points.size() - 1) {
            return null;
        }

        List<LasPoint> pointsWithSameTime = new ArrayList<>();
        pointsWithSameTime.add(points.get(fromIndex));
        double time = pointsWithSameTime.get(0).t;
        for (int iPoint = fromIndex + 1; iPoint < points.size(); iPoint++) {
            if (points.get(iPoint).t == time) {
                pointsWithSameTime.add(points.get(iPoint));
            } else {
                break;
            }
        }
        return pointsWithSameTime;
    }

    /**
     * Check whether the subset of LAS points is consistent in terms of echo
     * rank and number of echoes. Every LAS point should have a unique echo rank
     * and the same number of echoes.
     *
     * @param points, a subset of LAS points supposedly belonging to same shot,
     * already sorted by rank.
     * @return true if the LAS points have unique echo rank and same total echo
     * number.
     */
    private boolean checkEchoConsistency(final List<LasPoint> points, boolean warnEnabled) {

        boolean flawed = false;

        // checks on ranks
        Supplier<IntStream> rstream = () -> points.stream().mapToInt(point -> point.r);
        // ranks must be greater or equal to one
        flawed |= rstream.get().anyMatch(r -> r <= 0);
        // ranks must be unique
        flawed |= rstream.get().count() != rstream.get().distinct().count();
        // ranks increment between points must be equal to one (e.g [1, 2, 3] or [2, 3])
        int[] ranks = rstream.get().distinct().toArray();
        flawed |= (ranks.length > 1
                && IntStream.range(0, ranks.length - 1)
                        .map(i -> (int) Math.abs(ranks[i + 1] - ranks[i]))
                        .anyMatch(dr -> dr > 1));

        // checks on number of ranks
        Supplier<IntStream> nstream = () -> points.stream().mapToInt(point -> point.n);
        // nrank must be greater or equal to one
        flawed |= nstream.get().anyMatch(n -> n <= 0);
        // nrank must be the same for every point
        flawed |= nstream.get().distinct().count() > 1;

        if (warnEnabled && flawed) {
            StringBuilder sb = new StringBuilder();
            sb.append("LAS points with same GPS time (i.e. belonging to same shot) seem to have inconsistent echo ranks or echo numbers:");
            points.forEach(point -> {
                sb.append('\n').append("  ").append(point);
            });
            LOGGER.warn(sb.toString());
        }

        return !flawed;
    }

    /**
     * Checks collinearity of the collection of LAS points by ensuring that
     * every combination of three points is collinear, with margin of error.
     *
     * @param points, a list of LAS points.
     * @param epsilon, deviation from one, one meaning strict collinearity.
     * @return true if all the points are (approximately) collinear.
     */
    private boolean checkCollinearity(final List<LasPoint> points, final double epsilon) {

        boolean collinear = true;
        StringBuilder sb = new StringBuilder();
        if (points.size() >= 3) {
            int pmax = points.size();
            for (int p1 = 0; p1 < pmax - 2; p1++) {
                for (int p2 = p1 + 1; p2 < pmax - 1; p2++) {
                    for (int p3 = p2 + 1; p3 < pmax; p3++) {
                        if (!collinearity(points.get(p1), points.get(p2), points.get(p3), epsilon)) {
                            collinear = false;
                            // append error and carry on with collinearity test
                            // in case some others points are not lined up
                            if (sb.length() > 0) {
                                sb.append('\n');
                            }
                            sb.append("LAS points with same GPS time are not in line:\n");
                            sb.append("  ").append(points.get(p1)).append('\n');
                            sb.append("  ").append(points.get(p2)).append('\n');
                            sb.append("  ").append(points.get(p3));
                        }
                    }
                }
            }
        }
        if (collinearityWarningEnabled && !collinear) {
            sb.append('\n').append("LAS points discarded.");
            LOGGER.warn(sb.toString());
        }

        return collinear;
    }

    /**
     * Check collinearity of the three points, with margin of error.
     * |AB.AC|/|AB||AC|~1
     *
     * @param lpA, first LAS point
     * @param lpB, second LAS point
     * @param lpC, third LAS point
     * @param epsilon, deviation from one, one meaning strict collinearity.
     * @return true if the three LAS points are approximately on same line.
     */
    private boolean collinearity(final LasPoint lpA, final LasPoint lpB, final LasPoint lpC, final double epsilon) {

        Point3d A = toPoint3d(lpA);
        // AB vector
        Vector3d AB = new Vector3d();
        AB.sub(toPoint3d(lpB), A);
        AB.normalize();
        // AC vector
        Vector3d AC = new Vector3d();
        AC.sub(toPoint3d(lpC), A);
        AC.normalize();
        // check collinearity
        return Math.abs(1.d - Math.abs(AB.dot(AC))) < epsilon;
    }

    private Point3d toPoint3d(LasPoint point) {
        return new Point3d(point.x, point.y, point.z);
    }

    /**
     * Given the LAS device trajectory, find out the coordinate of the origin of
     * the current LAS point.
     *
     * @param point, the current LAS point
     * @return the coordinate of the origin of the current LAS point
     */
    private Point3d findOrigin(final LasPoint point) {

        // index of the trajectory point behind current LAS point
        int behindTrjPointIndex = nearestTrajectoryPoint(point.t, Search.MIN);
        // index of the trajectory point ahead of current LAS point
        int aheadTrjPointIndex = behindTrjPointIndex + 1;

        // handle invalid trajectory file
        // should never happen since the trimming of the trajecotry is already
        // handled in PointsToShot.java
        if (behindTrjPointIndex < 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("LAS point GPS time outside trajectory time span.").append('\n');
            sb.append("  ").append(point).append('\n');
            sb.append("  Trajectory time span ")
                    .append(trajectory.get(0).time).append(" ")
                    .append(trajectory.get(trajectory.size() - 1).time);
            throw new IndexOutOfBoundsException(sb.toString());
        }

        // GPS time of the trajectory point behind current LAS point
        double beforeTrjPointTime = trajectory.get(behindTrjPointIndex).time;
        // GPS time of the trajectory point ahead of current LAS point
        double afterTrjPointTime = trajectory.get(aheadTrjPointIndex).time;

        // coordinate of the origin at the time of current LAS point,
        // computed as a linear interpolation between trajectory points
        // before and after current LAS point time.
        Point3d origin = new Point3d();
        double ratio = (afterTrjPointTime != beforeTrjPointTime)
                ? (point.t - beforeTrjPointTime) / (afterTrjPointTime - beforeTrjPointTime)
                : 0.5d;
        // linear interpolation
        origin.interpolate(trajectory.get(behindTrjPointIndex), trajectory.get(aheadTrjPointIndex), ratio);

        return origin;
    }

    @Override
    public IteratorWithException<Shot> iterator() {
        LasShotIterator it = new LasShotIterator();
        new Thread(it).start();
        return it;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    private int nearestTrajectoryPoint(double value, Search search) {

        int low = 0;
        int high = trajectory.size() - 1;

        // special case value smaller than first element of the list
        if (value < trajectory.get(low).time) {
            return (search == Search.MIN) ? -1 : low;
        }

        // special case value bigger than last element of the list
        if (value > trajectory.get(high).time) {
            return (search == Search.MAX) ? -1 : high;
        }

        while (low <= high) {
            int mid = (low + high) >>> 1; // (low + high) / 2
            double midVal = trajectory.get(mid).time;

            if (midVal < value) {
                low = mid + 1;
            } else if (midVal > value) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }

        // at this stage we have high < low
        // list[high] < value < list[low]
        return (search == Search.MIN) ? high : low;
    }

    private enum Search {
        MIN, MAX;
    }

    /**
     * A thread-safe iterator for reading LAS points and grouping them into
     * {@link Shot} objects. Uses a producer-consumer pattern with a blocking
     * queue and a "poison pill" to handle exceptions and graceful termination.
     * If an exception occurs during processing, a poison pill is enqueued,
     * ensuring the consumer thread is unblocked and the exception is
     * propagated.
     *
     * <p>
     * Usage:
     * <pre>
     *   LasShotIterator it = new LasShotIterator();
     *   new Thread(it).start();
     *   while (it.hasNext()) {
     *       Shot shot = it.next();
     *       // Process shot
     *   }
     * </pre>
     *
     * <p>
     * Thread Safety: Safe for single-producer, single-consumer scenarios.
     */
    private class LasShotIterator implements Runnable, IteratorWithException<Shot> {

        private static final Object POISON_PILL = new Object();
        private int shotIndex;
        private int pointIndex;
        private final BlockingQueue<Object> queue = new ArrayBlockingQueue<>(SHOT_BUFFER);
        private Exception error;

        @Override
        public boolean hasNext() throws Exception {
            return (shotIndex - queue.size()) < nshot;
        }

        @Override
        public Shot next() throws Exception {
            if (null != error) {
                throw error;
            }
            Object item = queue.take();
            if (item == POISON_PILL) {
                throw error != null ? error : new NoSuchElementException("Shot iterator ended unexpectedly");
            }
            return (Shot) item;
        }

        @Override
        public void run() {
            try {
                List<LasPoint> points;
                while (null != (points = nextLasPoints(pointIndex, lasPoints))) {
                    queue.put(pointsToShot(points, shotIndex));
                    shotIndex++;
                    pointIndex += points.size();
                }
            } catch (Exception ex) {
                error = ex;
                try {
                    queue.put(POISON_PILL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
