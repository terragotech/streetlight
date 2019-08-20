package com.terragoedge.streetlight;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StreetlightDaoConnection {
	private static StreetlightDaoConnection dbInstance;
	private static Connection connection;

	private StreetlightDaoConnection() {
		// private constructor //
	}

	public static StreetlightDaoConnection getInstance() {
		if (dbInstance == null) {
			dbInstance = new StreetlightDaoConnection();
		}
		return dbInstance;
	}

	public Connection getConnection() {
		if (connection == null) {
			try {
				String host = "jdbc:postgresql://localhost:5432/terragoedge";
				//String host = "jdbc:postgresql://localhost:5432/terragoedge_staging";
				String username = "postgres";
				String password = "password";
				connection = DriverManager.getConnection(host, username, password);
			} catch (SQLException ex) {
				Logger.getLogger(StreetlightDaoConnection.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		return connection;

	}
}
