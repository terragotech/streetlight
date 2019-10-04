package com.slvinterface.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.slvinterface.dao.QueryExecutor;
import com.slvinterface.entity.DeviceEntity;
import com.slvinterface.entity.EdgeAllMac;
import com.slvinterface.entity.SLVSyncTable;
import com.slvinterface.entity.SLVTransactionLogs;
import com.slvinterface.enumeration.CallType;
import com.slvinterface.exception.*;
import com.slvinterface.json.*;
import com.slvinterface.utils.PropertiesReader;
import com.slvinterface.utils.ResourceDetails;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.wololo.geojson.Feature;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URLEncoder;
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

    public static int retryCount = 0;


    SLVInterfaceService() throws Exception {
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
//        Long lastSyncTime =   1570078800000L;
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
                processNoteData(notesData,slvSyncTable);
            } else {
                slvSyncTable.setErrorDetails("Unable to Get Note Details from Edge Server and status code is:"+responseEntity.getStatusCode());
            }

        }catch (SLVConnectionException e){
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

    public void checkTokenValidity(EdgeNote edgeNote,Edge2SLVData edge2SLVData)throws SLVConnectionException{
        try{
            logger.info("Checking Token Validity.");
            logger.info("Current RetryCount:"+retryCount);
            DeviceEntity deviceEntity = new DeviceEntity();
            loadDeviceValues(edgeNote,edge2SLVData);
            return;
        }catch (NoValueException e){
            logger.error("Error in NoValueException: ", e);
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
                checkTokenValidity(edgeNote,edge2SLVData);
            }catch (Exception e1){
                logger.error("Error in SLVConnectionException: ", e);
                throw new SLVConnectionException("Unable to connect with SLV.",e);
            }

        }
    }

    private void processNoteData(String notesData, SLVSyncTable slvSyncTable)throws SLVConnectionException {
        try {
            EdgeNote edgeNote = gson.fromJson(notesData, EdgeNote.class);
            if(!edgeNote.getCreatedBy().equals("admin") && !edgeNote.getCreatedBy().equals("slvinterface")) {
                populateSLVSyncTable(edgeNote, slvSyncTable);
                List<FormData> formDataList = getFormDataList(edgeNote);
                if (formDataList.size() < 1) {
                    slvSyncTable.setErrorDetails("Form Template is not present.");
                    slvSyncTable.setStatus("Failure");
                    logger.info("Form Template is not present.");
                    return;
                }

                processFormData(formDataList, slvSyncTable, edgeNote);
            }
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
        HttpResponse response = slvRestService.callGetMethod(url);
        if (response.getStatusLine().getStatusCode() == 200) {
            String responseString = slvRestService.getResponseBody(response);
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

    public void loadDeviceValues(EdgeNote edgeNote,Edge2SLVData edge2SLVData) throws NoValueException,SLVUnAuthorizeException, IOException, ClientProtocolException {
        logger.info("loadDeviceValues called.");
        String idOnController = URLEncoder.encode(edgeNote.getTitle(),"UTF-8");
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
                logger.info("LoadDevice Id :" + id);
                if (id == 0) {
                    logger.info("Device not present.calling create device");
                    createDevice(edgeNote,edge2SLVData);
                    logger.info("csl and context hashmap are cleared");
                } else {
                    String subDeviceUrl = getDeviceUrl(id);
                    logger.info("subDevice url:" + subDeviceUrl);
                    HttpResponse httpResponse = slvRestService.callGetMethod(subDeviceUrl);
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        String deviceResponse = slvRestService.getResponseBody(httpResponse);
                        if(deviceResponse != null){
                           // processDeviceValuesJson(deviceResponse, idOnController, deviceEntity);
                        }
                    }
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

    /**
     * Calls ReplaceOLCs
     *
     * @throws ReplaceOLCFailedException
     */
    public void replaceOLC(String controllerStrIdValue, String idOnController, String macAddress,SLVSyncTable slvSyncTable)
            throws ReplaceOLCFailedException {
        SLVTransactionLogs slvTransactionLogs = getSLVTransVal(slvSyncTable);
        try {
            String newNetworkId = macAddress;

            // Get Url detail from properties
            String mainUrl = properties.getProperty("streetlight.slv.base.url");
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
            logger.info("Replace OLC url: "+url);
            setSLVTransactionLogs(slvTransactionLogs, url, CallType.REPLACE_OLC);
            HttpResponse response = slvRestService.callGetMethod(url);
            String responseString =  slvRestService.getResponseBody(response);
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

        } catch (Exception e) {
            logger.error("Error in replaceOLC", e);
            throw new ReplaceOLCFailedException(e.getMessage());
        } finally {
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

    public String getSlvDateFormat(String date,String format){
        try{
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            Date date1 = simpleDateFormat.parse(date);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.format(date1);
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    private String checkValue(List<FormValues> formValuesList, int id) throws NoValueException {
        String result = "";
        try {
            result = valueById(formValuesList, id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }
    /**
     * Populate EdgeValue to Edge2SLVData from FormValue based on Configuration JSON
     * @param formData
     * @param edge2SLVData
     */
    public void processFormData(FormData formData,Edge2SLVData edge2SLVData,EdgeNote edgeNote){
        List<FormValues> formValuesList = formData.getFormDef();
        List<Priority> priorities = conditionsJson.getPriority();
        List<Config> configList = conditionsJson.getConfigList();
        String installStatus = "";
        try {
            installStatus = valueById(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.install.status.id")));
        }catch (Exception e){
            e.printStackTrace();
        }

        String installdate = "";
        try {
            installdate = valueById(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.install.date.id")));
        }catch (Exception e){
            e.printStackTrace();
        }
        if(!installdate.equals("")){
            edge2SLVData.setInstallDate(dateFormat(Long.valueOf(installdate)));
        }
        edge2SLVData.setInstallStatus(installStatus);

        try{
            String premiseNodeLocation = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.premiseNodeLocation.id")));
            edge2SLVData.setPremiseNodeLocation(premiseNodeLocation);
            String poleNo = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.poleNo.id")));
            edge2SLVData.setPoleNo(poleNo);
            String StreetAddress = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.StreetAddress.id")));
            edge2SLVData.setStreetAddress(StreetAddress);
            String LLCGrid = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.LLCGrid.id")));
            edge2SLVData.setLLCGrid(LLCGrid);
            String fixtureOwnerShipCode = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.fixtureOwnerShipCode.id")));
            edge2SLVData.setFixtureOwnerShipCode(fixtureOwnerShipCode);
            String fixturecompatibleUnit = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.fixturecompatibleUnit.id")));
            edge2SLVData.setFixturecompatibleUnit(fixturecompatibleUnit);
            String armCompatibleUnit = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.armCompatibleUnit.id")));
            edge2SLVData.setArmCompatibleUnit(armCompatibleUnit);
            String supplyType = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.supplyType.id")));
            edge2SLVData.setSupplyType(supplyType);
            String fixtureStyle = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.fixtureStyle.id")));
            edge2SLVData.setFixtureStyle(fixtureStyle);
            String lightInstallationDate = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.lightInstallationDate.id")));
            /*if(!lightInstallationDate.equals(""))
            {
                lightInstallationDate = dateFormat(Long.valueOf(lightInstallationDate));
            }*/
            edge2SLVData.setLightInstallationDate(lightInstallationDate);
            String armSize = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.armSize.id")));
            edge2SLVData.setArmSize(armSize);
            String armType = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.armType.id")));
            edge2SLVData.setArmType(armType);
            String lightLocationType = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.lightLocationType.id")));
            edge2SLVData.setLightLocationType(lightLocationType);
            Feature feature = (Feature) GeoJSONFactory.create(edgeNote.getGeometry());
            GeoJSONReader reader = new GeoJSONReader();
            Geometry geom = reader.read(feature.getGeometry());
            String latitude = String.valueOf(geom.getCoordinate().y);
            String longitude = String.valueOf(geom.getCoordinate().x);
            edge2SLVData.setLatitude(latitude);
            edge2SLVData.setLongitude(longitude);
            String associatedTransformer = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.associatedTransformer.id")));
            edge2SLVData.setAssociatedTransformer(associatedTransformer);
            String llcVoltage= checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.llcVoltage.id")));
            edge2SLVData.setLlcVoltage(llcVoltage);
            String shade = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.shade.id")));
            edge2SLVData.setShade(shade);
            String height = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.height.id")));
            edge2SLVData.setHeight(height);
            String poleInstallationDate = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.poleInstallationDate.id")));
            /*if(!poleInstallationDate.equals(""))
            {
                poleInstallationDate = dateFormat(Long.valueOf(poleInstallationDate));
            }*/
            edge2SLVData.setPoleInstallationDate(poleInstallationDate);
            String poleColor = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.poleColor.id")));
            edge2SLVData.setPoleColor(poleColor);
            String material = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.material.id")));
            edge2SLVData.setMaterial(material);

            String slopShroud = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.slopShroud.id")));
            edge2SLVData.setSlopShroud(slopShroud);
            String poleOwnershipCode = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.poleOwnershipCode.id")));
            edge2SLVData.setPoleOwnershipCode(poleOwnershipCode);

            String fixtureWattage = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.fixtureWattage.id")));
            edge2SLVData.setFixtureWattage(fixtureWattage);
            String lampType= checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.lampType.id")));
            edge2SLVData.setLampType(lampType);
            String fixtureType = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.fixtureType.id")));
            edge2SLVData.setFixtureType(fixtureType);
            String fixtureColor = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.fixtureColor.id")));
            edge2SLVData.setFixtureColor(fixtureColor);
            String installComments = checkValue(formValuesList, Integer.valueOf(properties.getProperty("streetlight.edge.installComments.id")));
            edge2SLVData.setInstallComments(installComments);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
                                if(id.getId() == 93){// Remove
                                    edge2SLVData.setPriority(priority);
                                }
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


    public HttpResponse createDevice(EdgeNote edgenote,Edge2SLVData edge2SLVData) {
        try {
            logger.info("Create device in slv");
            Feature feature = (Feature) GeoJSONFactory.create(edgenote.getGeometry());

            // parse Geometry from Feature
            GeoJSONReader reader = new GeoJSONReader();
            Geometry geom = reader.read(feature.getGeometry());
            String idoncontroller = "";
            try {
                idoncontroller = URLEncoder.encode(edgenote.getTitle(), "UTF-8");
            } catch (Exception e) {
                logger.error("Error while encode idoncontroller:", e);
            }
            if (idoncontroller.equals("")) {
                return null;
            }

            String mainUrl = properties.getProperty("streetlight.slv.base.url");
            String serveletApiUrl = properties.getProperty("streetlight.slv.url.device.create");
            String url = mainUrl + serveletApiUrl;
            String methodName = properties.getProperty("streetlight.slv.device.create.methodName");
            String categoryStrId = properties.getProperty("streetlight.categorystr.id");
            String controllerStrId = edge2SLVData.getControllerStrId();
            String nodeTypeStrId = properties.getProperty("streetlight.slv.equipment.type");
            Map<String, String> streetLightDataParams = new HashMap<String, String>();
            streetLightDataParams.put("methodName", methodName);
            streetLightDataParams.put("categoryStrId", URLEncoder.encode(categoryStrId,"UTF-8"));
            streetLightDataParams.put("controllerStrId", URLEncoder.encode(controllerStrId,"UTF-8"));
            streetLightDataParams.put("idOnController", idoncontroller);
            streetLightDataParams.put("userName", idoncontroller);
            streetLightDataParams.put("geoZoneId", properties.getProperty("streetlight.root.geozone"));
            streetLightDataParams.put("lng", String.valueOf(geom.getCoordinate().x));
            streetLightDataParams.put("lat", String.valueOf(geom.getCoordinate().y));
            streetLightDataParams.put("nodeTypeStrId", URLEncoder.encode(nodeTypeStrId,"UTF-8"));
            streetLightDataParams.put("ser", "json");
            // streetLightDataParams.put("modelFunctionId", nodeTypeStrId);
            // modelFunctionId

            Set<String> keys = streetLightDataParams.keySet();
            List<String> values = new ArrayList<String>();
            for (String key : keys) {
                String val = streetLightDataParams.get(key) != null ? streetLightDataParams.get(key).toString() : "";
                String tem = key + "=" + val;
                values.add(tem);
            }
            String params = StringUtils.join(values, "&");
            url = url + "?" + params;

            return slvRestService.callGetMethod(url);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}