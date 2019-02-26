package com.terragoedge.slvinterface.service;

import com.google.gson.*;
import com.terragoedge.slvinterface.dao.ConnectionDAO;
import com.terragoedge.slvinterface.dao.SLVInterfaceDAO;
import com.terragoedge.slvinterface.dao.tables.SlvDevice;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetail;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetails;
import com.terragoedge.slvinterface.enumeration.Status;
import com.terragoedge.slvinterface.exception.*;
import com.terragoedge.slvinterface.json.slvInterface.ConfigurationJson;
import com.terragoedge.slvinterface.json.slvInterface.Id;
import com.terragoedge.slvinterface.model.*;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import com.terragoedge.slvinterface.utils.Utils;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.wololo.geojson.Feature;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;

import java.util.*;

public abstract class AbstractSlvService extends EdgeService {
    private Properties properties = null;
    private JsonParser jsonParser = null;
    SlvRestService slvRestService = null;
    SLVInterfaceDAO slvInterfaceDAO = null;
    Gson gson = null;
    private static Logger logger = Logger.getLogger(AbstractSlvService.class);
    ConnectionDAO connectionDAO;

    public AbstractSlvService() {
        gson = new Gson();
        jsonParser = new JsonParser();
        properties = PropertiesReader.getProperties();
        slvRestService = new SlvRestService();
        slvInterfaceDAO = new SLVInterfaceDAO();
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
                                               JPSWorkflowModel jpsWorkflowModel) {

        Feature feature = (Feature) GeoJSONFactory.create(edgenote.getGeometry());

        // parse Geometry from Feature
        GeoJSONReader reader = new GeoJSONReader();
        Geometry geom = reader.read(feature.getGeometry());


        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String serveletApiUrl = properties.getProperty("streetlight.slv.url.device.create");
        String url = mainUrl + serveletApiUrl;
        String methodName = properties.getProperty("streetlight.slv.device.create.methodName");
        String categoryStrId = jpsWorkflowModel.getCategoryStrId();
        String controllerStrId = jpsWorkflowModel.getControllerStrId();
        String nodeTypeStrId = properties.getProperty("streetlight.slv.equipment.type");
        Map<String, String> streetLightDataParams = new HashMap<String, String>();
        streetLightDataParams.put("methodName", methodName);
        streetLightDataParams.put("categoryStrId", categoryStrId);
        streetLightDataParams.put("controllerStrId", controllerStrId);
        streetLightDataParams.put("idOnController", edgenote.getTitle());
        streetLightDataParams.put("geoZoneId", jpsWorkflowModel.getGeozoneId());
        streetLightDataParams.put("lng", String.valueOf(geom.getCoordinate().x));
        streetLightDataParams.put("lat", String.valueOf(geom.getCoordinate().y));
        streetLightDataParams.put("nodeTypeStrId", nodeTypeStrId);
        // streetLightDataParams.put("modelFunctionId", nodeTypeStrId);
        // modelFunctionId
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


    public String validateMACAddress(String existingNodeMacAddress, String idOnController, String geoZoneId)
            throws QRCodeNotMatchedException {
        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String geoZoneDevices = properties.getProperty("streetlight.slv.url.getgeozone.devices");
        String url = mainUrl + geoZoneDevices;

        List<Object> paramsList = new ArrayList<Object>();
        if (geoZoneId != null) {
            //  paramsList.add("geoZoneId=" + geoZoneId);
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
            if (jsonObject != null) {
                JsonArray deviceValuesAsArray = jsonObject.get("values").getAsJsonArray();
                int totalSize = deviceValuesAsArray.size();
                for (int i = 0; i < totalSize; i++) {
                    JsonArray deviceValues = deviceValuesAsArray.get(i).getAsJsonArray();
                    if (deviceValues.get(0).getAsString().equals(idOnController)) {
                        if (deviceValues.get(2).getAsString().equals(existingNodeMacAddress)) {
                            if (deviceValues.get(1).isJsonNull()) {
                                return "";
                            } else {
                                String comment = deviceValues.get(1).getAsString();
                                return comment;
                            }
                        }
                    }
                }
                // Throws given MAC Address not matched
                throw new QRCodeNotMatchedException(idOnController, existingNodeMacAddress);
            }
        }
        return null;
    }

    public List<Value> checkMacAddressExists(String macAddress) {
        List<Value> values = new ArrayList<>();
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
        System.out.println("Url :"+url);
        ResponseEntity<String> response = slvRestService.getRequest(url, true, null);
        if (response.getStatusCodeValue() == 200) {
            String responseString = response.getBody();
            logger.info("-------MAC Address----------");
            logger.info(responseString);
            logger.info("-------MAC Address End----------");
            DeviceMacAddress deviceMacAddress = gson.fromJson(responseString, DeviceMacAddress.class);
            values = deviceMacAddress.getValue();
        }
        return values;
    }

    public ResponseEntity<String> setDeviceValues(List<Object> paramsList) {
        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String updateDeviceValues = properties.getProperty("streetlight.slv.url.updatedevice");
        String url = mainUrl + updateDeviceValues;

        paramsList.add("ser=json");
        String params = StringUtils.join(paramsList, "&");
        url = url + "&" + params;
        System.out.println("SetDevice Called");
        System.out.println("URL : " + url);
        ResponseEntity<String> response = slvRestService.getPostRequest(url, null);
        return response;

    }

    public abstract void buildFixtureStreetLightData(String data, List<Object> paramsList, EdgeNote edgeNote)
            throws InValidBarCodeException;


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
        addStreetLightData("location.utillocationid",  "Lamp", paramsList);
        addStreetLightData("location.locationtype", "LOCATION_TYPE_PREMISE", paramsList);
        String nodeTypeStrId = properties.getProperty("streetlight.slv.equipment.type");
        System.out.println(nodeTypeStrId);

        //  addStreetLightData("DimmingGroupName", "Dimming Evaluation", paramsList);
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

    public void replaceOLC(String controllerStrIdValue, String idOnController, String macAddress,EdgeNote edgeNote,JPSWorkflowModel jpsWorkflowModel)
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
            System.out.println("Replace OLc called: " + macAddress);
            System.out.println("Replace OLc Url" + url);
            ResponseEntity<String> response = slvRestService.getPostRequest(url, null);
            String responseString = response.getBody();
            JsonObject replaceOlcResponse = (JsonObject) jsonParser.parse(responseString);
            String errorStatus = replaceOlcResponse.get("status").getAsString();
            SlvSyncDetail dbSyncDetail = connectionDAO.getSlvSyncDetail(idOnController);
            // As per doc, errorcode is 0 for success. Otherwise, its not success.
            Status status = Status.Success;
            String errorValue = "";
            if (errorStatus.equals("ERROR")) {
                errorValue = replaceOlcResponse.get("value").getAsString();
                status = Status.Failure;
            }
            if(dbSyncDetail == null){
                dbSyncDetail = new SlvSyncDetail();
                dbSyncDetail.setCreatedDateTime(edgeNote.getCreatedDateTime());
                dbSyncDetail.setNoteGuid(edgeNote.getNoteGuid());
                dbSyncDetail.setPoleNumber(jpsWorkflowModel.getIdOnController());
                dbSyncDetail.setProcessedDateTime(System.currentTimeMillis());
                dbSyncDetail.setSlvReplaceOLCResponse(response.getBody());
                dbSyncDetail.setTitle(edgeNote.getTitle());
                dbSyncDetail.setDeviceDetails(gson.toJson(jpsWorkflowModel));
                dbSyncDetail.setStatus(status);
                connectionDAO.saveSlvSyncDetail(dbSyncDetail);
            }else{
                dbSyncDetail.setSlvReplaceOLCResponse(response.getBody());
                dbSyncDetail.setCreatedDateTime(System.currentTimeMillis());
                dbSyncDetail.setStatus(status);
                connectionDAO.updateSlvSyncDetail(dbSyncDetail);
            }
            if(status == Status.Failure){
                throw new ReplaceOLCFailedException(errorValue);
            }
        } catch (Exception e) {
            logger.error("Error in replaceOLC", e);
            throw new ReplaceOLCFailedException(e.getMessage());
        }

    }


    public void loadDevices() throws DeviceLoadException {
        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String devicesUrl = properties.getProperty("streetlight.slv.url.getgeozone.devices");
        String url = mainUrl + devicesUrl;
        List<Object> paramsList = new ArrayList<Object>();
        paramsList.add("valueNames=idOnController");
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
                SlvDevice slvDevice = new SlvDevice();
                slvDevice.setDeviceId(deviceValues.get(0).getAsString());
                slvDevice.setDeviceName(slvDevice.getDeviceId());
                if (!deviceValues.get(1).isJsonNull()) {
                    slvDevice.setMacAddress(deviceValues.get(1).getAsString());
                }

                SlvDevice dbSlvDevice = connectionDAO.getSlvDevices(slvDevice.getDeviceId());
                if (dbSlvDevice != null) {
                    dbSlvDevice.setMacAddress(slvDevice.getMacAddress());
                    connectionDAO.updateSlvDevice(slvDevice.getDeviceId(), slvDevice.getMacAddress());
                } else {
                    connectionDAO.saveSlvDevices(slvDevice);
                }
            }

        } else {
            throw new DeviceLoadException("Unable to load device from SLV Interface");
        }
    }
    public JsonArray checkDeviceExist(String idOnController) {
        JsonArray deviceArray = null;
        try {
            logger.info("loadDeviceValues called.");
            String mainUrl = properties.getProperty("streetlight.slv.url.main");
            String deviceUrl = properties.getProperty("streetlight.slv.url.search.device");
            String url = mainUrl + deviceUrl;
            List<String> paramsList = new ArrayList<>();
            paramsList.add("attributeName=idOnController");
            paramsList.add("attributeValue=" + idOnController);
            paramsList.add("recurse=true");
            paramsList.add("returnedInfo=lightDevicesList");
            paramsList.add("attributeOperator=eq-i");
            paramsList.add("maxResults=1");
            paramsList.add("ser=json");
            String params = StringUtils.join(paramsList, "&");
            url = url + "?" + params;
            logger.info("Load Device url :" + url);
            ResponseEntity<String> response = slvRestService.getRequest(url, true, null);
            if (response.getStatusCodeValue() == 200) {
                logger.info("LoadDevice Respose :"+response.getBody());
                String responseString = response.getBody();
                JsonObject jsonObject = new JsonParser().parse(responseString).getAsJsonObject();
                logger.info("Device request json:" + gson.toJson(jsonObject));
                return  jsonObject.getAsJsonArray("value");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public int processDeviceJson(String deviceJson) {
        JsonObject jsonObject = new JsonParser().parse(deviceJson).getAsJsonObject();
        logger.info("Device request json:" + gson.toJson(jsonObject));
        JsonArray arr = jsonObject.getAsJsonArray("value");

        return 0;
    }
    public ResponseEntity<String> getDeviceData(int id) {
        logger.info("getDeviceUrl url called");
        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String getDeviceUrl = properties.getProperty("streetlight.slv.url.getdevice.device");
        String deviceMainUrl = mainUrl + getDeviceUrl;
        List<String> paramsList = new ArrayList<>();
        paramsList.add("deviceId=" + id);
        paramsList.add("ser=json");
        paramsList.add("valueName=MacAddress");
        String params = StringUtils.join(paramsList, "&");
        deviceMainUrl = deviceMainUrl + "?" + params;
        ResponseEntity<String> responseEntity = slvRestService.getRequest(deviceMainUrl, true, null);
        return responseEntity;
    }
}
