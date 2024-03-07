/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.converter;

import org.amapapvox.lidar.txt.ShotWriter2;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import com.sun.jna.Platform;
import org.amapvox.lidar.riegl.RxpExtraction;
import org.amapvox.lidar.riegl.RxpShot;
import org.amapapvox.lidar.txt.Shot;
import org.amapapvox.lidar.txt.Column;
import org.amapapvox.lidar.txt.Echo;
import org.amapapvox.lidar.txt.ShotFileContext;
import org.amapapvox.lidar.txt.ShotWriter;

/**
 *
 * @author calcul
 */
public class RXPScanConversion {

    private final float minReflectance, maxReflectance;
    private final float precisionX, precisionY, precisionZ;

    public RXPScanConversion(float minReflectance, float maxReflectance,
            float precisionX, float precisionY, float precisionZ) {
        this.minReflectance = minReflectance;
        this.maxReflectance = maxReflectance;
        this.precisionX = precisionX;
        this.precisionY = precisionY;
        this.precisionZ = precisionZ;

    }

    private String getTxt2las() throws UnsupportedOperationException {

        String osArch = System.getProperty("os.arch");
        String osName = System.getProperty("os.name").toLowerCase();

        String propertyValue = System.getProperty("user.dir");
//        System.out.println("Current jar directory : " + propertyValue);

        StringBuilder txtToLasPath = new StringBuilder();
        txtToLasPath.append(System.getProperty("user.dir"))
                .append(File.separator)
                .append("LASTools")
                .append(File.separator);

        if (osName.startsWith("win")) {
            txtToLasPath.append("windows").append(File.separator).append("txt2las");
            if (Platform.is64Bit()) {
                txtToLasPath.append("64");
            }
            txtToLasPath.append(".exe");
        } else if (osName.startsWith("linux")) {
            txtToLasPath.append("linux").append(File.separator).append("txt2las");
            if (Platform.is64Bit()) {
                txtToLasPath.append("64");
            } else {
                throw new UnsupportedOperationException("LASTools binary txt2las only provided for 64-bit environment");
            }
        } else {
            throw new UnsupportedOperationException("Platform " + osName + ":" + osArch + " not supported");
        }

        return txtToLasPath.toString();
    }

    private int reflectanceToIntensity(float reflectance, float min, float max) {

        return (int) (65535 * ((reflectance - min) / (max - min)));
    }

    public void toShots2(SimpleScan scan, File outputDirectory, boolean exportReflectance, boolean exportAmplitude, boolean exportDeviation, boolean exportTime, boolean exportXYZ) throws IOException, InterruptedException, UnsupportedOperationException, Exception {

        /**
         * *Convert rxp to txt**
         */
        /**
         * * Includes Shot timeStamp **
         */
        /**
         * *
         * SOP: pour chaque scan --> syst Projet POP: transformation globale du
         * Projet (en particulier vers coordonnées géographiques)
         *
         **
         */
        Matrix4d transfMatrix = new Matrix4d(scan.popMatrix);
        transfMatrix.mul(scan.sopMatrix);

        Matrix3d rotation = new Matrix3d();
        transfMatrix.getRotationScale(rotation);

        File outputTxtFile = new File(outputDirectory.getAbsolutePath() + File.separator + scan.file.getName() + ".txt");
        File outputMDFile = new File(outputDirectory.getAbsolutePath() + File.separator + scan.file.getName() + ".md");
        try (BufferedWriter writerMD = new BufferedWriter(new FileWriter(outputMDFile))) {
            writerMD.write("shotID shotTime nbEchos\n");

            int nbExtraAttributes = 0;
            if (exportReflectance) {
                nbExtraAttributes++;
            }
            if (exportDeviation) {
                nbExtraAttributes++;
            }
            if (exportAmplitude) {
                nbExtraAttributes++;
            }
            if (exportTime) {
                nbExtraAttributes++;
            }
            if (exportXYZ) {
                nbExtraAttributes += 3;
            }

            Column[] extraColumns = new Column[nbExtraAttributes];
            int index = 0;
            if (exportReflectance) {
                extraColumns[index] = new Column("reflectance", Column.Type.FLOAT);
                index++;
            }
            if (exportDeviation) {
                extraColumns[index] = new Column("deviation", Column.Type.FLOAT);
                index++;
            }
            if (exportAmplitude) {
                extraColumns[index] = new Column("amplitude", Column.Type.FLOAT);
                index++;
            }
            if (exportTime) {
                extraColumns[index] = new Column("time", Column.Type.DOUBLE);
                index++;
            }
            if (exportXYZ) {
                extraColumns[index] = new Column("x", Column.Type.DOUBLE);
                index++;
                extraColumns[index] = new Column("y", Column.Type.DOUBLE);
                index++;
                extraColumns[index] = new Column("z", Column.Type.DOUBLE);
                index++;
            }

            ShotFileContext context = new ShotFileContext(extraColumns);

            ShotWriter2 writer = new ShotWriter2(context, outputTxtFile);

            RxpExtraction extraction = new RxpExtraction();

            extraction.open(scan.file, RxpExtraction.REFLECTANCE, RxpExtraction.AMPLITUDE, RxpExtraction.DEVIATION, RxpExtraction.TIME);

            Iterator<RxpShot> iterator = extraction.iterator();

            int shotID = 0;

            while (iterator.hasNext()) {

                RxpShot shot = iterator.next();
                double shotTime = shot.time;

                Point3d origin = new Point3d(shot.origin);
                transfMatrix.transform(origin);
                Vector3d direction = shot.direction;
                transfMatrix.transform(direction);
                direction.normalize();

                writerMD.write(shotID + " " + shotTime + " " + shot.nEcho + "\n");
                if (shot.nEcho == 0) {
                    writer.write(new Shot(shotID, origin.x, origin.y, origin.z, direction.x, direction.y, direction.z), shotTime);
                } else {

                    Echo[] echoes = new Echo[shot.nEcho];

                    for (int i = 0; i < shot.nEcho; i++) {

                        Object[] extra = new Object[nbExtraAttributes];

                        index = 0;
                        if (exportReflectance) {
                            extra[index] = shot.reflectances[i];
                            index++;
                        }
                        if (exportDeviation) {
                            extra[index] = shot.deviations[i];
                            index++;
                        }
                        if (exportAmplitude) {
                            extra[index] = shot.amplitudes[i];
                            index++;
                        }
                        if (exportTime) {
                            extra[index] = shot.times[i];
                            index++;
                        }
                        if (exportXYZ) {
                            extra[index] = shot.origin.x + shot.direction.x * shot.ranges[i];
                            index++;
                            extra[index] = shot.origin.y + shot.direction.y * shot.ranges[i];
                            index++;
                            extra[index] = shot.origin.z + shot.direction.z * shot.ranges[i];
                            index++;
                        }

                        echoes[i] = new Echo(shot.ranges[i], extra);
                    }

                    writer.write(new Shot(shotID, origin.x, origin.y, origin.z, direction.x, direction.y, direction.z, echoes), shotTime);

                }

                shotID++;

            }

            extraction.close();
            writer.close();
        }
    }

    public void toShots(SimpleScan scan, File outputDirectory, boolean exportReflectance, boolean exportAmplitude, boolean exportDeviation, boolean exportTime, boolean exportXYZ) throws IOException, InterruptedException, UnsupportedOperationException, Exception {

        /**
         * *Convert rxp to txt**
         */
        Matrix4d transfMatrix = new Matrix4d(scan.popMatrix);
        transfMatrix.mul(scan.sopMatrix);

        Matrix3d rotation = new Matrix3d();
        transfMatrix.getRotationScale(rotation);
        File outputTxtFile = new File(outputDirectory.getAbsolutePath() + File.separator + scan.file.getName() + ".txt");

        int nbExtraAttributes = 0;
        if (exportReflectance) {
            nbExtraAttributes++;
        }
        if (exportDeviation) {
            nbExtraAttributes++;
        }
        if (exportAmplitude) {
            nbExtraAttributes++;
        }
        if (exportTime) {
            nbExtraAttributes++;
        }
        if (exportXYZ) {
            nbExtraAttributes += 3;
        }

        Column[] extraColumns = new Column[nbExtraAttributes];
        int index = 0;
        if (exportReflectance) {
            extraColumns[index] = new Column("reflectance", Column.Type.FLOAT);
            index++;
        }
        if (exportDeviation) {
            extraColumns[index] = new Column("deviation", Column.Type.FLOAT);
            index++;
        }
        if (exportAmplitude) {
            extraColumns[index] = new Column("amplitude", Column.Type.FLOAT);
            index++;
        }
        if (exportTime) {
            extraColumns[index] = new Column("time", Column.Type.DOUBLE);
            index++;
        }
        if (exportXYZ) {
            extraColumns[index] = new Column("x", Column.Type.DOUBLE);
            index++;
            extraColumns[index] = new Column("y", Column.Type.DOUBLE);
            index++;
            extraColumns[index] = new Column("z", Column.Type.DOUBLE);
            index++;
        }

        ShotFileContext context = new ShotFileContext(extraColumns);

        ShotWriter writer = new ShotWriter(context, outputTxtFile);

        RxpExtraction extraction = new RxpExtraction();

        extraction.open(scan.file, RxpExtraction.REFLECTANCE, RxpExtraction.AMPLITUDE, RxpExtraction.DEVIATION, RxpExtraction.TIME);

        Iterator<RxpShot> iterator = extraction.iterator();

        int shotID = 0;

        while (iterator.hasNext()) {

            RxpShot shot = iterator.next();

            Point3d origin = new Point3d(shot.origin);
            transfMatrix.transform(origin);
            Vector3d direction = shot.direction;
            transfMatrix.transform(direction);
            direction.normalize();

            if (shot.nEcho == 0) {
                writer.write(new Shot(shotID, origin.x, origin.y, origin.z, direction.x, direction.y, direction.z));
            } else {

                Echo[] echoes = new Echo[shot.nEcho];

                for (int i = 0; i < shot.nEcho; i++) {

                    Object[] extra = new Object[nbExtraAttributes];

                    index = 0;
                    if (exportReflectance) {
                        extra[index] = shot.reflectances[i];
                        index++;
                    }
                    if (exportDeviation) {
                        extra[index] = shot.deviations[i];
                        index++;
                    }
                    if (exportAmplitude) {
                        extra[index] = shot.amplitudes[i];
                        index++;
                    }
                    if (exportTime) {
                        extra[index] = shot.times[i];
                        index++;
                    }
                    if (exportXYZ) {
                        extra[index] = shot.origin.x + shot.direction.x * shot.ranges[i];
                        index++;
                        extra[index] = shot.origin.y + shot.direction.y * shot.ranges[i];
                        index++;
                        extra[index] = shot.origin.z + shot.direction.z * shot.ranges[i];
                        index++;
                    }

                    echoes[i] = new Echo(shot.ranges[i], extra);
                }

                writer.write(new Shot(shotID, origin.x, origin.y, origin.z, direction.x, direction.y, direction.z, echoes));
            }

            shotID++;
        }

        extraction.close();
        writer.close();
    }

    public void toLaz(SimpleScan scan, File outputDirectory, boolean laz, boolean exportTime, boolean exportIntensity, boolean exportDeviation, boolean exportAmplitude) throws IOException, InterruptedException, UnsupportedOperationException, Exception {

        /**
         * *Convert rxp to txt**
         */
        Matrix4d transfMatrix = new Matrix4d(scan.popMatrix);
        transfMatrix.mul(scan.sopMatrix);

        Matrix3d rotation = new Matrix3d();
        transfMatrix.getRotationScale(rotation);

        File outputTxtFile = new File(outputDirectory.getAbsolutePath() + File.separator + scan.file.getName() + ".txt");
        try (
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputTxtFile));
                RxpExtraction extraction = new RxpExtraction()) {

            List<Integer> shotTypes = new ArrayList();
            if (exportIntensity) {
                shotTypes.add(RxpExtraction.REFLECTANCE);
            }
            if (exportTime) {
                shotTypes.add(RxpExtraction.TIME);
            }
            if (exportDeviation) {
                shotTypes.add(RxpExtraction.DEVIATION);
            }
            if (exportAmplitude) {
                shotTypes.add(RxpExtraction.AMPLITUDE);
            }

            extraction.open(scan.file, shotTypes.stream().mapToInt(i -> i).toArray());

            Iterator<RxpShot> iterator = extraction.iterator();

            while (iterator.hasNext()) {

                RxpShot shot = iterator.next();

                Point3d origin = new Point3d(shot.origin);
                transfMatrix.transform(origin);
                Vector3d direction = shot.direction;
                transfMatrix.transform(direction);
                direction.normalize();

                for (int i = 0; i < shot.nEcho; i++) {

                    double x = origin.x + direction.x * shot.ranges[i];
                    double y = origin.y + direction.y * shot.ranges[i];
                    double z = origin.z + direction.z * shot.ranges[i];

                    StringBuilder line = new StringBuilder();
                    // xyz
                    line.append(x).append(" ")
                            .append(y).append(" ")
                            .append(z).append(" ");
                    // echo rank
                    line.append(i + 1).append(" ");
                    // number of echo
                    line.append(shot.nEcho).append(" ");
                    // attributes
                    if (exportTime) {
                        line.append(shot.times[i]).append(" ");
                    }
                    if (exportIntensity) {
                        line.append(reflectanceToIntensity(shot.reflectances[i], minReflectance, maxReflectance))
                                .append(" ");
                    }
                    if (exportDeviation) {
                        line.append(shot.deviations[i]).append(" ");
                    }
                    if (exportAmplitude) {
                        line.append(shot.amplitudes[i]).append(" ");
                    }
                    // line break
                    line.append("\n");
                    // write to text file
                    writer.write(line.toString());
                }
            }
        }

        /**
         * *Convert txt to laz**
         */
        File outputLazFile;
        if (laz) {
            outputLazFile = new File(outputDirectory.getAbsolutePath() + File.separator + scan.file.getName() + ".laz");
        } else {
            outputLazFile = new File(outputDirectory.getAbsolutePath() + File.separator + scan.file.getName() + ".las");
        }

        String parse = "xyzrn";
        if (exportTime) {
            parse += "t";
        }
        if (exportIntensity) {
            parse += "i";
        }
        int iattr = 0;
        if (exportDeviation) {
            parse += iattr;
            iattr++;
        }
        if (exportAmplitude) {
            parse += iattr;
            iattr++;
        }

        List<String> cmd = new ArrayList();
        // txt2las
        cmd.add(getTxt2las());
        // parse instruction
        cmd.add("-parse");
        cmd.add(parse);
        // scale option
        cmd.add("-set_scale");
        // x
        cmd.add(String.valueOf(precisionX));
        // y
        cmd.add(String.valueOf(precisionY));
        // z
        cmd.add(String.valueOf(precisionZ));
        // extra-bytes attributes
        if (exportDeviation) {
            cmd.add("-add_attribute");
            cmd.add("9");
            cmd.add("\"deviation\"");
            cmd.add("\"a measure of pulse shape distortion\"");
        }
        if (exportAmplitude) {
            cmd.add("-add_attribute");
            cmd.add("9");
            cmd.add("\"amplitude\"");
            cmd.add("\"relative amplitude in [dB]\"");
        }
        // input
        cmd.add("-i");
        cmd.add(outputTxtFile.getAbsolutePath());
        // output
        cmd.add("-o");
        cmd.add(outputLazFile.getAbsolutePath());

        // print command
        StringBuilder sb = new StringBuilder();
        cmd.forEach(s -> sb.append(s).append(" "));
        System.out.println("Command:" + "\n" + sb.toString());

        // run command
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process p = pb.start();
        int exitCode = p.waitFor();

        if (exitCode == 0) {
            System.err.println("txt2las succeeded. Deleting intermediary text file " + outputTxtFile.getAbsolutePath());
            outputTxtFile.delete();
        } else {
            System.err.println("txt2las command did not succeed. Check log message.");
            System.out.println("intermediary text file kept at " + outputTxtFile.getAbsolutePath());
        }

    }

    public void toTxt(SimpleScan scan, File outputDirectory,
            boolean exportReflectance, boolean exportAmplitude, boolean exportDeviation, boolean exportTime) throws IOException, Exception {

        /**
         * *Convert rxp to txt**
         */
        File outputTxtFile = new File(outputDirectory.getAbsolutePath() + File.separator + scan.file.getName() + ".txt");

        try (
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputTxtFile));
                RxpExtraction extraction = new RxpExtraction()) {

            extraction.open(scan.file, RxpExtraction.AMPLITUDE, RxpExtraction.DEVIATION, RxpExtraction.REFLECTANCE, RxpExtraction.TIME);

            Iterator<RxpShot> iterator = extraction.iterator();

            /**
             * Transformation*
             */
            Matrix4d transfMatrix = new Matrix4d(scan.popMatrix);
            transfMatrix.mul(scan.sopMatrix);

            Matrix3d rotation = new Matrix3d();
            transfMatrix.getRotationScale(rotation);

            DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
            otherSymbols.setDecimalSeparator('.');
            otherSymbols.setGroupingSeparator('.');
            DecimalFormat strictFormat = new DecimalFormat("###.##", otherSymbols);

            String header = "shotID x y z directionX directionY directionZ distance nbEchos rangEcho";

            if (exportReflectance) {
                header += " reflectance";
            }

            if (exportAmplitude) {
                header += " amplitude";
            }

            if (exportDeviation) {
                header += " deviation";
            }

            if (exportTime) {
                header += " time";
            }

            header += "\n";

            writer.write(header);

            int shotID = 0;
            while (iterator.hasNext()) {

                RxpShot shot = iterator.next();

                Point3d origin = new Point3d(shot.origin);
                transfMatrix.transform(origin);
                Vector3d direction = shot.direction;
                transfMatrix.transform(direction);
                direction.normalize();

                for (int i = 0; i < shot.nEcho; i++) {

                    double x = origin.x + direction.x * shot.ranges[i];
                    double y = origin.y + direction.y * shot.ranges[i];
                    double z = origin.z + direction.z * shot.ranges[i];

                    String echo = shotID + " " + x + " " + y + " " + z + " " + direction.x + " " + direction.y + " " + direction.z + " " + shot.ranges[i] + " " + shot.nEcho + " " + i;

                    if (exportReflectance) {
                        echo += " " + strictFormat.format(shot.reflectances[i]);
                    }

                    if (exportAmplitude) {
                        echo += " " + strictFormat.format(shot.amplitudes[i]);
                    }

                    if (exportDeviation) {
                        echo += " " + strictFormat.format(shot.deviations[i]);
                    }

                    if (exportTime) {
                        echo += " " + shot.times[i];
                    }

                    echo += "\n";

                    writer.write(echo);
                }

                shotID++;
            }

        }

    }
}
