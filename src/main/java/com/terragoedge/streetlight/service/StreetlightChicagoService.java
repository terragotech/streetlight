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

import com.terragoedge.streetlight.logging.InstallMaintenanceLogModel;
import com.terragoedge.streetlight.logging.LoggingModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import com.terragoedge.streetlight.exception.QRCodeNotMatchedException;
import com.terragoedge.streetlight.exception.ReplaceOLCFailedException;

public class StreetlightChicagoService extends AbstractProcessor{



	final Logger logger = Logger.getLogger(StreetlightChicagoService.class);
    InstallationMaintenanceProcessor installationMaintenanceProcessor;

	public StreetlightChicagoService() {
        super();
        installationMaintenanceProcessor = new InstallationMaintenanceProcessor();

	}

	public void run() {
		// Get Already synced noteguids from Database
		List<String> noteGuids = streetlightDao.getNoteIds();
		String accessToken = getEdgeToken();
		if (accessToken == null) {
			logger.error("Edge Invalid UserName and Password.");
			return;
		}
		// Get Edge Server Url from properties
		String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");

		url = url + PropertiesReader.getProperties().getProperty("streetlight.edge.url.notes.get");

		String systemDate = PropertiesReader.getProperties().getProperty("streetlight.edge.customdate");

		if (systemDate == null || systemDate.equals("false")) {
			String yesterday = getYesterdayDate();
			url = url + "modifiedAfter=" + yesterday;
		}

		// Get NoteList from edgeserver
		ResponseEntity<String> responseEntity = restService.getRequest(url, false, accessToken);

		// Process only response code as success
		if (responseEntity.getStatusCode().is2xxSuccessful()) {

			// Get Response String
			String notesData = responseEntity.getBody();
			System.out.println(notesData);

			// Convert notes Json to List of notes object
			Type listType = new TypeToken<ArrayList<EdgeNote>>() {
			}.getType();
			List<EdgeNote> edgeNoteList = gson.fromJson(notesData, listType);

			// Iterate each note
			for (EdgeNote edgeNote : edgeNoteList) {
			    try{
                    if (!noteGuids.contains(edgeNote.getNoteGuid())) {
                        InstallMaintenanceLogModel installMaintenanceLogModel = new InstallMaintenanceLogModel();

                        installMaintenanceLogModel.setProcessedNoteId(edgeNote.getNoteGuid());
                        installMaintenanceLogModel.setNoteName(edgeNote.getTitle());
                        installMaintenanceLogModel.setCreatedDatetime(String.valueOf(edgeNote.getCreatedDateTime()));

                        installationMaintenanceProcessor.processNewAction(edgeNote,installMaintenanceLogModel);
                        if(installMaintenanceLogModel.isProcessOtherForm()){
                            LoggingModel loggingModel = new LoggingModel();
                            syncData(edgeNote, noteGuids, loggingModel);
                            if (!loggingModel.isNoteAlreadySynced()) {
                                streetlightDao.insertProcessedNotes(loggingModel,installMaintenanceLogModel);
                            }
                        }else{
                            LoggingModel loggingModel = installMaintenanceLogModel;
                            streetlightDao.insertProcessedNotes(loggingModel,installMaintenanceLogModel);
                        }
                    }

                }catch (Exception e){
			        logger.error("Error while processing edge note. NoteGuid :"+edgeNote.getNoteGuid(),e);
                }





			}
		} else {
			logger.error("Unable to get message from EdgeServer. Response Code is :" + responseEntity.getStatusCode());
		}
	}

	public String getYesterdayDate() {
		// 2017-11-01T13:00:00.000-00:00
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS-00:00");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return dateFormat.format(cal.getTime());
	}

	private void syncData(EdgeNote edgeNote, List<String> noteGuids, LoggingModel loggingModel) {
		try {
			// Check current note is already synced with slv or not.
			if (!noteGuids.contains(edgeNote.getNoteGuid())) {
				// Get Form List
				List<FormData> formDatasList = edgeNote.getFormData();
				Map<String, FormData> formDataMaps = new HashMap<>();
				for (FormData formData : formDatasList) {
					formDataMaps.put(formData.getFormTemplateGuid(), formData);
				}
				loggingModel.setProcessedNoteId(edgeNote.getNoteGuid());
				loggingModel.setNoteName(edgeNote.getTitle());
				loggingModel.setCreatedDatetime(String.valueOf(edgeNote.getCreatedDateTime()));
				syncData(formDataMaps, edgeNote, noteGuids, loggingModel);

			} else {
				// Logging this note is already synced with SLV.
				logger.info("Note " + edgeNote.getTitle() + " is already synced with SLV.");
				loggingModel.setNoteAlreadySynced(true);
			}
		} catch (QRCodeAlreadyUsedException e1) {
			logger.error("MacAddress (" + e1.getMacAddress()
					+ ")  - Already in use. So this pole is not synced with SLV. Note Title :[" + edgeNote.getTitle()
					+ " ]");
			loggingModel.setStatus(MessageConstants.ERROR);
			loggingModel.setErrorDetails("MacAddress (" + e1.getMacAddress() + ")  - Already in use");
		} catch (ReplaceOLCFailedException e) {
			logger.error("Error in syncData", e);
			loggingModel.setStatus(MessageConstants.ERROR);
			loggingModel.setErrorDetails(e.getMessage());
		} catch (DeviceUpdationFailedException e) {
			logger.error("Error in syncData", e);
			loggingModel.setStatus(MessageConstants.ERROR);
			loggingModel.setErrorDetails(e.getMessage());
		} catch (InValidBarCodeException e) {
			logger.error("Error in syncData", e);
			loggingModel.setStatus(MessageConstants.ERROR);
			loggingModel.setErrorDetails(e.getMessage());
		} catch (NoValueException e) {
			logger.error("Error in syncData", e);
			loggingModel.setStatus(MessageConstants.ERROR);
			loggingModel.setErrorDetails(e.getMessage());
		} catch (Exception e) {
			logger.error("Error in syncData", e);
			loggingModel.setStatus(MessageConstants.ERROR);
			loggingModel.setErrorDetails(e.getMessage());
		}
	}

	public void syncData(Map<String, FormData> formDatas, EdgeNote edgeNote, List<String> noteGuids,
			LoggingModel loggingModel) throws InValidBarCodeException, DeviceUpdationFailedException,
			QRCodeAlreadyUsedException, NoValueException, ReplaceOLCFailedException, Exception {
		List<Object> paramsList = new ArrayList<Object>();
		String chicagoFormTemplateGuid = properties.getProperty("streetlight.edge.formtemplateguid.chicago");
		String fixtureFormTemplateGuid = properties.getProperty("streetlight.edge.formtemplateguid.fixture");
		String replaceOlcFormTemplateGuid = properties.getProperty("streetlight.edge.formtemplateguid.replacenode");

		FormData replaceOLCFormData = formDatas.get(replaceOlcFormTemplateGuid);
		if (replaceOLCFormData == null) {
			replaceOLCFormData = formDatas.get("606fb4ca-40a4-466b-ac00-7c0434f82bfa");
		}

		if (replaceOLCFormData != null) {
			loggingModel.setIsQuickNote(true);
		}

		FormData fixtureFormData = formDatas.get(fixtureFormTemplateGuid);
		if (fixtureFormData == null) {
			logger.error("No Fixture FormTemplate is not Present. So note is not processed. Note Title is :"
					+ edgeNote.getTitle());
			loggingModel.setErrorDetails(MessageConstants.FIXTURE_FORM_NOT_AVAILABLE);
			loggingModel.setStatus(MessageConstants.ERROR);
			return;
		}

		// Process Fixture Form data
		List<EdgeFormData> fixtureFromDef = fixtureFormData.getFormDef();

		// Get IdOnController value
		String idOnController = null;
		try {
			idOnController = value(fixtureFromDef, properties.getProperty("edge.fortemplate.fixture.label.idoncntrl"));
			paramsList.add("idOnController=" + idOnController);
			loggingModel.setIdOnController(idOnController);
		} catch (NoValueException e) {
			loggingModel.setErrorDetails(MessageConstants.ID_ON_CONTROLLER_NOT_AVAILABLE);
			loggingModel.setStatus(MessageConstants.ERROR);
			return;
		}

		FormData chicagoFromData = formDatas.get(chicagoFormTemplateGuid);
		if (chicagoFromData == null) {
			loggingModel.setErrorDetails(MessageConstants.CHICAGO_FORM_NOT_AVAILABLE);
			loggingModel.setStatus(MessageConstants.ERROR);
			logger.error("No Chicago FormTemplate is not Present. So note is not processed. Note Title is :"
					+ edgeNote.getTitle());
			return;
		}

		// Get ControllerStdId value
		String controllerStrId = null;
		try {
			controllerStrId = value(fixtureFromDef, properties.getProperty("edge.fortemplate.fixture.label.cnrlstrid"));
			paramsList.add("controllerStrId=" + controllerStrId);
		} catch (NoValueException e) {
			loggingModel.setErrorDetails(MessageConstants.CONTROLLER_STR_ID_NOT_AVAILABLE);
			loggingModel.setStatus(MessageConstants.ERROR);
			return;
		}

		if (replaceOLCFormData != null) {
			// Get ControllerStdId value
			String geoZoneId = null;
			try {
				geoZoneId = value(fixtureFromDef, "GeoZoneId");
			} catch (NoValueException e) {
				e.printStackTrace();
			}
			processReplaceOLCFormVal(replaceOLCFormData, idOnController, controllerStrId, paramsList, geoZoneId,
					edgeNote.getNoteGuid(), edgeNote.getCreatedDateTime(), noteGuids, edgeNote, loggingModel);
		} else {
			// Get Fixture Code
			try {
				String fixtureCode = value(fixtureFromDef,
						properties.getProperty("edge.fortemplate.fixture.label.fixture.code"));
			} catch (NoValueException e) {
				e.printStackTrace();
				loggingModel.setErrorDetails(MessageConstants.FIXTURE_CODE_NOT_AVAILABLE);
				loggingModel.setStatus(MessageConstants.ERROR);
				return;
			}

			String macAddress = null;
			// Process Chicago Form data
			List<EdgeFormData> chicagoFromDef = chicagoFromData.getFormDef();
			for (EdgeFormData edgeFormData : chicagoFromDef) {
				if (edgeFormData.getLabel()
						.equals(properties.getProperty("edge.fortemplate.chicago.label.fixture.macaddress"))) {
					if (edgeFormData.getValue() == null || edgeFormData.getValue().trim().isEmpty()) {
						// logger.info("Fixture MAC address is empty. So note is not processed. Note
						// Title :"+edgeNote.getTitle());
						// return; -- TODO Need to skip or not later decide
					} else {
						addStreetLightData("luminaire.installdate", dateFormat(edgeNote.getCreatedDateTime()),
								paramsList); // -- TODO
						buildFixtureStreetLightData(edgeFormData.getValue(), paramsList, edgeNote);
					}

				} else if (edgeFormData.getLabel()
						.equals(properties.getProperty("edge.fortemplate.chicago.label.node.macaddress"))) {
					if (edgeFormData.getValue() == null || edgeFormData.getValue().trim().isEmpty()) {
						logger.info("Node MAC address is empty. So note is not processed. Note Title :"
								+ edgeNote.getTitle());
						loggingModel.setErrorDetails(MessageConstants.NODE_MAC_ADDRESS_NOT_AVAILABLE);
						loggingModel.setStatus(MessageConstants.ERROR);
						return;
					}
					macAddress = loadMACAddress(edgeFormData.getValue(), paramsList, idOnController);
				}
			}
			loggingModel.setMacAddress(macAddress);

            addOtherParams(edgeNote,paramsList);

			String controllerStrIdValue = value(fixtureFromDef,
					properties.getProperty("edge.fortemplate.fixture.label.cnrlstrid"));
			// DimmingGroupName
			sync2Slv(paramsList, edgeNote.getNoteGuid(), idOnController, macAddress, controllerStrIdValue, edgeNote,
					loggingModel);
			noteGuids.add(edgeNote.getNoteGuid());
		}

	}




	private void processReplaceOLCFormVal(FormData replaceOLCFormData, String idOnController,
			String controllerStrIdValue, List<Object> paramsList, String geoZoneId, String noteGuid,
			long noteCreatedDateTime, List<String> noteGuids, EdgeNote edgeNote, LoggingModel loggingModel)
			throws QRCodeAlreadyUsedException, DeviceUpdationFailedException, Exception {
		List<EdgeFormData> replaceOLCFromDef = replaceOLCFormData.getFormDef();
		String existingNodeMacAddress = null;
		String newNodeMacAddress = null;
		// Get Existing Node MAC Address value
		try {
			existingNodeMacAddress = value(replaceOLCFromDef,
					properties.getProperty("streetlight.edge.replacenode.label.existing"));
			loggingModel.setExistingNodeMACaddress(existingNodeMacAddress);
		} catch (NoValueException e) {
			e.printStackTrace();
			loggingModel.setErrorDetails(MessageConstants.OLD_MAC_ADDRESS_NOT_AVAILABLE);
			loggingModel.setStatus(MessageConstants.ERROR);
			return;
		}
		// Get New Node MAC Address value
		try {
			newNodeMacAddress = value(replaceOLCFromDef,
					properties.getProperty("streetlight.edge.replacenode.label.newnode"));
			loggingModel.setNewNodeMACaddress(newNodeMacAddress);
		} catch (NoValueException e) {
			e.printStackTrace();
			loggingModel.setErrorDetails(MessageConstants.NEW_MAC_ADDRESS_NOT_AVAILABLE);
			loggingModel.setStatus(MessageConstants.ERROR);
			return;
		}
		String comment = "";
		// Check existingNodeMacAddress is valid or not
		try {
			comment = validateMacAddress(existingNodeMacAddress, idOnController, controllerStrIdValue, geoZoneId);
			// comment = validateMACAddress(existingNodeMacAddress, idOnController,
			// geoZoneId);
		} catch (QRCodeNotMatchedException e1) {
			loggingModel.setErrorDetails(MessageConstants.REPLACE_MAC_NOT_MATCH);
			loggingModel.setStatus(MessageConstants.ERROR);
			return;
		}

		checkMacAddressExists(newNodeMacAddress, idOnController);

		boolean isError = false;
		StringBuffer statusDescription = new StringBuffer();
		// Call Empty ReplaceOLC
		try {
			replaceOLC(controllerStrIdValue, idOnController, "");
			statusDescription.append(MessageConstants.EMPTY_REPLACE_OLC_SUCCESS);
		} catch (ReplaceOLCFailedException e) {
			statusDescription.append(e.getMessage());
			isError = true;
			e.printStackTrace();
		}

		// update device with new mac address
		addStreetLightData("MacAddress", newNodeMacAddress, paramsList);
		comment = comment + " replaced on " + dateFormat(noteCreatedDateTime);
		addStreetLightData("comment", comment, paramsList);

		try {
			String fixtureQRScan = value(replaceOLCFromDef, "Fixture QR Scan");
			addStreetLightData("luminaire.installdate", dateFormat(edgeNote.getCreatedDateTime()), paramsList); // --
																												// TODO
			buildFixtureStreetLightData(fixtureQRScan, paramsList, edgeNote);
		} catch (NoValueException e) {
			e.printStackTrace();
		} catch (InValidBarCodeException e) {
			e.printStackTrace();
		}

		int errorCode = setDeviceValues(paramsList);
		if (errorCode != 0) {
			statusDescription.append(MessageConstants.ERROR_UPDATE_DEVICE_VAL + errorCode);
			loggingModel.setErrorDetails(statusDescription.toString());
			loggingModel.setStatus(MessageConstants.ERROR);
			throw new DeviceUpdationFailedException(errorCode + "");
		} else {
			statusDescription.append(MessageConstants.SET_DEVICE_SUCCESS);
			try {
				// Call New Node MAC Address
				replaceOLC(controllerStrIdValue, idOnController, newNodeMacAddress);
				statusDescription.append(MessageConstants.NEW_REPLACE_OLC_SUCCESS);

				// Need to add new changes.

			} catch (Exception e) {
				isError = true;
				statusDescription.append(e.getMessage());
				e.printStackTrace();
			}
			if (isError) {
				loggingModel.setErrorDetails(statusDescription.toString());
				loggingModel.setStatus(MessageConstants.ERROR);
			} else {
				loggingModel.setStatus(MessageConstants.SUCCESS);
			}
			noteGuids.add(noteGuid);
		}

	}



	public void sync2Slv(List<Object> paramsList, String noteGuid, String idOnController, String macAddress,
			String controllerStrIdValue, EdgeNote edgeNote, LoggingModel loggingModel)
			throws DeviceUpdationFailedException, ReplaceOLCFailedException {
		int errorCode = setDeviceValues(paramsList);
		// As per doc, errorcode is 0 for success. Otherwise, its not
		// success.
		if (errorCode != 0) {
			loggingModel.setErrorDetails(MessageConstants.ERROR_UPDATE_DEVICE_VAL);
			loggingModel.setStatus(MessageConstants.ERROR);

			throw new DeviceUpdationFailedException(errorCode + "");
		} else {
			// replace OlC
			replaceOLC(controllerStrIdValue, idOnController, macAddress);
			loggingModel.setStatus(MessageConstants.SUCCESS);

		}
	}


	private String loadMACAddress(String data, List<Object> paramsList, String idOnController)
			throws InValidBarCodeException, QRCodeAlreadyUsedException, Exception {
		if (data.contains("MACid")) {
			String[] nodeInfo = data.split(",");
			if (nodeInfo.length > 0) {
				for (String nodeData : nodeInfo) {
					if (nodeData.startsWith("MACid")) {
						String macAddress = nodeData.substring(6);
						checkMacAddressExists(macAddress, idOnController);
						addStreetLightData("MacAddress", macAddress, paramsList);
						return macAddress;
					}
				}
			}
		} else {
			checkMacAddressExists(data, idOnController);
			addStreetLightData("MacAddress", data, paramsList);
			return data;
		}

		throw new InValidBarCodeException("Node MAC address is not valid. Value is:" + data);
	}

	// http://192.168.1.9:8080/edgeServer/oauth/token?grant_type=password&username=admin&password=admin&client_id=edgerestapp

	private String getEdgeToken() {
		String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
		String userName = properties.getProperty("streetlight.edge.username");
		String password = properties.getProperty("streetlight.edge.password");
		url = url + "/oauth/token?grant_type=password&username=" + userName + "&password=" + password
				+ "&client_id=edgerestapp";
		ResponseEntity<String> responseEntity = restService.getRequest(url);
		if (responseEntity.getStatusCode().is2xxSuccessful()) {
			JsonObject jsonObject = (JsonObject) jsonParser.parse(responseEntity.getBody());
			return jsonObject.get("access_token").getAsString();
		}
		return null;

	}

}
