/**
 *
 */
package org.amapvox.canopy.hemi;

import org.amapvox.commons.math.util.SphericalCoordinates;
import org.amapvox.canopy.DirectionalTransmittance;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3f;

import java.io.File;
import javax.imageio.ImageIO;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.apache.log4j.Logger;
import org.amapvox.commons.AVoxTask;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

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

    private int nbAzimuts;
    private int nbZeniths;
    private Pixel[][] pixTab;

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

    private void initArrays() {

        pixTab = new Pixel[nbPixels][];
        for (int x = 0; x < nbPixels; x++) {
            pixTab[x] = new Pixel[nbPixels];
            for (int y = 0; y < nbPixels; y++) {
                pixTab[x][y] = new Pixel();
            }
        }
    }

    @Override
    public File[] call() throws Exception {

        LOGGER.info(logHeader + " started...");

        DirectionalTransmittance direcTransmittance = new DirectionalTransmittance(
                parameters.getVoxelFile(),
                parameters.getPADVariable(),
                parameters.getLeafAngleDistribution(),
                parameters.getLeafAngleDistributionParameters());
        return hemiFromPAD(direcTransmittance, parameters.getSensorPositions());
    }

    private File[] hemiFromPAD(DirectionalTransmittance dt, List<Point3d> positions) throws Exception {

        int positionID = 0;

        List<File> outputFiles = new ArrayList();

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

            if (isCancelled()) {
                return null;
            }

            File hemiphotoFile = new File(parameters.getOutputBitmapFile(), "position_" + positionID + ".png");
            writeHemiPhoto(hemiphotoFile);
            outputFiles.add(hemiphotoFile);

            if (isCancelled()) {
                return null;
            }

            File hemiPhotoTextFile = new File(parameters.getOutputTextFile(), "position_" + positionID + ".txt");
            writeHemiPhotoAsText(hemiPhotoTextFile);
            outputFiles.add(hemiPhotoTextFile);
        }

        positionID++;

        return outputFiles.toArray(File[]::new);
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

    public int getNbPixels() {
        return nbPixels;
    }

    public Pixel[][] getPixTab() {
        return pixTab;
    }

}
