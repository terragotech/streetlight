package com.terragoedge.streetlight.service;

import com.google.gson.*;
import com.terragoedge.edgeserver.DeviceMacAddress;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.edgeserver.Value;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.dao.StreetlightDao;
import com.terragoedge.streetlight.exception.*;
import com.terragoedge.streetlight.logging.InstallMaintenanceLogModel;
import com.terragoedge.streetlight.logging.LoggingModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import java.text.SimpleDateFormat;
import java.util.*;

public abstract class AbstractProcessor {

    final Logger logger = Logger.getLogger(AbstractProcessor.class);

    StreetlightDao streetlightDao = null;
    RestService restService = null;
    Properties properties = null;
    Gson gson = null;
    JsonParser jsonParser = null;
    EdgeMailService edgeMailService = null;

    WeakHashMap<String, String> contextListHashMap = new WeakHashMap<>();

    public AbstractProcessor() {
        this.streetlightDao = new StreetlightDao();
        this.restService = new RestService();
        this.properties = PropertiesReader.getProperties();
        this.gson = new Gson();
        this.edgeMailService = new EdgeMailService();
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
                if (value == null || value.trim().isEmpty()||value.contains("null")) {
                    throw new NoValueException("Value is Empty or null." + value);
                }
                return value;
            }
        }

        throw new NoValueException(id + " is not found.");
    }


    /**
     * Load Mac address and corresponding IdOnController from SLV Server
     *
     * @throws Exception
     */
    public boolean checkMacAddressExists(String macAddress, String idOnController, String nightRideKey, String nightRideValue)
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
            if (values == null || values.size() == 0) {
                return false;
            } else {
                for (Value value : values) {
                    if (value.getIdOnController().equals(idOnController)) {
                        sendNightRideToSLV(value.getIdOnController(), nightRideKey, nightRideValue);
                    }
                    stringBuilder.append(value.getIdOnController());
                    stringBuilder.append("\n");
                }
            }
            throw new QRCodeAlreadyUsedException(stringBuilder.toString(), macAddress);
        } else {
            throw new Exception(response.getBody());
        }

    }


    protected void addOtherParams(EdgeNote edgeNote, List<Object> paramsList, String idOnContoller, String utilLocId, boolean isNew, String fixerQrScanValue) {
        // luminaire.installdate - 2017-09-07 09:47:35
        addStreetLightData("install.date", dateFormat(edgeNote.getCreatedDateTime()), paramsList);
        /*// controller.installdate - 2017/10/10
        addStreetLightData("luminaire.installdate", dateFormat(edgeNote.getCreatedDateTime()), paramsList);*/

        if (isNew) {
            addStreetLightData("cslp.node.install.date", dateFormat(edgeNote.getCreatedDateTime()), paramsList);
            if (fixerQrScanValue != null && fixerQrScanValue.trim().length() > 0) {
                logger.info("Fixture QR scan not empty and set luminare installdate" + dateFormat(edgeNote.getCreatedDateTime()));
                logger.info("Fixture QR scan not empty and set cslp.lum.install.date" + dateFormat(edgeNote.getCreatedDateTime()));
                addStreetLightData("cslp.lum.install.date", dateFormat(edgeNote.getCreatedDateTime()), paramsList);
                addStreetLightData("installStatus", "Installed", paramsList);
                // controller.installdate - 2017/10/10
                addStreetLightData("luminaire.installdate", dateFormat(edgeNote.getCreatedDateTime()), paramsList);
            }else {
               // addStreetLightData("installStatus", "Verified", paramsList);
                addStreetLightData("installStatus", "Installed", paramsList);
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
            if(dimmingGroupName.contains("Node Only") && isNew){
                addStreetLightData("installStatus", "Verified", paramsList);
            }
        }

        logger.info("DimmingGroupName After:" + edgeNotebookName);

       /* if (dimmingGroupName != null && dimmingGroupName.trim().toLowerCase().contains("acorns")) {
            edgeNotebookName = edgeNotebookName +" Acorns";
        }*/


        addStreetLightData("DimmingGroupName", edgeNotebookName, paramsList);
        // addStreetLightData("DimmingGroupName", "Group Calendar 1", paramsList);
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

    public void buildFixtureStreetLightData(String data, List<Object> paramsList, EdgeNote edgeNote)
            throws InValidBarCodeException {
        String[] fixtureInfo = data.split(",");
        logger.info("Fixture QR Scan Val lenght" + fixtureInfo.length);
        if (fixtureInfo.length >= 13) {
            addStreetLightData("luminaire.brand", fixtureInfo[0], paramsList);
            /**
             * As per Mail conversion, In the older data, the luminaire model was the
             * shorter version of the fixture, so for the General Electric fixtures it was
             * ERLH. The Luminaire Part Number would be the longer more detailed number.
             */
            String partNumber = fixtureInfo[1].trim();
            String model = fixtureInfo[2].trim();
            if (fixtureInfo[1].trim().length() <= fixtureInfo[2].trim().length()) {
                model = fixtureInfo[1].trim();
                partNumber = fixtureInfo[2].trim();
            }
            addStreetLightData("device.luminaire.partnumber", partNumber, paramsList);
            addStreetLightData("luminaire.model", model, paramsList);
            addStreetLightData("device.luminaire.manufacturedate", fixtureInfo[3], paramsList);
            String powerVal = fixtureInfo[4];
            if (powerVal != null && !powerVal.isEmpty()) {
                powerVal = powerVal.replaceAll("W", "");
                powerVal = powerVal.replaceAll("w", "");
            }

            addStreetLightData("power", powerVal, paramsList);
            addStreetLightData("comed.litetype", fixtureInfo[5], paramsList);
            // dailyReportCSV.setFixtureType(fixtureInfo[5]);
            addStreetLightData("device.luminaire.colortemp", fixtureInfo[6], paramsList);
            addStreetLightData("device.luminaire.lumenoutput", fixtureInfo[7], paramsList);
            addStreetLightData("luminaire.DistributionType", fixtureInfo[8], paramsList);
            addStreetLightData("luminaire.colorcode", fixtureInfo[9], paramsList);
            addStreetLightData("device.luminaire.drivermanufacturer", fixtureInfo[10], paramsList);
            addStreetLightData("device.luminaire.driverpartnumber", fixtureInfo[11], paramsList);
            addStreetLightData("ballast.dimmingtype", fixtureInfo[12], paramsList);

        } else {
            /*throw new InValidBarCodeException(
                    "Fixture MAC address is not valid (" + edgeNote.getTitle() + "). Value is:" + data);*/
        }
    }


    protected int setDeviceValues(List<Object> paramsList) {
        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String updateDeviceValues = properties.getProperty("streetlight.slv.url.updatedevice");
        String url = mainUrl + updateDeviceValues;

        paramsList.add("ser=json");
        String params = StringUtils.join(paramsList, "&");
        url = url + "&" + params;
        ResponseEntity<String> response = restService.getPostRequest(url, null);
        String responseString = response.getBody();
        JsonObject replaceOlcResponse = (JsonObject) jsonParser.parse(responseString);
        int errorCode = replaceOlcResponse.get("errorCode").getAsInt();
        return errorCode;
    }


    /**
     * Calls ReplaceOLCs
     *
     * @throws ReplaceOLCFailedException
     */
    public void replaceOLC(String controllerStrIdValue, String idOnController, String macAddress)
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
            ResponseEntity<String> response = restService.getPostRequest(url, null);
            String responseString = response.getBody();
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
                    clearAndUpdateDeviceData(idOnController, controllerStrId);
                    logger.info("Clear device process End.");
                }

            }

        } catch (Exception e) {
            logger.error("Error in replaceOLC", e);
            throw new ReplaceOLCFailedException(e.getMessage());
        }

    }


    public void clearAndUpdateDeviceData(String idOnController, String controllerStrId) {
        try {
            String mainUrl = properties.getProperty("streetlight.slv.url.main");
            String updateDeviceValues = properties.getProperty("streetlight.slv.url.updatedevice");
            String url = mainUrl + updateDeviceValues;

            List<Object> paramsList = new ArrayList<Object>();
            paramsList.add("controllerStrId=" + controllerStrId);
            paramsList.add("idOnController=" + idOnController);
            addStreetLightData("device.node.serialnumber", "", paramsList);
            addStreetLightData("device.node.hwversion", "", paramsList);
            addStreetLightData("device.node.swversion", "", paramsList);
            addStreetLightData("device.nic.serialnumber", "", paramsList);
            addStreetLightData("device.nic.swversion", "", paramsList);
            addStreetLightData("device.nic.hwversion", "", paramsList);
            addStreetLightData("device.nic.currentnode", "", paramsList);
            addStreetLightData("device.nic.fallbackmode", "", paramsList);
            addStreetLightData("device.node.manufdate", "", paramsList);
            addStreetLightData("device.node.name", "", paramsList);
            addStreetLightData("device.node.manufacturer", "", paramsList);
            addStreetLightData("device.uiqid", "", paramsList);
            addStreetLightData("SoftwareVersion", "", paramsList);
            addStreetLightData("device.meter.programid", "", paramsList);
            addStreetLightData("device.nic.catalog", "", paramsList);

            paramsList.add("doLog=true");
            paramsList.add("ser=json");
            String params = StringUtils.join(paramsList, "&");
            url = url + "&" + params;
            ResponseEntity<String> response = restService.getPostRequest(url, null);
            String responseString = response.getBody();
            JsonObject replaceOlcResponse = (JsonObject) jsonParser.parse(responseString);
            int errorCode = replaceOlcResponse.get("errorCode").getAsInt();
            if (errorCode == 0) {
                // success
            } else {
                // failure
            }
        } catch (Exception e) {
            logger.error("Error in clearAndUpdateDeviceData", e);
        }

    }


    protected String validateMacAddress(String existingNodeMacAddress, String idOnController, String controllerStrId) throws QRCodeNotMatchedException {
        String mainUrl = properties.getProperty("streetlight.url.main");
        String getMacAddress = properties.getProperty("streetlight.slv.url.getmacaddress");
        String url = mainUrl + getMacAddress;

        List<Object> paramsList = new ArrayList<Object>();
        paramsList.add("idOnController=" + idOnController);
        paramsList.add("controllerStrId=" + controllerStrId);
        paramsList.add("valueName=MacAddress");
        paramsList.add("valueName=comment");
        paramsList.add("ser=json");
        String params = StringUtils.join(paramsList, "&");
        url = url + "&" + params;
        ResponseEntity<String> response = restService.getPostRequest(url, null);
        if (response.getStatusCode().is2xxSuccessful()) {
            String responseString = response.getBody();
            JsonElement jsonElement = jsonParser.parse(responseString);
            if (jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                if (jsonArray.size() > 0) {
                    if (jsonArray.get(0).getAsString().toLowerCase().equals(existingNodeMacAddress.toLowerCase())) {
                        String comment = jsonArray.get(1).getAsString();
                        return comment;
                    } else {
                        // Throws given MAC Address not matched
                        throw new QRCodeNotMatchedException(idOnController, existingNodeMacAddress);
                    }

                } else {
                    throw new QRCodeNotMatchedException(idOnController, existingNodeMacAddress);
                }
            } else if (jsonElement.isJsonObject()) {
                return validateMACAddress(existingNodeMacAddress, idOnController, null);

            }
        } else {
            return validateMACAddress(existingNodeMacAddress, idOnController, null);
        }
        throw new QRCodeNotMatchedException(idOnController, existingNodeMacAddress);
    }


    /**
     * Check MAC Address is present in given IdonController or not and also if
     * mathches get comment
     *
     * @param existingNodeMacAddress
     * @param idOnController
     * @param geoZoneId
     * @return
     * @throws QRCodeNotMatchedException
     */
    private String validateMACAddress(String existingNodeMacAddress, String idOnController, String geoZoneId)
            throws QRCodeNotMatchedException {
        String mainUrl = properties.getProperty("streetlight.url.main");
        String geoZoneDevices = properties.getProperty("streetlight.slv.url.getgeozone.devices");
        String url = mainUrl + geoZoneDevices;

        List<Object> paramsList = new ArrayList<Object>();
        if (geoZoneId != null) {
            paramsList.add("geoZoneId=" + geoZoneId);
        }

        paramsList.add("valueNames=idOnController");
        paramsList.add("valueNames=comment");
        paramsList.add("valueNames=MacAddress");
        paramsList.add("ser=json");
        String params = StringUtils.join(paramsList, "&");
        url = url + "&" + params;
        ResponseEntity<String> response = restService.getPostRequest(url, null);
        if (response.getStatusCode().is2xxSuccessful()) {
            String geoZoneDeviceDetails = response.getBody();
            JsonObject jsonObject = (JsonObject) jsonParser.parse(geoZoneDeviceDetails);
            JsonArray deviceValuesAsArray = jsonObject.get("values").getAsJsonArray();
            int totalSize = deviceValuesAsArray.size();
            for (int i = 0; i < totalSize; i++) {
                JsonArray deviceValues = deviceValuesAsArray.get(i).getAsJsonArray();
                if (deviceValues.get(0).getAsString().equals(idOnController)) {
                    if (deviceValues.get(2).getAsString().equals(existingNodeMacAddress)) {
                        String comment = deviceValues.get(1).getAsString();
                        return comment;
                    }
                }
            }
            // Throws given MAC Address not matched
            throw new QRCodeNotMatchedException(idOnController, existingNodeMacAddress);
        }
        return null;
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

    private void sendNightRideToSLV(String idOnController, String nightRideKey, String nightRideValue) {
        List<Object> paramsList = new ArrayList<>();
        String controllerStrId = properties.getProperty("streetlight.slv.controllerstrid");
        paramsList.add("idOnController=" + idOnController);
        paramsList.add("controllerStrId=" + controllerStrId);
        if (nightRideValue != null) {
            addStreetLightData(nightRideKey, nightRideValue, paramsList);
            int errorCode = setDeviceValues(paramsList);
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
}
