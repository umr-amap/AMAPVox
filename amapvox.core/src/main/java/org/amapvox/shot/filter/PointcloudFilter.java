/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */
package org.amapvox.shot.filter;

import org.amapvox.commons.math.util.MatrixUtility;
import org.amapvox.commons.spds.Octree;
import org.amapvox.commons.util.filter.Filter;
import org.amapvox.commons.util.io.file.CSVFile;
import org.amapvox.shot.Shot;
import org.amapvox.commons.Util;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class PointcloudFilter implements Filter<Shot.Echo> {

    private final CSVFile pointcloudFile;
    private final float pointcloudErrorMargin;
    private final Behavior behavior;
    private final boolean retain;
    private final Matrix4d vop;
    private Octree octree;
    private final boolean applyVOPMatrix;
    //
     private final static Logger LOGGER = Logger.getLogger(PointcloudFilter.class);

    public PointcloudFilter(CSVFile pointcloudFile, float pointcloudErrorMargin, Behavior behavior, Matrix4d vop) {
        this.pointcloudFile = pointcloudFile;
        this.pointcloudErrorMargin = pointcloudErrorMargin;
        this.behavior = behavior;
        retain = behavior.equals(Behavior.RETAIN);
        this.vop = vop;
        Matrix4d identidy = MatrixUtility.identity4d();
        applyVOPMatrix = !identidy.equals(vop);
    }

    public CSVFile getPointcloudFile() {
        return pointcloudFile;
    }

    public float getPointcloudErrorMargin() {
        return pointcloudErrorMargin;
    }

    public Behavior behavior() {
        return behavior;
    }
    
    public boolean isApplyVOPMatrix() {
        return applyVOPMatrix;
    }

    @Override
    public void init() throws Exception {
        LOGGER.info("Loading point cloud filter " + pointcloudFile.getAbsolutePath());
        octree = Util.loadOctree(pointcloudFile, vop);
    }

    @Override
    public boolean accept(Shot.Echo echo) throws Exception {

        Point3d location = new Point3d(
                echo.location.x,
                echo.location.y,
                echo.location.z);

        boolean inside = octree.isPointBelongsToPointcloud(location, pointcloudErrorMargin, Octree.INCREMENTAL_SEARCH);
        return retain ? inside : !inside;
    }
}
