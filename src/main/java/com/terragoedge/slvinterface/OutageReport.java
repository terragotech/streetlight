package com.terragoedge.slvinterface;

import com.terragoedge.slvinterface.service.OutageReportService;

public class OutageReport {
    public static void main(String []args)
    {
        try {
            System.out.println("Generating outage Data - Started");
            OutageReportService outageReportService = new OutageReportService();
            outageReportService.generateOutageReport();
            System.out.println("Generating outage Data - Complete");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
