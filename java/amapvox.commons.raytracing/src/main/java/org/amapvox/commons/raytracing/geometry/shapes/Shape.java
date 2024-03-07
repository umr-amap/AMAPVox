package org.amapvox.commons.raytracing.geometry.shapes;

import org.amapvox.commons.raytracing.geometry.Intersection;
import org.amapvox.commons.raytracing.geometry.LineElement;
import java.util.ArrayList;


/**
 * Abstract class for simple geometry
 * @author Cresson/Dauzat, August 2012
 */
public abstract class Shape{
	
	public abstract boolean isIntersectedBy (LineElement linel);
	
	public abstract ArrayList<Intersection> getIntersections (LineElement linel);
	
	public abstract Intersection getNearestIntersection (LineElement linel);

}
