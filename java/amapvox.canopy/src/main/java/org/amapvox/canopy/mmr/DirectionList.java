package org.amapvox.canopy.mmr;

import javax.vecmath.Vector3f;

/**
 * List of directions with associated values.
 * 
 * @author J. Dauzat - February 2014
 */
public interface DirectionList {

	public Vector3f[] getDirections ();
	
	public float[] getDirectionalValues () ;
	
	public int getSize ();

	

}
