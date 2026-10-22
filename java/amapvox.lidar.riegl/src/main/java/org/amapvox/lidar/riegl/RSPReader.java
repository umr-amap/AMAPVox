/*
 * Copyright (C) 2016 UMR AMAP (botAnique et Modélisation de l'Architecture des Plantes et des végétations.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.amapvox.lidar.riegl;

import org.amapvox.commons.util.IteratorWithException;
import org.amapvox.lidar.commons.LidarProjectReader;
import org.amapvox.lidar.commons.LidarScan;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Matrix4d;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class RSPReader extends LidarProjectReader {

    private String projectName;
    private ArrayList<LidarScan> rxpList;
    private Matrix4d popMatrix;

    private Document document;
    private SAXBuilder sxb;
    private Element root;

    public RSPReader(File file) throws FileNotFoundException, IOException {
        super(file);
        popMatrix = new Matrix4d();
        popMatrix.setIdentity();
        read(file);
    }

    @Override
    public IteratorWithException<LidarScan> iterator() throws Exception {

        Iterator<LidarScan> it = rxpList.iterator();

        return new IteratorWithException() {

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public LidarScan next() throws Exception {
                return it.next();
            }
        };
    }

    public LidarScan getScan(String name) {

        for (LidarScan scan : rxpList) {
            if (scan.getFile().getName().equals(name)) {
                return scan;
            }
        }

        return null;
    }

    public Matrix4d getPopMatrix() {
        return popMatrix;
    }

    public String getProjectName() {
        return projectName;
    }

    private Matrix4d extractMat4D(String matString) {

        // replace line break by space
        String cs = matString.replaceAll("\n", " ");
        // replace coma by space
        cs = cs.replaceAll(",", " ");
        // replace 2 or more spaces with single space
        cs = cs.trim().replaceAll(" +", " ");

        String[] matSplit = cs.split(" ");
        Matrix4d matrix = new Matrix4d();

        double[] mat = new double[16];

        int index = 0;
        for (int i = 0; i < matSplit.length; i++) {

            matSplit[i] = matSplit[i].trim();
            if (!matSplit[i].isEmpty() && index < 16) {
                Double matxyx = Double.valueOf(matSplit[i]);
                mat[index] = matxyx;
                index++;
            }
        }

        matrix.set(mat);

        return matrix;
    }

    private void read(File rspFile) throws IOException {

        sxb = new SAXBuilder();
        rxpList = new ArrayList();

        //avoid loading of dtd file
        sxb.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        try {
            document = sxb.build(new FileInputStream(rspFile));
            root = document.getRootElement();

            // project name
            projectName = root.getAttributeValue("name");

            // pop matrix
            popMatrix = extractMat4D(root.getChild("pop").getChildText("matrix"));

            // project path name
            String rspPath = rspFile.getParent();

            // <scanpositions> and children <scanposition>
            Element scanPositionsElement = root.getChild("scanpositions");
            List<Element> scanPositionElements = scanPositionsElement.getChildren("scanposition");

            Element dataSourcesElement = root.getChild("datasources");
            if (null != dataSourcesElement) {
                // <app_version> >= 220010000       
                dataSourcesElement.getChildren("datasource").forEach(datasourceElement -> {
                    String dataSourceFold = datasourceElement.getChildText("fold").replaceAll("\\\\", File.separator);
                    scanPositionElements.stream()
                            .filter(this::isRegistered)
                            .forEach(scanPositionElement -> {
                                Element sop = scanPositionElement.getChild("sop");
                                Matrix4d sopMatrix = extractMat4D(sop.getChildText("matrix"));
                                String scanPositionName = scanPositionElement.getAttributeValue("name");
                                Element singleScansElement = scanPositionElement.getChild("singlescans");
                                singleScansElement.getChildren("scan").forEach(scanElement -> {
                                    String scanFold = scanElement.getChild("linkdatasource").getChildText("fold").replaceAll("\\\\", File.separator);
                                    String scanFile = scanElement.getChildText("file");
                                    String scanName = scanElement.getAttributeValue("name");
                                    // Default RXP file, warn if missing
                                    File rxpFile = Path.of(rspPath, dataSourceFold, scanFold, scanFile).toFile();
                                    if (rxpFile.isFile()) {
                                        rxpList.add(new LidarScan(rxpFile, sopMatrix, scanPositionName));
                                    } else {
                                        Logger.getLogger(RSPReader.class.getCanonicalName()).log(Level.WARNING, "RXP file listed in RSP project does not exist ({0})", rxpFile);
                                    }
                                    // Optional MON.RXP file, ignore if missing
                                    File monFile = Path.of(rspPath, dataSourceFold, scanFold, scanName + ".mon.rxp").toFile();
                                    if (monFile.isFile()) {
                                        rxpList.add(new LidarScan(monFile, sopMatrix, scanPositionName));
                                    }
                                });
                            });
                });
            } else {
                // <app_version> < 220010000       
                String scanPositionsFolder = scanPositionsElement.getAttributeValue("fold");
                scanPositionElements.stream()
                        .filter(this::isRegistered)
                        .forEach(scanPositionElement -> {
                            String scanPositionFolder = scanPositionElement.getAttributeValue("fold");
                            Element singlescans = scanPositionElement.getChild("singlescans");
                            String singlescansFolder = singlescans.getAttributeValue("fold");
                            Matrix4d sopMatrix = extractMat4D(scanPositionElement.getChild("sop").getChildText("matrix"));
                            singlescans.getChildren("scan").forEach(scanElement -> {
                                String fileName = scanElement.getChildText("file");
                                // Default RXP file, warn if missing
                                File rxpFile = Path.of(rspPath, scanPositionsFolder, scanPositionFolder, singlescansFolder, fileName).toFile();
                                if (rxpFile.exists()) {
                                    rxpList.add(new LidarScan(rxpFile, sopMatrix, scanPositionFolder));
                                } else {
                                    Logger.getLogger(RSPReader.class.getCanonicalName()).log(Level.WARNING, "RXP file listed in RSP project does not exist ({0})", rxpFile);
                                }
                                // Optional MON.RXP file listed in scan elements
                            });
                        });
            }
            Collections.sort(rxpList, (LidarScan s1, LidarScan s2) -> s1.getFile().compareTo(s2.getFile()));
        } catch (JDOMException | IOException ex) {
            throw new IOException("error parsing or reading rsp: " + rspFile.getAbsolutePath(), ex);
        }
    }

    private boolean isRegistered(Element scanPositionElement) {
        Element registeredElement = scanPositionElement.getChild("registered");
        // assuming that missing <resistered> element is registered
        return (null == registeredElement) || (Integer.parseInt(registeredElement.getText()) == 1);
    }
}
