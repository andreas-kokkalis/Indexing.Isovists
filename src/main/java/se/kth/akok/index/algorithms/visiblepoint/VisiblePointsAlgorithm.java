package se.kth.akok.index.algorithms.visiblepoint;

import java.sql.Connection;
import java.util.ArrayList;

import se.kth.akok.index.geometries.line.Ray;
import se.kth.akok.index.geometries.line.RayType;
import se.kth.akok.index.geometries.operations.Intersects;
import se.kth.akok.index.geometries.operations.Touches;
import se.kth.akok.index.geometries.point.BoundaryPoint;
import se.kth.akok.index.geometries.point.PolygonPoint;
import se.kth.akok.index.geometries.point.VisiblePoint;
import se.kth.akok.index.geometries.point.VisibleType;
import se.kth.akok.index.geometries.polygon.BasicPolygon;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * VisiblePointsAlgorithm computes all visible polygon points from a given startPoint. It creates lists of all the visible points and corresponding visible rays and
 * stores them in the given polygon point.
 * 
 * @author Andreas Kokkalis
 * 
 */
public class VisiblePointsAlgorithm {
	private static double MIN_DISTANCE = 0.01;
	private PolygonPoint startPoint;
	private ArrayList<Point> boundaryPoints;
	private ArrayList<BasicPolygon> polygons;
	private String buildingsTable;
	private Intersects intersectsFunction;
	/**
	 * @param connection The connection to the database.
	 * @param startPoint The point for which visible points are calculated
	 * @param polygons All the polygons in the scene.
	 * @param boundaryPoints The 4 boundary points of the scene.
	 */
	public VisiblePointsAlgorithm(Connection connection, PolygonPoint startPoint, ArrayList<BasicPolygon> polygons, ArrayList<Point> boundaryPoints, String buildingsTable) {
		this.startPoint = startPoint;
		this.polygons = polygons;
		this.boundaryPoints = boundaryPoints;
		this.buildingsTable = buildingsTable;
		this.intersectsFunction = new Intersects(connection);
	}

	/**
	 * Given a startPoint, compute all the other points that are visible from startPoint. It creates line segments as rays from startPoint to an endPoint. The endPoint is
	 * visible only if the ray touches both startPoint and endPoint Polygon, and does not intersect with any geometry in between.
	 * 
	 */
	public void setVisiblePointsOfStartPoint() {
		// Initialize array with all the points
		ArrayList<VisiblePoint> visiblePoints = startPoint.getVisiblePoints();
		GeometryFactory factory = new GeometryFactory();

		// Set visible points on the same polygon.
		for (PolygonPoint polygonPoint : startPoint.getPolygon().getPolygonPoints()) {
			if (startPoint.equals(polygonPoint))
				continue;
			
			LineSegment lineSegment = new LineSegment(startPoint.getPoint().getCoordinate(), polygonPoint.getPoint().getCoordinate());
			if (Touches.lineTouchesWithPolygon(lineSegment.toGeometry(factory), polygonPoint.getPolygon().getGeometry()) && lineSegment.getLength() > MIN_DISTANCE) {
				// added after spotting bug with geometry surrounding other geometries.
				if (!intersectsWithOtherGeometries(lineSegment.toGeometry(factory), startPoint, polygonPoint)) {
					VisiblePoint visiblePoint = new VisiblePoint(polygonPoint.getPoint(), polygonPoint.getPolygon(), VisibleType.SAME_OBJECT_VISIBLE);
					visiblePoints.add(visiblePoint);
					Ray ray = new Ray(lineSegment, RayType.VISIBLE_RAY, visiblePoint);
					visiblePoint.setRay(ray);
					startPoint.getVisibleRays().add(ray);
				}
			}
		}
		// Set visible point on other polygons.
		for (BasicPolygon polygon : polygons) {
			if (startPoint.getPolygon().equals(polygon))
				continue;
			for (PolygonPoint polygonPoint : polygon.getPolygonPoints()) {
				LineSegment lineSegment = new LineSegment(startPoint.getPoint().getCoordinate(), polygonPoint.getPoint().getCoordinate());
				LineString lineString = lineSegment.toGeometry(factory);
				Geometry endPointPolygon = polygonPoint.getPolygon().getGeometry(); // This is the polygon on which iterable point is on
				Geometry startPointPolygon = startPoint.getPolygon().getGeometry();
				// The minimum distance check is for some points that seem like duplicated in real scenarios. They are actually neighboring points, or two geometries that
				// are different, contain the same point.
				if (lineSegment.getLength() > MIN_DISTANCE && !intersectsWithOtherGeometries(lineString, startPoint, polygonPoint)) {
					boolean lineTouchesEndPolygon = Touches.lineTouchesWithPolygon(lineString, endPointPolygon);
					boolean lineTouchesStartPolygon = Touches.lineTouchesWithPolygon(lineString, startPointPolygon);
					// Two points are visible if the connecting line only touches those points.
					if (lineTouchesEndPolygon && lineTouchesStartPolygon) {
						VisiblePoint visiblePoint = new VisiblePoint(polygonPoint.getPoint(), polygon, VisibleType.OTHER_OBJECT_VISIBLE);
						visiblePoints.add(visiblePoint);
						// Add the ray
						Ray ray = new Ray(lineSegment, RayType.VISIBLE_RAY, visiblePoint);
						visiblePoint.setRay(ray);
						startPoint.getVisibleRays().add(ray);
					}
				}
			}
		}
		startPoint.setVisiblePoints(visiblePoints);

		// Set the boundary points as visible points. Introduces better results for polygons next to the boundary of the scene.
		for (Point endPoint : boundaryPoints) {
			LineSegment lineSegment = new LineSegment(startPoint.getPoint().getCoordinate(), endPoint.getCoordinate());
			LineString lineString = lineSegment.toGeometry(factory);
			if (!intersectsWithOtherGeometries(lineString, startPoint)) {
				boolean lineTouchesStartPolygon = Touches.lineTouchesWithPolygon(lineString, startPoint.getPolygon().getGeometry());
				// Two points are visible if the connecting line only touches those points.
				if (lineTouchesStartPolygon) {
					BoundaryPoint bPoint = new BoundaryPoint(endPoint);
					Ray ray = new Ray(lineSegment, RayType.VISIBLE_RAY_BOUNDARY, bPoint);
					bPoint.setRay(ray);
					startPoint.getBoundaryPoints().add(bPoint);
					startPoint.getBoundaryRays().add(ray);
				}
			}
		}
	}

	/**
	 * For a given lineString, it iterates through the known geometries, and tests if it intersects with them.
	 * <p>
	 * This function is moved to be executed in the database by a corresponding SQL query.
	 * </p>
	 * 
	 * @param lineString The line string to check if it intersects with other geometries
	 * @param startPoint PopygonPoint, the startPoint of the lineString
	 * @param endPoint PolygonPoint, the endPoint of the lineString
	 * @return Returns true if the lineString intersects with other geometries, else false.
	 */
	private boolean intersectsWithOtherGeometries(LineString lineString, PolygonPoint startPoint, PolygonPoint endPoint) {
		boolean intersects = false;
		int numOfIntersections = intersectsFunction.lineIntersectsWithBuildings(buildingsTable, lineString, startPoint.getPolygon().getGeometry().getSRID(), startPoint.getPolygon().getId(), endPoint.getPolygon().getId());
		if(numOfIntersections == 1 && startPoint.getPolygon().getId().equals(endPoint.getPolygon().getId())) {
			Geometry geometry = intersectsFunction.getPolygonThatIntersects(buildingsTable, lineString, startPoint.getPolygon().getGeometry().getSRID(), startPoint.getPolygon().getId());
			Geometry intersection = lineString.intersection(geometry);
			if(intersection.getLength() < MIN_DISTANCE)
				intersects = false;
			else
				intersects = true;
		}
		else {
			if(numOfIntersections > 0)
				intersects = true;
			else
				intersects = false;
		}
		return intersects;
	}

	/**
	 * For a given lineString, it iterates through the known geometries, and tests if it intersects with them.
	 * <p>
	 * This function is moved to be executed in the database by a corresponding SQL query.
	 * </p>
	 * 
	 * @param lineString The line string to check if it intersects with other geometries
	 * @param startPoint PopygonPoint, the startPoint of the lineString
	 * @return Returns true if the lineString intersects with other geometries, else false.
	 */
	private boolean intersectsWithOtherGeometries(LineString lineString, PolygonPoint startPoint) {
		int numOfIntersections = intersectsFunction.lineIntersectsWithBuildings(buildingsTable, lineString, startPoint.getPolygon().getGeometry().getSRID(), startPoint.getPolygon().getId(), startPoint.getPolygon().getId());
		if(numOfIntersections > 0)
			return true;
		else
			return false;
	}
	
	//TODO: testing, do it on memory
	private boolean intersectsWithOtherGeometries2(LineString lineString, PolygonPoint startPoint, PolygonPoint endPoint) {
		for(BasicPolygon polygon: polygons) {
			if(startPoint.getPolygon().equals(polygon) || endPoint.getPolygon().equals(polygon))
				continue;
			if(lineString.intersects(polygon.getGeometry()))
				return true;
		}
		return false;
		
//		boolean intersects = false;
//		int numOfIntersections = intersectsFunction.lineIntersectsWithBuildings(buildingsTable, lineString, startPoint.getPolygon().getGeometry().getSRID(), startPoint.getPolygon().getId(), endPoint.getPolygon().getId());
//		if(numOfIntersections == 1 && startPoint.getPolygon().getId().equals(endPoint.getPolygon().getId())) {
//			Geometry geometry = intersectsFunction.getPolygonThatIntersects(buildingsTable, lineString, startPoint.getPolygon().getGeometry().getSRID(), startPoint.getPolygon().getId());
//			Geometry intersection = lineString.intersection(geometry);
//			if(intersection.getLength() < MIN_DISTANCE)
//				intersects = false;
//			else
//				intersects = true;
//		}
//		else {
//			if(numOfIntersections > 0)
//				intersects = true;
//			else
//				intersects = false;
//		}
//		return intersects;
	}
	//TODO: testing, do it on memory
	private boolean intersectsWithOtherGeometries2(LineString lineString, PolygonPoint startPoint) {
//		int numOfIntersections = intersectsFunction.lineIntersectsWithBuildings(buildingsTable, lineString, startPoint.getPolygon().getGeometry().getSRID(), startPoint.getPolygon().getId(), startPoint.getPolygon().getId());
//		if(numOfIntersections > 0)
//			return true;
//		else
//			return false;
		for(BasicPolygon polygon: polygons) {
			if(startPoint.getPolygon().equals(polygon))
				continue;
			if(lineString.intersects(polygon.getGeometry()))
				return true;
		}
		return false;
	}
}
