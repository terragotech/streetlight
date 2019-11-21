package com.slvinterface.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.stmt.query.In;
import com.slvinterface.dao.tables.SlvDevice;
import com.slvinterface.dao.tables.SlvSyncDetails;
import com.slvinterface.enumeration.Status;
import com.slvinterface.exception.*;
import com.slvinterface.json.EdgeNote;
import com.slvinterface.json.FormValues;
import com.slvinterface.utils.*;
import com.slvinterface.dao.ConnectionDAO;
import com.slvinterface.dao.SLVInterfaceDAO;


import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.ResponseCache;
import java.util.ArrayList;
import java.util.List;

public class StreetLightCanadaService {
    private EdgeRestService edgeRestService;
    private JsonParser jsonParser;
    final Logger logger = Logger.getLogger(StreetLightCanadaService.class);
    private Gson gson;
    private SLVTools slvTools;
    private ConnectionDAO connectionDAO;
    //private SLVInterfaceDAO slvInterfaceDAO;
    public StreetLightCanadaService()
    {
        edgeRestService = new EdgeRestService();
        slvTools = new SLVTools();
        jsonParser = new JsonParser();
        gson = new Gson();
        connectionDAO = ConnectionDAO.INSTANCE;
        slvTools.setConnectionDAO(connectionDAO);
        //slvInterfaceDAO = new SLVInterfaceDAO();

    }
    private void populateDeviceValues(List<FormValues> formComponents,EdgeNote edgeNote,String controllerStrId,List<Object> paramsList) throws Exception {


        String strBlock = PropertiesReader.getProperties().getProperty("streetlight.form.blockid");
        String strFacility = PropertiesReader.getProperties().getProperty("streetlight.form.facilityid");
        String strPoleType = PropertiesReader.getProperties().getProperty("streetlight.form.poletypeid");
        String strLumH = PropertiesReader.getProperties().getProperty("streetlight.form.lumhtid");
        String strPoleColor = PropertiesReader.getProperties().getProperty("streetlight.form.polecolorid");
        String strPoleTag = PropertiesReader.getProperties().getProperty("streetlight.form.poletagid");

        String strArmType = PropertiesReader.getProperties().getProperty("streetlight.form.armtypeid");
        String strVegObj = PropertiesReader.getProperties().getProperty("streetlight.form.vegobjid");
        String strNewLumCode = PropertiesReader.getProperties().getProperty("streetlight.form.installlumcodeid");
        String strPoleLen = PropertiesReader.getProperties().getProperty("streetlight.form.polelenid");

        String strPoleCon = PropertiesReader.getProperties().getProperty("streetlight.form.poleconid");
        String strPoleManuf = PropertiesReader.getProperties().getProperty("streetlight.form.polemanufacid");
        String strFuse = PropertiesReader.getProperties().getProperty("streetlight.form.fuseid");
        String strPower = PropertiesReader.getProperties().getProperty("streetlight.form.powerid");

        String strPowerOther = PropertiesReader.getProperties().getProperty("streetlight.form.powerotherid");
        String strComment = PropertiesReader.getProperties().getProperty("streetlight.form.commentid");




        String strFormBlock = FormValueUtil.getValue(formComponents,DataTools.convertFormIDToInt(strBlock));
        String strFormFacility = FormValueUtil.getValue(formComponents,DataTools.convertFormIDToInt(strFacility ));
        String strFormPoleType = FormValueUtil.getValue(formComponents,DataTools.convertFormIDToInt(strPoleType ));
        String strFormLumH = FormValueUtil.getValue(formComponents,DataTools.convertFormIDToInt(strLumH ));
        String strFormPoleColor = FormValueUtil.getValue(formComponents,DataTools.convertFormIDToInt(strPoleColor ));
        String strFormPoleTag = FormValueUtil.getValue(formComponents,DataTools.convertFormIDToInt(strPoleTag  ));
        String strFormArmType = FormValueUtil.getValue(formComponents,DataTools.convertFormIDToInt(strArmType ));

        String strFormVegType = FormValueUtil.getValue(formComponents,DataTools.convertFormIDToInt(strVegObj  ));
        String strFormLumCode = FormValueUtil.getValue(formComponents,DataTools.convertFormIDToInt(strNewLumCode   ));
        String strFormPoleLen = FormValueUtil.getValue(formComponents,DataTools.convertFormIDToInt(strPoleLen   ));

        String strFormPoleCondition = FormValueUtil.getValue(formComponents,DataTools.convertFormIDToInt(strPoleCon));
        String strFormPoleManfu = FormValueUtil.getValue(formComponents,DataTools.convertFormIDToInt(strPoleManuf));

        String strFormFuse = FormValueUtil.getValue(formComponents,DataTools.convertFormIDToInt(strFuse));
        String strFormPower = FormValueUtil.getValue(formComponents,DataTools.convertFormIDToInt(strPower));
        String strFormPowerOther = FormValueUtil.getValue(formComponents,DataTools.convertFormIDToInt(strPowerOther));

        String strFormComment = FormValueUtil.getValue(formComponents,DataTools.convertFormIDToInt(strComment));

        String strMergedComment = "Lum_Ht : " + strFormLumH;
        strMergedComment = strMergedComment + " " + "Pole Tag : " + strFormPoleTag;
        strMergedComment = strMergedComment + " " + "Vegetation Obstruction : " + strFormVegType;
        strMergedComment = strMergedComment + " " + strFormComment;

        paramsList.add("idOnController="+DataTools.URLEncoder(edgeNote.getTitle()));
        paramsList.add("controllerStrId="+controllerStrId);
        slvTools.addStreetLightData("installStatus", "Installed", paramsList);
        slvTools.addStreetLightData("install.date", Utils.dateFormat(edgeNote.getCreatedDateTime()), paramsList);
        slvTools.addStreetLightData("location.utillocationid", edgeNote.getTitle() + ".Lamp", paramsList);


        slvTools.addStreetLightData("location.mapnumber",strFormBlock,paramsList);
        slvTools.addStreetLightData("address",strFormFacility,paramsList);
        slvTools.addStreetLightData("pole.material",strFormPoleType,paramsList);

        slvTools.addStreetLightData("pole.colorcode",strFormPoleColor,paramsList);
        slvTools.addStreetLightData("comment",strMergedComment,paramsList);
        slvTools.addStreetLightData("fixing.model",strFormArmType,paramsList);
        slvTools.addStreetLightData("luminaire.model",strFormLumCode,paramsList);

        slvTools.addStreetLightData("pole.height",strFormPoleLen,paramsList);
        slvTools.addStreetLightData("pole.status",strFormPoleCondition,paramsList);

        slvTools.addStreetLightData("pole.type",strFormPoleManfu,paramsList);

        slvTools.addStreetLightData("pole.groundtype",strFormFuse,paramsList);

        if(strFormPower.equals("OTHER"))
        {
            slvTools.addStreetLightData("power",strFormPowerOther,paramsList);
        }
        else
        {
            slvTools.addStreetLightData("power",strFormPower,paramsList);
        }
        slvTools.addStreetLightData("location.locationtype","LOCATION_TYPE_PREMISE",paramsList);
        slvTools.addStreetLightData("lamp.installdate",Utils.dateFormat(edgeNote.getCreatedDateTime()),paramsList);
    }
    private void clearDeviceValues(List<FormValues> formComponents,EdgeNote edgeNote,String controllerStrId,List<Object> paramsList) throws Exception {

        String strComment = PropertiesReader.getProperties().getProperty("streetlight.form.removecommentid");
        String strFormComment = FormValueUtil.getValue(formComponents,DataTools.convertFormIDToInt(strComment));
        String strReasonRemove =  PropertiesReader.getProperties().getProperty("streetlight.form.removereason");
        String strFormReasonRemove = FormValueUtil.getValue(formComponents,DataTools.convertFormIDToInt(strReasonRemove));

        String strRemove = PropertiesReader.getProperties().getProperty("streetlight.form.rreason.remove");
        String strPoleDown = PropertiesReader.getProperties().getProperty("streetlight.form.rreason.poledown");

        paramsList.add("idOnController="+DataTools.URLEncoder(edgeNote.getTitle()));
        paramsList.add("controllerStrId="+controllerStrId);

        if(strFormReasonRemove.equals(strRemove))
        {
            slvTools.addStreetLightData("installStatus", "Removed", paramsList);
        }
        else if(strFormReasonRemove.equals(strPoleDown))
        {
            slvTools.addStreetLightData("installStatus", "Pole Knocked Down", paramsList);
        }
        else
        {
            slvTools.addStreetLightData("installStatus", "Removed", paramsList);
        }

        slvTools.addStreetLightData("install.date", "", paramsList);



        slvTools.addStreetLightData("location.mapnumber","",paramsList);
        slvTools.addStreetLightData("address","",paramsList);
        slvTools.addStreetLightData("pole.material","",paramsList);

        slvTools.addStreetLightData("pole.colorcode","",paramsList);
        slvTools.addStreetLightData("comment",strFormComment,paramsList);
        slvTools.addStreetLightData("fixing.model","",paramsList);
        slvTools.addStreetLightData("luminaire.model","",paramsList);

        slvTools.addStreetLightData("pole.height","",paramsList);
        slvTools.addStreetLightData("pole.status","",paramsList);

        slvTools.addStreetLightData("pole.type","",paramsList);

        slvTools.addStreetLightData("pole.groundtype","",paramsList);

        slvTools.addStreetLightData("power","",paramsList);


        slvTools.addStreetLightData("lamp.installdate","",paramsList);
    }
    private void doProcess(String noteGuid,String accessToken,boolean resync) throws InvalidMacAddressException
    {

        String controllerStrId = PropertiesReader.getProperties().getProperty("streetlight.controller.str.id");

        //String geoZoneId = PropertiesReader.getProperties().getProperty("streetlight.geozoneid");
        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        String actionID = PropertiesReader.getProperties().getProperty("streetlight.form.actionid");
        String geoZoneID = PropertiesReader.getProperties().getProperty("streetlight.form.geozoneid");
        String strNewInstall = PropertiesReader.getProperties().getProperty("streetlight.form.newinstall");
        String strReplace = PropertiesReader.getProperties().getProperty("streetlight.form.replace");
        String strRemove = PropertiesReader.getProperties().getProperty("streetlight.form.remove");
        String newInstallMacAddress = PropertiesReader.getProperties().getProperty("streetlight.form.newinstall.mac");
        String existingMacAddress = PropertiesReader.getProperties().getProperty("streetlight.form.existmac");
        String replaceMacAddress = PropertiesReader.getProperties().getProperty("streetlight.form.repmac");





        int actionid = Integer.parseInt(actionID);
        int newMacid = Integer.parseInt(newInstallMacAddress);
        int formGeoZoneId = Integer.parseInt(geoZoneID);
        url = url + PropertiesReader.getProperties().getProperty("streetlight.edge.url.notes.get");
        url = url + "/" +noteGuid;
        String installationFormTempGuid = PropertiesReader.getProperties().getProperty("streetlight.installation.formtempguid");
        logger.info("Given url is :" + url);

        // Get NoteList from edgeserver
        ResponseEntity<String> responseEntity = edgeRestService.getRequest(url, false, accessToken);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String notesData = responseEntity.getBody();
            logger.info("rest service data:" + notesData);
            EdgeNote edgeNote = gson.fromJson(notesData, EdgeNote.class);
            if ((!edgeNote.getCreatedBy().contains("admin") && !edgeNote.getCreatedBy().contains("slvinterface")) ||  resync) {
                JsonObject edgenoteJson = new JsonParser().parse(notesData).getAsJsonObject();
                JsonArray serverForms = edgenoteJson.get("formData").getAsJsonArray();
                int size = serverForms.size();

                for (int i = 0; i < size; i++) {
                    JsonObject serverEdgeForm = serverForms.get(i).getAsJsonObject();
                    String formDefJson = serverEdgeForm.get("formDef").getAsString();
                    String formTemplate = serverEdgeForm.get("formTemplateGuid").getAsString();
                    formDefJson = formDefJson.replaceAll("\\\\", "");
                    formDefJson = formDefJson.replace("u0026", "\\u0026");
                    List<FormValues> formComponents = gson.fromJson(formDefJson, new TypeToken<List<FormValues>>() {
                    }.getType());
                    if(formTemplate.equals(installationFormTempGuid))
                    {
                        ////////////////////////////////////////////////////////////////////////
                        List<Object> paramsList = new ArrayList<>();
                        String strActionString = FormValueUtil.getValue(formComponents,actionid);
                        String strFormExistingMac = FormValueUtil.getValue(formComponents,DataTools.convertFormIDToInt(existingMacAddress));
                        String strFormReplaceMac = FormValueUtil.getValue(formComponents,DataTools.convertFormIDToInt(replaceMacAddress));
                        String macAddress = FormValueUtil.getValue(formComponents,newMacid);
                        boolean isDeviceExist = slvTools.deviceAlreadyExists (edgeNote.getTitle());

                        long currentTime = System.currentTimeMillis();
                        SlvSyncDetails slvSyncDetails = new SlvSyncDetails();
                        SlvDevice slvDevice = new SlvDevice();

                        slvSyncDetails.setNoteName(edgeNote.getTitle());
                        slvSyncDetails.setNoteGuid(edgeNote.getNoteGuid());
                        slvSyncDetails.setProcessedDateTime(currentTime);
                        slvSyncDetails.setNoteCreatedBy(edgeNote.getCreatedBy());

                        slvDevice.setDeviceName(edgeNote.getTitle());
                        slvDevice.setDeviceId(edgeNote.getTitle());
                        slvDevice.setProcessedDateTime(currentTime);

                        try {
                            if (!isDeviceExist) {
                                String strGeoZone = FormValueUtil.getValue(formComponents, formGeoZoneId);
                                if (!strGeoZone.equals("")) {
                                    int newDeviceGeoZoneID = slvTools.checkAndCreateGeoZone(strGeoZone, edgeNote);
                                    if (newDeviceGeoZoneID != -1) {
                                        slvTools.createNewDevice(edgeNote, slvSyncDetails, Integer.toString(newDeviceGeoZoneID));
                                    } else {
                                        //Failed to Create GeoZone
                                        slvSyncDetails.setErrorDetails("Failed to create GeoZone");
                                        connectionDAO.saveSlvSyncDetails(slvSyncDetails);
                                        continue;
                                    }
                                } else {
                                    //Bad GeoZone
                                    slvSyncDetails.setErrorDetails("Bad GeoZone");
                                    connectionDAO.saveSlvSyncDetails(slvSyncDetails);
                                    continue;
                                }
                            }
                            //At this Point Geozone and Device are ready
                            if(strActionString.equals(strNewInstall))
                            {
                                if(DataTools.checkForValidMacAddress(macAddress))
                                {
                                    slvTools.checkMacAddressExists(macAddress,edgeNote.getTitle());
                                    populateDeviceValues(formComponents,edgeNote,controllerStrId,paramsList);
                                    slvTools.setDeviceValues(paramsList,slvSyncDetails);
                                    try {
                                        slvTools.replaceOLC(controllerStrId, edgeNote.getTitle(), macAddress);
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    slvDevice.setMacAddress(macAddress);
                                    SlvDevice slvDevice1 = connectionDAO.getSlvDevices(edgeNote.getTitle());
                                    if(slvDevice1 != null)
                                    {
                                        connectionDAO.updateSlvDevice(edgeNote.getTitle(),macAddress);
                                    }
                                    else {
                                        connectionDAO.saveSlvDevices(slvDevice);
                                    }

                                }
                            }
                            else if (strActionString.equals(strReplace))
                            {
                                if(DataTools.checkForValidMacAddress(strFormReplaceMac))
                                {
                                    slvTools.checkMacAddressExists(strFormReplaceMac,edgeNote.getTitle());
                                    populateDeviceValues(formComponents,edgeNote,controllerStrId,paramsList);
                                    slvTools.setDeviceValues(paramsList,slvSyncDetails);
                                    try {
                                        slvTools.replaceOLC(controllerStrId, edgeNote.getTitle(), "");
                                        slvTools.replaceOLC(controllerStrId, edgeNote.getTitle(), strFormReplaceMac);
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    slvDevice.setMacAddress(strFormReplaceMac);
                                    SlvDevice slvDevice1 = connectionDAO.getSlvDevices(edgeNote.getTitle());
                                    if(slvDevice1 != null)
                                    {
                                        connectionDAO.updateSlvDevice(edgeNote.getTitle(),strFormReplaceMac);
                                    }
                                    else {
                                        connectionDAO.saveSlvDevices(slvDevice);
                                    }
                                }
                            }
                            else if (strActionString.equals(strRemove))
                            {

                                    clearDeviceValues(formComponents,edgeNote,controllerStrId,paramsList);
                                    slvTools.setDeviceValues(paramsList,slvSyncDetails);
                                    try {
                                        slvTools.replaceOLC(controllerStrId, edgeNote.getTitle(), "");
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    slvDevice.setMacAddress("");
                                    SlvDevice slvDevice1 = connectionDAO.getSlvDevices(edgeNote.getTitle());
                                    if(slvDevice1 != null)
                                    {
                                        connectionDAO.updateSlvDevice(edgeNote.getTitle(),"");
                                    }
                                    else {
                                        connectionDAO.saveSlvDevices(slvDevice);
                                    }


                            }
                        }
                        catch (DeviceCreationFailedException e)
                        {
                            slvSyncDetails.setErrorDetails("Failed to Create device");
                        }
                        catch (QRCodeAlreadyUsedException e)
                        {
                            slvSyncDetails.setErrorDetails("MacAddress already in use");
                        }
                        catch (IOException e)
                        {
                            slvSyncDetails.setErrorDetails(e.getMessage());
                        }
                        catch (Exception e)
                        {
                            slvSyncDetails.setErrorDetails(e.getMessage());
                        }
                        finally {
                            connectionDAO.saveSlvSyncDetails(slvSyncDetails);
                        }
                        ////////////////////////////////////////////////////////////////////////

                        //End Template match
                    }
                    //End Form Loop
                }


            }
        }

    }
    public void run(){


        String actionResync =  PropertiesReader.getProperties().getProperty("streetlight.edge.data.resync");

        if(actionResync != null)
        {
            if(actionResync.equals("true"))
            {
                //TODO: Resync Process
            }
        }
        else
        {
            //##############################################################################################
            do {
                //Normal SLV Process
                String edgeSlvUrl =  PropertiesReader.getProperties().getProperty("streetlight.edge.slvserver.url");
                edgeSlvUrl = edgeSlvUrl + "/notesGuid?lastSyncTime=";

                long lastSynctime = connectionDAO.getLastSyncTime();
                if (lastSynctime > 0) {
                    edgeSlvUrl = edgeSlvUrl + lastSynctime;

                } else {
                    lastSynctime = System.currentTimeMillis() - (10 * 60000);
                    edgeSlvUrl = edgeSlvUrl + lastSynctime;
                }

                String terragoAccessToken = edgeRestService.getEdgeToken();
                ResponseEntity<String> responseEntity = edgeRestService.getRequest(edgeSlvUrl, true, terragoAccessToken);
                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    String notesGuids = responseEntity.getBody();
                    System.out.println(notesGuids);
                    try {
                        JsonArray noteGuidsJsonArray = (JsonArray) jsonParser.parse(notesGuids);
                        if (noteGuidsJsonArray != null && !noteGuidsJsonArray.isJsonNull()) {
                            for (JsonElement noteGuidJson : noteGuidsJsonArray) {
                                String noteGuid = noteGuidJson.getAsString();
                                if (!connectionDAO.checkNoteProcessed(noteGuid)) {
                                    doProcess(noteGuid, terragoAccessToken, false);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                try {
                    Thread.sleep(10000);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }while(true);
            //########################################################################################
        }




    }
}
