package com.terragoedge.slvinterface;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.slvinterface.json.slvInterface.ConfigurationJson;
import com.terragoedge.slvinterface.service.AbstractSlvService;
import com.terragoedge.slvinterface.service.SlvInterfaceService;
import com.terragoedge.slvinterface.utils.PropertiesReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SlvInterfaceApp extends AbstractSlvService {
    public static List<ConfigurationJson> configurationJsons = new ArrayList<>();
    public static void main(String[] args) {
       // getFormConfiguration();

        while (true) {
            try{
                SlvInterfaceService slvInterfaceService = new SlvInterfaceService();
                slvInterfaceService.start();
                Thread.sleep(1000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
   }
    private static void getFormConfiguration(){
        Gson gson = new Gson();
        Properties properties = PropertiesReader.getProperties();
        String formDetails = properties.getProperty("formDetails");
        configurationJsons = gson.fromJson(formDetails, new TypeToken<List<ConfigurationJson>>(){}.getType());

    }
}
