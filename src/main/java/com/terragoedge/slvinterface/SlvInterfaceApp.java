package com.terragoedge.slvinterface;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.macaddress.slvtoedge.service.MacAddressService;
import com.report.automation.service.ReportAutomationService;
import com.terragoedge.slvinterface.json.slvInterface.ConfigurationJson;
import com.terragoedge.slvinterface.service.AbstractSlvService;
import com.terragoedge.slvinterface.service.CanadaEdgeInterface;
import com.terragoedge.slvinterface.service.KingCityEdgeInterface;
import com.terragoedge.slvinterface.service.SlvInterfaceService;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SlvInterfaceApp {

    public static void main(String[] args) {


      while (true) {
            try {
                SlvInterfaceService slvInterfaceService = new CanadaEdgeInterface();
                slvInterfaceService.start();
                Thread.sleep(60000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


}
