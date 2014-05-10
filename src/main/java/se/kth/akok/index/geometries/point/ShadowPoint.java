package se.kth.akok.index.geometries.point;

import com.vividsolutions.jts.geom.Point;

import se.kth.akok.index.geometries.polygon.BasicPolygon;

/**
 * A shadow point is the end point of the extended ray that starts from a polygon point A to a visible point B. A shadow point is on a polygon or on the boundary.
 * 
 * @author Andreas Kokkalis
 * 
 */
public class ShadowPoint extends BasicPolygonPoint {
	private ShadowPointType type;

	/**
	 * Constructs a shadow point that is on the boundary of the scene, and not an a polygon.
	 * 
	 * @param type The type of the polygon.
	 * @param point The geometry of the point.
	 */
	public ShadowPoint(ShadowPointType type, Point point) {
		super(point, null);
		this.type = type;
	}

	/**
	 * Constructs a shadow point that is on the boundary of a polygon.
	 * 
	 * @param type The type of the polygon
	 * @param point The geometry of the point.
	 * @param polygon The polygon on which the point is on.
	 */
	public ShadowPoint(ShadowPointType type, Point point, BasicPolygon polygon) {
		super(point, polygon);
		this.type = type;
	}

	public ShadowPointType getType() {
		return type;
	}

	public void setType(ShadowPointType type) {
		this.type = type;
	}
}
