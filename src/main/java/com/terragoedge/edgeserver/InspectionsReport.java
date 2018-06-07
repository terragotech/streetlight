package com.terragoedge.edgeserver;

public class InspectionsReport {
    private String name;
    private long dateModified;
    private String atlasPage;
    private String createdBy;
    private String type;
    private String lat;
    private String lon;
    private String issueType;
    private String addComment;
    private String address;
    private String macaddress;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMacaddress() {
        return macaddress;
    }

    public void setMacaddress(String macaddress) {
        this.macaddress = macaddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDateModified() {
        return dateModified;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }

    public String getAtlasPage() {
        return atlasPage;
    }

    public void setAtlasPage(String atlasPage) {
        this.atlasPage = atlasPage;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getAddComment() {
        return addComment;
    }

    public void setAddComment(String addComment) {
        this.addComment = addComment;
    }

    @Override
    public String toString() {
        return "InspectionsReport{" +
                "name='" + name + '\'' +
                ", dateModified=" + dateModified +
                ", atlasPage='" + atlasPage + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", type='" + type + '\'' +
                ", lat='" + lat + '\'' +
                ", lon='" + lon + '\'' +
                ", issueType='" + issueType + '\'' +
                ", addComment='" + addComment + '\'' +
                ", address='" + address + '\'' +
                ", macaddress='" + macaddress + '\'' +
                '}';
    }
}
