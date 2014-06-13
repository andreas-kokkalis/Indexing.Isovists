/**
 * 
 */
package se.kth.akok.index.experiments;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;

import com.vividsolutions.jts.geom.Point;

import se.kth.akok.index.database.scene.RandomPointGenerator;
import se.kth.akok.index.scene.SceneBuilder;

/**
 * @author Andreas Kokkalis <a.kokkalis@kth.se>
 * 
 */
public class CreateRandomPoints {
	public static void testScene10(int numPoints) throws FileNotFoundException, SQLException {
		SceneBuilder scene = Scenes.testScene10();
		RandomPointGenerator rpg = new RandomPointGenerator(scene.getPolygons(), scene.getBoundary().getBoundaryPoints());
		ArrayList<Point> randomPoints = rpg.generatePoints(numPoints);
		scene.getSceneLogger().storeRandomPoints(randomPoints, scene.getPolygons().get(0).getGeometry().getSRID());
		System.out.println("Created " + numPoints + " random points for the scene " + scene.getSceneLoader().getSceneName());
	}

	public static void testScene50(int numPoints) throws FileNotFoundException, SQLException {
		SceneBuilder scene = Scenes.testScene50();
		RandomPointGenerator rpg = new RandomPointGenerator(scene.getPolygons(), scene.getBoundary().getBoundaryPoints());
		ArrayList<Point> randomPoints = rpg.generatePoints(numPoints);
		scene.getSceneLogger().storeRandomPoints(randomPoints, scene.getPolygons().get(0).getGeometry().getSRID());
		System.out.println("Created " + numPoints + " random points for the scene " + scene.getSceneLoader().getSceneName());
	}

	public static void testScene100(int numPoints) throws FileNotFoundException, SQLException {
		SceneBuilder scene = Scenes.testScene100();
		RandomPointGenerator rpg = new RandomPointGenerator(scene.getPolygons(), scene.getBoundary().getBoundaryPoints());
		ArrayList<Point> randomPoints = rpg.generatePoints(numPoints);
		scene.getSceneLogger().storeRandomPoints(randomPoints, scene.getPolygons().get(0).getGeometry().getSRID());
		System.out.println("Created " + numPoints + " random points for the scene " + scene.getSceneLoader().getSceneName());
	}

	public static void testScene200(int numPoints) throws FileNotFoundException, SQLException {
		SceneBuilder scene = Scenes.testScene200();
		RandomPointGenerator rpg = new RandomPointGenerator(scene.getPolygons(), scene.getBoundary().getBoundaryPoints());
		ArrayList<Point> randomPoints = rpg.generatePoints(numPoints);
		scene.getSceneLogger().storeRandomPoints(randomPoints, scene.getPolygons().get(0).getGeometry().getSRID());
		System.out.println("Created " + numPoints + " random points for the scene " + scene.getSceneLoader().getSceneName());
	}

	public static void testScene300(int numPoints) throws FileNotFoundException, SQLException {
		SceneBuilder scene = Scenes.testScene300();
		RandomPointGenerator rpg = new RandomPointGenerator(scene.getPolygons(), scene.getBoundary().getBoundaryPoints());
		ArrayList<Point> randomPoints = rpg.generatePoints(numPoints);
		scene.getSceneLogger().storeRandomPoints(randomPoints, scene.getPolygons().get(0).getGeometry().getSRID());
		System.out.println("Created " + numPoints + " random points for the scene " + scene.getSceneLoader().getSceneName());
	}

	public static void testScene400(int numPoints) throws FileNotFoundException, SQLException {
		SceneBuilder scene = Scenes.testScene400();
		RandomPointGenerator rpg = new RandomPointGenerator(scene.getPolygons(), scene.getBoundary().getBoundaryPoints());
		ArrayList<Point> randomPoints = rpg.generatePoints(numPoints);
		scene.getSceneLogger().storeRandomPoints(randomPoints, scene.getPolygons().get(0).getGeometry().getSRID());
		System.out.println("Created " + numPoints + " random points for the scene " + scene.getSceneLoader().getSceneName());
	}

	public static void testScene500(int numPoints) throws FileNotFoundException, SQLException {
		SceneBuilder scene = Scenes.testScene500();
		RandomPointGenerator rpg = new RandomPointGenerator(scene.getPolygons(), scene.getBoundary().getBoundaryPoints());
		ArrayList<Point> randomPoints = rpg.generatePoints(numPoints);
		scene.getSceneLogger().storeRandomPoints(randomPoints, scene.getPolygons().get(0).getGeometry().getSRID());
		System.out.println("Created " + numPoints + " random points for the scene " + scene.getSceneLoader().getSceneName());
	}

	public static void main(String[] args) throws FileNotFoundException, SQLException {
		// 100 random points
//		CreateRandomPoints.testScene10(100);
//		CreateRandomPoints.testScene50(100);
//		CreateRandomPoints.testScene100(100);
//		CreateRandomPoints.testScene200(100);
//		CreateRandomPoints.testScene300(100);
//		CreateRandomPoints.testScene400(100);
//		CreateRandomPoints.testScene500(100);

		// 1000 random points
		CreateRandomPoints.testScene10(500);
		CreateRandomPoints.testScene50(500);
		CreateRandomPoints.testScene100(500);
		CreateRandomPoints.testScene200(500);
		CreateRandomPoints.testScene300(500);
		CreateRandomPoints.testScene400(500);
		CreateRandomPoints.testScene500(500);
		
		// 1000 random points
//		CreateRandomPoints.testScene10(1000);
//		CreateRandomPoints.testScene50(1000);
//		CreateRandomPoints.testScene100(1000);
//		CreateRandomPoints.testScene200(1000);
//		CreateRandomPoints.testScene300(1000);
//		CreateRandomPoints.testScene400(1000);
//		CreateRandomPoints.testScene500(1000);
	}
}
