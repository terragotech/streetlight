package com.terragoedge.slvinterface.service;

import com.google.gson.*;
import com.terragoedge.slvinterface.dao.ConnectionDAO;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetails;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;
import java.util.Properties;

public class TalkAddressService {
    private Gson gson = null;
    private JsonParser jsonParser = null;
    private Properties properties = null;
    private ConnectionDAO connectionDAO=null;
    private SlvRestService slvRestService = null;

    public TalkAddressService() {
        gson = new Gson();
        jsonParser = new JsonParser();
        properties = PropertiesReader.getProperties();
        connectionDAO = ConnectionDAO.INSTANCE;
        slvRestService = new SlvRestService();
    }

    public void run() {
        System.out.println("talq started");
        String slvBaseUrl = properties.getProperty("streetlight.slv.url.main");
        String talqAddressApi = properties.getProperty("streetlight.slv.url.gettalqaddress");
        System.out.println(slvBaseUrl + talqAddressApi);
        List<SlvSyncDetails> unSyncedTalqAddress = connectionDAO.getUnSyncedTalqaddress();
        System.out.println("un synced TalkAddressSize :" + unSyncedTalqAddress.size());
        if (unSyncedTalqAddress.size() > 0) {
            ResponseEntity<String> responseEntity = slvRestService.getRequest(slvBaseUrl + talqAddressApi, true, null);
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
                        SlvSyncDetails slvSyncDetails = connectionDAO.getFixtureSyncDetails(idOnController);
                        if (slvSyncDetails != null) {
                            slvSyncDetails.setTalcAddress(talqAddress);
                            slvSyncDetails.setTalcAddressDateTime(new Date().getTime());
                            connectionDAO.updateSlvSyncdetails(slvSyncDetails);
                            System.out.println("Updated : " + idOnController + " - " + talqAddress);
                        }
                    }

                }
            }
        }

    }

}
