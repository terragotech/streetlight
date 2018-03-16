package com.terragoedge.install;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terragoedge.streetlight.dao.UtilDao;

public class StreetLightInstallDAO extends UtilDao {
	
	public StreetLightInstallDAO(){
		super();
		createStreetLightSyncTable();
	}
	
	static final Logger logger = Logger.getLogger(StreetLightInstallDAO.class);

	private void createStreetLightSyncTable() {
		PreparedStatement preparedStatement = null;
		try {
			String sql = "CREATE TABLE IF NOT EXISTS streetlightinstallformsync (strtlightinstformsyncid integer NOT NULL,"
					+ " processednoteid integer, noteGuid text, title text, status text, actionType text, totalForms text, description text, createddatetime bigint  CONSTRAINT strtlightinstformsyncid_pkey PRIMARY KEY (strtlightinstformsyncid));";
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.execute();
		} catch (Exception e) {
			e.printStackTrace();
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

	public boolean isNoteSynced(String noteId) {
		PreparedStatement preparedStatement = null;
		ResultSet queryResponse = null;
		try {
			preparedStatement = connection
					.prepareStatement("SELECT * from streetlightinstallformsync WHERE processednoteid =" + noteId);
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

	/**
	 * Get List of NoteIds which is assigned to given formtemplate
	 * 
	 * @param formTemplateGuid
	 * @return
	 */
	public List<NoteDetails> getUnSyncedNoteIds(String notebookid) {
		Statement queryStatement = null;
		ResultSet queryResponse = null;
		List<NoteDetails> noteDetailsList = new ArrayList<>();
		JsonParser jsonParser = new JsonParser();
		try {

			queryStatement = connection.createStatement();
			queryResponse = queryStatement.executeQuery(
					"select title, noteguid, noteid,createddatetime, geojson from edgenote where iscurrent = true and isdeleted = false  and notebookid = "+notebookid+";");

			while (queryResponse.next()) {
				String noteId = queryResponse.getString("noteid");
				//boolean isSynced =  false;
				boolean isSynced = isNoteSynced(noteId);
				if (!isSynced) {
					NoteDetails noteDetails = new NoteDetails();
					noteDetails.setNoteid(noteId);
					noteDetails.setNoteGuid(queryResponse.getString("noteguid"));
					noteDetails.setGeojson(queryResponse.getString("geojson"));
					noteDetails.setTitle(queryResponse.getString("title"));
					noteDetails.setCreatedDateTime(queryResponse.getLong("createddatetime"));
					noteDetailsList.add(noteDetails);
					if (noteDetails.getGeojson() != null) {
						JsonObject geoJsonData = (JsonObject) jsonParser.parse(noteDetails.getGeojson());
						JsonObject geometryData = geoJsonData.getAsJsonObject();
						JsonObject data = geometryData.get("geometry").getAsJsonObject();
						JsonArray coordinates = data.get("coordinates").getAsJsonArray();
						String latitude = coordinates.get(1).getAsString();
						String longitude = coordinates.get(0).getAsString();
						noteDetails.setLat(latitude);
						noteDetails.setLng(longitude);
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeResultSet(queryResponse);
			closeStatement(queryStatement);
		}
		return noteDetailsList;
	}

	public void getFormDetails(NoteDetails noteDetails) {
		Statement queryStatement = null;
		ResultSet queryResponse = null;
		try {
			queryStatement = connection.createStatement();
			queryResponse = queryStatement.executeQuery(
					"select formdef,formtemplatedef,formGuid,formTemplateGuid from edgeform where edgenoteentity_noteid = "
							+ noteDetails.getNoteid());
			while (queryResponse.next()) {
				FormDetails formDetails = new FormDetails();
				formDetails.setFormDef(queryResponse.getString("formdef"));
				formDetails.setFormTemplateDef(queryResponse.getString("formtemplatedef"));
				formDetails.setFormGuid(queryResponse.getString("formGuid"));
				formDetails.setFormTemplateGuid(queryResponse.getString("formTemplateGuid"));
				noteDetails.getFormDetails().add(formDetails);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeResultSet(queryResponse);
			closeStatement(queryStatement);
		}

	}
	
	
	public void insertProcessedNotes(LoggingDetails loggingDetails){
		PreparedStatement preparedStatement = null;
		try{
			String sql = "SELECT max(strtlightinstformsyncid) + 1 from  streetlightinstallformsync;";
			long id = exceuteSql(sql);
			if(id == -1 || id == 0){
				id = 1; 
			}
			
			preparedStatement = connection.prepareStatement(
					"INSERT INTO streetlightinstallformsync (strtlightinstformsyncid , processednoteid, noteGuid,title,"
					+ "status, actionType,totalForms, description, createddatetime) values (?,?,?,?,?,?,?,?,?) ;");
			preparedStatement.setLong(1, id);
			preparedStatement.setInt(2, Integer.parseInt(loggingDetails.getNoteId()));
			preparedStatement.setString(3, loggingDetails.getNoteGuid());
			preparedStatement.setString(4, loggingDetails.getTitle());
			preparedStatement.setString(5, loggingDetails.getStatus());
			preparedStatement.setString(6, loggingDetails.getActionType());
			preparedStatement.setString(7, loggingDetails.getTotalForms());
			preparedStatement.setString(8, loggingDetails.getDescription());
			preparedStatement.setLong(9, loggingDetails.getCreateDateTime());
			preparedStatement.execute();
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("Error in insertProcessedNotes",e);
		} finally {
			closeStatement(preparedStatement);
		}
	}
	
	
	private long exceuteSql(String sql) {
		Statement statement = null;
		try {
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

}
