package com.terragoedge.slvinterface.model;

import java.util.ArrayList;
import java.util.List;

public class EdgeNotebook {

    private String notebookName = null;
    private String notebookGuid = null;

    private String notebookDescription;
    private String lastUpdatedTime;
    private String createdBy;

    private List<String> forms = new ArrayList<String>();

    public String getNotebookName() {
        return notebookName;
    }

    public void setNotebookName(String notebookName) {
        this.notebookName = notebookName;
    }

    public String getNotebookGuid() {
        return notebookGuid;
    }

    public void setNotebookGuid(String notebookGuid) {
        this.notebookGuid = notebookGuid;
    }


    public String getNotebookDescription() {
        return notebookDescription;
    }

    public void setNotebookDescription(String notebookDescription) {
        this.notebookDescription = notebookDescription;
    }

    public String getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(String lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<String> getForms() {
        return forms;
    }

    public void setForms(List<String> forms) {
        this.forms = forms;
    }

    @Override
    public String toString() {
        return "EdgeNotebook{" +
                "notebookName='" + notebookName + '\'' +
                ", notebookGuid='" + notebookGuid + '\'' +
                ", notebookDescription='" + notebookDescription + '\'' +
                ", lastUpdatedTime='" + lastUpdatedTime + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", forms=" + forms +
                '}';
    }
}
