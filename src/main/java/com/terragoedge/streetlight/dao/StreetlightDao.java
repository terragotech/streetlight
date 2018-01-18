package com.terragoedge.streetlight.dao;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.streetlight.StreetlightDaoConnection;
import com.terragoedge.streetlight.service.DailyReportCSV;

public class StreetlightDao extends UtilDao {

	static final Logger logger = Logger.getLogger(StreetlightDao.class);

	public StreetlightDao() {
		super();
	}
	
	
	
	
	
	

	/**
	 * Get List of NoteIds which is assigned to given formtemplate
	 * 
	 * @param formTemplateGuid
	 * @return
	 */
	public List<DailyReportCSV> getNoteIds() {
		Statement queryStatement = null;
		ResultSet queryResponse = null;
		List<DailyReportCSV> dailyReportCSVs = new ArrayList<>();
		try {
			
			Calendar calendar = Calendar.getInstance(Locale.getDefault());
			calendar.set(Calendar.HOUR_OF_DAY, 00);
			calendar.set(Calendar.MINUTE, 00);
			calendar.set(Calendar.SECOND, 00);
			long startOfDay = calendar.getTime().getTime();
			queryStatement = connection.createStatement();
			queryResponse = queryStatement.executeQuery("select noteid, createdby,description,title,groupname from edgenoteview where isdeleted = false and iscurrent = true and createddatetime >= "+startOfDay+";");
			
			while (queryResponse.next()) {
				DailyReportCSV dailyReportCSV = new DailyReportCSV();
				dailyReportCSV.setContext(queryResponse.getString("description"));
				dailyReportCSV.setFixtureType(queryResponse.getString("groupname"));
				dailyReportCSV.setNoteTitle(queryResponse.getString("title"));
				loadVal(queryResponse.getString("noteid"), dailyReportCSV);
				dailyReportCSVs.add(dailyReportCSV);
			}

		} catch (Exception e) {
			logger.error("Error in getNoteIds", e);
		} finally {
			closeResultSet(queryResponse);
			closeStatement(queryStatement);
		}
		return dailyReportCSVs;
	}
	
	
	public void loadVal(String noteId,DailyReportCSV dailyReportCSV){
		Statement queryStatement = null;
		ResultSet queryResponse = null;
		try {
			queryStatement = connection.createStatement();
			queryResponse = queryStatement.executeQuery("select formdef from edgeform where edgenoteentity_noteid = "+noteId);
			
			while (queryResponse.next()) {
				String formDef = queryResponse.getString("formdef");
				
				Type listType = new TypeToken<ArrayList<EdgeFormData>>() {
				}.getType();
				Gson gson = new Gson();
				List<EdgeFormData> edgeFormDatas = gson.fromJson(formDef, listType);
				
				for(EdgeFormData edgeFormData : edgeFormDatas){
					if(edgeFormData.getLabel().equals("Fixture QR Scan")){
						dailyReportCSV.setFixtureQrScan(edgeFormData.getValue());
						
					}else if(edgeFormData.getLabel().equals("Node MAC address")){
						dailyReportCSV.setQrCode(edgeFormData.getValue());
						if(edgeFormData.getValue() != null && !edgeFormData.getValue().trim().isEmpty()){
							checkDupMacAddress(edgeFormData.getValue(), dailyReportCSV, dailyReportCSV.getNoteTitle());
						}
					}
				}
			}

		} catch (Exception e) {
			logger.error("Error in getNoteIds", e);
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
			queryResponse = queryStatement.executeQuery("select title from edgenote where noteid in (select edgenoteentity_noteid from edgeform where edgeform.formdef like '%"+macAddress+"%') and title != '"+title+"'");
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
