/**
 * 
 */
package se.kth.akok.index.experiments;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;

import se.kth.akok.index.scene.SceneBuilder;

/**
 * @author Andreas Kokkalis <a.kokkalis@kth.se>
 * 
 */
public class SeperateIsovistOfRandomPoints {

	public static void isovistOfRandomPoints(SceneBuilder scene, int numRandomPoints, Connection connection) {
		String isovistTableName = scene.getSceneLoader().getSceneName() + "_all_isovists_mem";
		String randomPointsTable = "random_points_" + scene.getSceneLoader().getSceneName() + "_" + numRandomPoints;
		String query = "select r.id, s.polygon_id from " + isovistTableName + " s, " + randomPointsTable + " r where ST_Within(r.way, s.isovist) and r.id=?";
		
		Stopwatch stopwatch1 = SimonManager.getStopwatch("isovist-query_random-point_" + scene.getSceneLoader().getSceneName());
		for (int i = 1; i <= numRandomPoints; i++) {
			try {
				PreparedStatement statement = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				statement.setInt(1, i);
				
				Stopwatch stopwatch = SimonManager.getStopwatch("isovist-query_random-point_"+i +"_" + scene.getSceneLoader().getSceneName());
				Split split = stopwatch.start();
				
				ResultSet results = statement.executeQuery();
				results.last();
				if (results.getRow() == 0) {
					throw new NullPointerException("Error scene: " + scene.getSceneLoader().getSceneName() + "\t random points " + numRandomPoints);
				}
				
				split.stop();
				stopwatch1.addSplit(split);
				
				results.close();
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			// System.out.println("Result\t" + scene.getSceneLoader().getSceneName() + "\t random points " + numRandomPoints);
			// System.out.println(SimonUtils.presentNanoTime(stopwatch.getTotal()) + "\n---------------------------------\n");
		}
		StopWatchPrinter.printStopWatch(stopwatch1);
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
//		randomPointsTest(10);
//		randomPointsTest(50);
		randomPointsTest(100);
//		randomPointsTest(200);
//		randomPointsTest(100);


		
	}
}
