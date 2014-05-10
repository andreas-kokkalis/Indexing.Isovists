package se.kth.akok.index.database.scene;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import se.kth.akok.index.geometries.boundary.Boundary;
import se.kth.akok.index.geometries.point.PolygonPoint;
import se.kth.akok.index.geometries.polygon.BasicPolygon;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class SceneLoader {
	private String sceneName;
	private String buildingsTable;
	private String boundaryTable;
	private ArrayList<BasicPolygon> polygons;
	private ArrayList<PolygonPoint> points;
	private Boundary boundary;
	private Connection connection;

	public SceneLoader(String sceneName, String buildingsTable, String boundaryTable, Connection connection) {
		this.connection = connection;
		this.sceneName = sceneName;
		this.buildingsTable = buildingsTable;
		this.boundaryTable = boundaryTable;
		loadPolygons();
		loadBoundary();

		points = new ArrayList<PolygonPoint>();
		for (BasicPolygon polygon : polygons)
			points.addAll(polygon.getPolygonPoints());
	}

	private void loadPolygons() {
		ArrayList<BasicPolygon> loadedPolygons = new ArrayList<BasicPolygon>();
		try {
			String scenePolygons = "select b.osm_id, ST_AsText(b.way), st_SRID(b.way)  from " + buildingsTable + " b";
			PreparedStatement statement = connection.prepareStatement(scenePolygons, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet results = statement.executeQuery();

			results.last();
			if (results.getRow() == 0) {
				throw new NullPointerException("No polygons found with tableName: " + buildingsTable + " and scene name: " + sceneName);
			}

			results.beforeFirst();
			while (results.next()) {
				Integer id = results.getInt(1);
				String wktGeometry = results.getString(2);
				Integer srid = results.getInt(3);
				Geometry geometry = new WKTReader().read(wktGeometry);
				geometry.setSRID(srid);
				BasicPolygon polygon = new BasicPolygon(geometry);
				polygon.setId(id);
				loadedPolygons.add(polygon);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		this.polygons = loadedPolygons;
	}

	private void loadBoundary() {
		LineSegment minXSegment = null, maxXSegment = null, minYSegment = null, maxYSegment = null;
		try {
			String sceneBoundary = "select bounding_box from " + boundaryTable;
			PreparedStatement statement = connection.prepareStatement(sceneBoundary, ResultSet.CONCUR_READ_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE);
			ResultSet results = statement.executeQuery();

			results.next();
			String wktGeometry = results.getString(1);
			Geometry geometry = new WKTReader().read(wktGeometry);
			geometry.setSRID(900913);
			Coordinate coordinates[] = geometry.getCoordinates();
			LineSegment lineSegment1 = new LineSegment(coordinates[0], coordinates[1]);
			LineSegment lineSegment2 = new LineSegment(coordinates[1], coordinates[2]);
			LineSegment lineSegment3 = new LineSegment(coordinates[2], coordinates[3]);
			LineSegment lineSegment4 = new LineSegment(coordinates[3], coordinates[0]);

			double minX = 0, maxX = 0, minY = 0, maxY = 0;
			if (Double.compare(lineSegment1.p0.y, lineSegment1.p1.y) == 0) {
				if (Double.compare(lineSegment1.p0.x, lineSegment1.p1.x) > 0) {
					maxX = lineSegment1.p0.x;
					minX = lineSegment1.p1.x;
				} else {
					minX = lineSegment1.p0.x;
					maxX = lineSegment1.p1.x;
				}
			} else if (Double.compare(lineSegment2.p0.y, lineSegment2.p1.y) == 0) {
				if (Double.compare(lineSegment2.p0.x, lineSegment2.p1.x) > 0) {
					maxX = lineSegment2.p0.x;
					minX = lineSegment2.p1.x;
				} else {
					minX = lineSegment2.p0.x;
					maxX = lineSegment2.p1.x;
				}
			} else if (Double.compare(lineSegment3.p0.y, lineSegment3.p1.y) == 0) {
				if (Double.compare(lineSegment3.p0.x, lineSegment3.p1.x) > 0) {
					maxX = lineSegment3.p0.x;
					minX = lineSegment3.p1.x;
				} else {
					minX = lineSegment3.p0.x;
					maxX = lineSegment3.p1.x;
				}
			} else if (Double.compare(lineSegment4.p0.y, lineSegment4.p1.y) == 0) {
				if (Double.compare(lineSegment4.p0.x, lineSegment4.p1.x) > 0) {
					maxX = lineSegment4.p0.x;
					minX = lineSegment4.p1.x;
				} else {
					minX = lineSegment4.p0.x;
					maxX = lineSegment4.p1.x;
				}
			}

			if (Double.compare(lineSegment1.p0.x, lineSegment1.p1.x) == 0) {
				if (Double.compare(lineSegment1.p0.y, lineSegment1.p1.y) > 0) {
					maxY = lineSegment1.p0.y;
					minY = lineSegment1.p1.y;
				} else {
					minY = lineSegment1.p0.y;
					maxY = lineSegment1.p1.y;
				}
			} else if (Double.compare(lineSegment2.p0.x, lineSegment2.p1.x) == 0) {
				if (Double.compare(lineSegment2.p0.y, lineSegment2.p1.y) > 0) {
					maxY = lineSegment2.p0.y;
					minY = lineSegment2.p1.y;
				} else {
					minY = lineSegment2.p0.y;
					maxY = lineSegment2.p1.y;
				}
			} else if (Double.compare(lineSegment3.p0.x, lineSegment3.p1.x) == 0) {
				if (Double.compare(lineSegment3.p0.y, lineSegment3.p1.y) > 0) {
					maxY = lineSegment3.p0.y;
					minY = lineSegment3.p1.y;
				} else {
					minY = lineSegment3.p0.y;
					maxY = lineSegment3.p1.y;
				}
			} else if (Double.compare(lineSegment4.p0.x, lineSegment4.p1.x) == 0) {
				if (Double.compare(lineSegment4.p0.y, lineSegment4.p1.y) > 0) {
					maxY = lineSegment4.p0.y;
					minY = lineSegment4.p1.y;
				} else {
					minY = lineSegment4.p0.y;
					maxY = lineSegment4.p1.y;
				}
			}

			minXSegment = new LineSegment(minX, minY, minX, maxY);
			maxXSegment = new LineSegment(maxX, minY, maxX, maxY);
			minYSegment = new LineSegment(minX, minY, maxX, minY);
			maxYSegment = new LineSegment(minX, maxY, maxX, maxY);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		this.boundary = new Boundary(minXSegment, maxXSegment, minYSegment, maxYSegment);
	}

	public ArrayList<BasicPolygon> getPolygons() {
		return polygons;
	}

	public ArrayList<PolygonPoint> getPoints() {
		return points;
	}

	public Boundary getBoundary() {
		return boundary;
	}

	public String getBuildingsTable() {
		return buildingsTable;
	}

	public String getSceneName() {
		return sceneName;
	}
}
