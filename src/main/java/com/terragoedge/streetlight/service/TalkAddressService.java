package com.terragoedge.streetlight.service;

import com.google.gson.*;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.dao.StreetlightDao;
import com.terragoedge.streetlight.logging.LoggingModel;
import org.apache.commons.io.IOUtils;
import org.springframework.http.ResponseEntity;

import java.io.FileInputStream;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class TalkAddressService implements Runnable {
    private Gson gson = null;
    private JsonParser jsonParser = null;
    private Properties properties = null;
    private RestService restService = null;
    private StreetlightDao streetlightDao = null;

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
    }

}
