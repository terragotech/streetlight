package com.terrago.streetlights.json;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.streetlight.json.model.Dictionary;

import java.util.List;
import java.util.UUID;

public class TerragoData {
    EdgeNote restEdgeNote;
    JsonObject edgenoteJson;
    JsonArray serverForms;
    Gson gson;

    public TerragoData(String notesJson){
        gson = new Gson();
        restEdgeNote = gson.fromJson(notesJson, EdgeNote.class);
        edgenoteJson = new JsonParser().parse(notesJson).getAsJsonObject();
        serverForms = edgenoteJson.get("formData").getAsJsonArray();

    }

    public EdgeNote getRestEdgeNote() {
        return restEdgeNote;
    }

    public void setRestEdgeNote(EdgeNote restEdgeNote) {
        this.restEdgeNote = restEdgeNote;
    }

    public JsonObject getEdgenoteJson() {
        return edgenoteJson;
    }

    public void setEdgenoteJson(JsonObject edgenoteJson) {
        this.edgenoteJson = edgenoteJson;
    }

    public JsonArray getServerForms() {
        return serverForms;
    }

    public void setServerForms(JsonArray serverForms) {
        this.serverForms = serverForms;
    }

    public boolean hasForm(String formTemplateGUID)
    {
        boolean bFound = false;
        int size = serverForms.size();
        for (int i = 0; i < size; i++) {
            JsonObject serverEdgeForm = serverForms.get(i).getAsJsonObject();
            String formTemplateGuid1 = serverEdgeForm.get("formTemplateGuid").getAsString();
            if(formTemplateGuid1.equals(formTemplateGUID))
            {
                bFound = true;
            }
        }
        return bFound;
    }

    public List<EdgeFormData> getFormComponents(String formTemplateGUID)
    {
        int size = serverForms.size();
        List<EdgeFormData> result = null;
        for (int i = 0; i < size; i++) {
            JsonObject serverEdgeForm = serverForms.get(i).getAsJsonObject();
            String formDefJson = serverEdgeForm.get("formDef").getAsString();
            String formTemplateGuid = serverEdgeForm.get("formTemplateGuid").getAsString();
            formDefJson = formDefJson.replaceAll("\\\\", "");
            formDefJson = formDefJson.replace("u0026", "\\u0026");
            List<EdgeFormData> formComponents = gson.fromJson(formDefJson, new TypeToken<List<EdgeFormData>>() {
            }.getType());
            if(formTemplateGuid.equals(formTemplateGUID)){
                result = formComponents;
                break;
            }
            //serverEdgeForm.add("formDef", gson.toJsonTree(formComponents));
            //serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());

        }
        //edgenoteJson.add("formData", serverForms);
        //edgenoteJson.addProperty("createdBy", "admin");
        //long ntime = System.currentTimeMillis();
        //edgenoteJson.addProperty("createdDateTime", ntime);
        return result;
    }
    public String getNoteLayerGUID(){
        List<Dictionary> lstDictionary = restEdgeNote.getDictionary();
        String strValue = "";
        for(Dictionary dictionary:lstDictionary) {
            String strKey = dictionary.getKey();
            if (strKey.equals("groupGuid")) {
                strValue = dictionary.getValue();
                break;
            }
        }
        return strValue;
    }
}
