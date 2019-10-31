package com.terragoedge.slvinterface.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.slvinterface.model.EdgeFormData;
import com.terragoedge.slvinterface.utils.Utils;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class EdgeService {
    private Logger logger = Logger.getLogger(EdgeService.class);
    protected  Gson gson = new Gson();
    protected JsonParser jsonParser = new JsonParser();

    protected ResponseEntity<String> serverCall(String url, HttpMethod httpMethod, String body) {
        logger.info("------------ Request ------------------");
        logger.info("Request Url:" + url);
        logger.info("------------ input data ------------------");
        logger.info("Request Data:" + body);
        RestTemplate restTemplate = Utils.getRestTemplate();
        HttpHeaders headers = getHeaders(null);

        HttpEntity request = null;
        if(body != null){
            headers.add("Content-Type", "application/json");
            request = new HttpEntity<String>(body, headers);
        }else{
            request = new HttpEntity<>(headers);
        }

        ResponseEntity<String> responseEntity = restTemplate.exchange(url, httpMethod, request, String.class);
        logger.info("------------ Response ------------------");
        logger.info("Response Code:" + responseEntity.getStatusCode().toString());
        logger.info("------------ Response data ------------------");
        logger.info("Response Data:" + responseEntity.getBody());
        return responseEntity;
    }


    protected HttpHeaders getHeaders(String accessToken) {
        String userName = null;
        String password = null;
        HttpHeaders headers = new HttpHeaders();
        if(accessToken != null){
            headers.add("Authorization",  "Bearer "+accessToken);
            return headers;
        }else{
            userName = "admin";
            password = "T455wy04ry41!";
        }
        String plainCreds = userName + ":" + password;

        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        headers.add("Authorization", "Basic " + base64Creds);
        return headers;
    }


    public  void updateFormValue(List<EdgeFormData> edgeFormDatas, int id, String value) {
        EdgeFormData tempEdgeFormData = new EdgeFormData();
        tempEdgeFormData.setId(id);
        int pos = edgeFormDatas.indexOf(tempEdgeFormData);
        if (pos != -1) {
            EdgeFormData edgeFormData = edgeFormDatas.get(pos);
            edgeFormData.setValue(edgeFormData.getLabel() + "#" + value);
        }
    }


    protected List<EdgeFormData> getEdgeFormData(String data) {
        List<EdgeFormData> edgeFormDatas = gson.fromJson(data, new TypeToken<List<EdgeFormData>>() {
        }.getType());
        return edgeFormDatas;
    }

    public void setGroupValue(String value, JsonObject notesJson) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("key", "groupGuid");
        jsonObject.addProperty("value", value);
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(jsonObject);
        notesJson.add("dictionary", jsonArray);
    }

    protected ResponseEntity<String> updateNoteDetails(String baseUrl,String noteDetails, String noteGuid, String notebookGuid) {
        if(notebookGuid != null){
            String urlNew = baseUrl + "/rest/notebooks/" + notebookGuid + "/notes/" + noteGuid;
            return serverCall(urlNew,HttpMethod.PUT,noteDetails);
        }else{
            String urlNew = baseUrl + "/rest/notes/" + noteGuid;
            return serverCall(urlNew,HttpMethod.PUT,noteDetails);
        }


    }

    protected String geNoteDetails(String baseUrl, String noteName) {
        try {
            String urlNew = baseUrl + "/rest/notes/" + noteName;
            ResponseEntity<String> responseEntity = serverCall(urlNew, HttpMethod.GET, null);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String response = responseEntity.getBody();
                return response;
            }
        } catch (Exception e) {
            logger.error("Error in getNoteDetails", e);
        }
        return null;
    }
}
