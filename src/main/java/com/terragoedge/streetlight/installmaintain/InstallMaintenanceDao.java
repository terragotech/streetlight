package com.terragoedge.streetlight.installmaintain;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVWriter;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.dao.FormData;
import com.terragoedge.streetlight.dao.NoteData;
import com.terragoedge.streetlight.dao.UtilDao;
import com.terragoedge.streetlight.installmaintain.json.Config;
import com.terragoedge.streetlight.installmaintain.json.Ids;
import com.terragoedge.streetlight.installmaintain.json.Prop;
import com.terragoedge.streetlight.installmaintain.utills.Utils;
import com.terragoedge.streetlight.pdfreport.FilterNewInstallationOnly;
import com.terragoedge.streetlight.pdfreport.PDFExceptionUtils;
import com.terragoedge.streetlight.pdfreport.PDFReport;
import com.terragoedge.streetlight.service.EdgeMailService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

public class InstallMaintenanceDao extends UtilDao {
    private Gson gson;
    private InstallMaintenanceService installMaintenanceService;
    private EdgeMailService edgeMailService;
    private List<Config> configs = new ArrayList<>();
    private static Long startTime = 0l;
    private static Long endingTime = 0l;


    public InstallMaintenanceDao() {
        gson = new Gson();
        installMaintenanceService = new InstallMaintenanceService();
        edgeMailService = new EdgeMailService();
        configs = installMaintenanceService.getConfigList();
    }

    private Logger logger = Logger.getLogger(InstallMaintenanceDao.class);

    public void doProcess() {
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        CSVWriter dailyCompletedCSVWriter = null;
        List<DuplicateModel> duplicateModelList = new ArrayList<>();
        String customDate = PropertiesReader.getProperties().getProperty("amerescousa.custom.date");
        if (customDate != null && customDate.equals("true")) {
            logger.info("Custom date is called");
            String startingDate = PropertiesReader.getProperties().getProperty("amerescousa.startingDate");
            String endingDate = PropertiesReader.getProperties().getProperty("amerescousa.endingDate");
            startTime = getDateformatAsMilliSecond(startingDate);
            endingTime = getDateformatAsMilliSecond(endingDate);
            logger.info("custom date StartingDate : " + startingDate);
            logger.info("custom date EndingDate : " + endingDate);
            logger.info("custom date startTime : " + startTime);
            logger.info("custom date endingTime : " + endingTime);
        } else {
            startTime = getReportStartingTime();
            endingTime = System.currentTimeMillis();
            logger.info("startTime : " + startTime);
            logger.info("endingTime : " + endingTime);
        }
        // startTime = Long.parseLong(PropertiesReader.getProperties().getProperty("amerescousa.report.from"));
        logger.info("configs: " + gson.toJson(configs));
        try {
            String fileName = Utils.getDateTime();
            String dailyReportFile = "./report/daily_report_" + fileName + ".csv";
            String duplicateMacAddressFile = "./report/daily_mac_dup_report_" + fileName + ".csv";
            queryStatement = connection.createStatement();
            FileWriter fileWriter = new FileWriter(dailyReportFile);
            dailyCompletedCSVWriter = initCSV(fileWriter);
            logger.info("start time:" + startTime);
            logger.info("readable fromat starting time:" + Utils.getDateTime(startTime));
            logger.info("readable fromat ending time:" + Utils.getDateTime(endingTime));
            System.out.println("readable fromat starting time:" + Utils.getDateTime(startTime));
            System.out.println("readable fromat ending time:" + Utils.getDateTime(endingTime));
            queryResponse = queryStatement.executeQuery("select title,noteguid,parentnoteid,createddatetime from edgenote where iscurrent = true and isdeleted = false  and createddatetime >= " + startTime + " and createddatetime < " + endingTime + "   order by createddatetime;");
            // queryResponse = queryStatement.executeQuery("select title,noteguid,parentnoteid,createddatetime from edgenote where createddatetime >= " + startTime + " and createddatetime < " + endingTime + "   order by createddatetime;");
            String qury = "select title,noteguid,parentnoteid,createddatetime from edgenote where iscurrent = true and isdeleted = false and createddatetime >= " + startTime + " and createddatetime < " + endingTime + "   order by createddatetime";
            logger.info("query is :"+qury);
            logger.info("query response executed");
            int i = 0;
            while (queryResponse.next()) {
                i++;
                String currentNoteGuid = queryResponse.getString("noteguid");
                Long currentNoteDateTime = queryResponse.getLong("createddatetime");
                NoteData currentNoteData = new NoteData();
                currentNoteData.setNoteGuid(currentNoteGuid);
                currentNoteData.setCreatedDateTime(currentNoteDateTime);
                System.out.println("process count :" + i);
                String parentNoteId = queryResponse.getString("parentnoteid");
                logger.info("currentNoteGuid: " + currentNoteGuid);
                logger.info("parentNoteId: " + parentNoteId);

                List<FormData> formDatas = getCurrentNoteDetails(currentNoteGuid);
                logger.info("current note forms count: " + formDatas.size());
                InstallMaintenanceModel currentNoteInstallForm = getInstallMaintenanceModel(formDatas);
                if (currentNoteInstallForm.hasVal() || (currentNoteInstallForm.getAction() != null && currentNoteInstallForm.getAction().equals("CIMCON Node Replacements JBCC Only")) ) {
                    currentNoteData.setInstallMaintenanceModel(currentNoteInstallForm);
                    compareRevisionData(parentNoteId, currentNoteData, dailyCompletedCSVWriter, duplicateModelList);
                    logger.info("Processed item: " + i);
                }

            }
            writeDupCSV(duplicateModelList, duplicateMacAddressFile);
            logger.info("daily install report csv file created!");
            closeCSVBuffer(dailyCompletedCSVWriter);
            edgeMailService.sendMail(duplicateMacAddressFile, dailyReportFile);
            // startGeoPDFProcess(dailyReportFile);
        } catch (Exception e) {
            logger.error("Error in doProcess", e);
            closeCSVBuffer(dailyCompletedCSVWriter);
        } finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
    }


    public void startGeoPDFProcess(String dailyCompletedReport) {
        Properties properties = PropertiesReader.getProperties();
        String destFile = properties.getProperty("dailyreport.inputfile");
        String hostString = properties.getProperty("dailyreport.geomapservice");
        /*** Apply Filter : Current Installs only ***/
        String filterFile1 = "./report/" + "dailyreport_filtered.txt";
        try {
            FilterNewInstallationOnly.applyOperation(dailyCompletedReport, filterFile1);
            /** End of Filter : Current Installs only ***/
            Path source = Paths.get(filterFile1);
            Path destination = Paths.get(destFile);
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            String strDate = Utils.getGeoPdfDateTime();
            PDFReport pdfReport = new PDFReport();
            pdfReport.setHostString(hostString);
            pdfReport.setDateString(strDate);
            new Thread(pdfReport).start();
        } catch (Exception e) {
            String errorTrace = PDFExceptionUtils.getStackTrace(e);
            logger.error(e.getMessage());
            logger.error(errorTrace);
            PDFReport.sendErrorMail(errorTrace);
        }
    }


    private void writeDupCSV(List<DuplicateModel> duplicateModelList, String duplicateMacAddressFile) {
        logger.info("Duplicate MAC Address Writing Process starts.");
        if (duplicateModelList.size() > 0) {
            CSVWriter csvWriter = null;
            try {
                csvWriter = initMACDupCSV(duplicateMacAddressFile);
                for (DuplicateModel duplicateModel : duplicateModelList) {
                    String titles = StringUtils.join(duplicateModel.getTitles(), ",");
                    csvWriter.writeNext(new String[]{duplicateModel.getTitle(), duplicateModel.getMacAddress(), titles});
                }
            } catch (Exception e) {
                logger.error("Error in writeDupCSV", e);
            } finally {
                closeCSVBuffer(csvWriter);
            }

        }
        logger.info("Duplicate MAC Address Writing Process ends.");
    }


    private void compareRevisionData(String parentNoteId, NoteData currentNoteData, CSVWriter csvWriter, List<DuplicateModel> duplicateModelList) {
        List<NoteData> allRevisionsNotes = getAllRevisionsNoteGuids(parentNoteId, currentNoteData.getNoteGuid());


        logger.info("All Revisions notes Count: " + allRevisionsNotes.size());
        for (NoteData revisionNote : allRevisionsNotes) {
            List<FormData> revisionNoteInstallForm = getCurrentNoteDetails(revisionNote.getNoteGuid());
            logger.info("Revision Note: " + revisionNote.getNoteGuid());
            logger.info("child note forms count: " + revisionNoteInstallForm.size());
            InstallMaintenanceModel previousInstallForm = getInstallMaintenanceModel(revisionNoteInstallForm);
            revisionNote.setInstallMaintenanceModel(previousInstallForm);

            boolean isBothNoteSame = comparator(currentNoteData, revisionNote);
            if (isBothNoteSame) {
                currentNoteData = revisionNote;
                boolean todaysInstall = isInstalledOnTime(currentNoteData);
                if (!todaysInstall) {
                    break;
                }
            } else {
                break;
            }


        }
        boolean todaysInstall = isInstalledOnTime(currentNoteData);
        logger.info("Final Note: " + currentNoteData);
        logger.info("Final Note is within Start  Time:" + todaysInstall);
        if (todaysInstall) {
            logger.info("CSV Writing Process Starts.");
            writeCSV(currentNoteData, csvWriter, duplicateModelList);
            logger.info("CSV Writing Process Ends.");
        }
    }

    private void checkMACDuplicate(NoteData currentNoteData, List<DuplicateModel> duplicateModelList) {
        InstallMaintenanceModel installMaintenanceModel = currentNoteData.getInstallMaintenanceModel();
        String macAddress = installMaintenanceModel.getMacAddressRN();
        if (macAddress != null && macAddress.startsWith("00135")) {
            checkMACDuplicate(macAddress, currentNoteData.getTitle(), duplicateModelList);
            return;
        }
        macAddress = installMaintenanceModel.getFixtureQRScanRF();
        if (macAddress != null && macAddress.startsWith("00135")) {
            checkMACDuplicate(macAddress, currentNoteData.getTitle(), duplicateModelList);
            return;
        }

        macAddress = installMaintenanceModel.getMacAddressRNF();
        if (macAddress != null && macAddress.startsWith("00135")) {
            checkMACDuplicate(macAddress, currentNoteData.getTitle(), duplicateModelList);
            return;
        }

        macAddress = installMaintenanceModel.getFixtureQRScanRNF();
        if (macAddress != null && macAddress.startsWith("00135")) {
            checkMACDuplicate(macAddress, currentNoteData.getTitle(), duplicateModelList);
            return;
        }

        macAddress = installMaintenanceModel.getMacAddress();
        if (macAddress != null && macAddress.startsWith("00135")) {
            checkMACDuplicate(macAddress, currentNoteData.getTitle(), duplicateModelList);
            return;
        }

        macAddress = installMaintenanceModel.getFixtureQRScan();
        if (macAddress != null && macAddress.startsWith("00135")) {
            checkMACDuplicate(macAddress, currentNoteData.getTitle(), duplicateModelList);
            return;
        }
    }


    private void checkMACDuplicate(String macAddress, String title, List<DuplicateModel> duplicateModelList) {
        Set<String> macAddressTitle = isMACDuplicated(macAddress, title);
        if (macAddressTitle.size() > 0) {
            DuplicateModel duplicateModel = new DuplicateModel();
            duplicateModel.setTitle(title);
            duplicateModel.setMacAddress(macAddress);
            duplicateModel.setTitles(macAddressTitle);
            duplicateModelList.add(duplicateModel);
        }
    }


    private boolean isInstalledOnTime(NoteData currentNoteData) {
        return currentNoteData.getCreatedDateTime() >= startTime ;
    }


    private CSVWriter initCSV(FileWriter fileWriter) throws Exception {

        CSVWriter csvWriter = new CSVWriter(fileWriter,
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.NO_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);
        String[] headerRecord = {"Title", "Physical atlas page","Action", "MAC Address", "User Id", "Fixture QR Scan", "Fixture Type",
                "Context", "Lat", "Lng", "Date Time", "Is ReplaceNode", "Existing Node MAC Address", "New Node MAC Address", "New Fixture QR Scan", "Reason for Replacement", "Reason for removal", "Resolved Issue", "Resolved Comment", "Scan Existing MAC if wrong", "UnableToRepair Issue", "unableToRepair Comment", "InstallStatus", "Skipped Fixture Reason", "Skipped Reason",
                "Requires Head-To-Head Wiring Go-Back?"};
        csvWriter.writeNext(headerRecord);
        return csvWriter;
    }


    private CSVWriter initMACDupCSV(String duplicateMACFileName) throws Exception {
        FileWriter fileWriter = new FileWriter(duplicateMACFileName);
        CSVWriter csvWriter = new CSVWriter(fileWriter,
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.NO_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);
        String[] headerRecord = {"Title", "MAC Address", "Exists In"};
        csvWriter.writeNext(headerRecord);
        return csvWriter;
    }

    private void closeCSVBuffer(CSVWriter csvWriter) {
        try {
            if (csvWriter != null) {
                csvWriter.flush();
                csvWriter.close();
            }

        } catch (Exception e) {
            logger.error("Error in closeCSVBuffer", e);
        }

    }


    private void writeCSV(NoteData noteData, CSVWriter csvWriter, List<DuplicateModel> duplicateModelList) {
        loadNotesData(noteData);
        loadAtlasPhysicalPage(noteData);
        if ((!noteData.getCreatedBy().equals("admin") && !noteData.getCreatedBy().equals("slvinterface"))) {
            if (noteData.getInstallMaintenanceModel().getRemovalReason() == null) {
                checkMACDuplicate(noteData, duplicateModelList);
            }

            noteData.getInstallMaintenanceModel().checkReplacedDetails();
            logger.info(noteData);
            csvWriter.writeNext(new String[]{
                    noteData.getTitle(),
                    noteData.getNoteBookName(),
                    addDoubleQuotes(noteData.getInstallMaintenanceModel().getAction()),
                    addDoubleQuotes(noteData.getInstallMaintenanceModel().getMacAddress()),
                    noteData.getCreatedBy(),
                    addDoubleQuotes(noteData.getInstallMaintenanceModel().getFixtureQRScan()),
                    noteData.getFixtureType(),
                    addDoubleQuotes(noteData.getDescription()),
                    noteData.getLng(),
                    noteData.getLat(),
                    Utils.getDailyReportDateTime(noteData.getCreatedDateTime()),
                    noteData.getInstallMaintenanceModel().getIsReplaceNode(),
                    addDoubleQuotes(noteData.getInstallMaintenanceModel().getExMacAddressRNF()),
                    addDoubleQuotes(noteData.getInstallMaintenanceModel().getMacAddressRNF()),
                    addDoubleQuotes(noteData.getInstallMaintenanceModel().getNewFixtureQRScan()),
                    addDoubleQuotes(noteData.getInstallMaintenanceModel().getReasonforReplacement()),
                    addDoubleQuotes(noteData.getInstallMaintenanceModel().getRemovalReason()),
                    addDoubleQuotes(noteData.getInstallMaintenanceModel().getResolvedIssue()),
                    addDoubleQuotes(noteData.getInstallMaintenanceModel().getResolvedComment()),
                    addDoubleQuotes(noteData.getInstallMaintenanceModel().getExistingMacIfWrong()),
                    addDoubleQuotes(noteData.getInstallMaintenanceModel().getUnableToRepairIssue()),
                    addDoubleQuotes(noteData.getInstallMaintenanceModel().getUnableToRepairComment()),
                    addDoubleQuotes(noteData.getInstallMaintenanceModel().getInstallStatus()),
                    addDoubleQuotes(noteData.getInstallMaintenanceModel().getSkippedFixtureReason()),
                    addDoubleQuotes(noteData.getInstallMaintenanceModel().getSkippedReason()),
                    addDoubleQuotes(noteData.getInstallMaintenanceModel().getHeadToHead())

            });
        }


    }

    private String addDoubleQuotes(String value) {
        if (value != null) {
            return "\"" + value + "\"";
        }
        return "\"\"";
    }

    public List<FormData> getCurrentNoteDetails(String noteGuid) {
        List<FormData> formDatas = new ArrayList<>();
        PreparedStatement queryStatement = null;
        ResultSet queryResponse = null;
        try {
            queryStatement = connection.prepareStatement("select formdef,formtemplateguid from edgeform where edgenoteentity_noteid in (select noteid from edgenote where noteguid = ?)");
            queryStatement.setString(1, noteGuid);
            queryResponse = queryStatement.executeQuery();
            while (queryResponse.next()) {
                String formDef = queryResponse.getString("formdef");
                String formTemplateGuid = queryResponse.getString("formtemplateguid");
                if (checkFormTemplateInConfig(formTemplateGuid)) {
                    FormData formData = new FormData();
                    formData.setFormDef(formDef);
                    formData.setFormTemplateGuid(formTemplateGuid);
                    formDatas.add(formData);
                }
            }
        } catch (Exception e) {
            logger.error("error in getting forms: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
        return formDatas;
    }

    public List<NoteData> getAllRevisionsNoteGuids(String parentNoteGuid, String currentNoteGuid) {
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        List<NoteData> allRevisionsNoteGuid = new ArrayList<>();
        try {
            queryStatement = connection.createStatement();
            queryResponse = queryStatement.executeQuery("select noteguid,createddatetime from edgenote where parentnoteid='" + parentNoteGuid + "' or noteguid='" + parentNoteGuid + "' order by createddatetime desc");
            while (queryResponse.next()) {
                String noteGuid = queryResponse.getString("noteguid");
                Long createdDateTime = queryResponse.getLong("createddatetime");
                if (!currentNoteGuid.equals(noteGuid)) {
                    NoteData noteData = new NoteData();
                    noteData.setNoteGuid(noteGuid);
                    noteData.setCreatedDateTime(createdDateTime);
                    allRevisionsNoteGuid.add(noteData);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
        return allRevisionsNoteGuid;
    }

    private InstallMaintenanceModel getInstallMaintenanceModel(List<FormData> formDatas) {
        InstallMaintenanceModel installMaintenanceModel = new InstallMaintenanceModel();
        for (FormData formData : formDatas) {
            String formDef = formData.getFormDef();
            List<EdgeFormData> edgeFormDatas = gson.fromJson(formDef, new TypeToken<List<EdgeFormData>>() {
            }.getType());
            String action = getValue(17, edgeFormDatas);
            logger.info("selected new action:" + action);
            if (action == null) {
                action = "";
            }
            if (action.contains("Repairs")) {
                action = getValue(24, edgeFormDatas);
                logger.info("selected ReplaceAction :" + action);
            }
            installMaintenanceModel.setAction(action);
            for (Config config : configs) {
                if (config.getFormTemplateGuid().equals(formData.getFormTemplateGuid())) {
                    //installMaintenanceModel.setInstallStatus(getValue(config.getInstallStatus(), edgeFormDatas));
                    //installMaintenanceModel.setProposedContext(getValue(config.getProposedContext(), edgeFormDatas));
                    List<Prop> props = config.getProps();
                    logger.info("config: " + gson.toJson(config));
                    for (Prop prop : props) {
                        logger.info("prop: " + gson.toJson(prop));
                        Ids idsList = prop.getIds();
                        InstallMaintenanceEnum type = prop.getType();
                        logger.info("type:" + type.toString());
                        switch (type) {
                            case RF:

                                //To be installed To be  installed
                                // installMaintenanceModel.setAction(Constants.REPLACE_FIXTURE_ONLY);
                                installMaintenanceModel.setFixtureQRScanRF(setValue(installMaintenanceModel.getFixtureQRScanRF(), getValue(idsList.getFix(), edgeFormDatas)));
                                installMaintenanceModel.setExFixtureQRScanRF(setValue(installMaintenanceModel.getExFixtureQRScanRF(), getValue(idsList.getExFix(), edgeFormDatas)));
                                installMaintenanceModel.setReasonforReplacement(setValue(installMaintenanceModel.getReasonforReplacement(), getValue(idsList.getReasonforreplacement(), edgeFormDatas)));
                                break;
                            case NEW:
                                // installMaintenanceModel.setAction("New");
                                installMaintenanceModel.setMacAddress(setValue(installMaintenanceModel.getMacAddress(), getValue(idsList.getMac(), edgeFormDatas)));
                                installMaintenanceModel.setFixtureQRScan(setValue(installMaintenanceModel.getFixtureQRScan(), getValue(idsList.getFix(), edgeFormDatas)));
                                installMaintenanceModel.setHeadToHead(setValue(installMaintenanceModel.getHeadToHead(), getValue(idsList.getHeadToHeadWiring(), edgeFormDatas)));
                                break;
                            case RN:
                                // installMaintenanceModel.setAction(Constants.REPLACE_NODE_ONLY);
                                installMaintenanceModel.setMacAddressRN(setValue(installMaintenanceModel.getMacAddressRN(), getValue(idsList.getMac(), edgeFormDatas)));
                                installMaintenanceModel.setExMacAddressRN(setValue(installMaintenanceModel.getExMacAddressRN(), getValue(idsList.getExMac(), edgeFormDatas)));
                                break;
                            case RNF:
                                // installMaintenanceModel.setAction(Constants.REPLACE_NODE_FIXTURE);
                                installMaintenanceModel.setMacAddressRNF(setValue(installMaintenanceModel.getMacAddressRNF(), getValue(idsList.getMac(), edgeFormDatas)));
                                installMaintenanceModel.setExMacAddressRNF(setValue(installMaintenanceModel.getExMacAddressRNF(), getValue(idsList.getExMac(), edgeFormDatas)));
                                installMaintenanceModel.setFixtureQRScanRNF(setValue(installMaintenanceModel.getFixtureQRScanRNF(), getValue(idsList.getFix(), edgeFormDatas)));
                                installMaintenanceModel.setExFixtureQRScanRNF(setValue(installMaintenanceModel.getExFixtureQRScanRNF(), getValue(idsList.getExFix(), edgeFormDatas)));
                                break;
                            case RR:
                                //installMaintenanceModel.setAction(Constants.REMOVE);
                                installMaintenanceModel.setRemovalReason(setValue(installMaintenanceModel.getRemovalReason(), getValue(idsList.getRemove(), edgeFormDatas)));
                                break;
                            case RS:
                                // installMaintenanceModel.setAction(Constants.REPLACE_NODE_FIXTURE);
                                installMaintenanceModel.setResolvedIssue(setValue(installMaintenanceModel.getResolvedIssue(), getValue(idsList.getIssue(), edgeFormDatas)));
                                installMaintenanceModel.setResolvedComment(setValue(installMaintenanceModel.getResolvedComment(), getValue(idsList.getComment(), edgeFormDatas)));
                                installMaintenanceModel.setExistingMacIfWrong(setValue(installMaintenanceModel.getExistingMacIfWrong(), getValue(idsList.getScanifwrong(), edgeFormDatas)));
                                break;
                            case UR:
                                // installMaintenanceModel.setAction(Constants.UNABLE_TO_REPAIR);
                                installMaintenanceModel.setUnableToRepairIssue(setValue(installMaintenanceModel.getUnableToRepairIssue(), getValue(idsList.getUnabletorepairissue(), edgeFormDatas)));
                                installMaintenanceModel.setUnableToRepairComment(setValue(installMaintenanceModel.getUnableToRepairComment(), getValue(idsList.getUnabletorepaircomment(), edgeFormDatas)));
                                break;
                            case IS:
                                installMaintenanceModel.setInstallStatus(setValue(installMaintenanceModel.getInstallStatus(), getValue(idsList.getInstallstatus(), edgeFormDatas)));
                                installMaintenanceModel.setSkippedFixtureReason(setValue(installMaintenanceModel.getSkippedFixtureReason(), getValue(idsList.getSkippedfixtureReason(), edgeFormDatas)));
                                installMaintenanceModel.setSkippedReason(setValue(installMaintenanceModel.getSkippedReason(), getValue(idsList.getSkippedReason(), edgeFormDatas)));
                                break;

                            case CNR:
                                installMaintenanceModel.setCnrMACAddress(setValue(installMaintenanceModel.getCnrMACAddress(), getValue(idsList.getMac(), edgeFormDatas)));
                                break;

                        }
                    }
                }
            }
        }
        installMaintenanceModel.checkCouldNotComplete();
        installMaintenanceModel.checkReplaceAndRemoval();
        return installMaintenanceModel;
    }

    private String getValue(int id, List<EdgeFormData> edgeFormDatas) {
        EdgeFormData edgeFormData = new EdgeFormData();
        edgeFormData.setId(id);
        int pos = edgeFormDatas.indexOf(edgeFormData);
        String value = "";
        if (pos > -1) {
            value = edgeFormDatas.get(pos).getValue();
        }
        return value;
    }

    private String setValue(String oldValue, String newValue) {
        if (oldValue == null || oldValue.equals("")) {
            return newValue;
        } else {
            if (newValue == null || newValue.equals("")) {
                return oldValue;
            } else {
                return newValue;
            }
        }
    }

    private boolean checkFormTemplateInConfig(String formTemplateGuid) {
        Config config = new Config();
        config.setFormTemplateGuid(formTemplateGuid);
        return configs.contains(config);
    }

    private boolean comparator(NoteData currentNoteData, NoteData previousNoteData) {
        logger.info("Current Note:" + currentNoteData.getInstallMaintenanceModel());
        logger.info("Previous Note:" + previousNoteData.getInstallMaintenanceModel());
        if (currentNoteData.getInstallMaintenanceModel().equals(previousNoteData.getInstallMaintenanceModel())) {
            return true;
        } else {
            logger.info("Previous Note Not Match with Current Note.");
            return false;
        }
    }

    public void loadAtlasPhysicalPage(NoteData currentNoteData)
    {
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        try {
            queryStatement = connection.createStatement();
            String sql = "select edgenotebook.notebookname as notebookname from edgenote,edgenotebook where edgenote.noteguid = '" + currentNoteData.getNoteGuid() +"' and edgenote.notebookid=edgenotebook.notebookid;";
            queryResponse = queryStatement.executeQuery(sql);
            String noteBookName = "";
            while (queryResponse.next()) {
                noteBookName = queryResponse.getString("notebookname");
            }
            currentNoteData.setNoteBookName(noteBookName);
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
    }
    public void loadNotesData(NoteData currentNoteData) {
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        try {
            queryStatement = connection.createStatement();
            String sql = "select noteid,createddatetime, createdby,locationdescription,title,groupname,ST_X(geometry::geometry) as lng, ST_Y(geometry::geometry) as lat  from edgenoteview where  noteguid = '" + currentNoteData.getNoteGuid() + "';";
            queryResponse = queryStatement.executeQuery(sql);
            while (queryResponse.next()) {

                String locationDescription = queryResponse.getString("locationdescription");
                if (locationDescription != null) {
                    String[] locations = locationDescription.split("\\|");
                    String groupName = "";
                    if (locations.length == 2) {
                        locationDescription = locations[0];
                        groupName = locations[1];
                    }
                    currentNoteData.setFixtureType(groupName);
                }

                currentNoteData.setDescription(locationDescription);
                currentNoteData.setTitle(queryResponse.getString("title"));
                currentNoteData.setCreatedBy(queryResponse.getString("createdby"));
                currentNoteData.setCreatedDateTime(queryResponse.getLong("createddatetime"));
                currentNoteData.setLat(String.valueOf(queryResponse.getDouble("lat")));
                currentNoteData.setLng(String.valueOf(queryResponse.getDouble("lng")));

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
    }


    public Set<String> isMACDuplicated(String macAddress, String currentNoteTitle) {
        PreparedStatement queryStatement = null;
        ResultSet queryResponse = null;
        Set<String> duplicateTitle = new HashSet<>();
        try {
            String sql = "select title,nodemacaddress,fixtureqrscan,nodemacaddressrnf,fixtureqrscanrnf,nodemacaddressrn,fixtureqrscanrf from edge_install_form_data where (lower(nodemacaddress) = ?  or lower(fixtureqrscan) = ? or lower(nodemacaddressrnf) = ? or lower(fixtureqrscanrnf) = ? or lower(nodemacaddressrn) = ? or lower(fixtureqrscanrf) = ?) and title != ?";
            queryStatement = connection.prepareStatement(sql);
            macAddress = macAddress.trim().toLowerCase();
            queryStatement.setString(1, macAddress);
            queryStatement.setString(2, macAddress);
            queryStatement.setString(3, macAddress);
            queryStatement.setString(4, macAddress);
            queryStatement.setString(5, macAddress);
            queryStatement.setString(6, macAddress);
            queryStatement.setString(7, currentNoteTitle);
            queryResponse = queryStatement.executeQuery();
            while (queryResponse.next()) {

                String nodemacaddress = queryResponse.getString("nodemacaddress");
                String fixtureqrscan = queryResponse.getString("fixtureqrscan");
                String nodemacaddressrnf = queryResponse.getString("nodemacaddressrnf");
                String fixtureqrscanrnf = queryResponse.getString("fixtureqrscanrnf");
                String nodemacaddressrn = queryResponse.getString("nodemacaddressrn");
                String fixtureqrscanrf = queryResponse.getString("fixtureqrscanrf");
                String title = queryResponse.getString("title");

                if (nodemacaddressrn != null && nodemacaddressrn.startsWith("00135")) {
                    if (nodemacaddressrn.toLowerCase().equals(macAddress)) {
                        duplicateTitle.add(title);
                    }

                } else if (fixtureqrscanrf != null && fixtureqrscanrf.startsWith("00135")) {
                    if (fixtureqrscanrf.toLowerCase().equals(macAddress)) {
                        duplicateTitle.add(title);
                    }
                } else if (nodemacaddressrnf != null && nodemacaddressrnf.startsWith("00135")) {
                    if (nodemacaddressrnf.toLowerCase().equals(macAddress)) {
                        duplicateTitle.add(title);
                    }
                } else if (fixtureqrscanrnf != null && fixtureqrscanrnf.startsWith("00135")) {
                    if (fixtureqrscanrnf.toLowerCase().equals(macAddress)) {
                        duplicateTitle.add(title);
                    }
                } else if (fixtureqrscan != null && fixtureqrscan.startsWith("00135")) {
                    if (fixtureqrscan.toLowerCase().equals(macAddress)) {
                        duplicateTitle.add(title);
                    }
                } else {
                    duplicateTitle.add(title);
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
        return duplicateTitle;
    }

    public static long getDateformatAsMilliSecond(String configDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(configDate);
            return date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static long getReportStartingTime() {
        try {
            String customDate = PropertiesReader.getProperties().getProperty("amerescousa.timezone");
            Calendar cal = Calendar.getInstance();
            TimeZone tz = TimeZone.getTimeZone(customDate);
            cal.setTimeZone(tz);
            cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            return cal.getTimeInMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
