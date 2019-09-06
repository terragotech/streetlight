package com.terrago.jsoncsvconvertor.promoted;

import java.util.List;

public class PromotedConfig {
    private String formTemplateGuid;
    private List<PromotedDatum> promotedData = null;
    private String parentnoteguid;
    private String promotedId;
    private String notebookGuid;

    public String getFormTemplateGuid() {
        return formTemplateGuid;
    }

    public void setFormTemplateGuid(String formTemplateGuid) {
        this.formTemplateGuid = formTemplateGuid;
    }

    public List<PromotedDatum> getPromotedData() {
        return promotedData;
    }

    public void setPromotedData(List<PromotedDatum> promotedData) {
        this.promotedData = promotedData;
    }

    public String getParentnoteguid() {
        return parentnoteguid;
    }

    public void setParentnoteguid(String parentnoteguid) {
        this.parentnoteguid = parentnoteguid;
    }

    public String getPromotedId() {
        return promotedId;
    }

    public void setPromotedId(String promotedId) {
        this.promotedId = promotedId;
    }

    public String getNotebookGuid() {
        return notebookGuid;
    }

    public void setNotebookGuid(String notebookGuid) {
        this.notebookGuid = notebookGuid;
    }

    @Override
    public String toString() {
        return "PromotedConfig{" +
                "formTemplateGuid='" + formTemplateGuid + '\'' +
                ", promotedData=" + promotedData +
                ", parentnoteguid='" + parentnoteguid + '\'' +
                ", promotedId='" + promotedId + '\'' +
                ", notebookGuid='" + notebookGuid + '\'' +
                '}';
    }
}
