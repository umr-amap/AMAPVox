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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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

        String[] matSplit = matString.split(" ");
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
            projectName = root.getAttributeValue("name");
            Element scanPositions = root.getChild("scanpositions");
            String folderScanPositions = scanPositions.getAttributeValue("fold");
            List<Element> childrens = scanPositions.getChildren("scanposition");
            popMatrix = extractMat4D(root.getChild("pop").getChildText("matrix"));

            childrens.forEach(child -> {
                String rxpFold = child.getAttributeValue("fold");
                Element registeredElement = child.getChild("registered");
                if (registeredElement != null) {
                    if (Integer.valueOf(registeredElement.getText()) == 1) {

                        Element singlescans = child.getChild("singlescans");
                        String singlescansFold = singlescans.getAttributeValue("fold");

                        List<Element> scansElement = singlescans.getChildren("scan");

                        Element sop = child.getChild("sop");
                        Matrix4d sopMatrix = extractMat4D(sop.getChildText("matrix"));

                        scansElement.forEach(scanElement -> {
                            String name = scanElement.getAttributeValue("name");
                            String fileName = scanElement.getChildText("file");
                            String rspPath = rspFile.getAbsolutePath().substring(0, rspFile.getAbsolutePath().lastIndexOf(File.separator));
                            StringBuilder filePath = new StringBuilder();
                            filePath.append(rspPath).append(File.separator)
                                    .append(folderScanPositions).append(File.separator)
                                    .append(rxpFold).append(File.separator)
                                    .append(singlescansFold).append(File.separator)
                                    .append(fileName);
                            File rxpFile = new File(filePath.toString());
                            rxpList.add(new LidarScan(rxpFile, sopMatrix, name));
                        });
                        Collections.sort(rxpList, (LidarScan s1, LidarScan s2) -> s1.getFile().compareTo(s2.getFile()));
                    } else {
                        //logger.info("Scan "+ rxp.getName() +" skipped cause unregistered");
                    }
                }
            });
        } catch (JDOMException | IOException ex) {
            throw new IOException("error parsing or reading rsp: " + rspFile.getAbsolutePath(), ex);
        }
    }

}
