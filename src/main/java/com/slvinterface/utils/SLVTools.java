package com.slvinterface.utils;

import com.google.gson.*;
import com.slvinterface.dao.ConnectionDAO;
import com.slvinterface.dao.SLVInterfaceDAO;
import com.slvinterface.enumeration.Status;
import com.slvinterface.service.EdgeRestService;
import com.slvinterface.service.SLVRestService;
import com.slvinterface.json.*;
import com.slvinterface.exception.*;
import com.slvinterface.dao.tables.*;

import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.wololo.geojson.Feature;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

public class SLVTools {
    private SLVRestService slvRestService;
    private Gson gson;
    final Logger    logger = Logger.getLogger(SLVTools.class);
    private ConnectionDAO connectionDAO;
    private SLVInterfaceDAO slvInterfaceDAO;

    public SLVTools(){
        slvRestService = new SLVRestService();
        gson = new Gson();
    }
    private void setConnectionDAO(ConnectionDAO connectionDAO)
    {
        this.connectionDAO = connectionDAO;
    }
    private void setSLVDao(SLVInterfaceDAO slvInterfaceDAO)
    {
        this.slvInterfaceDAO = slvInterfaceDAO;
    }
    private int processDeviceJson(String deviceJson) {
        JsonObject jsonObject = new JsonParser().parse(deviceJson).getAsJsonObject();
        logger.info("Device request json:" + gson.toJson(jsonObject));
        JsonArray arr = jsonObject.getAsJsonArray("value");
        for (int i = 0; i < arr.size(); i++) {
            int id = arr.get(i).getAsJsonObject().get("id").getAsInt();
            logger.info("Device id value :" + id);
            System.out.println(id);
            return id;
        }
        return 0;
    }
    public boolean deviceAlreadyExists(String idOnController) {
        try {
            Properties properties = PropertiesReader.getProperties();
            String mainUrl = properties.getProperty("streetlight.slv.base.url");
            String deviceUrl = properties.getProperty("streetlight.slv.url.search.device");
            String url = mainUrl + deviceUrl;
            List<String> paramsList = new ArrayList<>();
            paramsList.add("attributeName=idOnController");
            paramsList.add("attributeValue=" + DataTools.URLEncoder(idOnController));
            paramsList.add("recurse=true");
            paramsList.add("returnedInfo=lightDevicesList");
            paramsList.add("attributeOperator=eq-i");
            paramsList.add("maxResults=1");
            paramsList.add("ser=json");
            String params = StringUtils.join(paramsList, "&");
            url = url + "?" + params;
            logger.info("Load Device url :" + url);
            HttpResponse response = slvRestService.callGetMethod(url);
            if (response.getStatusLine().getStatusCode() == 200) {
                String responseString = slvRestService.getResponseBody(response);
                int id = processDeviceJson(responseString);
                logger.info("LoadDevice Id :" + id);

                if (id == 0) {
                    return false;
                } else {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("Error",e);
        }
        return false;
    }
    public boolean checkMacAddressExists(String macAddress, String idOnController)
            throws QRCodeAlreadyUsedException, IOException {
        Properties properties = PropertiesReader.getProperties();
        /*boolean isExistMacAddress = false;
        boolean isExistMacAddress = connectionDAO.get


        if (isExistMacAddress) {
            logger.info("Given MAC Address Already Present in Local DB.");
             throw new QRCodeAlreadyUsedException("QR code [" + macAddress + "] is already processed.", macAddress);
        }*/
        logger.info("Getting Mac Address from SLV.");
        String mainUrl = properties.getProperty("streetlight.slv.base.url");
        String updateDeviceValues = properties.getProperty("streetlight.slv.url.search.device");
        String url = mainUrl + updateDeviceValues;
        List<String> paramsList = new ArrayList<String>();
        paramsList.add("attributeName=MacAddress");
        paramsList.add("attributeValue=" + macAddress);
        paramsList.add("attributeOperator=eq-i");
        String geoZoneId = PropertiesReader.getProperties().getProperty("streetlight.root.geozoneid");
        paramsList.add("geoZoneId="+geoZoneId);
        paramsList.add("recurse=true");
        paramsList.add("returnedInfo=devicesList");
        paramsList.add("ser=json");
        String params = StringUtils.join(paramsList, "&");
        url = url + "?" + params;
        System.out.println("Url :" + url);
        HttpResponse response = slvRestService.callGetMethod(url);
        if (response.getStatusLine().getStatusCode() == 200) {
            String responseString = slvRestService.getResponseBody(response);
            DeviceMacAddress deviceMacAddress = gson.fromJson(responseString, DeviceMacAddress.class);
            List<Value> values = deviceMacAddress.getValue();
            StringBuilder stringBuilder = new StringBuilder();
            if (values == null || values.size() == 0) {
                return false;
            } else {
                for (Value value : values) {
                    if (value.getIdOnController().equals(idOnController)) {
                        // return false;
                    }
                    stringBuilder.append(value.getIdOnController());
                    stringBuilder.append("\n");
                }
            }
            throw new QRCodeAlreadyUsedException("QR code [" + macAddress + "] is already Used in following devices [" + stringBuilder.toString() + "]", macAddress);
        } else {
            throw new QRCodeAlreadyUsedException("Error while getting data from SLV.", macAddress);
        }

    }
    public void replaceOLC(String controllerStrIdValue, String idOnController, String macAddress)
            throws ReplaceOLCFailedException{
        try {
            JsonParser jsonParser = new JsonParser();
            // Get Url detail from properties
            Properties properties = PropertiesReader.getProperties();
            String mainUrl = properties.getProperty("streetlight.slv.base.url");
            String dataUrl = properties.getProperty("streetlight.url.replaceolc");
            String replaceOlc = properties.getProperty("streetlight.url.replaceolc.method");
            String url = mainUrl + dataUrl;
            String controllerStrId = controllerStrIdValue;
            List<Object> paramsList = new ArrayList<Object>();
            paramsList.add("methodName=" + replaceOlc);
            paramsList.add("controllerStrId=" + controllerStrId);
            paramsList.add("idOnController=" + DataTools.URLEncoder(idOnController));
            paramsList.add("newNetworkId=" + macAddress);
            paramsList.add("ser=json");
            String params = StringUtils.join(paramsList, "&");
            url = url + "?" + params;
            System.out.println("Replace OLc called: " + macAddress);
            System.out.println("Replace OLc Url" + url);
            HttpResponse response = slvRestService.callPostMethod(url);


            if (response.getStatusLine().getStatusCode() == 200) {
                String responseString = slvRestService.getResponseBody(response);


                JsonObject replaceOlcResponse = (JsonObject) jsonParser.parse(responseString);
                String errorStatus = replaceOlcResponse.get("status").getAsString();
                // As per doc, errorcode is 0 for success. Otherwise, its not success.
                if (errorStatus.equals("ERROR")) {
                    logger.info("Replace Olc error is :" + errorStatus);
                    String value = replaceOlcResponse.get("value").getAsString();
                    throw new ReplaceOLCFailedException(value);
                } else {
                    //TODO: connectionDAO.updateSlvDevice(idOnController, macAddress);
                }
            }

        } catch (Exception e) {
            logger.error("Error in replaceOLC", e);
            throw new ReplaceOLCFailedException(e.getMessage());
        }

    }
    public void addStreetLightData(String key, String value, List<Object> paramsList) {

        paramsList.add("valueName=" + key.trim());
        try {
            paramsList.add("value=" + DataTools.URLEncoder(value.trim()));
        }catch (Exception e){
            logger.error("Error while encoding value in addStreetLightData",e);
        }
    }
    public HttpResponse createDevice(EdgeNote edgenote,
                                               String geoZoneId) throws Exception {
        String title = null;
        Properties properties = PropertiesReader.getProperties();
        try{
            title = DataTools.URLEncoder(edgenote.getTitle());
        }catch (Exception e){
            logger.error("Error while encode title: ",e);
            return null;
        }
        Feature feature = (Feature) GeoJSONFactory.create(edgenote.getGeometry());

        // parse Geometry from Feature
        GeoJSONReader reader = new GeoJSONReader();
        Geometry geom = reader.read(feature.getGeometry());


        String mainUrl = properties.getProperty("streetlight.slv.base.url");
        String serveletApiUrl = properties.getProperty("streetlight.slv.url.device.create");

        String methodName = properties.getProperty("streetlight.slv.device.create.methodName");
        String categoryStrId = properties.getProperty("streetlight.categorystr.id");
        String controllerStrId = properties.getProperty("streetlight.controller.str.id");
        String nodeTypeStrId = properties.getProperty("streetlight.slv.equipment.type");

        String url = mainUrl + serveletApiUrl + "/" + methodName;




        /*
        Map<String, String> streetLightDataParams = new HashMap<String, String>();
        streetLightDataParams.put("methodName", methodName);
        streetLightDataParams.put("categoryStrId", categoryStrId);
        streetLightDataParams.put("controllerStrId", controllerStrId);
        streetLightDataParams.put("idOnController", title);
        streetLightDataParams.put("geoZoneId", geoZoneId);
        streetLightDataParams.put("lng", String.valueOf(geom.getCoordinate().x));
        streetLightDataParams.put("lat", String.valueOf(geom.getCoordinate().y));
        streetLightDataParams.put("nodeTypeStrId", nodeTypeStrId);
        // streetLightDataParams.put("modelFunctionId", nodeTypeStrId);
        // modelFunctionId*/
        List<Object> paramsList = new ArrayList<>();

        paramsList.add("ser=json");
        paramsList.add("userName="+DataTools.URLEncoder(edgenote.getTitle()));
        paramsList.add("categoryStrId="+categoryStrId);
        paramsList.add("geozoneId="+geoZoneId);
        paramsList.add("controllerStrId="+controllerStrId);
        paramsList.add("idOnController="+DataTools.URLEncoder(edgenote.getTitle()));
        paramsList.add("lat="+String.valueOf(geom.getCoordinate().y));
        paramsList.add("lng="+String.valueOf(geom.getCoordinate().x));

        String params = StringUtils.join(paramsList, "&");
        url = url + "?" + params;
        logger.info(url);

        return slvRestService.callPostMethod(url);
    }
    public void setDeviceValues(List<Object> paramsList, SlvSyncDetails slvSyncDetails) throws DeviceUpdationFailedException {
        Properties properties = PropertiesReader.getProperties();
        JsonParser jsonParser = new JsonParser();
        String mainUrl = properties.getProperty("streetlight.slv.base.url");
        String updateDeviceValues = properties.getProperty("streetlight.slv.url.updatedevice");
        String url = mainUrl + updateDeviceValues;

        paramsList.add("ser=json");
        String params = StringUtils.join(paramsList, "&");
        url = url + "&" + params;
        System.out.println("SetDevice Called");
        System.out.println("URL : " + url);
        logger.info("Request URL " + url);
        HttpResponse httpResponse = null;
        try {
            httpResponse = slvRestService.callPostMethod(url);
        }
        catch (Exception e)
        {
            throw new DeviceUpdationFailedException(params);
        }
        String responseString = slvRestService.getResponseBody(httpResponse);
        JsonObject setDeviceValuesResponse = (JsonObject) jsonParser.parse(responseString);
        int errorCode = setDeviceValuesResponse.get("errorCode").getAsInt();
        logger.info("setDevice value :"+errorCode);
        logger.info(setDeviceValuesResponse);
        if (errorCode != 0) {
            slvSyncDetails.setErrorDetails(gson.toJson(setDeviceValuesResponse));
            slvSyncDetails.setStatus(Status.Failure.toString());
            throw new DeviceUpdationFailedException(gson.toJson(setDeviceValuesResponse));
        }
    }
    public void createSLVDevice(String title) {
        SlvDevice slvDevice = new SlvDevice();
        slvDevice.setDeviceId(title);
        slvDevice.setDeviceName(title);
        slvDevice.setProcessedDateTime(new Date().getTime());
        connectionDAO.saveSlvDevices(slvDevice);
    }
    public void createNewDevice(EdgeNote edgeNote, SlvSyncDetails slvSyncDetails, String geoZoneId) throws DeviceCreationFailedException {
        if (geoZoneId != null) {
            System.out.println("Device created method called");
            HttpResponse response = null;
            try {
                response = createDevice(edgeNote, geoZoneId);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            if (response.getStatusLine().getStatusCode() == 200)
            {
                String responseBody = slvRestService.getResponseBody(response);
                logger.info(responseBody);
                if (response.getStatusLine().getStatusCode()==200
                        && !responseBody.contains("<status>ERROR</status>")) {
                    logger.info("Device Created Successfully, NoteId:" + edgeNote.getNoteGuid() + "-"
                            + edgeNote.getTitle());
                    slvSyncDetails.setDeviceCreationStatus(Status.Success.toString());
                    System.out.println("Device created :" + edgeNote.getTitle());
                    createSLVDevice(edgeNote.getTitle());
                } else {
                    System.out.println("Device created Failed:" + edgeNote.getTitle());
                    logger.info("Device Created Failure, NoteId:" + edgeNote.getNoteGuid() + "-"
                            + edgeNote.getTitle());
                    slvSyncDetails.setDeviceCreationStatus(Status.Failure.toString());
                    slvSyncDetails.setErrorDetails("Device Creation Failed");
                    throw new DeviceCreationFailedException(edgeNote.getNoteGuid() + "-" + edgeNote.getTitle());
                }
            }
        } else {
            slvSyncDetails.setDeviceCreationStatus(Status.Failure.toString());
            slvSyncDetails.setErrorDetails("GeoZone should not be empty.");
            throw new DeviceCreationFailedException("GeoZone should not be empty.");
        }
    }
    public int checkAndCreateGeoZone(String geozone,EdgeNote edgeNote) {
        int geozoneId = -1;
        try {
            JsonParser jsonParser = new JsonParser();
            Properties properties = PropertiesReader.getProperties();
            String rootGeoZone = properties.getProperty("streetlight.root.geozone");
            String mainUrl = properties.getProperty("streetlight.slv.base.url");
            String searchGeoZone = properties.getProperty("com.slv.search.devices.url");
            String url = mainUrl + searchGeoZone;
            List<String> paramsList = new ArrayList<>();
            paramsList.add("ser=json");

            try{
                geozone = URLEncoder.encode(geozone.trim(), "UTF-8");
            }catch (Exception e){
                logger.error("Error in addStreetLightData",e);
            }

            paramsList.add("name="+geozone);
            paramsList.add("partialMatch=false");
            String params = StringUtils.join(paramsList, "&");
            url = url + "?" + params;
            logger.info("checkAndCreateGeoZone method called");
            logger.info("checkAndCreateGeoZone url:" + url);
            //setSLVTransactionLogs(slvTransactionLogs, url, CallType.SEARCH_GEOZONE);
            //ResponseEntity<String> response = restService.getPostRequest(url, null);
            HttpResponse response = slvRestService.callPostMethod(url);
            if (response.getStatusLine().getStatusCode() == 404)
            {
                geozoneId = -1;
            }else {
                String responseString = slvRestService.getResponseBody(response);
                //setResponseDetails(slvTransactionLogs, responseString);
                JsonArray jsonArray = jsonParser.parse(responseString).getAsJsonArray();
                geozone = URLDecoder.decode(geozone,"UTF-8");
                if (jsonArray != null && jsonArray.size() > 0) {
                    for (JsonElement jsonElement : jsonArray) {
                        JsonObject jsonObject = (JsonObject) jsonElement;
                        String geozoneNamePath = jsonObject.get("namesPath").getAsString();
                        if(geozoneNamePath.equals(rootGeoZone + geozone)){// inside unknown
                            geozoneId = jsonObject.get("id").getAsInt();
                        }
                    }
                }
            }
        } catch (Exception e) {
            //setResponseDetails(slvTransactionLogs, "Error in checkAndCreateGeoZone:" + e.getMessage());
            logger.error("Error in checkAndCreateGeoZone", e);
        } finally {
            //streetlightDao.insertTransactionLogs(slvTransactionLogs);
        }
        if(geozoneId == -1) {// no geozone present in unknown geozone so going to create geozone inside unknown
            geozoneId = createGeoZone(geozone,edgeNote);
        }
        return geozoneId;
    }
    public int createGeoZone(String geozone,EdgeNote edgeNote) {
        int geozoneId = -1;
        try {
            JsonParser jsonParser = new JsonParser();
            Properties properties = PropertiesReader.getProperties();
            int rootGeozoneId = Integer.valueOf(properties.getProperty("streetlight.root.geozoneid"));
            String mainUrl = properties.getProperty("streetlight.slv.base.url");
            String createGeozone = properties.getProperty("com.slv.create.geozone.method");
            Feature feature = (Feature) GeoJSONFactory.create(edgeNote.getGeometry());

            // parse Geometry from Feature
            GeoJSONReader reader = new GeoJSONReader();
            Geometry geom = reader.read(feature.getGeometry());

            double lat = geom.getCoordinate().y;
            double lng = geom.getCoordinate().x;

            double maxLat = lat;
            double maxLng = lng;
            double minLat = lat;
            double minLng = lng;
            String url = mainUrl + createGeozone;
            List<String> paramsList = new ArrayList<>();
            paramsList.add("ser=json");

            try{
                geozone = URLEncoder.encode(geozone.trim(), "UTF-8");
            }catch (Exception e){
                logger.error("Error in addStreetLightData",e);
            }

            paramsList.add("name="+geozone);
            paramsList.add("parentId="+rootGeozoneId);
            paramsList.add("latMax="+maxLat);
            paramsList.add("latMin="+minLat);
            paramsList.add("lngMax="+maxLng);
            paramsList.add("lngMin="+minLng);
            String params = StringUtils.join(paramsList, "&");
            url = url + "?" + params;
            logger.info("checkAndCreateGeoZone method called");
            logger.info("checkAndCreateGeoZone url:" + url);
            //setSLVTransactionLogs(slvTransactionLogs, url, CallType.CREATE_GEOZONE);
            //ResponseEntity<String> response = restService.getPostRequest(url, null);
            HttpResponse response = slvRestService.callPostMethod(url);
            if (response.getStatusLine().getStatusCode() == 404)
            {
                geozoneId = -1;
            }else {
                String responseString = slvRestService.getResponseBody(response);
                //setResponseDetails(slvTransactionLogs, responseString);
                JsonObject jsonObject = jsonParser.parse(responseString).getAsJsonObject();
                geozoneId = jsonObject.get("id").getAsInt();
            }
        } catch (Exception e) {
            //setResponseDetails(slvTransactionLogs, "Error in createGeoZone:" + e.getMessage());
            logger.error("Error in createGeoZone", e);
        } finally {
            //streetlightDao.insertTransactionLogs(slvTransactionLogs);
        }

        return geozoneId;
    }
}
