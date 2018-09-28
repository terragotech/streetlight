package com.terragoedge.automation.model;

import com.terragoedge.slvinterface.entity.EdgeFormEntity;
import com.terragoedge.slvinterface.entity.EdgeNoteView;

public class EdgeFormInfo {
    private String title;
    private String noteGuid;
    private String formName;
    private EdgeNoteView edgeNoteView;
    private EdgeFormEntity edgeFormEntity;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public EdgeFormEntity getEdgeFormEntity() {
        return edgeFormEntity;
    }

    public void setEdgeFormEntity(EdgeFormEntity edgeFormEntity) {
        this.edgeFormEntity = edgeFormEntity;
    }

    public EdgeNoteView getEdgeNoteView() {
        return edgeNoteView;
    }

    public void setEdgeNoteView(EdgeNoteView edgeNoteView) {
        this.edgeNoteView = edgeNoteView;
    }

    public String getNoteGuid() {
        return noteGuid;
    }

    public void setNoteGuid(String noteGuid) {
        this.noteGuid = noteGuid;
    }
}
