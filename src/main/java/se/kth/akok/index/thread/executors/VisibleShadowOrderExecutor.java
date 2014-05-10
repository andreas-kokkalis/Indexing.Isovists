package se.kth.akok.index.thread.executors;

import java.sql.Connection;
import java.util.ArrayList;

import se.kth.akok.index.algorithms.orderpoints.SortingPointsAlgorithm;
import se.kth.akok.index.algorithms.shadowpoint.ShadowPointsAlgorithm;
import se.kth.akok.index.algorithms.visiblepoint.VisiblePointsAlgorithmMemory;
import se.kth.akok.index.geometries.boundary.Boundary;
import se.kth.akok.index.geometries.point.PolygonPoint;
import se.kth.akok.index.geometries.polygon.BasicPolygon;

/**
 * This class is responsible for finding the visible points, the shadow points and ordering all the rays of a given polygon point. The process runs in order the following
 * algorithms:
 * <p>
 * VisiblePointsAlgorithm, ShadowPointsAlgorithm, SortingPointsAlgorithm
 * </p>
 * Each one of them, depends on the previous algorithm.
 * <p>
 * VisibleShadowOrderExecutor runs as a thread for each polygon point. The parallel computation decreases the total runtime.
 * </p>
 * 
 * @author Andreas Kokkalis
 * 
 */
public class VisibleShadowOrderExecutor implements Runnable {
	private PolygonPoint startPoint;
	private Boundary boundary;
	private ArrayList<BasicPolygon> polygons;
	private String buildingsName;
	private Connection connection;

	public VisibleShadowOrderExecutor(Connection connection, PolygonPoint startPoint, ArrayList<BasicPolygon> polygons, Boundary boundary, String buildingsName) {
		this.startPoint = startPoint;
		this.polygons = polygons;
		this.boundary = boundary;
		this.buildingsName = buildingsName;
		this.connection = connection;
	}

	public void run() {
		// VisiblePointsAlgorithm vpa = new VisiblePointsAlgorithm(connection, this.startPoint, this.polygons, this.boundary.getBoundaryPoints(), buildingsName);
		VisiblePointsAlgorithmMemory vpa = new VisiblePointsAlgorithmMemory(this.startPoint, this.polygons, this.boundary.getBoundaryPoints());
		vpa.setVisiblePointsOfStartPoint();

		ShadowPointsAlgorithm spa = new ShadowPointsAlgorithm(boundary, polygons);
		spa.setShadowPointsOf(startPoint);

		SortingPointsAlgorithm sopa = new SortingPointsAlgorithm();
		sopa.sortPointsFor(startPoint);
	}

}
