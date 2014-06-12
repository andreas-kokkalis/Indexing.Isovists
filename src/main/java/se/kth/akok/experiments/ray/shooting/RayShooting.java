package se.kth.akok.experiments.ray.shooting;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;

import se.kth.akok.experiments.stopwatch.StopWatchPrinter;
import se.kth.akok.index.geometries.boundary.Boundary;
import se.kth.akok.index.geometries.polygon.BasicPolygon;
import se.kth.akok.index.scene.SceneBuilder;

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
	private static double BUFFER_RADIUS = 500.0;
	private static double ANGLE = (double) (2 * Math.PI) / 360;
	private ArrayList<Point> randomPoints;
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
	public RayShooting(ArrayList<Point> randomPoints, ArrayList<BasicPolygon> polygons, Boundary boundary, Connection connection) {
		this.randomPoints = randomPoints;
		this.polygons = polygons;
		this.boundary = boundary;
		this.connection = connection;
		this.sceneSRID = polygons.get(1).getGeometry().getSRID();
	}

	/**
	 * Computes the isovist for all the provided random points.
	 * 
	 * @return A map that has as key the random point, and as value an array list that containes references to all the visible buildings for each key.
	 */
	public HashMap<Point, ArrayList<BasicPolygon>> computeRayIsovist() {
		HashMap<Point, ArrayList<BasicPolygon>> map = new HashMap<Point, ArrayList<BasicPolygon>>();
		GeometryFactory factory = new GeometryFactory();
		for (Point randomPoint : randomPoints) {
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

	/**
	 * The function segments the buffer into tiny segments created by two consecutive rays shot from random point with angle difference ANGLE. Then it uses the postGIS
	 * function ST_DumpPoints which returns all the points which form the segmented ring.
	 * 
	 * @param buffer The buffer arround the random point
	 * @return The points contained by all segments on the exterior ring of the buffer.
	 */
	private ArrayList<Point> getBufferEndPoints(Geometry buffer) {
		ArrayList<Point> bufferPoints = new ArrayList<Point>();
		String select = "select * from _isv_index_segmentize_buffer(ST_GeomFromText(?, ?),?,?)";
		try {
			PreparedStatement statement = connection.prepareStatement(select, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			statement.setObject(1, buffer.toText());
			statement.setInt(2, sceneSRID);
			statement.setDouble(3, ANGLE);
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
	 * Computes the closesest building that the ray intersects.
	 * 
	 * @param ray The ray shot from startPoint to bufferPoint
	 * @param startPoint THe startPoint (centroid) of the buffer
	 * @return The reference of the closest building which the ray intersects.
	 */
	private BasicPolygon setClosestIntersectedPolygon(LineString ray, Point startPoint) {
		GeometryFactory factory = new GeometryFactory();
		HashMap<Coordinate, BasicPolygon> allCoordinates = new HashMap<Coordinate, BasicPolygon>();
		for (BasicPolygon polygon : polygons) {
			if (ray.intersects(polygon.getGeometry()) || !ray.touches(polygon.getGeometry())) {
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

	public static void main(String[] args) throws FileNotFoundException {
//		SceneBuilder scene = new SceneBuilder("gis","scene_generated", "indexing_scene_generated_tbl", "indexing_boundary_generated_tbl");
//		 SceneBuilder scene = new SceneBuilder("gis","scene_small", "indexing_scene_small_tbl", "indexing_boundary_small_tbl");
		 SceneBuilder scene = new SceneBuilder("gis","scene_large", "indexing_scene_large_tbl", "indexing_boundary_large_tbl");

		System.out.println("#Polygons: " + scene.getPolygons().size());
		System.out.println("#Points: " + scene.getPoints().size());
		
		Stopwatch stopwatchOuter = SimonManager.getStopwatch("ray-shooting");
		ArrayList<Point> randomPoints = scene.getSceneLoader().loadRandomPoints(500);
		RayShooting rayShooting = new RayShooting(randomPoints, scene.getPolygons(), scene.getBoundary(), scene.getConnection().getConnection());
		HashMap<Point, ArrayList<BasicPolygon>> map = rayShooting.computeRayIsovist();
		StopWatchPrinter.printStopWatch(stopwatchOuter);
		for (Point point : map.keySet())
			System.out.println(map.get(point).size());
	}
}
