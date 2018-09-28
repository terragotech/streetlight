package com.terragoedge.slvinterface.utils;

import com.opencsv.CSVWriter;
import com.terragoedge.automation.model.InventoryResult;
import com.terragoedge.automation.model.MacValidationModel;
import com.terragoedge.automation.model.ReplaceModel;
import com.terragoedge.slvinterface.exception.NoValueException;
import com.terragoedge.slvinterface.model.CsvReportModel;
import com.terragoedge.slvinterface.model.EdgeFormData;
import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Utils {
    public static String dateFormat(Long dateTime) {
        Date date = new Date(Long.valueOf(dateTime));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dff = dateFormat.format(date);
        return dff;
    }

    public static String getFormValue(List<EdgeFormData> edgeFormDatas, int id) {
        EdgeFormData tempEdgeFormData = new EdgeFormData();
        tempEdgeFormData.setId(id);
        int pos = edgeFormDatas.indexOf(tempEdgeFormData);
        if (pos != -1) {
            EdgeFormData edgeFormData = edgeFormDatas.get(pos);
            return edgeFormData.getValue();
        }
        return "";
    }

    public static List<String[]> toStringArray(List<CsvReportModel> csvReportModelList) {
        List<String[]> records = new ArrayList<String[]>();
        //add header record
        records.add(new String[]{"NoteId", "idOnController", "latitude", "longitude", "createddatetime", "createdBy", "revisionOfNoteId", "noteType",
                "baseParentNoteId", "isCurrent", "isTask", "groupName", "groupGuid", "colorName", "formGuid", "formTemplateGuid", "noteGuid", "ExistingFixtureInformation",
                "SL", "controllerStrId", "geoZoneId", "ghildGeoZoneId", "address", "atlasPhysicalPage", "fixtureColor", "cDOTLampType", "colorCode", "fixtureCode",
                "mastArmAngle", "mastArmLength", "mastArmsCount", "proposedContext", "action", "nodeMACAddress", "fixtureQRScan1", "installStatus", "skippedFixtureReason",
                "skippedReason", "repairsAndOutages", "ExistingNodeMACAddress1", "newNodeMACAddress1", "fixtureQRScan2", "ExistingNodeMACAddress2", "NewNodeMACAddress2",
                "oldFixtureQRScan", "newFixtureQRScan", "reasonForReplacement", "name", "category", "isDelete", "noteBookName", "locationDescription",
                "altitude", "satellitesCount", "gpsTime", "locationProvider", "syncTime", "selectedRepair"});
        for (CsvReportModel csvReportModel : csvReportModelList) {
            //  if (csvReportModel != null && csvReportModel.getSelectedRepair() != null && csvReportModel.getSelectedRepair().equals("Unable to Repair(CDOT Issue)")) {
            records.add(new String[]{
                    String.valueOf(csvReportModel.getNoteID()),
                    csvReportModel.getIdOnController(),
                    csvReportModel.getLatitude(),
                    csvReportModel.getLongitude(),
                    csvReportModel.getCreateddatetime(),
                    csvReportModel.getCreatedBy(),
                    csvReportModel.getRevisionOfNoteId(),
                    csvReportModel.getBaseParentNoteId(),
                    csvReportModel.isCurrent(),
                    csvReportModel.isTask(),
                    csvReportModel.getGroupName(),
                    csvReportModel.getGroupGuid(),
                    csvReportModel.getColorName(),
                    csvReportModel.getFormGuid(),
                    csvReportModel.getFormTemplateGuid(),
                    csvReportModel.getNoteGuid(),
                    csvReportModel.getExistingFixtureInformation(),
                    csvReportModel.getSL(),
                    csvReportModel.getControllerStrId(),
                    csvReportModel.getGeoZoneId(),
                    csvReportModel.getGhildGeoZoneId(),
                    csvReportModel.getAddress(),
                    csvReportModel.getAtlasPhysicalPage(),
                    csvReportModel.getFixtureColor(),
                    csvReportModel.getcDOTLampType(),
                    csvReportModel.getColorCode(),
                    csvReportModel.getFixtureCode(),
                    csvReportModel.getMastArmAngle(),
                    csvReportModel.getMastArmLength(),
                    csvReportModel.getMastArmsCount(),
                    csvReportModel.getProposedContext(),
                    csvReportModel.getAction(),
                    csvReportModel.getNodeMACAddress(),
                    csvReportModel.getFixtureQRScan1(),
                    csvReportModel.getInstallStatus(),
                    csvReportModel.getSkippedFixtureReason(),
                    csvReportModel.getSkippedReason(),
                    csvReportModel.getRepairsAndOutages(),
                    csvReportModel.getExistingNodeMACAddress1(),
                    csvReportModel.getNewNodeMACAddress1(),
                    csvReportModel.getFixtureQRScan2(),
                    csvReportModel.getExistingNodeMACAddress2(),
                    csvReportModel.getNewNodeMACAddress2(),
                    csvReportModel.getOldFixtureQRScan(),
                    csvReportModel.getNewFixtureQRScan(),
                    csvReportModel.getReasonForReplacement(),
                    csvReportModel.getName(),
                    csvReportModel.getCategory(),
                    csvReportModel.isDelete(),
                    csvReportModel.getNoteBookName(),
                    csvReportModel.getLocationDescription(),
                    csvReportModel.getAltitude(),
                    csvReportModel.getSatellitesCount(),
                    csvReportModel.getGpsTime(),
                    csvReportModel.getLocationProvider(),
                    csvReportModel.getSyncTime(),
                    csvReportModel.getSelectedRepair()
            });
        }
        //   }
        return records;
    }

    public static void write(String data, String filePath) {
        System.out.println("OutputFile path is :" + filePath);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(filePath);
            // fileOutputStream = new FileOutputStream(filePath + fileName);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.flush();
            System.out.println("Successfully generated CSV file");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("file writting problem" + e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void writeCSVData(List<CsvReportModel> csvReportModelList) throws IOException {
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer, ',');
        List<String[]> data = toStringArray(csvReportModelList);
        csvWriter.writeAll(data);
        csvWriter.close();
        System.out.println(writer);
        String filePath = PropertiesReader.getProperties().getProperty("edge.replacepath.output");
        ;
        write(writer.toString(), filePath);
    }

    public static void writeInventoryData(List<InventoryResult> inventoryResults, String outputFilePath) {
        try {
            StringWriter writer = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(writer, ',');
            List<String[]> data = toStringInventoryArray(inventoryResults);
            csvWriter.writeAll(data);
            csvWriter.close();
            System.out.println(writer);
            write(writer.toString(), outputFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeMacValidationData(List<MacValidationModel> csvReportModelList, String outputFilePath) {
        try {
            StringWriter writer = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(writer, ',');
            List<String[]> data = toStringMacReportArray(csvReportModelList);
            csvWriter.writeAll(data);
            csvWriter.close();
            System.out.println(writer);
            write(writer.toString(), outputFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeReplacementData(List<ReplaceModel> replaceModelList, String outputFilePath) {
        try {
            StringWriter writer = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(writer, ',');
            List<String[]> data = toStringReplacementArray(replaceModelList);
            csvWriter.writeAll(data);
            csvWriter.close();
            System.out.println(writer);
            write(writer.toString(), outputFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static List<String[]> toStringInventoryArray(List<InventoryResult> inventoryResults) {
        List<String[]> records = new ArrayList<String[]>();
        //add header record
        records.add(new String[]{"Scanning", "PalletWorkflow", "Action", "AssignedUser", "DeliveryLocation", "PalletNumber", "InventoryHandlingList", "Title", "NoteGuid", "LoadingFromReport", "ToInstaller"});
        for (InventoryResult inventoryResult : inventoryResults) {
            records.add(new String[]{
                    inventoryResult.getScanning(),
                    inventoryResult.getPalletWorkflow(),
                    inventoryResult.getAction(),
                    inventoryResult.getAssignedToUser(),
                    inventoryResult.getDeliveryLocation(),
                    inventoryResult.getPalletNumber(),
                    inventoryResult.getInventoryHandlingPalletList(),
                    inventoryResult.getTitle(),
                    inventoryResult.getNoteGuid(),
                    inventoryResult.getReportFrom(),
                    inventoryResult.getToInstaller()
            });
        }
        //   }
        return records;
    }

    public static List<String[]> toStringMacReportArray(List<MacValidationModel> csvReportModelList) {
        List<String[]> records = new ArrayList<String[]>();
        //add header record
        records.add(new String[]{"macaddress", "fixtureid", "municipality", "modifieddate", "user", "assigneduser", "InstallStatus", "InventoryStatus"});
        for (MacValidationModel csvReportModel : csvReportModelList) {
            records.add(new String[]{
                    csvReportModel.getMacaddress(),
                    csvReportModel.getFixtureid(),
                    csvReportModel.getMunicipality(),
                    csvReportModel.getModifieddate(),
                    csvReportModel.getUser(),
                    csvReportModel.getAssigneduser(),
                    csvReportModel.getInstallStatus(),
                    csvReportModel.getInventoryStatus(),
            });
        }
        //   }
        return records;
    }

    public static List<String[]> toStringReplacementArray(List<ReplaceModel> replaceModelList) {
        List<String[]> records = new ArrayList<String[]>();
        //add header record
        records.add(new String[]{"FixtureId", "Municipality", "Workflow", "InstallMacAddress", "ExpectedMacAddress", "ReplaceMacAddress", "ModifiedDate", "User", "Status"});
        for (ReplaceModel replaceModel : replaceModelList) {
            records.add(new String[]{
                    replaceModel.getFixtureid(),
                    replaceModel.getMunicipality(),
                    replaceModel.getWorkflow(),
                    replaceModel.getInstalledmacaddress(),
                    replaceModel.getExpectedmacaddress(),
                    replaceModel.getReplacedmacaddress(),
                    replaceModel.getModifieddate(),
                    replaceModel.getUser(),
                    replaceModel.getStatus(),
            });
        }
        //   }
        return records;
    }

    public static String getFileNameWithoutExtension(File file) {
        String name = file.getName();
        int pos = name.lastIndexOf('.');
        if (pos > 0 && pos < (name.length() - 1)) {
            // there is a '.' and it's not the first, or last character.
            return name.substring(0, pos);
        }
        return name;
    }

    public static String valueById(List<EdgeFormData> edgeFormDatas, int id) throws NoValueException {
        EdgeFormData edgeFormTemp = new EdgeFormData();
        edgeFormTemp.setId(id);

        int pos = edgeFormDatas.indexOf(edgeFormTemp);
        if (pos != -1) {
            EdgeFormData edgeFormData = edgeFormDatas.get(pos);
            String value = edgeFormData.getValue();
            if (value.contains("#")) {
                String[] values = value.split("#");
                if (values.length == 2) {
                    return values[1];
                }
            }

            if (value == null || value.trim().isEmpty()) {
                throw new NoValueException("Value is Empty or null." + value);
            }
            return value;
        } else {
            throw new NoValueException(id + " is not found.");
        }
    }

}
