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
package org.amapvox.viewer3d.object.camera;

import org.amapvox.commons.math.util.MatrixUtility;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.event.EventListenerList;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public abstract class Camera {

    protected final PropertyChangeSupport props = new PropertyChangeSupport(this);

    protected Matrix4f projectionMatrix;
    protected Matrix4f viewMatrix;
    protected float fovy = 60.0f;
    protected float aspect;
    protected float nearPersp = 10;
    protected float farPersp = 1000;
    protected float nearOrtho = 0.1f;
    protected float farOrtho = 1000;
    protected float left = Float.NaN;
    protected float right = Float.NaN;
    protected float bottom = Float.NaN;
    protected float top = Float.NaN;
    protected Vector3f location;
    protected Vector3f target;
    protected Vector3f up;
    protected boolean isUpdated;
    protected float phi, theta;
    protected boolean perspective = true;
    protected float angleX = 0.0f;
    protected float angleY = 0.0f;
    protected float angleZ = 0.0f;
    protected EventListenerList listeners;
    public boolean isInit = false;

    //set lookat matrix (position, lookat)
    public abstract void init(Vector3f location, Vector3f target, Vector3f up);

    public abstract void setPerspective(float fovy, float aspect, float near, float far);

    public abstract void setOrthographic(float left, float right, float top, float bottom, float near, float far);

    public abstract void rotateAroundPoint(Vector3f axis, Vector3f pivot, float angle);

    public abstract void updateViewMatrix();

    public abstract void addCameraListener(CameraListener listener);

    public abstract void fireLocationChanged(Vector3f location);

    public abstract void fireTargetChanged(Vector3f target);

    public abstract void fireViewMatrixChanged(Matrix4f viewMatrix);

    public abstract void fireProjectionMatrixChanged(Matrix4f projMatrix);

    public abstract void setTarget(Vector3f target);

    public abstract void setLocation(Vector3f location);

    public abstract Vector3f getTarget();

    public abstract Vector3f getLocation();

    public void addPropertyChangeListener(String propName, PropertyChangeListener l) {
        props.addPropertyChangeListener(propName, l);
    }

    public void project(Vector3f location, Vector3f target) {

        this.location.x = location.x;
        this.location.y = location.y;
        this.location.z = location.z;

        this.target.x = target.x;
        this.target.y = target.y;
        this.target.z = target.z;

        updateViewMatrix();
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public void projectTop() {
        project(new Vector3f(location.x, location.y, location.z), new Vector3f(location.x, location.y - 1, location.z));
    }

    public void projectBottom() {
        project(new Vector3f(location.x, location.y, location.z), new Vector3f(location.x, location.y + 1, location.z));
    }

    public void projectLeft() {
        project(new Vector3f(location.x, location.y, location.z), new Vector3f(-1, 0, 0));
    }

    public void projectRight() {
        project(new Vector3f(location.x, location.y, location.z), new Vector3f(location.x, location.y, location.z));
    }

    public void projectFront() {

        project(new Vector3f(location.x, location.y, location.z), new Vector3f(location.x, location.y, location.z - 1));
    }

    public void projectBack() {
        project(new Vector3f(location.x, location.y, location.z), new Vector3f(location.x, location.y, location.z + 1));
    }

    public void projectIsometric() {

    }

    public float getFovy() {
        return fovy;
    }

    public float getNearPersp() {
        return nearPersp;
    }

    public float getNearOrtho() {
        return nearOrtho;
    }

    public float getLeft() {
        return left;
    }

    public float getRight() {
        return right;
    }

    public float getBottom() {
        return bottom;
    }

    public float getTop() {
        return top;
    }

    public Vector3f getUp() {
        return up;
    }

    public float getFarPersp() {
        return farPersp;
    }

    public float getFarOrtho() {
        return farOrtho;
    }

    public float getAspect() {
        return aspect;
    }

    public void setIsPerspective(boolean isPerspective) {
        this.perspective = isPerspective;
    }

    public void setRotation(Vector3f axis, float angle) {
        AxisAngle4f aa = new AxisAngle4f(axis, angle);
        viewMatrix.setRotation(aa);
    }

    public void updateProjMatrix() {

        Matrix4f oldValue = projectionMatrix;

        if (perspective) {
            projectionMatrix = MatrixUtility.perspective(fovy, aspect, nearPersp, farPersp);
        } else {
            projectionMatrix = MatrixUtility.ortho(left, right, bottom, top, nearOrtho, farOrtho);
        }

        fireProjectionMatrixChanged(projectionMatrix);
        props.firePropertyChange("projMatrix", oldValue, projectionMatrix);
    }

    public boolean isPerspective() {
        return perspective;
    }

    public void setNearPersp(float nearPersp) {
        this.nearPersp = nearPersp;
        updateProjMatrix();
    }

    public void setFarPersp(float farPersp) {
        this.farPersp = farPersp;
        updateProjMatrix();
    }

    /**
     *
     * @param translation translation vector of camera
     */
    public abstract void translate(Vector3f translation);
}
