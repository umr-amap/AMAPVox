/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.riegl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

/**
 *
 * @author pverley
 */
public class RxpReader {

    public static void main(String[] args) {

        try {

            if (null == args || args.length < 1) {
                throw new NullPointerException("No RXP file provided...");
            }

            File file = new File(args[0]);
            if (!file.exists()) {
                throw new FileNotFoundException("RXP file " + file.toString() + " does not exist");
            }

            try (RxpExtraction extraction = new RxpExtraction()) {
                extraction.open(file);
                Iterator<RxpShot> iterator = extraction.iterator();
                ProgressBarBuilder pbb = new ProgressBarBuilder()
                        .setInitialMax(file.length())
                        .setTaskName(file.getName())
                        .setUnit("MiB", 1048576)
                        .setStyle(ProgressBarStyle.ASCII);
                RxpShot shot;
                int nshot = 0;
                Logger.getLogger(RxpExtraction.class.getName()).log(Level.INFO, "Reading RXP file {0}", file.getName());
                try (ProgressBar pb = pbb.build()) {
                    while (iterator.hasNext()) {
                        shot = iterator.next();
                        nshot++;
                        pb.stepTo(extraction.progress());
                    }
                }
                Logger.getLogger(RxpExtraction.class.getName()).log(Level.INFO, "Scanned {0} shots successfully", nshot);
            }

        } catch (Exception ex) {
            Logger.getLogger(RxpExtraction.class.getName()).log(Level.SEVERE, "Failed to read RXP file", ex);
        }
    }

}
