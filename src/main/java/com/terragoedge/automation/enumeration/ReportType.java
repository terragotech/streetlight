package com.terragoedge.automation.enumeration;

public enum ReportType {
    MAC_VALIDATION(0), REPLACE(1);
    private int reportId;

    private ReportType(int reportId) {
        this.reportId = reportId;
    }

    public int getReportType() {
        return reportId;
    }
}
