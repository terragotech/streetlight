package com.terrago.streetlights.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.terrago.streetlights.dao.TerragoDAO;
import com.terrago.streetlights.dao.model.DeviceData;
import com.terrago.streetlights.utils.LastUpdated;
import com.terrago.streetlights.utils.PropertiesReader;
import com.terrago.streetlights.utils.TerragoUtils;
import com.terragoedge.edgeserver.EdgeFormData;
import org.apache.log4j.Logger;

import java.util.List;

public class SchuylkillPilot extends LUSPilot {
    private Logger logger = Logger.getLogger(SchuylkillPilot.class);
    public int doInstall(LastUpdated lastUpdated,
                         List<EdgeFormData> formComponents,
                         List<EdgeFormData> installformComponents, int mode){
        int result = 0;
        String idPoleId = PropertiesReader.getProperties().getProperty("ubicquia_poleid");
        String idPoleHeight = PropertiesReader.getProperties().getProperty("ubicquia_poleheight");
        String iduPoleHeight = PropertiesReader.getProperties().getProperty("ubicquia_upoleheight");
        String idPoleType = PropertiesReader.getProperties().getProperty("ubicquia_poletype");
        //String idPoleColor = PropertiesReader.getProperties().getProperty("ubicquia_polecolor");

        int nidPoleId = Integer.parseInt(idPoleId);
        int nidPoleHeight = Integer.parseInt(idPoleHeight);
        int niduPoleHeight = Integer.parseInt(iduPoleHeight);
        int nidPoleType = Integer.parseInt(idPoleType);
        //int nidPoleColor = Integer.parseInt(idPoleColor);

        //Mapped to Custom2
        String idFeed = PropertiesReader.getProperties().getProperty("ubicquia_feed");
        int nidFeed = Integer.parseInt(idFeed);

        //String idFixtureId = PropertiesReader.getProperties().getProperty("ubicquia_fixid");
        String idFixtureType = PropertiesReader.getProperties().getProperty("ubicquia_fixtype");
        String idFixtureuType = PropertiesReader.getProperties().getProperty("ubicquia_ufixtype");
        String idFixtureWattage = PropertiesReader.getProperties().getProperty("ubicquia_wattage");
        //Mapped to Custom 1
        String idAuditStreet = PropertiesReader.getProperties().getProperty("ubicquia_auditstreet");
        String idAuditId = PropertiesReader.getProperties().getProperty("ubicquia_auditid");
        //Mapped to Custom 3
        //Install Time Stamp

        //int nidFixtureId = Integer.parseInt(idFixtureId);
        int nidFixtureType = Integer.parseInt(idFixtureType);
        int niduFixtureType = Integer.parseInt(idFixtureuType);
        int nidFixtureWattage = Integer.parseInt(idFixtureWattage);
        int nidAuditStreet = Integer.parseInt(idAuditStreet);
        int nidAuditId = Integer.parseInt(idAuditId);


        String idDev = PropertiesReader.getProperties().getProperty("ubicquia_deveui");
        int nidDev = Integer.parseInt(idDev);

        //Now getting Form values
        String strPoleId = TerragoUtils.getEdgeFormValue(installformComponents,nidPoleId);
        String strPoleHeight = TerragoUtils.getEdgeFormValue(installformComponents,nidPoleHeight);
        String struPoleHeight = TerragoUtils.getEdgeFormValue(installformComponents,niduPoleHeight);
        String strPoleType = TerragoUtils.getEdgeFormValue(installformComponents,nidPoleType);
        //String strPoleColor =  TerragoUtils.getEdgeFormValue(installformComponents,nidPoleColor);
        //String strFeed = TerragoUtils.getEdgeFormValue(installformComponents,nidFeed);

        //String strFixtureId = TerragoUtils.getEdgeFormValue(installformComponents,nidFixtureId);
        String strFixtureType = TerragoUtils.getEdgeFormValue(installformComponents,nidFixtureType);
        String struFixtureType = TerragoUtils.getEdgeFormValue(installformComponents,niduFixtureType);
        String strFixtureWattage = TerragoUtils.getEdgeFormValue(installformComponents,nidFixtureWattage);
        String strAuditStreet = TerragoUtils.getEdgeFormValue(installformComponents,nidAuditStreet);
        String strAuditId = TerragoUtils.getEdgeFormValue(installformComponents,nidAuditId);

        String dev_eui = TerragoUtils.getEdgeFormValue(installformComponents,nidDev);

        if(strPoleHeight.equals("Unknown"))
        {
            strPoleHeight = struPoleHeight;
        }
        if(strFixtureType.equals("Unk Unknown"))
        {
            strFixtureType = struFixtureType;
        }
        dev_eui = TerragoUtils.parseDevUIAll(dev_eui);
        if(!dev_eui.equals(""))
        {
            //UbicquiaLightsInterface.requestDynamicToken();
            logger.info(dev_eui);
            JsonObject jsonObject = UbicquiaLightsInterface.getNodes(lastUpdated,dev_eui);
            if(jsonObject != null)
            {
                //Do Update
                //UbicquiaLightsInterface.requestDynamicToken();
                //Change Node Name
                String nodeid = jsonObject.get("id").getAsString();
                String strInstallDate = getTodayInstallDate();
                jsonObject.addProperty("poleId", strPoleId);//OK
                jsonObject.addProperty("poleHeight", strPoleHeight);//OK
                jsonObject.addProperty("poleType", strPoleType);//OK
                //jsonObject.addProperty("poleColor", strPoleColor);//OK
                //jsonObject.addProperty("Custom2", strPoleColor);

                jsonObject.addProperty("fixtureId", strPoleId);//OK
                jsonObject.addProperty("node", strPoleId);//OK
                jsonObject.addProperty("fixtureType", strFixtureType);//OK
                if(isNumeric(strFixtureWattage))
                {
                    jsonObject.addProperty("fixture_wattage", strFixtureWattage);//OK
                }

                //jsonObject.addProperty("Custom1", strLampType);
                //jsonObject.addProperty("Custom3",strInstallDate);



                //poleId


                String updateDataJSON =  jsonObject.toString();
                logger.info(updateDataJSON);
                String nodeUpdateResponse = UbicquiaLightsInterface.setNodeData(lastUpdated,nodeid,updateDataJSON);

                JsonObject jsonObject1 = new JsonObject();
                JsonArray customDataArray = new JsonArray();

                JsonObject cusDataLampType = new JsonObject();
                cusDataLampType.addProperty("display_name","Audit Street");
                cusDataLampType.addProperty("key","custom1");
                cusDataLampType.addProperty("value",strAuditStreet);

                JsonObject cusDataFeed = new JsonObject();
                cusDataFeed.addProperty("display_name","Audit ID");
                cusDataFeed.addProperty("key","custom2");
                cusDataFeed.addProperty("value",strAuditId);

                JsonObject cusDataInstallDate = new JsonObject();
                cusDataInstallDate.addProperty("display_name","Ubicell Install Date");
                cusDataInstallDate.addProperty("key","custom3");
                cusDataInstallDate.addProperty("value",strInstallDate);

                /*
                String strInstaller = lastUpdated.getCreatedBy();
                JsonObject cusDataInstaller = new JsonObject();
                cusDataInstaller.addProperty("display_name","Installer");
                cusDataInstaller.addProperty("key","custom4");
                cusDataInstaller.addProperty("value",strInstaller);*/

                customDataArray.add(cusDataLampType);
                customDataArray.add(cusDataFeed);
                customDataArray.add(cusDataInstallDate);
                //customDataArray.add(cusDataInstaller);

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
