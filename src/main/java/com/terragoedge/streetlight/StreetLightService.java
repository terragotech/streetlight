package com.terragoedge.streetlight;

import java.io.File;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
	HashMap<String,SLVDevice> devices = new HashMap<String,SLVDevice>();
	ArrayList<String> batchIdList = new ArrayList<>();
	static Connection connection = null;
	static Statement queryStatement = null;
	public StreetLightService( String mappingPath, String propertiesPath) {
		this.mappingFilePath = mappingPath;
		this.propertiesPath = propertiesPath;
		jsonParser = new JsonParser();
		gson = new Gson();
		properties = PropertiesReader.getProperties(propertiesPath);
	}
	public void sendFromData(List<FormValue> forms, String latitude, String longitude, String createdTime,String title) throws Exception {	
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
			int watt = 0;
			String parentNoteId = null;
			for(FormValue fv:forms)  {
				String formData = fv.getFormdata();
				 parentNoteId = fv.getParentnoteid();
				List<EdgeFormData> edgeFormDataList = gson.fromJson(formData, listType);
				if (edgeFormDataList != null) {
					for (EdgeFormData edgeFormData : edgeFormDataList) {
						JsonElement streetLightKey = mappingJson.get(edgeFormData.getLabel());
						if (streetLightKey != null && !streetLightKey.isJsonNull()) {
							String key = streetLightKey.getAsString();
							String value = edgeFormData.getValue();
							if(edgeFormData.getLabel().equals("SELC QR Code")){
								if(value == null){
									System.out.println("Not Processed because value is empty");
									return;
								}
								value = value.trim();
								if(value.isEmpty() || value.equalsIgnoreCase("(null)")){
									System.out.println("Not Processed because value is empty");
									return;
								}
							}
							StreetLightData streetLightData = new StreetLightData();
							streetLightData.setKey(key);
							streetLightData.setValue(value);
							if(key.equalsIgnoreCase("idOnController")) {
								idonController = value;
							}
							if(key.equalsIgnoreCase("power2")) {
								watt = Integer.parseInt(value);
							}
							if(key.equalsIgnoreCase("location.mapnumber"))
							{
								block = value;
							}
							if(key.equalsIgnoreCase("MacAddress")){
								macAddress = value;
							}
							if(key.equalsIgnoreCase("comment")){
								comment = comment + " " + edgeFormData.getLabel() + ":" + value;
							} else {
								if(key.equalsIgnoreCase("power")){
									if(!(value.trim().isEmpty()) && !(value.trim().equalsIgnoreCase("(null)"))){
										streetLightDatas.add(streetLightData);
										watt = watt - Integer.parseInt(value.replaceAll("[^\\d.]", ""));
									}
								}else{
									streetLightDatas.add(streetLightData);
								}
							}
						}
					}
				}
			}
			if(streetLightDatas.size() > 0){
				StreetLightData streetLightPowerData = new StreetLightData();
				streetLightPowerData.setKey("powerCorrection");
				streetLightPowerData.setValue(watt+"");
				streetLightDatas.add(streetLightPowerData);
				
				
				StreetLightData streetLightLocData = new StreetLightData();
				streetLightLocData.setKey("location.utillocationid");
				streetLightLocData.setValue(title+".Lamp");
				streetLightDatas.add(streetLightLocData);
				
				//nodeTypeStrId
				StreetLightData streetLightNodeTypeData = new StreetLightData();
				streetLightNodeTypeData.setKey("modelFunctionId");
				String nodeTypeStrId = properties.getProperty("streetlight.equipment.type");
				streetLightNodeTypeData.setValue(nodeTypeStrId);
				streetLightDatas.add(streetLightNodeTypeData);
				
				
				StreetLightData streetLightData = new StreetLightData();
				streetLightData.setKey("comment");
				streetLightData.setValue(comment);
				streetLightDatas.add(streetLightData);
				StreetLightData streetLightCreatedTime = new StreetLightData();
				streetLightCreatedTime.setKey("lamp.installdate");
				String streetLightDate = dateFormat(createdTime);
				streetLightCreatedTime.setValue(streetLightDate);
				streetLightDatas.add(streetLightCreatedTime);
				String blockSuffix = block;
			   String blockName = "Block " + blockSuffix;
			   
			   SLVDevice slvDevice = devices.get(macAddress.trim());
			   if(slvDevice == null){
					String blocNameResponse = getChildrenGeoZone(blockName);
					if(!isBaseParentNoteIdPresent(parentNoteId)){
					ResponseEntity<String> createresponseEntity = createDevice(idonController, blocNameResponse, lat, lng,macAddress.trim());
					String status = createresponseEntity.getStatusCode().toString();
					if(status.equalsIgnoreCase("ok")){
						insertParentNoteId(parentNoteId);	
					}
					}
					String setDeviceResponse = SetCommissionController(idonController);
					if(setDeviceResponse.equalsIgnoreCase("ok")){
					updateDeviceData(streetLightDatas, idonController);
					}
					//SetCommissionController(idonController);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static boolean isBaseParentNoteIdPresent(String parentNoteId){
		PreparedStatement preparedStatement = null;
		try{
			connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/terragoedge", "postgres",
					"password");
			connection.setAutoCommit(false);
		preparedStatement = connection.prepareStatement("SELECT * from streetlightsync WHERE parentnoteid =" + parentNoteId);
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
	private static void insertParentNoteId(String parentNoteId){
		PreparedStatement preparedStatement = null;
		try{
			connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/terragoedge", "postgres",
					"password");
			connection.setAutoCommit(false);
		preparedStatement = connection.prepareStatement("INSERT INTO streetlightsync (parentnoteid) VALUES (" + parentNoteId + ")");
		preparedStatement.executeQuery();
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
	public String getChildrenGeoZone(String blockName){
		String mainUrl = properties.getProperty("streetlight.url.main");
		String geoUrl = properties.getProperty("streetlight.url.children.geoZone");
		String url = mainUrl + geoUrl;
		String methodName = properties.getProperty("streetlight.children.geoZone.methodName");
		String geoZoneId = properties.getProperty("streetlight.url.getChildrenDevice.zoneid");
		List<Object> paramData = new ArrayList<Object>();
		paramData.add("methodName=" + methodName);
		paramData.add("geoZoneId=" + geoZoneId );
		paramData.add("ser=json");
		String params = StringUtils.join(paramData,"&");
		url = url + "?" + params;
		ResponseEntity<String> responseEntity =  getRequest(url);
		String response = responseEntity.getBody();
		JsonReader jsonReader = new JsonReader(new StringReader(response));
		jsonReader.setLenient(true);
		JsonParser jsonParser = new JsonParser();
		JsonArray geoZone = (JsonArray) jsonParser.parse(jsonReader);
	for(JsonElement slvGeoZoneDevice : geoZone){
		JsonObject jsonGeoDevice = slvGeoZoneDevice.getAsJsonObject();
		String geoBlockName = jsonGeoDevice.get("name").getAsString();
		if(geoBlockName.equals(blockName)){
			String zoneId = jsonGeoDevice.get("id").getAsString();	
		return 	zoneId;
		}else{
			continue;
		}
		}
	return null;
	}
	public ResponseEntity<String> createDevice(String idonController, String zoneId, String lat, String lng,String macAddress) {
		System.out.println("createDevice_idonController:"+idonController);
		System.out.println("Current MacAddress:"+macAddress);
		String mainUrl = properties.getProperty("streetlight.url.main");
		String serveletApiUrl = properties.getProperty("streetlight.url.device.create");
		String url = mainUrl + serveletApiUrl;
		String methodName = properties.getProperty("streetlight.url.device.create.methodName");
		String categoryStrId = properties.getProperty("streetlight.categorystr.id");
		String controllerStrId = properties.getProperty("streetlight.controllerstr.id");
		String nodeTypeStrId = properties.getProperty("streetlight.equipment.type");
		System.out.println("nodeTypeStrId:"+nodeTypeStrId);
		Map<String, String> streetLightDataParams = new HashMap<>();
		streetLightDataParams.put("methodName", methodName);
		streetLightDataParams.put("categoryStrId", categoryStrId);
		streetLightDataParams.put("controllerStrId", controllerStrId);
		streetLightDataParams.put("idOnController", idonController);
		streetLightDataParams.put("geoZoneId", zoneId);
		streetLightDataParams.put("nodeTypeStrId", nodeTypeStrId);
		streetLightDataParams.put("lat", lat);
		streetLightDataParams.put("lng", lng);
	    return getRequest(streetLightDataParams, url);
	    
	}
	public String updateDeviceData(List<StreetLightData> streetLightDatas,String idOnController) throws Exception {
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
				paramsList.add("value=" +  streetLightData.getValue());
			}
			String params = StringUtils.join(paramsList, "&");
			url = url + "?" + params;
			getRequest(url);
		}
		return null;
	}
	private String SetCommissionController(String idOnController){
		String controllerStrId = properties.getProperty("streetlight.controllerstr.id");
		ResponseEntity<String> controllerResponse = afterSetDevices(controllerStrId,idOnController);
		String response = controllerResponse.getBody();
		String statusResponse = controllerResponse.getStatusCode().toString();
		JsonParser jsonParser = new JsonParser();
		JsonObject slvBatchDeviceData = (JsonObject) jsonParser.parse(response);
		String batchIdResponse = slvBatchDeviceData.get("batchId").getAsString();
		batchIdList.add(batchIdResponse);
		return statusResponse;
	}
	public ResponseEntity<String> afterSetDevices(String controllerStrId, String idOnController){
		String mainUrl = properties.getProperty("streetlight.url.main");
		String controllerUrl = properties.getProperty("streetlight.url.setDevice.controller.api");
		String json = properties.getProperty("streetlight.url.controller.ser");
		String url = mainUrl + controllerUrl;
		List<Object> paramData = new ArrayList<Object>();
		paramData.add("controllerStrId=" + controllerStrId);
		paramData.add("idOnController=" + idOnController);
		paramData.add("ser=" + json);
		String params = StringUtils.join(paramData,"&");
		url = url + "?" + params;
		return getPostRequest(url);
	}
		private String getStreetLightMappingData() throws Exception {
			String mappingPath = properties.getProperty("streetlight.mapping.json.path");
			File file = new File(mappingFilePath + "/"+mappingPath);
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
		ResponseEntity<String> responseEntity = getRequest(streetLightDataParams, url);
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
					System.out.println("Mac Address:"+value.getContent().trim());
					devices.put(value.getContent().trim(), slvDev);
				}
			}
		}
		
		return -1;
	}
	
	public void callBatchStatus(){
		  Iterator<String> iter = batchIdList.iterator();
		  while (iter.hasNext()) {
		   String batchId = iter.next();
		   ResponseEntity<String> batchResponseEntity = getBatchResult(batchId);
		   String batchResponse = batchResponseEntity.getBody();
		   JsonObject slvBatchRespone = (JsonObject) jsonParser.parse(batchResponse);
		   String batchRunning = slvBatchRespone.get("batchRunning").getAsString();
		   String status = slvBatchRespone.get("status").getAsString();
		   if(batchRunning.equalsIgnoreCase("false")){
		    if(status.equalsIgnoreCase("ERROR")){
		     sendMail(batchResponse);
		    }
		    iter.remove();
		   }
		  }
	}
	public ResponseEntity<String> getBatchResult(String batchId){
		String mainUrl = properties.getProperty("streetlight.url.main");
		String batchUrl = properties.getProperty("streelight.url.getDevice.batch");
		String json = properties.getProperty("streetlight.url.controller.ser");
		String url = mainUrl + batchUrl;
		List<Object> ParamData = new ArrayList<Object>();
		ParamData.add("batchId=" + batchId);
		ParamData.add("ser=" + json);
		String params = StringUtils.join(ParamData,"&");
		url = url + "?" + params;
		return getRequest(url);
	}
	private HttpHeaders getHeaders() {
		String userName = properties.getProperty("streetlight.username");
		String password = properties.getProperty("streetlight.password");
		String plainCreds = userName+":"+password;
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes =  Base64.encodeBase64(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Creds);
		return headers;
	}
	private <T> ResponseEntity<String> getRequest(Map<String, String> streetLightDataParams, String url) {
		Set<String> keys = streetLightDataParams.keySet();
		List<String> values = new ArrayList<>();
		for (String key : keys) {
			String val = streetLightDataParams.get(key) != null ? streetLightDataParams.get(key).toString() : "";
			String tem = key + "=" + val;
			values.add(tem);
		}
		String params = StringUtils.join(values, "&");
		url = url + "?" + params;
		
		return getRequest(url);
	}
	private ResponseEntity<String> getRequest(String url){
		System.out.println("------------ Request ------------------");
		System.out.println(url);
		System.out.println("------------ Request End ------------------");
		HttpHeaders headers = getHeaders();
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity request = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
		System.out.println("------------ Response ------------------");
		System.out.println("Response Code:"+response.getStatusCode().toString());
		String responseBody = response.getBody();
		System.out.println(responseBody);
		System.out.println("------------ Response End ------------------");
		//return responseBody;
		return response;
	}
	private ResponseEntity<String> getPostRequest(String url){
		System.out.println("------------ Request ------------------");
		System.out.println(url);
		System.out.println("------------ Request End ------------------");
		HttpHeaders headers = getHeaders();
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity request = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
		System.out.println("------------ Response ------------------");
		System.out.println("Response Code:"+response.getStatusCode().toString());
		String responseBody = response.getBody();
		System.out.println(responseBody);
		System.out.println("------------ Response End ------------------");
		return response;
	}
	
	private String dateFormat(String dateTime){
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
            for( int i = 0; i < to.length; i++ ) {
                toAddress[i] = new InternetAddress(to[i]);
            }
            for( int i = 0; i < toAddress.length; i++) {
                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
            }
            message.setSubject(subject);
            message.setText(body);
            Transport transport = session.getTransport("smtp");
            transport.connect(host, fromEmail, emailPassword);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }
        catch (AddressException ae) {
            ae.printStackTrace();
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
    }
}
