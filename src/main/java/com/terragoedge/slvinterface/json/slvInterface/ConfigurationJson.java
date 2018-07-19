package com.terragoedge.slvinterface.json.slvInterface;
import java.util.List;
public class ConfigurationJson {
    private String type;
    private List<Id> ids = null;
    private List<Action> action = null;

    public String getType() {
        return type;
    }

    public void setType(String type) {
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
