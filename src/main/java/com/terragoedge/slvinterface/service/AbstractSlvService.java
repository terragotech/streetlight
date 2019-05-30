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


    public ResponseEntity<String> createDevice(EdgeNote edgenote,
                                               JPSWorkflowModel jpsWorkflowModel) {
        if(properties.getProperty("streetlights.create.enable").equals("true")) {
            logger.info("Create device in slv");
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
    public void deleteDevice(JPSWorkflowModel jpsWorkflowModel) {
        logger.info("Getting Delete Device from SLV.");
        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String updateDeviceValues = properties.getProperty("streetlight.slv.delete.device");
        String url = mainUrl + updateDeviceValues;
        List<String> paramsList = new ArrayList<String>();
        paramsList.add("controllerStrId="+jpsWorkflowModel.getControllerStrId());
        paramsList.add("idOnController=" + jpsWorkflowModel.getOldPoleNumber());
        paramsList.add("ser=json");
        String params = StringUtils.join(paramsList, "&");
        url = url + "?" + params;
        logger.info(" Deleted Url  :" + url);
        ResponseEntity<String> response = slvRestService.getRequest(url, true, null);
        if (response.getStatusCodeValue() == 200) {

            logger.info("-------Successfully deleted device----------");
            logger.info(response.getStatusCode());
        }
    }

    public ResponseEntity<String> setDeviceValues(List<Object> paramsList) {
        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String updateDeviceValues = properties.getProperty("streetlight.slv.url.updatedevice");
        String url = mainUrl + updateDeviceValues;

        paramsList.add("ser=json");
        String params = StringUtils.join(paramsList, "&");
        url = url + "&" + params;
        System.out.println("SetDevice Called");
        logger.info("settDevice value called");
        System.out.println("URL : " + url);
        ResponseEntity<String> response = slvRestService.getPostRequest(url, null);
        return response;
    }

    public abstract void buildFixtureStreetLightData(String data, List<Object> paramsList, EdgeNote edgeNote)
            throws InValidBarCodeException;


    public void replaceOLC(String controllerStrIdValue, String idOnController, String macAddress, EdgeNote edgeNote, JPSWorkflowModel jpsWorkflowModel)
            throws ReplaceOLCFailedException {
        try {
            logger.info("Replace Olc called, Given Mac:"+macAddress);
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
            logger.info("Replace OLc called: " + macAddress);
            logger.info("Replace OLc Url" + url);
            ResponseEntity<String> response = slvRestService.getPostRequest(url, null);
            logger.info("********************** replace OLC reponse code: "+response.getStatusCode());
            logger.info("replace OLC response: "+response.getBody());
            logger.info("********************** replace OLC reponse end *********");
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
            logger.info("Check given idOncontroller is exist or not");
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
            logger.info("Check idOnController's Url :"+url);
            ResponseEntity<String> response = slvRestService.getRequest(url, true, null);
            if (response.getStatusCodeValue() == 200) {
                logger.info("check idoncontroller url Respose :" + response.getBody());
                String responseString = response.getBody();
                JsonObject jsonObject = new JsonParser().parse(responseString).getAsJsonObject();
                logger.info("check request json:" + gson.toJson(jsonObject));
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
        paramsList.add("param0=" + id);
        paramsList.add("returnTimeAges="+false);
        paramsList.add("ser=json");
        paramsList.add("valueName=MacAddress");
        String params = StringUtils.join(paramsList, "&");
        deviceMainUrl = deviceMainUrl + "?" + params;
        logger.info("GetDevice Url :"+deviceMainUrl);
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
        paramsList.add("partialMatch=false");
        paramsList.add("ser=json");
        String params = StringUtils.join(paramsList, "&");
        geozoneUrl = geozoneUrl + "?" + params;
        logger.info("-------Create Geozone start-----");
        logger.info(geozoneUrl);
        logger.info(parentId);
        logger.info("-------Create Geozone end-----");
        ResponseEntity<String> responseEntity = slvRestService.getRequest(geozoneUrl, true, null);
        if (responseEntity.getStatusCodeValue() == 200) {
            String geozoneResponse = responseEntity.getBody();
            logger.info("Create Geozone Response : "+geozoneResponse);
            geozoneModels = gson.fromJson(geozoneResponse, new TypeToken<List<GeozoneModel>>() {
            }.getType());
            for(GeozoneModel geozoneModel : geozoneModels){
                if(parentId == geozoneModel.getParentId()){
                    return geozoneModel;
                }
            }
            /*if (geozoneModels.size() > 0) {
                return geozoneModels.get(0);
            }*/
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

    public GeozoneEntity getGeozoneEntity(JPSWorkflowModel jpsWorkflowModel) {
        String division = (jpsWorkflowModel.getDivision() == null || jpsWorkflowModel.getDivision().equals("") || jpsWorkflowModel.getDivision().equals("UNKNOWN")) ? jpsWorkflowModel.getNotebookName() : jpsWorkflowModel.getDivision();
        String streetName = jpsWorkflowModel.getAddress1();
        logger.info("Parent Geozone name as notebookName:"+division);
        logger.info("Sub or childgezone Name as StreetName :"+streetName);
        String rootGeozoneId = properties.getProperty("streetlight.slv.rootgeozone");
        GeozoneEntity geozoneEntity = connectionDAO.getGeozoneEntity(jpsWorkflowModel);
        if (geozoneEntity != null) {
            logger.info("Already has geozone values in geozone table");
            return geozoneEntity;
        }
        geozoneEntity = new GeozoneEntity();
        geozoneEntity.setParishZoneName(jpsWorkflowModel.getCity());
        geozoneEntity.setDivisionZoneName(jpsWorkflowModel.getNotebookName());
        geozoneEntity.setStreetZoneName(jpsWorkflowModel.getAddress1());
        //check parent notebook has exist or not in slv
        GeozoneModel divisionChildGeozone = null;
//        GeozoneModel streetChildGeozone = null;
        // Get parent geozone
        GeozoneModel parishGeozoneModel = getGeozoneIdByName(jpsWorkflowModel.getCity(), Integer.valueOf(rootGeozoneId));
        // create parent and child geozone if not exist parent geozone
        if (parishGeozoneModel == null) {
            logger.info("There is no parent Geozones, need to create parent and child");
            //create parent and child geozone
            parishGeozoneModel = createGeozone(jpsWorkflowModel.getCity(), Integer.parseInt(rootGeozoneId));
            logger.info("ParentGeozone value : "+((parishGeozoneModel!=null)? gson.toJson(parishGeozoneModel):null));
            if(parishGeozoneModel != null) {
                divisionChildGeozone = createGeozone(division, parishGeozoneModel.getId());
                logger.info("childGeozone value : " + ((divisionChildGeozone != null) ? gson.toJson(divisionChildGeozone) : null));
                /*if(divisionChildGeozone != null){
                    streetChildGeozone = createGeozone(jpsWorkflowModel.getAddress1(), divisionChildGeozone.getId());
                    logger.info("first childGeozone value : " + ((streetChildGeozone != null) ? gson.toJson(streetChildGeozone) : null));
                }*/

            }
        } else {
            //get child geozone
            logger.info("Exist parent geozone, Going to veryfi child geozone is exist or not");
            divisionChildGeozone = getGeozoneIdByName(division, parishGeozoneModel.getId());
            logger.info("division childGeozone value : "+((divisionChildGeozone!=null)? gson.toJson(divisionChildGeozone):null));
            //check if child geozone under another parentgeozone, create new child geozone based on parent geozone.
            if (divisionChildGeozone == null || divisionChildGeozone.getParentId() != parishGeozoneModel.getId()) {
                logger.info("check if child geozone under another parentgeozone, create new child geozone based on parent geozone");
                divisionChildGeozone = createGeozone(division, parishGeozoneModel.getId());
                logger.info("division childGeozone value : " + ((divisionChildGeozone != null) ? gson.toJson(divisionChildGeozone) : null));
                /*if(divisionChildGeozone != null){
                    streetChildGeozone = createGeozone(jpsWorkflowModel.getAddress1(),divisionChildGeozone.getId());
                    logger.info("street childGeozone value : " + ((streetChildGeozone != null) ? gson.toJson(streetChildGeozone) : null));
                }*/
            }else{
                /*streetChildGeozone = getGeozoneIdByName(jpsWorkflowModel.getAddress1(), divisionChildGeozone.getId());
                if(streetChildGeozone == null){
                    streetChildGeozone = createGeozone(jpsWorkflowModel.getAddress1(), divisionChildGeozone.getId());
                    logger.info("street childGeozone value : " + ((streetChildGeozone != null) ? gson.toJson(streetChildGeozone) : null));
                }*/
            }
        }
        geozoneEntity.setParishzoneId(parishGeozoneModel.getId());
        geozoneEntity.setDivisionZoneId(divisionChildGeozone.getId());
//        geozoneEntity.setStreetGeozoneId(streetChildGeozone.getId());
        connectionDAO.createGeozone(geozoneEntity);
        return geozoneEntity;
    }

    protected String valueById(List<EdgeFormData> edgeFormDatas, int id){
        for (EdgeFormData edgeFormData : edgeFormDatas) {
            if (edgeFormData.getId() == id) {
                String value = edgeFormData.getValue();
                if (value == null || value.trim().isEmpty() || value.contains("null")) {
                    return "";
                }
                return value;
            }
        }
        return "";
    }
}
