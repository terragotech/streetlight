package com.terragoedge.streetlight.service;

import java.lang.reflect.Type;
import java.util.ArrayList;
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
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.edgeserver.FormData;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.dao.StreetlightDao;
import com.terragoedge.streetlight.exception.DeviceUpdationFailedException;
import com.terragoedge.streetlight.exception.InValidBarCodeException;
import com.terragoedge.streetlight.exception.NoValueException;

public class StreetlightChicagoService {
	StreetlightDao streetlightDao = null;
	RestService restService = null;
	Properties properties = null;
	Gson gson = null;
	JsonParser jsonParser = null;
	
	final Logger logger = Logger.getLogger(StreetlightChicagoService.class);
	
	public StreetlightChicagoService(){
		this.streetlightDao = new StreetlightDao();
		this.restService = new RestService();
		this.properties = PropertiesReader.getProperties();
		this.gson = new Gson();
		this.jsonParser = new JsonParser();
	}
	
	public void run(){
		
		// Get Already synced noteguids from Database
		List<String> noteGuids = streetlightDao.getNoteIds();
		
		// Get Edge Server Url from properties
		String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
		url = url + "/"+PropertiesReader.getProperties().getProperty("streetlight.edge.url.notes.get");
		
		// Get NoteList from edgeserver
		ResponseEntity<String> responseEntity = restService.getRequest(url, false, true);
		
		// Process only response code as success
		if(responseEntity.getStatusCode().is2xxSuccessful()){
			
			// Get Response String
			String notesData = responseEntity.getBody();
			
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
		}catch(Exception e){
			logger.error("Error in syncData",e);
		}
	}
	
	public void syncData(Map<String, FormData> formDatas,EdgeNote edgeNote,List<String> noteGuids) throws InValidBarCodeException, DeviceUpdationFailedException{
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
		
		
		// Process Chicago Form data
		List<EdgeFormData> chicagoFromDef =  chicagoFromData.getFormDef();
		for(EdgeFormData edgeFormData : chicagoFromDef){
			if(edgeFormData.getLabel().equals(properties.getProperty("edge.fortemplate.chicago.label.fixture.macaddress"))){
				if(edgeFormData.getValue() == null ||  edgeFormData.getValue().trim().isEmpty()){
					logger.info("Fixture MAC address is empty. So note is not processed. Note Title :"+edgeNote.getTitle());
					return;
				}
				buildFixtureStreetLightData(edgeFormData.getValue(), paramsList);
			}else if(edgeFormData.getLabel().equals(properties.getProperty("edge.fortemplate.chicago.label.node.macaddress"))){
				if(edgeFormData.getValue() == null ||  edgeFormData.getValue().trim().isEmpty()){
					logger.info("Node MAC address is empty. So note is not processed. Note Title :"+edgeNote.getTitle());
					return;
				}
				loadMACAddress(edgeFormData.getValue(), paramsList);
			}
		}
		
		
		// Process Fixture Form data
		
		List<EdgeFormData> fixtureFromDef =  fixtureFormData.getFormDef();
		
		// Get IdOnController value
		try {
			String idOnController = value(fixtureFromDef, properties.getProperty("edge.fortemplate.fixture.label.idoncntrl"));
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
		
		
		sync2Slv(paramsList,edgeNote.getNoteGuid());
		noteGuids.add(edgeNote.getNoteGuid());
	}
	
	
	private void sync2Slv(List<Object> paramsList,String noteGuid) throws DeviceUpdationFailedException{
		String mainUrl = properties.getProperty("streetlight.slv.url.main");
		String updateDeviceValues = properties.getProperty("streetlight.slv.url.updatedevice");
		String url = mainUrl + updateDeviceValues;
		
		paramsList.add("ser=json");
		String params = StringUtils.join(paramsList, "&");
		url = url + "?" + params;
		ResponseEntity<String> response = restService.getRequest(url, true,false);
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
		paramsList.add("valueName=" + key);
		paramsList.add("value=" + value);
	}
	
	
	
	
	private void loadMACAddress(String data,List<Object> paramsList ) throws InValidBarCodeException{
		String[] nodeInfo = data.split(",");
		if(nodeInfo.length > 0){
			for(String nodeData : nodeInfo){
				if(nodeData.startsWith("MACid")){
					String macAddress = nodeData.substring(6);
					addStreetLightData("userproperty.MacAddress", macAddress, paramsList);
					return;
				}
			}
		}
		throw new InValidBarCodeException("Node MAC address is not valid. Value is:"+data);
	}
	
	public void buildFixtureStreetLightData(String data,List<Object> paramsList ) throws InValidBarCodeException{
		String[] fixtureInfo = data.split(",");
		if(fixtureInfo.length == 13){
			addStreetLightData("userproperty.luminaire.model", fixtureInfo[2], paramsList);
			addStreetLightData("userproperty.device.luminaire.manufacturedate", fixtureInfo[3], paramsList);
			addStreetLightData("power", fixtureInfo[2], paramsList);
			addStreetLightData("userproperty.ballast.dimmingtype", fixtureInfo[2], paramsList);
			addStreetLightData("userproperty.device.luminaire.colortemp", fixtureInfo[2], paramsList);
			addStreetLightData("userproperty.device.luminaire.lumenoutput", fixtureInfo[2], paramsList);
			addStreetLightData("userproperty.luminaire.DistributionType", fixtureInfo[2], paramsList);
			addStreetLightData("userproperty.luminaire.colorcode", fixtureInfo[2], paramsList);
			addStreetLightData("userproperty.device.luminaire.drivermanufacturer", fixtureInfo[2], paramsList);
			addStreetLightData("userproperty.device.luminaire.driverpartnumber", fixtureInfo[2], paramsList);
			addStreetLightData("userproperty.ballast.dimmingtype", fixtureInfo[2], paramsList);
			
			
			addStreetLightData("userproperty.luminaire.installdate", fixtureInfo[2], paramsList); // -- TODO
			//userproperty.luminaire.installdate - 2017-09-07 09:47:35
			
			//Controller Install Date  -- TODO
			addStreetLightData("userproperty.MacAddress", fixtureInfo[2], paramsList);
			
		}else{
			throw new InValidBarCodeException("Fixture MAC address is not valid. Value is:"+data);
		}
	}

}
