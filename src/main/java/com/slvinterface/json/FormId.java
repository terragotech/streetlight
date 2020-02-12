package com.slvinterface.json;

import java.util.Objects;

public class FormId {
    private String formId;
    private String name;
    private String slvName;

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlvName() {
        return slvName;
    }

    public void setSlvName(String slvName) {
        this.slvName = slvName;
    }

    @Override
    public String toString() {
        return "FormId{" +
                "formId=" + formId +
                ", name='" + name + '\'' +
                ", slvName='" + slvName + '\'' +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormId formId = (FormId) o;
        return Objects.equals(slvName, formId.slvName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slvName);
    }
}
