package com.slvinterface.json;

import java.util.ArrayList;
import java.util.List;

public class PromotedValue {

    private String formTemplateGuid;
    private List<PromotedComponent> promotedData = new ArrayList<>();

    public String getFormTemplateGuid() {
        return formTemplateGuid;
    }

    public void setFormTemplateGuid(String formTemplateGuid) {
        this.formTemplateGuid = formTemplateGuid;
    }

    public List<PromotedComponent> getPromotedData() {
        return promotedData;
    }

    public void setPromotedData(List<PromotedComponent> promotedData) {
        this.promotedData = promotedData;
    }

    @Override
    public String toString() {
        return "PromotedValue{" +
                "formTemplateGuid='" + formTemplateGuid + '\'' +
                ", promotedData=" + promotedData +
                '}';
    }
}
