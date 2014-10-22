package se.kth.akok.index.algorithms.rayshooting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;

import se.kth.akok.index.geometries.boundary.Boundary;
import se.kth.akok.index.geometries.polygon.BasicPolygon;

import com.google.common.collect.TreeMultimap;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.distance.DistanceOp;

public class RayShooting {
	private static double MIN_DISTANCE = 0.01;

	
	private double BUFFER_RADIUS; // diagonal of the scene
	private double ANGLE_RADIANTS = (double) (2 * Math.PI) / 1440; // 0.25 degrees
	private double ANGLE_DEGREES;
	
	private HashMap<Integer, Point> randomPoints;
	private ArrayList<BasicPolygon> polygons;
	
	@SuppressWarnings("unused")
	private Boundary boundary;
	private Connection connection;
	private int sceneSRID;

	/**
	 * Constructor of RayShooting
	 * 
	 * @param randomPoints The list of random points for which the isovist must be computed.
	 * @param polygons The polygons of the scene.
	 * @param boundary The bounding box of the scene
	 * @param connection The database connection
	 */
	public RayShooting(HashMap<Integer, Point> randomPoints, ArrayList<BasicPolygon> polygons, Boundary boundary, Connection connection, double angle, double radius) {
		this.randomPoints = randomPoints;
		this.polygons = polygons;
		this.boundary = boundary;
		this.connection = connection;
		this.sceneSRID = polygons.get(1).getGeometry().getSRID();

		LineSegment diagonal = new LineSegment(boundary.getMinX().getCoordinate(0), boundary.getMaxX().getCoordinate(1));
		if(radius <=0)
			BUFFER_RADIUS = diagonal.getLength();
		else
			BUFFER_RADIUS = radius;

		if(angle >=0 ) {
			ANGLE_RADIANTS = angle;
		}
		ANGLE_DEGREES = (double) (ANGLE_RADIANTS * 180) / Math.PI;
		System.out.println("radius:\t" + BUFFER_RADIUS + " meters \tangle: " + ANGLE_DEGREES + " degrees");
	}

	/**
	 * Computes the isovist for all the provided random points.
	 * 
	 * @return A map that has as key the random point, and as value an array list that contains references to all the visible buildings for each key.
	 */
	public HashMap<Point, ArrayList<BasicPolygon>> computeRayIsovist() {
		HashMap<Point, ArrayList<BasicPolygon>> map = new HashMap<Point, ArrayList<BasicPolygon>>();
		GeometryFactory factory = new GeometryFactory();
		for (Point randomPoint : randomPoints.values()) {
			Stopwatch stopwatch = SimonManager.getStopwatch("ray-shooting-module");
			Split split = stopwatch.start();
			Geometry buffer = randomPoint.buffer(BUFFER_RADIUS);
			ArrayList<Point> bufferPoints = getBufferEndPoints(buffer);
			ArrayList<BasicPolygon> visiblePolygons = new ArrayList<BasicPolygon>();

			for (Point bufferPoint : bufferPoints) {
				LineSegment ray = new LineSegment(randomPoint.getCoordinate(), bufferPoint.getCoordinate());
				BasicPolygon visiblePolygon = setClosestIntersectedPolygon(ray.toGeometry(factory), randomPoint);
				if (!visiblePolygons.contains(visiblePolygon))
					visiblePolygons.add(visiblePolygon);
			}
			map.put(randomPoint, visiblePolygons);
			split.stop();
			Stopwatch stopwatchOuter = SimonManager.getStopwatch("ray-shooting");
			stopwatchOuter.addSplit(split);
		}
		return map;
	}

	public TreeMultimap<Integer, Integer> rayShootingIsovist() {
		TreeMultimap<Integer, Integer> isovistResults = TreeMultimap.create();

		GeometryFactory factory = new GeometryFactory();
		for (Integer randomPointId : randomPoints.keySet()) {
			Point randomPoint = randomPoints.get(randomPointId);
			Stopwatch stopwatch = SimonManager.getStopwatch("ray-shooting-module");
			Split split = stopwatch.start();
			Geometry buffer = randomPoint.buffer(BUFFER_RADIUS);
			ArrayList<Point> bufferPoints = getBufferEndPoints(buffer);

			for (Point bufferPoint : bufferPoints) {
				LineSegment ray = new LineSegment(randomPoint.getCoordinate(), bufferPoint.getCoordinate());
				BasicPolygon visiblePolygon = setClosestIntersectedPolygon(ray.toGeometry(factory), randomPoint);
				if (visiblePolygon != null) {
					if (isovistResults == null || !isovistResults.containsEntry(randomPointId, visiblePolygon.getId())) {
						isovistResults.put(randomPointId, visiblePolygon.getId());
					}
				}
			}
			split.stop();
			Stopwatch stopwatchOuter = SimonManager.getStopwatch("ray-shooting");
			stopwatchOuter.addSplit(split);
		}
		return isovistResults;
	}

	/**
	 * The function segments the buffer into tiny segments created by two consecutive rays shot from random point with angle difference ANGLE. Then it uses the postGIS
	 * function ST_DumpPoints which returns all the points which form the segmented ring.
	 * 
	 * @param buffer The buffer around the random point
	 * @return The points contained by all segments on the exterior ring of the buffer.
	 */
	private ArrayList<Point> getBufferEndPoints(Geometry buffer) {
		ArrayList<Point> bufferPoints = new ArrayList<Point>();
		String select = "select * from _isv_index_segmentize_buffer(ST_GeomFromText(?, ?),?,?)";
		try {
			PreparedStatement statement = connection.prepareStatement(select, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setObject(1, buffer.toText());
			statement.setInt(2, sceneSRID);
			statement.setDouble(3, ANGLE_RADIANTS);
			statement.setDouble(4, BUFFER_RADIUS);

			ResultSet results = statement.executeQuery();

			while (results.next()) {
				String wktGeometry = results.getString(2);
				Geometry geometry = new WKTReader().read(wktGeometry);
				Point point = geometry.getCentroid();
				bufferPoints.add(point);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return bufferPoints;
	}

	/**
	 * Computes the closest building that the ray intersects.
	 * 
	 * @param ray The ray shot from startPoint to bufferPoint
	 * @param startPoint THe startPoint (centroid) of the buffer
	 * @return The reference of the closest building which the ray intersects.
	 */
	private BasicPolygon setClosestIntersectedPolygon(LineString ray, Point startPoint) {
		GeometryFactory factory = new GeometryFactory();
		HashMap<Coordinate, BasicPolygon> allCoordinates = new HashMap<Coordinate, BasicPolygon>();
		for (BasicPolygon polygon : polygons) {
			if (ray.intersects(polygon.getGeometry()) || !rayTouchesPolygon(ray, polygon.getGeometry())) {
				Coordinate coordinates[] = ray.intersection(polygon.getGeometry()).getCoordinates();
				for (Coordinate coordinate : coordinates) {
					allCoordinates.put(coordinate, polygon);
				}
			}
		}
		Coordinate closestPoint = null;
		if (!allCoordinates.isEmpty()) {
			Double distance = Double.MAX_VALUE;
			for (Coordinate coordinate : allCoordinates.keySet()) {
				DistanceOp dist = new DistanceOp(startPoint, factory.createPoint(coordinate));
				Double newDist = new Double(dist.distance());
				if (newDist.compareTo(distance) < 0) {
					distance = newDist;
					closestPoint = coordinate;
				}
			}
		}
		return allCoordinates.get(closestPoint);
	}

	private boolean rayTouchesPolygon(LineString ray, Geometry polygon) {
		if (ray.touches(polygon))
			return true;
		if (ray.intersects(polygon) && ray.isWithinDistance(polygon, MIN_DISTANCE))
			return true;
		if (!ray.touches(polygon) && !ray.intersects(polygon) && ray.isWithinDistance(polygon, MIN_DISTANCE))
			return true;
		return false;
	}

	public double getBUFFER_RADIUS() {
		return BUFFER_RADIUS;
	}
}
