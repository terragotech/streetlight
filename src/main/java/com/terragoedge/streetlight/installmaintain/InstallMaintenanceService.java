package com.terragoedge.streetlight.installmaintain;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.installmaintain.json.Config;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.util.List;

public class InstallMaintenanceService {

    private Logger logger = Logger.getLogger(InstallMaintenanceService.class);
    private Gson gson = new Gson();
    public List<Config> getConfigList(){
       String data = readFile();
       return  gson.fromJson(data,new TypeToken<List<Config>>(){}.getType());
    }

    private String readFile(){
        FileInputStream fis = null;
        try{
            String config = PropertiesReader.getProperties().getProperty("config");
             fis = new FileInputStream(config);
            String data =  IOUtils.toString(fis);
            return data;
        }catch (Exception e){
            logger.error("error in getting configs: "+e.getMessage());
            e.printStackTrace();
        }finally {
            if(fis != null){
                try {
                    fis.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
       return null;
    }
}
