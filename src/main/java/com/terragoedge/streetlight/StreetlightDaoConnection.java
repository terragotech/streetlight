package com.terragoedge.streetlight;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class StreetlightDaoConnection {
	static Connection connection = null;
	  static Connection getConnection() throws Exception {

	        String url = "jdbc:postgresql://localhost:5432";
	        String dbName = "/terragoedge";
	        String driver = "org.postgresql.Driver";
	        String userName = "postgres";
	        String password = "password";
	        Class.forName(driver).newInstance();
	        Connection connection = DriverManager.getConnection(url + dbName, userName,password);
//		  try {
//				Class.forName("org.postgresql.Driver");
//			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
//				return;
//			}
//	        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/terragoedge", "postgres",
//	    			"password");
//	    	connection.setAutoCommit(false);
	        return connection;
	    }
	  public static void closeConnection(Connection connection) {

	        try {
	        	connection.close();
	        } catch (SQLException e) {

	        }

	    }
//	  
	
}
