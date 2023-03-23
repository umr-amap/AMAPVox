/**
 *
 */
package org.amapvox.canopy.hemi;

import org.amapvox.lidar.commons.LidarScan;
import org.amapvox.commons.math.util.SphericalCoordinates;
import org.amapvox.lidar.riegl.RxpExtraction;
import org.amapvox.lidar.riegl.RxpShot;
import org.amapvox.canopy.DirectionalTransmittance;
import org.amapvox.canopy.LeafAngleDistribution;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import java.io.File;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.vecmath.Point2i;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.apache.commons.math3.util.FastMath;
import org.apache.log4j.Logger;
import org.amapvox.commons.AVoxTask;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;

/**
 * @author dauzat
 *
 */
public class HemiScanView extends AVoxTask {

    private final static Logger LOGGER = Logger.getLogger(HemiScanView.class);

    private final static float SKY_LUMINANCE = 1f;
    private final static float CANOPY_LUMINANCE = 0.1f;
    private Point3f rgbSky;
    private Point3f rgbCan;

    private int nbPixels;
    private int decimation; // the proportion of shots used for computation is 1/decimation

    private int minSampling;	// minimum number of shots sampling a sector for calculating its gap fraction

    private int nbAzimuts;
    private int nbZeniths;
    private Pixel[][] pixTab;
    private Sector[][] sectorTable;
    private boolean random;	// the color of pixels is drawn randomly depending on the gap fraction
    private Matrix4d transformation;

    private HemiParameters parameters;

    private final String logHeader = "[Hemispherical photography]";

    public HemiScanView(File file, int ncpu) {
        super(file, ncpu);
    }

    @Override
    public String getName() {
        return "Hemispherical photography";
    }

    @Override
    protected void doInit() throws Exception {
        
        parameters = ((HemiPhotoCfg) getConfiguration()).getParameters();

        nbPixels = parameters.getPixelNumber();
        nbZeniths = parameters.getZenithsNumber(); //6;
        nbAzimuts = parameters.getAzimutsNumber(); //24;
        random = true;
        minSampling = 50;
        rgbSky = new Point3f(0, 0, 255);
        rgbCan = new Point3f(0, 255, 0);
    }

    @Override
    protected Class<HemiPhotoCfg> getConfigurationClass() {
        return HemiPhotoCfg.class;
    }

    public class Pixel {

        int nbShots;
        float brightness;
        float azimut;
        float zenith;

        public Pixel() {

            this.nbShots = 0;
            this.brightness = Float.NaN;
        }

        protected void updatePixel(float luminance) {
            if (nbShots == 0 || Float.isNaN(brightness)) {
                brightness = luminance;
                nbShots++;
            } else {
                float newBrightness = (brightness * nbShots) + (luminance);
                nbShots++;
                brightness = newBrightness / (float) nbShots;
            }
        }

        public int getNbShots() {
            return nbShots;
        }

        public float getBrightness() {
            return brightness;
        }

    }

    private class Sector {

        int nbShots;
        float brightness;

        public Sector() {
            super();
            this.nbShots = 0;
            this.brightness = 0;
        }

        protected void updateSector(float luminance) {
            if (nbShots == 0) {
                brightness = luminance;
                nbShots++;
            } else {
                float newBrightness = (brightness * nbShots) + (luminance);
                nbShots++;
                brightness = newBrightness / (float) nbShots;
            }
        }
    }

    private void initArrays() {

        pixTab = new Pixel[nbPixels][];
        for (int x = 0; x < nbPixels; x++) {
            pixTab[x] = new Pixel[nbPixels];
            for (int y = 0; y < nbPixels; y++) {
                pixTab[x][y] = new Pixel();
            }
        }
        // table of azimuthAngle/zenith sectors
        sectorTable = new Sector[nbZeniths][];
        for (int z = 0; z < nbZeniths; z++) {
            sectorTable[z] = new Sector[nbAzimuts];
            for (int a = 0; a < nbAzimuts; a++) {
                sectorTable[z][a] = new Sector();
            }
        }
    }

    @Override
    public File[] call() throws Exception {
        
        LOGGER.info(logHeader + " started...");

        switch (parameters.getMode()) {
            case ECHOS:
                

                initArrays();

                for (LidarScan scan : parameters.getRxpScansList()) {

                    if (isCancelled()) {
                        return null;
                    }

                    setTransformation(scan.getMatrix());
                    setScan(scan.getFile());
                }

                if (parameters.isGenerateBitmapFile()) {

                    if (isCancelled()) {
                        return null;
                    }

                    switch (parameters.getBitmapMode()) {
                        case PIXEL:
                            writeHemiPhoto(parameters.getOutputBitmapFile());
                            break;
                        case COLOR:
                            sectorTable(parameters.getOutputBitmapFile());
                            break;
                    }
                    return new File[]{parameters.getOutputBitmapFile()};
                }

                break;

            case PAD:
                DirectionalTransmittance dt = new DirectionalTransmittance(
                        parameters.getVoxelFile(),
                        parameters.getPADVariable(),
                        LeafAngleDistribution.Type.SPHERIC);
                return hemiFromPAD(dt, parameters.getSensorPositions());
        }
        return null;
    }

    public void setTransformation(Matrix4d matrix) {

        transformation = matrix;
    }

    public final Matrix3d getRotationFromMatrix(Matrix4d matrix) {

        Matrix3d rotation = new Matrix3d();
        matrix.getRotationScale(rotation);
        return rotation;
    }

    public void setScan(File scan) throws Exception {

        int count = 0;
        int totalShots = 0;

        RxpExtraction extraction = new RxpExtraction();

        try {
            extraction.open(scan);

            Iterator<RxpShot> iterator = extraction.iterator();

            while (iterator.hasNext()) {

                if (isCancelled()) {
                    return;
                }

                RxpShot shot = iterator.next();

                Point3d location = shot.origin;
                transformation.transform(location);
                Vector3d direction = shot.direction;
                transformation.transform(direction);

                shot.setOriginAndDirection(location, direction);

                transform(shot);

                count++;
                totalShots++;
                if (count == 1000000) {
                    LOGGER.info(logHeader + " shots " + totalShots);
                    count = 0;
                }
            }

            LOGGER.info(logHeader + " shots " + totalShots);

            extraction.close();
        } catch (Exception ex) {
            throw ex;
        }
    }

    private void sectorTable(File outputFile) throws IOException {

        float radius = nbPixels / 2f;
        float zenithWidth = (float) ((Math.PI / 2) / nbZeniths);
        float azimWidth = (float) ((Math.PI * 2) / nbAzimuts);

        for (int z = 0; z < nbZeniths; z++) {
            System.out.println();
            for (int a = 0; a < nbAzimuts; a++) {
                System.out.print("\t" + sectorTable[z][a].brightness);
            }
        }

        for (int x = 0; x < nbPixels; x++) {
            for (int y = 0; y < nbPixels; y++) {
                Vector2f v = new Vector2f((x - radius) / radius, (y - radius) / radius);
                double zen = v.length() * Math.PI / 2;
                if (zen < Math.PI / 2) {

                    double azim = xyAzimuthNW(v.x, v.y);

                    int sx = (int) (zen / zenithWidth);
                    int sy = (int) (azim / azimWidth);

                    if (sectorTable[sx][sy].nbShots > minSampling) {
                        if (random) {
                            float gf = (sectorTable[sx][sy].brightness - CANOPY_LUMINANCE) / (SKY_LUMINANCE - CANOPY_LUMINANCE);
                            if (Math.random() > gf) {
                                pixTab[x][y].brightness = CANOPY_LUMINANCE;
                            } else {
                                pixTab[x][y].brightness = SKY_LUMINANCE;
                            }
                        } else {
                            pixTab[x][y].brightness = sectorTable[sx][sy].brightness;
                        }
                    }
                }
            }
        }

        writeHemiPhoto(outputFile);
    }

    private File[] hemiFromPAD(DirectionalTransmittance dt, List<Point3d> positions) throws Exception {

        int positionID = 0;

        List<File> outputFiles = new ArrayList<>();

        for (Point3d position : positions) {
            
            LOGGER.info(logHeader + " from position " + position);

            initArrays();

            float center = nbPixels / 2;

            for (int i = 0; i < nbPixels; i++) {
                for (int j = 0; j < nbPixels; j++) {

                    if (isCancelled()) {
                        return null;
                    }

                    float deltaX = i + 0.5f - center;
                    float deltaY = j + 0.5f - center;
                    double distToCenter = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));

                    if (distToCenter < center) {

                        double zenithAngle = (distToCenter / center) * Math.PI / 2;
                        double azimuthAngle = 0;

                        if (deltaY != 0) {
                            azimuthAngle = Math.atan(deltaX / deltaY);
                            if (deltaY < 0) {
                                azimuthAngle += Math.PI;
                            } else if (deltaX < 0) {
                                azimuthAngle += Math.PI * 2;
                            }
                        } else if (deltaX < 0) {
                            azimuthAngle = Math.PI / 2;
                        }

                        Vector3f direction = new Vector3f(SphericalCoordinates.toCartesian(azimuthAngle, zenithAngle));

                        /*Vector3f direction = new Vector3f(0,0,1);
                        Transformations transform = new Transformations();
                        transform.setRotationAroundX(zenithAngle);
                        transform.setRotationAroundZ(azimuthAngle);
                        transform.apply(direction);*/

 /*if(direction.x != rayDirection.x || direction.y != rayDirection.y || direction.z != rayDirection.z){
                            System.out.println("test");
                        }*/
                        pixTab[i][j].azimut = (float) Math.toDegrees(azimuthAngle);
                        pixTab[i][j].zenith = (float) Math.toDegrees(zenithAngle);

                        double transmittance = dt.directionalTransmittance(position, new Vector3d(direction.x, direction.y, direction.z));
                        if (!Double.isNaN(transmittance)) {
                            pixTab[i][j].updatePixel((float) transmittance);
                        }
                    }
                }
            }

            if (parameters.isGenerateBitmapFile()) {

                if (isCancelled()) {
                    return null;
                }

                File outputFile = new File(parameters.getOutputBitmapFile(), "position_" + positionID + ".png");

                switch (parameters.getBitmapMode()) {
                    case PIXEL:
                        writeHemiPhoto(outputFile);
                        break;
                    case COLOR:
                        sectorTable(outputFile);
                        break;
                }
                outputFiles.add(outputFile);
            }

            if (parameters.isGenerateTextFile()) {

                if (isCancelled()) {
                    return null;
                }

                File outputFile = new File(parameters.getOutputTextFile(), "position_" + positionID + ".txt");
                writeHemiPhotoAsText(outputFile);
                outputFiles.add(outputFile);
            }

            positionID++;
        }
        return outputFiles.toArray(new File[outputFiles.size()]);
    }

//    private void transform(String line, Transformations tr) {
//        String[] st = line.split(" ");
//        int echocount = Integer.valueOf(st[1]);
//        Point3d origin = new Point3d(Double.valueOf(st[2]), Double.valueOf(st[3]), Double.valueOf(st[4]));
//        Vector3d direction = new Vector3d(Double.valueOf(st[5]), Double.valueOf(st[6]), Double.valueOf(st[7]));
//        double range = -9999;
//        if (echocount > 0) {
//            range = Float.valueOf(st[8]);
//        }
//        tr.apply(direction);
//        tr.apply(origin);
//        direction.sub(origin);
//        direction.normalize();
//        float zenith = (float) FastMath.acos(direction.z);
//        double azimuth = xyAzimuthNW(direction.x, direction.y);
//        
//        SphericalCoordinates sc = new SphericalCoordinates();
//        sc.toSpherical(direction);
//
//        if (zenith < Math.PI / 2) {
//            updatePixTab(zenith, azimuth, range);
//            updateSectorTab(zenith, azimuth, range);
//        }
//    }
    public void transform(RxpShot shot) {

        double zenith = FastMath.acos(shot.direction.z);
        double azimut = xyAzimuthNW(shot.direction.x, shot.direction.y);

        double range = -9999;
        if (shot.nEcho > 0) {
            range = shot.ranges[shot.nEcho - 1];
        }

        if (zenith < Math.PI / 2) {
            updatePixTab(zenith, azimut, range);
            updateSectorTab(zenith, azimut, range);
        }
    }

    public void writeHemiPhotoAsText(File outputFile) throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            
            LOGGER.info(logHeader + " writing image (as text) " + outputFile.getName());

            DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
            otherSymbols.setDecimalSeparator('.');
            otherSymbols.setGroupingSeparator('.');

            DecimalFormat df = new DecimalFormat("#0.000", otherSymbols);

            writer.write("azimut zenith transmittance\n");

            float center = nbPixels / 2;

            for (int x = 0; x < pixTab.length; x++) {
                for (int y = 0; y < pixTab[x].length; y++) {

                    if (isCancelled()) {
                        return;
                    }

                    float deltaX = x + 0.5f - center;
                    float deltaY = y + 0.5f - center;
                    double distToCenter = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));

                    if (distToCenter < center) {

                        if (Float.isNaN(pixTab[x][y].azimut)) {
                            writer.write(df.format(pixTab[x][y].azimut) + " " + df.format(pixTab[x][y].zenith) + " " + pixTab[x][y].brightness + "\n");
                        } else {
                            writer.write(df.format(pixTab[x][y].azimut) + " " + df.format(pixTab[x][y].zenith) + " " + df.format(pixTab[x][y].brightness) + "\n");
                        }
                    }

                }
            }
        }
    }

    public void writeHemiPhoto(File outputFile) throws IOException {

        int border = 30;
        int nbPixImage = pixTab.length + (2 * border); //= 600;
        float center = nbPixImage / 2;
        float radius = center - border;

        BufferedImage bimg = new BufferedImage(nbPixImage, nbPixImage, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bimg.createGraphics();

        // background
        g.setColor(new Color(80, 30, 0));
        g.fillRect(0, 0, nbPixImage, nbPixImage);

        // black sky vault
        g.setColor(new Color(0, 0, 0));
        g.fillOval((int) (center - radius), (int) (center - radius), (int) (2 * radius), (int) (2 * radius));

        // draw points
        for (int x = 0; x < pixTab.length; x++) {
            for (int y = 0; y < pixTab[x].length; y++) {
                int yn = pixTab.length - 1 - y; // North in Y+
                if (pixTab[x][y].brightness > 0) {
                    float gf = (pixTab[x][y].brightness - CANOPY_LUMINANCE) / (SKY_LUMINANCE - CANOPY_LUMINANCE);
                    Point3f rgbr = new Point3f(rgbCan);
                    rgbr.scale(1 - gf);
                    Point3f rgbb = new Point3f(rgbSky);
                    rgbb.scale(gf);
                    Point3f rgb = new Point3f(rgbb);
                    rgb.add(rgbr);
                    rgb.x = Math.max(rgb.x, 0);
                    rgb.y = Math.max(rgb.y, 0);
                    rgb.z = Math.max(rgb.z, 0);
                    rgb.x = Math.min(rgb.x, 255);
                    rgb.y = Math.min(rgb.y, 255);
                    rgb.z = Math.min(rgb.z, 255);
                    Point3i c = new Point3i((int) rgb.x, (int) rgb.y, (int) rgb.z);
                    g.setColor(new Color(c.x, c.y, c.z));
                    g.drawRect(x + border, yn + border, 1, 1);
                }
            }
        }

        // parallels
        g.setColor(new Color(220, 240, 255));
        double rad = radius / nbZeniths;
        for (int i = 1; i <= nbZeniths; i++) {
            g.drawOval((int) (center - rad * i), (int) (center - rad * i), (int) (2 * rad * i), (int) (2 * rad * i));
        }
        // meridians
        for (int i = 1; i <= nbAzimuts; i++) {
            double azimuth = i * (Math.PI * 2 / nbAzimuts);
            double x = radius * Math.sin(azimuth);
            double y = radius * Math.cos(azimuth);
            g.drawLine((int) (center - x), (int) (center - y), (int) (center + x), (int) (center + y));
        }

        // cardinal points
        g.drawString("N", (int) center, border / 2);
        g.drawString("S", (int) center, nbPixImage - (border / 2));
        g.drawString("E", nbPixImage - (border / 2), center);
        g.drawString("W", border / 2, center);

        try {
            LOGGER.info(logHeader + " writing image " + outputFile.getName());
            ImageIO.write(bimg, "png", outputFile);
        } catch (IOException ex) {
            throw ex;
        }
    }

    /**
     * Get azimuth angle from 2D coordinates
     *
     * @param x 2D coordinates
     * @param y 2D coordinates
     * @return	azimuthAngle	[radian] clockwise from Y axis
     */
    public static double xyAzimuthNW(double x, double y) {

        double azimuth = 0;
        if (y != 0) {

            azimuth = FastMath.atan(x / y);
            if (y < 0) {
                azimuth += Math.PI;
            } else if (x < 0) {
                azimuth += Math.PI * 2;
            }
        } else if (x < 0) {
            azimuth = (Math.PI / 2);
        }

        return azimuth;
    }

    private void updatePixTab(double zenith, double azimut, double distance) {

        double radius = nbPixels / 2f;

        //normalization from 0 to radius
        double normalizedRadius = ((zenith / (Math.PI / 2)) * radius);

        double x = normalizedRadius * Math.cos(azimut);
        double y = normalizedRadius * Math.sin(azimut);

        int indexX = (int) (x + radius);
        int indexY = (int) (y + radius);

        indexX = Math.min(indexX, nbPixels - 1);
        indexY = Math.min(indexY, nbPixels - 1);

        if (distance < 0) {
            pixTab[indexX][indexY].updatePixel(SKY_LUMINANCE);
        } else {
            pixTab[indexX][indexY].updatePixel(CANOPY_LUMINANCE);
        }
    }

    public static Point2i getPixelIndicesFromDirection(int nbPixels, Vector3f direction) {

        double zenith = FastMath.acos(direction.z);
        double azimut = xyAzimuthNW(direction.x, direction.y);

        double radius = nbPixels / 2f;

        //normalization from 0 to radius
        double normalizedRadius = ((zenith / (Math.PI / 2)) * radius);

        double x = (normalizedRadius * Math.cos(azimut));
        double y = (normalizedRadius * Math.sin(azimut));

        int indexX = (int) (x + radius);
        int indexY = (int) (y + radius);

        indexX = Math.min(indexX, nbPixels - 1);
        indexY = Math.min(indexY, nbPixels - 1);

        return new Point2i(indexX, indexY);
    }

    public static Vector3d getDirectionFromPixel(int nbPixels, int i, int j) {

        double radius = nbPixels / 2;

        int centerX = nbPixels / 2;
        int centerY = centerX;

        double x = i - centerX;
        double y = j - centerY;

        double normalizedRadius = Math.sqrt((x * x) + (y * y));

        double azimut = Math.acos(x / normalizedRadius);
        double zenith = (normalizedRadius / radius) * Math.PI / 2.0;

        SphericalCoordinates sc = new SphericalCoordinates(azimut, zenith);
        Vector3d direction = new Vector3d(SphericalCoordinates.toCartesian(azimut, zenith));
        return direction;
    }

    private void updateSectorTab(double zenith, double azimuth, double distance) {

        int indexZn = (int) ((zenith * nbZeniths) / (Math.PI / 2));
        int indexAz = (int) ((azimuth * nbAzimuts) / (Math.PI * 2));

        if (distance < 0) {
            sectorTable[indexZn][indexAz].updateSector(SKY_LUMINANCE);
        } else {
            sectorTable[indexZn][indexAz].updateSector(CANOPY_LUMINANCE);
        }
    }

    public int getNbPixels() {
        return nbPixels;
    }

    public Pixel[][] getPixTab() {
        return pixTab;
    }

}
