package se.kth.akok.index.geometries.point;

import se.kth.akok.index.geometries.ray.Ray;

import com.vividsolutions.jts.geom.Point;

/**
 * BoundaryPoint is a point on the boundary, usually an evelope that contains the polygons in the scene.
 * 
 * @author Andreas Kokkalis
 * 
 */
public class BoundaryPoint extends BasicPolygonPoint {
	private Ray ray;

	public BoundaryPoint(Point point) {
		super(point, null);
	}

	public Ray getRay() {
		return ray;
	}

	public void setRay(Ray ray) {
		this.ray = ray;
	}

}
