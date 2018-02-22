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
			stringBuilder.append("select noteid,createddatetime, createdby,description,title,groupname,ST_X(geometry::geometry) as lat, ST_Y(geometry::geometry) as lng from edgenoteview where isdeleted = false and iscurrent = true ");
			String startOfDay = PropertiesReader.getProperties().getProperty("amerescousa.report.from");
			if(startOfDay != null && !startOfDay.isEmpty()){
				stringBuilder.append("and createddatetime >= ");
				stringBuilder.append(startOfDay);
			}
			
			String endOfDay = PropertiesReader.getProperties().getProperty("amerescousa.report.to");
			if(endOfDay != null && !endOfDay.isEmpty()){
				stringBuilder.append(" and createddatetime <= ");
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
			return "select noteid,createddatetime, createdby,description,title,groupname,ST_X(geometry::geometry) as lat, ST_Y(geometry::geometry) as lng from edgenoteview where isdeleted = false and iscurrent = true  and createddatetime >= "+startOfDay+";";
		}
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
			String sql = generateSQL();
			
			queryStatement = connection.createStatement();
			queryResponse = queryStatement.executeQuery(sql);
			
			while (queryResponse.next()) {
				DailyReportCSV dailyReportCSV = new DailyReportCSV();
				dailyReportCSV.setContext(queryResponse.getString("description"));
				dailyReportCSV.setFixtureType(queryResponse.getString("groupname"));
				dailyReportCSV.setNoteTitle(queryResponse.getString("title"));
				dailyReportCSV.setCreatedBy(queryResponse.getString("createdby"));
				dailyReportCSV.setCreateddatetime(queryResponse.getLong("createddatetime"));
				dailyReportCSV.setLat(String.valueOf(queryResponse.getDouble("lat")));
				dailyReportCSV.setLng(String.valueOf(queryResponse.getDouble("lng")));
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
					}else if(edgeFormData.getLabel().equals("Existing Node MAC Address")){
						dailyReportCSV.setExistingNodeMACAddress(edgeFormData.getValue());
						dailyReportCSV.setIsReplaceNode("Yes");
					}else if(edgeFormData.getLabel().equals("New Node MAC Address")){
						dailyReportCSV.setNewNodeMACAddress(edgeFormData.getValue());
						dailyReportCSV.setIsReplaceNode("Yes");
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
