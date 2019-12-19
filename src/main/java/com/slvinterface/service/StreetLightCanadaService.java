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
import com.slvinterface.json.LightDataCompareResult;
import com.slvinterface.utils.*;
import com.slvinterface.dao.ConnectionDAO;
import com.slvinterface.dao.SLVInterfaceDAO;


import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import javax.xml.crypto.Data;
import java.io.*;
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
    public StreetLightCanadaService() {
        edgeRestService = new EdgeRestService();
        slvTools = new SLVTools();
        jsonParser = new JsonParser();
        gson = new Gson();
        connectionDAO = ConnectionDAO.INSTANCE;
        slvTools.setConnectionDAO(connectionDAO);
        //slvInterfaceDAO = new SLVInterfaceDAO();

    }

    private LightDataCompareResult populateAndCheckWithOldValues(List<FormValues> formComponents,
                                                                 EdgeNote edgeNote, String controllerStrId,
                                                                 List<Object> paramsList, String currentValue, String dimmingValue) throws Exception {

        LightDataCompareResult lightDataCompareResult = new LightDataCompareResult();
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


        String strFormBlock = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(strBlock));
        String strFormFacility = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(strFacility));
        String strFormPoleType = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(strPoleType));
        String strFormLumH = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(strLumH));
        String strFormPoleColor = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(strPoleColor));
        String strFormPoleTag = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(strPoleTag));
        String strFormArmType = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(strArmType));

        String strFormVegType = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(strVegObj));
        String strFormLumCode = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(strNewLumCode));
        String strFormPoleLen = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(strPoleLen));

        String strFormPoleCondition = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(strPoleCon));
        String strFormPoleManfu = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(strPoleManuf));

        String strFormFuse = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(strFuse));
        String strFormPower = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(strPower));
        String strFormPowerOther = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(strPowerOther));

        String strFormComment = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(strComment));

        String strMergedComment = "Lum_Ht : " + strFormLumH;
        strMergedComment = strMergedComment + " " + "Pole Tag : " + strFormPoleTag;
        strMergedComment = strMergedComment + " " + "Vegetation Obstruction : " + strFormVegType;
        strMergedComment = strMergedComment + " " + strFormComment;


        boolean mustUpdate = false;
        if (currentValue != null) {
            JsonParser jsonParser = new JsonParser();
            JsonArray jsonArray = jsonParser.parse(currentValue).getAsJsonArray();
            int tc = jsonArray.size();
            if (tc == 0) {
                mustUpdate = true;
            }
            for (int idx = 0; idx < tc; idx++) {
                JsonObject jsonObject = jsonArray.get(idx).getAsJsonObject();
                String strName = DataTools.checkDataNull(jsonObject, "name");
                String strValue = DataTools.checkDataNull(jsonObject, "value");
                switch (strName) {
                    case "block":
                        if (!strFormBlock.equals(strValue)) {
                            mustUpdate = true;
                        }
                        break;
                    case "facility":
                        if (!strFormFacility.equals(strValue)) {
                            mustUpdate = true;
                        }
                        break;
                    case "poletype":
                        if (!strFormPoleType.equals(strValue)) {
                            mustUpdate = true;
                        }
                        break;
                    case "polecolor":
                        if (!strFormPoleColor.equals(strValue)) {
                            mustUpdate = true;
                        }
                        break;
                    case "comment":
                        if (!strMergedComment.equals(strValue)) {
                            mustUpdate = true;
                        }
                        break;
                    case "armtype":
                        if (!strFormArmType.equals(strValue)) {
                            mustUpdate = true;
                        }
                        break;
                    case "lummodel":
                        if (!strFormLumCode.equals(strValue)) {
                            mustUpdate = true;
                        }
                        break;
                    case "polelen":
                        if (!strFormPoleLen.equals(strValue)) {
                            mustUpdate = true;
                        }
                        break;
                    case "polecon":
                        if (!strFormPoleCondition.equals(strValue)) {
                            mustUpdate = true;
                        }
                        break;
                    case "polemanu":
                        if (!strFormPoleManfu.equals(strValue)) {
                            mustUpdate = true;
                        }
                        break;
                    case "fuse":
                        if (!strFormFuse.equals(strValue)) {
                            mustUpdate = true;
                        }
                        break;
                    case "power":
                        if (strFormPower.equals("OTHER")) {
                            if (!strFormPowerOther.equals(strValue)) {
                                mustUpdate = true;
                            }
                        } else {
                            if (!strFormPower.equals(strValue)) {
                                mustUpdate = true;
                            }
                        }
                        break;

                }
                if (mustUpdate) {
                    break;
                }
            }

        } else {
            mustUpdate = true;
        }
        lightDataCompareResult.setMustUpdate(mustUpdate);

        JsonArray jsonArray = new JsonArray();
        paramsList.add("idOnController=" + DataTools.URLEncoder(edgeNote.getTitle()));
        paramsList.add("controllerStrId=" + controllerStrId);

        slvTools.addStreetLightData("location.utillocationid", edgeNote.getTitle() + ".Lamp", paramsList);
        String modelFunctionName = PropertiesReader.getProperties().getProperty("streetlight.slv.equipment.type");
        slvTools.addStreetLightData("modelFunctionId", modelFunctionName, paramsList);

        slvTools.addStreetLightData("DimmingGroupName", dimmingValue, paramsList);

        slvTools.addStreetLightData("location.mapnumber", strFormBlock, paramsList);
        jsonArray.add(DataTools.createJsonObject("block", strFormBlock));

        slvTools.addStreetLightData("address", strFormFacility, paramsList);
        jsonArray.add(DataTools.createJsonObject("facility", strFormFacility));

        slvTools.addStreetLightData("pole.material", strFormPoleType, paramsList);
        jsonArray.add(DataTools.createJsonObject("poletype", strFormPoleType));

        slvTools.addStreetLightData("pole.colorcode", strFormPoleColor, paramsList);
        jsonArray.add(DataTools.createJsonObject("polecolor", strFormPoleColor));

        slvTools.addStreetLightData("comment", strMergedComment, paramsList);
        jsonArray.add(DataTools.createJsonObject("comment", strMergedComment));

        slvTools.addStreetLightData("fixing.model", strFormArmType, paramsList);
        jsonArray.add(DataTools.createJsonObject("armtype", strFormArmType));

        slvTools.addStreetLightData("luminaire.model", strFormLumCode, paramsList);
        jsonArray.add(DataTools.createJsonObject("lummodel", strFormLumCode));

        slvTools.addStreetLightData("pole.height", strFormPoleLen, paramsList);
        jsonArray.add(DataTools.createJsonObject("polelen", strFormPoleLen));

        slvTools.addStreetLightData("pole.status", strFormPoleCondition, paramsList);
        jsonArray.add(DataTools.createJsonObject("polecon", strFormPoleCondition));

        slvTools.addStreetLightData("pole.type", strFormPoleManfu, paramsList);
        jsonArray.add(DataTools.createJsonObject("polemanu", strFormPoleManfu));

        slvTools.addStreetLightData("pole.groundtype", strFormFuse, paramsList);
        jsonArray.add(DataTools.createJsonObject("fuse", strFormFuse));

        if (strFormPower.equals("OTHER")) {
            slvTools.addStreetLightData("power", strFormPowerOther, paramsList);
            jsonArray.add(DataTools.createJsonObject("power", strFormPowerOther));
        } else {
            slvTools.addStreetLightData("power", strFormPower, paramsList);
            jsonArray.add(DataTools.createJsonObject("power", strFormPower));
        }
        slvTools.addStreetLightData("location.locationtype", "LOCATION_TYPE_PREMISE", paramsList);
        lightDataCompareResult.setJsonArray(jsonArray);

        return lightDataCompareResult;

    }

    private void clearDeviceValues(List<FormValues> formComponents, EdgeNote edgeNote, String controllerStrId, List<Object> paramsList) throws Exception {

        String strComment = PropertiesReader.getProperties().getProperty("streetlight.form.removecommentid");
        String strFormComment = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(strComment));
        String strReasonRemove = PropertiesReader.getProperties().getProperty("streetlight.form.removereason");
        String strFormReasonRemove = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(strReasonRemove));

        String strRemove = PropertiesReader.getProperties().getProperty("streetlight.form.rreason.remove");
        String strPoleDown = PropertiesReader.getProperties().getProperty("streetlight.form.rreason.poledown");

        paramsList.add("idOnController=" + DataTools.URLEncoder(edgeNote.getTitle()));
        paramsList.add("controllerStrId=" + controllerStrId);

        if (strFormReasonRemove.equals(strRemove)) {
            slvTools.addStreetLightData("installStatus", "Removed", paramsList);
        } else if (strFormReasonRemove.equals(strPoleDown)) {
            slvTools.addStreetLightData("installStatus", "Pole Knocked Down", paramsList);
        } else {
            slvTools.addStreetLightData("installStatus", "Removed", paramsList);
        }

        slvTools.addStreetLightData("install.date", "", paramsList);


        slvTools.addStreetLightData("location.mapnumber", "", paramsList);
        slvTools.addStreetLightData("address", "", paramsList);
        slvTools.addStreetLightData("pole.material", "", paramsList);

        slvTools.addStreetLightData("pole.colorcode", "", paramsList);
        slvTools.addStreetLightData("comment", strFormComment, paramsList);
        slvTools.addStreetLightData("fixing.model", "", paramsList);
        slvTools.addStreetLightData("luminaire.model", "", paramsList);

        slvTools.addStreetLightData("pole.height", "", paramsList);
        slvTools.addStreetLightData("pole.status", "", paramsList);

        slvTools.addStreetLightData("pole.type", "", paramsList);

        slvTools.addStreetLightData("pole.groundtype", "", paramsList);

        slvTools.addStreetLightData("power", "", paramsList);


        slvTools.addStreetLightData("lamp.installdate", "", paramsList);
    }

    private void doProcess(String noteGuid, String accessToken, boolean resync) {
        boolean isForTesting = false;
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
        url = url + "/" + noteGuid;
        String installationFormTempGuid = PropertiesReader.getProperties().getProperty("streetlight.installation.formtempguid");
        logger.info("Given url is :" + url);

        // Get NoteList from edgeserver
        ResponseEntity<String> responseEntity = edgeRestService.getRequest(url, false, accessToken);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String notesData = responseEntity.getBody();
            logger.info("rest service data:" + notesData);
            EdgeNote edgeNote = gson.fromJson(notesData, EdgeNote.class);
            if ((!edgeNote.getCreatedBy().contains("admin") && !edgeNote.getCreatedBy().contains("slvinterface")) || resync) {
                JsonObject edgenoteJson = new JsonParser().parse(notesData).getAsJsonObject();
                JsonArray serverForms = edgenoteJson.get("formData").getAsJsonArray();
                int size = serverForms.size();
                String errorMacAddress = "";
                boolean foundFormTemplate = false;
                for (int i = 0; i < size; i++) {
                    JsonObject serverEdgeForm = serverForms.get(i).getAsJsonObject();
                    String formDefJson = serverEdgeForm.get("formDef").getAsString();
                    String formTemplate = serverEdgeForm.get("formTemplateGuid").getAsString();
                    formDefJson = formDefJson.replaceAll("\\\\", "");
                    formDefJson = formDefJson.replace("u0026", "\\u0026");
                    List<FormValues> formComponents = gson.fromJson(formDefJson, new TypeToken<List<FormValues>>() {
                    }.getType());
                    if (formTemplate.equals(installationFormTempGuid)) {
                        ////////////////////////////////////////////////////////////////////////
                        foundFormTemplate = true;
                        List<Object> paramsList = new ArrayList<>();
                        String strActionString = FormValueUtil.getValue(formComponents, actionid);
                        String strFormExistingMac = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(existingMacAddress));
                        String strFormReplaceMac = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(replaceMacAddress));
                        String macAddress = FormValueUtil.getValue(formComponents, newMacid);


                        long currentTime = System.currentTimeMillis();
                        SlvSyncDetails slvSyncDetails = new SlvSyncDetails();
                        SlvDevice slvDevice = new SlvDevice();

                        slvSyncDetails.setNoteName(edgeNote.getTitle());
                        slvSyncDetails.setNoteGuid(edgeNote.getNoteGuid());
                        slvSyncDetails.setProcessedDateTime(currentTime);
                        slvSyncDetails.setNoteCreatedBy(edgeNote.getCreatedBy());
                        slvSyncDetails.setNoteCreatedDateTime(edgeNote.getCreatedDateTime());

                        slvDevice.setDeviceName(edgeNote.getTitle());
                        slvDevice.setDeviceId(edgeNote.getTitle());
                        slvDevice.setProcessedDateTime(currentTime);
                        try {
                            boolean isDeviceExist = slvTools.deviceAlreadyExists(edgeNote.getTitle(), edgeNote);

                            if (!isDeviceExist) {
                                String strGeoZone = FormValueUtil.getValue(formComponents, formGeoZoneId);
                                if (strGeoZone.equals("")) {
                                    strGeoZone = edgeNote.getEdgeNotebook().getNotebookName();
                                }
                                if (!strGeoZone.equals("")) {
                                    int newDeviceGeoZoneID = slvTools.checkAndCreateGeoZone(strGeoZone, edgeNote);
                                    if (newDeviceGeoZoneID != -1) {
                                        slvTools.createNewDevice(edgeNote, slvSyncDetails, Integer.toString(newDeviceGeoZoneID));
                                    } else {
                                        //Failed to Create GeoZone
                                        slvSyncDetails.setErrorDetails("Creating GeoZone [FAILED]");
                                        slvSyncDetails.setStatus(Status.Failure.toString());
                                        //connectionDAO.saveSlvSyncDetails(slvSyncDetails);
                                        throw new GeoZoneCreationFailedException("Failed to Create GeoZone");
                                    }
                                } else {
                                    //Bad GeoZone
                                    slvSyncDetails.setErrorDetails("Bad GeoZone content");
                                    slvSyncDetails.setStatus(Status.Failure.toString());
                                    //connectionDAO.saveSlvSyncDetails(slvSyncDetails);
                                    throw new GeoZoneCreationFailedException("Bad GeoZone");
                                }
                                slvSyncDetails.setDeviceCreationStatus("Device Created");
                            } else {
                                slvSyncDetails.setDeviceCreationStatus("Device Exists");
                            }
                            //At this Point Geozone and Device are ready
                            String dimmingValue = connectionDAO.getDimmingValue(edgeNote.getTitle());
                            if (dimmingValue == null) {
                                //System.out.println("Dimming value not found");
                                //throw new DimmingValueException("No dimming value not found");

                                //This setting implemented based on the customer request for
                                //lights with no dimming value to be set 100% as default
                                dimmingValue = "100% Output Design Setting";
                            }
                            ///////////////////////////////////////////////////////////////////////////////////////////
                            SlvDevice slvDevice1 = connectionDAO.getSlvDevices(edgeNote.getTitle());
                            if (slvDevice1 == null) {
                                slvDevice1 = new SlvDevice();
                                slvDevice1.setDevicevalues("[]");
                                slvDevice1.setMacAddress("");
                                slvDevice1.setDeviceId(edgeNote.getTitle());
                                slvDevice1.setProcessedDateTime(System.currentTimeMillis());
                                slvDevice1.setDeviceName(edgeNote.getTitle());
                                connectionDAO.saveSlvDevices(slvDevice1);

                            }
                            if (slvDevice1 != null) {
                                String currentDeviceValue = slvDevice1.getDevicevalues();

                                LightDataCompareResult lightDataCompareResult = null;
                                try {
                                    lightDataCompareResult = populateAndCheckWithOldValues(formComponents, edgeNote, controllerStrId,
                                            paramsList, currentDeviceValue, dimmingValue);
                                } catch (Exception e) {
                                    throw new ValueCheckException(e.getMessage());
                                }
                                if (slvDevice1.getMacAddress() == null) {
                                    slvDevice1.setMacAddress("");
                                }
                                //if (slvDevice1.getMacAddress() != null || !slvDevice1.getMacAddress().equals("")) {

                                if (strActionString.equals(strNewInstall)) {
                                    errorMacAddress = macAddress;
                                    if (!slvDevice1.getMacAddress().toUpperCase().equals(macAddress.toUpperCase())) {
                                        slvSyncDetails.setSelectedAction("Install");
                                        slvTools.addStreetLightData("installStatus", "Installed", paramsList);
                                        String strInstallDateId = PropertiesReader.getProperties().getProperty("streetlight.form.installdateid");
                                        String strFormInstallDate = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(strInstallDateId));
                                        long installDateTime = 0;
                                        if (!strFormInstallDate.equals("") && !strFormInstallDate.equals("NaN")) {
                                            installDateTime = Long.parseLong(strFormInstallDate);
                                        } else {
                                            installDateTime = edgeNote.getCreatedDateTime();
                                        }
                                        slvTools.addStreetLightData("install.date", Utils.dateFormat(installDateTime), paramsList);
                                        slvTools.addStreetLightData("lamp.installdate", Utils.dateFormat(installDateTime), paramsList);
                                        if (!DataTools.checkForValidMacAddress(macAddress)) {
                                            throw new InvalidMacAddressException("Bad macaddress " + macAddress);
                                        }
                                        slvTools.checkMacAddressExists(macAddress, edgeNote.getTitle(), edgeNote);
                                        if (isForTesting) {
                                            //Send MacAddress for Testing
                                            List<Object> paramsList1 = new ArrayList<>();
                                            paramsList1.add("idOnController=" + DataTools.URLEncoder(edgeNote.getTitle()));
                                            paramsList1.add("controllerStrId=" + controllerStrId);

                                            slvTools.addStreetLightData("MacAddress", macAddress, paramsList1);
                                            slvTools.setDeviceMacValues(paramsList1, slvSyncDetails, edgeNote);
                                        }
                                        slvTools.setDeviceValues(paramsList, slvSyncDetails, edgeNote);
                                        if (isForTesting) {
                                            try {
                                                slvTools.replaceOLC(controllerStrId, edgeNote.getTitle(), "", edgeNote);
                                                slvTools.replaceOLC(controllerStrId, edgeNote.getTitle(), macAddress, edgeNote);
                                                slvTools.syncMacAddress2Edge(edgeNote.getTitle(), macAddress, edgeNote.getEdgeNotebook().getNotebookName());
                                            } catch (ReplaceOLCFailedException e) {
                                                logger.error("Error",e);
                                            }
                                        } else {
                                            slvTools.replaceOLC(controllerStrId, edgeNote.getTitle(), "", edgeNote);
                                            slvTools.replaceOLC(controllerStrId, edgeNote.getTitle(), macAddress, edgeNote);
                                            slvTools.syncMacAddress2Edge(edgeNote.getTitle(), macAddress, edgeNote.getEdgeNotebook().getNotebookName());
                                        }
                                        slvDevice.setMacAddress(macAddress);
                                        String gsonValues = gson.toJson(lightDataCompareResult.getJsonArray());
                                        connectionDAO.updateSlvDevice(edgeNote.getTitle(), macAddress, gsonValues);
                                        slvSyncDetails.setStatus(Status.Success.toString());
                                        slvSyncDetails.setMacAddress(macAddress);
                                    } else {
                                        if (lightDataCompareResult.isMustUpdate()) {
                                            slvSyncDetails.setSelectedAction("Install-Update Light data Only");
                                            slvTools.setDeviceValues(paramsList, slvSyncDetails, edgeNote);
                                            String gsonValues = gson.toJson(lightDataCompareResult.getJsonArray());
                                            connectionDAO.updateSlvDevice(edgeNote.getTitle(), macAddress, gsonValues);
                                            slvSyncDetails.setMacAddress(macAddress);
                                        } else {
                                            slvSyncDetails.setSelectedAction("Install-No Change");
                                            slvSyncDetails.setMacAddress(macAddress);
                                        }
                                        slvSyncDetails.setStatus(Status.Success.toString());
                                    }
                                } else if (strActionString.equals(strReplace)) {
                                    errorMacAddress = strFormReplaceMac;
                                    if (!slvDevice1.getMacAddress().toUpperCase().equals(strFormReplaceMac.toUpperCase())) {
                                        slvSyncDetails.setSelectedAction("Replace");
                                        slvTools.addStreetLightData("installStatus", "Installed", paramsList);
                                        String strReplaceDateId = PropertiesReader.getProperties().getProperty("streetlight.form.replacedateid");
                                        String strFormReplaceDate = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(strReplaceDateId));
                                        long replaceDateTime = 0;
                                        if (!strFormReplaceDate.equals("") && !strFormReplaceDate.equals("NaN")) {
                                            replaceDateTime = Long.parseLong(strFormReplaceDate);
                                        } else {
                                            replaceDateTime = edgeNote.getCreatedDateTime();
                                        }
                                        slvTools.addStreetLightData("install.date", Utils.dateFormat(replaceDateTime), paramsList);
                                        slvTools.addStreetLightData("lamp.installdate", Utils.dateFormat(replaceDateTime), paramsList);
                                        if (!DataTools.checkForValidMacAddress(strFormReplaceMac)) {
                                            throw new InvalidMacAddressException("Bad macaddress " + strFormReplaceMac);
                                        }
                                        slvTools.checkMacAddressExists(strFormReplaceMac, edgeNote.getTitle(), edgeNote);
                                        if (isForTesting) {
                                            //Send MacAddress for Testing
                                            List<Object> paramsList1 = new ArrayList<>();
                                            paramsList1.add("idOnController=" + DataTools.URLEncoder(edgeNote.getTitle()));
                                            paramsList1.add("controllerStrId=" + controllerStrId);

                                            slvTools.addStreetLightData("MacAddress", strFormReplaceMac, paramsList1);
                                            slvTools.setDeviceMacValues(paramsList1, slvSyncDetails, edgeNote);
                                        }
                                        slvTools.setDeviceValues(paramsList, slvSyncDetails, edgeNote);
                                        if (isForTesting) {
                                            try {
                                                slvTools.replaceOLC(controllerStrId, edgeNote.getTitle(), "", edgeNote);
                                                slvTools.replaceOLC(controllerStrId, edgeNote.getTitle(), strFormReplaceMac, edgeNote);
                                                slvTools.syncMacAddress2Edge(edgeNote.getTitle(), strFormReplaceMac, edgeNote.getEdgeNotebook().getNotebookName());
                                            } catch (ReplaceOLCFailedException e) {
                                                logger.error("Error",e);
                                            }
                                        } else {
                                            slvTools.replaceOLC(controllerStrId, edgeNote.getTitle(), "", edgeNote);
                                            slvTools.replaceOLC(controllerStrId, edgeNote.getTitle(), strFormReplaceMac, edgeNote);
                                            slvTools.syncMacAddress2Edge(edgeNote.getTitle(), strFormReplaceMac, edgeNote.getEdgeNotebook().getNotebookName());
                                        }
                                        String gsonValues = gson.toJson(lightDataCompareResult.getJsonArray());
                                        connectionDAO.updateSlvDevice(edgeNote.getTitle(), strFormReplaceMac, gsonValues);
                                        slvSyncDetails.setStatus(Status.Success.toString());
                                        slvSyncDetails.setMacAddress(strFormReplaceMac);
                                    } else {
                                        if (lightDataCompareResult.isMustUpdate()) {
                                            slvSyncDetails.setSelectedAction("Replace-Light Data Only");
                                            slvTools.setDeviceValues(paramsList, slvSyncDetails, edgeNote);
                                            String gsonValues = gson.toJson(lightDataCompareResult.getJsonArray());
                                            connectionDAO.updateSlvDevice(edgeNote.getTitle(), strFormReplaceMac, gsonValues);

                                        } else {
                                            slvSyncDetails.setSelectedAction("Replace-No Changes");
                                        }
                                        slvSyncDetails.setStatus(Status.Success.toString());
                                        slvSyncDetails.setMacAddress(strFormReplaceMac);
                                    }
                                } else if (strActionString.equals(strRemove)) {
                                    errorMacAddress = "";
                                    slvSyncDetails.setSelectedAction("Remove");
                                    String strReasonRemove = PropertiesReader.getProperties().getProperty("streetlight.form.removereason");
                                    String strFormReasonRemove = FormValueUtil.getValue(formComponents, DataTools.convertFormIDToInt(strReasonRemove));
                                    String strPoleRemove = PropertiesReader.getProperties().getProperty("streetlight.form.rreason.remove");
                                    String strPoleDown = PropertiesReader.getProperties().getProperty("streetlight.form.rreason.poledown");
                                    if (!strFormReasonRemove.equals(strPoleRemove) && !strFormReasonRemove.equals(strPoleDown)) {
                                        List<Object> paramsListClear = new ArrayList<>();
                                        try {

                                            clearDeviceValues(formComponents, edgeNote, controllerStrId, paramsListClear);
                                        } catch (Exception e) {
                                            throw new ValueClearException(e.getMessage());
                                        }
                                        slvTools.setDeviceValues(paramsListClear, slvSyncDetails, edgeNote);
                                        if (isForTesting) {
                                            //Send MacAddress for Testing
                                            List<Object> paramsList1 = new ArrayList<>();
                                            paramsList1.add("idOnController=" + DataTools.URLEncoder(edgeNote.getTitle()));
                                            paramsList1.add("controllerStrId=" + controllerStrId);

                                            slvTools.addStreetLightData("MacAddress", "", paramsList1);
                                            slvTools.setDeviceMacValues(paramsList1, slvSyncDetails, edgeNote);
                                        }
                                        if (isForTesting) {
                                            try {
                                                slvTools.replaceOLC(controllerStrId, edgeNote.getTitle(), "", edgeNote);
                                                slvTools.removeEdgeSLVMacAddress(edgeNote.getTitle());
                                            } catch (Exception e) {
                                                logger.error("Error",e);
                                            }
                                        } else {
                                            slvTools.replaceOLC(controllerStrId, edgeNote.getTitle(), "", edgeNote);
                                            slvTools.removeEdgeSLVMacAddress(edgeNote.getTitle());
                                        }

                                        connectionDAO.updateSlvDevice(edgeNote.getTitle(), "", "[]");
                                        slvSyncDetails.setStatus(Status.Success.toString());
                                        slvSyncDetails.setMacAddress("");
                                    }
                                }
                                //}
                            }

                            ////////////////////////////////////////////////////////////////////////

                            //End Template match
                        } catch (GeoZoneCreationFailedException e) {
                            logger.error("Error",e);
                            slvSyncDetails.setStatus(Status.Failure.toString());
                            slvSyncDetails.setErrorDetails("Error creating GeoZone");
                            slvSyncDetails.setMacAddress(errorMacAddress);
                        } catch (SearchGeoZoneException e) {
                            logger.error("Error",e);
                            slvSyncDetails.setStatus(Status.Failure.toString());
                            slvSyncDetails.setErrorDetails("Error searching GeoZone");
                            slvSyncDetails.setMacAddress(errorMacAddress);
                        } catch (DeviceCreationFailedException e) {
                            logger.error("Error",e);
                            slvSyncDetails.setStatus(Status.Failure.toString());
                            slvSyncDetails.setErrorDetails("Error creating Device");
                            slvSyncDetails.setMacAddress(errorMacAddress);
                        } catch (QRCodeAlreadyUsedException e) {
                            logger.error("Error",e);
                            slvSyncDetails.setStatus(Status.Failure.toString());
                            slvSyncDetails.setErrorDetails("Error using MacAddress " + e.getMessage());
                            slvSyncDetails.setMacAddress(errorMacAddress);
                        } catch (DeviceUpdationFailedException e) {
                            logger.error("Error",e);
                            slvSyncDetails.setStatus(Status.Failure.toString());
                            slvSyncDetails.setErrorDetails("Error creating updating Device attributes");
                            slvSyncDetails.setMacAddress(errorMacAddress);
                        } catch (ReplaceOLCFailedException e) {
                            logger.error("Error",e);
                            slvSyncDetails.setStatus(Status.Failure.toString());
                            slvSyncDetails.setErrorDetails("Error calling Replace OLC");
                            slvSyncDetails.setMacAddress(errorMacAddress);
                        } catch (IOException e) {
                            logger.error("Error",e);
                            slvSyncDetails.setStatus(Status.Failure.toString());
                            slvSyncDetails.setErrorDetails("Error during connection " + e.getMessage());
                            slvSyncDetails.setMacAddress(errorMacAddress);
                        } catch (ValueCheckException e) {
                            logger.error("Error",e);
                            slvSyncDetails.setStatus(Status.Failure.toString());
                            slvSyncDetails.setErrorDetails("Error Comparing Light attributes data");
                            slvSyncDetails.setMacAddress(errorMacAddress);
                        } catch (ValueClearException e) {
                            logger.error("Error",e);
                            slvSyncDetails.setStatus(Status.Failure.toString());
                            slvSyncDetails.setErrorDetails("Error clearing Light attributes  data");
                            slvSyncDetails.setMacAddress(errorMacAddress);
                        } catch (InvalidMacAddressException e) {
                            logger.error("Error",e);
                            slvSyncDetails.setStatus(Status.Failure.toString());
                            slvSyncDetails.setErrorDetails("Error in macaddress format");
                            slvSyncDetails.setMacAddress(errorMacAddress);
                        } catch (ErrorCheckDeviceExists e) {
                            logger.error("Error",e);
                            slvSyncDetails.setStatus(Status.Failure.toString());
                            slvSyncDetails.setErrorDetails(e.getMessage());
                            slvSyncDetails.setMacAddress(errorMacAddress);
                        } catch (DimmingValueException e) {
                            logger.error("Error",e);
                            slvSyncDetails.setErrorDetails("Not Dimming value found");
                            slvSyncDetails.setStatus(Status.Failure.toString());
                            slvSyncDetails.setMacAddress(errorMacAddress);
                        } catch (Exception e) {
                            logger.error("Error",e);
                            slvSyncDetails.setErrorDetails(e.getMessage());
                            slvSyncDetails.setStatus(Status.Failure.toString());
                            slvSyncDetails.setMacAddress(errorMacAddress);

                        } finally {
                            connectionDAO.saveSlvSyncDetails(slvSyncDetails);
                        }
                        //End Form Loop
                    }

                }
                if (!foundFormTemplate) {
                    SlvSyncDetails slvSyncDetails = new SlvSyncDetails();
                    slvSyncDetails.setNoteName(edgeNote.getTitle());
                    slvSyncDetails.setNoteGuid(edgeNote.getNoteGuid());
                    slvSyncDetails.setProcessedDateTime(System.currentTimeMillis());
                    slvSyncDetails.setNoteCreatedBy(edgeNote.getCreatedBy());
                    slvSyncDetails.setNoteCreatedDateTime(edgeNote.getCreatedDateTime());
                    slvSyncDetails.setStatus(Status.Failure.toString());
                    slvSyncDetails.setErrorDetails("No matching formtemplate");
                    connectionDAO.saveSlvSyncDetails(slvSyncDetails);
                }
            }
        }
    }

    public void run() {


        String actionResync = PropertiesReader.getProperties().getProperty("streetlight.edge.data.resync");

        if (actionResync == null) {
            actionResync = "false";
        }

        if (actionResync.equals("true")) {
            BufferedReader bufferedReader = null;
            try {

                File f = new File("./resync.txt");

                bufferedReader = new BufferedReader(new FileReader(f));

                String readLine = "";

                System.out.println("Reading file using Buffered Reader");

                while ((readLine = bufferedReader.readLine()) != null) {
                    System.out.println(readLine);
                    if (!readLine.equals("")) {
                        System.out.println("Processing " + readLine);
                        String terragoAccessToken = edgeRestService.getEdgeToken();
                        doProcess(readLine, terragoAccessToken, false);
                    }
                }


            } catch (IOException e) {
                logger.error("Error",e);
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e) {

                    }
                }
            }
        } else {
            //##############################################################################################
            do {
                //Normal SLV Process
                String edgeSlvUrl = PropertiesReader.getProperties().getProperty("streetlight.edge.slvserver.url");
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
                                    terragoAccessToken = edgeRestService.getEdgeToken();
                                    doProcess(noteGuid, terragoAccessToken, false);
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error",e);
                    }

                }
                try {
                    Thread.sleep(10000);
                } catch (Exception e) {
                    logger.error("Error",e);
                }
            } while (true);
            //########################################################################################
        }


    }
}
