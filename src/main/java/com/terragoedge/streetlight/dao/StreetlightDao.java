package com.terragoedge.streetlight.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terragoedge.streetlight.StreetlightDaoConnection;
import com.terragoedge.streetlight.entity.EdgeFormValues;
import com.terragoedge.streetlight.entity.EdgeNoteDetails;

public class StreetlightDao extends UtilDao {

	static final Logger logger = Logger.getLogger(StreetlightDao.class);
	private JsonParser jsonParser = null;

	public StreetlightDao() {
		super();
		jsonParser = new JsonParser();
		createStreetLightSyncTable();
	}
	
	
	private  void createStreetLightSyncTable(){
		PreparedStatement preparedStatement = null;
		try{
		String sql = "CREATE TABLE IF NOT EXISTS streetlightsync (streetlightsyncid integer NOT NULL,"
					+ " parentnoteid text, processednoteid integer, CONSTRAINT streetlightsync_pkey PRIMARY KEY (streetlightsyncid));";
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.execute();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(preparedStatement != null){
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Get List of NoteIds which is assigned to given formtemplate
	 * 
	 * @param formTemplateGuid
	 * @return
	 */
	public List<String> getNoteIds(String formTemplateGuid) {
		Statement queryStatement = null;
		ResultSet queryResponse = null;
		List<String> noteIds = new ArrayList<>();
		try {
			queryStatement = connection.createStatement();
			queryResponse = queryStatement.executeQuery(
					"SELECT edgenoteentity_noteid FROM edgeform WHERE formtemplateguid = '" + formTemplateGuid + "'");

			while (queryResponse.next()) {
				noteIds.add(queryResponse.getString("edgenoteentity_noteid"));
			}

		} catch (Exception e) {
			logger.error("Error in getNoteIds", e);
		} finally {
			closeResultSet(queryResponse);
			closeStatement(queryStatement);
		}
		return noteIds;
	}

	/**
	 * Check whether given noteid is present in streetlight or not
	 * @param noteId
	 * @return
	 */
	public boolean isNotePresent(String noteId) {
		// return false;
		PreparedStatement preparedStatement = null;
		ResultSet queryResponse = null;
		try {
			preparedStatement = connection
					.prepareStatement("SELECT * from streetlightsync WHERE processednoteid =" + noteId);
			ResultSet noteIdResponse = preparedStatement.executeQuery();
			return noteIdResponse.next();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeResultSet(queryResponse);
			closeStatement(preparedStatement);
		}
		return false;
	}

	public boolean isFormTemplateAssigned(String formTemplateGuid, String parentNoteId) {
		PreparedStatement preparedStatement = null;
		ResultSet queryResponse = null;
		try {
			preparedStatement = connection.prepareStatement(
					"SELECT ef.formtemplatedef,ef.formdef,ef.formtemplateguid, en.title, en.parentnoteid, en.noteid, en.geojson, en.createddatetime FROM edgeform ef, edgenote en WHERE ef.formtemplateguid = '"
							+ formTemplateGuid + "' and ef.edgenoteentity_noteid =  en.noteid and en.noteguid = '"
							+ parentNoteId + "'");
			ResultSet noteIdResponse = preparedStatement.executeQuery();
			return noteIdResponse.next();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeResultSet(queryResponse);
			closeStatement(preparedStatement);
		}
		return false;
	}

	public Map<String, EdgeNoteDetails> getFormData(String noteId) {
		Statement queryStatement = null;
		ResultSet queryResponse = null;
		Map<String, EdgeNoteDetails> results = new HashMap<>();
		try {
			queryStatement = connection.createStatement();
			queryResponse = queryStatement.executeQuery(
					"SELECT ef.formtemplatedef,ef.formdef,ef.formtemplateguid, en.title, en.noteid, en.parentnoteid, en.geojson, en.createddatetime FROM edgeform ef, edgenote en WHERE ef.edgenoteentity_noteid ="
							+ noteId + "  and en.noteid = " + noteId
							+ " and en.iscurrent = true and en.isdeleted = false ");

			while (queryResponse.next()) {
				EdgeNoteDetails edgeNoteDetails = new EdgeNoteDetails();

				EdgeFormValues edgeFormValues = new EdgeFormValues();
				edgeNoteDetails.setEdgeFormValues(edgeFormValues);

				populateValues(queryResponse, edgeNoteDetails);
				results.put(edgeFormValues.getFormTemplateGuid(), edgeNoteDetails);
			}

		} catch (Exception e) {
			logger.error("Error in getFormData", e);
		} finally {
			closeResultSet(queryResponse);
			closeStatement(queryStatement);
		}
		return results;

	}
	
	
	public void getFormData(String richFormTemplateGuid,String parentNoteId,Map<String, EdgeNoteDetails> formDetailsHolder) {
		Statement queryStatement = null;
		ResultSet queryResponse = null;
		try {
			queryStatement = connection.createStatement();
			queryResponse = queryStatement.executeQuery(
					"SELECT ef.formtemplatedef,ef.formdef,ef.formtemplateguid, en.title, en.parentnoteid, en.noteid, en.geojson, en.createddatetime FROM edgeform ef, edgenote en WHERE ef.formtemplateguid = '" + richFormTemplateGuid +  "' and ef.edgenoteentity_noteid =  en.noteid and en.noteguid = '" + parentNoteId + "'");

			while (queryResponse.next()) {
				EdgeNoteDetails edgeNoteDetails = new EdgeNoteDetails();

				EdgeFormValues edgeFormValues = new EdgeFormValues();
				edgeNoteDetails.setEdgeFormValues(edgeFormValues);

				populateValues(queryResponse, edgeNoteDetails);
				formDetailsHolder.put(edgeFormValues.getFormTemplateGuid(), edgeNoteDetails);
			}

		} catch (Exception e) {
			logger.error("Error in getFormData", e);
		} finally {
			closeResultSet(queryResponse);
			closeStatement(queryStatement);
		}

	}
	
	/**
	 * Check whether given parentNoteId is present in streetlightsync or not
	 * @param parentNoteId
	 * @return
	 */
	public boolean isBaseParentNoteIdPresent(String parentNoteId) {
		PreparedStatement preparedStatement = null;
		Connection connection = null;
		try {
			connection = StreetlightDaoConnection.getInstance().getConnection();
			preparedStatement = connection
					.prepareStatement("SELECT * from streetlightsync WHERE parentnoteid ='" + parentNoteId+"'");
			ResultSet noteIdResponse = preparedStatement.executeQuery();
			return noteIdResponse.next();
		} catch (Exception e) {
			logger.error("Error in isBaseParentNoteIdPresent",e);
		} finally {
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	

	private void populateValues(ResultSet queryResponse, EdgeNoteDetails edgeNoteDetails) throws Exception {
		edgeNoteDetails.getEdgeFormValues().setFormTempalteDef(queryResponse.getString("formtemplatedef"));
		edgeNoteDetails.getEdgeFormValues().setFormDef(queryResponse.getString("formdef"));
		edgeNoteDetails.getEdgeFormValues().setFormTemplateGuid(queryResponse.getString("formtemplateguid"));
		edgeNoteDetails.setTitle(queryResponse.getString("title"));
		edgeNoteDetails.setNoteId(queryResponse.getString("noteid"));
		edgeNoteDetails.setParentNoteId(queryResponse.getString("parentnoteid"));
		edgeNoteDetails.setGeoJson(queryResponse.getString("geojson"));
		edgeNoteDetails.setCreatedDateTime(queryResponse.getString("createddatetime"));
		if(edgeNoteDetails.getGeoJson() != null){
			JsonObject geoJsonData = (JsonObject) jsonParser.parse(edgeNoteDetails.getGeoJson());
			JsonObject geometryData = geoJsonData.getAsJsonObject();
			JsonObject data = geometryData.get("geometry").getAsJsonObject();
			JsonArray coordinates = data.get("coordinates").getAsJsonArray();
			String latitude = coordinates.get(1).getAsString();
			String longitude = coordinates.get(0).getAsString();
			
			edgeNoteDetails.setLat(latitude);
			edgeNoteDetails.setLng(longitude);
		}
		
	}
	
	
	public  void insertParentNoteId(String parentNoteId) {
		PreparedStatement preparedStatement = null;
		Connection connection = null;
		try {
			String sql = "SELECT max(streetlightsyncid) from streetlightsync";
			long maxStreetLight = exceuteSql(sql);
			if (maxStreetLight == -1) {
				maxStreetLight = 1;
			} else {
				maxStreetLight += 1;
			}
			connection = StreetlightDaoConnection.getInstance().getConnection();
			preparedStatement = connection.prepareStatement("INSERT INTO streetlightsync (streetlightsyncid , parentnoteid) VALUES ("+maxStreetLight+",'" + parentNoteId + "')");
			preparedStatement.execute();
		} catch (Exception e) {
			logger.error("Error in insertParentNoteId",e);
		} finally {
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public  void insertProcessedNoteId(Integer noteId) {
		PreparedStatement preparedStatement = null;
		Connection connection = null;
		try {
			String sql = "SELECT max(streetlightsyncid) from streetlightsync";
			long maxStreetLight = exceuteSql(sql);
			if (maxStreetLight == -1) {
				maxStreetLight = 1;
			} else {
				maxStreetLight += 1;
			}
			connection = StreetlightDaoConnection.getInstance().getConnection();
			preparedStatement = connection.prepareStatement("INSERT INTO streetlightsync (streetlightsyncid , processednoteid) VALUES ("+maxStreetLight+"," + noteId + ")");
			preparedStatement.execute();
		} catch (Exception e) {
			logger.error("Error in insertParentNoteId",e);
		} finally {
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	private static long exceuteSql(String sql) {
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
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return -1;
	}
	
	

	public void updateParentNoteId(String parentNoteId, String noteid) {
		PreparedStatement preparedStatement = null;
		Connection connection = null;
		try {
			connection = StreetlightDaoConnection.getInstance().getConnection();
			preparedStatement = connection.prepareStatement(
					"UPDATE streetlightsync SET processednoteid = '" + noteid + "' WHERE parentnoteid = '" + parentNoteId+"' ;");
			preparedStatement.execute();
		} catch (Exception e) {
			logger.error("Error in updateParentNoteId",e);
		} finally {
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}



}
