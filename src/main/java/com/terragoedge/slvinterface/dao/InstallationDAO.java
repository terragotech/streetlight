package com.terragoedge.slvinterface.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.terragoedge.automation.Constant;
import com.terragoedge.slvinterface.model.DailyReportEntity;
import com.terragoedge.slvinterface.model.InstallationReportModel;
import com.terragoedge.slvinterface.model.SlvData;
import com.terragoedge.slvinterface.model.SlvDataPrevious;
import com.terragoedge.slvinterface.utils.Constants;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class InstallationDAO {
    private Dao<SlvData, String> slvDataDao;
    private Dao<InstallationReportModel, String> installationDao;
    private Dao<SlvDataPrevious, String> slvDataPreviousDao;
    private Dao<DailyReportEntity, String> daiyReportDao;
    private static final Logger logger = Logger.getLogger(InstallationDAO.class);

    public InstallationDAO() {
        ConnectionSource connectionSource = DBConnection.getInstance();
        try {
            slvDataDao = DaoManager.createDao(connectionSource, SlvData.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            installationDao = DaoManager.createDao(connectionSource, InstallationReportModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            slvDataPreviousDao = DaoManager.createDao(connectionSource, SlvDataPrevious.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            daiyReportDao = DaoManager.createDao(connectionSource, DailyReportEntity.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<InstallationReportModel> getInstallationReports() {
        try {
            return installationDao.queryBuilder().query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public SlvData checkMacAddressExistInSlv(InstallationReportModel installationReportModel) {

        SlvData slvData = null;
        try {
            if (installationReportModel.getNewNodeMacAddressNR() != null && installationReportModel.getNewNodeMacAddressNR().startsWith("00135")) {
                slvData = slvDataDao.queryBuilder().where().eq("rn_new_macaddress", installationReportModel.getNewNodeMacAddressNR()).queryForFirst();
            } else if (installationReportModel.getNewNodeMacAddressNFR() != null && installationReportModel.getNewNodeMacAddressNFR().startsWith("00135")) {
                slvData = slvDataDao.queryBuilder().where().eq("nfr_new_macaddress", installationReportModel.getNewNodeMacAddressNFR()).queryForFirst();
            } else if (installationReportModel.getNodeMacAddressNew() != null && installationReportModel.getNodeMacAddressNew().startsWith("00135")) {
                slvData = slvDataDao.queryBuilder().where().eq("node_mac_address", installationReportModel.getNodeMacAddressNew()).queryForFirst();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return slvData;
        //  return (slvData != null) ? true : false;
    }

}
