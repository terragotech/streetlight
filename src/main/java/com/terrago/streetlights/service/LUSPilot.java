package com.terrago.streetlights.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terrago.streetlights.dao.TerragoDAO;
import com.terrago.streetlights.dao.model.DeviceData;
import com.terrago.streetlights.dao.model.UbiTransactionLog;
import com.terrago.streetlights.utils.LastUpdated;
import com.terrago.streetlights.utils.PropertiesReader;
import com.terrago.streetlights.utils.TerragoUtils;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import org.apache.log4j.Logger;

import java.lang.reflect.Type;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class LUSPilot extends MonitorChanges{
    private Logger logger = Logger.getLogger(MonitorChanges.class);
    public static int INSTALL_COMPLETE = 1;
    private String getTodayInstallDate(){
        String pattern = "MM-dd-yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("CDT"));
        String installDate = simpleDateFormat.format(new Date());

        System.out.println(installDate);
        return  installDate;
    }
    public void processNote(String nnguid,LastUpdated curNoteInfo,int mode)
    {
        String ignoreUser = PropertiesReader.getProperties().getProperty("ignoreuser");
        String instSuccessLayerGUID = PropertiesReader.getProperties().getProperty("lyrnodeinstallcomplete");
        String notesJson = RESTService.getNoteDetails(nnguid);
        Type listType = new TypeToken<ArrayList<EdgeNote>>() {
        }.getType();
        Gson gson = new Gson();
        List<EdgeNote> edgeNoteList = new ArrayList<>();
        //    List<EdgeNote> edgeNoteList = gson.fromJson(notesJson, listType);
        EdgeNote restEdgeNote = gson.fromJson(notesJson, EdgeNote.class);
        JsonObject edgenoteJson = new JsonParser().parse(notesJson).getAsJsonObject();
        String createdByUser = edgenoteJson.get("createdBy").getAsString();
        if(createdByUser.equals(ignoreUser))
        {
            UbiTransactionLog ubiTransactionLog = new UbiTransactionLog();
            ubiTransactionLog.setEventtime(System.currentTimeMillis());
            ubiTransactionLog.setSynctime(curNoteInfo.getSynctime());
            ubiTransactionLog.setDevui("");
            ubiTransactionLog.setDeviceStatus("");
            ubiTransactionLog.setAction("Not ignored because of revision");
            ubiTransactionLog.setTitle(curNoteInfo.getTitle());
            ubiTransactionLog.setNotegui(curNoteInfo.getNoteguid());
            TerragoDAO.addUbiTransactionLog(ubiTransactionLog);
            return;
        }
        curNoteInfo.setCreatedBy(createdByUser);
        JsonArray serverForms = edgenoteJson.get("formData").getAsJsonArray();
        int size = serverForms.size();

        boolean ispresent = false;
        boolean isDCPresent = false;
        boolean isInvalidUser = false;
        List<EdgeFormData> installFormComponents = null;
        List<EdgeFormData> auditFormComponents = null;
        for (int i = 0; i < size; i++) {
            JsonObject serverEdgeForm = serverForms.get(i).getAsJsonObject();
            String formDefJson = serverEdgeForm.get("formDef").getAsString();
            String formTemplate = serverEdgeForm.get("formTemplateGuid").getAsString();
            List<EdgeFormData> formComponents = gson.fromJson(formDefJson, new TypeToken<List<EdgeFormData>>() {
            }.getType());

            String instFrmTemplates = PropertiesReader.getProperties().getProperty("formtemplatetoinstall");
            String []arrinstFrmTemplates = instFrmTemplates.split(",");

            String auditFrmTemplates = PropertiesReader.getProperties().getProperty("formtemplatetoaudit");
            String []arrauditFrmTemplates = auditFrmTemplates.split(",");

            for(int idx=0;idx<arrinstFrmTemplates.length;idx++)
            {
                if (formTemplate.equals(arrinstFrmTemplates[idx])) {
                    installFormComponents = formComponents;
                }
            }
            for(int jdx=0;jdx<arrauditFrmTemplates.length;jdx++)
            {
                if (formTemplate.equals(arrauditFrmTemplates[jdx])) {
                    auditFormComponents = formComponents;
                }
            }

            if (formTemplate.equals(PropertiesReader.getProperties().getProperty("formtemplatedc"))) {
                isDCPresent = true;
            }

        }//End of For Loop
        if(isDCPresent)
        {
            //Do Device Control
            LastUpdated lastUpdated = TerragoDAO.getNoteInfo(nnguid);
            String strTitle = lastUpdated.getTitle();
            int idx = strTitle.lastIndexOf("-DC");
            String noteTitle = strTitle.substring(0,idx);
            String dcnoteid = TerragoDAO.getNoteGUID(noteTitle);
            doDeviceControl(dcnoteid);
        }
        if(installFormComponents != null) {
            int result = doInstall(curNoteInfo, auditFormComponents, installFormComponents, mode);
            if(result == INSTALL_COMPLETE)
            {
                //Must Update Note Layer
                String nguid = performLayerChange(curNoteInfo.getNoteguid(),notesJson,instSuccessLayerGUID);
                TerragoDAO.updateUser(nguid,ignoreUser);
            }
        }
        UbiTransactionLog ubiTransactionLog = new UbiTransactionLog();
        ubiTransactionLog.setEventtime(System.currentTimeMillis());
        ubiTransactionLog.setSynctime(curNoteInfo.getSynctime());
        ubiTransactionLog.setDevui("");
        ubiTransactionLog.setDeviceStatus("");
        ubiTransactionLog.setAction("Processed");
        ubiTransactionLog.setTitle(curNoteInfo.getTitle());
        ubiTransactionLog.setNotegui(curNoteInfo.getNoteguid());
        TerragoDAO.addUbiTransactionLog(ubiTransactionLog);
    }
    public void startMonitoring2()
    {
        String nnguid = "";
        do {
            try {
                processUnFoundDevices();
                long lastMaxUpdatedTime = TerragoDAO.readLastUpdatedTime();
                if (lastMaxUpdatedTime == 0) {
                    lastMaxUpdatedTime = System.currentTimeMillis() - 300000;
                }
                System.out.println("Looking for Changes ...");
                List<LastUpdated> lstUpdated = getUpdatedNotes(Long.toString(lastMaxUpdatedTime));
                for (LastUpdated lstCur : lstUpdated) {
                    System.out.println("Processing Changes ..." + lstCur.getNoteguid() );
                    logger.info("Processing Changes ..." + lstCur.getNoteguid() );
                    lastMaxUpdatedTime = Math.max(lastMaxUpdatedTime, lstCur.getSynctime());
                    nnguid = lstCur.getNoteguid();
                    try {
                        processNote(nnguid, lstCur, PROCESS_NORMAL_INSTALL);
                    }
                    catch (Exception e)
                    {
                        logger.error("Error processing note:" + lstCur.getNoteguid(),e);
                        UbiTransactionLog ubiTransactionLog = new UbiTransactionLog();
                        ubiTransactionLog.setEventtime(System.currentTimeMillis());
                        ubiTransactionLog.setSynctime(lstCur.getSynctime());
                        ubiTransactionLog.setDevui("");
                        ubiTransactionLog.setDeviceStatus("");
                        ubiTransactionLog.setAction("Error Processing");
                        ubiTransactionLog.setTitle(lstCur.getTitle());
                        ubiTransactionLog.setNotegui(lstCur.getNoteguid());
                        TerragoDAO.addUbiTransactionLog(ubiTransactionLog);
                    }
                }
                TerragoDAO.writeLastUpdateTime(lastMaxUpdatedTime);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }catch (Exception e)
            {
                logger.error("Error processing" + e);
            }
        }while(true);
    }
    public void doDeviceControl(String noteguid)
    {
        String notesJson = RESTService.getNoteDetails(noteguid);
        Type listType = new TypeToken<ArrayList<EdgeNote>>() {
        }.getType();
        Gson gson = new Gson();
        List<EdgeNote> edgeNoteList = new ArrayList<>();
        //    List<EdgeNote> edgeNoteList = gson.fromJson(notesJson, listType);
        EdgeNote restEdgeNote = gson.fromJson(notesJson, EdgeNote.class);
        JsonObject edgenoteJson = new JsonParser().parse(notesJson).getAsJsonObject();
        JsonArray serverForms = edgenoteJson.get("formData").getAsJsonArray();
        int size = serverForms.size();
        String ignoreUser = PropertiesReader.getProperties().getProperty("ignoreuser");
        boolean ispresent = false;
        boolean isDCPresent = false;
        boolean isInvalidUser = false;
        List<EdgeFormData> installFormComponents = null;
        List<EdgeFormData> auditFormComponents = null;

        for (int i = 0; i < size; i++) {
            JsonObject serverEdgeForm = serverForms.get(i).getAsJsonObject();
            String formDefJson = serverEdgeForm.get("formDef").getAsString();
            String formTemplate = serverEdgeForm.get("formTemplateGuid").getAsString();
            List<EdgeFormData> formComponents = gson.fromJson(formDefJson, new TypeToken<List<EdgeFormData>>() {
            }.getType());

            String instFrmTemplates = PropertiesReader.getProperties().getProperty("formtemplatetoinstall");
            String []arrinstFrmTemplates = instFrmTemplates.split(",");

            String auditFrmTemplates = PropertiesReader.getProperties().getProperty("formtemplatetoaudit");
            String []arrauditFrmTemplates = auditFrmTemplates.split(",");

            for(int idx=0;idx<arrinstFrmTemplates.length;idx++)
            {
                if (formTemplate.equals(arrinstFrmTemplates[idx])) {
                    installFormComponents = formComponents;
                }
            }


        }
        if(installFormComponents != null) {
            String idDev = PropertiesReader.getProperties().getProperty("ubicquia_deveui");
            int nidDev = Integer.parseInt(idDev);
            String dev_eui = TerragoUtils.getEdgeFormValue(installFormComponents, nidDev);
            dev_eui = TerragoUtils.parseDevUI(dev_eui);
            UbicquiaLightsInterface.requestDynamicToken();
            LastUpdated lastUpdated = TerragoDAO.getNoteInfo(noteguid);
            JsonObject jsonObject = UbicquiaLightsInterface.getNodes(lastUpdated, dev_eui);
            String nodeid = jsonObject.get("id").getAsString();
            new CycleSingleLightDeviceControl(lastUpdated, nodeid);
        }
    }
    public int doInstall(LastUpdated lastUpdated,
                          List<EdgeFormData> formComponents,
                          List<EdgeFormData> installformComponents, int mode){
        int result = 0;
        String idPoleId = PropertiesReader.getProperties().getProperty("ubicquia_poleid");
        String idPoleHeight = PropertiesReader.getProperties().getProperty("ubicquia_poleheight");
        String iduPoleHeight = PropertiesReader.getProperties().getProperty("ubicquia_upoleheight");
        String idPoleType = PropertiesReader.getProperties().getProperty("ubicquia_poletype");
        String idPoleColor = PropertiesReader.getProperties().getProperty("ubicquia_polecolor");

        int nidPoleId = Integer.parseInt(idPoleId);
        int nidPoleHeight = Integer.parseInt(idPoleHeight);
        int niduPoleHeight = Integer.parseInt(iduPoleHeight);
        int nidPoleType = Integer.parseInt(idPoleType);
        int nidPoleColor = Integer.parseInt(idPoleColor);

        //Mapped to Custom2
        String idFeed = PropertiesReader.getProperties().getProperty("ubicquia_feed");
        int nidFeed = Integer.parseInt(idFeed);

        String idFixtureId = PropertiesReader.getProperties().getProperty("ubicquia_fixid");
        String idFixtureType = PropertiesReader.getProperties().getProperty("ubicquia_fixtype");
        String idFixtureuType = PropertiesReader.getProperties().getProperty("ubicquia_ufixtype");
        String idFixtureWattage = PropertiesReader.getProperties().getProperty("ubicquia_wattage");
        //Mapped to Custom 1
        String idLampType = PropertiesReader.getProperties().getProperty("ubicquia_lamptype");
        //Mapped to Custom 3
        //Install Time Stamp

        int nidFixtureId = Integer.parseInt(idFixtureId);
        int nidFixtureType = Integer.parseInt(idFixtureType);
        int niduFixtureType = Integer.parseInt(idFixtureuType);
        int nidFixtureWattage = Integer.parseInt(idFixtureWattage);
        int nidLampType = Integer.parseInt(idLampType);



        String idDev = PropertiesReader.getProperties().getProperty("ubicquia_deveui");
        int nidDev = Integer.parseInt(idDev);

        //Now getting Form values
        String strPoleId = TerragoUtils.getEdgeFormValue(installformComponents,nidPoleId);
        String strPoleHeight = TerragoUtils.getEdgeFormValue(installformComponents,nidPoleHeight);
        String struPoleHeight = TerragoUtils.getEdgeFormValue(installformComponents,niduPoleHeight);
        String strPoleType = TerragoUtils.getEdgeFormValue(installformComponents,nidPoleType);
        String strPoleColor =  TerragoUtils.getEdgeFormValue(installformComponents,nidPoleColor);
        String strFeed = TerragoUtils.getEdgeFormValue(installformComponents,nidFeed);

        String strFixtureId = TerragoUtils.getEdgeFormValue(installformComponents,nidFixtureId);
        String strFixtureType = TerragoUtils.getEdgeFormValue(installformComponents,nidFixtureType);
        String struFixtureType = TerragoUtils.getEdgeFormValue(installformComponents,niduFixtureType);
        String strFixtureWattage = TerragoUtils.getEdgeFormValue(installformComponents,nidFixtureWattage);
        String strLampType = TerragoUtils.getEdgeFormValue(installformComponents,nidLampType);

        String dev_eui = TerragoUtils.getEdgeFormValue(installformComponents,nidDev);

        if(strPoleHeight.equals("Unknown"))
        {
            strPoleHeight = struPoleHeight;
        }
        if(strFixtureType.equals("Unk Unknown"))
        {
            strFixtureType = struFixtureType;
        }
        dev_eui = TerragoUtils.parseDevUI(dev_eui);
        if(!dev_eui.equals(""))
        {
            UbicquiaLightsInterface.requestDynamicToken();
            JsonObject jsonObject = UbicquiaLightsInterface.getNodes(lastUpdated,dev_eui);
            if(jsonObject != null)
            {
                //Do Update
                UbicquiaLightsInterface.requestDynamicToken();
                //Change Node Name
                String nodeid = jsonObject.get("id").getAsString();
                String strInstallDate = getTodayInstallDate();
                jsonObject.addProperty("poleId", strPoleId);
                jsonObject.addProperty("poleHeight", strPoleHeight);
                jsonObject.addProperty("poleType", strPoleType);
                jsonObject.addProperty("poleColor", strPoleColor);
                //jsonObject.addProperty("Custom2", strPoleColor);

                jsonObject.addProperty("fixtureId", strFixtureId);
                jsonObject.addProperty("node", strFixtureId);
                jsonObject.addProperty("fixtureType", strFixtureType);
                jsonObject.addProperty("fixture_wattage", strFixtureWattage);
                //jsonObject.addProperty("Custom1", strLampType);
                //jsonObject.addProperty("Custom3",strInstallDate);



                //poleId


                String updateDataJSON =  jsonObject.toString();
                String nodeUpdateResponse = UbicquiaLightsInterface.setNodeData(lastUpdated,nodeid,updateDataJSON);

                JsonObject jsonObject1 = new JsonObject();
                JsonArray customDataArray = new JsonArray();

                JsonObject cusDataLampType = new JsonObject();
                cusDataLampType.addProperty("display_name","Lamp Type");
                cusDataLampType.addProperty("key","custom1");
                cusDataLampType.addProperty("value",strLampType);

                JsonObject cusDataFeed = new JsonObject();
                cusDataFeed.addProperty("display_name","Feed");
                cusDataFeed.addProperty("key","custom2");
                cusDataFeed.addProperty("value",strFeed);

                JsonObject cusDataInstallDate = new JsonObject();
                cusDataInstallDate.addProperty("display_name","Ubicell Install Date");
                cusDataInstallDate.addProperty("key","custom3");
                cusDataInstallDate.addProperty("value",strInstallDate);

                String strInstaller = lastUpdated.getCreatedBy();
                JsonObject cusDataInstaller = new JsonObject();
                cusDataInstaller.addProperty("display_name","Installer");
                cusDataInstaller.addProperty("key","custom4");
                cusDataInstaller.addProperty("value",strInstaller);

                customDataArray.add(cusDataLampType);
                customDataArray.add(cusDataFeed);
                customDataArray.add(cusDataInstallDate);
                customDataArray.add(cusDataInstaller);

                jsonObject1.addProperty("dev_eui",dev_eui);
                jsonObject1.add("custom_data",customDataArray);
                String customDataToUpdate = jsonObject1.toString();
                System.out.println(customDataToUpdate);
                logger.info("Custom Data : " + customDataToUpdate);
                String CustomDataResponse = UbicquiaLightsInterface.updateCustomFields(lastUpdated,customDataToUpdate,dev_eui);
                if(CustomDataResponse != null && nodeUpdateResponse != null)
                {
                      result = 1;
                }
                if(mode == PROCESS_NOT_FOUND_DEVICES)
                {
                    DeviceData deviceData = new DeviceData();
                    deviceData.setParentnoteguid(lastUpdated.getParentnoteguid());
                    TerragoDAO.updateDeviceData(deviceData);
                }
            }
            else
            {
                LastUpdated l1 = TerragoDAO.getUpdateInfo(lastUpdated.getNoteguid());
                //Write into Table
                if(mode == PROCESS_NORMAL_INSTALL) {
                    DeviceData deviceData = new DeviceData();
                    deviceData.setStatus("NOT_FOUND");
                    deviceData.setDev_eui(dev_eui);
                    if(l1.getParentnoteguid() == null || l1.getParentnoteguid().equals("")){
                        deviceData.setParentnoteguid(lastUpdated.getNoteguid());
                    }
                    else
                    {
                        deviceData.setParentnoteguid(l1.getParentnoteguid());
                    }

                    TerragoDAO.addDeviceData(deviceData);
                }
            }
        }
        return result;
    }
}
