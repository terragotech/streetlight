package com.slvinterface.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.slvinterface.dao.QueryExecutor;
import com.slvinterface.entity.DeviceEntity;
import com.slvinterface.entity.EdgeAllMac;
import com.slvinterface.entity.SLVSyncTable;
import com.slvinterface.entity.SLVTransactionLogs;
import com.slvinterface.enumeration.CallType;
import com.slvinterface.exception.DatabaseException;
import com.slvinterface.exception.NoValueException;
import com.slvinterface.exception.QRCodeAlreadyUsedException;
import com.slvinterface.exception.ReplaceOLCFailedException;
import com.slvinterface.json.*;
import com.slvinterface.utils.PropertiesReader;
import com.slvinterface.utils.ResourceDetails;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public abstract class SLVInterfaceService {

    private static final Logger logger = Logger.getLogger(SLVInterfaceService.class);

    EdgeRestService edgeRestService = null;
    JsonParser jsonParser = null;
    QueryExecutor queryExecutor = null;
    Gson gson = null;
    ConditionsJson conditionsJson = null;
    Properties properties = null;


    SLVInterfaceService() throws Exception {
        edgeRestService = new EdgeRestService();
        jsonParser = new JsonParser();
        queryExecutor = new QueryExecutor();
        gson = new Gson();
        this.properties = PropertiesReader.getProperties();
    }

    public void run() {
        String accessToken = edgeRestService.getEdgeToken();
        if (accessToken == null) {
            logger.error("Edge Invalid UserName and Password.");
            return;
        }


        try {
            conditionsJson = getConditionsJson();
        } catch (Exception e) {
            logger.error("Unable to load Configuration file.", e);
            return;
        }

        // Get NoteList from edgeserver
        ResponseEntity<String> edgeSlvServerResponse = edgeRestService.getRequest(edgeSlvUrl, false, accessToken);

        // Process only response code as success
        if (edgeSlvServerResponse.getStatusCode().is2xxSuccessful()) {
            // Get Response String
            String notesGuids = edgeSlvServerResponse.getBody();

            JsonArray noteGuidsJsonArray = (JsonArray) jsonParser.parse(notesGuids);
            if (noteGuidsJsonArray != null && !noteGuidsJsonArray.isJsonNull()) {
                for (JsonElement noteGuidJson : noteGuidsJsonArray) {
                    String noteGuid = noteGuidJson.getAsString();
                }
            }
        }
    }


    private void run(String noteGuid, String accessToken) throws DatabaseException {
        try {
            SLVSyncTable slvSyncTable = queryExecutor.getSLSyncTable(noteGuid);
            if (slvSyncTable != null) {
                logger.info("Current NoteGuid [" + noteGuid + "] is already Processed.");
                return;
            }
        } catch (Exception e) {
            throw new DatabaseException(e);
        }

        try {
            SLVSyncTable slvSyncTable = new SLVSyncTable();
            slvSyncTable.setNoteGuid(noteGuid);
            slvSyncTable.setProcessedDateTime(System.currentTimeMillis());

            String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");

            url = url + PropertiesReader.getProperties().getProperty("streetlight.edge.url.notes.get");

            url = url + "/" + noteGuid;
            logger.info("Given url is :" + url);


            // Get NoteList from edgeserver
            ResponseEntity<String> responseEntity = edgeRestService.getRequest(url, false, accessToken);

            // Process only response code as success
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String notesData = responseEntity.getBody();
            } else {
                slvSyncTable.setErrorDetails("Unable to Get Note Details from Edge Server.");
            }

        } catch (Exception e) {
            logger.error("Error in run", e);
        }

    }

    private void processNoteData(String notesData, SLVSyncTable slvSyncTable) {
        try {
            EdgeNote edgeNote = gson.fromJson(notesData, EdgeNote.class);
            populateSLVSyncTable(edgeNote, slvSyncTable);
        } catch (Exception e) {
            logger.error("Error in processNoteData", e);
        }
    }

    /**
     * Populate Note Details to SLVSyncTable.
     *
     * @param edgeNote
     * @param slvSyncTable
     */
    private void populateSLVSyncTable(EdgeNote edgeNote, SLVSyncTable slvSyncTable) {
        slvSyncTable.setNoteName(edgeNote.getTitle());
        slvSyncTable.setNoteCreatedBy(edgeNote.getCreatedBy());
        slvSyncTable.setNoteCreatedDateTime(edgeNote.getCreatedDateTime());
        slvSyncTable.setSyncTime(edgeNote.getSyncTime());
    }


    /**
     * Get List of processable FormData(FormTemplateGuid configured in properties file)
     *
     * @param edgeNote
     * @return
     */
    private List<FormData> getFormDataList(EdgeNote edgeNote) {
        List<FormData> formDataList = edgeNote.getFormData();
        List<FormData> formDataRes = new ArrayList<>();
        String formTemplateGuid = PropertiesReader.getProperties().getProperty("streetlight.edge.formtemplate.guid");
        for (FormData formData : formDataList) {
            if (formData.getFormTemplateGuid().equals(formTemplateGuid)) {
                formDataRes.add(formData);
            }
        }
        return formDataRes;
    }


    /**
     * Get Configuration Details
     *
     * @return
     * @throws Exception
     */
    public ConditionsJson getConditionsJson() throws Exception {
        FileReader reader = new FileReader(ResourceDetails.CONFIG_JSON_PATH);
        String configjson = jsonParser.parse(reader).toString();

        ConditionsJson configs = gson.fromJson(configjson, ConditionsJson.class);
        return configs;


    }


    protected String valueById(List<FormValues> formValuesList, int id) throws NoValueException {
        FormValues edgeFormTemp = new FormValues();
        edgeFormTemp.setId(id);

        int pos = formValuesList.indexOf(edgeFormTemp);
        if (pos != -1) {
            FormValues formValues = formValuesList.get(pos);
            String value = formValues.getValue();
            logger.info("edgeFormData value:" + value);
            if (value == null || value.trim().isEmpty() || value.contains("null") || value.equals("null")) {
                throw new NoValueException("Value is Empty or null." + value);
            }
            return value;
        } else {
            throw new NoValueException(id + " is not found.");
        }
    }

    public boolean checkMacAddressExists(String macAddress, String idOnController)
            throws QRCodeAlreadyUsedException, Exception {

        boolean isExistMacAddress = queryExecutor.isExistMacAddress(idOnController, macAddress);
        if (isExistMacAddress) {
            throw new QRCodeAlreadyUsedException("QR code [" + macAddress + "] is already processed.", macAddress);
        }
        logger.info("Getting Mac Address from SLV.");
        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String updateDeviceValues = properties.getProperty("streetlight.slv.url.search.device");
        String url = mainUrl + updateDeviceValues;
        List<String> paramsList = new ArrayList<String>();
        paramsList.add("attribute=MacAddress");
        paramsList.add("value=" + macAddress);
        paramsList.add("operator=eq-i");
        paramsList.add("recurse=true");
        paramsList.add("ser=json");
        String params = StringUtils.join(paramsList, "&");
        url = url + "?" + params;
        System.out.println("Url :" + url);
        ResponseEntity<String> response = slvRestService.getRequest(url, true, null);
        if (response.getStatusCodeValue() == 200) {
            String responseString = response.getBody();
            logger.info("-------MAC Address----------");
            logger.info(responseString);
            logger.info("-------MAC Address End----------");
            DeviceMacAddress deviceMacAddress = gson.fromJson(responseString, DeviceMacAddress.class);
            List<Value> values = deviceMacAddress.getValue();
            StringBuilder stringBuilder = new StringBuilder();
            if (values == null || values.size() == 0) {
                return false;
            } else {
                for (Value value : values) {
                    if (value.getIdOnController().equals(idOnController)) {
                        // return false;
                    }
                    stringBuilder.append(value.getIdOnController());
                    stringBuilder.append("\n");
                }
            }
            throw new QRCodeAlreadyUsedException("QR code [" + macAddress + "] is already Used in following devices [" + stringBuilder.toString() + "]", macAddress);
        } else {
            throw new QRCodeAlreadyUsedException("Error while getting data from SLV.", macAddress);
        }

    }

    public void loadDeviceValues(String idOnController, DeviceEntity deviceEntity) throws Exception {
        try {
            logger.info("loadDeviceValues called.");
            String mainUrl = properties.getProperty("streetlight.slv.url.main");
            String deviceUrl = properties.getProperty("streetlight.slv.url.search.device");
            String url = mainUrl + deviceUrl;
            List<String> paramsList = new ArrayList<>();
            paramsList.add("attributeName=idOnController");
            paramsList.add("attributeValue=" + idOnController);
            paramsList.add("recurse=true");
            paramsList.add("returnedInfo=lightDevicesList");
            paramsList.add("attributeOperator=eq-i");
            paramsList.add("maxResults=1");
            paramsList.add("ser=json");
            String params = StringUtils.join(paramsList, "&");
            url = url + "?" + params;
            logger.info("Load Device url :" + url);
            ResponseEntity<String> response = restService.getRequest(url, true, null);
            if (response.getStatusCodeValue() == 200) {
                logger.info("LoadDevice Respose :" + response.getBody());
                String responseString = response.getBody();
                int id = processDeviceJson(responseString);
                logger.info("LoadDevice Id :" + id);

                if (id == 0) {
                    logger.info("csl and context hashmap are cleared");
                    throw new NoValueException("Device id:[" + idOnController + "] does not exists in SLV server");
                } else {
                    String subDeviceUrl = getDeviceUrl(id);
                    logger.info("subDevice url:" + subDeviceUrl);
                    ResponseEntity<String> responseEntity = restService.getRequest(subDeviceUrl, true, null);
                    if (response.getStatusCodeValue() == 200) {
                        String deviceResponse = responseEntity.getBody();
                        processDeviceValuesJson(deviceResponse, idOnController, deviceEntity);
                    }
                }
            }
        } catch (Exception e) {

            throw new Exception(e);
        }
    }

    public String getDeviceUrl(int id) {
        logger.info("getDeviceUrl url called");
        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String getDeviceUrl = properties.getProperty("streetlight.slv.url.getdevice.device");
        String deviceMainUrl = mainUrl + getDeviceUrl;
        List<String> paramsList = new ArrayList<>();
        paramsList.add("deviceId=" + id);
        paramsList.add("ser=json");
        String params = StringUtils.join(paramsList, "&");
        deviceMainUrl = deviceMainUrl + "?" + params;
        return deviceMainUrl;
    }

    public void processDeviceValuesJson(String deviceValuesjson, String idOnController, DeviceEntity deviceEntity) {
        logger.info("processDeviceValuesJson called start");
        String installStatusKey = properties.getProperty("streetlight.install.status");
        String cslInstallDateKey = properties.getProperty("streetlight.csl.installdate");
        String dimmingGroupKey = properties.getProperty("streetlight.dimminggroup");
        //String serialNumberKey = properties.getProperty("streetlight.luminaire.serialnumber");
        logger.info("installStatusKey :" + installStatusKey);
        logger.info("cslInstallDate :" + cslInstallDateKey);
        logger.info("cslLuminaireDate :" + dimmingGroupKey);
        JsonObject jsonObject = new JsonParser().parse(deviceValuesjson).getAsJsonObject();
        logger.info("Device request json:" + gson.toJson(jsonObject));
        JsonArray arr = jsonObject.getAsJsonArray("properties");
        String nodeInstall = null;
        String luminaireDate = null;
        String macAddress = null;
        String installStatus = null;
        String dimmingGroupName = null;
        for (int i = 0; i < arr.size(); i++) {
            JsonObject jsonObject1 = arr.get(i).getAsJsonObject();
            String keyValue = jsonObject1.get("key").getAsString();
            if (keyValue != null && keyValue.equals(installStatusKey)) {
                installStatus = jsonObject1.get("value").getAsString();
            } else if (keyValue != null && keyValue.equals(cslInstallDateKey)) {
                nodeInstall = jsonObject1.get("value").getAsString();
            } else if (keyValue != null && keyValue.equals("userproperty.MacAddress")) {
                macAddress = jsonObject1.get("value").getAsString();
            } else if (keyValue != null && keyValue.equals(dimmingGroupKey)) {
                dimmingGroupName = jsonObject1.get("value").getAsString();
            }
        }
        //
        if (nodeInstall != null && !nodeInstall.trim().isEmpty() && nodeInstall.trim().length() > 7) {
            deviceEntity.setInstallDate(nodeInstall);
        }
        if (installStatus != null && !installStatus.trim().isEmpty() && installStatus.trim().length() > 5) {
            deviceEntity.setInstallStatus(installStatus);
        }

        if (dimmingGroupName != null && !dimmingGroupName.trim().isEmpty() && dimmingGroupName.trim().length() > 3) {
            deviceEntity.setDimmingGroup(dimmingGroupName);
        }
        deviceEntity.setMacAddress(macAddress);
        deviceEntity.setDimmingGroup(dimmingGroupName);
        logger.info("processDeviceValuesJson End");
    }

    public void createEdgeAllMac(String title, String macAddress) {
        EdgeAllMac edgeAllMacData = new EdgeAllMac();
        edgeAllMacData.setMacAddress(macAddress);
        edgeAllMacData.setNoteTitle(title);
        queryExecutor.saveEdgeAllMac(edgeAllMacData);
    }

    private void setSLVTransactionLogs(SLVTransactionLogs slvTransactionLogs, String request, CallType callType) {
        slvTransactionLogs.setRequestDetails(request);
        slvTransactionLogs.setTypeOfCall(callType);
    }

    private void setResponseDetails(SLVTransactionLogs slvTransactionLogs, String responseString) {
        slvTransactionLogs.setResponseBody(responseString);
    }
    protected int setDeviceValues(List<Object> paramsList, SLVTransactionLogs slvTransactionLogs) {
        int errorCode = -1;
        try {
            String mainUrl = properties.getProperty("streetlight.slv.url.main");
            String updateDeviceValues = properties.getProperty("streetlight.slv.url.updatedevice");
            String url = mainUrl + updateDeviceValues;

            paramsList.add("ser=json");
            String params = StringUtils.join(paramsList, "&");
            url = url + "&" + params;
            logger.info("SetDevice method called");
            logger.info("SetDevice url:" + url);
            setSLVTransactionLogs(slvTransactionLogs, url, CallType.SET_DEVICE);
            ResponseEntity<String> response = restService.getPostRequest(url, null);
            String responseString = response.getBody();
            setResponseDetails(slvTransactionLogs, responseString);
            JsonObject replaceOlcResponse = (JsonObject) jsonParser.parse(responseString);
            errorCode = replaceOlcResponse.get("errorCode").getAsInt();
        } catch (Exception e) {
            setResponseDetails(slvTransactionLogs, "Error in setDeviceValues:" + e.getMessage());
            logger.error("Error in setDeviceValues", e);
        } finally {
            queryExecutor.saveSLVTransactionLogs(slvTransactionLogs);
        }

        return errorCode;
    }

    /**
     * Calls ReplaceOLCs
     *
     * @throws ReplaceOLCFailedException
     */
    public void replaceOLC(String controllerStrIdValue, String idOnController, String macAddress, SLVTransactionLogs slvTransactionLogs, SlvInterfaceLogEntity slvInterfaceLogEntity)
            throws ReplaceOLCFailedException {
        try {
            // String newNetworkId = slvSyncDataEntity.getMacAddress();
            String newNetworkId = macAddress;

            // Get Url detail from properties
            String mainUrl = properties.getProperty("streetlight.url.main");
            String dataUrl = properties.getProperty("streetlight.url.replaceolc");
            String replaceOlc = properties.getProperty("streetlight.url.replaceolc.method");
            String url = mainUrl + dataUrl;
            String controllerStrId = controllerStrIdValue;
            List<Object> paramsList = new ArrayList<Object>();
            paramsList.add("methodName=" + replaceOlc);
            paramsList.add("controllerStrId=" + controllerStrId);
            paramsList.add("idOnController=" + idOnController);
            paramsList.add("newNetworkId=" + newNetworkId);
            paramsList.add("ser=json");
            String params = StringUtils.join(paramsList, "&");
            url = url + "?" + params;
            setSLVTransactionLogs(slvTransactionLogs, url, CallType.REPLACE_OLC);
            ResponseEntity<String> response = restService.getPostRequest(url, null);
            String responseString = response.getBody();
            setResponseDetails(slvTransactionLogs, responseString);
            JsonObject replaceOlcResponse = (JsonObject) jsonParser.parse(responseString);
            String errorStatus = replaceOlcResponse.get("status").getAsString();
            logger.info("Replace OLC Process End.");
            // As per doc, errorcode is 0 for success. Otherwise, its not success.
            if (errorStatus.equals("ERROR")) {
                String value = replaceOlcResponse.get("value").getAsString();
                throw new ReplaceOLCFailedException(value);
            } else {
                if (macAddress != null && !macAddress.trim().isEmpty()) {
                    createEdgeAllMac(idOnController, macAddress);
                    logger.info("Clear device process starts.");
                    logger.info("Clear device process End.");
                }

            }

        } catch (Exception e) {
            logger.error("Error in replaceOLC", e);
            throw new ReplaceOLCFailedException(e.getMessage());
        } finally {
            queryExecutor.saveSLVTransactionLogs(slvTransactionLogs);
        }

    }

    public int processDeviceJson(String deviceJson) {
        JsonObject jsonObject = new JsonParser().parse(deviceJson).getAsJsonObject();
        logger.info("Device request json:" + gson.toJson(jsonObject));
        JsonArray arr = jsonObject.getAsJsonArray("value");
        for (int i = 0; i < arr.size(); i++) {
            int id = arr.get(i).getAsJsonObject().get("id").getAsInt();
            logger.info("Device id value :" + id);
            System.out.println(id);
            return id;
        }
        return 0;
    }
}
