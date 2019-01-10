package com.terragoedge.streetlight.edgeinterface;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.edgeserver.FormData;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.dao.StreetlightDao;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlvToEdgeService extends EdgeService {
    private Gson gson;
    private StreetlightDao streetlightDao;

    public SlvToEdgeService() {
        this.gson = new Gson();
        streetlightDao = new StreetlightDao();
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
        Type listType = new TypeToken<ArrayList<EdgeNote>>() {
        }.getType();
        Gson gson = new Gson();
        //List<EdgeNote> edgeNoteList = gson.fromJson(notesJson, listType);
        List<EdgeNote> edgeNoteList= new ArrayList<>();
        EdgeNote edgeNote1 = gson.fromJson(notesJson, EdgeNote.class);
        edgeNoteList.add(edgeNote1);
        for (EdgeNote edgeNote : edgeNoteList) {
            List<FormData> formDataList = edgeNote.getFormData();
            for (FormData formData : formDataList) {
                if (formData.getFormTemplateGuid().equals(formTemplateGuid)) {
                    String createdBy = edgeNote.getCreatedBy();
                    long createddatetime = edgeNote.getCreatedDateTime();
                    SlvData resultSlvData = processInstallationForm(edgeNote, formData, formTemplateGuid, slvData);
                    if (resultSlvData.getStatus().equals("Success")) {
                       streetlightDao.updateNoteDetails(createddatetime + 1000, "milhouseinterface", resultSlvData.getNewNoteGuid());
                        logger.info("------------------Success---------------------");
                        logger.info("Processed notetitle: " + edgeNote.getTitle());
                    }else {
                        logger.info("------------------failure---------------------");
                        logger.info("Processed notetitle: " + edgeNote.getTitle());
                    }
                }
            }
        }
    }

    public SlvData processInstallationForm(EdgeNote edgeNote, FormData formData, String formTemplateGuid, SlvData slvData) {
        try {
            String oldNoteGuid = edgeNote.getNoteGuid();
            String notebookGuid = null;
            notebookGuid = edgeNote.getEdgeNotebook().getNotebookGuid();
            List<EdgeFormData> edgeFormDataList = formData.getFormDef();
            JsonObject edgeNoteJsonObject = processEdgeForms(edgeNote, edgeFormDataList, formTemplateGuid, slvData);
            String newNoteGuid = edgeNoteJsonObject.get("noteGuid").getAsString();
            logger.info("ProcessedFormJson " + edgeNoteJsonObject.toString());
            ResponseEntity<String> responseEntity = updateNoteDetails(edgeNoteJsonObject.toString(), oldNoteGuid, notebookGuid);
            if (responseEntity.getStatusCode().value() == HttpStatus.CREATED.value()) {
                String body = responseEntity.getBody();
                slvData.setNewNoteGuid(body);
                slvData.setStatus("Success");
                logger.info("Success note : " + edgeNote.getTitle());
            } else {
                logger.info("Failure Note : " + edgeNote.getTitle());
            }
        } catch (Exception e) {
            slvData.setStatus("Failure");
            slvData.setErrorDetails(e.getMessage());
        }
        return slvData;
    }
}
