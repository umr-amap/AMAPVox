/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.shot;

import org.amapvox.commons.raytracing.geometry.LineSegment;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author Julien Heurtebize, Philippe Verley
 */
public class Shot {

    public final int index;
    public Point3d origin;
    public Vector3d direction;
    private double ranges[];
    private double angle = Double.NaN;
    public Echo[] echoes;
    private final static DecimalFormat dnf = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);

    static {
        dnf.applyPattern("#0.####");
        dnf.setGroupingUsed(false);
    }

    public Shot(int index, Point3d origin, Vector3d direction, double ranges[]) {

        this.index = index;
        this.origin = origin;
        this.direction = direction;
        this.ranges = ranges;
        init();
    }

    /**
     * Copy constructor
     *
     * @param shot the shot to copy
     */
    public Shot(Shot shot) {

        this.index = shot.index;
        this.origin = new Point3d(shot.origin);
        this.direction = new Vector3d(shot.direction);
        this.angle = shot.angle;
        if (shot.ranges != null) {
            this.ranges = new double[shot.ranges.length];
            System.arraycopy(shot.ranges, 0, this.ranges, 0, shot.ranges.length);
        }
        init();
    }

    private void init() {

        direction.normalize();

        echoes = new Echo[Math.max(getEchoesNumber(), 1)];

        if (getEchoesNumber() > 0) {
            for (int i = 0; i < echoes.length; i++) {
                updateEcho(i);
            }
        } else {
            // empty shot
            LineSegment seg = new LineSegment(origin, direction, 999999);
            echoes[0] = new Echo(-1, new Point3d(seg.getEnd()));
        }
    }

    private void updateEcho(int rank) {
        LineSegment seg = new LineSegment(origin, direction, ranges[rank]);
        echoes[rank] = new Echo(rank, new Point3d(seg.getEnd()));
    }

    public void setRange(int rank, double range) {
        ranges[rank] = range;
        updateEcho(rank);
    }

    public double getRange(int rank) {
        return ranges[rank];
    }

    public double[] getRanges() {
        return ranges;
    }

    public int getEchoesNumber() {
        return ranges == null ? 0 : ranges.length;
    }

    /**
     * Zenith angle (also called polar angle) in degrees. Angle between zenith
     * (origin at the ground) and shot direction. PHI angle
     * https://mathworld.wolfram.com/SphericalCoordinates.html
     *
     * @return shot zenith angle in degrees. 
     */
    public double getAngle() {

        if (Double.isNaN(angle)) {
            // phi   = acos(z / r) with r the norm of the direction == 1
            this.angle = Math.toDegrees(Math.acos(direction.z));
        }

        return angle;
    }

    public boolean isEmpty() {

        if (ranges == null) {
            return true;
        } else {
            return ranges.length == 0;
        }
    }

    public double getFirstRange() {
        if (isEmpty()) {
            return Double.NaN;
        } else {
            return ranges[0];
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Shot ").append(index).append(", ").append(getEchoesNumber()).append(" echoes").append('\n');
        str.append("  origin ").append(pointToString(origin)).append(" direction ").append(pointToString(direction));
        if (getEchoesNumber() > 0) {
            str.append("\n  Echo ranges ");
            for (int k = 0; k < getEchoesNumber(); k++) {
                str.append(dnf.format(ranges[k])).append("m ");
            }
        }
        return str.toString();
    }

    private String pointToString(Tuple3d point) {

        return new StringBuilder().append('(')
                .append(dnf.format(point.x)).append(", ")
                .append(dnf.format(point.y)).append(", ")
                .append(dnf.format(point.z)).append(')').toString();
    }

    public class Echo {

        public final int rank;
        public final Point3d location;
        public final Shot shot;

        public Echo(int rank, Point3d location) {
            this.rank = rank;
            this.location = location;
            this.shot = Shot.this;
        }
    }
}
