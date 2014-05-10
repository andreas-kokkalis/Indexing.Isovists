package se.kth.akok.index.geometries.point;

import se.kth.akok.index.geometries.polygon.BasicPolygon;

import com.vividsolutions.jts.geom.Point;

/**
 * Basic polygon point is associated with a single polygon. It contains the geometry of the point.
 * 
 * @author Andreas Kokkalis
 *
 */
public class BasicPolygonPoint {
	private Point point;
	private BasicPolygon polygon;

	public BasicPolygonPoint(Point point, BasicPolygon polygon) {
		this.point = point;
		this.polygon = polygon;
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}

	public BasicPolygon getPolygon() {
		return polygon;
	}

	public void setPolygon(BasicPolygon polygon) {
		this.polygon = polygon;
	}
}
