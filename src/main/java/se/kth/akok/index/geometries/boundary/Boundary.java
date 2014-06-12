package se.kth.akok.index.geometries.boundary;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * Represents the geometry of the bounding box of the scene.
 * 
 * @author Andreas Kokkalis
 * 
 */
public class Boundary {
	private LineSegment minX, maxX, minY, maxY;
	private ArrayList<LineString> boundingBox;
	private ArrayList<Point> boundaryPoints;

	public Boundary(LineSegment minX, LineSegment maxX, LineSegment minY, LineSegment maxY) {
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;

		this.boundingBox = new ArrayList<LineString>();
		GeometryFactory factory = new GeometryFactory();
		this.boundingBox.add(minX.toGeometry(factory));
		this.boundingBox.add(maxX.toGeometry(factory));
		this.boundingBox.add(minY.toGeometry(factory));
		this.boundingBox.add(maxY.toGeometry(factory));

		this.boundaryPoints = new ArrayList<Point>();
		for (LineString lineString : this.boundingBox) {
			Coordinate coordinates[] = lineString.getCoordinates();
			for (Coordinate c : coordinates)
				addBoundaryCoordinate(c);
		}
	}

	/**
	 * Iterates through the known coordinates to add the given one if it does not exist already.
	 * 
	 * @param coordinate The coordinate of the boundary point to add.
	 */
	private void addBoundaryCoordinate(Coordinate coordinate) {
		GeometryFactory factory = new GeometryFactory();
		boolean found = false;
		for (Point point : this.boundaryPoints) {
			Coordinate c = point.getCoordinate();
			if (c.x == coordinate.x && c.y == coordinate.y)
				found = true;
		}
		if (!found)
			boundaryPoints.add(factory.createPoint(coordinate));
	}

	/**
	 * Checks if the shadow point is on the bounding box. lineIntersection extends infinitely the line segments, and thus the projection maybe on a different edge of the
	 * bounding box than preferred.
	 * 
	 * @param ls the line segment representing the edge of the bounding box
	 * @param coordinate the coordinates of the intersection point
	 * @return true if coordinate is on line segment, else false
	 */
	public static boolean isPointOnBoundary(LineSegment ls, Coordinate coordinate) {
		// System.out.println("x: " + c.x + "\ty: " + c.y + "line: " + ls.toString());
		if (((ls.p0.x <= coordinate.x && coordinate.x <= ls.p1.x) && (ls.p0.y <= coordinate.y && coordinate.y <= ls.p1.y)) || ((ls.p1.x <= coordinate.x && coordinate.x <= ls.p0.x) && (ls.p1.y <= coordinate.y && coordinate.y <= ls.p0.y)))
			return true;
		else
			return false;
	}

	public LineSegment getMinX() {
		return minX;
	}

	public LineSegment getMaxX() {
		return maxX;
	}

	public LineSegment getMinY() {
		return minY;
	}

	public LineSegment getMaxY() {
		return maxY;
	}

	public ArrayList<LineString> getBoundingBox() {
		return boundingBox;
	}

	public ArrayList<Point> getBoundaryPoints() {
		return boundaryPoints;
	}
}
