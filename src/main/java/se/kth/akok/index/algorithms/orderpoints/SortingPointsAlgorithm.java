package se.kth.akok.index.algorithms.orderpoints;

import java.util.ArrayList;
import java.util.LinkedList;

import se.kth.akok.index.geometries.point.PolygonPoint;
import se.kth.akok.index.geometries.point.VisiblePoint;
import se.kth.akok.index.geometries.point.VisiblePointType;
import se.kth.akok.index.geometries.ray.Ray;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * SortingPointsAlgorithm sorts points in a clockwise order, using the angles of each ray from the X axis. It sorts VisiblePoints and shadowPoints for a specific
 * polygonPoint.
 * <p>
 * Added also visible boundary points, for polygons that are nearby the bounding boux.
 * </p>
 * 
 * @author Andreas Kokkalis
 * 
 */
public class SortingPointsAlgorithm {
	private LinkedList<Ray> allRays;
	private static double MIN_DISTANCE = 0.01;

	/**
	 * @param startPoint
	 * @throws Exception
	 */
	public void sortPointsFor(PolygonPoint startPoint) {

		GeometryFactory factory = new GeometryFactory();
		// Define adjacent edges. A ray from a to b is a polygon edge, if point a+b/2 is also on the polygon.
		ArrayList<VisiblePoint> adjacent = new ArrayList<VisiblePoint>();
		for (VisiblePoint visiblePoint : startPoint.getVisiblePoints()) {
			if (visiblePoint.getType().equals(VisiblePointType.SAME_OBJECT_VISIBLE)) {
				Coordinate middleCoordinate = visiblePoint.getRay().getLine().midPoint();
				Point middlePoint = factory.createPoint(middleCoordinate);
				if (startPoint.getPolygon().getGeometry().touches(middlePoint) || startPoint.getPolygon().getGeometry().isWithinDistance(middlePoint, MIN_DISTANCE)) {
					adjacent.add(visiblePoint);
				}
			}
		}
		startPoint.getAllRays().addAll(startPoint.getVisibleRays());
		startPoint.getAllRays().addAll(startPoint.getShadowRays());
		startPoint.getAllRays().addAll(startPoint.getBoundaryRays());
		this.allRays = startPoint.getAllRays();

		try {

			Ray ray1 = adjacent.get(0).getRay();
			Ray ray2 = adjacent.get(1).getRay();
			sort(ray1, ray2, startPoint);

		} catch (IndexOutOfBoundsException e) {
//			for (Ray ray : startPoint.getAllRays())
//				System.out.println(ray.getLine().toString());
//			System.out.println("visible points\n" + startPoint.getPolygon().getGeometry().toString());
//			for (Ray ray : startPoint.getVisibleRays()) {
//				VisiblePoint vp = (VisiblePoint) ray.getEndPoint();
//				System.out.println(ray.getLine().toString() + ";" + vp.getType().toString());
//			}
			startPoint.setPointIsovistNotComputable(true);
			return;
//			throw new NullPointerException("Could not identify the adjacent edges for the poinnt: " + startPoint.getPoint().toString() + " of polygon: " + startPoint.getPolygon().getGeometry().toString());
		}
	}

	/**
	 * Prepares the angles from x axis for each ray. Converts the angles from 0 to 2Π. Then it calls quicksort in descending order.
	 * 
	 * @param ray1 The first adjacent polygon edge
	 * @param ray2 The second adjacent polygon edge
	 */
	private void sort(Ray ray1, Ray ray2, PolygonPoint startPoint) {
		for (Ray ray : allRays) {
			if (ray.getLine().angle() < 0.0)
				ray.setAngle(ray.getLine().angle() + (2 * Math.PI));
			else
				ray.setAngle(ray.getLine().angle());
			// System.out.println(ray.getLine().angle() + "\t" + ray.getAngle());
		}
		// define min and man adjacent edges by angle
		Ray min = null;
		Ray max = null;
		if (Double.compare(ray1.getAngle(), ray2.getAngle()) < 0.0) {
			min = ray1;
			max = ray2;
		} else {
			min = ray2;
			max = ray1;
		}

		// Transform min to be 0. Subtract min from every one, and fix negatives again.
		max.setAngle(max.getAngle() - min.getAngle());
		for (Ray ray : allRays) {
			if (ray.equals(min) || ray.equals(max))
				continue;
			Double newAngle = ray.getAngle() - min.getAngle();
			double temp = 0;
			if (newAngle > 0)
				temp = (double) Math.floor(newAngle * 1000) / 1000;
			else
				temp = (double) Math.ceil(newAngle * 1000) / 1000;
			if (temp == 0.0)
				newAngle = 0.0;
			if (newAngle < 0.0)
				newAngle += (2 * Math.PI);
			// System.out.println(ray.getAngle() + "\t" + newAngle + "\t" + min.getAngle() + "\t" + max.getAngle());
			ray.setAngle(newAngle);
		}
		min.setAngle(0.0);

		//TODO: changing the accuracy
		for (Ray ray : allRays) {
			Double temp = Math.floor(ray.getAngle() * 100000.0) / 100000.0;
			ray.setAngle(temp);
		}
		// Remove digits from doubles.
//		for (Ray ray : allRays) {
//			Double temp = Math.floor(ray.getAngle() * 1000.0) / 1000.0;
//			ray.setAngle(temp);
//		}

		// Define if min is 0 or 2Π. This defines the clockwise order. If any angle is greater than max, then the order is from max to min, else from min to max.
		boolean zeroAs2Pi = false;
		for (Ray ray : allRays) {
			if (ray.equals(min) || ray.equals(max))
				continue;
			if (ray.getAngle().compareTo(max.getAngle()) > 0) {
				zeroAs2Pi = true;
				break;
			} else {
				zeroAs2Pi = false;
				break;
			}
		}
		if (zeroAs2Pi) {
			for (Ray ray : allRays) {
				if (ray.getAngle() == 0.0)
					ray.setAngle(2 * Math.PI);
			}
		}
		// Do the sorting.
		quickSort(0, this.allRays.size() - 1);
	}

	/**
	 * @param low Lower position of the partitioned array.
	 * @param high Higher position of the partitioned array.
	 */
	private void quickSort(int low, int high) {
		int i = low, j = high;
		// Get the pivot element from the middle of the list
		Double pivot = allRays.get(low + (high - low) / 2).getAngle();

		// Divide into two lists
		while (i <= j) {
			// If the current value from the left list is smaller then the pivot
			// element then get the next element from the left list
			while (Double.compare(allRays.get(i).getAngle(), pivot) > 0) {
				i++;
			}
			// If the current value from the right list is larger then the pivot
			// element then get the next element from the right list
			while (Double.compare(allRays.get(j).getAngle(), pivot) < 0) {
				j--;
			}

			// If we have found a values in the left list which is larger then
			// the pivot element and if we have found a value in the right list
			// which is smaller then the pivot element then we exchange the
			// values.
			// As we are done we can increase i and j
			if (i <= j) {
				exchange(i, j);
				i++;
				j--;
			}
		}
		// Recursion
		if (low < j)
			quickSort(low, j);
		if (i < high)
			quickSort(i, high);
	}

	/**
	 * @param i Position of element to swap with a[j]
	 * @param j Position of element to swap with a[i]
	 */
	private void exchange(int i, int j) {
		Ray temp = allRays.get(i);
		allRays.set(i, allRays.get(j));
		allRays.set(j, temp);
	}
}
