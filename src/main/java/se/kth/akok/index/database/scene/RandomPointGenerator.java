package se.kth.akok.index.database.scene;

import java.util.ArrayList;

import se.kth.akok.index.geometries.polygon.BasicPolygon;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class RandomPointGenerator {
	private ArrayList<BasicPolygon> polygons;
	private Polygon polygonalMask;

	public RandomPointGenerator(ArrayList<BasicPolygon> polygons, ArrayList<Point> boundaryPoints) {
		this.polygons = polygons;

		ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
		coordinates.add(boundaryPoints.get(0).getCoordinate());
		coordinates.add(boundaryPoints.get(1).getCoordinate());
		coordinates.add(boundaryPoints.get(2).getCoordinate());
		coordinates.add(boundaryPoints.get(3).getCoordinate());
		coordinates.add(boundaryPoints.get(0).getCoordinate());
		Coordinate[] allCoordinates = coordinates.toArray(new Coordinate[coordinates.size()]);
		GeometryFactory factory = new GeometryFactory();
		LinearRing linearRing = factory.createLinearRing(allCoordinates);
		polygonalMask = new Polygon(linearRing, null, factory);

	}

	public ArrayList<Point> generatePoints(int numPoints) {
		GeometryFactory factory = new GeometryFactory();
		Envelope env = polygonalMask.getEnvelopeInternal();
		ArrayList<Point> points = new ArrayList<Point>();
		
		while(points.size() < numPoints) {
			
			double x = Math.floor((env.getMinX() + Math.random()*env.getWidth())* 100) / 100;
			double y = Math.floor((env.getMinY() + Math.random()*env.getHeight())* 100) / 100;
			Point newPoint = factory.createPoint(new Coordinate(x, y));
			
			if(!pointsContain(x, y, points) && !pointWithinPolygons(newPoint)) {
				points.add(newPoint);
			}
		}
		return points;
	}

	private boolean pointsContain(double x, double y, ArrayList<Point> points) {
		for(Point point: points) {
			if(Double.compare(point.getX(), x) == 0 && Double.compare(point.getY(), y) == 0)
				return true;
		}
		return false;
	}
	
	private boolean pointWithinPolygons(Point point) {
		for(BasicPolygon polygon: polygons) {
			if(point.within(polygon.getGeometry()))
				return true;
		}
		return false;
	}
}
