package com.terragoedge.streetlight.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.edgeserver.EdgeNotebook;
import com.terragoedge.edgeserver.FormData;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.comparator.GroupRepeatableComparator;
import com.terragoedge.streetlight.dao.StreetlightDao;
import com.terragoedge.streetlight.enumeration.PoleStatus;
import com.terragoedge.streetlight.exception.NotesNotFoundException;
import com.terragoedge.streetlight.json.model.ErrorMessageModel;
import com.terragoedge.streetlight.json.model.FailureFormDBmodel;
import com.terragoedge.streetlight.json.model.FailureReportModel;
import com.terragoedge.streetlight.json.model.GeozoneModel;
import com.terragoedge.streetlight.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FailureReportService extends FailureAbstractService {
    final Logger logger = Logger.getLogger(FailureReportService.class);
    private RestService restService = null;
    private JsonParser jsonParser = null;
    private Gson gson = null;
    private String errorFormJson;
    //  private String tempResponse;
    private StreetlightDao streetlightDao = null;
    ExecutorService executor = Executors.newFixedThreadPool(2);

    public FailureReportService() {
        restService = new RestService();
        jsonParser = new JsonParser();
        gson = new Gson();
        streetlightDao = new StreetlightDao();
    }

    public void loadErrorFormJson() {
        try {
            logger.info("Loading Error FromTemplate.");
            FileInputStream fis = new FileInputStream("./resources/ErrorForm.json");
            //FileInputStream fis = new FileInputStream("./src/main/resources/ErrorForm.json");
            errorFormJson = IOUtils.toString(fis);

            // FileInputStream fisTemp = new FileInputStream("./src/main/resources/failurereportjson.txt");
            // FileInputStream fisTemp = new FileInputStream("./resources/failurereportjson.txt");
            //tempResponse = IOUtils.toString(fisTemp);

        } catch (Exception e) {
            logger.error("Error in loadErrorFormJson", e);
        }
    }

    public void run() {
        loadErrorFormJson();
        System.out.println("process started");
        Set<String> fixtureIdList = streetlightDao.getFixtureId();
        Set<String> processedFixtureIds = new TreeSet<>();
        // String accessToken = getEdgeToken();
        String url = PropertiesReader.getProperties().getProperty("streetlight.slv.url.main");
        url = url + PropertiesReader.getProperties().getProperty("streetlight.slv.geozoneUrl");
        List<GeozoneModel> geozoneModelList = getGeozoneModelList(url);
        System.out.println("GeozoneSize" + geozoneModelList.size());
        logger.info("GeozoneModelList Size:" + geozoneModelList.size());
        logger.info("FixtureIdList Size:" + fixtureIdList.size());
        logger.info("FixtureIdList Size:" + fixtureIdList.toString());
        for (GeozoneModel geozoneModel : geozoneModelList) {
            if (geozoneModel.getChildrenCount() == 0) {
                List<FailureReportModel> failureReportModelList = new ArrayList<>();
                processFailureReport(geozoneModel, failureReportModelList);

                for (FailureReportModel failureReportModel : failureReportModelList) {
                    logger.info("ProcessForm Started Title " + failureReportModel.toString());
                    System.out.println("title is:" + failureReportModel.getFixtureId());

                    if (failureReportModel.isOutage() || failureReportModel.isWarning()) {
                        Runnable processTask = new ProcessTask(failureReportModel);
                        executor.execute(processTask);
                    }
                    // processErrorForm(failureReportModel);
                    processedFixtureIds.add(failureReportModel.getFixtureId());
                }
            }
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Finished all threads");
        fixtureIdList.removeAll(processedFixtureIds);
        System.out.println("FixtureIdList: " + fixtureIdList.toString());
        logger.info("completedFixtureId" + fixtureIdList.toString());
        for (String fixtureId : fixtureIdList) {
            try {
                FailureFormDBmodel failureFormDBmodel = new FailureFormDBmodel();
                failureFormDBmodel.setNoteName(fixtureId);
                failureFormDBmodel.setPoleStatus(PoleStatus.FIXED.toString());
                processResolvedForm(fixtureId, failureFormDBmodel);
            } catch (Exception e) {
                logger.error("Error while processing :" + fixtureId, e);
            }

        }
    }

    public void processFailureReport(GeozoneModel geozoneModel, List<FailureReportModel> failureReportModelList) {
        try {
            // logger.info("Getting Failure Report for "+geozoneModel.getId());
            JsonObject jsonObject = getFailureReport(geozoneModel);
            JsonObject jsonSubObject = jsonObject.get("properties").getAsJsonObject();
            JsonElement jsonElement = jsonSubObject.get("rows");
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            if (jsonObject != null) {
                for (JsonElement jsonElementObject : jsonArray) {
                    FailureReportModel failureReportModel = new FailureReportModel();
                    failureReportModelList.add(failureReportModel);
                    JsonObject failureObject = jsonElementObject.getAsJsonObject();
                    String title = null;
                    String properties = null;
                    if (failureObject.get("label") != null) {
                        title = failureObject.get("label").toString();
                        failureReportModel.setLabel(title);
                        String splitValues[] = title.split("-");
                        if (splitValues.length > 3) {
                            failureReportModel.setFixtureId(splitValues[2]);
                        }
                    }
                    if (failureObject.get("properties") != null) {
                        properties = failureObject.get("properties").toString();
                        failureReportModel.setProperties(properties);
                    }
                    JsonArray jsonValuesArray = failureObject.get("value").getAsJsonArray();
                    setFailureModelObject(jsonValuesArray, failureReportModel);
                }
            }
        } catch (Exception e) {
            logger.error("Error in processFailureReport", e);
        }

    }

    public void processErrorForm(FailureReportModel failureReportModel) {
        FailureFormDBmodel failureFormDBmodel = new FailureFormDBmodel();
        failureFormDBmodel.setNoteName(failureReportModel.getFixtureId());
        failureFormDBmodel.setPoleStatus(PoleStatus.PROBLEM.toString());
        try {
            String notesJson = getNoteDetails(failureReportModel.getFixtureId());
            if (notesJson == null) {
                logger.info("Note not in Edge.");
                throw new NotesNotFoundException("Note [" + failureReportModel.getFixtureId() + "] not in Edge.");
            }

            Type listType = new TypeToken<ArrayList<EdgeNote>>() {
            }.getType();
            List<EdgeNote> edgeNoteList = gson.fromJson(notesJson, listType);
            for (EdgeNote edgeNote : edgeNoteList) {
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
        if (isAlreadyProcessedError(edgeFormDatas, failureReportModel)) {
            System.out.println("Already Exist: " + failureReportModel.getFixtureId());
            return;
        }
       /* if (typeValue != null && failureValue != null && !typeValue.trim().isEmpty()) {
            processNewErrorForm(type, edgeFormDataList, failureReportModel, typeId, failureId, lastUpdateId, failedSinceId, burningId, lifeTimeId);
        } else*/
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
        //setLayer as Outage
        String outageLayerGuid = PropertiesReader.getProperties().getProperty("outage_layer.guid");
        String notebookGuid = edgeNote.getEdgeNotebook().getNotebookGuid();
        String oldNoteGuid = edgeNote.getNoteGuid();
        JsonObject edgeNoteJsonObject = processEdgeForms(gson.toJson(edgeNote), edgeFormDatas, formTemplateGuid);
        String newNoteGuid = edgeNoteJsonObject.get("noteGuid").getAsString();
        setGroupValue(outageLayerGuid, edgeNoteJsonObject);
        logger.info("ProcessedFormJson " + edgeNoteJsonObject.toString());
        ResponseEntity<String> responseEntity = updateNoteDetails(edgeNoteJsonObject.toString(), oldNoteGuid, notebookGuid);
        failureFormDBmodel.setErrorDetails(responseEntity.getBody());
        System.out.println("ProcessedNote is :" + edgeNote.getTitle());
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

    public void setFailureModelObject(JsonArray jsonValuesArray, FailureReportModel failureReportModel) {
        String lifeTime = "";
        String burningHours = "";
        String failureSince = "";
        String lastUpdate = "";
        boolean warning = false;
        boolean outage = false;
        System.out.println(jsonValuesArray.toString());
        if (!jsonValuesArray.get(0).isJsonNull()) {
            warning = jsonValuesArray.get(0).getAsBoolean();
        }
        if (!jsonValuesArray.get(1).isJsonNull()) {
            outage = jsonValuesArray.get(1).getAsBoolean();
        }
        if (!jsonValuesArray.get(2).isJsonNull()) {
            JsonArray errorMessage = jsonValuesArray.get(2).getAsJsonArray();
            if (errorMessage != null && !errorMessage.equals("(null)")) {
                List<String> messageList = new ArrayList<>();
                List<ErrorMessageModel> errorMessageModelList = gson.fromJson(errorMessage, new TypeToken<List<ErrorMessageModel>>() {
                }.getType());
                for (ErrorMessageModel errorMessageModel : errorMessageModelList) {
                    messageList.add(errorMessageModel.getLabel());
                }
                String finalErrorReport = StringUtils.join(messageList, ",");
                failureReportModel.setFailureReason(finalErrorReport);
            }

        }
        if (!jsonValuesArray.get(3).isJsonNull()) {
            lastUpdate = jsonValuesArray.get(3).getAsString();
        }
        if (!jsonValuesArray.get(4).isJsonNull()) {
            failureSince = jsonValuesArray.get(4).getAsString();
        }
        if (!jsonValuesArray.get(5).isJsonNull()) {
            burningHours = jsonValuesArray.get(5).getAsString();
        }
        if (!jsonValuesArray.get(6).isJsonNull()) {
            lifeTime = jsonValuesArray.get(6).toString();
        }
        failureReportModel.setWarning(warning);
        failureReportModel.setOutage(outage);
        failureReportModel.setLastUpdate(lastUpdate);
        failureReportModel.setFailedSince(failureSince);
        failureReportModel.setBurningHours(burningHours);
        failureReportModel.setLifeTime(lifeTime);
    }

    public JsonObject getFailureReport(GeozoneModel geozoneModel) {
        //return (JsonObject) jsonParser.parse(tempResponse);
        String url = PropertiesReader.getProperties().getProperty("streetlight.slv.url.main");
        String failureUrl = PropertiesReader.getProperties().getProperty("streetlight.slv.failurereport");
        List<Object> paramsList = new ArrayList<>();
        paramsList.add("groupId=" + geozoneModel.getId());
        paramsList.add("reportBuilderClassName=FailuresApplicationReportBuilder");
        paramsList.add("ser=json");
        paramsList.add("reportPropertyName=detail");
        paramsList.add("reportPropertyValue=2");
        paramsList.add("time=" + System.currentTimeMillis());
        String params = StringUtils.join(paramsList, "&");
        String failureReportUrl = url + failureUrl + "?" + params;
        logger.info("Url to get Failure Report:");
        logger.info("Url:" + failureReportUrl);
        ResponseEntity<String> response = restService.getPostRequest(failureReportUrl, null);
        logger.info("Response Code:" + response.getStatusCodeValue());
        if (response.getStatusCodeValue() == 200) {
            String responseString = response.getBody();
            logger.info("--------Response----------");
            logger.info(responseString);
            // return (JsonObject) jsonParser.parse(tempResponse);
            return (JsonObject) jsonParser.parse(responseString);
        }
        return null;

    }

    public List<GeozoneModel> getGeozoneModelList(String url) {
        List<GeozoneModel> geozoneModels = new ArrayList<>();
        ResponseEntity<String> responseEntity = restService.getPostRequest(url, null);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseJson = responseEntity.getBody();
            geozoneModels = gson.fromJson(responseJson, new TypeToken<List<GeozoneModel>>() {
            }.getType());
            return geozoneModels;
        } else {
            logger.error("Unable to get message from EdgeServer. Response Code is :" + responseEntity.getStatusCode());
        }
        return new ArrayList<GeozoneModel>();

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
        EdgeFormData edgeFormData = Collections.max(edgeFormDataList, new GroupRepeatableComparator());
        return edgeFormData.getGroupRepeatableCount();
    }

    public void processResolvedForm(String fixtureId, FailureFormDBmodel failureFormDBmodel) {
        String notebookGuid = null;
        String edgenoteJson = getNoteDetails(fixtureId);
        Type listType = new TypeToken<ArrayList<EdgeNote>>() {
        }.getType();
        List<EdgeNote> edgeNoteList = gson.fromJson(edgenoteJson, listType);
        for (EdgeNote edgeNote : edgeNoteList) {
            String outageLayerGuid = PropertiesReader.getProperties().getProperty("complete_layer.guid");
            if (edgeNote.getEdgeNotebook() != null) {
                EdgeNotebook edgeNotebook = edgeNote.getEdgeNotebook();
                notebookGuid = edgeNotebook.getNotebookGuid();
            }
            String noteguid = edgeNote.getNoteGuid();
            JsonObject edgeJsonObject = (JsonObject) jsonParser.parse(gson.toJson(edgeNote));
            setGroupValue(outageLayerGuid, edgeJsonObject);
            JsonArray serverEdgeFormJsonArray = edgeJsonObject.get("formData").getAsJsonArray();
            int size = serverEdgeFormJsonArray.size();
            for (int i = 0; i < size; i++) {
                JsonObject serverEdgeForm = serverEdgeFormJsonArray.get(i).getAsJsonObject();
                String formDefJson = serverEdgeForm.get("formDef").getAsString();
                formDefJson = formDefJson.replace("\\\\", "");
                List<EdgeFormData> edgeFormDataList = getEdgeFormData(formDefJson);
                serverEdgeForm.add("formDef", gson.toJsonTree(edgeFormDataList));
                serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
            }
            edgeJsonObject.add("formData", serverEdgeFormJsonArray);
            logger.info("ProcessResolvedData " + edgeJsonObject.toString());
            edgeJsonObject.addProperty("createdDateTime", System.currentTimeMillis());
            edgeJsonObject.addProperty("noteGuid", UUID.randomUUID().toString());
            ResponseEntity<String> responseEntity = updateNoteDetails(edgeJsonObject.toString(), noteguid, notebookGuid);
            logger.info("edgenote update to server: " + responseEntity.getBody());

            failureFormDBmodel.setNoteid(edgeNote.getNoteGuid());
            failureFormDBmodel.setCreatedDatetime(String.valueOf(edgeNote.getCreatedDateTime()));
            failureFormDBmodel.setProcessDateTime(String.valueOf(System.currentTimeMillis()));
            failureFormDBmodel.setNewNoteGuid(responseEntity.getBody());
            streetlightDao.update(failureFormDBmodel);
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
            String lastUpdatedValue = Utils.getRepeatableFormValue(edgeFormDataList, 1, Integer.parseInt(lastUpdateId), reapeatableCount);
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
            }
            return isNeedProcess;
        }
        return false;
    }
}
