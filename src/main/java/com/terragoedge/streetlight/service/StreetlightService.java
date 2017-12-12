package com.terragoedge.streetlight.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.StreetLightData;
import com.terragoedge.streetlight.dao.StreetlightDao;
import com.terragoedge.streetlight.entity.EdgeFormValues;
import com.terragoedge.streetlight.entity.EdgeNoteDetails;
import com.terragoedge.streetlight.entity.EdgeSLNumber;
import com.terragoedge.streetlight.entity.SlvSyncDataEntity;
import com.terragoedge.streetlight.exception.DeviceCreationFailedException;
import com.terragoedge.streetlight.exception.DeviceNotFoundException;
import com.terragoedge.streetlight.exception.DeviceUpdationFailedException;
import com.terragoedge.streetlight.exception.InValidSLNumberException;
import com.terragoedge.streetlight.exception.QRCodeAlreadyUsedException;
import com.terragoedge.streetlight.exception.ReplaceOLCFailedException;
import com.terragoedge.xml.devices.SLVDevice;
import com.terragoedge.xml.devices.SLVDeviceArray;
import com.terragoedge.xml.devices.SLVKeyValuePair;
import com.terragoedge.xml.devices.Value;
import com.terragoedge.xml.parser.XMLMarshaller;

public class StreetlightService {

	StreetlightDao streetlightDao = null;
	HashMap<String, SLVDevice> devices = new HashMap<String, SLVDevice>();
	
	HashMap<String, List<String>> macAddress = new HashMap<String, List<String>>();
	Properties properties = null;
	RestService restService = null;
	Gson gson = null;
	JsonParser jsonParser = null;
	EdgeMailService edgeMailService = null;

	Map<String, String> dimmingValue = new HashMap<>();

	static Map<String, Integer> luminareCore = new HashMap<>();
	
	final Logger logger = Logger.getLogger(StreetlightService.class);

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

	public StreetlightService() {
		streetlightDao = new StreetlightDao();
		properties = PropertiesReader.getProperties();
		restService = new RestService();
		gson = new Gson();
		jsonParser = new JsonParser();
		edgeMailService = new EdgeMailService();
		loadDimmingValue();
	}

	public void run() {
		try {
			// load SLV Devices
			getDevices();
			
			// Read Block formtemplate Guids from Properties
			String blockFormTemplatesGuids = properties.getProperty("streetlight.ef.formtemplateguid.block");
			String[] blockFormTemplatesList = blockFormTemplatesGuids.split(",");
			
			//Read Richmond formtemplate Guid from Properties
			String richFormtemplateGuids = properties.getProperty("streetlight.ef.formtemplateguid.richmond");
			String[] richFormtemplatesList = richFormtemplateGuids.split(",");
			
			List<String> noteIds = new ArrayList<>();
			
			for(String richFormtemplateGuid : richFormtemplatesList){
				//Get Richmond form notes
				List<String> noteIdsTemp = streetlightDao.getNoteIds(richFormtemplateGuid); // -- TODO Need to get is current notes
				noteIds.addAll(noteIdsTemp);
			}
			
			for (String noteId : noteIds) {
				try {
					// Check this note is already synced with SLV or not
					boolean isNotePresent = streetlightDao.isNotePresent(noteId);
					
					if (!isNotePresent) {
						// Get All form details for particular notes
						Map<String, EdgeNoteDetails> formDetailsHolder = streetlightDao.getFormData(noteId);
						
						if(formDetailsHolder.size() == 0){
							continue;
						}
						
						//Holds SLNumber
						EdgeSLNumber edgeSLNumber = new EdgeSLNumber();
						
						// Check current note have block form or not. If not, try to get Base Parent note and also validate SL Number format
						boolean isBlockFormPresent = validateBlockFormPresent(formDetailsHolder,blockFormTemplatesList,edgeSLNumber);
						
						if (isBlockFormPresent) {
							if (richFormtemplatesList != null && richFormtemplatesList.length > 0)  {
								
								// Get Richmond form deails
								EdgeNoteDetails edgeNoteDetails =  null;
								for(String richFormtemplateGuid : richFormtemplatesList){
									 edgeNoteDetails = formDetailsHolder.get(richFormtemplateGuid);
									 if(edgeNoteDetails != null){
										 break;
									 }
								}
								
								
								// Check QR code is present or not in Richmond Hill streets.
								boolean isQRCodePresent = validateQRCodePresent(edgeNoteDetails);
								// Get ReplaceNode QR Code
								String replaceNoteValue = getReplaceNodeValue(formDetailsHolder);
								
								// If QR Code is Present in Richmond, then process. Otherwise, skipped this note
								if (isQRCodePresent) {
									List<EdgeNoteDetails> edgeNoteDetailsList = new ArrayList<>(formDetailsHolder.values());
									// Sync data with slv
									sendFromData(edgeNoteDetailsList, edgeNoteDetails,replaceNoteValue);
									
								} else {
									logger.warn("QR Code Not Present."+edgeNoteDetails.getTitle());
									logger.warn("SL Number is "+edgeSLNumber.getSlNumber());
									if(edgeSLNumber.getSlNumber() != null){
										SLVDevice slvDevice = devices.get(edgeSLNumber.getSlNumber().trim());
										if(slvDevice != null){
											reSetQrCodeInSLV(richFormtemplatesList, edgeNoteDetails, formDetailsHolder);
										}else{
											logger.warn("Richmond QR Code is empty.Note Id:"+edgeNoteDetails.getNoteId()+" , Note Title:"+edgeNoteDetails.getTitle());
										}
									}else{

										logger.warn("Richmond QR Code is empty.Note Id:"+edgeNoteDetails.getNoteId()+" , Note Title:"+edgeNoteDetails.getTitle());
										if (replaceNoteValue != null) {
											logger.warn("Richmond QR Code is empty but Replace Node QR Code is Not Empty"
													+ ". Note is not processed. Note Id:"+edgeNoteDetails.getNoteId()+" , Note Title:"+edgeNoteDetails.getTitle() +",Replace Node QR Code:"+replaceNoteValue);
											// Send mail - Richmond QR code not present and Replace Node QR Code Present
											edgeMailService.sendMailQRCodeMissing(edgeNoteDetails.getTitle(), replaceNoteValue);
											streetlightDao.insertProcessedNoteId(Integer.valueOf(noteId));
										}
									}
									
									
																	}
							}

						} else {
							logger.warn("Block Form is not present. Note is not processed. Note Id:"+noteId);
						}
					}
				} catch (InValidSLNumberException e) {
					logger.warn("InValid SL Number.Note is not processed. SLNumber:"+e.getMessage());
					edgeMailService.sendMailInValidSLNumber(e.getMessage());
					streetlightDao.insertProcessedNoteId(Integer.valueOf(noteId));
				} catch (Exception e) {
					logger.error("Error processing NoteId:"+noteId, e);
				}

			}
		} catch (Exception e) {
			logger.error("Error in run", e);
		}
	}
	
	
	
	private void reSetQrCodeInSLV(String[] richFormTemplateGuids,EdgeNoteDetails edgeNoteDetails,Map<String, EdgeNoteDetails> formDetailsHolder) throws Exception{
		List<String> formDefList = streetlightDao.getFormDef(richFormTemplateGuids, edgeNoteDetails.getRevisionFromNoteId());
		if(formDefList.size() > 0){
			for(String formDef : formDefList){
				String qrCode = getQRCode(formDef);
				if(qrCode != null){
					List<EdgeNoteDetails> edgeNoteDetailsList = new ArrayList<>(formDetailsHolder.values());
					// Sync data with slv
					sendFromData(edgeNoteDetailsList, edgeNoteDetails,null);
					return;
				}
			}
		}
		
		logger.warn("Richmond QR Code is empty.Note Id:"+edgeNoteDetails.getNoteId()+" , Note Title:"+edgeNoteDetails.getTitle());
	}

	/**
	 * Check QR Code is present or not
	 * @param edgeNoteDetails
	 * @return
	 */
	private boolean validateQRCodePresent(EdgeNoteDetails edgeNoteDetails) {
		if (edgeNoteDetails == null) {
			return false;
		}
		EdgeFormValues edgeFormValues = edgeNoteDetails.getEdgeFormValues();
		if (edgeFormValues != null) {
			String formDef = edgeFormValues.getFormDef();
			String qrCode = getQRCode(formDef);
			return qrCode == null ? false : true;
		}
		return false;
	}
	
	/**
	 * Get QR Code from formDef.
	 * @param formDef
	 * @return
	 */
	private String getQRCode(String formDef){
		Type listType = new TypeToken<ArrayList<EdgeFormData>>() {
		}.getType();

		List<EdgeFormData> edgeFormDataList = gson.fromJson(formDef, listType);
		if (edgeFormDataList != null) {
			for (EdgeFormData edgeFormData : edgeFormDataList) {
				if (edgeFormData.getLabel().equals("SELC QR Code")) {
					String value = edgeFormData.getValue();
					if (value == null) {
						return null;
					}
					value = value.trim();
					if (value.isEmpty() || value.equalsIgnoreCase("(null)")) {
						return null;
					}
					return value;
				}
			}
		}
		return null;
	}

	/**
	 * Check whether block form is present or not. If not present, then load
	 * from base parent note. If base parent note is not present, result will be
	 * false. If base parent note doesn't have this form, result will be false
	 * 
	 * @param formDetailsHolder
	 * @param blockFormTemplatesList
	 * @return
	 * @throws InValidSLNumberException
	 */
	private boolean validateBlockFormPresent(Map<String, EdgeNoteDetails> formDetailsHolder,
			String[] blockFormTemplatesList,EdgeSLNumber edgeSLNumber) throws InValidSLNumberException {

		// Check Block form is present or not and also SL_Number format
		boolean res = validateBlockFormPresent(blockFormTemplatesList, formDetailsHolder,edgeSLNumber);
		// If not present, then load block form from Base Parent Note
		logger.info("Block form is not present.");
		if (!res) {
			List<EdgeNoteDetails> edgeNoteDetails = new ArrayList<>(formDetailsHolder.values());
			// Check if parent note is present or not. If not present, then we didn't get Parent NoteId
			String parentNoteId = edgeNoteDetails.get(0).getParentNoteId();
			
			logger.info("Block form is not present.parentNoteId"+parentNoteId);
			
			if (parentNoteId == null) {
				return false;
			}
			
			// Check which block formtemplate is assigned.
			String blockFormTemplateGuid = null;
			for (String blockFormTemplate : blockFormTemplatesList) {
				boolean isAssigned = streetlightDao.isFormTemplateAssigned(blockFormTemplate, parentNoteId);
				if (isAssigned) {
					blockFormTemplateGuid = blockFormTemplate;
					break;
				}
			}
			logger.info("Block form is not present.blockFormTemplateGuid"+blockFormTemplateGuid);
			if (blockFormTemplateGuid != null) {
				// Load Block form from database
				streetlightDao.getFormData(blockFormTemplateGuid, parentNoteId, formDetailsHolder);
				// Again check Block form is present or not and also SL_Number format.
				return validateBlockFormPresent(blockFormTemplatesList, formDetailsHolder, edgeSLNumber);
			} else {
				return false;
			}
		} else {
			return res;
		}

	}

	/**
	 * Check whether Block form is present or not.
	 * 
	 * @param blockFormTemplatesList
	 * @param formDetailsHolder
	 * @return
	 * @throws InValidSLNumberException
	 */
	private boolean validateBlockFormPresent(String[] blockFormTemplatesList,
			Map<String, EdgeNoteDetails> formDetailsHolder,EdgeSLNumber edgeSLNumber) throws InValidSLNumberException {
		for (String blockFormTemplateGuid : blockFormTemplatesList) {
			EdgeNoteDetails edgeNoteDetails = formDetailsHolder.get(blockFormTemplateGuid);
			if (edgeNoteDetails != null) {
				validateSLNumber(edgeNoteDetails,edgeSLNumber);
				return true;
			}
		}
		return false;
	}

	/**
	 * Get SLV Devices and Load to In-memory with MAC address as key
	 * @return
	 */
	public int getDevices() {
		// Clear Previous device
		devices.clear();
		macAddress.clear();
		// Get Url details from Properties
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
		// Call REST Service
		ResponseEntity<String> responseEntity = restService.getRequest(streetLightDataParams, url, false);
		String response = responseEntity.getBody();
		XMLMarshaller xmlMarshaller = new XMLMarshaller();
		SLVDeviceArray slvDeviceArray = (SLVDeviceArray) xmlMarshaller.xmlToObject(response);
		List<SLVDevice> slvDevices = slvDeviceArray.getSLVDevice();
		for (SLVDevice slvDev : slvDevices) {
			devices.put(slvDev.getIdOnController().trim(), slvDev);
			com.terragoedge.xml.devices.Properties property = slvDev.getProperties();
			List<SLVKeyValuePair> slvKeyValueList = property.getSLVKeyValuePair();
			for (SLVKeyValuePair slvKey : slvKeyValueList) {
				String key = slvKey.getKey().trim().toLowerCase();
				if (key.equalsIgnoreCase("userproperty.macaddress")) {
					Value value = slvKey.getValue(); // userproperty.MacAddress
					addMacAddress(value.getContent().trim(), slvDev.getIdOnController().trim());
					break;
				}
			}
		}

		return -1;
	}

	private void loadDimmingValue() {
		BufferedReader csvFile = null;
		try {
			csvFile = new BufferedReader(new FileReader(properties.getProperty("dimming.group.csv")));
			String currentLine;
			while ((currentLine = csvFile.readLine()) != null) {
				String[] stringArray = currentLine.split(",");
				dimmingValue.put(stringArray[0], stringArray[1]);
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

	/**
	 * Validate SLNumber format. It must be number-number
	 * @param edgeNoteDetails
	 * @throws InValidSLNumberException
	 */
	private void validateSLNumber(EdgeNoteDetails edgeNoteDetails,EdgeSLNumber edgeSLNumber) throws InValidSLNumberException {
		if (edgeNoteDetails != null) {
			EdgeFormValues edgeFormValues = edgeNoteDetails.getEdgeFormValues();
			if (edgeFormValues != null) {
				String formDef = edgeFormValues.getFormDef();
				Type listType = new TypeToken<ArrayList<EdgeFormData>>() {
				}.getType();

				List<EdgeFormData> edgeFormDataList = gson.fromJson(formDef, listType);
				EdgeFormData tempEdgeFormData = new EdgeFormData();
				tempEdgeFormData.setLabel("SL"); 

				int pos = edgeFormDataList.indexOf(tempEdgeFormData);
				
				if(pos == -1){
					tempEdgeFormData.setLabel("SL_Num");
					pos = edgeFormDataList.indexOf(tempEdgeFormData);
				}
				
				if (pos != -1) {
					EdgeFormData edgeFormData = edgeFormDataList.get(pos);
					String value = edgeFormData.getValue();
					logger.warn("SL Number is"+value);
					if (value == null || value.trim().isEmpty()) {
						throw new InValidSLNumberException(value);// Need to tthrow
					} else {
						if(value.contains("New Pole")){
							value =	value.replaceAll("#", "");
							edgeSLNumber.setSlNumber(value);
						}else{
							edgeSLNumber.setSlNumber(value);
							value = value.replaceAll("-", "");
							Pattern p = Pattern.compile("^\\d+$");
							Matcher m = p.matcher(value);
							if (!m.matches()) {
								throw new InValidSLNumberException(value); // Need to
																		// throw
							}
						}
						
					}
					
				} else {
					throw new InValidSLNumberException(null);// Need to throw
				}
			}
		} else {
			throw new InValidSLNumberException(null);// Need to throw
		}
	}

	/**
	 * Get Replace Node QRCode Value
	 * @param formDetailsHolder
	 * @return
	 */
	private String getReplaceNodeValue(Map<String, EdgeNoteDetails> formDetailsHolder) {
		String replaceNodeForm = properties.getProperty("streetlight.ef.formtemplateguid.replacenode");
		EdgeNoteDetails edgeNoteDetails = formDetailsHolder.get(replaceNodeForm);
		if (edgeNoteDetails != null) {
			EdgeFormValues edgeFormValues = edgeNoteDetails.getEdgeFormValues();
			if (edgeFormValues != null) {
				String formDef = edgeFormValues.getFormDef();
				Type listType = new TypeToken<ArrayList<EdgeFormData>>() {
				}.getType();

				List<EdgeFormData> edgeFormDataList = gson.fromJson(formDef, listType);
				EdgeFormData tempEdgeFormData = new EdgeFormData();
				tempEdgeFormData.setLabel("NODE Barcode"); // Need to set

				int pos = edgeFormDataList.indexOf(tempEdgeFormData);

				if (pos != -1) {
					EdgeFormData edgeFormData = edgeFormDataList.get(pos);
					String value = edgeFormData.getValue();
					if (value != null && !value.trim().isEmpty() && !value.trim().equalsIgnoreCase("(null)")) {
						return value.trim();
					}

				}

			}
		}
		return null;
	}

	/**
	 * Convert Edgedata to SLV data and Sync with SLV
	 * @param edgeNoteDetailsList
	 * @param edgeNoteDetail
	 * @throws Exception
	 */
	public void sendFromData(List<EdgeNoteDetails> edgeNoteDetailsList,EdgeNoteDetails edgeNoteDetail,String replaceNoteValue) throws Exception {
		try {

			Type listType = new TypeToken<ArrayList<EdgeFormData>>() {
			}.getType();
			
			// Get Mapping Json
			JsonObject mappingJson = getStreetLightMappingJson();

			SlvSyncDataEntity slvSyncDataEntity = new SlvSyncDataEntity();
			slvSyncDataEntity.setLat(edgeNoteDetail.getLat());
			slvSyncDataEntity.setLng(edgeNoteDetail.getLng());

			String comment = "";
			int power2Watt = 0;
			int lWatt = 0;
			for (EdgeNoteDetails edgeNoteDetails : edgeNoteDetailsList) {
				String formData = edgeNoteDetails.getEdgeFormValues().getFormDef();
				if (slvSyncDataEntity.getParentNoteId() == null || slvSyncDataEntity.getParentNoteId().isEmpty()) {
					slvSyncDataEntity.setParentNoteId(edgeNoteDetails.getParentNoteId());
				}

				List<EdgeFormData> edgeFormDataList = gson.fromJson(formData, listType);
				if (edgeFormDataList != null) {
					for (EdgeFormData edgeFormData : edgeFormDataList) {
						JsonElement streetLightKey = mappingJson.get(edgeFormData.getLabel());
						if (streetLightKey != null && !streetLightKey.isJsonNull()) {
							String key = streetLightKey.getAsString();
							String value = edgeFormData.getValue();

							if (edgeFormData.getLabel().equals("New Luminare Code")) {
								if (value != null && !(value.trim().isEmpty())
										&& !(value.trim().equalsIgnoreCase("(null)"))) {
									String[] luminareCoreValues = value.split("-");
									if (luminareCoreValues.length > 0) {
										String luminareCoreValue = luminareCoreValues[0].trim();
										if (luminareCore.containsKey(luminareCoreValue)) {
											lWatt = luminareCore.get(luminareCoreValue);
										}
									}
								}
							}
							StreetLightData streetLightData = new StreetLightData();
							streetLightData.setKey(key);
							streetLightData.setValue(value);
							if (key.equalsIgnoreCase("idOnController")) {
								if (value.isEmpty() || value.equalsIgnoreCase("(null)")) {
									 logger.warn("Not Processed because idonController value is empty. NoteId:"+edgeNoteDetails.getNoteId()+"-"+edgeNoteDetails.getTitle());
									return;
								}
								
								slvSyncDataEntity.setIdOnController(value);
							}
							if (key.equalsIgnoreCase("power2")) {
								if (value != null && !(value.trim().isEmpty())
										&& !(value.trim().equalsIgnoreCase("(null)"))) {
									String temp = value.replaceAll("[^\\d.]", "");
									temp = temp.trim();
									power2Watt = Integer.parseInt(temp);
								}

							}
							if (key.equalsIgnoreCase("location.mapnumber")) {
								slvSyncDataEntity.setBlockName("Block " + value);

							}
							if (key.equalsIgnoreCase("MacAddress")) {
								slvSyncDataEntity.setMacAddress(value);
							}
							if (key.equalsIgnoreCase("comment")) {
								comment = comment + " " + edgeFormData.getLabel() + ":" + value;
							} else {
								if (key.equalsIgnoreCase("power")) {
									if (value != null && !(value.trim().isEmpty())
											&& !(value.trim().equalsIgnoreCase("(null)"))) {
										String temp = value.replaceAll("[^\\d.]", "");
										temp = temp.trim();
										lWatt = Integer.parseInt(temp);
									}

								} else {
									if (!key.equalsIgnoreCase("MacAddress")) {
										slvSyncDataEntity.getStreetLightDatas().add(streetLightData);
									}

								}
							}
						}
					}
				}
			}

			if (slvSyncDataEntity.getStreetLightDatas().size() > 0) {
				if (lWatt == 0) {
					addStreetLightData("power", "39 W", slvSyncDataEntity.getStreetLightDatas());
					lWatt = 39;
				} else {
					addStreetLightData("power", lWatt + " W", slvSyncDataEntity.getStreetLightDatas());
				}
				int watt = power2Watt - lWatt;
				addStreetLightData("powerCorrection", watt + "", slvSyncDataEntity.getStreetLightDatas());
				addStreetLightData("location.utillocationid", edgeNoteDetail.getTitle() + ".Lamp", slvSyncDataEntity.getStreetLightDatas());
				String nodeTypeStrId = properties.getProperty("streetlight.equipment.type");
				addStreetLightData("modelFunctionId", nodeTypeStrId, slvSyncDataEntity.getStreetLightDatas());

				addStreetLightData("comment", comment, slvSyncDataEntity.getStreetLightDatas());

				String streetLightDate = dateFormat(edgeNoteDetail.getCreatedDateTime());
				addStreetLightData("lamp.installdate", streetLightDate, slvSyncDataEntity.getStreetLightDatas());
				
				String dimmingGroupName = dimmingValue.get(slvSyncDataEntity.getIdOnController());
				addStreetLightData("DimmingGroupName", dimmingGroupName, slvSyncDataEntity.getStreetLightDatas());
				
				
				slvSyncDataEntity.setReplaceNodeQRVal(replaceNoteValue);
				sync2SLV(slvSyncDataEntity,edgeNoteDetail);
			}

		} catch (Exception e) {
			 logger.error("Error in sendFromData",e);
		}
	}
	
	private void validateMacAddress(String qrCode,String idOnControllers) throws QRCodeAlreadyUsedException{
		List<String> idOnControllersList =	macAddress.get(qrCode.trim());
		if(idOnControllersList != null && !idOnControllersList.contains(idOnControllers.trim())){
			// Need to throw
			String slNums = StringUtils.join(idOnControllersList, "\n");
			throw new QRCodeAlreadyUsedException(slNums);
		}
	}

	private void sync2SLV(SlvSyncDataEntity slvSyncDataEntity,EdgeNoteDetails edgeNoteDetail) {
		String newNetworkId =  null;
		try {
			
			// Call Create Device
			createDeviceInSlv(slvSyncDataEntity,edgeNoteDetail);
			// call Update Device
			updateSLVData(slvSyncDataEntity,edgeNoteDetail);
			// ReplaceOLC
			
			
			 newNetworkId = slvSyncDataEntity.getMacAddress();
			 
			 if(newNetworkId != null && !newNetworkId.trim().isEmpty()){
				// Get Mac address from replacenode form
					if (slvSyncDataEntity.getReplaceNodeQRVal() != null) {
						newNetworkId = slvSyncDataEntity.getReplaceNodeQRVal();
					}
					//Validate Mac address is already used any pole or not
					validateMacAddress(newNetworkId.trim(), slvSyncDataEntity.getIdOnController());
			 }
			
			
			replaceOLC(slvSyncDataEntity,edgeNoteDetail);
		} catch (DeviceCreationFailedException dec) {
			logger.info("Error in DeviceCreationFailedException",dec);
			streetlightDao.insertProcessedNoteId(Integer.valueOf(edgeNoteDetail.getNoteId()));
			// Need to send mail
		} catch (DeviceUpdationFailedException e) {
			logger.info("Error in DeviceCreationFailedException",e);
			// Need to send mail
		} catch (ReplaceOLCFailedException e) {
			edgeMailService.sendMailReplaceOLCsErrorCode(slvSyncDataEntity.getIdOnController(), e.getMessage());
			streetlightDao.updateParentNoteId(slvSyncDataEntity.getParentNoteId(), edgeNoteDetail.getNoteId());
		} catch (DeviceNotFoundException e) {
			edgeMailService.sendMailDeviceNotFound(slvSyncDataEntity.getIdOnController(), slvSyncDataEntity.getReplaceNodeQRVal(), slvSyncDataEntity.getMacAddress());
			streetlightDao.insertProcessedNoteId(Integer.valueOf(edgeNoteDetail.getNoteId()));
		} catch (QRCodeAlreadyUsedException e) {
			edgeMailService.sendMailMacAddressAlreadyUsed(newNetworkId, e.getMessage());
			streetlightDao.updateParentNoteId(slvSyncDataEntity.getParentNoteId(), edgeNoteDetail.getNoteId());
		}
	}

	public void createDeviceInSlv(SlvSyncDataEntity slvSyncDataEntity,EdgeNoteDetails edgeNoteDetails)
			throws DeviceCreationFailedException, DeviceNotFoundException {
		SLVDevice slvDevice = devices.get(slvSyncDataEntity.getIdOnController().trim());
		// Need to create device in SLV
		if (slvDevice == null) {
			logger.info("ControllerId Present is not present in SLV.Device creation called.");
			// Check if replace node val is present and QR Code val is present.
			// Then throws devicenotfoundexception
			if (slvSyncDataEntity.getReplaceNodeQRVal() != null && !slvSyncDataEntity.getReplaceNodeQRVal().trim()
					.toLowerCase().equals(slvSyncDataEntity.getMacAddress().trim().toLowerCase())) {
				throw new DeviceNotFoundException(""); // TODO
			}

			// Get GeoZone for that Block
			String geoZoneId = getChildrenGeoZone(slvSyncDataEntity.getBlockName());
			if (geoZoneId != null) {
				// Check whether this device is already created or not as per
				// edge database
				if (!streetlightDao.isBaseParentNoteIdPresent(slvSyncDataEntity.getParentNoteId())) {
					ResponseEntity<String> responseEntity = createDevice(slvSyncDataEntity, geoZoneId);

					String status = responseEntity.getStatusCode().toString();
					String responseBody = responseEntity.getBody();
					if ((status.equalsIgnoreCase("200") || status.equalsIgnoreCase("ok"))
							&& !responseBody.contains("<status>ERROR</status>")) {
						logger.info("Device Created Successfully, NoteId:" + edgeNoteDetails.getNoteId() + "-" + edgeNoteDetails.getTitle());
						streetlightDao.insertParentNoteId(slvSyncDataEntity.getParentNoteId());

						SLVDevice slvDeviceTemp = new SLVDevice();
						devices.put(slvSyncDataEntity.getIdOnController().trim(), slvDeviceTemp);
					} else {
						throw new DeviceCreationFailedException(edgeNoteDetails.getNoteId() + "-" + slvSyncDataEntity.getIdOnController());
					}
				} else {
					throw new DeviceCreationFailedException(
							edgeNoteDetails.getNoteId() + "-" + slvSyncDataEntity.getIdOnController() + ". This Pole is already present in Edge DB.");
				}

			} else {
				throw new DeviceCreationFailedException(edgeNoteDetails.getNoteId() + "-" + slvSyncDataEntity.getIdOnController() + ". GeoZone value is null;");
			}
		}else{
			streetlightDao.insertParentNoteId(slvSyncDataEntity.getParentNoteId());
		}
	}

	/**
	 * Update Device data
	 * 
	 * @param streetLightDatas
	 * @param idOnController
	 * @return
	 */
	public void updateSLVData(SlvSyncDataEntity slvSyncDataEntity,EdgeNoteDetails edgeNoteDetails) throws DeviceUpdationFailedException {
		if (slvSyncDataEntity.getStreetLightDatas().size() > 0) {
			String mainUrl = properties.getProperty("streetlight.url.main");
			String dataUrl = properties.getProperty("streetlight.data.url");
			String insertDevice = properties.getProperty("streetlight.url.device.insert.device");
			String url = mainUrl + dataUrl;
			String controllerStrId = properties.getProperty("streetlight.controllerstr.id");
			List<Object> paramsList = new ArrayList<Object>();
			paramsList.add("methodName=" + insertDevice);
			paramsList.add("controllerStrId=" + controllerStrId);
			paramsList.add("idOnController=" + slvSyncDataEntity.getIdOnController());
			List<StreetLightData> streetLightDatas = slvSyncDataEntity.getStreetLightDatas();
			for (StreetLightData streetLightData : streetLightDatas) {
				paramsList.add("valueName=" + streetLightData.getKey());
				paramsList.add("value=" + streetLightData.getValue());
			}
			paramsList.add("ser=json");
			String params = StringUtils.join(paramsList, "&");
			url = url + "?" + params;
			ResponseEntity<String> response = restService.getRequest(url, true);
			String responseString = response.getBody();
			JsonObject replaceOlcResponse = (JsonObject) jsonParser.parse(responseString);
			int errorCode = replaceOlcResponse.get("errorCode").getAsInt();
			// As per doc, errorcode is 0 for success. Otherwise, its not
			// success.
			if (errorCode != 0) {
				throw new DeviceUpdationFailedException(errorCode + "");
			}
			
		}

	}

	/**
	 * Calls ReplaceOLCs
	 * 
	 * @param slvSyncDataEntity
	 * @throws ReplaceOLCFailedException
	 */
	public void replaceOLC(SlvSyncDataEntity slvSyncDataEntity,EdgeNoteDetails edgeNoteDetails) throws ReplaceOLCFailedException {
		try{
			String newNetworkId = slvSyncDataEntity.getMacAddress();
			// Get Mac address from replacenode form
			if (slvSyncDataEntity.getReplaceNodeQRVal() != null) {
				newNetworkId = slvSyncDataEntity.getReplaceNodeQRVal();
			}
			
			// Get Url detail from properties
			String mainUrl = properties.getProperty("streetlight.url.main");
			String dataUrl = properties.getProperty("streetlight.url.replaceolc");
			String replaceOlc = properties.getProperty("streetlight.url.replaceolc.method");
			String url = mainUrl + dataUrl;
			String controllerStrId = properties.getProperty("streetlight.controllerstr.id");
			List<Object> paramsList = new ArrayList<Object>();
			paramsList.add("methodName=" + replaceOlc);
			paramsList.add("controllerStrId=" + controllerStrId);
			paramsList.add("idOnController=" + slvSyncDataEntity.getIdOnController());
			paramsList.add("newNetworkId=" + newNetworkId);
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
				throw new ReplaceOLCFailedException(value);
			}
			
			streetlightDao.updateParentNoteId(slvSyncDataEntity.getParentNoteId(), edgeNoteDetails.getNoteId());
			addMacAddress(newNetworkId, slvSyncDataEntity.getIdOnController());
		}catch(Exception e){
			logger.error("Error in replaceOLC", e);
			throw new ReplaceOLCFailedException(e.getMessage());
		}
		
	}
	
	
	private void addMacAddress(String qrCode,String slNumber){
		logger.info("QRCode Added: "+macAddress.size());
		List<String> idOnControllers = macAddress.get(qrCode.trim().toLowerCase());
		if(idOnControllers == null){
			idOnControllers = new ArrayList<>();
			macAddress.put(qrCode.trim().toLowerCase(), idOnControllers);
		}
		idOnControllers.add(slNumber);
		
	}

	public ResponseEntity<String> createDevice(SlvSyncDataEntity slvSyncDataEntity, String geoZoneId) {
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
		streetLightDataParams.put("idOnController", slvSyncDataEntity.getIdOnController());
		streetLightDataParams.put("geoZoneId", geoZoneId);
		// streetLightDataParams.put("geoZoneId", "738");
		streetLightDataParams.put("nodeTypeStrId", nodeTypeStrId);
		streetLightDataParams.put("lat", slvSyncDataEntity.getLat());
		streetLightDataParams.put("lng", slvSyncDataEntity.getLng());
		return restService.getRequest(streetLightDataParams, url, true);

	}

	private void addStreetLightData(String key, String value, List<StreetLightData> streetLightDatas) {
		StreetLightData streetLightPowerData = new StreetLightData();
		streetLightPowerData.setKey(key);
		streetLightPowerData.setValue(value);
		streetLightDatas.add(streetLightPowerData);
	}

	private String getStreetLightMappingData() throws Exception {
		String mappingPath = properties.getProperty("streetlight.mapping.json.path");
		

		File file = new File("./resources/" + mappingPath);
		String streetLightMappingData = FileUtils.readFileToString(file);
		return streetLightMappingData;
	}

	private JsonObject getStreetLightMappingJson() throws Exception {
		String streetLightMappingData = getStreetLightMappingData();
		return (JsonObject) jsonParser.parse(streetLightMappingData);
	}

	private String dateFormat(String dateTime) {
		Date date = new Date(Long.valueOf(dateTime));
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
		String dff = dateFormat.format(date);
		return dff;
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
		ResponseEntity<String> responseEntity = restService.getRequest(url, true);
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
		return "738";
	}

}
