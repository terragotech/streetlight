package com.slvinterface.service;

import com.automation.slvtoedge.utils.Utils;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.table.TableUtils;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.slvinterface.dao.TerragoDAO;
import com.slvinterface.entity.CommStatusEntity;
import com.slvinterface.entity.CommStatusNBHistoryEntity;
import com.slvinterface.json.Dictionary;
import com.slvinterface.json.FormValues;
import com.slvinterface.utils.DateOperationUtils;
import com.slvinterface.utils.PropertiesReader;

import java.io.FileReader;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class GlasgowOutageInbound {
    private TerragoDAO terragoDAO;
    private NoteOperations noteOperations;
    private Gson gson;
    public GlasgowOutageInbound() throws Exception {
        terragoDAO = new TerragoDAO();
        noteOperations = new NoteOperations();
        gson = new Gson();
    }
    private void processNoteLayer(JsonObject jsonObject,String layerGUID)
    {

        JsonArray jsonArray = jsonObject.get("dictionary").getAsJsonArray();
        List<Dictionary> dictionaryList = new ArrayList<>();
        for(JsonElement jsonElement : jsonArray){
            JsonObject dicObj = jsonElement.getAsJsonObject();
            Dictionary dictionary = new Dictionary();
            dictionary.setKey(dicObj.get("key").getAsString());
            dictionary.setValue(dicObj.get("value").getAsString());
            dictionaryList.add(dictionary);
        }
        jsonObject.remove("dictionary");
        setGroupValue(dictionaryList,layerGUID,jsonObject);
    }
    private void setGroupValue(List<Dictionary> dictionaryList, String value, JsonObject notesJson) {

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
                if (dictionary.getKey().equals("groupGuid")) {
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
    public String getNoteLayer(JsonObject jsonObject) {
        String result = "";
        JsonArray jsonArray = jsonObject.get("dictionary").getAsJsonArray();
        if(jsonArray != null) {
            for (JsonElement jsonElement : jsonArray) {
                JsonObject dicObj = jsonElement.getAsJsonObject();
                String k = dicObj.get("key").getAsString();
                String v = dicObj.get("value").getAsString();
                if(k.equals("groupGuid")) {
                    result = v;
                }
            }
        }
        return result;
    }
    private void handleNoDataEverReceived(String idOnController,String noteguid){
        String nbNoData = PropertiesReader.getProperties().getProperty("streetlight.edge.nbnodata");
        String lyrNoData = PropertiesReader.getProperties().getProperty("streetlight.edge.layernodata");
        String noteJson = noteOperations.getNoteDetails(noteguid);
        JsonObject edgenoteJson = new JsonParser().parse(noteJson).getAsJsonObject();
        String curNoteBookGUID = getNoteBookGuid(edgenoteJson);
        String curLayerGUID = getNoteLayer(edgenoteJson);
        CommStatusEntity commStatusEntity = new CommStatusEntity();
        String curDate = DateOperationUtils.getCurDate();
        commStatusEntity.setFixtureid(idOnController);
        commStatusEntity.setComstatusdate(curDate);
        commStatusEntity.setComstatus("No data ever received");
        commStatusEntity.setExistinterrago("FOUND");
        terragoDAO.addIfNotExist(commStatusEntity);
        if(!curNoteBookGUID.equals(nbNoData))
        {
            //Do Update
            String updatenoteguid = performLayerChange(noteguid,noteJson,lyrNoData);
            String inbUsrName = PropertiesReader.getProperties().getProperty("inbinterfaceusr");
            terragoDAO.updateUserName(updatenoteguid,inbUsrName);
            performNoteBookChanges(updatenoteguid,nbNoData);
            //Write to ComHistory
            CommStatusNBHistoryEntity commStatusNBHistoryEntity = new CommStatusNBHistoryEntity();
            commStatusNBHistoryEntity.setFixtureid(idOnController);
            commStatusNBHistoryEntity.setPrevnbguid(curNoteBookGUID);
            commStatusNBHistoryEntity.setPrevlrguid(curLayerGUID);
            terragoDAO.addToHistory(commStatusNBHistoryEntity);
        }
    }

    private String getCurrentDate(){
        String pattern = "MM-dd-yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String curDate = simpleDateFormat.format(new Date());
        return curDate;
    }
    private String getNoteBookGuid(JsonObject jsonObject)
    {
        JsonObject edgenotebookJson = jsonObject.getAsJsonObject("edgeNotebook");
        String noteBookGuid = edgenotebookJson.get("notebookGuid").getAsString();
        return noteBookGuid;
    }
    private void handleNoDataMoreThanTwodays(String idOnController,String noteguid){
        String nbTwoDays = PropertiesReader.getProperties().getProperty("streetlight.edge.nbguidmorethantwodays");
        String lyrMoreTwo = PropertiesReader.getProperties().getProperty("streetlight.edge.layermorethantwodays");
        String noteJson = noteOperations.getNoteDetails(noteguid);
        JsonObject edgenoteJson = new JsonParser().parse(noteJson).getAsJsonObject();
        String curNoteBookGUID = getNoteBookGuid(edgenoteJson);
        String curLayerGUID = getNoteLayer(edgenoteJson);
        CommStatusEntity commStatusEntity = new CommStatusEntity();
        String curDate = DateOperationUtils.getCurDate();
        commStatusEntity.setFixtureid(idOnController);
        commStatusEntity.setComstatusdate(curDate);
        commStatusEntity.setComstatus("No data for more than 48 hours");
        commStatusEntity.setExistinterrago("FOUND");
        terragoDAO.addIfNotExist(commStatusEntity);

        if(!curNoteBookGUID.equals(nbTwoDays))
        {
            boolean staToday = terragoDAO.getComStatusResultOnDay(idOnController,
                    DateOperationUtils.getCurDate());
            boolean staprev1 = terragoDAO.getComStatusResultOnDay(idOnController,
                    DateOperationUtils.getDateBeforeDay(1));
            boolean staprev2 = terragoDAO.getComStatusResultOnDay(idOnController,
                    DateOperationUtils.getDateBeforeDay(2));
            boolean staprev3 = terragoDAO.getComStatusResultOnDay(idOnController,
                    DateOperationUtils.getDateBeforeDay(3));
            boolean staprev4 = terragoDAO.getComStatusResultOnDay(idOnController,
                    DateOperationUtils.getDateBeforeDay(4));
            if(staToday){
            //if(staToday && staprev1 && staprev2 && staprev3 && staprev4) {
                //Do Update
                String updatenoteguid = performLayerChange(noteguid,noteJson,lyrMoreTwo);
                String inbUsrName = PropertiesReader.getProperties().getProperty("inbinterfaceusr");
                terragoDAO.updateUserName(updatenoteguid,inbUsrName);
                performNoteBookChanges(updatenoteguid,nbTwoDays);
                //Write to ComHistory
                CommStatusNBHistoryEntity commStatusNBHistoryEntity = new CommStatusNBHistoryEntity();
                commStatusNBHistoryEntity.setFixtureid(idOnController);
                commStatusNBHistoryEntity.setPrevnbguid(curNoteBookGUID);
                commStatusNBHistoryEntity.setPrevlrguid(curLayerGUID);
                terragoDAO.addToHistory(commStatusNBHistoryEntity);
            }
        }

    }
    public String performLayerChange(String noteguid,String noteJson,String layerGUI)
    {
        JsonObject edgenoteJson = new JsonParser().parse(noteJson).getAsJsonObject();
        JsonArray serverForms = edgenoteJson.get("formData").getAsJsonArray();
        int size = serverForms.size();
        for (int i = 0; i < size; i++) {
            JsonObject serverEdgeForm = serverForms.get(i).getAsJsonObject();
            String formDefJson = serverEdgeForm.get("formDef").getAsString();
            String formTemplate = serverEdgeForm.get("formTemplateGuid").getAsString();
            List<FormValues> formComponents = gson.fromJson(formDefJson, new TypeToken<List<FormValues>>() {
            }.getType());
            serverEdgeForm.add("formDef", gson.toJsonTree(formComponents));
            serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
        }
        edgenoteJson.add("formData", serverForms);
        edgenoteJson.addProperty("createdDateTime", System.currentTimeMillis());

        JsonObject edgenotebookJson = edgenoteJson.getAsJsonObject("edgeNotebook");
        String noteBookGuid = edgenotebookJson.get("notebookGuid").getAsString();
        processNoteLayer(edgenoteJson,layerGUI);

        String responseNoteGUID = noteOperations.updateNoteDetails(edgenoteJson.toString(),noteguid,noteBookGuid);
        return responseNoteGUID;
    }
    public void performNoteBookChanges(String noteGUID,String notebookGUID){
        noteOperations.moveNoteToNoteBook(noteGUID,notebookGUID);
    }
    public void startProcessing(String[] r){

        try {
            TableUtils.createTableIfNotExists(terragoDAO.getConnectionSource(), CommStatusEntity.class);
            TableUtils.createTableIfNotExists(terragoDAO.getConnectionSource(),CommStatusNBHistoryEntity.class);
        }
        catch (SQLException e)
        {
            //e.printStackTrace();
        }
        //noteOperations.moveNoteToNoteBook("99eb5e7b-c30e-4c5c-9503-6ea65092260a","e491651f-62c7-4ac4-8370-9b1033045492");


        /*String slvDataPath = "./slv_data_"+ Utils.getDateTime(new Date().getTime())+".csv";

        ImportSLVData2 importSLVData2 = new ImportSLVData2();
        importSLVData2.startImport(slvDataPath);
        System.exit(0);*/
        String slvDataPath = r[0];
        //String slvDataPath = "./slv_data_2020_10_14_19_12.csv";
        try {
            String curDate = DateOperationUtils.getCurDate();
            CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
            FileReader filereader = new FileReader(slvDataPath);
            CSVReader csvReader = new CSVReaderBuilder(filereader).withCSVParser(parser).build();
            String[] nextRecord;
            String formTemplateToProcess = PropertiesReader.getProperties().getProperty("streetlight.edge.formtemplate.guid");
            String keyField = PropertiesReader.getProperties().getProperty("streetlight.edge.formtemplate.keyfield");
            while ((nextRecord = csvReader.readNext()) != null) {
                String idOnController = nextRecord[0];
                String commStatus = nextRecord[1];
                if(commStatus.equals("No data for more than 48 hours") ||
                        commStatus.equals("No data ever received"))
                {
                    String noteguid = getNoteGUID(idOnController,keyField,formTemplateToProcess);
                    if(noteguid != null)
                    {
                        if(!noteguid.equals(""))
                        {
                            if(commStatus.equals("No data for more than 48 hours"))
                            {
                                handleNoDataMoreThanTwodays(idOnController,noteguid);
                            }
                            else if(commStatus.equals("No data ever received"))
                            {
                                handleNoDataEverReceived(idOnController,noteguid);
                            }
                        }
                    }
                }
                else
                {
                    //Check in his
                    CommStatusNBHistoryEntity commStatusNBHistoryEntity = terragoDAO.getPreviousNoteState(idOnController);
                    if(commStatusNBHistoryEntity != null)
                    {
                        String noteguid = getNoteGUID(idOnController,keyField,formTemplateToProcess);
                        if(noteguid != null) {
                            if (!noteguid.equals("")) {
                                String preLayerGUID = commStatusNBHistoryEntity.getPrevlrguid();
                                String notejson = noteOperations.getNoteDetails(noteguid);
                                String updatenoteguid = performLayerChange(noteguid,notejson,preLayerGUID);
                                String inbUsrName = PropertiesReader.getProperties().getProperty("inbinterfaceusr");
                                terragoDAO.updateUserName(updatenoteguid,inbUsrName);
                                performNoteBookChanges(updatenoteguid,commStatusNBHistoryEntity.getPrevnbguid());
                                terragoDAO.removeFromHistory(idOnController);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    private String getNoteGUID(String idoncontroller, String formfield,String formTemplateGUID) {
        String noteGUID = "";
        noteGUID = terragoDAO.getCurrentNoteGUIDFromIDOnController(idoncontroller,formfield,formTemplateGUID);
        return noteGUID;
    }

}
