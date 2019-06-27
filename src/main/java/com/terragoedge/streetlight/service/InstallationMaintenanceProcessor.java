package com.terragoedge.streetlight.service;

import com.terragoedge.edgeserver.*;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.enumeration.InstallStatus;
import com.terragoedge.streetlight.enumeration.DateType;
import com.terragoedge.streetlight.exception.*;
import com.terragoedge.streetlight.json.model.*;
import com.terragoedge.streetlight.logging.InstallMaintenanceLogModel;
import com.terragoedge.streetlight.logging.LoggingModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;

public class InstallationMaintenanceProcessor extends AbstractProcessor {

    public InstallationMaintenanceProcessor(WeakHashMap<String, String> contextListHashMap, HashMap<String, SLVDates> cslpDateHashMap, HashMap<String, String> macHashMap) {
        super();
        this.contextListHashMap = contextListHashMap;
        this.cslpDateHashMap = cslpDateHashMap;
        this.macHashMap = macHashMap;
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
        logger.info("Night Ride Form TemplateGuid:"+nightRideTemplateGuid);
        int pos = formDataList.indexOf(nightRideTemplateGuid);
        if (pos != -1) {
            logger.info("Night Ride Form is Present");
            FormData formData = formDataList.get(pos);
            List<EdgeFormData> edgeFormDatas = formData.getFormDef();
            try {
                String nightRideValue = valueById(edgeFormDatas, 1);
                logger.info("Night Ride Form Val:"+nightRideValue);
                if(loggingModel.getSlvLuminaireSerialNumber() != null && nightRideValue.contains(loggingModel.getSlvLuminaireSerialNumber())){
                    logger.info("SlvLuminaireSerialNumber Val:"+loggingModel.getSlvLuminaireSerialNumber());
                    logger.info("SLV LuminaireSerialNumber and Night Ride Values are same.");
                    return null;
                }
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

    private void syncNightRideFormAlone(List<FormData> formDataList, InstallMaintenanceLogModel installMaintenanceLogModel, EdgeNote edgeNote, SlvInterfaceLogEntity slvInterfaceLogEntity) {
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
                        SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(installMaintenanceLogModel);
                        int errorCode = setDeviceValues(paramsList, slvTransactionLogs);
                        if (errorCode != 0) {
                            installMaintenanceLogModel.setErrorDetails(MessageConstants.ERROR_NIGHTRIDE_FORM_VAL);
                            installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
                            slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
                            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                            slvInterfaceLogEntity.setErrordetails(MessageConstants.ERROR_NIGHTRIDE_FORM_VAL);
                            return;
                        } else {
                            logger.info("Night ride value successfully updated");
                            installMaintenanceLogModel.setStatus(MessageConstants.SUCCESS);
                            slvInterfaceLogEntity.setStatus(MessageConstants.SUCCESS);
                            logger.info("Status Changed. to Success");
                        }
                    } else {
                        slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
                        slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                        slvInterfaceLogEntity.setErrordetails(MessageConstants.NOVALUE_NIGHTRIDE_FORM);
                        installMaintenanceLogModel.setErrorDetails(MessageConstants.NOVALUE_NIGHTRIDE_FORM);
                        installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
                    }

                }
            } catch (Exception e) {
                logger.error("Error in while getting nightRideValue's value : ", e);
                installMaintenanceLogModel.setErrorDetails("Error while syncing Night Ride value.");
                installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
                slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
                slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                slvInterfaceLogEntity.setErrordetails("Error in while getting nightRideValue's value : " + e);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error in while getting nightRideValue's value : ", e);
            installMaintenanceLogModel.setErrorDetails("Error while syncing Night Ride value.");
            installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
            slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
            slvInterfaceLogEntity.setErrordetails("Error in while getting nightRideValue's value : " + e);
        }
    }

    public void processNewAction(EdgeNote edgeNote, InstallMaintenanceLogModel installMaintenanceLogModel, boolean isReSync, String utilLocId, SlvInterfaceLogEntity slvInterfaceLogEntity) {
        slvInterfaceLogEntity.setParentnoteid((edgeNote.getBaseParentNoteId() == null) ? edgeNote.getNoteGuid() : edgeNote.getBaseParentNoteId());
        logger.info("processNewAction");
        slvInterfaceLogEntity.setIdOnController(edgeNote.getTitle());
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
                List<EdgeFormData> edgeFormDatas = formData.getFormDef();
                try {
                    logger.info("before Action Val");
                    String value = null;
                    try {
                        value = getAction(edgeFormDatas, edgeNote.getTitle(), installMaintenanceLogModel, edgeNote, slvInterfaceLogEntity);
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
                                installMaintenanceLogModel.setActionNew(true);
                                slvInterfaceLogEntity.setSelectedAction("New");
                                processNewGroup(edgeFormDatas, edgeNote, installMaintenanceLogModel, isReSync, utilLocId, nightRideKey, formatedValueNR, slvInterfaceLogEntity);
                                installMaintenanceLogModel.setInstalledDate(edgeNote.getCreatedDateTime());
                                break;
                            case "Repairs & Outages":
                                installMaintenanceLogModel.setActionNew(false);
                                slvInterfaceLogEntity.setSelectedAction("Repairs & Outages");
                                repairAndOutage(edgeFormDatas, edgeNote, installMaintenanceLogModel, utilLocId, nightRideKey, formatedValueNR, formatedValueNR, slvInterfaceLogEntity);
                                installMaintenanceLogModel.setReplacedDate(edgeNote.getCreatedDateTime());
                                break;
                            case "Remove":
                                slvInterfaceLogEntity.setSelectedAction("Remove");
                                logger.info("entered remove action");
                                processRemoveAction(edgeFormDatas, utilLocId, installMaintenanceLogModel, slvInterfaceLogEntity);
                                break;
                            case "Other Task":
                                slvInterfaceLogEntity.setSelectedAction("Other Task");
                                // processOtherTask(edgeFormDatas, edgeNote, installMaintenanceLogModel, nightRideKey, formatedValueNR);
                                break;
                        }
                    }

                } catch (Exception e) {
                    logger.error("error in processNewAction method", e);
                    slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
                    slvInterfaceLogEntity.setErrordetails(MessageConstants.ACTION_NO_VAL);
                    slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                }
                syncEdgeDates(installMaintenanceLogModel);
            }
        }

        if (!isInstallForm) {
            syncNightRideFormAlone(formDatas, installMaintenanceLogModel, edgeNote, slvInterfaceLogEntity);
        }
    }


    private String getAction(List<EdgeFormData> edgeFormDatas, String idOnController, InstallMaintenanceLogModel loggingModel, EdgeNote edgeNote, SlvInterfaceLogEntity slvInterfaceLogEntity) throws AlreadyUsedException {
        try {

            loadDateValFromEdge(edgeFormDatas,loggingModel);
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

            } else if (value.equals("Remove")) {
                return value;
            }
        } catch (NoValueException e) {
            e.printStackTrace();
        }


        // Replace Fixture Only
        String newFixtureQrScanValue = null;
        try {
            newFixtureQrScanValue = valueById(edgeFormDatas, 39);
            checkFixtureQrScan(newFixtureQrScanValue, edgeNote, loggingModel, slvInterfaceLogEntity);
        } catch (NoValueException e) {

        } catch (InValidBarCodeException e) {
            // Replace Fixture Only, If QR Code is invalid, then no need to process.
            loggingModel.setFixtureQRSame(true);
            // No need to worry, crew may be node replaced.
        }
        if (loggingModel.isFixtureQRSame()) {
            slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
            slvInterfaceLogEntity.setErrordetails("Given fixtureQrScan Already exist in slv server");
        }
        //Check Replace Node only option
        try {
            String newNodeMacAddress = valueById(edgeFormDatas, 30);
            checkMacAddressExists(newNodeMacAddress, idOnController, null, null, loggingModel, slvInterfaceLogEntity);
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
            validateValue(edgeFormDatas, idOnController, loggingModel, edgeNote, 26, 38, slvInterfaceLogEntity);
            loggingModel.setRepairsOption("Replace Node and Fixture");
            return "Repairs & Outages";
        } catch (AlreadyUsedException e) {
            throw new AlreadyUsedException(e.getMessage());
        } catch (NoValueException e) {

        }


        // Get Install status
        String installStatusValue = null;
        try {
            installStatusValue = valueById(edgeFormDatas, 22);
            logger.info("installStatus Val" + installStatusValue);
        } catch (NoValueException e) {
            logger.error("Error in while getting installStatusValue", e);
        }


        if (installStatusValue != null && installStatusValue.equals("Button Photocell Installation")) {
            DeviceAttributes deviceAttributes = getDeviceValues(loggingModel);
            loggingModel.setButtonPhotoCell(true);
            if (deviceAttributes.getInstallStatus().equals(InstallStatus.Verified.getValue()) || deviceAttributes.getInstallStatus().equals(InstallStatus.Photocell_Only.getValue())) {
                loggingModel.setFixtureQRSame(true);
            }
        } else  if(installStatusValue != null && installStatusValue.equals("Could not complete")){
            DeviceAttributes deviceAttributes = getDeviceValues(loggingModel);
            String installStatus = properties.getProperty("could_note_complete_install_status");
            if (deviceAttributes.getInstallStatus().equals(installStatus)) {
                logger.info("Current edge and SLV Install Status is same (Could not be installed)");
                loggingModel.setCouldNotComplete(true);
            }
            return "New";
        }

        try {
            validateValue(edgeFormDatas, idOnController, loggingModel, edgeNote, 19, 20, slvInterfaceLogEntity);
            return "New";
        } catch (AlreadyUsedException e) {
            throw new AlreadyUsedException(e.getMessage());
        } catch (NoValueException e) {
            return "New";
        }
    }


    private void validateValue(List<EdgeFormData> edgeFormDatas, String idOnController, LoggingModel loggingModel, EdgeNote edgeNote, int macAddressId, int qrScanId, SlvInterfaceLogEntity slvInterfaceLogEntity) throws AlreadyUsedException, NoValueException {
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
        if (newNodeMacAddress == null && (fixerQrScanValue != null && fixerQrScanValue.startsWith("00"))) {
            newNodeMacAddress = fixerQrScanValue;
            fixerQrScanValue = null;
            loggingModel.setFixtureQRSame(true);
        }

        if (newNodeMacAddress != null) {
            try {
                checkMacAddressExists(newNodeMacAddress, idOnController, null, null, loggingModel, slvInterfaceLogEntity);
            } catch (QRCodeAlreadyUsedException e) {
                // No need to worry, Fixture QR Scan may be differ. So let's continue.
            } catch (Exception e) {
                // No need to worry, Fixture QR Scan may be differ. So let's continue.
            }

        }

        if (fixerQrScanValue != null && !loggingModel.isButtonPhotoCell()) {
            try {
                checkFixtureQrScan(fixerQrScanValue, edgeNote, loggingModel, slvInterfaceLogEntity);
            } catch (InValidBarCodeException e) {
                loggingModel.setFixtureQRSame(true);
                // No need to process,bcs its invalid.
                if (loggingModel.isMacAddressUsed()) {
                    throw new AlreadyUsedException(e.getMessage());
                }
            }
        }

        if (loggingModel.isMacAddressUsed() && loggingModel.isFixtureQRSame()) {
            slvInterfaceLogEntity.setErrordetails("MAC Address and QR Scan Already Used.");
            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
            throw new AlreadyUsedException("MAC Address and QR Scan Already Used.");
        }


    }


    private void checkFixtureQrScan(String fixtureQrScan, EdgeNote edgeNote, LoggingModel loggingModel, SlvInterfaceLogEntity slvInterfaceLogEntity) throws InValidBarCodeException {
        List<Object> paramsList = new ArrayList<>();
        SlvServerData slvServerData = new SlvServerData();
        try {
            // check given macaddress exist or not in edge_all_fix table
            boolean isExistFixture = connectionDAO.isExistFixture(edgeNote.getTitle(), fixtureQrScan);
            logger.info("given fixture idoncontroller :" + edgeNote.getTitle());
            logger.info("Given fixture fixturequrscan:" + fixtureQrScan);
            if (isExistFixture) {
                slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
                slvInterfaceLogEntity.setErrordetails("Fixture QR is present edge_all_fix table.");
                logger.info("Fixture QR is present edge_all_fix table.");
                loggingModel.setFixtureQRSame(true);
                return;
            }
            logger.info("Fixture QR Scan Validation Starts.");
            buildFixtureStreetLightData(fixtureQrScan, paramsList, edgeNote, slvServerData, loggingModel);
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
        } catch (Exception e) {
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
        SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(loggingModel);
        int errorCode = setDeviceValues(paramsList, slvTransactionLogs);
        logger.info("Error code" + errorCode);
        return errorCode;
    }

    private void sync2Slv(String macAddress, String fixerQrScanValue, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, String idOnController, String controllerStrIdValue, String utilLocId, boolean isNew, String nightRideKey, String nightRideValue, SlvInterfaceLogEntity slvInterfaceLogEntity) {
        try {

            List<Object> paramsList = new ArrayList<>();

            paramsList.add("idOnController=" + idOnController);
            paramsList.add("controllerStrId=" + controllerStrIdValue);
            SlvServerData slvServerData = new SlvServerData();

            logger.info("Atlasphysicalpage in SLV:"+loggingModel.getAtlasPhysicalPage());
            logger.info("IsDroppedPinWorkflow:"+loggingModel.isDroppedPinWorkflow());
            logger.info("Edge NoteBook:"+edgeNote.getEdgeNotebook());
            if(loggingModel.isDroppedPinWorkflow() && loggingModel.getAtlasPhysicalPage() == null && edgeNote.getEdgeNotebook() != null && edgeNote.getEdgeNotebook().getNotebookName() != null){
                logger.info("atlasphysicalpage is empty.");
                addStreetLightData("location.atlasphysicalpage", edgeNote.getEdgeNotebook().getNotebookName(), paramsList);
            }
            addOtherParams(edgeNote, paramsList, idOnController, utilLocId, isNew, fixerQrScanValue, macAddress, loggingModel);




            if (fixerQrScanValue != null && !loggingModel.isFixtureQRSame()) {
                buildFixtureStreetLightData(fixerQrScanValue, paramsList, edgeNote, slvServerData, loggingModel);//update fixer qrscan value


            }

            logger.info("Night Ride Val in Set Device"+nightRideValue);
            if (nightRideValue != null) {
                addStreetLightData(nightRideKey, nightRideValue, paramsList);
            }
            boolean isButtonPhotoCell = loggingModel.isButtonPhotoCell();
            if (isButtonPhotoCell) {
                addStreetLightData("install.date", dateFormat(edgeNote.getCreatedDateTime()), paramsList);
            }
            if (macAddress != null && !macAddress.trim().isEmpty() && !loggingModel.isMacAddressUsed()) {
                boolean isNodeDatePresent = isNodeDatePresent(idOnController);
                if (!isNodeDatePresent && !isButtonPhotoCell) {
                    addStreetLightData("cslp.node.install.date", dateFormat(edgeNote.getCreatedDateTime()), paramsList);
                }
                if (!isButtonPhotoCell) {
                    addStreetLightData("install.date", dateFormat(edgeNote.getCreatedDateTime()), paramsList);
                }
                addStreetLightData("MacAddress", macAddress, paramsList);
            }
            setEdgeDates(loggingModel,paramsList);
            SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(loggingModel);
            int errorCode = setDeviceValues(paramsList, slvTransactionLogs);
            logger.info("Error code" + errorCode);
            if (errorCode != 0) {
                loggingModel.setErrorDetails(MessageConstants.ERROR_UPDATE_DEVICE_VAL);
                loggingModel.setStatus(MessageConstants.ERROR);
                slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
                slvInterfaceLogEntity.setErrordetails(MessageConstants.ERROR_UPDATE_DEVICE_VAL);
                return;
            } else {
                slvInterfaceLogEntity.setSetDevice(MessageConstants.SETDEVICE);
                slvInterfaceLogEntity.setStatus(MessageConstants.SUCCESS);
                if (fixerQrScanValue != null && !fixerQrScanValue.trim().isEmpty()) {
                    createEdgeAllFixture(idOnController, fixerQrScanValue);
                }
                createAllSLVDate(loggingModel.getDatesHolder().getSyncEdgeDates(),idOnController);
                logger.info("Replace OLC called");
                // replace OlC
                System.out.println("New :" + isNew + " \nmacAddress :" + macAddress);
                logger.info("New :" + isNew + " \nmacAddress :" + macAddress);
                if (macAddress == null || macAddress.isEmpty()) {
                    loggingModel.setStatus(MessageConstants.SUCCESS);
                    return;
                } else {
                    if (!loggingModel.isMacAddressUsed()) {
                        slvTransactionLogs = getSLVTransactionLogs(loggingModel);
                        replaceOLC(controllerStrIdValue, idOnController, macAddress, slvTransactionLogs, slvInterfaceLogEntity);// insert mac address
                    }

                }
                logger.info("Replace OLC End");
                loggingModel.setStatus(MessageConstants.SUCCESS);
                slvInterfaceLogEntity.setStatus(MessageConstants.SUCCESS);
                logger.info("Status Changed. to Success");
            }

        } catch (InValidBarCodeException e) {
            logger.error("Error in processNewGroup", e);
            loggingModel.setStatus(MessageConstants.ERROR);
            slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
            loggingModel.setErrorDetails(e.getMessage());
            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
            slvInterfaceLogEntity.setErrordetails(e.getMessage());

        } catch (Exception e) {
            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
            slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
            slvInterfaceLogEntity.setErrordetails(e.getMessage());
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

    public void processOtherTask(List<EdgeFormData> edgeFormDatas, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, String nightRideKey, String nightRideValue, SlvInterfaceLogEntity slvInterfaceLogEntity) {
        logger.info("Other task value processed");
        String Key = properties.getProperty("amerescousa.night.ride.key_for_slv");
        logger.info("key :" + Key);
        List<Object> paramsList = new ArrayList<>();
        String cdotIssue = null;
        try {
            cdotIssue = valueById(edgeFormDatas, 106);
            cdotIssue =  getUnableRepairComment(cdotIssue,edgeFormDatas);
            logger.info("cdotIssue's issue Value :" + cdotIssue);
        } catch (NoValueException e) {
            logger.error("Error in while getting cdotIssue's issue", e);
            loggingModel.setErrorDetails("Value is not selected");
            loggingModel.setStatus(MessageConstants.ERROR);
            slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
            slvInterfaceLogEntity.setErrordetails("Error in while getting cdotIssue's issue:" + e);
            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
            return;
        }
        logger.info("cdotIssue :" + cdotIssue);
        String controllerStarId = getControllerStrId(edgeFormDatas);
        String idOnController = loggingModel.getIdOnController();
        paramsList.add("idOnController=" + idOnController);
        paramsList.add("controllerStrId=" + controllerStarId);

        if (nightRideValue != null) {
            nightRideValue = nightRideValue + "," + cdotIssue;
            addStreetLightData(nightRideKey, nightRideValue, paramsList);
        } else {

            if(loggingModel.getSlvLuminaireSerialNumber() != null && loggingModel.getSlvLuminaireSerialNumber().contains(cdotIssue)){
                logger.info("SLV Install Status is already Could not Complete and cdotIssue Value also already Synced");
                slvInterfaceLogEntity.setErrordetails("SLV Install Status is already Could not Complete and cdotIssue Value also already Synced");
                slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                return;
            }

            String formatedValue = dateFormat(edgeNote.getCreatedDateTime()) + " :" + cdotIssue;
            addStreetLightData(Key, formatedValue, paramsList);
        }
        SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(loggingModel);
        int errorCode = setDeviceValues(paramsList, slvTransactionLogs);
        if (errorCode != 0) {
            logger.info("CDOT issue value serDevice error");
            loggingModel.setErrorDetails(MessageConstants.ERROR_OTHERTASK_DEVICE_VAL);
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        } else {
            slvInterfaceLogEntity.setStatus(MessageConstants.SUCCESS);
            logger.info("OtherTask's value updated successfully in SLV");
            loggingModel.setStatus(MessageConstants.SUCCESS);
            logger.info("Status Changed. to Success");
        }
    }


    private String getSkippedReason(String skippedFixtureReasonVal,List<EdgeFormData> edgeFormDatas){
        if(skippedFixtureReasonVal.equals("Other - Add note")){
            try{
                skippedFixtureReasonVal = valueById(edgeFormDatas, 42);
            }catch (Exception e){
                logger.error("Error in getSkippedReason",e);
            }
        }
        return skippedFixtureReasonVal;
    }


    private String getUnableRepairComment(String unableRepair,List<EdgeFormData> edgeFormDatas){
        if(unableRepair.equals("Other (Comment)")){
            try{
                unableRepair = valueById(edgeFormDatas, 121);
            }catch (Exception e){
                logger.error("Error in getUnableRepairComment",e);
            }
        }
        return unableRepair;
    }

    private void processNewGroup(List<EdgeFormData> edgeFormDatas, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, boolean isResync, String utilLocId, String nightRideKey, String nightRideValue, SlvInterfaceLogEntity slvInterfaceLogEntity) {
        try {

            // Get Install status
            String installStatusValue = null;
            try {
                installStatusValue = valueById(edgeFormDatas, 22);
                slvInterfaceLogEntity.setInstallStatus(installStatusValue);
                logger.info("installStatus Val" + installStatusValue);
            } catch (NoValueException e) {
                logger.error("Error in while getting installStatusValue", e);
            }
            if (installStatusValue != null && installStatusValue.equals("Could not complete")) {
                try {
                    String skippedFixtureReasonVal = valueById(edgeFormDatas, 23);
                    skippedFixtureReasonVal = getSkippedReason(skippedFixtureReasonVal,edgeFormDatas);
                    logger.info("Skipped Fixture Reason Val" + skippedFixtureReasonVal);

                    if (nightRideValue != null && !nightRideValue.trim().isEmpty()) {
                        nightRideValue = nightRideValue + "," + skippedFixtureReasonVal;
                    } else {
                        logger.info("SLV Install Status is Could Not Complete"+loggingModel.isCouldNotComplete());
                        logger.info("SLV Luminaire SerialNumber"+loggingModel.getSlvLuminaireSerialNumber());
                        if(loggingModel.isCouldNotComplete() && loggingModel.getSlvLuminaireSerialNumber() != null && loggingModel.getSlvLuminaireSerialNumber().contains(skippedFixtureReasonVal)){
                            logger.info("SLV Install Status is already Could not Complete and Skipped Fixture Reason Value also already Synced");
                            slvInterfaceLogEntity.setErrordetails("SLV Install Status is already Could not Complete and Skipped Fixture Reason Value also already Synced");
                            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                            return;
                        }
                        nightRideValue = dateFormat(edgeNote.getCreatedDateTime()) + " :" + skippedFixtureReasonVal;
                    }
                } catch (NoValueException e) {
                    logger.error("Error in while getting installStatusValue", e);

                    if(loggingModel.isCouldNotComplete() && (nightRideValue == null || nightRideValue.trim().isEmpty())){
                        logger.info("SLV Install Status is already Could not Complete and Skipped Fixture Reason Value also already Synced");
                        slvInterfaceLogEntity.setErrordetails("SLV Install Status is already Could not Complete and nightRideValue Value is Empty");
                        slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                        return;
                    }
                }

                int errorCode = sync2SlvInstallStatus(loggingModel.getIdOnController(), loggingModel.getControllerSrtId(), loggingModel, nightRideKey, nightRideValue);
                if (errorCode != 0) {
                    loggingModel.setErrorDetails("Error while updating Could not complete install status.Corresponding Error code :" + errorCode);
                    loggingModel.setStatus(MessageConstants.ERROR);
                    slvInterfaceLogEntity.setErrordetails("Error while updating Could not complete install status.Corresponding Error code :" + errorCode);
                    slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                } else {
                    loggingModel.setErrorDetails(MessageConstants.COULD_NOT_COMPLETE_SUCCESS_MSG);
                    loggingModel.setStatus(MessageConstants.SUCCESS);
                    slvInterfaceLogEntity.setErrordetails(MessageConstants.COULD_NOT_COMPLETE_SUCCESS_MSG);
                    slvInterfaceLogEntity.setStatus(MessageConstants.SUCCESS);
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
                slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
                slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                slvInterfaceLogEntity.setErrordetails(MessageConstants.NO_MACADDRESS_NO_FIXTURE);
                return;
            }
            if (nodeMacValue != null && !nodeMacValue.startsWith("00") && (fixerQrScanValue != null && fixerQrScanValue.startsWith("00"))) {
                String temp = nodeMacValue;
                nodeMacValue = fixerQrScanValue;
                fixerQrScanValue = temp;
            }

            //Suppose fixtureqrscan value has mac address but MAC Address is empty
            if (nodeMacValue == null && (fixerQrScanValue != null && fixerQrScanValue.startsWith("00"))) {
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
                if (nodeMacValue != null && !nodeMacValue.trim().isEmpty() && !loggingModel.isMacAddressUsed()) {
                    try {
                        SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(loggingModel);
                        replaceOLC(loggingModel.getControllerSrtId(), loggingModel.getIdOnController(), "", slvTransactionLogs, slvInterfaceLogEntity);
                    } catch (Exception e) {
                        String message = e.getMessage();
                    }
                }


            }

            // Check Whether MAC Address is already assigned to other fixtures or not.
            try {
                if (nodeMacValue != null) {
                    checkMacAddressExists(nodeMacValue, loggingModel.getIdOnController(), nightRideKey, nightRideValue, loggingModel, slvInterfaceLogEntity);
                }
            } catch (QRCodeAlreadyUsedException e1) {
                logger.error("MacAddress (" + e1.getMacAddress()
                        + ")  - Already in use. So this pole is not synced with SLV. Note Title :[" + edgeNote.getTitle()
                        + " ]");
            }
            loggingModel.setMacAddress(nodeMacValue);
            slvInterfaceLogEntity.setMacAddress(nodeMacValue);
            slvInterfaceLogEntity.setFixtureqrscan(fixerQrScanValue);
            sync2Slv(nodeMacValue, fixerQrScanValue, edgeNote, loggingModel, loggingModel.getIdOnController(), loggingModel.getControllerSrtId(), utilLocId, true, nightRideKey, nightRideValue, slvInterfaceLogEntity);
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


    public void repairAndOutage(List<EdgeFormData> edgeFormDatas, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, String utilLocId, String nightRideKey, String nightRideValue, String formatedValueNR, SlvInterfaceLogEntity slvInterfaceLogEntity) {

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
                slvInterfaceLogEntity.setSelectedReplace("Replace Node and Fixture");
                replaceNodeFixture(edgeFormDatas, edgeNote, loggingModel, utilLocId, nightRideKey, nightRideValue, slvInterfaceLogEntity);
                break;
            case "Replace Node only":
                slvInterfaceLogEntity.setSelectedReplace("Replace Node only");
                replaceNodeOnly(edgeFormDatas, edgeNote, loggingModel, utilLocId, nightRideKey, nightRideValue, slvInterfaceLogEntity);
                break;
            case "Replace Fixture only":
                slvInterfaceLogEntity.setSelectedReplace("Replace Fixture only");
                String fixerQrScanValue = null;
                try {
                    fixerQrScanValue = valueById(edgeFormDatas, 39);
                    String idOnController = loggingModel.getIdOnController();
                    String controllerStrIdValue = loggingModel.getControllerSrtId();
                    sync2Slv(null, fixerQrScanValue, edgeNote, loggingModel, idOnController, controllerStrIdValue, utilLocId, false, nightRideKey, nightRideValue, slvInterfaceLogEntity);
                    break;
                } catch (NoValueException e) {
                    slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                    slvInterfaceLogEntity.setErrordetails("Fixture QR Scan value is empty");
                    slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
                }


            case "Power Issue":
                loggingModel.setStatus(MessageConstants.ERROR);
                loggingModel.setErrorDetails("SLV Interface Doest not support this(Power Issue option is selected).");
                slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
                slvInterfaceLogEntity.setSelectedReplace("Power Issue");
                break;
            case "Unable to Repair(CDOT Issue)":
                logger.info("Processed Unable to Repair option :" + edgeNote.getTitle());
                logger.info("Processed NoteGuid :" + edgeNote.getNoteGuid());
                slvInterfaceLogEntity.setSelectedReplace("Unable to Repair(CDOT Issue)");
                //  String nightRideKey = properties.getProperty("amerescousa.night.ride.key_for_slv");
                processOtherTask(edgeFormDatas, edgeNote, loggingModel, nightRideKey, formatedValueNR, slvInterfaceLogEntity);
                break;
        }
    }


    private void replaceNodeFixture(List<EdgeFormData> edgeFormDatas, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, String utilLocId, String nightRideKey, String nightRideValue, SlvInterfaceLogEntity slvInterfaceLogEntity) {
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
                slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
                slvInterfaceLogEntity.setErrordetails(MessageConstants.NEW_MAC_ADDRESS_NOT_AVAILABLE);
                slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
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
            if (newNodeMacAddress == null && (fixerQrScanValue != null && fixerQrScanValue.startsWith("00"))) {
                newNodeMacAddress = fixerQrScanValue;
                fixerQrScanValue = null;
                loggingModel.setFixtureQRSame(true);
            }

            String idOnController = loggingModel.getIdOnController();


            String controllerStrIdValue = loggingModel.getControllerSrtId();

            boolean isMatched = checkExistingMacAddressValid(edgeNote,loggingModel);
            if(!isMatched){
                return;
            }
            if (newNodeMacAddress != null && !newNodeMacAddress.isEmpty()) {
                try {
                    checkMacAddressExists(newNodeMacAddress, idOnController, nightRideKey, nightRideValue, loggingModel, slvInterfaceLogEntity);
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
                    SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(loggingModel);
                    replaceOLC(controllerStrIdValue, idOnController, "", slvTransactionLogs, slvInterfaceLogEntity);
                    statusDescription.append(MessageConstants.EMPTY_REPLACE_OLC_SUCCESS);
                }

            } catch (ReplaceOLCFailedException e) {
                isError = true;
                statusDescription.append(e.getMessage());
                e.printStackTrace();
            }


            sync2Slv(newNodeMacAddress, fixerQrScanValue, edgeNote, loggingModel, idOnController, controllerStrIdValue, utilLocId, false, nightRideKey, nightRideValue, slvInterfaceLogEntity);
            if (isError) {
                loggingModel.setErrorDetails(loggingModel.getErrorDetails() + statusDescription.toString());
            }
        } catch (Exception e) {
            loggingModel.setErrorDetails(e.getMessage());
            loggingModel.setStatus(MessageConstants.ERROR);
            slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
            slvInterfaceLogEntity.setErrordetails("Replace Olc Error :" + e.getMessage());
            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
            return;
        }

    }


    private void replaceNodeOnly(List<EdgeFormData> edgeFormDatas, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, String utilLocId, String nightRideKey, String nightRideValue, SlvInterfaceLogEntity slvInterfaceLogEntity) {
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
                slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
                slvInterfaceLogEntity.setErrordetails(MessageConstants.NEW_MAC_ADDRESS_NOT_AVAILABLE);
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
            
            boolean isMatched = checkExistingMacAddressValid(edgeNote,loggingModel);
            logger.info("Existing MAC Address match result:"+isMatched);
            if(!isMatched){
                logger.info("Existing MAC Address not match with SLV. So Current note is skipped.");
                return;
            }

            try {
                checkMacAddressExists(newNodeMacAddress, idOnController, nightRideKey, nightRideValue, loggingModel, slvInterfaceLogEntity);
            } catch (QRCodeAlreadyUsedException e1) {
                slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                slvInterfaceLogEntity.setErrordetails("MacAddress Already use. So this pole not synced with slv");
                slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
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
                    SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(loggingModel);
                    replaceOLC(controllerStrIdValue, idOnController, "", slvTransactionLogs, slvInterfaceLogEntity);
                }

            } catch (ReplaceOLCFailedException e) {
                e.printStackTrace();
            }


            sync2Slv(newNodeMacAddress, null, edgeNote, loggingModel, idOnController, controllerStrIdValue, utilLocId, false, nightRideKey, nightRideValue, slvInterfaceLogEntity);


        } catch (Exception e) {
            slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
            slvInterfaceLogEntity.setErrordetails("replaceNodeOnly failure" + e.getMessage());
            loggingModel.setErrorDetails(e.getMessage());
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        }
    }

    private void processRemoveAction(List<EdgeFormData> edgeFormDatas, String utilLocId, InstallMaintenanceLogModel installMaintenanceLogModel, SlvInterfaceLogEntity slvInterfaceLogEntity) {
        String removeReason = null;
        try {
            removeReason = valueById(edgeFormDatas, 35);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (removeReason != null) {
            logger.info("remove reason:" + removeReason);
            DeviceAttributes deviceAttributes = getDeviceValues(installMaintenanceLogModel);
            switch (removeReason) {
                case "Installed on Wrong Fixture":
                    try {
                        if (deviceAttributes != null && deviceAttributes.getInstallStatus().equals(InstallStatus.To_be_installed.getValue())) {
                            installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
                            installMaintenanceLogModel.setErrorDetails("Already Processed.Install Status: To be installed");
                            slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
                            slvInterfaceLogEntity.setErrordetails("Already Processed.Install Status: To be installed");
                            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                            return;
                        }
                        String macaddress = null;
                        if (deviceAttributes != null) {
                            logger.info("removed mac address:" + deviceAttributes.getMacAddress());
                            macaddress = deviceAttributes.getMacAddress();
                        }

                        try {
                            SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(installMaintenanceLogModel);
                            replaceOLC(installMaintenanceLogModel.getControllerSrtId(), installMaintenanceLogModel.getIdOnController(), "", slvTransactionLogs, slvInterfaceLogEntity);
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.error("error in replace OLC:" + e.getMessage());
                        }
                        logger.info("empty replaceOLC called");
                        try {
                            clearDeviceValues(installMaintenanceLogModel.getIdOnController(), installMaintenanceLogModel.getControllerSrtId(), "Installed on Wrong Fixture", installMaintenanceLogModel);
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.error("Error in clear device values:" + e.getMessage());
                        }
                        logger.info("cleared device value");
                        if (macaddress != null) {
                            DuplicateMacAddress duplicateMacAddress = connectionDAO.getDuplicateMacAddress(macaddress);
                            logger.info("duplicate mac address: " + duplicateMacAddress);
                            if (duplicateMacAddress != null) {
                                //reSync(duplicateMacAddress.getNoteguid(),getEdgeToken(),true,utilLocId,true);
                                logger.info("resync called due to duplicate mac address");
                            }

                            logger.info("Data going to remove from EdgeAllMac and EdgeAllFix Table.");
                            logger.info("IdOnController:"+installMaintenanceLogModel.getIdOnController());
                            logger.info("MACAddress:"+macaddress);
                            connectionDAO.removeEdgeAllMAC(installMaintenanceLogModel.getIdOnController(),macaddress);
                            connectionDAO.removeEdgeAllFixture(installMaintenanceLogModel.getIdOnController());
                        }

                    } catch (Exception e) {
                        logger.error("Error in processRemoveAction", e);
                    }
                    break;
                case "Pole Removed":
                    try {
                        if (deviceAttributes != null && deviceAttributes.getInstallStatus().equals(InstallStatus.Removed.getValue())) {
                            installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
                            installMaintenanceLogModel.setErrorDetails("Already Processed.Install Status: Pole Removed");
                            slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
                            slvInterfaceLogEntity.setErrordetails("Already Processed.Install Status: Removed");
                            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                            return;
                        }
                        clearDeviceValues(installMaintenanceLogModel.getIdOnController(), installMaintenanceLogModel.getControllerSrtId(), "Pole Removed", installMaintenanceLogModel);
                        slvInterfaceLogEntity.setStatus(MessageConstants.SUCCESS);
                    } catch (Exception e) {
                        logger.error("Error in processRemoveAction", e);
                    }
                    break;
                case "Pole Knocked-Down":
                    try {
                        if (deviceAttributes != null && deviceAttributes.getInstallStatus().equals(InstallStatus.Pole_Knocked_Down.getValue())) {
                            installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
                            installMaintenanceLogModel.setErrorDetails("Already Processed.Install Status: Pole Knocked Down");
                            slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
                            slvInterfaceLogEntity.setErrordetails("Already Processed.Install Status: Pole Knocked Down");
                            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                            return;
                        }
                        clearDeviceValues(installMaintenanceLogModel.getIdOnController(), installMaintenanceLogModel.getControllerSrtId(), "Pole Knocked-Down", installMaintenanceLogModel);
                        slvInterfaceLogEntity.setStatus(MessageConstants.SUCCESS);
                    } catch (Exception e) {
                        logger.error("Error in processRemoveAction", e);
                    }
                    break;
            }
        }
    }

    private void clearDeviceValues(String idOnController, String controllerStrIdValue, String type, InstallMaintenanceLogModel loggingModel) {
        List<Object> paramsList = new ArrayList<>();
        paramsList.add("idOnController=" + idOnController);
        paramsList.add("controllerStrId=" + controllerStrIdValue);
        switch (type) {
            case "Installed on Wrong Fixture":
                clearFixtureValues(paramsList);
                addStreetLightData("cslp.lum.install.date", "", paramsList);
                addStreetLightData("luminaire.installdate", "", paramsList);
                addStreetLightData("cslp.node.install.date", "", paramsList);
                addStreetLightData("install.date", "", paramsList);
                addStreetLightData("installStatus", InstallStatus.To_be_installed.getValue(), paramsList);
                break;
            case "Pole Removed":
                clearFixtureValues(paramsList);
                addStreetLightData("install.date", "", paramsList);
                addStreetLightData("luminaire.installdate", "", paramsList);
                addStreetLightData("installStatus", InstallStatus.Removed.getValue(), paramsList);
                addStreetLightData("luminaire.type", "HPS", paramsList);
                break;
            case "Pole Knocked-Down":
                clearFixtureValues(paramsList);
                addStreetLightData("install.date", "", paramsList);
                addStreetLightData("luminaire.installdate", "", paramsList);
                addStreetLightData("installStatus", InstallStatus.Pole_Knocked_Down.getValue(), paramsList);
                addStreetLightData("luminaire.type", "HPS", paramsList);
                break;
        }
        SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(loggingModel);
        int errorCode = setDeviceValues(paramsList, slvTransactionLogs);
        if (errorCode == 0) {
            logger.info("clearing device values completed: " + idOnController);
        } else {
            logger.error("Error in clearDeviceValues");
        }
    }

    private void clearFixtureValues(List<Object> paramsList) {
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

    public void reSync(String noteGuid, String accessToken, boolean isResync, String utilLocId, boolean isFromRemoveAction) throws Exception {
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
            SlvInterfaceLogEntity slvInterfaceLogEntity = new SlvInterfaceLogEntity();
            InstallMaintenanceLogModel installMaintenanceLogModel = new InstallMaintenanceLogModel();
            slvInterfaceLogEntity.setCreateddatetime(System.currentTimeMillis());
            slvInterfaceLogEntity.setResync(true);
            installMaintenanceLogModel.setLastSyncTime(edgeNote.getSyncTime());
            installMaintenanceLogModel.setProcessedNoteId(edgeNote.getNoteGuid());
            installMaintenanceLogModel.setNoteName(edgeNote.getTitle());
            installMaintenanceLogModel.setParentNoteId(edgeNote.getBaseParentNoteId());
            installMaintenanceLogModel.setCreatedDatetime(String.valueOf(edgeNote.getCreatedDateTime()));
            loadDefaultVal(edgeNote, installMaintenanceLogModel);
            loadDeviceValues(installMaintenanceLogModel.getIdOnController(), installMaintenanceLogModel);
            logger.info("going to call processnew action");
            boolean isDroppedPinWorkFlow = isDroppedPinNote(edgeNote,droppedPinTag);
            processNewAction(edgeNote, installMaintenanceLogModel, isResync, utilLocId, slvInterfaceLogEntity);

            //updateSlvStatusToEdge(installMaintenanceLogModel, edgeNote);
            LoggingModel loggingModel = installMaintenanceLogModel;
            streetlightDao.insertProcessedNotes(loggingModel, installMaintenanceLogModel);
            if (isFromRemoveAction) {
                connectionDAO.deleteDuplicateMacAddress(noteGuid);
            }
            //  }
        }
    }


    private void loadDateValFromEdge(List<EdgeFormData> edgeFormDatas,InstallMaintenanceLogModel installMaintenanceLogModel){
        SLVDates edgeSLVDates = new SLVDates();
        installMaintenanceLogModel.getDatesHolder().setEdgeDates(edgeSLVDates);
        // Get CSLP Node Install Date
        String cslpNodeInstallDate = null;
        try {
            cslpNodeInstallDate = valueById(edgeFormDatas, 169);
            edgeSLVDates.setCslpNodeDate(cslpNodeInstallDate);
            logger.info("cslpNodeInstallDate Val" + cslpNodeInstallDate);
        } catch (NoValueException e) {
            logger.error("CSLP Node Install Date is empty", e);
        }


        // Get CSLP Node Install Date
        String cslpLumInstallDate = null;
        try {
            cslpLumInstallDate = valueById(edgeFormDatas, 170);
            edgeSLVDates.setCslpLumDate(cslpLumInstallDate);
            logger.info("cslpLumInstallDate Val" + cslpLumInstallDate);
        } catch (NoValueException e) {
            logger.error("CSLP Lum Install Date is empty", e);
        }


        // Get Node Install Date
        String installDate = null;
        try {
            installDate = valueById(edgeFormDatas, 171);
            edgeSLVDates.setNodeInstallDate(installDate);
            logger.info("installDate Val" + installDate);
        } catch (NoValueException e) {
            logger.error("Install Date is empty", e);
        }


        // Get Lum Install Date
        String lumInstallDate = null;
        try {
            lumInstallDate = valueById(edgeFormDatas, 172);
            edgeSLVDates.setLumInstallDate(lumInstallDate);
            logger.info("lumInstallDate Val" + lumInstallDate);
        } catch (NoValueException e) {
            logger.error("Lum Install Date is empty", e);
        }

        getSlvSyncDates(installMaintenanceLogModel,edgeSLVDates);
    }


    private void getSlvSyncDates(InstallMaintenanceLogModel installMaintenanceLogModel,SLVDates edgeSLVDates){
        SLVDates syncEdgeDates = new SLVDates();
        installMaintenanceLogModel.getDatesHolder().setSyncEdgeDates(syncEdgeDates);
        SLVDates slvDates = installMaintenanceLogModel.getDatesHolder().getSlvDates();

        // CSLP Node Install Dates
        String edgeCslpNodeDate =  installMaintenanceLogModel.getDatesHolder().compareSLVEdgeDate(edgeSLVDates.getCslpNodeDate(),slvDates.getCslpNodeDate(),true);

        if(edgeCslpNodeDate != null){
            boolean res =  isDatePresent(installMaintenanceLogModel.getIdOnController(),edgeCslpNodeDate, DateType.CSLP_NODE.toString());
            if(!res){
                syncEdgeDates.setCslpNodeDate(edgeCslpNodeDate);
            }
        }

        // CSLP Lum Install Dates
        String edgeCslpLumDate =   installMaintenanceLogModel.getDatesHolder().compareSLVEdgeDate(edgeSLVDates.getCslpLumDate(),slvDates.getCslpLumDate(),true);

        if(edgeCslpLumDate != null){
            boolean res =  isDatePresent(installMaintenanceLogModel.getIdOnController(),edgeCslpLumDate, DateType.CSLP_LUM.toString());
            if(!res){
                syncEdgeDates.setCslpLumDate(edgeCslpLumDate);
            }
        }


        //  Node Install Dates
        String edgeNodeDate =  installMaintenanceLogModel.getDatesHolder().compareSLVEdgeDate(edgeSLVDates.getNodeInstallDate(),slvDates.getNodeInstallDate(),false);
        if(edgeNodeDate != null){
            boolean res =  isDatePresent(installMaintenanceLogModel.getIdOnController(),edgeNodeDate, DateType.NODE.toString());
            if(!res){
                syncEdgeDates.setCslpNodeDate(edgeNodeDate);
            }
        }

        // CSLP Lum Install Dates
        String edgeLumInstallDate = installMaintenanceLogModel.getDatesHolder().compareSLVEdgeDate(edgeSLVDates.getLumInstallDate(),slvDates.getLumInstallDate(),false);
        if(edgeLumInstallDate != null){
            boolean res =  isDatePresent(installMaintenanceLogModel.getIdOnController(),edgeLumInstallDate, DateType.LUM.toString());
            if(!res){
                syncEdgeDates.setLumInstallDate(edgeLumInstallDate);
            }
        }
    }


    /**
     * Check EdgeForm has CSLP node install date,CSLP lum install date,Install date, Lum Install date value.
     * If value is present, set those value to the corresponding fields and set date sync flag as true
     * @param loggingModel
     * @param paramsList
     */
    private void setEdgeDates(InstallMaintenanceLogModel loggingModel,List<Object> paramsList){
        DatesHolder datesHolder = loggingModel.getDatesHolder();
        logger.info("Populate EdgeForm Date Values.");
        if(datesHolder != null && datesHolder.getSyncEdgeDates() != null){
            SLVDates syncEdgeDates = datesHolder.getSyncEdgeDates();
            logger.info("SyncEdgeDates:"+syncEdgeDates.toString());
            // CSLP Node Install Date.
            boolean isCSLPNodeInstallDate = addDateParam("cslp.node.install.date",syncEdgeDates.getCslpNodeDate(),paramsList);
            logger.info("isCSLPNodeInstallDate added:"+isCSLPNodeInstallDate);
            datesHolder.setCslpNodeDateSynced(isCSLPNodeInstallDate);

            // CSLP Lum Install Date.
            boolean isCSLPLumInstallDate = addDateParam("cslp.lum.install.date",syncEdgeDates.getCslpLumDate(),paramsList);
            logger.info("isCSLPLumInstallDate added:"+isCSLPLumInstallDate);
            datesHolder.setCslpLumDateSynced(isCSLPLumInstallDate);

            // Install Date
            boolean isInstallDate = addDateParam("install.date",syncEdgeDates.getNodeInstallDate(),paramsList);
            logger.info("isInstallDate added:"+isInstallDate);
            datesHolder.setInstallDateSynced(isInstallDate);

            // Lum Install Date
            boolean isLumInstallDate = addDateParam("luminaire.installdate",syncEdgeDates.getLumInstallDate(),paramsList);
            logger.info("isLumInstallDate added:"+isLumInstallDate);
            datesHolder.setCslpLumDateSynced(isLumInstallDate);
        }
    }

    private boolean addDateParam(String paramName,String value,List<Object> paramsList){
        if(value != null){
            addStreetLightData(paramName, value, paramsList);
            return true;
        }
        return false;
    }



    private void syncEdgeDates(InstallMaintenanceLogModel installMaintenanceLogModel){
        logger.info("Edge Date Sync Process Starts.");
        if(installMaintenanceLogModel.getDatesHolder() != null && installMaintenanceLogModel.getDatesHolder().getSyncEdgeDates() != null){

            SLVDates syncEdgeDates = installMaintenanceLogModel.getDatesHolder().getSyncEdgeDates();
            logger.info("SyncEdgeDates:"+syncEdgeDates.toString());
            List<Object> paramsList = new ArrayList<>();

            if(syncEdgeDates.getCslpNodeDate() != null && !installMaintenanceLogModel.getDatesHolder().isCslpNodeDateSynced()){
                logger.info("CslpNodeInstallDate Added");
                addDateParam("cslp.node.install.date",syncEdgeDates.getCslpNodeDate(),paramsList);
            }

            if(syncEdgeDates.getCslpLumDate() != null && !installMaintenanceLogModel.getDatesHolder().isCslpLumDateSynced()){
                logger.info("CslpLumInstallDate Added");
                addDateParam("cslp.lum.install.date",syncEdgeDates.getCslpNodeDate(),paramsList);
            }


            if(syncEdgeDates.getNodeInstallDate() != null && !installMaintenanceLogModel.getDatesHolder().isInstallDateSynced()){
                logger.info("Install Date Added");
                addDateParam("install.date",syncEdgeDates.getCslpNodeDate(),paramsList);
            }


            if(syncEdgeDates.getLumInstallDate() != null && !installMaintenanceLogModel.getDatesHolder().isLumInstallDateSynced()){
                logger.info("Luminaire Install Date Added");
                addDateParam("luminaire.installdate",syncEdgeDates.getLumInstallDate(),paramsList);
            }

            if(paramsList.size() > 0){
                logger.info("Date value is Present. Syncing Date value....");
                String idOnController = installMaintenanceLogModel.getIdOnController();
                paramsList.add("idOnController=" + idOnController);
                paramsList.add("controllerStrId=" + installMaintenanceLogModel.getControllerSrtId());
                SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(installMaintenanceLogModel);
                int errorCode = setDeviceValues(paramsList, slvTransactionLogs);
                logger.info("Error Code:"+errorCode);
            }else{
                logger.info("No Date value is present");
            }

        }
    }


    private void createAllSLVDate(SLVDates slvDates,String idOnController){
        logger.info("createAllSLVDate Process Starts");
        if(slvDates != null){
            saveEdgeSLVDate(slvDates.getCslpNodeDate(),DateType.CSLP_NODE.toString(),idOnController);
            saveEdgeSLVDate(slvDates.getCslpLumDate(),DateType.CSLP_NODE.toString(),idOnController);
            saveEdgeSLVDate(slvDates.getNodeInstallDate(),DateType.NODE.toString(),idOnController);
            saveEdgeSLVDate(slvDates.getLumInstallDate(),DateType.LUM.toString(),idOnController);
        }


    }

    private void saveEdgeSLVDate(String date,String dateType,String idOnController){
        if(date != null){
            EdgeSLVDate edgeSLVDate = new EdgeSLVDate();
            edgeSLVDate.setTitle(idOnController);
            edgeSLVDate.setEdgeDate(date);
            edgeSLVDate.setDatesType(dateType);
            connectionDAO.saveEdgeNodeDate(edgeSLVDate);
            logger.info("EdgeSLVDate Saved..");
        }

    }
}
