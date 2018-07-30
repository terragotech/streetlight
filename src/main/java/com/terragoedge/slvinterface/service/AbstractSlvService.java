package com.terragoedge.slvinterface.service;

import com.google.gson.*;
import com.terragoedge.slvinterface.dao.ConnectionDAO;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetails;
import com.terragoedge.slvinterface.enumeration.Status;
import com.terragoedge.slvinterface.exception.*;
import com.terragoedge.slvinterface.json.slvInterface.Id;
import com.terragoedge.slvinterface.model.DeviceMacAddress;
import com.terragoedge.slvinterface.model.EdgeNote;
import com.terragoedge.slvinterface.model.Value;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import com.terragoedge.slvinterface.utils.Utils;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.wololo.geojson.Feature;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;

import java.util.*;

public class AbstractSlvService {
    private Properties properties = null;
    private JsonParser jsonParser = null;
    private SlvRestService slvRestService = null;
    private Gson gson = null;
    private static Logger logger = Logger.getLogger(AbstractSlvService.class);
    private ConnectionDAO connectionDAO;

    public AbstractSlvService() {
        gson = new Gson();
        jsonParser = new JsonParser();
        properties = PropertiesReader.getProperties();
        slvRestService = new SlvRestService();
        connectionDAO = ConnectionDAO.INSTANCE;
    }

    protected String getEdgeToken() {
        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        String userName = properties.getProperty("streetlight.edge.username");
        String password = properties.getProperty("streetlight.edge.password");
        url = url + "/oauth/token?grant_type=password&username=" + userName + "&password=" + password
                + "&client_id=edgerestapp";
        ResponseEntity<String> responseEntity = slvRestService.getRequest(url);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            JsonObject jsonObject = (JsonObject) jsonParser.parse(responseEntity.getBody());
            return jsonObject.get("access_token").getAsString();
        }
        return null;
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


    public ResponseEntity<String> createDevice(EdgeNote edgenote,
                                               String geoZoneId) {

        Feature feature = (Feature) GeoJSONFactory.create(edgenote.getGeometry());

        // parse Geometry from Feature
        GeoJSONReader reader = new GeoJSONReader();
        Geometry geom = reader.read(feature.getGeometry());


        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String serveletApiUrl = properties.getProperty("streetlight.slv.url.device.create");
        String url = mainUrl + serveletApiUrl;
        String methodName = properties.getProperty("streetlight.slv.device.create.methodName");
        String categoryStrId = properties.getProperty("streetlight.categorystr.id");
        String controllerStrId = properties.getProperty("streetlight.controller.str.id");
        String nodeTypeStrId = properties.getProperty("streetlight.slv.equipment.type");
        Map<String, String> streetLightDataParams = new HashMap<String, String>();
        streetLightDataParams.put("methodName", methodName);
        streetLightDataParams.put("categoryStrId", categoryStrId);
        streetLightDataParams.put("controllerStrId", controllerStrId);
        streetLightDataParams.put("idOnController", edgenote.getTitle());
        streetLightDataParams.put("geoZoneId", geoZoneId);
        streetLightDataParams.put("lng", String.valueOf(geom.getCoordinate().x));
        streetLightDataParams.put("lat", String.valueOf(geom.getCoordinate().y));
        streetLightDataParams.put("nodeTypeStrId", nodeTypeStrId);
        return slvRestService.getRequest(streetLightDataParams, url, true);
    }

    public Id getIDByType(List<Id> idList, String type) {
        Id id = new Id();
        id.setType(type);
        int pos = idList.indexOf(id);
        if (pos != -1) {
            return idList.get(pos);
        }
        return null;
    }
    public void buildFixtureStreetLightData(String data, List<Object> paramsList, EdgeNote edgeNote)
            throws InValidBarCodeException {
        System.out.println("buildFixtureStreetLightData = "+data);
        String[] fixtureInfo = data.split(",");
        logger.info("Fixture QR Scan Val lenght"+fixtureInfo.length);
        if (fixtureInfo.length >= 12) {
            addStreetLightData("serial_no", fixtureInfo[0], paramsList);
            /**
             * As per Mail conversion, In the older data, the luminaire model was the
             * shorter version of the fixture, so for the General Electric fixtures it was
             * ERLH. The Luminaire Part Number would be the longer more detailed number.
             */
            String partNumber = fixtureInfo[1].trim();
            String model = fixtureInfo[2].trim();
            /*if (fixtureInfo[1].trim().length() <= fixtureInfo[2].trim().length()) {
                model = fixtureInfo[1].trim();
                partNumber = fixtureInfo[2].trim();
            }*/
            addStreetLightData("categoryStrId", partNumber, paramsList);
            addStreetLightData("device_luminaire_drivermanufacturer", model, paramsList);
            addStreetLightData("UMSUGChargeCode", fixtureInfo[3], paramsList);
            String powerVal = fixtureInfo[4];
            if (powerVal != null && !powerVal.isEmpty()) {
                powerVal = powerVal.replaceAll("W", "");
                powerVal = powerVal.replaceAll("w", "");
            }

            addStreetLightData("lampType", powerVal, paramsList);
            addStreetLightData("luminaire_brand", fixtureInfo[5], paramsList);
            // dailyReportCSV.setFixtureType(fixtureInfo[5]);
            addStreetLightData("device_luminaire_colortemp", fixtureInfo[6], paramsList);
            addStreetLightData("luminaire_model", fixtureInfo[7], paramsList);
            addStreetLightData("device_luminaire_manufacturedate", fixtureInfo[8], paramsList);
            addStreetLightData("device_luminaire_partnumber", fixtureInfo[9], paramsList);
            addStreetLightData("luminaire_type", fixtureInfo[10], paramsList);
            addStreetLightData("power_watts", fixtureInfo[11], paramsList);
//            addStreetLightData("ballast.dimmingtype", fixtureInfo[12], paramsList);
            System.out.println("steetlight processed data"+ paramsList);

        } else {
            throw new InValidBarCodeException(
                    "Fixture MAC address is not valid (" + edgeNote.getTitle() + "). Value is:" + data);
        }
    }

    public String validateMACAddress(String existingNodeMacAddress, String idOnController, String geoZoneId)
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
            throws QRCodeAlreadyUsedException {
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
            throw new QRCodeAlreadyUsedException("QR code ["+macAddress+"] is already Used in following devices ["+stringBuilder.toString()+"]",macAddress);
        } else {
            throw new QRCodeAlreadyUsedException("Error while getting data from SLV.",macAddress);
        }

    }

    public void setDeviceValues(List<Object> paramsList,SlvSyncDetails slvSyncDetails)throws DeviceUpdationFailedException {
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
        if(errorCode != 0){
            slvSyncDetails.setErrorDetails(gson.toJson(replaceOlcResponse));
            slvSyncDetails.setStatus(Status.Failure.toString());
            throw new DeviceUpdationFailedException(gson.toJson(replaceOlcResponse));
        }
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

    public void addStreetLightData(String key, String value, List<Object> paramsList) {
        paramsList.add("valueName=" + key.trim());
        paramsList.add("value=" + value.trim());
    }

    public void addOtherParams(EdgeNote edgeNote, List<Object> paramsList) {
        // luminaire.installdate - 2017-09-07 09:47:35
        addStreetLightData("install.date", Utils.dateFormat(edgeNote.getCreatedDateTime()), paramsList);
        // controller.installdate - 2017/10/10

        addStreetLightData("installStatus", "Installed", paramsList);
        addStreetLightData("location.utillocationid",edgeNote.getTitle()+".Lamp", paramsList);
        addStreetLightData("location.locationtype", "LOCATION_TYPE_PREMISE", paramsList);

        addStreetLightData("DimmingGroupName", "Group Calendar 1", paramsList);
    }

    public void getTalqAddress() {
        String slvBaseUrl = properties.getProperty("streetlight.slv.url.main");
        String talqAddressApi = properties.getProperty("streetlight.slv.url.gettalqaddress");
        ResponseEntity<String> responseEntity = slvRestService.getRequest(slvBaseUrl + talqAddressApi, true);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String response = responseEntity.getBody();

            JsonObject jsonObject = (JsonObject) jsonParser.parse(response);
            JsonArray deviceValuesAsArray = jsonObject.get("values").getAsJsonArray();
            for (JsonElement jsonElement : deviceValuesAsArray) {
                JsonArray slvDetails = jsonElement.getAsJsonArray();
                if (slvDetails.size() == 2) {
                    String idOnController = slvDetails.get(0).getAsString();
                    String talqAddress = slvDetails.get(1).getAsString();
                    SlvSyncDetails slvSyncDetails = connectionDAO.getSlvSyncDetailWithoutTalq(idOnController);
                    if (slvSyncDetails != null) {
                        slvSyncDetails.setTalcAddress(talqAddress);
                        slvSyncDetails.setTalcAddressDateTime(new Date().getTime());
                        connectionDAO.saveSlvSyncDetails(slvSyncDetails);
                    }
                }

            }
        }

    }

    public void replaceOLC(String controllerStrIdValue, String idOnController, String macAddress)
            throws ReplaceOLCFailedException {
        try {
            // Get Url detail from properties
            String mainUrl = properties.getProperty("streetlight.slv.url.main");
            String dataUrl = properties.getProperty("streetlight.url.replaceolc");
            String replaceOlc = properties.getProperty("streetlight.url.replaceolc.method");
            String url = mainUrl + dataUrl;
            String controllerStrId = controllerStrIdValue;
            List<Object> paramsList = new ArrayList<Object>();
            paramsList.add("methodName=" + replaceOlc);
            paramsList.add("controllerStrId=" + controllerStrId);
            paramsList.add("idOnController=" + idOnController);
            paramsList.add("newNetworkId=" + macAddress);
            paramsList.add("ser=json");
            String params = StringUtils.join(paramsList, "&");
            url = url + "?" + params;
            ResponseEntity<String> response = slvRestService.getPostRequest(url, null);
            String responseString = response.getBody();
            JsonObject replaceOlcResponse = (JsonObject) jsonParser.parse(responseString);
            String errorStatus = replaceOlcResponse.get("status").getAsString();
            // As per doc, errorcode is 0 for success. Otherwise, its not success.
            if (errorStatus.equals("ERROR")) {
                String value = replaceOlcResponse.get("value").getAsString();
                throw new ReplaceOLCFailedException(value);
            } else {
                if (macAddress != null)
                    clearAndUpdateDeviceData(idOnController, controllerStrId);
            }

        } catch (Exception e) {
            logger.error("Error in replaceOLC", e);
            throw new ReplaceOLCFailedException(e.getMessage());
        }

    }
}
