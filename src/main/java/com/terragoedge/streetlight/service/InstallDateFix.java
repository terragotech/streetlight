package com.terragoedge.streetlight.service;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class InstallDateFix extends AbstractProcessor {
    final Logger logger = Logger.getLogger(InstallDateFix.class);

    public void run(){
        try{
            String data = null;
            BufferedReader  fis = new BufferedReader(new FileReader("C:\\edge2slv\\installdatafix\\data.csv"));
             while((data =  fis.readLine()) != null){
                 try{
                     List<Object> paramsList = new ArrayList<>();
                     String[] res = data.split(",");
                     paramsList.add("idOnController=" + res[0]);
                     paramsList.add("controllerStrId=TalqBridge@TB1009802308");


                     addStreetLightData("install.date", dateFormat(Long.valueOf(res[1])), paramsList);
                     addStreetLightData("installStatus", "Installed", paramsList);
                     int errorCode = setDeviceValues(paramsList);
                     logger.info("Response Code"+errorCode);
                 }catch (Exception e){
                     e.printStackTrace();
                 }
             }

        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
