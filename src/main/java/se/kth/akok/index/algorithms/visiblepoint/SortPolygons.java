/**
 * 
 */
package se.kth.akok.index.algorithms.visiblepoint;

import java.util.ArrayList;
import java.util.LinkedList;

import se.kth.akok.index.geometries.polygon.BasicPolygon;

import com.vividsolutions.jts.geom.Point;

/**
 * @author Andreas Kokkalis <a.kokkalis@kth.se>
 * 
 */
public class SortPolygons {
	private Point point;
	private LinkedList<BasicPolygon> polygons;
	
	/**
	 * 
	 */
	public SortPolygons(Point point, ArrayList<BasicPolygon> polygons) {
		this.polygons = new LinkedList<BasicPolygon>(polygons);
		this.point = point;
	}
	
	public LinkedList<BasicPolygon> sort() {
		quickSort(0, polygons.size() - 1);
		return this.polygons;
	}

	/**
	 * @param low Lower position of the partitioned array.
	 * @param high Higher position of the partitioned array.
	 */
	private void quickSort(int low, int high) {
		int i = low, j = high;
		// Get the pivot element from the middle of the list
		Double pivot = point.distance(polygons.get(low + (high - low) / 2).getGeometry());

		// Divide into two lists
		while (i <= j) {
			// If the current value from the left list is smaller then the pivot
			// element then get the next element from the left list
			while (Double.compare(point.distance(polygons.get(i).getGeometry()), pivot) > 0) {
				i++;
			}
			// If the current value from the right list is larger then the pivot
			// element then get the next element from the right list
			while (Double.compare(point.distance(polygons.get(j).getGeometry()), pivot) < 0) {
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
	private void exchange(int i, int j) {
		BasicPolygon temp = polygons.get(i);
		polygons.set(i, polygons.get(j));
		polygons.set(j, temp);
	}
}
