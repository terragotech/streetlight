package com.terragoedge.slvinterface.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terragoedge.slvinterface.dao.ConnectionDAO;
import com.terragoedge.slvinterface.dao.CsvConnectionDao;
import com.terragoedge.slvinterface.dao.tables.DuplicateMacAddress;
import com.terragoedge.slvinterface.dao.tables.GeozoneEntity;
import com.terragoedge.slvinterface.dao.tables.SlvDevice;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetail;
import com.terragoedge.slvinterface.enumeration.Status;
import com.terragoedge.slvinterface.exception.*;
import com.terragoedge.slvinterface.model.*;
import org.apache.commons.lang3.StringUtils;
import com.terragoedge.slvinterface.utils.EmailUtils;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.util.ArrayList;
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
        GeozoneEntity geozoneEntity = getGeozoneEntity(jpsWorkflowModel.getNotebookName(), jpsWorkflowModel.getAddress1());
        jpsWorkflowModel.setGeozoneId(geozoneEntity.getChildgeozoneId());
        JsonArray devices = checkDeviceExist(jpsWorkflowModel.getIdOnController());// search idoncontroller on slv - ** rest call
        if (devices != null && devices.size() == 1) {// device already present in slv
            processDeviceValues(jpsWorkflowModel, true, edgeNote);
            processMacAddress(jpsWorkflowModel, edgeNote, devices);
        } else if (devices == null || devices.size() == 0) {// device not present in slv
            ResponseEntity<String> responseEntity = createDevice(edgeNote, jpsWorkflowModel);// create device in slv
            if(responseEntity != null) {
                saveDevice(jpsWorkflowModel, edgeNote, responseEntity);// save or update slvdevice in local db
                if (responseEntity.getStatusCode() == HttpStatus.OK) {
                    processDeviceValues(jpsWorkflowModel, false, edgeNote);
                    processMacAddress(jpsWorkflowModel, edgeNote, devices);
                }
            }
        } else {
            throw new Exception("more devices found for this pole: " + edgeNote.getTitle());
        }
    }

    private void processDeviceValues(JPSWorkflowModel jpsWorkflowModel, boolean devicePresentInSlv, EdgeNote edgeNote) throws Exception {
        if(properties.getProperty("streetlights.setdevice.enable").equals("true")) {
            SlvSyncDetail dbSyncDetail = connectionDAO.getSlvSyncDetail(jpsWorkflowModel.getIdOnController());
            if (devicePresentInSlv) { // if false, then no need to check device values are same or not
                if (dbSyncDetail == null) {// get device values from local dp and check
                    ResponseEntity<String> responseEntity = callSetDeviceValues(jpsWorkflowModel);// call set device values - ** rest call
                    SlvSyncDetail slvSyncDetail = new SlvSyncDetail();// save slvSyncDetail in local db
                    saveSlvSyncDetail(slvSyncDetail, edgeNote, jpsWorkflowModel, responseEntity, false);
                    if (responseEntity.getStatusCode() != HttpStatus.OK) {
                        throw new Exception("Error in set device values");
                    }
                } else {
                    if (!dbSyncDetail.getDeviceDetails().equals(gson.toJson(jpsWorkflowModel))) {// check local and current device details changed or not
                        ResponseEntity<String> responseEntity = callSetDeviceValues(jpsWorkflowModel);// call set device values - ** rest call
                        saveSlvSyncDetail(dbSyncDetail, edgeNote, jpsWorkflowModel, responseEntity, true);
                        if (responseEntity.getStatusCode() != HttpStatus.OK) {
                            throw new Exception("Error in set device values");
                        }
                    }
                }
            } else {
                ResponseEntity<String> responseEntity = callSetDeviceValues(jpsWorkflowModel);// call set device values
                SlvSyncDetail slvSyncDetail = (dbSyncDetail == null) ? new SlvSyncDetail() : dbSyncDetail;
                saveSlvSyncDetail(slvSyncDetail, edgeNote, jpsWorkflowModel, responseEntity, (dbSyncDetail == null) ? false : true);// save or update slvSyncDetail in local db
                if (responseEntity.getStatusCode() != HttpStatus.OK) {
                    throw new Exception("Error in set device values");
                }
            }
        }
    }


    private void processMacAddress(JPSWorkflowModel jpsWorkflowModel, EdgeNote edgeNote, JsonArray devices) throws ReplaceOLCFailedException {
        if(properties.getProperty("streetlights.replaceolc.enable").equals("true")) {
            List<Value> values = checkMacAddressExists(jpsWorkflowModel.getMacAddress());// check mac address already present
            if (values != null && values.size() > 0) {
                String existingPoleNumber = "";
                boolean isDuplicateMacAddress = false;
                for (Value value : values) {
                    if (!value.getIdOnController().equals(jpsWorkflowModel.getIdOnController())) { // check assigned pole and current pole are same
                        isDuplicateMacAddress = true;
                        existingPoleNumber = value.getIdOnController();
                    }
                }
                if (isDuplicateMacAddress) {
                    // save duplicate mac address to local db
                    DuplicateMacAddress duplicateMacAddress = new DuplicateMacAddress();
                    duplicateMacAddress.setExistingPoleNumber(existingPoleNumber);
                    duplicateMacAddress.setMacAddress(jpsWorkflowModel.getMacAddress());
                    duplicateMacAddress.setNoteguid(edgeNote.getNoteGuid());
                    duplicateMacAddress.setPoleNumber(jpsWorkflowModel.getIdOnController());
                    duplicateMacAddress.setProcessedDateTime(System.currentTimeMillis());
                    duplicateMacAddress.setTitle(edgeNote.getTitle());
                    connectionDAO.saveDuplicateMacAddress(duplicateMacAddress);
                }// else pole already assigned with correct macaddress
            } else {
                int deviceID = devices.get(0).getAsJsonObject().get("id").getAsInt();
                ResponseEntity<String> responseEntity = getDeviceData(deviceID);// check device has another mac  - ** rest call
                if (responseEntity.getStatusCode() == HttpStatus.OK) {
                    JsonObject jsonObject = new JsonParser().parse(responseEntity.getBody()).getAsJsonObject();
                    JsonArray macvalues = jsonObject.get("values").getAsJsonArray();
                    if (macvalues != null && macvalues.size() == 1) {
                        String mac = macvalues.get(0).getAsString();
                        if (!mac.equals(jpsWorkflowModel.getMacAddress())) {// slv and edge having different mac address
                            replaceOLC(jpsWorkflowModel.getControllerStrId(), jpsWorkflowModel.getIdOnController(), "", edgeNote, jpsWorkflowModel);
                            replaceOLC(jpsWorkflowModel.getControllerStrId(), jpsWorkflowModel.getIdOnController(), jpsWorkflowModel.getMacAddress(), edgeNote, jpsWorkflowModel);
                        }
                    } else if (macvalues == null || macvalues.size() == 0) {// mac address not present in slv so update mac address
                        replaceOLC(jpsWorkflowModel.getControllerStrId(), jpsWorkflowModel.getIdOnController(), jpsWorkflowModel.getMacAddress(), edgeNote, jpsWorkflowModel);
                    } else {
                        logger.error("slv device having more mac address pole: " + jpsWorkflowModel.getIdOnController());
                    }
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
            String response = StringUtils.left(responseEntity.getBody(), 100);
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

    private ResponseEntity<String> callSetDeviceValues(JPSWorkflowModel jpsWorkflowModel) {
        List<Object> paramList = new ArrayList<>();
        paramList.add("address1=" + jpsWorkflowModel.getAddress1());
        paramList.add("location.streetdescription=" + jpsWorkflowModel.getStreetdescription());
        paramList.add("categoryStrId=" + jpsWorkflowModel.getCategoryStrId());
        paramList.add("location.city=" + jpsWorkflowModel.getCity());
        paramList.add("controllerStrId=" + jpsWorkflowModel.getControllerStrId());
        paramList.add("dimmingGroupName=" + jpsWorkflowModel.getDimmingGroupName());
        paramList.add("provider.name=" + jpsWorkflowModel.getProvider_name());
        paramList.add("geoZone path=" + jpsWorkflowModel.getGeozonePath());
        paramList.add("network.highvoltagethreshold=" + jpsWorkflowModel.getHighvoltagethreshold());
        paramList.add("idOnController=" + jpsWorkflowModel.getIdOnController());
        paramList.add("installStatus=" + jpsWorkflowModel.getInstallStatus());
        paramList.add("lampType=" + jpsWorkflowModel.getLampType());
        paramList.add("lat=" + jpsWorkflowModel.getLat());
        paramList.add("location.locationtype=" + jpsWorkflowModel.getLocationtype());
        paramList.add("lng=" + jpsWorkflowModel.getLng());
        paramList.add("network.lowvoltagethreshold=" + jpsWorkflowModel.getLowvoltagethreshold());
        paramList.add("name=" + jpsWorkflowModel.getIdOnController());
        paramList.add("pole.type=" + jpsWorkflowModel.getPole_type());
        paramList.add("model=" + jpsWorkflowModel.getModel());
        paramList.add("macAddress=" + jpsWorkflowModel.getMacAddress());
        paramList.add("location.utillocationid=" + jpsWorkflowModel.getUtillocationid());
        paramList.add("fixing.type=" + jpsWorkflowModel.getFixing_type());
        paramList.add("install.date=" + jpsWorkflowModel.getInstall_date());
        paramList.add("network.type=" + jpsWorkflowModel.getNetwork_type());
        paramList.add("pole.shape=" + jpsWorkflowModel.getPole_shape());
        paramList.add("pole.status=" + jpsWorkflowModel.getPole_status());
        paramList.add("device.node.serialnumber=" + jpsWorkflowModel.getSerialnumber());
        paramList.add("location.zipcode=" + jpsWorkflowModel.getLocation_zipcode());
        paramList.add("address1=" + jpsWorkflowModel.getAddress1());
        paramList.add("address1=" + jpsWorkflowModel.getAddress1());
        ResponseEntity<String> responseEntity = setDeviceValues(paramList);
        return responseEntity;
    }

    private void saveSlvSyncDetail(SlvSyncDetail slvSyncDetail, EdgeNote edgeNote, JPSWorkflowModel jpsWorkflowModel, ResponseEntity<String> responseEntity, boolean isUpdate) {
        slvSyncDetail.setCreatedDateTime(edgeNote.getCreatedDateTime());
        slvSyncDetail.setNoteGuid(edgeNote.getNoteGuid());
        slvSyncDetail.setPoleNumber(jpsWorkflowModel.getIdOnController());
        slvSyncDetail.setProcessedDateTime(System.currentTimeMillis());
        slvSyncDetail.setSlvDeviceDetailsResponse(responseEntity.getBody());
        slvSyncDetail.setTitle(edgeNote.getTitle());
        slvSyncDetail.setDeviceDetails(gson.toJson(jpsWorkflowModel));
        slvSyncDetail.setStatus((responseEntity.getStatusCode() == HttpStatus.OK) ? Status.Success : Status.Failure);
        if (isUpdate) {
            connectionDAO.updateSlvSyncDetail(slvSyncDetail);
        } else {
            connectionDAO.saveSlvSyncDetail(slvSyncDetail);
        }
    }

    public void startReport(){
        String prePath = "./output/email/";
        DateTime dateTime = new DateTime();
        long todayMillis = dateTime.withTimeAtStartOfDay().getMillis();
        String folderName = dateTime.toString("yyy_MM_dd_HH_mm");
        String outputPath = prePath+folderName;
        File folder = new File(outputPath);
        if(!folder.exists()){
            folder.mkdirs();
        }
        String date = dateTime.toString("yyy_MM_dd");
        String slvDeviceCsvPath = outputPath+File.separator+"slv_device_"+folderName+".csv";
        String duplicateMacCsvPath = outputPath+File.separator+"duplicate_mac_"+folderName+".csv";
        String slvSyncDetailCsvPath = outputPath+File.separator+"slv_sync_details_"+folderName+".csv";
        CsvConnectionDao.importCsv("COPY(select title,noteguid,pole_number,old_pole_number,to_timestamp(created_date_time/1000) as createdtime,to_timestamp(processed_date_time/1000) as processedTime, status,slv_reponse from slvdevice where processed_date_time >= "+todayMillis+") TO '"+slvDeviceCsvPath+"' DELIMITER ',' CSV HEADER;");
        CsvConnectionDao.importCsv("COPY(select title,noteguid,pole_number,existing_pole_number,macaddress,to_timestamp(processed_date_time/1000) as processedTime from slv_duplicate_macaddress where processed_date_time >= "+todayMillis+") TO '"+duplicateMacCsvPath+"' DELIMITER ',' CSV HEADER;");
        CsvConnectionDao.importCsv("COPY(select title,noteguid,pole_number,to_timestamp(created_date_time/1000) as createdtime,to_timestamp(processed_date_time/1000) as processedTime, status,slv_device_detail_reponse,slv_replace_olc_response from slvsyncdetails where processed_date_time >= "+todayMillis+") TO '"+slvSyncDetailCsvPath+"' DELIMITER ',' CSV HEADER;");
        String zipFilePath = prePath+"report_"+folderName+".zip";
        try {
            EmailUtils.compressZipfile(outputPath, zipFilePath);
        }catch (Exception e){
            e.printStackTrace();
        }
        File file = new File(zipFilePath);
        if(file.exists()) {
            EmailUtils.sendEmail(zipFilePath,("JPS-SLV Interface Reports on"+date),"Please Find the attachment","report_"+date+".zip");
        }
    }

}
