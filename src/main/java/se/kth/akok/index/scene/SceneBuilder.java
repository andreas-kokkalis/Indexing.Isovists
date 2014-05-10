package se.kth.akok.index.scene;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.vividsolutions.jts.geom.Point;

import se.kth.akok.index.algorithms.isovist.Isovist;
import se.kth.akok.index.database.scene.DatabaseConnection;
import se.kth.akok.index.database.scene.RandomPointGenerator;
import se.kth.akok.index.database.scene.SceneLoader;
import se.kth.akok.index.database.scene.SceneLogger;
import se.kth.akok.index.geometries.boundary.Boundary;
import se.kth.akok.index.geometries.point.PolygonPoint;
import se.kth.akok.index.geometries.polygon.BasicPolygon;
import se.kth.akok.index.thread.executors.VisibleShadowOrderExecutor;

public class SceneBuilder {
	private ArrayList<BasicPolygon> polygons;
	private ArrayList<PolygonPoint> points;
	private Boundary boundary;
	private SceneLoader sceneLoader;
	private DatabaseConnection connection;

	// private SceneLogger sceneLogger;

	/**
	 * Initialize the scene that contains the Boundary lines, the polygons within the boundary and the polygon points.
	 */
	public SceneBuilder(String sceneName, String buildingsTable, String boundaryTable) {
		this.connection = new DatabaseConnection();
		this.sceneLoader = new SceneLoader(sceneName, buildingsTable, boundaryTable, connection.getConnection());
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

	public void run() throws FileNotFoundException {

		SceneLogger sceneLogger = new SceneLogger(sceneLoader, connection.getConnection());

		ArrayList<Thread> executionThreads = new ArrayList<Thread>();
		long startTime = System.nanoTime();
		System.out.println("started");
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
		long endTime = System.nanoTime();

		long startTime2 = System.nanoTime();

		executionThreads.clear();
		for (BasicPolygon polygon : polygons) {
			Thread t = new Thread(new Isovist(polygons, polygon));
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

		long endTime2 = System.nanoTime();
		double duration = (double) (endTime - startTime) / 1000000000.0;
		double duration2 = (double) (endTime2 - startTime2) / 1000000000.0;
		System.out.println("Total runtime: " + duration + duration2);

		sceneLogger.writeRays(polygons, true, true, true);
		sceneLogger.writeIncomingRays(polygons);
		for (BasicPolygon thisPolygon : polygons) {
			sceneLogger.writePolygonIsovist(thisPolygon, true, true);
		}
		sceneLogger.storeIsovists(polygons);
		
		RandomPointGenerator rpg = new RandomPointGenerator(polygons, boundary.getBoundaryPoints());
		ArrayList<Point> randomPoints = rpg.generatePoints(500);
		sceneLogger.storeRandomPoints(randomPoints, polygons.get(0).getGeometry().getSRID());

	}

	public static void main(String[] args) throws Exception {

//		 SceneBuilder scene = new SceneBuilder("scene_generated", "indexing_scene_generated_tbl", "indexing_boundary_generated_tbl");
//		SceneBuilder scene = new SceneBuilder("scene_small", "indexing_scene_small_tbl", "indexing_boundary_small_tbl");
		 SceneBuilder scene = new SceneBuilder("scene_large", "indexing_scene_large_tbl", "indexing_boundary_large_tbl");

		System.out.println("#Polygons: " + scene.polygons.size());
		System.out.println("#Points: " + scene.points.size());

		scene.run();
		System.out.println("ended");
	}

}
