package com.terragoedge.streetlight.edgeinterface;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.edgeserver.FormData;
import com.terragoedge.streetlight.PropertiesReader;
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
            List<FormData> formDataList = edgeNote.getFormData();
            for (FormData formData : formDataList) {
                if (formData.getFormTemplateGuid().equals(formTemplateGuid)) {
                    processInstallationForm(edgeNote, formData, formTemplateGuid, slvData);
                    return;
                }
            }
        }
    }

    public void processInstallationForm(EdgeNote edgeNote, FormData formData, String formTemplateGuid, SlvData slvData) {
        String oldNoteGuid = edgeNote.getNoteGuid();
        if (slvData.getNoteTitle() != null) {
            edgeNote.setTitle(slvData.getNoteTitle());
        }
        String notebookGuid = edgeNote.getEdgeNotebook().getNotebookGuid();
        List<EdgeFormData> edgeFormDataList = formData.getFormDef();
        JsonObject edgeNoteJsonObject = processEdgeForms(gson.toJson(edgeNote), edgeFormDataList, formTemplateGuid, slvData);
        String newNoteGuid = edgeNoteJsonObject.get("noteGuid").getAsString();
        logger.info("ProcessedFormJson " + edgeNoteJsonObject.toString());
        ResponseEntity<String> responseEntity = updateNoteDetails(edgeNoteJsonObject.toString(), oldNoteGuid, notebookGuid);
        logger.info(responseEntity.getBody());

    }
}
