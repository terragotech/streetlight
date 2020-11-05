package com.slvinterface.service;

import com.google.gson.*;
import com.slvinterface.dao.QueryExecutor;
import com.slvinterface.entity.DeviceEntity;
import com.slvinterface.entity.EdgeAllMac;
import com.slvinterface.entity.SLVSyncTable;
import com.slvinterface.entity.SLVTransactionLogs;
import com.slvinterface.enumeration.CallType;
import com.slvinterface.enumeration.SLVProcess;
import com.slvinterface.exception.*;
import com.slvinterface.json.*;
import com.slvinterface.utils.PropertiesReader;
import com.slvinterface.utils.ResourceDetails;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class SLVInterfaceService {

    private static final Logger logger = Logger.getLogger(SLVInterfaceService.class);

    EdgeRestService edgeRestService = null;
    JsonParser jsonParser = null;
    QueryExecutor queryExecutor = null;
    Gson gson = null;
    ConditionsJson conditionsJson = null;
    Properties properties = null;
    SLVRestService slvRestService = null;
    GenericProcess genericProcess = null;
    ReplaceFormService replaceFormService = null;

    public static int retryCount = 0;

    SLVTools slvTools;


    SLVInterfaceService() throws Exception {
        edgeRestService = new EdgeRestService();
        jsonParser = new JsonParser();
        queryExecutor = new QueryExecutor();
        gson = new Gson();
        slvTools = new SLVTools();
        slvRestService = new SLVRestService();
        genericProcess = new GenericProcess();
        replaceFormService = new ReplaceFormService();
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

        String reSync =  PropertiesReader.getProperties().getProperty("streetlight.edge.resync");
        if(reSync != null && reSync.trim().equals("true")){
            logger.info("ReSync Process Starts.");
            reSync(accessToken);
            logger.info("ReSync Process Ends.");
            return;
        }

        String edgeSlvUrl =  PropertiesReader.getProperties().getProperty("streetlight.edge.slvServerUrl");

        Long lastSyncTime =   queryExecutor.getMaxSyncTime();
        if(lastSyncTime == -1){
            lastSyncTime = System.currentTimeMillis() - (30 * 60000);
        }
        edgeSlvUrl = edgeSlvUrl +"/notesGuid?lastSyncTime="+lastSyncTime;
        // Get NoteList from edgeserver
        ResponseEntity<String> edgeSlvServerResponse = edgeRestService.getRequest(edgeSlvUrl, false, accessToken);

        // Process only response code as success
        if (edgeSlvServerResponse.getStatusCode().is2xxSuccessful()) {
            // Get Response String
            String notesGuids =  edgeSlvServerResponse.getBody();
            JsonArray noteGuidsJsonArray = (JsonArray) jsonParser.parse(notesGuids);
            if (noteGuidsJsonArray != null && !noteGuidsJsonArray.isJsonNull()) {
                for (JsonElement noteGuidJson : noteGuidsJsonArray) {
                    String noteGuid = noteGuidJson.getAsString();
                    logger.info("Current NoteGuid:"+noteGuid);
                    try {
                        run(noteGuid,accessToken);
                    }catch (DatabaseException e){
                        logger.error("Error while getting value from DB.Due to DB Error we are skipping other notes also",e);
                        return;
                    }catch (SLVConnectionException e){
                        logger.error("Unable to connect with SLV Server.");
                        return;
                    }catch (Exception e){
                        logger.error("Error while processing this note.NoteGuid:"+noteGuid);
                    }

                }
            }
        }
    }

    private void reSync(String accessToken){
        BufferedReader bufferedReader = null;
        try{
            bufferedReader = new BufferedReader(new FileReader("./data/resynclist.txt"));
            String noteGuid = null;
            while ((noteGuid = bufferedReader.readLine()) != null) {
                try {
                    run(noteGuid,accessToken);
                }catch (DatabaseException e){
                    logger.error("Error while getting value from DB.Due to DB Error we are skipping other notes also",e);
                    return;
                }catch (SLVConnectionException e){
                    logger.error("Unable to connect with SLV Server.");
                    return;
                }catch (Exception e){
                    logger.error("Error while processing this note.NoteGuid:"+noteGuid);
                }
            }
        }catch (Exception e) {
            logger.error("Error in  reSync.", e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void run(String noteGuid, String accessToken) throws DatabaseException,SLVConnectionException {
        try {
            SLVSyncTable slvSyncTable = queryExecutor.getSLSyncTable(noteGuid);
            if (slvSyncTable != null) {
                logger.info("Current NoteGuid [" + noteGuid + "] is already Processed.");
               // return;
            }
        } catch (Exception e) {
            //throw new DatabaseException(e);
        }

        SLVSyncTable slvSyncTable = new SLVSyncTable();
        slvSyncTable.setNoteGuid(noteGuid);
        slvSyncTable.setProcessedDateTime(System.currentTimeMillis());

        try {
            String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");

            url = url + PropertiesReader.getProperties().getProperty("streetlight.edge.url.notes.get");

            url = url + "/" + noteGuid;
            logger.info("Given url is :" + url);


            // Get NoteList from edgeserver
            ResponseEntity<String> responseEntity = edgeRestService.getRequest(url, false, accessToken);

            // Process only response code as success
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                try{
                    SlvRestTemplate.INSTANCE.refreshToken();
                }catch (Exception e){
                    logger.error("Error while refreshing token: ",e);
                }
                logger.info("Response from edge.");
                String notesData = responseEntity.getBody();
                EdgeNote edgeNote = gson.fromJson(notesData, EdgeNote.class);
                String replaceFormTemplateGuid = PropertiesReader.getProperties().getProperty("streetlight.edge.replaceform");
                boolean isReplaceFormPresent = false;
                if(replaceFormTemplateGuid != null){
                    List<FormData> formDataList =  edgeNote.getFormData();
                    for(FormData formData : formDataList){
                       if(formData.getFormTemplateGuid().equals(replaceFormTemplateGuid)){
                           isReplaceFormPresent = true;
                       }
                    }
                }
                if(isReplaceFormPresent){
                    replaceFormService.processReplaceForm(edgeNote);
                }else{
                    genericProcess.process(edgeNote);
                }

                //processNoteData(notesData,slvSyncTable);
            } else {
                slvSyncTable.setErrorDetails("Unable to Get Note Details from Edge Server and status code is:"+responseEntity.getStatusCode());
            }

        }/*catch (SLVConnectionException e){
            slvSyncTable.setStatus("Failure");
            slvSyncTable.setErrorDetails(e.getMessage());
            throw new SLVConnectionException(e);
        }*/catch (Exception e) {
            slvSyncTable.setStatus("Failure");
            slvSyncTable.setErrorDetails(e.getMessage());
            logger.error("Error in run", e);
        }finally {
            try {
                queryExecutor.saveSLVTransactionLogs(slvSyncTable);
            }catch (Exception e){
                throw new DatabaseException(e);
            }

        }

    }

    public void checkTokenValidity(Edge2SLVData edge2SLVData)throws SLVConnectionException{
        try{
            logger.info("Checking Token Validity.");
            logger.info("Current RetryCount:"+retryCount);
            DeviceEntity deviceEntity = new DeviceEntity();
            loadDeviceValues(edge2SLVData.getIdOnController(),deviceEntity);
            return;
        }catch (NoValueException e){
            return;
        }catch (Exception e){
            retryCount = retryCount + 1;
            logger.error("Error in checkTokenValidity",e);
            if(retryCount > 5){
                throw new SLVConnectionException("Unable to connect with SLV.",e);
            }
            try{
                if(retryCount > 1){
                    logger.info("Tried more that one time.So try after 30secs...");
                    Thread.sleep(30000);
                }
                try{
                    SlvRestTemplate.INSTANCE.refreshToken();
                }catch (Exception e1){
                    logger.error("Error while refreshing token: ",e1);
                }
                checkTokenValidity(edge2SLVData);
            }catch (Exception e1){
                throw new SLVConnectionException("Unable to connect with SLV.",e);
            }

        }
    }
    private void handleRemoveForm(List<FormData> formDataList,SLVSyncTable slvSyncTable){
        try {
            String controllerStrId = properties.getProperty("streetlight.controller.str.id");
        String idoncontroller = "";
        String installformTemplate1 = PropertiesReader.getProperties().getProperty("streetlight.edge.installid");
        String installformTemplate2 = PropertiesReader.getProperties().getProperty("streetlight.edge.installidn");
        String []arinstallformTemplate1 = installformTemplate1.split(",");
        String []arinstallformTemplate2 = installformTemplate2.split(",");
        for(FormData formData:formDataList)
        {
            if(formData.getFormTemplateGuid().equals(arinstallformTemplate1[1]))
            {
                List<FormValues> frmValues = formData.getFormDef();
                idoncontroller = valueById(frmValues,Integer.parseInt(arinstallformTemplate1[0]));
            }
            else if(formData.getFormTemplateGuid().equals(arinstallformTemplate2[1]))
            {
                List<FormValues> frmValues = formData.getFormDef();
                idoncontroller = valueById(frmValues,Integer.parseInt(arinstallformTemplate2[0]));
            }
        }
           replaceOLC(controllerStrId, idoncontroller, "", slvSyncTable);
        }
        catch (ReplaceOLCFailedException e)
        {

            logger.error(e);
        }
        catch (NoValueException e)
        {
            logger.error(e);
        }
    }
    private void processNoteData(String notesData, SLVSyncTable slvSyncTable)throws SLVConnectionException {
        try {
            EdgeNote edgeNote = gson.fromJson(notesData, EdgeNote.class);
            populateSLVSyncTable(edgeNote, slvSyncTable);
            List<FormData> formDataList = getFormDataList(edgeNote);
            if(formDataList.size() < 1){
                slvSyncTable.setErrorDetails("Form Template is not present.");
                slvSyncTable.setStatus("Failure");
                logger.info("Form Template is not present.");
                return;
            }
            String formTemplateGuid = PropertiesReader.getProperties().getProperty("streetlight.edge.formtemplate.guid");
            String []arformTemplateGuid = formTemplateGuid.split(",");
            /* To Handle the Remove Form */
            int mode = 0;
            String removeFormGUID = PropertiesReader.getProperties().getProperty("streetlight.edge.removeformtemplate.guid");
            for(FormData formData:formDataList)
            {
                if(formData.getFormTemplateGuid().equals(removeFormGUID))
                {
                    handleRemoveForm(formDataList,slvSyncTable);
                    return;
                }
                else if (formData.getFormTemplateGuid().equals(arformTemplateGuid[0]))
                {
                    mode = 0;
                }
                else if (formData.getFormTemplateGuid().equals(arformTemplateGuid[1]))
                {
                    mode = 1;
                }
            }
            /* End of Handle to Remove Form */
            conditionsJson = getConditionsJson(mode);
            processFormData(formDataList,slvSyncTable,edgeNote);
        }catch (SLVConnectionException e){
            throw new SLVConnectionException(e);
        }catch (Exception e) {
            slvSyncTable.setErrorDetails(e.getMessage());
            slvSyncTable.setStatus("Failure");
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
        slvSyncTable.setParentNoteId(edgeNote.getBaseParentNoteId());
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
        String []arformTemplateGuid = formTemplateGuid.split(",");

        for (FormData formData : formDataList) {
            if(arformTemplateGuid.length >= 3)
            {
                if (formData.getFormTemplateGuid().equals(arformTemplateGuid[0]) ||
                        formData.getFormTemplateGuid().equals(arformTemplateGuid[1]) ||
                                formData.getFormTemplateGuid().equals(arformTemplateGuid[2])) {
                    formDataRes.add(formData);
                }
            }
            else if(arformTemplateGuid.length == 1)
            {
                if (formData.getFormTemplateGuid().equals(arformTemplateGuid[0])) {
                    formDataRes.add(formData);
                }
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
    public ConditionsJson getConditionsJson(int option) throws Exception {
        ConditionsJson configs = null;
        if(option == 0) {
            FileReader reader = new FileReader(ResourceDetails.CONFIG_JSON_PATH);
            String configjson = jsonParser.parse(reader).toString();

            configs = gson.fromJson(configjson, ConditionsJson.class);
        }
        else if(option == 1)
        {
            FileReader reader = new FileReader(ResourceDetails.CONFIG_JSON_PATH1);
            String configjson = jsonParser.parse(reader).toString();
            configs = gson.fromJson(configjson, ConditionsJson.class);
        }
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
            return value.trim();
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
        String mainUrl = properties.getProperty("streetlight.slv.base.url");
        String updateDeviceValues = properties.getProperty("streetlight.slv.url.search.device");
        String url = mainUrl + updateDeviceValues;
        List<String> paramsList = new ArrayList<String>();
        paramsList.add("attributeName=MacAddress");
        paramsList.add("attributeValue=" + macAddress);
        paramsList.add("attributeOperator=eq-i");
        String geoZoneId = PropertiesReader.getProperties().getProperty("streetlight.root.geozone");
        paramsList.add("geoZoneId="+geoZoneId);
        paramsList.add("recurse=true");
        paramsList.add("returnedInfo=devicesList");
        paramsList.add("ser=json");
        String params = StringUtils.join(paramsList, "&");
        url = url + "?" + params;
        System.out.println("Url :" + url);
        ResponseEntity<String> response = slvRestService.callGetMethod(url);
        if (response.getStatusCode() == HttpStatus.OK) {
            String responseString = response.getBody();
            DeviceMacAddress deviceMacAddress = gson.fromJson(responseString, DeviceMacAddress.class);
            List<Value> values = deviceMacAddress.getValue();
            StringBuilder stringBuilder = new StringBuilder();
            if (values == null || values.size() == 0) {
                return false;
            } else {
                for (Value value : values) {
                    if (value.getIdOnController().equals(idOnController)) {
                        if (values.size() == 1) {
                             return false;
                        }
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

    public void loadDeviceValues(String idOnController, DeviceEntity deviceEntity) throws NoValueException,SLVUnAuthorizeException, IOException, ClientProtocolException {
        logger.info("loadDeviceValues called.");
        String mainUrl = properties.getProperty("streetlight.slv.base.url");
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
        ResponseEntity<String> response = slvRestService.callGetMethod(url);
        if (response.getStatusCode() == HttpStatus.OK) {
            String responseString = response.getBody();
            logger.info("LoadDevice Respose :" + responseString);
            if(responseString != null){
                int id = processDeviceJson(responseString);
                logger.info("LoadDevice Id :" + id);

                if (id == 0) {
                    logger.info("csl and context hashmap are cleared");
                    throw new NoValueException("Device id:[" + idOnController + "] does not exists in SLV server");
                } else {
                    String subDeviceUrl = getDeviceUrl(id);
                    logger.info("subDevice url:" + subDeviceUrl);
                    ResponseEntity<String> httpResponse = slvRestService.callGetMethod(subDeviceUrl);
                    if (httpResponse.getStatusCode() == HttpStatus.OK) {
                        String deviceResponse = httpResponse.getBody();
                        if(deviceResponse != null){
                           // processDeviceValuesJson(deviceResponse, idOnController, deviceEntity);
                        }
                    }
                }
            }

        }else if(response.getStatusCode() == HttpStatus.FORBIDDEN){
            String responseString = response.getBody();
            logger.info("LoadDevice Respose :" + responseString);
            throw new SLVUnAuthorizeException(responseString);
        }
    }

    public String getDeviceUrl(int id) {
        logger.info("getDeviceUrl url called");
        String mainUrl = properties.getProperty("streetlight.slv.base.url");
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
        JsonObject jsonObject = new JsonParser().parse(deviceValuesjson).getAsJsonObject();
        logger.info("Device request json:" + gson.toJson(jsonObject));

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
        //slvTransactionLogs.setTypeOfCall(callType);
    }

    private void setResponseDetails(SLVTransactionLogs slvTransactionLogs, String responseString) {
        slvTransactionLogs.setResponseBody(responseString);
    }
    protected int setDeviceValues(LinkedMultiValueMap<String,String> paramsList, SLVTransactionLogs slvTransactionLogs) {
        int errorCode = -1;
        try {
            String mainUrl = properties.getProperty("streetlight.slv.base.url");
            String updateDeviceValues = properties.getProperty("streetlight.slv.url.updatedevice");
            String url = mainUrl + updateDeviceValues;

            paramsList.add("ser","json");
            logger.info("SetDevice method called");
            logger.info("SetDevice url:" + url);
            setSLVTransactionLogs(slvTransactionLogs, url, CallType.SET_DEVICE);
            ResponseEntity<String> response = slvRestService.getPostRequest(url,null,paramsList);
            String responseString =  response.getBody();
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
    public void replaceOLC(String controllerStrIdValue, String idOnController, String macAddress,SLVSyncTable slvSyncTable)
            throws ReplaceOLCFailedException {
        SLVTransactionLogs slvTransactionLogs = getSLVTransVal(slvSyncTable);
        try {
            // Check MAC Address already Present.
            if(macAddress != null && !macAddress.trim().isEmpty()){
                checkMacAddressExists(macAddress,idOnController);
            }
            String newNetworkId = macAddress;

            // Get Url detail from properties
            String mainUrl = properties.getProperty("streetlight.slv.base.url");
            String dataUrl = properties.getProperty("streetlight.url.replaceolc");
//            String replaceOlc = properties.getProperty("streetlight.url.replaceolc.method");
            String url = mainUrl + dataUrl;
            String controllerStrId = controllerStrIdValue;
            LinkedMultiValueMap<String,String> paramsList = new LinkedMultiValueMap<>();
//            paramsList.add("methodName",replaceOlc);
            paramsList.add("controllerStrId",controllerStrId);
            paramsList.add("idOnController",idOnController);
            paramsList.add("newNetworkId",newNetworkId);
            paramsList.add("ser","json");
            setSLVTransactionLogs(slvTransactionLogs, url, CallType.REPLACE_OLC);
            ResponseEntity<String> response = slvRestService.getPostRequest(url,null,paramsList);
            String responseString =  response.getBody();
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
                }

            }

        } catch (QRCodeAlreadyUsedException e){
            slvSyncTable.setStatus("Failure");
            slvSyncTable.setErrorDetails(e.getMessage());
            logger.info("MAC Address is Empty. So Note is not synced.");
            return;
        }
        catch (Exception e) {
            logger.error("Error in replaceOLC", e);
            throw new ReplaceOLCFailedException(e.getMessage());
        }
        finally {
            queryExecutor.saveSLVTransactionLogs(slvTransactionLogs);
        }

    }

    public SLVTransactionLogs getSLVTransVal(SLVSyncTable slvSyncTable){
        SLVTransactionLogs slvTransactionLogs = new SLVTransactionLogs();
        slvTransactionLogs.setTitle(slvSyncTable.getNoteName());
        slvTransactionLogs.setNoteGuid(slvSyncTable.getNoteGuid());
        slvTransactionLogs.setCreatedDateTime(slvSyncTable.getNoteCreatedDateTime());
        slvTransactionLogs.setParentNoteGuid(slvSyncTable.getParentNoteId());
        return slvTransactionLogs;
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


    public void processFormData(List<FormData> formDataList, SLVSyncTable slvSyncTable,EdgeNote edgeNote)throws SLVConnectionException{

    }


    protected void addStreetLightData(String key, String value, LinkedMultiValueMap<String,String> paramsList) {
        paramsList.add("valueName",key.trim());
        try {
//            value =  URLEncoder.encode(value,"UTF-8");
            paramsList.add("value", value.trim());
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void loadVal(LinkedMultiValueMap<String,String> paramsList, Edge2SLVData previousEdge2SLVData){
        paramsList.add("idOnController",previousEdge2SLVData.getIdOnController());
        paramsList.add("controllerStrId",previousEdge2SLVData.getControllerStrId());
    }


    protected String dateFormat(Long dateTime) {
        Date date = new Date(Long.valueOf(dateTime));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String timeZone = properties.getProperty("streetlight.timezone");
        if(timeZone != null && !timeZone.trim().isEmpty()){
            dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        }else{
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        }

        String dff = dateFormat.format(date);
        return dff;
    }


    /**
     * Populate EdgeValue to Edge2SLVData from FormValue based on Configuration JSON
     * @param formData
     * @param edge2SLVData
     */
    public void processFormData(FormData formData,Edge2SLVData edge2SLVData){
        List<FormValues> formValuesList = formData.getFormDef();
        List<Priority> priorities = conditionsJson.getPriority();
        List<Config> configList = conditionsJson.getConfigList();
        for(Priority priority : priorities){
            Config temp = new Config();
            temp.setType(priority.getType());

            int pos = configList.indexOf(temp);

            if(pos != -1){
                Config config =  configList.get(pos);
                List<Id> idList = config.getIds();
                for(Id id : idList){
                    switch (id.getType()){
                        case MAC:
                            try{
                                String macAddress = valueById(formValuesList,id.getId()).toUpperCase();
                                edge2SLVData.setMacAddress(macAddress);
                                edge2SLVData.setPriority(priority);
                            }catch (NoValueException e){
                                e.printStackTrace();
                            }


                            break;
                        case FIXTURE:
                            try{
                                String fixtureQRScan = valueById(formValuesList,id.getId());
                                edge2SLVData.setFixtureQRScan(fixtureQRScan);
                                edge2SLVData.setPriority(priority);
                            }catch (NoValueException e){
                                e.printStackTrace();
                            }

                            break;

                        case IDONCONTROLLER:
                            try{
                                String idOnController = valueById(formValuesList,id.getId());
                                edge2SLVData.setIdOnController(idOnController);
                            }catch (NoValueException e){
                                e.printStackTrace();
                            }
                            break;

                        case EXISTING_MAC:
                            try{
                                String existingMACAddress = valueById(formValuesList,id.getId());
                                edge2SLVData.setExistingMACAddress(existingMACAddress);
                            }catch (NoValueException e){
                                e.printStackTrace();
                            }
                            break;

                        case FIXTURE_TYPE:
                            try {
                                String fixtureType = valueById(formValuesList, id.getId());
                                edge2SLVData.setFixtureType(fixtureType);
                                if(priority.getType() == SLVProcess.OPERATION) {
                                    edge2SLVData.setPriority(priority);
                                }
                            }
                            catch (NoValueException e){
                                e.printStackTrace();
                            }
                            break;
                        case LAMP_TYPE:
                            try {
                                String lampType = valueById(formValuesList, id.getId());
                                edge2SLVData.setLampType(lampType);
                                if(priority.getType() == SLVProcess.OPERATION) {
                                    edge2SLVData.setPriority(priority);
                                }
                            }
                            catch (NoValueException e){
                                e.printStackTrace();
                            }
                            break;
                        case LAMP_TYPE_OTHER:
                            try {
                                String lampTypeOther = valueById(formValuesList, id.getId());
                                edge2SLVData.setLampTypeOther(lampTypeOther);
                                if(priority.getType() == SLVProcess.OPERATION) {
                                    edge2SLVData.setPriority(priority);
                                }
                            }
                            catch (NoValueException e){
                                e.printStackTrace();
                            }
                            break;
                        case WATTAGE:
                            try {
                                String wattage = valueById(formValuesList, id.getId());
                                edge2SLVData.setWattage(wattage);
                                if(priority.getType() == SLVProcess.OPERATION) {
                                    edge2SLVData.setPriority(priority);
                                }
                            }
                            catch (NoValueException e){
                                e.printStackTrace();
                            }
                            break;
                        case WATTAGE_OTHER:
                            try {
                                String wattageOther = valueById(formValuesList, id.getId());
                                edge2SLVData.setWattageOther(wattageOther);
                                if(priority.getType() == SLVProcess.OPERATION) {
                                    edge2SLVData.setPriority(priority);
                                }
                            }
                            catch (NoValueException e){
                                e.printStackTrace();
                            }
                            break;
                        case SUPPLY_TYPE:
                            try {
                                String supplyType = valueById(formValuesList, id.getId());
                                edge2SLVData.setSupplyType(supplyType);
                                if(priority.getType() == SLVProcess.OPERATION) {
                                    edge2SLVData.setPriority(priority);
                                }
                            }
                            catch (NoValueException e){
                                e.printStackTrace();
                            }
                            break;

                        case PART_NUMBER:
                            try {
                                String partNumber = valueById(formValuesList, id.getId());
                                edge2SLVData.setLuminairePartNumber(partNumber);

                            }
                            catch (NoValueException e){
                                e.printStackTrace();
                            }
                            break;
                        case SERIAL_NUMBER:
                            try {
                                String serialNumber = valueById(formValuesList, id.getId());
                                edge2SLVData.setLuminaireSerialNumber(serialNumber);

                            }
                            catch (NoValueException e){
                                e.printStackTrace();
                            }
                            break;
                        case LUMINAIRE_MODEL:
                            try{
                                String luminaireModel = valueById(formValuesList,id.getId());
                                edge2SLVData.setLuminaireModel(luminaireModel);
                            }catch (NoValueException e){
                                e.printStackTrace();
                            }
                            break;

                        case POLE_MATERIAL:
                            try{
                                String poleMaterial = valueById(formValuesList,id.getId());
                                edge2SLVData.setPoleMaterial(poleMaterial);
                            }catch (NoValueException e){
                                e.printStackTrace();
                            }
                            break;
                        case POLE_STATUS:
                            try{
                                String poleStatus = valueById(formValuesList,id.getId());
                                edge2SLVData.setPoleStatus(poleStatus);
                            }catch (NoValueException e){
                                e.printStackTrace();
                            }
                            break;

                    }
                }

                if(edge2SLVData.getPriority() != null){
                    return;
                }
            }
        }
    }
}
