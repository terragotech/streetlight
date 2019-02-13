package com.terragoedge.streetlight.edgeinterface;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.exception.NoValueException;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class EdgeService {
    private String baseUrl = null;
    private JsonParser jsonParser;
    private Gson gson;
    final Logger logger = Logger.getLogger(EdgeService.class);

    public EdgeService() {
        baseUrl = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        jsonParser = new JsonParser();
        gson = new Gson();
    }

    protected String getNoteDetails(String noteGuid) {
        try {
            // String urlNew = baseUrl + "/rest/notes/notesdata/" + noteGuid;
            String urlNew = baseUrl + "/rest/notes/" + noteGuid;
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

    public ResponseEntity<String> serverCall(String url, HttpMethod httpMethod, String body) {
        logger.info("Request Url : " + url);
        logger.info("Request Data : " + body);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = getHeaders();

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
        if (responseEntity.getBody() != null) {
            logger.info("Response Data:" + responseEntity.getBody());
        }

        return responseEntity;
    }

    private HttpHeaders getHeaders() {
        String userName = PropertiesReader.getProperties().getProperty("edge.username");
        String password = PropertiesReader.getProperties().getProperty("edge.password");
        HttpHeaders headers = new HttpHeaders();
        String plainCreds = userName + ":" + password;
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);
        headers.add("Authorization", "Basic " + base64Creds);
        return headers;
    }

    protected List<EdgeFormData> getEdgeFormData(String formDefJson) {
        try {
            List<EdgeFormData> edgeFormDatas = gson.fromJson(formDefJson, new TypeToken<List<EdgeFormData>>() {
            }.getType());
            return edgeFormDatas;
        } catch (Exception e) {
            formDefJson = formDefJson.substring(1, formDefJson.length() - 1);
            List<EdgeFormData> edgeFormDatas = gson.fromJson(formDefJson, new TypeToken<List<EdgeFormData>>() {
            }.getType());
            return edgeFormDatas;
        }
    }

    public JsonObject processEdgeForms(EdgeNote edgeNote, List<EdgeFormData> edgeFormDataList, String errorFormTemplateGuid, SlvData slvData) {
        // updateInstallationForm(edgeFormDataList, slvData, edgeNote);
        String edgenoteJson = gson.toJson(edgeNote);
        JsonObject edgeJsonObject = (JsonObject) jsonParser.parse(edgenoteJson);
        JsonArray serverEdgeFormJsonArray = edgeJsonObject.get("formData").getAsJsonArray();
        int size = serverEdgeFormJsonArray.size();
        for (int i = 0; i < size; i++) {
            JsonObject serverEdgeForm = serverEdgeFormJsonArray.get(i).getAsJsonObject();
            String formDefJson = serverEdgeForm.get("formDef").toString();
            formDefJson = formDefJson.replaceAll("\\\\", "");
            List<EdgeFormData> formDataList = getEdgeFormData(formDefJson);
            serverEdgeForm.add("formDef", gson.toJsonTree(formDataList));
            serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
        }
        edgeJsonObject.add("formData", serverEdgeFormJsonArray);
        edgeJsonObject.addProperty("noteGuid", UUID.randomUUID().toString());
        return edgeJsonObject;
    }
/*


    public JsonObject processEdgeForms(EdgeNote edgeNote, List<EdgeFormData> edgeFormDataList, String errorFormTemplateGuid, SlvData slvData) {
       // updateInstallationForm(edgeFormDataList, slvData, edgeNote);
        String edgenoteJson = gson.toJson(edgeNote);
        JsonObject edgeJsonObject = (JsonObject) jsonParser.parse(edgenoteJson);
        JsonArray serverEdgeFormJsonArray = edgeJsonObject.get("formData").getAsJsonArray();
        int size = serverEdgeFormJsonArray.size();
        for (int i = 0; i < size; i++) {
            JsonObject serverEdgeForm = serverEdgeFormJsonArray.get(i).getAsJsonObject();
            String currentFormTemplateGuid = serverEdgeForm.get("formTemplateGuid").getAsString();
            if (currentFormTemplateGuid.equals(errorFormTemplateGuid)) {
                String formDefJson = serverEdgeForm.get("formDef").toString();
                formDefJson = formDefJson.replaceAll("\\\\", "");
                List<EdgeFormData> formDataList = getEdgeFormData(formDefJson);
                updateInstallationForm(formDataList, slvData,edgeNote);
                serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
                serverEdgeForm.add("formDef", gson.toJsonTree(formDataList));
            } else {
                String formDefJson = serverEdgeForm.get("formDef").toString();
                formDefJson = formDefJson.replaceAll("\\\\", "");
                List<EdgeFormData> formDataList = getEdgeFormData(formDefJson);
                serverEdgeForm.add("formDef", gson.toJsonTree(formDataList));
                serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
            }

        }
        edgeJsonObject.add("formData", serverEdgeFormJsonArray);
        edgeJsonObject.addProperty("createdDateTime", System.currentTimeMillis());
        edgeJsonObject.addProperty("noteGuid", UUID.randomUUID().toString());
        return edgeJsonObject;
    }
*/

    public ResponseEntity<String> updateNoteDetails(String noteDetails, String noteGuid, String notebookGuid) {
        String baseUrl = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        String urlNew = baseUrl + "/rest/notebooks/" + notebookGuid + "/notes/" + noteGuid;
        return serverCall(urlNew, HttpMethod.PUT, noteDetails);
    }

    public void updateInstallationForm(List<EdgeFormData> edgeFormDataList, SlvData slvData, EdgeNote edgeNote) {
        try {
            if (slvData.getMacAddress() != null) {
                //set municipality values from csv
                String macAddress = slvData.getMacAddress().trim();
                if (macAddress != null)
                    updateFormValue(edgeFormDataList, 19, macAddress);
            }
            if (slvData.getFixtureQRScan() != null) {
                String fixtureQrScan = slvData.getFixtureQRScan().trim();
                if (fixtureQrScan != null) {
                    updateFormValue(edgeFormDataList, 20, fixtureQrScan);
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateFormValue(List<EdgeFormData> edgeFormDatas, int id, String value) {
        EdgeFormData tempEdgeFormData = new EdgeFormData();
        tempEdgeFormData.setId(id);
        int pos = edgeFormDatas.indexOf(tempEdgeFormData);
        if (pos != -1) {
            EdgeFormData edgeFormData = edgeFormDatas.get(pos);
            if (value != null && !value.trim().isEmpty()) {
                edgeFormData.setValue(edgeFormData.getLabel() + "#" + value);
            } else {
                edgeFormData.setValue(edgeFormData.getLabel() + "#");
            }

        }
    }

    public String formatDate(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("CST"));
        Date dd = new Date(Long.valueOf(time));
        return dateFormat.format(dd);
    }

    protected String valueById(List<EdgeFormData> edgeFormDatas, int id) throws NoValueException {
        for (EdgeFormData edgeFormData : edgeFormDatas) {
            if (edgeFormData.getId() == id) {
                String value = edgeFormData.getValue();
                if (value == null || value.trim().isEmpty()) {
                    throw new NoValueException("Value is Empty or null." + value);
                }
                return value;
            }
        }

        throw new NoValueException(id + " is not found.");
    }

}
