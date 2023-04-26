/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation.txt;

import org.amapvox.commons.util.IteratorWithException;
import org.amapvox.commons.util.LineCount;
import org.amapvox.shot.Shot;
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
    private Shot shot;
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
    public Shot next() throws IOException {

        if (hasNextCalled) {
            hasNextCalled = false;
            return shot;
        } else {
            return nextShot();
        }
    }

    private Shot nextShot() throws IOException {

        line = reader.readLine();
        if (null == line) {
            reader.close();
            return null;
        }

        // increment shot ID
        shotId++;

        String[] split = line.split(" ");
        double xOrigin = Double.parseDouble(split[0]);
        double yOrigin = Double.parseDouble(split[1]);
        double zOrigin = Double.parseDouble(split[2]);

        double xDirection = Double.parseDouble(split[3]);
        double yDirection = Double.parseDouble(split[4]);
        double zDirection = Double.parseDouble(split[5]);

        int nEcho = Integer.parseInt(split[6]);

        double[] ranges = new double[nEcho];
        int[] classifications = new int[nEcho];

        for (int i = 0; i < ranges.length; i++) {
            if ((14 + i) > split.length) {
                throw new IOException("Columns missing inside shot file");
            }
            ranges[i] = Double.parseDouble(split[7 + i]);
            classifications[i] = Integer.parseInt(split[14 + i]);
        }

        Shot shot = new Shot(shotId, new Point3d(xOrigin, yOrigin, zOrigin), new Vector3d(xDirection, yDirection, zDirection), ranges);
        for (int r = 0; r < nEcho; r++) {
            shot.getEcho(r).addInteger("classification", classifications[r]);
        }

        return shot;
    }
}
