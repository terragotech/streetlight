package com.terragoedge.streetlight.installmaintain.json;

import com.terragoedge.streetlight.installmaintain.InstallMaintenanceEnum;

import java.util.ArrayList;
import java.util.List;

public class Prop {
    private InstallMaintenanceEnum type;
    private Ids ids;

    public InstallMaintenanceEnum getType() {
        return type;
    }

    public void setType(InstallMaintenanceEnum type) {
        this.type = type;
    }

    public Ids getIds() {
        return ids;
    }

    public void setIds(Ids ids) {
        this.ids = ids;
    }

    @Override
    public String toString() {
        return "Prop{" +
                "type=" + type +
                ", ids=" + ids +
                '}';
    }
}
