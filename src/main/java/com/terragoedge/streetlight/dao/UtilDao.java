package com.terragoedge.streetlight.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.terragoedge.streetlight.StreetlightDaoConnection;

public abstract class UtilDao {
	
	Connection connection = null; 
	
	public UtilDao(){
		connection = StreetlightDaoConnection.getInstance().getConnection();
	}

	
	public void closeResultSet(ResultSet resultSet){
		if(resultSet != null){
			try {
				resultSet.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void closeStatement(Statement queryStatement){
		if(queryStatement != null){
			try {
				queryStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
}
