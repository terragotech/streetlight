package com.slvinterface.service;

import com.slvinterface.entity.SLVSyncTable;
import com.slvinterface.entity.SLVTransactionLogs;
import com.slvinterface.exception.QRCodeAlreadyUsedException;
import com.slvinterface.exception.ReplaceOLCFailedException;
import com.slvinterface.exception.SLVConnectionException;
import com.slvinterface.json.Edge2SLVData;
import com.slvinterface.json.FormData;
import org.apache.log4j.Logger;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class BrentSLVInterface extends  SLVInterfaceService {

    private static final Logger logger = Logger.getLogger(BrentSLVInterface.class);

    public BrentSLVInterface()throws Exception{
        super();
    }



    // 118 - Controller ID, Node Installation Date -  155,Scan Node QR Code - 85,New Node QR Code-132,Replacement Date-159,
    public void processFormData(List<FormData> formDataList, SLVSyncTable slvSyncTable)throws SLVConnectionException {
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
            if(macAddress == null || macAddress.trim().isEmpty()){
                slvSyncTable.setStatus("Failure");
                slvSyncTable.setErrorDetails("MAC Address is Empty.");
                logger.info("MAC Address is Empty. So Note is not synced.");
                return;
            }
            try{
                slvSyncTable.setIdOnController(previousEdge2SLVData.getIdOnController());

                previousEdge2SLVData.setIdOnController(URLEncoder.encode(previousEdge2SLVData.getIdOnController(),"UTF-8"));
                retryCount = 0;
                checkTokenValidity(previousEdge2SLVData);

                checkMacAddressExists(macAddress,previousEdge2SLVData.getIdOnController());
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
            case UPDATE_DEVICE:
                slvSyncTable.setSelectedAction("Replace WorkFlow");
                replaceOLC(previousEdge2SLVData.getControllerStrId(),previousEdge2SLVData.getIdOnController(),"",slvSyncTable);
                setDeviceVal(slvSyncTable,previousEdge2SLVData);
                replaceOLC(previousEdge2SLVData.getControllerStrId(),previousEdge2SLVData.getIdOnController(),previousEdge2SLVData.getMacAddress(),slvSyncTable);
                slvSyncTable.setStatus("Success");
                break;



        }
    }


    private void setDeviceVal(SLVSyncTable slvSyncTable,Edge2SLVData previousEdge2SLVData){
        SLVTransactionLogs slvTransactionLogs = getSLVTransVal(slvSyncTable);
        List<Object> paramsList = new ArrayList<>();
        loadVal(paramsList,previousEdge2SLVData);
        addStreetLightData("installStatus","Installed",paramsList);
        addStreetLightData("MacAddress",previousEdge2SLVData.getMacAddress(),paramsList);
        addStreetLightData("install.date",previousEdge2SLVData.getInstallDate(),paramsList);

        String slvCalender = "Surrey 100-50-off-50";
        try {
            slvCalender =  URLEncoder.encode(slvCalender,"UTF-8");
            addStreetLightData("DimmingGroupName",slvCalender,paramsList);
        }catch (Exception e){
            e.printStackTrace();
        }

        String modelFunctionId = "talq.streetlight.v1:lightNodeFunction6";

        try {
            modelFunctionId =  URLEncoder.encode(modelFunctionId,"UTF-8");
            addStreetLightData("modelfunctionid",modelFunctionId,paramsList);
            // addStreetLightData("nodeTypeStrId", modelFunctionId,paramsList);
        }catch (Exception e){
            e.printStackTrace();
        }

        setDeviceValues(paramsList,slvTransactionLogs);
    }
}
