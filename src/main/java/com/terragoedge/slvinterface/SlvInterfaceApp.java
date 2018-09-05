package com.terragoedge.slvinterface;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.automation.service.ExceptionReportAutomationService;
import com.terragoedge.automation.service.ReportAutomationService;
import com.terragoedge.slvinterface.json.slvInterface.ConfigurationJson;
import com.terragoedge.slvinterface.service.AbstractSlvService;
import com.terragoedge.slvinterface.service.AmerescoReportService;
import com.terragoedge.slvinterface.service.LoadForAssignmentService;
import com.terragoedge.slvinterface.service.SlvInterfaceService;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SlvInterfaceApp extends AbstractSlvService {
    public static List<ConfigurationJson> configurationJsons = new ArrayList<>();

    public static void main(String[] args) {
        // AmerescoReportService amerescoReportService = new AmerescoReportService();
        // amerescoReportService.start();
        /*ReportAutomationService reportAutomationService = new ReportAutomationService();
        reportAutomationService.run();*/
        /*ExceptionReportAutomationService exceptionReportAutomationService = new ExceptionReportAutomationService();
        exceptionReportAutomationService.start();*/
        LoadForAssignmentService loadForAssignmentService = new LoadForAssignmentService();
        loadForAssignmentService.start();
/*
        while (true) {
            try {
                SlvInterfaceService slvInterfaceService = new SlvInterfaceService();
                slvInterfaceService.start();
                Thread.sleep(60000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
*/
       /* try {
            ApplicationContext context = new ClassPathXmlApplicationContext("schedulepropertiesconfig.xml");
            System.out.println("tested");
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    private static void getFormConfiguration() {
        Gson gson = new Gson();
        Properties properties = PropertiesReader.getProperties();
        String formDetails = properties.getProperty("formDetails");
        configurationJsons = gson.fromJson(formDetails, new TypeToken<List<ConfigurationJson>>() {
        }.getType());

    }
}
