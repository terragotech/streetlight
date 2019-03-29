package com.terragoedge.slvinterface.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "slvdata")
public class SlvData {
    @DatabaseField(columnName = "idoncontroller")
    private String fixtureId;
    @DatabaseField(columnName = "lat")
    private String latitude;
    @DatabaseField(columnName = "lng")
    private String longitude;
    @DatabaseField(columnName = "location_atlasphysicalpage")
    private String altasPhysicalPage;
    @DatabaseField(columnName = "lamp_cdotlamptype")
    private String cdotType;
    @DatabaseField(columnName = "luminaire_colorcode")
    private String colorCode;
    @DatabaseField(columnName = "location_fixtureaddress")
    private String fixtureAddress;
    @DatabaseField(columnName = "luminaire_fixturecode")
    private String fixtureCode;
    @DatabaseField(columnName = "geoZone_namesPath")
    private String GeoZonePath;
    @DatabaseField(columnName = "fixing_mastarmangle")
    private String maAngle;
    @DatabaseField(columnName = "fixing_mastarmlength")
    private String maLength;
    @DatabaseField(columnName = "fixing_numberofmastarms")
    private String maQty;
    @DatabaseField(columnName = "location_proposedcontext")
    private String proposedContext;
  //  @DatabaseField(columnName = "controllerStrId")
    private String slvControllerId;
    @DatabaseField(columnName = "macAddress")
    private String macAddress;
    @DatabaseField(columnName = "install_date")
    private String installDate;
    @DatabaseField(columnName = "cslp_lum_install_date")
    private String cslp_lum_install_date;
    @DatabaseField(columnName = "cslp_node_install_date")
    private String cslp_node_install_date;
    @DatabaseField(columnName = "luminaire_fixturecode")
    private String luminaire_fixturecode;
    @DatabaseField(columnName = "installstatus")
    private String installstatus;

    public String getFixtureId() {
        return fixtureId;
    }

    public void setFixtureId(String fixtureId) {
        this.fixtureId = fixtureId;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAltasPhysicalPage() {
        return altasPhysicalPage;
    }

    public void setAltasPhysicalPage(String altasPhysicalPage) {
        this.altasPhysicalPage = altasPhysicalPage;
    }

    public String getCdotType() {
        return cdotType;
    }

    public void setCdotType(String cdotType) {
        this.cdotType = cdotType;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getFixtureAddress() {
        return fixtureAddress;
    }

    public void setFixtureAddress(String fixtureAddress) {
        this.fixtureAddress = fixtureAddress;
    }

    public String getFixtureCode() {
        return fixtureCode;
    }

    public void setFixtureCode(String fixtureCode) {
        this.fixtureCode = fixtureCode;
    }

    public String getGeoZonePath() {
        return GeoZonePath;
    }

    public void setGeoZonePath(String geoZonePath) {
        GeoZonePath = geoZonePath;
    }

    public String getMaAngle() {
        return maAngle;
    }

    public void setMaAngle(String maAngle) {
        this.maAngle = maAngle;
    }

    public String getMaLength() {
        return maLength;
    }

    public void setMaLength(String maLength) {
        this.maLength = maLength;
    }

    public String getMaQty() {
        return maQty;
    }

    public void setMaQty(String maQty) {
        this.maQty = maQty;
    }

    public String getProposedContext() {
        return proposedContext;
    }

    public void setProposedContext(String proposedContext) {
        this.proposedContext = proposedContext;
    }

    public String getSlvControllerId() {
        return slvControllerId;
    }

    public void setSlvControllerId(String slvControllerId) {
        this.slvControllerId = slvControllerId;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getInstallDate() {
        return installDate;
    }

    public void setInstallDate(String installDate) {
        this.installDate = installDate;
    }

    public String getCslp_lum_install_date() {
        return cslp_lum_install_date;
    }

    public void setCslp_lum_install_date(String cslp_lum_install_date) {
        this.cslp_lum_install_date = cslp_lum_install_date;
    }

    public String getCslp_node_install_date() {
        return cslp_node_install_date;
    }

    public void setCslp_node_install_date(String cslp_node_install_date) {
        this.cslp_node_install_date = cslp_node_install_date;
    }

    public String getLuminaire_fixturecode() {
        return luminaire_fixturecode;
    }

    public void setLuminaire_fixturecode(String luminaire_fixturecode) {
        this.luminaire_fixturecode = luminaire_fixturecode;
    }

    public String getInstallstatus() {
        return installstatus;
    }

    public void setInstallstatus(String installstatus) {
        this.installstatus = installstatus;
    }

    @Override
    public String toString() {
        return "SlvData{" +
                "fixtureId='" + fixtureId + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", altasPhysicalPage='" + altasPhysicalPage + '\'' +
                ", cdotType='" + cdotType + '\'' +
                ", colorCode='" + colorCode + '\'' +
                ", fixtureAddress='" + fixtureAddress + '\'' +
                ", fixtureCode='" + fixtureCode + '\'' +
                ", GeoZonePath='" + GeoZonePath + '\'' +
                ", maAngle='" + maAngle + '\'' +
                ", maLength='" + maLength + '\'' +
                ", maQty='" + maQty + '\'' +
                ", proposedContext='" + proposedContext + '\'' +
                ", slvControllerId='" + slvControllerId + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", installDate='" + installDate + '\'' +
                ", cslp_lum_install_date='" + cslp_lum_install_date + '\'' +
                ", cslp_node_install_date='" + cslp_node_install_date + '\'' +
                ", luminaire_fixturecode='" + luminaire_fixturecode + '\'' +
                ", installstatus='" + installstatus + '\'' +
                '}';
    }
}