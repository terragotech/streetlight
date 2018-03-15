package com.terragoedge.install;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.install.exception.DifferentMACAddressException;
import com.terragoedge.install.exception.InValidFormException;
import com.terragoedge.install.exception.MultipeFormsException;
import com.terragoedge.install.exception.NoMacAddressException;
import com.terragoedge.install.exception.SLNumberException;
import com.terragoedge.install.slvdata.SLVDeviceDetails;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.exception.DeviceCreationFailedException;
import com.terragoedge.streetlight.exception.DeviceUpdationFailedException;
import com.terragoedge.streetlight.exception.QRCodeAlreadyUsedException;
import com.terragoedge.streetlight.exception.ReplaceOLCFailedException;
import com.terragoedge.streetlight.service.RestService;

public class StreetLightInstallService {

	private StreetLightInstallDAO streetLightInstallDAO;
	private Properties properties = null;
	private Gson gson = null;
	private JsonParser jsonParser = null;
	RestService restService = null;
	JsonObject mappingJson = null;
	private String formTemplateGuid = null;
	
	private Map<String, String> deviceList = new ConcurrentHashMap<>();
	private Map<String, String> macAddressList = new ConcurrentHashMap<>();
	Map<String, String> dimmingValue = new HashMap<>();
	
	static Map<String, Integer> luminareCore = new HashMap<>();
	
	static {
		luminareCore.put("L1", 39);
		luminareCore.put("L2", 58);
		luminareCore.put("L3", 27);
		luminareCore.put("L4", 80);
		luminareCore.put("L5", 108);
		luminareCore.put("L6", 108);
		luminareCore.put("L7", 133);
		luminareCore.put("L8", 158);
		luminareCore.put("L9", 80);
		luminareCore.put("L10", 133);
		luminareCore.put("L11", 108);
		luminareCore.put("L12", 158);
		luminareCore.put("L13", 80);
		luminareCore.put("L14", 52);

	}


	public StreetLightInstallService() {
		streetLightInstallDAO = new StreetLightInstallDAO();
		gson = new Gson();
		jsonParser = new JsonParser();
		restService = new RestService();
		properties = PropertiesReader.getProperties();
		loadDimmingValue();
	}
	

	public void process() throws Exception {
		List<NotebookGeoZones> notebookGeoZonesList = loadNotebookGeoZones();
		loadSLVData();
		formTemplateGuid = properties.getProperty("edge.streetlight.install.fromtemplateguid");
		
		for(NotebookGeoZones notebookGeoZones : notebookGeoZonesList){
			List<NoteDetails> noteDetailsList = streetLightInstallDAO.getUnSyncedNoteIds(notebookGeoZones.getNotebookName());
			mappingJson = getStreetLightMappingJson();
			for (NoteDetails noteDetails : noteDetailsList) {
				LoggingDetails loggingDetails = new LoggingDetails();
				try {
					SLVDataEntity slvDataEntity = new SLVDataEntity();
					loadLoggingDetails(noteDetails, loggingDetails);
					process(noteDetails, loggingDetails,slvDataEntity,notebookGeoZones);
				} catch (Exception e) {
					e.printStackTrace();
					loggingDetails.setStatus(Constants.FAILURE);
					loggingDetails.setDescription(e.getMessage());
				} 
				streetLightInstallDAO.insertProcessedNotes(loggingDetails);
			}
		}
		
		
	}
	
	
	private List<NotebookGeoZones> loadNotebookGeoZones(){
		String notebookGeoZoneJson = properties.getProperty("streetlight.url.load.slvmacaddress");
		List<NotebookGeoZones> notebookGeoZones = gson.fromJson(notebookGeoZoneJson,
				new TypeToken<List<NotebookGeoZones>>() {
				}.getType());
		return notebookGeoZones;
	}
	
	private void loadSLVData(){
		String mainUrl = properties.getProperty("streetlight.url.main");
		String createDeviceUrl = properties.getProperty("streetlight.url.load.slvmacaddress");
		mainUrl = mainUrl + createDeviceUrl;
		ResponseEntity<String> responseEntity = restService.getRequest(mainUrl, false);
		String data = responseEntity.getBody();
		SLVDeviceDetails slvDeviceDetails = gson.fromJson(data, SLVDeviceDetails.class);
		List<List<String>> values = slvDeviceDetails.getValues();
		for(List<String> slvValues : values){
			String idOnController = slvValues.get(0);
			String macAddress = slvValues.get(1);
			if(idOnController != null){
				idOnController = idOnController.replaceAll("\"", "");
			}
			
			if(macAddress != null){
				macAddress = macAddress.replaceAll("\"", "").trim().toLowerCase();
				macAddressList.put(macAddress, idOnController);
			}else{
				macAddress = "";
			}
			deviceList.put(idOnController, macAddress);
		}
	}
	

	private void loadLoggingDetails(NoteDetails noteDetails, LoggingDetails loggingDetails) {
		loggingDetails.setNoteId(noteDetails.getNoteid());
		loggingDetails.setNoteGuid(noteDetails.getNoteGuid());
		loggingDetails.setTitle(noteDetails.getTitle());
	}
	

	private void process(NoteDetails noteDetails, LoggingDetails loggingDetails,SLVDataEntity slvDataEntity,NotebookGeoZones notebookGeoZones) {
		try {
			streetLightInstallDAO.getFormDetails(noteDetails);
			int totalSize = noteDetails.getFormDetails().size();
			loggingDetails.setTotalForms(String.valueOf(totalSize));
			if (totalSize > 0) {
				if(totalSize == 1){
					validateFormTemplate(noteDetails);
					processEdgeForm(noteDetails, slvDataEntity, noteDetails.getFormDetails().get(0),loggingDetails,notebookGeoZones);
					loggingDetails.setStatus(Constants.SUCCESS);
				}else{
					FormDetails formDetails = processEdgeForms(noteDetails, slvDataEntity);
					processEdgeForm(noteDetails, slvDataEntity,formDetails,loggingDetails,notebookGeoZones);
					loggingDetails.setStatus(Constants.SUCCESS);
				}
				
			} else {
				loggingDetails.setStatus(Constants.FAILURE);
				loggingDetails.setDescription("No Form is present in this note.");
			}
		} catch (InValidFormException | SLNumberException | NoMacAddressException | DifferentMACAddressException | QRCodeAlreadyUsedException | ReplaceOLCFailedException | MultipeFormsException | DeviceUpdationFailedException | DeviceCreationFailedException e1) {
			loggingDetails.setStatus(Constants.FAILURE);
			loggingDetails.setDescription(e1.getMessage());
		} 
	}
	
	
	private FormDetails processEdgeForms(NoteDetails noteDetails, SLVDataEntity slvDataEntity) throws SLNumberException,MultipeFormsException, NoMacAddressException, DifferentMACAddressException, QRCodeAlreadyUsedException {
		getSLNumber(noteDetails, slvDataEntity);
		List<FormDetails> formDetailsList = noteDetails.getFormDetails();
		Set<String> actionType = new HashSet<>();
		String actionTypeVal = null;
		for (FormDetails formDetails : formDetailsList) {
			if (formDetails.getFormTemplateGuid().equals(formTemplateGuid)) { // -- TODO
				List<FormValues> edgeFormValuesList = gson.fromJson(formDetails.getFormDef(),
						new TypeToken<List<FormValues>>() {
						}.getType());
				FormValues actionFormValues = getFormValues(edgeFormValuesList, "Action");
				actionType.add(actionFormValues.getValue());
				actionTypeVal = actionFormValues.getValue();
			}
		}
		if(actionType.size() == 0){
			throw new MultipeFormsException("More than one form with no Action Type");
		}else if(actionType.size() > 1){
			throw new MultipeFormsException("More than one form with Different Action Type");
		}else{
			if(actionTypeVal.equals("New Streetlight")){
				int pos = validateNewStreetMacAddress(noteDetails, slvDataEntity);
				return formDetailsList.get(pos);
			}else if(actionTypeVal.equals("Update Streetlight")){
				int pos =  validateUpdateStreetMacAddress(noteDetails, slvDataEntity);
				return formDetailsList.get(pos);
			}
			return formDetailsList.get(0);
		}
	}
	
	
	private void processEdgeForm(NoteDetails noteDetails, SLVDataEntity slvDataEntity,FormDetails formDetails,LoggingDetails loggingDetails,NotebookGeoZones notebookGeoZones ) throws SLNumberException, NoMacAddressException, DifferentMACAddressException, QRCodeAlreadyUsedException, ReplaceOLCFailedException, DeviceUpdationFailedException, DeviceCreationFailedException{
		List<FormValues> edgeFormValuesList = gson.fromJson(formDetails.getFormDef(),
				new TypeToken<List<FormValues>>() {
				}.getType());
		FormValues qrCodeFormValues = getFormValues(edgeFormValuesList, "Action");
		loggingDetails.setActionType(qrCodeFormValues.getValue());
		switch (qrCodeFormValues.getValue()) {
		case "New Streetlight":
			getSLNumber(noteDetails, slvDataEntity);
			validateNewStreetMacAddress(noteDetails, slvDataEntity);
			createDeviceInSLV(noteDetails, slvDataEntity,notebookGeoZones);
			updateDeviceValues( noteDetails, slvDataEntity, edgeFormValuesList);
			replaceOLC(slvDataEntity, slvDataEntity.getMacAddress());
			deviceList.put(slvDataEntity.getIdOnController().trim(), slvDataEntity.getMacAddress().trim().toLowerCase());
			break;
			
		case "Update Streetlight":
			getSLNumber(noteDetails, slvDataEntity);
			updateStreetLight(edgeFormValuesList, slvDataEntity,noteDetails);
			break;
			
		case "Remove Streetlight":
			getSLNumber(noteDetails, slvDataEntity);
			addStreetLightData("MacAddress", "", slvDataEntity.getParamsList());
			replaceOLC(slvDataEntity, "");
			deviceList.remove(slvDataEntity.getIdOnController().trim());
			break;

		
		}
	}
	
	
	private void updateStreetLight(List<FormValues> edgeFormValuesList,SLVDataEntity slvDataEntity,NoteDetails noteDetails)throws QRCodeAlreadyUsedException, NoMacAddressException, ReplaceOLCFailedException, DeviceUpdationFailedException{
		// Get Existing QR Code
		FormValues existingQRCode = getFormValues(edgeFormValuesList, "Existing SELC QR Code");
		String existingQRCodeVal = existingQRCode.getValue();
		//Check Existing QR Code has value or not
		if(existingQRCodeVal == null || existingQRCodeVal.trim().isEmpty()){
			throw new NoMacAddressException("Existing SELC QR Code is empty.");
		}
		
		//Check Existing QR Code is correct or wrong
		checkMacAddressExists(existingQRCodeVal, slvDataEntity.getIdOnController());
		
		// Get New MAC Address
		FormValues newQRCode = getFormValues(edgeFormValuesList, "New SELC QR Code");
		
		//Check New QR Code has value or not
		if(newQRCode.getValue() == null || newQRCode.getValue().trim().isEmpty()){
			throw new NoMacAddressException("New SELC QR Code is empty.");
		}
		// Check New QR Code map with any other SL
		boolean macAddressExists = macAddressList.containsKey(newQRCode.getValue());
		if(macAddressExists){
			String idOnCntrl = macAddressList.get(newQRCode.getValue());
			throw new QRCodeAlreadyUsedException("QR code ["+newQRCode.getValue()+"] is already used in "+idOnCntrl);
		}
		// Empty QR Code by calling set device value
		addStreetLightData("MacAddress", "", slvDataEntity.getParamsList());
		updateSLVData(slvDataEntity, noteDetails, slvDataEntity.getParamsList());
		// Empty Replace OLC
		replaceOLC(slvDataEntity, "");
		// Replace OLC with New MAC Address
		replaceOLC(slvDataEntity, newQRCode.getValue());
		
		deviceList.put(slvDataEntity.getIdOnController().trim(), newQRCode.getValue().trim().toLowerCase());
	}
	
	
	private void createDeviceInSLV(NoteDetails noteDetails, SLVDataEntity slvDataEntity,NotebookGeoZones notebookGeoZones) throws DeviceCreationFailedException{
		String idOnController = slvDataEntity.getIdOnController();
		if(!deviceList.containsKey(idOnController)){
			String mainUrl = properties.getProperty("streetlight.url.main");
			String createDeviceUrl = properties.getProperty("streetlight.url.create.device");
			mainUrl = mainUrl + createDeviceUrl;
			String methodCreateDevice = properties.getProperty("streetlight.method.create.device");
			String categoryStrId = properties.getProperty("streetlight.create.device.categoryStrId");
			String controllerStrId = properties.getProperty("streetlight.controller.strid");
			String nodeTypeStrId = properties.getProperty("streetlight.equipment.type");
			Map<String, String> streetLightDataParams = new HashMap<>();
			streetLightDataParams.put("methodName", methodCreateDevice);
			streetLightDataParams.put("categoryStrId", categoryStrId);
			streetLightDataParams.put("controllerStrId", controllerStrId);
			streetLightDataParams.put("idOnController", slvDataEntity.getIdOnController());
			streetLightDataParams.put("geoZoneId", notebookGeoZones.getGeoZoneId()); 
			streetLightDataParams.put("nodeTypeStrId", nodeTypeStrId);
			streetLightDataParams.put("lat", slvDataEntity.getLat()); 
			streetLightDataParams.put("lng", slvDataEntity.getLng());
			ResponseEntity<String> responseEntity = restService.getRequest(streetLightDataParams, mainUrl, true);
			String status = responseEntity.getStatusCode().toString();
			String responseBody = responseEntity.getBody();
			if ((status.equalsIgnoreCase("200") || status.equalsIgnoreCase("ok"))
					&& !responseBody.contains("<status>ERROR</status>")) {
				deviceList.put(idOnController, "");
			} else {
				throw new DeviceCreationFailedException("Device Creation Failed."+responseBody);
			}
		}
	}
	
	private String getStreetLightMappingData() throws Exception {
		String mappingPath = properties.getProperty("streetlight.mapping.json.path");
		//File file = new File("./resources/" + mappingPath);
		File file = new File(mappingPath);
		String streetLightMappingData = FileUtils.readFileToString(file);
		return streetLightMappingData;
	}

	private JsonObject getStreetLightMappingJson() throws Exception {
		String streetLightMappingData = getStreetLightMappingData();
		return (JsonObject) jsonParser.parse(streetLightMappingData);
	}
	
	
	private void updateDeviceValues(NoteDetails noteDetails, SLVDataEntity slvDataEntity,
			List<FormValues> edgeFormValuesList) throws DeviceUpdationFailedException {
		int lWatt = 0;
		int power2Watt = 0;
		String comment = "";
		for (FormValues formValues : edgeFormValuesList) {
			JsonElement streetLightKey = mappingJson.get(formValues.getLabel());
			if (streetLightKey != null && !streetLightKey.isJsonNull()) {
				String key = streetLightKey.getAsString();
				String value = formValues.getValue();
				if(!key.equals("comment") && !key.equals("MacAddress") && !key.toLowerCase().equals("power")){
					addStreetLightData(key, value, slvDataEntity.getParamsList());
				}
				
				switch (key) {
				
				case "luminaire.model":
					if(formValues.getValue() != null){
						String[] luminareCoreValues = formValues.getValue().split("-");
						if (luminareCoreValues.length > 0) {
							String luminareCoreValue = luminareCoreValues[0].trim();
							if (luminareCore.containsKey(luminareCoreValue)) {
								lWatt = luminareCore.get(luminareCoreValue);
							}
						}
					}
					
					break;

				case "power2":
					value = formValues.getValue();
					if (value != null && !(value.trim().isEmpty()) && !(value.trim().equalsIgnoreCase("(null)"))) {
						String temp = value.replaceAll("[^\\d.]", "");
						temp = temp.trim();
						power2Watt = Integer.parseInt(temp);
					}
					
				case "location.mapnumber":
					value = formValues.getValue();// TODO
					String tt = "Block " + value;
					//addStreetLightData(key, value, slvDataEntity.getParamsList());
					break;
					
				case "comment":
					comment = comment + " " + formValues.getLabel() + ":" + formValues.getValue();
					break;
					
				case "power":
					value = formValues.getValue();
					if (value != null && !(value.trim().isEmpty()) && !(value.trim().equalsIgnoreCase("(null)"))) {
						String temp = value.replaceAll("[^\\d.]", "");
						temp = temp.trim();
						lWatt = Integer.parseInt(temp);
					}
					break;

				default:
					break;
				}
			}
			
		}
		
		if (lWatt == 0) {
			addStreetLightData("power", "39 W", slvDataEntity.getParamsList());
			lWatt = 39;
		} else {
			addStreetLightData("power", lWatt + " W", slvDataEntity.getParamsList());
		}

		int watt = power2Watt - lWatt;
		addStreetLightData("powerCorrection", watt + "", slvDataEntity.getParamsList());
		addStreetLightData("location.utillocationid", slvDataEntity.getIdOnController() + ".Lamp", slvDataEntity.getParamsList());
		String nodeTypeStrId = properties.getProperty("streetlight.equipment.type");
		addStreetLightData("modelFunctionId", nodeTypeStrId, slvDataEntity.getParamsList());

		addStreetLightData("comment", comment, slvDataEntity.getParamsList());

		String streetLightDate = dateFormat(noteDetails.getCreatedDateTime());
		addStreetLightData("lamp.installdate", streetLightDate, slvDataEntity.getParamsList());
		
		String dimmingGroupName = dimmingValue.get(slvDataEntity.getIdOnController());
		addStreetLightData("DimmingGroupName", dimmingGroupName, slvDataEntity.getParamsList());
		
		updateSLVData(slvDataEntity, noteDetails, slvDataEntity.getParamsList());
	}
	
	
	public void updateSLVData(SLVDataEntity slvDataEntity,NoteDetails noteDetail,List<Object> paramsList) throws DeviceUpdationFailedException {
		if (paramsList.size() > 0) {
			String mainUrl = properties.getProperty("streetlight.url.main");
			String dataUrl = properties.getProperty("streetlight.url.update.device");
			String insertDevice = properties.getProperty("streetlight.method.update.device");
			String controllerStrId = properties.getProperty("streetlight.controller.strid");
			String url = mainUrl + dataUrl;
			paramsList.add("methodName=" + insertDevice);
			paramsList.add("controllerStrId=" + controllerStrId);
			paramsList.add("idOnController=" + slvDataEntity.getIdOnController());
			paramsList.add("ser=json");
			String params = StringUtils.join(paramsList, "&");
			url = url + "?" + params;
			System.out.println(url);
			ResponseEntity<String> response = restService.getRequest(url, true);
			String responseString = response.getBody();
			JsonObject replaceOlcResponse = (JsonObject) jsonParser.parse(responseString);
			int errorCode = replaceOlcResponse.get("errorCode").getAsInt();
			// As per doc, errorcode is 0 for success. Otherwise, its not
			// success.
			if (errorCode != 0) {
				throw new DeviceUpdationFailedException("Device Updation Failed."+errorCode + "");
			}
			
		}

	}
	
	
	private String dateFormat(Long dateTime) {
		Date date = new Date(Long.valueOf(dateTime));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dff = dateFormat.format(date);
		return dff;
	}
	
	
	private void getSLNumber(NoteDetails noteDetails, SLVDataEntity slvDataEntity) throws SLNumberException{
		List<FormDetails> formDetailsList = noteDetails.getFormDetails();
		Set<String> slNumbers = new HashSet<>();
		for (FormDetails formDetails : formDetailsList) {
			List<FormValues> edgeFormValuesList = gson.fromJson(formDetails.getFormDef(),
					new TypeToken<List<FormValues>>() {
					}.getType());
			FormValues qrCodeFormValues = getFormValues(edgeFormValuesList, "SL");
			String values = qrCodeFormValues.getValue();
			if (values != null) {
				slNumbers.add(values);
				slvDataEntity.setIdOnController(values);
				
				FormValues controllerSrtValues = getFormValues(edgeFormValuesList, "Controller Str ID");
				String controllerSrtValue = controllerSrtValues.getValue();
				slvDataEntity.setControllerStrId(controllerSrtValue);
			}
		}
		int size = slNumbers.size();
		if(size == 0){
			throw new SLNumberException("ID on Controller is not present.");
			// throw no mac address
		}else if(size == 1){
			// no process
		}else{
			throw new SLNumberException("More than one ID on Controller is present.");
		}
	}
	
	
	private int validateNewStreetMacAddress(NoteDetails noteDetails, SLVDataEntity slvDataEntity) throws NoMacAddressException,DifferentMACAddressException,QRCodeAlreadyUsedException {
		List<FormDetails> formDetailsList = noteDetails.getFormDetails();
		Set<String> qrCodes = new HashSet<>();
		int pos = -1;
		int count = 0;
		for (FormDetails formDetails : formDetailsList) {
			List<FormValues> edgeFormValuesList = gson.fromJson(formDetails.getFormDef(),
					new TypeToken<List<FormValues>>() {
					}.getType());
			
			FormValues physicalInstall = getFormValues(edgeFormValuesList, "Physical Install");
			String physicalInstallVal = physicalInstall.getValue();
			if(physicalInstallVal == null || physicalInstallVal.toLowerCase().equals("no")){
				throw new NoMacAddressException("Physical Install Status is no.");
			}
			
			FormValues qrCodeFormValues = getFormValues(edgeFormValuesList, "SELC QR Code");
			String values = qrCodeFormValues.getValue();
			if (values != null) {
				qrCodes.add(values);
				pos = count;
				slvDataEntity.setMacAddress(values);
			}
			count +=1;
			FormValues luminareScanValues = getFormValues(edgeFormValuesList, "Luminaire Scan");
			String luminareScanValue = luminareScanValues.getValue();
			slvDataEntity.setLuminareCode(luminareScanValue);
		}
		int size = qrCodes.size();
		if(size == 0){
			 throw new NoMacAddressException("No MAC Address is Present.");
		}else if(size == 1){
			boolean macAddressExists = macAddressList.containsKey(slvDataEntity.getMacAddress());
			if(macAddressExists){
				String idOnCntrl = macAddressList.get(slvDataEntity.getMacAddress());
				throw new QRCodeAlreadyUsedException("QR code ["+slvDataEntity.getMacAddress()+"] is already used in "+idOnCntrl);
			}
			// no process
			return pos;
		}else{
			throw new DifferentMACAddressException("More than one MAC address is present");
		}
	}
	
	
	private int validateUpdateStreetMacAddress(NoteDetails noteDetails, SLVDataEntity slvDataEntity) throws NoMacAddressException,DifferentMACAddressException,QRCodeAlreadyUsedException {
		List<FormDetails> formDetailsList = noteDetails.getFormDetails();
		Set<String> qrCodes = new HashSet<>();
		int pos = -1;
		int count = 0;
		for (FormDetails formDetails : formDetailsList) {
			List<FormValues> edgeFormValuesList = gson.fromJson(formDetails.getFormDef(),
					new TypeToken<List<FormValues>>() {
					}.getType());
			
			
			FormValues qrCodeFormValues = getFormValues(edgeFormValuesList, "Existing SELC QR Code");
			String values = qrCodeFormValues.getValue();
			if (values != null) {
				FormValues newSelectQrCodeFormValues = getFormValues(edgeFormValuesList, "New SELC QR Code");
				values =  values + newSelectQrCodeFormValues.getValue();
				qrCodes.add(values);
				pos = count;
			}
			count =+ 1;
		}
		int size = qrCodes.size();
		if(size == 0){
			 throw new NoMacAddressException("MAC address is empty in Update Streetlight.");
		}else if(size == 1){
			return pos;
		}else{
			throw new DifferentMACAddressException("MAC address is differ in Update Streetlight.");
		}
	}
	
	
	private FormValues getFormValues(List<FormValues> edgeFormValuesList,String lab){
		FormValues formValues = new FormValues();
		formValues.setLabel(lab);
		int pos = edgeFormValuesList.indexOf(formValues);
		if(pos != -1){
			return edgeFormValuesList.get(pos);
		}
		return null;
	}
	
	/***
	 * Check this note contains Streetlight install form or not
	 * @param noteDetails
	 * @throws InValidFormException 
	 */
	private void validateFormTemplate(NoteDetails noteDetails) throws InValidFormException{
		List<FormDetails> formDetailsList = noteDetails.getFormDetails();
		boolean isFormPresent = false;
		for(FormDetails formDetails : formDetailsList){
			if(formDetails.getFormTemplateGuid().equals(formTemplateGuid)){ // -- TODO
				isFormPresent = true;
			}
		}
		
		if(!isFormPresent){
			throw new InValidFormException("Streetlight Installation Form is not present in this note.");
		}
	}
	
	
	
	private void addStreetLightData(String key,String value,List<Object> paramsList){
		paramsList.add("valueName=" + key.trim());
		if(value != null){
			paramsList.add("value=" + value.trim());
		}else{
			paramsList.add("value=");
		}
		
	}
	
	
	public void replaceOLC(SLVDataEntity slvSyncDataEntity,String macAddress) throws ReplaceOLCFailedException {
		try{
			// Get Url detail from properties
			String mainUrl = properties.getProperty("streetlight.url.main");
			String dataUrl = properties.getProperty("streetlight.url.replaceolc");
			String replaceOlc = properties.getProperty("streetlight.url.replaceolc.method");
			String url = mainUrl + dataUrl;
			String controllerStrId = properties.getProperty("streetlight.controller.strid");
			List<Object> paramsList = new ArrayList<Object>();
			paramsList.add("methodName=" + replaceOlc);
			paramsList.add("controllerStrId=" + controllerStrId);
			paramsList.add("idOnController=" + slvSyncDataEntity.getIdOnController());
			paramsList.add("newNetworkId=" + macAddress);
			paramsList.add("ser=json");
			String params = StringUtils.join(paramsList, "&");
			url = url + "?" + params;
			ResponseEntity<String> response = restService.getPostRequest(url);
			String responseString = response.getBody();
			JsonObject replaceOlcResponse = (JsonObject) jsonParser.parse(responseString);
			String errorStatus = replaceOlcResponse.get("status").getAsString();
			// As per doc, errorcode is 0 for success. Otherwise, its not success.
			if (errorStatus.equals("ERROR")) {
				String value = replaceOlcResponse.get("value").getAsString();
				throw new ReplaceOLCFailedException("Replace OLC Failed."+value);
			}
			
		}catch(Exception e){
			throw new ReplaceOLCFailedException("Replace OLC Failed."+e.getMessage());
		}
		
	}
	
	
	/**
	 * Load Mac address and corresponding IdOnController from SLV Server
	 * @throws Exception 
	 */
	public boolean checkMacAddressExists(String macAddress,String idOnController)throws QRCodeAlreadyUsedException{
		String mainUrl = properties.getProperty("streetlight.url.main");
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
		ResponseEntity<String> response = restService.getRequest(url,true);
		if(response.getStatusCode().toString().equals(HttpStatus.OK.toString())){
			String responseString = response.getBody();
			DeviceMacAddress deviceMacAddress = gson.fromJson(responseString, DeviceMacAddress.class);
			List<Value> values = deviceMacAddress.getValue();
			StringBuilder stringBuilder = new StringBuilder();
			if(values == null || values.size() == 0){
				throw new QRCodeAlreadyUsedException("Existing QR Code not match.");
			}else{
				for(Value value : values){
					if(value.getIdOnController().equals(idOnController)){
						return  false;
					}
					stringBuilder.append(value.getIdOnController());
					stringBuilder.append("\n");
				}
			}
			throw new QRCodeAlreadyUsedException("Existing QR Code present in following IdonController "+stringBuilder.toString());
		}else{
			throw new QRCodeAlreadyUsedException("Error while checking Existing SELC QR Code.");
		}
		
	}
	
	
	private void loadDimmingValue() {
		BufferedReader csvFile = null;
		try {
			csvFile = new BufferedReader(new FileReader(properties.getProperty("dimming.group.csv.location")));
			String currentLine;
			while ((currentLine = csvFile.readLine()) != null) {
				String[] stringArray = currentLine.split(",");
				String key = stringArray[0];
				if(key.contains("New Pole")){
					key = key.replaceAll("#", "");
				}
				dimmingValue.put(key, stringArray[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (csvFile != null) {
				try {
					csvFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
