package org.amapvox.commons.raytracing.geometry;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point4d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple3f;
import javax.vecmath.Tuple4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * Construct a 4x4 transformation matrix combining successive transformations
 * (rotations, translations and ISOTROPIC scaling).
 * The apply method applies the transformation to the tuple in argument.
 * @author Dauzat  -August 2012
 */
public class Transformations {

	Matrix4d mat;
	/**
	 *  Single precision floating transforms
	 */
	public Transformations () {
		mat = new Matrix4d ();
		mat.setIdentity ();
	}
	
	public Transformations (Vector3d translation) {
		mat = new Matrix4d ();
		mat.setIdentity ();
		setTranslation(translation);
	}
	
        
	public void setRotationAroundX (double angle) {
		Matrix4d matRot = new Matrix4d ();
		matRot.rotX (angle);
		mat.mul (matRot, mat);
	}
        
	public void setRotationAroundY (double angle) {
		Matrix4d matRot = new Matrix4d ();
		matRot.rotY (angle);
		mat.mul (matRot, mat);
	}
        
	public void setRotationAroundZ (double angle) {
		Matrix4d matRot = new Matrix4d ();
		matRot.rotZ (angle);
		mat.mul (matRot, mat);
	}
        
	public void setRotationAroundAxis (Vector3d rotAxis, double angle) {
		AxisAngle4d axisAngle = new AxisAngle4d (rotAxis, angle);
		Matrix4d matRot = new Matrix4d ();
		matRot.set (axisAngle);
		mat.mul (matRot, mat);
	}
	
        
	public void setTranslation (Vector3d translation) {
		Matrix4d matScale = new Matrix4d ();
		matScale.setIdentity ();
		matScale.setTranslation (translation);
		mat.mul (matScale, mat);
	}
	
	public void setScale (double scale) {
		Matrix4d matScale = new Matrix4d();
		matScale.setIdentity ();
		matScale.setScale (scale);
		mat.mul (matScale, mat);
	}

	public void setMatrix (Matrix4d matrix) {
		mat = matrix;
	}

	/**
	 * Applies the transformations to the tuple
	 * @param tuple (Point3f or Vector3f) to transform
	 */
	public void apply (Tuple3d tuple) {
		Tuple4d t = new Point4d (tuple.x, tuple.y, tuple.z, 1);
		mat.transform (t);
		tuple.x = t.x;
		tuple.y = t.y;
		tuple.z = t.z;

	}

	
	public Matrix4d getMatrix () {
		return mat;
	}

	//==================== statics methods ====================//
	
	public static void rotateAroundX (Tuple3f tuple, float angle) {
		Matrix3f mat = new Matrix3f ();
		mat.rotX (angle);
		mat.transform (tuple);
	}
	
	public static void rotateAroundY (Tuple3f tuple, float angle) {
		Matrix3f mat = new Matrix3f ();
		mat.rotY (angle);
		mat.transform (tuple);
	}
	
	public static void rotateAroundZ (Tuple3f tuple, float angle) {
		Matrix3f mat = new Matrix3f ();
		mat.rotZ (angle);
		mat.transform (tuple);
	}
	
	public static void rotateAroundAxis (Tuple3f tuple, Vector3f axis, float angle) {
		Matrix3f mat = new Matrix3f ();
		AxisAngle4f axisAngle = new AxisAngle4f (axis, angle);
		mat.set (axisAngle);
		mat.transform (tuple);
	}
	
	public static void translate (Tuple3f tuple, Tuple3f translation) {
		tuple.x += translation.x;
		tuple.y += translation.y;
		tuple.z += translation.z;
	}
	
	public static void scale (Tuple3f tuple, float scale) {
		tuple.scale (scale);
	}

}
