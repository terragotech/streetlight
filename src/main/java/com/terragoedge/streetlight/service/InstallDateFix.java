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
            System.out.println("Started");
            BufferedReader  fis = new BufferedReader(new FileReader("./resources/emptyqrscan.csv"));
             while((data =  fis.readLine()) != null){
                 try{
                     List<Object> paramsList = new ArrayList<>();
                     String[] res = data.split(",");
                     System.out.println("IdonController : "+res[0]);
                     paramsList.add("idOnController=" + res[0]);
                     paramsList.add("controllerStrId=TalqBridge@TB1009802308");
                     addStreetLightData("cslp.lum.install.date", "", paramsList);
                     addStreetLightData("luminaire.installdate","", paramsList);
                     addStreetLightData("installStatus", "Verified", paramsList);
                     //  addStreetLightData("DimmingGroupName", res[1], paramsList);
                    int errorCode = setDeviceValues(paramsList);

                    System.out.println("status "+res[0]+" - "+errorCode);
                    // logger.info("Response Code"+errorCode);
                 }catch (Exception e){
                     e.printStackTrace();
                 }
             }

        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
