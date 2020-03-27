package com.terragoedge.streetlight.json.model;

import java.util.ArrayList;
import java.util.List;

public class DataComparatorConfig {

    private String formTemplateGuid;
    private String noteGuid;
    private String revisionFromNoteId;
    private List<Integer> ids = new ArrayList<>();

    public String getFormTemplateGuid() {
        return formTemplateGuid;
    }

    public void setFormTemplateGuid(String formTemplateGuid) {
        this.formTemplateGuid = formTemplateGuid;
    }

    public String getNoteGuid() {
        return noteGuid;
    }

    public void setNoteGuid(String noteGuid) {
        this.noteGuid = noteGuid;
    }

    public String getRevisionFromNoteId() {
        return revisionFromNoteId;
    }

    public void setRevisionFromNoteId(String revisionFromNoteId) {
        this.revisionFromNoteId = revisionFromNoteId;
    }

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }


}
