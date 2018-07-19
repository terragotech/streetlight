package com.terragoedge.slvinterface.service;

import com.google.gson.*;
import com.terragoedge.slvinterface.dao.ConnectionDAO;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetails;
import com.terragoedge.slvinterface.exception.QRCodeAlreadyUsedException;
import com.terragoedge.slvinterface.exception.QRCodeNotMatchedException;
import com.terragoedge.slvinterface.model.DeviceMacAddress;
import com.terragoedge.slvinterface.model.EdgeNote;
import com.terragoedge.slvinterface.model.Value;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import java.util.*;

public class AbstractSlvService {
    private Properties properties = null;
    private JsonParser jsonParser = null;
    private SlvRestService slvRestService = null;
    private Gson gson=null;
    private static Logger logger = Logger.getLogger(AbstractSlvService.class);
    private ConnectionDAO connectionDAO;

    public AbstractSlvService() {
        gson=new Gson();
        jsonParser = new JsonParser();
        properties = PropertiesReader.getProperties();
        slvRestService = new SlvRestService();
        connectionDAO = ConnectionDAO.INSTANCE;
    }

    public List<String> getSlvDeviceList() {
        List<String> slvDeviceList = new ArrayList<String>();
        String mainUrl = properties.getProperty("streetlight.url.main");
        String getMacAddress = properties.getProperty("streetlight.slv.url.getmacaddress");
        String url = mainUrl + getMacAddress;
        List<Object> paramsList = new ArrayList<Object>();
        paramsList.add("valueNames=idOnController");
        paramsList.add("valueNames=comment");
        paramsList.add("valueNames=MacAddress");
        paramsList.add("ser=json");
        String params = StringUtils.join(paramsList, "&");
        url = url + "&" + params;
        ResponseEntity<String> responseEntity = slvRestService.getPostRequest(url, null);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String deviceJson = responseEntity.getBody();
            JsonObject jsonObject = (JsonObject) jsonParser.parse(deviceJson);
            JsonArray deviceValuesAsArray = jsonObject.get("values").getAsJsonArray();
            int totalSize = deviceValuesAsArray.size();
            for (int i = 0; i < totalSize; i++) {
                JsonArray deviceValues = deviceValuesAsArray.get(i).getAsJsonArray();
                slvDeviceList.add(deviceValues.get(0).getAsString());
            }
            System.out.println("TotalSize " + slvDeviceList.size());
            return slvDeviceList;
        } else {
            logger.error("Unable to get message from EdgeServer. Response Code is :" + responseEntity.getStatusCode());
        }
        return new ArrayList<String>();
    }


    public ResponseEntity<String> createDevice(EdgeNote edgenote, KingCitySyncModel kingCitySyncModel,
                                               String geoZoneId) {
        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String serveletApiUrl = properties.getProperty("streetlight.slv.url.device.create");
        String url = mainUrl + serveletApiUrl;
        String methodName = properties.getProperty("streetlight.url.device.create.methodName");
        String categoryStrId = properties.getProperty("streetlight.categorystr.id");
        String nodeTypeStrId = properties.getProperty("streetlight.equipment.type");
        Map<String, String> streetLightDataParams = new HashMap<String, String>();
        streetLightDataParams.put("methodName", methodName);
        streetLightDataParams.put("categoryStrId", categoryStrId);
        streetLightDataParams.put("idOnController", edgenote.getTitle());
        streetLightDataParams.put("geoZoneId", geoZoneId);
        streetLightDataParams.put("nodeTypeStrId", nodeTypeStrId);
        return slvRestService.getRequest(streetLightDataParams, url, true);
    }

    private String validateMACAddress(String existingNodeMacAddress, String idOnController, String geoZoneId)
            throws QRCodeNotMatchedException {
        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String geoZoneDevices = properties.getProperty("streetlight.slv.url.getgeozone.devices");
        String url = mainUrl + geoZoneDevices;

        List<Object> paramsList = new ArrayList<Object>();
        if (geoZoneId != null) {
            paramsList.add("geoZoneId=" + geoZoneId);
        }

        paramsList.add("valueNames=idOnController");
        paramsList.add("valueNames=comment");
        paramsList.add("valueNames=MacAddress");
        paramsList.add("ser=json");
        String params = StringUtils.join(paramsList, "&");
        url = url + "&" + params;
        ResponseEntity<String> response = slvRestService.getPostRequest(url, null);
        if (response.getStatusCode().is2xxSuccessful()) {
            String geoZoneDeviceDetails = response.getBody();
            JsonObject jsonObject = (JsonObject) jsonParser.parse(geoZoneDeviceDetails);
            JsonArray deviceValuesAsArray = jsonObject.get("values").getAsJsonArray();
            int totalSize = deviceValuesAsArray.size();
            for (int i = 0; i < totalSize; i++) {
                JsonArray deviceValues = deviceValuesAsArray.get(i).getAsJsonArray();
                if (deviceValues.get(0).getAsString().equals(idOnController)) {
                    if (deviceValues.get(2).getAsString().equals(existingNodeMacAddress)) {
                        String comment = deviceValues.get(1).getAsString();
                        return comment;
                    }
                }
            }
            // Throws given MAC Address not matched
            throw new QRCodeNotMatchedException(idOnController, existingNodeMacAddress);
        }
        return null;
    }

    public boolean checkMacAddressExists(String macAddress, String idOnController)
            throws QRCodeAlreadyUsedException, Exception {
        logger.info("Getting Mac Address from SLV.");
        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String updateDeviceValues = properties.getProperty("streetlight.slv.url.search.device");
        String url = mainUrl + updateDeviceValues;
        List<String> paramsList = new ArrayList<String>();
        paramsList.add("attribute=MacAddress");
        paramsList.add("value=" + macAddress);
        paramsList.add("operator=eq-i");
        paramsList.add("recurse=true");
        paramsList.add("ser=json");
        String params = StringUtils.join(paramsList, "&");
        url = url + "?" + params;
        ResponseEntity<String> response = slvRestService.getRequest(url, true, null);
        if (response.getStatusCodeValue() == 200) {
            String responseString = response.getBody();
            logger.info("-------MAC Address----------");
            logger.info(responseString);
            logger.info("-------MAC Address End----------");
            DeviceMacAddress deviceMacAddress = gson.fromJson(responseString, DeviceMacAddress.class);
            List<Value> values = deviceMacAddress.getValue();
            StringBuilder stringBuilder = new StringBuilder();
            if (values == null || values.size() == 0) {
                return false;
            } else {
                for (Value value : values) {
                    if (value.getIdOnController().equals(idOnController)) {
                        return false;
                    }
                    stringBuilder.append(value.getIdOnController());
                    stringBuilder.append("\n");
                }
            }
            throw new QRCodeAlreadyUsedException(stringBuilder.toString(), macAddress);
        } else {
            throw new Exception();
        }

    }

    private int setDeviceValues(List<Object> paramsList) {
        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String updateDeviceValues = properties.getProperty("streetlight.slv.url.updatedevice");
        String url = mainUrl + updateDeviceValues;

        paramsList.add("ser=json");
        String params = StringUtils.join(paramsList, "&");
        url = url + "&" + params;
        ResponseEntity<String> response = slvRestService.getPostRequest(url, null);
        String responseString = response.getBody();
        JsonObject replaceOlcResponse = (JsonObject) jsonParser.parse(responseString);
        int errorCode = replaceOlcResponse.get("errorCode").getAsInt();
        return errorCode;
    }

    public void clearAndUpdateDeviceData(String idOnController, String controllerStrId) {
        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String updateDeviceValues = properties.getProperty("streetlight.slv.url.updatedevice");
        String url = mainUrl + updateDeviceValues;

        List<Object> paramsList = new ArrayList<Object>();
        paramsList.add("controllerStrId=" + controllerStrId);
        paramsList.add("idOnController=" + idOnController);
        addStreetLightData("device.node.serialnumber", "", paramsList);
        addStreetLightData("device.node.hwversion", "", paramsList);
        addStreetLightData("device.node.swversion", "", paramsList);
        addStreetLightData("device.nic.serialnumber", "", paramsList);
        addStreetLightData("device.nic.swversion", "", paramsList);
        addStreetLightData("device.nic.hwversion", "", paramsList);
        addStreetLightData("device.nic.currentnode", "", paramsList);
        addStreetLightData("device.nic.fallbackmode", "", paramsList);
        addStreetLightData("device.node.manufdate", "", paramsList);
        addStreetLightData("device.node.name", "", paramsList);
        addStreetLightData("device.node.manufacturer", "", paramsList);
        addStreetLightData("device.uiqid", "", paramsList);
        addStreetLightData("SoftwareVersion", "", paramsList);
        addStreetLightData("device.meter.programid", "", paramsList);
        addStreetLightData("device.nic.catalog", "", paramsList);

        paramsList.add("doLog=true");
        paramsList.add("ser=json");
        String params = StringUtils.join(paramsList, "&");
        url = url + "&" + params;
        ResponseEntity<String> response = slvRestService.getPostRequest(url, null);
        String responseString = response.getBody();
        JsonObject replaceOlcResponse = (JsonObject) jsonParser.parse(responseString);
        int errorCode = replaceOlcResponse.get("errorCode").getAsInt();
        if (errorCode == 0) {
            // success
        } else {
            // failure
        }
    }
    private void addStreetLightData(String key, String value, List<Object> paramsList) {
        paramsList.add("valueName=" + key.trim());
        paramsList.add("value=" + value.trim());
    }

    protected void getTalqAddress(){
        String slvBaseUrl = properties.getProperty("streetlight.slv.url.main");
        String talqAddressApi = properties.getProperty("streetlight.slv.url.gettalqaddress");
        ResponseEntity<String> responseEntity = slvRestService.getRequest(slvBaseUrl+talqAddressApi, true);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String response = responseEntity.getBody();
            JsonObject jsonObject = (JsonObject) jsonParser.parse(response);
            JsonArray deviceValuesAsArray = jsonObject.get("values").getAsJsonArray();
            for(JsonElement jsonElement : deviceValuesAsArray){
                JsonArray slvDetails = jsonElement.getAsJsonArray();
                if(slvDetails.size() == 2){
                    String idOnController = slvDetails.get(0).getAsString();
                    String talqAddress = slvDetails.get(1).getAsString();
                    SlvSyncDetails slvSyncDetails = connectionDAO.getSlvSyncDetailWithoutTalq(idOnController);
                    if(slvSyncDetails != null){
                        slvSyncDetails.setTalcAddress(talqAddress);
                        slvSyncDetails.setTalcAddressDateTime(new Date().getTime());
                        connectionDAO.saveSlvSyncDetails(slvSyncDetails);
                    }
                }

            }
        }

    }
}
