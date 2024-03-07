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
package org.amapvox.viewer3d.object.scene;

import org.amapvox.viewer3d.object.camera.Camera;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

/**
 *
 * @author Julien Heurtebize
 */
public class MousePicker {

    private Vector3f currentRay;

    private Matrix4f invertedProjectionMatrix;
    private Matrix4f invertedViewMatrix;
    private final Camera camera;
    private Vector4f eyeWorldCoords;

    private final static boolean DEBUG = false;

    public MousePicker(Camera camera) {
        currentRay = new Vector3f();
        invertedProjectionMatrix = new Matrix4f();
        invertedProjectionMatrix.setIdentity();
        invertedViewMatrix = new Matrix4f();
        invertedViewMatrix.setIdentity();
        this.camera = camera;
    }

    public void update(float mouseX, float mouseY, int startX, int startY, float displayWidth, float displayHeight) {

        currentRay = calculateMouseRay(mouseX, mouseY, startX, startY, displayWidth, displayHeight);
    }

    public Vector3f getCurrentRay() {
        return currentRay;
    }

    /**
     * For ray picking, the difference between a perspective and orthogonal
     * projection is that for perspective, all rays have the same initial
     * position (the camera position), and their directions depend on the mouse
     * location. In the orthographic case, all rays have the same direction (the
     * camera direction) but have initial positions dependent on the mouse
     * position.
     *
     * @param distance
     * @return
     */
    public Point3f getPointOnray(float distance) {

        if (!camera.isPerspective()) {

            Vector3f start = new Vector3f(eyeWorldCoords.x, eyeWorldCoords.y, eyeWorldCoords.z);

            Point3f point = new Point3f();
            point.add(start, camera.getTarget());
            Vector3f dir = new Vector3f();
            dir.sub(camera.getTarget(), camera.getLocation());
            dir.normalize();
            dir.scale(distance);
            point.add(dir);

            return point;

        } else {
            Vector3f start = new Vector3f(camera.getLocation().x, camera.getLocation().y, camera.getLocation().z);
            Vector3f scaledRay = new Vector3f(currentRay.x * distance, currentRay.y * distance, currentRay.z * distance);
            Point3f point = new Point3f();
            point.add(start, scaledRay);

            return point;
        }

    }

    public static Point3f getPointOnray(Point3f camPosition, Vector3f ray, float distance) {

        Vector3f start = new Vector3f(camPosition.x, camPosition.y, camPosition.z);
        Vector3f scaledRay = new Vector3f(ray.x * distance, ray.y * distance, ray.z * distance);
        Point3f tmp = new Point3f();
        tmp.add(start, scaledRay);

        return tmp;
    }

    public Vector3f calculateMouseRay(float mouseX, float mouseY, int startX, int startY, float displayWidth, float displayHeight) {

        Vector2f normalizedCoords = getNormalizedDeviceCoords(mouseX, mouseY, startX, startY, displayWidth, displayHeight);

        if (DEBUG) {
            System.out.println("Normalized device coord : " + normalizedCoords.x + "\t" + normalizedCoords.y);
        }

        Vector4f clipCoords = new Vector4f(normalizedCoords.x, normalizedCoords.y, -1, 1f);
        Vector4f eyeCoords = toEyeCoords(clipCoords);
        Vector3f worldRay = toWorldCoords(eyeCoords);
        return new Vector3f(worldRay.x, worldRay.y, worldRay.z);
    }

    public Vector3f toWorldCoords(Vector4f eyeCoords) {

        Vector4f rayWorld = new Vector4f();
        invertedViewMatrix.transform(eyeCoords, rayWorld);
        this.eyeWorldCoords = new Vector4f(rayWorld);
        
        rayWorld.normalize();
        Vector3f mouseRay = new Vector3f(rayWorld.x, rayWorld.y, rayWorld.z);
        mouseRay.normalize();

        return mouseRay;
    }

    public Vector4f toEyeCoords(Vector4f clipCoords) {

        Vector4f eyeCoords = new Vector4f();
        invertedProjectionMatrix.transform(clipCoords, eyeCoords);
        return new Vector4f(eyeCoords.x, eyeCoords.y, -1, 0);
    }

    /**
     * Get the normalized device coordinates of the mouse from -1 to 1. The left
     * lower corner of the screen is -1 and the right upper corner of the screen
     * is 1 on both axis.
     *
     * @param mouseX mouse location x in screen coordinates
     * @param mouseY mouse location y in screen coordinates
     * @param startX viewport start x
     * @param startY viewport start y
     * @param displayWidth viewport width
     * @param displayHeight viewport height
     * @return the normalized device coordinates (NDC)
     */
    public Vector2f getNormalizedDeviceCoords(float mouseX, float mouseY, int startX, int startY, float displayWidth, float displayHeight) {

        float x = (2f * (mouseX - startX)) / displayWidth - 1;
        float y = -((2f * (mouseY - startY)) / displayHeight - 1);

        return new Vector2f(x, y);
    }

    public void setProjectionMatrix(Matrix4f projectionMatrix) {
        
        invertedProjectionMatrix = new Matrix4f();
        invertedProjectionMatrix.transpose(projectionMatrix);
        invertedProjectionMatrix.invert();
    }

    public void setViewMatrix(Matrix4f viewMatrix) {
        
        invertedViewMatrix = new Matrix4f();
        invertedViewMatrix.transpose(viewMatrix);
        invertedViewMatrix.invert();
    }

    public Point3f getCamPosition() {

        Vector3f location = camera.getLocation();

        return new Point3f(location.x, location.y, location.z);
    }

    public Camera getCamera() {
        return camera;
    }

    public Vector4f getEyeCoords() {
        return eyeWorldCoords;
    }
}
