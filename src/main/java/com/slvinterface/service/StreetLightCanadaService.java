package com.slvinterface.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.stmt.query.In;
import com.slvinterface.dao.tables.SlvDevice;
import com.slvinterface.dao.tables.SlvSyncDetails;
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
        //slvInterfaceDAO = new SLVInterfaceDAO();

    }

    private void doProcess(String noteGuid,String accessToken,boolean resync) throws InvalidMacAddressException
    {
        String controllerStrId = PropertiesReader.getProperties().getProperty("streetlight.controller.str.id");

        //String geoZoneId = PropertiesReader.getProperties().getProperty("streetlight.geozoneid");
        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        String actionID = PropertiesReader.getProperties().getProperty("streetlight.form.actionid");
        String geoZoneID = PropertiesReader.getProperties().getProperty("streetlight.form.geozoneid");
        String strNewInstall = PropertiesReader.getProperties().getProperty("streetlight.form.newinstall");
        String newInstallMacAddress = PropertiesReader.getProperties().getProperty("streetlight.form.newinstall.mac");
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
                        String strActionString = FormValueUtil.getValue(formComponents,actionid);
                        if(strActionString.equals(strNewInstall))
                        {
                            //New Install MAC Address
                            String macAddress = FormValueUtil.getValue(formComponents,newMacid);
                            if(DataTools.checkForValidMacAddress(macAddress))
                            {
                                try{
                                    SlvDevice slvDevice = connectionDAO.getSlvDevices(edgeNote.getTitle());
                                    SlvSyncDetails slvSyncDetails = new SlvSyncDetails();
                                    slvTools.checkMacAddressExists(macAddress,edgeNote.getTitle());
                                    boolean isDeviceExist = slvTools.deviceAlreadyExists (edgeNote.getTitle());
                                    if (!isDeviceExist) {
                                        String strGeoZone = FormValueUtil.getValue(formComponents,formGeoZoneId);
                                        if(!strGeoZone.equals(""))
                                        {
                                            int newDeviceGeoZoneID = slvTools.checkAndCreateGeoZone(strGeoZone,edgeNote);
                                            if(newDeviceGeoZoneID != -1)
                                            {
                                                slvTools.createNewDevice(edgeNote, slvSyncDetails, Integer.toString(newDeviceGeoZoneID));
                                            }
                                            else
                                            {
                                                continue;
                                            }
                                        }
                                        else
                                        {
                                            continue;
                                        }
                                    }
                                    List<Object> paramsList = new ArrayList<>();


                                    paramsList.add("idOnController="+DataTools.URLEncoder(edgeNote.getTitle()));
                                    paramsList.add("controllerStrId="+controllerStrId);
                                    slvTools.addStreetLightData("installStatus", "Installed", paramsList);
                                    slvTools.addStreetLightData("install.date", Utils.dateFormat(edgeNote.getCreatedDateTime()), paramsList);
                                    slvTools.addStreetLightData("location.utillocationid", edgeNote.getTitle() + ".Lamp", paramsList);
                                    slvTools.setDeviceValues(paramsList,slvSyncDetails);
                                    slvTools.replaceOLC(controllerStrId,edgeNote.getTitle(),macAddress);

                                }
                                catch (QRCodeAlreadyUsedException e)
                                {
                                    e.printStackTrace();
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                                catch (DeviceCreationFailedException e)
                                {
                                    e.printStackTrace();
                                }
                                catch (ReplaceOLCFailedException e)
                                {
                                    e.printStackTrace();
                                }
                                catch (DeviceUpdationFailedException e)
                                {
                                    e.printStackTrace();
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                            else
                            {
                                throw new InvalidMacAddressException("Invaild Macaddrees");
                            }
                        }
                        else if(strActionString.equals(""))
                        {

                        }
                        else if(strActionString.equals(""))
                        {

                        }
                        //End Template match
                    }
                    //End Form Loop
                }
            }
        }
    }
    public void run(){

        String edgeSlvUrl =  PropertiesReader.getProperties().getProperty("streetlight.edge.slvserver.url");
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
            //Normal SLV Process
            edgeSlvUrl = edgeSlvUrl+"/notesGuid?lastSyncTime=";

            long lastSynctime = 1574254528978L; // TODO: streetlightDao.getLastSyncTime();
            if(lastSynctime > 0){
                edgeSlvUrl = edgeSlvUrl + lastSynctime;

            }else{
                lastSynctime = System.currentTimeMillis() - (10 * 60000);
                edgeSlvUrl = edgeSlvUrl + lastSynctime;
            }

            String terragoAccessToken = edgeRestService.getEdgeToken();
            ResponseEntity<String> responseEntity = edgeRestService.getRequest(edgeSlvUrl,true,terragoAccessToken);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String notesGuids = responseEntity.getBody();
                System.out.println(notesGuids);
                try {
                    JsonArray noteGuidsJsonArray = (JsonArray) jsonParser.parse(notesGuids);
                    if (noteGuidsJsonArray != null && !noteGuidsJsonArray.isJsonNull()) {
                        for (JsonElement noteGuidJson : noteGuidsJsonArray) {
                            String noteGuid = noteGuidJson.getAsString();
                            //TODO: Check if already exists from DAO
                            //if(!noteGuids.contains(noteGuid)){
                            doProcess(noteGuid, terragoAccessToken, false);
                            //}

                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }




    }
}
