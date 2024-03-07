package org.amapvox.canopy.mmr;

import javax.vecmath.Vector3f;



/**
 * The radiative fluxes.
 * 
 * @author J. Dauzat - April 2012
 */
public class IncidentRadiation implements DirectionList {

	public float global; // MegaJoules
	public float direct; // MJ
	public float diffuse; // MJ
	public float[] directionalDiffuse;
	public float[] directionalGlobals; // MJ
	public Vector3f[] directions;
	private final int size;

	/**
	 * Constructor
         * @param size size
	 */
	public IncidentRadiation (int size) {
		this.size = size;
		directionalGlobals = new float[size];
		directionalDiffuse = new float[size];
		directions = new Vector3f[size];
	}

        @Override
	public int getSize () {
		return size;
	}

	public float getGlobal () {
		return global;
	}

	public void setGlobal (float global) {
		this.global = global;
	}

	public float getDirect () {
		return direct;
	}

	public void setDirect (float direct) {
		this.direct = direct;
	}

	public float getDiffuse () {
		return diffuse;
	}

	public void setDiffuse (float diffuse) {
		this.diffuse = diffuse;
	}

	public float[] getDirectionalGlobals () {
		return directionalGlobals;
	}

	public void setDirectionalGlobals (float[] directionalGlobals) {
		this.directionalGlobals = directionalGlobals;
	}

	public float[] getDirectionalDiffuse() {
		return directionalDiffuse;
	}

	public Vector3f[] getDirections () {
		return directions;
	}

	public void setDirections (Vector3f[] directions) {
		this.directions = directions;
	}
/*
        @Override
	public String toString () {
		StringBuilder str = new StringBuilder ();
		// str.append ("global= "+global+ " MJ m-2");
		// str.append ("\t(direct="+direct);
		// str.append (" diffuse="+diffuse+")");
		// for (int dir=0; dir<directionalGlobals.length; dir++){
		// str.append("\ndir"+dir+"\t"+directionalGlobals[dir]);
		// }

		NumberFormat nf = DefaultNumberFormat.getInstance ();
		
		str.append ("\nGlobal radiation integrated over the period (MJ m-2)\n");
		str.append ("('hemispherical' fluxes, i.e. as measured with horizontal sensor)\n");
		str.append ("Global: " + global);
		str.append ("\t(diffuse= " + diffuse + "; direct= " + direct + ")\n");
		str.append ("global in sectors\n");
		for (int s = 0; s < size; s++) {

			Vector3f dir = directions[s];
			Point2f angles = CoordinatesConversion.cartesianToPolar (dir);

			float zenith = (float) Math.toDegrees (angles.x);
			float azimuth = (float) Math.toDegrees (angles.y);

			str.append ("Sector: " + s + " (zenith: " + nf.format (zenith) + " azimuth:" + nf.format (azimuth) + ") directionalGlobal: " + directionalGlobals[s]+"\n");
		}

		return str.toString ();
	}
*/
	@Override
	public float[] getDirectionalValues() {
		return directionalGlobals;
	}
	


}