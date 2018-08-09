package com.terragoedge.streetlight.service;

import com.google.gson.*;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.dao.StreetlightDao;
import com.terragoedge.streetlight.logging.LoggingModel;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
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
        try{
            String mainUrl = "";
            List<LoggingModel> unSyncedTalqAddress = streetlightDao.getTalqaddressDetails();//TODO
            List<String> noteGuidList = new ArrayList();
            if (unSyncedTalqAddress.size() > 0) {
                for(LoggingModel talqAddress : unSyncedTalqAddress){
                    Date syncDate = new Date(talqAddress.getLastSyncTime());
                    Date currentDate = new Date(System.currentTimeMillis());
                    int timeGap = getTimeDiff(currentDate,syncDate);
                    if(timeGap > 3){
                        noteGuidList.add(talqAddress.getProcessedNoteId());
                    }
                }
            }
            for(String noteGuid : noteGuidList){
                logger.info(noteGuid);
                ResponseEntity<String> responseEntity = getNoteDetails(mainUrl, noteGuid);
                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    // Get Response String
                    String notesData = responseEntity.getBody();
                    JsonObject jsonObject = (JsonObject) jsonParser.parse(notesData);
                    String currntNoteGuid = jsonObject.get("noteguid").getAsString();//TODO
                    String notebookGuid = jsonObject.get("notebookguid").getAsString();//TODO
                    jsonObject.addProperty("layer", "No data ever received");//TODO
                    updateNoteDetails(jsonObject.toString(), currntNoteGuid, notebookGuid, mainUrl);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private int getTimeDiff(Date dateOne, Date dateTwo) {
        String diff = "";
        long timeDiff = Math.abs(dateOne.getTime() - dateTwo.getTime());
        diff = String.format("%d hour(s) %d min(s)", TimeUnit.MILLISECONDS.toHours(timeDiff),
                TimeUnit.MILLISECONDS.toMinutes(timeDiff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeDiff)));
        //diff = String.format("%d hour(s)", TimeUnit.MILLISECONDS.toHours(timeDiff));
        String timeGapArray[] = diff.split(" ");
        return Integer.parseInt(timeGapArray[0]);

    }
}
