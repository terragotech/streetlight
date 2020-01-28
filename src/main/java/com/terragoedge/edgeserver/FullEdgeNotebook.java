package com.terragoedge.edgeserver;

import java.util.ArrayList;
import java.util.List;

public class FullEdgeNotebook {

    private String createdBy;
    private long lastUpdatedTime;
    private String notebookDescription;
    private String notebookName;
    private String quickNoteFormTemplateId = "c8acc150-6228-4a27-bc7e-0fabea0e2b93";
    private String quickNoteNameType;
    private String quickNoteCustomName;
    private boolean isIncludeDateTime;

    private List<String> forms = new ArrayList<String>();


    public List<String> getForms() {
        return forms;
    }

    public void setForms(List<String> forms) {
        this.forms = forms;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(long lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public String getNotebookDescription() {
        return notebookDescription;
    }

    public void setNotebookDescription(String notebookDescription) {
        this.notebookDescription = notebookDescription;
    }

    public String getNotebookName() {
        return notebookName;
    }

    public void setNotebookName(String notebookName) {
        this.notebookName = notebookName;
    }

    public String getQuickNoteFormTemplateId() {
        return quickNoteFormTemplateId;
    }

    public void setQuickNoteFormTemplateId(String quickNoteFormTemplateId) {
        this.quickNoteFormTemplateId = quickNoteFormTemplateId;
    }

    public String getQuickNoteNameType() {
        return quickNoteNameType;
    }

    public void setQuickNoteNameType(String quickNoteNameType) {
        this.quickNoteNameType = quickNoteNameType;
    }

    public String getQuickNoteCustomName() {
        return quickNoteCustomName;
    }

    public void setQuickNoteCustomName(String quickNoteCustomName) {
        this.quickNoteCustomName = quickNoteCustomName;
    }

    public boolean isIncludeDateTime() {
        return isIncludeDateTime;
    }

    public void setIncludeDateTime(boolean includeDateTime) {
        isIncludeDateTime = includeDateTime;
    }

    @Override
    public String toString() {
        return "FullEdgeNotebook{" +
                "createdBy='" + createdBy + '\'' +
                ", lastUpdatedTime=" + lastUpdatedTime +
                ", notebookDescription='" + notebookDescription + '\'' +
                ", notebookName='" + notebookName + '\'' +
                ", quickNoteFormTemplateId=" + quickNoteFormTemplateId +
                ", quickNoteNameType='" + quickNoteNameType + '\'' +
                ", quickNoteCustomName='" + quickNoteCustomName + '\'' +
                ", isIncludeDateTime=" + isIncludeDateTime +
                '}';
    }
}
