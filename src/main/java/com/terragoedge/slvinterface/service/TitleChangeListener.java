package com.terragoedge.slvinterface.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.slvinterface.dao.ConnectionDAO;
import com.terragoedge.slvinterface.dao.TitleChangeDetail;
import com.terragoedge.slvinterface.model.EdgeFormData;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class TitleChangeListener {
    private Logger logger = Logger.getLogger(TitleChangeListener.class);
    private Properties properties;
    private JsonParser jsonParser;
    private Gson gson;
    private SlvRestService slvRestService;
    private ConnectionDAO connectionDAO;
    public TitleChangeListener() {
        properties= PropertiesReader.getProperties();
        jsonParser = new JsonParser();
        gson = new Gson();
        slvRestService = new SlvRestService();
        connectionDAO = ConnectionDAO.INSTANCE;
    }

    public void start() {
        long synctime = connectionDAO.getMaxSyncTime();
        if (synctime > 0) {
            String token = getEdgeToken();
            List<String> noteguids = getNoteGuids(synctime, token);
            for (String noteguid : noteguids) {
                changeNoteTitle(noteguid);
            }
        }
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

    private List<String> getNoteGuids(long lastSyncTime,String accessToken){
        List<String> noteguids = new ArrayList<>();
        String edgeSlvUrl =  PropertiesReader.getProperties().getProperty("streetlight.edge.slvserver.url");
        edgeSlvUrl = edgeSlvUrl+"/notesGuid?withRevision=false&lastSyncTime=";

        if(lastSyncTime > 0){
            edgeSlvUrl = edgeSlvUrl + lastSyncTime;

        }else{
            lastSyncTime = System.currentTimeMillis() - (10 * 60000);
            edgeSlvUrl = edgeSlvUrl + lastSyncTime;
        }



        // Get NoteList from edgeserver
        ResponseEntity<String> edgeSlvServerResponse = slvRestService.getRequest(edgeSlvUrl, false, accessToken);

        if (edgeSlvServerResponse.getStatusCode().is2xxSuccessful()) {

            // Get Response String
            String notesGuids = edgeSlvServerResponse.getBody();
            logger.info(notesGuids);

            JsonArray noteGuidsJsonArray = (JsonArray) jsonParser.parse(notesGuids);
            if(noteGuidsJsonArray != null &&  !noteGuidsJsonArray.isJsonNull()){
                for(JsonElement noteGuidJson : noteGuidsJsonArray){
                    String noteGuid = noteGuidJson.getAsString();
                    noteguids.add(noteGuid);
                }
            }

        } else {
            logger.error("Unable to get message from EdgeServer. Response Code is :" + edgeSlvServerResponse.getStatusCode());
        }
        return noteguids;
    }

    private void changeNoteTitle(String noteguid){
        long synctime = 0;
        String newTitle = null;
        String baseURl = properties.getProperty("streetlight.edge.url.main");
        String processingTemplateguid = properties.getProperty("streetlight.edge.processing.formtemplateguid");
        try {

            ResponseEntity<String> responseEntity = slvRestService.serverCall(baseURl+"/rest/notes/" + noteguid, HttpMethod.GET, null);
            logger.info("Get notes rest call response: "+responseEntity.getStatusCode().value());
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                String body = responseEntity.getBody();
                if (body == null) {
                    logger.error("no note present in inventory server for this serial no: " + noteguid);
                    return;
                } else {
                    JsonObject edgeJsonObject = jsonParser.parse(body).getAsJsonObject();
                    String oldNoteguid = edgeJsonObject.get("noteGuid").getAsString();
                    synctime = edgeJsonObject.get("syncTime").getAsLong();
                    JsonElement notebookElement = edgeJsonObject.get("edgeNotebook");
                    if (notebookElement == null || notebookElement.isJsonNull()) {
                        logger.error("This senor is undefined: " + noteguid);
                        return;
                    } else {
                        String title = edgeJsonObject.get("title").getAsString();
                            String notebookGuid = edgeJsonObject.get("edgeNotebook").getAsJsonObject().get("notebookGuid").getAsString();
                            String createdBy = edgeJsonObject.get("createdBy").getAsString();
                            createdBy = createdBy == null ? "admin" : createdBy;
                            JsonArray serverEdgeFormJsonArray = edgeJsonObject.get("formData").getAsJsonArray();
                            int size = serverEdgeFormJsonArray.size();
                            int validFormsCount = 0;
                            for (int i = 0; i < size; i++) {
                                JsonObject serverEdgeForm = serverEdgeFormJsonArray.get(i).getAsJsonObject();
                                String formDefJson = serverEdgeForm.get("formDef").getAsString();
                                String formTemplateGuid = serverEdgeForm.get("formTemplateGuid").getAsString();
                                formDefJson = formDefJson.replaceAll("\\\\", "");
                                List<EdgeFormData> edgeFormDataList = getEdgeFormData(formDefJson);
                                if (formTemplateGuid.equals(processingTemplateguid)) {
                                    validFormsCount = validFormsCount + 1;
                                    newTitle = getTitleFromForm(edgeFormDataList);
                                }

                                serverEdgeForm.add("formDef", gson.toJsonTree(edgeFormDataList));
                                serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
                            }
                            if (validFormsCount == 0) {
                                logger.info("There is no processing formtemplate present in this note. so skipping");
                                return;
                            } else if (validFormsCount > 1) {
                                logger.info("There is two or more processing formtemplate present in this note. so skipping");
                                return;
                            }
                            if(newTitle == null || newTitle.equals("")){
                logger.error("new title is empty or null. it will create empty title. So it's skipping");
                return;
            }
                        if (title.equals(newTitle)) {
                            logger.info("Title already matched !");
                            return;
                        }
                            edgeJsonObject.add("formData", serverEdgeFormJsonArray);
                            edgeJsonObject.addProperty("noteGuid", UUID.randomUUID().toString());
                            edgeJsonObject.addProperty("createdDateTime", System.currentTimeMillis());
                            edgeJsonObject.addProperty("createdBy", "admin");
                            edgeJsonObject.addProperty("title", newTitle);
                            ResponseEntity<String> responseEntity1 = slvRestService.serverCall(baseURl+"/rest/notebooks/" + notebookGuid + "/notes/" + oldNoteguid, HttpMethod.PUT, gson.toJson(edgeJsonObject));
                            logger.info("Update note rest call response: "+responseEntity1.getStatusCode().value());
                            String createdNoteGuid = responseEntity1.getBody();
                            connectionDAO.updateEdgeNote(createdBy, createdNoteGuid);
                        }
                }
            } else {
                logger.error("Error while getting inventory note: " + responseEntity.getBody());
            }
        }catch (Exception e){
            logger.error("Error in changeNoteTitle: ",e);
        }finally {
            TitleChangeDetail dbDetail = connectionDAO.getTitleChangeDetail(noteguid);
            if (dbDetail == null) {
                TitleChangeDetail titleChangeDetail = new TitleChangeDetail();
                titleChangeDetail.setNoteguid(noteguid);
                titleChangeDetail.setSynctime(synctime);
                connectionDAO.saveSlvSyncDetail(titleChangeDetail);
            }
        }
    }

    protected List<EdgeFormData> getEdgeFormData(String data) {
        List<EdgeFormData> edgeFormDatas = gson.fromJson(data, new TypeToken<List<EdgeFormData>>() {
        }.getType());
        return edgeFormDatas;
    }

    private String getFormValue(List<EdgeFormData> edgeFormDatas, int id){
        for(EdgeFormData edgeFormData : edgeFormDatas) {
            if(edgeFormData.getId() == id){
                String value = edgeFormData.getValue();
                value = (value == null || value.contains("null")) ? "" : value;
                value = value.contains("#") ? value.split("#",-1)[1] : value;
                return value;
            }
        }
        return null;
    }

    private String getTitleFromForm(List<EdgeFormData> edgeFormDatas){
        String cabinetId = getFormValue(edgeFormDatas,Integer.valueOf(properties.getProperty("streetlight.edge.form.cabinetid.id")));
        String processingNo = getFormValue(edgeFormDatas,Integer.valueOf(properties.getProperty("streetlight.edge.form.processing.no.id")));
        String fixtureCount = getFormValue(edgeFormDatas,Integer.valueOf(properties.getProperty("streetlight.edge.form.fixture.count.id")));

        if ((cabinetId == null || cabinetId.equals("")) && (processingNo == null || processingNo.equals("")) && (fixtureCount == null || fixtureCount.equals("")))
        {
            return null;
        } else {
            cabinetId = cabinetId == null ? "" : cabinetId;
            processingNo = processingNo == null ? "" : processingNo;
            fixtureCount = fixtureCount == null ? "" : fixtureCount;
            return cabinetId + "." + processingNo + "-" + fixtureCount;
        }
    }
}
