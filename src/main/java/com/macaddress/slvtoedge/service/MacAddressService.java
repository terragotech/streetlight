package com.macaddress.slvtoedge.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.macaddress.slvtoedge.model.EdgeMacAddress;
import com.terragoedge.slvinterface.dao.ConnectionDAO;
import com.terragoedge.slvinterface.dao.SLVInterfaceDAO;
import com.terragoedge.slvinterface.exception.NotesNotFoundException;
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
        EdgeMacAddress edgeMacAddress1 = new EdgeMacAddress();
        edgeMacAddress1.setMacAddress("a");
        edgeMacAddress1.setTitle("1");
        edgeMacAddressList.add(edgeMacAddress1);
        EdgeMacAddress edgeMacAddress2 = new EdgeMacAddress();
        edgeMacAddress2.setMacAddress("b");
        edgeMacAddress2.setTitle("2");
        edgeMacAddressList.add(edgeMacAddress2);

        //TODO getMAcADdress From Edge;
        //   List<EdgeMacAddress> slvMacAddressList = getSLVEmptyMacAddress();
        List<EdgeMacAddress> slvMacAddressList = new ArrayList<>();
        EdgeMacAddress edgeMacAddress3 = new EdgeMacAddress();
        edgeMacAddress3.setMacAddress("a");
        edgeMacAddress3.setTitle("1");
        slvMacAddressList.add(edgeMacAddress3);
        EdgeMacAddress edgeMacAddress4 = new EdgeMacAddress();
        edgeMacAddress4.setMacAddress("00135005007F130F");
        edgeMacAddress4.setTitle("1489077");
        slvMacAddressList.add(edgeMacAddress4);
        System.out.println(slvMacAddressList.size());

        for (EdgeMacAddress edgeMacAddress : slvMacAddressList) {
            if (!edgeMacAddressList.contains(edgeMacAddress)) {
                System.out.println(edgeMacAddress);
                processEdgeNote(edgeMacAddress, formTemplateGuid, mainUrl, completeGuid, macAddressId);
            }
        }
    }

    public void processEdgeNote(EdgeMacAddress edgeMacAddress, String formTemplateGuid, String mainUrl, String completeGuid, String macAddressId) {
        try {
            String notesJson = geNoteDetails(mainUrl, edgeMacAddress.getTitle());
            if (notesJson == null) {
                logger.info("Note not in Edge.");
                throw new NotesNotFoundException("Note [" + edgeMacAddress.getTitle() + "] not in Edge.");
            }
            Type listType = new TypeToken<ArrayList<EdgeNote>>() {
            }.getType();
            List<EdgeNote> edgeNoteList = gson.fromJson(notesJson, listType);
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
                        serverEdgeForm.add("formDef", gson.toJsonTree(edgeFormDataList));
                        serverEdgeForm.addProperty("formGuid", UUID.randomUUID().toString());
                    }else {
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
                System.out.println("success");
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

    protected String geNoteDetails(String baseUrl, String noteName) {
        try {
            String urlNew = baseUrl + "/rest/notes/notesdata/" + noteName;

            //  String urlNew = baseUrl + "/rest/notes?search=" + noteName;
            logger.info("Url to get Note Details:" + urlNew);
            ResponseEntity<String> responseEntity = serverCall(urlNew, HttpMethod.GET, null);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String response = responseEntity.getBody();
                logger.info("----------Response-------");
                logger.info(response);
                return response;
            }
        } catch (Exception e) {
            logger.error("Error in getNoteDetails", e);
        }
        return null;
    }

    public static void updateFormValue(List<EdgeFormData> edgeFormDatas, int id, String value) {
        EdgeFormData tempEdgeFormData = new EdgeFormData();
        tempEdgeFormData.setId(id);
        int pos = edgeFormDatas.indexOf(tempEdgeFormData);
        if (pos != -1) {
            EdgeFormData edgeFormData = edgeFormDatas.get(pos);
            edgeFormData.setValue(edgeFormData.getLabel() + "#" + value);
        }
    }

    protected List<EdgeFormData> getEdgeFormData(String data) {
        List<EdgeFormData> edgeFormDatas = gson.fromJson(data, new TypeToken<List<EdgeFormData>>() {
        }.getType());
        return edgeFormDatas;
    }

    public void setGroupValue(String value, JsonObject notesJson) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("key", "groupGuid");
        jsonObject.addProperty("value", value);
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(jsonObject);
        notesJson.add("dictionary", jsonArray);
    }
}
