package com.slvinterface.service;

import com.slvinterface.entity.SLVSyncTable;
import com.slvinterface.entity.SLVTransactionLogs;
import com.slvinterface.exception.NoValueException;
import com.slvinterface.exception.QRCodeAlreadyUsedException;
import com.slvinterface.exception.ReplaceOLCFailedException;
import com.slvinterface.exception.SLVConnectionException;
import com.slvinterface.json.*;
import com.slvinterface.utils.PropertiesReader;
import org.apache.log4j.Logger;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

public class SurreySLVInterface extends  SLVInterfaceService {
    private static final Logger logger = Logger.getLogger(SurreySLVInterface.class);

    public SurreySLVInterface()throws Exception{
        super();
    }



    // 118 - Controller ID, Node Installation Date -  155,Scan Node QR Code - 85,New Node QR Code-132,Replacement Date-159,
    public void processFormData(List<FormData> formDataList, SLVSyncTable slvSyncTable,EdgeNote edgeNote)throws SLVConnectionException{
        Edge2SLVData previousEdge2SLVData = null;
        for(FormData formData : formDataList){
            Edge2SLVData currentEdge2SLVData = new Edge2SLVData();
            processFormData(formData,currentEdge2SLVData);
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

        if(previousEdge2SLVData != null && previousEdge2SLVData.getPriority() != null){
            String macAddress = previousEdge2SLVData.getMacAddress();
            String installDate =  dateFormat(Long.valueOf(edgeNote.getCreatedDateTime()));
            previousEdge2SLVData.setInstallDate(installDate);
            try{
                slvSyncTable.setIdOnController(previousEdge2SLVData.getIdOnController());
                if(slvSyncTable.getIdOnController() == null || slvSyncTable.getIdOnController().trim().isEmpty()){
                    slvSyncTable.setIdOnController(edgeNote.getTitle());
                    previousEdge2SLVData.setIdOnController(edgeNote.getTitle());
                }

                previousEdge2SLVData.setIdOnController(URLEncoder.encode(previousEdge2SLVData.getIdOnController(),"UTF-8"));
                retryCount = 0;
                checkTokenValidity(previousEdge2SLVData);
                if(macAddress != null && !macAddress.trim().isEmpty()){
                    checkMacAddressExists(macAddress,previousEdge2SLVData.getIdOnController());
                }

                slvSync(slvSyncTable,previousEdge2SLVData);
            }catch (SLVConnectionException e){
                throw new SLVConnectionException(e);
            }catch (QRCodeAlreadyUsedException e){
                slvSyncTable.setStatus("Failure");
                slvSyncTable.setErrorDetails(e.getMessage());
                logger.info("MAC Address is Empty. So Note is not synced.");
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



    /**
     * Send Value to SLV.
     * @param slvSyncTable
     * @param previousEdge2SLVData
     * @throws ReplaceOLCFailedException
     */
    private void slvSync(SLVSyncTable slvSyncTable,Edge2SLVData previousEdge2SLVData)throws ReplaceOLCFailedException {
        switch (previousEdge2SLVData.getPriority().getType()){
            case REMOVE:
                slvSyncTable.setSelectedAction("Remove WorkFlow");
                replaceOLC(previousEdge2SLVData.getControllerStrId(),previousEdge2SLVData.getIdOnController(),"",slvSyncTable);
                slvSyncTable.setStatus("Success");
                break;

            case UPDATE_DEVICE:
                slvSyncTable.setSelectedAction("Replace WorkFlow");
                //replaceOLC(previousEdge2SLVData.getControllerStrId(),previousEdge2SLVData.getIdOnController(),"",slvSyncTable);
                setDeviceVal(slvSyncTable,previousEdge2SLVData);
                //replaceOLC(previousEdge2SLVData.getControllerStrId(),previousEdge2SLVData.getIdOnController(),previousEdge2SLVData.getMacAddress(),slvSyncTable);
                slvSyncTable.setStatus("Success");
                break;

            case NEW_DEVICE:
                slvSyncTable.setSelectedAction("Install WorkFlow");
                setDeviceVal(slvSyncTable,previousEdge2SLVData);
                //replaceOLC(previousEdge2SLVData.getControllerStrId(),previousEdge2SLVData.getIdOnController(),previousEdge2SLVData.getMacAddress(),slvSyncTable);
                slvSyncTable.setStatus("Success");
                break;


        }
    }


    private void setDeviceVal(SLVSyncTable slvSyncTable,Edge2SLVData previousEdge2SLVData){
        SLVTransactionLogs slvTransactionLogs = getSLVTransVal(slvSyncTable);
        List<Object> paramsList = new ArrayList<>();
        loadVal(paramsList,previousEdge2SLVData);
        addStreetLightData("installStatus","Installed",paramsList);
        if(previousEdge2SLVData.getMacAddress() != null){
            addStreetLightData("MacAddress",previousEdge2SLVData.getMacAddress(),paramsList);
        }

        addStreetLightData("install.date",previousEdge2SLVData.getInstallDate(),paramsList);

        String slvCalender = PropertiesReader.getProperties().getProperty("streetlight.calendar");
        if(slvCalender != null && !slvCalender.trim().isEmpty()){
            try {
                addStreetLightData("DimmingGroupName",slvCalender,paramsList);
            }catch (Exception e){
                e.printStackTrace();
            }
        }


        String modelFunctionId = "talq.streetlight.v1:lightNodeFunction6";

        try {
            modelFunctionId =  URLEncoder.encode(modelFunctionId,"UTF-8");
            addStreetLightData("modelfunctionid",modelFunctionId,paramsList);
           // addStreetLightData("nodeTypeStrId", modelFunctionId,paramsList);
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            buildFixtureStreetLightData(previousEdge2SLVData.getFixutreQRScan(),paramsList);
        }catch (Exception e){
            e.printStackTrace();
        }

        setDeviceValues(paramsList,slvTransactionLogs);
    }


    public void buildFixtureStreetLightData(String data, List<Object> paramsList) throws Exception{
        if(data != null && !data.trim().isEmpty()){

            String[] fixtureInfo = data.split(",");
            if (fixtureInfo.length >= 9) {
                addStreetLightData("luminaire.brand", URLEncoder.encode(fixtureInfo[0],"UTF-8"), paramsList);
                addStreetLightData("device.luminaire.partnumber", URLEncoder.encode(fixtureInfo[1],"UTF-8"), paramsList);
                addStreetLightData("luminaire.model", URLEncoder.encode(fixtureInfo[2],"UTF-8"), paramsList);
                addStreetLightData("ballast.brand", URLEncoder.encode(fixtureInfo[3],"UTF-8"), paramsList);
                addStreetLightData("ballast.type", URLEncoder.encode(fixtureInfo[4],"UTF-8"), paramsList);
                addStreetLightData("ballast.dimmingtype", URLEncoder.encode(fixtureInfo[5],"UTF-8"), paramsList);
                addStreetLightData("luminaire.distributiontype", URLEncoder.encode(fixtureInfo[6],"UTF-8"), paramsList);
                addStreetLightData("luminaire.DistributionType",URLEncoder.encode(fixtureInfo[6],"UTF-8"), paramsList);

                String powerVal = fixtureInfo[7];
                if (powerVal != null && !powerVal.isEmpty()) {
                    powerVal = powerVal.replaceAll("W", "");
                    powerVal = powerVal.replaceAll("w", "");
                }


                addStreetLightData("power", URLEncoder.encode(powerVal,"UTF-8"), paramsList);
                addStreetLightData("device.luminaire.colortemp", URLEncoder.encode(fixtureInfo[8],"UTF-8"), paramsList);

            }
        }


    }

}
