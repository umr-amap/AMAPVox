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
package org.amapvox.viewer3d.mesh;

import com.jogamp.common.nio.Buffers;
import org.amapvox.commons.format.mesh3d.Mtl;
import org.amapvox.commons.format.mesh3d.Obj;
import org.amapvox.commons.format.mesh3d.ObjHelper;
import org.amapvox.viewer3d.loading.texture.Texture;
import org.amapvox.viewer3d.object.mesh.Grid;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3f;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class GLMeshFactory {

    public static TexturedGLMesh createTexturedCube(float size) {

        TexturedGLMesh texturedMesh = (TexturedGLMesh) createCube(size);

        float textCoordData[] = new float[]{0, 1,
            1, 1,
            0, 0,
            1, 0};

        texturedMesh.textureCoordinatesBuffer = Buffers.newDirectFloatBuffer(textCoordData);

        return texturedMesh;
    }

    public static PointCloudGLMesh createPointCloud(float[] vertexData, float[] colorData) {

        PointCloudGLMesh pointcloud = new PointCloudGLMesh();
        pointcloud.setVertexBuffer(Buffers.newDirectFloatBuffer(vertexData));
        pointcloud.vertexCount = vertexData.length / 3;
        pointcloud.colorBuffer = Buffers.newDirectFloatBuffer(colorData);

        return pointcloud;
    }

    public static GLMesh createLineSegment(Point3f startPoint, Point3f endPoint) {

        float[] vertexData = new float[]{
            startPoint.x, startPoint.y, startPoint.z,
            endPoint.x, endPoint.y, endPoint.z
        };

        int indexData[] = new int[]{0, 1};

        GLMesh segmentLine = new SimpleGLMesh();
        //cube.aoBuffer =  Buffers.newDirectFloatBuffer(aoData);
        segmentLine.setVertexBuffer(Buffers.newDirectFloatBuffer(vertexData));
        segmentLine.indexBuffer = Buffers.newDirectIntBuffer(indexData);
        segmentLine.vertexCount = indexData.length;

        return segmentLine;
    }

    public static GLMesh createCube(float size) {

        float vertexData[] = new float[]{size / 2.0f, size / 2.0f, -size / 2.0f,
            size / 2.0f, -size / 2.0f, -size / 2.0f,
            -size / 2.0f, -size / 2.0f, -size / 2.0f,
            -size / 2.0f, size / 2.0f, -size / 2.0f,
            size / 2.0f, size / 2.0f, size / 2.0f,
            size / 2.0f, -size / 2.0f, size / 2.0f,
            -size / 2.0f, -size / 2.0f, size / 2.0f,
            -size / 2.0f, size / 2.0f, size / 2.0f};

        float[] normalData = new float[]{0.577349f, 0.577349f, -0.577349f,
            0.577349f, -0.577349f, -0.577349f,
            -0.577349f, -0.577349f, -0.577349f,
            -0.577349f, 0.577349f, -0.577349f,
            0.577349f, 0.577349f, 0.577349f,
            0.577349f, -0.577349f, 0.577349f,
            -0.577349f, -0.577349f, 0.577349f,
            -0.577349f, 0.577349f, 0.577349f};

        int indexData[] = new int[]{0, 1, 2,
            4, 7, 6,
            0, 4, 5,
            1, 5, 6,
            2, 6, 7,
            4, 0, 3,
            3, 0, 2,
            5, 4, 6,
            1, 0, 5,
            2, 1, 6,
            3, 2, 7,
            7, 4, 3};

        GLMesh cube = new SimpleGLMesh();
        //cube.aoBuffer =  Buffers.newDirectFloatBuffer(aoData);
        cube.setVertexBuffer(Buffers.newDirectFloatBuffer(vertexData));
        cube.indexBuffer = Buffers.newDirectIntBuffer(indexData);
        cube.vertexCount = indexData.length;
        cube.normalBuffer = Buffers.newDirectFloatBuffer(normalData);

        return cube;
    }
    
    public static GLMesh createCuboid(Point3f size) {
        
        Point3f halfSize = new Point3f(size);
        halfSize.scale(0.5f);

        float vertexData[] = new float[]{
            halfSize.x, halfSize.y, -halfSize.z,
            halfSize.x, -halfSize.y, -halfSize.z,
            -halfSize.x, -halfSize.y, -halfSize.z,
            -halfSize.x, halfSize.y, -halfSize.z,
            halfSize.x, halfSize.y, halfSize.z,
            halfSize.x, -halfSize.y, halfSize.z,
            -halfSize.x, -halfSize.y, halfSize.z,
            -halfSize.x, halfSize.y, halfSize.z};

        float[] normalData = new float[]{0.577349f, 0.577349f, -0.577349f,
            0.577349f, -0.577349f, -0.577349f,
            -0.577349f, -0.577349f, -0.577349f,
            -0.577349f, 0.577349f, -0.577349f,
            0.577349f, 0.577349f, 0.577349f,
            0.577349f, -0.577349f, 0.577349f,
            -0.577349f, -0.577349f, 0.577349f,
            -0.577349f, 0.577349f, 0.577349f};

        int indexData[] = new int[]{0, 1, 2,
            4, 7, 6,
            0, 4, 5,
            1, 5, 6,
            2, 6, 7,
            4, 0, 3,
            3, 0, 2,
            5, 4, 6,
            1, 0, 5,
            2, 1, 6,
            3, 2, 7,
            7, 4, 3};

        GLMesh cuboid = new SimpleGLMesh();
        //cube.aoBuffer =  Buffers.newDirectFloatBuffer(aoData);
        cuboid.setVertexBuffer(Buffers.newDirectFloatBuffer(vertexData));
        cuboid.indexBuffer = Buffers.newDirectIntBuffer(indexData);
        cuboid.vertexCount = indexData.length;
        cuboid.normalBuffer = Buffers.newDirectFloatBuffer(normalData);

        return cuboid;
    }

    public static GLMesh createPlane(Vector3f startPoint, float width, float height) {

        float vertexData[] = new float[]{startPoint.x, startPoint.y, startPoint.z,
            startPoint.x + width, startPoint.y, startPoint.z,
            startPoint.x, startPoint.y + height, startPoint.z,
            startPoint.x + width, startPoint.y + height, startPoint.z};

        float textCoordData[] = new float[]{0, 1,
            1, 1,
            0, 0,
            1, 0};

        int indexData[] = new int[]{0, 1, 3,
            2, 0, 3};

        TexturedGLMesh plane = new TexturedGLMesh();
        /*
        MeshBuffer meshBuffer= new MeshBuffer();
        meshBuffer.setBuffer(MeshBuffer.VERTEX_BUFFER, Buffers.newDirectFloatBuffer(vertexData));
        meshBuffer.setBuffer(MeshBuffer.TEXTURE_COORDINATES_BUFFER, Buffers.newDirectFloatBuffer(textCoordData));
        meshBuffer.setBuffer(MeshBuffer.INDEX_BUFFER, Buffers.newDirectShortBuffer(indexData));
        plane.meshBuffer = meshBuffer;
         */
        plane.setVertexBuffer(Buffers.newDirectFloatBuffer(vertexData));
        plane.textureCoordinatesBuffer = Buffers.newDirectFloatBuffer(textCoordData);
        plane.indexBuffer = Buffers.newDirectIntBuffer(indexData);
        plane.vertexCount = indexData.length;

        return plane;
    }

    public static Grid createGrid(float resolution, int size, float ground) {

        int pointsNumber = (int) (size / resolution);
        float pointsArray[] = new float[12 * pointsNumber];
        int indexArray[] = new int[12 * pointsNumber];
        int pointsArrayIndex = 0, indexArrayIndex = 0;

        /**
         * grid for x axis*
         */
        for (int i = 0; i < pointsNumber; i++) {

            /**
             * first point*
             */
            //x
            pointsArray[pointsArrayIndex] = (i + resolution) - (size / 2);
            pointsArrayIndex++;
            //z
            pointsArray[pointsArrayIndex] = ground;
            pointsArrayIndex++;
            //y
            pointsArray[pointsArrayIndex] = (float) -size / 2;
            pointsArrayIndex++;

            indexArray[indexArrayIndex] = (pointsArrayIndex - 1);
            indexArrayIndex++;

            /**
             * second point*
             */
            //x
            pointsArray[pointsArrayIndex] = (i + resolution) - (size / 2);
            pointsArrayIndex++;
            //z
            pointsArray[pointsArrayIndex] = ground;
            pointsArrayIndex++;
            //y
            pointsArray[pointsArrayIndex] = (float) size / 2;
            pointsArrayIndex++;

            indexArray[indexArrayIndex] = (pointsArrayIndex - 1);
            indexArrayIndex++;
        }

        /**
         * grid for y axis*
         */
        for (int i = 0; i < pointsNumber; i++) {

            /**
             * first point*
             */
            //x
            pointsArray[pointsArrayIndex] = (float) -size / 2;
            pointsArrayIndex++;
            //z
            pointsArray[pointsArrayIndex] = ground;
            pointsArrayIndex++;
            //y
            pointsArray[pointsArrayIndex] = (i + resolution) - (size / 2);
            pointsArrayIndex++;

            indexArray[indexArrayIndex] = (pointsArrayIndex - 1);
            indexArrayIndex++;

            /**
             * second point*
             */
            //x
            pointsArray[pointsArrayIndex] = (float) size / 2;
            pointsArrayIndex++;
            //z
            pointsArray[pointsArrayIndex] = ground;
            pointsArrayIndex++;
            //y
            pointsArray[pointsArrayIndex] = (i + resolution) - (size / 2);
            pointsArrayIndex++;

            indexArray[indexArrayIndex] = (pointsArrayIndex - 1);
            indexArrayIndex++;
        }

        Grid grid = new Grid();
        grid.setVertexBuffer(Buffers.newDirectFloatBuffer(pointsArray));
        grid.indexBuffer = Buffers.newDirectIntBuffer(indexArray);
        grid.vertexCount = indexArrayIndex;

        return grid;
    }

    public static GLMesh createLandmark(float min, float max) {

        GLMesh mesh = new SimpleGLMesh();

        float vertexData[] = new float[]{
            0.0f, 0.0f, min,
            0.0f, 0.0f, max,
            0.0f, min, 0.0f,
            0.0f, max, 0.0f,
            min, 0.0f, 0.0f,
            max, 0.0f, 0.0f
        };

        float colorData[] = new float[]{
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f
        };

        int indexData[] = new int[]{
            0, 1,
            2, 3,
            4, 5
        };

        mesh.setVertexBuffer(Buffers.newDirectFloatBuffer(vertexData));
        mesh.colorBuffer = Buffers.newDirectFloatBuffer(colorData);
        mesh.indexBuffer = Buffers.newDirectIntBuffer(indexData);

        mesh.vertexCount = indexData.length;

        return mesh;
    }

    public static GLMesh createPlaneFromTexture(Vector3f startPoint, Texture texture) {

        return GLMeshFactory.createPlane(startPoint, texture.getWidth(), texture.getHeight());
    }

    public static GLMesh createPlaneFromTexture(Vector3f startPoint, Texture texture, float width, float height) {

        return GLMeshFactory.createPlane(startPoint, width, height);
    }

    /*public static GLMesh createMeshFromX3D(InputStreamReader x3dFile) throws JDOMException, IOException{
        
        SAXBuilder sxb = new SAXBuilder();
        sxb.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        
        try {
            Document document = sxb.build(x3dFile);
            Element root = document.getRootElement();
            List<Element> shapes = root.getChild("Scene")
                                        .getChild("Transform")
                                        .getChild("Group")
                                        .getChildren("Shape");
            
            List<Integer> faces = new ArrayList<>();
            List<Point3f> vertices = new ArrayList<>();
            List<Point3f> normales = new ArrayList<>();
            Map<Integer, Vector3f> colors = new HashMap<>();
            
            int facesOffset = 0;
            int verticesOffset = 0;
            
            for(Element shape : shapes){
                
                Element indexedFaceSetElement = shape.getChild("IndexedFaceSet");
                
                if(indexedFaceSetElement == null){
                    indexedFaceSetElement = shape.getChild("IndexedTriangleSet");
                }
                Element appearanceElement = shape.getChild("Appearance");
                String diffuseColorString = appearanceElement.getChild("Material").getAttributeValue("diffuseColor");
                String[] split = diffuseColorString.split(" ");
                Vector3f currentColor = new Vector3f(Float.valueOf(split[0]), Float.valueOf(split[1]), Float.valueOf(split[2]));
                
                String coordinatesIndices = indexedFaceSetElement.getAttributeValue("coordIndex");
                
                if(coordinatesIndices == null){
                    coordinatesIndices = indexedFaceSetElement.getAttributeValue("index");
                }
                String[] coordinatesIndicesArray = coordinatesIndices.split(" ");
                
                int tmp = 0;
                
                for(String s : coordinatesIndicesArray){
                    int indice = Integer.valueOf(s);
                    
                    if(indice != -1){
                        faces.add(indice + facesOffset);
                        colors.put(indice + facesOffset, currentColor);
                        tmp++;
                    }
                }
                
                facesOffset += tmp; 
                
                tmp = 0;
                
                Element coordinateElement = indexedFaceSetElement.getChild("Coordinate");
                if(coordinateElement != null){
                    String point = coordinateElement.getAttributeValue("point");
                    
                    if(point != null){
                        String[] pointArray = point.split(" ");
                    
                    int count = -1;
                    float x = 0, y = 0, z;
                    
                        for (String p : pointArray) {

                            float value = Float.valueOf(p);
                            count++;

                            switch (count) {
                                case 0:
                                    x = value;
                                    break;
                                case 1:
                                    y = value;
                                    break;
                                case 2:
                                    z = value;
                                    vertices.add(new Point3f(x, y, z));
                                    count = -1;
                                    tmp++;
                                    break;
                            }

                        }
                    }
                    
                }
                
                verticesOffset += tmp;
                
                Element normalElement = indexedFaceSetElement.getChild("Normal");
                if(normalElement != null){
                    String normalesVector = normalElement.getAttributeValue("vector");
                    
                    if(normalesVector != null){
                        String[] normalesArray = normalesVector.split(" ");

                        int count = -1;
                        float x = 0, y = 0, z;

                        for (String p : normalesArray) {

                            float value = Float.valueOf(p);
                            count++;

                            switch (count) {
                                case 0:
                                    x = value;
                                    break;
                                case 1:
                                    y = value;
                                    break;
                                case 2:
                                    z = value;
                                    normales.add(new Point3f(x, y, z));
                                    count = -1;
                                    break;
                            }

                        }
                    }
                }
                
            }
            
            int[] facesArray = new int[faces.size()];
            for(int i=0;i<faces.size();i++){
                facesArray[i] = faces.get(i);
            }
            
            GLMesh mesh = GLMeshFactory.createMesh(vertices, normales, facesArray);
            
            float colorData[] = new float[vertices.size()*3];
            for(int i=0, j=0;i<vertices.size();i++,j+=3){

                colorData[j] = colors.get(i).x;
                colorData[j+1] = colors.get(i).y;
                colorData[j+2] = colors.get(i).z;

            }

            mesh.colorBuffer = Buffers.newDirectFloatBuffer(colorData);
            
            return mesh;
            
            
        } catch (JDOMException | IOException ex) {
            throw ex;
        }
    }*/
    public static GLMesh createBoundingBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {

        GLMesh mesh = new SimpleGLMesh();

        float vertexData[] = new float[]{maxX, maxY, minZ,
            maxX, minY, minZ,
            minX, minY, minZ,
            minX, maxY, minZ,
            maxX, maxY, maxZ,
            maxX, minY, maxZ,
            minX, minY, maxZ,
            minX, maxY, maxZ};

        int indexData[] = new int[]{0, 1,
            5, 4,
            4, 0,
            1, 5,
            5, 6,
            4, 7,
            1, 2,
            2, 6,
            7, 6,
            2, 3,
            3, 7,
            0, 3};

        mesh.setVertexBuffer(Buffers.newDirectFloatBuffer(vertexData));
        mesh.indexBuffer = Buffers.newDirectIntBuffer(indexData);
        mesh.vertexCount = indexData.length;

        return mesh;
    }

    public static GLMesh createMeshFromObj(InputStream objFile, InputStream mtlFile) throws FileNotFoundException, IOException, Exception {

        Obj obj = ObjHelper.readObj(new InputStreamReader(objFile), new InputStreamReader(mtlFile));

        return createMeshFromObj(obj);
    }

    public static GLMesh createMeshFromObj(Obj obj) throws FileNotFoundException, IOException {

        GLMesh mesh = GLMeshFactory.createMesh(obj.getPoints(), obj.getNormals(), obj.get1DFaces());

        int nbPoints = obj.getPoints().length;
        float colorData[] = new float[nbPoints * 3];

        for (int i = 0, j = 0; i < nbPoints; i++, j += 3) {

            colorData[j] = 0;
            colorData[j + 1] = 0;
            colorData[j + 2] = 0;
        }

        Point3i[] faces = obj.getFaces();

        Map<String, Mtl> materials = obj.getMaterials();

        if (materials != null) {
            int[] materialOffsets = obj.getMaterialOffsets();
            Map<Integer, String> materialLinks = obj.getMaterialLinks();

            int faceID = 0;
            int currentMaterial = 1;

            for (Point3i face : faces) {

                String materialName = materialLinks.get(currentMaterial - 1);
                Mtl mtl = materials.get(materialName);

                colorData[face.x * 3] = mtl.getDiffuseColor().x;
                colorData[face.x * 3 + 1] = mtl.getDiffuseColor().y;
                colorData[face.x * 3 + 2] = mtl.getDiffuseColor().z;

                colorData[face.y * 3] = mtl.getDiffuseColor().x;
                colorData[face.y * 3 + 1] = mtl.getDiffuseColor().y;
                colorData[face.y * 3 + 2] = mtl.getDiffuseColor().z;

                colorData[face.z * 3] = mtl.getDiffuseColor().x;
                colorData[face.z * 3 + 1] = mtl.getDiffuseColor().y;
                colorData[face.z * 3 + 2] = mtl.getDiffuseColor().z;

                faceID++;

                if (currentMaterial < materialOffsets.length && faceID >= materialOffsets[currentMaterial]) {
                    currentMaterial++;
                }
            }
        }

        mesh.colorBuffer = Buffers.newDirectFloatBuffer(colorData);

        return mesh;
    }

    public static GLMesh createMeshFromObj(File objFile, File mtlFile) throws FileNotFoundException, IOException, Exception {

        Obj obj = ObjHelper.readObj(objFile, mtlFile);

        return createMeshFromObj(obj);
    }

    /*public static GLMesh createMeshFromObj(File objFile) throws FileNotFoundException, IOException{
        
        return createMeshFromObj(new InputStreamReader(new FileInputStream(objFile)), null);
    }*/
 /*public static GLMesh createMeshFromObj(InputStreamReader objFile, InputStreamReader objMaterial) throws FileNotFoundException, IOException{
        
        GLMesh mesh;
        
        ArrayList<Point3f> vertices = new ArrayList<>();
        ArrayList<Point3f> normales = new ArrayList<>();
        Map<Integer, Vector3f> colors = new HashMap<>();
        ArrayList<Integer> faces = new ArrayList<>();
        Map<String, Vector3f> materials = new HashMap<>();
        
        
        Point3f[] normalesArray = new Point3f[0];
        
        boolean hasAMaterial = false;
        Vector3f defaultColor = new Vector3f(0, 0, 0);
        
        if (objMaterial == null) {
            LOGGER.info("Obj material not set");
        } else {
            try (BufferedReader reader = new BufferedReader(objMaterial)) {
                String line;
                String currentMaterial = "";

                while ((line = reader.readLine()) != null) {

                    if (line.startsWith("newmtl ")) {

                        String[] material = line.split(" ");
                        currentMaterial = material[1];

                    } else if (line.startsWith("Kd ")) {
                        String[] diffuse = line.split(" ");
                        materials.put(currentMaterial, new Vector3f(Float.valueOf(diffuse[1]), Float.valueOf(diffuse[2]), Float.valueOf(diffuse[3])));
                    }
                }

                hasAMaterial = true;

            } catch (FileNotFoundException ex) {
                throw ex;
            } catch (IOException ex) {
                throw ex;
            }
        }
        
        int count = 0;
        boolean firstFace = true;
        
        try{
                        
            BufferedReader reader = new BufferedReader(objFile);
            
            String line;
            
            Vector3f currentColor = new Vector3f();
            
            while((line = reader.readLine()) != null){
                
                if(line.startsWith("v ")){
                    
                    String[] vertex = line.split(" ");
                    vertices.add(new Point3f(Float.valueOf(vertex[1]), Float.valueOf(vertex[2]), Float.valueOf(vertex[3])));
                    
                }else if(line.startsWith("vn ")){
                    
                    String[] normale = line.split(" ");
                    normales.add(new Point3f(Float.valueOf(normale[1]), Float.valueOf(normale[2]), Float.valueOf(normale[3])));
                    
                }else if(line.startsWith("f ")){
                    
                    if(firstFace){
                        normalesArray = new Point3f[vertices.size()];
                        firstFace = false;
                    }
                    
                    String[] faceSplit = line.replaceAll("//", " ").split(" ");
                    
                    Vec3I face = new Vec3I(Integer.valueOf(faceSplit[1]), Integer.valueOf(faceSplit[3]), Integer.valueOf(faceSplit[5]));
                    
                    normalesArray[face.x-1] = normales.get(Integer.valueOf(faceSplit[2])-1);
                    normalesArray[face.y-1] = normales.get(Integer.valueOf(faceSplit[4])-1);
                    normalesArray[face.z-1] = normales.get(Integer.valueOf(faceSplit[6])-1);
                    count ++;
                    
                    colors.put(face.x-1, currentColor);
                    colors.put(face.y-1, currentColor);
                    colors.put(face.z-1, currentColor);
                    
                    faces.add(face.x-1);
                    faces.add(face.y-1);
                    faces.add(face.z-1);
                    
                }else if(line.startsWith("usemtl ")){
                    if(hasAMaterial){
                        currentColor = materials.get(line.split(" ")[1]);
                    }else{
                        currentColor = defaultColor;
                    }
                }
            }
            
            reader.close();
            
        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
        
        normales = new ArrayList<>();
        
        for (int i=0;i<normalesArray.length;i++){
            if(normalesArray[i] == null){
                normales.add(new Point3f(0, 0, 0));
            }else{
                normales.add(normalesArray[i]);
            }
        }
        
        mesh = GLMeshFactory.createMesh(vertices, normales, faces);
        
        float colorData[] = new float[vertices.size()*3];
        for(int i=0, j=0;i<vertices.size();i++,j+=3){
            
            if(colors.get(i) == null){
                colorData[j] = defaultColor.x;
                colorData[j+1] = defaultColor.y;
                colorData[j+2] = defaultColor.z;
            }else{
                colorData[j] = colors.get(i).x;
                colorData[j+1] = colors.get(i).y;
                colorData[j+2] = colors.get(i).z;
            }
            
            
        }
        
        mesh.colorBuffer = Buffers.newDirectFloatBuffer(colorData);
        
        return mesh;
    }*/
    public static GLMesh createMesh(ArrayList<Vector3f> points, ArrayList<Integer> faces) {

        GLMesh mesh = new SimpleGLMesh();

        float[] vertexData = new float[points.size() * 3];
        for (int i = 0, j = 0; i < points.size(); i++, j += 3) {

            vertexData[j] = points.get(i).x;
            vertexData[j + 1] = points.get(i).y;
            vertexData[j + 2] = points.get(i).z;
        }
        /*
        for(int i=0 ; i<points.size()-3 ; i += 3){
            
            vertexData[i] = points.get(i).x;
            vertexData[i+1] = points.get(i).y;
            vertexData[i+2] = points.get(i).z;
        }
         */
        int indexData[] = new int[faces.size()];

        for (int i = 0; i < faces.size(); i++) {

            indexData[i] = faces.get(i);
        }

        mesh.setVertexBuffer(Buffers.newDirectFloatBuffer(vertexData));
        mesh.indexBuffer = Buffers.newDirectIntBuffer(indexData);

        mesh.vertexCount = indexData.length;

        return mesh;
    }

    public static GLMesh createMesh(Point3f[] points, Point3f[] normales, int[] faces) {

        GLMesh mesh = new SimpleGLMesh();

        float[] vertexData = new float[points.length * 3];
        for (int i = 0, j = 0; i < points.length; i++, j += 3) {

            vertexData[j] = points[i].x;
            vertexData[j + 1] = points[i].y;
            vertexData[j + 2] = points[i].z;
        }

        mesh.setVertexBuffer(Buffers.newDirectFloatBuffer(vertexData));

        if (normales != null) {
            float[] normalData = new float[normales.length * 3];

            for (int i = 0, j = 0; i < normales.length; i++, j += 3) {

                normalData[j] = normales[i].x;
                normalData[j + 1] = normales[i].y;
                normalData[j + 2] = normales[i].z;
            }

            mesh.normalBuffer = Buffers.newDirectFloatBuffer(normalData);
        }

        mesh.indexBuffer = Buffers.newDirectIntBuffer(faces);
        mesh.vertexCount = faces.length;

        return mesh;
    }
}
