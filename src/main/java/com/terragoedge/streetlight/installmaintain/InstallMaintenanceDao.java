package com.terragoedge.streetlight.installmaintain;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.streetlight.dao.FormData;
import com.terragoedge.streetlight.dao.NoteData;
import com.terragoedge.streetlight.dao.UtilDao;
import com.terragoedge.streetlight.installmaintain.json.Config;
import com.terragoedge.streetlight.installmaintain.json.CsvStatus;
import com.terragoedge.streetlight.installmaintain.json.Ids;
import com.terragoedge.streetlight.installmaintain.json.Prop;
import com.terragoedge.streetlight.installmaintain.utills.Utils;
import com.terragoedge.streetlight.service.StreetlightChicagoService;
import org.apache.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class InstallMaintenanceDao extends UtilDao {
    private Gson gson;
    private InstallMaintenanceService installMaintenanceService;
    private List<Config> configs = new ArrayList<>();
    private String today = "";
    private String yesterday = "";

    public InstallMaintenanceDao() {
        gson = new Gson();
        installMaintenanceService = new InstallMaintenanceService();
        configs = installMaintenanceService.getConfigList();
    }
    private Logger logger = Logger.getLogger(InstallMaintenanceDao.class);

    public void doProcess(){
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        logger.info("configs: "+gson.toJson(configs));
        try{
            queryStatement = connection.createStatement();
            StringBuilder stringBuilder = new StringBuilder();
            StreetlightChicagoService.populateNotesHeader(stringBuilder);
            today = Utils.getDate(1552712400000L);
//            today = Utils.getDate(DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay().getMillis());
            long startTime = 1552626000000L;
//            long startTime = DateTime.now(DateTimeZone.UTC).minusDays(1).withTimeAtStartOfDay().getMillis();
            yesterday = Utils.getDate(startTime);
            logger.info("start time:"+startTime);
            logger.info("readable fromat time:"+Utils.getDateTime(startTime));
            queryResponse = queryStatement.executeQuery("select title,noteguid,parentnoteid,createddatetime from edgenote where iscurrent = true and isdeleted = false and noteguid = 'e98c84f1-d9c1-403b-b33e-07e018b7fd62'");
//            queryResponse = queryStatement.executeQuery("select noteguid,parentnoteid,createddatetime,noteid,createdby,locationdescription,title,groupname,ST_X(geometry::geometry) as lat, ST_Y(geometry::geometry) as lng from edgenoteview where title in (select distinct title from edgenoteview where noteguid='88e9b5ef-7793-432b-ae88-d5d593f4abe3' ) and iscurrent = true and isdeleted = false;");
            logger.info("query response executed");
            int i = 0;
            while (queryResponse.next()) {
                i++;
                String currentNoteGuid = queryResponse.getString("noteguid");
                Long currentNoteDateTime = queryResponse.getLong("noteguid");
                NoteData currentNoteData = new NoteData();
                currentNoteData.setNoteGuid(currentNoteGuid);
                currentNoteData.setCreatedDateTime(currentNoteDateTime);

                String parentNoteId =  queryResponse.getString("parentnoteid");
                logger.info("currentNoteGuid: "+currentNoteGuid);
                logger.info("parentNoteId: "+parentNoteId);

                List<FormData> formDatas = getCurrentNoteDetails(currentNoteGuid);
                logger.info("current note forms count: "+formDatas.size());
                InstallMaintenanceModel currentNoteInstallForm = getInstallMaintenanceModel(formDatas);
                currentNoteData.setInstallMaintenanceModel(currentNoteInstallForm);


                List<NoteData> allRevisionsNotes = getAllRevisionsNoteGuids(parentNoteId,currentNoteGuid);


                logger.info("All Revisions notes Count: "+allRevisionsNotes.size());
                for (NoteData revisionNote : allRevisionsNotes) {
                    List<FormData> revisionNoteInstallForm = getCurrentNoteDetails(revisionNote.getNoteGuid());
                    logger.info("Revision Note: "+revisionNote.getNoteGuid());
                    logger.info("child note forms count: "+revisionNoteInstallForm.size());
                    InstallMaintenanceModel previousInstallForm = getInstallMaintenanceModel(revisionNoteInstallForm);
                    revisionNote.setInstallMaintenanceModel(previousInstallForm);

                    CsvStatus csvStatus = processForm(currentNoteData,revisionNote);
                    if(csvStatus.isWritten()){
                        break;
                    }
                    if(csvStatus.isChangeParent()){
                        parentFormData = childFormData;
                        parentNoteData = noteData;
                    }
                }
               logger.info("Processed item: "+i);
            }
            StreetlightChicagoService.logData(stringBuilder.toString(),"daily_install_report_"+Utils.getDate(System.currentTimeMillis())+".csv");
            logger.info("daily install report csv file created!");
        }catch (Exception e){
            logger.error("Error: "+e.getMessage());
            e.printStackTrace();
        }finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
    }

    private void processCSV(InstallMaintenanceModel installMaintenanceModel,NoteData noteData,StringBuilder stringBuilder){
            logger.info("csv written");
            updateCSV(stringBuilder, noteData, installMaintenanceModel);
    }

    public List<FormData> getCurrentNoteDetails(String noteGuid){
        List<FormData> formDatas = new ArrayList<>();
        PreparedStatement queryStatement = null;
        ResultSet queryResponse = null;
        try{
            queryStatement = connection.prepareStatement("select formdef,formtemplateguid from edgeform where edgenoteentity_noteid in (select noteid from edgenote where noteguid = ?)");
            queryStatement.setString(1,noteGuid);
            queryResponse = queryStatement.executeQuery();
            while (queryResponse.next()) {
                String formDef = queryResponse.getString("formdef");
                String formTemplateGuid = queryResponse.getString("formtemplateguid");
                if(checkFormTemplateInConfig(formTemplateGuid)) {
                    FormData formData = new FormData();
                    formData.setFormDef(formDef);
                    formData.setFormTemplateGuid(formTemplateGuid);
                    formDatas.add(formData);
                }
            }
        }catch (Exception e){
            logger.error("error in getting forms: "+e.getMessage());
            e.printStackTrace();
        }finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
        return formDatas;
    }

    public List<NoteData> getAllRevisionsNoteGuids(String parentNoteGuid,String currentNoteGuid){
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        List<NoteData> allRevisionsNoteGuid = new ArrayList<>();
        try{
            queryStatement = connection.createStatement();
            queryResponse = queryStatement.executeQuery("select noteguid,createddatetime from edgenote where parentnoteid='"+parentNoteGuid+"' or noteguid='"+parentNoteGuid+"' order by createddatetime desc");
            while (queryResponse.next()) {
                String noteGuid = queryResponse.getString("noteguid");
                Long createdDateTime = queryResponse.getLong("createddatetime");
                if(!currentNoteGuid.equals(noteGuid)){
                    NoteData noteData = new NoteData();
                    noteData.setNoteGuid(noteGuid);
                    noteData.setCreatedDateTime(createdDateTime);
                    allRevisionsNoteGuid.add(noteData);
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
        return allRevisionsNoteGuid;
    }

    private InstallMaintenanceModel getInstallMaintenanceModel(List<FormData> formDatas) {
        InstallMaintenanceModel installMaintenanceModel = new InstallMaintenanceModel();
        for (FormData formData : formDatas){
            String formDef = formData.getFormDef();
            List<EdgeFormData> edgeFormDatas = gson.fromJson(formDef, new TypeToken<List<EdgeFormData>>() {
            }.getType());
        for (Config config : configs) {
            if (config.getFormTemplateGuid().equals(formData.getFormTemplateGuid())) {
                installMaintenanceModel.setInstallStatus(getValue(config.getInstallStatus(), edgeFormDatas));
                installMaintenanceModel.setProposedContext(getValue(config.getProposedContext(), edgeFormDatas));
                List<Prop> props = config.getProps();
                logger.info("config: "+gson.toJson(config));
                for (Prop prop : props) {
                    logger.info("prop: "+gson.toJson(prop));
                    Ids idsList = prop.getIds();
                    InstallMaintenanceEnum type = prop.getType();
                    logger.info("type:"+type.toString());
                    switch (type) {
                        case RF:
                            installMaintenanceModel.setFixtureQRScanRF(setValue(installMaintenanceModel.getFixtureQRScanRF(),getValue(idsList.getFix(), edgeFormDatas)));
                            installMaintenanceModel.setExFixtureQRScanRF(setValue(installMaintenanceModel.getExFixtureQRScanRF(),getValue(idsList.getExFix(), edgeFormDatas)));
                            break;
                        case NEW:
                            installMaintenanceModel.setMacAddress(setValue(installMaintenanceModel.getMacAddress(),getValue(idsList.getMac(), edgeFormDatas)));
                            installMaintenanceModel.setFixtureQRScan(setValue(installMaintenanceModel.getFixtureQRScan(),getValue(idsList.getFix(), edgeFormDatas)));
                            break;
                        case RN:
                            installMaintenanceModel.setMacAddressRN(setValue(installMaintenanceModel.getMacAddressRN(),getValue(idsList.getMac(), edgeFormDatas)));
                            installMaintenanceModel.setExMacAddressRN(setValue(installMaintenanceModel.getExMacAddressRN(),getValue(idsList.getExMac(), edgeFormDatas)));
                            break;
                        case RNF:
                            installMaintenanceModel.setMacAddressRNF(setValue(installMaintenanceModel.getMacAddressRNF(),getValue(idsList.getMac(), edgeFormDatas)));
                            installMaintenanceModel.setExMacAddressRNF(setValue(installMaintenanceModel.getExMacAddressRNF(),getValue(idsList.getExMac(), edgeFormDatas)));
                            installMaintenanceModel.setFixtureQRScanRNF(setValue(installMaintenanceModel.getFixtureQRScanRNF(),getValue(idsList.getFix(), edgeFormDatas)));
                            installMaintenanceModel.setExFixtureQRScanRNF(setValue(installMaintenanceModel.getExFixtureQRScanRNF(),getValue(idsList.getExFix(), edgeFormDatas)));
                            break;
                    }
                }
            }
        }
    }
        return installMaintenanceModel;
    }

    private String getValue(int id,List<EdgeFormData> edgeFormDatas){
        EdgeFormData edgeFormData = new EdgeFormData();
        edgeFormData.setId(id);
        int pos = edgeFormDatas.indexOf(edgeFormData);
        String value = "";
        if(pos > -1){
            value = edgeFormDatas.get(pos).getValue();
        }
        return value;
    }

    private String setValue(String oldValue, String newValue){
        if(oldValue == null || oldValue.equals("")){
            return newValue;
        }else{
            if(newValue == null || newValue.equals("")){
                return oldValue;
            }else{
                return newValue;
            }
        }
    }

    private boolean checkFormTemplateInConfig(String formTemplateGuid){
        Config config = new Config();
        config.setFormTemplateGuid(formTemplateGuid);
        return configs.contains(config);
    }
    private String isReplace(String macRNF,String macRN){
        if ((macRNF != null && !macRNF.equals(""))  || (macRN != null && !macRN.equals(""))){
            return "Yes";
        } else {
            return "No";
        }
    }

    private String validateTwoString(String txt,String txt1){
        if(txt == null && txt1 == null){
            return "";
        }else if(txt == null && txt1 != null){
            return txt1;
        }else if(txt1 == null && txt != null){
            return txt;
        }else{
            return txt;
        }
    }

    private void updateCSV(StringBuilder stringBuilder,NoteData noteData,InstallMaintenanceModel installMaintenanceModel){
        stringBuilder.append(noteData.getTitle());
        stringBuilder.append(",");
        stringBuilder.append(installMaintenanceModel.getMacAddress());
        stringBuilder.append(",");
        stringBuilder.append(noteData.getCreatedBy());
        stringBuilder.append(",\"");
        stringBuilder.append(installMaintenanceModel.getFixtureQRScan());
        stringBuilder.append("\",");
        stringBuilder.append(noteData.getFixtureType());
        stringBuilder.append(",\"");
        stringBuilder.append(installMaintenanceModel.getProposedContext());
        stringBuilder.append("\",");
        stringBuilder.append(noteData.getLat());
        stringBuilder.append(",");
        stringBuilder.append(noteData.getLng());
        stringBuilder.append(",");
        stringBuilder.append(Utils.getDateTime(noteData.getCreatedDateTime()));
        stringBuilder.append(",");
        stringBuilder.append(isReplace(installMaintenanceModel.getMacAddressRNF(),installMaintenanceModel.getMacAddressRN()));
        stringBuilder.append(",");
        stringBuilder.append(validateTwoString(installMaintenanceModel.getExMacAddressRNF(),installMaintenanceModel.getExMacAddressRN()));
        stringBuilder.append(",");
        stringBuilder.append(validateTwoString(installMaintenanceModel.getMacAddressRNF(),installMaintenanceModel.getMacAddressRN()));
        stringBuilder.append("\n");
    }
    private CsvStatus processForm(NoteData currentNoteData,NoteData previousNoteData){
        if(currentNoteData.getInstallMaintenanceModel().equals(previousNoteData.getInstallMaintenanceModel()) && previousNoteData){

        }

        CsvStatus csvStatus = new CsvStatus();
        boolean changeParent = false;
        boolean isWritten = false;
        String childCreatedDate = Utils.getDate(noteData.getCreatedDateTime());
        String createdDate = Utils.getDate(parentNoteData.getCreatedDateTime());
        if(parentFormData.equals(childFormData) && !createdDate.equals(childCreatedDate)){
            if(!childCreatedDate.equals(today) && !childCreatedDate.equals(yesterday)) {
                logger.info("parent and child form equal and create date changed so breaking...");
            }else{
                changeParent = true;
            }
        } else if(!parentFormData.equals(childFormData)){
            logger.info("parent and child form not equal. so going to write it in csv");
            logger.info("**************************************");
            logger.info("parent data"+gson.toJson(parentFormData));
            logger.info("child data"+gson.toJson(childFormData));
            logger.info("**************************************");
            processCSV(parentFormData, parentNoteData, stringBuilder);
            isWritten = true;
        }else{
            changeParent = true;
        }
        csvStatus.setChangeParent(changeParent);
        csvStatus.setWritten(isWritten);
        return csvStatus;
        /*for(FormData formData : formDatas) {
            String formTemplateGuid = formData.getFormTemplateGuid();
            if (checkFormTemplateInConfig(formTemplateGuid)) {
                InstallMaintenanceModel installMaintenanceModel = getInstallMaintenanceModel(formData.getFormDef(), formTemplateGuid);
                for (FormData formData1 : childFormDatas) {
                    if (formData1.getFormTemplateGuid().equals(formData.getFormTemplateGuid())) {
                        InstallMaintenanceModel childInstallMaintenanceModel = getInstallMaintenanceModel(formData1.getFormDef(), formData1.getFormTemplateGuid());
                        logger.info("parent model: "+gson.toJson(installMaintenanceModel));
                        logger.info("parent date: "+createdDate);
                        logger.info("child model: "+gson.toJson(childInstallMaintenanceModel));
                        logger.info("child date: "+childCreatedDate);
                        logger.info("is equal: "+installMaintenanceModel.equals(childInstallMaintenanceModel));
                        if (installMaintenanceModel.equals(childInstallMaintenanceModel) && !createdDate.equals(childCreatedDate)) {
                            if(!childCreatedDate.equals(today) && !childCreatedDate.equals(yesterday)) {
                                logger.info("parent and child form equal and create date changed so breaking...");
                                return true;
                            }
                        } else if (!installMaintenanceModel.equals(childInstallMaintenanceModel)) {
                            logger.info("parent and child form not equal. so going to write it in csv");
                            isCsvWritten = processCSV(formTemplateGuid, newInstallMaintenanceModel, replaceNoteMaintenanceModel, installMaintenanceModel, parentNoteData, formData, stringBuilder);
                            if (isCsvWritten) {
                                return true;
                            }
                        }
                    }
                }
            }
        }*/
    }

    private NoteData getNoteData(String locationDescription,String title,String createdBy,String noteGuid,String groupname,double lat,double lng,long noteid,long createddatetime){
        NoteData noteData = new NoteData();
        String fixtureType = "";
        String[] locations = locationDescription.split("\\|");

        if (locations.length == 2) {
            locationDescription = locations[0];
            fixtureType = locations[1];
        }

        noteData.setCreatedDateTime(createddatetime);
        noteData.setCreatedBy(createdBy);
        noteData.setNoteGuid(noteGuid);
        noteData.setDescription(locationDescription);
        noteData.setGroupName(groupname);
        noteData.setLat(String.valueOf(lat));
        noteData.setLng(String.valueOf(lng));
        noteData.setTitle(title);
        noteData.setNoteId(noteid);
        noteData.setFixtureType(fixtureType);
        return noteData;
    }
}
