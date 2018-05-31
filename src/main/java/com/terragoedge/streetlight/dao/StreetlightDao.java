package com.terragoedge.streetlight.dao;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.edgeserver.InspectionsReport;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.StreetlightDaoConnection;
import com.terragoedge.streetlight.service.DailyReportCSV;

public class StreetlightDao extends UtilDao {

	static final Logger logger = Logger.getLogger(StreetlightDao.class);

	public StreetlightDao() {
		super();
	}
	
	
	
	private String generateSQL(){
		String customDate = PropertiesReader.getProperties().getProperty("amerescousa.custom.date");
		if(customDate != null && customDate.equals("true")){
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("select noteid,createddatetime, edgenoteview.createdby,description,title,groupname,ST_X(geometry::geometry) as lat, ST_Y(geometry::geometry) as lng  from edgenoteview where  edgenoteview.isdeleted = false and edgenoteview.iscurrent = true ");
			String startOfDay = PropertiesReader.getProperties().getProperty("amerescousa.report.from");
			if(startOfDay != null && !startOfDay.isEmpty()){
				stringBuilder.append("and edgenoteview.createddatetime >= ");
				stringBuilder.append(startOfDay);
			}
			
			String endOfDay = PropertiesReader.getProperties().getProperty("amerescousa.report.to");
			if(endOfDay != null && !endOfDay.isEmpty()){
				stringBuilder.append(" and edgenoteview.createddatetime <= ");
				stringBuilder.append(endOfDay);
			}
			stringBuilder.append(";");
			return stringBuilder.toString();
		}else{
			Calendar calendar = Calendar.getInstance(Locale.getDefault());
			calendar.set(Calendar.HOUR_OF_DAY, 00);
			calendar.set(Calendar.MINUTE, 00);
			calendar.set(Calendar.SECOND, 00);
			long startOfDay = calendar.getTime().getTime();
            return "select noteid,createddatetime, createdby,description,title,groupname,ST_X(geometry::geometry) as lat, ST_Y(geometry::geometry) as lng  from edgenoteview where  edgenoteview.isdeleted = false and edgenoteview.iscurrent = true  and edgenoteview.createdby != 'admin' and edgenoteview.createddatetime >= "+startOfDay+";";
		}
	}

	private String generateSQLQuery(String formTemplateGuid){
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(Calendar.HOUR_OF_DAY, 00);
        calendar.set(Calendar.MINUTE, 00);
        calendar.set(Calendar.SECOND, 00);
        long startOfDay = calendar.getTime().getTime();
        return  "select notebookname, name,description,edgenoteview.createdby,edgenoteview.groupname,locationdescription, ST_Y(geometry::geometry) Longitude, title,ST_X(geometry::geometry) Latitude, replace(replace(substring(a.formdef from 'Issue Type#(.+?)count'),'\"',''),',','') fieldreport, replace(replace(substring(a.formdef from 'Add Comment#(.+?)count'),'\"',''),',','') addcomment,edgenoteview.createddatetime from edgenoteview , edgenotebook, edgeform a where edgenoteview.notebookid = edgenotebook.notebookid and a.formtemplateguid = '"+formTemplateGuid+"' and a.edgenoteentity_noteid = edgenoteview.noteid and edgenoteview.isdeleted = 'f' and edgenoteview.iscurrent = 't' and edgenoteview.createddatetime >= "+startOfDay+";";
    }
	

	/**
	 * Get List of NoteIds which is assigned to given formtemplate
	 * 
	 * @return
	 */
	public List<DailyReportCSV> getNoteIds() {
		Statement queryStatement = null;
		ResultSet queryResponse = null;
		List<DailyReportCSV> dailyReportCSVs = new ArrayList<>();
		try {
			String sql = generateSQL();
            System.out.println(sql);
			queryStatement = connection.createStatement();
			queryResponse = queryStatement.executeQuery(sql);
			List<NoteData> noteDataList = new ArrayList<>();
			int count = 0;
			while (queryResponse.next()) {
                NoteData noteData = new NoteData();
                count = count + 1;
                System.out.println(count);
                long noteId = queryResponse.getLong("noteid");
                noteData.setNoteId(noteId);

                noteDataList.add(noteData);
                noteData.setDescription(queryResponse.getString("description"));
                noteData.setGroupName(queryResponse.getString("groupname"));
                noteData.setTitle(queryResponse.getString("title"));
                noteData.setCreatedBy(queryResponse.getString("createdby"));
                noteData.setCreatedDateTime(queryResponse.getLong("createddatetime"));
                noteData.setLat(String.valueOf(queryResponse.getDouble("lat")));
                noteData.setLng(String.valueOf(queryResponse.getDouble("lng")));

            }
            System.out.println("Total count "+noteDataList.size());
            int size = noteDataList.size();
			List<Long> noteIdLong = new ArrayList<>();
            for(int i = 0; i < size; i++){
                NoteData noteData =  noteDataList.get(i);
                noteIdLong.add(noteData.getNoteId());
                if((i + 1) % 100 == 0){
                    getFormData(noteIdLong,noteDataList);
                    noteIdLong.clear();
                }
            }
if(noteIdLong.size() > 0){
    getFormData(noteIdLong,noteDataList);
}
            System.out.println("Total count "+noteDataList.size());
            processNoteData(noteDataList,dailyReportCSVs);

		} catch (Exception e) {
		    e.printStackTrace();
			logger.error("Error in getNoteIds", e);
		} finally {
			closeResultSet(queryResponse);
			closeStatement(queryStatement);
		}
		return dailyReportCSVs;
	}


	private void processNoteData(List<NoteData> noteDataList,List<DailyReportCSV> dailyReportCSVs){
       int count = 0;
        for(NoteData noteData : noteDataList){
            count = count + 1;
            logger.info("Current count"+count);
            DailyReportCSV dailyReportCSV = new DailyReportCSV();
            dailyReportCSVs.add(dailyReportCSV);
            dailyReportCSV.setContext(noteData.getDescription());
            dailyReportCSV.setFixtureType(noteData.getGroupName());
            dailyReportCSV.setNoteTitle(noteData.getTitle());
            if(dailyReportCSV.getNoteTitle().contains("Fixture")){
                dailyReportCSV.setQuickNote(true);
            }
            dailyReportCSV.setCreatedBy(noteData.getCreatedBy());
            dailyReportCSV.setCreateddatetime(noteData.getCreatedDateTime());
            dailyReportCSV.setLat(noteData.getLat());
            dailyReportCSV.setLng(noteData.getLng());

            Map<String,List<String>> formDatas = new HashMap<>();
            for(FormData formData : noteData.getFormDataList()){
                String formTemplateGuid = formData.getFormTemplateGuid();
                String formdef = formData.getFormDef();
                List<String> formsList =  formDatas.get(formTemplateGuid);
                if(formsList == null){
                    formsList = new ArrayList<>();
                    formDatas.put(formTemplateGuid,formsList);
                }
                formsList.add(formdef);
            }
            processFormData(formDatas,dailyReportCSV);


        }
    }


    private boolean processFormData(List<String> formsData, DailyReportCSV dailyReportCSV,boolean isInstallMaintenance) {
        if (formsData != null) {
            for (String formDef : formsData) {
                if(isInstallMaintenance){
                    boolean result =  processInstallMaintenanceForm(formDef,dailyReportCSV);
                    if (result) {
                        return result;
                    }
                }else{
                    boolean result = dataLoad(formDef, dailyReportCSV);
                    if (result) {
                        return result;
                    }
                }

            }
        }
        return false;
    }


    private void processFormData(Map<String,List<String>> formDatas,DailyReportCSV dailyReportCSV){

        List<String> installMaintenanceFinal =   formDatas.get("c8acc150-6228-4a27-bc7e-0fabea0e2b93");
        boolean  isDataLoaded =  processFormData(installMaintenanceFinal,dailyReportCSV,true);
        if(isDataLoaded){
            return;
        }

        List<String> installMaintenanceUpdated =   formDatas.get("fa47c708-fb82-4877-938c-992e870ae2a4");
        isDataLoaded =  processFormData(installMaintenanceUpdated,dailyReportCSV,true);
        if(isDataLoaded){
            return;
        }

        List<String> installMaintenanceList =   formDatas.get("8b722347-c3a7-41f4-a8a9-c35dece6f98b");
        isDataLoaded = processFormData(installMaintenanceList,dailyReportCSV,true);
        if(isDataLoaded){
            return;
        }


        List<String> replaceList =   formDatas.get("606fb4ca-40a4-466b-ac00-7c0434f82bfa");
        isDataLoaded =  processFormData(replaceList,dailyReportCSV,false);
        if(isDataLoaded){
            return;
        }




        List<String> newInstallQRScan =   formDatas.get("0ea4f5d4-0a17-4a17-ba8f-600de1e2515f");
        isDataLoaded =  processFormData(newInstallQRScan,dailyReportCSV,false);
        if(isDataLoaded){
            return;
        }



    }

    private boolean processInstallMaintenanceForm(String formDef, DailyReportCSV dailyReportCSV) {
        Type listType = new TypeToken<ArrayList<EdgeFormData>>() {
        }.getType();
        Gson gson = new Gson();
        boolean isDataLoad = false;
        List<EdgeFormData> edgeFormDatas = gson.fromJson(formDef, listType);
        // Action id
        String actionValue = getValue(17, edgeFormDatas);
        if (actionValue != null) {
            switch (actionValue) {
                case "New":
                    String nodeMACAddress = getValue(19, edgeFormDatas);
                    if (nodeMACAddress != null) {
                        isDataLoad = true;
                        dailyReportCSV.setQrCode(nodeMACAddress);
                        checkDupMacAddress(nodeMACAddress, dailyReportCSV, dailyReportCSV.getNoteTitle());
                    }

                    String fixtureQrScan = getValue(20, edgeFormDatas);
                    dailyReportCSV.setFixtureQrScan(fixtureQrScan);
                    break;
                case "Repairs & Outages":
                    String repairOutagesVal = getValue(24, edgeFormDatas);
                    if (repairOutagesVal != null) {
                        isDataLoad = true;
                        dailyReportCSV.setIsReplaceNode("Yes");
                        switch (repairOutagesVal) {
                            case "Replace Node and Fixture":
                                String newNodeMACAddress = getValue(26, edgeFormDatas);
                                if (newNodeMACAddress != null) {
                                    isDataLoad = true;
                                    dailyReportCSV.setNewNodeMACAddress(newNodeMACAddress);
                                    checkDupMacAddress(newNodeMACAddress, dailyReportCSV, dailyReportCSV.getNoteTitle());
                                }

                                fixtureQrScan = getValue(38, edgeFormDatas);
                                dailyReportCSV.setFixtureQrScan(fixtureQrScan);

                                String existingNodeMACAddress = getValue(36, edgeFormDatas);
                                dailyReportCSV.setExistingNodeMACAddress(existingNodeMACAddress);
                                break;
                            case "Replace Node only":
                                newNodeMACAddress = getValue(30, edgeFormDatas);
                                if (newNodeMACAddress != null) {
                                    isDataLoad = true;
                                    dailyReportCSV.setNewNodeMACAddress(newNodeMACAddress);
                                    checkDupMacAddress(newNodeMACAddress, dailyReportCSV, dailyReportCSV.getNoteTitle());
                                }

                                existingNodeMACAddress = getValue(29, edgeFormDatas);
                                dailyReportCSV.setExistingNodeMACAddress(existingNodeMACAddress);
                                break;
                            case "Replace Fixture only":
                                fixtureQrScan = getValue(39, edgeFormDatas);
                                dailyReportCSV.setFixtureQrScan(fixtureQrScan);
                                break;
                            case "Power Issue":
                            case "Resolved (Other)":
                                existingNodeMACAddress = getValue(46, edgeFormDatas);
                                dailyReportCSV.setExistingNodeMACAddress(existingNodeMACAddress);
                                break;
                        }
                    }

                    break;

                case "Other Task":
                    isDataLoad = true;
                    break;


            }
        }

        return isDataLoad;

    }

    private String getValue(int id,List<EdgeFormData> edgeFormDatas){
        EdgeFormData tempEdgeFormData = new EdgeFormData();
        tempEdgeFormData.setId(id);
        int pos = edgeFormDatas.indexOf(tempEdgeFormData);
        if(pos != -1){
            tempEdgeFormData =  edgeFormDatas.get(pos);
           return tempEdgeFormData.getValue();
        }
        return null;
    }

	private boolean dataLoad(String formDef,DailyReportCSV dailyReportCSV){
        Type listType = new TypeToken<ArrayList<EdgeFormData>>() {
        }.getType();
        Gson gson = new Gson();
        List<EdgeFormData> edgeFormDatas = gson.fromJson(formDef, listType);
        boolean isDataLoad = false;
        for(EdgeFormData edgeFormData : edgeFormDatas){
            if(edgeFormData.getLabel() == null){
                continue;
            }
            if(edgeFormData.getLabel().equals("Fixture QR Scan")){
                dailyReportCSV.setFixtureQrScan(edgeFormData.getValue());
            }else if(edgeFormData.getLabel().equals("Node MAC address")){
                dailyReportCSV.setQrCode(edgeFormData.getValue());
                if(edgeFormData.getValue() != null && !edgeFormData.getValue().trim().isEmpty()){
                    isDataLoad = true;
                    checkDupMacAddress(edgeFormData.getValue(), dailyReportCSV, dailyReportCSV.getNoteTitle());
                }
            }else if(edgeFormData.getLabel().equals("Existing Node MAC Address")){
                String val = edgeFormData.getValue();
                if(val != null){
                    dailyReportCSV.setExistingNodeMACAddress(edgeFormData.getValue());
                    dailyReportCSV.setIsReplaceNode("Yes");
                }

            }else if(edgeFormData.getLabel().equals("New Node MAC Address")){
                String val = edgeFormData.getValue();
                if(val != null){
                    dailyReportCSV.setNewNodeMACAddress(edgeFormData.getValue());
                    if(dailyReportCSV.getNewNodeMACAddress() != null && !dailyReportCSV.getNewNodeMACAddress().isEmpty()){
                        isDataLoad = true;
                    }

                    dailyReportCSV.setIsReplaceNode("Yes");
                }

            }
        }
        return isDataLoad;
    }


    public void getFormData(List<Long> noteIds, List<NoteData> noteData){
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        try {
            String noteId = StringUtils.join(noteIds,",");
            queryStatement = connection.createStatement();
            queryResponse = queryStatement.executeQuery("select name,formdef,formtemplateguid,edgenoteentity_noteid from edgeform where edgenoteentity_noteid in ("+noteId+");");
            while (queryResponse.next()) {
               long edgeNoteId = queryResponse.getLong("edgenoteentity_noteid");
                NoteData noteData1 = new NoteData();
                noteData1.setNoteId(edgeNoteId);

               int pos =  noteData.indexOf(noteData1);
               if(pos != -1){
                   NoteData noteData2 =  noteData.get(pos);
                   FormData formData = new FormData();
                   noteData2.getFormDataList().add(formData);

                   formData.setName(queryResponse.getString("name"));
                   formData.setFormDef(queryResponse.getString("formdef"));
                   formData.setFormTemplateGuid(queryResponse.getString("formtemplateguid"));
               }

            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error in getFormData", e);
        } finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
    }
	
	

	public void checkDupMacAddress(String macAddress,DailyReportCSV dailyReportCSV,String title){
		Statement queryStatement = null;
		ResultSet queryResponse = null;
		try {
			queryStatement = connection.createStatement();
			queryResponse = queryStatement.executeQuery("select title from edgenote where isdeleted = false and iscurrent = true and noteid in (select edgenoteentity_noteid from edgeform where edgeform.formdef like '%"+macAddress+"%') and title != '"+title+"'");
			Set<String> datas = new HashSet<>();
			while (queryResponse.next()) {
				datas.add(queryResponse.getString("title"));
			}
			if(datas.size() > 0){
			    dailyReportCSV.setMacAddressDub(macAddress);
            }
			String res = StringUtils.join(datas, ",");
			dailyReportCSV.setMacAddressNoteTitle(res);
		} catch (Exception e) {
			logger.error("Error in getNoteIds", e);
		} finally {
			closeResultSet(queryResponse);
			closeStatement(queryStatement);
		}
	}

	public List<InspectionsReport> processInspectionsReport(String formTemplateGuid){
	    String query = generateSQLQuery(formTemplateGuid);
        List<InspectionsReport> inspectionsReports  =new ArrayList<>();
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        try {
            System.out.println(query);
            queryStatement = connection.createStatement();
            queryResponse = queryStatement.executeQuery(query);
            while (queryResponse.next()) {
                InspectionsReport inspectionsReport = new InspectionsReport();
                String groupName = queryResponse.getString("groupname");
                String locationDescription = queryResponse.getString("locationdescription");
                String[] locations = locationDescription.split("\\|");
                if(groupName.equals("Complete") || groupName.equals("Not Yet Complete") || groupName.equals("Completed")){
                    if(locations.length == 2) {
                        groupName = locations[1];
                    }
                }
                String description = "";
                if(locations.length == 2){
                    description = locations[0];
                }
                inspectionsReport.setType(groupName);
                inspectionsReport.setLon(queryResponse.getString("Latitude"));
                inspectionsReport.setLat(queryResponse.getString("Longitude"));
                inspectionsReport.setCreatedBy(queryResponse.getString("createdby"));
                inspectionsReport.setDescription(description);
                inspectionsReport.setAtlasPage(queryResponse.getString("notebookname"));
                inspectionsReport.setName(queryResponse.getString("title"));
                inspectionsReport.setAddComment(queryResponse.getString("addcomment"));
                inspectionsReport.setDateModified(queryResponse.getLong("createddatetime"));
                inspectionsReport.setIssueType(queryResponse.getString("fieldreport"));
                inspectionsReports.add(inspectionsReport);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
        return inspectionsReports;
    }
	

}
