package org.amapvox.commons.raytracing.geometry.shapes;

import org.amapvox.commons.raytracing.geometry.HalfLine;
import org.amapvox.commons.raytracing.geometry.Intersection;
import org.amapvox.commons.raytracing.geometry.Line;
import org.amapvox.commons.raytracing.geometry.LineElement;
import org.amapvox.commons.raytracing.geometry.LineSegment;
import java.util.ArrayList;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Triangle-fashioned mesh
 * @author Cresson, sept. 2012
 */
public class TriangulatedMesh extends Shape{

	protected Point3d[]		points;
	protected Vector3d[]	normals;
	protected int[][]		paths;
	protected boolean		culling;
	
	/*
	 * Private class used to manage the intersections computation
	 */
	private class IntersectionContext {
		public boolean intersect;
		public double   length;
		public IntersectionContext(boolean intersect, double length) {
			this.intersect	= intersect;
			this.length		= length;
		}
	}
	
	/**
	 * Constructor with points array and paths
	 * @param points	points array
	 * @param paths		paths
	 */
	public TriangulatedMesh(Point3d[] points, int[][] paths) {
		this(points,paths,false);
	}
	
	/**
	 * Constructor with points array, paths and culling option
	 * @param points	points array
	 * @param paths		paths
	 * @param culling	culling opt. When sets to true, the intersection happens only when face_normal[dot]ray greater than 0
	 */
	public TriangulatedMesh(Point3d[] points, int[][] paths, boolean culling) {
		this.points		= points;
		this.paths		= paths;
		this.culling	= culling;
		this.normals	= new Vector3d[paths.length];
		computeNormals();
	}
	
	public Point3d[] getPoints(){
		return points;
	}
	
	public int[][] getPaths(){
		return paths;
	}
	
	@Override
	public boolean isIntersectedBy (LineElement linel) {
		for (int i = 0 ; i < paths.length ; i++) {
			if (isIntersectedBy(i,linel))
				return true;
		}
		return false;
	}

	@Override
	public ArrayList<Intersection> getIntersections (LineElement linel) {
		ArrayList<Intersection> intersections = new ArrayList<Intersection>();
		for (int i = 0 ; i < paths.length ; i++) {
			Intersection intersection = getTriangleIntersection(i, linel);
			if (intersection!=null)
				intersections.add (intersection);
		}
		return intersections;
	}

	@Override
	public Intersection getNearestIntersection (LineElement linel) {
		
		Intersection nearestIntersection = null;
		double distance = Double.MAX_VALUE;
		for (int i = 0 ; i < paths.length ; i++) {
			Intersection intersection = getTriangleIntersection(i, linel);
			if (intersection!=null) {
				double intersectionDistance = intersection.getDistance ();
				if (intersectionDistance < distance) {
					distance = intersectionDistance;
					nearestIntersection = intersection;
				}
			}
		}
		return nearestIntersection;
	}
	
	//////////////////////////////////// TRIANGLE ROUTINES //////////////////////////////////////
	
	/*
	 * Computes normal of all triangles
	 */
	private void computeNormals() {
		for (int i = 0 ; i < paths.length ; i++) {
			computeNormal(i);
		}
	}
	
	/*
	 * Computes normal of the triangle
	 * 
	 * @param tri	triangle indice
	 */
	private void computeNormal(int tri) {
		Point3d point0 = points[paths[tri][0]];
		Point3d point1 = points[paths[tri][1]];
		Point3d point2 = points[paths[tri][2]];
		Vector3d edge1 = new Vector3d(point1.x - point0.x,point1.y - point0.y,point1.z - point0.z);
		Vector3d edge2 = new Vector3d(point2.x - point1.x,point2.y - point1.y,point2.z - point1.z);
		Vector3d normal  = new Vector3d();
		normal.cross (edge1, edge2);
		normal.normalize ();
		this.normals[tri] = normal;
	}

	/*
	 * Computes intersection Triangle-Line.
	 * 
	 * @param	tri							triangle indice
	 * @param	direction					direction of the line element
	 * @param	origin						origin of the line element
	 * @param	cullingDesired				true if result depends of triangle orientation (i.e. if triangle is back, there is no intersection). Faster code.
	 * @param	allowNegativeLengths		true if result depends of line orientation (i.e. allow intersection point to be back to the line element origin).
	 * @return	IntersectionContext (length: distance between ray origin and ray impact, boolean: true if there is an intersection, false if not)
	 */
	private IntersectionContext computeIntersection (int tri, Vector3d direction, Point3d origin, boolean cullingDesired, boolean allowNegativeLengths) {
		/* **************************************************************************************************
			Following code inspired by the paper :
			"Fast, Minimum Storage Ray/Triangle intersection" from Ben Trumbore & Thomas Möller
			(Program of Computer Graphics, Cornell University / Prosolvia Clarus AB, Chalmers University of Technology)
		
			Link: http://www.graphics.cornell.edu/pubs/1997/MT97.html
		****************************************************************************************************/
		
		          // default length
            double length = Float.MAX_VALUE;

            // find triangle vertex
            Point3d point0 = points[paths[tri][0]];
            Point3d point1 = points[paths[tri][1]];
            Point3d point2 = points[paths[tri][2]];

            // find vectors for two edges sharing vertex0
            Vector3d edge1 = new Vector3d(point1.x - point0.x,
                    point1.y - point0.y,
                    point1.z - point0.z);
            Vector3d edge2 = new Vector3d(point2.x - point0.x,
                    point2.y - point0.y,
                    point2.z - point0.z);

            // begin computing determinant - also used to calculate u
            Vector3d pvec = new Vector3d();
            pvec.cross(direction, edge2);

            // if determinant is zero, ray lies in plane of triangle
            double det = edge1.dot(pvec);
            if (cullingDesired == true) { // if culling is desired
                if (det < 0) {
                    return new IntersectionContext(false, length);
                }

                // calculate distance from vert0 to ray origin
                Vector3d tvec = new Vector3d();
                tvec.sub(origin, point0);

                // calculate u and test bounds
                double u = tvec.dot(pvec);
                if (u < 0 | u > det) {
                    return new IntersectionContext(false, length);
                }

                // prepare to test v
                Vector3d qvec = new Vector3d();
                qvec.cross(tvec, edge1);

                // computing v and test bounds
                double v = qvec.dot(direction);
                if (v < 0 | u + v > det) {
                    return new IntersectionContext(false, length);
                }

                // calculate length, ray intersects triangle
                length = edge2.dot(qvec);
                length /= det;
            } else { // the non-culling branch
                if (det == 0) {
                    return new IntersectionContext(false, length);
                }

                // invert of determinant
                double inv_det = 1.0 / det;

                // calculate distance from vert0 to ray origin
                Vector3d tvec = new Vector3d();
                tvec.sub(origin, point0);

                // calculate u and test bounds
                double u = tvec.dot(pvec);
                u *= inv_det;
                if (u < 0.0 | u > 1.0) {
                    return new IntersectionContext(false, length);
                }

                // prepare to test v
                Vector3d qvec = new Vector3d();
                qvec.cross(tvec, edge1);

                // calculate v and test bounds
                double v = qvec.dot(direction);
                v *= inv_det;
                if (v < 0.0 | u + v > 1.0) {
                    return new IntersectionContext(false, length);
                }

                // calculate length, ray intersects triangle
                length = edge2.dot(qvec);
                length *= inv_det;
            }
            if (allowNegativeLengths == true) {
                return new IntersectionContext(true, length);
            } else if (length > 0.0) {
                return new IntersectionContext(true, length);
            }
            return new IntersectionContext(false, length);
		
	}
	
	/*
	 * Tests if triangle is intersected by the specified line element
	 * 
	 * @param	tri						triangle indice
	 * @param	lineElement				line element
	 * @return	true if triangle is intersected by the line element. false if not.
	*/
	private boolean isIntersectedBy (int tri, LineElement lineElement) {
		
		if (lineElement instanceof Line) {
			// allow negative distances
			return computeIntersection(tri, lineElement.getDirection(), lineElement.getOrigin (), culling, true).intersect;
		}
		else if (lineElement instanceof HalfLine) {
			// dont allow negative distances
			return computeIntersection(tri, lineElement.getDirection(), lineElement.getOrigin (), culling, false).intersect;
		}
		// dont allow negative distance and compare length vs distance
		IntersectionContext intersectionContext = computeIntersection(tri, lineElement.getDirection(), lineElement.getOrigin (), culling, false);
		if (lineElement.getLength () > intersectionContext.length & intersectionContext.intersect == true)
			return true;
		return false;
	}
	
	/*
	 *  Manage intersectionContext to return a correct Intersection 
	 * (depending on instance of lineElement, e.g. test for segment is different from test for half-line)
	 * 
	 * @param	tri							triangle indice
	 * @param	lineElement					line element
	 * @param	allowNegativeLengths		allow intersection point to be back to the line element origin
	 * @return	computed intersection (null if empty)
	 */
	private Intersection computeIntersection (int tri, LineElement lineElement, boolean allowNegativeLengths) {
		IntersectionContext intersectionContext = computeIntersection(tri, lineElement.getDirection(), lineElement.getOrigin (), culling, allowNegativeLengths);
		if (intersectionContext.intersect) {
			double distance = intersectionContext.length; 
			//boolean side = linel.getDirection ().dot (normal)>0;
			//return new Intersection(point,normal,side);
			if (lineElement instanceof LineSegment)
				if (lineElement.getLength () < intersectionContext.length)
					return null;
			return new Intersection(distance,normals[tri]);
		}
		return null;
	}
	
	/*
	 * Get the intersection of the triangle with the line element
	 * 
	 * @param	tri							triangle indice
	 * @param	lineElement					line element
	 * @return	Intersection
	*/
	private Intersection getTriangleIntersection (int tri, LineElement lineElement) {
		if (lineElement instanceof LineSegment | lineElement instanceof HalfLine) {
			// dont allow negative distances
			return computeIntersection(tri, lineElement,false);
		}
		// allow negative distances
		return computeIntersection(tri, lineElement,true);	
	}
}
