/**
 * 
 */
package org.amapvox.commons.raytracing.geometry;

import javax.vecmath.Vector3d;

/**
 * information about intersection (distance and normal at the intersection)
 * @author DAUZAT/Cresson; August 2012
 */
public class Intersection implements java.lang.Comparable{

	private final Vector3d	normal;
	public	double		distance;

	public Intersection (double distance, Vector3d normal){
		this.distance	= distance;
		this.normal		= normal;
	}
	
	public double getDistance () {
		return distance;
	}
	
	public Vector3d getNormal () {
		return normal;
	}

	@Override
	public int compareTo(Object o) {
		if (((Intersection) o).distance>distance)
			return -1;
		return 1;
	}

	
}
