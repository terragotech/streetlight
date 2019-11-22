package com.slvinterface.json;

import com.google.gson.JsonArray;

public class LightDataCompareResult {
    JsonArray jsonArray;
    boolean mustUpdate;

    public JsonArray getJsonArray() {
        return jsonArray;
    }

    public void setJsonArray(JsonArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    public boolean isMustUpdate() {
        return mustUpdate;
    }

    public void setMustUpdate(boolean mustUpdate) {
        this.mustUpdate = mustUpdate;
    }
}
