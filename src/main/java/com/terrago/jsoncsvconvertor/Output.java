package com.terrago.jsoncsvconvertor;

public class Output {
    private String idOnController;
    private String formDef;

    public String getIdOnController() {
        return idOnController;
    }

    public void setIdOnController(String idOnController) {
        this.idOnController = idOnController;
    }

    public String getFormDef() {
        return formDef;
    }

    public void setFormDef(String formDef) {
        this.formDef = formDef;
    }

    @Override
    public String toString() {
        return "Output{" +
                "idOnController='" + idOnController + '\'' +
                ", formDef='" + formDef + '\'' +
                '}';
    }
}
