package se.kth.akok.index.algorithms.isovist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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

		polygonIsovist(thisPolygon);
		if (!thisPolygon.getIncomingPoints().isEmpty())
			incomingIsovist(thisPolygon);

		if (thisPolygon.getPolygonIsovist() == null) {
			String e = "EXCEPTION NULL REGULAR ISOVIST polygon " + thisPolygon.getGeometry().toString();
			System.out.println(e);
			throw (new NullPointerException(e));
		}

		if (thisPolygon.getIncomingIsovist() != null) {
			Geometry finalIsovist = null;
			for (Geometry geom : thisPolygon.getIncomingIsovist()) {
				try {
					if (thisPolygon.getPolygonIsovist().contains(geom))
						continue;
					finalIsovist = thisPolygon.getPolygonIsovist().union(geom);
				} catch (IllegalArgumentException | TopologyException e1) {
					try{
						finalIsovist = geom.union(thisPolygon.getPolygonIsovist());
					}catch(IllegalArgumentException | TopologyException e) {
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
						System.out.println(sb.toString());
					}
				}
			}if(finalIsovist != null) {
				if(finalIsovist.getGeometryType().equals("GeometryCollection")) {
					for(int i=0; i<finalIsovist.getNumGeometries(); i++) {
						if(finalIsovist.getGeometryN(i).getGeometryType().equals("Polygon")) {
							finalIsovist = finalIsovist.getGeometryN(i);
							break;
						}
					}
				}
			}
			else
				finalIsovist = thisPolygon.getPolygonIsovist();
			thisPolygon.setFullIsovist(finalIsovist);
			
			if(finalIsovist.getGeometryType().equals("GeometryCollection")) {
				String e = "EXCEPTION GEOMETRY COLLECTION FINAL ISOVIST polygon " + thisPolygon.getGeometry().toString();
				System.out.println(e);
				throw (new NullPointerException(e));
			}
			
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

//		// TODO: change this to be done manually for each polygon point IsoVist
//		Geometry[] polygonIsovist = pointIsovist.toArray(new Geometry[pointIsovist.size()]);
//		GeometryCollection collection = new GeometryCollection(polygonIsovist, factory);
//		Geometry union = null;
//		try {
//			union = collection.union();
//			polygon.setPolygonIsovist(union);
//		} catch (TopologyException e) {
//			}
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
						}catch(TopologyException e2) {
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
			System.out.println("isovist not comutable: point\t" + startPoint.getPoint() + "\tpolygon\t" + startPoint.getPolygon().getGeometry().toString());
			return;
		}

		ArrayListMultimap<Double, Ray> allRays = ArrayListMultimap.create();
		ArrayList<Ray> checkedRays = new ArrayList<Ray>();
		for (Ray ray : startPoint.getAllRays()) {
			allRays.put(ray.getAngle(), ray);
		}

		// if (startPoint.getPoint().toText().equals("POINT (2004233.38 8252313.35)")) {
		// Set<Entry<Double, Collection<Ray>>> set = allRays.asMap().entrySet();
		// for (Entry entry : set) {
		// Collection<Ray> theRays = (Collection<Ray>) entry.getValue();
		// System.out.println("---------------");
		// for (Ray ray : theRays)
		// System.out.println(startPoint.getAllRays().indexOf(ray) + "\t" + ray.getAngle() + "\t" + ray.getLine().toString());
		// }
		// }

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
						else
							union = union.union(polygon);
						if (Double.compare(union.getArea(), 0.01) < 0)
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
						try{
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
						//throw e;
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
							System.out.println("Casting intersection to point: " + intersection.toString() + "\touter: " + outer.getLine().toString() + "\tinner: " + inner.getLine().toString());
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
		
		ArrayList<Geometry> removeFromCollection = new ArrayList<Geometry>();
		
		for(Geometry geom: geometryCollection) {
			if(Double.compare(geom.getArea(), MIN_AREA) <= 0) {
				removeFromCollection.add(geom);
			}
			else if(!geom.getGeometryType().equals("Polygon")) {
				removeFromCollection.add(geom);
			}
		}
		
		for(Geometry geom: removeFromCollection)
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
				// System.out.println("Polygon: " + thisPolygon + "\totherPolygon" + polygon.getGeometry());
				try {
					Geometry intersection = p.intersection(polygon.getGeometry());
					// Due to high accuracy intersections occur when they shouldn't. Avoid them with the following if statement.
					if (Double.compare(intersection.getArea(), MIN_AREA) < 0)
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
