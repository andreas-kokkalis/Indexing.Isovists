package se.kth.akok.index.database.scene;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
	private String dbName;
	private static String dbHost = "localhost";
	private static String dbUser = "gis";
	private static String dbPassword = "gis";
	private Connection connection;

	public DatabaseConnection(String dbName) {
		this.dbName = dbName;
		connection = null;
		try {
			Class.forName("org.postgresql.Driver");
			connection = DriverManager.getConnection("jdbc:postgresql://" + dbHost + "/" + this.dbName, dbUser, dbPassword);
			((org.postgresql.PGConnection) connection).addDataType("geometry", Class.forName("org.postgis.PGgeometry"));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		return connection;
	}
}
