package com.slvinterface.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FormData {
    private String formTemplateGuid = null;
    private String formDef = null;
    private String category = null;
    private String formGuid = null;
    private String name = null;

    public String getFormTemplateGuid() {
        return formTemplateGuid;
    }

    public void setFormTemplateGuid(String formTemplateGuid) {
        this.formTemplateGuid = formTemplateGuid;
    }

    public List<FormValues> getFormDef() {
        Type listType = new TypeToken<ArrayList<FormValues>>() {
        }.getType();
        Gson gson = new Gson();
        List<FormValues> edgeFormDatas = gson.fromJson(formDef, listType);
        return edgeFormDatas;
    }




    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFormGuid() {
        return formGuid;
    }

    public void setFormGuid(String formGuid) {
        this.formGuid = formGuid;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormData formData = (FormData) o;
        return Objects.equals(formTemplateGuid, formData.formTemplateGuid);
    }

    @Override
    public int hashCode() {

        return Objects.hash(formTemplateGuid);
    }
}
