package com.terragoedge.streetlight.installmaintain.json;

import com.terragoedge.streetlight.installmaintain.InstallMaintenanceEnum;

import java.util.ArrayList;
import java.util.List;

public class Prop {
    private InstallMaintenanceEnum type;
    private List<Ids> ids = new ArrayList<>();

    public InstallMaintenanceEnum getType() {
        return type;
    }

    public void setType(InstallMaintenanceEnum type) {
        this.type = type;
    }

    public List<Ids> getIds() {
        return ids;
    }

    public void setIds(List<Ids> ids) {
        this.ids = ids;
    }
}
