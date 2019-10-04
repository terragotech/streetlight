package com.slvinterface.service;

import com.slvinterface.entity.SLVSyncTable;
import com.slvinterface.entity.SLVTransactionLogs;
import com.slvinterface.enumeration.SLVProcess;
import com.slvinterface.exception.QRCodeAlreadyUsedException;
import com.slvinterface.exception.ReplaceOLCFailedException;
import com.slvinterface.exception.SLVConnectionException;
import com.slvinterface.json.*;
import com.slvinterface.utils.PropertiesReader;
import com.vividsolutions.jts.geomgraph.Edge;
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

            processFormData(formData,currentEdge2SLVData,edgeNote);
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
        if(!previousEdge2SLVData.getPremiseNodeLocation().equals(""))
        {
            addStreetLightData("device.premise",previousEdge2SLVData.getPremiseNodeLocation(),paramsList);
        }
        if(!previousEdge2SLVData.getPoleNo().equals(""))
        {
            addStreetLightData("pole.status",previousEdge2SLVData.getPoleNo(),paramsList);
        }
        if(!previousEdge2SLVData.getStreetAddress().equals(""))
        {
            addStreetLightData("address",previousEdge2SLVData.getStreetAddress(),paramsList);
        }
        if(!previousEdge2SLVData.getLLCGrid().equals(""))
        {
            addStreetLightData("location.mapnumber",previousEdge2SLVData.getLLCGrid(),paramsList);
        }
        /*if(!previousEdge2SLVData.getFixtureOwnerShipCode().equals(""))
        {
            addStreetLightData("",previousEdge2SLVData.getFixtureOwnerShipCode(),paramsList);
        }*/
        if(!previousEdge2SLVData.getFixturecompatibleUnit().equals(""))
        {
            addStreetLightData("luminaire.partdescription",previousEdge2SLVData.getFixturecompatibleUnit(),paramsList);
        }

        if(!previousEdge2SLVData.getArmCompatibleUnit().equals(""))
        {
            addStreetLightData("fixing.model",previousEdge2SLVData.getArmCompatibleUnit(),paramsList);
        }
        if(!previousEdge2SLVData.getSupplyType().equals(""))
        {
            addStreetLightData("network.type",previousEdge2SLVData.getSupplyType(),paramsList);
        }
        if(!previousEdge2SLVData.getFixtureStyle().equals(""))
        {
            addStreetLightData("luminaire.style",previousEdge2SLVData.getFixtureStyle(),paramsList);
        }
        if(!previousEdge2SLVData.getLightInstallationDate().equals(""))
        {
            addStreetLightData("luminaire.installdate",previousEdge2SLVData.getLightInstallationDate(),paramsList);
        }
        if(!previousEdge2SLVData.getArmSize().equals(""))
        {
            addStreetLightData("fixing.brand",previousEdge2SLVData.getArmSize(),paramsList);
        }
        if(!previousEdge2SLVData.getArmType().equals(""))
        {
            addStreetLightData("fixing.type",previousEdge2SLVData.getArmType(),paramsList);
        }
        if(!previousEdge2SLVData.getLightLocationType().equals(""))
        {
            addStreetLightData("device.premise",previousEdge2SLVData.getLightLocationType(),paramsList);
        }
        if(!previousEdge2SLVData.getLatitude().equals(""))
        {
            addStreetLightData("lat",previousEdge2SLVData.getLatitude(),paramsList);
        }
        if(!previousEdge2SLVData.getLongitude().equals(""))
        {
            addStreetLightData("lng",previousEdge2SLVData.getLongitude(),paramsList);
        }
        if(!previousEdge2SLVData.getAssociatedTransformer().equals(""))
        {
            addStreetLightData("network.transformer",previousEdge2SLVData.getAssociatedTransformer(),paramsList);
        }
        if(!previousEdge2SLVData.getLlcVoltage().equals(""))
        {
            addStreetLightData("network.supplyvoltage",previousEdge2SLVData.getLlcVoltage(),paramsList);
        }
        if(!previousEdge2SLVData.getShade().equals(""))
        {
            addStreetLightData("luminaire.lightsource",previousEdge2SLVData.getShade(),paramsList);
        }
        if(!previousEdge2SLVData.getHeight().equals(""))
        {
            addStreetLightData("pole.height",previousEdge2SLVData.getHeight(),paramsList);
        }
        if(!previousEdge2SLVData.getPoleInstallationDate().equals(""))
        {
            addStreetLightData("pole.installdate",previousEdge2SLVData.getPoleInstallationDate(),paramsList);
        }
        if(!previousEdge2SLVData.getPoleColor().equals(""))
        {
            addStreetLightData("pole.colorcode",previousEdge2SLVData.getPoleColor(),paramsList);
        }
        if(!previousEdge2SLVData.getMaterial().equals(""))
        {
            addStreetLightData("pole.material",previousEdge2SLVData.getMaterial(),paramsList);
        }
        if(!previousEdge2SLVData.getSlopShroud().equals(""))
        {
            addStreetLightData("pole.groundtype",previousEdge2SLVData.getSlopShroud(),paramsList);
        }
        if(!previousEdge2SLVData.getPoleOwnershipCode().equals(""))
        {
            addStreetLightData("pole.type",previousEdge2SLVData.getPoleOwnershipCode(),paramsList);
        }
        if(!previousEdge2SLVData.getFixtureWattage().equals(""))
        {
            addStreetLightData("luminaire.proposedwattage",previousEdge2SLVData.getFixtureWattage(),paramsList);
        }
        if(!previousEdge2SLVData.getLampType().equals(""))
        {
            addStreetLightData("luminaire.type",previousEdge2SLVData.getLampType(),paramsList);
        }
        if(!previousEdge2SLVData.getFixtureType().equals(""))
        {
            addStreetLightData("fixing.type",previousEdge2SLVData.getFixtureType(),paramsList);
        }
        if(!previousEdge2SLVData.getFixtureColor().equals(""))
        {
            addStreetLightData("fixing.color",previousEdge2SLVData.getFixtureColor(),paramsList);
        }
        if(!previousEdge2SLVData.getInstallComments().equals(""))
        {
            addStreetLightData("comment",previousEdge2SLVData.getInstallComments(),paramsList);
        }
        addStreetLightData("DimmingGroupName",Edge2SLVData.CALENDAR,paramsList);
        addStreetLightData("luminaire.cabinet.name",Edge2SLVData.CABINET_CONTROLLER,paramsList);
        addStreetLightData("TimeZoneId",Edge2SLVData.TIMEZONE,paramsList);
        addStreetLightData("network.highvoltagethreshold",Edge2SLVData.HIGH_VOLTAGE_THRESHOLD,paramsList);
        addStreetLightData("network.lowvoltagethreshold",Edge2SLVData.LOW_VOLTAGE_THRESHOLD,paramsList);
        addStreetLightData("PowerFactorThreshold",Edge2SLVData.POWER_FACTOR_THRESHOLD,paramsList);
        addStreetLightData("device.nic.fallbackmode",Edge2SLVData.NIC_FALLBACK_MODE,paramsList);
        addStreetLightData("device.nic.defaultLightState",Edge2SLVData.DEFAULT_LIGHT_LEVEL,paramsList);
        addStreetLightData("device.nic.vlo",Edge2SLVData.VIRTUAL_POWER_OUTPUT,paramsList);
        addStreetLightData("device.nic.initialClo",Edge2SLVData.CLO_INITIAL_VALUE,paramsList);
        addStreetLightData("OnLuxLevel",Edge2SLVData.ON_LUX_LEVEL,paramsList);
        addStreetLightData("OffLuxLevel", Edge2SLVData.OFF_LUX_LEVEL,paramsList);
        addStreetLightData("device.photocell.pollInterval",Edge2SLVData.POLE_INTERVAL,paramsList);
        addStreetLightData("device.photocell.pollIntervalSamples",Edge2SLVData.SAMPLES_INTERVAL,paramsList);
        addStreetLightData("ballast.dimmingtype",Edge2SLVData.DIMMING_INTERFACE,paramsList);
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
        if(previousEdge2SLVData.getFixtureQRScan() != null){
            addStreetLightData("luminaire.serialnumber", previousEdge2SLVData.getFixtureQRScan(), paramsList);
            //buildFixtureStreetLightData(previousEdge2SLVData.getFixtureQRScan(),paramsList);
        }

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
