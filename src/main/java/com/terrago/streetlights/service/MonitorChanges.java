package com.terrago.streetlights.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import com.terrago.streetlights.App;
import com.terrago.streetlights.dao.DataBaseConnector;
import com.terrago.streetlights.dao.TerragoDAO;
import com.terrago.streetlights.dao.model.DeviceData;
import com.terrago.streetlights.dao.model.UbiTransactionLog;
import com.terrago.streetlights.json.UbiData;
import com.terrago.streetlights.utils.JsonDataParser;
import com.terrago.streetlights.utils.LastUpdated;
import com.terrago.streetlights.utils.PropertiesReader;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.edgeserver.FormData;
import com.terragoedge.streetlight.json.model.NoteInfo;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.swing.plaf.nimbus.State;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.terrago.streetlights.utils.*;

public class MonitorChanges {
    private Logger logger = Logger.getLogger(MonitorChanges.class);
    private static final int PROCESS_NOT_FOUND_DEVICES = 1;
    private static final int PROCESS_NORMAL_INSTALL = 0;
    //
    private String getDevicControlStatus(List<EdgeFormData> formComponents)
    {
        //ubicquia_replacedeveui
        int idReplacedevui = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_devicecontrol"));
        String strReplacedevui = "";
        EdgeFormData repdevui = new EdgeFormData();
        repdevui.setId(idReplacedevui);
        int pos = formComponents.indexOf(repdevui);
        if(pos != -1){
            EdgeFormData tmp1 = formComponents.get(pos);
            strReplacedevui = tmp1.getValue();
        }
        return strReplacedevui;
    }
    private String getReplaceDevui(List<EdgeFormData> formComponents)
    {
        //ubicquia_replacedeveui
        int idReplacedevui = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_replacedeveui"));
        String strReplacedevui = "";
        EdgeFormData repdevui = new EdgeFormData();
        repdevui.setId(idReplacedevui);
        int pos = formComponents.indexOf(repdevui);
        if(pos != -1){
            EdgeFormData tmp1 = formComponents.get(pos);
            strReplacedevui = tmp1.getValue();
        }
        return strReplacedevui;
    }
    private String getPoleID(List<EdgeFormData> formComponents)
    {
        int idPoleIdInstall = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_poleidi"));
        String strPoleID = "";
        EdgeFormData poleIdInstall = new EdgeFormData();
        poleIdInstall.setId(idPoleIdInstall);
        int pos = formComponents.indexOf(poleIdInstall);
        if(pos != -1){
            EdgeFormData tmp1 = formComponents.get(pos);
            strPoleID = tmp1.getValue();
        }
        return strPoleID;
    }
    private String getAction(List<EdgeFormData> formComponents)
    {
        int idAction = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_action"));
        String actionString = "";
        EdgeFormData actionInstall = new EdgeFormData();
        actionInstall.setId(idAction);
        int pos = formComponents.indexOf(actionInstall);
        if(pos != -1){
            EdgeFormData tmp1 = formComponents.get(pos);
            actionString = tmp1.getValue();
        }
        return actionString;
    }

    private List<LastUpdated> getUpdatedNotes(String lastProcessedTime)
    {
        List<LastUpdated> lstString = new ArrayList<LastUpdated>();
        Connection conn = DataBaseConnector.getConnection();
        Statement statement = null;
        ResultSet resultSet = null;
        //logger.info("Checking for updates");
        try {
            statement = conn.createStatement();
            resultSet = statement.executeQuery("select title,noteguid,synctime from edgenote where iscurrent=true and isdeleted=false and synctime > " + lastProcessedTime + " order by synctime ");
            while(resultSet.next())
            {
                String noteguid = resultSet.getString("noteguid");
                LastUpdated lastUpdated = new LastUpdated();
                lastUpdated.setNoteguid(noteguid);
                lastUpdated.setSynctime(resultSet.getLong("synctime"));
                lastUpdated.setTitle(resultSet.getString("title"));
                lstString.add(lastUpdated);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if(resultSet != null)
            {
                try{
                    resultSet.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
            if(statement != null)
            {
                try{
                    statement.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
            DataBaseConnector.closeConnection();
        }
        return lstString;
    }

    private String checkDataNull(JsonObject jsonObject,String key)
    {
        if(!jsonObject.get(key).isJsonNull() && jsonObject.get(key) != null)
        {
            return jsonObject.get(key).getAsString();
        }
        return "";
    }
    private void doSingleLightOn(LastUpdated lastUpdated,String fixtureID,List<EdgeFormData> formComponents)
    {
        if(fixtureID != null)
        {
            try
            {
                UbicquiaLightsInterface.requestDynamicToken();
                String result = UbicquiaLightsInterface.getQueryData(fixtureID);
                if(result != null)
                {
                    TerragoUpdate.updateEdgeForm(formComponents, 39, "");
                    JsonObject jobj = JsonDataParser.getJsonObject(result);
                    String strID = checkDataNull(jobj, "id");
                    if(strID != null)
                    {
                        new SingleLightDeviceControl(lastUpdated,strID);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    private void doGroupLightsOn2(LastUpdated lastUpdated,List<EdgeFormData> formComponents,List<UbiData> lstUbiData)
    {

            String mfc = PropertiesReader.getProperties().getProperty("ubicquia_mdc");
            int idmfc = Integer.parseInt(mfc);
            TerragoUpdate.updateEdgeForm(formComponents, idmfc, "No");

            String strDCType = PropertiesReader.getProperties().getProperty("ubicquia_dctype");
            int idDCType = Integer.parseInt(strDCType);
            TerragoUpdate.updateEdgeForm(formComponents, idDCType, "");


        List<String> lstID = new ArrayList<String>();
        for(UbiData ubiData:lstUbiData)
        {
            lstID.add(ubiData.getId());
        }
        if(lstID.size() > 0) {
            new GroupDeviceControl(lastUpdated,lstID);
        }
    }
    private void doGroupLightsOn(LastUpdated lastUpdated,String result,List<EdgeFormData> formComponents)
    {
        if(result != null)
        {
            try {
                //Update the Form Data
                String mfc = PropertiesReader.getProperties().getProperty("ubicquia_mdc");
                int idmfc = Integer.parseInt(mfc);

                TerragoUpdate.updateEdgeForm(formComponents, idmfc, "No");
                JsonObject jobj = JsonDataParser.getJsonObject(result);
                String groupID = checkDataNull(jobj, "groupId");
                List<String> lstID = UbicquiaLightsInterface.getGroupNodes(groupID);
                System.out.println(lstID.size());
                if(lstID.size() > 0) {
                    new GroupDeviceControl(lastUpdated,lstID);
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    private void doInstallation(LastUpdated lastUpdated,String result,List<EdgeFormData> formComponents, int iMode,LatLong latLong,String repdevui)
    {
        if (result != null)
        {
            /*
            int idpoletype = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_poletype"));
            int idfixtype = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_fixtype"));
            int idID = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_id"));
            int idnode = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_node"));
            int idlat = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_lat"));
            int idlng = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_long"));
            int idmc = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_mc"));
            int idpoleid = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_poleid"));
            int idfixid = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_fixid"));
            */

            //Existing Fixture Ids
            int idPoleid = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_poleid"));
            int idID = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_id"));
            int idnode = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_node"));

            int idGroup = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_group"));
            int idZone = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_zone"));

            int idlat = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_lat"));
            int idlng = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_long"));

            int idmc = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_mc"));

            int idfixid = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_fixid"));
            int idfixtype = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_fixtype"));


            //Install IDs
            int idPoleIdInstall = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_poleidi"));
            int idPoleTypeInstall = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_poletype"));
            int idfixTypeInstall = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_fixtypei"));


            EdgeFormData poleIdInstall = new EdgeFormData();
            poleIdInstall.setId(idPoleIdInstall);

            EdgeFormData poleType = new EdgeFormData();
            poleType.setId(idPoleTypeInstall);
            EdgeFormData fixtureType = new EdgeFormData();
            fixtureType.setId(idfixTypeInstall);
            String strpoleType = "";
            String strFixtureType = "";
            String strPoleID = "";

            int pos  = formComponents.indexOf(poleType);
            if (pos != -1) {
                EdgeFormData tmp1 = formComponents.get(pos);
                strpoleType = tmp1.getValue();
            }
            pos = formComponents.indexOf(fixtureType);
            if (pos != -1) {
                EdgeFormData tmp1 = formComponents.get(pos);
                strFixtureType = tmp1.getValue();
            }
            pos = formComponents.indexOf(poleIdInstall);
            if(pos != -1){
                EdgeFormData tmp1 = formComponents.get(pos);
                strPoleID = tmp1.getValue();
            }
            JsonObject jsonObject = null;
            try {
                jsonObject = JsonDataParser.getJsonObject(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(strpoleType == null)
            {
                strpoleType = "";
            }
            if(strFixtureType == null)
            {
                strFixtureType = "";
            }
            if(strPoleID == null)
            {
                strPoleID = "";
            }
            if(strpoleType.equals("Not selected"))
            {
                strpoleType = "";
            }
            if(strFixtureType.equals("Not selected"))
            {
                strFixtureType = "";
            }

            jsonObject.addProperty("poleType", strpoleType);
            jsonObject.addProperty("fixtureType", strFixtureType);
            if(!strPoleID.equals(""))
            {
                jsonObject.addProperty("poleId", strPoleID);
            }
            jsonObject.addProperty("node", strPoleID);
            if(iMode == 1)
            {
                if(latLong != null) {
                    jsonObject.addProperty("latitude", latLong.getLat());
                    jsonObject.addProperty("longitude", latLong.getLng());
                }
                else
                {
                    jsonObject.addProperty("latitude", "");
                    jsonObject.addProperty("longitude", "");
                }
            }
            /*if(repdevui != null)
            {
                jsonObject.addProperty("dev_eui", repdevui);
            }*/
            String id = jsonObject.get("id").getAsString();
            String result2 = jsonObject.toString();
            UbicquiaLightsInterface.requestDynamicToken();
            String result3 = UbicquiaLightsInterface.setNodeData(lastUpdated,id, result2);
            if (result3 != null) {
                JsonObject jsonObject2 = null;
                try {
                    jsonObject2 = JsonDataParser.getJsonObject(result3);
                    String strID = checkDataNull(jsonObject2, "id");
                    String strNode = checkDataNull(jsonObject2, "node");
                    String strLat = checkDataNull(jsonObject2, "latitude");

                    String strLong = checkDataNull(jsonObject2, "longitude");
                    String strMC = checkDataNull(jsonObject2, "maintenanceCompany");

                    String strPoleID1 = checkDataNull(jsonObject2, "poleId");

                    String strFixID = checkDataNull(jsonObject2, "fixtureId");
                    String strPoleType = checkDataNull(jsonObject2, "poleType");
                    String strFixType = checkDataNull(jsonObject2, "fixtureType");




                    //TerragoUpdate.updateEdgeForm(formComponents, idID, strID);
                    //TerragoUpdate.updateEdgeForm(formComponents, idnode, strNode);
                    //TerragoUpdate.updateEdgeForm(formComponents, idlat, strLat);

                   // TerragoUpdate.updateEdgeForm(formComponents, idlng, strLong);
                    //TerragoUpdate.updateEdgeForm(formComponents, idmc, strMC);

                    //TerragoUpdate.updateEdgeForm(formComponents, idfixid, strFixID);
                    //TerragoUpdate.updateEdgeForm(formComponents, idPoleid, strPoleID1);

                    if(strPoleType.equals(""))
                    {
                        strPoleType = "Not selected";
                    }
                    if(strFixType.equals(""))
                    {
                        strFixType = "Not selected";
                    }
                    //TerragoUpdate.updateEdgeForm(formComponents, idPoleTypeInstall, strPoleType);
                    //TerragoUpdate.updateEdgeForm(formComponents, idfixtype, strFixType);

                    // Create Revision


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println(result3);
        }
    }
    private String get_devui(String qrstring)
    {
        String result = "";
        String []values = qrstring.split(",");
        if(values.length > 0)
        {
            result = values[0];
        }
        else
        {
            result = qrstring;
        }
        return result;
    }

    private void processUnFoundDevices()
    {
        List<DeviceData> lstDeviceData = TerragoDAO.getAllDeviceData();
        for(DeviceData deviceData:lstDeviceData){
            String parentNoteGUI = TerragoDAO.getCurrentNoteFromParentGUID(deviceData.getParentnoteguid());
            String processGUID = "";
            if(parentNoteGUI == null || parentNoteGUI.equals(""))
            {
                String noteGUID = TerragoDAO.isCurrentNote(deviceData.getParentnoteguid());
                if(noteGUID.equals(parentNoteGUI))
                {
                    //No revision on this note so no parent
                    processGUID = noteGUID;
                }
            }
            else
            {
                processGUID = parentNoteGUI;
            }
            if(!processGUID.equals(""))
            {
                LastUpdated lastUpdated = TerragoDAO.getNoteInfo(processGUID);
                processNote(processGUID,lastUpdated,PROCESS_NOT_FOUND_DEVICES);
            }
        }
    }
    public void startMonitoring2()
    {
        String nnguid = "";
        do {
            processUnFoundDevices();
            long lastMaxUpdatedTime = TerragoDAO.readLastUpdatedTime();
            if (lastMaxUpdatedTime == 0) {
                lastMaxUpdatedTime = System.currentTimeMillis() - 300000;
            }
            System.out.println("Looking for Changes ...");
            List<LastUpdated> lstUpdated = getUpdatedNotes(Long.toString(lastMaxUpdatedTime));
            for (LastUpdated lstCur : lstUpdated) {
                System.out.println("Processing Changes ...");
                lastMaxUpdatedTime = Math.max(lastMaxUpdatedTime, lstCur.getSynctime());
                nnguid = lstCur.getNoteguid();
                processNote(nnguid,lstCur,PROCESS_NORMAL_INSTALL);
            }
            TerragoDAO.writeLastUpdateTime(lastMaxUpdatedTime);
            try{
                Thread.sleep(10000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }while(true);
    }
    private void processNote(String nnguid,LastUpdated curNoteInfo,int mode)
    {
        String notesJson = RESTService.getNoteDetails(nnguid);
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

            if (formTemplate.equals(PropertiesReader.getProperties().getProperty("formtemplatetoinstall"))) {
                installFormComponents = formComponents;
            }
            if (formTemplate.equals(PropertiesReader.getProperties().getProperty("formtemplatetoaudit"))) {
                auditFormComponents = formComponents;
            }
            if (formTemplate.equals(PropertiesReader.getProperties().getProperty("formtemplatedc"))) {
                isDCPresent = true;
            }

        }
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
        if(installFormComponents != null && auditFormComponents != null) {
            doInstall(curNoteInfo, auditFormComponents, installFormComponents, mode);
        }
    }
    private void doDeviceControl(String noteguid)
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

            if (formTemplate.equals(PropertiesReader.getProperties().getProperty("formtemplatetoinstall"))) {
                installFormComponents = formComponents;
            }
            if (formTemplate.equals(PropertiesReader.getProperties().getProperty("formtemplatetoaudit"))) {
                auditFormComponents = formComponents;
            }
            if (formTemplate.equals(PropertiesReader.getProperties().getProperty("formtemplatedc"))) {
                isDCPresent = true;
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
            new SingleLightDeviceControl(lastUpdated, nodeid);
        }
    }
    private void doInstall(LastUpdated lastUpdated,
                           List<EdgeFormData> formComponents,
                           List<EdgeFormData> installformComponents,int mode){

        String idDev = PropertiesReader.getProperties().getProperty("ubicquia_deveui");
        String idFixtureType = PropertiesReader.getProperties().getProperty("ubicquia_fixtype");
        String idFixtureWattage = PropertiesReader.getProperties().getProperty("ubicquia_wattage");



        String idPoleNumber = PropertiesReader.getProperties().getProperty("ubicquia_polenumber");
        String idPoleHeight = PropertiesReader.getProperties().getProperty("ubicquia_poleheight");
        String idPoleType = PropertiesReader.getProperties().getProperty("ubicquia_poletype");

        int nidDev = Integer.parseInt(idDev);
        int nidFixtureType = Integer.parseInt(idFixtureType);
        int nidFixtureWattage = Integer.parseInt(idFixtureWattage);

        int nidPoleNumber = Integer.parseInt(idPoleNumber);
        int nidPoleHeight = Integer.parseInt(idPoleHeight);
        int nidPoleType = Integer.parseInt(idPoleType);

        String dev_eui = TerragoUtils.getEdgeFormValue(installformComponents,nidDev);
        String strFixtureType = TerragoUtils.getEdgeFormValue(installformComponents,nidFixtureType);
        String strFixtureWattage = TerragoUtils.getEdgeFormValue(installformComponents,nidFixtureWattage);

        String strPoleNumber = TerragoUtils.getEdgeFormValue(formComponents,nidPoleNumber);
        String strPoleHeight = TerragoUtils.getEdgeFormValue(formComponents,nidPoleHeight);
        String strPoleType = TerragoUtils.getEdgeFormValue(formComponents,nidPoleType);
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
                jsonObject.addProperty("fixtureType", strFixtureType);
                jsonObject.addProperty("fixture_wattage", strFixtureWattage);

                jsonObject.addProperty("fixtureId", strPoleNumber);
                jsonObject.addProperty("poleId", strPoleNumber);
                jsonObject.addProperty("node", strPoleNumber);
                //poleId
                jsonObject.addProperty("poleType", strPoleType);
                jsonObject.addProperty("poleHeight", strPoleHeight);
                String updateDataJSON =  jsonObject.toString();
                UbicquiaLightsInterface.setNodeData(lastUpdated,nodeid,updateDataJSON);
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
    }
    public void startMonitoring()
    {
        String ubicquia_actioninstall = PropertiesReader.getProperties().getProperty("ubicquia_actioninstall");
        String ubicquia_actionmaintain = PropertiesReader.getProperties().getProperty("ubicquia_actionmaintain");
        String nnguid = "";
        /*DoLocationUpdate doLocationUpdate = new DoLocationUpdate();
        Thread t = new Thread(doLocationUpdate);
        t.start();*/
        do {
            long lastMaxUpdatedTime = TerragoDAO.readLastUpdatedTime();
            if(lastMaxUpdatedTime == 0)
            {
                lastMaxUpdatedTime = System.currentTimeMillis() - 300000;
            }
            System.out.println("Looking for Changes ...");
            List<LastUpdated> lstUpdated = getUpdatedNotes(Long.toString(lastMaxUpdatedTime));
            for (LastUpdated lstCur : lstUpdated) {
                System.out.println("Processing Changes ...");
                lastMaxUpdatedTime = Math.max(lastMaxUpdatedTime, lstCur.getSynctime());
                nnguid = lstCur.getNoteguid();
                String notesJson =  RESTService.getNoteDetails(nnguid);
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
                for (int i = 0; i < size; i++) {
                    JsonObject serverEdgeForm = serverForms.get(i).getAsJsonObject();
                    String formDefJson = serverEdgeForm.get("formDef").getAsString();
                    String formTemplate = serverEdgeForm.get("formTemplateGuid").getAsString();
                    if (formTemplate.equals(PropertiesReader.getProperties().getProperty("formtemplatetoprocess"))) {
                        if(!restEdgeNote.getCreatedBy().equals(ignoreUser))
                        {
                            ispresent = true;
                        }
                        else
                        {
                            isInvalidUser = true;
                            System.out.println("Ignoring user " + ignoreUser);
                        }
                        List<EdgeFormData> formComponents = gson.fromJson(formDefJson, new TypeToken<List<EdgeFormData>>() {
                        }.getType());

                    }
                    else if(formTemplate.equals(PropertiesReader.getProperties().getProperty("dc_template"))) {
                        isDCPresent = true;
                    }

                }
                if(!ispresent)
                {
                    UbiTransactionLog ubiTransactionLog = new UbiTransactionLog();
                    ubiTransactionLog.setTitle(lstCur.getTitle());
                    ubiTransactionLog.setNotegui(lstCur.getNoteguid());
                    ubiTransactionLog.setSynctime(lstCur.getSynctime());
                    ubiTransactionLog.setEventtime(System.currentTimeMillis());
                    if(isInvalidUser)
                    {
                        ubiTransactionLog.setAction("Cannot process this user");
                    }
                    else
                    {
                        ubiTransactionLog.setAction("Not valid formtemplate");
                    }
                    TerragoDAO.addUbiTransactionLog(ubiTransactionLog);
                }
                if (ispresent) {
                    boolean mustUpdate = false;
                    for (int i = 0; i < size; i++) {
                        JsonObject serverEdgeForm = serverForms.get(i).getAsJsonObject();
                        String formDefJson = serverEdgeForm.get("formDef").getAsString();
                        String formTemplate = serverEdgeForm.get("formTemplateGuid").getAsString();
                        //formDefJson = formDefJson.replaceAll("\\\\", "");
                        //formDefJson = formDefJson.replace("u0026","\\u0026");
                        List<EdgeFormData> formComponents = gson.fromJson(formDefJson, new TypeToken<List<EdgeFormData>>() {
                        }.getType());
                        if (formTemplate.equals(PropertiesReader.getProperties().getProperty("formtemplatetoprocess"))) {
                            logger.info("Matching template found");
                            int iMode = 0;
                            LatLong latLong = null;
                            EdgeFormData cur = new EdgeFormData();
                            String idDev = PropertiesReader.getProperties().getProperty("ubicquia_deveui");
                            int nidDev = Integer.parseInt(idDev);
                            cur.setId(nidDev);
                            EdgeFormData cur1 = new EdgeFormData();
                            String mfc = PropertiesReader.getProperties().getProperty("ubicquia_mdc");
                            int idmfc = Integer.parseInt(mfc);
                            cur1.setId(idmfc);
                            EdgeFormData cur2 = new EdgeFormData();
                            String sfc = PropertiesReader.getProperties().getProperty("ubicquia_sdc");
                            int idsfc = Integer.parseInt(sfc);
                            cur2.setId(idsfc);//Single
                            EdgeFormData cur3 = new EdgeFormData();
                            String strIdDim = PropertiesReader.getProperties().getProperty("ubicquia_dimvalue");
                            int idDV = Integer.parseInt(strIdDim);
                            cur3.setId(idDV);
                            int pos = formComponents.indexOf(cur);
                            int pos1 = formComponents.indexOf(cur1);
                            int pos2 = formComponents.indexOf(cur2);
                            int pos3 = formComponents.indexOf(cur3);
                            String frmAction = getAction(formComponents);
                            boolean isDeviceControl = false;
                            if(frmAction.equals(ubicquia_actioninstall))
                            {
                                /***************************************************************
                                 *  I N S T A L L    S E C T I O N
                                 */
                                UbiTransactionLog ubiTransactionLog = new UbiTransactionLog();
                                ubiTransactionLog.setNotegui(nnguid);
                                ubiTransactionLog.setTitle(lstCur.getTitle());
                                ubiTransactionLog.setAction("Install");

                                if (pos != -1) {
                                    EdgeFormData f1 = formComponents.get(pos);
                                    String dev_eui = f1.getValue();
                                    if (dev_eui == null) {
                                        dev_eui = "";
                                    }
                                    if (!dev_eui.equals("")) {
                                        UbicquiaLightsInterface.requestDynamicToken();
                                        dev_eui = get_devui(dev_eui);
                                        System.out.println("Parsed : " + dev_eui);
                                        ubiTransactionLog.setDevui(dev_eui);
                                        JsonObject jobj1 = UbicquiaLightsInterface.getNodes(lstCur,dev_eui);
                                        String result = null;
                                        if (jobj1 == null) {
                                            ubiTransactionLog.setDeviceStatus("CREATE");
                                            String strGeom = restEdgeNote.getGeometry();
                                            //Create Node here
                                            iMode = 1;
                                            latLong = LatLongUtils.getLatLngFromGeoJson(strGeom);
                                            String strNodeStatus = "";
                                            String recPoleID = getPoleID(formComponents);
                                            if (latLong != null) {
                                                strNodeStatus = UbicquiaLightsInterface.CreateNewNode(lstCur,dev_eui, latLong.getLat(), latLong.getLng(), recPoleID);
                                            } else {
                                                strNodeStatus = UbicquiaLightsInterface.CreateNewNode(lstCur,dev_eui, "", "", recPoleID);
                                            }
                                            if (strNodeStatus.equals("success")) {
                                                ubiTransactionLog.setDeviceStatus("CREATE_OK");
                                                jobj1 = UbicquiaLightsInterface.getNodes(lstCur,dev_eui);
                                            } else {
                                                jobj1 = null;
                                            }

                                        }
                                        else
                                        {
                                            ubiTransactionLog.setDeviceStatus("EXISTS");
                                        }
                                        if (jobj1 != null) {
                                            result = jobj1.toString();
                                        }
                                        if (result != null) {
                                            //mustUpdate = true;
                                        }


                                        doInstallation(lstCur,result, formComponents,iMode,latLong,null);
                                        ubiTransactionLog.setSynctime(lstCur.getSynctime());
                                        ubiTransactionLog.setEventtime(System.currentTimeMillis());
                                        TerragoDAO.addUbiTransactionLog(ubiTransactionLog);


                                    }
                                    else
                                    {
                                        ubiTransactionLog.setSynctime(lstCur.getSynctime());
                                        ubiTransactionLog.setEventtime(System.currentTimeMillis());
                                        TerragoDAO.addUbiTransactionLog(ubiTransactionLog);

                                    }
                                }
                            }
                            if(frmAction.equals(ubicquia_actionmaintain))
                            {

                                /***************************************************************
                                 *  M A I N T A I N     S E C T I O N
                                 */
                                String dcStatus = getDevicControlStatus(formComponents);
                                if(dcStatus != null ) {
                                    if(dcStatus.equals("Show")) {
                                        /***************************************************************
                                         *  D E V I C E  C N T R L    S E C T I O N
                                         */
                                        UbiTransactionLog ubiTransactionLog = new UbiTransactionLog();
                                        ubiTransactionLog.setNotegui(nnguid);
                                        ubiTransactionLog.setTitle(lstCur.getTitle());
                                        ubiTransactionLog.setSynctime(lstCur.getSynctime());
                                        ubiTransactionLog.setEventtime(System.currentTimeMillis());
                                        //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@2
                                        if (pos1 != -1) {
                                            EdgeFormData f2 = formComponents.get(pos1);
                                            EdgeFormData f3 = formComponents.get(pos2);
                                            String groupControl = f2.getValue();
                                            String singleFixture = f3.getValue();
                                            if (groupControl == null) {
                                                groupControl = "";
                                            }
                                            if (singleFixture == null) {
                                                singleFixture = "";
                                            }
                                            if (groupControl != null || singleFixture != null) {
                                                if (pos != -1) {
                                                    EdgeFormData f1 = formComponents.get(pos);
                                                    String dev_eui = f1.getValue();
                                                    if (dev_eui == null) {
                                                        dev_eui = "";
                                                    }
                                                    if (!dev_eui.equals("")) {
                                                        String strRepDevUI = getReplaceDevui(formComponents);
                                                        if(strRepDevUI == null)
                                                        {
                                                            strRepDevUI = "";
                                                        }
                                                        if (!strRepDevUI.equals("")) {
                                                            dev_eui = strRepDevUI;

                                                        }
                                                        UbicquiaLightsInterface.requestDynamicToken();
                                                        dev_eui = get_devui(dev_eui);
                                                        JsonObject jobj1 = UbicquiaLightsInterface.getNodes(lstCur,dev_eui);
                                                        String result = jobj1.toString();

                                                        String strDCType = TerragoUtils.getPropertyValue("ubicquia_dctype");
                                                        int iddcType = TerragoUtils.getIdFromString(strDCType);
                                                        String dcType = TerragoUtils.getEdgeFormValue(formComponents,iddcType);
                                                        String strDCTypeSF = TerragoUtils.getPropertyValue("ubicquia_cnstsingle");

                                                            if (singleFixture.equals("Yes") && dcType.equals(strDCTypeSF)) {
                                                                ubiTransactionLog.setAction("DC-SF");
                                                                ubiTransactionLog.setDeviceStatus("EXISTS");
                                                                mustUpdate = false;
                                                                System.out.println("Single Device Control");
                                                                isDeviceControl = true;
                                                                //doSingleLightOn(dev_eui, formComponents);
                                                                JsonObject jobj = null;
                                                                try {
                                                                    logger.info("Parsing the ID");
                                                                    jobj = JsonDataParser.getJsonObject(result);
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                                if (jobj != null) {
                                                                    String strID = jobj.get("id").getAsString();
                                                                    System.out.println(strID);
                                                                    logger.info("Turing the light ON");

                                                                    if (pos3 != -1) {
                                                                        EdgeFormData f4 = formComponents.get(pos3);
                                                                        String dimmingValue = f4.getValue();
                                                                        UbicquiaLightsInterface.requestDynamicToken();
                                                                        UbicquiaLightsInterface.SetDimmingValue(lstCur,strID, dimmingValue);
                                                                        UbicquiaLightsInterface.SetDevice(lstCur,strID, true);
                                                                        try {
                                                                            Thread.sleep(1000);
                                                                        } catch (InterruptedException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }
                                                                    new DeviceMeteringData(lstCur,dev_eui, strID, nnguid,"update");

                                                                }
                                                            }
                                                            else
                                                            {

                                                                List<UbiData> lstUbiData = null;
                                                                String strDCTypeMF = TerragoUtils.getPropertyValue("ubicquia_cnstmultiple");
                                                                if(dcType.equals(strDCTypeMF))
                                                                {
                                                                    ubiTransactionLog.setAction("DC-MF");
                                                                    ubiTransactionLog.setDeviceStatus("EXISTS");
                                                                    isDeviceControl = true;
                                                                    String strID = TerragoUtils.getPropertyValue("ubicquia_dcoffset");
                                                                    int id = TerragoUtils.getIdFromString(strID);
                                                                    String dcoffsetValue = TerragoUtils.getEdgeFormValue(formComponents,id);
                                                                    dcoffsetValue = TerragoUtils.getOffsetValue(dcoffsetValue);

                                                                    if(dcoffsetValue.equals(""))
                                                                    {
                                                                        //Converting foot into meters
                                                                        double dbldcoffset = 0.3048 * 500;
                                                                        dcoffsetValue = Double.toString(dbldcoffset);
                                                                    }
                                                                    else
                                                                    {
                                                                        //Converting foot into meters
                                                                        double dbldcoffset = 0.3048 * Integer.parseInt(dcoffsetValue);
                                                                        dcoffsetValue = Double.toString(dbldcoffset);
                                                                    }
                                                                    String strGeom = restEdgeNote.getGeometry();
                                                                    LatLong latLong1 = null;
                                                                    latLong1 = LatLongUtils.getLatLngFromGeoJson(strGeom);
                                                                    if(latLong1 != null)
                                                                    {
                                                                        lstUbiData = TerragoUtils.getAllNearByLights(lstCur,latLong1.getLat(),latLong1.getLng(),dcoffsetValue);
                                                                        JsonObject jobj = null;
                                                                        try {
                                                                            jobj = JsonDataParser.getJsonObject(result);
                                                                        }
                                                                        catch (Exception e)
                                                                        {
                                                                            e.printStackTrace();
                                                                        }
                                                                        String groupID = "";
                                                                        if(jobj != null)
                                                                        {
                                                                            groupID = checkDataNull(jobj, "groupId");
                                                                            if(groupID == null)
                                                                            {
                                                                                groupID = "";
                                                                            }
                                                                        }
                                                                        if (groupControl.equals("Yes")) {
                                                                            ubiTransactionLog.setAction("DC-MF-GROUP");
                                                                            if(!groupID.equals("")) {
                                                                                lstUbiData = TerragoUtils.filterNearbyLights(lstUbiData, groupID);
                                                                            }

                                                                            isDeviceControl = true;
                                                                            System.out.println("Group Device Control");
                                                                            doGroupLightsOn2(lstCur,formComponents,lstUbiData);
                                                                            mustUpdate = true;
                                                                            //doGroupLightsOn(result, formComponents);
                                                                        }
                                                                        else
                                                                        {
                                                                            isDeviceControl = true;
                                                                            doGroupLightsOn2(lstCur,formComponents,lstUbiData);
                                                                            mustUpdate = true;
                                                                        }

                                                                    }


                                                                    /*if (groupControl.equals("Yes")) {
                                                                        isDeviceControl = true;
                                                                        System.out.println("Group Device Control");

                                                                        doGroupLightsOn(result, formComponents);
                                                                    }*/
                                                                }

                                                            }
                                                    }

                                                }
                                            }
                                        }
                                        TerragoDAO.addUbiTransactionLog(ubiTransactionLog);
                                    }
                                    //@@@@@@@@@@@@@@@@@@@@@@@@@@@
                                }

                                    if (!isDeviceControl) {
                                        //Perform Replace
                                        /***************************************************************
                                         *  R E P L A C E   S E C T I O N
                                         */
                                        UbiTransactionLog ubiTransactionLog = new UbiTransactionLog();
                                        ubiTransactionLog.setNotegui(nnguid);
                                        ubiTransactionLog.setTitle(lstCur.getTitle());
                                        ubiTransactionLog.setAction("Replace");
                                        String strRepDevUI = getReplaceDevui(formComponents);
                                        if(strRepDevUI == null)
                                        {
                                            strRepDevUI = "";
                                        }
                                        if (!strRepDevUI.equals("")) {
                                            UbicquiaLightsInterface.requestDynamicToken();
                                            strRepDevUI = get_devui(strRepDevUI);
                                            ubiTransactionLog.setDevui(strRepDevUI);
                                            JsonObject jobj1 = UbicquiaLightsInterface.getNodes(lstCur,strRepDevUI);
                                            String result = null;
                                            if (jobj1 == null) {
                                                ubiTransactionLog.setDeviceStatus("CREATE");
                                                String strGeom = restEdgeNote.getGeometry();
                                                //Create Node here
                                                iMode = 1;
                                                latLong = LatLongUtils.getLatLngFromGeoJson(strGeom);
                                                String strNodeStatus = "";
                                                String recPoleID = getPoleID(formComponents);
                                                if (latLong != null) {
                                                    strNodeStatus = UbicquiaLightsInterface.CreateNewNode(lstCur,strRepDevUI, latLong.getLat(), latLong.getLng(), recPoleID);
                                                } else {
                                                    strNodeStatus = UbicquiaLightsInterface.CreateNewNode(lstCur,strRepDevUI, "", "", recPoleID);
                                                }
                                                if (strNodeStatus.equals("success")) {
                                                    jobj1 = UbicquiaLightsInterface.getNodes(lstCur,strRepDevUI);
                                                    ubiTransactionLog.setDeviceStatus("CREATE_OK");
                                                } else {
                                                    jobj1 = null;
                                                }

                                            }
                                            else
                                            {
                                                ubiTransactionLog.setDeviceStatus("EXISTS");
                                            }
                                            if (jobj1 != null) {
                                                result = jobj1.toString();
                                            }
                                            if (jobj1 != null) {
                                                result = jobj1.toString();
                                            }
                                            if (result != null) {
                                                //mustUpdate = true;
                                            }

                                            doInstallation(lstCur,result, formComponents, iMode, latLong, strRepDevUI);
                                            ubiTransactionLog.setSynctime(lstCur.getSynctime());
                                            ubiTransactionLog.setEventtime(System.currentTimeMillis());
                                            TerragoDAO.addUbiTransactionLog(ubiTransactionLog);
                                        }
                                        //End of replace
                                    }


                            }

                        }
                        serverEdgeForm.add("formDef", gson.toJsonTree(formComponents));
                        serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
                    }
                    edgenoteJson.add("formData", serverForms);
                    edgenoteJson.addProperty("createdBy", "admin");
                    long ntime = System.currentTimeMillis();
                    lastMaxUpdatedTime = Math.max(lastMaxUpdatedTime, ntime);
                    edgenoteJson.addProperty("createdDateTime", ntime);
                    if(mustUpdate) {
                        ResponseEntity<String> ge = RESTService.updateNoteDetails(edgenoteJson.toString(), lstCur.getNoteguid(), restEdgeNote.getEdgeNotebook().getNotebookGuid());
                        String ne1 = ge.getBody();
                    }
                    /*DoLocationUpdate doLocationUpdate = new DoLocationUpdate();
                    doLocationUpdate.setNoteguid(lstCur.getNoteguid());
                    doLocationUpdate.processLocationChange();*/
                }

                if(isDCPresent)
                {
                    System.out.println("Device Control from Device");
                    /**************************************************
                     *      D E V I C E   C O N T R O L   F R O M  B U T T ON
                     */
                    UbiTransactionLog ubiTransactionLog = new UbiTransactionLog();
                    ubiTransactionLog.setTitle(lstCur.getTitle());
                    ubiTransactionLog.setNotegui(lstCur.getNoteguid());
                    ubiTransactionLog.setSynctime(lstCur.getSynctime());
                    for (int i = 0; i < size; i++) {
                        JsonObject serverEdgeForm = serverForms.get(i).getAsJsonObject();
                        String formDefJson = serverEdgeForm.get("formDef").getAsString();
                        String formTemplate = serverEdgeForm.get("formTemplateGuid").getAsString();
                        //formDefJson = formDefJson.replaceAll("\\\\", "");
                        //formDefJson = formDefJson.replace("u0026","\\u0026");
                        List<EdgeFormData> formComponents = gson.fromJson(formDefJson, new TypeToken<List<EdgeFormData>>() {
                        }.getType());
                        if (formTemplate.equals(PropertiesReader.getProperties().getProperty("dc_template"))) {
                            EdgeFormData cur = new EdgeFormData();
                            cur.setId(41);
                            int pos = formComponents.indexOf(cur);
                            if (pos != -1) {
                                EdgeFormData f1 = formComponents.get(pos);
                                String fixtureId = f1.getValue();

                                NoteInfo noteInfo = TerragoDAO.getIMEI(fixtureId);
                                System.out.println(fixtureId);
                                //String nguid = TerragoDAO.getNoteGUID(fixtureId);

                                ///////////////////////////////////////////////////////////
                                if(!noteInfo.getIMEI().equals(""))
                                {
                                    fixtureId = noteInfo.getIMEI();
                                    fixtureId = get_devui(fixtureId);

                                    logger.info("Requesting the Fixture information");
                                    String queryResults = null;

                                    //queryResults = UbicquiaLightsInterface.getQueryData(fixtureId);
                                    UbicquiaLightsInterface.requestDynamicToken();
                                    ubiTransactionLog.setAction("Button DC");
                                    ubiTransactionLog.setDevui(fixtureId);
                                    JsonObject jsonObject = UbicquiaLightsInterface.getNodes(lstCur,fixtureId);
                                    if (jsonObject != null) {
                                        queryResults = jsonObject.toString();
                                    }

                                    if (queryResults != null) {
                                        JsonObject jobj = null;
                                        try {
                                            logger.info("Parsing the ID");
                                            jobj = JsonDataParser.getJsonObject(queryResults);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        if (jobj != null) {
                                            String strID = jobj.get("id").getAsString();
                                            System.out.println(strID);
                                            logger.info("Turing the light ON");
                                            UbicquiaLightsInterface.SetDevice(lstCur,strID, true);
                                            new DeviceMeteringData(lstCur,fixtureId, strID,noteInfo.getNoteguid(),null);

                                        }
                                    }
                                }
                                else
                                {
                                    ubiTransactionLog.setAction("Invalid devui");
                                }
                                ///////////////////////////////////////////////////////////////////
                            }
                        }
                    }
                    ubiTransactionLog.setEventtime(System.currentTimeMillis());
                    TerragoDAO.addUbiTransactionLog(ubiTransactionLog);
                }
            }
            /*if (lstUpdated.size() > 0) {
                long lntime = TerragoDAO.readLastUpdatedTime();
                if(lntime >= lastMaxUpdatedTime)
                {
                    lastMaxUpdatedTime = lntime;
                }
                TerragoDAO.writeLastUpdateTime(lastMaxUpdatedTime);
            }*/
            try {
                Thread.sleep(1000);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }while(true);
    }
    

}
