/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.raster.asc;

import org.amapvox.commons.math.geometry.BoundingBox2F;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point2f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector4d;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Raster {

    private ArrayList<Point> points;
    private ArrayList<Face> faces;

    private float zMin;
    private float zMax;

    private int indiceXMin = -1;
    private int indiceYMin = -1;
    private int indiceXMax = -1;
    private int indiceYMax = -1;

    private String path;

    private float[][] zArray;
    private float xLeftLowerCorner;
    private float yLeftLowerCorner;
    private float cellSize;
    private int rowNumber;
    private int colNumber;

    private Matrix4d transformationMatrix;
    private Matrix4d inverseTransfMat;

    private boolean built;

    /**
     *
     */
    public Raster() {

    }

    /**
     *
     * @return
     */
    public String getPath() {
        return path;
    }

    /**
     *
     * @return
     */
    public List<Point> getPoints() {
        return points;
    }

    public Point3d getLowerCorner() {

        Point3d result = new Point3d(xLeftLowerCorner, yLeftLowerCorner, zMin);
        transformationMatrix.transform(result);

        return result;
    }

    public Point3d getUpperCorner() {

        Point3d result = new Point3d(
                xLeftLowerCorner + colNumber * cellSize,
                yLeftLowerCorner + rowNumber * cellSize,
                zMax);
        transformationMatrix.transform(result);

        return result;
    }

    /**
     *
     * @return
     */
    public List<Face> getFaces() {
        return faces;
    }

    /**
     * Instantiate a grid raster type
     *
     * @param path Absolute path of the file
     * @param zArray Two-dimensional array representing the 2d grid and
     * containing z values
     * @param xLeftLowerCorner X left lower corner
     * @param yLeftLowerCorner Y left lower corner
     * @param cellSize Size of a cell, in meters (m)
     * @param nbCols Column number
     * @param nbRows Row number
     */
    public Raster(String path, float[][] zArray, float xLeftLowerCorner, float yLeftLowerCorner, float cellSize, int nbCols, int nbRows) {

        this.transformationMatrix = new Matrix4d();
        this.transformationMatrix .setIdentity();
        this.inverseTransfMat = new Matrix4d();
        this.inverseTransfMat.setIdentity();

        this.path = path;
        this.zArray = zArray;
        this.xLeftLowerCorner = xLeftLowerCorner;
        this.yLeftLowerCorner = yLeftLowerCorner;
        this.cellSize = cellSize;
        this.rowNumber = nbRows;
        this.colNumber = nbCols;
    }

    /**
     * Get corresponding height from a x,y couple of points<br>
     * This method doesn't perform interpolation, it gets nearest cell and
     * return corresponding value.<br>
     *
     * @param posX position x
     * @param posY position y
     * @return height from x,y position
     */
    public float getSimpleHeight(float posX, float posY) {

        Vector4d multiply = new Vector4d(posX, posY, 1, 1);
        inverseTransfMat.transform(multiply);
        posX = (float) multiply.x;
        posY = (float) multiply.y;

        float z;

        int indiceX = (int) ((posX - xLeftLowerCorner) / cellSize);
        int indiceY = (int) (rowNumber - (posY - yLeftLowerCorner) / cellSize);

        if (indiceX < 0 || indiceY < 0 || indiceY >= rowNumber) {
            return Float.NaN;
        }

        if (indiceX < zArray.length && indiceY < zArray[0].length) {
            z = zArray[indiceX][indiceY];

        } else {
            return Float.NaN;
        }

        return z;
    }

    private BoundingBox2F getLargestBoundingBox(BoundingBox2F boundingBox2F) {

        //calculate the 4 corners
        Point2f min = boundingBox2F.min;
        Point2f max = boundingBox2F.max;

        Vector4d corner1 = new Vector4d(min.x, min.y, 0, 1);
        inverseTransfMat.transform(corner1);
        Vector4d corner2 = new Vector4d(max.x, min.y, 0, 1);
        inverseTransfMat.transform(corner2);
        Vector4d corner3 = new Vector4d(max.x, max.y, 0, 1);
        inverseTransfMat.transform(corner3);
        Vector4d corner4 = new Vector4d(min.x, max.y, 0, 1);
        inverseTransfMat.transform(corner4);

        float xMin = (float) corner1.x;
        float yMin = (float) corner1.y;
        float xMax = (float) corner3.x;
        float yMax = (float) corner3.y;

        xMin = Float.min(xMin, (float) Double.min(corner2.x, corner4.x));
        yMin = Float.min(yMin, (float) Double.min(corner2.y, corner4.y));

        xMax = Float.max(xMax, (float) Double.max(corner2.x, corner4.x));
        yMax = Float.max(yMax, (float) Double.max(corner2.y, corner4.y));

        return new BoundingBox2F(new Point2f(xMin, yMin), new Point2f(xMax, yMax));
    }

    /**
     *
     * @param boundingBox
     * @param offset
     */
    public void setLimits(BoundingBox2F boundingBox, int offset) {

        //calculate the 4 corners
        boundingBox.min.x -= (offset * cellSize);
        boundingBox.min.y -= (offset * cellSize);

        boundingBox.max.x += (offset * cellSize);
        boundingBox.max.y += (offset * cellSize);

        BoundingBox2F largestBoundingBox = getLargestBoundingBox(boundingBox);

        indiceXMin = (int) ((largestBoundingBox.min.x - xLeftLowerCorner) / cellSize);
        if (indiceXMin < 0) {
            indiceXMin = -1;
        }

        indiceYMin = (int) (rowNumber - (largestBoundingBox.max.y - yLeftLowerCorner) / cellSize);
        if (indiceYMin < 0) {
            indiceYMin = -1;
        }

        indiceXMax = (int) ((largestBoundingBox.max.x - xLeftLowerCorner) / cellSize);
        if (indiceXMax > zArray.length) {
            indiceXMax = -1;
        }

        indiceYMax = (int) (rowNumber - (largestBoundingBox.min.y - yLeftLowerCorner) / cellSize);
        if (indiceYMax > zArray[0].length) {
            indiceYMax = -1;
        }

    }

    /**
     *
     * @param boundingBox2F
     * @param offset
     * @return
     */
    public Raster subset(BoundingBox2F boundingBox2F, int offset) {

        Raster dtm = new Raster();

        //calculate the 4 corners
        boundingBox2F.min.x -= (offset * cellSize);
        boundingBox2F.min.y -= (offset * cellSize);

        boundingBox2F.max.x += (offset * cellSize);
        boundingBox2F.max.y += (offset * cellSize);

        BoundingBox2F largestBoundingBox = getLargestBoundingBox(boundingBox2F);

        int minXId, minYId;
        int maxXId, maxYId;

        minXId = (int) ((largestBoundingBox.min.x - xLeftLowerCorner) / cellSize);
        if (minXId < 0) {
            minXId = -1;
        }

        minYId = (int) (rowNumber - (largestBoundingBox.max.y - yLeftLowerCorner) / cellSize);
        if (minYId < 0) {
            minYId = -1;
        }

        maxXId = (int) ((largestBoundingBox.max.x - xLeftLowerCorner) / cellSize);
        if (maxXId > zArray.length) {
            maxXId = -1;
        }

        maxYId = (int) (rowNumber - (largestBoundingBox.min.y - yLeftLowerCorner) / cellSize);
        if (maxYId > zArray[0].length) {
            maxYId = -1;
        }

        dtm.xLeftLowerCorner = largestBoundingBox.min.x;
        dtm.yLeftLowerCorner = largestBoundingBox.min.y;
        dtm.cellSize = cellSize;
        dtm.rowNumber = maxYId - minYId;
        dtm.colNumber = maxXId - minXId;

        dtm.zArray = new float[dtm.colNumber][dtm.rowNumber];
        for (int i = minXId, i2 = 0; i < maxXId; i++, i2++) {
            for (int j = minYId, j2 = 0; j < maxYId; j++, j2++) {
                dtm.zArray[i2][j2] = zArray[i][j];
            }
        }

        return dtm;
    }

    /**
     * Build a 3d mesh from the raster, creating points, faces (as indices)
     */
    public void buildMesh() {

        if (zArray != null) {

            int width = zArray.length;
            if (width > 0) {

                int height = zArray[0].length;
                faces = new ArrayList<>();
                points = new ArrayList<>();

                if (indiceXMin == -1) {
                    indiceXMin = 0;
                }

                if (indiceYMin == -1) {
                    indiceYMin = 0;
                }

                if (indiceXMax == -1) {
                    indiceXMax = width;
                }

                if (indiceYMax == -1) {
                    indiceYMax = height;
                }

                for (int i = indiceXMin; i < indiceXMax; i++) {
                    for (int j = indiceYMin; j < indiceYMax; j++) {

                        float z;

                        //if(!Float.isNaN(zArray[i][j])){
                        z = zArray[i][j];
                        Point point = new Point((i * cellSize + xLeftLowerCorner), (-j + rowNumber) * cellSize + yLeftLowerCorner, z);
                        Vector4d result = new Vector4d(point.x, point.y, point.z, 1);
                        transformationMatrix.transform(result);

                        point.x = (float) result.x;
                        point.y = (float) result.y;

                        if (!Float.isNaN(z)) {
                            if (i == 0 && j == 0) {
                                zMin = (float) result.z;
                                zMax = (float) result.z;
                            } else {
                                if (result.z < zMin) {
                                    zMin = (float) result.z;
                                }
                                if (result.z > zMax) {
                                    zMax = (float) result.z;
                                }
                            }
                        }

                        point.z = (float) result.z;
                        points.add(point);
                        /*}else{
                            //z = -10.0f;
                        }*/

                    }
                }

                for (int i = indiceXMin; i < indiceXMax; i++) {
                    for (int j = indiceYMin; j < indiceYMax; j++) {

                        int point1, point2, point3, point4;

                        point1 = get1dFrom2d(i, j);

                        if (i + 1 < (indiceXMax)) {
                            point2 = get1dFrom2d(i + 1, j);

                            if (j + 1 < (indiceYMax)) {
                                point3 = get1dFrom2d(i + 1, j + 1);
                                point4 = get1dFrom2d(i, j + 1);

                                if (!Float.isNaN(zArray[i][j]) && !Float.isNaN(zArray[i + 1][j + 1])) {

                                    if (point1 < points.size() && point2 < points.size() && point3 < points.size()) {
                                        if (!Float.isNaN(zArray[i + 1][j])) {
                                            faces.add(new Face(point1, point2, point3));

                                            int faceID = faces.size() - 1;
                                            points.get(point1).faces.add(faceID);
                                            points.get(point2).faces.add(faceID);
                                            points.get(point3).faces.add(faceID);
                                        }

                                    }

                                    if (point1 < points.size() && point3 < points.size() && point4 < points.size()) {
                                        if (!Float.isNaN(zArray[i][j + 1])) {
                                            faces.add(new Face(point1, point3, point4));
                                            int faceID = faces.size() - 1;
                                            points.get(point1).faces.add(faceID);
                                            points.get(point3).faces.add(faceID);
                                            points.get(point4).faces.add(faceID);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            built = true;
        }

        zArray = null;
    }

    private int get1dFrom2d(int i, int j) {

        return ((indiceYMax - indiceYMin) * (i - indiceXMin)) + (j - indiceYMin);
    }

    /**
     *
     * @param outputFile
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void exportObj(File outputFile) throws FileNotFoundException, IOException {

        if (points == null || faces == null) {
            buildMesh();
        }

        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));

            writer.write("o terrain\n");

            for (Point point : points) {
                writer.write("v " + point.x + " " + point.y + " " + point.z + " " + "\n");
            }

            for (Face face : faces) {
                writer.write("f " + (face.getPoint1() + 1) + " " + (face.getPoint2() + 1) + " " + (face.getPoint3() + 1) + "\n");
            }

            writer.close();
        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
    }

    /**
     *
     * @param transformationMatrix
     */
    public void setTransformationMatrix(Matrix4d transformationMatrix) {
        this.transformationMatrix = transformationMatrix;
        inverseTransfMat = new Matrix4d(transformationMatrix);
        inverseTransfMat.invert();
    }

    /**
     *
     * @return
     */
    public Matrix4d getTransformationMatrix() {
        return transformationMatrix;
    }

    /**
     *
     * @return
     */
    public float getzMin() {
        return zMin;
    }

    /**
     *
     * @return
     */
    public float getzMax() {
        return zMax;
    }

    public float[][] getzArray() {
        return zArray;
    }

    public float getxLeftLowerCorner() {
        return xLeftLowerCorner;
    }

    public float getyLeftLowerCorner() {
        return yLeftLowerCorner;
    }

    public float getCellSize() {
        return cellSize;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public int getColNumber() {
        return colNumber;
    }

    public boolean isBuilt() {
        return built;
    }

    public void setBuilt(boolean built) {
        this.built = built;
    }
}
