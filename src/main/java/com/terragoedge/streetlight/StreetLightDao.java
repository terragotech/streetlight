package com.terragoedge.streetlight;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class StreetLightDao {
	static Connection connection = null;
	static Statement queryStatement = null;
	static StreetLightService slService = null;
	static String resPath = null;

	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		resPath = new File("").getAbsolutePath();
//		try {
//			Class.forName("org.postgresql.Driver");
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//			return;
//		}
		try {
			connection = StreetlightDaoConnection.getInstance().getConnection();
			slService = new StreetLightService(resPath + "/resources", resPath + "/resources");
//			connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/terragoedge", "postgres",
//					"password");
//			connection.setAutoCommit(false);
			createStreetLightSyncTable();
			queryStatement = connection.createStatement();
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				public void run() {
					ResultSet queryResponse = null;
					ResultSet paramData = null;
					try {
						slService.getDevices();
						Properties properties = PropertiesReader.getProperties(resPath + "/resources");
						String formtemplateguid = properties.getProperty("streetlight.formtemplateguid.create");
						
						queryResponse = queryStatement
								.executeQuery("SELECT edgenoteentity_noteid FROM edgeform WHERE formtemplateguid = '"
										+ formtemplateguid + "'");
						
						System.out.println(queryResponse);
						ArrayList<String> noteIds = new ArrayList<String>();
						while (queryResponse.next()) {
							noteIds.add(queryResponse.getString("edgenoteentity_noteid"));
						}
						for (String noteid : noteIds) {
							if(!isNotePresent(noteid)){
								List<FormValue> forms = new ArrayList<FormValue>();
								NoteValue noteValue = new NoteValue();
								
								paramData = queryStatement.executeQuery(
										"SELECT ef.formtemplatedef,ef.formdef,ef.formtemplateguid, en.title, en.noteid, en.parentnoteid, en.geojson, en.createddatetime FROM edgeform ef, edgenote en WHERE ef.edgenoteentity_noteid ="
												+ noteid + "  and en.noteid = " + noteid + " and en.iscurrent = true and en.isdeleted = false ");
								
								getFormData(paramData, noteValue, forms);
								paramData.close();
								if(forms.size() == 1){
									loadBlockForm(forms, formtemplateguid,  noteValue);
								}
								slService.sendFromData(forms, noteValue.getLatitude(), noteValue.getLongitude(), noteValue.getCreatedDate(),noteValue.getTitle());
							}
						}

					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						closeResultSet(paramData);
						closeResultSet(queryResponse);
					}
				}
			}, 0, 30000);
			Timer timer2 = new Timer();
			timer2.scheduleAtFixedRate(new TimerTask() {
				public void run() {
					slService.callBatchStatus();
				}
			}, 0, 10000);
		} catch (Exception e) {
			//catch (SQLException e) {
			e.printStackTrace();
			return;
		} finally {
			//StreetlightDaoConnection.closeConnection(connection);
			// queryStatement.close();
			// connection.close();
		}
	}
	private static void createStreetLightSyncTable(){
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
	private static void closeResultSet(ResultSet resultSet){
		if(resultSet != null){
			try {
				resultSet.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	private static boolean isNotePresent(String noteId){
		PreparedStatement preparedStatement = null;
		try{
		preparedStatement = connection.prepareStatement("SELECT * from streetlightsync WHERE streetlightsyncid =" + noteId);
		ResultSet noteIdResponse = preparedStatement.executeQuery();
		return noteIdResponse.next();
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
		return false;
	}
	
	private static void getFormData(ResultSet paramData,NoteValue noteValue,List<FormValue> forms) throws Exception, SQLException{
		while (paramData.next()) {
			String noteid = null;
			try{
			    noteid = paramData.getString("noteid");
				String formTemplateDefData = paramData.getString("formtemplatedef");
				String formData = paramData.getString("formdef");
				String dbFormtemplateguid = paramData.getString("formtemplateguid");
				String geoJson = paramData.getString("geojson");
				String parentnoteid = paramData.getString("parentnoteid");
				String title = paramData.getString("title");
				String createdDate = paramData.getString("createddatetime");
				FormValue fv = new FormValue();
				fv.setFormdata(formData);
				fv.setFormdef(formTemplateDefData);
				fv.setFormTemplateGuid(dbFormtemplateguid);
				if(parentnoteid != null  && !parentnoteid.trim().isEmpty()){
					fv.setParentnoteid(parentnoteid);
				}
				JsonParser jsonParser = new JsonParser();
				if(geoJson != null){
					JsonObject geoJsonData = (JsonObject) jsonParser.parse(geoJson);
					JsonObject geometryData = geoJsonData.getAsJsonObject();
					JsonObject data = geometryData.get("geometry").getAsJsonObject();
					JsonArray coordinates = data.get("coordinates").getAsJsonArray();
					String latitude = coordinates.get(1).getAsString();
					String longitude = coordinates.get(0).getAsString();
					
					noteValue.setLatitude(latitude);
					noteValue.setLongitude(longitude);
				}
				noteValue.setCreatedDate(createdDate);
				noteValue.setTitle(title);
				forms.add(fv);
			}catch(Exception e){
				System.out.println("Error in getFormData, noteId:"+noteid);
				throw new Exception(e);
			}
		}
	}
	private static void loadBlockForm(List<FormValue> forms,String formtemplateguid,NoteValue noteValue) throws Exception{
		ResultSet paramData = null;
		try{
			FormValue fv = forms.get(0);
			if(fv.getFormTemplateGuid().equals(formtemplateguid)){
				Properties properties = PropertiesReader.getProperties(resPath + "/resources");
				String richFormTemplateGuid = properties.getProperty("streetlight.ef.formtemplateguid.update");
			    paramData = queryStatement.executeQuery(
						"SELECT ef.formtemplatedef,ef.formdef,ef.formtemplateguid, en.title, en.parentnoteid, en.noteid, en.geojson, en.createddatetime FROM edgeform ef, edgenote en WHERE ef.formtemplateguid = '" + richFormTemplateGuid +  "' and ef.edgenoteentity_noteid =  en.noteid and en.noteguid = '" + fv.getParentnoteid() + "'");
				
				getFormData(paramData, noteValue, forms);
			}
		}catch(Exception e){
			throw new Exception(e);
		}finally{
			closeResultSet(paramData);
		}
	}
	public String fetchData(ResultSet queryResponseData) {
		return null;
	}
}
