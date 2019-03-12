package com.terragoedge.streetlight.service;

import com.terragoedge.edgeserver.*;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.exception.*;
import com.terragoedge.streetlight.json.model.CslpDate;
import com.terragoedge.streetlight.json.model.DuplicateMacAddress;
import com.terragoedge.streetlight.json.model.SlvServerData;
import com.terragoedge.streetlight.logging.InstallMaintenanceLogModel;
import com.terragoedge.streetlight.logging.LoggingModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;

public class InstallationMaintenanceProcessor extends AbstractProcessor {

    public InstallationMaintenanceProcessor(WeakHashMap<String, String> contextListHashMap, HashMap<String, CslpDate> cslpDateHashMap) {
        super();
        this.contextListHashMap = contextListHashMap;
        this.cslpDateHashMap = cslpDateHashMap;
    }

    private static final String INSTATALLATION_AND_MAINTENANCE_GUID = "8b722347-c3a7-41f4-a8a9-c35dece6f98b";

    final Logger logger = Logger.getLogger(InstallationMaintenanceProcessor.class);


    private LoggingModel getExistingIdOnContoller(EdgeNote edgeNote) {
        String fixtureFormTemplateGuid = properties.getProperty("streetlight.edge.formtemplateguid.fixture");
        LoggingModel loggingModel = new LoggingModel();
        for (FormData formData : edgeNote.getFormData()) {
            if (formData.getFormTemplateGuid().equals(fixtureFormTemplateGuid)) {
                List<EdgeFormData> fixtureFromDef = formData.getFormDef();
                loggingModel.setIdOnController(getIdOnController(fixtureFromDef));
                loggingModel.setControllerSrtId(getControllerStrId(fixtureFromDef));
            }

        }
        return loggingModel;

    }


    private String getNightRideFormVal(List<FormData> formDataList, EdgeNote edgeNote, LoggingModel loggingModel) {
        String nightRideTemplateGuid = properties.getProperty("amerescousa.night.ride.formtemplateGuid");
        int pos = formDataList.indexOf(nightRideTemplateGuid);
        if (pos != -1) {
            FormData formData = formDataList.get(pos);
            List<EdgeFormData> edgeFormDatas = formData.getFormDef();
            try {
                String nightRideValue = valueById(edgeFormDatas, 1);
                try {
                    SlvServerData dbSlvServerData = streetlightDao.getSlvServerData(edgeNote.getTitle());
                    if (dbSlvServerData != null && dbSlvServerData.getSerialNumber() != null) {
                        List<String> nightRideList = new ArrayList<>();
                        String serialNumber = dbSlvServerData.getSerialNumber();
                        String[] nightRideValues = serialNumber.split(",");
                        for (String nightRideVal : nightRideValues) {
                            String[] nightRideValArray = nightRideVal.split(":");
                            if (nightRideValArray.length > 1) {
                                nightRideList.add(nightRideValArray[1]);
                            }
                        }
                        boolean res = nightRideList.contains(nightRideValue);
                        loggingModel.setNigthRideSame(res);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return dateFormat(edgeNote.getCreatedDateTime()) + " :" + nightRideValue;

            } catch (NoValueException e) {

            }
        }
        return null;
    }

    private void syncNightRideFormAlone(List<FormData> formDataList, InstallMaintenanceLogModel installMaintenanceLogModel, EdgeNote edgeNote) {
        try {
            String nightRideKey = properties.getProperty("amerescousa.night.ride.key_for_slv");
            try {

                String nightRideTemplateGuid = properties.getProperty("amerescousa.night.ride.formtemplateGuid");
                int pos = formDataList.indexOf(nightRideTemplateGuid);
                if (pos != -1) {
                    List<Object> paramsList = new ArrayList<>();
                    String nightRideValue = getNightRideFormVal(formDataList, edgeNote, installMaintenanceLogModel);
                    if (nightRideValue != null) {
                        String idOnController = installMaintenanceLogModel.getIdOnController();
                        paramsList.add("idOnController=" + idOnController);
                        paramsList.add("controllerStrId=" + installMaintenanceLogModel.getControllerSrtId());
                        addStreetLightData(nightRideKey, nightRideValue, paramsList);

                        int errorCode = setDeviceValues(paramsList);
                        if (errorCode != 0) {
                            installMaintenanceLogModel.setErrorDetails(MessageConstants.ERROR_NIGHTRIDE_FORM_VAL);
                            installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
                            return;
                        } else {
                            logger.info("Night ride value successfully updated");
                            installMaintenanceLogModel.setStatus(MessageConstants.SUCCESS);
                            logger.info("Status Changed. to Success");
                        }
                    } else {
                        installMaintenanceLogModel.setErrorDetails(MessageConstants.NOVALUE_NIGHTRIDE_FORM);
                        installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
                    }

                }
            } catch (Exception e) {
                logger.error("Error in while getting nightRideValue's value : ", e);
                installMaintenanceLogModel.setErrorDetails("Error while syncing Night Ride value.");
                installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error in while getting nightRideValue's value : ", e);
            installMaintenanceLogModel.setErrorDetails("Error while syncing Night Ride value.");
            installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
        }
    }

    public void processNewAction(EdgeNote edgeNote, InstallMaintenanceLogModel installMaintenanceLogModel, boolean isReSync, String utilLocId) {
        logger.info("processNewAction");
        List<FormData> formDatas = edgeNote.getFormData();

        String nightRideKey = properties.getProperty("amerescousa.night.ride.key_for_slv");
        String formatedValueNR = getNightRideFormVal(formDatas, edgeNote, installMaintenanceLogModel);

        boolean isInstallForm = false;
        for (FormData formData : formDatas) {
            logger.info("Processing Form :" + formData.getFormTemplateGuid());
            if (formData.getFormTemplateGuid().equals(INSTATALLATION_AND_MAINTENANCE_GUID) || formData.getFormTemplateGuid().equals("fa47c708-fb82-4877-938c-992e870ae2a4") || formData.getFormTemplateGuid().equals("c8acc150-6228-4a27-bc7e-0fabea0e2b93")) {
                isInstallForm = true;
                logger.info("Install  Form  is present.");
                installMaintenanceLogModel.setInstallFormPresent(true);
                LoggingModel existingFixtureInfo = getExistingIdOnContoller(edgeNote);
                List<EdgeFormData> edgeFormDatas = formData.getFormDef();
                try {
                    logger.info("before Action Val");
                    String value = null;
                    try {
                        value = getAction(edgeFormDatas, edgeNote.getTitle(), installMaintenanceLogModel, edgeNote);
                        logger.info("After Action Val");
                    } catch (AlreadyUsedException w) {
                        if (installMaintenanceLogModel.isNigthRideSame()) {
                            installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
                            continue;
                        }
                    }

                    logger.info("Action Val is:" + value);
                    if (value != null) {
                        switch (value) {
                            case "New":
                                processNewGroup(edgeFormDatas, edgeNote, installMaintenanceLogModel, isReSync, existingFixtureInfo, utilLocId, nightRideKey, formatedValueNR);
                                installMaintenanceLogModel.setInstalledDate(edgeNote.getCreatedDateTime());
                                break;
                            case "Repairs & Outages":
                                repairAndOutage(edgeFormDatas, edgeNote, installMaintenanceLogModel, existingFixtureInfo, utilLocId, nightRideKey, formatedValueNR, formatedValueNR);
                                installMaintenanceLogModel.setReplacedDate(edgeNote.getCreatedDateTime());
                                break;
                            case "Remove":
                                processRemoveAction(edgeFormDatas,existingFixtureInfo,utilLocId);
                                break;
                            case "Other Task":
                                // processOtherTask(edgeFormDatas, edgeNote, installMaintenanceLogModel, nightRideKey, formatedValueNR);
                                break;
                        }
                    }

                } catch (Exception e) {
                    logger.error("error in processNewAction method", e);
                    installMaintenanceLogModel.setErrorDetails(MessageConstants.ACTION_NO_VAL);
                    installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
                }
            }
        }

        if (!isInstallForm) {
            syncNightRideFormAlone(formDatas, installMaintenanceLogModel, edgeNote);
        }
    }


    private String getAction(List<EdgeFormData> edgeFormDatas, String idOnController, LoggingModel loggingModel, EdgeNote edgeNote) throws AlreadyUsedException {
        try {
            String value = value(edgeFormDatas, "Action");
            if (value.equals("Repairs & Outages")) {
                String repairsOutagesValue = null;
                try {
                    repairsOutagesValue = valueById(edgeFormDatas, 24);
                    if (repairsOutagesValue.equals("Power Issue")) {
                        loggingModel.setRepairsOption("Power Issue");
                        return "Repairs & Outages";
                    } else if (repairsOutagesValue.equals("Unable to Repair(CDOT Issue)")) {
                        loggingModel.setRepairsOption("Unable to Repair(CDOT Issue)");
                        return "Repairs & Outages";
                    }


                } catch (NoValueException e) {
                    loggingModel.setStatus(MessageConstants.ERROR);
                    loggingModel.setErrorDetails("Repairs & Outages options are not selected.");
                }

            }else if(value.equals("Remove")){
                return value;
            }
        } catch (NoValueException e) {
            e.printStackTrace();
        }


        // Replace Fixture Only
        String newFixtureQrScanValue = null;
        try {
            newFixtureQrScanValue = valueById(edgeFormDatas, 39);
            checkFixtureQrScan(newFixtureQrScanValue, edgeNote, loggingModel);
        } catch (NoValueException e) {

        } catch (InValidBarCodeException e) {
            // Replace Fixture Only, If QR Code is invalid, then no need to process.
            loggingModel.setFixtureQRSame(true);
            // No need to worry, crew may be node replaced.
        }

        //Check Replace Node only option
        try {
            String newNodeMacAddress = valueById(edgeFormDatas, 30);
            checkMacAddressExists(newNodeMacAddress, idOnController, null, null, loggingModel);
            loggingModel.setRepairsOption("Replace Node only");
            return "Repairs & Outages";
        } catch (NoValueException e) {
            // Crew may be replaced Fixture QR value.
            if (newFixtureQrScanValue != null) {
                if (loggingModel.isFixtureQRSame()) {
                    throw new AlreadyUsedException("QR Scan Already Used");
                }
                loggingModel.setRepairsOption("Replace Fixture only");
                return "Repairs & Outages";
            }
        } catch (Exception e) {
            if (newFixtureQrScanValue != null) {
                if (loggingModel.isFixtureQRSame()) {
                    throw new AlreadyUsedException("QR Scan Already Used");
                }
                loggingModel.setRepairsOption("Replace Fixture only");
                return "Repairs & Outages";
            }
            throw new AlreadyUsedException(e.getMessage());

        }

        loggingModel.setMacAddressUsed(false);
        loggingModel.setFixtureQRSame(false);

        try {
            validateValue(edgeFormDatas, idOnController, loggingModel, edgeNote, 26, 38);
            loggingModel.setRepairsOption("Replace Node and Fixture");
            return "Repairs & Outages";
        } catch (AlreadyUsedException e) {
            throw new AlreadyUsedException(e.getMessage());
        } catch (NoValueException e) {

        }


        try {
            validateValue(edgeFormDatas, idOnController, loggingModel, edgeNote, 19, 20);
            return "New";
        } catch (AlreadyUsedException e) {
            throw new AlreadyUsedException(e.getMessage());
        } catch (NoValueException e) {
            return "New";
        }
    }


    private void validateValue(List<EdgeFormData> edgeFormDatas, String idOnController, LoggingModel loggingModel, EdgeNote edgeNote, int macAddressId, int qrScanId) throws AlreadyUsedException, NoValueException {
        //Replace Node and Fixture
        String newNodeMacAddress = null;
        try {
            newNodeMacAddress = valueById(edgeFormDatas, macAddressId);
        } catch (NoValueException e) {
            loggingModel.setMacAddressUsed(true);
        }


        String fixerQrScanValue = null;
        try {
            fixerQrScanValue = valueById(edgeFormDatas, qrScanId);
        } catch (NoValueException e) {
            loggingModel.setFixtureQRSame(true);
        }

        if (newNodeMacAddress == null && fixerQrScanValue == null) {
            loggingModel.setMacAddressUsed(false);
            loggingModel.setFixtureQRSame(false);
            throw new NoValueException("MAC Address and Fixture QR Scan value is Empty");
        }

        if (newNodeMacAddress != null && !newNodeMacAddress.startsWith("00") && (fixerQrScanValue != null && fixerQrScanValue.startsWith("00"))) {
            String temp = newNodeMacAddress;
            newNodeMacAddress = fixerQrScanValue;
            fixerQrScanValue = temp;
        }


        //Suppose fixtureqrscan value has mac address but MAC Address is empty
        if(newNodeMacAddress == null &&  (fixerQrScanValue != null && fixerQrScanValue.startsWith("00"))){
            newNodeMacAddress = fixerQrScanValue;
            fixerQrScanValue = null;
            loggingModel.setFixtureQRSame(true);
        }

        if (newNodeMacAddress != null) {
            try {
                checkMacAddressExists(newNodeMacAddress, idOnController, null, null, loggingModel);
            } catch (QRCodeAlreadyUsedException e) {
                // No need to worry, Fixture QR Scan may be differ. So let's continue.
            } catch (Exception e) {
                // No need to worry, Fixture QR Scan may be differ. So let's continue.
            }

        }

        if (fixerQrScanValue != null) {
            try {
                checkFixtureQrScan(fixerQrScanValue, edgeNote, loggingModel);
            } catch (InValidBarCodeException e) {
                loggingModel.setFixtureQRSame(true);
                // No need to process,bcs its invalid.
                if (loggingModel.isMacAddressUsed()) {
                    throw new AlreadyUsedException(e.getMessage());
                }
            }
        }

        if (loggingModel.isMacAddressUsed() && loggingModel.isFixtureQRSame()) {
            throw new AlreadyUsedException("MAC Address and QR Scan Already Used.");
        }


    }


    private void checkFixtureQrScan(String fixtureQrScan, EdgeNote edgeNote, LoggingModel loggingModel) throws InValidBarCodeException {
        List<Object> paramsList = new ArrayList<>();
        SlvServerData slvServerData = new SlvServerData();
        try {
            logger.info("Fixture QR Scan Validation Starts.");
            buildFixtureStreetLightData(fixtureQrScan, paramsList, edgeNote, slvServerData,loggingModel);
            paramsList.clear();
            slvServerData.setIdOnController(edgeNote.getTitle());

            String mainUrl = properties.getProperty("streetlight.slv.url.main");
            String updateDeviceValues = properties.getProperty("streetlight.slv.url.search.device");
            String url = mainUrl + updateDeviceValues;
            paramsList.add("attribute=idOnController");
            paramsList.add("value=" + edgeNote.getTitle().trim());

            addFixtureQrScanData("luminaire.brand", slvServerData.getLuminaireBrand(), paramsList);
            addFixtureQrScanData("device.luminaire.partnumber", slvServerData.getLuminairePartNumber(), paramsList);
            addFixtureQrScanData("luminaire.model", slvServerData.getLuminaireModel(), paramsList);
            addFixtureQrScanData("device.luminaire.manufacturedate", slvServerData.getLuminaireManufacturedate(), paramsList);
            addFixtureQrScanData("device.luminaire.driverpartnumber", slvServerData.getDriverPartNumber(), paramsList);
            addFixtureQrScanData("luminaire.colorcode", slvServerData.getColorCode(), paramsList);
            paramsList.add("operator=eq-i");
            paramsList.add("recurse=true");
            paramsList.add("ser=json");
            String params = StringUtils.join(paramsList, "&");
            url = url + "?" + params;
            ResponseEntity<String> response = restService.getRequest(url, true, null);
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseString = response.getBody();
                logger.info("-------QR Address Check Address----------");
                logger.info(responseString);
                logger.info("-------QR Address Check End----------");

                DeviceMacAddress deviceMacAddress = gson.fromJson(responseString, DeviceMacAddress.class);
                List<Value> values = deviceMacAddress.getValue();
                StringBuilder stringBuilder = new StringBuilder();
                if (values == null || values.size() == 0) {
                    logger.info("Fixture QR Not present.");
                    loggingModel.setFixtureQRSame(false);
                } else {
                    logger.info("Fixture QR is present.");
                    loggingModel.setFixtureQRSame(true);
                }
            }


            logger.info("Fixture QR Scan Validation End.");
        } catch (InValidBarCodeException e) {
            loggingModel.setFixtureQRSame(true);
            throw new InValidBarCodeException(e.getMessage());
        }catch (Exception e){
            loggingModel.setFixtureQRSame(true);
            throw new InValidBarCodeException(e.getMessage());

        }

    }

    /*private void checkFixtureQrScan(String fixtureQrScan, EdgeNote edgeNote, LoggingModel loggingModel) throws InValidBarCodeException {
        List<Object> paramsList = new ArrayList<>();
        SlvServerData slvServerData = new SlvServerData();
        try {
            buildFixtureStreetLightData(fixtureQrScan, paramsList, edgeNote, slvServerData);
            slvServerData.setIdOnController(edgeNote.getTitle());
            SlvServerData dbSlvServerData = streetlightDao.getSlvServerData(edgeNote.getTitle());
            if (slvServerData.equals(dbSlvServerData)) {
                loggingModel.setFixtureQRSame(true);
            }
        } catch (InValidBarCodeException e) {
            throw new InValidBarCodeException(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }*/


    private int sync2SlvInstallStatus(String idOnController, String controllerStrIdValue, InstallMaintenanceLogModel loggingModel, String nightRideKey, String nightRideValue) {
        List<Object> paramsList = new ArrayList<>();
        String installStatus = properties.getProperty("could_note_complete_install_status");
        paramsList.add("idOnController=" + idOnController);
        paramsList.add("controllerStrId=" + controllerStrIdValue);
        if (nightRideValue != null) {
            addStreetLightData(nightRideKey, nightRideValue, paramsList);
        }
        addStreetLightData("installStatus", installStatus, paramsList);
        int errorCode = setDeviceValues(paramsList);
        logger.info("Error code" + errorCode);
        return errorCode;
    }

    private void sync2Slv(String macAddress, String fixerQrScanValue, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, String idOnController, String controllerStrIdValue, String comment, String utilLocId, boolean isNew, String nightRideKey, String nightRideValue) {
        try {

            List<Object> paramsList = new ArrayList<>();

            paramsList.add("idOnController=" + idOnController);
            paramsList.add("controllerStrId=" + controllerStrIdValue);
            SlvServerData slvServerData = new SlvServerData();

            addOtherParams(edgeNote, paramsList, idOnController, utilLocId, isNew, fixerQrScanValue, macAddress, loggingModel);

            if (fixerQrScanValue != null && !loggingModel.isFixtureQRSame()) {
                buildFixtureStreetLightData(fixerQrScanValue, paramsList, edgeNote, slvServerData,loggingModel);//update fixer qrscan value
            }

            if (comment != null) {
                addStreetLightData("comment", comment, paramsList);
            }

            if (nightRideValue != null) {
                addStreetLightData(nightRideKey, nightRideValue, paramsList);
            }
            if (macAddress != null && !macAddress.trim().isEmpty() && !loggingModel.isMacAddressUsed()) {
                boolean isNodeDatePresent = isNodeDatePresent(idOnController);
                if (!isNodeDatePresent) {
                    addStreetLightData("cslp.node.install.date", dateFormat(edgeNote.getCreatedDateTime()), paramsList);
                }
                addStreetLightData("install.date", dateFormat(edgeNote.getCreatedDateTime()), paramsList);
                addStreetLightData("MacAddress", macAddress, paramsList);
            }


            int errorCode = setDeviceValues(paramsList);
            logger.info("Error code" + errorCode);
            if (errorCode != 0) {
                loggingModel.setErrorDetails(MessageConstants.ERROR_UPDATE_DEVICE_VAL);
                loggingModel.setStatus(MessageConstants.ERROR);
                return;
            } else {
                logger.info("Replace OLC called");
                // replace OlC
                System.out.println("New :" + isNew + " \nmacAddress :" + macAddress);
                logger.info("New :" + isNew + " \nmacAddress :" + macAddress);
                if (isNew && macAddress == null || macAddress.isEmpty()) {
                    loggingModel.setStatus(MessageConstants.SUCCESS);
                    return;
                } else {
                    if (!loggingModel.isMacAddressUsed()) {
                        replaceOLC(controllerStrIdValue, idOnController, macAddress);// insert mac address
                    }

                }
                logger.info("Replace OLC End");
                loggingModel.setStatus(MessageConstants.SUCCESS);
                logger.info("Status Changed. to Success");
            }

        } catch (InValidBarCodeException e) {
            logger.error("Error in processNewGroup", e);
            loggingModel.setStatus(MessageConstants.ERROR);
            loggingModel.setErrorDetails(e.getMessage());

        } catch (Exception e) {
            logger.error("Error in processNewGroup", e);
            loggingModel.setStatus(MessageConstants.ERROR);
            loggingModel.setErrorDetails(e.getMessage());

        }
    }


    private String getIdOnController(List<EdgeFormData> fixtureFromDef) {
        // Get IdOnController value
        String idOnController = null;
        try {
            idOnController = value(fixtureFromDef, properties.getProperty("edge.fortemplate.fixture.label.idoncntrl"));
        } catch (NoValueException e) {
        }
        return idOnController;

    }


    private String getControllerStrId(List<EdgeFormData> fixtureFromDef) {
        // Get ControllerStdId value
        String controllerStrId = null;
        try {
            controllerStrId = value(fixtureFromDef, properties.getProperty("edge.fortemplate.fixture.label.cnrlstrid"));
        } catch (NoValueException e) {
        }
        return controllerStrId;
    }

    public void processOtherTask(List<EdgeFormData> edgeFormDatas, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, String nightRideKey, String nightRideValue) {
        logger.info("Other task value processed");
        String Key = properties.getProperty("amerescousa.night.ride.key_for_slv");
        logger.info("key :" + Key);
        List<Object> paramsList = new ArrayList<>();
        String cdotIssue = null;
        try {
            cdotIssue = valueById(edgeFormDatas, 106);
            logger.info("cdotIssue's issue Value :" + cdotIssue);
        } catch (NoValueException e) {
            logger.error("Error in while getting cdotIssue's issue", e);
            loggingModel.setErrorDetails("Value is not selected");
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        }
        logger.info("value :" + cdotIssue);
        String controllerStarId = getControllerStrId(edgeFormDatas);
        String idOnController = loggingModel.getIdOnController();
        paramsList.add("idOnController=" + idOnController);
        paramsList.add("controllerStrId=" + controllerStarId);
        String formatedValue = dateFormat(edgeNote.getCreatedDateTime()) + " :" + cdotIssue;

        if (nightRideValue != null) {
            nightRideValue = nightRideValue + "," + cdotIssue;
            addStreetLightData(nightRideKey, nightRideValue, paramsList);
        } else {
            addStreetLightData(Key, formatedValue, paramsList);
        }
        int errorCode = setDeviceValues(paramsList);
        if (errorCode != 0) {
            logger.info("CDOT issue value serDevice error");
            loggingModel.setErrorDetails(MessageConstants.ERROR_OTHERTASK_DEVICE_VAL);
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        } else {
            logger.info("OtherTask's value updated successfully in SLV");
            loggingModel.setStatus(MessageConstants.SUCCESS);
            logger.info("Status Changed. to Success");
        }
    }


    private void processNewGroup(List<EdgeFormData> edgeFormDatas, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, boolean isResync, LoggingModel existingFixtureInfo, String utilLocId, String nightRideKey, String nightRideValue) {
        try {

            // Get Install status
            String installStatusValue = null;
            try {
                installStatusValue = valueById(edgeFormDatas, 22);
                logger.info("installStatus Val" + installStatusValue);
            } catch (NoValueException e) {
                logger.error("Error in while getting installStatusValue", e);
            }
            if (installStatusValue != null && installStatusValue.equals("Could not complete")) {
                try {
                    String skippedFixtureReasonVal = valueById(edgeFormDatas, 23);
                    logger.info("Skipped Fixture Reason Val" + installStatusValue);
                    if (nightRideValue != null && !nightRideValue.trim().isEmpty()) {
                        nightRideValue = nightRideValue + "," + skippedFixtureReasonVal;
                    } else {
                        nightRideValue = dateFormat(edgeNote.getCreatedDateTime()) + " :" + skippedFixtureReasonVal;
                    }
                } catch (NoValueException e) {
                    logger.error("Error in while getting installStatusValue", e);
                }

                int errorCode = sync2SlvInstallStatus(loggingModel.getIdOnController(), loggingModel.getControllerSrtId(), loggingModel, nightRideKey, nightRideValue);
                if (errorCode != 0) {
                    loggingModel.setErrorDetails("Error while updating Could not complete install status.Corresponding Error code :" + errorCode);
                    loggingModel.setStatus(MessageConstants.ERROR);
                } else {
                    loggingModel.setErrorDetails(MessageConstants.COULD_NOT_COMPLETE_SUCCESS_MSG);
                    loggingModel.setStatus(MessageConstants.SUCCESS);
                }

                return;
            }

            // Get MAC Address
            String nodeMacValue = null;
            try {
                nodeMacValue = valueById(edgeFormDatas, 19);
                logger.info("Node MAC Val" + nodeMacValue);
            } catch (NoValueException e) {
                logger.error("Error in while getting MAC Address", e);
                // loggingModel.setErrorDetails(MessageConstants.NODE_MAC_ADDRESS_NOT_AVAILABLE);
                // loggingModel.setStatus(MessageConstants.ERROR);
                // return;
            }

            // Get Fixer QR Scan value
            String fixerQrScanValue = null;
            try {
                fixerQrScanValue = valueFixtureValueById(edgeFormDatas, 20);
                logger.info("Fixture QR Scan Val" + fixerQrScanValue);
                if (fixerQrScanValue == null || fixerQrScanValue.contains("null") || fixerQrScanValue.trim().isEmpty()) {
                    fixerQrScanValue = valueById(edgeFormDatas, 115);
                    logger.info("Fixture QR Scan Val - 115" + fixerQrScanValue);
                }
            } catch (NoValueException e) {

                logger.info("Fixture QR Scan Val" + fixerQrScanValue);
                logger.error("Error in while getting MAC Fixture QR Scan", e);
                /*loggingModel.setErrorDetails(MessageConstants.FIXTURE_CODE_NOT_AVAILABLE);
                loggingModel.setStatus(MessageConstants.ERROR);*/
                // loggingModel.setProcessOtherForm(true);
                // return;
            }
            if ((nodeMacValue == null && fixerQrScanValue == null) || ((nodeMacValue != null && nodeMacValue.isEmpty()) && (fixerQrScanValue != null && fixerQrScanValue.isEmpty()))) {
                loggingModel.setStatus(MessageConstants.ERROR);
                loggingModel.setErrorDetails("No MacAddress and FixtureQrScan");
                return;
            }
            if (nodeMacValue != null && !nodeMacValue.startsWith("00") && (fixerQrScanValue != null && fixerQrScanValue.startsWith("00"))) {
                String temp = nodeMacValue;
                nodeMacValue = fixerQrScanValue;
                fixerQrScanValue = temp;
            }

            //Suppose fixtureqrscan value has mac address but MAC Address is empty
            if(nodeMacValue == null &&  (fixerQrScanValue != null && fixerQrScanValue.startsWith("00"))){
                nodeMacValue = fixerQrScanValue;
                fixerQrScanValue = null;
                loggingModel.setFixtureQRSame(true);
            }

            if ((nodeMacValue == null || nodeMacValue.isEmpty()) && fixerQrScanValue != null) {
                loggingModel.setFixtureOnly(true);
            } else {
                loggingModel.setFixtureOnly(false);
            }

            if (isResync) {
                try {
                    replaceOLC(loggingModel.getControllerSrtId(), loggingModel.getIdOnController(), "");
                } catch (Exception e) {
                    String message = e.getMessage();
                }

            }

            // Check Whether MAC Address is already assigned to other fixtures or not.
            try {
                if (nodeMacValue != null)
                    checkMacAddressExists(nodeMacValue, loggingModel.getIdOnController(), nightRideKey, nightRideValue, loggingModel);
            } catch (QRCodeAlreadyUsedException e1) {
                logger.error("MacAddress (" + e1.getMacAddress()
                        + ")  - Already in use. So this pole is not synced with SLV. Note Title :[" + edgeNote.getTitle()
                        + " ]");
            }
            loggingModel.setMacAddress(nodeMacValue);
            sync2Slv(nodeMacValue, fixerQrScanValue, edgeNote, loggingModel, loggingModel.getIdOnController(), loggingModel.getControllerSrtId(), null, utilLocId, true, nightRideKey, nightRideValue);
        } catch (NoValueException e) {
            logger.error("Error no value", e);
            loggingModel.setErrorDetails(e.getMessage());
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        } catch (Exception e) {
            loggingModel.setErrorDetails(MessageConstants.ERROR + "" + e.getMessage());
            loggingModel.setStatus(MessageConstants.ERROR);
            logger.error("Error ", e);
        }

    }


    public void repairAndOutage(List<EdgeFormData> edgeFormDatas, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, LoggingModel existingFixtureInfo, String utilLocId, String nightRideKey, String nightRideValue, String formatedValueNR) {

        String repairsOutagesValue = loggingModel.getRepairsOption();
        /*try {
            repairsOutagesValue = valueById(edgeFormDatas, 24);
        } catch (NoValueException e) {
            loggingModel.setStatus(MessageConstants.ERROR);
            loggingModel.setErrorDetails("Repairs & Outages options are not selected.");
            return;
        }*/

        System.out.println(repairsOutagesValue);
        switch (repairsOutagesValue) {
            case "Replace Node and Fixture":
                replaceNodeFixture(edgeFormDatas, edgeNote, loggingModel, existingFixtureInfo, utilLocId, nightRideKey, nightRideValue);
                break;
            case "Replace Node only":
                replaceNodeOnly(edgeFormDatas, edgeNote, loggingModel, existingFixtureInfo, utilLocId, nightRideKey, nightRideValue);
                break;
            case "Replace Fixture only":

                String fixerQrScanValue = null;
                try {
                    fixerQrScanValue = valueById(edgeFormDatas, 39);
                    String idOnController = loggingModel.getIdOnController();
                    String controllerStrIdValue = loggingModel.getControllerSrtId();
                    sync2Slv(null, fixerQrScanValue, edgeNote, loggingModel, idOnController, controllerStrIdValue, "", utilLocId, false, nightRideKey, nightRideValue);
                    break;
                } catch (NoValueException e) {
                    loggingModel.setStatus(MessageConstants.ERROR);
                    loggingModel.setErrorDetails("Fixture QR Scan value is empty");
                }


            case "Power Issue":
                loggingModel.setStatus(MessageConstants.ERROR);
                loggingModel.setErrorDetails("SLV Interface Doest not support this(Power Issue option is selected).");
                break;
            case "Unable to Repair(CDOT Issue)":
                logger.info("Processed Unable to Repair option :" + edgeNote.getTitle());
                logger.info("Processed NoteGuid :" + edgeNote.getNoteGuid());
                //  String nightRideKey = properties.getProperty("amerescousa.night.ride.key_for_slv");
                processOtherTask(edgeFormDatas, edgeNote, loggingModel, nightRideKey, formatedValueNR);
                break;
        }
    }


    private void replaceNodeFixture(List<EdgeFormData> edgeFormDatas, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, LoggingModel existingFixtureInfo, String utilLocId, String nightRideKey, String nightRideValue) {
        try {
            String existingNodeMacAddress = null;
            String newNodeMacAddress = null;

            // Get New Node MAC Address value
            try {
                newNodeMacAddress = valueById(edgeFormDatas, 26);
                loggingModel.setNewNodeMACaddress(newNodeMacAddress);
            } catch (NoValueException e) {
                loggingModel.setErrorDetails(MessageConstants.NEW_MAC_ADDRESS_NOT_AVAILABLE);
                loggingModel.setStatus(MessageConstants.ERROR);
            }

            // Get Existing Node MAC Address value
            try {
                existingNodeMacAddress = valueById(edgeFormDatas, 36);
                loggingModel.setExistingNodeMACaddress(existingNodeMacAddress);
            } catch (NoValueException e) {
                /*loggingModel.setErrorDetails(MessageConstants.OLD_MAC_ADDRESS_NOT_AVAILABLE);
                loggingModel.setProcessOtherForm(true);
                loggingModel.setStatus(MessageConstants.ERROR);
                return;*/
            }

            // Get Fixer QR Scan value
            String fixerQrScanValue = null;
            try {
                fixerQrScanValue = valueById(edgeFormDatas, 38);
            } catch (NoValueException e) {
               /* loggingModel.setProcessOtherForm(true);
                loggingModel.setErrorDetails(MessageConstants.FIXTURE_CODE_NOT_AVAILABLE);
                loggingModel.setStatus(MessageConstants.ERROR);
                // loggingModel.setProcessOtherForm(true);*/
                // return;
            }


            if (newNodeMacAddress != null && !newNodeMacAddress.startsWith("00") && (fixerQrScanValue != null && fixerQrScanValue.startsWith("00"))) {
                String temp = newNodeMacAddress;
                newNodeMacAddress = fixerQrScanValue;
                fixerQrScanValue = temp;
            }

            //Suppose fixtureqrscan value has mac address but MAC Address is empty
            if(newNodeMacAddress == null &&  (fixerQrScanValue != null && fixerQrScanValue.startsWith("00"))){
                newNodeMacAddress = fixerQrScanValue;
                fixerQrScanValue = null;
                loggingModel.setFixtureQRSame(true);
            }

            String idOnController = loggingModel.getIdOnController();


            String controllerStrIdValue = loggingModel.getControllerSrtId();


            String comment = "";
            // Check existingNodeMacAddress is valid or not
            if (existingNodeMacAddress != null && !existingNodeMacAddress.trim().isEmpty()) {
                try {
                    comment = validateMacAddress(existingNodeMacAddress, idOnController, controllerStrIdValue);
                } catch (QRCodeNotMatchedException e1) {
                    loggingModel.setStatus(MessageConstants.ERROR);
                    loggingModel.setErrorDetails(MessageConstants.REPLACE_MAC_NOT_MATCH);
                    //    return;
                }
            }

            if (newNodeMacAddress != null && !newNodeMacAddress.isEmpty()) {
                try {
                    checkMacAddressExists(newNodeMacAddress, idOnController, nightRideKey, nightRideValue, loggingModel);
                } catch (QRCodeAlreadyUsedException e1) {
                    logger.error("MacAddress (" + e1.getMacAddress()
                            + ")  - Already in use. So this pole is not synced with SLV. Note Title :[" + edgeNote.getTitle()
                            + " ]");
                }
            }

            boolean isError = false;
            StringBuffer statusDescription = new StringBuffer();
            // Call Empty ReplaceOLC
            try {
                if (!loggingModel.isMacAddressUsed()) {
                    replaceOLC(controllerStrIdValue, idOnController, "");
                    statusDescription.append(MessageConstants.EMPTY_REPLACE_OLC_SUCCESS);
                }

            } catch (ReplaceOLCFailedException e) {
                isError = true;
                statusDescription.append(e.getMessage());
                e.printStackTrace();
            }

            comment = comment + " replaced on " + dateFormat(edgeNote.getCreatedDateTime());

            sync2Slv(newNodeMacAddress, fixerQrScanValue, edgeNote, loggingModel, idOnController, controllerStrIdValue, comment, utilLocId, false, nightRideKey, nightRideValue);
            if (isError) {
                loggingModel.setErrorDetails(loggingModel.getErrorDetails() + statusDescription.toString());
            }
        } catch (Exception e) {
            loggingModel.setErrorDetails(e.getMessage());
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        }

    }


    private void replaceNodeOnly(List<EdgeFormData> edgeFormDatas, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, LoggingModel existingFixtureInfo, String utilLocId, String nightRideKey, String nightRideValue) {
        try {
            String existingNodeMacAddress = null;
            String newNodeMacAddress = null;
            // Get New Node MAC Address value
            try {
                newNodeMacAddress = valueById(edgeFormDatas, 30);
                loggingModel.setNewNodeMACaddress(newNodeMacAddress);
            } catch (NoValueException e) {
                loggingModel.setErrorDetails(MessageConstants.NEW_MAC_ADDRESS_NOT_AVAILABLE);
                loggingModel.setStatus(MessageConstants.ERROR);
                return;
            }

            // Get Existing Node MAC Address value
            try {
                existingNodeMacAddress = valueById(edgeFormDatas, 29);
                loggingModel.setExistingNodeMACaddress(existingNodeMacAddress);
            } catch (NoValueException e) {
                /*loggingModel.setErrorDetails(MessageConstants.OLD_MAC_ADDRESS_NOT_AVAILABLE);
                loggingModel.setStatus(MessageConstants.ERROR);
                return;*/
            }

            String idOnController = loggingModel.getIdOnController();

            String controllerStrIdValue = loggingModel.getControllerSrtId();


            String comment = null;


            if (existingNodeMacAddress != null && !existingNodeMacAddress.trim().isEmpty()) {
                try {
                    comment = validateMacAddress(existingNodeMacAddress, idOnController, controllerStrIdValue);
                } catch (Exception e1) {

                }
            }


            try {
                checkMacAddressExists(newNodeMacAddress, idOnController, nightRideKey, nightRideValue, loggingModel);
            } catch (QRCodeAlreadyUsedException e1) {
                logger.error("MacAddress (" + e1.getMacAddress()
                        + ")  - Already in use. So this pole is not synced with SLV. Note Title :[" + edgeNote.getTitle()
                        + " ]");
                loggingModel.setStatus(MessageConstants.ERROR);
                loggingModel.setErrorDetails("MacAddress (" + e1.getMacAddress()
                        + ")  - Already in use. So this pole is not synced with SLV. Note Title :[" + edgeNote.getTitle()
                        + " ]");
                return;
            }


            // Call Empty ReplaceOLC
            try {
                if (!loggingModel.isMacAddressUsed()) {
                    replaceOLC(controllerStrIdValue, idOnController, "");
                }

            } catch (ReplaceOLCFailedException e) {
                e.printStackTrace();
            }

            comment = comment + " replaced on " + dateFormat(edgeNote.getCreatedDateTime());

            sync2Slv(newNodeMacAddress, null, edgeNote, loggingModel, idOnController, controllerStrIdValue, comment, utilLocId, false, nightRideKey, nightRideValue);


        } catch (Exception e) {
            loggingModel.setErrorDetails(e.getMessage());
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        }
    }

    private void processRemoveAction(List<EdgeFormData> edgeFormDatas,LoggingModel loggingModel,String utilLocId){
        String removeReason = null;
        try {
            removeReason = valueById(edgeFormDatas, 35);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(removeReason != null){
            switch (removeReason){
                case "Installed on Wrong Fixture":
                    try {
                            String macaddress = getMacAddress(loggingModel.getIdOnController(),loggingModel.getControllerSrtId());
                            replaceOLC(loggingModel.getControllerSrtId(), loggingModel.getIdOnController(), "");
                            clearDeviceValues(loggingModel.getIdOnController(),loggingModel.getControllerSrtId(),"Installed on Wrong Fixture");
                            DuplicateMacAddress duplicateMacAddress = connectionDAO.getDuplicateMacAddress(macaddress);
                            if(duplicateMacAddress != null){
                                reSync(duplicateMacAddress.getNoteguid(),getEdgeToken(),true,utilLocId,true);
                            }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case "Pole Removed":
                    try {
                        clearDeviceValues(loggingModel.getIdOnController(),loggingModel.getControllerSrtId(),"Pole Removed");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case "Pole Knocked-Down":
                    try {
                        clearDeviceValues(loggingModel.getIdOnController(),loggingModel.getControllerSrtId(),"Pole Knocked-Down");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    private void clearDeviceValues(String idOnController,String controllerStrIdValue,String type){
        List<Object> paramsList = new ArrayList<>();
        paramsList.add("idOnController=" + idOnController);
        paramsList.add("controllerStrId=" + controllerStrIdValue);
        switch (type){
            case "Installed on Wrong Fixture":
                clearFixtureValues(paramsList);
                addStreetLightData("cslp.lum.install.date", "", paramsList);
                addStreetLightData("luminaire.installdate","", paramsList);
                addStreetLightData("cslp.node.install.date", "", paramsList);
                addStreetLightData("install.date", "", paramsList);
                addStreetLightData("installStatus", "To be installed", paramsList);
                break;
            case "Pole Removed":
                clearFixtureValues(paramsList);
                addStreetLightData("install.date", "", paramsList);
                addStreetLightData("luminaire.installdate","", paramsList);
                addStreetLightData("installStatus", "Removed", paramsList);
                addStreetLightData("luminaire.type", "HPS", paramsList);
                break;
            case "Pole Knocked-Down":
                clearFixtureValues(paramsList);
                addStreetLightData("install.date", "", paramsList);
                addStreetLightData("luminaire.installdate","", paramsList);
                addStreetLightData("installStatus", "Pole Knocked Down", paramsList);
                addStreetLightData("luminaire.type", "HPS", paramsList);
                break;
        }
        int errorCode = setDeviceValues(paramsList);
        if(errorCode == 0){
            logger.info("clearing device values completed: "+idOnController);
        }else{
            logger.error("Error in clearDeviceValues");
        }
    }

    private void clearFixtureValues(List<Object> paramsList){
        addStreetLightData("luminaire.brand", "", paramsList);
        addStreetLightData("device.luminaire.partnumber", "", paramsList);
        addStreetLightData("luminaire.model", "", paramsList);
        addStreetLightData("device.luminaire.manufacturedate", "", paramsList);
        addStreetLightData("power", "", paramsList);
        addStreetLightData("device.luminaire.colortemp", "", paramsList);
        addStreetLightData("device.luminaire.lumenoutput", "", paramsList);
        addStreetLightData("luminaire.DistributionType", "", paramsList);
        addStreetLightData("luminaire.colorcode", "", paramsList);
        addStreetLightData("device.luminaire.drivermanufacturer", "", paramsList);
        addStreetLightData("device.luminaire.driverpartnumber", "", paramsList);
        addStreetLightData("ballast.dimmingtype", "", paramsList);
    }

    public void reSync(String noteGuid, String accessToken, boolean isResync, String utilLocId,boolean isFromRemoveAction) {
        logger.info("resync method called ");
        // Get Edge Server Url from properties
        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");

        url = url + PropertiesReader.getProperties().getProperty("streetlight.edge.url.notes.get");

        url = url + "/" + noteGuid;
        logger.info("Given url is :" + url);
        // Get NoteList from edgeserver
        ResponseEntity<String> responseEntity = restService.getRequest(url, false, accessToken);

        // Process only response code as success
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            // Get Response String
            String notesData = responseEntity.getBody();
            logger.info("rest service data:" + notesData);
            EdgeNote edgeNote = gson.fromJson(notesData, EdgeNote.class);
            //   if(!edgeNote.getCreatedBy().contains("admin")){
            InstallMaintenanceLogModel installMaintenanceLogModel = new InstallMaintenanceLogModel();
            installMaintenanceLogModel.setLastSyncTime(edgeNote.getSyncTime());
            installMaintenanceLogModel.setProcessedNoteId(edgeNote.getNoteGuid());
            installMaintenanceLogModel.setNoteName(edgeNote.getTitle());
            installMaintenanceLogModel.setCreatedDatetime(String.valueOf(edgeNote.getCreatedDateTime()));
            loadDefaultVal(edgeNote, installMaintenanceLogModel);
            loadDeviceValues(installMaintenanceLogModel.getIdOnController());
            logger.info("going to call processnew action");
            processNewAction(edgeNote, installMaintenanceLogModel, isResync, utilLocId);

            //updateSlvStatusToEdge(installMaintenanceLogModel, edgeNote);
            LoggingModel loggingModel = installMaintenanceLogModel;
            streetlightDao.insertProcessedNotes(loggingModel, installMaintenanceLogModel);
            if(isFromRemoveAction) {
                connectionDAO.deleteDuplicateMacAddress(noteGuid);
            }
            //  }
        }
    }
}
