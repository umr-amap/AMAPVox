package org.amapvox.commons.raytracing.geometry.shapes;

import org.amapvox.commons.raytracing.geometry.Intersection;
import org.amapvox.commons.raytracing.geometry.LineElement;
import java.util.ArrayList;


import javax.vecmath.Point3d;

/**
 * Volumic Shape Interface.
 * @author Cresson, Nov. 2012
 *
 */
public interface VolumicShape {
    
	boolean contains(Point3d point);
	
	/* Shape methods */
	boolean isIntersectedBy(LineElement lineElement);
	Intersection getNearestIntersection (LineElement lineElement); 
	ArrayList<Intersection> getIntersections (LineElement lineElement); 
	}
	
	