package com.terragoedge.streetlight.dao;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

import com.terragoedge.edgeserver.EdgeNote;
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
			System.out.println(startOfDay);
			return "select noteid,createddatetime, createdby,description,title,groupname,ST_X(geometry::geometry) as lat, ST_Y(geometry::geometry) as lng  from edgenoteview where  edgenoteview.isdeleted = false and edgenoteview.iscurrent = true  and edgenoteview.createddatetime >= "+startOfDay+";";
		}
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


    private boolean processFormData(List<String> formsData, DailyReportCSV dailyReportCSV) {
        if (formsData != null) {
            for (String formDef : formsData) {
                boolean result = dataLoad(formDef, dailyReportCSV);
                if (result) {
                    return result;
                }
            }
        }
        return false;
    }


    private void processFormData(Map<String,List<String>> formDatas,DailyReportCSV dailyReportCSV){


        List<String> installQRScan =   formDatas.get("fa47c708-fb82-4877-938c-992e870ae2a4");
        boolean  isDataLoaded =  processFormData(installQRScan,dailyReportCSV);
        if(isDataLoaded){
            return;
        }

        List<String> installMaintenanceList =   formDatas.get("8b722347-c3a7-41f4-a8a9-c35dece6f98b");
         isDataLoaded = processFormData(installMaintenanceList,dailyReportCSV);
        if(isDataLoaded){
            return;
        }


        List<String> replaceList =   formDatas.get("606fb4ca-40a4-466b-ac00-7c0434f82bfa");
        isDataLoaded =  processFormData(replaceList,dailyReportCSV);
        if(isDataLoaded){
            return;
        }




        List<String> newInstallQRScan =   formDatas.get("0ea4f5d4-0a17-4a17-ba8f-600de1e2515f");
        isDataLoaded =  processFormData(newInstallQRScan,dailyReportCSV);
        if(isDataLoaded){
            return;
        }



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
			String res = StringUtils.join(datas, ",");
			dailyReportCSV.setMacAddressNoteTitle(res);
		} catch (Exception e) {
			logger.error("Error in getNoteIds", e);
		} finally {
			closeResultSet(queryResponse);
			closeStatement(queryStatement);
		}
	}

	

}
