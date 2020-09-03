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
import org.springframework.util.LinkedMultiValueMap;
import org.wololo.geojson.Feature;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractSlvService extends EdgeService {
    private Properties properties = null;
    private JsonParser jsonParser = null;
    SlvRestService slvRestService = null;
    Gson gson = null;
    private static Logger logger = Logger.getLogger(AbstractSlvService.class);
    public ConnectionDAO connectionDAO;

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
//            String serveletApiUrl = properties.getProperty("streetlight.slv.url.device.create");
            String createDeviceUrl = properties.getProperty("com.slv.create.device.url");
            String url = mainUrl + createDeviceUrl;
            String categoryStrId = jpsWorkflowModel.getCategoryStrId();
            String controllerStrId = jpsWorkflowModel.getControllerStrId();
            String nodeTypeStrId = properties.getProperty("streetlight.slv.equipment.type");
            LinkedMultiValueMap<String, String> streetLightDataParams = new LinkedMultiValueMap<>();
            streetLightDataParams.add("categoryStrId", categoryStrId);
            streetLightDataParams.add("controllerStrId", controllerStrId);
            streetLightDataParams.add("idOnController", jpsWorkflowModel.getIdOnController().trim());
            streetLightDataParams.add("geoZoneId", String.valueOf(jpsWorkflowModel.getGeozoneId()));
            streetLightDataParams.add("lng", String.valueOf(geom.getCoordinate().x));
            streetLightDataParams.add("lat", String.valueOf(geom.getCoordinate().y));
            streetLightDataParams.add("nodeTypeStrId", nodeTypeStrId);
            streetLightDataParams.add("ser","json");
            // streetLightDataParams.put("modelFunctionId", nodeTypeStrId);
            // modelFunctionId
            return slvRestService.getPostRequest(url,null,streetLightDataParams);
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

    public ResponseEntity<String> setDeviceValues(LinkedMultiValueMap<String,String> paramsList) {
        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String updateDeviceValues = properties.getProperty("streetlight.slv.url.updatedevice");
        String url = mainUrl + updateDeviceValues;

        paramsList.add("ser","json");
        logger.info("settDevice value called");
        System.out.println("URL : " + url);
        ResponseEntity<String> response = slvRestService.getPostRequest(url, null,paramsList);
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
//            String replaceOlc = properties.getProperty("streetlight.url.replaceolc.method");
            String url = mainUrl + dataUrl;
            String controllerStrId = controllerStrIdValue;
            LinkedMultiValueMap<String,String> paramsList = new LinkedMultiValueMap<>();
            paramsList.add("controllerStrId=", controllerStrId);
            paramsList.add("idOnController=", idOnController.trim());
            paramsList.add("newNetworkId=", macAddress);
            paramsList.add("ser","json");
            logger.info("Replace OLc called: " + macAddress);
            logger.info("Replace OLc Url" + url);
            ResponseEntity<String> response = slvRestService.getPostRequest(url, null,paramsList);
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
                dbSyncDetail.setCreatedDateTime(edgeNote.getSyncTime());
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
                dbSyncDetail.setProcessedDateTime(System.currentTimeMillis());
                dbSyncDetail.setCreatedDateTime(edgeNote.getSyncTime());
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
            paramsList.add("attributeValue=" + encode(idOnController.trim()));
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

    public String encode(String data){
        try{
            return  URLEncoder.encode(data, "UTF-8");
        }catch (UnsupportedEncodingException e){
            logger.error("Error while encoding data: "+data,e);
        }
       return data;
    }

    public GeozoneModel getGeozoneIdByName(String notebookName, int parentId) {
        List<GeozoneModel> geozoneModels = new ArrayList<>();
        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String getDeviceUrl = properties.getProperty("streetlight.slv.url.getgeozone.name");
        String geozoneUrl = mainUrl + getDeviceUrl;
        List<String> paramsList = new ArrayList<>();

        paramsList.add("name=" + encode(notebookName));
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

    public GeozoneModel createGeozone(String geoZoneName, int parentId){
        GeozoneModel geozoneModel = null;
        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String createGeozoneUrl = properties.getProperty("streetlight.slv.url.creategeozone");
        String latitudeMin = properties.getProperty("streetlight.slv.latMin");
        String latitudeMax = properties.getProperty("streetlight.slv.latMax");
        String longitudeMin = properties.getProperty("streetlight.slv.lngMin");
        String longitudeMax = properties.getProperty("streetlight.slv.lngMax");
        String geozoneUrl = mainUrl + createGeozoneUrl;
        List<String> paramsList = new ArrayList<>();
        paramsList.add("name=" +  encode(geoZoneName));
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

    protected void addStreetLightData(String key, String value, LinkedMultiValueMap<String,String> paramsList, boolean isEncode) {
        paramsList.add("valueName",key.trim());
        if(value != null && !value.trim().isEmpty()){
            if(isEncode) {
                value = encode(value);
            }
            paramsList.add("value",value);
        }else{
            paramsList.add("value","");
        }

    }

    public JPSWorkflowModel processWorkFlowForm(List<FormData> formDataList, EdgeNote edgeNote, WorkFlowFormId installWorkflowFormId) {
        JPSWorkflowModel jpsWorkflowModel = new JPSWorkflowModel();
        if (edgeNote.getEdgeNotebook() != null) {
            jpsWorkflowModel.setNotebookName(edgeNote.getEdgeNotebook().getNotebookName());
            jpsWorkflowModel.setDimmingGroupName(edgeNote.getEdgeNotebook().getNotebookName());
        }
        String categoryStrId = properties.getProperty("streetlight.categorystr.id");
        String controllerStrId = properties.getProperty("streetlight.controller.str.id");

        String nodeTypeStrId = properties.getProperty("streetlight.slv.equipment.type");
        Feature feature = (Feature) GeoJSONFactory.create(edgeNote.getGeometry());
        // parse Geometry from Feature
        GeoJSONReader reader = new GeoJSONReader();
        Geometry geom = reader.read(feature.getGeometry());
        if (edgeNote.getGeometry() != null) {
            jpsWorkflowModel.setLat(String.valueOf(geom.getCoordinate().y));
            jpsWorkflowModel.setLng(String.valueOf(geom.getCoordinate().x));
        } else {
            logger.info("There is no location given note :" + edgeNote.getTitle());
        }
        jpsWorkflowModel.setControllerStrId(controllerStrId);
        jpsWorkflowModel.setEquipmentType(nodeTypeStrId);
        jpsWorkflowModel.setProvider_name(properties.getProperty("jps.provider.name"));
        jpsWorkflowModel.setLowvoltagethreshold(Integer.valueOf(properties.getProperty("jps.low.voltage.thershold")));
        jpsWorkflowModel.setHighvoltagethreshold(Integer.valueOf(properties.getProperty("jps.high.voltage.thershold")));
        jpsWorkflowModel.setCategoryStrId(categoryStrId);
        jpsWorkflowModel.setLocationtype(properties.getProperty("jps.location.type"));
        jpsWorkflowModel.setModel(properties.getProperty("jps.model"));
        for (FormData formData : formDataList) {
            List<EdgeFormData> edgeFormDataList = formData.getFormDef();

            String feederName = getFormValue(edgeFormDataList,installWorkflowFormId.getFeederName());
            if (nullCheck(feederName))
                jpsWorkflowModel.setStreetdescription(feederName);

            String streetName = getFormValue(edgeFormDataList,installWorkflowFormId.getStreetName());
            if (nullCheck(streetName))
                jpsWorkflowModel.setAddress1(streetName);

            String parrish = getFormValue(edgeFormDataList,installWorkflowFormId.getParrish());
            if (nullCheck(parrish))
                jpsWorkflowModel.setCity(parrish);

            String division = getFormValue(edgeFormDataList,installWorkflowFormId.getDivision());
            if (nullCheck(division))
                jpsWorkflowModel.setDivision(division);

            String newPoleNumber = getFormValue(edgeFormDataList,installWorkflowFormId.getNewPoleNumber());
            if (nullCheck(newPoleNumber)) {
                jpsWorkflowModel.setIdOnController(newPoleNumber);
                jpsWorkflowModel.setName(newPoleNumber);
                jpsWorkflowModel.setUtillocationid(newPoleNumber);
            }

            String retrofitStatus = getFormValue(edgeFormDataList,installWorkflowFormId.getRetrofitStatus());
            if (nullCheck(retrofitStatus))
                jpsWorkflowModel.setInstallStatus(retrofitStatus);

            String newLampWattage = getFormValue(edgeFormDataList,installWorkflowFormId.getNewLampWatage());
            if (nullCheck(newLampWattage)) {
                String lampType = newLampWattage;
                if (lampType.equals("Other")) {
                    processLampType(edgeFormDataList, jpsWorkflowModel);
                } else {
                    Pattern pattern = Pattern.compile("\\d+");
                    Matcher matcher = pattern.matcher(newLampWattage);
                    if (matcher != null && matcher.find()) {
                        jpsWorkflowModel.setPower(matcher.group(0).trim());
                    }
                    jpsWorkflowModel.setLampType(newLampWattage);
                }
            }

            String oldPoleNumber = getFormValue(edgeFormDataList,installWorkflowFormId.getOldPoleNumber());
            if (nullCheck(oldPoleNumber))
                jpsWorkflowModel.setOldPoleNumber(oldPoleNumber);

            String poleType = getFormValue(edgeFormDataList,installWorkflowFormId.getPoleType());
            if (nullCheck(poleType))
                jpsWorkflowModel.setPole_type(poleType);

            String macaddress = getFormValue(edgeFormDataList,installWorkflowFormId.getMacAddress());
            if (nullCheck(macaddress))
                if (macaddress != null && macaddress.startsWith("00135"))
                    jpsWorkflowModel.setMacAddress(macaddress);

            String mastArmLength = getFormValue(edgeFormDataList,installWorkflowFormId.getMastArmLength());
            if (nullCheck(mastArmLength))
                jpsWorkflowModel.setFixing_type(mastArmLength);

            String mastArmLengthOther = getFormValue(edgeFormDataList,installWorkflowFormId.getMastArmLengthOther());
            if (nullCheck(mastArmLengthOther))
                jpsWorkflowModel.setOtherFixtureType(mastArmLengthOther);

            String conversionDate = getFormValue(edgeFormDataList,installWorkflowFormId.getConversionDate());
            if (nullCheck(conversionDate)) {
                jpsWorkflowModel.setInstall_date(Utils.installDateFormat(Long.valueOf(conversionDate)));
            }

            String feed = getFormValue(edgeFormDataList,installWorkflowFormId.getFeed());
            if (nullCheck(feed))
                jpsWorkflowModel.setNetwork_type(feed);

            String poleShape = getFormValue(edgeFormDataList,installWorkflowFormId.getPoleShape());
            if (nullCheck(poleShape))
                jpsWorkflowModel.setPole_shape(poleShape);

            String condition = getFormValue(edgeFormDataList,installWorkflowFormId.getCondition());
            if (nullCheck(condition))
                jpsWorkflowModel.setPole_status(condition);

            String poleNumber = getFormValue(edgeFormDataList,installWorkflowFormId.getPoleNumber());
            if (nullCheck(poleNumber))
                jpsWorkflowModel.setLocation_zipcode(poleNumber);
        }
        String geozonePaths = "/" + jpsWorkflowModel.getNotebookName() + "/" + jpsWorkflowModel.getAddress1();
        jpsWorkflowModel.setGeozonePath(geozonePaths);
        return jpsWorkflowModel;
    }


    private boolean nullCheck(String data) {
        return (data != null && data.length() > 0 && !data.contains("null")) ? true : false;
    }

    protected String getFormValue(List<EdgeFormData> edgeFormDatas,int id){
        EdgeFormData edgeFormData = new EdgeFormData();
        edgeFormData.setId(id);
        int pos = edgeFormDatas.indexOf(edgeFormData);
        if(pos > -1){
            return edgeFormDatas.get(pos).getValue();
        }
        return "";
    }



    public void processLampType(List<EdgeFormData> edgeFormDataList, JPSWorkflowModel jpsWorkflowModel) {
        String otherLampId = properties.getProperty("jps.edge.otherlamptype");//201
        String otherWattage = properties.getProperty("jps.edge.otherwattage");//188
        String otherNewLampModel = valueById(edgeFormDataList, Integer.parseInt(otherLampId));
        String lampWattage = valueById(edgeFormDataList, Integer.parseInt(otherWattage));
        jpsWorkflowModel.setLampType(otherNewLampModel);
        if(lampWattage != null){
            jpsWorkflowModel.setPower(lampWattage);
        }

    }

}
