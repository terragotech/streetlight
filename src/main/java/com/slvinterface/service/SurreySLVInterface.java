package com.slvinterface.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.slvinterface.entity.SLVSyncTable;
import com.slvinterface.entity.SLVTransactionLogs;
import com.slvinterface.enumeration.SLVProcess;
import com.slvinterface.exception.*;
import com.slvinterface.json.*;
import com.slvinterface.utils.PropertiesReader;
import com.slvinterface.utils.SLVInterfaceUtilsModel;
import org.apache.log4j.Logger;
import org.springframework.util.LinkedMultiValueMap;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

public class SurreySLVInterface extends  SLVInterfaceService {
    private static final Logger logger = Logger.getLogger(SurreySLVInterface.class);

    SLVInterfaceUtils slvInterfaceUtils;

    public SurreySLVInterface()throws Exception{
        super();
        slvInterfaceUtils = new SLVInterfaceUtils(queryExecutor);
    }


    private void getLocationDetails(EdgeNote edgeNote,Edge2SLVData currentEdge2SLVData){
        String geoJson = edgeNote.getGeometry();
        JsonObject geojsonObject = jsonParser.parse(geoJson).getAsJsonObject();
        JsonObject geometryObject = geojsonObject.get("geometry").getAsJsonObject();
        JsonArray latlngs = geometryObject.get("coordinates").getAsJsonArray();
        currentEdge2SLVData.setLat(latlngs.get(1).toString());
        currentEdge2SLVData.setLng(latlngs.get(0).toString());
        String controllerStrId = properties.getProperty("streetlight.controller.str.id");
        currentEdge2SLVData.setControllerStrId(controllerStrId);

    }


    // 118 - Controller ID, Node Installation Date -  155,Scan Node QR Code - 85,New Node QR Code-132,Replacement Date-159,
    public void processFormData(List<FormData> formDataList, SLVSyncTable slvSyncTable,EdgeNote edgeNote)throws SLVConnectionException{
        logger.info("Processing form value.");
        Edge2SLVData previousEdge2SLVData = null;
        for(FormData formData : formDataList){
            Edge2SLVData currentEdge2SLVData = new Edge2SLVData();
            processFormData(formData,currentEdge2SLVData);
            getLocationDetails(edgeNote,currentEdge2SLVData);
            logger.info("Current Edge2SLVData:"+currentEdge2SLVData.toString());
            if(previousEdge2SLVData == null){
                previousEdge2SLVData = currentEdge2SLVData;
            }else{
                logger.info("Current Edge2SLVData:"+currentEdge2SLVData.toString());
                logger.info("Previous Edge2SLVData:"+currentEdge2SLVData.toString());
                if(currentEdge2SLVData.getPriority().getOrder() > previousEdge2SLVData.getPriority().getOrder()){
                    logger.info("Current Edge2SLVData Priority is higher than Previous. So data getting swapped");
                    previousEdge2SLVData = currentEdge2SLVData;
                }
            }
        }
        logger.info("Data:"+previousEdge2SLVData);
        if(previousEdge2SLVData != null && previousEdge2SLVData.getPriority() != null){
            logger.info("Going to Sync With SLV.");
            String macAddress = previousEdge2SLVData.getMacAddress();
            if(macAddress == null || macAddress.trim().isEmpty()){
                slvSyncTable.setStatus("Failure");
                slvSyncTable.setErrorDetails("MAC Address is Empty.");
                logger.info("MAC Address is Empty. So Note is not synced.");
                return;
            }
            try{
                slvSyncTable.setIdOnController(previousEdge2SLVData.getIdOnController());
                previousEdge2SLVData.setTitle(slvSyncTable.getNoteName());
                encodeData(previousEdge2SLVData);
                retryCount = 0;
                checkTokenValidity(previousEdge2SLVData);
                if(!previousEdge2SLVData.getPriority().getType().toString().equals(SLVProcess.REMOVE.toString())){
                    checkMacAddressExists(macAddress,previousEdge2SLVData.getIdOnController());
                }

                slvSync(slvSyncTable,previousEdge2SLVData);
            }catch (SLVConnectionException e){
                throw new SLVConnectionException(e);
            }catch (QRCodeAlreadyUsedException e){
                slvSyncTable.setStatus("Failure");
                slvSyncTable.setErrorDetails(e.getMessage());
                logger.info("MAC Address is already Present. So Note is not synced.");
                return;
            }catch (ReplaceOLCFailedException e){
                slvSyncTable.setStatus("Failure");
                slvSyncTable.setErrorDetails("Error while during replaceOLC Call.");
                logger.error("Error while during replaceOLC Call.",e);
                return;
            }catch (Exception e){
                logger.error("Error in checkMacAddressExists",e);
                slvSyncTable.setStatus("Failure");
                slvSyncTable.setErrorDetails(e.getMessage());

                return;
            }

        }else{
            slvSyncTable.setStatus("Failure");
            slvSyncTable.setErrorDetails("MAC Address is Empty.");
            logger.info("MAC Address is Empty. So Note is not synced.");
            return;
        }

    }


    private void encodeData(Edge2SLVData currentEdge2SLVData)throws Exception{
        currentEdge2SLVData.setIdOnController(encodeData(currentEdge2SLVData.getIdOnController()));
        currentEdge2SLVData.setCurrentGeoZone(encodeData(currentEdge2SLVData.getCurrentGeoZone()));
        currentEdge2SLVData.setTitle(encodeData(currentEdge2SLVData.getTitle()));
    }


    private String encodeData(String data)throws Exception{
       return URLEncoder.encode(data,"UTF-8");
    }



    /**
     * Send Value to SLV.
     * @param slvSyncTable
     * @param previousEdge2SLVData
     * @throws ReplaceOLCFailedException
     */
    private void slvSync(SLVSyncTable slvSyncTable,Edge2SLVData previousEdge2SLVData)throws ReplaceOLCFailedException, DeviceSearchException,GeoZoneSearchException,CreateGeoZoneException,DeviceCreationException {
        SLVInterfaceUtilsModel slvInterfaceUtilsModel = getSLVInterfaceUtilsModel(previousEdge2SLVData,slvSyncTable);
        slvInterfaceUtils.checkDeviceDetails(slvInterfaceUtilsModel);
        switch (previousEdge2SLVData.getPriority().getType()){
            case REMOVE:
                logger.info("Remove Option is Selected.");
                slvSyncTable.setSelectedAction("Remove WorkFlow");
                logger.info("Empty Replace OLC going to call.");
                replaceOLC(previousEdge2SLVData.getControllerStrId(),previousEdge2SLVData.getIdOnController(),"",slvSyncTable);
                logger.info("Empty Replace OLC called.");
                clearValue(slvSyncTable,previousEdge2SLVData);
                logger.info("Clearing Local DB");
                queryExecutor.removeEdgeAllMac(previousEdge2SLVData.getIdOnController());
                slvSyncTable.setStatus("Success");
                break;

            case UPDATE_DEVICE:
                logger.info("Replace Option is Selected.");
                slvSyncTable.setSelectedAction("Replace WorkFlow");
                logger.info("Empty Replace OLC going to call.");
                replaceOLC(previousEdge2SLVData.getControllerStrId(),previousEdge2SLVData.getIdOnController(),"",slvSyncTable);
                logger.info("Empty Replace OLC called.");
                logger.info("Set Install Status and Install Date.");
                setDeviceVal(slvSyncTable,previousEdge2SLVData);
                logger.info("Replace OLC With New MAC.");
                replaceOLC(previousEdge2SLVData.getControllerStrId(),previousEdge2SLVData.getIdOnController(),previousEdge2SLVData.getMacAddress(),slvSyncTable);
                logger.info("Replace OLC With New MAC Success.");
                slvSyncTable.setStatus("Success");
                break;

            case NEW_DEVICE:
                logger.info("New Option is Selected.");
                slvSyncTable.setSelectedAction("Install WorkFlow");
                logger.info("Set Install Status and Install Date.");
                setDeviceVal(slvSyncTable,previousEdge2SLVData);
                logger.info("Replace OLC With MAC.");
                replaceOLC(previousEdge2SLVData.getControllerStrId(),previousEdge2SLVData.getIdOnController(),previousEdge2SLVData.getMacAddress(),slvSyncTable);
                slvSyncTable.setStatus("Success");
                break;


        }
    }


    private void clearValue(SLVSyncTable slvSyncTable,Edge2SLVData previousEdge2SLVData){
        logger.info("Clearing values from SLV.");
        SLVTransactionLogs slvTransactionLogs = getSLVTransVal(slvSyncTable);
        LinkedMultiValueMap<String,String> paramsList = new LinkedMultiValueMap<>();
        loadVal(paramsList,previousEdge2SLVData);
        addStreetLightData("installStatus","To be installed",paramsList);
        addStreetLightData("install.date","",paramsList);
        setDeviceValues(paramsList,slvTransactionLogs);

    }

    //Customer asset ID,Customer prefix,Feature ID,Road name,Location description
    private void setDeviceVal(SLVSyncTable slvSyncTable,Edge2SLVData previousEdge2SLVData){
        SLVTransactionLogs slvTransactionLogs = getSLVTransVal(slvSyncTable);
        LinkedMultiValueMap<String,String> paramsList = new LinkedMultiValueMap<>();
        loadVal(paramsList,previousEdge2SLVData);
        addStreetLightData("installStatus","Installed",paramsList);
        addStreetLightData("MacAddress",previousEdge2SLVData.getMacAddress(),paramsList);
        addStreetLightData("install.date",previousEdge2SLVData.getInstallDate(),paramsList);

        addStreetLightData("client.number",previousEdge2SLVData.getClientNumber(),paramsList);
        addStreetLightData("client.name",previousEdge2SLVData.getCentralAssetId(),paramsList);
        addStreetLightData("device.premise",previousEdge2SLVData.getFeatureId(),paramsList);
        addStreetLightData("address",previousEdge2SLVData.getSiteName(),paramsList);
        String featureLocation = previousEdge2SLVData.getFeatureLocation();
        featureLocation = featureLocation == null ? "" : featureLocation;
        if(featureLocation.length()>80){// while commissioning, address2 field supports only upto 80 chars
            featureLocation = featureLocation.substring(0,80);
        }
        addStreetLightData("location.streetdescription",featureLocation,paramsList);

        String slvCalender = PropertiesReader.getProperties().getProperty("streetlight.calendar");
        if(slvCalender != null && !slvCalender.trim().isEmpty()){
            try {
                addStreetLightData("DimmingGroupName",slvCalender,paramsList);
            }catch (Exception e){
                e.printStackTrace();
            }
        }


        String modelFunctionId = PropertiesReader.getProperties().getProperty("streetlight.slv.equipment.type");

        try {
            addStreetLightData("modelFunctionId",modelFunctionId,paramsList);
           // addStreetLightData("nodeTypeStrId", modelFunctionId,paramsList);
        }catch (Exception e){
            e.printStackTrace();
        }

        setDeviceValues(paramsList,slvTransactionLogs);
    }



    public SLVInterfaceUtilsModel getSLVInterfaceUtilsModel(Edge2SLVData edge2SLVData,SLVSyncTable slvSyncTable){
        SLVInterfaceUtilsModel slvInterfaceUtilsModel = new SLVInterfaceUtilsModel(
                edge2SLVData.getIdOnController(),
                edge2SLVData.getCurrentGeoZone(),
                edge2SLVData.getLat(),
                edge2SLVData.getLng(),
                edge2SLVData.getTitle(),
                slvSyncTable
        );
        return slvInterfaceUtilsModel;
    }

}
