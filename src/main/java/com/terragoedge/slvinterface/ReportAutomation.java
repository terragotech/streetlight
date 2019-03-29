package com.terragoedge.slvinterface;

import com.terragoedge.slvinterface.service.InstallationReportService;

public class ReportAutomation {
    public static void main(String[] args) {
        InstallationReportService installationReportService = new InstallationReportService();
        installationReportService.root();
    }
}
