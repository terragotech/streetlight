package com.terragoedge.streetlight.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.dao.StreetlightDao;
import com.terragoedge.streetlight.exception.NotesNotFoundException;
import com.terragoedge.streetlight.logging.LoggingModel;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TalkAddressService extends AbstractService implements Runnable {
    private Gson gson = null;
    private JsonParser jsonParser = null;
    private Properties properties = null;
    private RestService restService = null;
    private StreetlightDao streetlightDao = null;
    private Logger logger = Logger.getLogger(TalkAddressService.class);

    public TalkAddressService() {
        gson = new Gson();
        jsonParser = new JsonParser();
        properties = PropertiesReader.getProperties();
        restService = new RestService();
        streetlightDao = new StreetlightDao();
    }

    @Override
    public void run() {
        System.out.println("talq started");
        String slvBaseUrl = properties.getProperty("streetlight.slv.url.main");
        String talqAddressApi = properties.getProperty("streetlight.slv.url.gettalqaddress");
        System.out.println(slvBaseUrl + talqAddressApi);
        List<LoggingModel> unSyncedTalqAddress = streetlightDao.getUnSyncedTalqaddress();
        System.out.println("un synced TalkAddressSize :" + unSyncedTalqAddress.size());
        if (unSyncedTalqAddress.size() > 0) {
            ResponseEntity<String> responseEntity = restService.getRequest(slvBaseUrl + talqAddressApi, true, null);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String response = responseEntity.getBody();
                //test
           /* String response=null;
            try {
               // FileInputStream fis = new FileInputStream("./resources/sampletal.txt");
                 FileInputStream fis = new FileInputStream("./src/main/resources/sampletal.txt");
                 response = IOUtils.toString(fis);
            }catch (Exception e){
                e.printStackTrace();
            }
           // end
          */
                JsonObject jsonObject = (JsonObject) jsonParser.parse(response);
                JsonArray deviceValuesAsArray = jsonObject.get("values").getAsJsonArray();
                for (JsonElement jsonElement : deviceValuesAsArray) {
                    JsonArray slvDetails = jsonElement.getAsJsonArray();
                    if (slvDetails.size() == 2) {
                        String idOnController = slvDetails.get(0).getAsString();
                        if (slvDetails.get(1).isJsonNull()) {
                            continue;
                        }
                        String talqAddress = slvDetails.get(1).getAsString();
                        LoggingModel loggingModel = streetlightDao.getLoggingModel(idOnController);
                        if (loggingModel != null) {
                            loggingModel.setTalqAddress(talqAddress);
                            loggingModel.setTalqCreatedTime(new Date().getTime());
                            streetlightDao.updateTalqAddress(idOnController, talqAddress);
                            System.out.println("Updated : " + idOnController + " - " + talqAddress);
                        }
                    }

                }
            }
        }

    }

    public void getTalqAddress() {
        String emptyTalqAddressGuid = PropertiesReader.getProperties().getProperty("empty.talkaddress.layerguid");
        String mainUrl = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        List<LoggingModel> unSyncedTalqAddress = streetlightDao.getTalqaddressDetails(getYesterdayDate());
        for (LoggingModel loggingModel : unSyncedTalqAddress) {
            logger.info(loggingModel.getNoteName());
            loggingModel.setNoteName("a5fcf53f-d67b-41e3-8b78-d6a87c49debb");
            try {
                String notesJson = geTalqNoteDetails(mainUrl, loggingModel.getNoteName());
                if (notesJson == null) {
                    logger.info("Note not in Edge.");
                    throw new NotesNotFoundException("Note [" + loggingModel.getNoteName() + "] not in Edge.");
                }
                Type listType = new TypeToken<ArrayList<EdgeNote>>() {
                }.getType();
                // List<EdgeNote> edgeNoteList = gson.fromJson(notesJson, listType);
                List<EdgeNote> edgeNoteList = new ArrayList<>();
                EdgeNote edgeNote1 = gson.fromJson(notesJson, EdgeNote.class);
                edgeNoteList.add(edgeNote1);
                for (EdgeNote edgeNote : edgeNoteList) {
                    String oldNoteGuid = edgeNote.getNoteGuid();
                    String notebookGuid = edgeNote.getEdgeNotebook().getNotebookGuid();
                    JsonObject jsonObject = processEdgeForms(gson.toJson(edgeNote));
                    boolean isProcess = isProcessNoteLayer(jsonObject, emptyTalqAddressGuid);
                    if (isProcess) {
                        ResponseEntity<String> responseEntity = updateNoteDetails(jsonObject.toString(), oldNoteGuid, notebookGuid, mainUrl);
                        logger.info("edgenote update to server: " + responseEntity.getBody());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isProcessNoteLayer(JsonObject jsonObject, String talqAddressGuid) {
        JsonArray jsonDictionary = jsonObject.get("dictionary").getAsJsonArray();
        if (jsonDictionary.size() > 0) {
            for (JsonElement jsonElement : jsonDictionary) {
                JsonObject dictionaryObject = jsonElement.getAsJsonObject();
                if (dictionaryObject != null) {
                    String groupGuid = dictionaryObject.get("value").getAsString();
                    if (talqAddressGuid.equals(groupGuid))
                        return false;
                }
            }
        }
        jsonObject.addProperty("createdDateTime", System.currentTimeMillis());
        jsonObject.addProperty("noteGuid", UUID.randomUUID().toString());
        jsonObject.remove("dictionary");
        setGroupValue(talqAddressGuid, jsonObject);
        return true;
    }

    public void setGroupValue(String value, JsonObject notesJson) {
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
