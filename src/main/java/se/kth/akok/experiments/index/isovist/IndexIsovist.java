package se.kth.akok.experiments.index.isovist;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.javasimon.SimonManager;
import org.javasimon.Stopwatch;

import com.vividsolutions.jts.geom.Point;

import se.kth.akok.index.geometries.polygon.BasicPolygon;
import se.kth.akok.index.scene.SceneBuilder;

public class IndexIsovist {
	private String sceneName;
	private Connection connection;
	private ArrayList<Point> randomPoints;

	public IndexIsovist(String sceneName, ArrayList<Point> randomPoints) {
		this.sceneName = sceneName;
		this.randomPoints = randomPoints;
	}

	public HashMap<Point, ArrayList<BasicPolygon>> computeIndexIsovist() {
		HashMap<Point, ArrayList<BasicPolygon>> isovists = new HashMap<Point, ArrayList<BasicPolygon>>();
		
		String tableIsovistName = sceneName + "_all_isovists_mem";
		String tablePointsName = "random_points_" + sceneName + "_" + randomPoints.size();
		String query = "select r.id, s.polygon_id from " + tableIsovistName + " s, " + tablePointsName + " r where ST_Within(r.way, s.isovist) ;";
		try {
			PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet results = statement.executeQuery();
			while(results.next()) {
				int pointId = results.getInt(1);
				int polygonId = results.getInt(2);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return isovists;
	}

	public static void main(String[] args) throws FileNotFoundException {
		// SceneBuilder scene = new SceneBuilder("gis","scene_generated", "indexing_scene_generated_tbl", "indexing_boundary_generated_tbl");
		SceneBuilder scene = new SceneBuilder("gis", "scene_small", "indexing_scene_small_tbl", "indexing_boundary_small_tbl");
		// SceneBuilder scene = new SceneBuilder("gis","scene_large", "indexing_scene_large_tbl", "indexing_boundary_large_tbl");

		int NUM_POINTS = 500;

		System.out.println("#Polygons: " + scene.getPolygons().size());
		System.out.println("#Points: " + scene.getPoints().size());

		Stopwatch stopwatchOuter = SimonManager.getStopwatch("index-isovist");
		ArrayList<Point> randomPoints = scene.getSceneLoader().loadRandomPoints(NUM_POINTS);

		IndexIsovist ii = new IndexIsovist(scene.getSceneLoader().getSceneName(), randomPoints);
		HashMap<Point, ArrayList<BasicPolygon>> map = ii.computeIndexIsovist();
	}

}
