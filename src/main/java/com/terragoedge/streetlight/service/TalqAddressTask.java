package com.terragoedge.streetlight.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.dao.StreetlightDao;
import com.terragoedge.streetlight.exception.NotesNotFoundException;
import com.terragoedge.streetlight.logging.LoggingModel;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TalqAddressTask extends AbstractService implements Runnable {
    LoggingModel loggingModel = null;
    private Gson gson = null;
    private JsonParser jsonParser = null;
    private Properties properties = null;
    private RestService restService = null;
    private StreetlightDao streetlightDao = null;
    private Logger logger = Logger.getLogger(TalkAddressService.class);

    public TalqAddressTask(LoggingModel loggingModel) {
        this.loggingModel = loggingModel;
        gson = new Gson();
        jsonParser = new JsonParser();
        properties = PropertiesReader.getProperties();
        restService = new RestService();
        streetlightDao = new StreetlightDao();
    }

    @Override
    public void run() {
        String emptyTalqAddressGuid = PropertiesReader.getProperties().getProperty("empty.talkaddress.layerguid");
        String completeLayerGuid = PropertiesReader.getProperties().getProperty("talkaddress.complete.layerguid");
        String mainUrl = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        String locationDescKeyword = PropertiesReader.getProperties().getProperty("edge.locationdesc.keyword");
        // loggingModel.setNoteName("a5fcf53f-d67b-41e3-8b78-d6a87c49debb");
        try {
            String notesJson = geTalqNoteDetails(mainUrl, loggingModel.getNoteName());
            if (notesJson == null) {
                logger.info("Note not in Edge.");
                throw new NotesNotFoundException("Note [" + loggingModel.getNoteName() + "] not in Edge.");
            }
            Type listType = new TypeToken<ArrayList<EdgeNote>>() {
            }.getType();
            List<EdgeNote> edgeNoteList = gson.fromJson(notesJson, listType);
              /*  List<EdgeNote> edgeNoteList = new ArrayList<>();
                EdgeNote edgeNote1 = gson.fromJson(notesJson, EdgeNote.class);
                edgeNoteList.add(edgeNote1);*/
            for (EdgeNote edgeNote : edgeNoteList) {
                String locationDesc = edgeNote.getLocationDescription();
                System.out.println("locationDescription is : " + locationDesc);
                if (locationDesc != null && !locationDesc.contains(locationDescKeyword)) {
                    logger.info("Current Location Description :"+locationDesc);
                    String oldNoteGuid = edgeNote.getNoteGuid();
                    String notebookGuid = edgeNote.getEdgeNotebook().getNotebookGuid();
                    JsonObject jsonObject = processEdgeForms(gson.toJson(edgeNote));
                    boolean isProcess = isProcessNoteLayer(jsonObject, emptyTalqAddressGuid,completeLayerGuid,loggingModel);
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
                }else{
                    logger.info("There is no valid location description, its not processed:"+locationDesc);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Error: " + e);
        }

    }

    public boolean isProcessNoteLayer(JsonObject jsonObject, String talqAddressGuid,String completeLayerGuid,LoggingModel loggingModel) {
        JsonArray jsonDictionary = jsonObject.get("dictionary").getAsJsonArray();
        /*if (jsonDictionary.size() > 0) {
            for (JsonElement jsonElement : jsonDictionary) {
                JsonObject dictionaryObject = jsonElement.getAsJsonObject();
                if (dictionaryObject != null) {
                    String groupGuid = dictionaryObject.get("value").getAsString();
                    if (talqAddressGuid.equals(groupGuid))
                        return false;
                }
            }
        }
       */
        jsonObject.addProperty("createdDateTime", System.currentTimeMillis());
        jsonObject.addProperty("noteGuid", UUID.randomUUID().toString());
        jsonObject.remove("dictionary");
        if(loggingModel!=null){
            if(loggingModel.getLayerType().equals("No data ever received")){
                setGroupValue(talqAddressGuid,jsonObject);
            }else if(loggingModel.getLayerType().equals("complete")){
                setGroupValue(completeLayerGuid,jsonObject);
            }
        }

        return true;
    }

    public void setGroupValue(String value,JsonObject notesJson) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("key", "groupGuid");
        jsonObject.addProperty("value", value);
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(jsonObject);
        notesJson.add("dictionary", jsonArray);
    }

    public long getYesterdayDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTimeInMillis();
    }

    public JsonObject processEdgeForms(String edgenoteJson) {
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
        edgeJsonObject.addProperty("createdDateTime", System.currentTimeMillis());
        edgeJsonObject.addProperty("noteGuid", UUID.randomUUID().toString());
        return edgeJsonObject;
    }
}
