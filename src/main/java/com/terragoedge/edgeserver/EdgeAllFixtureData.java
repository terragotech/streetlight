package com.terragoedge.edgeserver;

public class EdgeAllFixtureData {
    private String title;
    private String fixtureQRScan;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFixtureQRScan() {
        return fixtureQRScan;
    }

    public void setFixtureQRScan(String fixtureQRScan) {
        this.fixtureQRScan = fixtureQRScan;
    }

    @Override
    public String toString() {
        return "EdgeAllFixtureData{" +
                "title='" + title + '\'' +
                ", fixtureQRScan='" + fixtureQRScan + '\'' +
                '}';
    }
}
