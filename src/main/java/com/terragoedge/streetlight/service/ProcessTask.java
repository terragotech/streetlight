package com.terragoedge.streetlight.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.edgeserver.FormData;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.comparator.GroupRepeatableComparator;
import com.terragoedge.streetlight.dao.StreetlightDao;
import com.terragoedge.streetlight.enumeration.PoleStatus;
import com.terragoedge.streetlight.exception.NotesNotFoundException;
import com.terragoedge.streetlight.json.model.FailureFormDBmodel;
import com.terragoedge.streetlight.json.model.FailureReportModel;
import com.terragoedge.streetlight.utils.Utils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.ResponseEntity;

import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.util.*;

public class ProcessTask extends FailureAbstractService implements Runnable {
    FailureReportModel failureReportModel = null;
    Gson gson = null;
    private StreetlightDao streetlightDao = null;
    JsonParser jsonParser = null;
 //   private String tempResponse = null;
    private String errorFormJson;
    private FailureReportService failureReportService = null;

    public ProcessTask(FailureReportModel failureReportModel) {
        this.failureReportModel = failureReportModel;
        streetlightDao = new StreetlightDao();
        jsonParser = new JsonParser();
        gson = new Gson();
        failureReportService = new FailureReportService();
        loadErrorFormJson();
    }

    @Override
    public void run() {
        FailureFormDBmodel failureFormDBmodel = new FailureFormDBmodel();
        failureFormDBmodel.setNoteName(failureReportModel.getFixtureId());
        failureFormDBmodel.setPoleStatus(PoleStatus.PROBLEM.toString());
        try {
            FailureReportModel dbFailureReportModel = streetlightDao.getProcessedReportsByFixtureId(failureReportModel.getFixtureId());
            if (dbFailureReportModel != null) {
                if (dbFailureReportModel.getFailureReason() != null && dbFailureReportModel.getFailureReason().equals(failureReportModel.getFailureReason())) {
                    logger.info("Already Exist LocalDB: " + failureReportModel.getFixtureId());
                    return;
                } else if (dbFailureReportModel.isOutage() != failureReportModel.isOutage()) {
                    logger.info("Set CompleteLayer"+failureReportModel.getFixtureId());
                    failureReportModel.setComplete(true);
                }
            }
            logger.info("Set OutageLayer"+failureReportModel.getFixtureId());
            String notesJson = getNoteDetails(failureReportModel.getFixtureId());
            if (notesJson == null) {
                logger.info("Note not in Edge.");
                return;
            }

            Type listType = new TypeToken<ArrayList<EdgeNote>>() {
            }.getType();
            List<EdgeNote> edgeNoteList = gson.fromJson(notesJson, listType);
            for (EdgeNote edgeNote : edgeNoteList) {
                if(edgeNote.getTitle().equals(failureReportModel.getFixtureId())){
                    List<FormData> formDatasList = edgeNote.getFormData();
                    Map<String, FormData> formDataMaps = new HashMap<>();
                    for (FormData formData : formDatasList) {
                        formDataMaps.put(formData.getFormTemplateGuid(), formData);
                    }
                    String errorFormTemplateGuid = PropertiesReader.getProperties().getProperty("streetlight.edge.formtemplateguid.errorform");
                    FormData errorFormData = formDataMaps.get(errorFormTemplateGuid);
                    failureFormDBmodel.setNoteid(edgeNote.getNoteGuid());
                    failureFormDBmodel.setCreatedDatetime(String.valueOf(edgeNote.getCreatedDateTime()));
                    processErrorFormTemplate(errorFormData, edgeNote, failureReportModel, errorFormTemplateGuid, failureFormDBmodel);
                }

            }

        } catch (Exception e) {
            logger.error("Error in processErrorForm", e);
        }

    }

    public void processErrorFormTemplate(FormData formData, EdgeNote edgeNote, FailureReportModel failureReportModel, String formTemplateGuid, FailureFormDBmodel failureFormDBmodel) throws Exception {
        if (gson == null) {
            gson = new Gson();
        }
        failureFormDBmodel.setModelJson(gson.toJson(failureReportModel));
        List<EdgeFormData> edgeFormDataList = null;
        boolean isExist = true;
        if (formData == null) {
            formData = readNewFormVal();
            isExist = false;
        }
        edgeFormDataList = formData.getFormDef();
        String typeId = PropertiesReader.getProperties().getProperty("ameresco.errorreport.typeid");
        String failureId = PropertiesReader.getProperties().getProperty("ameresco.errorreport.failureid");
        String lastUpdateId = PropertiesReader.getProperties().getProperty("ameresco.errorreport.lastupdateid");
        String failedSinceId = PropertiesReader.getProperties().getProperty("ameresco.errorreport.failuresinceid");
        String burningId = PropertiesReader.getProperties().getProperty("ameresco.errorreport.burininghourid");
        String lifeTimeId = PropertiesReader.getProperties().getProperty("ameresco.errorreport.lifetimeid");
        String typeValue = Utils.getFormValue(edgeFormDataList, Integer.parseInt(typeId));
        String failureValue = Utils.getFormValue(edgeFormDataList, Integer.parseInt(failureId));
        List<EdgeFormData> edgeFormDatas = new ArrayList<>();
        edgeFormDatas.addAll(edgeFormDataList);
        String type = "";
        if (failureReportModel.isWarning()) {
            type = "Warning";
        }
        if (failureReportModel.isOutage()) {
            type = type + "," + "Outage";
        }

        if (!isExist) {
            processNewErrorForm(type, edgeFormDataList, failureReportModel, typeId, failureId, lastUpdateId, failedSinceId, burningId, lifeTimeId);
        } else {
            int maxGroupRecount = getLastRepeatableCount(edgeFormDataList);
            int newRepeatableCount = maxGroupRecount + 1;
            List<EdgeFormData> newEdgeFormDataList = readNewFormVal().getFormDef();
            Utils.updateFormValueAndRepeatableCount(newEdgeFormDataList, Integer.parseInt(typeId), type, newRepeatableCount);
            Utils.updateFormValueAndRepeatableCount(newEdgeFormDataList, Integer.parseInt(failureId), failureReportModel.getFailureReason(), newRepeatableCount);
            Utils.updateFormValueAndRepeatableCount(newEdgeFormDataList, Integer.parseInt(lastUpdateId), failureReportModel.getLastUpdate(), newRepeatableCount);
            Utils.updateFormValueAndRepeatableCount(newEdgeFormDataList, Integer.parseInt(failedSinceId), failureReportModel.getFailedSince(), newRepeatableCount);
            Utils.updateFormValueAndRepeatableCount(newEdgeFormDataList, Integer.parseInt(burningId), failureReportModel.getBurningHours(), newRepeatableCount);
            Utils.updateFormValueAndRepeatableCount(newEdgeFormDataList, Integer.parseInt(lifeTimeId), failureReportModel.getLifeTime(), newRepeatableCount);
            edgeFormDatas.addAll(newEdgeFormDataList);
        }
        Collections.sort(edgeFormDatas,new GroupRepeatableComparator(true));// sorting form defs by decending order to change the group position
        //setLayer as Outage
        String outageLayerGuid = PropertiesReader.getProperties().getProperty("outage_layer.guid");
        String completeLayerGuid = PropertiesReader.getProperties().getProperty("complete_layer.guid");
        String notebookGuid = edgeNote.getEdgeNotebook().getNotebookGuid();
        String oldNoteGuid = edgeNote.getNoteGuid();
        JsonObject edgeNoteJsonObject = processEdgeForms(gson.toJson(edgeNote), edgeFormDatas, formTemplateGuid);
        String newNoteGuid = edgeNoteJsonObject.get("noteGuid").getAsString();
        if (failureReportModel.isOutage()) {
           // edgeNoteJsonObject.remove("dictionary");
           // setGroupValue(outageLayerGuid, edgeNoteJsonObject);
        } else if (failureReportModel.isComplete()) {
            edgeNoteJsonObject.remove("dictionary");
            setGroupValue(completeLayerGuid, edgeNoteJsonObject);
            logger.info("CompletedLayer : " + edgeNote.getTitle());
        }
        logger.info("ProcessedFormJson " + edgeNoteJsonObject.toString());
        ResponseEntity<String> responseEntity = updateNoteDetails(edgeNoteJsonObject.toString(), oldNoteGuid, notebookGuid);
        failureFormDBmodel.setErrorDetails(responseEntity.getBody());
        logger.info("ProcessedNote is :" + edgeNote.getTitle());
        logger.info("edgenote update to server: " + responseEntity.getBody());
        //save
        failureFormDBmodel.setNewNoteGuid(newNoteGuid);
        failureFormDBmodel.setProcessDateTime(String.valueOf(System.currentTimeMillis()));
        failureFormDBmodel.setStatus("Success");
        streetlightDao.insertErrorFormNotes(failureFormDBmodel);
    }

    public JsonObject processEdgeForms(String edgenoteJson, List<EdgeFormData> edgeFormDataList, String errorFormTemplateGuid) {
        JsonObject edgeJsonObject = (JsonObject) jsonParser.parse(edgenoteJson);
        JsonArray serverEdgeFormJsonArray = edgeJsonObject.get("formData").getAsJsonArray();
        int size = serverEdgeFormJsonArray.size();
        boolean isExisterrorForm = false;
        for (int i = 0; i < size; i++) {
            JsonObject serverEdgeForm = serverEdgeFormJsonArray.get(i).getAsJsonObject();
            String currentFormTemplateGuid = serverEdgeForm.get("formTemplateGuid").getAsString();
            if (currentFormTemplateGuid.equals(errorFormTemplateGuid)) {
                serverEdgeForm.add("formDef", gson.toJsonTree(edgeFormDataList));
                serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
                isExisterrorForm = true;
            }
            String formDefJson = serverEdgeForm.get("formDef").toString();
            formDefJson = formDefJson.replaceAll("\\\\", "");
            formDefJson = formDefJson.replace("u0026","\\u0026");
            List<EdgeFormData> formDataList = getEdgeFormData(formDefJson);
            serverEdgeForm.add("formDef", gson.toJsonTree(formDataList));
            serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
        }
        if (!isExisterrorForm) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("formDef", gson.toJsonTree(edgeFormDataList));
            jsonObject.addProperty("formGuid", UUID.randomUUID().toString());
            jsonObject.addProperty("name", "Failure report");
            String formTemplateGuid = PropertiesReader.getProperties().getProperty("streetlight.edge.formtemplateguid.errorform");
            jsonObject.addProperty("formTemplateGuid", formTemplateGuid);
            jsonObject.addProperty("category", "Report");
            jsonObject.addProperty("formValidationAction", "Warning");
            serverEdgeFormJsonArray.add(jsonObject);
        }
        edgeJsonObject.add("formData", serverEdgeFormJsonArray);
        edgeJsonObject.addProperty("createdDateTime", System.currentTimeMillis());
        edgeJsonObject.addProperty("noteGuid", UUID.randomUUID().toString());
        return edgeJsonObject;
    }


    protected List<EdgeFormData> getEdgeFormData(String formDefJson) {
        try {
            List<EdgeFormData> edgeFormDatas = gson.fromJson(formDefJson, new TypeToken<List<EdgeFormData>>() {
            }.getType());
            return edgeFormDatas;
        } catch (Exception e) {
            formDefJson = formDefJson.substring(1, formDefJson.length() - 1);
            List<EdgeFormData> edgeFormDatas = gson.fromJson(formDefJson, new TypeToken<List<EdgeFormData>>() {
            }.getType());
            return edgeFormDatas;
        }

    }

    public EdgeNote processEdgeFormData(EdgeNote edgeNote, List<EdgeFormData> edgeFormDataList) {
        String errorFormTemplateGuid = PropertiesReader.getProperties().getProperty("streetlight.edge.formtemplateguid.errorform");
        List<FormData> formDataList = edgeNote.getFormData();
        List<FormData> newFormDataList = new ArrayList<>();
        for (FormData formData : formDataList) {
            String formTemplateGuid = formData.getFormTemplateGuid();
            if (formTemplateGuid.equals(errorFormTemplateGuid)) {
                FormData formDataTemp = new FormData();
                formDataTemp.setFormGuid(formData.getFormGuid());
                formDataTemp.setFormTemplateGuid(formData.getFormTemplateGuid());
                formDataTemp.setName(formData.getName());
                formDataTemp.setCategory(formData.getCategory());
                formDataTemp.setFormDef(gson.toJsonTree(edgeFormDataList).getAsString());
                newFormDataList.add(formDataTemp);
            } else {
                newFormDataList.add(formData);
            }
        }
        if (edgeNote.getFormData().size() == 0) {
        }
        edgeNote.setFormData(newFormDataList);
        return edgeNote;
    }

    public void processNewErrorForm(String type, List<EdgeFormData> edgeFormDataList, FailureReportModel failureReportModel, String typeId, String failureId, String lastUpdatedId, String failedSinceId, String burningId, String lifeTimeId) {
        Utils.updateFormValue(edgeFormDataList, Integer.parseInt(typeId), type);
        Utils.updateFormValue(edgeFormDataList, Integer.parseInt(failureId), failureReportModel.getFailureReason());
        Utils.updateFormValue(edgeFormDataList, Integer.parseInt(lastUpdatedId), failureReportModel.getLastUpdate());
        Utils.updateFormValue(edgeFormDataList, Integer.parseInt(failedSinceId), failureReportModel.getFailedSince());
        Utils.updateFormValue(edgeFormDataList, Integer.parseInt(burningId), failureReportModel.getBurningHours());
        Utils.updateFormValue(edgeFormDataList, Integer.parseInt(lifeTimeId), failureReportModel.getLifeTime());
    }

    private FormData readNewFormVal() throws Exception {
        Gson gson = new Gson();
        FormData formData = gson.fromJson(errorFormJson, FormData.class);
        return formData;
    }

    public void loadErrorFormJson() {
        try {
            logger.info("Loading Error FromTemplate.");
            FileInputStream fis = new FileInputStream("./resources/ErrorForm.json");
            //FileInputStream fis = new FileInputStream("./src/main/resources/ErrorForm.json");
            errorFormJson = IOUtils.toString(fis);

           // FileInputStream fisTemp = new FileInputStream("./src/main/resources/failurereportjson.txt");
           // tempResponse = IOUtils.toString(fisTemp);
        } catch (Exception e) {
            logger.error("Error in loadErrorFormJson", e);
        }
    }

    public boolean isAlreadyProcessedError(List<EdgeFormData> edgeFormDataList, FailureReportModel failureReportModel) {
        String typeId = PropertiesReader.getProperties().getProperty("ameresco.errorreport.typeid");
        String failureId = PropertiesReader.getProperties().getProperty("ameresco.errorreport.failureid");
        String lastUpdateId = PropertiesReader.getProperties().getProperty("ameresco.errorreport.lastupdateid");
        String failedSinceId = PropertiesReader.getProperties().getProperty("ameresco.errorreport.failuresinceid");
        String burningId = PropertiesReader.getProperties().getProperty("ameresco.errorreport.burininghourid");
        String lifeTimeId = PropertiesReader.getProperties().getProperty("ameresco.errorreport.lifetimeid");
        boolean isNeedProcess = true;
        int reapeatableCount = getLastRepeatableCount(edgeFormDataList);
        if (reapeatableCount != -1) {
            String type = Utils.getRepeatableFormValue(edgeFormDataList, 1, Integer.parseInt(typeId), reapeatableCount);
            String failureValue = Utils.getRepeatableFormValue(edgeFormDataList, 1, Integer.parseInt(failureId), reapeatableCount);
            if (failureValue != null && !failureValue.equals(failureReportModel.getFailureReason())) {
                isNeedProcess = false;
            }
            /*String lastUpdatedValue = Utils.getRepeatableFormValue(edgeFormDataList, 1, Integer.parseInt(lastUpdateId), reapeatableCount);
            if (lastUpdatedValue != null && !lastUpdatedValue.equals(failureReportModel.getLastUpdate())) {
                isNeedProcess = false;
            }
           String failedSinceValue = Utils.getRepeatableFormValue(edgeFormDataList, 1, Integer.parseInt(failedSinceId), reapeatableCount);
            if (failedSinceValue != null && !failedSinceValue.equals(failureReportModel.getFailedSince())) {
                isNeedProcess = false;
            }
            String burningValue = Utils.getRepeatableFormValue(edgeFormDataList, 1, Integer.parseInt(burningId), reapeatableCount);
            if (burningValue != null && !burningValue.equals(failureReportModel.getBurningHours())) {
                isNeedProcess = false;
            }
            String lifeTimeValue = Utils.getRepeatableFormValue(edgeFormDataList, 1, Integer.parseInt(lifeTimeId), reapeatableCount);
            if (lifeTimeValue != null && !lifeTimeValue.equals(failureReportModel.getLifeTime())) {
                isNeedProcess = false;
            }*/
            return isNeedProcess;
        }
        return false;
    }

    public void setGroupValue(String value, JsonObject notesJson) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("key", "groupGuid");
        jsonObject.addProperty("value", value);
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(jsonObject);
        notesJson.add("dictionary", jsonArray);
    }

    public int getLastRepeatableCount(List<EdgeFormData> edgeFormDataList) {
        EdgeFormData edgeFormData = Collections.max(edgeFormDataList, new GroupRepeatableComparator(false));
        return edgeFormData.getGroupRepeatableCount();
    }

}
