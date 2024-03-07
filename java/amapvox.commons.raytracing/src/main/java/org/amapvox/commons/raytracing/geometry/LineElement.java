package org.amapvox.commons.raytracing.geometry;

import java.util.ArrayList;

import org.amapvox.commons.raytracing.geometry.shapes.Shape;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Interface for handling intersections between line elements (line, half-line or line-segment) with "Shape" objects (polygon, sphere, mesh, ...)
 * @author Dauzat/Cresson - August 2012
 */
public interface LineElement {

	public Point3d	getOrigin ();
	public Point3d	getEnd ();
	public Vector3d	getDirection ();
	public double	getLength ();

	
	/**
	 * @param shape Shape object (polygon, mesh, sphere...)
         * @return is intersecting
	 */
	public boolean doesIntersect (Shape shape);
	
	/**
	 * @param shape Shape object (polygon, mesh, sphere...)
	 * @return a List of intersections (empty list if no intersection)
	 */
	public ArrayList<Intersection> getIntersections (Shape shape);

	/**
	 * @param shape Shape object (polygon, mesh, sphere...)
	 * @return the nearest intersection from the point "origin" in the line element direction
	 */
	public Intersection getNearestIntersection (Shape shape);

	/**
	 * Translate the line element
	 * @param translation	translation vector
	 */
	public void translate(Vector3d translation);
}
