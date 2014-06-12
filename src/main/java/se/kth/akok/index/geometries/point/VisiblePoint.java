package se.kth.akok.index.geometries.point;

import se.kth.akok.index.geometries.polygon.BasicPolygon;
import se.kth.akok.index.geometries.ray.Ray;

import com.vividsolutions.jts.geom.Point;

/**
 * A visible point is a point that can be seen from a given polygon point. A list of visible points is part of a polygon point.
 * 
 * @author Andreas Kokkalis
 * 
 */
public class VisiblePoint extends PolygonPoint {
	private VisiblePointType type;
	private Ray ray;

	public VisiblePoint(Point point, BasicPolygon polygon, VisiblePointType type) {
		super(point, polygon);
		setType(type);
	}

	public VisiblePointType getType() {
		return type;
	}

	public void setType(VisiblePointType type) {
		this.type = type;
	}

	public Ray getRay() {
		return ray;
	}

	public void setRay(Ray ray) {
		this.ray = ray;
	}

}
