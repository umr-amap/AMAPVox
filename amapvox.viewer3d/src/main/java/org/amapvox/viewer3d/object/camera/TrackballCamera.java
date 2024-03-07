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
import org.amapvox.commons.math.util.SphericalCoordinates;
import org.amapvox.viewer3d.object.scene.MousePicker;
import org.amapvox.viewer3d.object.scene.SceneObject;
import javax.swing.event.EventListenerList;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;
//import org.apache.commons.math3.util.MathUtils;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class TrackballCamera extends Camera {

    private Vector3f forwardVec;
    private Vector3f rightVec;
    private Vector3f upVec;

    private float viewportWidth;
    private float viewportHeight;

    private float width;
    private float height;

    private boolean inverseY = false;
    private MousePicker mousePicker;

    private SceneObject pivot;

    public float getAngleX() {
        return angleX;
    }

    public float getAngleY() {
        return angleY;
    }

    public float getAngleZ() {
        return angleZ;
    }

    public TrackballCamera() {

        listeners = new EventListenerList();
        viewMatrix = new Matrix4f();
        viewMatrix.setIdentity();
        location = new Vector3f();
        target = new Vector3f();
        up = new Vector3f(0, 0, 1);
        mousePicker = new MousePicker(this);
    }

    @Override
    public void init(Vector3f location, Vector3f target, Vector3f up) {

        /*
        this.location =location;
        this.target = target;
         */
        this.up = up;
        /*
        viewMatrix = Matrix4f.identity();
        viewMatrix = Matrix4f.lookAt(location, target, up);
        
        orientation = Vector3f.substract(target, location);
        orientation = Vector3f.normalize(orientation);
         */

        //updateViewMatrix();
    }

    public void initOrtho(float left, float right, float top, float bottom, float near, float far) {

        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.nearOrtho = near;
        this.farOrtho = far;
    }

    @Override
    public void setPerspective(float fovy, float aspect, float near, float far) {

        this.fovy = fovy;
        this.aspect = aspect;
        this.nearPersp = near;
        this.farPersp = far;

        this.perspective = true;

        updateProjMatrix();

    }

    @Override
    public void setOrthographic(float left, float right, float top, float bottom, float near, float far) {

        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.nearOrtho = near;
        this.farOrtho = far;

        this.perspective = false;

        updateProjMatrix();

    }

    @Override
    public void rotateAroundPoint(Vector3f axis, Vector3f pivot, float angle) {

        if (axis.x != 0) {

            //this.angleX = angle;
            this.angleX = this.angleX + angle;

            float r = (float) Math.sqrt(Math.pow(pivot.z - location.z, 2) + Math.pow(pivot.y - location.y, 2));

            float z = (float) (pivot.z + r * Math.cos(this.angleX));
            float y = (float) (pivot.y + r * Math.sin(this.angleX));

            location.z = z;
            location.y = y;

        } else if (axis.y != 0) {

            //this.angleY = angle;
            this.angleY = this.angleY + angle;

            /*
            if(Math.toDegrees(angleY)>360){
                
                angleY = (float) Math.toRadians(Math.toDegrees(angleY) - 360);
            }else if(Math.toDegrees(angleY)<0){
                angleY = (float) Math.toRadians(360 - Math.toDegrees(-angleY));
            }
             */
            float r = (float) Math.sqrt(Math.pow(pivot.x - location.x, 2) + Math.pow(pivot.z - location.z, 2));

            float x = (float) (pivot.x + r * Math.cos(this.angleY));
            float z = (float) (pivot.z + r * Math.sin(this.angleY));

            location.x = x;
            location.z = z;

        } else if (axis.z != 0) {

            //this.angleZ = angle;
            this.angleZ = this.angleZ + angle;

            float r = (float) Math.sqrt(Math.pow(pivot.x - location.x, 2) + Math.pow(pivot.y - location.y, 2));

            float x = (float) (pivot.x + r * Math.cos(this.angleZ));
            float y = (float) (pivot.y + r * Math.sin(this.angleZ));

            location.x = x;
            location.y = y;
        }

        target = pivot;

        updateViewMatrix();
    }

    public void setPivot(SceneObject sceneObject) {

        this.target = new Vector3f(sceneObject.getGravityCenter().x, sceneObject.getGravityCenter().y, sceneObject.getGravityCenter().z);
        this.pivot = sceneObject;
    }

    public void setPivot(Vector3f pivot) {

        this.target = pivot;
    }

//    private float normalizeTheta(float theta) {
//
//        return (float) MathUtils.normalizeAngle(theta, 0);
//        //normalize between 0 and 2pi
//        /*while(theta < 0){
//            theta += Math.PI * 2;
//        }
//        while(theta > (Math.PI * 2)){
//            theta -= (Math.PI  * 2);
//        } 
//        
//        return theta;*/
//    }
//
//    private float normalizePhi(float phi) {
//
//        //lock between 0 excluded and pi exluded
//        while (phi < 0) {
//            phi += Math.PI * 2;
//        }
//        while (phi > (Math.PI * 2)) {
//            phi -= (Math.PI * 2);
//        }
//
//        return phi;
//    }

    public void rotateFromOrientationV2(float offsetX, float offsetY) {

        //get current theta and phi
        SphericalCoordinates sc1 = SphericalCoordinates.fromCartesian(location.x - target.x, location.y - target.y, location.z - target.z);

        float oldTheta = (float) sc1.getAzimut();
        float oldPhi = (float) sc1.getZenith();

        if (location.x == target.x && location.y == target.y) {

            SphericalCoordinates sc2 = SphericalCoordinates.fromCartesian(upVec.x, upVec.y, upVec.z);
            double azimut1 = sc2.getAzimut();

            oldTheta = (float) -azimut1;

            if (upVec.y == -1) {
                offsetY = 0;
            }
        }

        //theta doit être compris entre 0 et 2pi
        //phi doit être compris entre entre ]0 et pi[
        float theta = 0, phi = 0;

        forwardVec = getForwardVector();
        float radius = forwardVec.length();

        float thetaStep = (float) Math.toRadians(Math.abs(offsetX));
        float phiStep = (float) Math.toRadians(Math.abs(offsetY / 2.0f));

        //float thetaStep = (float) ((Math.PI * 2)/360.0f); //1° step
        //float phiStep = (float) ((Math.PI * 2)/360.0f); //1° step
        theta += oldTheta;
        phi += oldPhi;

        if (offsetX > 0) {
            theta -= thetaStep;
        } else if (offsetX < 0) {
            theta += thetaStep;
        }
        //theta = normalizeTheta(theta);

        if (offsetY > 0) {
            phi -= phiStep;
            if (phi < 0) {
                phi += phiStep;
            }
        } else if (offsetY < 0) {

            if (phi + phiStep < Math.PI) {
                phi += phiStep;
            }
            /*if(phi > Math.PI){
                phi -= phiStep + 0.000001;
            }*/
        }

        //phi = normalizePhi(phi);
        Point3d cartesian = SphericalCoordinates.toCartesian(theta, phi, radius);

        location.x = target.x + (float) cartesian.getX();
        location.y = target.y + (float) cartesian.getY();
        location.z = target.z + (float) cartesian.getZ();

        updateViewMatrix();
    }

    public void rotateFromOrientation(Vector3f axis, Vector3f center, float angle) {

        forwardVec = getForwardVector();

        float radius = forwardVec.length();

        forwardVec.normalize();

        rightVec = new Vector3f();
        rightVec.cross(forwardVec, up);

        if (rightVec.length() == 0) {
            rightVec.y = 1;
        }

        upVec = getUpVector();

        //forwardVec = Vector3f.normalize(forwardVec);
        rightVec.normalize();
        upVec.normalize();

        if (axis.x != 0) {

            //équation du cercle:
            //M = C + A* r * cos(t) + B * r *sin(t) avec M = position sur le cercle, C = centre du cercle, A = vecteur du cercle, B = vecteur du cercle orthogonal à A, r = rayon du cercle, t = angle
            //position = Vec3.add(pivot, Vec3.add(Vec3.multiply(viewXAxis, r* (float)Math.cos(angleX)), Vec3.multiply(viewYAxis, r* (float)Math.sin(angleX))));
            //pondération de l'angle par l'inclinaison
            float n = forwardVec.dot(up);
            float d = forwardVec.length() * up.length();

            float tilt = (float) (Math.acos(Math.abs(n / d)));
            /*
            if(tilt == 0){
                tilt = 0.18f;
            }*/

            angle *= (tilt / (Math.PI / 2.0d));

            angle = -angle;
            float angleSinus = (float) Math.sin(angle);
            float angleCosinus = (float) Math.cos(angle);

            location.x = target.x + (-forwardVec.x * radius * angleCosinus) + (rightVec.x * radius * angleSinus);
            location.y = target.y + (-forwardVec.y * radius * angleCosinus) + (rightVec.y * radius * angleSinus);
            location.z = target.z + (-forwardVec.z * radius * angleCosinus) + (rightVec.z * radius * angleSinus);

        }
        if (axis.y != 0) {

            float angleSinus = (float) Math.sin(angle);
            float angleCosinus = (float) Math.cos(angle);

            //copy
            Vector3f oldLocation = new Vector3f(location.x, location.y, location.z);

            location.x = target.x + (-forwardVec.x * radius * angleCosinus) + (upVec.x * radius * angleSinus);
            location.y = target.y + (-forwardVec.y * radius * angleCosinus) + (upVec.y * radius * angleSinus);
            location.z = target.z + (-forwardVec.z * radius * angleCosinus) + (upVec.z * radius * angleSinus);

            Vector3f newForwardVec = getForwardVector();
            newForwardVec.normalize();

            Vector3f newRightVec = new Vector3f();
            newRightVec.cross(newForwardVec, up);

            if ((newRightVec.y < 0 && rightVec.y > 0) || (newRightVec.y > 0 && rightVec.y < 0)) {
                location = oldLocation;
            }
        }

        //target = pivot;
        updateViewMatrix();

    }

    public void rotateX(Vector3f center, float angle) {

        rotateAroundPoint(new Vector3f(1.0f, 0.0f, 0.0f), center, angle);
    }

    public void rotateY(Vector3f center, float angle) {

        rotateAroundPoint(new Vector3f(0.0f, 1.0f, 0.0f), center, angle);
    }

    public void rotateZ(Vector3f center, float angle) {

        rotateAroundPoint(new Vector3f(0.0f, 0.0f, 1.0f), center, angle);
    }

    /**
     *
     * @param translation translation vector of camera
     */
    @Override
    public void translate(Vector3f translation) {

        float copyDistanceToTarget = getDistanceToTarget();

        forwardVec = getForwardVector();

        forwardVec.normalize();

        if (translation.x != 0.0f) {

            Vector3f sideShift = new Vector3f();
            sideShift.cross(up, forwardVec);
            sideShift.normalize();
            sideShift.scale(translation.x);
            location.add(sideShift);
            //target = Vector3f.add(target, Vector3f.multiply(sideShift, translation.x));
        }
        if (translation.y != 0.0f) {

            Vector3f verticalShift = new Vector3f(up);
            verticalShift.scale(translation.y);
            location.add(verticalShift);
            //target = Vector3f.add(target, Vector3f.multiply(verticalShift, translation.y));
        }
        if (translation.z != 0.0f) {

            if (perspective) {
                //target = Vector3f.add(target, Vector3f.multiply(orientation, translation.z)); //use for not reaching the target
                //setPerspective(70.0f, (1.0f*640)/480, near-translation.z, far-translation.z);
            } else {

                if ((left < -1.0f && right > 1.0f) || translation.z < 0) {

                    float widthCoeff = viewportWidth / 100.0f;
                    float heightCoeff = viewportHeight / 100.0f;

                    float leftCopy = left;
                    float rightCopy = right;
                    float topCopy = top;
                    float bottomCopy = bottom;

                    left = left + translation.z * (widthCoeff);

                    right = right - translation.z * (widthCoeff);

                    bottom = bottom + translation.z * (heightCoeff);

                    top = top - translation.z * (heightCoeff);

                    if (left > -1 || right < 1 || bottom > -1 || top < 1) {
                        left = leftCopy;
                        right = rightCopy;
                        bottom = bottomCopy;
                        top = topCopy;
                    }
                }

                //updateProjMatrix();
            }

            //copy old location
            Vector3f oldForwardVector = getForwardVector();
            Vector3f oldLocation = location;

            //test translation effect
            Vector3f shift = new Vector3f(forwardVec);
            shift.scale(translation.z);
            location.add(shift);
            Vector3f newForwardVector = getForwardVector();

            //if translation is not good, get back to the original location (equivalent to not move)
            if ((newForwardVector.z < 0 && oldForwardVector.z > 0) || (newForwardVector.z > 0 && oldForwardVector.z < 0)) {
                location = oldLocation;
            }

        }

        updateViewMatrix();

        float distanceToTarget = getDistanceToTarget();

        nearPersp = distanceToTarget - 500.0f;
        nearPersp = Float.max(nearPersp, 0.01f);

        farPersp = distanceToTarget + 500.0f;

        //nearOrtho = nearPersp;
        //farOrtho = farPersp;
        updateProjMatrix();
    }

    public void translateV2(Vector3f translation) {

        forwardVec = getForwardVector();

        //slow down relatively from the length of the forward vector
        if (perspective) {
            translation.scale((forwardVec.length() / (float) Math.tan(fovy)) * 0.001f);
        } else {
            translation.scale(forwardVec.length() * 0.0025f);
        }

        forwardVec.normalize();

        rightVec = getRightVector();
        upVec = getUpVector();

        Vector3f xTranslation = new Vector3f(rightVec);
        xTranslation.scale(-translation.x);
        Vector3f yTranslation = new Vector3f(upVec);
        yTranslation.scale(translation.y);

        Vector3f translationVec = new Vector3f();
        translationVec.add(xTranslation, yTranslation);

        location.add(translationVec);
        target.add(translationVec);

        updateViewMatrix();
    }

    @Override
    public void updateViewMatrix() {

        Matrix4f oldValue = viewMatrix;
        forwardVec = getForwardVector();
        viewMatrix = MatrixUtility.lookAt(location, target, up);
        notifyViewMatrixChanged();
        props.firePropertyChange("viewMatrix", oldValue, viewMatrix);

    }

    public void notifyViewMatrixChanged() {

        fireLocationChanged(location);
        fireTargetChanged(target);
        fireViewMatrixChanged(viewMatrix);
    }

    @Override
    public void addCameraListener(CameraListener listener) {
        listeners.add(CameraListener.class, listener);
    }

    @Override
    public void fireLocationChanged(Vector3f location) {

        for (CameraListener listener : listeners.getListeners(CameraListener.class)) {

            listener.locationChanged(location);
        }
    }

    @Override
    public void fireTargetChanged(Vector3f target) {

        for (CameraListener listener : listeners.getListeners(CameraListener.class)) {

            listener.targetChanged(target);
        }
    }

    @Override
    public void setTarget(Vector3f target) {
        this.target = target;
    }

    @Override
    public void setLocation(Vector3f location) {
        this.location = location;
    }

    @Override
    public void fireViewMatrixChanged(Matrix4f viewMatrix) {

        for (CameraListener listener : listeners.getListeners(CameraListener.class)) {

            listener.viewMatrixChanged(viewMatrix);
        }
    }

    @Override
    public void fireProjectionMatrixChanged(Matrix4f projMatrix) {

        for (CameraListener listener : listeners.getListeners(CameraListener.class)) {

            try {
                listener.projMatrixChanged(projMatrix);
            } catch (RuntimeException e) {
                listeners.remove(CameraListener.class, listener);
            }

        }
    }

    @Override
    public boolean isPerspective() {
        return perspective;
    }

    public void setOrthographic(float near, float far) {

        this.nearOrtho = near;
        this.farOrtho = far;

        this.perspective = false;

        updateProjMatrix();
    }

    private float getTargetDistance() {

        Vector3f center = new Vector3f(getPivot().getGravityCenter().x,
                getPivot().getGravityCenter().y,
                getPivot().getGravityCenter().z);
        center.sub(location);
        center.length();
        return center.length();
    }

    public void setViewToBack() {

        project(
                new Vector3f(getPivot().getGravityCenter().x,
                        getPivot().getGravityCenter().y + getTargetDistance(),
                        getPivot().getGravityCenter().z),
                new Vector3f(getPivot().getGravityCenter().x,
                        getPivot().getGravityCenter().y,
                        getPivot().getGravityCenter().z));

        updateViewMatrix();
    }

    public void setViewToFront() {

        project(
                new Vector3f(getPivot().getGravityCenter().x,
                        getPivot().getGravityCenter().y - getTargetDistance(),
                        getPivot().getGravityCenter().z),
                new Vector3f(getPivot().getGravityCenter().x,
                        getPivot().getGravityCenter().y,
                        getPivot().getGravityCenter().z));

        updateViewMatrix();
    }

    public void setViewToLeft() {

        setLocation(new Vector3f(
                getPivot().getGravityCenter().x - getTargetDistance(),
                getPivot().getGravityCenter().y,
                getPivot().getGravityCenter().z));

        setTarget(new Vector3f(getPivot().getGravityCenter().x,
                getLocation().y,
                getLocation().z));

        updateViewMatrix();
    }

    public void setViewToRight() {

        setLocation(new Vector3f(
                getPivot().getGravityCenter().x + getTargetDistance(),
                getPivot().getGravityCenter().y,
                getPivot().getGravityCenter().z));

        setTarget(new Vector3f(getPivot().getGravityCenter().x,
                getLocation().y,
                getLocation().z));

        updateViewMatrix();
    }

    public void setViewToBottom() {

        project(new Vector3f(getPivot().getGravityCenter().x,
                getPivot().getGravityCenter().y,
                getPivot().getGravityCenter().z - getTargetDistance()),
                new Vector3f(getPivot().getGravityCenter().x + 0.001F,
                        getPivot().getGravityCenter().y + 0.001F,
                        getPivot().getGravityCenter().z + 0.001F));

        updateViewMatrix();

        upVec = new Vector3f(0, -1, 0);
        rightVec = new Vector3f(1, 0, 0);
    }

    public void setViewToTop() {

        project(new Vector3f(getPivot().getGravityCenter().x,
                getPivot().getGravityCenter().y,
                getPivot().getGravityCenter().z + getTargetDistance()),
                new Vector3f(getPivot().getGravityCenter().x + 0.001F,
                        getPivot().getGravityCenter().y + 0.001F,
                        getPivot().getGravityCenter().z + 0.001F));

        //rightVec = new Vector3f(0, 0, 1);
        upVec = new Vector3f(0, 1, 0);
        rightVec = new Vector3f(1, 0, 0);

        updateViewMatrix();
    }

    public void setViewToOrthographic() {

        /*float objectDepth = Vector3f.dot(
                Vector3f.substract(
                        new Vector3f(voxelSpace.getCenterX(), voxelSpace.getCenterY(), voxelSpace.getCenterZ()),
                        camera.getLocation()),
                camera.getForwardVector());

        float cameraWidth = (2.0f / camera.getProjectionMatrix().mat[0]) * objectDepth;
        float cameraHeight = (2.0f / camera.getProjectionMatrix().mat[5]) * objectDepth;
            
        float ymax = (float) Math.tan(camera.getFovy() * Math.PI / 360.0f);
        float xmax = ymax * camera.getAspect();
        cameraWidth = objectDepth * xmax;
        cameraHeight = objectDepth * ymax;
        
        camera.setWidth(width);
        camera.setHeight(height);*/
        setOrthographic(getLeft(), getRight(), getTop(), getBottom(), getNearOrtho(), getFarOrtho());
    }

    public void setViewToOrthographic(float left, float right, float top, float bottom, float near, float far) {

        setOrthographic(left, right, top, bottom, near, far);
    }

    public void setViewToPerspective() {

        setPerspective(getFovy(), getAspect(), getNearPersp(), getFarPersp());
    }

    public void setViewToPerspective(float fov, float near, float far) {

        setPerspective(fov, getAspect(), near, far);
    }

    public void switchPerspective() {

        if (isPerspective()) {
            setViewToOrthographic();
        } else {
            setViewToPerspective();
        }
    }

    public Vector3f getForwardVector() {
        Vector3f forwardVector = new Vector3f();
        forwardVector.sub(target, location);
        return forwardVector;
    }

    public float getDistanceToTarget() {
        return getForwardVector().length();
    }

    public Vector3f getUpVector() {
        Vector3f upVector = new Vector3f();
        upVector.cross(rightVec, forwardVec);
        return upVector;
    }

    public Vector3f getRightVector() {

        Vector3f rightVector = new Vector3f();
        rightVector.cross(forwardVec, up);

        if (rightVector.length() == 0) {
            rightVector.x = 1;
        }

        rightVec = rightVector;

        return rightVector;
    }

    @Override
    public Vector3f getTarget() {
        return target;
    }

    @Override
    public Vector3f getLocation() {
        return location;
    }

    public void setViewportWidth(float viewportWidth) {
        this.viewportWidth = viewportWidth;
    }

    public void setViewportHeight(float viewportHeight) {
        this.viewportHeight = viewportHeight;
    }

    public float getViewportWidth() {
        return viewportWidth;
    }

    public float getViewportHeight() {
        return viewportHeight;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public SceneObject getPivot() {
        return pivot;
    }
}
