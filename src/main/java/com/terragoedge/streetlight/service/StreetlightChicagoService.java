package com.terragoedge.streetlight.service;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.edgeserver.DeviceMacAddress;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.edgeserver.FormData;
import com.terragoedge.edgeserver.Value;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.dao.StreetlightDao;
import com.terragoedge.streetlight.exception.DeviceUpdationFailedException;
import com.terragoedge.streetlight.exception.InValidBarCodeException;
import com.terragoedge.streetlight.exception.NoValueException;
import com.terragoedge.streetlight.exception.QRCodeAlreadyUsedException;

public class StreetlightChicagoService {
	StreetlightDao streetlightDao = null;
	RestService restService = null;
	Properties properties = null;
	Gson gson = null;
	JsonParser jsonParser = null;
	EdgeMailService edgeMailService = null;
	
	
	final Logger logger = Logger.getLogger(StreetlightChicagoService.class);
	
	public StreetlightChicagoService(){
		this.streetlightDao = new StreetlightDao();
		this.restService = new RestService();
		this.properties = PropertiesReader.getProperties();
		this.gson = new Gson();
		this.edgeMailService = new EdgeMailService();
		this.jsonParser = new JsonParser();
	}
	
	public void run(){
		// Get Already synced noteguids from Database
		List<String> noteGuids = streetlightDao.getNoteIds();
		String accessToken = getEdgeToken();
		if(accessToken == null){
			logger.error("Edge Invalid UserName and Password.");
			return;
		}
		// Get Edge Server Url from properties
		String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
		
		
		url = url +PropertiesReader.getProperties().getProperty("streetlight.edge.url.notes.get");
		
		
		String systemDate = PropertiesReader.getProperties().getProperty("streetlight.edge.customdate");
		
		if(systemDate == null || systemDate.equals("false")){
			String yesterday = getYesterdayDate();
			url = url +"modifiedAfter="+yesterday;
		}
		
		
		// Get NoteList from edgeserver
		ResponseEntity<String> responseEntity = restService.getRequest(url, false, accessToken);
		
		// Process only response code as success
		if(responseEntity.getStatusCode().is2xxSuccessful()){
			
			// Get Response String
			String notesData = responseEntity.getBody();
			System.out.println(notesData);
			
			//Convert notes Json to List of notes object
			Type listType = new TypeToken<ArrayList<EdgeNote>>() {
			}.getType();
			List<EdgeNote> edgeNoteList = gson.fromJson(notesData, listType);
			
			
			// Iterate each note
			for(EdgeNote edgeNote : edgeNoteList){
				syncData(edgeNote, noteGuids);
			}
		}else{
			logger.error("Unable to get message from EdgeServer. Response Code is :"+responseEntity.getStatusCode());
		}
	}
	
	
	public String  getYesterdayDate() {
		//2017-11-01T13:00:00.000-00:00
	     SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS-00:00");
	     Calendar cal = Calendar.getInstance();
	     cal.add(Calendar.DATE, -1);
	     cal.set(Calendar.HOUR_OF_DAY,0);
	     cal.set(Calendar.MINUTE,0);
	     cal.set(Calendar.SECOND,0);
	     cal.set(Calendar.MILLISECOND,0);
	    return dateFormat.format(cal.getTime());
	 }
	
	
	
	private void syncData(EdgeNote edgeNote,List<String> noteGuids){
		try{
			// Check current note is already synced with slv or not.
			if(!noteGuids.contains(edgeNote.getNoteGuid())){
				// Get Form List
				List<FormData> formDatasList = edgeNote.getFormData();
				Map<String, FormData> formDataMaps = new HashMap<>();
				for(FormData formData : formDatasList){
					formDataMaps.put(formData.getFormTemplateGuid(), formData);
				}
				syncData(formDataMaps, edgeNote,noteGuids);
				
			}else{
				// Logging this note is already synced with SLV.
				logger.info("Note "+edgeNote.getTitle()+" is already synced with SLV.");
			}
		}catch(QRCodeAlreadyUsedException e1){
			logger.error("MacAddress ("+e1.getMacAddress()+")  - Already in use. So this pole is not synced with SLV. Note Title :["+edgeNote.getTitle()+" ]");
			edgeMailService.sendMailMacAddressAlreadyUsed(e1.getMacAddress(), e1.getMessage());
		}catch(Exception e){
			logger.error("Error in syncData",e);
		}
	}
	
	public void syncData(Map<String, FormData> formDatas,EdgeNote edgeNote,List<String> noteGuids) throws InValidBarCodeException, DeviceUpdationFailedException, QRCodeAlreadyUsedException,Exception{
		List<Object> paramsList = new ArrayList<Object>();
		String chicagoFormTemplateGuid = properties.getProperty("streetlight.edge.formtemplateguid.chicago");
		String fixtureFormTemplateGuid = properties.getProperty("streetlight.edge.formtemplateguid.fixture");
		
		FormData chicagoFromData = formDatas.get(chicagoFormTemplateGuid);
		if(chicagoFromData == null){
			logger.error("No Chicago FormTemplate is not Present. So note is not processed. Note Title is :"+edgeNote.getTitle());
			return;
		}
		
		FormData fixtureFormData =	formDatas.get(fixtureFormTemplateGuid);
		if(fixtureFormData == null){
			logger.error("No Fixture FormTemplate is not Present. So note is not processed. Note Title is :"+edgeNote.getTitle());
			return;
		}

		// Process Fixture Form data
		List<EdgeFormData> fixtureFromDef = fixtureFormData.getFormDef();

		// Get IdOnController value
		String idOnController = null;
		try {
			idOnController = value(fixtureFromDef,properties.getProperty("edge.fortemplate.fixture.label.idoncntrl"));
			paramsList.add("idOnController=" + idOnController);
		} catch (NoValueException e) {
			e.printStackTrace();
			return;
		}

		// Get ControllerStdId value
		try {
			String controllerStrId = value(fixtureFromDef,properties.getProperty("edge.fortemplate.fixture.label.cnrlstrid"));
			paramsList.add("controllerStrId=" + controllerStrId);
		} catch (NoValueException e) {
			e.printStackTrace();
			return;
		}
		
		String macAddress = null;
		// Process Chicago Form data
		List<EdgeFormData> chicagoFromDef =  chicagoFromData.getFormDef();
		for(EdgeFormData edgeFormData : chicagoFromDef){
			if(edgeFormData.getLabel().equals(properties.getProperty("edge.fortemplate.chicago.label.fixture.macaddress"))){
				if(edgeFormData.getValue() == null ||  edgeFormData.getValue().trim().isEmpty()){
					logger.info("Fixture MAC address is empty. So note is not processed. Note Title :"+edgeNote.getTitle());
					// return; -- TODO Need to skip or not later decide
				}else{
					buildFixtureStreetLightData(edgeFormData.getValue(), paramsList,edgeNote);
				}
				
			}else if(edgeFormData.getLabel().equals(properties.getProperty("edge.fortemplate.chicago.label.node.macaddress"))){
				if(edgeFormData.getValue() == null ||  edgeFormData.getValue().trim().isEmpty()){
					logger.info("Node MAC address is empty. So note is not processed. Note Title :"+edgeNote.getTitle());
					return;
				}
				macAddress = loadMACAddress(edgeFormData.getValue(), paramsList,idOnController);
			}
		}
		
		
		
		
		addStreetLightData("installStatus", "Installed", paramsList);
		
		addStreetLightData("DimmingGroupName", "Group Calendar 1", paramsList);
		
		//DimmingGroupName
		sync2Slv(paramsList,edgeNote.getNoteGuid(),idOnController,macAddress);
		noteGuids.add(edgeNote.getNoteGuid());
	}
	
	
	private void sync2Slv(List<Object> paramsList,String noteGuid,String idOnController,String macAddress) throws DeviceUpdationFailedException{
		String mainUrl = properties.getProperty("streetlight.slv.url.main");
		String updateDeviceValues = properties.getProperty("streetlight.slv.url.updatedevice");
		String url = mainUrl + updateDeviceValues;
		
		paramsList.add("ser=json");
		String params = StringUtils.join(paramsList, "&");
		url = url + "&" + params;
		ResponseEntity<String> response = restService.getPostRequest(url,null);
		String responseString = response.getBody();
		JsonObject replaceOlcResponse = (JsonObject) jsonParser.parse(responseString);
		int errorCode = replaceOlcResponse.get("errorCode").getAsInt();
		// As per doc, errorcode is 0 for success. Otherwise, its not
		// success.
		if (errorCode != 0) {
			throw new DeviceUpdationFailedException(errorCode + "");
		}else{
			streetlightDao.insertProcessedNoteGuids(noteGuid);
		}
	}
	
	
	private String value(List<EdgeFormData> edgeFormDatas,String key) throws NoValueException{
		EdgeFormData edgeFormTemp = new EdgeFormData();
		edgeFormTemp.setLabel(key);
		
		int pos = edgeFormDatas.indexOf(edgeFormTemp);
		if(pos != -1){
			EdgeFormData edgeFormData = edgeFormDatas.get(pos);
			String value = edgeFormData.getValue();
			if(value == null ||  value.trim().isEmpty()){
				throw new NoValueException("Value is Empty or null."+value);
			}
			return value;
		}else{
			throw new NoValueException(key+" is not found.");
		}
	}
	
	private void addStreetLightData(String key,String value,List<Object> paramsList){
		paramsList.add("valueName=" + key.trim());
		paramsList.add("value=" + value.trim());
	}
	
	
	
	
	private String loadMACAddress(String data,List<Object> paramsList,String idOnController ) throws InValidBarCodeException, QRCodeAlreadyUsedException,Exception{
		if(data.contains("MACid")){
			String[] nodeInfo = data.split(",");
			if(nodeInfo.length > 0){
				for(String nodeData : nodeInfo){
					if(nodeData.startsWith("MACid")){
						String macAddress = nodeData.substring(6);
						checkMacAddressExists(macAddress, idOnController);
						addStreetLightData("MacAddress", macAddress, paramsList);
						return macAddress;
					}
				}
			}
		}else{
			checkMacAddressExists(data, idOnController);
			addStreetLightData("MacAddress", data, paramsList);
			return data;
		}
		
		throw new InValidBarCodeException("Node MAC address is not valid. Value is:"+data);
	}
	
	//Philips RoadFocus, RFS-54W16LED3K-T-R2M-UNIV-DMG-PH9-RCD-SP2-GY3,
	//RFM0455, 07/24/17, 54W, 120/277V, 4000K, 8140 Lm, R2M, Gray, Advance, 442100083510, DMG
	
	public void buildFixtureStreetLightData(String data,List<Object> paramsList,EdgeNote edgeNote ) throws InValidBarCodeException{
		String[] fixtureInfo = data.split(",");
		if(fixtureInfo.length >= 13){
			addStreetLightData("luminaire.brand", fixtureInfo[0], paramsList);
			/**
			 * As per Mail conversion, In the older data, the luminaire model
			 * was the shorter version of the fixture, so for the General
			 * Electric fixtures it was ERLH. The Luminaire Part Number would be
			 * the longer more detailed number.
			 */
			String partNumber = fixtureInfo[1].trim();
			String model = fixtureInfo[2].trim();
			if(fixtureInfo[1].trim().length() <= fixtureInfo[2].trim().length()){
				model = fixtureInfo[1].trim();
				partNumber = fixtureInfo[2].trim();
			}
			addStreetLightData("device.luminaire.partnumber", partNumber, paramsList);
			addStreetLightData("luminaire.model", model, paramsList);
			addStreetLightData("device.luminaire.manufacturedate", fixtureInfo[3], paramsList);
			addStreetLightData("power", fixtureInfo[4], paramsList);
			addStreetLightData("fixing.type", fixtureInfo[5], paramsList);
			addStreetLightData("device.luminaire.colortemp", fixtureInfo[6], paramsList);
			addStreetLightData("device.luminaire.lumenoutput", fixtureInfo[7], paramsList);
			addStreetLightData("luminaire.DistributionType", fixtureInfo[8], paramsList);
			addStreetLightData("luminaire.colorcode", fixtureInfo[9], paramsList);
			addStreetLightData("device.luminaire.drivermanufacturer", fixtureInfo[10], paramsList);
			addStreetLightData("device.luminaire.driverpartnumber", fixtureInfo[11], paramsList);
			addStreetLightData("ballast.dimmingtype", fixtureInfo[12], paramsList);
			
			
			addStreetLightData("luminaire.installdate", dateFormat(edgeNote.getCreatedDateTime()), paramsList); // -- TODO
			//luminaire.installdate - 2017-09-07 09:47:35
			
			addStreetLightData("install.date", dateFormat(edgeNote.getCreatedDateTime()) , paramsList);
			//controller.installdate  - 2017/10/10
			
			
		}else{
			throw new InValidBarCodeException("Fixture MAC address is not valid ("+edgeNote.getTitle()+"). Value is:"+data);
		}
	}
	
	
	private String dateFormat(Long dateTime) {
		Date date = new Date(Long.valueOf(dateTime));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dff = dateFormat.format(date);
		return dff;
	}
	
	
	private String dateFormat_1(Long dateTime) {
		Date date = new Date(Long.valueOf(dateTime));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		String dff = dateFormat.format(date);
		return dff;
	}
	
	
	
	//http://192.168.1.9:8080/edgeServer/oauth/token?grant_type=password&username=admin&password=admin&client_id=edgerestapp
	
	private String getEdgeToken(){
		String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
		String userName = properties.getProperty("streetlight.edge.username");
		String  password = properties.getProperty("streetlight.edge.password");
		url = url + "/oauth/token?grant_type=password&username=" +userName+"&password="+password+"&client_id=edgerestapp";
		ResponseEntity<String> responseEntity 
		 = restService.getRequest(url);
		if(responseEntity.getStatusCode().is2xxSuccessful()){
			JsonObject jsonObject = (JsonObject) jsonParser.parse(responseEntity.getBody());
			return jsonObject.get("access_token").getAsString();
		}
		return null;
	
	}
	
	
	
	/**
	 * Load Mac address and corresponding IdOnController from SLV Server
	 * @throws Exception 
	 */
	public boolean checkMacAddressExists(String macAddress,String idOnController)throws Exception{
		logger.info("Getting Mac Address from SLV.");
		String mainUrl = properties.getProperty("streetlight.slv.url.main");
		String updateDeviceValues = properties.getProperty("streetlight.slv.url.search.device");
		String url = mainUrl + updateDeviceValues;
		List<String> paramsList = new ArrayList<>();
		paramsList.add("attribute=MacAddress");
		paramsList.add("value="+macAddress);
		paramsList.add("operator=eq-i");
		paramsList.add("recurse=true");
		paramsList.add("ser=json");
		String params = StringUtils.join(paramsList, "&");
		url = url + "?" + params;
		ResponseEntity<String> response = restService.getRequest(url,true,null);
		if(response.getStatusCodeValue() == 200){
			String responseString = response.getBody();
			logger.info("-------MAC Address----------");
			logger.info(responseString);
			logger.info("-------MAC Address End----------");
			DeviceMacAddress deviceMacAddress = gson.fromJson(responseString, DeviceMacAddress.class);
			List<Value> values = deviceMacAddress.getValue();
			StringBuilder stringBuilder = new StringBuilder();
			if(values == null || values.size() == 0){
				return false;
			}else{
				for(Value value : values){
					if(value.getIdOnController().equals(idOnController)){
						return  false;
					}
					stringBuilder.append(value.getIdOnController());
					stringBuilder.append("\n");
				}
			}
			throw new QRCodeAlreadyUsedException(stringBuilder.toString(), macAddress);
		}else{
			throw new Exception();
		}
		
	}
	
	
	
	
	
	
	
}
