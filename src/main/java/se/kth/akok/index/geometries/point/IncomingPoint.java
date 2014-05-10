package se.kth.akok.index.geometries.point;

import se.kth.akok.index.geometries.line.Ray;
import se.kth.akok.index.geometries.polygon.BasicPolygon;

import com.vividsolutions.jts.geom.Point;

/**
 * Incoming point is a point on an edge of a polygon. It is actually the shadow point of another visible point that falls on an object.
 * 
 * @author Andreas Kokkalis
 *
 */
public class IncomingPoint extends BasicPolygonPoint{
	ShadowPoint shadowPoint;
	Ray ray;

	public IncomingPoint(Point point, BasicPolygon polygon) {
		super(point, polygon);
	}
	public ShadowPoint getShadowPoint() {
		return shadowPoint;
	}
	public Ray getRay() {
		return ray;
	}
	public void setShadowPoint(ShadowPoint shadowPoint) {
		this.shadowPoint = shadowPoint;
	}
	public void setRay(Ray ray) {
		this.ray = ray;
	}
	
	
}
