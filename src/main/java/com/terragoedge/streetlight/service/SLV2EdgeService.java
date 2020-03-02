package com.terragoedge.streetlight.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.json.model.SLVEdgeFormData;
import com.terragoedge.streetlight.swap.SwapTemplateProcessor;
import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SLV2EdgeService {

    final Logger logger = Logger.getLogger(SLV2EdgeService.class);
    RestService restService = null;
    JsonParser jsonParser = null;

    public SLV2EdgeService(){
        restService = new RestService();
        jsonParser = new JsonParser();
    }


    public void removeSwapForm(EdgeNote edgeNoteObject, String edgeNote){
        try {
            String noteGuid = edgeNoteObject.getNoteGuid();
            String notebookGuid = edgeNoteObject.getEdgeNotebook() != null ? edgeNoteObject.getEdgeNotebook().getNotebookGuid() : null;
            JsonObject edgeNoteJson = (JsonObject) jsonParser.parse(edgeNote);
            JsonArray serverEdgeFormJsonArray = edgeNoteJson.get("formData").getAsJsonArray();
            int size = serverEdgeFormJsonArray.size();
            List<Integer> swapFormPositionList = new ArrayList<>();
            boolean isSwapFormPresent = false;
            String formTemplateGuid = PropertiesReader.getProperties().getProperty("streetlight.edge.coc.formtemplate.guid");
            for (int i = 0; i < size; i++) {
                JsonObject serverEdgeForm = serverEdgeFormJsonArray.get(i).getAsJsonObject();
                String currentFormTemplateGuid = serverEdgeForm.get("formTemplateGuid").getAsString();
                if (currentFormTemplateGuid.equals(formTemplateGuid)) {
                    swapFormPositionList.add(i);
                    isSwapFormPresent = true;
                } else {
                    serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
                }

            }
            if(isSwapFormPresent){
                for(Integer swapFormPosition : swapFormPositionList){
                    serverEdgeFormJsonArray.remove(swapFormPosition);
                }
                edgeNoteJson.add("formData", serverEdgeFormJsonArray);
                edgeNoteJson.addProperty("createdDateTime", System.currentTimeMillis());
                edgeNoteJson.addProperty("createdBy", "slvinterface");
                edgeNoteJson.addProperty("noteGuid", UUID.randomUUID().toString());

                String baseUrl = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
                String urlNew = null;
                if(notebookGuid != null){
                    urlNew = baseUrl + "/rest/notebooks/" + notebookGuid + "/notes/" + noteGuid;
                }else{
                    urlNew = baseUrl + "/rest/notes/" + noteGuid;
                }

                ResponseEntity responseEntity =  restService.callPostMethod(urlNew, HttpMethod.PUT, edgeNoteJson.toString(),false);
                if(responseEntity.getStatusCode().is2xxSuccessful()){
                    logger.info("Swap Form Removed");
                }else{
                    logger.info("Swap Form Not Removed");
                }
            }

        }catch (Exception e){
            logger.error("Error in removeSwapForm",e);
        }

    }
}
