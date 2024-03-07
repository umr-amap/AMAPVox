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
package org.amapvox.canopy.lai2xxx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author calcul
 */
public class LAI2000 extends LAI2xxx {

    public LAI2000(int shotNumber, ViewCap viewCap, boolean[] masked) {

        super(shotNumber, viewCap, new Ring(13, 0, 7, masked[0], 0.034f, 0.033f),
                new Ring(28, 16, 23, masked[1], 0.104f, 0.097f),
                new Ring(43, 32, 38, masked[2], 0.160f, 0.127f),
                new Ring(58, 47, 53, masked[3], 0.218f, 0.141f),
                new Ring(74, 61, 68, masked[4], 0.494f, 0.102f));

    }

    @Override
    public void writeOutput(File outputFile) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            /**
             * ***write header****
             */
            writer.write("FILE\tDATE\tTIME\tCROP\tPLOT\tLAI\tSEL\tDIFN\tMTA\tSEM\tSMP\n");

            DateFormat dateFormat = new SimpleDateFormat("dd M\tHH:mm:ss");
            String file = "19";
            String dateAndTime = dateFormat.format(Calendar.getInstance().getTime());
            String crop = "WHEAT";
            String plot = "5";
            float sel = Float.NaN;
            float difn = Float.NaN;
            float mta = Float.NaN;
            float sem = Float.NaN;
            float smp = Float.NaN;

            computeValues();

            writer.write(file + "\t" + dateAndTime + "\t" + crop + "\t" + plot + "\t" + LAI + "\t" + sel + "\t" + difn + "\t" + mta + "\t" + sem + "\t" + smp + "\n");

            /**
             * ***write statistics****
             */
            String anglesLine = "",
                    cntcLine = "",
                    stddevLine = "",
                    distsLine = "",
                    gapsLine = "";

            for (int i = 0; i < rings.length; i++) {

                anglesLine += rings[i].getViewAngle() + "\t";
                cntcLine += contactNumberByRing[i] + "\t";
                stddevLine += stdevByRing[i] + "\t";
                distsLine += rings[i].getDist() + "\t";
                gapsLine += gapsByRing[i] + "\t";
            }

            String statistics = "ANGLES" + "\t" + anglesLine + "\n"
                    + "CNTCT#" + "\t" + cntcLine + "\n"
                    + "STDDEV" + "\t" + stddevLine + "\n"
                    + "DISTS" + "\t" + distsLine + "\n"
                    + "GAPS" + "\t" + gapsLine + "\n";

            writer.write(statistics);

            //write observations
        } catch (IOException ex) {
            //logger.error("Cannot write LAI2200 output file", ex);
        }
    }

}
