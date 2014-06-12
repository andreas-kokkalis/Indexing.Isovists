package se.kth.akok.index.geometries.ray;

import se.kth.akok.index.geometries.point.BasicPolygonPoint;

import com.vividsolutions.jts.geom.LineSegment;

/**
 * A ray represents a line segment that is a visible or shadow or incoming ray.
 * 
 * @author Andreas Kokkalis
 *
 */
public class Ray {
	LineSegment line;
	RayType rayType;
	Double angle;
	BasicPolygonPoint endPoint;

	public Ray(LineSegment ray, RayType rayType, BasicPolygonPoint endPoint) {
		super();
		this.line = ray;
		this.rayType = rayType;
		this.endPoint = endPoint;
	}

	public Double getAngle() {
		return angle;
	}

	public void setAngle(Double angle) {
		this.angle = angle;
	}

	public LineSegment getLine() {
		return line;
	}

	public RayType getRayType() {
		return rayType;
	}

	public void setLine(LineSegment ray) {
		this.line = ray;
	}

	public void setRayType(RayType rayType) {
		this.rayType = rayType;
	}

	public BasicPolygonPoint getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(BasicPolygonPoint endPoint) {
		this.endPoint = endPoint;
	}

}
