package com.slvinterface.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.slvinterface.dao.QueryExecutor;
import com.slvinterface.entity.SLVSyncTable;
import com.slvinterface.entity.SLVTransactionLogs;
import com.slvinterface.enumeration.CallType;
import com.slvinterface.exception.CreateGeoZoneException;
import com.slvinterface.exception.DeviceCreationException;
import com.slvinterface.exception.DeviceSearchException;
import com.slvinterface.exception.GeoZoneSearchException;
import com.slvinterface.utils.PropertiesReader;
import com.slvinterface.utils.SLVInterfaceUtilsModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SLVInterfaceUtils {
    Properties properties = PropertiesReader.getProperties();

    static final Logger logger = Logger.getLogger(SLVInterfaceUtils.class);
    SLVRestService slvRestService = null;
    JsonParser jsonParser = null;
    QueryExecutor queryExecutor;

    SLVInterfaceUtils(QueryExecutor queryExecutor){
        slvRestService = new SLVRestService();
        jsonParser = new JsonParser();
        this.queryExecutor =queryExecutor;
    }


    protected boolean isDevicePresent(SLVTransactionLogs slvTransactionLogs, String deviceName)throws DeviceSearchException{
        boolean isDevicePresent = false;
        try {
            String mainUrl = properties.getProperty("streetlight.slv.base.url");
            String searchDeviceMethodName = properties.getProperty("com.slv.search.device");
            int rootGeoZoneId = Integer.valueOf(properties.getProperty("com.slv.root.geozone.id"));
            String url = mainUrl + searchDeviceMethodName;
            List<String> paramsList = new ArrayList<>();

            paramsList.add("ser=json");
            paramsList.add("geozoneId="+rootGeoZoneId);
            paramsList.add("recurse=true");
            paramsList.add("returnedInfo=lightDevicesList");
            paramsList.add("attributeName=idOnController");
            paramsList.add("attributeValue="+deviceName);
            paramsList.add("maxResults=1");
            paramsList.add("attributeOperator=eq");
            String params = StringUtils.join(paramsList, "&");
            url = url + "?" + params;
            logger.info("isDevicePresent method called");
            logger.info("isDevicePresent url:" + url);
            setSLVTransactionLogs(slvTransactionLogs, url, CallType.SEARCH_DEVICE);
            HttpResponse  response = slvRestService.callGetMethod(url);
            if (response.getStatusLine().getStatusCode() == 404) {
                isDevicePresent = false;
            }
            else {
                String responseString = slvRestService.getResponseBody(response);
                setResponseDetails(slvTransactionLogs, responseString);
                JsonObject searchDeviceResponse = (JsonObject) jsonParser.parse(responseString);
                JsonArray jsonArray = searchDeviceResponse.get("value").getAsJsonArray();
                if (jsonArray != null && jsonArray.size() > 0) {
                    isDevicePresent = true;
                }
            }
        } catch (Exception e) {
            setResponseDetails(slvTransactionLogs, "Error in isDevicePresent:" + e.getMessage());
            logger.error("Error in isDevicePresent", e);
            throw  new DeviceSearchException(e);
        } finally {
            queryExecutor.saveSLVTransactionLogs(slvTransactionLogs);
        }

        return isDevicePresent;
    }


    protected int getGeoZoneId(String geozone, SLVTransactionLogs slvTransactionLogs) throws GeoZoneSearchException{
        int geozoneId = -1;
        try {
            String rootGeoZone = properties.getProperty("com.slv.root.geozone.name");
            String mainUrl = properties.getProperty("streetlight.slv.base.url");
            String searchGeoZone = properties.getProperty("com.slv.search.geozone");
            String url = mainUrl + searchGeoZone;
            List<String> paramsList = new ArrayList<>();
            paramsList.add("ser=json");
            //paramsList.add("name="+geozone);
            //paramsList.add("partialMatch=true");
            String params = StringUtils.join(paramsList, "&");
            url = url + "?" + params;
            logger.info("checkAndCreateGeoZone method called");
            logger.info("checkAndCreateGeoZone url:" + url);
            setSLVTransactionLogs(slvTransactionLogs, url, CallType.SEARCH_GEOZONE);
            HttpResponse  response = slvRestService.callGetMethod(url);
            if (response.getStatusLine().getStatusCode() == 404) {
                geozoneId = -1;
            }else {
                String responseString = slvRestService.getResponseBody(response);
                setResponseDetails(slvTransactionLogs, responseString);
                JsonArray jsonArray = jsonParser.parse(responseString).getAsJsonArray();
                if (jsonArray != null && jsonArray.size() > 0) {
                    for (JsonElement jsonElement : jsonArray) {
                        JsonObject jsonObject = (JsonObject) jsonElement;
                        String geozoneNamePath = jsonObject.get("namesPath").getAsString();
                        geozoneNamePath = geozoneNamePath.replaceAll("\'","");
                        geozoneNamePath = URLEncoder.encode(geozoneNamePath,"UTF-8");
                        String rootGeoZoneEncoded = URLEncoder.encode(rootGeoZone+"/","UTF-8");
                        if(geozoneNamePath.endsWith(geozone)){// inside unknown
                            geozoneId = jsonObject.get("id").getAsInt();
                            return geozoneId;
                        }
                    }
                }
            }
        } catch (Exception e) {
            setResponseDetails(slvTransactionLogs, "Error in checkAndCreateGeoZone:" + e.getMessage());
            logger.error("Error in checkAndCreateGeoZone", e);
            throw  new GeoZoneSearchException(e);
        } finally {
            queryExecutor.saveSLVTransactionLogs(slvTransactionLogs);
        }

        return geozoneId;
    }



    protected int createGeoZone(SLVInterfaceUtilsModel slvInterfaceUtilsModel)throws CreateGeoZoneException {
        int geozoneId = -1;
        SLVTransactionLogs slvTransactionLogs = getSLVTransVal(slvInterfaceUtilsModel);
        try {
            int previousGeoZoneId = Integer.valueOf(properties.getProperty("com.slv.root.geozone.id"));
            String mainUrl = properties.getProperty("streetlight.slv.base.url");
            String createGeozone = properties.getProperty("com.slv.create.geozone");
            String url = mainUrl + createGeozone;
            List<String> paramsList = new ArrayList<>();
            paramsList.add("ser=json");
            paramsList.add("name="+slvInterfaceUtilsModel.getCurrentGeoZoneName());
            paramsList.add("parentId="+previousGeoZoneId);
            paramsList.add("latMax="+slvInterfaceUtilsModel.getLat());
            paramsList.add("latMin="+slvInterfaceUtilsModel.getLat());
            paramsList.add("lngMax="+slvInterfaceUtilsModel.getLng());
            paramsList.add("lngMin="+slvInterfaceUtilsModel.getLng());
            String params = StringUtils.join(paramsList, "&");
            url = url + "?" + params;
            logger.info("checkAndCreateGeoZone method called");
            logger.info("checkAndCreateGeoZone url:" + url);
            setSLVTransactionLogs(slvTransactionLogs, url, CallType.CREATE_GEOZONE);
            HttpResponse  response = slvRestService.callGetMethod(url);
            if (response.getStatusLine().getStatusCode() == 404) {
                geozoneId = -1;
            }else {
                String responseString = slvRestService.getResponseBody(response);
                setResponseDetails(slvTransactionLogs, responseString);
                JsonObject jsonObject = jsonParser.parse(responseString).getAsJsonObject();
                geozoneId = jsonObject.get("id").getAsInt();
            }
        } catch (Exception e) {
            setResponseDetails(slvTransactionLogs, "Error in createGeoZone:" + e.getMessage());
            logger.error("Error in createGeoZone", e);
            throw new CreateGeoZoneException(e);
        } finally {
            queryExecutor.saveSLVTransactionLogs(slvTransactionLogs);
        }

        return geozoneId;
    }


    protected int createDevice(SLVInterfaceUtilsModel slvInterfaceUtilsModel)throws DeviceCreationException{
        int deviceId = -1;
        SLVTransactionLogs slvTransactionLogs = getSLVTransVal(slvInterfaceUtilsModel);
        try {
            String mainUrl = properties.getProperty("streetlight.slv.base.url");
            String createDeviceMethodName = properties.getProperty("com.slv.create.device.url");
            String controllerStrId = properties.getProperty("streetlight.controller.str.id");
            String categoryStrId = properties.getProperty("streetlight.categorystr.id");
            String url = mainUrl + createDeviceMethodName;
            List<String> paramsList = new ArrayList<>();
            paramsList.add("ser=json");
            paramsList.add("userName="+slvInterfaceUtilsModel.getDeviceName());
            paramsList.add("categoryStrId="+categoryStrId);
            paramsList.add("geozoneId="+slvInterfaceUtilsModel.getGeoZoneId());
            paramsList.add("controllerStrId="+controllerStrId);
            paramsList.add("idOnController="+slvInterfaceUtilsModel.getIdOnController());
            paramsList.add("lat="+slvInterfaceUtilsModel.getLat());
            paramsList.add("lng="+slvInterfaceUtilsModel.getLng());

            String params = StringUtils.join(paramsList, "&");
            url = url + "?" + params;
            logger.info("createDevice method called");
            logger.info("createDevice url:" + url);
            setSLVTransactionLogs(slvTransactionLogs, url, CallType.CREATE_DEVICE);
            HttpResponse  response = slvRestService.callGetMethod(url);
            String responseString = slvRestService.getResponseBody(response);
            setResponseDetails(slvTransactionLogs, responseString);
            JsonObject replaceOlcResponse = (JsonObject) jsonParser.parse(responseString);
            deviceId = replaceOlcResponse.get("id").getAsInt();
        } catch (Exception e) {
            setResponseDetails(slvTransactionLogs, "Error in createDevice:" + e.getMessage());
            logger.error("Error in createDevice", e);
            throw new DeviceCreationException(e);
        } finally {
            queryExecutor.saveSLVTransactionLogs(slvTransactionLogs);
        }

        return deviceId;
    }



    private void setSLVTransactionLogs(SLVTransactionLogs slvTransactionLogs, String request, CallType callType) {
        slvTransactionLogs.setRequestDetails(request);
        slvTransactionLogs.setTypeOfCall(callType);
    }

    private void setResponseDetails(SLVTransactionLogs slvTransactionLogs, String responseString) {
        slvTransactionLogs.setResponseBody(responseString);
    }



    public void checkDeviceDetails(SLVInterfaceUtilsModel slvInterfaceUtilsModel)throws DeviceSearchException,GeoZoneSearchException,CreateGeoZoneException,DeviceCreationException{
       boolean isDevicePresent =  isDevicePresent(getSLVTransVal(slvInterfaceUtilsModel),slvInterfaceUtilsModel.getIdOnController());
        if(isDevicePresent){
            return;
        }
        int geozoneId =  getGeoZoneId(slvInterfaceUtilsModel.getCurrentGeoZoneName(),getSLVTransVal(slvInterfaceUtilsModel));
        if(geozoneId == -1){
            /*geozoneId =  createGeoZone(slvInterfaceUtilsModel);
            if(geozoneId != -1){
                slvInterfaceUtilsModel.setGeoZoneId(geozoneId);
                createDevice(slvInterfaceUtilsModel);
            }*/
            throw  new CreateGeoZoneException("GeoZone not found");
        }else{
            slvInterfaceUtilsModel.setGeoZoneId(geozoneId);
            createDevice(slvInterfaceUtilsModel);
        }
    }


    public SLVTransactionLogs getSLVTransVal(SLVInterfaceUtilsModel slvInterfaceUtilsModel){
        SLVSyncTable slvSyncTable = slvInterfaceUtilsModel.getSlvSyncTable();
        SLVTransactionLogs slvTransactionLogs = new SLVTransactionLogs();
        slvTransactionLogs.setTitle(slvSyncTable.getNoteName());
        slvTransactionLogs.setNoteGuid(slvSyncTable.getNoteGuid());
        slvTransactionLogs.setCreatedDateTime(slvSyncTable.getNoteCreatedDateTime());
        slvTransactionLogs.setParentNoteGuid(slvSyncTable.getParentNoteId());
        return slvTransactionLogs;
    }
}
