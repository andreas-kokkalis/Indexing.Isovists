package se.kth.akok.index.geometries.operations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class Intersects {
	private Connection connection;
	
	public Intersects(Connection connection) {
		this.connection = connection;
	}
	
	public int lineIntersectsWithBuildings(String tableName, LineString lineString, int SRID, int startGeom, int endGeom) {
		String query = "select count(*) from " + tableName + " b where ST_Intersects(b.way, ST_GeomFromText(?,?)) and b.osm_id <> ? and b.osm_id <> ?";
		
		int response = 0;
		try {
			PreparedStatement st = connection.prepareStatement(query);
			st.setString(1, lineString.toText());
			st.setInt(2, SRID);
			st.setInt(3, startGeom);
			st.setInt(4, endGeom);
			ResultSet results = st.executeQuery();
			results.next();

			response = results.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return response;
	}

	public Geometry getPolygonThatIntersects(String tableName, LineString lineString, int SRID, int startGeom) {
		String query = "select ST_AsText(b.way), st_SRID(b.way) from " + tableName + " b where ST_Intersects(b.way, ST_GeomFromText(?,?)) and b.osm_id <> ?";
		
		Geometry geometry = null;
		try {
			PreparedStatement st = connection.prepareStatement(query);
			st.setString(1, lineString.toText());
			st.setInt(2, SRID);
			st.setInt(3, startGeom);
			ResultSet results = st.executeQuery();
			results.next();
			String wktGeometry = results.getString(1);
			Integer srid = results.getInt(2);
			geometry = new WKTReader().read(wktGeometry);
			geometry.setSRID(srid);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return geometry;
	}
}
