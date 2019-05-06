package com.slvinterface.service;

import com.slvinterface.entity.SLVSyncTable;
import com.slvinterface.entity.SLVTransactionLogs;
import com.slvinterface.exception.NoValueException;
import com.slvinterface.exception.QRCodeAlreadyUsedException;
import com.slvinterface.exception.ReplaceOLCFailedException;
import com.slvinterface.json.*;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class SurreySLVInterface extends  SLVInterfaceService {
    private static final Logger logger = Logger.getLogger(UrbanControlSLVInterfaceService.class);

    public SurreySLVInterface()throws Exception{
        super();
    }



    // 118 - Controller ID, Node Installation Date -  155,Scan Node QR Code - 85,New Node QR Code-132,Replacement Date-159,
    public void processFormData(List<FormData> formDataList, SLVSyncTable slvSyncTable){
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
                checkMacAddressExists(macAddress,slvSyncTable.getIdOnController());
                slvSync(slvSyncTable,previousEdge2SLVData);
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
     * Populate EdgeValue to Edge2SLVData from FormValue based on Configuration JSON
     * @param formData
     * @param edge2SLVData
     */
    private void processFormData(FormData formData,Edge2SLVData edge2SLVData){
        List<FormValues> formValuesList = formData.getFormDef();
        List<Priority> priorities = conditionsJson.getPriority();
        List<Config> configList = conditionsJson.getConfigList();
        for(Priority priority : priorities){
            Config temp = new Config();
            temp.setType(priority.getType());

            int pos = configList.indexOf(priorities);

            if(pos != -1){
                Config config =  configList.get(pos);
                List<Id> idList = config.getIds();
                for(Id id : idList){
                    switch (id.getType()){
                        case MAC:
                            try{
                                String macAddress = valueById(formValuesList,id.getId()).toUpperCase();
                                edge2SLVData.setMacAddress(macAddress);
                                edge2SLVData.setPriority(priority);
                            }catch (NoValueException e){
                                e.printStackTrace();
                            }


                            break;
                        case FIXTURE:
                            try{
                                String installDate = valueById(formValuesList,id.getId());
                                installDate =  dateFormat(Long.valueOf(installDate));
                                edge2SLVData.setInstallDate(installDate);
                            }catch (NoValueException e){
                                e.printStackTrace();
                            }

                            break;

                        case IDONCONTROLLER:
                            try{
                                String idOnController = valueById(formValuesList,id.getId());
                                edge2SLVData.setIdOnController(idOnController);
                            }catch (NoValueException e){
                                e.printStackTrace();
                            }
                            break;
                    }
                }

                if(edge2SLVData.getPriority() != null){
                    return;
                }
            }
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
                replaceOLC(previousEdge2SLVData.getControllerStrId(),previousEdge2SLVData.getIdOnController(),"",slvSyncTable);
                replaceOLC(previousEdge2SLVData.getControllerStrId(),previousEdge2SLVData.getIdOnController(),previousEdge2SLVData.getMacAddress(),slvSyncTable);

                setDeviceVal(slvSyncTable,previousEdge2SLVData);
                slvSyncTable.setStatus("Success");
                break;

            case NEW_DEVICE:
                slvSyncTable.setSelectedAction("Install WorkFlow");
                replaceOLC(previousEdge2SLVData.getControllerStrId(),previousEdge2SLVData.getIdOnController(),previousEdge2SLVData.getMacAddress(),slvSyncTable);

                setDeviceVal(slvSyncTable,previousEdge2SLVData);
                slvSyncTable.setStatus("Success");
                break;


        }
    }


    private void setDeviceVal(SLVSyncTable slvSyncTable,Edge2SLVData previousEdge2SLVData){
        SLVTransactionLogs slvTransactionLogs = getSLVTransVal(slvSyncTable);
        List<Object> paramsList = new ArrayList<>();
        loadVal(paramsList,slvSyncTable);
        addStreetLightData("installStatus","Installed",paramsList);
        addStreetLightData("install.date",previousEdge2SLVData.getInstallDate(),paramsList);
        addStreetLightData("SLV.Calendar","Surrey 100-50-off-50",paramsList);
        setDeviceValues(paramsList,slvTransactionLogs);
    }


    private void loadVal( List<Object> paramsList,SLVSyncTable slvSyncTable){
        paramsList.add("idOnController=" + slvSyncTable.getNoteName());
        paramsList.add("controllerStrId=");
    }


    protected String dateFormat(Long dateTime) {
        Date date = new Date(Long.valueOf(dateTime));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String dff = dateFormat.format(date);
        return dff;
    }

}
