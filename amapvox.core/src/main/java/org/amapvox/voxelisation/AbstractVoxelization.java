/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation;

import org.amapvox.commons.math.util.MatrixUtility;
import org.amapvox.commons.util.CallableTask;
import org.amapvox.commons.util.filter.Filter;
import org.amapvox.lidar.commons.LidarScan;
import javax.vecmath.Matrix4d;
import org.amapvox.shot.filter.DigitalTerrainModelFilter;
import org.amapvox.shot.filter.EchoRankFilter;

/**
 *
 * @author calcul
 */
public abstract class AbstractVoxelization extends CallableTask<Object> {

    protected final VoxelizationTask parent;
    protected VoxelizationCfg cfg;
    protected Voxelization voxelization;
    protected Matrix4d transformation;
    private final int iscan;
    private String logHeader;
    
    abstract public String getName();
    

    public AbstractVoxelization(VoxelizationTask task, VoxelizationCfg cfg, int iscan) {
        this.parent = task;
        this.cfg = cfg;
        this.iscan = iscan;
        logHeader = "[Voxelisation]";
    }

    public LidarScan getLidarScan() {
        return cfg.getLidarScans().get(iscan);
    }
    
    public String logHeader() {
        return logHeader;
    }

    protected void init() throws Exception {
        
        int nscan = cfg.getLidarScans().size();
        logHeader = "[Voxelisation " + (iscan + 1) + "/" + nscan + "]";

        // Transformation matrices
        Matrix4d pop = (null == cfg.getPopMatrix())
                ? MatrixUtility.identity4d()
                : new Matrix4d(cfg.getPopMatrix());

        Matrix4d vop = (null == cfg.getVopMatrix())
                ? MatrixUtility.identity4d()
                : new Matrix4d(cfg.getVopMatrix());

        Matrix4d sop = getLidarScan().getMatrix();

        // multiply vop by pop
        transformation = new Matrix4d(vop);
        transformation.mul(pop);
        // multiply voppop by sop
        transformation.mul(sop);

        // echo weight by file
        cfg.setLidarScan(getLidarScan());

        for (Filter filter : cfg.getEchoFilters()) {
            // echo rank filter by file
            if (filter instanceof EchoRankFilter) {
                ((EchoRankFilter) filter).setScanName(getLidarScan().getFile().getName());
            }
            // dtm filter
            if (filter instanceof DigitalTerrainModelFilter) {
                ((DigitalTerrainModelFilter) filter).setDTM(parent.getDTM());
            }
        }

        voxelization = new Voxelization(cfg, logHeader, parent.getDTM(),
                parent.getVoxelSpace(), parent.getPathLengthOutput());
        voxelization.addProcessingListener(parent);
    }

    void setCancelled(boolean cancelled) {
        // propagate cancellation to Voxelization.java
        voxelization.setCancelled(cancelled);
    }
}
