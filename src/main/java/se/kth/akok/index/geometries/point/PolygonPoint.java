package se.kth.akok.index.geometries.point;

import java.util.ArrayList;
import java.util.LinkedList;

import se.kth.akok.index.geometries.polygon.BasicPolygon;
import se.kth.akok.index.geometries.ray.Ray;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * A polygon point has a set of visible, shadow and boundary rays. It holds information for the corresponding rays. Finally it stores the isovist of the given point.
 * 
 * @author Andreas Kokkalis
 *
 */
public class PolygonPoint extends BasicPolygonPoint {
	private static double MIN_DISTANCE = 0.01;
	private ArrayList<VisiblePoint> visiblePoints;
	private ArrayList<ShadowPoint> shadowPoints;
	private ArrayList<BoundaryPoint> boundaryPoints;
	private ArrayList<Ray> visibleRays;
	private ArrayList<Ray> shadowRays;
	private ArrayList<Ray> boundaryRays;
	private LinkedList<Ray> allRays;
	private Geometry pointIsovist;
	private boolean pointIsovistNotComputable;

	public PolygonPoint(Point point, BasicPolygon polygon) {
		super(point, polygon);

		this.visiblePoints = new ArrayList<VisiblePoint>();
		this.shadowPoints = new ArrayList<ShadowPoint>();
		this.boundaryPoints = new ArrayList<BoundaryPoint>();

		this.visibleRays = new ArrayList<Ray>();
		this.shadowRays = new ArrayList<Ray>();
		this.boundaryRays = new ArrayList<Ray>();
		this.allRays = new LinkedList<Ray>();
		pointIsovistNotComputable = false;
	}

	public ArrayList<VisiblePoint> getVisiblePoints() {
		return visiblePoints;
	}

	public ArrayList<ShadowPoint> getShadowPoints() {
		return shadowPoints;
	}

	public ArrayList<BoundaryPoint> getBoundaryPoints() {
		return boundaryPoints;
	}

	public ArrayList<Ray> getVisibleRays() {
		return visibleRays;
	}

	public ArrayList<Ray> getShadowRays() {
		return shadowRays;
	}

	public ArrayList<Ray> getBoundaryRays() {
		return boundaryRays;
	}

	public LinkedList<Ray> getAllRays() {
		return allRays;
	}

	public Geometry getPointIsovist() {
		return pointIsovist;
	}

	public void setVisiblePoints(ArrayList<VisiblePoint> visiblePoints) {
		this.visiblePoints = visiblePoints;
	}

	public void setPointIsovist(Geometry pointIsovist) {
		this.pointIsovist = pointIsovist;
	}

	public boolean isPointIsovistNotComputable() {
		return pointIsovistNotComputable;
	}

	public void setPointIsovistNotComputable(boolean pointIsovistNotComputable) {
		this.pointIsovistNotComputable = pointIsovistNotComputable;
	}

	/**
	 * Finds if the given coordinate corresponds to a known shadow point of this polygon point. If the coordinate is equal to a known shadow point coordinate, or if it is
	 * within the minimum defined distance, then it considers it the same.
	 * 
	 * @param Coordinate of the shadow point in question.
	 * @return true if the shadow point already exists for the given polygon point.
	 */
	public boolean shadowPointExists(Coordinate coordinate) {
		GeometryFactory factory = new GeometryFactory();
		for (ShadowPoint shadowPoint : shadowPoints) {
			if (shadowPoint.getPoint().getCoordinate().equals(coordinate) || shadowPoint.getPoint().isWithinDistance(factory.createPoint(coordinate), MIN_DISTANCE))
				return true;
		}
		return false;
	}
	
	/**
	 * Finds if the given coordinate corresponds to a visible point already defined for this polygon point. If the coordinate is equal to a known visible point
	 * coordinate, or if it is within the minimum defined distance, then it considers it the same.
	 * 
	 * @param coordinate
	 * @return true if the coordinate already exists as a visible point.
	 */
	public boolean isVisiblePoint(Coordinate coordinate) {
		GeometryFactory factory = new GeometryFactory();
		for (VisiblePoint visiblePoint : visiblePoints)
			if (visiblePoint.getPoint().getCoordinate().equals(coordinate) || visiblePoint.getPoint().isWithinDistance(factory.createPoint(coordinate), MIN_DISTANCE))
				return true;
		return false;
	}
}
