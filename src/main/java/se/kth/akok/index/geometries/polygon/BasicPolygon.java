package se.kth.akok.index.geometries.polygon;

import java.util.ArrayList;
import java.util.Collection;

import se.kth.akok.index.geometries.point.IncomingPoint;
import se.kth.akok.index.geometries.point.PolygonPoint;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Point;

/**
 * Represents a polygon. Holds information about its points, and the visible area around this polygon.
 * 
 * @author Andreas Kokkalis
 * 
 */
public class BasicPolygon {
	private static double MIN_DISTANCE = 0.01;
	private Integer id;
	private Geometry geometry;
	private ArrayList<Point> points;
	private ArrayList<PolygonPoint> polygonPoints;
	private ArrayList<IncomingPoint> incomingPoints;
	private Geometry polygonIsovist;
	private Collection<Geometry> incomingIsovist;
	private Geometry fullIsovist;

	public BasicPolygon(Geometry geometry) {
		this.geometry = geometry;
		this.points = new ArrayList<Point>();
		// Initialize ArrayList with the points created by the polygon's coordinates
		GeometryFactory fact = new GeometryFactory();
		for (Coordinate coordinate : this.geometry.getCoordinates()) {
			if (!pointInPolygon(coordinate))
				points.add(fact.createPoint(coordinate));
		}
		polygonPoints = new ArrayList<PolygonPoint>();
		for (Point point : points) {
			polygonPoints.add(new PolygonPoint(point, this));
		}
		this.polygonIsovist = null;
		this.incomingIsovist = null;
		this.incomingPoints = new ArrayList<IncomingPoint>();
	}

	public Integer getId() {
		return id;
	}

	public ArrayList<Point> getPoints() {
		return points;
	}

	public ArrayList<PolygonPoint> getPolygonPoints() {
		return polygonPoints;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public Geometry getPolygonIsovist() {
		return polygonIsovist;
	}

	public Collection<Geometry> getIncomingIsovist() {
		return incomingIsovist;
	}

	public void setPolygonPoints(ArrayList<PolygonPoint> polygonPoints) {
		this.polygonPoints = polygonPoints;
	}

	public ArrayList<IncomingPoint> getIncomingPoints() {
		return incomingPoints;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setPolygonIsovist(Geometry polygonIsovist) {
		this.polygonIsovist = polygonIsovist;
	}

	public void setIncomingIsovist(Collection<Geometry> incomingIsovist) {
		this.incomingIsovist = incomingIsovist;
	}

	/**
	 * Checks whether a point is equal to a known point of the polygon, or within minimum distance of a known polygon point.
	 * 
	 * @param coordinate The coordinate of the point to check.
	 * @return True if the point is part of the polygon.
	 */
	public boolean pointInPolygon(Coordinate coordinate) {
		GeometryFactory factory = new GeometryFactory();
		for (Point point : points)
			if ((point.getX() == coordinate.x && point.getY() == coordinate.y) || point.isWithinDistance(factory.createPoint(coordinate), MIN_DISTANCE))
				return true;
		return false;
	}

	/**
	 * Checks whether an incoming point is already stored for this polygon.
	 * 
	 * @param coordinate The coordinate of the incoming point.
	 * @param ray The incoming ray.
	 * @return Returns true, if the incoming point already exists and the incoming ray already exist.
	 */
	public boolean incomingPointExists(Coordinate coordinate, LineSegment ray) {
		GeometryFactory factory = new GeometryFactory();
		for (IncomingPoint point : incomingPoints) {
			// TODO: change this
			if ((point.getPoint().getCoordinate().equals(coordinate) || point.getPoint().isWithinDistance(factory.createPoint(coordinate), MIN_DISTANCE)) && point.getRay().getLine().equals(ray))
				return true;
		}
		return false;
	}

	public Geometry getFullIsovist() {
		return fullIsovist;
	}

	public void setFullIsovist(Geometry fullIsovist) {
		this.fullIsovist = fullIsovist;
	}
}
