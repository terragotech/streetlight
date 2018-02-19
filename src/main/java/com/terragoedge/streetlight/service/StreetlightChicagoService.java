package com.terragoedge.streetlight.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.terragoedge.streetlight.dao.StreetlightDao;

public class StreetlightChicagoService {
	StreetlightDao streetlightDao = null;
	EdgeMailService edgeMailService = null;
	
	final Logger logger = Logger.getLogger(StreetlightChicagoService.class);
	
	public StreetlightChicagoService(){
		this.streetlightDao = new StreetlightDao();
		this.edgeMailService = new EdgeMailService();
	}
	
	
	private StringBuilder getMACDup(){
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Title,");
		stringBuilder.append("MAC Address,");
		stringBuilder.append("Exists In");
		stringBuilder.append("\n");
		return stringBuilder;
	}
	
	
	private void loadDup(StringBuilder stringBuilder,DailyReportCSV dailyReportCSV){
		stringBuilder.append(dailyReportCSV.getNoteTitle());
		stringBuilder.append(",");
		stringBuilder.append("\"");
		stringBuilder.append(dailyReportCSV.getQrCode());
		stringBuilder.append("\"");
		stringBuilder.append(",");
		stringBuilder.append("\"");
		stringBuilder.append(dailyReportCSV.getMacAddressNoteTitle());
		stringBuilder.append("\"");
		stringBuilder.append("\n");
	}
	
	public void run() throws IOException{
		List<DailyReportCSV> dailyReportCSVs = streetlightDao.getNoteIds();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Title,");
		stringBuilder.append("MAC Address,");
		stringBuilder.append("User Id,");
		stringBuilder.append("Fixture QR Scan,");
		stringBuilder.append("Fixture Type,");
		stringBuilder.append("Context,");
		stringBuilder.append("Lat,");
		stringBuilder.append("Lng,");
		stringBuilder.append("Date Time,");
		stringBuilder.append("Is ReplaceNode,");
		stringBuilder.append("Existing Node MAC Address,");
		stringBuilder.append("New Node MAC Address");
		stringBuilder.append("\n");
		StringBuilder dupMacStringBuilder = getMACDup();
		boolean isMacDup = false;
		for(DailyReportCSV dailyReportCSV : dailyReportCSVs){
			stringBuilder.append(dailyReportCSV.getNoteTitle());
			stringBuilder.append(",");
			stringBuilder.append("\"");
			stringBuilder.append(dailyReportCSV.getQrCode());
			stringBuilder.append("\"");
			stringBuilder.append(",");
			stringBuilder.append(dailyReportCSV.getCreatedBy());
			stringBuilder.append(",");
			stringBuilder.append("\"");
			stringBuilder.append(dailyReportCSV.getFixtureQrScan());
			stringBuilder.append("\"");
			stringBuilder.append(",");
			stringBuilder.append(dailyReportCSV.getFixtureType());
			stringBuilder.append(",");
			stringBuilder.append("\"");
			stringBuilder.append(dailyReportCSV.getContext());
			stringBuilder.append("\"");
			stringBuilder.append(",");
			stringBuilder.append(dailyReportCSV.getLat());
			stringBuilder.append(",");
			stringBuilder.append(dailyReportCSV.getLng());
			stringBuilder.append(",");
			stringBuilder.append(formatDateTime(dailyReportCSV.getCreateddatetime()));
			stringBuilder.append(",");
			stringBuilder.append(dailyReportCSV.getIsReplaceNode());
			stringBuilder.append(",");
			stringBuilder.append("\"");
			stringBuilder.append(dailyReportCSV.getExistingNodeMACAddress());
			stringBuilder.append("\"");
			stringBuilder.append(",");
			stringBuilder.append("\"");
			stringBuilder.append(dailyReportCSV.getNewNodeMACAddress());
			stringBuilder.append("\"");
			stringBuilder.append("\n");
			if(dailyReportCSV.getMacAddressNoteTitle() != null && !dailyReportCSV.getMacAddressNoteTitle().trim().isEmpty()){
				loadDup(dupMacStringBuilder, dailyReportCSV);
				isMacDup = true;
			}
		}
		String fileName = getDateTime();
		String dailyReportFile = "daily_report_"+fileName+".csv";
		String dupMacAddressFile = null;
		logData(stringBuilder.toString(), dailyReportFile);
		
		if(isMacDup){
			dupMacAddressFile = "daily_mac_dup_report_"+fileName+".csv";
			logData(dupMacStringBuilder.toString(), dupMacAddressFile);
		}
		//edgeMailService.sendMail(dupMacAddressFile, dailyReportFile);
		
	}
	
	
	private void logData(String data,String fileName){
		FileOutputStream fileOutputStream = null;
		try{
			 //fileOutputStream = new FileOutputStream("./report/"+fileName);
			fileOutputStream = new FileOutputStream(fileName);
			 fileOutputStream.write(data.getBytes());
			 fileOutputStream.flush();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(fileOutputStream != null){
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	private String getDateTime(){
		Calendar calendar = Calendar.getInstance(Locale.getDefault());
		Date date = new Date(calendar.getTimeInMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMMyyyy");
		return dateFormat.format(date);
	}
	
	
	private String formatDateTime(long currentDateTime){
		Date date = new Date(currentDateTime);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
		return dateFormat.format(date);
	}
	
	
	
	
}
