/**
 * 
 */
package se.kth.akok.index.experiments;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import se.kth.akok.index.algorithms.rayshooting.RayShooting;
import se.kth.akok.index.geometries.polygon.BasicPolygon;
import se.kth.akok.index.scene.SceneBuilder;

import com.google.common.collect.TreeMultimap;
import com.vividsolutions.jts.geom.Point;

/**
 * @author Andreas Kokkalis <a.kokkalis@kth.se>
 * 
 */
public class RayShootingVsIsoVist {
	private static void logResults(TreeMultimap<Integer, Integer> indexIsv, TreeMultimap<Integer, Integer> rayIsv, SceneBuilder scene, int numRandPoints) throws FileNotFoundException {
		PrintWriter w1 = new PrintWriter("/home/andrew/Desktop/test/" + scene.getSceneLoader().getSceneName() + "_" + numRandPoints + "_log-ray.txt");
		Set<Entry<Integer, Collection<Integer>>> set = rayIsv.asMap().entrySet();
		for (Entry<Integer, Collection<Integer>> entry : set) {
			Collection<Integer> visiblePolygons = (Collection<Integer>) entry.getValue();
			for (Integer polygonId : visiblePolygons) {
				for (BasicPolygon polygon : scene.getPolygons())
					if (polygon.getId().equals(polygonId)) {
						w1.println(entry.getKey() + ";" + polygon.getGeometry().toString() + ";" + polygonId);
						break;
					}
			}
		}
		w1.close();

		PrintWriter w2 = new PrintWriter("/home/andrew/Desktop/test/" + scene.getSceneLoader().getSceneName() + "_" + numRandPoints + "_log-isv.txt");
		Set<Entry<Integer, Collection<Integer>>> set2 = indexIsv.asMap().entrySet();
		for (Entry<Integer, Collection<Integer>> entry : set2) {
			Collection<Integer> visiblePolygons = (Collection<Integer>) entry.getValue();
			for (Integer polygonId : visiblePolygons) {
				for (BasicPolygon polygon : scene.getPolygons())
					if (polygon.getId().equals(polygonId)) {
						w2.println(entry.getKey() + ";" + polygon.getGeometry().toString() + ";" + polygonId);
						break;
					}
			}
		}
		w2.close();
	}

	public static void main(String[] args) throws FileNotFoundException, SQLException {
		SceneBuilder scene = Scenes.testScene50();

		// Stopwatch stopwatchOuter = SimonManager.getStopwatch("ray-shooting");
		HashMap<Integer, Point> randomPoints = scene.getSceneLoader().loadRandomPoints(100);
		RayShooting rayShooting = new RayShooting(randomPoints, scene.getPolygons(), scene.getBoundary(), scene.getConnection().getConnection());
		TreeMultimap<Integer, Integer> rayShootingIsovist = rayShooting.rayShootingIsovist();

		// StopWatchPrinter.printStopWatch(stopwatchOuter);
		TreeMultimap<Integer, Integer> indexIsovist = IndexIsovist.computeIndexIsovist(scene, 100, scene.getConnection().getConnection());

		scene.getConnection().getConnection().close();

		boolean equals = rayShootingIsovist.equals(indexIsovist);
		System.out.println("equals:\t" + equals);

		System.out.println("Ray:\t" + rayShootingIsovist.size());
		System.out.println("Isv:\t" + indexIsovist.size());

		logResults(indexIsovist, rayShootingIsovist, scene, 100);

		System.out.println("Ray shooting\n==================================");
		System.out.println(rayShootingIsovist.toString());
		System.out.println("IsoVist\n==================================");
		System.out.println(indexIsovist.toString());

	}
}
