package com.terragoedge.slv2edge.metering.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.terragoedge.slv2edge.metering.dao.MeteringServiceDAO;
import com.terragoedge.slv2edge.metering.model.MeteringEntity;
import com.terragoedge.slvinterface.exception.NotesNotFoundException;
import com.terragoedge.slvinterface.model.EdgeFormData;
import com.terragoedge.slvinterface.service.EdgeService;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class MeteringInterface extends EdgeService {

    private Logger logger = Logger.getLogger(MeteringInterface.class);
    private MeteringServiceDAO meteringServiceDAO;
    Properties mainProperties;

    public void run(){
        try{
            List<MeteringEntity> meteringEntityList = meteringServiceDAO.getMeteringEntityList();
            String formTemplateGuid = mainProperties.getProperty("edge.metering.formtemplate.guid");
            String mainUrl = mainProperties.getProperty("edge.server.url");
            for(MeteringEntity meteringEntity : meteringEntityList){
                processEdgeNote(meteringEntity,formTemplateGuid,mainUrl);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void processEdgeNote(MeteringEntity meteringEntity, String formTemplateGuid, String mainUrl) {
        try {
            String notesJson = geNoteDetails(mainUrl, meteringEntity.getIdOnController());
            if (notesJson == null) {
                logger.info("Note not in Edge.");
                throw new NotesNotFoundException("Note [" + meteringEntity.getIdOnController() + "] not in Edge.");
            }

            JsonObject edgeJsonObj = (JsonObject) jsonParser.parse(notesJson);
            String notebookGuid = null;
            if (edgeJsonObj.get("edgeNotebook") != null) {
                JsonObject notebookJson = edgeJsonObj.get("edgeNotebook").getAsJsonObject();
                notebookGuid = notebookJson.get("notebookGuid").getAsString();
            }
            String noteGuid = edgeJsonObj.get("noteGuid").getAsString();
            JsonArray serverEdgeFormJsonArray = edgeJsonObj.get("formData").getAsJsonArray();
            int size = serverEdgeFormJsonArray.size();
            boolean noteHasMeteringValue = false;
            for (int i = 0; i < size; i++) {
                JsonObject serverEdgeForm = serverEdgeFormJsonArray.get(i).getAsJsonObject();
                String currentFormTemplateGuid = serverEdgeForm.get("formTemplateGuid").getAsString();
                if (currentFormTemplateGuid.equals(formTemplateGuid)) {
                    String formDefJson = serverEdgeForm.get("formDef").getAsString();
                    formDefJson = formDefJson.replace("\\\\", "");
                    List<EdgeFormData> edgeFormDataList = getEdgeFormData(formDefJson);
                    populateMeteringValue(edgeFormDataList,meteringEntity);
                    serverEdgeForm.add("formDef", gson.toJsonTree(edgeFormDataList));
                    serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
                    noteHasMeteringValue = true;
                } else {
                    String formDefJson = serverEdgeForm.get("formDef").getAsString();
                    formDefJson = formDefJson.replace("\\\\", "");
                    List<EdgeFormData> edgeFormDataList = getEdgeFormData(formDefJson);
                    serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
                    serverEdgeForm.add("formDef", gson.toJsonTree(edgeFormDataList));

                }
            }

            if(!noteHasMeteringValue){
                JsonObject serverEdgeForm = loadMeteringFormJson();
                String formDefJson = serverEdgeForm.get("formDef").getAsString();
                formDefJson = formDefJson.replace("\\\\", "");
                List<EdgeFormData> edgeFormDataList = getEdgeFormData(formDefJson);
                populateMeteringValue(edgeFormDataList,meteringEntity);
                serverEdgeForm.add("formDef", gson.toJsonTree(edgeFormDataList));
                serverEdgeForm.addProperty("formTemplateGuid", formTemplateGuid);
                serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
            }
           // setGroupValue(completeGuid, edgeJsonObj);
            edgeJsonObj.add("formData", serverEdgeFormJsonArray);
            edgeJsonObj.addProperty("createdDateTime", System.currentTimeMillis());
            edgeJsonObj.addProperty("createdBy", "admin");
            edgeJsonObj.addProperty("noteGuid", UUID.randomUUID().toString());
            ResponseEntity<String> responseEntity = updateNoteDetails(mainUrl, edgeJsonObj.toString(), noteGuid, notebookGuid);
            System.out.println("success" + responseEntity.getBody());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void populateMeteringValue(List<EdgeFormData> edgeFormDataList,MeteringEntity meteringEntity){
        updateFormValue(edgeFormDataList, readPropertyValue("edge.metering.formtemplate.meteredpower.id"), "Complete");
        updateFormValue(edgeFormDataList, readPropertyValue("edge.metering.formtemplate.lampvoltage.id"), "Complete");
        updateFormValue(edgeFormDataList, readPropertyValue("edge.metering.formtemplate.dimminglevel.id"), "Complete");
        updateFormValue(edgeFormDataList, readPropertyValue("edge.metering.formtemplate.errorstatus.id"), "Complete");
    }

    private int readPropertyValue(String key){
       return Integer.valueOf(mainProperties.getProperty(key));
    }

    public JsonObject loadMeteringFormJson() {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("./resources/Metering.json");
           return (JsonObject) jsonParser.parse(IOUtils.toString(fis));
        } catch (Exception e) {
            logger.error("Error in loadErrorFormJson", e);
        }finally {
            if(fis != null){
                try{
                    fis.close();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
        return null;
    }



}
