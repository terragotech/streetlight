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
    ExecutorService executor =null;

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
        int threadCount = Integer.parseInt(PropertiesReader.getProperties().getProperty("edge.threadcount"));
        System.out.println("ThreadCount: " + threadCount);
        executor= Executors.newFixedThreadPool(threadCount);
        List<LoggingModel> unSyncedTalqAddress = streetlightDao.getTalqaddressDetails(getYesterdayDate());
        for (LoggingModel loggingModel : unSyncedTalqAddress) {
            logger.info(loggingModel.getNoteName());
            Runnable processTask = new TalqAddressTask(loggingModel);
            executor.execute(processTask);
        }
        executor.shutdown();
        System.out.println("All thread Finished");
    }

    public long getYesterdayDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTimeInMillis();
    }

}
