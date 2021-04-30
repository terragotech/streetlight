package com.terragoedge.streetlight.service;

import com.google.gson.*;
import com.terragoedge.edgeserver.*;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.Utils;
import com.terragoedge.streetlight.dao.ConnectionDAO;
import com.terragoedge.streetlight.dao.StreetlightDao;
import com.terragoedge.streetlight.enumeration.CallType;
import com.terragoedge.streetlight.enumeration.InstallStatus;
import com.terragoedge.streetlight.enumeration.ProcessType;
import com.terragoedge.streetlight.exception.*;
import com.terragoedge.streetlight.json.model.*;
import com.terragoedge.streetlight.logging.InstallMaintenanceLogModel;
import com.terragoedge.streetlight.logging.LoggingModel;
import com.terragoedge.streetlight.service.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractProcessor {

    final Logger logger = Logger.getLogger(AbstractProcessor.class);

    protected StreetlightDao streetlightDao = null;
    protected  RestService restService = null;
    protected Properties properties = null;
    protected  Gson gson = null;
    protected  JsonParser jsonParser = null;
    protected ConnectionDAO connectionDAO;
    protected SLV2EdgeService slv2EdgeService;

   protected WeakHashMap<String, String> contextListHashMap = new WeakHashMap<>();
    protected  HashMap<String, SLVDates> cslpDateHashMap = new HashMap<>();
    HashMap<String, String> macHashMap = new HashMap<>();
    protected String droppedPinTag;

    protected SLVDates edgeNoteCreatedDateTime = null;
    protected String noteCreatedDateTime = null;

    ExecutorService promotedSLVMACAddressExecutor = Executors.newCachedThreadPool();

    public AbstractProcessor() {
        this.connectionDAO = ConnectionDAO.INSTANCE;
        this.streetlightDao = new StreetlightDao();
        this.restService = new RestService();
        this.properties = PropertiesReader.getProperties();
        this.gson = new Gson();
        this.jsonParser = new JsonParser();
        this.slv2EdgeService = new SLV2EdgeService();
        droppedPinTag = properties.getProperty("com.droppedpin.tag");
    }

    protected String value(List<EdgeFormData> edgeFormDatas, String key) throws NoValueException {
        EdgeFormData edgeFormTemp = new EdgeFormData();
        edgeFormTemp.setLabel(key);

        int pos = edgeFormDatas.indexOf(edgeFormTemp);
        if (pos != -1) {
            EdgeFormData edgeFormData = edgeFormDatas.get(pos);
            String value = edgeFormData.getValue();
            if (value == null || value.trim().isEmpty()) {
                throw new NoValueException("Value is Empty or null." + value);
            }
            return value;
        } else {
            throw new NoValueException(key + " is not found.");
        }
    }


    protected String valueById(List<EdgeFormData> edgeFormDatas, int id) throws NoValueException {
        for (EdgeFormData edgeFormData : edgeFormDatas) {
            if (edgeFormData.getId() == id) {
                String value = edgeFormData.getValue();
                if (value == null || value.trim().isEmpty() || value.contains("null") || value.equals("Scan Node MAC#") ) {
                    throw new NoValueException("Value is Empty or null." + value);
                }
                return value;
            }
        }

        throw new NoValueException(id + " is not found.");
    }

    protected String valueFixtureValueById(List<EdgeFormData> edgeFormDatas, int id) {
        for (EdgeFormData edgeFormData : edgeFormDatas) {
            if (edgeFormData.getId() == id) {
                String value = edgeFormData.getValue();
                if (value == null || value.trim().isEmpty() || value.contains("null")) {
                    return null;
                }
                return value;
            }
        }

        return null;
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

    public void processDeviceValuesJson(String deviceValuesjson, String idOnController, String noteGuid,InstallMaintenanceLogModel installMaintenanceLogModel) {
        logger.info("processDeviceValuesJson called start");
        String proposedContextKey = properties.getProperty("streetlight.location.proposedcontext");
        String cslInstallDateKey = properties.getProperty("streetlight.csl.installdate");
        String cslLuminaireDateKey = properties.getProperty("streetlight.csl.luminairedate");
        String offLuxKey = properties.getProperty("com.slv.off.lux.level.getdevice.key");
        String onLuxKey = properties.getProperty("com.slv.on.lux.level.getdevice.key");
        //String serialNumberKey = properties.getProperty("streetlight.luminaire.serialnumber");
        logger.info("contextKey :" + proposedContextKey);
        logger.info("cslInstallDate :" + cslInstallDateKey);
        logger.info("cslLuminaireDate :" + cslLuminaireDateKey);
        JsonObject jsonObject = new JsonParser().parse(deviceValuesjson).getAsJsonObject();
        logger.info("Device request json:" + gson.toJson(jsonObject));
        JsonArray arr = jsonObject.getAsJsonArray("properties");
        contextListHashMap.clear();
        cslpDateHashMap.clear();
        macHashMap.clear();

        SLVDates slvDates = new SLVDates();
        SLVDates slvDatesTemp = new SLVDates();
        installMaintenanceLogModel.getDatesHolder().setSlvDates(slvDates);

        String cslpNodeInstall = null;
        String cslpLuminaireDate = null;
        String nodeInstallDate = null;
        String luminaireInstallDate = null;
        String macAddress = null;
        String luminaireSerialNumber = null;
        String atlasPhysicalPage = null;
        String communicationSatus = null;
        String fixtureCode = null;
        String proposedContextValue = null;
        String atlasGroupValue = null;
        float onLuxValue = 0.0f;
        float offLuxValue = 0.0f;
        String dimmingGroupName = null;
        for (int i = 0; i < arr.size(); i++) {
            JsonObject jsonObject1 = arr.get(i).getAsJsonObject();
            String keyValue = jsonObject1.get("key").getAsString();
            if (keyValue != null && keyValue.equals(proposedContextKey)) {
                String proposedContext = getJsonValue(jsonObject1);
                proposedContextValue = proposedContext;
                contextListHashMap.put(idOnController, proposedContext);
            } else if (keyValue != null && keyValue.equals(cslInstallDateKey)) {
                cslpNodeInstall = getJsonValue(jsonObject1);
            } else if (keyValue != null && keyValue.equals(cslLuminaireDateKey)) {
                cslpLuminaireDate = getJsonValue(jsonObject1);
            } else if (keyValue != null && keyValue.equals("userproperty.MacAddress")) {
                macAddress = getJsonValue(jsonObject1);
                installMaintenanceLogModel.setSlvMacaddress(macAddress);
            }else if (keyValue != null && keyValue.equals("userproperty.luminaire.serialnumber")) {
                luminaireSerialNumber = getJsonValue(jsonObject1);
            }else if (keyValue != null && keyValue.equals("install.date")) {
                nodeInstallDate = getJsonValue(jsonObject1);
            }else if (keyValue != null && keyValue.equals("userproperty.luminaire.installdate")) {
                luminaireInstallDate = getJsonValue(jsonObject1);
            }else if(keyValue != null && keyValue.equals("userproperty.location.atlasphysicalpage")){
                atlasPhysicalPage = getJsonValue(jsonObject1);
            }else if(keyValue != null && keyValue.equals("CommunicationStatus")){
                communicationSatus = getJsonValue(jsonObject1);
            }else if(keyValue != null && keyValue.equals("userproperty.luminaire.fixturecode")){
                fixtureCode = getJsonValue(jsonObject1);
            }else if(keyValue != null && keyValue.equals("userProperty.network.atlasgroup")){
                atlasGroupValue = getJsonValue(jsonObject1);
            } else if(keyValue != null && keyValue.equals(onLuxKey)){
                if (jsonObject1.has("value") && !jsonObject1.get("value").isJsonNull()){
                    onLuxValue = jsonObject1.get("value").getAsFloat();
                }
            } else if(keyValue != null && keyValue.equals(offLuxKey)){
                if (jsonObject1.has("value") && !jsonObject1.get("value").isJsonNull()){
                    offLuxValue = jsonObject1.get("value").getAsFloat();
                }
            }else if(keyValue != null && keyValue.equals("userproperty.DimmingGroupName")){
                dimmingGroupName = getJsonValue(jsonObject1);
            }else if(keyValue != null && keyValue.equals("userproperty.luminaire.model")){
                String luminaireModel = getJsonValue(jsonObject1);
                if(luminaireModel != null && !luminaireModel.trim().isEmpty()){
                    installMaintenanceLogModel.setLuminaireModel(luminaireModel.trim());
                }
            }
            //userproperty.DimmingGroupName
            //userproperty.location.atlasphysicalpage

        }

        //
        if (cslpNodeInstall != null && !cslpNodeInstall.trim().isEmpty() && cslpNodeInstall.trim().length() > 7) {
            slvDatesTemp.setCslpNodeDate(cslpNodeInstall);
            slvDates.setCslpNodeDate(slvDateFormat(cslpNodeInstall,"CSLP Node Install Date"));
        }
        if (cslpLuminaireDate != null && !cslpLuminaireDate.trim().isEmpty() && cslpLuminaireDate.trim().length() > 7) {
            slvDatesTemp.setCslpLumDate(cslpLuminaireDate);
            slvDates.setCslpLumDate(slvDateFormat(cslpLuminaireDate,"CSLP Lum Install Date"));
        }


        if(nodeInstallDate != null && !nodeInstallDate.trim().isEmpty()){
            slvDates.setNodeInstallDate(slvDateFormat(nodeInstallDate,"Node Install Date"));
        }

        if(luminaireInstallDate != null && !luminaireInstallDate.trim().isEmpty()){
            slvDates.setLumInstallDate(slvDateFormat(luminaireInstallDate,"Lum Install Date"));
        }

        if(luminaireSerialNumber != null && !luminaireSerialNumber.trim().isEmpty() && luminaireSerialNumber.trim().length() > 3){
            installMaintenanceLogModel.setSlvLuminaireSerialNumber(luminaireSerialNumber);
        }

        if(atlasPhysicalPage != null && !atlasPhysicalPage.trim().isEmpty()){
            installMaintenanceLogModel.setAtlasPhysicalPage(atlasPhysicalPage);
        }

        if(communicationSatus != null && !communicationSatus.trim().isEmpty()){
            installMaintenanceLogModel.setCommunicationStatus(communicationSatus);
        }

        if(fixtureCode != null && !fixtureCode.trim().isEmpty()){
            installMaintenanceLogModel.setLuminaireFixturecode(fixtureCode);
        }

        if(proposedContextValue != null && !proposedContextValue.trim().isEmpty()){
            installMaintenanceLogModel.setProposedContext(proposedContextValue);
        }

        if(atlasGroupValue != null && !atlasGroupValue.trim().isEmpty()){
            installMaintenanceLogModel.setAtlasGroup(atlasGroupValue);
        }

        if(dimmingGroupName != null && !dimmingGroupName.trim().isEmpty()){
            installMaintenanceLogModel.setDimmingGroupName(dimmingGroupName);
        }

        installMaintenanceLogModel.setOnLuxLevel(onLuxValue);
        installMaintenanceLogModel.setOffLuxLevel(offLuxValue);
        if(nodeInstallDate == null){
            logger.info("Node Install Date is Empty");
            nodeInstallDate = getSLVInstallDate(installMaintenanceLogModel);
            logger.info("nodeInstallDate value:::::::::"+nodeInstallDate);
            if(nodeInstallDate != null && !nodeInstallDate.trim().isEmpty()){
                slvDates.setNodeInstallDate(slvDateFormat(nodeInstallDate,"Node Install Date"));
            }
        }


        logger.info("SLVDates :" + gson.toJson(slvDates));
        logger.info("SLVDates :" + gson.toJson(slvDatesTemp));
        logger.info("installMaintenanceLogModel: "+gson.toJson(installMaintenanceLogModel));
        cslpDateHashMap.put(idOnController, slvDates);
        cslpDateHashMap.put(idOnController,slvDatesTemp);
        macHashMap.put(idOnController, macAddress);
        DeviceAttributes deviceAttributes = new DeviceAttributes();
        deviceAttributes.setMacAddress(macAddress);
        deviceAttributes.setIdOnController(idOnController);
        deviceAttributes.setEventTime(System.currentTimeMillis());
        deviceAttributes.setNoteGuid(noteGuid);
        connectionDAO.saveDeviceAttributes(deviceAttributes);
        logger.info("processDeviceValuesJson End");
    }


    private String getJsonValue(JsonObject jsonObject1){
        if (jsonObject1.has("value") && !jsonObject1.get("value").isJsonNull()){
            return  jsonObject1.get("value").getAsString();
        }
        return null;
    }



    protected String slvDateFormat(String dateVal,String dateType){
        logger.info(dateType+":"+dateVal);
        if(dateVal.contains("-")){
            try {
                DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
                        .withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("CST")));
                DateTime dt =  fmt.parseDateTime(dateVal);

                return String.valueOf(dt.withTimeAtStartOfDay().getMillis());
            }catch (Exception e){
                logger.error("Error in slvDateFormat",e);
            }

            try {
                DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS")
                        .withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("CST")));
                DateTime dt =  fmt.parseDateTime(dateVal);

                return String.valueOf(dt.withTimeAtStartOfDay().getMillis());
            }catch (Exception e){
                logger.error("Error in slvDateFormat",e);
            }

            try {
                DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd")
                        .withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("CST")));
                DateTime dt =  fmt.parseDateTime(dateVal);

                return String.valueOf(dt.withTimeAtStartOfDay().getMillis());
            }catch (Exception e){
                logger.error("Error in slvDateFormat",e);
            }
        }

        try {
            DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yyyy")
                    .withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("CST")));
            DateTime dt =  fmt.parseDateTime(dateVal);

            return String.valueOf(dt.withTimeAtStartOfDay().getMillis());
        }catch (Exception e){
            logger.error("Error in slvDateFormat",e);
        }
        return null;
    }

    public void createEdgeAllFixture(String title, String fixerQrScanValue) {
        EdgeAllFixtureData edgeAllFixtureData = new EdgeAllFixtureData();
        edgeAllFixtureData.setFixtureQRScan(fixerQrScanValue);
        edgeAllFixtureData.setTitle(title);
        connectionDAO.saveEdgeAllFixture(edgeAllFixtureData);
    }

    public void createEdgeAllMac(String title, String macAddress) {
        EdgeAllMacData edgeAllMacData = new EdgeAllMacData();
        edgeAllMacData.setMacAddress(macAddress);
        edgeAllMacData.setTitle(title);
        connectionDAO.saveEdgeAllMac(edgeAllMacData);
    }

    public void loadDeviceValues(String idOnController, InstallMaintenanceLogModel installMaintenanceLogModel) throws Exception {
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
                    cslpDateHashMap.clear();
                    contextListHashMap.clear();
                    throw new NoValueException("Device id:[" + idOnController + "] does not exists in SLV server");
                } else {
                    installMaintenanceLogModel.setDeviceId(id);
                    String subDeviceUrl = getDeviceUrl(id);
                    logger.info("subDevice url:" + subDeviceUrl);
                    ResponseEntity<String> responseEntity = restService.getRequest(subDeviceUrl, true, null);
                    if (response.getStatusCodeValue() == 200) {
                        String deviceResponse = responseEntity.getBody();
                        processDeviceValuesJson(deviceResponse, idOnController, installMaintenanceLogModel.getProcessedNoteId(),installMaintenanceLogModel);
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

    /**
     * Load Mac address and corresponding IdOnController from SLV Server
     *
     * @throws Exception
     */
    public boolean checkMacAddressExists(String macAddress, String idOnController, String nightRideKey, String nightRideValue, LoggingModel loggingModel, SlvInterfaceLogEntity slvInterfaceLogEntity)
            throws QRCodeAlreadyUsedException, Exception {
        boolean isExistMacAddress = connectionDAO.isExistMacAddress(idOnController, macAddress);
        logger.info("given idoncontroller :" + idOnController);
        logger.info("given macaddress is :" + macAddress);
        if (isExistMacAddress) {
            logger.info("Already mac address exist in local table");
            loggingModel.setMacAddressUsed(true);
            slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
            slvInterfaceLogEntity.setErrordetails("Already mac address exist in local table");
            throw new QRCodeAlreadyUsedException("MacAddress Already present in edge_all_mac", macAddress);
        }
        logger.info("Getting Mac Address from SLV.");
        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String updateDeviceValues = properties.getProperty("streetlight.slv.url.search.device");
        String url = mainUrl + updateDeviceValues;
        List<String> paramsList = new ArrayList<>();
        paramsList.add("attribute=MacAddress");
        paramsList.add("value=" + macAddress);
        paramsList.add("operator=eq-i");
        paramsList.add("recurse=true");
        paramsList.add("ser=json");
        String params = StringUtils.join(paramsList, "&");
        url = url + "?" + params;
        ResponseEntity<String> response = restService.getRequest(url, true, null);
        if (response.getStatusCodeValue() == 200) {
            String responseString = response.getBody();
            logger.info("-------MAC Address----------");
            logger.info(responseString);
            logger.info("-------MAC Address End----------");
            DeviceMacAddress deviceMacAddress = gson.fromJson(responseString, DeviceMacAddress.class);
            List<Value> values = deviceMacAddress.getValue();
            StringBuilder stringBuilder = new StringBuilder();
            logger.info("check mac address exist values:" + values);
            if (values == null || values.size() == 0) {
                loggingModel.setMacAddressUsed(false);
                return false;
            } else {
                boolean isDuplicate = false;
                String slvIdOnController = null;
                for (Value value : values) {
                    if (!value.getIdOnController().equals(idOnController)) {
                        isDuplicate = true;
                    }
                    if (value.getIdOnController().equals(idOnController) && nightRideKey != null) {
                        sendNightRideToSLV(value.getIdOnController(), nightRideKey, nightRideValue, loggingModel);
                    }
                    slvIdOnController = value.getIdOnController();
                    stringBuilder.append(value.getIdOnController());
                    stringBuilder.append("\n");
                }
                logger.info("isduplicate:" + isDuplicate);
                if (isDuplicate) {
                    try {
                        slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
                        slvInterfaceLogEntity.setErrordetails("Already MacAddress Assigned another fixture.");
                        DuplicateMacAddress duplicateMacAddress = new DuplicateMacAddress();
                        duplicateMacAddress.setTitle(idOnController);
                        duplicateMacAddress.setMacaddress(macAddress);
                        duplicateMacAddress.setNoteguid(loggingModel.getProcessedNoteId());
                        duplicateMacAddress.setEventTime(System.currentTimeMillis());
                        duplicateMacAddress.setAssignedTo(slvIdOnController);
                        connectionDAO.saveDuplicateMacAddress(duplicateMacAddress);

                    } catch (Exception e) {
                        logger.error("Error in DuplicateMacAddress", e);
                    }


                    try {
                        DuplicateMACAddressEventLog duplicateMACAddressEventLog = new DuplicateMACAddressEventLog();
                        duplicateMACAddressEventLog.setIdOnController(idOnController);
                        duplicateMACAddressEventLog.setMacaddress(macAddress);
                        duplicateMACAddressEventLog.setDeviceList(stringBuilder.toString());
                        duplicateMACAddressEventLog.setNoteGuid(loggingModel.getProcessedNoteId());
                        duplicateMACAddressEventLog.setEventTime(System.currentTimeMillis());
                        connectionDAO.saveMacAddressEventLog(duplicateMACAddressEventLog);
                    } catch (Exception e) {
                        logger.error("Error in DuplicateMACAddressEventLog", e);
                    }


                }
            }
            loggingModel.setMacAddressUsed(true);
            throw new QRCodeAlreadyUsedException(stringBuilder.toString(), macAddress);
        } else {
            throw new Exception(response.getBody());
        }

    }

    private boolean isLumDatePresent(String idOnContoller) {
        logger.info("IdOnController:" + idOnContoller);
        logger.info("cslpDateHashMap:" + cslpDateHashMap.size());
        SLVDates SLVDates = cslpDateHashMap.get(idOnContoller);
        logger.info("SLVDates:" + SLVDates);
        if (SLVDates != null) {
            String val = SLVDates.getCslpLumDate();
            if (val != null) {
                return true;
            }
        }
        return false;
    }


    public boolean isNodeDatePresent(String idOnContoller) {
        logger.info("isNodePresent Json:" + gson.toJson(cslpDateHashMap));
        SLVDates SLVDates = cslpDateHashMap.get(idOnContoller);
        return SLVDates != null && SLVDates.getCslpNodeDate() != null;
    }


    protected void addOtherParams(EdgeNote edgeNote, LinkedMultiValueMap<String, String> paramsList, String idOnContoller, String utilLocId, boolean isNew, String fixerQrScanValue, String macAddress, InstallMaintenanceLogModel loggingModel) {
        // luminaire.installdate - 2017-09-07 09:47:35
        String installStatus = InstallStatus.Installed.getValue();

        String slvMacAddress = macHashMap.get(idOnContoller);
        if (fixerQrScanValue != null && fixerQrScanValue.trim().length() > 0 && !loggingModel.isFixtureQRSame()) {
            logger.info("Fixture QR scan not empty and set luminare installdate" + dateFormat(edgeNote.getCreatedDateTime()));
            logger.info("Fixture QR scan not empty and set cslp.lum.install.date" + dateFormat(edgeNote.getCreatedDateTime()));
            boolean isLumDate = isLumDatePresent(idOnContoller);
            boolean isButtonPhotoCelll = loggingModel.isButtonPhotoCell();
            boolean isNodeOnly =  loggingModel.isNodeOnly();
            if (!isLumDate && !isButtonPhotoCelll && !isNodeOnly) {
                // If its bulk import, then we need to send only Form Date value
                if(!loggingModel.isBulkImport()){
                    addStreetLightData("cslp.lum.install.date", dateFormat(edgeNote.getCreatedDateTime()), paramsList);
                }

            }
            if (!isButtonPhotoCelll && !isNodeOnly) {
                // If its bulk import, then we need to send only Form Date value
                if(!loggingModel.isBulkImport()){
                    addStreetLightData("luminaire.installdate", dateFormat(edgeNote.getCreatedDateTime()), paramsList);
                }

            }
            // As per Mail Conversion - Re: New Release updated on Test Server (.175) - 4.6.18
            if(fixerQrScanValue.startsWith("Existing") && loggingModel.isActionNew()){
                installStatus = InstallStatus.Node_Only.getValue();
            }

        }

        if (utilLocId != null) {
            addStreetLightData("location.utillocationid", utilLocId, paramsList);
        }


        String dimmingGroupName = contextListHashMap.get(idOnContoller);
        logger.info("DimmingGroupName :" + dimmingGroupName);
        logger.info("edgeNote :" + edgeNote.toString());
        logger.info("edgeNote :" + edgeNote.getEdgeNotebook().toString());
        String edgeNotebookName = edgeNote.getEdgeNotebook().getNotebookName();
        /*if (dimmingGroupName != null && dimmingGroupName.trim().toLowerCase().contains("acorns")) {
            edgeNotebookName = "Acorn Calendar";
        } else {
            edgeNotebookName = "Group Calendar 1";
        }*/
        edgeNotebookName = edgeNotebookName.replace("Residential", "").trim();
        logger.info("ProposedContextName :" + dimmingGroupName);
        logger.info("DimmingGroupName :" + edgeNotebookName);
        if (dimmingGroupName != null) {
            if (dimmingGroupName.startsWith("4") || dimmingGroupName.startsWith("13")) {
                edgeNotebookName = edgeNotebookName + " Acorns";
            }
            if (dimmingGroupName.contains("Node Only") && installStatus != null) {
                //installStatus = InstallStatus.Verified.getValue();

            }
        }

        logger.info("DimmingGroupName After:" + edgeNotebookName);

       /* if (dimmingGroupName != null && dimmingGroupName.trim().toLowerCase().contains("acorns")) {
            edgeNotebookName = edgeNotebookName +" Acorns";
        }*/
        // As per Mail Conversion - Re: New Release updated on Test Server (.175) - 4.6.18
        if (installStatus != null && loggingModel.isActionNew()) {
            addStreetLightData("installStatus", loggingModel.isButtonPhotoCell() ? InstallStatus.Photocell_Only.getValue() : installStatus, paramsList);
        }
        if(loggingModel.isDroppedPinWorkflow() && (loggingModel.getLuminaireFixturecode() != null && loggingModel.getLuminaireFixturecode().equalsIgnoreCase("piggy-back"))){
            addStreetLightData("DimmingGroupName", edgeNotebookName +" Acorns", paramsList);
        }else {
            addStreetLightData("DimmingGroupName", edgeNotebookName, paramsList);
        }
        // addStreetLightData("DimmingGroupName", "Group Calendar 1", paramsList);
        addPower(loggingModel,null,paramsList,null);
    }

    protected void addFixtureQrScanData(String key, String value, List<Object> paramsList) {
        try{
            /* The following code for changing ";" to "."  based on the request of the customer */
            if(value != null)
            {
                if(!value.equals("")) {
                    value = value.replaceAll (";",".");
                    value = value.replaceAll("'","");
                    value = value.replaceAll("[^\\p{ASCII}]", "");
                }
            }
            if(key.equals("power")){
                value = isNumber(value);
                if(value == null){
                    logger.info("Power value is not Present.");
                    return;
                }
            }
            paramsList.add("attribute="+ key.trim());
            //paramsList.add("value=" + URLEncoder.encode(value.trim(), "UTF-8"));
            paramsList.add("value="+ value.trim());
        }catch (Exception e){
            logger.error("Error in addStreetLightData",e);
        }
    }

    public String isNumber(String value){
        if(value != null){
            value = value.trim();
            if(NumberUtils.isParsable(value)){
                logger.info("Is Number:true");
                return value;
            }
        }
        logger.info("Is Number:false");
        return null;
    }

    protected void addStreetLightData(String key, String value, LinkedMultiValueMap<String, String> paramsList) {
        logger.info(key);
       // paramsList.add("value=" + value.trim());
        try{
            /* The following code for changing ";" to "."  based on the request of the customer */
            if(value != null)
            {
                if(!value.equals(""))
                {
                    value = value.replaceAll (";",".");
                    value = value.replaceAll("'","");
                    value = value.replaceAll("[^\\p{ASCII}]", "");
                }
            }
            if(key.equals("power")){
                logger.info("Value:"+value);
                value = isNumber(value);
                if(value == null){
                    logger.info("Power value is not Present.");
                    return;
                }
            }
            paramsList.add("valueName" , key.trim());
            //paramsList.add("value=" + URLEncoder.encode(value.trim(), "UTF-8"));
            paramsList.add("value" , value.trim());
        }catch (Exception e){
            logger.error("Error in addStreetLightData",e);
        }
        if(value != null && edgeNoteCreatedDateTime != null){
            switch (key.trim()){
                case "install.date":
                    edgeNoteCreatedDateTime.setNodeInstallDate(value);
                    break;
                case "cslp.node.install.date":
                    edgeNoteCreatedDateTime.setCslpNodeDate(value);
                    break;
                case "luminaire.installdate":
                    edgeNoteCreatedDateTime.setLumInstallDate(value);
                    break;
                case "cslp.lum.install.date":
                    edgeNoteCreatedDateTime.setCslpLumDate(value);
                    break;

            }
        }




    }


    protected String dateFormat(Long dateTime) {
        Date date = new Date(Long.valueOf(dateTime));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("CST"));
        String dff = dateFormat.format(date);
        return dff;
    }

    protected String promotedDateFormat(Long dateTime) {
        Date date = new Date(Long.valueOf(dateTime));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("CST"));
        String dff = dateFormat.format(date);
        return dff;
    }


    // Philips RoadFocus, RFS-54W16LED3K-T-R2M-UNIV-DMG-PH9-RCD-SP2-GY3,
    // RFM0455, 07/24/17, 54W, 120/277V, 4000K, 8140 Lm, R2M, Gray, Advance,
    // 442100083510, DMG
    public static String replaceCharAt(String s, int pos, char c) {
        return s.substring(0, pos) + c + s.substring(pos + 1).trim();
    }

    public boolean stringContainsNumber(String s) {
        if (s != null && !s.trim().isEmpty()) {
            Pattern p = Pattern.compile("[0-9]");
            Matcher m = p.matcher(s);

            return m.find();
        }
        return false;
    }

    private void addPower(LoggingModel loggingModel, String powerVal, LinkedMultiValueMap<String, String> paramsList,ProContextLookupData proContextLookupData) {
        logger.info("Start of addPower");
        logger.info("Is Power Added:"+loggingModel.isPowerAdded());
        logger.info("Is Dropped Pin Workflow:"+loggingModel.isDroppedPinWorkflow());
        logger.info("Is Node Only:"+loggingModel.isNodeOnly());
        logger.info("Is Button Photocell:"+loggingModel.isButtonPhotoCell());
        if(!loggingModel.isPowerAdded()){
            logger.info("Wattage not Yet Added.");
            if (loggingModel.isDroppedPinWorkflow() && (loggingModel.isNodeOnly() || loggingModel.isButtonPhotoCell())){
                logger.info("Current Fixture is Dropped Pin and Either Node only or Button PhotoCell.");
                logger.info("Fixture Code: "+loggingModel.getLuminaireFixturecode());
                logger.info("Is Node Only: "+loggingModel.getLuminaireFixturecode());
                logger.info("Is Button PhotoCell: "+loggingModel.getLuminaireFixturecode());
                if(loggingModel.getLuminaireFixturecode() != null){
                    if(loggingModel.isNodeOnly() && !loggingModel.isButtonPhotoCell() && loggingModel.getLuminaireFixturecode().toUpperCase().contains("COBRAHEAD")){
                        addStreetLightData("power", "125", paramsList);
                        if(proContextLookupData != null){
                            proContextLookupData.setLumWattage("125");
                        }

                        loggingModel.setPowerAdded(true);
                        return;
                    }else if(loggingModel.isButtonPhotoCell() && loggingModel.getLuminaireFixturecode().toUpperCase().contains("PIGGY")){
                        addStreetLightData("power", "50", paramsList);
                        if(proContextLookupData != null){
                            proContextLookupData.setLumWattage("50");
                        }

                        loggingModel.setPowerAdded(true);
                        return;
                    }
                }
            }
            if(powerVal != null){
                logger.info("Default Power Value is added.");
                logger.info("Power Val:"+powerVal);
                addStreetLightData("power", powerVal, paramsList);
                if(proContextLookupData != null){
                    proContextLookupData.setLumWattage(powerVal);
                }
                loggingModel.setPowerAdded(true);
            }

        }
        logger.info("End of addPower");

    }

    private void processFixtureQRScan(String data, LinkedMultiValueMap<String, String> paramsList, EdgeNote edgeNote, SlvServerData slvServerData, LoggingModel loggingModel,ProContextLookupData proContextLookupData){
        addStreetLightData("luminaire.brand", "Acuity", paramsList);
        slvServerData.setLuminaireBrand("Acuity");


        addStreetLightData("device.luminaire.partnumber", "MVOLT WFL 30K TM GYSDP10KVMP PER7 NL RFD275482", paramsList);
        slvServerData.setLuminairePartNumber("MVOLT WFL 30K TM GYSDP10KVMP PER7 NL RFD275482");

        addStreetLightData("luminaire.model", "ACP0LED PK3", paramsList);
        slvServerData.setLuminaireModel("ACP0LED PK3");

        addStreetLightData("device.luminaire.manufacturedate", "01/28/2019", paramsList);
        slvServerData.setLuminaireManufacturedate("01/28/2019");

        addPower(loggingModel,"119",paramsList,proContextLookupData);

        addStreetLightData("luminaire.type", "LED", paramsList);


        addStreetLightData("device.luminaire.colortemp", "3000K", paramsList);
        slvServerData.setLuminaireColorTemp("3000K");

    }


    private void processExistingFixtureQRScan(String data,LinkedMultiValueMap<String, String> paramsList,SlvServerData slvServerData,InstallMaintenanceLogModel loggingModel,ProContextLookupData proContextLookupData){
        if(data != null && !data.trim().isEmpty()){
            String[] fixtureQrScan =  parseFixtureQRScan(data);
            String lumBrand =  addExistingFixtureQRScan(fixtureQrScan,paramsList,0,"Existing LED","luminaire.brand",loggingModel,proContextLookupData);
            if(lumBrand != null){
                slvServerData.setLuminaireBrand(lumBrand);
            }
            String lumPartNum =  addExistingFixtureQRScan(fixtureQrScan,paramsList,1,"Node Only","device.luminaire.partnumber",loggingModel,proContextLookupData);
            if(lumPartNum != null){
                slvServerData.setLuminairePartNumber(lumPartNum);
            }
            String lumModel =  addExistingFixtureQRScan(fixtureQrScan,paramsList,2,"Node Only","luminaire.model",loggingModel,proContextLookupData);
            if(lumModel != null){
                slvServerData.setLuminaireModel(lumModel);
            }

            // Fix: ES-257
            /*
            String lumManu =  addExistingFixtureQRScan(fixtureQrScan,paramsList,3,"Node Only","device.luminaire.manufacturedate");
            if(lumManu != null){
                slvServerData.setLuminaireManufacturedate(lumBrand);
            }*/
             addExistingFixtureQRScan(fixtureQrScan,paramsList,4,"Unknown","power",loggingModel,proContextLookupData);

             addExistingFixtureQRScan(fixtureQrScan,paramsList,5,"LED","luminaire.type",loggingModel,proContextLookupData);

            String lumColor =  addExistingFixtureQRScan(fixtureQrScan,paramsList,6,"Node Only","device.luminaire.colortemp",loggingModel,proContextLookupData);
            if(lumColor != null){
                slvServerData.setLuminaireColorTemp(lumColor);
            }
            String lumLumen =  addExistingFixtureQRScan(fixtureQrScan,paramsList,7,"Node Only","device.luminaire.lumenoutput",loggingModel,proContextLookupData);
            if(lumLumen != null){
                slvServerData.setLumenOutput(lumLumen);
            }
            String lumDist =  addExistingFixtureQRScan(fixtureQrScan,paramsList,8,"Node Only","luminaire.DistributionType",loggingModel,proContextLookupData);
            if(lumDist != null){
                slvServerData.setDistributionType(lumDist);
            }
            String lumColorCode = addExistingFixtureQRScan(fixtureQrScan,paramsList,9,"","luminaire.colorcode",loggingModel,proContextLookupData);
            if(lumColorCode != null){
                slvServerData.setColorCode(lumColorCode);
            }
            String lumDriverManu = addExistingFixtureQRScan(fixtureQrScan,paramsList,10,"","device.luminaire.drivermanufacturer",loggingModel,proContextLookupData);
            if(lumDriverManu != null){
                slvServerData.setDriverManufacturer(lumDriverManu);
            }
            String lumDriverPart =  addExistingFixtureQRScan(fixtureQrScan,paramsList,11,"","device.luminaire.driverpartnumber",loggingModel,proContextLookupData);
            if(lumDriverPart != null){
                slvServerData.setDriverPartNumber(lumDriverPart);
            }
            String dimmingType =  addExistingFixtureQRScan(fixtureQrScan,paramsList,12,"","ballast.dimmingtype",loggingModel,proContextLookupData);
            if(dimmingType != null){
                slvServerData.setDimmingType(dimmingType);
            }

        }

    }


    private String addExistingFixtureQRScan(String[] fixtureQrScan, LinkedMultiValueMap<String, String> paramsList, int pos, String defaultVal, String key,InstallMaintenanceLogModel logModel,ProContextLookupData proContextLookupData) {
        try {
            String fixtureQrScanVal = fixtureQrScan[pos];
            fixtureQrScanVal = (fixtureQrScanVal == null || fixtureQrScanVal.trim().isEmpty()) ? defaultVal : fixtureQrScanVal;
            if(!key.equals("power")){
                addStreetLightData(key, fixtureQrScanVal, paramsList);
            }else{
                addPower(logModel,fixtureQrScanVal,paramsList,proContextLookupData);
            }

            return fixtureQrScanVal;
        } catch (ArrayIndexOutOfBoundsException e) {

        }

        if(!key.equals("power")){
            addStreetLightData(key, defaultVal, paramsList);
        }else{
            addPower(logModel,defaultVal,paramsList,proContextLookupData);
        }
        return null;
    }

    private void addProContextLookupData(ProContextLookupData proContextLookupData,SlvServerData slvServerData,LinkedMultiValueMap<String, String> paramsList,InstallMaintenanceLogModel installMaintenanceLogModel){
        if(installMaintenanceLogModel.getProposedContext() == null || installMaintenanceLogModel.getProposedContext().isEmpty()){
            proContextLookupData.setLumBrand(slvServerData.getLuminaireBrand());
            proContextLookupData.setLumModel(slvServerData.getLuminaireModel());
            proContextLookupData.setLumPartNumber(slvServerData.getLuminairePartNumber());
            addProposedContext(proContextLookupData,paramsList,installMaintenanceLogModel);
        }
    }


    private String[] parseFixtureQRScan(String fixtureQRScan) {
        logger.info("Fixture QR Scan:" + fixtureQRScan);
        String[] res = StringUtils.splitPreserveAllTokens(fixtureQRScan, ",");
        logger.info("Fixture QR Scan length:" + res.length);
        return res;
    }




    public void buildFixtureStreetLightData(String data, LinkedMultiValueMap<String, String> paramsList, EdgeNote edgeNote, SlvServerData slvServerData, InstallMaintenanceLogModel loggingModel)
            throws InValidBarCodeException {
        ProContextLookupData proContextLookupData = new ProContextLookupData();

        if(data.startsWith("LB60") || data.startsWith("Luminaire Manufacturer")){
            //ES-265
            addCustomerNumber(edgeNote,loggingModel,paramsList);
            logger.info("Default Value Parser Starts");
            processFixtureQRScan(data,paramsList,edgeNote,slvServerData,loggingModel,proContextLookupData);
            addProContextLookupData(proContextLookupData,slvServerData,paramsList,loggingModel);
            return;
        }else if(data.startsWith("Existing")){
            logger.info("Existing Parser Starts");
            processExistingFixtureQRScan(data,paramsList,slvServerData,loggingModel,proContextLookupData);
            proContextLookupData.setLumWattage(null);
            //ES-265
            addCustomerNumber(edgeNote,loggingModel,paramsList);
            logger.info("After Existing Parser current value in paramsList are: "+paramsList.toString());
            logger.info("Existing Parser Ends");
            addProContextLookupData(proContextLookupData,slvServerData,paramsList,loggingModel);
            return;
        }
        String[] fixtureInfo = parseFixtureQRScan(data);
        logger.info("Fixture QR Scan Val length" + fixtureInfo.length);
        // The highlighted sections are where it look like Philips replaced a “-“ with a “,” causing a single field to become 2 fields. I can have Dan contact the manufacturer but we won’t be able to change any of the QR codes on the fixtures already delivered.
        if (fixtureInfo.length >= 15) {
            if (fixtureInfo[1].trim().equals("RFM1315G2")) {
                int index = StringUtils.ordinalIndexOf(data, ",", 3);
                fixtureInfo = replaceCharAt(data, index, '-').split(",");
                logger.info("Fixture QR Scan Val " + fixtureInfo);
                logger.info("Fixture QR Scan Val length" + fixtureInfo.length);
            }
        }
        if (fixtureInfo.length >= 13) {
            addStreetLightData("luminaire.brand", fixtureInfo[0], paramsList);
            slvServerData.setLuminaireBrand(fixtureInfo[0]);
            /**
             * As per Mail conversion, In the older data, the luminaire model was the
             * shorter version of the fixture, so for the General Electric fixtures it was
             * ERLH. The Luminaire Part Number would be the longer more detailed number.
             */
            String model = fixtureInfo[1].trim();
            String partNumber = fixtureInfo[2].trim();
            logger.info("luminaire.brand " + fixtureInfo[0]);
            if (fixtureInfo[1].trim().length() > fixtureInfo[2].trim().length() && !fixtureInfo[0].trim().contains("LV Manufacturing")) {
                model = fixtureInfo[2].trim();
                partNumber = fixtureInfo[1].trim();
                logger.info("device.luminaire.partnumber " + partNumber);
                logger.info("luminaire.model " + model);
            }
            addStreetLightData("device.luminaire.partnumber", partNumber, paramsList);
            slvServerData.setLuminairePartNumber(partNumber);
            addStreetLightData("luminaire.model", model, paramsList);
            slvServerData.setLuminaireModel(model);
            addStreetLightData("device.luminaire.manufacturedate", fixtureInfo[3], paramsList);
            slvServerData.setLuminaireManufacturedate(fixtureInfo[3]);
            String powerVal = fixtureInfo[4];
            if (powerVal != null && !powerVal.isEmpty()) {
                powerVal = powerVal.replaceAll("W", "");
                powerVal = powerVal.replaceAll("w", "");
            }

             // As per mail conversion, It looks like there is also a version of this Fixture QR Scan that contains a blank wattage as well. Instead of relying on the “912400973824” value like I stated below,
             // it looks like it will be safer to just tell the system that whenever a fixture qr scan contains “RFM108WG”, push “108” as the wattage/power into SLV.
            if(data.toUpperCase().contains("RFM108WG")){
                powerVal = "108";
            }else if(data.toUpperCase().contains("RFL180WB")){
                powerVal = "180";
            }

            addPower(loggingModel,powerVal,paramsList,proContextLookupData);
            //addStreetLightData("power", powerVal, paramsList);

            String dimmingGroupName = contextListHashMap.get(loggingModel.getIdOnController());
            logger.info("dimming groupname :"+dimmingGroupName);
            logger.info("stringContainsNumber(fixtureInfo[5]) status :"+stringContainsNumber(fixtureInfo[5]));
            logger.info("stringContainsNumber(fixtureInfo[5]) status :"+stringContainsNumber(fixtureInfo[5]));
            if (dimmingGroupName != null && stringContainsNumber(fixtureInfo[5]) && (dimmingGroupName.startsWith("3") || dimmingGroupName.startsWith("11") || dimmingGroupName.startsWith("12"))) {
                fixtureInfo[5] = "LED";
                logger.info("Entered if Statement Lum Type" +fixtureInfo[5]);
            }

            //Re: SLV: Invalid Luminaire Type For Philips Fixture Scans (20190925) as per mail conversion
            if(slvServerData.getLuminaireBrand() != null && slvServerData.getLuminaireBrand().toLowerCase().startsWith("philips")){
                fixtureInfo[5] = "LED";
            }

            logger.info("Final Lum Type" +fixtureInfo[5]);
            //luminaire.type
            addStreetLightData("luminaire.type", fixtureInfo[5], paramsList);
            // addStreetLightData("comed.litetype", fixtureInfo[5], paramsList);
            // dailyReportCSV.setFixtureType(fixtureInfo[5]);


            addStreetLightData("device.luminaire.colortemp", fixtureInfo[6], paramsList);
            slvServerData.setLuminaireColorTemp(fixtureInfo[6]);
            addStreetLightData("device.luminaire.lumenoutput", fixtureInfo[7], paramsList);
            slvServerData.setLumenOutput(fixtureInfo[7]);
            addStreetLightData("luminaire.DistributionType", fixtureInfo[8], paramsList);
            slvServerData.setDistributionType(fixtureInfo[8]);
            addStreetLightData("luminaire.colorcode", fixtureInfo[9], paramsList);
            slvServerData.setColorCode(fixtureInfo[9]);
            addStreetLightData("device.luminaire.drivermanufacturer", fixtureInfo[10], paramsList);
            slvServerData.setDriverManufacturer(fixtureInfo[10]);
            addStreetLightData("device.luminaire.driverpartnumber", fixtureInfo[11], paramsList);
            slvServerData.setDriverPartNumber(fixtureInfo[11]);
            addStreetLightData("ballast.dimmingtype", fixtureInfo[12], paramsList);
            slvServerData.setDimmingType(fixtureInfo[12]);
            //ES-265
            addCustomerNumber(edgeNote,loggingModel,paramsList);
            addProContextLookupData(proContextLookupData,slvServerData,paramsList,loggingModel);

        } else {
            /*throw new InValidBarCodeException(
                    "Fixture MAC address is not valid (" + edgeNote.getTitle() + "). Value is:" + data);*/
        }
    }


    protected int setDeviceValues(LinkedMultiValueMap<String, String> paramsList, SLVTransactionLogs slvTransactionLogs) {
        int errorCode = -1;
        try {
            String mainUrl = properties.getProperty("streetlight.slv.url.main");
            String updateDeviceValues = properties.getProperty("streetlight.slv.url.updatedevice");
            String url = mainUrl + updateDeviceValues;

            paramsList.add("ser","json");
            if(slvTransactionLogs.isDroppedPinWorkflow()) {
                paramsList.add("valueName","location.locationtype");
                paramsList.add("value" , properties.getProperty("com.slv.location.locationtype"));
                paramsList.add("valueName","modelFunctionId");
                paramsList.add("value" , properties.getProperty("com.slv.type.equipment"));
            }
            // -- No Need for production
            paramsList.add("valueName","modelFunctionId");
            paramsList.add("value" , properties.getProperty("com.slv.type.equipment"));


            logger.info("SetDevice method called");
            logger.info("SetDevice url:" + url);
            logger.info("SetDevice Prams:" + gson.toJson(paramsList));
            setSLVTransactionLogs(slvTransactionLogs, gson.toJson(paramsList), CallType.SET_DEVICE,url);
            ResponseEntity<String> response = restService.getPostRequest(url, null,paramsList);
            String responseString = response.getBody();
            setResponseDetails(slvTransactionLogs, responseString);
            JsonObject replaceOlcResponse = (JsonObject) jsonParser.parse(responseString);
            errorCode = replaceOlcResponse.get("errorCode").getAsInt();
        } catch (Exception e) {
            setResponseDetails(slvTransactionLogs, "Error in setDeviceValues:" + e.getMessage());
            logger.error("Error in setDeviceValues", e);
        } finally {
            streetlightDao.insertTransactionLogs(slvTransactionLogs);
        }

        return errorCode;
    }
    protected int checkAndCreateGeoZone(String geozone, SLVTransactionLogs slvTransactionLogs) {
        int geozoneId = -1;
        try {
            String rootGeoZone = properties.getProperty("com.slv.root.geozone");
            String mainUrl = properties.getProperty("streetlight.slv.url.main");
            String searchGeoZone = properties.getProperty("com.slv.search.devices.url");
            String url = mainUrl + searchGeoZone;
            List<String> paramsList = new ArrayList<>();
            paramsList.add("ser=json");

            try{
               // geozone = URLEncoder.encode(geozone.trim(), "UTF-8");
                geozone = geozone.trim();
            }catch (Exception e){
                logger.error("Error in addStreetLightData",e);
            }

            paramsList.add("name="+geozone);
            paramsList.add("partialMatch=false");
            String params = StringUtils.join(paramsList, "&");
            url = url + "?" + params;
            logger.info("checkAndCreateGeoZone method called");
            logger.info("checkAndCreateGeoZone url:" + url);
            setSLVTransactionLogs(slvTransactionLogs, url, CallType.SEARCH_GEOZONE,mainUrl + searchGeoZone);
            ResponseEntity<String> response = restService.getPostRequest(url, null,null);
            if(response.getStatusCode() == HttpStatus.NOT_FOUND){
                geozoneId = -1;
            }else {
                String responseString = response.getBody();
                setResponseDetails(slvTransactionLogs, responseString);
                JsonArray jsonArray = jsonParser.parse(responseString).getAsJsonArray();
                if (jsonArray != null && jsonArray.size() > 0) {
                    for (JsonElement jsonElement : jsonArray) {
                        JsonObject jsonObject = (JsonObject) jsonElement;
                        String geozoneNamePath = jsonObject.get("namesPath").getAsString();
                        if(geozoneNamePath.equals(rootGeoZone + geozone)){// inside unknown
                            geozoneId = jsonObject.get("id").getAsInt();
                        }
                    }
                }
            }
        } catch (Exception e) {
            setResponseDetails(slvTransactionLogs, "Error in checkAndCreateGeoZone:" + e.getMessage());
            logger.error("Error in checkAndCreateGeoZone", e);
        } finally {
            streetlightDao.insertTransactionLogs(slvTransactionLogs);
        }
        if(geozoneId == -1) {// no geozone present in unknown geozone so going to create geozone inside unknown
            geozoneId = createGeoZone(geozone,slvTransactionLogs);
        }
        return geozoneId;
    }

    protected int createDevice(SLVTransactionLogs slvTransactionLogs,EdgeNote edgeNote,int geoZoneId,String atlasGroup){
        int deviceId = -1;
        try {
            String mainUrl = properties.getProperty("streetlight.slv.url.main");
            String createDeviceMethodName = properties.getProperty("com.slv.create.device.url");
            String controllerStrId = properties.getProperty("streetlight.slv.controllerstrid");
            String categoryStrId = properties.getProperty("com.slv.categorystr.id");
            String url = mainUrl + createDeviceMethodName;
            LinkedMultiValueMap<String, String> paramsList = new LinkedMultiValueMap<>();
            EdgeNotebook edgeNotebook = edgeNote.getEdgeNotebook();
            List<FormData> formDatas = edgeNote.getFormData();
            FormData formData = new FormData();
            formData.setFormTemplateGuid(properties.getProperty("amerescousa.edge.formtemplateGuid"));
            int pos = formDatas.indexOf(formData);
            List<EdgeFormData> edgeFormDatas = formDatas.get(pos).getFormDef();
            String proposedContext = getFormValue(edgeFormDatas,16);
            String formFixtureCode = getFormValue(edgeFormDatas,12);

            String atlasPage = Utils.getAtlasPage(edgeNotebook.getNotebookName());

            String fixtureName = getFixtureName(proposedContext,formFixtureCode,atlasPage,edgeNote.getTitle(),atlasGroup);
            String geoJson = edgeNote.getGeometry();
            JsonObject geojsonObject = jsonParser.parse(geoJson).getAsJsonObject();
            JsonObject geometryObject = geojsonObject.get("geometry").getAsJsonObject();
            JsonArray latlngs = geometryObject.get("coordinates").getAsJsonArray();
            paramsList.add("ser","json");
            paramsList.add("userName",fixtureName);
            paramsList.add("categoryStrId",categoryStrId);
            paramsList.add("geozoneId",geoZoneId+"");
            paramsList.add("controllerStrId",controllerStrId);
            paramsList.add("idOnController",edgeNote.getTitle());
            paramsList.add("lat",latlngs.get(1)+"");
            paramsList.add("lng",latlngs.get(0)+"");

           // String params = StringUtils.join(paramsList, "&");
            //url = url + "?" + params;
            logger.info("createDevice method called");
            logger.info("createDevice url:" + url);
            logger.info("createDevice Prams:" + gson.toJson(paramsList));
            setSLVTransactionLogs(slvTransactionLogs, gson.toJson(paramsList), CallType.CREATE_DEVICE,url);
            ResponseEntity<String> response = restService.getPostRequest(url, null,paramsList);
            String responseString = response.getBody();
            setResponseDetails(slvTransactionLogs, responseString);
            JsonObject replaceOlcResponse = (JsonObject) jsonParser.parse(responseString);
            deviceId = replaceOlcResponse.get("id").getAsInt();
        } catch (Exception e) {
            setResponseDetails(slvTransactionLogs, "Error in createDevice:" + e.getMessage());
            logger.error("Error in createDevice", e);
        } finally {
            streetlightDao.insertTransactionLogs(slvTransactionLogs);
        }

        return deviceId;
    }

    protected boolean isDevicePresent(SLVTransactionLogs slvTransactionLogs,String deviceName){
        boolean isDevicePresent = false;
        try {
            String mainUrl = properties.getProperty("streetlight.slv.url.main");
            String searchDeviceMethodName = properties.getProperty("com.slv.search.device");
            int firstGeoZoneId = Integer.valueOf(properties.getProperty("com.slv.first.geozone.id"));
            String url = mainUrl + searchDeviceMethodName;
            List<String> paramsList = new ArrayList<>();

            paramsList.add("ser=json");
            paramsList.add("geozoneId="+firstGeoZoneId);
            paramsList.add("recurse=true");
            paramsList.add("returnedInfo=lightDevicesList");
            paramsList.add("attributeName=idOnController");
            paramsList.add("attributeValue="+deviceName);
            paramsList.add("maxResults=1");
            paramsList.add("attributeOperator=eq");
            String params = StringUtils.join(paramsList, "&");
            url = url + "?" + params;
            logger.info("isDevicePresent method called");
            logger.info("isDevicePresent url:" + url);
            setSLVTransactionLogs(slvTransactionLogs, url, CallType.SEARCH_DEVICE,mainUrl + searchDeviceMethodName);
            ResponseEntity<String> response = restService.getPostRequest(url, null,null);
            if(response.getStatusCode() == HttpStatus.NOT_FOUND){
                isDevicePresent = false;
            }else {
                String responseString = response.getBody();
                setResponseDetails(slvTransactionLogs, responseString);
                JsonObject searchDeviceResponse = (JsonObject) jsonParser.parse(responseString);
                JsonArray jsonArray = searchDeviceResponse.get("value").getAsJsonArray();
                if (jsonArray != null && jsonArray.size() > 0) {
                    isDevicePresent = true;
                }
            }
        } catch (Exception e) {
            setResponseDetails(slvTransactionLogs, "Error in isDevicePresent:" + e.getMessage());
            logger.error("Error in isDevicePresent", e);
        } finally {
            streetlightDao.insertTransactionLogs(slvTransactionLogs);
        }

        return isDevicePresent;
    }
    private void setSLVTransactionLogs(SLVTransactionLogs slvTransactionLogs, String request, CallType callType,String url) {
        slvTransactionLogs.setRequestDetails(request);
        slvTransactionLogs.setTypeOfCall(callType);
        slvTransactionLogs.setRequestUrl(url);
    }

    private void setResponseDetails(SLVTransactionLogs slvTransactionLogs, String responseString) {
        slvTransactionLogs.setResponseBody(responseString);
    }


    /**
     * Calls ReplaceOLCs
     *
     * @throws ReplaceOLCFailedException
     */
    public void replaceOLC(String controllerStrIdValue, String idOnController, String macAddress, SLVTransactionLogs slvTransactionLogs, SlvInterfaceLogEntity slvInterfaceLogEntity,String atlasPhysicalPage,InstallMaintenanceLogModel loggingModel,EdgeNote edgeNote)
            throws ReplaceOLCFailedException {

        try {
            // String newNetworkId = slvSyncDataEntity.getMacAddress();
            String newNetworkId = macAddress;
            if (newNetworkId != null && !newNetworkId.trim().isEmpty()) {
                sendLuxValueToSLV(idOnController,loggingModel,slvTransactionLogs);
            }
            // Get Url detail from properties
            String mainUrl = properties.getProperty("streetlight.url.main");
            String dataUrl = properties.getProperty("streetlight.url.replaceolc");
            String replaceOlc = properties.getProperty("streetlight.url.replaceolc.method");
            String url = mainUrl + dataUrl;
            String controllerStrId = controllerStrIdValue;
            LinkedMultiValueMap<String,String> paramsList = new LinkedMultiValueMap<>();
            //paramsList.add("methodName" , replaceOlc);
            paramsList.add("controllerStrId" , controllerStrId);
            paramsList.add("idOnController" , idOnController);
            paramsList.add("newNetworkId" , newNetworkId);
            paramsList.add("ser","json");
           // String params = StringUtils.join(paramsList, "&");
           // url = url + "?" + params;
            setSLVTransactionLogs(slvTransactionLogs, url, CallType.REPLACE_OLC,mainUrl + dataUrl);
            ResponseEntity<String> response = restService.getPostRequest(url, null,paramsList);
            String responseString = response.getBody();
            setResponseDetails(slvTransactionLogs, responseString);
            JsonObject replaceOlcResponse = (JsonObject) jsonParser.parse(responseString);
            String errorStatus = replaceOlcResponse.get("status").getAsString();
            logger.info("Replace OLC Process End.");
            // As per doc, errorcode is 0 for success. Otherwise, its not success.
            if (errorStatus.equals("ERROR")) {
                String value = replaceOlcResponse.get("value").getAsString();
                if(macAddress != null && !macAddress.trim().isEmpty()){
                    createEdgeAllMac(idOnController, macAddress);
                    syncMacAddress2Edge(idOnController,macAddress,atlasPhysicalPage);
                    paramsList = new LinkedMultiValueMap<>();
                    syncAccountNumber(paramsList,loggingModel,edgeNote,Utils.SUCCESSFUL,macAddress);
                }
                throw new ReplaceOLCFailedException(value);

            } else {
                if (macAddress != null && !macAddress.trim().isEmpty()) {
                    slvInterfaceLogEntity.setReplaceOlc(MessageConstants.REPLACEOLC);
                    slvInterfaceLogEntity.setStatus(MessageConstants.SUCCESS);
                    createEdgeAllMac(idOnController, macAddress);
                    syncMacAddress2Edge(idOnController,macAddress,atlasPhysicalPage);
                    paramsList = new LinkedMultiValueMap<>();
                    syncAccountNumber(paramsList,loggingModel,edgeNote,Utils.SUCCESSFUL,macAddress);
                    syncCustomerName(loggingModel);
                }

            }

        } catch (Exception e) {
            logger.error("Error in replaceOLC", e);
            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
            slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
            slvInterfaceLogEntity.setErrordetails("ReplaceOlc Failed :" + e.getMessage());
            throw new ReplaceOLCFailedException(e.getMessage());
        } finally {
            streetlightDao.insertTransactionLogs(slvTransactionLogs);
        }


    }

    private void sendLuxValueToSLV(String idOnController, InstallMaintenanceLogModel loggingModel, SLVTransactionLogs slvTransactionLogs) {
        logger.info("present on lux level: "+loggingModel.getOnLuxLevel());
        logger.info("present off lux level: "+loggingModel.getOffLuxLevel());
        if (loggingModel.getOnLuxLevel() == 0.0 && loggingModel.getOffLuxLevel() == 0.0){
            logger.info("Going to call setdevice values to set lux level");
            LinkedMultiValueMap<String, String> paramsList = new LinkedMultiValueMap<>();
            String controllerStrId = properties.getProperty("streetlight.slv.controllerstrid");
            paramsList.add("idOnController" , idOnController);
            paramsList.add("controllerStrId" , controllerStrId);
            addStreetLightData(properties.getProperty("com.slv.on.lux.level.setdevice.key"),properties.getProperty("com.slv.on.lux.level.value"),paramsList);
            addStreetLightData(properties.getProperty("com.slv.off.lux.level.setdevice.key"),properties.getProperty("com.slv.off.lux.level.value"),paramsList);
            setDeviceValues(paramsList, slvTransactionLogs);
        }else {
            logger.info("Lux level already there in SLV. So Skipping lux level update for this idoncontroller: "+idOnController);
        }
    }

    protected String getUtilLocationId(String errorDetails) {
        if (errorDetails != null && errorDetails.contains("Service point is already associated with LocationUtilID")) {
            int startAt = errorDetails.indexOf("LocationUtilID");
            int endAt = errorDetails.indexOf("with type", startAt);
            String utilLocationId = errorDetails.substring(startAt + 17, endAt);
            return utilLocationId.trim();
        }
        return null;
    }


    protected void loadDefaultVal(EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel,String accessToken,String droppedPinUser) throws SQLException {
        loggingModel.setIdOnController(edgeNote.getTitle());
        String controllerStrId = properties.getProperty("streetlight.slv.controllerstrid");
        loggingModel.setControllerSrtId(controllerStrId);
        DatesHolder datesHolder = new DatesHolder();
        loggingModel.setDatesHolder(datesHolder);
        if(loggingModel.isDroppedPinWorkflow() && droppedPinUser != null)
        {
            DroppedPinRemoveEvent result = connectionDAO.getDroppedPinRemovedEntryFor(loggingModel.getIdOnController());
            if(result != null)
            {
                checkAmerescoUser(accessToken,loggingModel,edgeNote.getCreatedBy());
            }
            else
            {
                checkAmerescoUser(accessToken,loggingModel,droppedPinUser);
            }
        }else {
            checkAmerescoUser(accessToken,loggingModel,edgeNote.getCreatedBy());
        }

    }

    private void sendNightRideToSLV(String idOnController, String nightRideKey, String nightRideValue, LoggingModel loggingModel) {
        LinkedMultiValueMap<String, String> paramsList = new LinkedMultiValueMap<>();
        String controllerStrId = properties.getProperty("streetlight.slv.controllerstrid");
        paramsList.add("idOnController" , idOnController);
        paramsList.add("controllerStrId" , controllerStrId);
        if (nightRideValue != null) {
            addStreetLightData(nightRideKey, nightRideValue, paramsList);
            SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(loggingModel);
            int errorCode = setDeviceValues(paramsList, slvTransactionLogs);
            logger.info("Error code" + errorCode);
            if (errorCode != 0) {
                logger.error(MessageConstants.ERROR_UPDATE_DEVICE_VAL);
                logger.error(MessageConstants.ERROR);
            } else {
                logger.info(MessageConstants.SET_DEVICE_SUCCESS);
                logger.info(MessageConstants.SUCCESS);
            }
        }
    }


    public SLVTransactionLogs getSLVTransactionLogs(LoggingModel loggingModel) {
        SLVTransactionLogs slvTransactionLogs = new SLVTransactionLogs();
        slvTransactionLogs.setNoteGuid(loggingModel.getProcessedNoteId());
        slvTransactionLogs.setTitle(loggingModel.getNoteName());
        slvTransactionLogs.setCreatedDateTime(Long.valueOf(loggingModel.getCreatedDatetime()));
        slvTransactionLogs.setParentNoteGuid(loggingModel.getParentNoteId());
        slvTransactionLogs.setDroppedPinWorkflow(loggingModel.isDroppedPinWorkflow());

        return slvTransactionLogs;
    }


    private void loadColumnPos(String columnName, SlvServerDataColumnPos slvServerDataColumnPos, int pos) {
        switch (columnName) {
            case "idOnController":
                slvServerDataColumnPos.setIdOnController(pos);
                break;
            case "MacAddress":
                slvServerDataColumnPos.setMacAddress(pos);
                break;
            case "device.luminaire.partnumber":
                slvServerDataColumnPos.setLuminairePartNumber(pos);
                break;
            case "luminaire.model":
                slvServerDataColumnPos.setLuminaireModel(pos);
                break;
            case "device.luminaire.manufacturedate":
                slvServerDataColumnPos.setLuminaireManufacturedate(pos);
                break;
            case "device.luminaire.colortemp":
                slvServerDataColumnPos.setLuminaireColorTemp(pos);
                break;
            case "device.luminaire.lumenoutput":
                slvServerDataColumnPos.setLumenOutput(pos);
                break;
            case "luminaire.DistributionType":
                slvServerDataColumnPos.setDistributionType(pos);
                break;
            case "luminaire.colorcode":
                slvServerDataColumnPos.setColorCode(pos);
                break;
            case "device.luminaire.drivermanufacturer":
                slvServerDataColumnPos.setDriverManufacturer(pos);
                break;
            case "device.luminaire.driverpartnumber":
                slvServerDataColumnPos.setDriverPartNumber(pos);
                break;
            case "ballast.dimmingtype":
                slvServerDataColumnPos.setDimmingType(pos);
                break;

            case "luminaire.serialnumber":
                slvServerDataColumnPos.setSerialNumber(pos);
                break;

        }
    }


    private void loadSlvServerData(SlvServerData slvServerData, List<String> valueList, SlvServerDataColumnPos slvServerDataColumnPos) {
        slvServerData.setIdOnController(getData(valueList, slvServerDataColumnPos.getIdOnController()));
        slvServerData.setMacAddress(getData(valueList, slvServerDataColumnPos.getMacAddress()));
        slvServerData.setLuminairePartNumber(getData(valueList, slvServerDataColumnPos.getLuminairePartNumber()));
        slvServerData.setLuminaireModel(getData(valueList, slvServerDataColumnPos.getLuminaireModel()));
        slvServerData.setLuminaireManufacturedate(getData(valueList, slvServerDataColumnPos.getLuminaireManufacturedate()));
        slvServerData.setLuminaireColorTemp(getData(valueList, slvServerDataColumnPos.getLuminaireColorTemp()));
        slvServerData.setLumenOutput(getData(valueList, slvServerDataColumnPos.getLumenOutput()));
        slvServerData.setDistributionType(getData(valueList, slvServerDataColumnPos.getDistributionType()));
        slvServerData.setColorCode(getData(valueList, slvServerDataColumnPos.getColorCode()));
        slvServerData.setDriverManufacturer(getData(valueList, slvServerDataColumnPos.getDriverManufacturer()));
        slvServerData.setDriverPartNumber(getData(valueList, slvServerDataColumnPos.getDriverPartNumber()));
        slvServerData.setDimmingType(getData(valueList, slvServerDataColumnPos.getDimmingType()));
    }


    private String getData(List<String> valueList, int id) {
        try {
            String value = valueList.get(id);

            if (value != null && !value.trim().isEmpty() && !value.contains("null")) {
                return value;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void closeConnection() {
        streetlightDao.closeConnection();
    }

    protected String getEdgeToken() {
        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        String userName = properties.getProperty("streetlight.edge.username");
        String password = properties.getProperty("streetlight.edge.password");
        url = url + "/oauth/token?grant_type=password&username=" + userName + "&password=" + password
                + "&client_id=edgerestapp";
        ResponseEntity<String> responseEntity = restService.getRequest(url);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            JsonObject jsonObject = (JsonObject) jsonParser.parse(responseEntity.getBody());
            return jsonObject.get("access_token").getAsString();
        }
        return null;

    }


    public String getSLVInstallDate(InstallMaintenanceLogModel installMaintenanceLogModel){
        try{
            if (installMaintenanceLogModel.getDeviceId() > 0) {
                String mainUrl = properties.getProperty("streetlight.slv.url.main");
                String commentUrl = properties.getProperty("streetlight.slv.url.comment.get");
                String url = mainUrl + commentUrl;
                List<String> paramsList = new ArrayList<>();
                paramsList.add("returnTimeAges=false");
                paramsList.add("param0=" + installMaintenanceLogModel.getDeviceId());
                paramsList.add("valueName=install.date");
                paramsList.add("ser=json");
                String params = StringUtils.join(paramsList, "&");
                url = url + "?" + params;
                logger.info("Get Install Date url :" + url);
                ResponseEntity<String> response = restService.getRequest(url, true, null);
                if (response.getStatusCodeValue() == 200) {
                    logger.info("Get Install Date url :" + response.getBody());
                    String responseString = response.getBody();
                    JsonArray jsonArray = (JsonArray) jsonParser.parse(responseString);
                    for (JsonElement installDateJsonArray : jsonArray) {
                        JsonObject installDateJson = installDateJsonArray.getAsJsonObject();
                        if (installDateJson.get("name") != null) {
                            String paramName = installDateJson.get("name").getAsString();
                            if (paramName.equals("install.date")) {
                                if (installDateJson.get("value") != null) {
                                    return installDateJson.get("value").getAsString();

                                }
                            }
                        }

                    }
                }

            }
        }catch (Exception e){
            logger.error("Error in getSLVInstallDate",e);
        }
        return null;
    }



    public String getSLVValues(InstallMaintenanceLogModel installMaintenanceLogModel,String key){
        try{
            if (installMaintenanceLogModel.getDeviceId() > 0 && key != null && !key.trim().isEmpty()) {
                String mainUrl = properties.getProperty("streetlight.slv.url.main");
                String commentUrl = properties.getProperty("streetlight.slv.url.comment.get");
                String url = mainUrl + commentUrl;
                List<String> paramsList = new ArrayList<>();
                paramsList.add("returnTimeAges=false");
                paramsList.add("valueName="+key);
                paramsList.add("param0=" + installMaintenanceLogModel.getDeviceId());
                paramsList.add("ser=json");
                String params = StringUtils.join(paramsList, "&");
                url = url + "?" + params;
                logger.info("Get Install Date url :" + url);
                ResponseEntity<String> response = restService.getRequest(url, true, null);
                if (response.getStatusCodeValue() == 200) {
                    logger.info("Get Install Date url :" + response.getBody());
                    String responseString = response.getBody();
                    JsonArray jsonArray = (JsonArray) jsonParser.parse(responseString);
                    for (JsonElement installDateJsonArray : jsonArray) {
                        JsonObject installDateJson = installDateJsonArray.getAsJsonObject();
                        if (installDateJson.get("name") != null) {
                            String paramName = installDateJson.get("name").getAsString();
                            if (paramName.equals(key)) {
                                if (installDateJson.get("value") != null) {
                                    return installDateJson.get("value").getAsString();

                                }
                            }
                        }

                    }
                }

            }
        }catch (Exception e){
            logger.error("Error in getSLVValues",e);
        }
        return null;
    }


    public DeviceAttributes getDeviceValues(InstallMaintenanceLogModel installMaintenanceLogModel) {
        try {
            if (installMaintenanceLogModel.getDeviceId() > 0) {
                DeviceAttributes deviceAttributes = new DeviceAttributes();
                String mainUrl = properties.getProperty("streetlight.slv.url.main");
                String commentUrl = properties.getProperty("streetlight.slv.url.comment.get");
                String url = mainUrl + commentUrl;
                List<String> paramsList = new ArrayList<>();
                paramsList.add("returnTimeAges=false");
                paramsList.add("param0=" + installMaintenanceLogModel.getDeviceId());
                paramsList.add("valueName=MacAddress");
                paramsList.add("valueName=installStatus");
                paramsList.add("ser=json");
                String params = StringUtils.join(paramsList, "&");
                url = url + "?" + params;
                logger.info("Get MAC Address and installStatus url :" + url);
                ResponseEntity<String> response = restService.getRequest(url, true, null);
                if (response.getStatusCodeValue() == 200) {
                    logger.info("Get MAC Address and installStatus Response :" + response.getBody());
                    String responseString = response.getBody();
                    JsonArray jsonArray = (JsonArray) jsonParser.parse(responseString);
                    for (JsonElement macAddressJson : jsonArray) {
                        JsonObject macAddressJsonObject = macAddressJson.getAsJsonObject();
                        if (macAddressJsonObject.get("name") != null) {
                            String paramName = macAddressJsonObject.get("name").getAsString();
                            if (paramName.equals("MacAddress")) {
                                if (macAddressJsonObject.get("value") != null) {
                                    String macAddress = macAddressJsonObject.get("value").getAsString();
                                    if (macAddress != null && !macAddress.trim().isEmpty()) {
                                        deviceAttributes.setMacAddress(macAddress);
                                    }
                                }
                            } else if (paramName.equals("installStatus")) {
                                String installStatus = macAddressJsonObject.get("value").getAsString();
                                if (installStatus != null && !installStatus.trim().isEmpty()) {
                                    deviceAttributes.setInstallStatus(installStatus);
                                }
                            }

                        }

                    }
                }
                logger.info(deviceAttributes);
                return deviceAttributes;
            }
        } catch (Exception e) {
            logger.error("Error in getDeviceValues", e);
        }
        return null;
    }
    protected Boolean isDroppedPinNote(EdgeNote edgeNote,String droppedPinTag){
        List<String> tags = edgeNote.getTags();
        boolean isDroppedPinWorkFlow = false;
        if(tags.contains(droppedPinTag)){
            isDroppedPinWorkFlow = true;
        }
        return isDroppedPinWorkFlow;
    }


    protected String getDroppedPinUser(EdgeNote edgeNote){
        List<String> tags = edgeNote.getTags();
        for(String tag: tags){
            if(tag.startsWith("user:")){
                return tag.replace("user:","").trim();
            }
        }
        return null;
    }

public boolean checkExistingMacAddressValid(EdgeNote edgeNote, InstallMaintenanceLogModel installMaintenanceLogModel) throws Exception{
        try {
            logger.info("Validate Existing MacAddress");
            String idonController = edgeNote.getTitle();
            String slvMacAddress = installMaintenanceLogModel.getSlvMacaddress() == null ? "" : installMaintenanceLogModel.getSlvMacaddress();
            String existingMacAddress = installMaintenanceLogModel.getExistingNodeMACaddress() == null ? "" : installMaintenanceLogModel.getExistingNodeMACaddress();
            logger.info("Existing MacAddress:"+existingMacAddress);
            logger.info("SLV MacAddress:"+slvMacAddress);
            if(existingMacAddress.startsWith("00000")){
                return  true;
            }
                if(slvMacAddress.trim().toLowerCase().equals(existingMacAddress.trim().toLowerCase())) {
                    List<ExistingMacValidationFailure> existingMacValidationFailures = connectionDAO.getExistingMacValidationFailure(idonController,existingMacAddress);
                    for(ExistingMacValidationFailure existingMacValidationFailure : existingMacValidationFailures){
                        connectionDAO.deleteExistingMacVaildationFailure(existingMacValidationFailure);
                    }
                    logger.info("Existing MacAddress Matches with SLV MacAddress.");
                    return true;
                }else{
                    logger.info("Existing MacAddress not Matched with SLV MacAddress.");
                    ExistingMacValidationFailure existingMacValidationFailure = new ExistingMacValidationFailure();
                    existingMacValidationFailure.setCreatedBy(edgeNote.getCreatedBy());
                    existingMacValidationFailure.setProcessedDateTime(System.currentTimeMillis());
                    existingMacValidationFailure.setCreatedDateTime(edgeNote.getCreatedDateTime());
                    existingMacValidationFailure.setNoteGuid(edgeNote.getNoteGuid());
                    existingMacValidationFailure.setEdgeNewNodeMacaddress(installMaintenanceLogModel.getNewNodeMACaddress());
                    existingMacValidationFailure.setEdgeExistingMacaddress(existingMacAddress);
                    existingMacValidationFailure.setIdOnController(idonController);
                    existingMacValidationFailure.setSlvMacaddress(slvMacAddress);
                    connectionDAO.saveExistingMacFailure(existingMacValidationFailure);
                    installMaintenanceLogModel.setErrorDetails("Existing macaddress not matched with slv macaddress");
                    installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
                    return false;
                }
    } catch (Exception e) {
            logger.error("Error in checkExistingMacAddressValid",e);
        throw new Exception(e);
    }
}
    private String getFormValue(List<EdgeFormData> edgeFormDatas,String label){
        for(EdgeFormData edgeFormData : edgeFormDatas){
            if(edgeFormData.getLabel().equals(label)){
                return edgeFormData.getValue();
            }
        }
        return "";
    }
    private String getFormValue(List<EdgeFormData> edgeFormDatas,int id){
        for(EdgeFormData edgeFormData : edgeFormDatas){
            if(edgeFormData.getId() == id){
                return edgeFormData.getValue();
            }
        }
        return "";
    }

    protected boolean isDatePresent(String title,String date,String type){
        EdgeSLVDate edgeSLVDate = connectionDAO.getEdgeNodeDate(title,date,type);
        return edgeSLVDate != null;
    }



    protected int createGeoZone(String geozone, SLVTransactionLogs slvTransactionLogs) {
        int geozoneId = -1;
        try {
            int rootGeozoneId = Integer.valueOf(properties.getProperty("com.slv.root.geozone.id"));
            String mainUrl = properties.getProperty("streetlight.slv.url.main");
            String createGeozone = properties.getProperty("com.slv.create.geozone.method");
            float maxLat = Float.valueOf(properties.getProperty("com.slv.unknown.maxlat"));
            float maxLng = Float.valueOf(properties.getProperty("com.slv.unknown.maxlng"));
            float minLat = Float.valueOf(properties.getProperty("com.slv.unknown.minlat"));
            float minLng = Float.valueOf(properties.getProperty("com.slv.unknown.minlng"));
            String url = mainUrl + createGeozone;
            LinkedMultiValueMap<String, String> paramsList = new LinkedMultiValueMap<>();
            paramsList.add("ser","json");

            try{
               // geozone = URLEncoder.encode(geozone.trim(), "UTF-8");
                geozone = geozone.trim();
            }catch (Exception e){
                logger.error("Error in addStreetLightData",e);
            }

            paramsList.add("name",geozone);
            paramsList.add("parentId",rootGeozoneId+"");
            paramsList.add("latMax",maxLat+"");
            paramsList.add("latMin",minLat+"");
            paramsList.add("lngMax",maxLng+"");
            paramsList.add("lngMin",minLng+"");
            //String params = StringUtils.join(paramsList, "&");
           // url = url + "?" + params;
            logger.info("checkAndCreateGeoZone method called");
            logger.info("checkAndCreateGeoZone url:" + url);
            logger.info("checkAndCreateGeoZone Params:" + gson.toJson(paramsList));
            setSLVTransactionLogs(slvTransactionLogs, gson.toJson(paramsList), CallType.CREATE_GEOZONE,url);
            ResponseEntity<String> response = restService.getPostRequest(url, null,paramsList);
            if(response.getStatusCode() == HttpStatus.NOT_FOUND){
                geozoneId = -1;
            }else {
                String responseString = response.getBody();
                setResponseDetails(slvTransactionLogs, responseString);
                JsonObject jsonObject = jsonParser.parse(responseString).getAsJsonObject();
                geozoneId = jsonObject.get("id").getAsInt();
            }
        } catch (Exception e) {
            setResponseDetails(slvTransactionLogs, "Error in createGeoZone:" + e.getMessage());
            logger.error("Error in createGeoZone", e);
        } finally {
            streetlightDao.insertTransactionLogs(slvTransactionLogs);
        }

        return geozoneId;
    }


    public void syncMacAddress2Edge(String idOnController,String macAddress,String atlasPhysicalPage){
        if(macAddress != null && !macAddress.trim().isEmpty()){
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("slvMacAddress",macAddress);
            params.add("slvIdOnController",idOnController);
            if(atlasPhysicalPage != null){
                params.add("atlasPhysicalPage",atlasPhysicalPage);
            }

            restService.slv2Edge("/rest/validation/updateSLVSyncedMAC", HttpMethod.GET,params);

            syncMacAddress2Promoted(idOnController,macAddress,0);
        }
    }


    protected void syncMacAddress2Promoted(final String idOnController, final String macAddress, final int retryCount){
        promotedSLVMACAddressExecutor.execute(new Runnable() {
            @Override
            public void run() {
                // Remove MAC Address from the Promoted Data.
                logger.info("Remove MAC Address from the Promoted Data.");
                try {
                    String url = "/promoted/updateSLVMacAddress?idOnController="+idOnController;
                    if(macAddress != null && !macAddress.trim().isEmpty()){
                        url = url + "&slvMacAddress="+macAddress;
                    }
                    restService.callPostMethod(url,HttpMethod.GET,null,true);
                    logger.info("Remove MAC Address from the Promoted Data End.");
                }catch (HttpServerErrorException e){
                    logger.error("Error in update macaddress HttpServerErrorException", e);
                    //Since it is executed in background thread, if it throws unique constraint error, it will send request one more time(in promotedformdata table parentnoteguid is unique constraint so it throw this error)
                        if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR && retryCount == 0){
                            syncMacAddress2Promoted(idOnController, macAddress, (retryCount + 1));
                        }
                }catch (Exception e){
                    logger.error("Error in update macaddress", e);
                }
            }
        });

    }


    public void addAccountNumber(EdgeNote edgeNote,String status,String macAddress,LinkedMultiValueMap<String, String> paramsList){
        logger.info("Start of addAccountNumber");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Last Modified By");
        stringBuilder.append(" - ");
        stringBuilder.append(edgeNote.getCreatedBy());
        stringBuilder.append(" - ");
        stringBuilder.append(dateFormat(edgeNote.getCreatedDateTime()));
        stringBuilder.append(" - ");
        stringBuilder.append(status);
        stringBuilder.append(" - ");
        stringBuilder.append(macAddress);
        addStreetLightData("comed.componentffectivedate", stringBuilder.toString(), paramsList);
        logger.info("End of addAccountNumber");
    }

    //ES-265
    private void addCustomerNumber(EdgeNote edgeNote,InstallMaintenanceLogModel installMaintenanceLogModel,LinkedMultiValueMap<String, String> paramsList){
        logger.info("Start of addCustomerNumber Method.");
        if(installMaintenanceLogModel.isReplace()){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Last Modified By");
            stringBuilder.append(" - ");
            stringBuilder.append(edgeNote.getCreatedBy());
            stringBuilder.append(" - ");
            stringBuilder.append(dateFormat(edgeNote.getCreatedDateTime()));
            addStreetLightData("comed.locationeffectivedate", stringBuilder.toString(), paramsList);
            logger.info("Customer Number Added.");
        }
        logger.info("End of addCustomerNumber Method.");
        addCustomerName(installMaintenanceLogModel,paramsList);

    }


    public void syncAccountNumber(LinkedMultiValueMap<String, String> paramsList,InstallMaintenanceLogModel installMaintenanceLogModel,EdgeNote edgeNote,String status,String macAddress){
        logger.info("Start of syncAccountNumber");
        if(installMaintenanceLogModel.isReplace()){
            logger.info("AccountNumber values going to sync with SLV.");
            addAccountNumber(edgeNote,status,macAddress,paramsList);
            String idOnController = installMaintenanceLogModel.getIdOnController();
            paramsList.add("idOnController" , idOnController);
            paramsList.add("controllerStrId" , installMaintenanceLogModel.getControllerSrtId());
            SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(installMaintenanceLogModel);
            int errorCode = setDeviceValues(paramsList, slvTransactionLogs);
            logger.info("Error Code:"+errorCode);
        }else{
            logger.info("syncAccountNumber Not Called.");
        }
        logger.info("End of syncAccountNumber");
    }



    public void syncCustomerName(InstallMaintenanceLogModel installMaintenanceLogModel){
        logger.info("Start of syncCustomerName");
        if(installMaintenanceLogModel.isActionNew() && installMaintenanceLogModel.isAmerescoUser()){
            LinkedMultiValueMap<String, String> paramsList = new LinkedMultiValueMap<>();
            String idOnController = installMaintenanceLogModel.getIdOnController();
            paramsList.add("idOnController" , idOnController);
            addCustomerName(installMaintenanceLogModel,paramsList);
            paramsList.add("controllerStrId" , installMaintenanceLogModel.getControllerSrtId());
            SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(installMaintenanceLogModel);
            int errorCode = setDeviceValues(paramsList, slvTransactionLogs);
            logger.info("Error Code:"+errorCode);
        }else{
            logger.info("syncCustomerName Not Called.");
        }
        logger.info("End of syncCustomerName");

    }


    public void addCustomerName(InstallMaintenanceLogModel installMaintenanceLogModel, LinkedMultiValueMap<String, String> paramsList){
        logger.info("Start of addCustomerName Method.");
        if(installMaintenanceLogModel.isActionNew() && installMaintenanceLogModel.isAmerescoUser()){
            addStreetLightData("client.name", "Ameresco Install", paramsList);
            logger.info("Customer Name Added.");
        }
        logger.info("End of addCustomerName Method.");
    }


    public void checkAmerescoUser(String accessToken, InstallMaintenanceLogModel installMaintenanceLogModel, String userName) {
        logger.info("Start of checkAmerescoUser");
        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        url = url + PropertiesReader.getProperties().getProperty("com.edge.url.get.groupids");
        url = url + "?userName=" + userName;
        logger.info("Given url is :" + url);
        ResponseEntity<String> responseEntity = restService.getRequest(url, true, accessToken);
        logger.info("Response Code:"+responseEntity.getStatusCodeValue());
        if (responseEntity.getStatusCodeValue() == HttpStatus.OK.value()) {
            String reponseData = responseEntity.getBody();
            JsonArray groupJsonArray = jsonParser.parse(reponseData).getAsJsonArray();
            String amerescoGroups = PropertiesReader.getProperties().getProperty("com.edge.ameresco.groupids");
            logger.info("Ameresco Groups:"+amerescoGroups);
            for (JsonElement groupJsonElement : groupJsonArray) {
                if (groupJsonElement.getAsString().equals(amerescoGroups)) {
                    logger.info("Ameresco User added.");
                    installMaintenanceLogModel.setAmerescoUser(true);
                }
            }
        }
        logger.info("End of checkAmerescoUser");
    }


    //ES-275
    protected void addProposedContext(ProContextLookupData proContextLookupData,LinkedMultiValueMap<String, String> paramsList,InstallMaintenanceLogModel installMaintenanceLogModel){
        logger.info("ProContextLookupData:"+proContextLookupData.toString());
        logger.info("isDroppedPinWorkflow:"+installMaintenanceLogModel.isDroppedPinWorkflow());
        logger.info("isAmerescoUser:"+installMaintenanceLogModel.isAmerescoUser());
        if(installMaintenanceLogModel.isDroppedPinWorkflow() && installMaintenanceLogModel.isAmerescoUser()){
            boolean isLumModelExact = false;
            boolean isLumPartExact = false;
            logger.info("proContextLookupData.getLumBrand().toLowerCase():::::"+proContextLookupData.getLumBrand().toLowerCase());
            if(proContextLookupData.getLumBrand().toLowerCase().startsWith("philips")){
                logger.info("philips......");
               int res = updateLumModel(proContextLookupData);
               if(res == 1){
                   isLumModelExact = true;
                   isLumPartExact = true;
               }else if(res == 0){
                   isLumModelExact = true;
               }
            }

            if(proContextLookupData.getLumBrand().toLowerCase().contains("acuity")){
                proContextLookupData.setLumBrand("Acuity");
                proContextLookupData.setLumPartNumber(null);
                proContextLookupData.setLumModel(null);
            }
            logger.info("ProContextLookupData after modified:"+proContextLookupData.toString());
            ProContextLookupData dbProContextLookupData =  connectionDAO.getProContextLookupData(proContextLookupData,isLumModelExact,isLumPartExact);
            logger.info("DB ProContextLookupData"+dbProContextLookupData);
            if(dbProContextLookupData != null && dbProContextLookupData.getProposedContext() != null){
                logger.info("DB ProContextLookupData"+dbProContextLookupData.toString());
                if(dbProContextLookupData.getLumBrand() != null && dbProContextLookupData.getLumBrand().toLowerCase().startsWith("existing") && installMaintenanceLogModel.isButtonPhotoCell()){
                    addStreetLightData("location.proposedcontext", "Photocell Only", paramsList);
                    String fixtureName = getFixtureName("Photocell Only",installMaintenanceLogModel.getLuminaireFixturecode(),installMaintenanceLogModel.getAtlasPhysicalPage(),installMaintenanceLogModel.getNoteName(),installMaintenanceLogModel.getAtlasGroup());
                    addStreetLightData("userName", fixtureName, paramsList);
                }else if(dbProContextLookupData.getLumBrand() != null && dbProContextLookupData.getLumBrand().toLowerCase().startsWith("existing") && installMaintenanceLogModel.isNodeOnly()){
                    addStreetLightData("location.proposedcontext", "Node Only", paramsList);
                    String fixtureName = getFixtureName("Node Only",installMaintenanceLogModel.getLuminaireFixturecode(),installMaintenanceLogModel.getAtlasPhysicalPage(),installMaintenanceLogModel.getNoteName(),installMaintenanceLogModel.getAtlasGroup());
                    addStreetLightData("userName", fixtureName, paramsList);
                }else {
                    addStreetLightData("location.proposedcontext", dbProContextLookupData.getProposedContext(), paramsList);
                    String fixtureName = getFixtureName(dbProContextLookupData.getProposedContext(),installMaintenanceLogModel.getLuminaireFixturecode(),installMaintenanceLogModel.getAtlasPhysicalPage(),installMaintenanceLogModel.getNoteName(),installMaintenanceLogModel.getAtlasGroup());
                    addStreetLightData("userName", fixtureName, paramsList);
                }

            }
        }

    }


    private int updateLumModel(ProContextLookupData proContextLookupData){
        int flag = -1;
        if(proContextLookupData.getLumModel() != null){
            logger.info("proContextLookupData.getLumModel():::::"+proContextLookupData.getLumModel());
            if(proContextLookupData.getLumModel().startsWith("RFM")){
                logger.info("RFM");
                proContextLookupData.setLumModel("RFM");
                flag = 0;
                if(proContextLookupData.getLumPartNumber().startsWith("[RFM-077]-[108W32LED3K-001]-G2-R2M")){
                    flag = 1;
                    proContextLookupData.setLumPartNumber("[RFM-077]-[108W32LED3K-001]-G2-R2M");
                }else if(proContextLookupData.getLumPartNumber().startsWith("[RFM-077]-[108W32LED3K-002]-G2-R3S")){
                    flag = 1;
                    proContextLookupData.setLumPartNumber("[RFM-077]-[108W32LED3K-002]-G2-R3S");
                }else if(proContextLookupData.getLumPartNumber().startsWith("[RFM-077]-[108W32LED3K-002]-T-R3S")){
                    flag = 1;
                    proContextLookupData.setLumPartNumber("[RFM-077]-[108W32LED3K-002]-T-R3S");
                }
            }else if(proContextLookupData.getLumModel().startsWith("RFL")){
                logger.info("RFL");
                proContextLookupData.setLumModel("RFL");
                flag = 0;
                if(proContextLookupData.getLumPartNumber().startsWith("[RFL-053]-[180W80LED3K-003]-G2-R2M")){
                    flag = 1;
                    proContextLookupData.setLumPartNumber("[RFL-053]-[180W80LED3K-003]-G2-R2M");
                }else if(proContextLookupData.getLumPartNumber().startsWith("[RFL-053]-[180W80LED3K-003]-T-R2M")){
                    flag = 1;
                    proContextLookupData.setLumPartNumber("[RFL-053]-[180W80LED3K-003]-T-R2M");
                }
            }

        }
        return flag;
    }

    private String getFixtureName(String proposedContext,String formFixtureCode,String atlasPage,String title,String atlasGroupValue){
        String atlasGroup = null;
        if(atlasGroupValue != null && !atlasGroupValue.equals("") && !atlasGroupValue.equals("0")){
            int size = atlasGroupValue.length();
            if(size == 2){
                atlasGroup = atlasGroupValue;
            }else if(size == 1){
                atlasGroup = "0"+atlasGroupValue;
            }
        }
        if(atlasGroup == null) {
            atlasGroup = Utils.getAtlasGroup(proposedContext);
        }
        String fixtureCode = Utils.getFixtureCode(formFixtureCode.startsWith(" ") ? formFixtureCode.substring(1) : formFixtureCode);
        String fixtureName = atlasPage+"-"+atlasGroup+"-"+title+"-"+fixtureCode;
        return fixtureName;
    }



    public void isBulkImport(EdgeNote edgeNote,String accessToken,InstallMaintenanceLogModel loggingModel){
       try{
           String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
           url = url + PropertiesReader.getProperties().getProperty("com.edge.url.bulkimport.check");
           url = url + "?noteGuid="+edgeNote.getNoteGuid();
           logger.info("Bulk Import Url:"+url);
           ResponseEntity<String> responseEntity = restService.getRequest(url, false, accessToken);
           if(responseEntity.getStatusCode().is2xxSuccessful()){
               logger.info("Current Note is created via Bulk Import.");
               loggingModel.setBulkImport(true);
           }
       }catch (Exception e){
           logger.error("Error in isBulkImport",e);
       }

    }

    public RestTemplate getRestTemplate(){
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setReadTimeout(1000 * 60 * 5);
        simpleClientHttpRequestFactory.setConnectTimeout(1000 * 60 * 5);
        RestTemplate restTemplate = new RestTemplate(simpleClientHttpRequestFactory);
        return restTemplate;
    }


    public void removeSwapPromotedData(String idOnController){
        try{
            logger.info("removeSwapPromotedData Called.....");
            String httpUrl = "/promoted/removeSwapPromoted.html?idOnController="+idOnController;
            logger.info("httpUrl"+httpUrl);
            restService.callPostMethod(httpUrl,HttpMethod.GET,null,true);
            logger.info("removeSwapPromotedData Done.");
        }catch (Exception e){
            logger.error("Error in removeSwapPromotedData",e);
        }
    }

}
