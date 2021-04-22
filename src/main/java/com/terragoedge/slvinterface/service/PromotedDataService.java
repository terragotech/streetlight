package com.terragoedge.slvinterface.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.slvinterface.exception.InValidBarCodeException;
import com.terragoedge.slvinterface.model.EdgeFormData;
import com.terragoedge.slvinterface.model.EdgeNote;
import com.terragoedge.slvinterface.model.FormData;
import com.terragoedge.slvinterface.model.JPSWorkflowModel;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class PromotedDataService extends AbstractSlvService {
    private Logger logger = Logger.getLogger(PromotedDataService.class);
    private SlvRestService slvRestService;
    private JsonParser jsonParser;
    private Gson gson;
    private Properties properties;
    private String slvServerBaseUrl;
    public PromotedDataService() {
        gson = new Gson();
        properties = PropertiesReader.getProperties();
        jsonParser = new JsonParser();
        slvRestService = new SlvRestService();
        slvServerBaseUrl = properties.getProperty("streetlight.edge.slvserver.url");
    }

    private boolean changeNoteTitle(String noteguid,String newPoleNumber, String formTemplateGuid){
        boolean isTitleUpdated = false;
        String baseURl = properties.getProperty("streetlight.edge.url.main");
        try {
            if(newPoleNumber == null || newPoleNumber.equals("")){
                logger.error("new pole number is empty or null. it will create empty title. So it's skipping");
                return false;
            }
            ResponseEntity<String> responseEntity = slvRestService.serverCall(baseURl+"/rest/notes/" + noteguid, HttpMethod.GET, null,false);
            logger.info("Get notes rest call response: "+responseEntity.getStatusCode().value());
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                String body = responseEntity.getBody();
                if (body == null) {
                    logger.error("no note present in inventory server for this serial no: " + noteguid);
                } else {
                    JsonObject edgeJsonObject = jsonParser.parse(body).getAsJsonObject();
                    String oldNoteguid = edgeJsonObject.get("noteGuid").getAsString();
                    JsonElement notebookElement = edgeJsonObject.get("edgeNotebook");
                    if (notebookElement == null || notebookElement.isJsonNull()) {
                        logger.error("This senor is undefined: " + noteguid);
                    } else {
                        String title = edgeJsonObject.get("title").getAsString();
                        if (title.equals(newPoleNumber)) {
                            logger.info("Title already matched !");
                        } else {
                            String notebookGuid = edgeJsonObject.get("edgeNotebook").getAsJsonObject().get("notebookGuid").getAsString();
                            JsonArray serverEdgeFormJsonArray = edgeJsonObject.get("formData").getAsJsonArray();
                            int size = serverEdgeFormJsonArray.size();
                            for (int i = 0; i < size; i++) {
                                JsonObject serverEdgeForm = serverEdgeFormJsonArray.get(i).getAsJsonObject();
                                String formDefJson = serverEdgeForm.get("formDef").getAsString();
                                String currentTemplateGuid = serverEdgeForm.get("formTemplateGuid").getAsString();
                                formDefJson = formDefJson.replaceAll("\\\\", "");
                                List<EdgeFormData> edgeFormDataList = getEdgeFormData(formDefJson);
                                processCrewUserId(formTemplateGuid, edgeFormDataList, currentTemplateGuid, edgeJsonObject);
                                serverEdgeForm.add("formDef", gson.toJsonTree(edgeFormDataList));
                                serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
                            }

                            edgeJsonObject.add("formData", serverEdgeFormJsonArray);
                            edgeJsonObject.addProperty("noteGuid", UUID.randomUUID().toString());
                            edgeJsonObject.addProperty("createdDateTime", System.currentTimeMillis());
                            edgeJsonObject.addProperty("createdBy", "admin");
                            edgeJsonObject.addProperty("title", newPoleNumber);
                            ResponseEntity<String> responseEntity1 = slvRestService.serverCall(baseURl+"/rest/notebooks/" + notebookGuid + "/notes/" + oldNoteguid, HttpMethod.PUT, gson.toJson(edgeJsonObject),false);
                            isTitleUpdated = true;
                            logger.info("Update note rest call response: "+responseEntity1.getStatusCode().value());
                        }

                    }
                }
            } else {
                logger.error("Error while getting inventory note: " + responseEntity.getBody());
            }
        }catch (Exception e){
            logger.error("Error in changeNoteTitle: ",e);
        }
        return isTitleUpdated;
    }

    public void updateCrewUserOnNote(String noteguid, String formTemplateGuid){
        String baseURl = properties.getProperty("streetlight.edge.url.main");
        try {
            ResponseEntity<String> responseEntity = slvRestService.serverCall(baseURl+"/rest/notes/" + noteguid, HttpMethod.GET, null,false);
            logger.info("Get notes rest call response: "+responseEntity.getStatusCode().value());
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                String body = responseEntity.getBody();
                if (body == null) {
                    logger.error("no note present in inventory server for this serial no: " + noteguid);
                } else {
                    JsonObject edgeJsonObject = jsonParser.parse(body).getAsJsonObject();
                    String oldNoteguid = edgeJsonObject.get("noteGuid").getAsString();
                    JsonElement notebookElement = edgeJsonObject.get("edgeNotebook");
                    if (notebookElement == null || notebookElement.isJsonNull()) {
                        logger.error("This senor is undefined: " + noteguid);
                    } else {
                            String notebookGuid = edgeJsonObject.get("edgeNotebook").getAsJsonObject().get("notebookGuid").getAsString();
                            JsonArray serverEdgeFormJsonArray = edgeJsonObject.get("formData").getAsJsonArray();
                            int size = serverEdgeFormJsonArray.size();
                            boolean isCrewUserUpdated = false;
                            for (int i = 0; i < size; i++) {
                                JsonObject serverEdgeForm = serverEdgeFormJsonArray.get(i).getAsJsonObject();
                                String formDefJson = serverEdgeForm.get("formDef").getAsString();
                                String currentTemplateGuid = serverEdgeForm.get("formTemplateGuid").getAsString();
                                formDefJson = formDefJson.replaceAll("\\\\", "");
                                List<EdgeFormData> edgeFormDataList = getEdgeFormData(formDefJson);
                                boolean formUpdated = processCrewUserId(formTemplateGuid, edgeFormDataList, currentTemplateGuid, edgeJsonObject);
                                if (!isCrewUserUpdated) {
                                    isCrewUserUpdated = formUpdated;
                                }
                                serverEdgeForm.add("formDef", gson.toJsonTree(edgeFormDataList));
                                serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
                            }

                            edgeJsonObject.add("formData", serverEdgeFormJsonArray);
                            edgeJsonObject.addProperty("noteGuid", UUID.randomUUID().toString());
                            edgeJsonObject.addProperty("createdDateTime", System.currentTimeMillis());
                            edgeJsonObject.addProperty("createdBy", "admin");
                            if (isCrewUserUpdated) {
                                ResponseEntity<String> responseEntity1 = slvRestService.serverCall(baseURl+"/rest/notebooks/" + notebookGuid + "/notes/" + oldNoteguid, HttpMethod.PUT, gson.toJson(edgeJsonObject),false);
                                logger.info("Update note rest call response: "+responseEntity1.getStatusCode().value());
                            } else {
                                logger.info("Crew user email id not updated on form. It is skipping update");
                            }
                    }
                }
            } else {
                logger.error("Error while getting inventory note: " + responseEntity.getBody());
            }
        }catch (Exception e){
            logger.error("Error in updateCrewUserOnNote: ",e);
        }
    }

    private boolean processCrewUserId(String formTemplateGuid, List<EdgeFormData> edgeFormDatas, String currentTemplateGuid, JsonObject edgeJsonObject){
        boolean crewUserUpdated = false;
        try{
            if (currentTemplateGuid.equals(formTemplateGuid)) {
                String user = edgeJsonObject.get("createdBy").getAsString();
                String skippingUserStr = properties.getProperty("com.edge.skipping.users");
                List<String> skippingUsers = gson.fromJson(skippingUserStr, new TypeToken<List<String>>(){}.getType());
                if (skippingUsers.contains(user)) {
                    logger.info("this note captured by skipping users. So crewUser update skipped");
                    return false;
                }
                String crewUserConfigStr = properties.getProperty("crew.user.update.config");
                JsonObject crewUserConfig = jsonParser.parse(crewUserConfigStr).getAsJsonObject();
                JsonObject crewUserConfigForTemplate = null;
                try {
                    crewUserConfigForTemplate = crewUserConfig.get(formTemplateGuid).getAsJsonObject();
                }catch (Exception e){
                    logger.error("Error in parsing crewCursorConfig", e);
                }
                if (crewUserConfigForTemplate == null) {
                    logger.info("this formtemplate not present in config to update crew user");
                } else {
                    String existingCrewUser = getFormValue(edgeFormDatas, crewUserConfigForTemplate.get("crewUserId").getAsInt());
                    if (existingCrewUser == null || existingCrewUser.equals("")) {
                        ResponseEntity<String> responseEntity = serverCall("/rest/users", HttpMethod.GET, null);
                        String responseBody = responseEntity.getBody();
                        if (responseBody != null) {
                            String userEmail = null;
                            JsonArray userArray = jsonParser.parse(responseBody).getAsJsonArray();
                            for (JsonElement userElement : userArray) {
                                JsonObject userObject = userElement.getAsJsonObject();
                                String userName = userObject.get("userName").getAsString();
                                userName = userName != null ? userName : "";
                                if (userName.equals(user)) {
                                    userEmail = userObject.get("email").getAsString();
                                }
                            }
                            if(userEmail != null && !userEmail.equals("")) {
                                updateFormValue(edgeFormDatas, crewUserConfigForTemplate.get("crewUserId").getAsInt(), "Other");
                                updateFormValue(edgeFormDatas, crewUserConfigForTemplate.get("crewUserIdOther").getAsInt(), userEmail);
                                crewUserUpdated = true;
                            } else {
                                logger.info("user email null or empty. It is skipping update");
                            }
                        }
                    } else {
                        logger.info("crew user already updated. so skipping crew user update");
                    }
                }
            }
        }catch (Exception e){
            logger.error("Error in processCrewUserId", e);
        }
        return crewUserUpdated;
    }

    private void updateSlvData(String idOncontroller){
        JsonArray devices = checkDeviceExist(idOncontroller);
        if (devices == null || devices.size() == 0) {
            logger.info("The device is not present: "+idOncontroller);
        }else{
           int deviceId = devices.get(0).getAsJsonObject().get("id").getAsInt();
            String mainUrl = properties.getProperty("streetlight.slv.url.main");
            List<String> params = new ArrayList<>();
            params.add("ser=json");
            params.add("valueName=MacAddress");
            params.add("valueName=power");
            params.add("valueName=ConfigStatus");
            params.add("valueName=CommunicationStatus");
            params.add("valueName=location.zipcode");
            params.add("param0="+deviceId);
            ResponseEntity<String> responseEntity = slvRestService.getRequest(mainUrl+"/reports/api/logging/getDeviceLastValues?"+ StringUtils.join(params,"&"),true,null);
            String body = responseEntity.getBody();
            if(responseEntity.getStatusCode() == HttpStatus.OK){
                JsonArray slvdatas = new JsonArray();
                JsonArray jsonArray = jsonParser.parse(body).getAsJsonArray();
                for(JsonElement jsonElement : jsonArray){
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    String key = jsonObject.get("name").getAsString();
                    String value = jsonObject.get("value").getAsString();

                        JsonObject jsonObject1 = new JsonObject();
                        key = key.toLowerCase();
                        key = key.replaceAll("\\.","_");
                        jsonObject1.addProperty("key",key);
                        jsonObject1.addProperty("value",value == null ? "" : value);
                        slvdatas.add(jsonObject1);
                }

                slvRestService.serverCall(slvServerBaseUrl+"/promoted/updateSlvData?idOnController="+idOncontroller,HttpMethod.POST,gson.toJson(slvdatas),false);
            }
        }
    }

    public void updatePromotedData(JPSWorkflowModel jpsWorkflowModel,EdgeNote edgeNote, String formTemplateGuid){
        String noteguid = edgeNote.getNoteGuid();
        String idOnController = jpsWorkflowModel.getIdOnController();
        boolean isTitleUpdated = changeNoteTitle(noteguid,idOnController, formTemplateGuid);
        if (!isTitleUpdated) {
            logger.info("Title not updated. So going to update installed user email on form.");
            updateCrewUserOnNote(noteguid, formTemplateGuid);
        }
        updateSlvData(idOnController);
        updateEdgeData(edgeNote,idOnController);
        ResponseEntity<String> responseEntity  = slvRestService.serverCall(slvServerBaseUrl+"/updatePromotedDataforIdonController?idoncontroller="+idOnController,HttpMethod.GET,null,false);
        logger.info("update promoteddata for an idoncontroller response: "+responseEntity.getStatusCode().value());
    }

    private void updateEdgeData(EdgeNote edgeNote, String idoncontroller){
        JsonArray jsonArray = getEdgeDataValues(edgeNote,idoncontroller);
        String parentNoteguid = edgeNote.getBaseParentNoteId() == null ? edgeNote.getNoteGuid() : edgeNote.getBaseParentNoteId();
        slvRestService.serverCall(slvServerBaseUrl+"/promoted/updateEdgeData?parentNoteguid="+parentNoteguid,HttpMethod.POST,gson.toJson(jsonArray),false);
    }

    private JsonArray getEdgeDataValues(EdgeNote edgeNote,String idonController){
        JsonArray jsonArray = new JsonArray();
//        addJson("parentnoteguid",edgeNote.getBaseParentNoteId() == null ? edgeNote.getNoteGuid() : edgeNote.getBaseParentNoteId(),jsonArray);
        addJson("title",idonController,jsonArray);
        addJson("newpolenumber",idonController,jsonArray);

        String installFormtemplateGuid = properties.getProperty("streetlight.edge.install.formtemplateguid");
        String newFixtureFormtemplateGuid = properties.getProperty("streetlight.edge.new_workflow.formtemplateguid");
        List<FormData> formDatas = edgeNote.getFormData();
        for(FormData formData : formDatas){
            List<EdgeFormData> edgeFormDatas = formData.getFormDef();
            if(formData.getFormTemplateGuid().equals(installFormtemplateGuid)){
                String duplicatePoleNumber = getFormValue(edgeFormDatas,226);
                String controllerid = getFormValue(edgeFormDatas,192);
                String installstatus = getFormValue(edgeFormDatas,198);
                String lampserialnumber = getFormValue(edgeFormDatas,202);
                addJson("duplicatepolenumber",duplicatePoleNumber,jsonArray);
                addJson("controllerid",controllerid,jsonArray);
                addJson("installstatus",installstatus,jsonArray);
                addJson("lampserialnumber",lampserialnumber,jsonArray);
                break;
            }else if(formData.getFormTemplateGuid().equals(newFixtureFormtemplateGuid)){
                String duplicatePoleNumber = getFormValue(edgeFormDatas,225);
                String controllerid = getFormValue(edgeFormDatas,192);
                String installstatus = getFormValue(edgeFormDatas,198);
                String lampserialnumber = getFormValue(edgeFormDatas,202);
                addJson("duplicatepolenumber",duplicatePoleNumber,jsonArray);
                addJson("controllerid",controllerid,jsonArray);
                addJson("installstatus",installstatus,jsonArray);
                addJson("lampserialnumber",lampserialnumber,jsonArray);
                break;
            }
        }
        return jsonArray;
    }

    private void addJson(String key,String value,JsonArray jsonArray){
        value = value == null ? "" : value;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("key",key);
        jsonObject.addProperty("value",value);
        jsonArray.add(jsonObject);
    }

    @Override
    public void buildFixtureStreetLightData(String data, List<Object> paramsList, EdgeNote edgeNote) throws InValidBarCodeException {

    }
}
