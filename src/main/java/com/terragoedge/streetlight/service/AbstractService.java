package com.terragoedge.streetlight.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNotebook;
import com.terragoedge.edgeserver.FullEdgeNotebook;
import com.terragoedge.streetlight.PropertiesReader;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class AbstractService {
    private Gson gson;
    private Logger logger = Logger.getLogger(AbstractService.class);
    private JsonParser jsonParser;

    public AbstractService() {
        gson = new Gson();
        jsonParser = new JsonParser();
    }

    protected ResponseEntity<String> serverCall(String url, HttpMethod httpMethod, String body) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = getHeaders(null);

        HttpEntity request = null;
        if (body != null) {
            headers.add("Content-Type", "application/json");
            request = new HttpEntity<String>(body, headers);
        } else {
            request = new HttpEntity<>(headers);
        }

        ResponseEntity<String> responseEntity = restTemplate.exchange(url, httpMethod, request, String.class);
        logger.info("------------ Response ------------------");
        logger.info("Response Code:" + responseEntity.getStatusCode().toString());
        return responseEntity;
    }


    protected HttpHeaders getHeaders(String accessToken) {
        String userName = null;
        String password = null;
        HttpHeaders headers = new HttpHeaders();
        if (accessToken != null) {
            headers.add("Authorization", "Bearer " + accessToken);
            return headers;
        } else {
            userName = "slvinterface";
            password = "TerraGoEdge1!";
        }
        String plainCreds = userName + ":" + password;

        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        headers.add("Authorization", "Basic " + base64Creds);
        return headers;
    }

    protected ResponseEntity<String> getNoteDetails(String baseUrl, String noteGuid) {
        String urlNew = baseUrl + "/rest/notes?search=" + noteGuid;
        return serverCall(urlNew, HttpMethod.GET, null);
    }

    protected String geTalqNoteDetails(String baseUrl, String noteName) {
        try {
            String urlNew = baseUrl + "/rest/notes/notesdata/" + noteName;

            //  String urlNew = baseUrl + "/rest/notes?search=" + noteName;
            logger.info("Url to get Note Details:" + urlNew);
            ResponseEntity<String> responseEntity = serverCall(urlNew, HttpMethod.GET, null);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String response = responseEntity.getBody();
                logger.info("----------Response-------");
                logger.info(response);
                return response;
            }
        } catch (Exception e) {
            logger.error("Error in getNoteDetails", e);
        }
        return null;
    }

    public ResponseEntity<String> updateNoteDetails(String noteDetails, String noteGuid, String notebookGuid, String baseUrl) {
        String urlNew = baseUrl + "/rest/notebooks/" + notebookGuid + "/notes/" + noteGuid;
        return serverCall(urlNew, HttpMethod.PUT, noteDetails);
    }

    protected List<EdgeFormData> getEdgeFormData(String data) {
        try {
            List<EdgeFormData> edgeFormDatas = gson.fromJson(data, new TypeToken<List<EdgeFormData>>() {
            }.getType());
            return edgeFormDatas;
        } catch (Exception e) {
            data = data.substring(1, data.length() - 1);
            List<EdgeFormData> edgeFormDatas = gson.fromJson(data, new TypeToken<List<EdgeFormData>>() {
            }.getType());
            return edgeFormDatas;
        }

    }

    protected void updateServer(String edgeNoteJson, String notebookGuid, String noteguid, String url) {
        ResponseEntity<String> noteUpdateresponse = updateNoteDetails(edgeNoteJson, noteguid, notebookGuid, url);
        if (noteUpdateresponse.getStatusCode().is2xxSuccessful()) {
            String notesResponse = noteUpdateresponse.getBody();
            System.out.println(notesResponse);
            logger.info(noteguid + " note updated to server");
        } else {
            logger.error(noteguid + " Error while sending server.");
//            throw new WorkFlowException("Error while sending server.");
        }

    }

    protected String createNotebook(String baseUrl, FullEdgeNotebook edgeNotebook) {
        String notebookJson = gson.toJson(edgeNotebook);
        String notebookUrl = baseUrl + "/rest/notebooks";
        ResponseEntity<String> responseEntity = serverCall(notebookUrl, HttpMethod.POST, notebookJson);
        if (responseEntity != null && responseEntity.getStatusCodeValue() == HttpStatus.CREATED.value()) {
            String notebookGuidJson = responseEntity.getBody();
            JsonObject jsonObject = (JsonObject) jsonParser.parse(notebookGuidJson);
            return jsonObject.get("notebookGuid").getAsString();
        }
        return null;
    }
}
