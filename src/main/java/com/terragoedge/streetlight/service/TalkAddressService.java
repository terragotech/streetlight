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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TalkAddressService extends AbstractService implements Runnable {
    private Gson gson = null;
    private JsonParser jsonParser = null;
    private Properties properties = null;
    private RestService restService = null;
    private StreetlightDao streetlightDao = null;
    private Logger logger = Logger.getLogger(TalkAddressService.class);
    ExecutorService executor = null;
    private TalqAddressTask talqAddressTask = null;

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
        logger.info("start slvtalqInterface");
        String slvBaseUrl = properties.getProperty("streetlight.slv.url.main");
        String talqAddressApi = properties.getProperty("streetlight.slv.url.gettalqaddress");
        System.out.println(slvBaseUrl + talqAddressApi);
        List<LoggingModel> unSyncedTalqAddress = streetlightDao.getUnSyncedTalqaddress();
        logger.info("unSyncedTalqAddress size :" + unSyncedTalqAddress.size());
        logger.info("Request Url :" + slvBaseUrl + talqAddressApi);
        if (unSyncedTalqAddress.size() > 0) {
            ResponseEntity<String> responseEntity = restService.getRequest(slvBaseUrl + talqAddressApi, true, null);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String response = responseEntity.getBody();
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
                            logger.info("updateTalqAddress :" + idOnController + " - " + talqAddress);
                            System.out.println("Updated : " + idOnController + " - " + talqAddress);
                        }
                    }

                }
            }
        }

    }

    public void getTalqAddress() {
        logger.info("start terragotalqudate");
        int threadCount = Integer.parseInt(PropertiesReader.getProperties().getProperty("edge.threadcount"));
        System.out.println("ThreadCount: " + threadCount);
        logger.info("ThreadCount :" + threadCount);
        executor = Executors.newFixedThreadPool(threadCount);
        //  List<LoggingModel> unSyncedTalqAddress = streetlightDao.getTalqaddressDetails(getYesterdayDate());
        List<LoggingModel> unSyncedTalqAddress = getUnsyncedTalqAddress();
        logger.info("------------ Total records ------------------");
        logger.info(unSyncedTalqAddress.size());
        logger.info("------------ end ------------------");
        System.out.println("unSyncedTalqAddress: " + unSyncedTalqAddress);
        for (LoggingModel loggingModel : unSyncedTalqAddress) {
            System.out.println("Process NoteName" + loggingModel.getNoteName());
            logger.info("Process NoteName" + loggingModel.getNoteName());
            Runnable processTask = new TalqAddressTask(loggingModel);
            executor.execute(processTask);
        }
        executor.shutdown();
        System.out.println("All thread Finished");
    }

    public void processCompleteLayer() {
        List<LoggingModel> unSyncedTalqAddress = streetlightDao.getProcessedTalqAddress(getYesterdayDate());
        for (LoggingModel loggingModel : unSyncedTalqAddress) {
            String completeLayerGuid = PropertiesReader.getProperties().getProperty("talkaddress.complete.layerguid");
            String mainUrl = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
            String locationDescKeyword = PropertiesReader.getProperties().getProperty("edge.locationdesc.keyword");
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
                    String locationDesc = edgeNote.getLocationDescription();
                    System.out.println("locationDescription is : " + locationDesc);
                    if (locationDesc != null && !locationDesc.contains(locationDescKeyword)) {
                        String oldNoteGuid = edgeNote.getNoteGuid();
                        String notebookGuid = edgeNote.getEdgeNotebook().getNotebookGuid();
                        JsonObject jsonObject = talqAddressTask.processEdgeForms(gson.toJson(edgeNote));
                        if (true) {
                            ResponseEntity<String> responseEntity = updateNoteDetails(jsonObject.toString(), oldNoteGuid, notebookGuid, mainUrl);
                            streetlightDao.updateTalqGuid(edgeNote.getTitle(), responseEntity.getBody());
                            logger.info("edgenote update to server: " + responseEntity.getBody());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public long getYesterdayDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTimeInMillis();
    }

    public List<LoggingModel> getUnsyncedTalqAddress() {
        List<LoggingModel> loggingModelList = new ArrayList<>();
        BufferedReader fis = null;
        try {
            String data = null;
            System.out.println("Started");

            fis = new BufferedReader(new FileReader("./data/emptytalq.txt"));
            while ((data = fis.readLine()) != null) {
                LoggingModel loggingModel = new LoggingModel();
                try {
                    List<Object> paramsList = new ArrayList<>();
                    String[] res = data.split(",");
                    loggingModel.setNoteName(res[0]);
                    loggingModel.setMacAddress(res[1]);
                    loggingModel.setLayerType(res[2]);
                    logger.info("------------------------------process Notes------------------");
                    logger.info(res[0] + " - " + res[1]);
                    logger.info("--------------------------------end----------------------------");
                    System.out.println("IdonController-MAC : " + res[0] + " - " + res[1]);
                    loggingModelList.add(loggingModel);
                    // logger.info("Response Code"+errorCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(fis != null){
                try{
                    fis.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return loggingModelList;
    }
}
