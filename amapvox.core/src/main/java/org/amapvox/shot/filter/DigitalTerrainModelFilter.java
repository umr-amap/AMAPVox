/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.shot.filter;

import org.amapvox.commons.raster.asc.Raster;
import org.amapvox.commons.util.filter.Filter;
import org.amapvox.shot.Shot;

/**
 *
 * @author pverley
 */
public class DigitalTerrainModelFilter implements Filter<Shot.Echo> {

    private final float minDistance;
    private Raster dtm;

    public DigitalTerrainModelFilter(float minDistance) {
        this.minDistance = minDistance;
    }

    @Override
    public void init() throws Exception {
        // nothing to do
        // dtm is loaded once by DigitalTerrainModelManager
    }

    public void setDTM(Raster dtm) {
        this.dtm = dtm;
    }

    public float getMinDistance() {
        return minDistance;
    }

    @Override
    public boolean accept(Shot.Echo echo) throws Exception {

        double distanceToGround = echo.location.z - dtm.getSimpleHeight((float) echo.location.x, (float) echo.location.y);
        return distanceToGround >= minDistance;
    }

}
