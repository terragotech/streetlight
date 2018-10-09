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
    private StreetlightDao streetlightDao = null;
    ExecutorService executor = Executors.newFixedThreadPool(2);

    public FailureReportService() {
        restService = new RestService();
        jsonParser = new JsonParser();
        gson = new Gson();
        streetlightDao = new StreetlightDao();
    }

    public void loadErrorFormJson() {
        FileInputStream fis = null;
        try {
            logger.info("Loading Error FromTemplate.");
            fis = new FileInputStream("./resources/ErrorForm.json");
            errorFormJson = IOUtils.toString(fis);
        } catch (Exception e) {
            logger.error("Error in loadErrorFormJson", e);
        }finally{
            if(fis != null){
                try{
                    fis.close();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }

    public void run() {
        loadErrorFormJson();
        logger.info("process started");
        Set<String> fixtureIdList = streetlightDao.getFixtureId();
        Set<String> processedFixtureIds = new TreeSet<>();
        // String accessToken = getEdgeToken();
        String url = PropertiesReader.getProperties().getProperty("streetlight.slv.url.main");
        url = url + PropertiesReader.getProperties().getProperty("streetlight.slv.geozoneUrl");
        List<GeozoneModel> geozoneModelList = getGeozoneModelList(url);
        logger.info("GeozoneModelList Size:" + geozoneModelList.size());
        logger.info("FixtureIdList Size:" + fixtureIdList.size());
        logger.info("FixtureIdList Data:" + fixtureIdList.toString());
        for (GeozoneModel geozoneModel : geozoneModelList) {
            if (geozoneModel.getChildrenCount() == 0) {
                List<FailureReportModel> failureReportModelList = new ArrayList<>();
                processFailureReport(geozoneModel, failureReportModelList);

                for (FailureReportModel failureReportModel : failureReportModelList) {
                    logger.info("ProcessForm Started Title " + failureReportModel.toString());

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
            if (jsonObject != null) {
                JsonObject jsonSubObject = jsonObject.get("properties").getAsJsonObject();
                JsonElement jsonElement = jsonSubObject.get("rows");
                JsonArray jsonArray = jsonElement.getAsJsonArray();

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


}
