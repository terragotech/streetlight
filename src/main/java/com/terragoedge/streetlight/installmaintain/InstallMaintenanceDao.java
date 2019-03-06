package com.terragoedge.streetlight.installmaintain;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.streetlight.dao.FormData;
import com.terragoedge.streetlight.dao.NoteData;
import com.terragoedge.streetlight.dao.UtilDao;
import com.terragoedge.streetlight.installmaintain.json.Config;
import com.terragoedge.streetlight.installmaintain.json.Ids;
import com.terragoedge.streetlight.installmaintain.json.Prop;
import com.terragoedge.streetlight.installmaintain.utills.Utils;
import com.terragoedge.streetlight.service.StreetlightChicagoService;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
            today = Utils.getDate(DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay().getMillis());
            long startTime = DateTime.now(DateTimeZone.UTC).minusDays(1).withTimeAtStartOfDay().getMillis();
            yesterday = Utils.getDate(startTime);
            logger.info("start time:"+startTime);
            logger.info("readable fromat time:"+Utils.getDateTime(startTime));
            queryResponse = queryStatement.executeQuery("select noteguid,parentnoteid,createddatetime,noteid,createdby,locationdescription,title,groupname,ST_X(geometry::geometry) as lat, ST_Y(geometry::geometry) as lng from edgenoteview where title in (select distinct title from edgenoteview where createddatetime >="+ startTime +" ) and createddatetime>="+startTime+" and iscurrent = true and isdeleted = false;");
//            queryResponse = queryStatement.executeQuery("select noteguid,parentnoteid,createddatetime,noteid,createdby,locationdescription,title,groupname,ST_X(geometry::geometry) as lat, ST_Y(geometry::geometry) as lng from edgenoteview where title in (select distinct title from edgenoteview where noteguid='88e9b5ef-7793-432b-ae88-d5d593f4abe3' ) and iscurrent = true and isdeleted = false;");
            logger.info("query response executed");
            int i = 0;
            while (queryResponse.next()) {
                i++;
                String noteGuid = queryResponse.getString("noteguid");
                String parentNoteId =  queryResponse.getString("parentnoteid");
                logger.info("noteguid: "+noteGuid);
                logger.info("parentNoteId: "+parentNoteId);
                long createddatetime = queryResponse.getLong("createddatetime");
                long noteid = queryResponse.getLong("noteid");
                String createdBy = queryResponse.getString("createdby");
                String locationDescription = queryResponse.getString("locationdescription");
                String title = queryResponse.getString("title");
                String groupname = queryResponse.getString("groupname");
                double lat = queryResponse.getDouble("lat");
                double lng = queryResponse.getDouble("lng");
                NoteData parentNoteData = getNoteData(locationDescription,title,createdBy,noteGuid,groupname,lat,lng,noteid,createddatetime);

                InstallMaintenanceModel newInstallMaintenanceModel = null;
                InstallMaintenanceModel replaceNoteMaintenanceModel = null;
               List<FormData> formDatas = getCurrentNoteDetails(noteGuid);
               logger.info("current note forms count: "+formDatas.size());
                boolean isCsvWritten = false;

                List<NoteData> noteDatas = getChildNotes(parentNoteId);
                logger.info("child notes count: "+noteDatas.size());
                for (NoteData noteData : noteDatas) {
                    if (!noteData.getNoteGuid().equals(noteGuid)) {
                        boolean isProcessed = processForm(noteData,parentNoteData,formDatas,newInstallMaintenanceModel,replaceNoteMaintenanceModel,stringBuilder,isCsvWritten);
                        if(isProcessed){
                            break;
                        }else {
                            parentNoteData = noteData;
                        }
                    }
                }

               if(!isCsvWritten && (newInstallMaintenanceModel != null || replaceNoteMaintenanceModel != null)){
                   InstallMaintenanceModel oldInstallMaintenanceModel = new InstallMaintenanceModel();
                   if(newInstallMaintenanceModel != null){
                       oldInstallMaintenanceModel.setMacAddress(newInstallMaintenanceModel.getMacAddress());
                       oldInstallMaintenanceModel.setFixtureQRScan(newInstallMaintenanceModel.getFixtureQRScan());
                   }
                   if(replaceNoteMaintenanceModel != null){
                       oldInstallMaintenanceModel.setExMacAddressRN(replaceNoteMaintenanceModel.getExMacAddressRN());
                       oldInstallMaintenanceModel.setMacAddressRN(replaceNoteMaintenanceModel.getMacAddressRN());
                   }
                   logger.info("csv written for old install or replace node form template");
                   updateCSV(stringBuilder, parentNoteData, oldInstallMaintenanceModel);
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

    private boolean processCSV(String formTemplateGuid,InstallMaintenanceModel newInstallMaintenanceModel, InstallMaintenanceModel replaceNoteMaintenanceModel,InstallMaintenanceModel installMaintenanceModel,NoteData noteData,FormData formData,StringBuilder stringBuilder){
        if(formTemplateGuid.equals("0ea4f5d4-0a17-4a17-ba8f-600de1e2515f")){// new installation form template
            newInstallMaintenanceModel = getInstallMaintenanceModel(formData.getFormDef(), formTemplateGuid);
        }else if(formTemplateGuid.equals("606fb4ca-40a4-466b-ac00-7c0434f82bfa")){// replace node form template
            replaceNoteMaintenanceModel = getInstallMaintenanceModel(formData.getFormDef(), formTemplateGuid);
        }else {
            logger.info("csv written");
            updateCSV(stringBuilder, noteData, installMaintenanceModel);
            return true;
        }
        return false;
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

    public List<NoteData> getChildNotes(String parentNoteGuid){
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        List<NoteData> noteDatas = new ArrayList<>();
        try{
            queryStatement = connection.createStatement();
            queryResponse = queryStatement.executeQuery("select noteguid,parentnoteid,createddatetime,noteid,createdby,locationdescription,title,groupname,ST_X(geometry::geometry) as lat, ST_Y(geometry::geometry) as lng from edgenoteview where parentnoteid='"+parentNoteGuid+"' or noteguid='"+parentNoteGuid+"' order by createddatetime desc");
            while (queryResponse.next()) {
                String noteGuid = queryResponse.getString("noteguid");
                String parentNoteId =  queryResponse.getString("parentnoteid");
                logger.info("noteguid: "+noteGuid);
                logger.info("parentNoteId: "+parentNoteId);
                long createddatetime = queryResponse.getLong("createddatetime");
                long noteid = queryResponse.getLong("noteid");
                String createdBy = queryResponse.getString("createdby");
                String locationDescription = queryResponse.getString("locationdescription");
                String title = queryResponse.getString("title");
                String groupname = queryResponse.getString("groupname");
                double lat = queryResponse.getDouble("lat");
                double lng = queryResponse.getDouble("lng");
                NoteData noteData = getNoteData(locationDescription,title,createdBy,noteGuid,groupname,lat,lng,noteid,createddatetime);
                noteDatas.add(noteData);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
        return noteDatas;
    }

    private InstallMaintenanceModel getInstallMaintenanceModel(String formDef, String formTemplateGuid){
        InstallMaintenanceModel installMaintenanceModel = new InstallMaintenanceModel();
        List<EdgeFormData> edgeFormDatas = gson.fromJson(formDef,new TypeToken<List<EdgeFormData>>(){}.getType());
        for(Config config : configs){
            installMaintenanceModel.setInstallStatus(getValue(config.getInstallStatus(),edgeFormDatas));
            installMaintenanceModel.setProposedContext(getValue(config.getProposedContext(),edgeFormDatas));
            if(config.getFormTemplateGuid().equals(formTemplateGuid)){
                List<Prop> props = config.getProps();
                for(Prop prop : props){
                    Ids idsList = prop.getIds();
                    InstallMaintenanceEnum type =  prop.getType();
                    switch (type){
                        case RF:
                            installMaintenanceModel.setFixtureQRScanRF(getValue(idsList.getFix(),edgeFormDatas));
                            installMaintenanceModel.setExFixtureQRScanRF(getValue(idsList.getExFix(),edgeFormDatas));
                            break;
                        case NEW:
                            installMaintenanceModel.setMacAddress(getValue(idsList.getMac(),edgeFormDatas));
                            installMaintenanceModel.setFixtureQRScan(getValue(idsList.getFix(),edgeFormDatas));
                            break;
                        case RN:
                            installMaintenanceModel.setMacAddressRN(getValue(idsList.getMac(),edgeFormDatas));
                            installMaintenanceModel.setExMacAddressRN(getValue(idsList.getExMac(),edgeFormDatas));
                            break;
                        case RNF:
                            installMaintenanceModel.setMacAddressRNF(getValue(idsList.getMac(),edgeFormDatas));
                            installMaintenanceModel.setExMacAddressRNF(getValue(idsList.getExMac(),edgeFormDatas));
                            installMaintenanceModel.setFixtureQRScanRNF(getValue(idsList.getFix(),edgeFormDatas));
                            installMaintenanceModel.setExFixtureQRScanRNF(getValue(idsList.getExFix(),edgeFormDatas));
                            break;
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
        if(pos > -1){
            return edgeFormDatas.get(pos).getValue();
        }
        return null;
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
    private boolean processForm(NoteData noteData,NoteData parentNoteData,List<FormData> formDatas,InstallMaintenanceModel newInstallMaintenanceModel, InstallMaintenanceModel replaceNoteMaintenanceModel,StringBuilder stringBuilder,boolean isCsvWritten){
        String childCreatedDate = Utils.getDate(noteData.getCreatedDateTime());
        String createdDate = Utils.getDate(parentNoteData.getCreatedDateTime());
        List<FormData> childFormDatas = getCurrentNoteDetails(noteData.getNoteGuid());
        logger.info("child note: "+noteData.getNoteGuid());
        logger.info("child note forms count: "+childFormDatas.size());

        for(FormData formData : formDatas) {
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
        }
        return false;
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
