/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation.txt;

import org.amapvox.commons.util.IteratorWithException;
import org.amapvox.commons.util.LineCount;
import org.amapvox.shot.Shot;
import org.amapvox.voxelisation.las.LasShot;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author pverley
 */
public class TxtShotReader implements IteratorWithException<Shot> {

    private final BufferedReader reader;
    private String line;
    private int shotId;
    private boolean hasNextCalled = false;
    private LasShot shot;
    private final int nshot;

    TxtShotReader(File shotsFile) throws FileNotFoundException, IOException {
        reader = new BufferedReader(new FileReader(shotsFile));
        //skip header
        reader.readLine();
        // initialise shot ID
        shotId = -1;
        nshot = LineCount.count(shotsFile.getAbsolutePath());
    }
    
    public int getNShot() {
        return nshot;
    }

    @Override
    public boolean hasNext() throws IOException {
        if (!hasNextCalled) {
            hasNextCalled = true;
            shot = nextShot();
        }
        return shot != null;
    }

    @Override
    public LasShot next() throws IOException {
        
        if (hasNextCalled) {
            hasNextCalled = false;
            return shot;
        } else {
            return nextShot();
        }
    }

    private LasShot nextShot() throws IOException {

        line = reader.readLine();
        if (null == line) {
            reader.close();
            return null;
        }

        // increment shot ID
        shotId++;

        String[] split = line.split(" ");
        double xOrigin = Double.valueOf(split[0]);
        double yOrigin = Double.valueOf(split[1]);
        double zOrigin = Double.valueOf(split[2]);

        double xDirection = Double.valueOf(split[3]);
        double yDirection = Double.valueOf(split[4]);
        double zDirection = Double.valueOf(split[5]);

        int nbEchos = Integer.valueOf(split[6]);

        double[] ranges = new double[nbEchos];
        int[] classifications = new int[nbEchos];

        for (int i = 0; i < ranges.length; i++) {
            if ((14 + i) > split.length) {
                throw new IOException("Columns missing inside shot file");
            }
            ranges[i] = Double.valueOf(split[7 + i]);
            classifications[i] = Integer.valueOf(split[14 + i]);
        }

        LasShot shot = new LasShot(shotId, new Point3d(xOrigin, yOrigin, zOrigin), new Vector3d(xDirection, yDirection, zDirection), ranges);
        shot.classifications = classifications;

        return shot;
    }
}
