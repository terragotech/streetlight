package com.terragoedge.streetlight.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NoteData {


    private long noteId;
    private long createdDateTime;
    private String createdBy;
    private String description;
    private String title;
    private String groupName;
    private String lat;
    private String lng;
    private String noteGuid;


    public String getNoteGuid() {
        return noteGuid;
    }

    public void setNoteGuid(String noteGuid) {
        this.noteGuid = noteGuid;
    }

    private List<FormData> formDataList = new ArrayList<>();

    public long getNoteId() {
        return noteId;
    }

    public void setNoteId(long noteId) {
        this.noteId = noteId;
    }

    public long getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(long createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public List<FormData> getFormDataList() {
        return formDataList;
    }

    public void setFormDataList(List<FormData> formDataList) {
        this.formDataList = formDataList;
    }

    @Override
    public String toString() {
        return "NoteData{" +
                "noteId=" + noteId +
                ", createdDateTime=" + createdDateTime +
                ", createdBy='" + createdBy + '\'' +
                ", description='" + description + '\'' +
                ", title='" + title + '\'' +
                ", groupName='" + groupName + '\'' +
                ", lat='" + lat + '\'' +
                ", lng='" + lng + '\'' +
                ", noteGuid='" + noteGuid + '\'' +
                ", formDataList=" + formDataList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoteData noteData = (NoteData) o;
        return noteId == noteData.noteId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(noteId);
    }
}
