package com.terragoedge.streetlight.json.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "slvinterfacelog")
public class SlvInterfaceLogEntity {
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(columnName = "idoncontroller")
    private String idOnController;
    @DatabaseField(columnName = "selectedaction")
    private String selectedAction;
    @DatabaseField(columnName = "noteguid")
    private String noteGuid;
    @DatabaseField(columnName = "parentnoteid")
    private String parentnoteid;
    @DatabaseField(columnName = "macaddress")
    private String macAddress;
    @DatabaseField(columnName = "fixtureqrscan")
    private String fixtureqrscan;
    @DatabaseField(columnName = "status")
    private String status;
    @DatabaseField(columnName = "errorcategory")
    private String errorcategory;
    @DatabaseField(columnName = "errordetails")
    private String errordetails;
    @DatabaseField(columnName = "createddatetime")
    private long createddatetime;
    @DatabaseField(columnName = "installstatus")
    private String installStatus;
    @DatabaseField(columnName = "replaceolc")
    private String replaceOlc;
    @DatabaseField(columnName = "setdevice")
    private String setDevice;
    @DatabaseField(columnName = "selectedreplace")
    private String selectedReplace;
    @DatabaseField(columnName = "isresync")
    private boolean isResync;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdOnController() {
        return idOnController;
    }

    public void setIdOnController(String idOnController) {
        this.idOnController = idOnController;
    }

    public String getSelectedAction() {
        return selectedAction;
    }

    public void setSelectedAction(String selectedAction) {
        this.selectedAction = selectedAction;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrordetails() {
        return errordetails;
    }

    public void setErrordetails(String errordetails) {
        this.errordetails = errordetails;
    }

    public long getCreateddatetime() {
        return createddatetime;
    }

    public void setCreateddatetime(long createddatetime) {
        this.createddatetime = createddatetime;
    }

    public String getNoteGuid() {
        return noteGuid;
    }

    public void setNoteGuid(String noteGuid) {
        this.noteGuid = noteGuid;
    }

    public String getInstallStatus() {
        return installStatus;
    }

    public void setInstallStatus(String installStatus) {
        this.installStatus = installStatus;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getFixtureqrscan() {
        return fixtureqrscan;
    }

    public void setFixtureqrscan(String fixtureqrscan) {
        this.fixtureqrscan = fixtureqrscan;
    }

    public String getReplaceOlc() {
        return replaceOlc;
    }

    public void setReplaceOlc(String replaceOlc) {
        this.replaceOlc = replaceOlc;
    }

    public String getSetDevice() {
        return setDevice;
    }

    public void setSetDevice(String setDevice) {
        this.setDevice = setDevice;
    }

    public String getSelectedReplace() {
        return selectedReplace;
    }

    public void setSelectedReplace(String selectedReplace) {
        this.selectedReplace = selectedReplace;
    }

    public boolean isResync() {
        return isResync;
    }

    public void setResync(boolean resync) {
        isResync = resync;
    }

    public String getErrorcategory() {
        return errorcategory;
    }

    public void setErrorcategory(String errorcategory) {
        this.errorcategory = errorcategory;
    }

    public String getParentnoteid() {
        return parentnoteid;
    }

    public void setParentnoteid(String parentnoteid) {
        this.parentnoteid = parentnoteid;
    }

    @Override
    public String toString() {
        return "SlvInterfaceLogEntity{" +
                "id=" + id +
                ", idOnController='" + idOnController + '\'' +
                ", selectedAction='" + selectedAction + '\'' +
                ", noteGuid='" + noteGuid + '\'' +
                ", parentnoteid='" + parentnoteid + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", fixtureqrscan='" + fixtureqrscan + '\'' +
                ", status='" + status + '\'' +
                ", errorcategory='" + errorcategory + '\'' +
                ", errordetails='" + errordetails + '\'' +
                ", createddatetime=" + createddatetime +
                ", installStatus='" + installStatus + '\'' +
                ", replaceOlc='" + replaceOlc + '\'' +
                ", setDevice='" + setDevice + '\'' +
                ", selectedReplace='" + selectedReplace + '\'' +
                ", isResync=" + isResync +
                '}';
    }
}
