/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.format.mesh3d;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3f;

/**
 *
 * @author calcul
 */
public class ObjHelper {
    
    private static class FaceItem{
        
        public boolean hasTexCoordIndices = false;
        public boolean hasNormalsIndices = false;
        
        public int vertexIndex;
        public int texCoordIndex;
        public int normalIndex;
    }
    
    public static void main(String[] args) throws IOException, Exception {
        
        Obj obj = ObjHelper.readObj(new File("/home/julien/Bureau/tmp3/cube6.obj"));
        System.out.println("test");
    }
    
    private static class Face{
        
        public boolean hasTexCoordIndices = false;
        public boolean hasNormalsIndices = false;
        
        public Point3i verticesIndices;
        public Point3i texCoordIndices;
        public Point3i normalsIndices;
        
        public static Face parseFromLine(String line){
            
            Face face = new Face();
            
            String[] faceItems = line.split(" ");
            if(faceItems.length == 4){
                
                FaceItem faceItem1 = parseFaceItem(faceItems[1]);
                int index1 = faceItem1.vertexIndex;
                int normalIndex1 = faceItem1.normalIndex;
                int texCoordsIndex1 = faceItem1.texCoordIndex;
                
                face.hasNormalsIndices = faceItem1.hasNormalsIndices;
                face.hasTexCoordIndices = faceItem1.hasTexCoordIndices;
                
                FaceItem faceItem2 = parseFaceItem(faceItems[2]);
                int index2 = faceItem2.vertexIndex;
                int normalIndex2 = faceItem2.normalIndex;
                int texCoordsIndex2 = faceItem2.texCoordIndex;
                
                FaceItem faceItem3 = parseFaceItem(faceItems[3]);
                int index3 = faceItem3.vertexIndex;
                int normalIndex3 = faceItem3.normalIndex;
                int texCoordsIndex3 = faceItem3.texCoordIndex;
                
                face.verticesIndices = new Point3i(index1, index2, index3);
                
                if(face.hasNormalsIndices){
                    face.normalsIndices = new Point3i(normalIndex1, normalIndex2, normalIndex3);
                }
                if(face.hasTexCoordIndices){
                    face.normalsIndices = new Point3i(texCoordsIndex1, texCoordsIndex2, texCoordsIndex3);
                }
                
                return face;
                
            }else{
                return null;
            }
        }
        
        private static FaceItem parseFaceItem(String item){
            
            FaceItem faceItem = new FaceItem();
            
            String[] split = item.split("/");
                      
            faceItem.vertexIndex = Integer.valueOf(split[0]) - 1;

            if(split.length > 2){ //may contains normal indices and/or texture coordinates

                faceItem.hasNormalsIndices = true;
                faceItem.normalIndex = Integer.valueOf(split[2]) - 1;

                if(split[1].equals("")){ //contains normale indices
                }else{ //contains normale indices and texture coordinates
                    faceItem.texCoordIndex = Integer.valueOf(split[1]) - 1;
                    faceItem.hasTexCoordIndices = true;
                }
            }
            
            return faceItem;
        }
    }
    
    public static Obj readObj(Reader objReader) throws FileNotFoundException, IOException, Exception{
        
        try (BufferedReader reader = new BufferedReader(objReader)) {
            
            List<Point3f> vertices = new ArrayList<>();
            List<Point3f> normals = new ArrayList<>();
            List<Point2f> texCoordinates = new ArrayList<>();
            
            List<Point3i> faces = new ArrayList<>();
            Point3f[] normalsArray = new Point3f[0];
            Point2f[] texCoordArray = new Point2f[0];
            
            List<Integer> materialOffsets = new ArrayList<>();
            
            Obj obj = new Obj();

            String line;

            int faceIndex = 0;
            
            boolean hasFaceLine = false, hasNormalLine = false, hasVertexLine = false, hasVertexTexCoordLine = false, hasUseMtlLine = false;
            
            while ((line = reader.readLine()) != null) {

                if (line.startsWith("v ")) {

                    if(!hasVertexLine){
                        hasVertexLine = true;
                    }
                    
                    String[] vertex = line.split(" ");
                    vertices.add(new Point3f(Float.valueOf(vertex[1]), Float.valueOf(vertex[2]), Float.valueOf(vertex[3])));

                } else if (line.startsWith("vn ")) {

                    if(!hasNormalLine){
                        hasNormalLine = true;
                    }
                    
                    String[] normale = line.split(" ");
                    normals.add(new Point3f(Float.valueOf(normale[1]), Float.valueOf(normale[2]), Float.valueOf(normale[3])));

                }else if (line.startsWith("vt ")) {

                    if(!hasVertexTexCoordLine){
                        hasVertexTexCoordLine = true;
                    }
                    
                    String[] coordinates = line.split(" ");
                    texCoordinates.add(new Point2f(Float.valueOf(coordinates[1]), Float.valueOf(coordinates[2])));

                }else if (line.startsWith("f ")) {

                    if (!hasFaceLine) {
                        
                        if(hasNormalLine){
                            normalsArray = new Point3f[vertices.size()];
                        }
                        if(hasVertexTexCoordLine){
                            texCoordArray = new Point2f[vertices.size()];
                        }
                        hasFaceLine = true;
                    }
                    
                    Face face = Face.parseFromLine(line);
                    
                    if(face != null){
                        
                        faces.add(new Point3i(face.verticesIndices.x, face.verticesIndices.y, face.verticesIndices.z));
                        
                        if(face.hasNormalsIndices && hasNormalLine){
                            normalsArray[face.verticesIndices.x] = normals.get(face.normalsIndices.x);
                            normalsArray[face.verticesIndices.y] = normals.get(face.normalsIndices.y);
                            normalsArray[face.verticesIndices.z] = normals.get(face.normalsIndices.z);
                        }
                        
                        if(face.hasTexCoordIndices && hasVertexTexCoordLine){
                            texCoordArray[face.verticesIndices.x] = texCoordinates.get(face.texCoordIndices.x);
                            texCoordArray[face.verticesIndices.y] = texCoordinates.get(face.texCoordIndices.y);
                            texCoordArray[face.verticesIndices.z] = texCoordinates.get(face.texCoordIndices.z);
                        }
                        
                    }else{
                        throw new Exception("Faces are not triangles !");
                    }
                    
                    faceIndex++;

                } else if (line.startsWith("usemtl ")) {
                    
                    if (!hasUseMtlLine) {
                        hasUseMtlLine = true;
                    }
                    
                    materialOffsets.add(faceIndex);
                }
            }
            
            //convert vertices list to array
            Point3f[] verticesArray = new Point3f[vertices.size()];
            vertices.toArray(verticesArray);
            obj.setPoints(verticesArray);
            
            //convert faces list to faces array
            Point3i[] facesArray = new Point3i[faces.size()];
            faces.toArray(facesArray);
            obj.setFaces(facesArray);
            
            //convert material offsets list to array
            int[] materialOffsetsArray = new int[materialOffsets.size()];
            for(int i=0;i<materialOffsets.size();i++){
                materialOffsetsArray[i] = materialOffsets.get(i);
            }
            obj.setMaterialOffsets(materialOffsetsArray);
            
            
            for(int i=0;i<normalsArray.length;i++){
                if(normalsArray[i] == null){
                    normalsArray[i] = new Point3f();
                }
            }
            obj.setNormals(normalsArray);
            
            
            obj.setTexCoords(texCoordArray);
            
            obj.setHasNormalsIndices(hasNormalLine);
            obj.setHasTexCoordIndices(hasVertexTexCoordLine);
            
            return obj;

        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
    }
    
    public static Obj readObj(InputStreamReader objStream) throws FileNotFoundException, IOException{
        
        try (BufferedReader reader = new BufferedReader(objStream)) {
            
            List<Point3f> vertices = new ArrayList<>();
            List<Point3f> normals = new ArrayList<>();
            List<Point2f> texCoordinates = new ArrayList<>();
            
            List<Point3i> faces = new ArrayList<>();
            Point3f[] normalsArray = new Point3f[0];
            Point2f[] texCoordArray = new Point2f[0];
            
            List<Integer> materialOffsets = new ArrayList<>();
            
            Obj obj = new Obj();

            String line;

            int faceIndex = 0;
            
            boolean hasFaceLine = false, hasNormalLine = false, hasVertexLine = false, hasVertexTexCoordLine = false, hasUseMtlLine = false;
            
            while ((line = reader.readLine()) != null) {

                if (line.startsWith("v ")) {

                    if(!hasVertexLine){
                        hasVertexLine = true;
                    }
                    
                    String[] vertex = line.split(" ");
                    vertices.add(new Point3f(Float.valueOf(vertex[1]), Float.valueOf(vertex[2]), Float.valueOf(vertex[3])));

                } else if (line.startsWith("vn ")) {

                    if(!hasNormalLine){
                        hasNormalLine = true;
                    }
                    
                    String[] normale = line.split(" ");
                    normals.add(new Point3f(Float.valueOf(normale[1]), Float.valueOf(normale[2]), Float.valueOf(normale[3])));

                }else if (line.startsWith("vt ")) {

                    if(!hasVertexTexCoordLine){
                        hasVertexTexCoordLine = true;
                    }
                    
                    String[] coordinates = line.split(" ");
                    texCoordinates.add(new Point2f(Float.valueOf(coordinates[1]), Float.valueOf(coordinates[2])));

                }else if (line.startsWith("f ")) {

                    if (!hasFaceLine) {
                        
                        if(hasNormalLine){
                            normalsArray = new Point3f[vertices.size()];
                        }
                        if(hasVertexTexCoordLine){
                            texCoordArray = new Point2f[vertices.size()];
                        }
                        hasFaceLine = true;
                    }
                    
                    Face face = Face.parseFromLine(line);
                    
                    if(face != null){
                        
                        faces.add(new Point3i(face.verticesIndices.x, face.verticesIndices.y, face.verticesIndices.z));
                        
                        if(face.hasNormalsIndices && hasNormalLine){
                            normalsArray[face.verticesIndices.x] = normals.get(face.normalsIndices.x);
                            normalsArray[face.verticesIndices.y] = normals.get(face.normalsIndices.y);
                            normalsArray[face.verticesIndices.z] = normals.get(face.normalsIndices.z);
                        }
                        
                        if(face.hasTexCoordIndices && hasVertexTexCoordLine){
                            texCoordArray[face.verticesIndices.x] = texCoordinates.get(face.texCoordIndices.x);
                            texCoordArray[face.verticesIndices.y] = texCoordinates.get(face.texCoordIndices.y);
                            texCoordArray[face.verticesIndices.z] = texCoordinates.get(face.texCoordIndices.z);
                        }
                        
                    }else{
                        //ignore face
                    }
                    
                    faceIndex++;

                } else if (line.startsWith("usemtl ")) {
                    
                    if (!hasUseMtlLine) {
                        hasUseMtlLine = true;
                    }
                    
                    materialOffsets.add(faceIndex);
                }
            }
            
            //convert vertices list to array
            Point3f[] verticesArray = new Point3f[vertices.size()];
            vertices.toArray(verticesArray);
            obj.setPoints(verticesArray);
            
            //convert faces list to faces array
            Point3i[] facesArray = new Point3i[faces.size()];
            faces.toArray(facesArray);
            obj.setFaces(facesArray);
            
            //convert material offsets list to array
            int[] materialOffsetsArray = new int[materialOffsets.size()];
            for(int i=0;i<materialOffsets.size();i++){
                materialOffsetsArray[i] = materialOffsets.get(i);
            }
            obj.setMaterialOffsets(materialOffsetsArray);
            
            
            for(int i=0;i<normalsArray.length;i++){
                if(normalsArray[i] == null){
                    normalsArray[i] = new Point3f();
                }
            }
            obj.setNormals(normalsArray);
            
            
            obj.setTexCoords(texCoordArray);
            
            obj.setHasNormalsIndices(hasNormalLine);
            obj.setHasTexCoordIndices(hasVertexTexCoordLine);
            
            return obj;

        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
    }
    
    public static Obj readObj(Reader objReader, Reader mtlReader) throws FileNotFoundException, IOException, Exception{
        
        Obj obj = new Obj();
        
        try (BufferedReader reader = new BufferedReader(objReader)) {
            
            List<Point3f> vertices = new ArrayList<>();
            List<Point3f> normals = new ArrayList<>();
            List<Point2f> texCoordinates = new ArrayList<>();
            
            List<Point3i> faces = new ArrayList<>();
            Point3f[] normalsArray = new Point3f[0];
            Point2f[] texCoordArray = new Point2f[0];
            
            List<Integer> materialOffsets = new ArrayList<>();
            Map<Integer, String> materialLinks = new HashMap<>();

            String line;

            int faceIndex = 0;
            
            boolean hasFaceLine = false, hasNormalLine = false, hasVertexLine = false, hasVertexTexCoordLine = false, hasUseMtlLine = false;
            
            while ((line = reader.readLine()) != null) {

                if(line.startsWith("#")){
                    
                    obj.getComments().add(line);
                    
                }else if (line.startsWith("v ")) {

                    if(!hasVertexLine){
                        hasVertexLine = true;
                    }
                    
                    String[] vertex = line.split(" ");
                    vertices.add(new Point3f(Float.valueOf(vertex[1]), Float.valueOf(vertex[2]), Float.valueOf(vertex[3])));

                } else if (line.startsWith("vn ")) {

                    if(!hasNormalLine){
                        hasNormalLine = true;
                    }
                    
                    String[] normale = line.split(" ");
                    normals.add(new Point3f(Float.valueOf(normale[1]), Float.valueOf(normale[2]), Float.valueOf(normale[3])));

                }else if (line.startsWith("vt ")) {

                    if(!hasVertexTexCoordLine){
                        hasVertexTexCoordLine = true;
                    }
                    
                    String[] coordinates = line.split(" ");
                    texCoordinates.add(new Point2f(Float.valueOf(coordinates[1]), Float.valueOf(coordinates[2])));

                }else if (line.startsWith("f ")) {

                    if (!hasFaceLine) {
                        
                        if(hasNormalLine){
                            normalsArray = new Point3f[vertices.size()];
                        }
                        if(hasVertexTexCoordLine){
                            texCoordArray = new Point2f[vertices.size()];
                        }
                        hasFaceLine = true;
                    }
                    
                    Face face = Face.parseFromLine(line);
                    
                    if(face != null){
                        
                        faces.add(new Point3i(face.verticesIndices.x, face.verticesIndices.y, face.verticesIndices.z));
                        
                        if(face.hasNormalsIndices && hasNormalLine){
                            normalsArray[face.verticesIndices.x] = normals.get(face.normalsIndices.x);
                            normalsArray[face.verticesIndices.y] = normals.get(face.normalsIndices.y);
                            normalsArray[face.verticesIndices.z] = normals.get(face.normalsIndices.z);
                        }
                        
                        if(face.hasTexCoordIndices && hasVertexTexCoordLine){
                            texCoordArray[face.verticesIndices.x] = texCoordinates.get(face.texCoordIndices.x);
                            texCoordArray[face.verticesIndices.y] = texCoordinates.get(face.texCoordIndices.y);
                            texCoordArray[face.verticesIndices.z] = texCoordinates.get(face.texCoordIndices.z);
                        }
                        
                    }else{
                        throw new Exception("Faces are not triangles !");
                    }
                    
                    faceIndex++;

                } else if (line.startsWith("usemtl ")) {
                    
                    if (!hasUseMtlLine) {
                        hasUseMtlLine = true;
                    }
                    
                    String[] split = line.split(" ");
                    materialLinks.put(materialOffsets.size(), split[1]);
                    materialOffsets.add(faceIndex);
                    
                }else if (line.startsWith("g ")) {
                    obj.getGroups().put(line.substring(2), faces.size());
                    hasFaceLine = false;
                }
            }
            
            //convert vertices list to array
            Point3f[] verticesArray = new Point3f[vertices.size()];
            vertices.toArray(verticesArray);
            obj.setPoints(verticesArray);
            
            //convert faces list to faces array
            Point3i[] facesArray = new Point3i[faces.size()];
            faces.toArray(facesArray);
            obj.setFaces(facesArray);
            
            //convert material offsets list to array
            int[] materialOffsetsArray = new int[materialOffsets.size()];
            for(int i=0;i<materialOffsets.size();i++){
                materialOffsetsArray[i] = materialOffsets.get(i);
            }
            
            obj.setMaterialOffsets(materialOffsetsArray);
            obj.setMaterialLinks(materialLinks);
            
            for(int i=0;i<normalsArray.length;i++){
                if(normalsArray[i] == null){
                    normalsArray[i] = new Point3f();
                }
            }
            obj.setNormals(normalsArray);
            
            
            obj.setTexCoords(texCoordArray);
            
            obj.setHasNormalsIndices(hasNormalLine);
            obj.setHasTexCoordIndices(hasVertexTexCoordLine);
            
            

        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
        
        if (mtlReader != null) {
            
            
            
            Map<String, Mtl> materials = new HashMap<>();

            try (BufferedReader reader = new BufferedReader(mtlReader)) {

                String line;
                String materialName = null;
                Vector3f ambientColor = null, diffuseColor = null, specularColor = null;
                
                while((line = reader.readLine()) != null){
                    
                    if(line.startsWith("newmtl ")){
                        
                        materialName = line.split(" ")[1];
                        
                    }else if(line.startsWith("Ka ")){
                        
                        String[] split = line.split(" ");
                        ambientColor = new Vector3f(Float.valueOf(split[1]), Float.valueOf(split[2]), Float.valueOf(split[3]));
                        
                    }else if(line.startsWith("Kd ")){
                        
                        String[] split = line.split(" ");
                        diffuseColor = new Vector3f(Float.valueOf(split[1]), Float.valueOf(split[2]), Float.valueOf(split[3]));
                        
                    }else if(line.startsWith("Ks ")){
                        
                        String[] split = line.split(" ");
                        specularColor = new Vector3f(Float.valueOf(split[1]), Float.valueOf(split[2]), Float.valueOf(split[3]));
                        
                        if(materialName != null && diffuseColor != null && ambientColor != null){
                            
                            materials.put(materialName, new Mtl(materialName, diffuseColor, ambientColor, specularColor));
                        }
                    }
                }
                
            } catch (FileNotFoundException ex) {
                throw ex;
            } catch (IOException ex) {
                throw ex;
            }
            
            obj.setMaterials(materials);
        }
        
        return obj;
    }
    
    public static Obj readObj(File objFile) throws FileNotFoundException, IOException, Exception{
        
        return readObj(objFile, null);
    }
            
    
    public static Obj readObj(File objFile, File mtlFile) throws FileNotFoundException, IOException, Exception{
        
        if(mtlFile == null){
            
            File newMtlFile = new File(objFile.getParent(), objFile.getName().substring(0, objFile.getName().length()-4)+".mtl");
            if(newMtlFile.exists()){
                return readObj(new FileReader(objFile), new FileReader(newMtlFile));
            }else{
                return readObj(new FileReader(objFile), null);
            }
            
        }else{
            return readObj(new FileReader(objFile), new FileReader(mtlFile));
        }
    }
}
