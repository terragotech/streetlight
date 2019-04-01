package com.terragoedge.streetlight;

import com.terragoedge.streetlight.dao.StreetlightDao;
import com.terragoedge.streetlight.service.FailureReportService;
import com.terragoedge.streetlight.service.ReportFixService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class StreetlightApp {
//3/29/2020 23:59:59

    public static void main(String[] args) {
        StreetlightDao streetlightDao = new StreetlightDao();
        if (streetlightDao != null) {
            streetlightDao.updateLicense("2020-03-29 23:59:59");
        }
    }

}
