package org.amapvox.commons.raytracing.geometry;

import java.util.ArrayList;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.amapvox.commons.raytracing.geometry.shapes.Shape;

/**
 * Straight line in 3D
 * @author Dauzat/Cresson; August 2012
 */
public class Line implements LineElement {

	protected Point3d	onePoint;
	protected Vector3d	direction;

	/**
	 * Constructor with 1 point and 1 direction
	 * @param onePoint		one point on the line
	 * @param direction		direction of the line
	 */
	public Line (Point3d onePoint, Vector3d direction) {
		this.onePoint	= new Point3d (onePoint);
		this.direction	= new Vector3d (direction);
		this.direction.normalize ();
	}

	/**
	 * Constructor with 2 points
	 * @param point1		one point on the line
	 * @param point2		one other point on the line
	 */
	public Line (Point3d point1, Point3d point2) {
		onePoint	= new Point3d (point1);
		direction	= new Vector3d (point2);
		direction.sub (point1);
		direction.normalize ();
	}

	//---------------------- Intersections ----------------------//
	@Override
	public boolean doesIntersect (Shape shape) {
		
		return shape.isIntersectedBy (this);
	}
	@Override
	public ArrayList<Intersection> getIntersections (Shape shape) {
		
		return shape.getIntersections (this);
	}
	@Override
	public Intersection getNearestIntersection (Shape shape) {
		
		return shape.getNearestIntersection (this);
	}

	
	//--------------------------- Getters ------------------------//
	@Override
	public Point3d getOrigin () {
		return new Point3d (onePoint);
	}

	@Override
	public Vector3d getDirection () {
		return new Vector3d (direction);
	}
	
	@Override
	public Point3d getEnd () {
		return null;
	}

	@Override
	public double getLength () {
		return Double.MAX_VALUE;
	}


	//---------------------- Transformations ----------------------//
	@Override
	public void translate (Vector3d translation) {
		onePoint.add (translation);
	}
	
	
	
	
	//---------------------- Old stuffs -------------------------//
//	/**
//	 * @return The shortest distance between the line and the point
//	 */
//	public float distanceToPoint (Tuple3f tuple3f) {
//
//		Vector3d vpp = new Vector3d (origin);
//		vpp.sub (tuple3f);
//
//		vpp.cross (vpp, direction);
//		
//		return vpp.length ();
//	}
//	
//	public boolean doesIntersectSphere (Sphere sphere) {
//		
//		if (distanceToPoint(sphere.getCentre()) > sphere.getRadius ())
//			return false;
//		
//		return true;
//	}
//
//	public List<Point3d> intersectionsWithSphere (Sphere sphere) {
//		
//		ArrayList<Point3d> intersections = new ArrayList<Point3d>();
//		
//		Vector3d dir = getDirection ();
//		Vector3d voc = new Vector3d (sphere.getCentre ());
//		voc.sub (getOrigin ());
//
//		float dop = voc.dot (dir);	// distance to projected point
//
//		Point3d projection = new Point3d (dir);
//		projection.scaleAdd (dop, getOrigin ());
//		
//		Vector3d vcp = new Vector3d (projection);
//		vcp.sub (sphere.getCentre ());
//
//		float dist2 = vcp.lengthSquared ();
//		float radius2 = sphere.getRadius()*sphere.getRadius();
//		
//		// no intersection
//		if (dist2 > radius2)
//			return intersections;
//		
//		float dip = (float) Math.sqrt (radius2-dist2);
//		
//		// one intersection
//		if (dip == 0) {
//			intersections.add (projection);
//			return intersections;
//		}
//		
//		// two intersections
//		Vector3d vip = new Vector3d (getDirection ());
//		vip.scale (dip);
//		Point3d nearest = new Point3d (projection);
//		nearest.sub (vip);
//		intersections.add (nearest);
//		Point3d furthest = new Point3d (projection);
//		furthest.add (vip);
//		intersections.add (furthest);
//
//		return intersections;
//	}
//	
//	@Override
//	public void translate (Tuple3f translation) {
//		// TODO Auto-generated method stub
//		
//	}
//
////	@Override
//	public void rotX (double angle) {
//		// TODO Auto-generated method stub
//		
//	}
//
////	@Override
//	public void rotY (double angle) {
//		// TODO Auto-generated method stub
//		
//	}
//
////	@Override
//	public void rotZ (double angle) {
//		// TODO Auto-generated method stub
//		
//	}
//
////	@Override
//	public void rotAroundAxis (double angle, Vector3d axis) {
//		// TODO Auto-generated method stub
//		
//	}

	
}