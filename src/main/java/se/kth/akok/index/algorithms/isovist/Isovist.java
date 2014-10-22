package se.kth.akok.index.algorithms.isovist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;

import se.kth.akok.index.database.scene.SceneLoader;
import se.kth.akok.index.geometries.point.IncomingPoint;
import se.kth.akok.index.geometries.point.PolygonPoint;
import se.kth.akok.index.geometries.polygon.BasicPolygon;
import se.kth.akok.index.geometries.ray.Ray;

import com.google.common.collect.ArrayListMultimap;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
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
	private SceneLoader loader;
	private static double MIN_DISTANCE = 0.01;
	private static double MIN_AREA = 0.00001;

	/**
	 * Constructor of the Isovist class
	 * 
	 * @param allPolygons All polygons in the scene.
	 */
	public Isovist(ArrayList<BasicPolygon> allPolygons, BasicPolygon thisPolygon, SceneLoader loader) {
		this.allPolygons = allPolygons;
		this.thisPolygon = thisPolygon;
		this.loader = loader;
	}

	public void run() {
		// Stopwatch counting time for a given polygon.
		Stopwatch stopwatch = SimonManager.getStopwatch("thread_" + thisPolygon.getId());
		Split split = stopwatch.start();

		// Compute the regular polygon isovist
		polygonIsovist(thisPolygon);
		// Compute the incoming isovist
		if (!thisPolygon.getIncomingPoints().isEmpty())
			incomingIsovist(thisPolygon);

		// If polygon isovist is null, something went wrong, terminate.
		if (thisPolygon.getPolygonIsovist() == null) {
			String e = "EXCEPTION NULL REGULAR ISOVIST polygon " + thisPolygon.getGeometry().toString();
			System.out.println(e);
			throw (new NullPointerException(e));
		}

		// Merge incoming isovist with regular isovist.
		if (thisPolygon.getIncomingIsovist() != null) {
			Geometry finalIsovist = null;
			for (Geometry geom : thisPolygon.getIncomingIsovist()) {
				try {
					if (thisPolygon.getPolygonIsovist().contains(geom)) {
						continue;
					}
					if (finalIsovist == null)
						finalIsovist = thisPolygon.getPolygonIsovist().union(geom);
					else
						finalIsovist = finalIsovist.union(geom);
				} catch (IllegalArgumentException | TopologyException e1) {
					try {
						if (finalIsovist == null)
							finalIsovist = geom.union(thisPolygon.getPolygonIsovist());
						else
							finalIsovist = geom.union(finalIsovist);
					} catch (IllegalArgumentException | TopologyException e) {
						StringBuilder sb = new StringBuilder();
						sb.append("\n\n---------------------------------------\n");
						sb.append("Test scene: " + loader.getSceneName() + "\n");
						sb.append("---------------------------------------\n");
						sb.append("Cannot add INCOMING isovist: " + geom.toString() + "\n");
						sb.append("Polygon :" + thisPolygon.getId() + "\t" + thisPolygon.getGeometry().toString() + "\n");
						sb.append("Polygon isovist: " + thisPolygon.getPolygonIsovist() + "\n");
						sb.append("Union: " + finalIsovist + "\n");
						sb.append("---------------------------------------\n");
						sb.append(e.getMessage() + "\n");
						sb.append("---------------------------------------\n\n");
						loader.getLogFile().append(sb.toString());
//						System.out.println(sb.toString());
					}
				}
				if (finalIsovist != null) {
					if (finalIsovist.getGeometryType().equals("GeometryCollection")) {
						for (int i = 0; i < finalIsovist.getNumGeometries(); i++) {
							if (finalIsovist.getGeometryN(i).getGeometryType().equals("Polygon")) {
								finalIsovist = finalIsovist.getGeometryN(i);
								break;
							}
						}
					}
				}
			}
			thisPolygon.setFullIsovist(finalIsovist);
		} else
			thisPolygon.setFullIsovist(thisPolygon.getPolygonIsovist());

		// Add the split to the stopwatch that collects the results in the SceneBuilder
		split.stop();
		SimonManager.getStopwatch("se.kth.akok.index.scene.SceneBuilder-isovist").addSplit(split);

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

			// Compute the isovist of PolygonPoint point.
			pointIsovist(point);
			if (point.getPointIsovist() != null) {
				// Add the isovist only if it is not empty. An isovist can of a polygon can be empty if none of the rays can be combined to create a triangle.
				pointIsovist.add(point.getPointIsovist());
			}
		}

		Geometry theUnion = null;
		for (PolygonPoint point : polygon.getPolygonPoints()) {
			if (point.getPointIsovist() != null) {

				if (theUnion == null)
					theUnion = point.getPointIsovist();
				else {
					try {
						theUnion = theUnion.union(point.getPointIsovist());
					} catch (TopologyException e1) {
						// Try to do the union the opposite way.
						try {
							theUnion = point.getPointIsovist().union(theUnion);
						} catch (TopologyException e2) {
							StringBuilder sb = new StringBuilder();
							sb.append("\n\n---------------------------------------\n");
							sb.append("Test scene: " + loader.getSceneName() + "\n");
							sb.append("---------------------------------------\n");
							sb.append("Cannot add REGULAR isovist of point: " + point.getPoint().toString() + "\n");
							sb.append("Polygon :" + point.getPolygon().getId() + "\t" + point.getPolygon().getGeometry().toString() + "\n");
							sb.append("Isovist of point: " + point.getPointIsovist() + "\n");
							sb.append("Union: " + theUnion + "\n");
							sb.append("---------------------------------------\n");
							sb.append(e1.getMessage() + "\n");
							sb.append("---------------------------------------\n\n");
							loader.getLogFile().append(sb.toString());
							System.out.println(sb.toString());
						}
					}
				}
			}
			polygon.setPolygonIsovist(theUnion);
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
			return;
		}

		ArrayListMultimap<Double, Ray> allRays = ArrayListMultimap.create();
		ArrayList<Ray> checkedRays = new ArrayList<Ray>();
		for (Ray ray : startPoint.getAllRays()) {
			allRays.put(ray.getAngle(), ray);
		}

		LinkedList<Ray> myRays = startPoint.getAllRays();

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
						else {
							try {
								union = union.union(polygon);
							} catch (TopologyException e) {
								try {
									union = polygon.union(union);
								} catch (TopologyException e1) {
									loader.getLogFile().append("\nCould not add point isovist of: " + startPoint.getPoint().toString() + "\tisovist: " + polygon.toString());
									System.out.println("could not add point isovist of: " + startPoint.getPoint().toString() + "\tisovist: " + polygon.toString());
								}
							}
						}
						if (Double.compare(union.getArea(), MIN_AREA) < 0)
							union = null;
					}
				}
			}
			checkedRays.addAll(currentRays);
			if (pointIsovist == null && union != null)
				pointIsovist = union;
			else if (union != null && !pointIsovist.equals(union)) {
				try {
					pointIsovist = pointIsovist.union(union);
				} catch (TopologyException e1) {
					try {
						pointIsovist = union.union(pointIsovist);
					} catch (TopologyException e) {
						StringBuilder sb = new StringBuilder();
						sb.append("\n\n---------------------------------------\n");
						sb.append("Test scene: " + loader.getSceneName() + "\n");
						sb.append("---------------------------------------\n");
						sb.append("Cannot add isovist of rays current: " + currentRays.get(0).getLine().toString() + "rays next: " + nextRays.get(0).getLine().toString() + "\n");
						sb.append("Polygon :" + startPoint.getPolygon().getId() + "\t" + startPoint.getPolygon().getGeometry().toString() + "\n");
						sb.append("Isovist of point - so far: " + pointIsovist.toString() + "\n");
						sb.append("Triangle that failed: " + union.toString() + "\n");
						sb.append("---------------------------------------\n");
						sb.append(e.getMessage() + "\n");
						sb.append("---------------------------------------\n\n");
						loader.getLogFile().append(sb.toString());
						System.out.println(sb.toString());
					}
				}
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
	private List<Ray> getNextRayOf(Ray currentRay, LinkedList<Ray> myRays, ArrayListMultimap<Double, Ray> allRays) {
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
		for (IncomingPoint incomingPoint : polygon.getIncomingPoints()) {
			// if (!incomingPoint.getRay().getLine().toGeometry(factory).within(polygon.getPolygonIsovist())) {
			incomingRays.add(incomingPoint.getRay());
			// System.out.println(polygon.getIncomingPoints().indexOf(incomingPoint)+ ";" + incomingPoint.getRay().getLine().toString());
		}
		// }
		// try {
		ArrayListMultimap<Ray, Ray> checkedRays = ArrayListMultimap.create();
		// HashSet<Ray> checkedRays = new HashSet<Ray>();
		for (Ray outer : incomingRays) {
			for (Ray inner : incomingRays) {
				// Check only rays that where not checked from the outer for loop.
				if (outer.equals(inner) || checkedRays.containsEntry(outer, inner) || checkedRays.containsEntry(inner, outer))
					continue;

				// Check rays that their starting points are collinear and the line is a polygon edge
				LineSegment line = new LineSegment(outer.getLine().p0, inner.getLine().p0);
				Coordinate middleCoordinate = line.midPoint();
				Point middlePoint = factory.createPoint(middleCoordinate);
				// If the middle point of the line is not on the polygon, then inner_p0 and outer_p0 are not on the same polygon edge
				if (middlePointOnPolygonBoundary(middlePoint, polygon.getGeometry())) {
					LineString outerString = outer.getLine().toGeometry(factory);
					LineString innerString = inner.getLine().toGeometry(factory);
					// The two chosen line segments intersect. Two triangles are formed. (newPolygon1 and newPolygon2)
					if (outerString.intersects(innerString)) {
						Point point = null;
						try {
							point = (Point) outerString.intersection(innerString);

						} catch (ClassCastException e) {
							continue;
						}
						ArrayList<Coordinate> c1 = new ArrayList<Coordinate>();
						c1.add(outer.getLine().p0);
						c1.add(point.getCoordinate());
						c1.add(inner.getLine().p0);
						c1.add(outer.getLine().p0);
						Coordinate[] allCoordinates1 = c1.toArray(new Coordinate[c1.size()]);
						LinearRing linearRing1 = factory.createLinearRing(allCoordinates1);
						Polygon newPolygon1 = new Polygon(linearRing1, null, factory);

						if (newPolygon1.getArea() > MIN_AREA && !intersectsWithBuildings(newPolygon1, polygon))
							geometryCollection.add(newPolygon1);

						ArrayList<Coordinate> c2 = new ArrayList<Coordinate>();
						c2.add(outer.getLine().p1);
						c2.add(point.getCoordinate());
						c2.add(inner.getLine().p1);
						c2.add(outer.getLine().p1);
						Coordinate[] allCoordinates2 = c2.toArray(new Coordinate[c2.size()]);
						LinearRing linearRing2 = factory.createLinearRing(allCoordinates2);
						Polygon newPolygon2 = new Polygon(linearRing2, null, factory);

						if (newPolygon2.getArea() > MIN_AREA && !intersectsWithBuildings(newPolygon2, polygon))
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

						// if (newPolygon.isValid() && !intersectsWithScenePolygons(newPolygon, polygon))
						if (newPolygon.getArea() > MIN_AREA && !intersectsWithBuildings(newPolygon, polygon)) {
							geometryCollection.add(newPolygon);
						}
					}
				} 
				// Adds computation overhead - removed since the result is not affected.
				/*
				else {
					// trying to merge points that are not on the same edge
					LineString outerString = outer.getLine().toGeometry(factory);
					LineString innerString = inner.getLine().toGeometry(factory);
					if (!outerString.intersects(innerString)) {
						ArrayList<Coordinate> c = new ArrayList<Coordinate>();
						c.add(outer.getLine().p0);
						c.add(inner.getLine().p0);
						c.add(inner.getLine().p1);
						c.add(outer.getLine().p1);
						c.add(outer.getLine().p0);
						Coordinate[] allCoordinates = c.toArray(new Coordinate[c.size()]);
						LinearRing linearRing = factory.createLinearRing(allCoordinates);
						Polygon newPolygon = new Polygon(linearRing, null, factory);

						try {
							Geometry reflectionIsv = newPolygon.difference(polygon.getGeometry());
							if (reflectionIsv.getGeometryType().equals("Polygon") && !intersectsWithBuildings((Polygon) reflectionIsv, polygon))
								geometryCollection.add(newPolygon);
						} catch (TopologyException e) {
							// Could not create a valid polygon.
							continue;
						}
					}
				}
				*/
				checkedRays.put(outer, inner);
			}
		}

		// Remove invalid geometries
		ArrayList<Geometry> removeFromCollection = new ArrayList<Geometry>();
		for (Geometry geom : geometryCollection) {
			if (!geom.getGeometryType().equals("Polygon")) {
				removeFromCollection.add(geom);
			}
		}
		for (Geometry geom : removeFromCollection)
			geometryCollection.remove(geom);
		polygon.setIncomingIsovist(geometryCollection);
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

	private boolean intersectsWithScenePolygons(Polygon generatedPolygon, BasicPolygon thisPolygon) {
		if (generatedPolygon.intersection(thisPolygon.getGeometry()).getArea() > 0.01)
			return true;

		final Polygon p = generatedPolygon;
		if (Double.compare(generatedPolygon.getArea(), 0) == 0) // Used for the isovist of allPoints
			return true;
		for (BasicPolygon polygon : allPolygons) {
			if (p.intersects(polygon.getGeometry()) && !p.touches(polygon.getGeometry())) {
				try {
					Geometry intersection = p.intersection(polygon.getGeometry());
					// Due to high accuracy intersections occur when they shouldn't. Avoid them with the following if statement.
					if (Double.compare(intersection.getArea(), MIN_AREA) < 0)
						continue;
					else
						return true;
				} catch (TopologyException e) {
					try {
						Geometry intersection = polygon.getGeometry().intersection(p);
						if (Double.compare(intersection.getArea(), MIN_AREA) < 0)
							continue;
						else
							return true;
					} catch (TopologyException e1) {
						// If cannot perform intersection because of Topology exception, assume it intersects.
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Allows intersection to be a lineString which means it touches.
	 * 
	 * @param generatedPolygon
	 * @param thisPolygon
	 * @return
	 */
	private boolean intersectsWithBuildings(Polygon generatedPolygon, BasicPolygon thisPolygon) {
		for (BasicPolygon polygon : allPolygons) {
			if (polygon.equals(thisPolygon))
				continue;
			if (generatedPolygon.intersects(polygon.getGeometry()) && !generatedPolygon.touches(polygon.getGeometry())) {
				try {
					Geometry intersection = generatedPolygon.intersection(polygon.getGeometry());
					if (!intersection.getGeometryType().equals("LineString") || intersection.getArea() > MIN_AREA)
						return true;
				} catch (TopologyException e) {
					try {
						Geometry intersection = generatedPolygon.intersection(polygon.getGeometry());
						if (!intersection.getGeometryType().equals("LineString") || intersection.getArea() > MIN_AREA)
							return true;
					} catch (TopologyException e1) {
						// If cannot perform intersection because of Topology exception, assume it intersects.
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean middlePointOnPolygonBoundary(Point middlePoint, Geometry polygon) {
		Geometry boundary = polygon.getBoundary();
		if (middlePoint.touches(polygon))
			return true;
		if (middlePoint.touches(boundary))
			return true;
		if (middlePoint.isWithinDistance(boundary, MIN_DISTANCE))
			return true;
		return false;
	}
}
