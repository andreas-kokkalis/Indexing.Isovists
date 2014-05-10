package se.kth.akok.index.algorithms.shadowpoint;

import java.util.ArrayList;
import java.util.HashMap;

import se.kth.akok.index.geometries.boundary.Boundary;
import se.kth.akok.index.geometries.line.Ray;
import se.kth.akok.index.geometries.line.RayType;
import se.kth.akok.index.geometries.operations.Touches;
import se.kth.akok.index.geometries.point.IncomingPoint;
import se.kth.akok.index.geometries.point.PolygonPoint;
import se.kth.akok.index.geometries.point.ShadowPoint;
import se.kth.akok.index.geometries.point.ShadowPointType;
import se.kth.akok.index.geometries.point.VisiblePoint;
import se.kth.akok.index.geometries.point.VisibleType;
import se.kth.akok.index.geometries.polygon.BasicPolygon;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;

/**
 * ShadowPointsAlgorithm calculates the shadow points and corresponding shadow rays of a given polygon point in the scene.
 * 
 * @author Andreas Kokkalis
 * 
 */
public class ShadowPointsAlgorithm {
	private static double MIN_DISTANCE = 0.01;
	private Boundary boundary;
	private ArrayList<BasicPolygon> polygons;

	/**
	 * @param boundary The boundary contains the 4 line segments of the bounding box, and the 4 coordinates.
	 * @param polygons The polygons of the scene.
	 */
	public ShadowPointsAlgorithm(Boundary boundary, ArrayList<BasicPolygon> polygons) {
		this.boundary = boundary;
		this.polygons = polygons;
	}

	/**
	 * For each visible point, it extends the ray to the boundary. Then defines a shadowPoint as the closest point intersection of the ray with any of the geometries that
	 * are not startPoint or endPoint polygon.
	 * 
	 * @param startPoint The point for which shadow points are calculated.
	 * @param polygons The polygons in the scene.
	 */
	public void setShadowPointsOf(PolygonPoint startPoint) {
		ArrayList<VisiblePoint> visiblePoints = startPoint.getVisiblePoints();

		for (VisiblePoint endPoint : visiblePoints) {
			Geometry endPointPolygon = endPoint.getPolygon().getGeometry();
			// Create the line segment from startPoint to endPoint. Then, create the line segment that starts from point i , passes from point j and has endPoint a point
			// on the bounding box
			LineSegment visibleLine = endPoint.getRay().getLine();
			LineSegment extendedSegment = extendSegmentToBoundary(endPoint.getPoint(), visibleLine);
			computeShadowPointForSegment(extendedSegment, startPoint, endPoint, endPointPolygon);

			// This adds the opposite segment of a polygon edge. It provides more accurate isovist for buildings that are close to the boundary.
			// TODO: probably this code is useless
			if (endPoint.getType().equals(VisibleType.SAME_OBJECT_VISIBLE)) {
				LineSegment reversed = new LineSegment(endPoint.getPoint().getCoordinate(), startPoint.getPoint().getCoordinate());
				LineSegment extendedReversed = extendSegmentToBoundary(startPoint.getPoint(), reversed);
				computeShadowPointForSegment(extendedReversed, endPoint, startPoint, endPointPolygon);
			}
		}
	}

	/**
	 * Computes the shadow points and the shadow rays if exists for a given startPoint and a given endPoint.
	 * <p>
	 * For each visible point, it extends the ray to the boundary. Then defines a shadowPoint as the closest point intersection of the ray with any of the geometries that
	 * are not startPoint or endPoint polygon.
	 * </p>
	 * 
	 * @param extendedSegment The extended to boundary segment from startPoint to endPoint
	 * @param startPoint The point for which shadow points are calculated.
	 * @param endPoint The visible point
	 * @param endPointPolygon
	 */
	private void computeShadowPointForSegment(LineSegment extendedSegment, PolygonPoint startPoint, PolygonPoint endPoint, Geometry endPointPolygon) {
		GeometryFactory factory = new GeometryFactory();
		LineString extendedString = extendedSegment.toGeometry(factory);

		// Find the lineSegments that intersect the same polygon. For some geometries, shadow points fall on the same geometry. Find this shadow points.
		if (startPoint.getPolygon().getId() == endPoint.getPolygon().getId()) {
			Geometry intersection = extendedString.intersection(endPointPolygon);
			// Extended Segment intersects the endPointPolygon.
			// This can be either, the line touches an edge but the increased JTS accuracy shows it as it intersects or
			// the extended string passes through the endPointpolygon, which has a complex shape different than a simple rectangle.
			if (!Touches.lineTouchesWithPolygon(extendedString, endPointPolygon)) {

				Coordinate coordinates[] = intersection.getCoordinates();
				ArrayList<Coordinate> allCoordinates = new ArrayList<Coordinate>();

				// In this case, the line touches the geometry on the start and end point, thus it is an extension of the polygon edge.
				if (coordinates.length <= 2) {
					Point point0 = factory.createPoint(coordinates[0]);
					Point point1 = factory.createPoint(coordinates[1]);
					// Edge of the polygon from startPoint to endPoint
					if (point0.isWithinDistance(startPoint.getPoint(), MIN_DISTANCE) && point1.isWithinDistance(endPoint.getPoint(), MIN_DISTANCE) || point0.isWithinDistance(endPoint.getPoint(), MIN_DISTANCE) && point1.isWithinDistance(startPoint.getPoint(), MIN_DISTANCE)) {

						setClosestShadowPoint(extendedString, startPoint, endPoint);
						// System.out.println(extendedString.toString());
					}
				} else {
					// Rays that cross the startPoint geometry.
					for (Coordinate coordinate : coordinates) {
						Point point = factory.createPoint(coordinate);
						if (point.isWithinDistance(startPoint.getPoint(), MIN_DISTANCE) || point.isWithinDistance(endPoint.getPoint(), MIN_DISTANCE))
							continue;
						LineSegment line = new LineSegment(endPoint.getPoint().getCoordinate(), coordinate);
						// To filter out extended rays that cross the geometry, keep only the ones that touch it.
						// There are still some rays that should touch but do not, because of the accuracy. The algorithm does not consider these cases.
						if (line.toGeometry(factory).touches(endPointPolygon))
							allCoordinates.add(coordinate);
					}
					Coordinate closestPoint = null;
					if (!allCoordinates.isEmpty()) {
						Double distance = Double.MAX_VALUE;
						for (Coordinate coordinate : allCoordinates) {
							DistanceOp dist = new DistanceOp(endPoint.getPoint(), factory.createPoint(coordinate));
							Double newDist = new Double(dist.distance());
							if (newDist.compareTo(distance) < 0) {
								distance = newDist;
								closestPoint = coordinate;
							}
						}
						ShadowPoint shadowPoint = new ShadowPoint(ShadowPointType.SAME_OBJECT, factory.createPoint(closestPoint), startPoint.getPolygon());
						startPoint.getShadowPoints().add(shadowPoint);
						LineSegment lineSegment = new LineSegment(startPoint.getPoint().getCoordinate(), closestPoint);
						Ray ray = new Ray(lineSegment, RayType.VISIBLE_RAY_EXTENDED, shadowPoint);
						startPoint.getShadowRays().add(ray);
						// System.out.println(/*"case: " + */lineSegment.toGeometry(factory));
					}
				}
			}
			// The extended segment simply touches the endPoint polygon.
			else {
				setClosestShadowPoint(extendedString, startPoint, endPoint);
				// System.out.println(extendedString.toString());
			}

		}
		// extended string is a line segment from startPoint to a visible point on other geometry
		else {
			setClosestShadowPoint(extendedString, startPoint, endPoint);
			// System.out.println(extendedString.toString());
		}
	}

	/**
	 * The extended segment intersects some geometries. Depending on the type of intersection, it gathers the intersection points and considers them as possible shadow
	 * points. It chooses as shadow point the closest point to start point.
	 * 
	 * @param extendedString The extended visible segment.
	 * @param startPoint The start point of the segment.
	 * @param endPoint The end point of the segment.
	 */
	private void setClosestShadowPoint(LineString extendedString, PolygonPoint startPoint, PolygonPoint endPoint) {
		GeometryFactory factory = new GeometryFactory();
		HashMap<Coordinate, BasicPolygon> allCoordinates = new HashMap<Coordinate, BasicPolygon>();
		for (BasicPolygon polygon : polygons) {
			if (extendedString.intersects(polygon.getGeometry()) || !extendedString.touches(polygon.getGeometry())) {
				// Geometry intersection = extendedString.intersection(polygon.getGeometry());
				// Do not count the startPoint polygon
				if (polygon.getId() == startPoint.getPolygon().getId() || Touches.lineTouchesWithPolygon(extendedString, polygon.getGeometry()))
					continue;
				Coordinate coordinates[] = extendedString.intersection(polygon.getGeometry()).getCoordinates();
				for (Coordinate coordinate : coordinates) {
					// Visible points do not count as shadow points. This allows for special cases such as the segment touches the visible point and ends on different
					// edge of the polygon.
					if (!startPoint.isVisiblePoint(coordinate))
						allCoordinates.put(coordinate, polygon);
				}
			}
		}
		Coordinate closestPoint = null;
		if (!allCoordinates.isEmpty()) {
			Double distance = Double.MAX_VALUE;
			for (Coordinate coordinate : allCoordinates.keySet()) {
				DistanceOp dist = new DistanceOp(endPoint.getPoint(), factory.createPoint(coordinate));
				Double newDist = new Double(dist.distance());
				if (newDist.compareTo(distance) < 0) {
					distance = newDist;
					closestPoint = coordinate;
				}
			}
		}

		// There is a shadow point on another object.
		if (closestPoint != null) {
			ShadowPoint shadowPoint = new ShadowPoint(ShadowPointType.OTHER_OBJECT, factory.createPoint(closestPoint), allCoordinates.get(closestPoint));

			if (!startPoint.shadowPointExists(shadowPoint.getPoint().getCoordinate())) {
				LineSegment shadowRay = new LineSegment(startPoint.getPoint().getCoordinate(), shadowPoint.getPoint().getCoordinate());
				Geometry intersection = shadowRay.toGeometry(factory).intersection(allCoordinates.get(closestPoint).getGeometry());
				// Drop segments that cross the geometry
				if (intersection.getLength() > MIN_DISTANCE || shadowRay.getLength() < MIN_DISTANCE)
					return;
				startPoint.getShadowPoints().add(shadowPoint);
				Ray ray = new Ray(new LineSegment(startPoint.getPoint().getCoordinate(), shadowPoint.getPoint().getCoordinate()), RayType.SHADOW_RAY_OBJECT, shadowPoint);
				startPoint.getShadowRays().add(ray);
				updateIncomingShadowPoint(shadowPoint, startPoint, endPoint);
			}

		}
		// Shadow point on boundary
		if (allCoordinates.isEmpty()) {
			ShadowPoint shadowPoint = new ShadowPoint(ShadowPointType.BOUNDARY_OBJECT, extendedString.getEndPoint());
			if (!startPoint.shadowPointExists(shadowPoint.getPoint().getCoordinate())) {
				startPoint.getShadowPoints().add(shadowPoint);
				Ray ray = new Ray(new LineSegment(extendedString.getStartPoint().getCoordinate(), extendedString.getEndPoint().getCoordinate()), RayType.SHADOW_RAY_BOUNDARY, shadowPoint);
				startPoint.getShadowRays().add(ray);
			}
		}
	}

	/**
	 * When a shadowPoint falls on another geometry, an incoming shadowPoint may exist. This function follows the same procedure with the main algorithm, to find the
	 * incoming shadowPoint
	 * <p>
	 * Creates a lineSegment from shadowPoint to startPoint. extends to boundary and finds the new shadow point. The old shadowPoint is now the new incomingPoint.
	 * </p>
	 * 
	 * @param shadowPoint This is the new incomingPoint
	 * @param startPoint The start point that becomes the endPoint of incomingPoint
	 * @param polygons The list of polygons
	 */
	private void updateIncomingShadowPoint(ShadowPoint shadowPoint, PolygonPoint startPoint, PolygonPoint endPoint) {
		GeometryFactory factory = new GeometryFactory();
		BasicPolygon shadowPolygon = shadowPoint.getPolygon();
		IncomingPoint incomingPoint = new IncomingPoint(factory.createPoint(shadowPoint.getPoint().getCoordinate()), shadowPolygon);
		// The shadow segment reversed.
		LineSegment incomingSegment = new LineSegment(incomingPoint.getPoint().getCoordinate(), startPoint.getPoint().getCoordinate());
		// Extend the segment towards the opposite direction.
		LineSegment incomingExtendedSegment = extendSegmentToBoundary(startPoint.getPoint(), incomingSegment);
		LineString incomingExtendedString = incomingExtendedSegment.toGeometry(factory);

		HashMap<Coordinate, BasicPolygon> allCoordinates = new HashMap<Coordinate, BasicPolygon>();
		for (BasicPolygon polygon : polygons) {
			if (Touches.lineTouchesWithPolygon(incomingExtendedString, polygon.getGeometry()))
				continue;
			Geometry intersection = incomingExtendedString.intersection(polygon.getGeometry());
			Coordinate coordinates[] = intersection.getCoordinates();
			if (startPoint.getPolygon().getId() == endPoint.getPolygon().getId() && coordinates.length <= 2) {
				Point point0 = factory.createPoint(coordinates[0]);
				Point point1 = factory.createPoint(coordinates[1]);
				// Edge of the polygon from startPoint to endPoint
				if (point0.isWithinDistance(startPoint.getPoint(), MIN_DISTANCE) && point1.isWithinDistance(endPoint.getPoint(), MIN_DISTANCE) || point0.isWithinDistance(endPoint.getPoint(), MIN_DISTANCE) && point1.isWithinDistance(startPoint.getPoint(), MIN_DISTANCE)) {
					continue;
				}
			}
			for (Coordinate coordinate : coordinates)
				allCoordinates.put(coordinate, polygon);
		}
		// COMPUTE closest shadow point.
		Coordinate closestPoint = null;
		if (!allCoordinates.isEmpty()) {
			Double distance = Double.MAX_VALUE;
			for (Coordinate coordinate : allCoordinates.keySet()) {
				DistanceOp dist = new DistanceOp(incomingPoint.getPoint(), factory.createPoint(coordinate));
				Double newDist = new Double(dist.distance());
				if (newDist.compareTo(distance) < 0) {
					distance = newDist;
					closestPoint = coordinate;
				}
			}
		}

		if (closestPoint != null) {
			BasicPolygon incomingPolygon = allCoordinates.get(closestPoint);
			ShadowPoint incomingShadowPoint = new ShadowPoint(ShadowPointType.OTHER_OBJECT, factory.createPoint(closestPoint), incomingPolygon);
			LineSegment shadowRay = new LineSegment(incomingPoint.getPoint().getCoordinate(), incomingShadowPoint.getPoint().getCoordinate());

			Geometry intersection = shadowRay.toGeometry(factory).intersection(incomingPolygon.getGeometry());
			// Drop segments that cross the geometry
			if (intersection.getLength() > MIN_DISTANCE || shadowRay.getLength() < MIN_DISTANCE)
				return;

			Ray ray = new Ray(shadowRay, RayType.OPPOSITE_RAY, incomingShadowPoint);

			incomingPoint.setRay(ray);
			incomingPoint.setShadowPoint(incomingShadowPoint);
			if (!shadowPolygon.incomingPointExists(incomingPoint.getPoint().getCoordinate(), shadowRay))
				shadowPolygon.getIncomingPoints().add(incomingPoint);
		} else {
			ShadowPoint incomingShadowPoint = new ShadowPoint(ShadowPointType.BOUNDARY_OBJECT, incomingExtendedString.getEndPoint());
			LineSegment shadowRay = new LineSegment(incomingPoint.getPoint().getCoordinate(), incomingShadowPoint.getPoint().getCoordinate());

			Ray ray = new Ray(shadowRay, RayType.OPPOSITE_RAY_BOUNDARY, incomingShadowPoint);

			incomingPoint.setRay(ray);
			incomingPoint.setShadowPoint(incomingShadowPoint);

			if (!shadowPolygon.incomingPointExists(incomingPoint.getPoint().getCoordinate(), shadowRay))
				shadowPolygon.getIncomingPoints().add(incomingPoint);
		}
	}

	/**
	 * The function computes the equation of each line segment. Then tests for each boundary line segment if the resulting coordinate (x,y) falls into the corresponding
	 * line segment. The four boundary segments are minX, maxX, minY, maxY. An extended segments intersects at least two of these boundary segments. The extended ray that
	 * has same angle from the X axis with the initial visible ray is returned.
	 * 
	 * @param endPoint The visible point from startPoint.
	 * @param lineSegment The visible ray from startPoint to endPoint.
	 * @return The extended to the boundary ray.
	 */
	private LineSegment extendSegmentToBoundary(Point endPoint, LineSegment lineSegment) {

		double minX = boundary.getMinX().p0.x;
		double minY = boundary.getMinY().p0.y;
		double maxX = boundary.getMaxX().p0.x;
		double maxY = boundary.getMaxY().p0.y;

		// y = ax + b
		double slope = (lineSegment.p1.y - lineSegment.p0.y) / (lineSegment.p1.x - lineSegment.p0.x);
		double intercept = lineSegment.p0.y - (slope * lineSegment.p0.x);

		double y1 = (slope * minX) + intercept; // minX
		if (Double.compare(minY, y1) <= 0 && Double.compare(y1, maxY) <= 0) {
			// inbounds
			LineSegment segmentToBoundary = new LineSegment(lineSegment.p0.x, lineSegment.p0.y, minX, y1);
			Double angle1 = (double) Math.floor(lineSegment.angle() * 100) / 100;
			Double angle2 = (double) Math.floor(segmentToBoundary.angle() * 100) / 100;

			if (Double.compare(angle1, angle2) == 0) {
				return segmentToBoundary;
			}
		}
		double y2 = (slope * maxX) + intercept; // maxX
		if (Double.compare(minY, y2) <= 0 && Double.compare(y2, maxY) <= 0) {
			// inbounds
			LineSegment segmentToBoundary = new LineSegment(lineSegment.p0.x, lineSegment.p0.y, maxX, y2);
			Double angle1 = (double) Math.floor(lineSegment.angle() * 100) / 100;
			Double angle2 = (double) Math.floor(segmentToBoundary.angle() * 100) / 100;
			if (Double.compare(angle1, angle2) == 0) {
				return segmentToBoundary;
			}
		}
		double x1 = (double) (minY - intercept) / slope; // minY
		// line parallel with y axis
		if (Double.isNaN(x1))
			x1 = lineSegment.p0.x;
		if (Double.compare(minX, x1) <= 0 && Double.compare(x1, maxX) <= 0) {
			// inbounds
			LineSegment segmentToBoundary = new LineSegment(lineSegment.p0.x, lineSegment.p0.y, x1, minY);
			Double angle1 = (double) Math.floor(lineSegment.angle() * 100) / 100;
			Double angle2 = (double) Math.floor(segmentToBoundary.angle() * 100) / 100;
			if (Double.compare(angle1, angle2) == 0) {
				return segmentToBoundary;
			}
		}
		double x2 = (double) (maxY - intercept) / slope; // maxY
		// line parallel with y axis
		if (Double.isNaN(x2))
			x2 = lineSegment.p0.x;
		if (Double.compare(minX, x2) <= 0 && Double.compare(x2, maxX) <= 0) {
			// inbounds
			LineSegment segmentToBoundary = new LineSegment(lineSegment.p0.x, lineSegment.p0.y, x2, maxY);
			Double angle1 = (double) Math.floor(lineSegment.angle() * 100) / 100;
			Double angle2 = (double) Math.floor(segmentToBoundary.angle() * 100) / 100;
			if (Double.compare(angle1, angle2) == 0) {
				return segmentToBoundary;
			}
		}
		// If the function did not return an extended segment, an error with the data occured. Probably the visible ray was invalid.
		throw new NullPointerException("Exception: " + lineSegment + "\tangle: " + lineSegment.angle() + "\t" + endPoint.toString());
	}
}