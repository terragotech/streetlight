package com.slvinterface.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

public class SLVInterfaceService {

    private static final Logger logger = Logger.getLogger(SLVInterfaceService.class);

    EdgeRestService edgeRestService = null;
    JsonParser jsonParser = null;
    QueryExecutor queryExecutor = null;
    Gson gson = null;
    ConditionsJson conditionsJson = null;
    Properties properties = null;
    SLVRestService slvRestService = null;

    public static int retryCount = 0;


    public SLVInterfaceService() throws Exception {
        edgeRestService = new EdgeRestService();
        jsonParser = new JsonParser();
        queryExecutor = new QueryExecutor();
        gson = new Gson();
        slvRestService = new SLVRestService();
        this.properties = PropertiesReader.getProperties();
    }

    public void run() {
        String accessToken = edgeRestService.getEdgeToken();
        if (accessToken == null) {
            logger.error("Edge Invalid UserName and Password.");
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
            String notesGuids = edgeSlvServerResponse.getBody();
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
                return;
            }
        } catch (Exception e) {
            logger.error("Error in DB",e);
            throw new DatabaseException(e);
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
                logger.info("Response from edge.");
                String notesData = responseEntity.getBody();
                logger.info("----------------Edge Note Data--------------");
                logger.info(notesData);
                logger.info("----------------Edge Note Data End--------------");
                processNoteData(notesData,slvSyncTable);
            } else {
                logger.error("Unable to Get Note Details from Edge Server and status code is:"+responseEntity.getStatusCode());
                slvSyncTable.setErrorDetails("Unable to Get Note Details from Edge Server and status code is:"+responseEntity.getStatusCode());
            }

        }catch (SLVConnectionException e){
            logger.error("Error in SLVConnectionException",e);
            slvSyncTable.setStatus("Failure");
            slvSyncTable.setErrorDetails(e.getMessage());
            throw new SLVConnectionException(e);
        }catch (Exception e) {
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
            logger.error("Error in checkTokenValidity",e);
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
                RestTemplate.INSTANCE.reConnect();
                checkTokenValidity(edge2SLVData);
            }catch (Exception e1){
                throw new SLVConnectionException("Unable to connect with SLV.",e);
            }

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
            DeviceEntity deviceEntity = new DeviceEntity();
            loadDeviceValues(edgeNote.getTitle(),deviceEntity);
            processFormData(formDataList,slvSyncTable,deviceEntity);
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
        slvSyncTable.setIdOnController(edgeNote.getTitle());
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

    public void loadDeviceValues(String idOnController, DeviceEntity deviceEntity) throws NoValueException,SLVUnAuthorizeException, IOException, ClientProtocolException,Exception {
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
        HttpResponse response = slvRestService.callGetMethod(url);
        if (response.getStatusLine().getStatusCode() == 200) {
            String responseString = slvRestService.getResponseBody(response);
            logger.info("LoadDevice Respose :" + responseString);
            if(responseString != null){
                int id = processDeviceJson(responseString);
                deviceEntity.setDeviceId(id);
                logger.info("LoadDevice Id :" + id);

                if (id == 0) {
                    throw new NoValueException("Device id:[" + idOnController + "] does not exists in SLV server");
                } else {
                    String dimmingGroupName = getSLVDimmingGroupName(id);
                    deviceEntity.setDimmingGroup(dimmingGroupName);
                }
            }

        }else if(response.getStatusLine().getStatusCode() == 403){
            String responseString = slvRestService.getResponseBody(response);
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
            String mainUrl = properties.getProperty("streetlight.slv.base.url");
            String updateDeviceValues = properties.getProperty("streetlight.slv.url.updatedevice");
            String url = mainUrl + updateDeviceValues;

            paramsList.add("ser=json");
            String params = StringUtils.join(paramsList, "&");
            url = url + "&" + params;
            logger.info("SetDevice method called");
            logger.info("SetDevice url:" + url);
            setSLVTransactionLogs(slvTransactionLogs, url, CallType.SET_DEVICE);
            HttpResponse response = slvRestService.callGetMethod(url);
            String responseString =  slvRestService.getResponseBody(response);
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


    public void processFormData(List<FormData> formDataList, SLVSyncTable slvSyncTable,DeviceEntity deviceEntity)throws SLVConnectionException{
        logger.info("Processing form value.");
        Edge2SLVData previousEdge2SLVData = null;
        for(FormData formData : formDataList){
            Edge2SLVData currentEdge2SLVData = new Edge2SLVData();
            processFormData(formData,currentEdge2SLVData);
            logger.info("Current Edge2SLVData:"+currentEdge2SLVData.toString());

            if(currentEdge2SLVData.getCalendar() != null){
                if(previousEdge2SLVData == null){
                    previousEdge2SLVData = currentEdge2SLVData;
                }else{
                    logger.info("Current Edge2SLVData:"+currentEdge2SLVData.toString());
                    logger.info("Previous Edge2SLVData:"+currentEdge2SLVData.toString());
                    if(currentEdge2SLVData.getCalendar().equals(previousEdge2SLVData.getCalendar())){
                        previousEdge2SLVData = currentEdge2SLVData;
                    }
                }
            }
        }

        if(previousEdge2SLVData.getCalendar() == null){
            slvSyncTable.setStatus("Failure");
            slvSyncTable.setErrorDetails("Calendar Value is not present for this note.");
            return;
        }else{
            if(deviceEntity.getDimmingGroup() == null || !previousEdge2SLVData.getCalendar().equals(deviceEntity.getDimmingGroup())){
                List<EdgeAllMac> edgeAllMacList =  queryExecutor.getEdgeAllCalendar(previousEdge2SLVData.getTitle(),previousEdge2SLVData.getCalendar());
                if(edgeAllMacList.size() > 0){
                    slvSyncTable.setStatus("Failure");
                    slvSyncTable.setErrorDetails("Calendar value already present in our local table.");
                }else{
                    slvSync(slvSyncTable,previousEdge2SLVData);
                }
            }else{
                slvSyncTable.setStatus("Failure");
                slvSyncTable.setErrorDetails("SLV Calendar matches with Edge.");
            }
        }

    }


    protected void addStreetLightData(String key, String value, List<Object> paramsList) {
        paramsList.add("valueName=" + key.trim());
        try {
            value =  URLEncoder.encode(value,"UTF-8");
            paramsList.add("value=" + value.trim());
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void loadVal( List<Object> paramsList,Edge2SLVData previousEdge2SLVData){
        paramsList.add("idOnController=" + previousEdge2SLVData.getIdOnController());
        paramsList.add("controllerStrId="+previousEdge2SLVData.getControllerStrId());
    }




    /**
     * Populate EdgeValue to Edge2SLVData from FormValue based on Configuration JSON
     * @param formData
     * @param edge2SLVData
     */
    public void processFormData(FormData formData,Edge2SLVData edge2SLVData){
        try{
            List<FormValues> formValuesList = formData.getFormDef();
            String calendarFormID = properties.getProperty("edge.calendar.form.id");
            String calendar = valueById(formValuesList,Integer.valueOf(calendarFormID));
            edge2SLVData.setCalendar(calendar);
        }catch (Exception e){
            logger.error("Error in processFormData",e);
        }

    }




    public String getSLVDimmingGroupName(int deviceId)throws Exception{
        try{
            if (deviceId > 0) {
                String mainUrl = properties.getProperty("streetlight.slv.url.main");
                String commentUrl = properties.getProperty("streetlight.slv.url.comment.get");
                String url = mainUrl + commentUrl;
                List<String> paramsList = new ArrayList<>();
                paramsList.add("returnTimeAges=false");
                paramsList.add("param0=" + deviceId);
                paramsList.add("valueName=DimmingGroupName");
                paramsList.add("ser=json");
                String params = StringUtils.join(paramsList, "&");
                url = url + "?" + params;
                logger.info("Get Install Date url :" + url);
                HttpResponse response = slvRestService.callGetMethod(url);
                if (response.getStatusLine().getStatusCode() == 200) {
                    String responseString =  slvRestService.getResponseBody(response);
                    JsonArray jsonArray = (JsonArray) jsonParser.parse(responseString);
                    for (JsonElement installDateJsonArray : jsonArray) {
                        JsonObject installDateJson = installDateJsonArray.getAsJsonObject();
                        if (installDateJson.get("name") != null) {
                            String paramName = installDateJson.get("name").getAsString();
                            if (paramName.equals("DimmingGroupName")) {
                                if (installDateJson.get("value") != null) {
                                    return installDateJson.get("value").getAsString();

                                }
                            }
                        }

                    }
                }else{
                    throw  new Exception("Not able to get Data.");
                }


            }
        }catch (Exception e){
            throw  new Exception("Not able to get Data.");
        }
        return null;
    }



    /**
     * Send Value to SLV.
     * @param slvSyncTable
     * @param previousEdge2SLVData
     */
    private void slvSync(SLVSyncTable slvSyncTable,Edge2SLVData previousEdge2SLVData) {
        SLVTransactionLogs slvTransactionLogs = getSLVTransVal(slvSyncTable);
        List<Object> paramsList = new ArrayList<>();
        loadVal(paramsList,previousEdge2SLVData);
        addStreetLightData("DimmingGroupName",previousEdge2SLVData.getCalendar(),paramsList);
        int errorCode = setDeviceValues(paramsList,slvTransactionLogs);
        if(errorCode != 0){
            slvSyncTable.setStatus("Failure");
            slvSyncTable.setErrorDetails("Check Response Log");
        }else{
            slvSyncTable.setStatus("Success");
        }
    }

}
