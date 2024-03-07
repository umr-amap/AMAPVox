/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation;

import org.amapvox.lidar.riegl.RxpExtraction;
import org.amapvox.lidar.riegl.RxpShot;
import org.amapvox.commons.util.IteratorWithException;
import java.io.File;
import java.util.Iterator;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class RXPVoxelization extends AbstractVoxelization {

    private final static Logger LOGGER = Logger.getLogger(RXPVoxelization.class);

    public RXPVoxelization(VoxelizationTask task, VoxelizationCfg cfg, int iscan) {
        super(task, cfg, iscan);
    }

    @Override
    public String getName() {
        return "Voxelisation (RXP)";
    }

    @Override
    public Object call() throws Exception {

        //System.out.println(Thread.currentThread().getName());
        LOGGER.info(logHeader() + " RXP extraction started");

        File[] files;
        try (RxpExtraction rxpExtraction = new RxpExtraction()) {
            int result = rxpExtraction.open(getLidarScan().getFile(), RxpExtraction.REFLECTANCE, RxpExtraction.DEVIATION);
            if (result != 0) {
                LOGGER.error(logHeader() + " Extraction aborted");
                return null;
            }
            String fileName = getLidarScan().getName();
            long fileSize = getLidarScan().getFile().length();
            Iterator<RxpShot> iterator = cfg.isEnableEmptyShotsFiltering()
                    ? new RXPFalseEmptyShotRemover(rxpExtraction.iterator()).iterator()
                    : rxpExtraction.iterator();
            IteratorWithException<org.amapvox.shot.Shot> it = new IteratorWithException<>() {

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public org.amapvox.shot.Shot next() throws Exception {
                    RxpShot shot = iterator.next();
                    Point3d location = new Point3d(shot.origin);
                    transformation.transform(location);
                    Vector3d direction = shot.direction;
                    transformation.transform(direction);
                    // do not fire yet, since task manager does not control
                    // multitask progress
                    fireProgress(fileName, rxpExtraction.progress(), fileSize);
                    return new org.amapvox.shot.Shot(rxpExtraction.getShotID(), location, direction, shot.ranges);
                }
            };
            voxelization.voxelization(it, -1);
            // close rxp reader
        }

        fireSucceeded();

        return null;
    }
}
