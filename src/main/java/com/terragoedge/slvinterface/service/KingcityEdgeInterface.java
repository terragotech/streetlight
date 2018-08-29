package com.terragoedge.slvinterface.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terragoedge.slvinterface.exception.DeviceUpdationFailedException;
import com.terragoedge.slvinterface.model.EdgeFormData;
import com.terragoedge.slvinterface.model.NoteDetails;
import com.terragoedge.slvinterface.model.SLVDataEntity;
import com.terragoedge.slvinterface.utils.PropertiesReader;

public class KingcityEdgeInterface {

	private Properties properties = null;
	private Gson gson = null;
	private JsonParser jsonParser = null;
	SlvRestService restService = null;
	JsonObject mappingJson = null;
	static Map<String, Integer> luminareCore = new HashMap<>();
	Map<String, String> dimmingValue = new HashMap<>();
	Map<String, String> WattAndIdonController = new HashMap<>();

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
	public KingcityEdgeInterface() {
		properties = PropertiesReader.getProperties();
		gson = new Gson();
		jsonParser = new JsonParser();
		restService = new SlvRestService();
		loadDimmingValue();
	}

	public void updateDeviceValues(NoteDetails noteDetails, SLVDataEntity slvDataEntity,
			List<EdgeFormData> edgeFormValuesList) throws DeviceUpdationFailedException {
		int lWatt = 0;
		int power2Watt = 0;
		String comment = "";
		for (EdgeFormData formValues : edgeFormValuesList) {
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
		addStreetLightData("location.locationtype", "LOCATION_TYPE_PREMISE", slvDataEntity.getParamsList());

		String streetLightDate = dateFormat(noteDetails.getCreatedDateTime());
		addStreetLightData("lamp.installdate", streetLightDate, slvDataEntity.getParamsList());

		String dimmingGroupName = dimmingValue.get(slvDataEntity.getIdOnController());
		addStreetLightData("DimmingGroupName", dimmingGroupName, slvDataEntity.getParamsList());

		updateSLVData(slvDataEntity, noteDetails, slvDataEntity.getParamsList());
	}

	private void addStreetLightData(String key,String value,List<Object> paramsList){
		paramsList.add("valueName=" + key.trim());
		if(value != null){
			paramsList.add("value=" + value.trim());
		}else{
			paramsList.add("value=");
		}

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
	
	private void loadWattAndIdOnControllerValue() {
		BufferedReader csvFile = null;
		try {
			csvFile = new BufferedReader(new FileReader(properties.getProperty("watt.idoncontroller.csv.location")));
			String currentLine;
			while ((currentLine = csvFile.readLine()) != null) {
				String[] stringArray = currentLine.split(",");
				WattAndIdonController.put(stringArray[0], stringArray[1]);
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
