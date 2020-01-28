package com.terragoedge.streetlight.json.model;

public class CronModel {
    private String reportType;
    private String cronExpression;
    private String filePath;
    private boolean iscustomdate;
    private long startingdate;
    private long endingdate;

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isIscustomdate() {
        return iscustomdate;
    }

    public void setIscustomdate(boolean iscustomdate) {
        this.iscustomdate = iscustomdate;
    }

    public long getStartingdate() {
        return startingdate;
    }

    public void setStartingdate(long startingdate) {
        this.startingdate = startingdate;
    }

    public long getEndingdate() {
        return endingdate;
    }

    public void setEndingdate(long endingdate) {
        this.endingdate = endingdate;
    }

    @Override
    public String toString() {
        return "CronModel{" +
                "reportType='" + reportType + '\'' +
                ", cronExpression='" + cronExpression + '\'' +
                ", filePath='" + filePath + '\'' +
                ", iscustomdate=" + iscustomdate +
                ", startingdate=" + startingdate +
                ", endingdate=" + endingdate +
                '}';
    }
}
