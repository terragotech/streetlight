package com.slvinterface.main;

import com.slvinterface.service.EdgeRestService;
import com.slvinterface.service.StreetLightCanadaService;
import org.apache.log4j.Logger;

import java.util.TimeZone;

public class SLVInterfaceApp {
    final static  Logger logger = Logger.getLogger(SLVInterfaceApp.class);
    public static void main(String[] args) {
        try{
            while(true){
                try {
                    StreetLightCanadaService streetLightCanadaService = new StreetLightCanadaService();
                    streetLightCanadaService.run();
                } catch (Exception e) {
                    logger.error("Error",e);
                }

                try{
                    Thread.sleep(10000);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }
}
