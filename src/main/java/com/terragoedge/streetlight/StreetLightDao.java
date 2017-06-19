package com.terragoedge.streetlight;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}
		try {
			slService = new StreetLightService(resPath + "/resources", resPath + "/resources");
			connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/terragoedge", "postgres",
					"password");
			connection.setAutoCommit(false);
			queryStatement = connection.createStatement();
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				public void run() {
					try {
						slService.getDevices();
						Properties properties = PropertiesReader.getProperties(resPath + "/resources");
						String formtemplateguid = properties.getProperty("streetlight.formtemplateguid.create");
						ResultSet queryResponse = queryStatement
								.executeQuery("SELECT edgenoteentity_noteid FROM edgeform WHERE formtemplateguid = '"
										+ formtemplateguid + "'");
						System.out.println(queryResponse);
						ArrayList<String> noteIds = new ArrayList<String>();
						while (queryResponse.next()) {
							noteIds.add(queryResponse.getString("edgenoteentity_noteid"));
						}
						for (String noteid : noteIds) {
							ArrayList<FormValue> forms = new ArrayList<FormValue>();
							String latitude = null;
							String longitude = null;
							String createdDate = null;
							ResultSet paramData = queryStatement.executeQuery(
									"SELECT ef.formtemplatedef,ef.formdef, en.geojson, en.createddatetime FROM edgeform ef, edgenote en WHERE ef.edgenoteentity_noteid ="
											+ noteid + "  and en.noteid = " + noteid + " and en.iscurrent = true and en.isdeleted = false ");
							
							while (paramData.next()) {
								String formTemplateDefData = paramData.getString("formtemplatedef");
								String formData = paramData.getString("formdef");
								String geoJson = paramData.getString("geojson");
								createdDate = paramData.getString("createddatetime");
								FormValue fv = new FormValue();
								fv.setFormdata(formData);
								fv.setFormdef(formTemplateDefData);
								JsonParser jsonParser = new JsonParser();
								JsonObject geoJsonData = (JsonObject) jsonParser.parse(geoJson);
								JsonObject geometryData = geoJsonData.getAsJsonObject();
								JsonObject data = geometryData.get("geometry").getAsJsonObject();
								JsonArray coordinates = data.get("coordinates").getAsJsonArray();
								latitude = coordinates.get(1).getAsString();
								longitude = coordinates.get(0).getAsString();
								forms.add(fv);
							}
							slService.sendFromData(forms, latitude, longitude, createdDate);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, 0, 30000);
			Timer timer2 = new Timer();
			timer2.scheduleAtFixedRate(new TimerTask() {
				public void run() {
					slService.callBatchStatus();
				}
			}, 0, 10000);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		} finally {
			// queryStatement.close();
			// connection.close();
		}
	}

	public String fetchData(ResultSet queryResponseData) {
		return null;
	}

}
