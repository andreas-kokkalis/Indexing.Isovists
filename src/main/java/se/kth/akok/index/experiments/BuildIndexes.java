package se.kth.akok.index.experiments;

import java.io.FileNotFoundException;
import java.sql.SQLException;

import se.kth.akok.index.scene.SceneBuilder;

public class BuildIndexes {
	public static void testScene10() throws FileNotFoundException, SQLException {
		SceneBuilder scene = new SceneBuilder("isovist", "indexing_scene_10", "indexing_scene_10", "indexing_boundary_10");
		System.out.println("#Polygons: " + scene.getPolygons().size());
		System.out.println("#Points: " + scene.getPoints().size());
		scene.buildIndex();
	}

	public static void testScene50() throws FileNotFoundException, SQLException {
		SceneBuilder scene = new SceneBuilder("isovist", "indexing_scene_50", "indexing_scene_50", "indexing_boundary_50");
		System.out.println("#Polygons: " + scene.getPolygons().size());
		System.out.println("#Points: " + scene.getPoints().size());
		scene.buildIndex();
	}

	public static void testScene100() throws FileNotFoundException, SQLException {
		SceneBuilder scene = new SceneBuilder("isovist", "indexing_scene_100", "indexing_scene_100", "indexing_boundary_100");
		System.out.println("#Polygons: " + scene.getPolygons().size());
		System.out.println("#Points: " + scene.getPoints().size());
		scene.buildIndex();
	}

	public static void testScene200() throws FileNotFoundException, SQLException {
		SceneBuilder scene = new SceneBuilder("isovist", "indexing_scene_200", "indexing_scene_200", "indexing_boundary_200");
		System.out.println("#Polygons: " + scene.getPolygons().size());
		System.out.println("#Points: " + scene.getPoints().size());
		scene.buildIndex();
	}

	public static void testScene300() throws FileNotFoundException, SQLException {
		SceneBuilder scene = new SceneBuilder("isovist", "indexing_scene_300", "indexing_scene_300", "indexing_boundary_300");
		System.out.println("#Polygons: " + scene.getPolygons().size());
		System.out.println("#Points: " + scene.getPoints().size());
		scene.buildIndex();
	}

	public static void testScene400() throws FileNotFoundException, SQLException {
		SceneBuilder scene = new SceneBuilder("isovist", "indexing_scene_400", "indexing_scene_400", "indexing_boundary_400");
		System.out.println("#Polygons: " + scene.getPolygons().size());
		System.out.println("#Points: " + scene.getPoints().size());
		scene.buildIndex();
	}

	public static void testScene500() throws FileNotFoundException, SQLException {
		SceneBuilder scene = new SceneBuilder("isovist", "indexing_scene_500", "indexing_scene_500", "indexing_boundary_500");
		System.out.println("#Polygons: " + scene.getPolygons().size());
		System.out.println("#Points: " + scene.getPoints().size());
		scene.buildIndex();
	}

	public static void main(String[] args) throws FileNotFoundException, SQLException {
		BuildIndexes.testScene10();
//		BuildIndexes.testScene50();
//		BuildIndexes.testScene100();
//		BuildIndexes.testScene200();
//		BuildIndexes.testScene300();
//		BuildIndexes.testScene400();
//		BuildIndexes.testScene500();
	}
}
