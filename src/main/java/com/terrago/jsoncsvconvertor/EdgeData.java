package com.terrago.jsoncsvconvertor;

public class EdgeData {
    private String title;
    private long slvCslpNodeInstallDate;
    private long slvCslpLumInstallDate;
    private long slvInstallDate;
    private long slvLumInstallDate;

    private long edgeCslpNodeInstallDate;
    private long edgeCslpLumInstallDate;
    private long edgeInstallDate;
    private long edgeLumInstallDate;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getSlvCslpNodeInstallDate() {
        return slvCslpNodeInstallDate;
    }

    public void setSlvCslpNodeInstallDate(long slvCslpNodeInstallDate) {
        this.slvCslpNodeInstallDate = slvCslpNodeInstallDate;
    }

    public long getSlvCslpLumInstallDate() {
        return slvCslpLumInstallDate;
    }

    public void setSlvCslpLumInstallDate(long slvCslpLumInstallDate) {
        this.slvCslpLumInstallDate = slvCslpLumInstallDate;
    }

    public long getSlvInstallDate() {
        return slvInstallDate;
    }

    public void setSlvInstallDate(long slvInstallDate) {
        this.slvInstallDate = slvInstallDate;
    }

    public long getSlvLumInstallDate() {
        return slvLumInstallDate;
    }

    public void setSlvLumInstallDate(long slvLumInstallDate) {
        this.slvLumInstallDate = slvLumInstallDate;
    }

    public long getEdgeCslpNodeInstallDate() {
        return edgeCslpNodeInstallDate;
    }

    public void setEdgeCslpNodeInstallDate(long edgeCslpNodeInstallDate) {
        this.edgeCslpNodeInstallDate = edgeCslpNodeInstallDate;
    }

    public long getEdgeCslpLumInstallDate() {
        return edgeCslpLumInstallDate;
    }

    public void setEdgeCslpLumInstallDate(long edgeCslpLumInstallDate) {
        this.edgeCslpLumInstallDate = edgeCslpLumInstallDate;
    }

    public long getEdgeInstallDate() {
        return edgeInstallDate;
    }

    public void setEdgeInstallDate(long edgeInstallDate) {
        this.edgeInstallDate = edgeInstallDate;
    }

    public long getEdgeLumInstallDate() {
        return edgeLumInstallDate;
    }

    public void setEdgeLumInstallDate(long edgeLumInstallDate) {
        this.edgeLumInstallDate = edgeLumInstallDate;
    }

    @Override
    public String toString() {
        return "EdgeData{" +
                "title='" + title + '\'' +
                ", slvCslpNodeInstallDate=" + slvCslpNodeInstallDate +
                ", slvCslpLumInstallDate=" + slvCslpLumInstallDate +
                ", slvInstallDate=" + slvInstallDate +
                ", slvLumInstallDate=" + slvLumInstallDate +
                ", edgeCslpNodeInstallDate=" + edgeCslpNodeInstallDate +
                ", edgeCslpLumInstallDate=" + edgeCslpLumInstallDate +
                ", edgeInstallDate=" + edgeInstallDate +
                ", edgeLumInstallDate=" + edgeLumInstallDate +
                '}';
    }
}

