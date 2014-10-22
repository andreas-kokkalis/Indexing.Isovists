/**
 * 
 */
package se.kth.akok.index.experiments;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.javasimon.SimonManager;
import org.javasimon.Stopwatch;

import se.kth.akok.index.algorithms.rayshooting.RayShooting;
import se.kth.akok.index.geometries.polygon.BasicPolygon;
import se.kth.akok.index.scene.SceneBuilder;

import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;
import com.google.common.collect.Sets.SetView;
import com.vividsolutions.jts.geom.Point;

/**
 * @author Andreas Kokkalis <a.kokkalis@kth.se>
 * 
 */
public class RayShootingVsIsoVist {

	/**
	 * Compares logs and prints the results of the two methods.
	 * 
	 * @param rayShootingIsovist The treemap with the results for rayShooting
	 * @param indexIsovist The treemap with results for using the index
	 * @param scene The scene
	 * @param stopWatchIndex
	 * @param stopWatchRayShooting
	 * @throws FileNotFoundException
	 */
	private static void compareResults(TreeMultimap<Integer, Integer> rayShootingIsovist, TreeMultimap<Integer, Integer> indexIsovist, SceneBuilder scene, double angle, double radius, Stopwatch stopWatchRayShooting, Stopwatch stopWatchIndex)
			throws FileNotFoundException {
		int numRandPoints = indexIsovist.asMap().keySet().size();
		double angleDegree = (double) (angle * 180) / Math.PI;
		String location = "/home/andrew/Desktop/test/";
		String experimentName = scene.getSceneLoader().getSceneName() + "-" + "points_" + numRandPoints + "-" + "angle_" + 2 * angleDegree + "-" + "rayVsIndex";
		PrintWriter writer = new PrintWriter(location + experimentName);

		writer.println("========================================================");
		writer.println("\t\tExperiment");
		writer.println("========================================================");
		writer.println("Scene:\t" + scene.getSceneLoader().getSceneName());
		writer.println("Random points:\t" + numRandPoints);
		writer.println("Angle in degrees:\t" + 2 * angleDegree); // Angle degree is half.
		writer.println("Angle in radiands:\t" + 2 * angle); // Angle degree is half.
		writer.println("Radius / Diagonal:\t" + radius);
		writer.println("========================================================");
		writer.println("\n\n");
		writer.println("========================================================");
		writer.println("\t\tStatistics");
		writer.println("========================================================");

		int equalResults = 0, totalNotInRay = 0, totalNotInIndex = 0;

		// Set of unique building ids not contained in the other method's results.
		HashSet<Integer> notInRaySet = new HashSet<Integer>();
		HashSet<Integer> notInIndexSet = new HashSet<Integer>();
		int maxNotInIndex = 0, maxNotInRay = 0;
		StringBuilder sb = new StringBuilder();
		for (Integer randomPoint : indexIsovist.asMap().keySet()) {

			Set<Integer> visibleFromIndex = (Set<Integer>) indexIsovist.asMap().get(randomPoint);
			Set<Integer> visibleFromRays = (Set<Integer>) rayShootingIsovist.asMap().get(randomPoint);

			SetView<Integer> symmetricDifference = Sets.symmetricDifference(visibleFromIndex, visibleFromRays);
			SetView<Integer> diffIndexNotInRay = Sets.difference(visibleFromIndex, visibleFromRays);
			SetView<Integer> diffRayNotInIndex = Sets.difference(visibleFromRays, visibleFromIndex);

			// Total number of missed buildings per method.
			totalNotInRay += diffIndexNotInRay.size();
			totalNotInIndex += diffRayNotInIndex.size();

			// Maximum number of missed buildings per method.
			if (diffIndexNotInRay.size() > maxNotInRay)
				maxNotInRay = diffIndexNotInRay.size();
			if (diffRayNotInIndex.size() > maxNotInIndex)
				maxNotInIndex = diffRayNotInIndex.size();

			// Unique buildings id's missed by each method.
			for (Integer i1 : diffIndexNotInRay)
				if (!notInRaySet.contains(i1))
					notInRaySet.add(i1);
			for (Integer i2 : diffRayNotInIndex) {
				if (!notInIndexSet.contains(i2))
					notInIndexSet.add(i2);
			}

			if (!symmetricDifference.isEmpty()) {
				sb.append("RanPoint: " + randomPoint + "\t|\t" + symmetricDifference.toString() + "\t|\t Index not in Ray: " + diffIndexNotInRay.toString() + "\t|\t Ray not in Index: " + diffRayNotInIndex.toString()).append("\n");
				System.out.println("RandomPoint: " + randomPoint + "\t|\t" + symmetricDifference.toString() + "\t|\t Index not in Ray: " + diffIndexNotInRay.toString() + "\t|\t Ray not in Index: " + diffRayNotInIndex.toString());
			} else
				equalResults++;
		}
		System.out.println("Same: " + equalResults + "\t Different: " + (numRandPoints - equalResults));
		System.out.println("Not in Ray: " + totalNotInRay + "\t Not in Index: " + totalNotInIndex);
		System.out.println("#Unique buildings Not in Ray: " + notInRaySet.size() + "\t #Unique buildings Not in Index: " + notInIndexSet.size());

		writer.println("\n-------------------------------------------------------------------------");
		writer.println("Execution time index:");
		writer.print(StopWatchPrinter.printStopWatchString(stopWatchIndex));
		writer.println("-------------------------------------------------------------------------");
		writer.println("Execution time Ray shooting:");
		writer.print(StopWatchPrinter.printStopWatchString(stopWatchRayShooting));
		writer.println("-------------------------------------------------------------------------");
		writer.println("Ray shooting size:\t" + rayShootingIsovist.size());
		writer.println("Index size:\t" + indexIsovist.size());
		writer.println("-------------------------------------------------------------------------");
		writer.println("Total equal:" + equalResults);
		writer.println("Total different:" + (numRandPoints - equalResults));
		writer.println("-------------------------------------------------------------------------");
		writer.println("Total not in Ray:\t" + totalNotInRay);
		writer.println("Total not in Index:\t" + totalNotInIndex);
		writer.println("-------------------------------------------------------------------------");
		writer.println("Unique not in Ray:\t" + notInRaySet.size());
		writer.println("Unique not in Index:\t" + notInIndexSet.size());
		writer.println("-------------------------------------------------------------------------");
		writer.println("Maximum not in Ray:\t" + maxNotInRay);
		writer.println("Maximum not in Index:\t" + maxNotInIndex);
		writer.println("-------------------------------------------------------------------------");

		writer.println("\n\n");
		writer.println("========================================================");
		writer.println("\t\tDetailed Differences");
		writer.println("========================================================");
		writer.print(sb.toString());

		writer.println("\n\n");
		writer.println("========================================================");
		writer.println("\t\tResults Ray shooting");
		writer.println("========================================================");

		Set<Entry<Integer, Collection<Integer>>> set = rayShootingIsovist.asMap().entrySet();
		for (Entry<Integer, Collection<Integer>> entry : set) {
			Collection<Integer> visiblePolygons = (Collection<Integer>) entry.getValue();
			for (Integer polygonId : visiblePolygons) {
				for (BasicPolygon polygon : scene.getPolygons())
					if (polygon.getId().equals(polygonId)) {
						writer.println(entry.getKey() + ";" + polygonId + ";" + polygon.getGeometry().toString());
						break;
					}
			}
		}
		writer.println("\n\n");
		writer.println("========================================================");
		writer.println("\t\tResults Index");
		writer.println("========================================================");
		Set<Entry<Integer, Collection<Integer>>> set2 = indexIsovist.asMap().entrySet();
		for (Entry<Integer, Collection<Integer>> entry : set2) {
			Collection<Integer> visiblePolygons = (Collection<Integer>) entry.getValue();
			for (Integer polygonId : visiblePolygons) {
				for (BasicPolygon polygon : scene.getPolygons())
					if (polygon.getId().equals(polygonId)) {
						writer.println(entry.getKey() + ";" + polygonId + ";" + polygon.getGeometry().toString());
						break;
					}
			}
		}
		writer.close();
	}

	public static void main(String[] args) throws FileNotFoundException, SQLException {
		// scene10((double) (2 * Math.PI) / 2880); // ground truth scene 10
		// scene10((double) (2 * Math.PI) / 1440);
		// scene10((double) (2 * Math.PI) / 720);
		// scene10((double) (2 * Math.PI) / 360);
		// scene10((double) (2 * Math.PI) / 180);
		// scene10((double) (2 * Math.PI) / 90);
		// scene10((double) (2 * Math.PI) / 45);
		// scene10((double) (2 * Math.PI) / 22.5);
		// scene10((double) (2 * Math.PI) / 11.25);
		// scene10((double) (2 * Math.PI) * 2);

		scene50((double) (2 * Math.PI) / 92160);
//		scene50((double) (2 * Math.PI) / 46080);	// ground truth one result wrong
//		scene50((double) (2 * Math.PI) / 23040);
//		scene50((double) (2 * Math.PI) / 11520);
//		scene50((double) (2 * Math.PI) / 5760); 
//		scene50((double) (2 * Math.PI) / 2880);
//		scene50((double) (2 * Math.PI) / 1440);
//		scene50((double) (2 * Math.PI) / 720);
//		scene50((double) (2 * Math.PI) / 360);
//		scene50((double) (2 * Math.PI) / 180);
//		scene50((double) (2 * Math.PI) / 90);
//		scene50((double) (2 * Math.PI) / 45);
//		scene50((double) (2 * Math.PI) / 22.5);
//		scene50((double) (2 * Math.PI) / 11.25);

		scene100((double) (2 * Math.PI) / 92160);
		scene100((double) (2 * Math.PI) / 46080);
		scene100((double) (2 * Math.PI) / 23040);
		scene100((double) (2 * Math.PI) / 11520); 
		scene100((double) (2 * Math.PI) / 5760); 
		scene100((double) (2 * Math.PI) / 2880);
		scene100((double) (2 * Math.PI) / 1440);
		scene100((double) (2 * Math.PI) / 720);
		scene100((double) (2 * Math.PI) / 360);
		scene100((double) (2 * Math.PI) / 180);
		scene100((double) (2 * Math.PI) / 90);
		scene100((double) (2 * Math.PI) / 45);
		scene100((double) (2 * Math.PI) / 22.5);
		scene100((double) (2 * Math.PI) / 11.25);
		
		// scene100();

	}

	private static void scene10(double ANGLE_RADIANTS) throws FileNotFoundException, SQLException {
		SceneBuilder scene = Scenes.testScene10();
		// Create the randomPoints for this scene
		HashMap<Integer, Point> randomPoints = scene.getSceneLoader().loadRandomPoints(100);
		// Run ray shooting for each of the random points.
		RayShooting rayShooting = new RayShooting(randomPoints, scene.getPolygons(), scene.getBoundary(), scene.getConnection().getConnection(), ANGLE_RADIANTS, 0);
		Stopwatch stopWatchRayShooting = SimonManager.getStopwatch("ray-shooting");
		TreeMultimap<Integer, Integer> rayShootingIsovist = rayShooting.rayShootingIsovist();

		// Run IsoVist Index queries.
		Stopwatch stopWatchIndex = SimonManager.getStopwatch("isovist-query_" + scene.getSceneLoader().getSceneName());
		TreeMultimap<Integer, Integer> indexIsovist = IndexIsovist.computeIndexIsovist(scene, 100, scene.getConnection().getConnection());

		scene.getConnection().getConnection().close();

		compareResults(rayShootingIsovist, indexIsovist, scene, ANGLE_RADIANTS, rayShooting.getBUFFER_RADIUS(), stopWatchRayShooting, stopWatchIndex);
		stopWatchIndex.reset();
		stopWatchRayShooting.reset();
	}

	private static void scene50(double ANGLE_RADIANTS) throws FileNotFoundException, SQLException {
		SceneBuilder scene = Scenes.testScene50();

		// Create the randomPoints for this scene
		HashMap<Integer, Point> randomPoints = scene.getSceneLoader().loadRandomPoints(100);
		// Run ray shooting for each of the random points.
		RayShooting rayShooting = new RayShooting(randomPoints, scene.getPolygons(), scene.getBoundary(), scene.getConnection().getConnection(), ANGLE_RADIANTS, 0);
		Stopwatch stopWatchRayShooting = SimonManager.getStopwatch("ray-shooting");
		TreeMultimap<Integer, Integer> rayShootingIsovist = rayShooting.rayShootingIsovist();

		// Run IsoVist Index queries.
		Stopwatch stopWatchIndex = SimonManager.getStopwatch("isovist-query_" + scene.getSceneLoader().getSceneName());
		TreeMultimap<Integer, Integer> indexIsovist = IndexIsovist.computeIndexIsovist(scene, 100, scene.getConnection().getConnection());

		scene.getConnection().getConnection().close();

		compareResults(rayShootingIsovist, indexIsovist, scene, ANGLE_RADIANTS, rayShooting.getBUFFER_RADIUS(), stopWatchRayShooting, stopWatchIndex);
		stopWatchIndex.reset();
		stopWatchRayShooting.reset();
	}

	private static void scene100(double ANGLE_RADIANTS) throws FileNotFoundException, SQLException {
		SceneBuilder scene = Scenes.testScene100();

		// Create the randomPoints for this scene
		HashMap<Integer, Point> randomPoints = scene.getSceneLoader().loadRandomPoints(100);
		// Run ray shooting for each of the random points.
		RayShooting rayShooting = new RayShooting(randomPoints, scene.getPolygons(), scene.getBoundary(), scene.getConnection().getConnection(), ANGLE_RADIANTS, 0);
		Stopwatch stopWatchRayShooting = SimonManager.getStopwatch("ray-shooting");
		TreeMultimap<Integer, Integer> rayShootingIsovist = rayShooting.rayShootingIsovist();

		// Run IsoVist Index queries.
		Stopwatch stopWatchIndex = SimonManager.getStopwatch("isovist-query_" + scene.getSceneLoader().getSceneName());
		TreeMultimap<Integer, Integer> indexIsovist = IndexIsovist.computeIndexIsovist(scene, 100, scene.getConnection().getConnection());

		scene.getConnection().getConnection().close();

		compareResults(rayShootingIsovist, indexIsovist, scene, ANGLE_RADIANTS, rayShooting.getBUFFER_RADIUS(), stopWatchRayShooting, stopWatchIndex);
		stopWatchIndex.reset();
		stopWatchRayShooting.reset();
	}
}
