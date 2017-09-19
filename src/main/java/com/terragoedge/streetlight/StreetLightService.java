package com.terragoedge.streetlight;

import java.io.File;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.xml.devices.SLVDevice;
import com.terragoedge.xml.devices.SLVDeviceArray;
import com.terragoedge.xml.devices.SLVKeyValuePair;
import com.terragoedge.xml.devices.Value;
import com.terragoedge.xml.parser.XMLMarshaller;

public class StreetLightService {

	JsonParser jsonParser = null;
	Gson gson = null;
	Properties properties = null;
	String mappingFilePath;
	String propertiesPath;
	HashMap<String, SLVDevice> devices = new HashMap<String, SLVDevice>();
	ArrayList<String> batchIdList = new ArrayList<>();
	
	static Map<String,Integer> luminareCore  =new HashMap<>();
	
	static{
		luminareCore.put("L1",39);
		luminareCore.put("L2",58);
		luminareCore.put("L3",27);
		luminareCore.put("L4",80);
		luminareCore.put("L5",108);
		luminareCore.put("L6",108);
		luminareCore.put("L7",133);
		luminareCore.put("L8",158);
		luminareCore.put("L9",80);
		luminareCore.put("L10",133);
		luminareCore.put("L11",108);
		luminareCore.put("L12",158);
		luminareCore.put("L13",80);
		
	}

	private static Logger logger = Logger.getLogger(StreetLightService.class);

	public StreetLightService(String mappingPath, String propertiesPath) {
		this.mappingFilePath = mappingPath;
		this.propertiesPath = propertiesPath;
		jsonParser = new JsonParser();
		gson = new Gson();
		properties = PropertiesReader.getProperties(propertiesPath);
	}

	public void sendFromData(List<FormValue> forms, String latitude, String longitude, String createdTime, String title,
			String noteid) throws Exception {
		try {
			String lat = latitude;
			String lng = longitude;
			Type listType = new TypeToken<ArrayList<EdgeFormData>>() {
			}.getType();
			JsonObject mappingJson = getStreetLightMappingJson();
			List<StreetLightData> streetLightDatas = new ArrayList<>();
			String idonController = null;
			String macAddress = null;
			String comment = "";
			String block = null;
			int power2Watt  = 0;
			String luminareCoreValueLog = null;
			int lWatt = 0;
			String parentNoteId = null;
			for (FormValue fv : forms) {
				String formData = fv.getFormdata();
				if(parentNoteId == null){
					parentNoteId = fv.getParentnoteid();
				}
				
				List<EdgeFormData> edgeFormDataList = gson.fromJson(formData, listType);
				if (edgeFormDataList != null) {
					for (EdgeFormData edgeFormData : edgeFormDataList) {
						JsonElement streetLightKey = mappingJson.get(edgeFormData.getLabel());
						if (streetLightKey != null && !streetLightKey.isJsonNull()) {
							String key = streetLightKey.getAsString();
							String value = edgeFormData.getValue();
							if (edgeFormData.getLabel().equals("SELC QR Code")) {
								if (value == null) {
									logger.warn("Not Processed because value is empty");
									return;
								}
								value = value.trim();
								if (value.isEmpty() || value.equalsIgnoreCase("(null)")) {
									logger.warn("Not Processed because value is empty");
									return;
								}
							}
							if(edgeFormData.getLabel().equals("New Luminare Code")){
								luminareCoreValueLog = value;
								if (value != null && !(value.trim().isEmpty()) && !(value.trim().equalsIgnoreCase("(null)"))){
									String[] luminareCoreValues = value.split("-");
									if(luminareCoreValues.length > 0){
										String luminareCoreValue = luminareCoreValues[0].trim();
										if(luminareCore.containsKey(luminareCoreValue)){
											lWatt = luminareCore.get(luminareCoreValue);
										}
									}
								}
							}
							StreetLightData streetLightData = new StreetLightData();
							streetLightData.setKey(key);
							streetLightData.setValue(value);
							if (key.equalsIgnoreCase("idOnController")) {
								idonController = value;
								if (value.isEmpty() || value.equalsIgnoreCase("(null)")) {
									logger.warn("Not Processed because idonController value is empty. NoteId:"+noteid+"-"+title);
									return;
								}
							}
							if (key.equalsIgnoreCase("power2")) {
								if (value != null && !(value.trim().isEmpty()) && !(value.trim().equalsIgnoreCase("(null)"))) {
									String temp = value.replaceAll("[^\\d.]", "");
									temp = temp.trim();
									power2Watt = Integer.parseInt(temp);
								}
								
							}
							if (key.equalsIgnoreCase("location.mapnumber")) {
								block = value;
							}
							if (key.equalsIgnoreCase("MacAddress")) {
								macAddress = value;
							}
							if (key.equalsIgnoreCase("comment")) {
								comment = comment + " " + edgeFormData.getLabel() + ":" + value;
							} else {
								if (key.equalsIgnoreCase("power")) {
									if (value != null && !(value.trim().isEmpty()) && !(value.trim().equalsIgnoreCase("(null)"))) {
										String temp = value.replaceAll("[^\\d.]", "");
										temp = temp.trim();
										lWatt = Integer.parseInt(temp);
									}
									
								} else {
									streetLightDatas.add(streetLightData);
								}
							}
						}
					}
				}
			}
			
			if (streetLightDatas.size() > 0) {
				if(lWatt == 0){
					addStreetLightData("power", "39 W", streetLightDatas);
					lWatt = 39;
				}else{
					addStreetLightData("power", lWatt+" W", streetLightDatas);
				}
				int watt = power2Watt - lWatt;
				addStreetLightData("powerCorrection", watt + "", streetLightDatas);
				addStreetLightData("location.utillocationid", title + ".Lamp", streetLightDatas);
				String nodeTypeStrId = properties.getProperty("streetlight.equipment.type");
				addStreetLightData("modelFunctionId", nodeTypeStrId, streetLightDatas);

				addStreetLightData("comment", comment, streetLightDatas);

				String streetLightDate = dateFormat(createdTime);
				addStreetLightData("lamp.installdate", streetLightDate, streetLightDatas);

				String blockSuffix = block;
				String blockName = "Block " + blockSuffix;
				sendData(streetLightDatas, blockName, macAddress, title, idonController, parentNoteId, noteid, lat, lng);
				
			}

		} catch (Exception e) {
			logger.error("Error in sendFromData",e);
		}
	}
	
	// edge revision note slv -> 
	private void sendData(List<StreetLightData> streetLightDatas,String blockName,String macAddress,String title,String idonController,String parentNoteId,String noteid,String lat, String lng) throws Exception{
		SLVDevice slvDevice = devices.get(macAddress.trim().toLowerCase());

		if (slvDevice == null) {
			String blocNameResponse = getChildrenGeoZone(blockName);
			if (!isBaseParentNoteIdPresent(parentNoteId)) {
				logger.info("Given NoteGuid "+parentNoteId+"-"+title+" is not present in db.");
				logger.info("Create Device Called.");
				ResponseEntity<String> createresponseEntity = createDevice(idonController, blocNameResponse,
						lat, lng, macAddress.trim());
				
				String status = createresponseEntity.getStatusCode().toString();
				String responseBody = createresponseEntity.getBody();
				if ((status.equalsIgnoreCase("200") || status.equalsIgnoreCase("ok")) && !responseBody.contains("<status>ERROR</status>")) {
					logger.info("Device Created Successfully, NoteId:"+noteid+"-"+title);
					insertParentNoteId(parentNoteId);
				}else{
					logger.info("Device  Not Created Successfully, NoteId:"+noteid+"-"+title);
					logger.info("Note Not Synced with StreetLight Server. NoteId:"+noteid+"-"+title);
					//return;
				}
			}else{
				logger.info("Given NoteGuid "+parentNoteId+"-"+title+" is already present in db.");
			}
			SLVDevice slvDeviceTemp = new SLVDevice();
			devices.put(macAddress.trim().toLowerCase(), slvDeviceTemp);
			updateDeviceData(streetLightDatas, idonController, title, parentNoteId, noteid);
		/*	ResponseEntity<String> responseEntity = updateDeviceData(streetLightDatas, idonController);
			if (responseEntity != null) {
				String setDeviceResponse = setCommissionController(idonController);
				if (responseEntity.getStatusCode().value() == 200) {
					logger.info("Note Synced with StreetLight Server. NoteId:"+noteid+"-"+title);
					updateParentNoteId(parentNoteId, noteid);
				}else{
					logger.info("Note Not Synced with StreetLight Server. NoteId:"+noteid+"-"+title);
					logger.info("Response Code:"+responseEntity.getStatusCode().value());
					logger.info("Response Body:"+responseEntity.getBody());
				}
			}else{
				logger.info("Note Not Synced with StreetLight Server. NoteId:"+noteid+"-"+title);
			}*/

		} else {
			logger.info("Mac Address is already present " + macAddress.trim());
			/*logger.info("Mac Address is already present . Device Update Called" + macAddress.trim());
			StringBuffer sb = new StringBuffer();
			sb.append("idonController:"+idonController);
			sb.append("title:"+idonController);
			sb.append("parentNoteId:"+idonController);
			sb.append("noteid:"+noteid);
			logger.info("Data :"+sb.toString());
			updateDeviceData(streetLightDatas, idonController, title, parentNoteId, noteid);*/
		}
	}
	
	private void updateDeviceData(List<StreetLightData> streetLightDatas,String idonController,String title,String parentNoteId,String noteid) throws Exception{
		ResponseEntity<String> responseEntity = updateDeviceData(streetLightDatas, idonController);
		if (responseEntity != null) {
			String setDeviceResponse = setCommissionController(idonController);
			if (responseEntity.getStatusCode().value() == 200) {
				logger.info("Note Synced with StreetLight Server. NoteId:"+noteid+"-"+title);
				updateParentNoteId(parentNoteId, noteid);
			}else{
				logger.info("Note Not Synced with StreetLight Server. NoteId:"+noteid+"-"+title);
				logger.info("Response Code:"+responseEntity.getStatusCode().value());
				logger.info("Response Body:"+responseEntity.getBody());
			}
		}else{
			logger.info("Note Not Synced with StreetLight Server. NoteId:"+noteid+"-"+title);
		}
	}

	private void addStreetLightData(String key, String value, List<StreetLightData> streetLightDatas) {
		StreetLightData streetLightPowerData = new StreetLightData();
		streetLightPowerData.setKey(key);
		streetLightPowerData.setValue(value);
		streetLightDatas.add(streetLightPowerData);
	}

	private static boolean isBaseParentNoteIdPresent(String parentNoteId) {
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

	private static void insertParentNoteId(String parentNoteId) {
		PreparedStatement preparedStatement = null;
		Connection connection = null;
		try {
			String sql = "SELECT max(streetlightsyncid) from streetlightsync";
			long maxStreetLight = exceuteSequence(sql);
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
	
	
	private static long exceuteSequence(String sql) {
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


	private static void updateParentNoteId(String parentNoteId, String noteid) {
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

	public String getChildrenGeoZone(String blockName) {
		String mainUrl = properties.getProperty("streetlight.url.main");
		String geoUrl = properties.getProperty("streetlight.url.children.geoZone");
		String url = mainUrl + geoUrl;
		String methodName = properties.getProperty("streetlight.children.geoZone.methodName");
		String geoZoneId = properties.getProperty("streetlight.url.getChildrenDevice.zoneid");
		List<Object> paramData = new ArrayList<Object>();
		paramData.add("methodName=" + methodName);
		paramData.add("geoZoneId=" + geoZoneId);
		paramData.add("ser=json");
		String params = StringUtils.join(paramData, "&");
		url = url + "?" + params;
		ResponseEntity<String> responseEntity = getRequest(url,true);
		String response = responseEntity.getBody();
		JsonReader jsonReader = new JsonReader(new StringReader(response));
		jsonReader.setLenient(true);
		JsonParser jsonParser = new JsonParser();
		JsonArray geoZone = (JsonArray) jsonParser.parse(jsonReader);
		for (JsonElement slvGeoZoneDevice : geoZone) {
			JsonObject jsonGeoDevice = slvGeoZoneDevice.getAsJsonObject();
			String geoBlockName = jsonGeoDevice.get("name").getAsString();
			if (geoBlockName.equals(blockName)) {
				String zoneId = jsonGeoDevice.get("id").getAsString();
				return zoneId;
			} else {
				continue;
			}
		}
		return null;
	}

	public ResponseEntity<String> createDevice(String idonController, String zoneId, String lat, String lng,
			String macAddress) {
		String mainUrl = properties.getProperty("streetlight.url.main");
		String serveletApiUrl = properties.getProperty("streetlight.url.device.create");
		String url = mainUrl + serveletApiUrl;
		String methodName = properties.getProperty("streetlight.url.device.create.methodName");
		String categoryStrId = properties.getProperty("streetlight.categorystr.id");
		String controllerStrId = properties.getProperty("streetlight.controllerstr.id");
		String nodeTypeStrId = properties.getProperty("streetlight.equipment.type");
		Map<String, String> streetLightDataParams = new HashMap<>();
		streetLightDataParams.put("methodName", methodName);
		streetLightDataParams.put("categoryStrId", categoryStrId);
		streetLightDataParams.put("controllerStrId", controllerStrId);
		streetLightDataParams.put("idOnController", idonController);
		streetLightDataParams.put("geoZoneId", zoneId);
		streetLightDataParams.put("nodeTypeStrId", nodeTypeStrId);
		streetLightDataParams.put("lat", lat);
		streetLightDataParams.put("lng", lng);
		return getRequest(streetLightDataParams, url,true);

	}

	public ResponseEntity<String> updateDeviceData(List<StreetLightData> streetLightDatas, String idOnController)
			throws Exception {
		if (streetLightDatas != null && streetLightDatas.size() > 0) {
			String mainUrl = properties.getProperty("streetlight.url.main");
			String dataUrl = properties.getProperty("streetlight.data.url");
			String insertDevice = properties.getProperty("streetlight.url.device.insert.device");
			String url = mainUrl + dataUrl;
			String controllerStrId = properties.getProperty("streetlight.controllerstr.id");
			List<Object> paramsList = new ArrayList<Object>();
			paramsList.add("methodName=" + insertDevice);
			paramsList.add("controllerStrId=" + controllerStrId);
			paramsList.add("idOnController=" + idOnController);
			for (StreetLightData streetLightData : streetLightDatas) {
				paramsList.add("valueName=" + streetLightData.getKey());
				paramsList.add("value=" + streetLightData.getValue());
			}
			String params = StringUtils.join(paramsList, "&");
			url = url + "?" + params;
			ResponseEntity<String> response = getRequest(url,true);
			return response;
		}
		return null;
	}

	private String setCommissionController(String idOnController) {
		try{
			String controllerStrId = properties.getProperty("streetlight.controllerstr.id");
			ResponseEntity<String> controllerResponse = afterSetDevices(controllerStrId, idOnController);
			String response = controllerResponse.getBody();
			String statusResponse = controllerResponse.getStatusCode().toString();
			JsonParser jsonParser = new JsonParser();
			JsonObject slvBatchDeviceData = (JsonObject) jsonParser.parse(response);
			String batchIdResponse = slvBatchDeviceData.get("batchId").getAsString();
			batchIdList.add(batchIdResponse);
			return statusResponse;
		}catch(Exception e){
			logger.error("Error in setCommissionController",e);
		}
		return null;
	}

	public ResponseEntity<String> afterSetDevices(String controllerStrId, String idOnController) {
		String mainUrl = properties.getProperty("streetlight.url.main");
		String controllerUrl = properties.getProperty("streetlight.url.setDevice.controller.api");
		String json = properties.getProperty("streetlight.url.controller.ser");
		String url = mainUrl + controllerUrl;
		List<Object> paramData = new ArrayList<Object>();
		paramData.add("controllerStrId=" + controllerStrId);
		paramData.add("idOnController=" + idOnController);
		paramData.add("ser=" + json);
		String params = StringUtils.join(paramData, "&");
		url = url + "?" + params;
		return getPostRequest(url);
	}

	private String getStreetLightMappingData() throws Exception {
		String mappingPath = properties.getProperty("streetlight.mapping.json.path");
		File file = new File(mappingFilePath + "/" + mappingPath);
		String streetLightMappingData = FileUtils.readFileToString(file);
		return streetLightMappingData;
	}

	private JsonObject getStreetLightMappingJson() throws Exception {
		String streetLightMappingData = getStreetLightMappingData();
		return (JsonObject) jsonParser.parse(streetLightMappingData);
	}

	public int getDevices() {
		devices.clear();
		String mainUrl = properties.getProperty("streetlight.url.main");
		String deviceUrl = properties.getProperty("streetlight.url.getdevice");
		String methodName = properties.getProperty("streetlight.url.getdevice.methodName");
		String zoneId = properties.getProperty("streetlight.url.getDevice.zoneid");
		String controllerStrId = properties.getProperty("streetlight.controllerstr.id");
		String recurse = properties.getProperty("streetlight.url.getDevices.recurse");
		String url = mainUrl + deviceUrl;
		Map<String, String> streetLightDataParams = new HashMap<String, String>();
		streetLightDataParams.put("methodName", methodName);
		streetLightDataParams.put("geoZoneId", zoneId);
		streetLightDataParams.put("controllerStrId", controllerStrId);
		streetLightDataParams.put("recurse", recurse);
		ResponseEntity<String> responseEntity = getRequest(streetLightDataParams, url,false);
		String response = responseEntity.getBody();
		XMLMarshaller xmlMarshaller = new XMLMarshaller();
		SLVDeviceArray slvDeviceArray = (SLVDeviceArray) xmlMarshaller.xmlToObject(response);
		List<SLVDevice> slvDevices = slvDeviceArray.getSLVDevice();
		for (SLVDevice slvDev : slvDevices) {
			com.terragoedge.xml.devices.Properties property = slvDev.getProperties();
			List<SLVKeyValuePair> slvKeyValueList = property.getSLVKeyValuePair();
			for (SLVKeyValuePair slvKey : slvKeyValueList) {
				String key = slvKey.getKey().trim().toLowerCase();
				Value value = slvKey.getValue(); // userproperty.MacAddress
				if (key.equalsIgnoreCase("userproperty.macaddress")) {
					devices.put(value.getContent().trim().toLowerCase(), slvDev);
				}
			}
		}

		return -1;
	}

	public void callBatchStatus() {
		Iterator<String> iter = batchIdList.iterator();
		while (iter.hasNext()) {
			String batchId = iter.next();
			ResponseEntity<String> batchResponseEntity = getBatchResult(batchId);
			String batchResponse = batchResponseEntity.getBody();
			JsonObject slvBatchRespone = (JsonObject) jsonParser.parse(batchResponse);
			String batchRunning = slvBatchRespone.get("batchRunning").getAsString();
			String status = slvBatchRespone.get("status").getAsString();
			if (batchRunning.equalsIgnoreCase("false")) {
				if (status.equalsIgnoreCase("ERROR")) {
					sendMail(batchResponse);
				}
				iter.remove();
			}
		}
	}

	public ResponseEntity<String> getBatchResult(String batchId) {
		String mainUrl = properties.getProperty("streetlight.url.main");
		String batchUrl = properties.getProperty("streelight.url.getDevice.batch");
		String json = properties.getProperty("streetlight.url.controller.ser");
		String url = mainUrl + batchUrl;
		List<Object> ParamData = new ArrayList<Object>();
		ParamData.add("batchId=" + batchId);
		ParamData.add("ser=" + json);
		String params = StringUtils.join(ParamData, "&");
		url = url + "?" + params;
		return getRequest(url,true);
	}

	private HttpHeaders getHeaders() {
		String userName = properties.getProperty("streetlight.username");
		String password = properties.getProperty("streetlight.password");
		String plainCreds = userName + ":" + password;
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Creds);
		return headers;
	}

	private <T> ResponseEntity<String> getRequest(Map<String, String> streetLightDataParams, String url,boolean isLog) {
		Set<String> keys = streetLightDataParams.keySet();
		List<String> values = new ArrayList<>();
		for (String key : keys) {
			String val = streetLightDataParams.get(key) != null ? streetLightDataParams.get(key).toString() : "";
			String tem = key + "=" + val;
			values.add(tem);
		}
		String params = StringUtils.join(values, "&");
		url = url + "?" + params;

		return getRequest(url,isLog);
	}

	private ResponseEntity<String> getRequest(String url,boolean isLog) {
		logger.info("------------ Request ------------------");
		logger.info(url);
		logger.info("------------ Request End ------------------");
		HttpHeaders headers = getHeaders();
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity request = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		logger.info("------------ Response ------------------");
		logger.info("Response Code:" + response.getStatusCode().toString());
		String responseBody = response.getBody();
		if(isLog){
			logger.info(responseBody);
		}
		
		logger.info("------------ Response End ------------------");
		// return responseBody;
		return response;
	}

	private ResponseEntity<String> getPostRequest(String url) {
		logger.info("------------ Request ------------------");
		logger.info(url);
		logger.info("------------ Request End ------------------");
		HttpHeaders headers = getHeaders();
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity request = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
		logger.info("------------ Response ------------------");
		logger.info("Response Code:" + response.getStatusCode().toString());
		String responseBody = response.getBody();
		logger.info(responseBody);
		logger.info("------------ Response End ------------------");
		return response;
	}

	private String dateFormat(String dateTime) {
		Date date = new Date(Long.valueOf(dateTime));
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
		String dff = dateFormat.format(date);
		return dff;
	}

	public void sendMail(String body) {
		Properties props = System.getProperties();
		final String fromEmail = properties.getProperty("email.id");
		final String emailPassword = properties.getProperty("email.password");
		String recipients = properties.getProperty("email.recipients");
		String host = properties.getProperty("email.host");
		String port = properties.getProperty("email.port");
		String[] to = recipients.split(",", -1);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.user", fromEmail);
		props.put("mail.smtp.password", emailPassword);
		props.put("mail.smtp.port", port);
		props.put("mail.smtp.auth", "true");
		String subject = "Error At Street Light Vision Commision report";
		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromEmail, emailPassword);
			}
		});
		MimeMessage message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress(fromEmail));
			InternetAddress[] toAddress = new InternetAddress[to.length];
			for (int i = 0; i < to.length; i++) {
				toAddress[i] = new InternetAddress(to[i]);
			}
			for (int i = 0; i < toAddress.length; i++) {
				message.addRecipient(Message.RecipientType.TO, toAddress[i]);
			}
			message.setSubject(subject);
			message.setText(body);
			Transport transport = session.getTransport("smtp");
			transport.connect(host, fromEmail, emailPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
		} catch (AddressException ae) {
			ae.printStackTrace();
		} catch (MessagingException me) {
			me.printStackTrace();
		}
	}
}
