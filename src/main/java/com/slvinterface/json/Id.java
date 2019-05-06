package com.slvinterface.json;

import java.util.Objects;

public class Id {
    private Integer id;
    private String type;
    private boolean isRequired;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Id id = (Id) o;
        return Objects.equals(type, id.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

}
