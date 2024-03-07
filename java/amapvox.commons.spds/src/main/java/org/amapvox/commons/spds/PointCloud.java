
/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */

package org.amapvox.commons.spds;

import org.amapvox.commons.math.geometry.BoundingBox3D;
import org.amapvox.commons.util.io.file.CSVFile;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

/**
 *
 * @author Julien
 */


public class PointCloud {
        
    public List<Point3d> points;
    private Point3d min;
    private Point3d max;
    
     public PointCloud(){
        points = new ArrayList<>();
    }
    
    public boolean isPointInsidePointCloud(Point3d point, float maxDistance){
        
        int index = nearestPoint(point, maxDistance);
        return index >= 0;
        
    }
    
    public int nearestPoint(Point3d point, float maxDistance){
        int low = 0;
        int high = points.size()-1;

        Point3dComparator comparator = new Point3dComparator();
        while (low <= high) {
            int mid = (low + high) >>> 1;
            Point3d midVal = points.get(mid);
            int cmp = comparator.compare(midVal, point);

            if (cmp < 0){
                low = mid + 1;
            }else if (cmp > 0){
                high = mid - 1;
            }else{
                return mid; // key found
            }
        }
        
        if((low < points.size()) && (point.distance(points.get(low)) < maxDistance)){
            return low;
        }else if(high >= 0 && (point.distance(points.get(high)) < maxDistance)){
            return high;
        }
        
        return -(low + 1);  // key not found
        //return Collections.binarySearch(points, point);
    }
    
    public void readFromFile(CSVFile file, Matrix4d transfMatrix) throws FileNotFoundException, IOException{
        
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                
                points = new ArrayList<>();
                String line;
                
                if(file.containsHeader()){
                    reader.readLine();
                }
                
                for(int i=0;i<file.getNbOfLinesToSkip();i++){
                    reader.readLine();
                }
                
                Map<String, Integer> columnAssignment = file.getColumnAssignment();
                
                while((line = reader.readLine()) != null){
                    
                    String[] split = line.split(file.getColumnSeparator());
                    
                    Point3d transformedPoint = new Point3d(
                            Float.valueOf(split[columnAssignment.get("X")]),
                            Float.valueOf(split[columnAssignment.get("Y")]),
                            Float.valueOf(split[columnAssignment.get("Z")])
                    );
                    transfMatrix.transform(transformedPoint);
                
                    points.add(transformedPoint);
                }
            
            long startTime = System.currentTimeMillis();
            Collections.sort(points, new Point3dComparator());
            long endTime = System.currentTimeMillis();
            
            if(points != null && points.size()>0){
                min = points.get(0);
                max = points.get(points.size()-1);
            }
            
            System.out.println("temps de tri: "+((endTime-startTime)*Math.pow(10, -3)));
            
        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
    }
    
    public BoundingBox3D getBoundingBox(){
        
        double xMin = 0, yMin = 0, zMin = 0;
        double xMax = 0, yMax = 0, zMax = 0;
        
        int count = 0;
        
        for(Point3d point : points){
            
            if(count == 0){
                
                xMin = point.x;
                xMax = point.x;
                yMin = point.y;
                yMax = point.y;
                zMin = point.z;
                zMax = point.z;
                
            }else{
                
                if(point.x < xMin){
                    xMin = point.x;
                }else if(point.x > xMax){
                    xMax = point.x;
                }
                
                if(point.y < yMin){
                    yMin = point.y;
                }else if(point.y > yMax){
                    yMax = point.y;
                }
                
                if(point.z < zMin){
                    zMin = point.z;
                }else if(point.z > zMax){
                    zMax = point.z;
                }
            }
            
            count++;
        }
        
        return new BoundingBox3D(new Point3d(xMin, yMin, zMin), new Point3d(xMax, yMax, zMax));
    }
}
