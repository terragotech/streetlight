package com.terragoedge.streetlight.dao;

public class FormData {

    private String name;
    private String formTemplateGuid;
    private String formDef;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormTemplateGuid() {
        return formTemplateGuid;
    }

    public void setFormTemplateGuid(String formTemplateGuid) {
        this.formTemplateGuid = formTemplateGuid;
    }

    public String getFormDef() {
        return formDef;
    }

    public void setFormDef(String formDef) {
        this.formDef = formDef;
    }

    @Override
    public String toString() {
        return "FormData{" +
                "name='" + name + '\'' +
                ", formTemplateGuid='" + formTemplateGuid + '\'' +
                ", formDef='" + formDef + '\'' +
                '}';
    }
}
