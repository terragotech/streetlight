package com.terragoedge.slvinterface;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.macaddress.slvtoedge.service.MacAddressService;
import com.report.automation.service.ReportAutomationService;
import com.terragoedge.slvinterface.json.slvInterface.ConfigurationJson;
import com.terragoedge.slvinterface.service.*;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SlvInterfaceApp {

    final static  Logger logger = Logger.getLogger(SlvInterfaceApp.class);
    public static void main(String[] args) {
        SlvInterfaceService slvInterfaceService = new KingCityEdgeInterface();
      while (true) {
            try {
                slvInterfaceService.loadDevices();
                while (true) {
                    slvInterfaceService.start();
                    Thread.sleep(60000);
                }
            } catch (Exception e) {
                logger.error("Error in main",e);
            }
        }

    }

}
//resynced path