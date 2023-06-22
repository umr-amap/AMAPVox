/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.converter;

import org.amapvox.lidar.leica.ptg.PTGScan;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.amapvox.commons.util.IteratorWithException;
import org.amapvox.shot.Shot;
import org.amapvox.voxelisation.gridded.GriddedScanShotExtractor;

/**
 *
 * @author Julien Heurtebize
 */
public class PTGScanConversion {

    private final float precisionX, precisionY, precisionZ;

    public PTGScanConversion(
            float precisionX, float precisionY, float precisionZ) {
        this.precisionX = precisionX;
        this.precisionY = precisionY;
        this.precisionZ = precisionZ;
    }

    private String getOSName() throws UnsupportedOperationException {

        String osArch = System.getProperty("os.arch");
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.startsWith("win")) {
            if (osArch.equalsIgnoreCase("x86")) {
                throw new UnsupportedOperationException("Platform " + osName + ":" + osArch + " not supported");
            } else {
                return "windows";
            }
        } else if (osName.startsWith("linux")) {
            if (osArch.equalsIgnoreCase("amd64")) {
                return "linux";
            } else if (osArch.equalsIgnoreCase("ia64")) {
                return "linux";
            } else if (osArch.equalsIgnoreCase("i386")) {
                throw new UnsupportedOperationException("Platform " + osName + ":" + osArch + " not supported");
            } else {
                throw new UnsupportedOperationException("Platform " + osName + ":" + osArch + " not supported");
            }
        } else {
            throw new UnsupportedOperationException("Platform " + osName + ":" + osArch + " not supported");
        }
    }

    public void toLaz(SimpleScan scan, File outputDirectory, boolean laz, boolean exportIntensity) throws IOException, InterruptedException, UnsupportedOperationException, Exception {

        /**
         * *Convert rxp to txt**
         */
        Matrix4d transfMatrix = new Matrix4d(scan.popMatrix);
        transfMatrix.mul(scan.sopMatrix);

        Matrix3d rotation = new Matrix3d();
        transfMatrix.getRotationScale(rotation);

        File outputTxtFile = new File(outputDirectory.getAbsolutePath() + File.separator + scan.file.getName() + ".txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputTxtFile))) {
            PTGScan ptgScan = new PTGScan(scan.file);
            ptgScan.open();

            GriddedScanShotExtractor shots = new GriddedScanShotExtractor(ptgScan, transfMatrix);
            IteratorWithException<Shot> it = shots.iterator();
            while (it.hasNext()) {
                Shot shot = it.next();
                shot.direction.normalize();
                Point3d origin = new Point3d(shot.origin);
                transfMatrix.transform(origin);
                Vector3d direction = shot.direction;
                transfMatrix.transform(direction);
                direction.normalize();

                for (int i = 0; i < shot.getEchoesNumber(); i++) {

                    double x = origin.x + direction.x * shot.getRange(i);
                    double y = origin.y + direction.y * shot.getRange(i);
                    double z = origin.z + direction.z * shot.getRange(i);

                    if (exportIntensity) {
                        writer.write(x + " " + y + " " + z + " " + (i + 1) + " " + shot.getEchoesNumber() + " " + shot.getEcho(i).getFloat("intensity") + "\n");
                    } else {
                        writer.write(x + " " + y + " " + z + " " + (i + 1) + " " + shot.getEchoesNumber() + "\n");
                    }

                }
            }
        }

        /**
         * *Convert txt to laz**
         */
        String propertyValue = System.getProperty("user.dir");
        System.out.println("Current jar directory : " + propertyValue);

        String txtToLasPath;

        String osName = getOSName();

        switch (osName) {
            case "windows":
            case "linux":
                txtToLasPath = propertyValue + File.separator + "LASTools" + File.separator + osName + File.separator + "txt2las";
                break;
            default:
                throw new UnsupportedOperationException("Os architecture not supported");
        }

        if (osName.equals("windows")) {
            txtToLasPath = txtToLasPath + ".exe";
        }

        File outputLazFile;
        if (laz) {
            outputLazFile = new File(outputDirectory.getAbsolutePath() + File.separator + scan.file.getName() + ".laz");
        } else {
            outputLazFile = new File(outputDirectory.getAbsolutePath() + File.separator + scan.file.getName() + ".las");
        }

        String[] commandLine;

        if (exportIntensity) {
            commandLine = new String[]{txtToLasPath, "-i", outputTxtFile.getAbsolutePath(),
                "-o", outputLazFile.getAbsolutePath(),
                "-parse", "xyzrni",
                "-set_scale", String.valueOf(precisionX), String.valueOf(precisionY), String.valueOf(precisionZ)
            };
        } else {
            commandLine = new String[]{txtToLasPath, "-i", outputTxtFile.getAbsolutePath(),
                "-o", outputLazFile.getAbsolutePath(),
                "-parse", "xyzrn",
                "-set_scale", String.valueOf(precisionX), String.valueOf(precisionY), String.valueOf(precisionZ)
            };
        }

        System.out.println("Command line : " + stringArrayToString(commandLine));

        ProcessBuilder pb = new ProcessBuilder(commandLine);
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

    private static String stringArrayToString(String[] strArr) {
        StringBuilder sb = new StringBuilder();
        for (String str : strArr) {
            sb.append(str).append(" ");
        }
        return sb.substring(0, sb.length() - 1);
    }

    public void toTxt(SimpleScan scan, File outputDirectory,
            boolean exportRGB, boolean exportIntensity) throws IOException, Exception {

        /**
         * *Convert rxp to txt**
         */
        File outputTxtFile = new File(outputDirectory.getAbsolutePath() + File.separator + scan.file.getName() + ".txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputTxtFile))) {

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

            String header = "directionX directionY directionZ x y z empty";

            if (exportIntensity) {
                header += " intensity";
            }

            if (exportRGB) {
                header += " red green blue";
            }

            header += "\n";

            writer.write(header);

            PTGScan ptgScan = new PTGScan(scan.file);
            ptgScan.open();

            GriddedScanShotExtractor shots = new GriddedScanShotExtractor(ptgScan, transfMatrix);
            IteratorWithException<Shot> it = shots.iterator();
            int shotID = 0;
            while (it.hasNext()) {
                Shot shot = it.next();
                shot.direction.normalize();
                Point3d origin = new Point3d(shot.origin);
                transfMatrix.transform(origin);
                Vector3d direction = shot.direction;
                transfMatrix.transform(direction);
                direction.normalize();

                short empty = 1;

                if (shot.getEchoesNumber() > 0) {
                    empty = 0;
                }

                double x = origin.x + direction.x * 100;
                double y = origin.y + direction.y * 100;
                double z = origin.z + direction.z * 100;

                writer.write(direction.x + " " + direction.y + " " + direction.z + " " + x + " " + y + " " + z + " " + empty + "\n");

//                for(int i=0;i<shot.ranges.length;i++){
//                    
//                    double x = origin.x + direction.x * shot.ranges[i];
//                    double y = origin.y + direction.y * shot.ranges[i];
//                    double z = origin.z + direction.z * shot.ranges[i];
//                    
//                    String echo = shotID + " " + x + " " + y + " " + z + " " + direction.x + " " + direction.y+ " " + direction.z + " " + shot.ranges[i]+" "+shot.ranges.length+" "+i;
//                    
//                    if(exportIntensity){
//                        echo += " " + strictFormat.format(shot.point.intensity);
//                    }
//                    
//                    if(exportRGB){
//                        echo += " " + strictFormat.format(shot.point.red);
//                        echo += " " + strictFormat.format(shot.point.green);
//                        echo += " " + strictFormat.format(shot.point.blue);
//                    }
//                    
//                    echo += "\n";
//                    
//                    writer.write(echo);
//                }
                shotID++;
            }
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }
}
