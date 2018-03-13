package com.terragoedge.install;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terragoedge.streetlight.dao.UtilDao;

public class StreetLightInstallDAO extends UtilDao{
	
	private  void createStreetLightSyncTable(){
		PreparedStatement preparedStatement = null;
		try{
		String sql = "CREATE TABLE IF NOT EXISTS streetlightinstallformsync (strtlightinstformsyncid integer NOT NULL,"
					+ " processednoteid integer, status text, operationtype text, error text, CONSTRAINT strtlightinstformsyncid_pkey PRIMARY KEY (strtlightinstformsyncid));";
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
	
	
	public boolean isNoteSynced(String noteId){
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
	public List<NoteDetails> getUnSyncedNoteIds() {
		Statement queryStatement = null;
		ResultSet queryResponse = null;
		List<NoteDetails> noteDetailsList = new ArrayList<>();
		JsonParser jsonParser = new JsonParser();
		try {
			
			queryStatement = connection.createStatement();
			queryResponse = queryStatement.executeQuery("select title, noteguid, noteid, geojson from edgenote where iscurrent = true and isdeleted = false;");
			
			while (queryResponse.next()) {
				String noteId = queryResponse.getString("noteid");
				boolean isSynced = isNoteSynced(noteId);
				if(!isSynced){
					NoteDetails noteDetails = new  NoteDetails();
					noteDetails.setNoteid(noteId);
					noteDetails.setNoteGuid(queryResponse.getString("noteguid"));
					noteDetails.setGeojson(queryResponse.getString("geojson"));
					noteDetails.setTitle(queryResponse.getString("title"));
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
			queryResponse = queryStatement.executeQuery("select formdef,formtemplatedef,formGuid,formTemplateGuid from edgeform where edgenoteentity_noteid = "+noteDetails.getNoteid());
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

}
