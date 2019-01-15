package com.macaddress.slvtoedge.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.macaddress.slvtoedge.model.EdgeMacAddress;
import com.terragoedge.slvinterface.dao.ConnectionDAO;
import com.terragoedge.slvinterface.dao.SLVInterfaceDAO;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetails;
import com.terragoedge.slvinterface.exception.DeviceUpdationFailedException;
import com.terragoedge.slvinterface.exception.InValidBarCodeException;
import com.terragoedge.slvinterface.exception.NoValueException;
import com.terragoedge.slvinterface.exception.NotesNotFoundException;
import com.terragoedge.slvinterface.json.slvInterface.ConfigurationJson;
import com.terragoedge.slvinterface.model.EdgeFormData;
import com.terragoedge.slvinterface.model.EdgeNote;
import com.terragoedge.slvinterface.service.AbstractSlvService;
import com.terragoedge.slvinterface.service.SlvInterfaceService;
import com.terragoedge.slvinterface.service.SlvRestService;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;

public class MacAddressService extends AbstractSlvService {
    Properties properties = null;
    Gson gson = null;
    JsonParser jsonParser = null;
    final Logger logger = Logger.getLogger(SlvInterfaceService.class);
    private SlvRestService slvRestService = null;
    private ConnectionDAO connectionDAO = null;
    private SLVInterfaceDAO slvInterfaceDAO = null;

    public MacAddressService() {
        this.properties = PropertiesReader.getProperties();
        this.gson = new Gson();
        this.jsonParser = new JsonParser();
        slvRestService = new SlvRestService();
        connectionDAO = ConnectionDAO.INSTANCE;
        slvInterfaceDAO = new SLVInterfaceDAO();
    }

    public void run() {
        String formTemplateGuid = properties.getProperty("streetlight.edge.formtemplateguid");
        String mainUrl = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        String completeGuid = PropertiesReader.getProperties().getProperty("edge.completelayer.guid");
        String macAddressId = properties.getProperty("edge.formtemplate.macaddressid");
        String accessToken = getEdgeToken();
        logger.info("AccessToken is :" + accessToken);
        if (accessToken == null) {
            logger.error("Edge Invalid UserName and Password.");
            return;
        }

        List<EdgeMacAddress> edgeMacAddressList = new ArrayList<>();
        /*System.out.println("EdgeNoteMacAddress title"+edgeMacAddressList.size());
        EdgeMacAddress edgeMacAddress1 = new EdgeMacAddress();
        edgeMacAddress1.setMacAddress("a");
        edgeMacAddress1.setTitle("1");
        edgeMacAddressList.add(edgeMacAddress1);
        EdgeMacAddress edgeMacAddress2 = new EdgeMacAddress();
        edgeMacAddress2.setMacAddress("b");
        edgeMacAddress2.setTitle("2");
        edgeMacAddressList.add(edgeMacAddress2);*/

        //TODO getMAcADdress From Edge;
        //   List<EdgeMacAddress> slvMacAddressList = getSLVEmptyMacAddress();
        List<EdgeMacAddress> slvMacAddressList = getEdgeNoteFromServer();

        for (EdgeMacAddress edgeMacAddress : slvMacAddressList) {
            if (!edgeMacAddressList.contains(edgeMacAddress)) {
                System.out.println(edgeMacAddress);
                processEdgeNote(edgeMacAddress, formTemplateGuid, mainUrl, completeGuid, macAddressId);
            }
        }
    }

    public void processEdgeNote(EdgeMacAddress edgeMacAddress, String formTemplateGuid, String mainUrl, String completeGuid, String macAddressId) {
        try {
            String notesJson = geNoteDetails(mainUrl, edgeMacAddress.getNoteGuid());
            if (notesJson == null) {
                logger.info("Note not in Edge.");
                throw new NotesNotFoundException("Note [" + edgeMacAddress.getTitle() + "] not in Edge.");
            }
          /*  Type listType = new TypeToken<ArrayList<EdgeNote>>() {
            }.getType();
            List<EdgeNote> edgeNoteList = gson.fromJson(notesJson, listType);*/
            List<EdgeNote> edgeNoteList= new ArrayList<>();
            EdgeNote edgeNoteTemp = gson.fromJson(notesJson,EdgeNote.class);
            edgeNoteList.add(edgeNoteTemp);
            for (EdgeNote edgeNote : edgeNoteList) {
                JsonObject edgeJsonObj = (JsonObject) jsonParser.parse(gson.toJson(edgeNote));
                String notebookGuid = null;
                if (edgeJsonObj.get("edgeNotebook") != null) {
                    JsonObject notebookJson = edgeJsonObj.get("edgeNotebook").getAsJsonObject();
                    notebookGuid = notebookJson.get("notebookGuid").getAsString();
                }
                String noteGuid = edgeJsonObj.get("noteGuid").getAsString();
                JsonArray serverEdgeFormJsonArray = edgeJsonObj.get("formData").getAsJsonArray();
                int size = serverEdgeFormJsonArray.size();
                for (int i = 0; i < size; i++) {
                    JsonObject serverEdgeForm = serverEdgeFormJsonArray.get(i).getAsJsonObject();
                    String currentFormTemplateGuid = serverEdgeForm.get("formTemplateGuid").getAsString();
                    if (currentFormTemplateGuid.equals(formTemplateGuid)) {
                        String formDefJson = serverEdgeForm.get("formDef").getAsString();
                        formDefJson = formDefJson.replace("\\\\", "");
                        List<EdgeFormData> edgeFormDataList = getEdgeFormData(formDefJson);
                        updateFormValue(edgeFormDataList, Integer.parseInt(macAddressId), edgeMacAddress.getMacAddress());
                        updateFormValue(edgeFormDataList, 22, "Complete");
                        serverEdgeForm.add("formDef", gson.toJsonTree(edgeFormDataList));
                        serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
                    } else {
                        String formDefJson = serverEdgeForm.get("formDef").getAsString();
                        formDefJson = formDefJson.replace("\\\\", "");
                        List<EdgeFormData> edgeFormDataList = getEdgeFormData(formDefJson);
                        serverEdgeForm.add("formDef", gson.toJsonTree(edgeFormDataList));
                        serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
                    }
                }
                setGroupValue(completeGuid, edgeJsonObj);
                edgeJsonObj.add("formData", serverEdgeFormJsonArray);
                edgeJsonObj.addProperty("createdDateTime", System.currentTimeMillis());
                edgeJsonObj.addProperty("createdBy", "admin");
                edgeJsonObj.addProperty("noteGuid", UUID.randomUUID().toString());
                ResponseEntity<String> responseEntity = updateNoteDetails(mainUrl, edgeJsonObj.toString(), noteGuid, notebookGuid);
                System.out.println("success" + responseEntity.getBody());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<EdgeMacAddress> getSLVEmptyMacAddress() {
        List<EdgeMacAddress> edgeMacAddressList = new ArrayList<>();
        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String geoZoneDevices = properties.getProperty("streetlight.slv.url.getgeozone.devices");
        String url = mainUrl + geoZoneDevices;

        List<Object> paramsList = new ArrayList<Object>();
        paramsList.add("valueNames=idOnController");
        paramsList.add("valueNames=MacAddress");
        paramsList.add("ser=json");
        String params = StringUtils.join(paramsList, "&");
        url = url + "&" + params;
        ResponseEntity<String> response = slvRestService.getPostRequest(url, null);
        if (response.getStatusCode().is2xxSuccessful()) {
            String geoZoneDeviceDetails = response.getBody();
            JsonObject jsonObject = (JsonObject) jsonParser.parse(geoZoneDeviceDetails);
            JsonArray deviceValuesAsArray = jsonObject.get("values").getAsJsonArray();
            int totalSize = deviceValuesAsArray.size();
            for (int i = 0; i < totalSize; i++) {
                JsonArray deviceValues = deviceValuesAsArray.get(i).getAsJsonArray();
                if (deviceValues.size() >= 2) {
                    EdgeMacAddress edgeMacAddress = new EdgeMacAddress();
                    edgeMacAddress.setMacAddress(deviceValues.get(0).getAsString());
                    if (!deviceValues.get(1).isJsonNull()) {
                        String macAddress = deviceValues.get(1).getAsString();
                        if (macAddress != null && macAddress.contains("null")) {
                            edgeMacAddress.setMacAddress(macAddress);
                        } else {
                            edgeMacAddress.setMacAddress(null);
                        }
                    }
                }

            }
        }
        return edgeMacAddressList;
    }



    public List<EdgeMacAddress> getEdgeNoteFromServer() {
        List<EdgeMacAddress> edgeMacAddressList = new ArrayList<>();
        try {
            String data = null;
            System.out.println("Started");

            BufferedReader fis = new BufferedReader(new FileReader("./resources/edgedata.csv"));
            while ((data = fis.readLine()) != null) {
                EdgeMacAddress edgeMacAddress = new EdgeMacAddress();
                try {
                    String[] res = data.split(",");
                    edgeMacAddress.setTitle(res[0]);
                    edgeMacAddress.setNoteGuid(res[1]);
                    edgeMacAddress.setMacAddress(res[2]);
                    edgeMacAddressList.add(edgeMacAddress);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return edgeMacAddressList;

    }

    @Override
    public void buildFixtureStreetLightData(String data, List<Object> paramsList, EdgeNote edgeNote) throws InValidBarCodeException {

    }

    @Override
    public void processSetDevice(List<EdgeFormData> edgeFormDataList, ConfigurationJson configurationJson, EdgeNote edgeNote, List<Object> paramsList, SlvSyncDetails slvSyncDetails, String controllerStrIdValue) throws NoValueException, DeviceUpdationFailedException {

    }
}