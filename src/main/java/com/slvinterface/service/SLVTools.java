package com.slvinterface.service;

import com.google.gson.*;
import com.slvinterface.dao.QueryExecutor;
import com.slvinterface.entity.SLVSyncTable;
import com.slvinterface.entity.SLVTransactionLogs;
import com.slvinterface.service.SLVRestService;
import com.slvinterface.json.*;
import com.slvinterface.exception.*;

import com.slvinterface.utils.DataTools;
import com.slvinterface.utils.PropertiesReader;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.wololo.geojson.Feature;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;


public class SLVTools {
    private SLVRestService slvRestService;
    private Gson gson;
    QueryExecutor queryExecutor = null;
    final Logger    logger = Logger.getLogger(SLVTools.class);

    public SLVTools()throws Exception{
        slvRestService = new SLVRestService();
        gson = new Gson();
        queryExecutor = new QueryExecutor();
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
    public boolean deviceAlreadyExists(String idOnController) throws ErrorCheckDeviceExists {
        try {
            Properties properties = PropertiesReader.getProperties();
            String mainUrl = properties.getProperty("streetlight.slv.base.url");
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
            ResponseEntity<String> response = slvRestService.callGetMethod(url);
            if (response.getStatusCode() == HttpStatus.OK) {
                String responseString = response.getBody();
                int id = processDeviceJson(responseString);
                logger.info("LoadDevice Id :" + id);

                if (id == 0) {
                    return false;
                } else {
                    return true;
                }
            }

        } catch (Exception e) {
            throw new ErrorCheckDeviceExists(e.getMessage());
        }
        return false;
    }

    public ResponseEntity<String> createDevice(Edge2SLVData previousEdge2SLVData,
                                               String geoZoneId, SLVTransactionLogs slvTransactionLogs) throws Exception {
        Properties properties = PropertiesReader.getProperties();

        Feature feature = (Feature) GeoJSONFactory.create(previousEdge2SLVData.getGeometry());

        // parse Geometry from Feature
        GeoJSONReader reader = new GeoJSONReader();
        Geometry geom = reader.read(feature.getGeometry());


        String mainUrl = properties.getProperty("streetlight.slv.base.url");
        String serveletApiUrl = properties.getProperty("streetlight.slv.url.device.create");

//        String methodName = properties.getProperty("streetlight.slv.device.create.methodName");
        String categoryStrId = properties.getProperty("streetlight.categorystr.id");
        String controllerStrId = properties.getProperty("streetlight.controller.str.id");
        String nodeTypeStrId = properties.getProperty("streetlight.slv.equipment.type");

//        String url = mainUrl + serveletApiUrl + "/" + methodName;
        String url = mainUrl + serveletApiUrl;




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
        LinkedMultiValueMap<String,String> paramsList = new LinkedMultiValueMap<>();

        paramsList.add("ser","json");
        paramsList.add("userName",previousEdge2SLVData.getIdOnController());
        paramsList.add("categoryStrId",DataTools.URLEncoder(categoryStrId));
        paramsList.add("geozoneId",geoZoneId);
        paramsList.add("controllerStrId",DataTools.URLEncoder(controllerStrId));
        paramsList.add("idOnController",previousEdge2SLVData.getIdOnController());
        paramsList.add("lat",String.valueOf(geom.getCoordinate().y));
        paramsList.add("lng",String.valueOf(geom.getCoordinate().x));
        //paramsList.add("nodeTypeStrId="+DataTools.URLEncoder(nodeTypeStrId));

        logger.info(url);
        slvTransactionLogs.setRequestDetails(url);
        return slvRestService.getPostRequest(url,null,paramsList);
    }

    public void createNewDevice(Edge2SLVData previousEdge2SLVData, SLVSyncTable slvSyncDetails, String geoZoneId) throws DeviceCreationFailedException {
        if (geoZoneId != null) {
            System.out.println("Device created method called");
            ResponseEntity<String> response = null;
            SLVTransactionLogs slvTransactionLogs = new SLVTransactionLogs();

            try {
                response = createDevice(previousEdge2SLVData, geoZoneId,slvTransactionLogs);
            }
            catch (Exception e)
            {
                throw new DeviceCreationFailedException(e.getMessage());
            }
            if (response.getStatusCode() == HttpStatus.OK)
            {
                String responseBody = response.getBody();
                slvTransactionLogs.setResponseBody(responseBody);
                logger.info(responseBody);
                if (response.getStatusCode()==HttpStatus.OK
                        && !responseBody.contains("<status>ERROR</status>")) {
                    logger.info("Device Created Successfully, NoteId:" + slvSyncDetails.getNoteGuid() + "-"
                            + previousEdge2SLVData.getTitle());
                    slvSyncDetails.setDeviceCreationStatus("Success");
                    System.out.println("Device created :" + previousEdge2SLVData.getTitle());
                    //createSLVDevice(edgeNote.getTitle());
                } else {
                    System.out.println("Device created Failed:" + previousEdge2SLVData.getTitle());
                    logger.info("Device Created Failure, NoteId:" + slvSyncDetails.getNoteGuid() + "-"
                            + previousEdge2SLVData.getTitle());
                    slvSyncDetails.setDeviceCreationStatus("Failure");
                    throw new DeviceCreationFailedException(slvSyncDetails.getNoteGuid() + "-" + previousEdge2SLVData.getTitle());
                }
            }
            //connectionDAO.saveSLVTransactionLog(slvTransactionLogs);
        }
        else {
            slvSyncDetails.setDeviceCreationStatus("Failure");
            slvSyncDetails.setErrorDetails("GeoZone should not be empty.");
            throw new DeviceCreationFailedException("GeoZone should not be empty.");
        }
    }

    public void syncMacAddress2Edge(String idOnController,String macAddress,String blockName){
        Properties properties = PropertiesReader.getProperties();


        if(macAddress != null && !macAddress.trim().isEmpty()){
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("slvMacAddress",macAddress);
            params.add("slvIdOnController",idOnController);
            if(blockName != null){
                params.add("atlasPhysicalPage",blockName);
            }
            slv2Edge( "rest/validation/updateSLVSyncedMAC", HttpMethod.GET,params);
        }
    }
    public void removeEdgeSLVMacAddress(String idOnController){
        removeSLVMAC("rest/validation/removeSLVMacAddress?slvIdOnController="+idOnController, HttpMethod.GET);
    }
    private  HttpHeaders getEdgeHeaders() {
        Properties properties = PropertiesReader.getProperties();
        String userName = properties.getProperty("streetlight.edge.username");
        String password = properties.getProperty("streetlight.edge.password");
        HttpHeaders headers = new HttpHeaders();
        String plainCreds = userName + ":" + password;

        byte[] plainCredsBytes = plainCreds.getBytes();

        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        headers.add("Authorization", "Basic " + base64Creds);
        return headers;
    }
    public ResponseEntity<String> slv2Edge(String httpUrl,  HttpMethod httpMethod, MultiValueMap<String, String> params){
        HttpHeaders headers = getEdgeHeaders();
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity request = new HttpEntity<>(headers);

        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        url = url + httpUrl;

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);

        uriBuilder.queryParams(params);

        ResponseEntity<String> response = restTemplate.exchange(uriBuilder.toUriString(), httpMethod, request, String.class);
        logger.info("------------ Response ------------------");
        logger.info("Response Code:" + response.getStatusCode().toString());
        String responseBody = response.getBody();
        logger.info(responseBody);
        logger.info("------------ Response End ------------------");
        return response;
    }


    public ResponseEntity<String> removeSLVMAC(String httpUrl,  HttpMethod httpMethod){
        HttpHeaders headers = getEdgeHeaders();
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity request = new HttpEntity<>(headers);

        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        url = url + httpUrl;


        ResponseEntity<String> response = restTemplate.exchange(httpUrl, httpMethod, request, String.class);
        logger.info("------------ Response ------------------");
        logger.info("Response Code:" + response.getStatusCode().toString());
        String responseBody = response.getBody();
        logger.info(responseBody);
        logger.info("------------ Response End ------------------");
        return response;
    }


}