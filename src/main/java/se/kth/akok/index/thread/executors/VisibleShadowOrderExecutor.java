package se.kth.akok.index.thread.executors;

import java.sql.Connection;
import java.util.ArrayList;

import se.kth.akok.index.algorithms.orderpoints.SortingPointsAlgorithm;
import se.kth.akok.index.algorithms.shadowpoint.ShadowPointsAlgorithm;
import se.kth.akok.index.algorithms.visiblepoint.VisiblePointsAlgorithmMemory;
import se.kth.akok.index.algorithms.visiblepoint.VisiblePointsAlgorithm;
import se.kth.akok.index.geometries.boundary.Boundary;
import se.kth.akok.index.geometries.point.PolygonPoint;
import se.kth.akok.index.geometries.polygon.BasicPolygon;

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
//		VisiblePointsAlgorithm vpa = new VisiblePointsAlgorithm(connection, this.startPoint, this.polygons, this.boundary.getBoundaryPoints(), buildingsName);
		VisiblePointsAlgorithmMemory vpa = new VisiblePointsAlgorithmMemory(connection, this.startPoint, this.polygons, this.boundary.getBoundaryPoints(), buildingsName);
		vpa.setVisiblePointsOfStartPoint();
		
		
		ShadowPointsAlgorithm spa = new ShadowPointsAlgorithm(boundary, polygons);
		spa.setShadowPointsOf(startPoint);
		
		SortingPointsAlgorithm sopa = new SortingPointsAlgorithm();
		sopa.sortPointsFor(startPoint);
	}
	
}
