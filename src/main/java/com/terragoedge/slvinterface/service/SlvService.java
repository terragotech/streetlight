package com.terragoedge.slvinterface.service;

import com.google.gson.*;
import com.terragoedge.slvinterface.dao.ConnectionDAO;
import com.terragoedge.slvinterface.dao.CsvConnectionDao;
import com.terragoedge.slvinterface.dao.tables.DuplicateMacAddress;
import com.terragoedge.slvinterface.dao.tables.GeozoneEntity;
import com.terragoedge.slvinterface.dao.tables.SlvDevice;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetail;
import com.terragoedge.slvinterface.enumeration.Status;
import com.terragoedge.slvinterface.exception.*;
import com.terragoedge.slvinterface.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import com.terragoedge.slvinterface.utils.EmailUtils;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

public class SlvService extends AbstractSlvService {
    private Logger logger = Logger.getLogger(SlvService.class);
    private ConnectionDAO connectionDAO;
    Properties properties;
    private Gson gson;

    public SlvService() {
        properties = PropertiesReader.getProperties();
        gson = new Gson();
        connectionDAO = ConnectionDAO.INSTANCE;
    }

    public void processSlv(JPSWorkflowModel jpsWorkflowModel, EdgeNote edgeNote) throws Exception {
        logger.info("ProcessSlv method start.");
//        GeozoneEntity geozoneEntity = getGeozoneEntity(jpsWorkflowModel);
//        logger.info("---------------------Geozone info start---------------------------");
//        logger.info(gson.toJson(geozoneEntity));
//        logger.info("---------------------Geozone info end---------------------------");
//        jpsWorkflowModel.setGeozoneId(geozoneEntity.getStreetGeozoneId());
        JsonArray devices = checkDeviceExist(jpsWorkflowModel.getIdOnController());// search idoncontroller on slv - ** rest call
        //start
        int deviceId = -1;
        if (devices == null || devices.size() == 0) {
            ResponseEntity<String> responseEntity = createDevice(edgeNote, jpsWorkflowModel);// create device in slv
            if (responseEntity != null) {
                logger.info("Create device Json response" + responseEntity.getBody());
                saveDevice(jpsWorkflowModel, edgeNote, responseEntity);// save or update slvdevice in local db
                if (responseEntity.getStatusCode() == HttpStatus.OK) {
                    JsonObject jsonObject = new JsonParser().parse(responseEntity.getBody()).getAsJsonObject();
                    deviceId = jsonObject.get("id").getAsInt();
                }
            } else {
                throw new Exception("Device creation response return null: " + edgeNote.getTitle());
            }
        } else {
            deviceId = devices.get(0).getAsJsonObject().get("id").getAsInt();
        }
        /*if (jpsWorkflowModel.getOldPoleNumber() != null && !jpsWorkflowModel.getOldPoleNumber().isEmpty()) {
            replaceOLC(jpsWorkflowModel.getControllerStrId(), jpsWorkflowModel.getOldPoleNumber(), "", edgeNote, jpsWorkflowModel);
            deleteDevice(jpsWorkflowModel);
        }*/
        processDeviceValues(jpsWorkflowModel, true, edgeNote);
        processMacAddress(jpsWorkflowModel, edgeNote, deviceId);
        logger.info("Going to call delete device");
    }

    private void processDeviceValues(JPSWorkflowModel jpsWorkflowModel, boolean devicePresentInSlv, EdgeNote edgeNote) throws Exception {
        if (properties.getProperty("streetlights.setdevice.enable").equals("true")) {
            SlvSyncDetail dbSyncDetail = connectionDAO.getSlvSyncDetail(jpsWorkflowModel.getIdOnController());
            if (devicePresentInSlv) { // if false, then no need to check device values are same or not
                logger.info("Given fixtures already exist in slv.");
                if (dbSyncDetail == null) {// get device values from local dp and check
                    logger.info("Given fixture value not exist in local db");
                    ResponseEntity<String> responseEntity = callSetDeviceValues(jpsWorkflowModel);// call set device values - ** rest call
                    SlvSyncDetail slvSyncDetail = new SlvSyncDetail();// save slvSyncDetail in local db
                    saveSlvSyncDetail(slvSyncDetail, edgeNote, jpsWorkflowModel, responseEntity, false);
                    if (responseEntity.getStatusCode() != HttpStatus.OK) {
                        logger.info("Error in set device values of fixture's info exist in local db");
                        throw new Exception("Error in set device values");
                    }
                } else {
                    logger.info("Given fixture value exist in local db");
                    if (!dbSyncDetail.getDeviceDetails().equals(gson.toJson(jpsWorkflowModel))) {// check local and current device details changed or not
                        logger.info("Current fixtures and previous revision values are mismatch.");
                        ResponseEntity<String> responseEntity = callSetDeviceValues(jpsWorkflowModel);// call set device values - ** rest call
                        saveSlvSyncDetail(dbSyncDetail, edgeNote, jpsWorkflowModel, responseEntity, true);
                        if (responseEntity.getStatusCode() != HttpStatus.OK) {
                            logger.info("Error in set device values of fixture's info exist in local db");
                            throw new Exception("Error in set device values");
                        }
                    } else {
                        logger.info("Current fixtures and previous revision values are same.");
                    }
                }
            } /*else {
                ResponseEntity<String> responseEntity = callSetDeviceValues(jpsWorkflowModel);// call set device values
                SlvSyncDetail slvSyncDetail = (dbSyncDetail == null) ? new SlvSyncDetail() : dbSyncDetail;
                saveSlvSyncDetail(slvSyncDetail, edgeNote, jpsWorkflowModel, responseEntity, (dbSyncDetail == null) ? false : true);// save or update slvSyncDetail in local db
                if (responseEntity.getStatusCode() != HttpStatus.OK) {
                    throw new Exception("Error in set device values");
                }
            }*/
        }
    }


    private void processMacAddress(JPSWorkflowModel jpsWorkflowModel, EdgeNote edgeNote, int deviceID) throws ReplaceOLCFailedException {
        if (deviceID == -1) {
            return;
        }
        if (properties.getProperty("streetlights.replaceolc.enable").equals("true")) {
            List<Value> values = checkMacAddressExists(jpsWorkflowModel.getMacAddress());// check mac address already present
            if (values != null && values.size() > 0) {
                logger.info("Given fixture's macaddress is already present");
                String existingPoleNumber = "";
                boolean isDuplicateMacAddress = false;
                for (Value value : values) {
                    if (!value.getIdOnController().equals(jpsWorkflowModel.getIdOnController())) { // check assigned pole and current pole are same
                        isDuplicateMacAddress = true;
                        existingPoleNumber = value.getIdOnController();
                    }
                }
                logger.info("Existing pole Number :" + existingPoleNumber);
                logger.info("isDuplicateMacAddress :" + isDuplicateMacAddress);
                if (isDuplicateMacAddress) {
                    // save duplicate mac address to local db
                    DuplicateMacAddress duplicateMacAddress = new DuplicateMacAddress();
                    duplicateMacAddress.setExistingPoleNumber(existingPoleNumber);
                    duplicateMacAddress.setMacAddress(jpsWorkflowModel.getMacAddress());
                    duplicateMacAddress.setNoteguid(edgeNote.getNoteGuid());
                    duplicateMacAddress.setPoleNumber(jpsWorkflowModel.getIdOnController());
                    duplicateMacAddress.setProcessedDateTime(System.currentTimeMillis());
                    duplicateMacAddress.setTitle(edgeNote.getTitle());
                    logger.info("Going to insert duplicateMacAddress Json:" + gson.toJson(duplicateMacAddress));
                    connectionDAO.saveDuplicateMacAddress(duplicateMacAddress);
                } else {
                    logger.info("Given Macaddress assigned in same idoncontroller");
                }
            } else {
                logger.info("check device has another mac");
                ResponseEntity<String> responseEntity = getDeviceData(deviceID);
                if (responseEntity.getStatusCode() == HttpStatus.OK) {
                    JsonArray macvalues = new JsonParser().parse(responseEntity.getBody()).getAsJsonArray();
                    logger.info("Device has another mac address response :" + gson.toJson(macvalues));
                    if (macvalues != null && macvalues.size() == 1) {
                        JsonObject jsonObject = macvalues.get(0).getAsJsonObject();
                        String mac = null;
                        if (jsonObject != null && !jsonObject.isJsonNull()) {
                            mac = jsonObject.get("value").getAsString();
                        }
                        if (mac == null || mac.equals("")) {
                            logger.info("MacAddress is null");
                            replaceOLC(jpsWorkflowModel.getControllerStrId(), jpsWorkflowModel.getIdOnController(), jpsWorkflowModel.getMacAddress(), edgeNote, jpsWorkflowModel);
                        } else if (!mac.equals(jpsWorkflowModel.getMacAddress())) {
                            logger.info("slv and edge having different mac address, Ready to called empty replace olc and macaddress");
                            replaceOLC(jpsWorkflowModel.getControllerStrId(), jpsWorkflowModel.getIdOnController(), "", edgeNote, jpsWorkflowModel);
                            replaceOLC(jpsWorkflowModel.getControllerStrId(), jpsWorkflowModel.getIdOnController(), jpsWorkflowModel.getMacAddress(), edgeNote, jpsWorkflowModel);
                        }
                    } else if (macvalues == null || macvalues.size() == 0) {
                        logger.info("mac address not present in slv so update mac address");
                        replaceOLC(jpsWorkflowModel.getControllerStrId(), jpsWorkflowModel.getIdOnController(), jpsWorkflowModel.getMacAddress(), edgeNote, jpsWorkflowModel);
                    } else {
                        logger.error("slv device having more mac address pole: " + jpsWorkflowModel.getIdOnController());
                    }
                } else if (responseEntity.getStatusCode() == HttpStatus.NOT_FOUND) {// device doesn't had mac address
                    logger.info("Device doesn't had mac address");
                    replaceOLC(jpsWorkflowModel.getControllerStrId(), jpsWorkflowModel.getIdOnController(), jpsWorkflowModel.getMacAddress(), edgeNote, jpsWorkflowModel);
                } else {
                    logger.error("error while fetching device data for this idoncontroller: " + jpsWorkflowModel.getIdOnController());
                    logger.error("error while fetching device data for this device id: " + deviceID);
                }
            }
        }
    }

    @Override
    public void buildFixtureStreetLightData(String data, List<Object> paramsList, EdgeNote edgeNote) throws InValidBarCodeException {

    }

    private void saveDevice(JPSWorkflowModel jpsWorkflowModel, EdgeNote edgeNote, ResponseEntity<String> responseEntity) {
        SlvDevice dbSlvDevice = connectionDAO.getSlvDevice(jpsWorkflowModel.getIdOnController());
        if (dbSlvDevice == null) {
            SlvDevice slvDevice = new SlvDevice();
            slvDevice.setTitle(edgeNote.getTitle());
            slvDevice.setCreatedDateTime(edgeNote.getCreatedDateTime());
            slvDevice.setNoteguid(edgeNote.getNoteGuid());
            slvDevice.setProcessedDateTime(System.currentTimeMillis());
            slvDevice.setStatus((responseEntity.getStatusCode() == HttpStatus.OK) ? Status.Success : Status.Failure);
            String response = responseEntity.getBody();
            slvDevice.setSlvResponse(response);
            slvDevice.setPoleNumber(jpsWorkflowModel.getIdOnController());
            slvDevice.setOldPoleNumber(jpsWorkflowModel.getOldPoleNumber());
            connectionDAO.saveSlvDevice(slvDevice);
        } else {
            dbSlvDevice.setTitle(edgeNote.getTitle());
            dbSlvDevice.setCreatedDateTime(edgeNote.getCreatedDateTime());
            dbSlvDevice.setNoteguid(edgeNote.getNoteGuid());
            dbSlvDevice.setProcessedDateTime(System.currentTimeMillis());
            dbSlvDevice.setStatus((responseEntity.getStatusCode() == HttpStatus.OK) ? Status.Success : Status.Failure);
            dbSlvDevice.setSlvResponse(responseEntity.getBody());
            dbSlvDevice.setPoleNumber(jpsWorkflowModel.getIdOnController());
            dbSlvDevice.setOldPoleNumber(jpsWorkflowModel.getOldPoleNumber());
            connectionDAO.updateSlvDevice(dbSlvDevice);
        }
    }

    protected void addStreetLightData(String key, String value, List<Object> paramsList) {
        paramsList.add("valueName=" + key.trim());
        paramsList.add("value=" + ((value != null) ? value.trim() : ""));
    }

    private ResponseEntity<String> callSetDeviceValues(JPSWorkflowModel jpsWorkflowModel) {
        List<Object> paramList = new ArrayList<>();
        paramList.add("idOnController=" + jpsWorkflowModel.getIdOnController());
        paramList.add("controllerStrId=" + jpsWorkflowModel.getControllerStrId());
        addStreetLightData("address1", jpsWorkflowModel.getAddress1(), paramList);
        addStreetLightData("location.streetdescription", jpsWorkflowModel.getStreetdescription(), paramList);
        addStreetLightData("categoryStrId", jpsWorkflowModel.getCategoryStrId(), paramList);
        addStreetLightData("location.city", jpsWorkflowModel.getCity(), paramList);
        addStreetLightData("dimmingGroupName", jpsWorkflowModel.getDimmingGroupName(), paramList);
        addStreetLightData("provider.name", jpsWorkflowModel.getProvider_name(), paramList);
//        addStreetLightData("geoZone path", jpsWorkflowModel.getGeozonePath(), paramList);
        addStreetLightData("network.highvoltagethreshold", String.valueOf(jpsWorkflowModel.getHighvoltagethreshold()), paramList);
        addStreetLightData("idOnController", jpsWorkflowModel.getIdOnController(), paramList);
        addStreetLightData("installStatus", jpsWorkflowModel.getInstallStatus(), paramList);
        addStreetLightData("lampType", jpsWorkflowModel.getLampType(), paramList);
        addStreetLightData("power", jpsWorkflowModel.getPower(), paramList);
//        addStreetLightData("lat", jpsWorkflowModel.getLat(), paramList);
        addStreetLightData("location.locationtype", jpsWorkflowModel.getLocationtype(), paramList);
//        addStreetLightData("lng", jpsWorkflowModel.getLng(), paramList);
        addStreetLightData("network.lowvoltagethreshold", String.valueOf(jpsWorkflowModel.getLowvoltagethreshold()), paramList);
        addStreetLightData("name", jpsWorkflowModel.getIdOnController(), paramList);
        addStreetLightData("pole.type", jpsWorkflowModel.getPole_type(), paramList);
        addStreetLightData("model", jpsWorkflowModel.getModel(), paramList);
        // addStreetLightData("MacAddress", jpsWorkflowModel.getMacAddress(), paramList);
        addStreetLightData("location.utillocationid", jpsWorkflowModel.getUtillocationid(), paramList);
        addStreetLightData("fixing.type", jpsWorkflowModel.getFixing_type(), paramList);
        addStreetLightData("install.date", jpsWorkflowModel.getInstall_date(), paramList);
        addStreetLightData("network.type", jpsWorkflowModel.getNetwork_type(), paramList);
        addStreetLightData("pole.shape", jpsWorkflowModel.getPole_shape(), paramList);
        addStreetLightData("pole.status", jpsWorkflowModel.getPole_status(), paramList);
        addStreetLightData("device.node.serialnumber", jpsWorkflowModel.getSerialnumber(), paramList);
        addStreetLightData("location.zipcode", jpsWorkflowModel.getLocation_zipcode(), paramList);
        ResponseEntity<String> responseEntity = setDeviceValues(paramList);
        logger.info("********************** set device values reponse code: " + responseEntity.getStatusCode());
        logger.info("set device values response: " + responseEntity.getBody());
        logger.info("********************** set device values reponse end *********");
        return responseEntity;
    }

    private void saveSlvSyncDetail(SlvSyncDetail slvSyncDetail, EdgeNote edgeNote, JPSWorkflowModel jpsWorkflowModel, ResponseEntity<String> responseEntity, boolean isUpdate) {
        slvSyncDetail.setCreatedDateTime(edgeNote.getCreatedDateTime());
        slvSyncDetail.setNoteGuid(edgeNote.getNoteGuid());
        slvSyncDetail.setPoleNumber(jpsWorkflowModel.getIdOnController());
        slvSyncDetail.setProcessedDateTime(System.currentTimeMillis());
        String response = responseEntity.getBody();
        slvSyncDetail.setSlvDeviceDetailsResponse(response);
        slvSyncDetail.setTitle(edgeNote.getTitle());
        slvSyncDetail.setDeviceDetails(gson.toJson(jpsWorkflowModel));
        slvSyncDetail.setStatus((responseEntity.getStatusCode() == HttpStatus.OK) ? Status.Success : Status.Failure);
        if (isUpdate) {
            logger.info("slvsyncdetails object is updated");
            connectionDAO.updateSlvSyncDetail(slvSyncDetail);
        } else {
            logger.info("slvsyncdetails object is newly inserted");
            connectionDAO.saveSlvSyncDetail(slvSyncDetail);
        }
    }

    public long getSchedulerTime() {
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 12);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            return cal.getTimeInMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return System.currentTimeMillis();
    }

    public void startReport() {
        //  String prePath = "./output/email/";
        String prePath = properties.getProperty("jps.reportpath");
        DateTime dateTime = new DateTime();
        long todayMillis = getSchedulerTime();
        System.out.println(todayMillis);
        String folderName = dateTime.toString("yyy_MM_dd_HH_mm");
        String outputPath = prePath + folderName;
        String tempPath = properties.getProperty("jps.reportpath.temp");
        ;
        File folder = new File(outputPath);
        if (!folder.exists()) {
            // folder.mkdirs();
        }
        String date = dateTime.toString("yyy_MM_dd");

        String slvDeviceCsvPath = tempPath + File.separator + "slv_device_" + folderName + ".csv";
        String duplicateMacCsvPath = tempPath + File.separator + "duplicate_mac_" + folderName + ".csv";
        String slvSyncDetailCsvPath = tempPath + File.separator + "slv_sync_details_" + folderName + ".csv";

        CsvConnectionDao.importCsv("COPY(select title,noteguid,pole_number,old_pole_number,to_timestamp(created_date_time/1000) as createdtime,to_timestamp(processed_date_time/1000) as processedTime, status,slv_reponse from slvdevice where processed_date_time >= " + todayMillis + ") TO '" + slvDeviceCsvPath + "' DELIMITER ',' CSV HEADER;");
        CsvConnectionDao.importCsv("COPY(select title,noteguid,pole_number,existing_pole_number,macaddress,to_timestamp(processed_date_time/1000) as processedTime from slv_duplicate_macaddress where processed_date_time >= " + todayMillis + ") TO '" + duplicateMacCsvPath + "' DELIMITER ',' CSV HEADER;");
        CsvConnectionDao.importCsv("COPY(select title,noteguid,pole_number,to_timestamp(created_date_time/1000) as createdtime,to_timestamp(processed_date_time/1000) as processedTime, status,slv_device_detail_reponse,slv_replace_olc_response from slvsyncdetails where processed_date_time >= " + todayMillis + ") TO '" + slvSyncDetailCsvPath + "' DELIMITER ',' CSV HEADER;");
        String zipFilePath = prePath + "report_" + folderName + ".zip";
        try {
            File tempFile = new File(tempPath);
            FileUtils.copyDirectory(tempFile, new File(outputPath));
            File[] files = tempFile.listFiles();
            for (File file : files) {
                if (!file.delete()) {
                    System.out.println("Failed to delete " + file);
                }
            }
            EmailUtils.zipFiles(zipFilePath, outputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        File file = new File(zipFilePath);
        if (file.exists()) {
            EmailUtils.sendEmail(zipFilePath, ("JPS-SLV Interface Reports on" + date), "Please Find the attachment", "report_" + date + ".zip");
        }
    }

}
