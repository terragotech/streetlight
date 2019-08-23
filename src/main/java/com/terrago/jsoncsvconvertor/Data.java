package com.terrago.jsoncsvconvertor;

public class Data {
    private int id;
    private String label;
    private String value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        if(value != null){
            value = value.replace(label+"#","");
        }

        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Data{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
