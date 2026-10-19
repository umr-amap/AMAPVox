package org.amapvox.canopy.mmr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import org.amapvox.commons.raytracing.geometry.Transformations;

public class Turtle {

    public Vector3f[] directions;
    private float[] elevation;
    private float[] azimuth;

    public Turtle(int directionCount, float rotation) throws IOException {

        InputStream resource = Turtle.class.getResource("directions").openStream();

        if (resource == null) {
            throw new IOException("Cannot load resource file directions");
        }

        InputStreamReader pointsStream = new InputStreamReader(resource);

        directions = new Vector3f[directionCount];

        try (BufferedReader reader = new BufferedReader(pointsStream)) {

            String line;
            int directionIndex = 0;

            while ((line = reader.readLine()) != null) {

                if (directionIndex < directionCount) {
                    String[] split = line.split("\t");
                    directions[directionIndex] = new Vector3f(Float.parseFloat(split[0]), Float.parseFloat(split[1]), Float.parseFloat(split[2]));
                    directionIndex++;
                } else {
                    break;
                }
            }

            if (rotation != 0) {

                Transformations transform = new Transformations();
                transform.setRotationAroundZ(rotation);

                for (directionIndex = 0; directionIndex < directions.length; directionIndex++) {
                    Vector3d direction = new Vector3d(directions[directionIndex]);
                    transform.apply(direction);
                    directions[directionIndex] = new Vector3f(direction);
                }
            }

            elevation = new float[directionCount];
            azimuth = new float[directionCount];

            for (directionIndex = 0; directionIndex < directionCount; directionIndex++) {

                elevation[directionIndex] = (float) Math.asin(directions[directionIndex].z);
                Vector2f proj = new Vector2f(directions[directionIndex].x, directions[directionIndex].y);
                proj.normalize();
                azimuth[directionIndex] = (float) Math.acos(proj.x);
                if (proj.y > 0) {
                    azimuth[directionIndex] = (float) (Math.PI * 2.) - azimuth[directionIndex];
                }

                azimuth[directionIndex] -= Math.PI / 2;
                if (azimuth[directionIndex] < 0) {
                    azimuth[directionIndex] += 2 * Math.PI;
                }
            }
        }
    }
    
    public Turtle(int directionCount) throws IOException {
        this(directionCount, 0.f);
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

    public int getDirectionCount() {
        return directions.length;
    }
}
