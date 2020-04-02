package com.terragoedge.slvinterface;

import com.terragoedge.slvinterface.service.OutageReportService;

public class OutageReport {
    public static void main(String []args)
    {
        try {
            OutageReportService outageReportService = new OutageReportService();
            outageReportService.generateOutageReport();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
