package se.kth.akok.index.geometries.operations;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class Touches {
	private static double MIN_DISTANCE = 0.01;

	/**
	 * Checks if the given line segment touches the given polygon. Because of increased accuracy of the JTS library, there are cases that a line segment instead of
	 * touching the polygon, it intersects it. In such case the intersection is also a linestring and the length is less than the minimum length of 1mm.
	 * <p>
	 * There is still the case where the intersection of the line with the polygon is an edge of the polygon, but still shows as an intersection. This function does not
	 * consider this scenario.
	 * </p>
	 * 
	 * @param lineString The lineString to check.
	 * @param polygon The polygon to check.
	 * @return Returns true if the line touches the polygon, else false.
	 */
	public static boolean lineTouchesWithPolygon(LineString lineString, Geometry polygon) {
		if (lineString.touches(polygon))
			return true;
		Geometry intersection = lineString.intersection(polygon);
		if (intersection.getLength() < MIN_DISTANCE)
			return true;
		return false;
	}
}
