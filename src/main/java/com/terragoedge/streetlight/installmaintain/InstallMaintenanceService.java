package com.terragoedge.streetlight.installmaintain;

import com.google.gson.Gson;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.installmaintain.json.Config;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.util.List;

public class InstallMaintenanceService {

    private Gson gson;
    private List<Config> getConfigList(){

       String data = readFile();

    }

    private String readFile(){
        FileInputStream fis = null;
        try{
            String config = PropertiesReader.getProperties().getProperty("config");
             fis = new FileInputStream(config);
            String data =  IOUtils.toString(fis);
            return data;
        }catch (Exception e){

        }finally {

        }
       return null;
    }
}
