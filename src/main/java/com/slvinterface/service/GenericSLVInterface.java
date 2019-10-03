package com.slvinterface.service;

import com.slvinterface.entity.SLVSyncTable;
import com.slvinterface.entity.SLVTransactionLogs;
import com.slvinterface.enumeration.SLVProcess;
import com.slvinterface.exception.QRCodeAlreadyUsedException;
import com.slvinterface.exception.ReplaceOLCFailedException;
import com.slvinterface.exception.SLVConnectionException;
import com.slvinterface.json.*;
import com.slvinterface.utils.PropertiesReader;
import org.apache.log4j.Logger;

import java.net.URLEncoder;
import java.util.*;

public class GenericSLVInterface extends  SLVInterfaceService {
    private static final Logger logger = Logger.getLogger(GenericSLVInterface.class);

    public GenericSLVInterface()throws Exception{
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
            try{
                //Controller Str Id
                String controllerStrId = properties.getProperty("streetlight.controller.str.id");
                previousEdge2SLVData.setControllerStrId(controllerStrId);

                // Id On Controller
                if(previousEdge2SLVData.getIdOnController() == null){
                    previousEdge2SLVData.setIdOnController(slvSyncTable.getNoteName());
                }
                slvSyncTable.setIdOnController(previousEdge2SLVData.getIdOnController());

                previousEdge2SLVData.setIdOnController(URLEncoder.encode(previousEdge2SLVData.getIdOnController(),"UTF-8"));
                retryCount = 0;
                checkTokenValidity(edgeNote,previousEdge2SLVData);

                // Check MAC Address already Present.
                if((macAddress != null && !macAddress.trim().isEmpty()) || previousEdge2SLVData.getPriority().getType() == SLVProcess.REMOVE){
                    if(previousEdge2SLVData.getPriority().getType() == SLVProcess.REMOVE){
                        previousEdge2SLVData.setMacAddress("");
                        slvSync(slvSyncTable,previousEdge2SLVData);
                    }else{
                        checkMacAddressExists(macAddress,previousEdge2SLVData.getIdOnController());
                        slvSync(slvSyncTable,previousEdge2SLVData);
                    }
                }

                /*//Install Date
                String installDate  = dateFormat(slvSyncTable.getNoteCreatedDateTime());
                previousEdge2SLVData.setInstallDate(installDate);*/
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
                logger.info("Replace workflow called");
                slvSyncTable.setSelectedAction("Replace WorkFlow");
                replaceOLC(previousEdge2SLVData.getControllerStrId(),previousEdge2SLVData.getIdOnController(),"",slvSyncTable);
                logger.info("empty replace olc called");
                setDeviceVal(slvSyncTable,previousEdge2SLVData);
                replaceOLC(previousEdge2SLVData.getControllerStrId(),previousEdge2SLVData.getIdOnController(),previousEdge2SLVData.getMacAddress(),slvSyncTable);
                slvSyncTable.setStatus("Success");
                break;

            case NEW_DEVICE:
                slvSyncTable.setSelectedAction("Install WorkFlow");
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
        addStreetLightData("installStatus",previousEdge2SLVData.getInstallStatus(),paramsList);
//        addStreetLightData("MacAddress",previousEdge2SLVData.getMacAddress(),paramsList);
        addStreetLightData("install.date",previousEdge2SLVData.getInstallDate(),paramsList);

        /*String slvCalender = PropertiesReader.getProperties().getProperty("streetlight.calendar");
        if(slvCalender != null && !slvCalender.trim().isEmpty()){
            try {
                addStreetLightData("DimmingGroupName",slvCalender,paramsList);
            }catch (Exception e){
                e.printStackTrace();
            }
        }*/

        String modelFunctionId = PropertiesReader.getProperties().getProperty("streetlight.model.functionid");
        //String modelFunctionId = "talq.streetlight.v1:lightNodeFunction6";
        if(modelFunctionId != null && !modelFunctionId.trim().isEmpty()){
            try {
                addStreetLightData("modelFunctionId",modelFunctionId,paramsList);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        /*if(previousEdge2SLVData.getFixtureQRScan() != null){
            addStreetLightData("luminaire.installdate", previousEdge2SLVData.getInstallDate(), paramsList);
            buildFixtureStreetLightData(previousEdge2SLVData.getFixtureQRScan(),paramsList);
        }*/

        setDeviceValues(paramsList,slvTransactionLogs);
    }



    public void buildFixtureStreetLightData(String data, List<Object> paramsList)
            {
        String[] fixtureInfo = data.split(",");
        logger.info("Fixture QR Scan Val length" + fixtureInfo.length);
        if (fixtureInfo.length >= 13) {
            addStreetLightData("luminaire.brand", fixtureInfo[0], paramsList);

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
            addStreetLightData("luminaire.type", fixtureInfo[5], paramsList);
           // addStreetLightData("fixing.type", fixtureInfo[5], paramsList);
            addStreetLightData("device.luminaire.colortemp", fixtureInfo[6], paramsList);
            addStreetLightData("device.luminaire.lumenoutput", fixtureInfo[7], paramsList);
            addStreetLightData("luminaire.DistributionType", fixtureInfo[8], paramsList);
            addStreetLightData("luminaire.colorcode", fixtureInfo[9], paramsList);
            addStreetLightData("device.luminaire.drivermanufacturer", fixtureInfo[10], paramsList);
            addStreetLightData("device.luminaire.driverpartnumber", fixtureInfo[11], paramsList);
            addStreetLightData("ballast.dimmingtype", fixtureInfo[12], paramsList);
        }
    }


}
