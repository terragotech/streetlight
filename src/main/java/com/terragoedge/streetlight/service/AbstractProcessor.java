package com.terragoedge.streetlight.service;

import com.google.gson.*;
import com.terragoedge.streetlight.PropertiesReader;
import org.apache.log4j.Logger;

import java.util.Properties;

public abstract class AbstractProcessor {

    final Logger logger = Logger.getLogger(AbstractProcessor.class);

    RestService restService = null;
    Properties properties = null;
    Gson gson = null;
    JsonParser jsonParser = null;


    public AbstractProcessor() {
        this.restService = new RestService();
        this.properties = PropertiesReader.getProperties();
        this.gson = new Gson();
        this.jsonParser = new JsonParser();
    }

    public int processDeviceJson(String deviceJson) {
        JsonObject jsonObject = new JsonParser().parse(deviceJson).getAsJsonObject();
        logger.info("Device request json:" + gson.toJson(jsonObject));
        JsonArray arr = jsonObject.getAsJsonArray("value");
        for (int i = 0; i < arr.size(); i++) {
            int id = arr.get(i).getAsJsonObject().get("id").getAsInt();
            logger.info("Device id value :" + id);
            System.out.println(id);
            return id;
        }
        return 0;
    }
}
