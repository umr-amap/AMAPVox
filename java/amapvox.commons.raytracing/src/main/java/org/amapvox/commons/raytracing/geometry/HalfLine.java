package org.amapvox.commons.raytracing.geometry;

import java.util.ArrayList;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.amapvox.commons.raytracing.geometry.shapes.Shape;


/**
 * Half-line defined by a point and a direction
 * @author Dauzat/Cresson; August 2012
 */
public class HalfLine implements LineElement{

	private Vector3d	direction;
	private Point3d		origin;

	/**
	 * Constructor with 1 point, 1 direction
	 * @param origin		origin of the half-line
	 * @param direction		direction of the half-line
	 */
	public HalfLine (Point3d origin, Vector3d direction) {
		this.direction	= new Vector3d(direction);
		this.origin		= new Point3d(origin);
	}

	/**
	 * Constructor with 2 points
	 * @param startPoint	origin of the half-line
	 * @param onePoint		one point on the half-line
	 */
	public HalfLine (Point3d startPoint, Point3d onePoint) {
		Vector3d direction = new Vector3d (onePoint);
		direction.sub (startPoint);
		direction.normalize ();
		this.direction 	= new Vector3d(direction);
		this.origin		= new Point3d(startPoint);
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
	
	@Override
	public void translate (Vector3d translation) {
		origin.add (translation);
	}
	
	//--------------------------- Getters ------------------------//
	@Override
	public Point3d getOrigin () {
		return new Point3d(origin);
	}

	@Override
	public Point3d getEnd () {
		return null;
	}

	@Override
	public Vector3d getDirection () {
		return new Vector3d(direction);
	}

	@Override
	public double getLength () {
		return Double.MAX_VALUE;
	}

	
	
	//--------------------------- Old stuffs ------------------------//
//	/**
//	 * @return The shortest distance between 
//	 * <li>the point and the line 
//	 * <li>or the distance between the point and the half-line origin
//	 */
////	@Override
//	public float distanceToPoint (Point3d point) {
//		
//		Vector3d dir= super.getDirection ();
//
//		Vector3d vop= new Vector3d (point);
//		vop.sub (super.getOrigin ());
//
//		float dop = vop.dot (dir);	// distance to projected point
//
//		if (dop < 0)
//			return vop.length ();
//
//		Vector3d vpn = new Vector3d (dir);
//		dir.scaleAdd (dop, getOrigin ());
//		vpn.sub(point);
//		
//		return vpn.length ();
//	}
//	
//	public List<Point3d> intersectionsWithSphere (Sphere sphere) {
//		
//		ArrayList<Point3d> intersections = new ArrayList<Point3d>();
//		
//		return intersections;
//	}
//
//	@Override
//	public boolean doesIntersectSphere (Sphere sphere) {
//
//		Vector3d dir= super.getDirection ();
//
//		Vector3d vop= new Vector3d (sphere.getCentre ());
//		vop.sub (super.getOrigin ());
//
//		float dop = vop.dot (dir);	// distance to projected point
//
//		if (dop < 0)
//			return false;
//
//		Vector3d vcn = new Vector3d (dir);
//		vcn.scaleAdd (dop, getOrigin ());
//		vcn.sub (sphere.getCentre ());
//
//		float dist = vcn.length ();
//
//		if (dist > sphere.getRadius ())
//			return false;
//
//		return true;
//	}
//
//	public String toString () {
//		
//		return ("origin: "+getOrigin ()+"\tdirection: "+getDirection ());
//	}

}
