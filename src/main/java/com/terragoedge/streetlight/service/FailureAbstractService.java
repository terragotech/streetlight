package com.terragoedge.streetlight.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terragoedge.streetlight.PropertiesReader;
import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class FailureAbstractService {
    private String baseUrl = null;
    private RestService restService = null;
    private JsonParser jsonParser;

    final Logger logger = Logger.getLogger(FailureAbstractService.class);

    FailureAbstractService() {
        baseUrl = PropertiesReader.getProperties().getProperty("amerescousa.edge.url.main");
        restService = new RestService();
        jsonParser = new JsonParser();
    }

    protected String getEdgeToken() {
        String url = PropertiesReader.getProperties().getProperty("server.edge.url.main");
        String userName = PropertiesReader.getProperties().getProperty("streetlight.edge.username");
        String password = PropertiesReader.getProperties().getProperty("streetlight.edge.password");
        url = url + "/oauth/token?grant_type=password&username=" + userName + "&password=" + password
                + "&client_id=edgerestapp";
        ResponseEntity<String> responseEntity = restService.getRequest(url);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            JsonObject jsonObject = (JsonObject) jsonParser.parse(responseEntity.getBody());
            return jsonObject.get("access_token").getAsString();
        }
        return null;

    }


    protected String getNoteDetails(String noteName) {
       try {
            String urlNew = baseUrl + "/rest/notes?search=" + noteName;
            logger.info("Url to get Note Details:"+urlNew);
            ResponseEntity<String> responseEntity = restService.serverCall(urlNew, HttpMethod.GET, null);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String response = responseEntity.getBody();
                logger.info("----------Response-------");
                logger.info(response);
                return response;
            }
        } catch (Exception e) {
           logger.error("Error in getNoteDetails",e);
        }
        return null;
    }

    public ResponseEntity<String> updateNoteDetails(String noteDetails, String noteGuid, String notebookGuid) {
        String baseUrl = PropertiesReader.getProperties().getProperty("amerescousa.edge.url.main");
        String urlNew = baseUrl + "/rest/notebooks/" + notebookGuid + "/notes/" + noteGuid;
        return restService.serverCall(urlNew, HttpMethod.PUT, noteDetails);
    }

}
