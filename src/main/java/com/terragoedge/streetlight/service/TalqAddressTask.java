package com.terragoedge.streetlight.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.dao.ConnectionDAO;
import com.terragoedge.streetlight.dao.StreetlightDao;
import com.terragoedge.streetlight.exception.NotesNotFoundException;
import com.terragoedge.streetlight.logging.LoggingModel;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;
import com.terragoedge.streetlight.json.model.Dictionary;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TalqAddressTask extends AbstractService implements Runnable {
    LoggingModel loggingModel = null;
    private Gson gson = null;
    private JsonParser jsonParser = null;
    private StreetlightDao streetlightDao = null;
    private Logger logger = Logger.getLogger(TalkAddressService.class);
    private Properties properties = null;

    public TalqAddressTask(LoggingModel loggingModel) {
        this.loggingModel = loggingModel;
        gson = new Gson();
        jsonParser = new JsonParser();
        streetlightDao = new StreetlightDao();
        properties = PropertiesReader.getProperties();
    }

    @Override
    public void run() {
        String emptyTalqAddressGuid = PropertiesReader.getProperties().getProperty("empty.talkaddress.layerguid");
        String completeLayerGuid = PropertiesReader.getProperties().getProperty("talkaddress.complete.layerguid");
        String mainUrl = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        try {
            String notesJson = geTalqNoteDetails(mainUrl, loggingModel.getNoteName());
            if (notesJson == null) {
                logger.info("Note not in Edge.");
                throw new NotesNotFoundException("Note [" + loggingModel.getNoteName() + "] not in Edge.");
            }
            Type listType = new TypeToken<ArrayList<EdgeNote>>() {
            }.getType();
            List<EdgeNote> edgeNoteList = gson.fromJson(notesJson, listType);
            for (EdgeNote edgeNote : edgeNoteList) {
                    String oldNoteGuid = edgeNote.getNoteGuid();
                    String notebookGuid = edgeNote.getEdgeNotebook().getNotebookGuid();
                    JsonObject jsonObject = processEdgeForms(gson.toJson(edgeNote),loggingModel);
                    boolean isProcess = isProcessNoteLayer(edgeNote, jsonObject, emptyTalqAddressGuid, completeLayerGuid, loggingModel);
                    if (isProcess) {
                        logger.info("-------------------request json--------------- ");
                        logger.info(jsonObject.toString());
                        logger.info("-------------------request End--------------- ");
                        ResponseEntity<String> responseEntity = updateNoteDetails(jsonObject.toString(), oldNoteGuid, notebookGuid, mainUrl);
                        loggingModel.setCreatedDatetime(String.valueOf(System.currentTimeMillis()));
                        loggingModel.setStatus("Success");
                        loggingModel.setTalqAddressnoteGuid(responseEntity.getBody());
                        streetlightDao.insertTalqSync(loggingModel);
                        logger.info("edgenote update to server: " + responseEntity.getBody());
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Error: " + e);
        }

    }

    public boolean isProcessNoteLayer(EdgeNote edgeNote, JsonObject jsonObject, String talqAddressGuid, String completeLayerGuid, LoggingModel loggingModel) {
        List<Dictionary> dictionaryList = edgeNote.getDictionary();
        jsonObject.addProperty("createdDateTime", System.currentTimeMillis());
        jsonObject.addProperty("noteGuid", UUID.randomUUID().toString());
        jsonObject.addProperty("createdBy","admin");
        jsonObject.remove("dictionary");
        if (loggingModel != null) {
            if (loggingModel.getLayerType().equals("No data ever received")) {
                setGroupValue(dictionaryList,talqAddressGuid, jsonObject);
            } else if (loggingModel.getLayerType().equals("Complete")) {
                setGroupValue(dictionaryList,completeLayerGuid, jsonObject);
            }else{
                String layerguid = streetlightDao.getGroupGuid(loggingModel.getLayerType());
                if(layerguid == null){
                    logger.error("layer guid not present for giver layer: "+loggingModel.getLayerType());
                    return false;
                }else{
                    setGroupValue(dictionaryList,layerguid,jsonObject);
                }
            }
        }

        return true;
    }

    public void setGroupValue(List<Dictionary> dictionaryList, String value, JsonObject notesJson) {
        if(dictionaryList == null){
            dictionaryList = new ArrayList<>();
        }
        if (dictionaryList == null || dictionaryList.size() == 0) {
            Dictionary dictionary = new Dictionary();
            dictionary.setKey("groupGuid");
            dictionary.setValue(value);
            dictionaryList.add(dictionary);
        }else{
            boolean isLayerUpdated= false;
            for (Dictionary dictionary : dictionaryList) {
                if (dictionary.getKey().equals("groupGuid") && !dictionary.getValue().equals(value)) {
                    isLayerUpdated = true;
                    dictionary.setValue(value);
                }
            }
            if(!isLayerUpdated){
                Dictionary dictionary = new Dictionary();
                dictionary.setKey("groupGuid");
                dictionary.setValue(value);
                dictionaryList.add(dictionary);
            }
        }
        notesJson.add("dictionary", gson.toJsonTree(dictionaryList));
    }

    public JsonObject processEdgeForms(String edgenoteJson,LoggingModel loggingModel) {
        JsonObject edgeJsonObject = (JsonObject) jsonParser.parse(edgenoteJson);
        JsonArray serverEdgeFormJsonArray = edgeJsonObject.get("formData").getAsJsonArray();
        int size = serverEdgeFormJsonArray.size();
        for (int i = 0; i < size; i++) {
            JsonObject serverEdgeForm = serverEdgeFormJsonArray.get(i).getAsJsonObject();
            String formDefJson = serverEdgeForm.get("formDef").toString();
            formDefJson = formDefJson.replaceAll("\\\\", "");
            List<EdgeFormData> formDataList = getEdgeFormData(formDefJson);
            if(serverEdgeForm.get("formTemplateGuid").getAsString().equals(properties.getProperty("com.layer.changes.formtemplateguid"))){
                String componentIdsStr = properties.getProperty("com.layer.changes.form.component.ids");
                if(componentIdsStr != null){
                    String[] componentIds = componentIdsStr.split(",",-1);
                    for(String componentId : componentIds){
                        updateFormValue(formDataList,componentId,loggingModel.getMacAddress());
                    }
                }
            }
            serverEdgeForm.add("formDef", gson.toJsonTree(formDataList));
            serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
        }
        edgeJsonObject.add("formData", serverEdgeFormJsonArray);
        edgeJsonObject.addProperty("createdDateTime", System.currentTimeMillis());
        edgeJsonObject.addProperty("noteGuid", UUID.randomUUID().toString());
        return edgeJsonObject;
    }
    private void updateFormValue(List<EdgeFormData> edgeFormDatas,String componentId,String value){
        for(EdgeFormData edgeFormData : edgeFormDatas){
            if(edgeFormData.getId() == Integer.valueOf(componentId)){
                edgeFormData.setValue(value);
            }
        }
    }
}
