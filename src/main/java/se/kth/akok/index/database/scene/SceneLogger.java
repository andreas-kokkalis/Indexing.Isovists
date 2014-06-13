package se.kth.akok.index.database.scene;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import org.javasimon.Stopwatch;

import se.kth.akok.index.experiments.StopWatchPrinter;
import se.kth.akok.index.geometries.point.IncomingPoint;
import se.kth.akok.index.geometries.point.PolygonPoint;
import se.kth.akok.index.geometries.point.ShadowPoint;
import se.kth.akok.index.geometries.polygon.BasicPolygon;
import se.kth.akok.index.geometries.ray.Ray;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * SceneLogger stores the execution results in files, in the database and also provides an API for printing in STDOUT.
 * 
 * @author Andreas Kokkalis
 * 
 */
public class SceneLogger {
	private SceneLoader sceneLoader;
	private static String filesLocation = "/mnt/201CB79E1CB76E02/Dropbox/Studies/KTH/Thesis/database/QGis";
	private Connection connection;

	public SceneLogger(SceneLoader sceneLoader, Connection connection) {
		this.sceneLoader = sceneLoader;
		this.connection = connection;
	}

	/**
	 * Stores in the database the isovist of each polygon for the given test scene
	 * 
	 * @param polygons The list of polygons with computed isovist per polygon
	 */
	public void storeIsovists(ArrayList<BasicPolygon> polygons) {
		String tableName = sceneLoader.getSceneName() + "_all_isovists_mem";
		try {

			for (BasicPolygon polygon : polygons) {
				String viewName = "_" + tableName + "_polygon_isovist_" + polygon.getId();

				String deleteView = "DROP TABLE IF EXISTS " + viewName;
				PreparedStatement statement = connection.prepareStatement(deleteView);
				statement.execute();
				statement.close();
			}

			String delete = "DROP TABLE IF EXISTS " + tableName;
			String create = "CREATE TABLE " + tableName + " (isovist geometry, polygon_id bigint primary key)";
			String index = "create index " + tableName + "_geom_index on " + tableName + " using gist(isovist)";
			String insert = "INSERT INTO " + tableName + "(isovist, polygon_id) VALUES(ST_GeomFromText(?, ?), ?)";

			PreparedStatement statement = connection.prepareStatement(delete);
			statement.execute();
			statement.close();
			statement = connection.prepareStatement(create);
			statement.execute();
			statement.close();

			statement = connection.prepareStatement(index);
			statement.execute();
			statement.close();

			connection.setAutoCommit(false);
			statement = connection.prepareStatement(insert);
			for (BasicPolygon polygon : polygons) {
				if (polygon.getPolygonIsovist() != null) {
					statement.setObject(1, polygon.getFullIsovist().toText());
					statement.setInt(2, polygon.getGeometry().getSRID());
					statement.setObject(3, polygon.getId());
					statement.addBatch();

				}
			}
			statement.executeBatch();
			connection.commit();
			connection.setAutoCommit(true);
			storeSeperatePolygonIsovists(polygons, tableName);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stores the isovist of a polygon.
	 * 
	 * @param polygons The list of polygons
	 * @param tableName	The tableName of the scene
	 */
	private void storeSeperatePolygonIsovists(ArrayList<BasicPolygon> polygons, String tableName) {

		try {
			for (BasicPolygon polygon : polygons) {
				if (polygon.getPolygonIsovist() != null) {

					String viewName = "_" + tableName + "_polygon_isovist_" + polygon.getId();

					String insert = "CREATE TABLE " + viewName + " AS SELECT isovist, polygon_id FROM " + tableName + " where polygon_id = " + polygon.getId();
					PreparedStatement st = connection.prepareStatement(insert);

					st.executeUpdate();
					st.close();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stores an arrayList of random points in the database, for a given test scene.
	 * 
	 * @param randomPoints The list of points.
	 * @param srid
	 */
	public void storeRandomPoints(ArrayList<Point> randomPoints, int srid) {
		String tableName = "random_points_" + sceneLoader.getSceneName() + "_" + randomPoints.size();
		try {
			String delete = "DROP TABLE IF EXISTS " + tableName;
			PreparedStatement statement = connection.prepareStatement(delete);
			statement.execute();
			statement.close();
			String create = "CREATE TABLE " + tableName + " (id serial primary key, way geometry)";
			statement = connection.prepareStatement(create);
			statement.execute();
			statement.close();
			String insert = "INSERT INTO " + tableName + " (way) VALUES(ST_GeomFromText(?, ?))";
			connection.setAutoCommit(false);
			statement = connection.prepareStatement(insert);
			for (Point point : randomPoints) {
				statement.setObject(1, point.toText());
				statement.setInt(2, srid);
				statement.addBatch();
			}
			statement.executeBatch();
			connection.commit();
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// ========================================================================================================================== //
	// =================================== PRINT IN STDOUT ====================================================================== //
	// ========================================================================================================================== //

	public void printAllPolygons(ArrayList<BasicPolygon> polygons) {
		System.out.println("\n===============================================================================");
		System.out.println("SCENE NAME:\t" + sceneLoader.getSceneName());
		System.out.println("ALL POLYGONS:\t" + polygons.size() + "\n");
		for (BasicPolygon polygon : polygons)
			System.out.println(polygon.getId() + "\t" + polygon.getGeometry().toString());
		System.out.println("===============================================================================\n");
	}

	public void printAllPoints(ArrayList<PolygonPoint> points) {
		System.out.println("\n===============================================================================");
		System.out.println("SCENE NAME:\t" + sceneLoader.getSceneName());
		System.out.println("POINTS on POLYGONS:\t" + points.size() + "\n");
		for (PolygonPoint point : points)
			System.out.println(point.getPoint().toString() + "\tpolygon:\t" + point.getPolygon().getId());
		System.out.println("===============================================================================\n");

	}

	public void printPoints(PolygonPoint point, boolean visible, boolean shadow) {
		System.out.println("\n===============================================================================");
		System.out.println("Polygon: " + point.getPolygon().getId());
		if (visible) {
			System.out.println("VISIBLE POINTS:\t" + point.getVisiblePoints().size() + "\n-----------------\n" + point.getPoint() + "\n-----------------\n");
			for (PolygonPoint visiblePoint : point.getVisiblePoints())
				System.out.println(visiblePoint.getPoint().toString() + ",");
		}
		if (shadow) {
			System.out.println("\nSHADOW POINTS:\t" + point.getShadowPoints().size() + "\n-----------------");
			for (ShadowPoint shadowPoint : point.getShadowPoints())
				System.out.println(shadowPoint.getPoint().toString() + ",");
		}
		System.out.println("===============================================================================\n");
	}

	public void printIncomingPoints(BasicPolygon polygon) {
		GeometryFactory factory = new GeometryFactory();
		System.out.println("\n===============================================================================");
		System.out.println("Polygon: " + polygon.getId());
		System.out.println("\nINCOMING POINTS:\t" + polygon.getIncomingPoints().size() + "\n-----------------");
		for (IncomingPoint incomingPoint : polygon.getIncomingPoints()) {
			if (!incomingPoint.getRay().getLine().toGeometry(factory).within(polygon.getPolygonIsovist()))
				System.out.println(incomingPoint.getPoint().toString());
		}
		System.out.println("===============================================================================\n");
	}

	public void printIncomingShadowPoints(BasicPolygon polygon) {
		GeometryFactory factory = new GeometryFactory();
		System.out.println("\n===============================================================================");
		System.out.println("Polygon: " + polygon.getId());
		System.out.println("\nINCOMING SHADOW POINTS:\t" + polygon.getIncomingPoints().size() + "\n-----------------");
		for (IncomingPoint incomingPoint : polygon.getIncomingPoints()) {
			if (!incomingPoint.getRay().getLine().toGeometry(factory).within(polygon.getPolygonIsovist()))
				System.out.println(incomingPoint.getShadowPoint().getPoint().toString());
		}
		System.out.println("===============================================================================\n");
	}

	public static void printRays(PolygonPoint point, boolean visible, boolean shadow, boolean allRays) {
		System.out.println("\n===============================================================================");
		System.out.println("Polygon: " + point.getPolygon().getId());
		if (visible) {
			System.out.println("\nVISIBLE - RAYS: " + point.getVisibleRays().size() + "\n-----------------");
			for (Ray ray : point.getVisibleRays())
				System.out.println(ray.getLine().toString());
		}
		if (shadow) {
			System.out.println("\nSHADOW - RAYS: " + point.getShadowRays().size() + "\n-----------------");
			for (Ray ray : point.getShadowRays())
				System.out.println(ray.getLine().toString());
		}
		if (allRays) {
			System.out.println("\nALL RAYS: " + point.getAllRays().size() + "\n-----------------");
			for (Ray ray : point.getAllRays())
				System.out.println(point.getAllRays().indexOf(ray) + ";" + ray.getLine().toString() + ",");
		}
		System.out.println("===============================================================================\n");

	}

	public void printIncomingRays(BasicPolygon polygon) {
		System.out.println("\n===============================================================================");
		System.out.println("Polygon: " + polygon.getId());
		GeometryFactory factory = new GeometryFactory();
		System.out.println("\nOppositeRays of polygon:" + polygon.getGeometry() + "\n-----------------");
		if (!polygon.getIncomingPoints().isEmpty())
			for (IncomingPoint oppositePoint : polygon.getIncomingPoints())
				if (!oppositePoint.getRay().getLine().toGeometry(factory).within(polygon.getPolygonIsovist()))
					System.out.println(oppositePoint.getRay().getLine() + ",");
		System.out.println("===============================================================================\n");
	}

	public void printPointIsovist(BasicPolygon polygon) {
		System.out.println("\n===============================================================================");
		System.out.println("Polygon: " + polygon.getId());
		System.out.println("\nPoint Isovists of polygon: " + polygon.getGeometry() + "\n-----------------");
		for (PolygonPoint point : polygon.getPolygonPoints())
			System.out.println(point.getPointIsovist() + ",");
		System.out.println("===============================================================================\n");
	}

	public void printPolygonIsovist(BasicPolygon polygon, boolean regular, boolean incoming) {
		System.out.println("\n===============================================================================");
		System.out.println("Polygon: " + polygon.getId());
		System.out.println("\nIsovist of polygon: " + polygon.getGeometry() + "\n-----------------");
		System.out.println("Regular:\t" + polygon.getPolygonIsovist());
		System.out.println("Incoming:\t" + polygon.getIncomingIsovist());
		System.out.println("===============================================================================\n");
	}

	// ========================================================================================================================== //
	// =================================== WRITE IN FILES ======================================================================= //
	// ========================================================================================================================== //
	public void writeRays(ArrayList<BasicPolygon> polygons, boolean visible, boolean shadow, boolean allRays) throws FileNotFoundException {
		if (visible) {
			for (BasicPolygon thisPolygon : polygons) {
				PrintWriter writer = new PrintWriter(filesLocation + "/" + sceneLoader.getSceneName() + "/" + "rays_visible/" + "polygon_" + thisPolygon.getId() + " _visible_rays.txt");
				for (PolygonPoint point : thisPolygon.getPolygonPoints())
					for (Ray ray : point.getVisibleRays())
						writer.println(ray.getLine().toString());
				writer.close();
			}
		}
		if (shadow) {
			for (BasicPolygon thisPolygon : polygons) {
				PrintWriter writer = new PrintWriter(filesLocation + "/" + sceneLoader.getSceneName() + "/" + "rays_shadow/" + "polygon_" + thisPolygon.getId() + " _shadow_rays.txt");
				for (PolygonPoint point : thisPolygon.getPolygonPoints())
					for (Ray ray : point.getShadowRays())
						writer.println(ray.getLine().toString());
				writer.close();
			}
		}
		if (allRays) {
			for (BasicPolygon thisPolygon : polygons) {
				PrintWriter writer = new PrintWriter(filesLocation + "/" + sceneLoader.getSceneName() + "/" + "rays_all/" + "polygon_" + thisPolygon.getId() + " _all_rays.txt");
				for (PolygonPoint point : thisPolygon.getPolygonPoints())
					for (Ray ray : point.getAllRays())
						writer.println(point.getAllRays().indexOf(ray) + ";" + ray.getLine().toString());
				writer.close();
			}
		}
	}

	public void writeIncomingRays(ArrayList<BasicPolygon> polygons) throws FileNotFoundException {
		int sum = 0;
		for (BasicPolygon polygon : polygons) {
			sum += polygon.getId();
		}
		PrintWriter writer = new PrintWriter(filesLocation + "/" + sceneLoader.getSceneName() + "/rays_incoming/" + "incomingRays_" + sum + ".txt");
		for (BasicPolygon polygon : polygons) {
			// GeometryFactory factory = new GeometryFactory();
			// System.out.println("\nOppositeRays of polygon:" + polygon.getGeometry() + "\n-----------------");
			if (!polygon.getIncomingPoints().isEmpty()) {
				for (IncomingPoint oppositePoint : polygon.getIncomingPoints()) {
					// if (!oppositePoint.getRay().getLine().toGeometry(factory).within(polygon.getPolygonIsovist()))
					writer.println(oppositePoint.getRay().getLine());
				}
			}
		}
		writer.close();
	}

	public void writePolygonIsovist(BasicPolygon polygon, boolean regular, boolean incoming) throws FileNotFoundException {
		if (regular) {
			PrintWriter writer = new PrintWriter(filesLocation + "/" + sceneLoader.getSceneName() + "/" + "isovist_regular/" + "regular_isovist_" + polygon.getId() + ".txt");
			writer.println(polygon.getPolygonIsovist());
			writer.close();
		}
		if (incoming) {
			if (polygon.getIncomingIsovist() != null) {
				PrintWriter writer = new PrintWriter(filesLocation + "/" + sceneLoader.getSceneName() + "/" + "isovist_incoming/" + "incoming_isovist_" + polygon.getId() + ".txt");
				writer.println(polygon.getIncomingIsovist());
				writer.close();
			}
		}
	}

	public void logExecutionTime(String sceneName, Stopwatch stopwatchTotal, Stopwatch stopWatchShadow, Stopwatch stopWatchIsovist) throws FileNotFoundException {
		String location = "/mnt/201CB79E1CB76E02/Dropbox/Studies/KTH/Thesis/Experiments/";
		String fileName = "stopwatch_" + sceneName + ".txt";

		PrintWriter writer = new PrintWriter(location + fileName);
		writer.print(StopWatchPrinter.getStopWatch(stopwatchTotal));
		writer.println();
		writer.print(StopWatchPrinter.getStopWatch(stopWatchShadow));
		writer.println();
		writer.print(StopWatchPrinter.getStopWatch(stopWatchIsovist));

		writer.close();
	}
}
