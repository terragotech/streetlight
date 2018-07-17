package com.terragoedge.streetlight;

import com.terragoedge.streetlight.service.FailureReportService;
import com.terragoedge.streetlight.service.ReportFixService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class StreetlightApp {


    public static void main(String[] args) {
        FailureReportService failureReportService = new FailureReportService();
        failureReportService.run();
     /*   ReportFixService reportFixService = new ReportFixService();
        reportFixService.run();*/
    }

}
