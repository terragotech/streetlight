package com.terragoedge.streetlight;

import java.io.File;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.edgeserver.EdgeFormData;

public class StreetLightDao {
	static Connection connection = null;
	static Statement queryStatement = null;
	static StreetLightService slService = null;
	static String resPath = null;
	
	static Logger logger = Logger.getLogger(StreetLightDao.class);

	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		resPath = new File("").getAbsolutePath();
		try {
			connection = StreetlightDaoConnection.getInstance().getConnection();
			slService = new StreetLightService(resPath + "/resources", resPath + "/resources");

			createStreetLightSyncTable();
			queryStatement = connection.createStatement();
			while(true){
				try{
					ResultSet queryResponse = null;
					ResultSet paramData = null;
					try {
						
						slService.getDevices();
						Properties properties = PropertiesReader.getProperties(resPath + "/resources");
						String formtemplateguid = properties.getProperty("streetlight.formtemplateguid.create");
						
						queryResponse = queryStatement
								.executeQuery("SELECT edgenoteentity_noteid FROM edgeform WHERE formtemplateguid = '"
										+ formtemplateguid + "'");
						List<String> noteIds = new ArrayList<>();
						while (queryResponse.next()) {
							noteIds.add(queryResponse.getString("edgenoteentity_noteid"));
						}
						
						//String[] noteIdTemp = {"55773","56558","56563","55772"};
						
						
						for (String noteid : noteIds) {
							if(!isNotePresent(noteid)){
								logger.info("Note id ["+noteid+" ] not yet processed." );
								List<FormValue> forms = new ArrayList<FormValue>();
								NoteValue noteValue = new NoteValue();
								
								paramData = queryStatement.executeQuery(
										"SELECT ef.formtemplatedef,ef.formdef,ef.formtemplateguid, en.title, en.noteid, en.parentnoteid, en.geojson, en.createddatetime FROM edgeform ef, edgenote en WHERE ef.edgenoteentity_noteid ="
												+ noteid + "  and en.noteid = " + noteid + " and en.iscurrent = true and en.isdeleted = false ");
								
								getFormData(paramData, noteValue, forms);
								paramData.close();
								logger.info("Total No of forms present:"+forms.size());
								if(forms.size() == 1){
									loadBlockForm(forms, formtemplateguid,  noteValue);
									logger.info("Total No of forms present (including base note):"+forms.size());
								}
								validateFormData(forms);
								if(forms.size() > 1){
									slService.sendFromData(forms, noteValue.getLatitude(), noteValue.getLongitude(), noteValue.getCreatedDate(),noteValue.getTitle(),noteid);
								}else{
									logger.info("Total No of forms present :"+forms.size());
									logger.info("Note id is ["+noteid+" ] not processed." );
								}
								
							}else{
								logger.info("Note id is ["+noteid+" ] already processed." );
							}
						}
						logger.info("----------Completed---------------");
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						closeResultSet(paramData);
						closeResultSet(queryResponse);
					}
				
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					Thread.sleep(3000);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			//StreetlightDaoConnection.closeConnection(connection);
			// queryStatement.close();
			// connection.close();
		}
	}
	
	
	private static void validateFormData(List<FormValue> forms){
		if (forms.size() > 2) {
			Gson gson = new Gson();
			List<Integer> position = new ArrayList<>();
			int i = -1;
			for (FormValue fv : forms) {
				String formData = fv.getFormdata();
				i += 1;
				Type listType = new TypeToken<ArrayList<EdgeFormData>>() {
				}.getType();

				List<EdgeFormData> edgeFormDataList = gson.fromJson(formData, listType);
				if (edgeFormDataList != null) {
					for (EdgeFormData edgeFormData : edgeFormDataList) {
						String value = edgeFormData.getValue();
						if (edgeFormData.getLabel().equals("SELC QR Code")) {
							logger.info("SELC QR Code val."+value);
							if (value == null) {
								logger.info("val is null.");
								position.add(i);
							} else {
								value = value.trim();
								if (value.isEmpty() || value.equalsIgnoreCase("(null)")) {
									logger.info("val is empty.");
									position.add(i);
								}
							}

						}
					}
				}
			}
			
			if(position.size() > 0){
				for(int pos : position){
					forms.remove(pos);
					logger.info("Position "+pos);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				}
			}

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
		//return false;
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
	
	
	private static boolean isFormTemplateAssigned(String formTemplateGuid,String parentNoteId){
		//return false;
		PreparedStatement preparedStatement = null;
		try{
		preparedStatement = connection.prepareStatement(
				"SELECT ef.formtemplatedef,ef.formdef,ef.formtemplateguid, en.title, en.parentnoteid, en.noteid, en.geojson, en.createddatetime FROM edgeform ef, edgenote en WHERE ef.formtemplateguid = '" + formTemplateGuid +  "' and ef.edgenoteentity_noteid =  en.noteid and en.noteguid = '" + parentNoteId + "'");
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
				String blockFormTemplates = properties.getProperty("streetlight.ef.formtemplateguid.update");
				String[] blockFormTemplatesList =  blockFormTemplates.split(",");
				String richFormTemplateGuid = null;
				for(String blockFormTemplate : blockFormTemplatesList){
					boolean res = isFormTemplateAssigned(blockFormTemplate,fv.getParentnoteid());
					if(res){
						richFormTemplateGuid = blockFormTemplate;
						logger.info("FormTemplate "+richFormTemplateGuid + " is Exists.");
						break;
					}
				}
				
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
