package se.kth.akok.index.scene;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;

import se.kth.akok.index.algorithms.isovist.Isovist;
import se.kth.akok.index.database.scene.DatabaseConnection;
import se.kth.akok.index.database.scene.SceneLoader;
import se.kth.akok.index.database.scene.SceneLogger;
import se.kth.akok.index.experiments.StopWatchPrinter;
import se.kth.akok.index.geometries.boundary.Boundary;
import se.kth.akok.index.geometries.point.PolygonPoint;
import se.kth.akok.index.geometries.polygon.BasicPolygon;
import se.kth.akok.index.thread.executors.VisibleShadowOrderExecutor;

/**
 * Scene Builder is responsible for loading the scene polygons, running the experiment and logging the results.
 * 
 * @author Andreas Kokkalis
 * 
 */
public class SceneBuilder {
	private ArrayList<BasicPolygon> polygons;
	private ArrayList<PolygonPoint> points;
	private Boundary boundary;
	private SceneLoader sceneLoader;
	private DatabaseConnection connection;
	private SceneLogger sceneLogger;

	/**
	 * Initialize the scene that contains the Boundary lines, the polygons within the boundary and the polygon points.
	 * @throws FileNotFoundException 
	 */
	public SceneBuilder(String dbName, String sceneName, String buildingsTable, String boundaryTable) throws FileNotFoundException {
		this.connection = new DatabaseConnection(dbName);
		this.sceneLoader = new SceneLoader(sceneName, buildingsTable, boundaryTable, connection.getConnection());
		this.sceneLogger = new SceneLogger(sceneLoader, connection.getConnection());
		this.polygons = this.sceneLoader.getPolygons();
		this.boundary = this.sceneLoader.getBoundary();
		this.points = this.sceneLoader.getPoints();
	}

	public ArrayList<BasicPolygon> getPolygons() {
		return polygons;
	}

	public ArrayList<PolygonPoint> getPoints() {
		return points;
	}

	/**
	 * @throws FileNotFoundException
	 * @throws SQLException
	 */
	public void buildIndex() throws FileNotFoundException, SQLException {

		ArrayList<Thread> executionThreads = new ArrayList<Thread>();

		System.out.println("started");
		Stopwatch stopwatchTotal = SimonManager.getStopwatch("total");
		Split split = stopwatchTotal.start();

		Stopwatch stopWatchShadow = SimonManager.getStopwatch("se.kth.akok.index.scene.SceneBuilder-visible-shadow-order");
		for (BasicPolygon thisPolygon : polygons) {
			for (PolygonPoint point : thisPolygon.getPolygonPoints()) {
				Thread t = new Thread(new VisibleShadowOrderExecutor(connection.getConnection(), point, polygons, boundary, sceneLoader.getBuildingsTable()));
				executionThreads.add(t);
			}
		}
		for (Thread t : executionThreads)
			t.run();
		try {
			for (Thread t : executionThreads)
				t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		StopWatchPrinter.printStopWatch(stopWatchShadow);

		Stopwatch stopWatchIsovist = SimonManager.getStopwatch("se.kth.akok.index.scene.SceneBuilder-isovist");

		executionThreads.clear();
		System.out.println("started isovist");
		for (BasicPolygon polygon : polygons) {
			Thread t = new Thread(new Isovist(polygons, polygon, sceneLoader));
			executionThreads.add(t);
		}
		for (Thread t : executionThreads)
			t.run();
		try {
			for (Thread t : executionThreads)
				t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		/*
		 * Print stop watches
		 */
		StopWatchPrinter.printStopWatch(stopWatchIsovist);

		split.stop();
		System.out.println("Total runtime: " + stopwatchTotal);

		sceneLogger.logExecutionTime(sceneLoader.getSceneName(), stopwatchTotal, stopWatchShadow, stopWatchIsovist);

		/*
		 * Log the rays
		 */
		sceneLogger.writeRays(polygons, true, true, true);
		sceneLogger.writeIncomingRays(polygons);
		for (BasicPolygon thisPolygon : polygons) {
			sceneLogger.writePolygonIsovist(thisPolygon, true, true);
		}
		sceneLogger.storeIsovists(polygons);

		connection.getConnection().close();
		sceneLoader.getLogFile().close();
	}

	public SceneLoader getSceneLoader() {
		return sceneLoader;
	}

	public Boundary getBoundary() {
		return boundary;
	}

	public DatabaseConnection getConnection() {
		return connection;
	}

	public SceneLogger getSceneLogger() {
		return this.sceneLogger;
	}

}
