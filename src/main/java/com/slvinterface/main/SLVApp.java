package com.slvinterface.main;

import com.slvinterface.service.*;
import org.apache.log4j.Logger;

public class SLVApp {

    private static final Logger logger = Logger.getLogger(SLVApp.class);
    public static void main(String[] r){
        try{
            SLVInterfaceService slvInterfaceService = new SurreySLVInterface();
            while(true){
                try{
                    SLVRestTemplate.INSTANCE.refreshToken();
                    slvInterfaceService.run();
                    Thread.sleep(60000);
                }catch (Exception e){
                    logger.error("Error in main",e);
                }

            }


        }catch (Exception e){
            e.printStackTrace();
        }


    }
}
