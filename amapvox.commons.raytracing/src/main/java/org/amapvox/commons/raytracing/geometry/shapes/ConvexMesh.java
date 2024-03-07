package org.amapvox.commons.raytracing.geometry.shapes;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Convex mesh class
 * @author Cresson
 *
 */
public class ConvexMesh extends TriangulatedMesh implements VolumicShape{

	public ConvexMesh (Point3d[] points, int[][] paths) {
		super (points, paths);
	}
	public ConvexMesh (Point3d[] points, int[][] paths, boolean culling) {
		super (points, paths, culling);
	}

	@Override
	public boolean contains(Point3d point) {
		for (int i = 0 ; i < paths.length ; i++) {
			Point3d p = new Point3d(points[paths[i][0]]);
			Vector3d v = new Vector3d(point.x - p.x, point.y - p.y, point.z - p.z);

			if (normals[i].dot (v) >= 0)
				return false;
		}
		return true;
	}

}
