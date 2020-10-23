package com.terragoedge.streetlight.edgeinterface;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.edgeserver.FormData;
import com.terragoedge.streetlight.dao.StreetlightDao;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.terrago.streetlights.utils.PropertiesReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SlvToEdgeService extends EdgeService {
    private Gson gson;
    private StreetlightDao streetlightDao;

    public SlvToEdgeService() {
        this.gson = new Gson();
        streetlightDao = new StreetlightDao();
    }

    public void run(SlvData slvData) {
      //  logger.info("-----SLV to Edge Sync Process------------");
       // logger.info(slvData.toString());

        logger.info("processing title " + slvData.getNoteTitle());
        String formTemplateGuid = PropertiesReader.getProperties().getProperty("amerescousa.edge.formtemplateGuid");
        List<com.terragoedge.edgeserver.SlvData> noteGuidList = streetlightDao.getNoteDetails(slvData.getNoteTitle());
        for (com.terragoedge.edgeserver.SlvData dbSLVData : noteGuidList) {
            slvData.setNoteGuid(dbSLVData.getGuid().trim());
            String notesJson = getNoteDetails(slvData.getNoteGuid());
            if (notesJson == null) {
                logger.info("Note not in AmerescoUSA.");
                return;
            }
            Type listType = new TypeToken<ArrayList<EdgeNote>>() {
            }.getType();
            Gson gson = new Gson();
            List<EdgeNote> edgeNoteList = new ArrayList<>();
            //    List<EdgeNote> edgeNoteList = gson.fromJson(notesJson, listType);
            EdgeNote restEdgeNote = gson.fromJson(notesJson, EdgeNote.class);
            edgeNoteList.add(restEdgeNote);
            for (EdgeNote edgeNote : edgeNoteList) {
                List<FormData> formDataList = edgeNote.getFormData();
                
                //edgeNote.getEdgeNotebook().getNotebookGuid();
                for (FormData formData : formDataList) {
                    //if (formData.getFormTemplateGuid().equals(formTemplateGuid)) {
                        //String notebookguid = streetlightDao.getNoteBookGUID(slvData.getLocation_atlasphysicalpage());
                        String createdBy = edgeNote.getCreatedBy();
                        long createddatetime = edgeNote.getCreatedDateTime();
                        Long currentTime = System.currentTimeMillis();
                        //SlvData resultSlvData = processInstallationForm(edgeNote, formData, formTemplateGuid, slvData);
                        SlvData resultSlvData = processInstallationForm_altaspageupdate(edgeNote, formData, formTemplateGuid,
                                slvData,currentTime);


                        return;
                    //}
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
          //  logger.info("ProcessedFormJson " + edgeNoteJsonObject.toString());
            ResponseEntity<String> responseEntity = updateNoteDetails(edgeNoteJsonObject.toString(), oldNoteGuid, notebookGuid);
            if (responseEntity.getStatusCode().value() == HttpStatus.CREATED.value()) {
                String body = responseEntity.getBody();
                slvData.setNewNoteGuid(body);
                slvData.setStatus("Success");
               // logger.info("Success note : " + edgeNote.getTitle());
            } else {
               // logger.info("Failure Note : " + edgeNote.getTitle());
            }
        } catch (Exception e) {
            slvData.setStatus("Failure");
            slvData.setErrorDetails(e.getMessage());
        }
        return slvData;
    }

    private void updateLocationData(EdgeNote edgeNote,String longitude,String latitude)
    {
        String geomStr = "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Point\",\"coordinates\":[";
        geomStr =  geomStr + longitude + "," + latitude;
        geomStr = geomStr + "]}}";
        edgeNote.setGeometry(geomStr);
    }
    public SlvData processInstallationForm_altaspageupdate(
            EdgeNote edgeNote, FormData formData, String formTemplateGuid, SlvData slvData, Long currentDateTime) {
        try {
            String oldNoteGuid = edgeNote.getNoteGuid();
            String notebookGuid = null;
            notebookGuid = edgeNote.getEdgeNotebook().getNotebookGuid();
            List<EdgeFormData> edgeFormDataList = formData.getFormDef();
            String strLongitude = slvData.getSlvLongitude();
            String strLatitude = slvData.getSlvLatitude();
            if(strLatitude != null && strLongitude != null)
            {
                updateLocationData(edgeNote,strLongitude,strLatitude);
            }
            edgeNote.setCreatedDateTime(currentDateTime);
            //JsonObject edgeNoteJsonObject = processEdgeForms(edgeNote, edgeFormDataList, formTemplateGuid, slvData);
            JsonObject edgeNoteJsonObject = processEdgeForms_altaspage(edgeNote, edgeFormDataList, formTemplateGuid, slvData,null);
            edgeNoteJsonObject.addProperty("createdBy","milhouseinterface");


            String newNoteGuid = edgeNoteJsonObject.get("noteGuid").getAsString();
            //  logger.info("ProcessedFormJson " + edgeNoteJsonObject.toString());
            //ResponseEntity<String> responseEntity = updateNoteDetails(edgeNoteJsonObject.toString(), oldNoteGuid, notebookGuid);
            ResponseEntity<String> responseEntity = updateNoteDetails(edgeNoteJsonObject.toString(), oldNoteGuid, notebookGuid);
            if (responseEntity.getStatusCode().value() == HttpStatus.CREATED.value()) {
                String body = responseEntity.getBody();
                System.out.println("!!!!!!!!!!! " + body + "!!!!!!!!!!!!!!1");
                slvData.setNewNoteGuid(body);
                slvData.setStatus("Success");
                // logger.info("Success note : " + edgeNote.getTitle());
            } else {
                // logger.info("Failure Note : " + edgeNote.getTitle());
            }
        } catch (Exception e) {
            slvData.setStatus("Failure");
            slvData.setErrorDetails(e.getMessage());
            e.printStackTrace();
        }
        return slvData;
    }
}
