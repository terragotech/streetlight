package com.slvinterface.main;

import com.slvinterface.service.*;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class SLVApp {

    private static final Logger logger = Logger.getLogger(SLVApp.class);
    public static void main(String[] r){


        try{

            GlasgowOutageInbound glasgowOutageInbound = new GlasgowOutageInbound();
            glasgowOutageInbound.startProcessing(r);

        }catch (Exception e){
            e.printStackTrace();
        }


    }
}
