/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.math.util;

import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * Convert Matrix from vecmath to Mat
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class MatrixUtility {

    /**
     *
     * @return 4x4 identity matrix
     */
    public static Matrix4f identity4f() {
        Matrix4f identity = new Matrix4f();
        identity.setIdentity();
        return identity;
    }
    
    /**
     *
     * @return 4x4 identity matrix
     */
    public static Matrix4d identity4d() {
        Matrix4d identity = new Matrix4d();
        identity.setIdentity();
        return identity;
    }

    /**
     * Constructs and initialize a 4x4 single precision perspective matrix
     *
     * @param fovy The field of view
     * @param aspect The aspect ratio
     * @param near The near limit of the frustum
     * @param far The far limit of the frustum
     * @return A perspective single precision 4x4 matrix
     */
    public static Matrix4f perspective(float fovy, float aspect, float near, float far) {

        float top = (float) (near * Math.tan(fovy * Math.PI / 360.0));
        float right = top * aspect;
        return frustum(-right, right, -top, top, near, far);
    }

    /**
     * <p>
     * Constructs and initialize a 4x4 single precision orthographic projection
     * matrix.</p>
     * An orthographic matrix is represented by a rectangular volume.
     *
     * @param left The left limit
     * @param right The right limit
     * @param bottom The bottom limit
     * @param top The top limit
     * @param near The near limit
     * @param far The far limit
     * @return An orthographic projection 4x4 matrix
     */
    public static Matrix4f ortho(float left, float right, float bottom, float top, float near, float far) {

        Matrix4f dest = new Matrix4f();

        float rl = (right - left);
        float tb = (top - bottom);
        float fn = (far - near);

        dest.set(new float[]{
            2 / rl, 0, 0, 0,
            0, 2 / tb, 0, 0,
            0, 0, -2 / fn, 0,
            -(left + right) / rl, -(top + bottom) / tb, -(far + near) / fn, 1
        });

        return dest;
    }

    /**
     * Constructs a square frustum by the given parameters, represents this
     * frustum like a 4x4 matrix
     *
     * @param left The left limit
     * @param right The right limit
     * @param bottom The bottom limit
     * @param top The top limit
     * @param near The near limit
     * @param far The far limit
     * @return The frustum 4x4 matrix
     */
    private static Matrix4f frustum(float left, float right, float bottom, float top, float near, float far) {

        Matrix4f dest = new Matrix4f();

        float rl = (right - left);
        float tb = (top - bottom);
        float fn = (far - near);

        dest.set(new float[]{
            (near * 2) / rl, 0, 0, 0,
            0, (near * 2) / tb, 0, 0,
            (right + left) / rl, (top + bottom) / tb, -(far + near) / fn, -1,
            0, 0, -(far * near * 2) / fn, 0
        });

        return dest;
    }

    /**
     * Compute a view matrix from the world position of the camera (eye), a
     * global up vector and a target point (the point we want to look at)
     *
     * @param eye The eye position as a 3d vector
     * @param center The target position as a 3d vector
     * @param up The up direction as a 3d vector
     * @return a 4x4 single precision lookat matrix
     */
    public static Matrix4f lookAt(Vector3f eye, Vector3f center, Vector3f up) {

        if (eye.x == center.x && eye.y == center.y && eye.z == center.z) {
            return identity4f();
        }

        // z axis
        Vector3f forward = new Vector3f();
        forward.sub(eye, center);
        forward.normalize();
        // x axis
        Vector3f right = new Vector3f();
        right.cross(up, forward);
        right.normalize();
        if (right.length() == 0) {
            right.x = 1;
        }
        // y axis
        Vector3f newUp = new Vector3f();
        newUp.cross(forward, right);

        //System.out.println("up : "+newUp.x+"\t"+newUp.y+"\t"+newUp.z+"\t"+"forward : "+forward.x+"\t"+forward.y+"\t"+forward.z+"\t"+"right : "+right.x+"\t"+right.y+"\t"+right.z+"\t");
        Matrix4f result = new Matrix4f();

        float m30 = -1.f * (new Vector3f(right).dot(eye));
        float m31 = -1.f * (new Vector3f(newUp).dot(eye));
        float m32 = -1.f * (new Vector3f(forward).dot(eye));
        result.set(new float[]{
            right.x, newUp.x, forward.x, 0,
            right.y, newUp.y, forward.y, 0,
            right.z, newUp.z, forward.z, 0,
            /*translation part*/ m30, m31, m32 /*translation part*/, 1});

        return result;
    }
    
    public static float[] toFloatArray(Matrix4f matrix) {
        
        float[] elements = new float[16];

        int count = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                elements[count] = matrix.getElement(i, j);
                count++;
            }
        }
        
        return elements;
    }

    public static Matrix4d getMatrixTransformation(Vector3d point1, Vector3d point2) {

        if ((point1.x == point2.x) && (point1.y == point2.y) && (point1.z == point2.z)) {

            return new Matrix4d();
        }
        Vector2d v = new Vector2d(point1.x - point2.x, point1.y - point2.y);
        double rho = (double) Math.atan(v.x / v.y);

        Vector3d trans = new Vector3d(-point2.x, -point2.y, -point2.z);
        trans.z = 0; //no vertical translation

        Matrix4d mat4x4Rotation = new Matrix4d();
        Matrix4d mat4x4Translation = new Matrix4d();

        //rotation autour de l'axe z
        mat4x4Rotation.set(new double[]{
            (double) Math.cos(rho), (double) -Math.sin(rho), 0, 0,
            (double) Math.sin(rho), (double) Math.cos(rho), 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        });

        mat4x4Translation.set(new double[]{
            1, 0, 0, trans.x,
            0, 1, 0, trans.y,
            0, 0, 1, trans.z,
            0, 0, 0, 1
        });

        mat4x4Rotation.mul(mat4x4Translation);
        return mat4x4Rotation;
    }
}
