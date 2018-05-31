package com.terragoedge.streetlight.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.terragoedge.edgeserver.InspectionsReport;
import com.terragoedge.streetlight.PropertiesReader;
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
		stringBuilder.append(dailyReportCSV.getMacAddressDub());
		stringBuilder.append("\"");
		stringBuilder.append(",");
		stringBuilder.append("\"");
		stringBuilder.append(dailyReportCSV.getMacAddressNoteTitle());
		stringBuilder.append("\"");
		stringBuilder.append("\n");
	}
	
	private void populateQuickNoteHeader(StringBuilder quickNoteBuilder){
		quickNoteBuilder.append("Title,");
		quickNoteBuilder.append("MAC Address,");
		quickNoteBuilder.append("User Id,");
		quickNoteBuilder.append("Fixture QR Scan,");
		quickNoteBuilder.append("Fixture Type,");
		quickNoteBuilder.append("Context,");
		quickNoteBuilder.append("Lat,");
		quickNoteBuilder.append("Lng,");
		quickNoteBuilder.append("Date Time");
		quickNoteBuilder.append("\n");
	}
	
	
	private void populateQuickNoteData(StringBuilder quickNoteBuilder,DailyReportCSV dailyReportCSV){
		quickNoteBuilder.append(dailyReportCSV.getNoteTitle());
		quickNoteBuilder.append(",");
		quickNoteBuilder.append("\"");
		quickNoteBuilder.append(dailyReportCSV.getQrCode());
		quickNoteBuilder.append("\"");
		quickNoteBuilder.append(",");
		quickNoteBuilder.append(dailyReportCSV.getCreatedBy());
		quickNoteBuilder.append(",");
		quickNoteBuilder.append("\"");
		quickNoteBuilder.append(dailyReportCSV.getFixtureQrScan());
		quickNoteBuilder.append("\"");
		quickNoteBuilder.append(",");
		quickNoteBuilder.append(dailyReportCSV.getFixtureType());
		quickNoteBuilder.append(",");
		quickNoteBuilder.append("\"");
		quickNoteBuilder.append(dailyReportCSV.getContext());
		quickNoteBuilder.append("\"");
		quickNoteBuilder.append(",");
		quickNoteBuilder.append(dailyReportCSV.getLat());
		quickNoteBuilder.append(",");
		quickNoteBuilder.append(dailyReportCSV.getLng());
		quickNoteBuilder.append(",");
		quickNoteBuilder.append(formatDateTime(dailyReportCSV.getCreateddatetime()));
		quickNoteBuilder.append("\n");
	}
	
	private void populateNotesHeader(StringBuilder noteBuilder){
		noteBuilder.append("Title,");
		noteBuilder.append("MAC Address,");
		noteBuilder.append("User Id,");
		noteBuilder.append("Fixture QR Scan,");
		noteBuilder.append("Fixture Type,");
		noteBuilder.append("Context,");
		noteBuilder.append("Lat,");
		noteBuilder.append("Lng,");
		noteBuilder.append("Date Time,");
		noteBuilder.append("Is ReplaceNode,");
		noteBuilder.append("Existing Node MAC Address,");
		noteBuilder.append("New Node MAC Address");
		noteBuilder.append("\n");
	}
	
	
	
	private void populateNoteData(DailyReportCSV dailyReportCSV,StringBuilder noteBuilder){
		noteBuilder.append(dailyReportCSV.getNoteTitle());
		noteBuilder.append(",");
		noteBuilder.append("\"");
		noteBuilder.append(dailyReportCSV.getQrCode());
		noteBuilder.append("\"");
		noteBuilder.append(",");
		noteBuilder.append(dailyReportCSV.getCreatedBy());
		noteBuilder.append(",");
		noteBuilder.append("\"");
		noteBuilder.append(dailyReportCSV.getFixtureQrScan());
		noteBuilder.append("\"");
		noteBuilder.append(",");
		noteBuilder.append(dailyReportCSV.getFixtureType());
		noteBuilder.append(",");
		noteBuilder.append("\"");
		noteBuilder.append(dailyReportCSV.getContext());
		noteBuilder.append("\"");
		noteBuilder.append(",");
		noteBuilder.append(dailyReportCSV.getLat());
		noteBuilder.append(",");
		noteBuilder.append(dailyReportCSV.getLng());
		noteBuilder.append(",");
		noteBuilder.append(formatDateTime(dailyReportCSV.getCreateddatetime()));
		noteBuilder.append(",");
		noteBuilder.append(dailyReportCSV.getIsReplaceNode());
		noteBuilder.append(",");
		noteBuilder.append("\"");
		noteBuilder.append(dailyReportCSV.getExistingNodeMACAddress());
		noteBuilder.append("\"");
		noteBuilder.append(",");
		noteBuilder.append("\"");
		noteBuilder.append(dailyReportCSV.getNewNodeMACAddress());
		noteBuilder.append("\"");
		noteBuilder.append("\n");
	}
	
	public void run() throws IOException{
		String fileName = getDateTime();
		List<InspectionsReport> inspectionsReports = streetlightDao.processInspectionsReport(PropertiesReader.getProperties().getProperty("amrescouso.inspections.formtemplate.guid"));
		StringBuilder inspectionBuilder = new StringBuilder();
		populateInspectionsHeader(inspectionBuilder);

		for(InspectionsReport inspectionsReport : inspectionsReports){
			populateInspectionData(inspectionBuilder,inspectionsReport);
		}

		String inspectionFileName = "daily_Inspections_note_report_"+fileName+".csv";
		logData(inspectionBuilder.toString(), inspectionFileName);

		List<DailyReportCSV> dailyReportCSVs = streetlightDao.getNoteIds();
		
		StringBuilder quickNoteBuilder = new StringBuilder();
		populateQuickNoteHeader(quickNoteBuilder);
		
		StringBuilder stringBuilder = new StringBuilder();
		populateNotesHeader(stringBuilder);
		StringBuilder dupMacStringBuilder = getMACDup();
		boolean isMacDup = false;
		boolean isQuickNote = false;
        logger.info("");
        int totalSize = dailyReportCSVs.size();
        int count = 0;
		for(DailyReportCSV dailyReportCSV : dailyReportCSVs){
            count = count + 1;
		    logger.info("Current count:"+count);
		    logger.info("Total Count:"+totalSize);
			if(dailyReportCSV.isQuickNote()){
				isQuickNote = true;
				populateQuickNoteData(quickNoteBuilder, dailyReportCSV);
			}else{
				populateNoteData(dailyReportCSV, stringBuilder);
				if(dailyReportCSV.getMacAddressNoteTitle() != null && !dailyReportCSV.getMacAddressNoteTitle().trim().isEmpty()){
					loadDup(dupMacStringBuilder, dailyReportCSV);
					isMacDup = true;
				}
			}
			
		}
		String dailyReportFile = "daily_report_"+fileName+".csv";
		String dupMacAddressFile = null;
		String quickNoteFileName = null;
		logData(stringBuilder.toString(), dailyReportFile);
		
		if(isMacDup){
			dupMacAddressFile = "daily_mac_dup_report_"+fileName+".csv";
			logData(dupMacStringBuilder.toString(), dupMacAddressFile);
		}
		
		if(isQuickNote){
			quickNoteFileName = "daily_quick_note_report_"+fileName+".csv";
			logData(quickNoteBuilder.toString(), quickNoteFileName);
		}
		edgeMailService.sendMail(dupMacAddressFile, dailyReportFile,quickNoteFileName,inspectionFileName);
	}
	
	
	private void logData(String data,String fileName){
		FileOutputStream fileOutputStream = null;
		try{
			 fileOutputStream = new FileOutputStream("./report/"+fileName);
			//fileOutputStream = new FileOutputStream("./"+fileName);
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
		dateFormat.setTimeZone(TimeZone.getTimeZone("US/Central"));
		return dateFormat.format(date);
	}

	private String formatInspectionDateTime(long currentDateTime){
		Date date = new Date(currentDateTime);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		dateFormat.setTimeZone(TimeZone.getTimeZone("US/Central"));
		return dateFormat.format(date);
	}


	private void populateInspectionsHeader(StringBuilder quickNoteBuilder){
		quickNoteBuilder.append("Name,");
		quickNoteBuilder.append("Date Modified,");
		quickNoteBuilder.append("Atlas Page,");
		quickNoteBuilder.append("Description,");
		quickNoteBuilder.append("Created By,");
		quickNoteBuilder.append("Type,");
		quickNoteBuilder.append("Lat,");
		quickNoteBuilder.append("Lon,");
		quickNoteBuilder.append("Issue Type,");
		quickNoteBuilder.append("Add Comment");
		quickNoteBuilder.append("\n");
	}


	private void populateInspectionData(StringBuilder quickNoteBuilder,InspectionsReport inspectionsReport){
		quickNoteBuilder.append(inspectionsReport.getName());
		quickNoteBuilder.append(",");
		quickNoteBuilder.append("\"");
		long modifiedDate = inspectionsReport.getDateModified();
		quickNoteBuilder.append(formatInspectionDateTime(modifiedDate));
		quickNoteBuilder.append("\"");
		quickNoteBuilder.append(",");
		quickNoteBuilder.append(inspectionsReport.getAtlasPage());
		quickNoteBuilder.append(",");
		quickNoteBuilder.append("\"");
		quickNoteBuilder.append(inspectionsReport.getDescription());
		quickNoteBuilder.append("\"");
		quickNoteBuilder.append(",");
		quickNoteBuilder.append(inspectionsReport.getCreatedBy());
		quickNoteBuilder.append(",");
		quickNoteBuilder.append("\"");
		quickNoteBuilder.append(inspectionsReport.getType());
		quickNoteBuilder.append("\"");
		quickNoteBuilder.append(",");
		quickNoteBuilder.append(inspectionsReport.getLat());
		quickNoteBuilder.append(",");
		quickNoteBuilder.append(inspectionsReport.getLon());
		quickNoteBuilder.append(",");
		quickNoteBuilder.append(inspectionsReport.getIssueType());
		quickNoteBuilder.append(",");
		quickNoteBuilder.append(inspectionsReport.getAddComment());
		quickNoteBuilder.append("\n");
	}
}
