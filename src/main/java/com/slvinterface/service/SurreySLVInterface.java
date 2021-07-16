package com.slvinterface.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.slvinterface.entity.LookupEntity;
import com.slvinterface.entity.PromotedFormDataEntity;
import com.slvinterface.entity.SLVSyncTable;
import com.slvinterface.entity.SLVTransactionLogs;
import com.slvinterface.enumeration.SLVProcess;
import com.slvinterface.exception.*;
import com.slvinterface.json.*;
import com.slvinterface.utils.PropertiesReader;
import com.slvinterface.utils.SLVInterfaceUtilsModel;
import org.apache.log4j.Logger;

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
                previousEdge2SLVData.setParentNoteId(
                        (edgeNote.getBaseParentNoteId() != null && !edgeNote.getBaseParentNoteId().trim().isEmpty()) ?
                                edgeNote.getBaseParentNoteId() : edgeNote.getNoteGuid()
                );
                if(edgeNote.getEdgeNotebook() != null){
                    previousEdge2SLVData.setNotebookGuid(edgeNote.getEdgeNotebook().getNotebookGuid());
                }

                encodeData(previousEdge2SLVData);
                retryCount = 0;
                checkTokenValidity(previousEdge2SLVData);
                boolean isMacAlreadyPresent= false;
                if(!previousEdge2SLVData.getPriority().getType().toString().equals(SLVProcess.REMOVE.toString())){
                    try {
                        checkMacAddressExists(macAddress, previousEdge2SLVData.getIdOnController());
                    }catch (QRCodeAlreadyUsedException e){
                        isMacAlreadyPresent = true;
                        logger.error("MAC Address is already Present.");
                    }
                }

                slvSync(slvSyncTable,previousEdge2SLVData,isMacAlreadyPresent);
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
    private void slvSync(SLVSyncTable slvSyncTable,Edge2SLVData previousEdge2SLVData,boolean isMacPresent)throws ReplaceOLCFailedException, DeviceSearchException,GeoZoneSearchException,CreateGeoZoneException,DeviceCreationException {
        SLVInterfaceUtilsModel slvInterfaceUtilsModel = getSLVInterfaceUtilsModel(previousEdge2SLVData,slvSyncTable);
        boolean isDeviceCreate = slvInterfaceUtils.checkDeviceDetails(slvInterfaceUtilsModel);
        switch (previousEdge2SLVData.getPriority().getType()){
            case REMOVE:
                logger.info("Remove Option is Selected.");
                slvSyncTable.setSelectedAction("Remove WorkFlow");
                logger.info("Empty Replace OLC going to call.");
                if(!isMacPresent) {
                    replaceOLC(previousEdge2SLVData.getControllerStrId(), previousEdge2SLVData.getIdOnController(), "", slvSyncTable);
                }
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
                if(!isMacPresent) {
                    replaceOLC(previousEdge2SLVData.getControllerStrId(), previousEdge2SLVData.getIdOnController(), "", slvSyncTable);
                }
                logger.info("Empty Replace OLC called.");
                logger.info("Set Install Status and Install Date.");
                setDeviceVal(slvSyncTable,previousEdge2SLVData);
                logger.info("Replace OLC With New MAC.");
                if(!isMacPresent) {
                    replaceOLC(previousEdge2SLVData.getControllerStrId(), previousEdge2SLVData.getIdOnController(), previousEdge2SLVData.getMacAddress(), slvSyncTable);
                }
                logger.info("Replace OLC With New MAC Success.");
                slvSyncTable.setStatus("Success");
                break;

            case NEW_DEVICE:
                logger.info("New Option is Selected.");
                slvSyncTable.setSelectedAction("Install WorkFlow");
                logger.info("Set Install Status and Install Date.");
                setDeviceVal(slvSyncTable,previousEdge2SLVData);
                logger.info("Replace OLC With MAC.");
                if(!isMacPresent) {
                    replaceOLC(previousEdge2SLVData.getControllerStrId(), previousEdge2SLVData.getIdOnController(), previousEdge2SLVData.getMacAddress(), slvSyncTable);
                }
                slvSyncTable.setStatus("Success");
                break;


        }
    }


    private void clearValue(SLVSyncTable slvSyncTable,Edge2SLVData previousEdge2SLVData){
        logger.info("Clearing values from SLV.");
        SLVTransactionLogs slvTransactionLogs = getSLVTransVal(slvSyncTable);
        List<Object> paramsList = new ArrayList<>();
        loadVal(paramsList,previousEdge2SLVData);
        addStreetLightData("installStatus","To be installed",paramsList);
        addStreetLightData("install.date","",paramsList);
        addStreetLightData("luminaire.partdescription","",paramsList);
        addStreetLightData("luminaire.model","",paramsList);
        addStreetLightData("luminaire.lumenoutput","",paramsList);
        addStreetLightData("luminaire.color","",paramsList);
        addStreetLightData("luminaire.lightsource","",paramsList);
        addStreetLightData("luminaire.DistributionType","",paramsList);
        addStreetLightData("luminaire.style","",paramsList);
        addStreetLightData("ElexonChargeCode","",paramsList);
        addStreetLightData("power","",paramsList);
        addStreetLightData("dimmingGroupName","",paramsList);
        addStreetLightData("network.highvoltagethreshold","",paramsList);
        addStreetLightData("network.lowvoltagethreshold","",paramsList);
        addStreetLightData("powerCorrection","",paramsList);
        PromotedFormDataEntity promotedFormDataEntity = new PromotedFormDataEntity();
        promotedFormDataEntity.setLastupdateddatetime(System.currentTimeMillis());
        promotedFormDataEntity.setPromotedvalue(gson.toJson(new ArrayList<>()));
        promotedFormDataEntity.setNotebookguid(previousEdge2SLVData.getNotebookGuid());
        promotedFormDataEntity.setParentnoteguid(previousEdge2SLVData.getParentNoteId());

        setDeviceValues(paramsList,slvTransactionLogs,promotedFormDataEntity,previousEdge2SLVData);

    }


    private void setDeviceVal(SLVSyncTable slvSyncTable,Edge2SLVData previousEdge2SLVData){
        List<Object> paramsList = new ArrayList<>();
        loadVal(paramsList,previousEdge2SLVData);
        loadDefaultVal(slvSyncTable,previousEdge2SLVData,paramsList);
        SLVTransactionLogs slvTransactionLogs = getSLVTransVal(slvSyncTable);


        addStreetLightData("installStatus","Installed",paramsList);
        addStreetLightData("MacAddress",previousEdge2SLVData.getMacAddress(),paramsList);
        addStreetLightData("install.date",previousEdge2SLVData.getInstallDate(),paramsList);
        setDeviceValues(paramsList,slvTransactionLogs,null,previousEdge2SLVData);
    }

    //Customer asset ID,Customer prefix,Feature ID,Road name,Location description
    private void loadDefaultVal(SLVSyncTable slvSyncTable,Edge2SLVData previousEdge2SLVData,List<Object> paramsList){
        SLVTransactionLogs slvTransactionLogs = getSLVTransVal(slvSyncTable);
       // loadVal (paramsList,previousEdge2SLVData);

        processFixtureQRScan(previousEdge2SLVData.getFixtureQRScan(),paramsList,previousEdge2SLVData,null);


        String slvCalender = previousEdge2SLVData.getCalendar();
        if(previousEdge2SLVData.getCalendar() == null || previousEdge2SLVData.getCalendar().trim().isEmpty()){
            slvCalender = PropertiesReader.getProperties().getProperty("streetlight.calendar");
        }

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

        //setDeviceValues(paramsList,slvTransactionLogs,null,previousEdge2SLVData);
    }

    private PromotedComponent getPromotedComponent(List<FormId> formIdList,String slvKey,String value){
        FormId formId = new FormId();
        formId.setSlvName(slvKey);
        int pos =  formIdList.indexOf(formId);
        if(pos != -1){
            formId =  formIdList.get(pos);
            PromotedComponent promotedComponent = new PromotedComponent();
            promotedComponent.setComponentId(formId.getFormId());
            promotedComponent.setName(formId.getName());
            promotedComponent.setValue(value);
            return promotedComponent;
        }
        return null;
    }


    private void processFixtureQRScan(String fixtureQRScan,List<Object> paramsList,Edge2SLVData previousEdge2SLVData,PromotedValue promotedValue){
        addData("client.name",previousEdge2SLVData.getClientName(),paramsList);
        addData("address",previousEdge2SLVData.getAddress(),paramsList);
        addData("luminaire.type",previousEdge2SLVData.getLanternType(),paramsList);
        addData("network.highvoltagethreshold","253",paramsList);
        addData("network.lowvoltagethreshold","207",paramsList);
        addData("powerCorrection","0",paramsList);
        if(previousEdge2SLVData.getLanternType() != null){
            LookupEntity lookupEntity = queryExecutor.getLookupEntity(previousEdge2SLVData.getLanternType());
            if(lookupEntity != null){
                addData("luminaire.partdescription",lookupEntity.getPartDescription(),paramsList);
                addData("luminaire.model",lookupEntity.getModel(),paramsList);
                addData("luminaire.lumenoutput",lookupEntity.getLumenOutput(),paramsList);
                addData("luminaire.color",lookupEntity.getColor(),paramsList);
                addData("luminaire.lightsource",lookupEntity.getLightSource(),paramsList);
                addData("luminaire.DistributionType",lookupEntity.getDistributionType(),paramsList);
                addData("luminaire.style",lookupEntity.getStyle(),paramsList);
                addData("ElexonChargeCode",lookupEntity.getElexonChargeCode(),paramsList);
                addData("power",lookupEntity.getPower(),paramsList);
            }
        }

    }


    private void addData(String key,String value,List<Object> paramsList){
        if(value != null && !value.trim().isEmpty()){
            addStreetLightData(key,value.trim(),paramsList);
        }

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
