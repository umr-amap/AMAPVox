/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation.txt;

import org.amapvox.commons.util.IteratorWithException;
import org.amapvox.shot.Shot;
import org.amapvox.voxelisation.las.LasShot;
import org.amapvox.voxelisation.las.PointsToShot;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 *
 * @author pverley
 */
public class TxtShotWriter {
    
    private final PointsToShot alsShotBuilder;
    
    public TxtShotWriter(PointsToShot alsShotBuilder) {
        this.alsShotBuilder = alsShotBuilder;
    }
    
    public void write(File file) throws Exception {
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

            writer.write("xOrigin yOrigin zOrigin xDirection yDirection zDirection nbEchoes r1 r2 r3 r4 r5 r6 r7 c1 c2 c3 c4 c5 c6 c7\n");

            IteratorWithException<Shot> iterator = alsShotBuilder.iterator();

            LasShot shot;

            while ((shot = (LasShot) iterator.next()) != null) {

                String line = shot.origin.x + " " + shot.origin.y + " " + shot.origin.z + " " + shot.direction.x + " " + shot.direction.y + " " + shot.direction.z + " " + shot.getEchoesNumber();

                for (int i = 0; i < shot.getEchoesNumber(); i++) {
                    line += " " + shot.getRange(i);
                }

                for (int i = shot.getEchoesNumber(); i < 7; i++) {
                    line += " " + "NaN";
                }

                for (int i = 0; i < shot.classifications.length; i++) {
                    line += " " + shot.classifications[i];
                }

                for (int i = shot.classifications.length; i < 7; i++) {
                    line += " " + "NaN";
                }

                line += "\n";

                writer.write(line);
            }
        }
    }
    
}
