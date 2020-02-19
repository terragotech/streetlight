package com.terrago.streetlights.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import com.terrago.streetlights.App;
import com.terrago.streetlights.dao.DataBaseConnector;
import com.terrago.streetlights.dao.TerragoDAO;
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
            resultSet = statement.executeQuery("select title,noteguid,createddatetime from edgenote where iscurrent=true and isdeleted=false and createddatetime > " + lastProcessedTime );
            while(resultSet.next())
            {
                String noteguid = resultSet.getString("noteguid");
                LastUpdated lastUpdated = new LastUpdated();
                lastUpdated.setNoteguid(noteguid);
                lastUpdated.setCreateddatetime(resultSet.getLong("createddatetime"));
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
    private void doSingleLightOn(String fixtureID,List<EdgeFormData> formComponents)
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
                        new SingleLightDeviceControl(strID);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    private void doGroupLightsOn(String result,List<EdgeFormData> formComponents)
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
                    new GroupDeviceControl(lstID);
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    private void doInstallation(String result,List<EdgeFormData> formComponents, int iMode,LatLong latLong,String repdevui)
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
            String result3 = UbicquiaLightsInterface.setNodeData(id, result2);
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
    public void startMonitoring()
    {
        String ubicquia_actioninstall = PropertiesReader.getProperties().getProperty("ubicquia_actioninstall");
        String ubicquia_actionmaintain = PropertiesReader.getProperties().getProperty("ubicquia_actionmaintain");
        String nnguid = "";
        do {
            long lastMaxUpdatedTime = TerragoDAO.readLastUpdatedTime();
            System.out.println("Looking for Changes ...");
            List<LastUpdated> lstUpdated = getUpdatedNotes(Long.toString(lastMaxUpdatedTime));
            for (LastUpdated lstCur : lstUpdated) {
                System.out.println("Processing Changes ...");
                lastMaxUpdatedTime = Math.max(lastMaxUpdatedTime, lstCur.getCreateddatetime());
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

                boolean ispresent = false;
                boolean isDCPresent = false;
                for (int i = 0; i < size; i++) {
                    JsonObject serverEdgeForm = serverForms.get(i).getAsJsonObject();
                    String formDefJson = serverEdgeForm.get("formDef").getAsString();
                    String formTemplate = serverEdgeForm.get("formTemplateGuid").getAsString();
                    if (formTemplate.equals(PropertiesReader.getProperties().getProperty("formtemplatetoprocess"))) {
                        ispresent = true;
                        List<EdgeFormData> formComponents = gson.fromJson(formDefJson, new TypeToken<List<EdgeFormData>>() {
                        }.getType());

                    }
                    else if(formTemplate.equals(PropertiesReader.getProperties().getProperty("dc_template"))) {
                        isDCPresent = true;
                    }
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
                                if (pos != -1) {
                                    EdgeFormData f1 = formComponents.get(pos);
                                    String dev_eui = f1.getValue();
                                    if (dev_eui == null) {
                                        dev_eui = "";
                                    }
                                    if (!dev_eui.equals("")) {
                                        UbicquiaLightsInterface.requestDynamicToken();
                                        JsonObject jobj1 = UbicquiaLightsInterface.getNodes(dev_eui);
                                        String result = null;
                                        if (jobj1 == null) {
                                            String strGeom = restEdgeNote.getGeometry();
                                            //Create Node here
                                            iMode = 1;
                                            latLong = LatLongUtils.getLatLngFromGeoJson(strGeom);
                                            String strNodeStatus = "";
                                            String recPoleID = getPoleID(formComponents);
                                            if (latLong != null) {
                                                strNodeStatus = UbicquiaLightsInterface.CreateNewNode(dev_eui, latLong.getLat(), latLong.getLng(), recPoleID);
                                            } else {
                                                strNodeStatus = UbicquiaLightsInterface.CreateNewNode(dev_eui, "", "", recPoleID);
                                            }
                                            if (strNodeStatus.equals("success")) {
                                                jobj1 = UbicquiaLightsInterface.getNodes(dev_eui);
                                            } else {
                                                jobj1 = null;
                                            }

                                        }
                                        if (jobj1 != null) {
                                            result = jobj1.toString();
                                        }
                                        if (result != null) {
                                            mustUpdate = true;
                                        }
                                        doInstallation(result, formComponents,iMode,latLong,null);
                                    }
                                }
                            }
                            if(frmAction.equals(ubicquia_actionmaintain))
                            {
                                String dcStatus = getDevicControlStatus(formComponents);
                                if(dcStatus != null ) {
                                    if(dcStatus.equals("Show")) {
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
                                                        if (!strRepDevUI.equals("")) {
                                                            dev_eui = strRepDevUI;
                                                        }
                                                        UbicquiaLightsInterface.requestDynamicToken();
                                                        JsonObject jobj1 = UbicquiaLightsInterface.getNodes(dev_eui);
                                                        String result = jobj1.toString();
                                                         if (singleFixture != null && !singleFixture.equals(""))
                                                         {
                                                            if (singleFixture.equals("Yes")) {
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
                                                                        UbicquiaLightsInterface.SetDimmingValue(strID, dimmingValue);
                                                                        UbicquiaLightsInterface.SetDevice(strID, true);
                                                                        try {
                                                                            Thread.sleep(1000);
                                                                        } catch (InterruptedException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }
                                                                    new DeviceMeteringData(dev_eui, strID, nnguid);

                                                                }
                                                            }
                                                        }
                                                        else if (groupControl.equals("Yes")) {
                                                            isDeviceControl = true;
                                                            System.out.println("Group Device Control");

                                                            doGroupLightsOn(result, formComponents);
                                                        }
                                                    }

                                                }


                                            }

                                        }
                                    }
                                    //@@@@@@@@@@@@@@@@@@@@@@@@@@@
                                }

                                    if (!isDeviceControl) {
                                        //Perform Replace
                                        String strRepDevUI = getReplaceDevui(formComponents);
                                        if (!strRepDevUI.equals("")) {
                                            UbicquiaLightsInterface.requestDynamicToken();
                                            JsonObject jobj1 = UbicquiaLightsInterface.getNodes(strRepDevUI);
                                            String result = null;
                                            if (jobj1 == null) {
                                                String strGeom = restEdgeNote.getGeometry();
                                                //Create Node here
                                                iMode = 1;
                                                latLong = LatLongUtils.getLatLngFromGeoJson(strGeom);
                                                String strNodeStatus = "";
                                                String recPoleID = getPoleID(formComponents);
                                                if (latLong != null) {
                                                    strNodeStatus = UbicquiaLightsInterface.CreateNewNode(strRepDevUI, latLong.getLat(), latLong.getLng(), recPoleID);
                                                } else {
                                                    strNodeStatus = UbicquiaLightsInterface.CreateNewNode(strRepDevUI, "", "", recPoleID);
                                                }
                                                if (strNodeStatus.equals("success")) {
                                                    jobj1 = UbicquiaLightsInterface.getNodes(strRepDevUI);
                                                } else {
                                                    jobj1 = null;
                                                }

                                            }
                                            if (jobj1 != null) {
                                                result = jobj1.toString();
                                            }
                                            if (jobj1 != null) {
                                                result = jobj1.toString();
                                            }
                                            if (result != null) {
                                                mustUpdate = true;
                                            }
                                            doInstallation(result, formComponents, iMode, latLong, strRepDevUI);
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
                        RESTService.updateNoteDetails(edgenoteJson.toString(), lstCur.getNoteguid(), restEdgeNote.getEdgeNotebook().getNotebookGuid());
                    }
                }

                if(isDCPresent)
                {
                    System.out.println("Device Control from Device");
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
                                    UbicquiaLightsInterface.requestDynamicToken();
                                    logger.info("Requesting the Fixture information");
                                    String queryResults = null;

                                    //queryResults = UbicquiaLightsInterface.getQueryData(fixtureId);
                                    JsonObject jsonObject = UbicquiaLightsInterface.getNodes(fixtureId);
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
                                            UbicquiaLightsInterface.SetDevice(strID, true);
                                            new DeviceMeteringData(fixtureId, strID,noteInfo.getNoteguid());

                                        }
                                    }
                                }
                                ///////////////////////////////////////////////////////////////////
                            }
                        }
                    }
                }
            }
            if (lstUpdated.size() > 0) {
                long lntime = TerragoDAO.readLastUpdatedTime();
                if(lntime >= lastMaxUpdatedTime)
                {
                    lastMaxUpdatedTime = lntime;
                }
                TerragoDAO.writeLastUpdateTime(lastMaxUpdatedTime);
            }
            try {
                Thread.sleep(5000);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }while(true);
    }


}
