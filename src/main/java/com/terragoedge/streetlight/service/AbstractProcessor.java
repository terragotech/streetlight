package com.terragoedge.streetlight.service;

import com.google.gson.*;
import com.terragoedge.edgeserver.DeviceMacAddress;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.edgeserver.Value;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.dao.ConnectionDAO;
import com.terragoedge.streetlight.dao.StreetlightDao;
import com.terragoedge.streetlight.enumeration.CallType;
import com.terragoedge.streetlight.enumeration.ProcessType;
import com.terragoedge.streetlight.exception.*;
import com.terragoedge.streetlight.json.model.*;
import com.terragoedge.streetlight.logging.InstallMaintenanceLogModel;
import com.terragoedge.streetlight.logging.LoggingModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractProcessor {

    final Logger logger = Logger.getLogger(AbstractProcessor.class);

    StreetlightDao streetlightDao = null;
    RestService restService = null;
    Properties properties = null;
    Gson gson = null;
    JsonParser jsonParser = null;
    ConnectionDAO connectionDAO;

    WeakHashMap<String, String> contextListHashMap = new WeakHashMap<>();
    HashMap<String, CslpDate> cslpDateHashMap = new HashMap<>();

    public AbstractProcessor() {
        this.connectionDAO = ConnectionDAO.INSTANCE;
        this.streetlightDao = new StreetlightDao();
        this.restService = new RestService();
        this.properties = PropertiesReader.getProperties();
        this.gson = new Gson();
        this.jsonParser = new JsonParser();
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
                if (value == null || value.trim().isEmpty() || value.contains("null")) {
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

    public void processDeviceValuesJson(String deviceValuesjson, String idOnController) {
        logger.info("processDeviceValuesJson called start");
        String proposedContextKey = properties.getProperty("streetlight.location.proposedcontext");
        String cslInstallDateKey = properties.getProperty("streetlight.csl.installdate");
        String cslLuminaireDateKey = properties.getProperty("streetlight.csl.luminairedate");
        //String serialNumberKey = properties.getProperty("streetlight.luminaire.serialnumber");
        logger.info("contextKey :" + proposedContextKey);
        logger.info("cslInstallDate :" + cslInstallDateKey);
        logger.info("cslLuminaireDate :" + cslLuminaireDateKey);
        JsonObject jsonObject = new JsonParser().parse(deviceValuesjson).getAsJsonObject();
        logger.info("Device request json:" + gson.toJson(jsonObject));
        JsonArray arr = jsonObject.getAsJsonArray("properties");
        contextListHashMap.clear();
        cslpDateHashMap.clear();
        CslpDate cslpDate = new CslpDate();
        String nodeInstall = null;
        String luminaireDate = null;
        String macAddress = null;
        for (int i = 0; i < arr.size(); i++) {
            JsonObject jsonObject1 = arr.get(i).getAsJsonObject();
            String keyValue = jsonObject1.get("key").getAsString();
            if (keyValue != null && keyValue.equals(proposedContextKey)) {
                String proposedContext = jsonObject1.get("value").getAsString();
                contextListHashMap.put(idOnController, proposedContext);
            }
           else if (keyValue != null && keyValue.equals(cslInstallDateKey)) {
                nodeInstall = jsonObject1.get("value").getAsString();
            }
           else if (keyValue != null && keyValue.equals(cslLuminaireDateKey)) {
                luminaireDate = jsonObject1.get("value").getAsString();
            }else if(keyValue != null && keyValue.equals("userproperty.MacAddress")){
                macAddress = jsonObject1.get("value").getAsString();
            }

        }

        //
        if (nodeInstall != null && !nodeInstall.trim().isEmpty() && nodeInstall.trim().length() > 7) {
            cslpDate.setCslpNodeDate(nodeInstall);
        }
        if (luminaireDate != null && !luminaireDate.trim().isEmpty() && luminaireDate.trim().length() > 7) {
            cslpDate.setCslpLumDate(luminaireDate);
        }
        logger.info("cslpDate :" + gson.toJson(cslpDate));
        cslpDateHashMap.put(idOnController, cslpDate);
        DeviceAttributes deviceAttributes = new DeviceAttributes();
        deviceAttributes.setMacAddress(macAddress);
        deviceAttributes.setIdOnController(idOnController);
        connectionDAO.saveDeviceAttributes(deviceAttributes);
        logger.info("processDeviceValuesJson End");
    }




    public void loadDeviceValues(String idOnController,InstallMaintenanceLogModel installMaintenanceLogModel) throws Exception{
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
                logger.info("LoadDevice Respose :"+response.getBody());
                String responseString = response.getBody();
                int id = processDeviceJson(responseString);
                logger.info("LoadDevice Id :"+id);

                if (id == 0) {
                    logger.info("csl and context hashmap are cleared");
                    cslpDateHashMap.clear();
                    contextListHashMap.clear();
                    throw new NoValueException("Device id:["+idOnController+"] does not exists in SLV server");
                } else {
                    installMaintenanceLogModel.setDeviceId(id);
                    String subDeviceUrl = getDeviceUrl(id);
                    logger.info("subDevice url:" + subDeviceUrl);
                    ResponseEntity<String> responseEntity = restService.getRequest(subDeviceUrl, true, null);
                    if (response.getStatusCodeValue() == 200) {
                        String deviceResponse = responseEntity.getBody();
                        processDeviceValuesJson(deviceResponse, idOnController);
                    }
                }
            }
        } catch ( Exception e) {

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
    public boolean checkMacAddressExists(String macAddress, String idOnController, String nightRideKey, String nightRideValue, LoggingModel loggingModel)
            throws QRCodeAlreadyUsedException, Exception {
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
            logger.info("check mac address exist values:"+values);
            if (values == null || values.size() == 0) {
                loggingModel.setMacAddressUsed(false);
                return false;
            } else {
                boolean isDuplicate = false;
                for (Value value : values) {
                    if(!value.getIdOnController().equals(idOnController)){
                        isDuplicate = true;
                    }
                    if (value.getIdOnController().equals(idOnController) && nightRideKey != null) {
                        sendNightRideToSLV(value.getIdOnController(), nightRideKey, nightRideValue,loggingModel);
                    }
                    stringBuilder.append(value.getIdOnController());
                    stringBuilder.append("\n");
                }
                logger.info("isduplicate:"+isDuplicate);
                if(isDuplicate) {
                    try{
                        DuplicateMacAddress duplicateMacAddress = new DuplicateMacAddress();
                        duplicateMacAddress.setTitle(idOnController);
                        duplicateMacAddress.setMacaddress(macAddress);
                        duplicateMacAddress.setNoteguid(loggingModel.getProcessedNoteId());
                        connectionDAO.saveDuplicateMacAddress(duplicateMacAddress);

                    }catch (Exception e){
                        logger.error("Error in DuplicateMacAddress",e);
                    }


                    try{
                        DuplicateMACAddressEventLog duplicateMACAddressEventLog = new DuplicateMACAddressEventLog();
                        duplicateMACAddressEventLog.setIdOnController(idOnController);
                        duplicateMACAddressEventLog.setMacaddress(macAddress);
                        duplicateMACAddressEventLog.setDeviceList(stringBuilder.toString());
                        duplicateMACAddressEventLog.setNoteGuid(loggingModel.getProcessedNoteId());
                        duplicateMACAddressEventLog.setEventTime(System.currentTimeMillis());
                        connectionDAO.saveMacAddressEventLog(duplicateMACAddressEventLog);
                    }catch (Exception e){
                        logger.error("Error in DuplicateMACAddressEventLog",e);
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
        CslpDate cslpDate = cslpDateHashMap.get(idOnContoller);
        logger.info("CslpDate:" + cslpDate);
        if (cslpDate != null) {
            String val = cslpDate.getCslpLumDate();
            if (val != null) {
                return true;
            }
        }
        return false;
    }


    public boolean isNodeDatePresent(String idOnContoller) {
        logger.info("isNodePresent Json:"+gson.toJson(cslpDateHashMap));
        CslpDate cslpDate = cslpDateHashMap.get(idOnContoller);
        return cslpDate != null && cslpDate.getCslpNodeDate() != null;
    }


    protected void addOtherParams(EdgeNote edgeNote, List<Object> paramsList, String idOnContoller, String utilLocId, boolean isNew, String fixerQrScanValue, String macAddress, InstallMaintenanceLogModel loggingModel) {
        // luminaire.installdate - 2017-09-07 09:47:35
        String installStatus = null;
        if (fixerQrScanValue != null && fixerQrScanValue.trim().length() > 0 && !loggingModel.isFixtureQRSame()) {
            logger.info("Fixture QR scan not empty and set luminare installdate" + dateFormat(edgeNote.getCreatedDateTime()));
            logger.info("Fixture QR scan not empty and set cslp.lum.install.date" + dateFormat(edgeNote.getCreatedDateTime()));
            boolean isLumDate = isLumDatePresent(idOnContoller);
            boolean isButtonPhotoCelll = loggingModel.isButtonPhotoCell();
            if (!isLumDate && !isButtonPhotoCelll) {
                addStreetLightData("cslp.lum.install.date", dateFormat(edgeNote.getCreatedDateTime()), paramsList);
            }
            if(!isButtonPhotoCelll) {
                addStreetLightData("luminaire.installdate", dateFormat(edgeNote.getCreatedDateTime()), paramsList);
            }
            if (macAddress == null || macAddress.trim().isEmpty()) {
                installStatus = "Fixture Only";
            } else {
                installStatus = "Installed";
            }


        } else if (macAddress != null && macAddress.trim().length() > 0) {
            installStatus = "Installed";
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
                installStatus = "Verified";

            }
        }

        logger.info("DimmingGroupName After:" + edgeNotebookName);

       /* if (dimmingGroupName != null && dimmingGroupName.trim().toLowerCase().contains("acorns")) {
            edgeNotebookName = edgeNotebookName +" Acorns";
        }*/
        if (installStatus != null) {
            addStreetLightData("installStatus", loggingModel.isButtonPhotoCell() ? "Verified" : installStatus, paramsList);
        }


        addStreetLightData("DimmingGroupName", edgeNotebookName, paramsList);
        // addStreetLightData("DimmingGroupName", "Group Calendar 1", paramsList);
    }

    protected void addFixtureQrScanData(String key, String value, List<Object> paramsList) {
        paramsList.add("attribute=" + key.trim());
        paramsList.add("value=" + value.trim());
    }

    protected void addStreetLightData(String key, String value, List<Object> paramsList) {
        paramsList.add("valueName=" + key.trim());
        paramsList.add("value=" + value.trim());
    }


    protected String dateFormat(Long dateTime) {
        Date date = new Date(Long.valueOf(dateTime));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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

    public boolean stringContainsNumber( String s )
    {
        if(s != null && !s.trim().isEmpty()){
            Pattern p = Pattern.compile( "[0-9]" );
            Matcher m = p.matcher( s );

            return m.find();
        }
        return false;
    }

    public void buildFixtureStreetLightData(String data, List<Object> paramsList, EdgeNote edgeNote, SlvServerData slvServerData,LoggingModel loggingModel)
            throws InValidBarCodeException {
        String[] fixtureInfo = data.split(",");
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
            logger.info("luminaire.brand "+fixtureInfo[0]);
            if (fixtureInfo[1].trim().length() > fixtureInfo[2].trim().length() && !fixtureInfo[0].trim().contains("LV Manufacturing")) {
                model = fixtureInfo[2].trim();
                partNumber = fixtureInfo[1].trim();
                logger.info("device.luminaire.partnumber "+partNumber);
                logger.info("luminaire.model "+model);
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

            addStreetLightData("power", powerVal, paramsList);

            String dimmingGroupName = contextListHashMap.get(loggingModel.getIdOnController());
            if(dimmingGroupName != null && stringContainsNumber(fixtureInfo[5]) && (dimmingGroupName.startsWith("3") || dimmingGroupName.startsWith("11") || dimmingGroupName.startsWith("12"))){
                fixtureInfo[5] = "LED";
            }

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

        } else {
            /*throw new InValidBarCodeException(
                    "Fixture MAC address is not valid (" + edgeNote.getTitle() + "). Value is:" + data);*/
        }
    }


    protected int setDeviceValues(List<Object> paramsList,SLVTransactionLogs slvTransactionLogs) {
        int errorCode = -1;
        try{
            String mainUrl = properties.getProperty("streetlight.slv.url.main");
            String updateDeviceValues = properties.getProperty("streetlight.slv.url.updatedevice");
            String url = mainUrl + updateDeviceValues;

            paramsList.add("ser=json");
            String params = StringUtils.join(paramsList, "&");
            url = url + "&" + params;
            logger.info("SetDevice method called");
            logger.info("SetDevice url:"+url);
            setSLVTransactionLogs(slvTransactionLogs,url,CallType.SET_DEVICE);
            ResponseEntity<String> response = restService.getPostRequest(url, null);
            String responseString = response.getBody();
            setResponseDetails(slvTransactionLogs,responseString);
            JsonObject replaceOlcResponse = (JsonObject) jsonParser.parse(responseString);
            errorCode = replaceOlcResponse.get("errorCode").getAsInt();
       }catch (Exception e){
            setResponseDetails(slvTransactionLogs,"Error in setDeviceValues:"+e.getMessage());
           logger.error("Error in setDeviceValues",e);
       }finally {
           streetlightDao.insertTransactionLogs(slvTransactionLogs);
       }

        return errorCode;
    }

    private void setSLVTransactionLogs(SLVTransactionLogs slvTransactionLogs, String request,CallType  callType){
        slvTransactionLogs.setRequestDetails(request);
        slvTransactionLogs.setTypeOfCall(callType);
    }

    private void setResponseDetails(SLVTransactionLogs slvTransactionLogs,String responseString){
        slvTransactionLogs.setResponseBody(responseString);
    }



    /**
     * Calls ReplaceOLCs
     *
     * @throws ReplaceOLCFailedException
     */
    public void replaceOLC(String controllerStrIdValue, String idOnController, String macAddress,SLVTransactionLogs slvTransactionLogs)
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
            setSLVTransactionLogs(slvTransactionLogs,url,CallType.REPLACE_OLC);
            ResponseEntity<String> response = restService.getPostRequest(url, null);
            String responseString = response.getBody();
            setResponseDetails(slvTransactionLogs,responseString);
            JsonObject replaceOlcResponse = (JsonObject) jsonParser.parse(responseString);
            String errorStatus = replaceOlcResponse.get("status").getAsString();
            logger.info("Replace OLC Process End.");
            // As per doc, errorcode is 0 for success. Otherwise, its not success.
            if (errorStatus.equals("ERROR")) {
                String value = replaceOlcResponse.get("value").getAsString();
                throw new ReplaceOLCFailedException(value);
            } else {

                if (macAddress != null) {
                    logger.info("Clear device process starts.");
                    logger.info("Clear device process End.");
                }

            }

        } catch (Exception e) {
            logger.error("Error in replaceOLC", e);
            throw new ReplaceOLCFailedException(e.getMessage());
        }finally {
           streetlightDao.insertTransactionLogs(slvTransactionLogs);
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


    protected void loadDefaultVal(EdgeNote edgeNote, LoggingModel loggingModel) {
        loggingModel.setIdOnController(edgeNote.getTitle());
        String controllerStrId = properties.getProperty("streetlight.slv.controllerstrid");
        loggingModel.setControllerSrtId(controllerStrId);
    }

    private void sendNightRideToSLV(String idOnController, String nightRideKey, String nightRideValue,LoggingModel loggingModel) {
        List<Object> paramsList = new ArrayList<>();
        String controllerStrId = properties.getProperty("streetlight.slv.controllerstrid");
        paramsList.add("idOnController=" + idOnController);
        paramsList.add("controllerStrId=" + controllerStrId);
        if (nightRideValue != null) {
            addStreetLightData(nightRideKey, nightRideValue, paramsList);
            SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(loggingModel);
            int errorCode = setDeviceValues(paramsList,slvTransactionLogs);
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


    public SLVTransactionLogs getSLVTransactionLogs(LoggingModel loggingModel){
        SLVTransactionLogs slvTransactionLogs = new SLVTransactionLogs();
        slvTransactionLogs.setNoteGuid(loggingModel.getProcessedNoteId());
        slvTransactionLogs.setTitle(loggingModel.getNoteName());
        slvTransactionLogs.setCreatedDateTime(Long.valueOf(loggingModel.getCreatedDatetime()));
        slvTransactionLogs.setParentNoteGuid(loggingModel.getParentNoteId());
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


    public DeviceAttributes getDeviceValues(InstallMaintenanceLogModel installMaintenanceLogModel){
        try{
            if(installMaintenanceLogModel.getDeviceId() > 0){
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
                    logger.info("Get MAC Address and installStatus Response :"+response.getBody());
                    String responseString = response.getBody();
                    JsonArray jsonArray = (JsonArray)jsonParser.parse(responseString);
                    for(JsonElement macAddressJson : jsonArray){
                        JsonObject macAddressJsonObject = macAddressJson.getAsJsonObject();
                        if(macAddressJsonObject.get("name") != null){
                            String paramName = macAddressJsonObject.get("name").getAsString();
                            if(paramName.equals("MacAddress")){
                                if(macAddressJsonObject.get("value") != null){
                                    String macAddress = macAddressJsonObject.get("value").getAsString();
                                    if(macAddress != null && !macAddress.trim().isEmpty()){
                                        deviceAttributes.setMacAddress(macAddress);
                                    }
                                }
                            }else if(paramName.equals("installStatus")){
                                String installStatus = macAddressJsonObject.get("value").getAsString();
                                if(installStatus != null && !installStatus.trim().isEmpty()){
                                    deviceAttributes.setInstallStatus(installStatus);
                                }
                            }

                        }

                    }
                }
                logger.info(deviceAttributes);
                return deviceAttributes;
            }
        }catch (Exception e){
            logger.error("Error in getDeviceValues",e);
        }
        return null;
    }



}
