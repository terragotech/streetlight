package com.slvinterface.json;

import com.slvinterface.enumeration.SLVProcess;

import java.util.List;
import java.util.Objects;

public class Config {
    private SLVProcess type;
    private List<Id> ids = null;

    public SLVProcess getType() {
        return type;
    }

    public void setType(SLVProcess type) {
        this.type = type;
    }

    public List<Id> getIds() {
        return ids;
    }

    public void setIds(List<Id> ids) {
        this.ids = ids;
    }

    @Override
    public String toString() {
        return "Config{" +
                "type=" + type +
                ", ids=" + ids +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Config config = (Config) o;
        return type == config.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
