package com.terragoedge.slvinterface.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terragoedge.slvinterface.dao.ConnectionDAO;
import com.terragoedge.slvinterface.dao.tables.DuplicateMacAddress;
import com.terragoedge.slvinterface.dao.tables.SlvDevice;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetail;
import com.terragoedge.slvinterface.enumeration.Status;
import com.terragoedge.slvinterface.exception.*;
import com.terragoedge.slvinterface.model.EdgeFormData;
import com.terragoedge.slvinterface.model.EdgeNote;
import com.terragoedge.slvinterface.model.JPSWorkflowModel;
import com.terragoedge.slvinterface.model.Value;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class SlvService extends AbstractSlvService {
    private Logger logger = Logger.getLogger(SlvService.class);
    private ConnectionDAO connectionDAO;
    private Gson gson;

    public SlvService() {
        gson = new Gson();
        connectionDAO = ConnectionDAO.INSTANCE;
    }

    public void processSlv(JPSWorkflowModel jpsWorkflowModel, EdgeNote edgeNote) throws Exception{
            JsonArray devices = checkDeviceExist(jpsWorkflowModel.getIdOnController());// search idoncontroller on slv - ** rest call
            if(devices != null && devices.size() == 1){// device already present in slv
                processDeviceValues(jpsWorkflowModel,true,edgeNote);
                processMacAddress(jpsWorkflowModel,edgeNote,devices);
            }else if(devices == null || devices.size() == 0){// device not present in slv
                ResponseEntity<String> responseEntity = createDevice(edgeNote,jpsWorkflowModel);// create device in slv
                saveDevice(jpsWorkflowModel,edgeNote,responseEntity);// save or update slvdevice in local db
                if(responseEntity.getStatusCode() == HttpStatus.OK){
                    processDeviceValues(jpsWorkflowModel,false,edgeNote);
                    processMacAddress(jpsWorkflowModel,edgeNote,devices);
                }
            }else {
                throw new Exception("more devices found for this pole: "+edgeNote.getTitle());
            }
    }

    private void processDeviceValues(JPSWorkflowModel jpsWorkflowModel, boolean devicePresentInSlv, EdgeNote edgeNote) throws Exception{
        SlvSyncDetail dbSyncDetail = connectionDAO.getSlvSyncDetail(jpsWorkflowModel.getIdOnController());
        if (devicePresentInSlv) { // if false, then no need to check device values are same or not
            if (dbSyncDetail == null) {// get device values from local dp and check
                ResponseEntity<String> responseEntity = callSetDeviceValues(jpsWorkflowModel);// call set device values - ** rest call
                SlvSyncDetail slvSyncDetail = new SlvSyncDetail();// save slvSyncDetail in local db
                saveSlvSyncDetail(slvSyncDetail, edgeNote, jpsWorkflowModel, responseEntity, false);
                if(responseEntity.getStatusCode() != HttpStatus.OK){
                    throw new Exception("Error in set device values");
                }
            } else {
                if (!dbSyncDetail.getDeviceDetails().equals(gson.toJson(jpsWorkflowModel))) {// check local and current device details changed or not
                    ResponseEntity<String> responseEntity = callSetDeviceValues(jpsWorkflowModel);// call set device values - ** rest call
                    saveSlvSyncDetail(dbSyncDetail, edgeNote, jpsWorkflowModel, responseEntity, true);
                    if(responseEntity.getStatusCode() != HttpStatus.OK){
                        throw new Exception("Error in set device values");
                    }
                }
            }
        } else {
            ResponseEntity<String> responseEntity = callSetDeviceValues(jpsWorkflowModel);// call set device values
            SlvSyncDetail slvSyncDetail = (dbSyncDetail == null) ? new SlvSyncDetail() : dbSyncDetail;
            saveSlvSyncDetail(slvSyncDetail, edgeNote, jpsWorkflowModel, responseEntity, (dbSyncDetail == null) ? false : true);// save or update slvSyncDetail in local db
            if(responseEntity.getStatusCode() != HttpStatus.OK){
                throw new Exception("Error in set device values");
            }
        }
    }


    private void processMacAddress(JPSWorkflowModel jpsWorkflowModel,EdgeNote edgeNote,JsonArray devices) throws ReplaceOLCFailedException{
        List<Value> values = checkMacAddressExists(jpsWorkflowModel.getMacAddress());// check mac address already present
        if(values != null && values.size() > 0){
            String existingPoleNumber = "";
            boolean isDuplicateMacAddress = false;
            for(Value value : values){
                if(!value.getIdOnController().equals(jpsWorkflowModel.getIdOnController())){ // check assigned pole and current pole are same
                    isDuplicateMacAddress = true;
                    existingPoleNumber = value.getIdOnController();
                }
            }
            if(isDuplicateMacAddress) {
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
            if(responseEntity.getStatusCode() == HttpStatus.OK){
                JsonObject jsonObject = new JsonParser().parse(responseEntity.getBody()).getAsJsonObject();
                JsonArray macvalues = jsonObject.get("values").getAsJsonArray();
                if(macvalues != null && macvalues.size() == 1){
                    String mac = macvalues.get(0).getAsString();
                    if(!mac.equals(jpsWorkflowModel.getMacAddress())){// slv and edge having different mac address
                        replaceOLC(jpsWorkflowModel.getControllerStrId(), jpsWorkflowModel.getIdOnController(), "",edgeNote,jpsWorkflowModel);
                        replaceOLC(jpsWorkflowModel.getControllerStrId(), jpsWorkflowModel.getIdOnController(), jpsWorkflowModel.getMacAddress(),edgeNote,jpsWorkflowModel);
                    }
                }else if(macvalues == null || macvalues.size() == 0){// mac address not present in slv so update mac address
                    replaceOLC(jpsWorkflowModel.getControllerStrId(), jpsWorkflowModel.getIdOnController(), jpsWorkflowModel.getMacAddress(),edgeNote,jpsWorkflowModel);
                }else {
                    logger.error("slv device having more mac address pole: "+jpsWorkflowModel.getIdOnController());
                }
            }else{
                logger.error("error while fetching device data for this idoncontroller: "+jpsWorkflowModel.getIdOnController());
                logger.error("error while fetching device data for this device id: "+deviceID);
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
            slvDevice.setSlvResponse(responseEntity.getBody());
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


}
