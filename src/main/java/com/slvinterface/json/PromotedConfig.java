package com.slvinterface.json;

import java.util.ArrayList;
import java.util.List;

public class PromotedConfig {
    private String formTemplateGuid;
    private List<FormId> formIds = new ArrayList<>();

    public String getFormTemplateGuid() {
        return formTemplateGuid;
    }

    public void setFormTemplateGuid(String formTemplateGuid) {
        this.formTemplateGuid = formTemplateGuid;
    }

    public List<FormId> getFormIds() {
        return formIds;
    }

    public void setFormIds(List<FormId> formIds) {
        this.formIds = formIds;
    }

    @Override
    public String toString() {
        return "PromotedConfig{" +
                "formTemplateGuid='" + formTemplateGuid + '\'' +
                ", formIds=" + formIds +
                '}';
    }
}
