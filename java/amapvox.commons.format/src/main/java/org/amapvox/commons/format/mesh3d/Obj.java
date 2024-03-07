/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.format.mesh3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;

/**
 *
 * @author calcul
 */
public class Obj {

    private final List<String> comments;

    private Point3f[] points;
    private Point3i[] faces;
    private Point3f[] normals;
    private Point2f[] texCoords;

    private boolean hasTexCoordIndices;
    private boolean hasNormalsIndices;

    private int[] materialOffsets;
    private Map<String, Mtl> materials;
    private Map<Integer, String> materialLinks;
    private final Map<String, Integer> groups;

    public Obj() {
        comments = new ArrayList<>();
        groups = new HashMap<>();
    }

    public Point3f[] getPoints() {
        return points;
    }

    public void setPoints(Point3f[] points) {
        this.points = points;
    }

    public Point3i[] getFaces() {
        return faces;
    }

    public int[] get1DFaces() {

        int[] facesArray = new int[faces.length * 3];

        for (int i = 0, j = 0; i < faces.length; i++, j += 3) {
            facesArray[j] = faces[i].x;
            facesArray[j + 1] = faces[i].y;
            facesArray[j + 2] = faces[i].z;
        }
        return facesArray;
    }

    public void setFaces(Point3i[] faces) {
        this.faces = faces;
    }

    public Point3f[] getNormals() {
        return normals;
    }

    public void setNormals(Point3f[] normals) {
        this.normals = normals;
    }

    public Point2f[] getTexCoords() {
        return texCoords;
    }

    public void setTexCoords(Point2f[] texCoords) {
        this.texCoords = texCoords;
    }

    public boolean isHasTexCoordIndices() {
        return hasTexCoordIndices;
    }

    public void setHasTexCoordIndices(boolean hasTexCoordIndices) {
        this.hasTexCoordIndices = hasTexCoordIndices;
    }

    public boolean isHasNormalsIndices() {
        return hasNormalsIndices;
    }

    public void setHasNormalsIndices(boolean hasNormalsIndices) {
        this.hasNormalsIndices = hasNormalsIndices;
    }

    public int[] getMaterialOffsets() {
        return materialOffsets;
    }

    public void setMaterialOffsets(int[] materialOffsets) {
        this.materialOffsets = materialOffsets;
    }

    public Map<String, Mtl> getMaterials() {
        return materials;
    }

    public void setMaterials(Map<String, Mtl> materials) {
        this.materials = materials;
    }

    public Map<Integer, String> getMaterialLinks() {
        return materialLinks;
    }

    public void setMaterialLinks(Map<Integer, String> materialLinks) {
        this.materialLinks = materialLinks;
    }

    public List<String> getComments() {
        return comments;
    }

    public Map<String, Integer> getGroups() {
        return groups;
    }

}
