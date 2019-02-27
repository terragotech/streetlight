package com.terragoedge.slvinterface.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.slvinterface.dao.ConnectionDAO;
import com.terragoedge.slvinterface.dao.tables.GeozoneEntity;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetail;
import com.terragoedge.slvinterface.enumeration.Status;
import com.terragoedge.slvinterface.exception.*;
import com.terragoedge.slvinterface.model.*;
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

public abstract class AbstractSlvService extends EdgeService {
    private Properties properties = null;
    private JsonParser jsonParser = null;
    SlvRestService slvRestService = null;
    Gson gson = null;
    private static Logger logger = Logger.getLogger(AbstractSlvService.class);
    ConnectionDAO connectionDAO;

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
                                               JPSWorkflowModel jpsWorkflowModel) {
        if(properties.getProperty("streetlights.create.enable").equals("true")) {
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
            streetLightDataParams.put("idOnController", jpsWorkflowModel.getIdOnController());
            streetLightDataParams.put("geoZoneId", String.valueOf(jpsWorkflowModel.getGeozoneId()));
            streetLightDataParams.put("lng", String.valueOf(geom.getCoordinate().x));
            streetLightDataParams.put("lat", String.valueOf(geom.getCoordinate().y));
            streetLightDataParams.put("nodeTypeStrId", nodeTypeStrId);
            streetLightDataParams.put("ser","json");
            // streetLightDataParams.put("modelFunctionId", nodeTypeStrId);
            // modelFunctionId
            return slvRestService.getRequest(streetLightDataParams, url, true);
        }else{
            return null;
        }
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
        System.out.println("Url :" + url);
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


    public void replaceOLC(String controllerStrIdValue, String idOnController, String macAddress, EdgeNote edgeNote, JPSWorkflowModel jpsWorkflowModel)
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
            if (dbSyncDetail == null) {
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
            } else {
                dbSyncDetail.setSlvReplaceOLCResponse(response.getBody());
                dbSyncDetail.setCreatedDateTime(System.currentTimeMillis());
                dbSyncDetail.setStatus(status);
                connectionDAO.updateSlvSyncDetail(dbSyncDetail);
            }
            if (status == Status.Failure) {
                throw new ReplaceOLCFailedException(errorValue);
            }
        } catch (Exception e) {
            logger.error("Error in replaceOLC", e);
            throw new ReplaceOLCFailedException(e.getMessage());
        }

    }

    public JsonArray checkDeviceExist(String idOnController) {
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
                logger.info("LoadDevice Respose :" + response.getBody());
                String responseString = response.getBody();
                JsonObject jsonObject = new JsonParser().parse(responseString).getAsJsonObject();
                logger.info("Device request json:" + gson.toJson(jsonObject));
                return jsonObject.getAsJsonArray("value");
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

    public GeozoneModel getGeozoneIdByName(String notebookName, int parentId) {
        List<GeozoneModel> geozoneModels = new ArrayList<>();
        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String getDeviceUrl = properties.getProperty("streetlight.slv.url.getgeozone.name");
        String geozoneUrl = mainUrl + getDeviceUrl;
        List<String> paramsList = new ArrayList<>();
        paramsList.add("name=" + notebookName);
        if (parentId != -1) {
            paramsList.add("parentId=" + parentId);
        }
        paramsList.add("partialMatch=true");
        paramsList.add("ser=json");
        String params = StringUtils.join(paramsList, "&");
        geozoneUrl = geozoneUrl + "?" + params;
        ResponseEntity<String> responseEntity = slvRestService.getRequest(geozoneUrl, true, null);
        if (responseEntity.getStatusCodeValue() == 200) {
            String geozoneResponse = responseEntity.getBody();
            geozoneModels = gson.fromJson(geozoneResponse, new TypeToken<List<GeozoneModel>>() {
            }.getType());
            if (geozoneModels.size() > 0) {
                return geozoneModels.get(0);
            }
        }
        return null;
    }

    public GeozoneModel createGeozone(String geoZoneName, int parentId) {
        GeozoneModel geozoneModel = null;
        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String createGeozoneUrl = properties.getProperty("streetlight.slv.url.creategeozone");
        String latitudeMin = properties.getProperty("streetlight.slv.latMin");
        String latitudeMax = properties.getProperty("streetlight.slv.latMax");
        String longitudeMin = properties.getProperty("streetlight.slv.lngMin");
        String longitudeMax = properties.getProperty("streetlight.slv.lngMax");
        String geozoneUrl = mainUrl + createGeozoneUrl;
        List<String> paramsList = new ArrayList<>();
        paramsList.add("name=" + geoZoneName);
        paramsList.add("parentId=" + parentId);
        paramsList.add("latMin="+latitudeMin);
        paramsList.add("latMax="+latitudeMax);
        paramsList.add("lngMin="+longitudeMin);
        paramsList.add("lngMax="+longitudeMax);
        paramsList.add("ser=json");
        String params = StringUtils.join(paramsList, "&");
        geozoneUrl = geozoneUrl + "?" + params;
        ResponseEntity<String> responseEntity = slvRestService.getRequest(geozoneUrl, true, null);
        if (responseEntity.getStatusCodeValue() == 200) {
            String geozoneResponse = responseEntity.getBody();
            System.out.println("Geozone Value: " + geozoneResponse);
            geozoneModel = gson.fromJson(geozoneResponse, GeozoneModel.class);
            return geozoneModel;
        }
        return null;
    }

    public GeozoneEntity getGeozoneEntity(String notebookName, String streetName) {
        GeozoneEntity geozoneEntity = connectionDAO.getGeozoneEntity(notebookName, streetName);
        if (geozoneEntity != null) {
            return geozoneEntity;
        }
        geozoneEntity = new GeozoneEntity();
        geozoneEntity.setGeozoneName(notebookName);
        geozoneEntity.setChildgeozoneName(streetName);
        //check parent notebook has exist or not in slv
        GeozoneModel childGeozone;
        // Get parent geozone
        GeozoneModel parentGeozoneModel = getGeozoneIdByName(notebookName, -1);
        // create parent and child geozone if not exist parent geozone
        if (parentGeozoneModel == null) {
            //create parent and child geozone
            parentGeozoneModel = createGeozone(notebookName, 467);
            childGeozone = createGeozone(streetName, parentGeozoneModel.getId());
        } else {
            //get child geozone
            childGeozone = getGeozoneIdByName(streetName, parentGeozoneModel.getId());
            //check if child geozone under another parentgeozone, create new child geozone based on parent geozone.
            if (childGeozone == null || !(childGeozone.getParentId() == parentGeozoneModel.getId())) {
                childGeozone = createGeozone(streetName, parentGeozoneModel.getId());
                //  childGeozone = getGeozoneIdByName(streetName, parentGeozoneModel.getId());
            }
        }
        geozoneEntity.setGeozoneId(parentGeozoneModel.getId());
        geozoneEntity.setChildgeozoneId(childGeozone.getId());
        connectionDAO.createGeozone(geozoneEntity);
        return geozoneEntity;
    }
}
