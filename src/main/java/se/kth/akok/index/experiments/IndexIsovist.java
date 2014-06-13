package se.kth.akok.index.experiments;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.javasimon.utils.SimonUtils;

import se.kth.akok.index.scene.SceneBuilder;

public class IndexIsovist {
//	private String sceneName;
//	private Connection connection;
//	private ArrayList<Point> randomPoints;

//	public IndexIsovist(String sceneName, ArrayList<Point> randomPoints) {
//		this.sceneName = sceneName;
//		this.randomPoints = randomPoints;
//	}

//	public HashMap<Point, ArrayList<BasicPolygon>> computeIndexIsovist() {
//		HashMap<Point, ArrayList<BasicPolygon>> isovists = new HashMap<Point, ArrayList<BasicPolygon>>();
//
//		String tableIsovistName = sceneName + "_all_isovists_mem";
//		String tablePointsName = "random_points_" + sceneName + "_" + randomPoints.size();
//		String query = "select r.id, s.polygon_id from " + tableIsovistName + " s, " + tablePointsName + " r where ST_Within(r.way, s.isovist) ;";
//		try {
//			PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//			ResultSet results = statement.executeQuery();
//			while (results.next()) {
//				int pointId = results.getInt(1);
//				int polygonId = results.getInt(2);
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//
//		return isovists;
//	}

	public static void isovistOfRandomPoints(SceneBuilder scene, int numRandomPoints, Connection connection) {
		String isovistTableName = scene.getSceneLoader().getSceneName() + "_all_isovists_mem";
		String randomPointsTable = "random_points_" + scene.getSceneLoader().getSceneName() + "_" + numRandomPoints;
		String query = "select r.id, s.polygon_id from " + isovistTableName + " s, " + randomPointsTable + " r where ST_Within(r.way, s.isovist)";
		Stopwatch stopwatch = SimonManager.getStopwatch("isovist-query_" + scene.getSceneLoader().getSceneName());
		try {
			PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			Split split = stopwatch.start();
			ResultSet results = statement.executeQuery();
			results.last();
			if (results.getRow() == 0) {
				throw new NullPointerException("Error scene: " + scene.getSceneLoader().getSceneName() + "\t random points " + numRandomPoints);
			}
			split.stop();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Result\t" + scene.getSceneLoader().getSceneName() + "\t random points " + numRandomPoints);
		System.out.println(SimonUtils.presentNanoTime(stopwatch.getTotal()) + "\n---------------------------------\n");
//		StopWatchPrinter.printStopWatch(stopwatch);
		stopwatch.reset();
	}

	private static void randomPointsTest(int numPoints) throws FileNotFoundException, SQLException {
		SceneBuilder scene1 = Scenes.testScene10();
		isovistOfRandomPoints(scene1, numPoints, scene1.getConnection().getConnection());
		
		SceneBuilder scene2 = Scenes.testScene50();
		isovistOfRandomPoints(scene2, numPoints, scene2.getConnection().getConnection());
		
		SceneBuilder scene3 = Scenes.testScene100();
		isovistOfRandomPoints(scene3, numPoints, scene3.getConnection().getConnection());
		
		SceneBuilder scene4 = Scenes.testScene200();
		isovistOfRandomPoints(scene4, numPoints, scene4.getConnection().getConnection());
		
		SceneBuilder scene5 = Scenes.testScene300();
		isovistOfRandomPoints(scene5, numPoints, scene5.getConnection().getConnection());
		
		SceneBuilder scene6 = Scenes.testScene400();
		isovistOfRandomPoints(scene6, numPoints, scene6.getConnection().getConnection());
		
		SceneBuilder scene7 = Scenes.testScene500();
		isovistOfRandomPoints(scene7, numPoints, scene7.getConnection().getConnection());
	}
	
	public static void main(String[] args) throws FileNotFoundException, SQLException {
		
//		randomPointsTest(100);
		randomPointsTest(500);
		randomPointsTest(500);
		randomPointsTest(500);

//		randomPointsTest(1000);

	}

}
