package com.slvinterface.service;

import com.slvinterface.entity.SLVSyncTable;
import com.slvinterface.enumeration.SLVProcess;
import com.slvinterface.exception.NoValueException;
import com.slvinterface.exception.QRCodeAlreadyUsedException;
import com.slvinterface.exception.ReplaceOLCFailedException;
import com.slvinterface.json.*;
import org.apache.log4j.Logger;

import java.util.List;

public class UrbanControlSLVInterfaceService extends  SLVInterfaceService{

    private static final Logger logger = Logger.getLogger(UrbanControlSLVInterfaceService.class);

    public UrbanControlSLVInterfaceService()throws Exception{
        super();
    }


    /**
     * Iterate each form value and check whether value is already processed or synced. If not, synced to SLV.
     * @param formDataList
     * @param slvSyncTable
     */
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
               checkMacAddressExists(macAddress,slvSyncTable.getNoteName());
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
                replaceOLC("",slvSyncTable.getNoteName(),"",slvSyncTable);
                slvSyncTable.setStatus("Success");
                break;

            case UPDATE_DEVICE:
                slvSyncTable.setSelectedAction("Replace WorkFlow");
                replaceOLC("",slvSyncTable.getNoteName(),"",slvSyncTable);
                replaceOLC("",slvSyncTable.getNoteName(),previousEdge2SLVData.getMacAddress(),slvSyncTable);
                slvSyncTable.setStatus("Success");
                break;

            case NEW_DEVICE:
                slvSyncTable.setSelectedAction("Install WorkFlow");
                replaceOLC("",slvSyncTable.getNoteName(),previousEdge2SLVData.getMacAddress(),slvSyncTable);
                slvSyncTable.setStatus("Success");
                break;


        }
    }
}
