package com.terragoedge.streetlight.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.terragoedge.streetlight.StreetlightDaoConnection;

public class StreetlightDao extends UtilDao {

	static final Logger logger = Logger.getLogger(StreetlightDao.class);

	public StreetlightDao() {
		super();
		createStreetLightSyncTable();
	}
	
	
	private void executeStatement(String sql) {
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.execute();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeStatement(preparedStatement);
		}
	}
	
	
	private long exceuteSql(String sql) {
		Statement statement = null;
		Connection connection = null;
		try {
			connection = StreetlightDaoConnection.getInstance().getConnection();
			statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			while(resultSet.next()){
				long res = resultSet.getLong(1);
				return res;
			}
		} catch (Exception e) {
			logger.error("Error in exceuteSequence",e);
		} finally {
			closeStatement(statement);
		}
		return -1;
	}

	private void createStreetLightSyncTable() {
		String sql = "CREATE TABLE IF NOT EXISTS notesyncdetails (streetlightsyncid integer NOT NULL,"
				+ " processednoteid text, status text, errordetails text, createddatetime bigint, notename text, CONSTRAINT notesyncdetails_pkey PRIMARY KEY (streetlightsyncid));";
		executeStatement(sql);
		
		//sql = "CREATE TABLE IF NOT EXISTS lastsyncstatus (lastsyncstatusid integer not null, lastsynctime text, CONSTRAINT lastsyncstatus_pkey PRIMARY KEY (lastsyncstatusid))";
	}
	
	
	
	public long getLastSyncTime(){
		String sql = "select lastsynctime from lastsyncstatus;";
		return exceuteSql(sql);
	}
	
	
	public void updateSyncTime(String syncTime){
		PreparedStatement preparedStatement = null;
		Connection connection = null;
		try {
			connection = StreetlightDaoConnection.getInstance().getConnection();
			preparedStatement = connection.prepareStatement(
					"UPDATE lastsyncstatus SET lastsynctime = ? ;");
			preparedStatement.setString(1, syncTime);
			preparedStatement.execute();
		} catch (Exception e) {
			logger.error("Error in updateSyncTime",e);
		} finally {
			closeStatement(preparedStatement);
		}
	}
	
	
	public void insertProcessedNoteGuids(String noteGuid,String status,String errorDetails,long createdDateTime,String noteName){
		PreparedStatement preparedStatement = null;
		Connection connection = null;
		try {
			String sql = "SELECT max(streetlightsyncid) + 1 from  notesyncdetails";
			long id = exceuteSql(sql);
			if(id == -1 || id == 0){
				id = 1; 
			}
			connection = StreetlightDaoConnection.getInstance().getConnection();
			preparedStatement = connection.prepareStatement(
					"INSERT INTO notesyncdetails (streetlightsyncid , processednoteid, status,errordetails,createddatetime, notename ) values (?,?,?,?,?,?) ;");
			preparedStatement.setLong(1, id);
			preparedStatement.setString(2, noteGuid);
			preparedStatement.setString(3, status);
			preparedStatement.setString(4, errorDetails);
			preparedStatement.setLong(5, createdDateTime);
			preparedStatement.setString(6, noteName);
			preparedStatement.execute();
		} catch (Exception e) {
			logger.error("Error in insertParentNoteId",e);
		} finally {
			closeStatement(preparedStatement);
		}
	}
	
	
	
	

	/**
	 * Get List of NoteIds which is assigned to given formtemplate
	 * 
	 * @param formTemplateGuid
	 * @return
	 */
	public List<String> getNoteIds() {
		Statement queryStatement = null;
		ResultSet queryResponse = null;
		List<String> noteIds = new ArrayList<>();
		try {
			queryStatement = connection.createStatement();
			queryResponse = queryStatement.executeQuery("Select processednoteid from notesyncdetails;");
			
			while (queryResponse.next()) {
				noteIds.add(queryResponse.getString("processednoteid"));
			}

		} catch (Exception e) {
			logger.error("Error in getNoteIds", e);
		} finally {
			closeResultSet(queryResponse);
			closeStatement(queryStatement);
		}
		return noteIds;
	}

	

}
