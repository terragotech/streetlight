package com.slvinterface.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.slvinterface.json.EdgeNote;
import com.slvinterface.json.FormValues;
import com.slvinterface.utils.FormValueUtil;
import com.slvinterface.utils.PropertiesReader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public class CreateRevision {
    private EdgeRestService edgeRestService;
    protected Gson gson;
    public boolean updateLogic(JsonObject edgenoteJson,JsonArray serverForms,Object obj){
        boolean mustUpdate = false;
        int size = serverForms.size();
        String netSenseformTemplateGUID = PropertiesReader.getProperties().getProperty("netsense.formtemp");
        String netSenseDeviceControl = PropertiesReader.getProperties().getProperty("netsense.dc");
        String netSenseActionID = PropertiesReader.getProperties().getProperty("netsense.actionid");
        String deviceControlFormTemplateGUID = PropertiesReader.getProperties().getProperty("dc_form_template");
        String dc_nodeid_id = PropertiesReader.getProperties().getProperty("dc_nodeid_id");

        for (int i = 0; i < size; i++) {
            JsonObject serverEdgeForm = serverForms.get(i).getAsJsonObject();
            String formDefJson = serverEdgeForm.get("formDef").getAsString();
            String formTemplate = serverEdgeForm.get("formTemplateGuid").getAsString();
            if (formTemplate.equals(deviceControlFormTemplateGUID))
            {
                formDefJson = formDefJson.replaceAll("\\\\", "");
                formDefJson = formDefJson.replace("u0026", "\\u0026");
                List<FormValues> formComponents = gson.fromJson(formDefJson, new TypeToken<List<FormValues>>() {
                }.getType());
                int ndc_nodeid_id  = Integer.parseInt(dc_nodeid_id);
                String nodeID = FormValueUtil.getValue(formComponents,ndc_nodeid_id);
                new DeviceControl(nodeID,"80","1");
                mustUpdate = false;
            }
            else if (formTemplate.equals(netSenseformTemplateGUID))
            {


                formDefJson = formDefJson.replaceAll("\\\\", "");
                formDefJson = formDefJson.replace("u0026", "\\u0026");
                List<FormValues> formComponents = gson.fromJson(formDefJson, new TypeToken<List<FormValues>>() {
                }.getType());
                int deviceControlID = Integer.parseInt(netSenseDeviceControl);
                int actionID = Integer.parseInt(netSenseActionID);
                if(FormValueUtil.getValue(formComponents,actionID).equals("Diagnostic") && FormValueUtil.getValue(formComponents,deviceControlID).equals("This Fixture"))
                {
                    //Device Controll Path
                    String strnodeid = PropertiesReader.getProperties().getProperty("netsense.installid");
                    int nodeid = Integer.parseInt(strnodeid);
                    String nodeID = FormValueUtil.getValue(formComponents,nodeid);
                    FormValueUtil.updateEdgeForm(formComponents,deviceControlID,"");
                    mustUpdate = true;
                    String dimvalue_id = PropertiesReader.getProperties().getProperty("dimming_value_id");
                    String turnon_id = PropertiesReader.getProperties().getProperty("turn_on_duration_id");
                    int dimvalueid = Integer.parseInt(dimvalue_id);
                    int turnonid = Integer.parseInt(turnon_id);
                    String strLightLevel = FormValueUtil.getValue(formComponents,dimvalueid);
                    String strTurnON = FormValueUtil.getValue(formComponents,turnonid);
                    if(strLightLevel.equals(""))
                    {
                        strLightLevel = "80";
                    }
                    if(strTurnON.equals(""))
                    {
                        strTurnON = "1";
                    }
                    new DeviceControl(nodeID,strLightLevel,strTurnON);
                }
                if(FormValueUtil.getValue(formComponents,actionID).equals("Install"))
                {
                    //Install Path
                    String strnodeid = PropertiesReader.getProperties().getProperty("netsense.installid");
                    int nodeid = Integer.parseInt(strnodeid);
                    String nodeID = FormValueUtil.getValue(formComponents,nodeid);
                    if(!nodeID.equals("")) {
                        String strid = PropertiesReader.getProperties().getProperty("netsense.qrcode");
                        int id = Integer.parseInt(strid);

                        String qrscanCode = FormValueUtil.getValue(formComponents, id);

                        strid = PropertiesReader.getProperties().getProperty("netsense.lumtype");
                        id = Integer.parseInt(strid);
                        String lumType = FormValueUtil.getValue(formComponents, id);

                        strid = PropertiesReader.getProperties().getProperty("netsense.nema");
                        id = Integer.parseInt(strid);
                        String nema = FormValueUtil.getValue(formComponents, id);

                        String responseString = NetSenseInterface.createFixture(qrscanCode, lumType, nema);
                        if (responseString != null) {
                            JsonParser jsonParser = new JsonParser();
                            JsonElement jsonElement = jsonParser.parse(responseString);
                            JsonObject jsonObject = jsonElement.getAsJsonObject();
                            String fixtureID = jsonObject.get("fixtureid").getAsString();


                            NetSenseInterface.assignNode(fixtureID, nodeID);
                            JsonObject jsonObject1 = NetSenseInterface.getNodeDetails(nodeID);

                            if (jsonObject1 != null) {
                                String fixtureID1 = jsonObject1.get("fixtureid").getAsString();
                                String siteID = PropertiesReader.getProperties().getProperty("netsense.site.id");
                                JsonObject jsonObject2 = NetSenseInterface.getFixtureDetails(fixtureID);
                                FormValueUtil.updateEdgeForm(formComponents, 34, siteID);
                                String lat = jsonObject1.get("latitude").getAsString();
                                String lng = jsonObject1.get("longitude").getAsString();
                                String manufacture = jsonObject2.get("manufacturer").getAsString();
                                String fixtureType = jsonObject2.get("fixtureType").getAsString();
                                String description = jsonObject2.get("description").getAsString();
                                String sw = jsonObject1.get("softwareVersion").getAsString();


                                FormValueUtil.updateEdgeForm(formComponents, 6, lat);
                                FormValueUtil.updateEdgeForm(formComponents, 7, lng);
                                FormValueUtil.updateEdgeForm(formComponents, 8, fixtureID1);
                                FormValueUtil.updateEdgeForm(formComponents, 10, manufacture);
                                FormValueUtil.updateEdgeForm(formComponents, 11, description);
                                FormValueUtil.updateEdgeForm(formComponents, 12, fixtureType);
                                FormValueUtil.updateEdgeForm(formComponents, 19, sw);
                            }
                            mustUpdate = true;
                        }
                    }

                }



                serverEdgeForm.add("formDef", gson.toJsonTree(formComponents));
                serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
            }
        }
        edgenoteJson.add("formData", serverForms);
        edgenoteJson.addProperty("createdBy", "admin");
        long ntime = System.currentTimeMillis();

        edgenoteJson.addProperty("createdDateTime", ntime);
        return mustUpdate;
    }

    public CreateRevision(){
        edgeRestService = new EdgeRestService();
        gson = new Gson();
    }

    private String getNoteDetails(String noteguid) {
        String response = "";
        String baseUrl = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        String urlNew = baseUrl + "rest/notes/" + noteguid;
        System.out.println(urlNew);
        String tokenString = edgeRestService.getEdgeToken();
        ResponseEntity<String> requestEntity = edgeRestService.getRequest(urlNew,true,tokenString);
        if(requestEntity.getStatusCode() == HttpStatus.OK)
        {
            response = requestEntity.getBody();
        }
        return response;
    }

    private String updateNoteDetails(String noteJson,String noteGuid,String notebookGuid)
    {
        String response = "";
        String baseUrl = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        String urlNew = baseUrl + "rest/notebooks/" + notebookGuid + "/notes/" + noteGuid;
        System.out.println(urlNew);
        String tokenString = edgeRestService.getEdgeToken();
        ResponseEntity<String>  responseEntity = edgeRestService.putRequest(urlNew,noteJson,true,tokenString);
        if(responseEntity.getStatusCode() == HttpStatus.OK)
        {
            response = responseEntity.getBody();
        }
        return response;
    }
    public void createRevision(String noteGUID,Object obj){
        String noteJson = getNoteDetails(noteGUID);
        EdgeNote restEdgeNote = gson.fromJson(noteJson, EdgeNote.class);
        JsonObject edgenoteJson = new JsonParser().parse(noteJson).getAsJsonObject();
        JsonArray serverForms = edgenoteJson.get("formData").getAsJsonArray();
        boolean mustUpdate = updateLogic(edgenoteJson,serverForms,obj);
        if(mustUpdate) {
            updateNoteDetails(edgenoteJson.toString(), noteGUID, restEdgeNote.getEdgeNotebook().getNotebookGuid());

        }
    }

}
