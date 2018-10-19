package com.terragoedge.streetlight.service;

import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.edgeserver.FormData;
import com.terragoedge.streetlight.exception.*;
import com.terragoedge.streetlight.logging.InstallMaintenanceLogModel;
import com.terragoedge.streetlight.logging.LoggingModel;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class InstallationMaintenanceProcessor extends AbstractProcessor {

    public InstallationMaintenanceProcessor(WeakHashMap<String, String> contextListHashMap) {
        super();
        this.contextListHashMap = contextListHashMap;
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

    public void processNewAction(EdgeNote edgeNote, InstallMaintenanceLogModel installMaintenanceLogModel, boolean isReSync, String utilLocId) {
        List<FormData> formDatas = edgeNote.getFormData();
        for (FormData formData : formDatas) {
            if (formData.getFormTemplateGuid().equals(INSTATALLATION_AND_MAINTENANCE_GUID) || formData.getFormTemplateGuid().equals("fa47c708-fb82-4877-938c-992e870ae2a4") || formData.getFormTemplateGuid().equals("c8acc150-6228-4a27-bc7e-0fabea0e2b93")) {
                installMaintenanceLogModel.setInstallFormPresent(true);
                LoggingModel existingFixtureInfo = getExistingIdOnContoller(edgeNote);
                List<EdgeFormData> edgeFormDatas = formData.getFormDef();
                try {

                    String value = value(edgeFormDatas, "Action");
                    logger.info("Action Val" + value);
                    switch (value) {
                        case "New":
                            processNewGroup(edgeFormDatas, edgeNote, installMaintenanceLogModel, isReSync, existingFixtureInfo, utilLocId);
                            installMaintenanceLogModel.setInstalledDate(String.valueOf(edgeNote.getCreatedDateTime()));
                            return;
                        case "Repairs & Outages":
                            repairAndOutage(edgeFormDatas, edgeNote, installMaintenanceLogModel, existingFixtureInfo, utilLocId);
                            installMaintenanceLogModel.setReplacedDate(String.valueOf(edgeNote.getCreatedDateTime()));
                            return;
                        case "Other Task":
                            processOtherTask(edgeFormDatas, edgeNote, installMaintenanceLogModel);
                            return;
                    }
                } catch (NoValueException e) {
                    logger.error("error in processNewAction method", e);
                    installMaintenanceLogModel.setErrorDetails(MessageConstants.ACTION_NO_VAL);
                    installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
                }
            } else if (formData.getFormTemplateGuid().equals("29a5f0ea-d04e-4d39-9769-5fac4fcb396d")) {
                List<Object> paramsList = new ArrayList<>();
                List<EdgeFormData> edgeFormDatas = formData.getFormDef();
                String nightRideValue = null;
                try {
                    nightRideValue = valueById(edgeFormDatas, 1);
                    logger.info("nightRideValue's Value :" + nightRideValue);
                    String controllerStarId = getControllerStrId(edgeFormDatas);
                    String idOnController = installMaintenanceLogModel.getIdOnController();
                    paramsList.add("idOnController=" + idOnController);
                    paramsList.add("controllerStrId=" + controllerStarId);
                    String formatedValue = dateFormat(edgeNote.getCreatedDateTime()) + " :" + nightRideValue;
                    addStreetLightData("key", formatedValue, paramsList);
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

                } catch (NoValueException e) {
                    logger.error("Error in while getting nightRideValue's value : ", e);
                }

            }
                /*else {
                installMaintenanceLogModel.setErrorDetails(MessageConstants.ACTION_NO_VAL);
                installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
                installMaintenanceLogModel.setInstallFormPresent(false);
            }*/
        }
    }


    private int sync2SlvInstallStatus(String idOnController, String controllerStrIdValue, InstallMaintenanceLogModel loggingModel) {
        List<Object> paramsList = new ArrayList<>();
        String installStatus = properties.getProperty("could_note_complete_install_status");
        paramsList.add("idOnController=" + idOnController);
        paramsList.add("controllerStrId=" + controllerStrIdValue);
        addStreetLightData("installStatus", installStatus, paramsList);
        int errorCode = setDeviceValues(paramsList);
        logger.info("Error code" + errorCode);
        return errorCode;
    }

    private void sync2Slv(String macAddress, String fixerQrScanValue, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, String idOnController, String controllerStrIdValue, String comment, String utilLocId, boolean isNew) {
        try {

            List<Object> paramsList = new ArrayList<>();

            paramsList.add("idOnController=" + idOnController);
            paramsList.add("controllerStrId=" + controllerStrIdValue);
            addOtherParams(edgeNote, paramsList, idOnController, utilLocId, isNew);
            if (fixerQrScanValue != null) {
                buildFixtureStreetLightData(fixerQrScanValue, paramsList, edgeNote);//update fixer qrscan value
            }

            if (comment != null) {
                addStreetLightData("comment", comment, paramsList);
            }

            if (macAddress != null && !macAddress.trim().isEmpty()) {
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
                replaceOLC(controllerStrIdValue, idOnController, macAddress);// insert mac address
                logger.info("Replace OLC End");
                loggingModel.setStatus(MessageConstants.SUCCESS);
                logger.info("Status Changed. to Success");
            }

        } catch (ReplaceOLCFailedException | InValidBarCodeException e) {
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

    public void processOtherTask(List<EdgeFormData> edgeFormDatas, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel) {
        List<Object> paramsList = new ArrayList<>();
        String otherTaskValue = null;
        try {
            otherTaskValue = valueById(edgeFormDatas, 44);
            logger.info("OtherTask's issue Value :" + otherTaskValue);
        } catch (NoValueException e) {
            logger.error("Error in while getting processTask's issue", e);
        }
        String controllerStarId = getControllerStrId(edgeFormDatas);
        String idOnController = loggingModel.getIdOnController();
        paramsList.add("idOnController=" + idOnController);
        paramsList.add("controllerStrId=" + controllerStarId);
        String formatedValue = dateFormat(edgeNote.getCreatedDateTime()) + " :" + otherTaskValue;
        addStreetLightData("key", formatedValue, paramsList);
        int errorCode = setDeviceValues(paramsList);
        if (errorCode != 0) {
            loggingModel.setErrorDetails(MessageConstants.ERROR_OTHERTASK_DEVICE_VAL);
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        } else {
            logger.info("OtherTask's value updated successfully in SLV");
            loggingModel.setStatus(MessageConstants.SUCCESS);
            logger.info("Status Changed. to Success");
        }
    }


    private void processNewGroup(List<EdgeFormData> edgeFormDatas, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, boolean isResync, LoggingModel existingFixtureInfo, String utilLocId) {
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
                int errorCode = sync2SlvInstallStatus(loggingModel.getIdOnController(), loggingModel.getControllerSrtId(), loggingModel);
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
                loggingModel.setErrorDetails(MessageConstants.NODE_MAC_ADDRESS_NOT_AVAILABLE);
                loggingModel.setStatus(MessageConstants.ERROR);
                return;
            }

            // Get Fixer QR Scan value
            String fixerQrScanValue = null;
            try {
                fixerQrScanValue = valueById(edgeFormDatas, 20);
                logger.info("Fixture QR Scan Val" + fixerQrScanValue);
            } catch (NoValueException e) {
                logger.error("Error in while getting MAC Fixture QR Scan", e);
                /*loggingModel.setErrorDetails(MessageConstants.FIXTURE_CODE_NOT_AVAILABLE);
                loggingModel.setStatus(MessageConstants.ERROR);*/
                // loggingModel.setProcessOtherForm(true);
                // return;
            }

            if (!nodeMacValue.startsWith("00") && (fixerQrScanValue != null && fixerQrScanValue.startsWith("00"))) {
                String temp = nodeMacValue;
                nodeMacValue = fixerQrScanValue;
                fixerQrScanValue = temp;
            }

            if (isResync) {
                try {
                    replaceOLC(loggingModel.getControllerSrtId(), loggingModel.getIdOnController(), "");
                } catch (ReplaceOLCFailedException e) {
                    String message = e.getMessage();

                }

            }

            // Check Whether MAC Address is already assigned to other fixtures or not.
            try {
                checkMacAddressExists(nodeMacValue, loggingModel.getIdOnController());
            } catch (QRCodeAlreadyUsedException e1) {
                logger.error("MacAddress (" + e1.getMacAddress()
                        + ")  - Already in use. So this pole is not synced with SLV. Note Title :[" + edgeNote.getTitle()
                        + " ]");
                loggingModel.setStatus(MessageConstants.ERROR);
                loggingModel.setErrorDetails("MacAddress (" + e1.getMacAddress() + ")  - Already in use.");
                return;
            }

            sync2Slv(nodeMacValue, fixerQrScanValue, edgeNote, loggingModel, loggingModel.getIdOnController(), loggingModel.getControllerSrtId(), null, utilLocId, true);
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


    public void repairAndOutage(List<EdgeFormData> edgeFormDatas, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, LoggingModel existingFixtureInfo, String utilLocId) {

        String repairsOutagesValue = null;
        try {
            repairsOutagesValue = valueById(edgeFormDatas, 24);
        } catch (NoValueException e) {
            loggingModel.setStatus(MessageConstants.ERROR);
            loggingModel.setErrorDetails("Repairs & Outages options are not selected.");
            return;
        }


        switch (repairsOutagesValue) {
            case "Replace Node and Fixture":
                replaceNodeFixture(edgeFormDatas, edgeNote, loggingModel, existingFixtureInfo, utilLocId);
                break;
            case "Replace Node only":
                replaceNodeOnly(edgeFormDatas, edgeNote, loggingModel, existingFixtureInfo, utilLocId);
                break;
            case "Replace Fixture only":
                loggingModel.setStatus(MessageConstants.ERROR);
                loggingModel.setErrorDetails("SLV Interface Doest not support this(Replace Fixture only option is selected).");
                break;
            case "Power Issue":
                loggingModel.setStatus(MessageConstants.ERROR);
                loggingModel.setErrorDetails("SLV Interface Doest not support this(Power Issue option is selected).");
                break;
        }
    }


    private void replaceNodeFixture(List<EdgeFormData> edgeFormDatas, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, LoggingModel existingFixtureInfo, String utilLocId) {
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
                return;
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


            if (!newNodeMacAddress.startsWith("00") && (fixerQrScanValue != null && fixerQrScanValue.startsWith("00"))) {
                String temp = newNodeMacAddress;
                newNodeMacAddress = fixerQrScanValue;
                fixerQrScanValue = temp;
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
                    return;
                }
            }


            try {
                checkMacAddressExists(newNodeMacAddress, idOnController);
            } catch (QRCodeAlreadyUsedException e1) {
                logger.error("MacAddress (" + e1.getMacAddress()
                        + ")  - Already in use. So this pole is not synced with SLV. Note Title :[" + edgeNote.getTitle()
                        + " ]");
                loggingModel.setStatus(MessageConstants.ERROR);
                loggingModel.setErrorDetails("MacAddress (" + e1.getMacAddress() + ")  - Already in use");
            } catch (Exception e) {
                logger.error(e.getMessage());
                loggingModel.setStatus(MessageConstants.ERROR);
                return; // No need to process if mac address assigned to another device. Confirmed by vish (13 July 2018)
            }


            boolean isError = false;
            StringBuffer statusDescription = new StringBuffer();
            // Call Empty ReplaceOLC
            try {
                replaceOLC(controllerStrIdValue, idOnController, "");
                statusDescription.append(MessageConstants.EMPTY_REPLACE_OLC_SUCCESS);
            } catch (ReplaceOLCFailedException e) {
                isError = true;
                statusDescription.append(e.getMessage());
                e.printStackTrace();
            }

            comment = comment + " replaced on " + dateFormat(edgeNote.getCreatedDateTime());

            sync2Slv(newNodeMacAddress, fixerQrScanValue, edgeNote, loggingModel, idOnController, controllerStrIdValue, comment, utilLocId, false);
            if (isError) {
                loggingModel.setErrorDetails(loggingModel.getErrorDetails() + statusDescription.toString());
            }
        } catch (Exception e) {
            loggingModel.setErrorDetails(e.getMessage());
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        }

    }


    private void replaceNodeOnly(List<EdgeFormData> edgeFormDatas, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, LoggingModel existingFixtureInfo, String utilLocId) {
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
                } catch (QRCodeNotMatchedException e1) {
                    loggingModel.setStatus(MessageConstants.ERROR);
                    loggingModel.setErrorDetails(MessageConstants.REPLACE_MAC_NOT_MATCH);
                    return;
                }
            }


            try {
                checkMacAddressExists(newNodeMacAddress, idOnController);
            } catch (QRCodeAlreadyUsedException e1) {
                logger.error("MacAddress (" + e1.getMacAddress()
                        + ")  - Already in use. So this pole is not synced with SLV. Note Title :[" + edgeNote.getTitle()
                        + " ]");
                loggingModel.setStatus(MessageConstants.ERROR);
                loggingModel.setErrorDetails("MacAddress (" + e1.getMacAddress() + ")  - Already in use");
            } catch (Exception e) {
                logger.error(e.getMessage());
                loggingModel.setStatus(MessageConstants.ERROR);
                return;
            }


            // Call Empty ReplaceOLC
            try {
                replaceOLC(controllerStrIdValue, idOnController, "");
            } catch (ReplaceOLCFailedException e) {
                e.printStackTrace();
            }

            comment = comment + " replaced on " + dateFormat(edgeNote.getCreatedDateTime());

            sync2Slv(newNodeMacAddress, null, edgeNote, loggingModel, idOnController, controllerStrIdValue, comment, utilLocId, false);


        } catch (Exception e) {
            loggingModel.setErrorDetails(e.getMessage());
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        }


    }

}
