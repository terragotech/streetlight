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

    public InstallMaintenanceDao() {
        gson = new Gson();
    }

    public void doProcess(){
        File file = new File("./daily_install_report_"+Utils.getDate(System.currentTimeMillis()));
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        List<Config> configs = new ArrayList<Config>();
        try{
            StringBuilder stringBuilder = new StringBuilder();
            StreetlightChicagoService.populateNotesHeader(stringBuilder);
            queryResponse = queryStatement.executeQuery("select noteguid,parentnoteid,createddatetime,noteid,createdby,locationdescription,title,groupname,ST_X(geometry::geometry) as lat, ST_Y(geometry::geometry) as lng from edgenoteview where title in (select distinct title from edgenoteview where createddatetime >= ) where iscurrent = true and isdeleted = false;");
            while (queryResponse.next()) {
               NoteData parentNoteData = new NoteData();
               String noteGuid = queryResponse.getString("noteguid");
               String parentNoteId =  queryResponse.getString("parentnoteid");
               long createddatetime = queryResponse.getLong("createddatetime");
               String createdDate = Utils.getDate(createddatetime);
                long noteid = queryResponse.getLong("noteid");
                String createdBy = queryResponse.getString("createdby");
                String locationDescription = queryResponse.getString("locationdescription");
                String[] locations = locationDescription.split("\\|");

                if (locations.length == 2) {
                    locationDescription = locations[0];
                }
                String title = queryResponse.getString("title");
                String groupname = queryResponse.getString("groupname");
                double lat = queryResponse.getDouble("lat");
                double lng = queryResponse.getDouble("lng");
                parentNoteData.setCreatedDateTime(createddatetime);
                parentNoteData.setCreatedBy(createdBy);
                parentNoteData.setNoteGuid(noteGuid);
                parentNoteData.setDescription(locationDescription);
                parentNoteData.setGroupName(groupname);
                parentNoteData.setLat(String.valueOf(lat));
                parentNoteData.setLng(String.valueOf(lng));
                parentNoteData.setTitle(title);
                parentNoteData.setNoteId(noteid);


               List<FormData> formDatas = getCurrentNoteDetails(noteGuid,configs);
               for(FormData formData : formDatas){
                       InstallMaintenanceModel installMaintenanceModel = getInstallMaintenanceModel(configs, formData.getFormDef(), formData.getFormTemplateGuid());
                       List<NoteData> noteDatas = getChildNotes(parentNoteId);
                       for (NoteData noteData : noteDatas) {
                           if (!noteData.getNoteGuid().equals(noteGuid)) {
                               String childCreatedDate = Utils.getDate(noteData.getCreatedDateTime());
                               List<FormData> childFormDatas = getCurrentNoteDetails(noteGuid,configs);
                               for(FormData formData1 : childFormDatas){
                                   if(formData1.getFormTemplateGuid().equals(formData.getFormTemplateGuid())) {
                                       InstallMaintenanceModel childInstallMaintenanceModel = getInstallMaintenanceModel(configs, formData1.getFormDef(), formData1.getFormTemplateGuid());
                                       if(installMaintenanceModel.equals(childInstallMaintenanceModel) && !createdDate.equals(childCreatedDate)){
                                            break;
                                       }else if(!installMaintenanceModel.equals(childInstallMaintenanceModel)) {
                                           stringBuilder.append(parentNoteData.getTitle());
                                           stringBuilder.append(",");
                                           stringBuilder.append(installMaintenanceModel.getMacAddress());
                                           stringBuilder.append(",");
                                           stringBuilder.append(parentNoteData.getCreatedBy());
                                           stringBuilder.append(",");
                                           stringBuilder.append(installMaintenanceModel.getFixtureQRScan());
                                           stringBuilder.append(",");
                                           stringBuilder.append(parentNoteData.getTitle());
                                           stringBuilder.append(",");
                                           stringBuilder.append(installMaintenanceModel.getProposedContext());
                                           stringBuilder.append(",");
                                           stringBuilder.append(parentNoteData.getLat());
                                           stringBuilder.append(",");
                                           stringBuilder.append(parentNoteData.getLng());
                                           stringBuilder.append(",");
                                           stringBuilder.append(Utils.getDateTime(parentNoteData.getCreatedDateTime()));
                                           stringBuilder.append(",");
                                           stringBuilder.append(isReplace(installMaintenanceModel.getMacAddressRNF(),installMaintenanceModel.getMacAddressRN()));
                                           stringBuilder.append(",");
                                           stringBuilder.append(validateTwoString(installMaintenanceModel.getExMacAddressRNF(),installMaintenanceModel.getExMacAddressRN()));
                                           stringBuilder.append(",");
                                           stringBuilder.append(validateTwoString(installMaintenanceModel.getMacAddressRNF(),installMaintenanceModel.getMacAddressRN()));
                                           stringBuilder.append("\n");
                                           break;
                                       }
                                   }
                               }
                           }
                       }
               }
            }
            StreetlightChicagoService.logData(stringBuilder.toString(),"daily_install_report_"+Utils.getDate(System.currentTimeMillis())+".csv");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
    }


    public List<FormData> getCurrentNoteDetails(String noteGuid,List<Config> configs){
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
                if(checkFormTemplateInConfig(formTemplateGuid,configs)) {
                    FormData formData = new FormData();
                    formData.setFormDef(formDef);
                    formData.setFormTemplateGuid(formTemplateGuid);
                    formDatas.add(formData);
                }
            }
        }catch (Exception e){
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
            queryResponse = queryStatement.executeQuery("select noteguid,createddatetime,createdby from edgenote where parentnoteid='"+parentNoteGuid+"' order by createddatetime desc");
            while (queryResponse.next()) {
                NoteData noteData = new NoteData();
                String noteGuid = queryResponse.getString("noteguid");
                noteData.setNoteGuid(noteGuid);
                noteData.setCreatedBy(queryResponse.getString("createdby"));
                noteData.setCreatedDateTime(queryResponse.getLong("createddatetime"));
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

    private InstallMaintenanceModel getInstallMaintenanceModel(List<Config> configList, String formDef, String formTemplateGuid){
        InstallMaintenanceModel installMaintenanceModel = new InstallMaintenanceModel();
        List<EdgeFormData> edgeFormDatas = gson.fromJson(formDef,new TypeToken<List<EdgeFormData>>(){}.getType());
        for(Config config : configList){
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
                            installMaintenanceModel.setExMacAddressRN(getValue(idsList.getExMax(),edgeFormDatas));
                            break;
                        case RNF:
                            installMaintenanceModel.setMacAddressRNF(getValue(idsList.getMac(),edgeFormDatas));
                            installMaintenanceModel.setExMacAddressRNF(getValue(idsList.getExMax(),edgeFormDatas));
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

    private boolean checkFormTemplateInConfig(String formTemplateGuid,List<Config> configs){
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
}
