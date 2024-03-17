package org.amapvox.canopy.mmr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import org.apache.log4j.Logger;

public class Turtle {

    private final static Logger logger = Logger.getLogger(Turtle.class);
    
    public Vector3f[] directions;
    private float[] elevation;
    private float[] azimuth;
    
    
    
    public Turtle(){
         
    }
    
    static{
        
    }

    public Turtle(int nbDirections, float rotation) throws IOException {
        
        InputStream resource = Turtle.class.getResource("directions").openStream();
        
        if(resource == null){
            throw new IOException("Cannot load resource file directions");
        }
        
        InputStreamReader pointsStream = new InputStreamReader(resource);
        
        directions = new Vector3f[nbDirections];
        
        try (BufferedReader reader = new BufferedReader(pointsStream)) {

            String line;
            int count = 0;

            while ((line = reader.readLine()) != null) {
                
                if(count < nbDirections){
                    String[] split = line.split("\t");
                    directions[count] = new Vector3f(Float.valueOf(split[0]), Float.valueOf(split[1]), Float.valueOf(split[2]));
                    count++;
                }else{
                    break;
                }
            }
            
//            if(rotation != 0){
//            
//                Transformations transform = new Transformations();
//                transform.setRotationAroundZ(rotation);
//                
//                for(int i=0;i<directions.length;i++){
//
//                    Vector3f direction = directions[i];
//                    transform.apply(new Vector3d(direction));
//                    directions[i] = direction;
//                }
//            }
            
            int nbSectors = nbDirections;
            
            elevation = new float[nbSectors];
            azimuth = new float[nbSectors];

            for (int p = 0, sector = 0; p < nbDirections; p++) {
                    
                elevation[sector] = (float) Math.asin(directions[p].z);
                Vector2f proj = new Vector2f(directions[p].x, directions[p].y);
                proj.normalize();
                azimuth[sector] = (float) Math.acos(proj.x);
                if (proj.y > 0) {
                    azimuth[sector] = (float) (Math.PI * 2.) - azimuth[sector];
                }

                azimuth[sector] -= Math.PI / 2;
                if (azimuth[sector] < 0) {
                    azimuth[sector] += 2 * Math.PI;
                }

                sector++;
            }

        } catch (IOException ex) {
            
            throw ex;
        }
        
    }
   
    public float getElevationAngle(int d) {
        return elevation[d];
    }

    public float getZenithAngle(int d) {
        return (float) ((Math.PI / 2) - elevation[d]);
    }

    public float getAzimuthAngle(int d) {
        return azimuth[d];
    }


    public int getNbDirections() {
        return directions.length;
    }

    public void setDirections(Vector3f[] directions) {
        this.directions = directions;
    }

    public void setElevation(float[] elevation) {
        this.elevation = elevation;
    }

    public void setAzimuth(float[] azimuth) {
        this.azimuth = azimuth;
    }

    /**
     * TODO: compute the radius of sectors; either individually or on average
     */
    public void edgeLength() {

		// edges length
        // for (int sp=0; sp<sPath.size()-1; sp++) {
        // Vector3f edge = new Vector3f(pts2[sPath.get(sp)]);
        // edge.sub(pts2[sPath.get(sp+1)]);
        // System.out.println(edge.length());
        // }
    }
}
