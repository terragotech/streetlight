package com.terragoedge.streetlight.edgeinterface;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.edgeserver.FormData;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.json.model.SLVEdgeFormData;
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
            // String urlNew = baseUrl + "/rest/notes/notesdata/" + noteName;
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
        String userName = PropertiesReader.getProperties().getProperty("streetlight.edge.username");
        String password = PropertiesReader.getProperties().getProperty("streetlight.edge.password");
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


    protected List<SLVEdgeFormData> getSLVEdgeFormData(String formDefJson) {
        try {
            List<SLVEdgeFormData> edgeFormDatas = gson.fromJson(formDefJson, new TypeToken<List<SLVEdgeFormData>>() {
            }.getType());
            return edgeFormDatas;
        } catch (Exception e) {
            formDefJson = formDefJson.substring(1, formDefJson.length() - 1);
            List<SLVEdgeFormData> edgeFormDatas = gson.fromJson(formDefJson, new TypeToken<List<SLVEdgeFormData>>() {
            }.getType());
            return edgeFormDatas;
        }
    }

    public JsonObject processEdgeForms(String edgenoteJson, String errorFormTemplateGuid, SlvData slvData) {
        JsonObject edgeJsonObject = (JsonObject) jsonParser.parse(edgenoteJson);
        JsonArray serverEdgeFormJsonArray = edgeJsonObject.get("formData").getAsJsonArray();
        int size = serverEdgeFormJsonArray.size();
        for (int i = 0; i < size; i++) {
            JsonObject serverEdgeForm = serverEdgeFormJsonArray.get(i).getAsJsonObject();
            String currentFormTemplateGuid = serverEdgeForm.get("formTemplateGuid").getAsString();
            if (currentFormTemplateGuid.equals(errorFormTemplateGuid)) {
                String formDefJson = serverEdgeForm.get("formDef").toString();
                formDefJson = formDefJson.replaceAll("\\\\", "");

                List<SLVEdgeFormData> edgeFormDataList = getSLVEdgeFormData(formDefJson);
                updateInstallationForm(edgeFormDataList, slvData);
                serverEdgeForm.add("formDef", gson.toJsonTree(edgeFormDataList));
                serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
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
        edgeJsonObject.addProperty("createdBy", "slvinterface");
        edgeJsonObject.addProperty("noteGuid", UUID.randomUUID().toString());
        return edgeJsonObject;
    }

    public ResponseEntity<String> updateNoteDetails(String noteDetails, String noteGuid, String notebookGuid) {
        String baseUrl = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        String urlNew = baseUrl + "/rest/notebooks/" + notebookGuid + "/notes/" + noteGuid;
        return serverCall(urlNew, HttpMethod.PUT, noteDetails);
    }

    public void updateInstallationForm(List<SLVEdgeFormData> edgeFormDataList, SlvData slvData) {
        String syncToSlvId = PropertiesReader.getProperties().getProperty("edge.slv.status");
        String errorDetailid = PropertiesReader.getProperties().getProperty("edge.slv.error");
        String processTimeId = PropertiesReader.getProperties().getProperty("edge.slv.processtime");
        String installedDateId = PropertiesReader.getProperties().getProperty("edge.slv.installedid");
        String relacedDateId = PropertiesReader.getProperties().getProperty("edge.slv.replacedid");
        System.out.println("synctoslv :" + syncToSlvId);
        System.out.println("errorDetailid :" + errorDetailid);
        try {
            for(EdgeFormData edgeFormData: edgeFormDataList){
                if(edgeFormData.getId()==115 && edgeFormData.getLabel().equals("Fixture QR Scan")){
                    edgeFormData.setId(20);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            updateFormValue(edgeFormDataList, Integer.parseInt(errorDetailid), slvData.getErrorDetails());
            updateFormValue(edgeFormDataList, Integer.parseInt(syncToSlvId), slvData.getSyncToSlvStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateFormValue(edgeFormDataList, Integer.parseInt(processTimeId), formatDate(Long.valueOf(slvData.getProcessedTime())));
        if (slvData.getInstalledDate() > 0) {
            updateFormValue(edgeFormDataList, Integer.parseInt(installedDateId), formatDate(slvData.getInstalledDate()));
        }
        if (slvData.getReplacedDate() > 0) {
            updateFormValue(edgeFormDataList, Integer.parseInt(relacedDateId), formatDate(slvData.getReplacedDate()));
        }
    }

    public static void updateFormValue(List<SLVEdgeFormData> edgeFormDatas, int id, String value) {
        SLVEdgeFormData tempEdgeFormData = new SLVEdgeFormData();
        tempEdgeFormData.setId(id);
        int pos = edgeFormDatas.indexOf(tempEdgeFormData);
        if (pos != -1) {
            SLVEdgeFormData edgeFormData = edgeFormDatas.get(pos);
            if (value != null && !value.trim().isEmpty()) {
                String componentValue = edgeFormData.getLabel() + "#" + value;
                edgeFormData.setValue(componentValue);
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
}