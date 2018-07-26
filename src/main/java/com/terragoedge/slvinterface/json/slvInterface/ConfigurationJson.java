package com.terragoedge.slvinterface.json.slvInterface;
import com.terragoedge.slvinterface.enumeration.SLVProcess;

import java.util.List;
public class ConfigurationJson {
    private SLVProcess type;
    private List<Id> ids = null;
    private List<Action> action = null;

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

    public List<Action> getAction() {
        return action;
    }

    public void setAction(List<Action> action) {
        this.action = action;
    }
}
