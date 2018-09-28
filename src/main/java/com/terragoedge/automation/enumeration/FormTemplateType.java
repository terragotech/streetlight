package com.terragoedge.automation.enumeration;

public enum FormTemplateType {
    ReturntoStock(1,"Return to Stock"), LoadforAssignment(2,"Load for Assignment");
    private int templateTypeId;
    private String templateTypeName;

    FormTemplateType(int templateTypeId, String templateTypeName) {
        this.templateTypeId = templateTypeId;
        this.templateTypeName = templateTypeName;
    }

    public int getTemplateTypeId() {
        return templateTypeId;
    }

    public void setTemplateTypeId(int templateTypeId) {
        this.templateTypeId = templateTypeId;
    }

    public String getTemplateTypeName() {
        return templateTypeName;
    }

    public void setTemplateTypeName(String templateTypeName) {
        this.templateTypeName = templateTypeName;
    }
}
