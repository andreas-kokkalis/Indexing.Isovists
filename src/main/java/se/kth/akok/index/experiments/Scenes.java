/**
 * 
 */
package se.kth.akok.index.experiments;

import java.io.FileNotFoundException;
import java.sql.SQLException;

import se.kth.akok.index.scene.SceneBuilder;

/**
 * @author Andreas Kokkalis <a.kokkalis@kth.se>
 * 
 */
public class Scenes {
	public static SceneBuilder testScene10() throws FileNotFoundException, SQLException {
		return new SceneBuilder("isovist", "indexing_scene_10", "indexing_scene_10", "indexing_boundary_10");
	}

	public static SceneBuilder testScene50() throws FileNotFoundException, SQLException {
		return new SceneBuilder("isovist", "indexing_scene_50", "indexing_scene_50", "indexing_boundary_50");
	}

	public static SceneBuilder testScene100() throws FileNotFoundException, SQLException {
		return new SceneBuilder("isovist", "indexing_scene_100", "indexing_scene_100", "indexing_boundary_100");
	}

	public static SceneBuilder testScene200() throws FileNotFoundException, SQLException {
		return new SceneBuilder("isovist", "indexing_scene_200", "indexing_scene_200", "indexing_boundary_200");
	}

	public static SceneBuilder testScene300() throws FileNotFoundException, SQLException {
		return new SceneBuilder("isovist", "indexing_scene_300", "indexing_scene_300", "indexing_boundary_300");
	}

	public static SceneBuilder testScene400() throws FileNotFoundException, SQLException {
		return new SceneBuilder("isovist", "indexing_scene_400", "indexing_scene_400", "indexing_boundary_400");
	}

	public static SceneBuilder testScene500() throws FileNotFoundException, SQLException {
		return new SceneBuilder("isovist", "indexing_scene_500", "indexing_scene_500", "indexing_boundary_500");
	}
}
