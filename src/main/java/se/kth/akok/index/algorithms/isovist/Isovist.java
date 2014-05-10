package se.kth.akok.index.algorithms.isovist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import se.kth.akok.index.geometries.line.Ray;
import se.kth.akok.index.geometries.point.IncomingPoint;
import se.kth.akok.index.geometries.point.PolygonPoint;
import se.kth.akok.index.geometries.polygon.BasicPolygon;

import com.google.common.collect.ArrayListMultimap;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;

/**
 * Isovist computes the polygon isovist of a given polygon.
 * <p>
 * First the algorithm computes the point isovist of each polygon point. The resulting polygon isovist is the union of each point isovist.
 * </p>
 * <p>
 * Finally, it considers the incoming points of the polygon to augment the final result, if possible.
 * </p>
 * 
 * @author Andreas Kokkalis
 * 
 */
public class Isovist implements Runnable {
	private ArrayList<BasicPolygon> allPolygons;
	private BasicPolygon thisPolygon;
	private GeometryFactory factory;
	private static double MIN_DISTANCE = 0.01;

	/**
	 * Constructor of the Isovist class
	 * 
	 * @param allPolygons All polygons in the scene.
	 */
	public Isovist(ArrayList<BasicPolygon> allPolygons, BasicPolygon thisPolygon) {
		this.allPolygons = allPolygons;
		this.thisPolygon = thisPolygon;
		this.factory = new GeometryFactory();
	}

	public void run() {
		polygonIsovist(thisPolygon);
		if (!thisPolygon.getIncomingPoints().isEmpty())
			incomingIsovist(thisPolygon);
	}

	/**
	 * Compute the isovist of a polygon by computing the isovist for each polygon point.
	 * 
	 * <p>
	 * The function iterates through the point isovists and performs a union operation. If the JTS library does not allow for a union to be performed, the buggy point
	 * isovist is not in the final polygon isovist.
	 * </p>
	 * 
	 * @param polygon The polygon for which to compute the isovist.
	 * @throws Exception
	 */
	private void polygonIsovist(BasicPolygon polygon) throws TopologyException {
		Collection<Geometry> pointIsovist = new ArrayList<Geometry>();
		for (PolygonPoint point : polygon.getPolygonPoints()) {
			pointIsovist(point);
			if (point.getPointIsovist() != null)
				pointIsovist.add(point.getPointIsovist());
		}
		Geometry[] polygonIsovist = pointIsovist.toArray(new Geometry[pointIsovist.size()]);
		GeometryCollection collection = new GeometryCollection(polygonIsovist, factory);
		Geometry union = null;
		try {
			union = collection.union();
			polygon.setPolygonIsovist(union);
		} catch (TopologyException e) {
			Geometry theUnion = null;
			for (PolygonPoint point : polygon.getPolygonPoints()) {
				if (point.getPointIsovist() != null) {

					if (theUnion == null)
						theUnion = point.getPointIsovist();
					else {
						try {
							theUnion = theUnion.union(point.getPointIsovist());
						} catch (TopologyException e1) {
							System.out.println("cannot add isovist of point: " + point.getPoint().toString() + " of polygon " + point.getPolygon().getGeometry().toString());
						}
					}
				}
			}
			polygon.setPolygonIsovist(theUnion);
			System.out.println("=======================================\n" + theUnion.toString() + "\n=======================================\n");
		}
	}

	/**
	 * Compute the isovist for the given polygon point.
	 * <p>
	 * The function assumes that all visible and shadow rays are sorted in a clockwise order.
	 * </p>
	 * 
	 * <p>
	 * They rays are sorted on clockwise order. The outer loop selects all rays with angle a. The inner loop selects all rays with angle b > a. It creates all polygons by
	 * combining the the common start point and the different endPoints. Finally it unions the polygons and proceeds. The outer loop now has all rays with angle b, and
	 * the inner c>b. The algorithm terminates, when all rays are checked, and the final point isovist is computed.
	 * </p>
	 * 
	 * <p>
	 * There are points for which the isovist is impossible to be computed with the current implementation. These are points on edges of a polygon that touches with an
	 * edge of a different polygon. The result polygon isovist is not affected since neighboring points guarantee to compute the visible space.
	 * </p>
	 * 
	 * 
	 * @param startPoint
	 * @throws Exception
	 */
	private void pointIsovist(PolygonPoint startPoint) throws TopologyException {
		if (startPoint.isPointIsovistNotComputable()) {
			startPoint.setPointIsovist(null);
			System.out.println("isovist not comutable: point\t" + startPoint.getPoint() + "\tpolygon\t" + startPoint.getPolygon().getGeometry().toString());
			return;
		}

		ArrayListMultimap<Double, Ray> allRays = ArrayListMultimap.create();
		ArrayList<Ray> checkedRays = new ArrayList<Ray>();
		for (Ray ray : startPoint.getAllRays()) {
			allRays.put(ray.getAngle(), ray);
		}
		ArrayList<Ray> myRays = startPoint.getAllRays();

		GeometryFactory factory = new GeometryFactory();
		Collection<Geometry> geometryCollection = new ArrayList<Geometry>();
		Geometry pointIsovist = null;
		for (Ray ray : myRays) {
			// Initialize the first
			if (checkedRays.contains(ray))
				continue;
			List<Ray> currentRays = allRays.get(ray.getAngle());

			List<Ray> nextRays = getNextRayOf(ray, myRays, allRays);
			if (nextRays.isEmpty())
				continue;
			Geometry union = null;
			for (Ray currentRay : currentRays) {
				for (Ray nextRay : nextRays) {
					ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
					coordinates.add(currentRay.getLine().p0);
					coordinates.add(currentRay.getLine().p1);
					coordinates.add(nextRay.getLine().p1);
					coordinates.add(currentRay.getLine().p0);
					Coordinate[] allCoordinates = coordinates.toArray(new Coordinate[coordinates.size()]);

					LinearRing linearRing = factory.createLinearRing(allCoordinates);
					Polygon polygon = new Polygon(linearRing, null, factory);

					if (!intersectsWithScenePolygons(polygon, startPoint.getPolygon())) {
						// System.out.println("No intersection");
						geometryCollection.add(polygon);
						if (union == null)
							union = polygon;
						else
							union = union.union(polygon);
						if (Double.compare(union.getArea(), 0.01) < 0)
							union = null;
					}
				}
			}
			checkedRays.addAll(currentRays);
			try {
				if (pointIsovist == null && union != null)
					pointIsovist = union;
				else if (union != null && !pointIsovist.equals(union)) {
					pointIsovist = pointIsovist.union(union);
				}
			} catch (TopologyException e) {
				System.out.println(union.toString());
				System.out.println(pointIsovist.toString());
				System.out.println(startPoint.getPoint().toString());
				System.out.println(startPoint.getPolygon().getGeometry().toString());
				throw e;
			}
		}
		startPoint.setPointIsovist(pointIsovist);
	}

	/**
	 * Get the next ray in clockwise order, that has an angle different than the angle of currentRay.
	 * 
	 * <p>
	 * This funtion is based on the utilities of the "guava library" MultiMap which allows for multiple values to be associated with the same key.
	 * </p>
	 * 
	 * @param currentRay The ray with angle a
	 * @param myRays All sorted rays
	 * @param allRays All rays categorized by angle in a multimap
	 * @return the nextRay if exists.
	 */
	private List<Ray> getNextRayOf(Ray currentRay, ArrayList<Ray> myRays, ArrayListMultimap<Double, Ray> allRays) {
		boolean found = false;
		for (Ray ray : myRays) {
			if (ray.getAngle().equals(currentRay.getAngle())) {
				found = true;
				continue;
			}
			if (found && !currentRay.getAngle().equals(ray.getAngle()))
				return allRays.get(ray.getAngle());
		}
		return new ArrayList<Ray>();
	}

	/**
	 * Computes the isovist of incoming polygon points, by iterating through pairs of incoming line segments and creating polygons. The isovist is the union of these
	 * polygons.
	 * <p>
	 * It only checks for pairs of line segments that originate from the same polygon edge. If they can create a simple polygon then an isovist is later unionized with
	 * the regular point isovists.
	 * </p>
	 * <p>
	 * This algorithm has no effect on real data, since incoming points and their corresponding rays always fall into the polygon isovist. With the generated test scene
	 * where buildings are positioned parallel or perpendicular the result augments the polygon isovist.
	 * </p>
	 * 
	 * @param polygon The polygon for which the incoming point isovist is computed.
	 */
	private void incomingIsovist(BasicPolygon polygon) {
		GeometryFactory factory = new GeometryFactory();
		Collection<Geometry> geometryCollection = new ArrayList<Geometry>();

		ArrayList<Ray> incomingRays = new ArrayList<Ray>();
		for (IncomingPoint incomingPoint : polygon.getIncomingPoints())
			if (!incomingPoint.getRay().getLine().toGeometry(factory).within(polygon.getPolygonIsovist()))
				incomingRays.add(incomingPoint.getRay());

		try {

			HashSet<Ray> checkedRays = new HashSet<Ray>();
			for (Ray outer : incomingRays) {
				for (Ray inner : incomingRays) {
					// Check rays that their starting points are collinear and the line is a polygon edge
					LineSegment line = new LineSegment(outer.getLine().p0, inner.getLine().p0);
					Coordinate middleCoordinate = line.midPoint();
					Point middlePoint = factory.createPoint(middleCoordinate);
					// If the middle point of the line is not on the polygon, then inner_p0 and outer_p0 are not on the same polygon edge
					if (!polygon.getGeometry().touches(middlePoint) || !polygon.getGeometry().isWithinDistance(middlePoint, MIN_DISTANCE))
						continue;

					// Check only rays that where not checked from the outer for loop.
					if (checkedRays.contains(inner) || outer.equals(inner))
						continue;
					LineString outerString = outer.getLine().toGeometry(factory);
					LineString innerString = inner.getLine().toGeometry(factory);
					// The two chosen line segments intersect. Two triangles are formed. (newPolygon1 and newPolygon2)
					if (outerString.intersects(innerString)) {
						Geometry intersection = outerString.intersection(innerString);
						Point point = null;
						try {
							point = (Point) outerString.intersection(innerString);

						} catch (ClassCastException e) {
							System.out.println(intersection.toString() + "\touter: " + outer.getLine().toString() + "\tinner: " + inner.getLine().toString());
						}
						ArrayList<Coordinate> c1 = new ArrayList<Coordinate>();
						c1.add(outer.getLine().p0);
						c1.add(point.getCoordinate());
						c1.add(inner.getLine().p0);
						c1.add(outer.getLine().p0);
						Coordinate[] allCoordinates1 = c1.toArray(new Coordinate[c1.size()]);
						LinearRing linearRing1 = factory.createLinearRing(allCoordinates1);
						Polygon newPolygon1 = new Polygon(linearRing1, null, factory);
						if (newPolygon1.isValid() && !intersectsWithScenePolygons(newPolygon1, polygon))
							geometryCollection.add(newPolygon1);
						ArrayList<Coordinate> c2 = new ArrayList<Coordinate>();
						c2.add(outer.getLine().p1);
						c2.add(point.getCoordinate());
						c2.add(inner.getLine().p1);
						c2.add(outer.getLine().p1);
						Coordinate[] allCoordinates2 = c2.toArray(new Coordinate[c2.size()]);
						LinearRing linearRing2 = factory.createLinearRing(allCoordinates2);
						Polygon newPolygon2 = new Polygon(linearRing2, null, factory);
						if (newPolygon2.isValid() && !intersectsWithScenePolygons(newPolygon2, polygon))
							geometryCollection.add(newPolygon2);
					}
					// The two line segments do not intersect. A polygon is formed.
					else {
						ArrayList<Coordinate> c = new ArrayList<Coordinate>();
						c.add(outer.getLine().p0);
						c.add(inner.getLine().p0);
						c.add(inner.getLine().p1);
						c.add(outer.getLine().p1);
						c.add(outer.getLine().p0);
						Coordinate[] allCoordinates = c.toArray(new Coordinate[c.size()]);
						LinearRing linearRing = factory.createLinearRing(allCoordinates);
						Polygon newPolygon = new Polygon(linearRing, null, factory);
						if (newPolygon.isValid() && !intersectsWithScenePolygons(newPolygon, polygon))
							geometryCollection.add(newPolygon);
					}
				}
				checkedRays.add(outer);
			}
		} catch (TopologyException e) {
			System.out.println("polygon: " + polygon.getId());
			throw e;
		}

		// Union all the polygons to extract the isovist of incoming polygon points.
		Geometry union = null;
		for (Geometry geom : geometryCollection) {
			if (union == null)
				union = geom;
			else
				union = union.union(geom);
		}
		polygon.setIncomingIsovist(union);
	}

	/**
	 * Checks if the given polygon thisPolygon intersects with each of the scene polygons. If at least one intersection is found returns true.
	 * <p>
	 * This function considers that two geometries intersects only if they intersect but do not touch.
	 * </p>
	 * 
	 * @param generatedPolygon The polygon to check
	 * @return true if it intersects with at least one of the scene polygons
	 */

	// TODO: move this operation in the database and check if it speeds up the result.
	private boolean intersectsWithScenePolygons(Polygon generatedPolygon, BasicPolygon thisPolygon) {
		if (generatedPolygon.intersection(thisPolygon.getGeometry()).getArea() > 0.01)
			return true;

		final Polygon p = generatedPolygon;
		if (Double.compare(generatedPolygon.getArea(), 0) == 0) // Used for the isovist of allPoints
			return true;
		for (BasicPolygon polygon : allPolygons) {
			if (p.intersects(polygon.getGeometry()) && !p.touches(polygon.getGeometry())) {
				// System.out.println("Polygon: " + thisPolygon + "\totherPolygon" + polygon.getGeometry());
				try {
					Geometry intersection = p.intersection(polygon.getGeometry());
					// Due to high accuracy intersections occur when they shouldn't. Avoid them with the following if statement.
					if (Double.compare(intersection.getArea(), 0.00001) < 0)
						continue;
					else
						return true;
				} catch (TopologyException e) {
					System.out.println(polygon.getGeometry().toString() + "\t" + thisPolygon.toString());
					throw e;
				}
			}
		}
		return false;
	}
}
