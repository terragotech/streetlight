package com.terragoedge.streetlight.edgeinterface;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.edgeserver.FormData;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.json.model.Dictionary;
import com.terragoedge.streetlight.json.model.SLVEdgeFormData;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlvToEdgeService extends EdgeService {
    private Gson gson;

    public SlvToEdgeService() {
        this.gson = new Gson();
    }

    public void run(SlvData slvData) {
        logger.info("-----SLV to Edge Sync Process------------");
        logger.info(slvData.toString());
        String formTemplateGuid = PropertiesReader.getProperties().getProperty("amerescousa.edge.formtemplateGuid");
        String notesJson = getNoteDetails(slvData.getNoteGuid());
        if (notesJson == null) {
            logger.info("Note not in AmerescoUSA.");
            return;
        }
        EdgeNote edgeNote = gson.fromJson(notesJson, EdgeNote.class);
        if (edgeNote != null) {
            if(slvData.isFixtureOnly()){
                setFixtureOnly(edgeNote);
            }
            List<FormData> formDataList = edgeNote.getFormData();
            for (FormData formData : formDataList) {
                if (formData.getFormTemplateGuid().equals(formTemplateGuid)) {
                    processInstallationForm(edgeNote, formData,formTemplateGuid,slvData);
                    return;
                }
            }
        }
    }


    private void setFixtureOnly(EdgeNote edgeNote) {
        List<Dictionary> dictionaryList = edgeNote.getDictionary();
        boolean isLayerPresent = false;
        for (Dictionary dictionary : dictionaryList) {
            if (dictionary.getKey() != null && dictionary.getKey().equals("groupGuid")) {
                isLayerPresent = true;
                if (dictionary.getValue() == null || !dictionary.getValue().equals("f1fbdfd3-89f4-4ffe-b7fa-160437d782be")) {
                    dictionary.setValue("f1fbdfd3-89f4-4ffe-b7fa-160437d782be");
                }
            }
        }

        if (!isLayerPresent) {
            Dictionary dictionary = new Dictionary();
            dictionary.setKey("groupGuid");
            dictionary.setValue("f1fbdfd3-89f4-4ffe-b7fa-160437d782be");
            dictionaryList.add(dictionary);
        }
    }

    public void processInstallationForm(EdgeNote edgeNote, FormData formData, String formTemplateGuid, SlvData slvData) {
        String oldNoteGuid = edgeNote.getNoteGuid();
        String notebookGuid = edgeNote.getEdgeNotebook().getNotebookGuid();

        JsonObject edgeNoteJsonObject = processEdgeForms(gson.toJson(edgeNote),formTemplateGuid,slvData);
        String newNoteGuid = edgeNoteJsonObject.get("noteGuid").getAsString();
        logger.info("ProcessedFormJson " + edgeNoteJsonObject.toString());
        ResponseEntity<String> responseEntity = updateNoteDetails(edgeNoteJsonObject.toString(), oldNoteGuid, notebookGuid);
        logger.info(responseEntity.getBody());
    }
}
