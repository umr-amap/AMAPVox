/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.converter;

import org.amapvox.commons.math.util.SphericalCoordinates;
import org.amapvox.lidar.gridded.LPointShotExtractor;
import org.amapvox.lidar.gridded.LShot;
import org.amapvox.lidar.leica.ptg.PTGScan;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;
import java.util.Locale;
import javax.vecmath.Vector3d;

/**
 *
 * @author Julien Heurtebize
 */
public class PTXScanConversion {

    public PTXScanConversion() {
    }
    
    
    
    public void toTxt(SimpleScan scan, File outputDirectory,
            boolean exportRGB, boolean exportIntensity) throws IOException, Exception{
        
        throw new UnsupportedOperationException();
        /***Convert rxp to txt***/

//        File outputTxtFile = new File(outputDirectory.getAbsolutePath()+File.separator+scan.file.getName()+".txt");
//        
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputTxtFile))) {
//            
//            
//            /**Transformation**/
//            Mat4D popMatrix = scan.popMatrix;
//            Mat4D sopMatrix = scan.sopMatrix;
//            
//            Mat4D transfMatrix = Mat4D.multiply(sopMatrix, popMatrix);
//
//            Mat3D rotation = new Mat3D();
//            rotation.mat = new double[]{
//                transfMatrix.mat[0],transfMatrix.mat[1],transfMatrix.mat[2],
//                transfMatrix.mat[4],transfMatrix.mat[5],transfMatrix.mat[6],
//                transfMatrix.mat[8],transfMatrix.mat[9],transfMatrix.mat[10]
//            };   
//            
//            DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
//            otherSymbols.setDecimalSeparator('.');
//            otherSymbols.setGroupingSeparator('.'); 
//            DecimalFormat strictFormat = new DecimalFormat("###.##", otherSymbols);
//            
//            String header = "shotID x y z directionX directionY directionZ distance nbEchos rangEcho";
//            
//            if(exportIntensity){
//                header += " intensity";
//            }
//
//            if(exportRGB){
//                header += " red green blue";
//            }
//            
//            header += "\n";
//            
//            writer.write(header);
//            
//            PTGScan ptgScan = new PTGScan();
//            ptgScan.openScanFile(scan.file);
//                
//            LPointShotExtractor shots = new LPointShotExtractor(ptgScan);
//            Iterator<LShot> iterator = shots.iterator();
//            
//            int shotID = 0;
//             
//            while(iterator.hasNext()){
//                
//                LShot shot = iterator.next();
//                shot.direction.normalize();
//                
//                Vec4D origin = Mat4D.multiply(transfMatrix, new Vec4D(shot.origin.x, shot.origin.y, shot.origin.z, 1.0d));
//                Vec3D direction = Mat3D.multiply(rotation, new Vec3D(shot.direction.x, shot.direction.y, shot.direction.z));
//                direction = Vec3D.normalize(direction);
//                
//                SphericalCoordinates sc = new SphericalCoordinates();
//                sc.toSpherical(new Vector3d(direction.x, direction.y, direction.z));
//                
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
//                
//                shotID++;
//            }
//        }catch(Exception ex){
//            System.err.println(ex);
//        }
    }
}
