package org.amapvox.canopy.util;

import javax.vecmath.Point2f;

/**
 * Circles intersections.
 * 
 * @author J. Dauzat - May 2012
 */
public class Circle implements Cloneable {

	private float radius;
	private Point2f center;

        
	public Circle (float radius, Point2f center) {
		this.radius = radius;
		this.center = new Point2f (center);
	}

	/**
	 * Calculates the area of the intersection (lumen) with circle2
	 * (http://mathworld.wolfram.com/Circle-CircleIntersection.html)
	 * 
	 * @param circle2 circle
	 * @return lumen area
	 */
	public float intersectionCircleArea (Circle circle2) {
		float d = center.distance (circle2.center);

		return lumenArea (d, this.radius, circle2.radius);
	}

	/**
	 * Calculates the area of the intersection (lumen) between 2 circles
	 * (http://mathworld.wolfram.com/Circle-CircleIntersection.html)
	 * 
	 * @param d: distance between centers of circles
	 * @param R: radius of first circle
	 * @param r: radius of second circle
	 * @return lumen area
	 */
	public static float lumenArea (float d, float R, float r) {
		float lumenArea = 0;

		// no intersection
		if (d >= r + R) return lumenArea;

		// simplified equation in that case
		if (R == r) {
			float halfDist = d / 2;
			float h = (r * r) - (halfDist * halfDist);
			h = (float) Math.sqrt (h);
			float alpha = (float) Math.acos (halfDist / r);
			lumenArea = 2 * ((alpha * (r * r)) - (h * halfDist));

			return lumenArea;
		}

		// general case
		float d2 = d * d;
		float R2 = R * R;
		float r2 = r * r;

		// cases of one circle included in the second one
		if (d + r < R) return (float) (Math.PI * r * r);
		if (d + R < r) return (float) (Math.PI * R * R);
		// other cases
		lumenArea = (float) (r2 * Math.acos ((d2 + r2 - R2) / (2 * d * r)));
		lumenArea += (float) (R2 * Math.acos ((d2 + R2 - r2) / (2 * d * R)));
		lumenArea -= Math.sqrt ((-d + r + R) * (d + r - R) * (d - r + R) * (d + r + R)) / 2;

		return lumenArea;
	}

	public static void testsCircle () {
		float R = 5;
		float r = 3;
		Circle circle1 = new Circle (R, new Point2f (0, 0));
		Circle circle2 = new Circle (r, new Point2f (4, 0));
		System.out.println ("Area= " + circle1.intersectionCircleArea (circle2));
		System.out.println ("Area= " + Circle.lumenArea (4, R, r));
	}	
}
