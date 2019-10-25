package com.slvinterface.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.slvinterface.json.EdgeNote;
import com.slvinterface.json.FormValues;
import com.slvinterface.json.SLVFields;
import com.slvinterface.utils.FormValueUtil;
import com.slvinterface.utils.PropertiesReader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

abstract public class CreateRevision {
    private EdgeRestService edgeRestService;
    protected Gson gson;
    public void updateLogic(JsonObject edgenoteJson,JsonArray serverForms,Object obj){
        int size = serverForms.size();
        for (int i = 0; i < size; i++) {
            JsonObject serverEdgeForm = serverForms.get(i).getAsJsonObject();
            String formDefJson = serverEdgeForm.get("formDef").getAsString();
            String formTemplate = serverEdgeForm.get("formTemplateGuid").getAsString();
            formDefJson = formDefJson.replaceAll("\\\\", "");
            formDefJson = formDefJson.replace("u0026","\\u0026");
            List<FormValues> formComponents = gson.fromJson(formDefJson, new TypeToken<List<FormValues>>() {
            }.getType());
            serverEdgeForm.add("formDef", gson.toJsonTree(formComponents));
            serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
        }
        edgenoteJson.add("formData", serverForms);
        edgenoteJson.addProperty("createdBy", "admin");
        long ntime = System.currentTimeMillis();

        edgenoteJson.addProperty("createdDateTime", ntime);
    }

    public CreateRevision(){
        edgeRestService = new EdgeRestService();
        gson = new Gson();
    }

    private String getNoteDetails(String noteguid) {
        String response = "";
        String baseUrl = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        String urlNew = baseUrl + "rest/notes/" + noteguid;
        System.out.println(urlNew);
        String tokenString = edgeRestService.getEdgeToken();
        ResponseEntity<String> requestEntity = edgeRestService.getRequest(urlNew,true,tokenString);
        if(requestEntity.getStatusCode() == HttpStatus.OK)
        {
            response = requestEntity.getBody();
        }
        return response;
    }
    private String updateNoteDetails(String noteJson,String noteGuid,String notebookGuid)
    {
        String response = "";
        String baseUrl = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        String urlNew = baseUrl + "rest/notebooks/" + notebookGuid + "/notes/" + noteGuid;
        System.out.println(urlNew);
        String tokenString = edgeRestService.getEdgeToken();
        ResponseEntity<String>  responseEntity = edgeRestService.putRequest(urlNew,noteJson,true,tokenString);
        if(responseEntity.getStatusCode() == HttpStatus.OK)
        {
            response = responseEntity.getBody();
        }
        return response;
    }
    public void createRevision(String noteGUID,Object obj){
        String noteJson = getNoteDetails(noteGUID);
        EdgeNote restEdgeNote = gson.fromJson(noteJson, EdgeNote.class);
        JsonObject edgenoteJson = new JsonParser().parse(noteJson).getAsJsonObject();
        JsonArray serverForms = edgenoteJson.get("formData").getAsJsonArray();
        updateLogic(edgenoteJson,serverForms,obj);
        updateNoteDetails(edgenoteJson.toString(),noteGUID,restEdgeNote.getEdgeNotebook().getNotebookGuid());
    }

}
