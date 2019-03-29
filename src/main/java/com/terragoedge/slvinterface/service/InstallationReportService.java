package com.terragoedge.slvinterface.service;

import com.terragoedge.slvinterface.dao.InstallationDAO;
import com.terragoedge.slvinterface.model.InstallationReportModel;
import com.terragoedge.slvinterface.model.SlvData;
import com.terragoedge.slvinterface.utils.Constants;
import com.terragoedge.slvinterface.utils.FileUtils;
import com.terragoedge.slvinterface.utils.PropertiesReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class InstallationReportService extends AbstractReportService {
    private InstallationDAO installationDAO;
    private Properties properties;

    public InstallationReportService() {
        this.installationDAO = new InstallationDAO();
        properties = PropertiesReader.getProperties();
    }

    public void root() {
        List<InstallationReportModel> installationReportModelList = installationDAO.getInstallationReports();
        List<InstallationReportModel> syncedInstalltionReports = new ArrayList<>();
        List<InstallationReportModel> unSyncedInstalltionReports = new ArrayList<>();
        List<InstallationReportModel> errorInstalltionReports = new ArrayList<>();
        for (InstallationReportModel installationReportModel : installationReportModelList) {
            SlvData slvData = installationDAO.checkMacAddressExistInSlv(installationReportModel);
            long installDateMillis = getInstallDateAsLong(slvData);
            if (slvData != null && Constants.slv_pole_status != null && Constants.slv_pole_status.contains(slvData.getInstallstatus()) && installationReportModel.getCreatedDatetime() < installDateMillis) {
                errorInstalltionReports.add(installationReportModel);
            } else if (slvData != null) {
                syncedInstalltionReports.add(installationReportModel);
            } else {
                unSyncedInstalltionReports.add(installationReportModel);
            }
        }
        String basePath = FileUtils.getReportPath("SyncReport", System.currentTimeMillis());

        //process SyncedInstallationReports
        String syncedReportPath = basePath + "syncedReport.csv";
        FileUtils.writeInstllationReports(syncedInstalltionReports, syncedReportPath);
        //process UnSynced installation form
        String unSyncedReportPath = basePath + "unSyncedReport.csv";
        FileUtils.writeInstllationReports(unSyncedInstalltionReports, unSyncedReportPath);
        //process Error installation report
        String errorReportpath = basePath + "errorReport.csv";
        FileUtils.writeInstllationReports(errorInstalltionReports, errorReportpath);
        String zippedPath = basePath + ".zip";
        FileUtils.zipFiles(zippedPath, basePath);
        String recipients = properties.getProperty("email.sync.recipients");
        String fileName = properties.getProperty("com.sync.report.fileName");
        String subject = properties.getProperty("com.sync.report.subject");
        String msgBody = "Please find attached the csv with the today's sync data. \n \n";
        FileUtils.sendSlvSyncReport(zippedPath, recipients, subject, fileName, msgBody);
    }
}
