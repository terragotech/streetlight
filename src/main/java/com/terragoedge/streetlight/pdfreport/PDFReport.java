package com.terragoedge.streetlight.pdfreport;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.dropbox.core.DbxException;
import com.google.gson.Gson;

import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.service.EdgeMailService;

public class PDFReport implements Runnable{
		private static String hostString;
		
		public String getHostString() {
			return hostString;
		}
		public void setHostString(String hostString) {
			this.hostString = hostString;
		}
		public static final String DAILY_REPORT_STATUS = "/geomapservice/getDailyReportStatus.html";
		public static final String DAILY_REPORT_PURGE = "/geomapservice/purgeReportStatus.html";
		public static final String DAILY_REPORT_START= "/geomapservice/startDailyReportCreation.html";
		
		private static final Logger logger = Logger.getLogger(PDFReport.class);
		private static ReportStartRequestStatus getDailyReportStatus(String hostURL)
		{
	 		ReportStartRequestStatus reportStartStatus = null;
	 		RestTemplate restTemplate = new RestTemplate();
	 		HttpHeaders headers = new HttpHeaders();
            HttpEntity request = new HttpEntity<>(headers);
	 		String hostService = hostURL + DAILY_REPORT_STATUS;
	 		System.out.println(hostService);
            ResponseEntity<String> response = restTemplate.exchange(hostService, HttpMethod.GET, request, String.class);
            HttpStatus responseStatusCode = response.getStatusCode();
            if (responseStatusCode.toString().equals("200"))
            {
                String jsonResponseString = response.getBody();
                Gson gson = new Gson();
                System.out.println(jsonResponseString);
                reportStartStatus = gson.fromJson(jsonResponseString, ReportStartRequestStatus.class);
            }
            return reportStartStatus;
		}
		private static ReportActionStatus purgeReportStatus(String hostURL)
		{
			ReportActionStatus reportActionStatus = null;
	 		RestTemplate restTemplate = new RestTemplate();
	 		HttpHeaders headers = new HttpHeaders();
            HttpEntity request = new HttpEntity<>(headers);
	 		String hostService = hostURL + DAILY_REPORT_PURGE;
            ResponseEntity<String> response = restTemplate.exchange(hostService, HttpMethod.GET, request, String.class);
            HttpStatus responseStatusCode = response.getStatusCode();
            if (responseStatusCode.toString().equals("200"))
            {
            	 String jsonResponseString = response.getBody();
                 Gson gson = new Gson();
                 reportActionStatus = gson.fromJson(jsonResponseString, ReportActionStatus.class);
            }
            return reportActionStatus;
            
		}
		private static void startReportGeneration(String hostURL) throws PDFReportException, DbxException, IOException
		{
			ReportStartRequestStatus reportStartStatus = null;
	 		RestTemplate restTemplate = new RestTemplate();
	 		HttpHeaders headers = new HttpHeaders();
            HttpEntity request = new HttpEntity<>(headers);
	 		String hostService = hostURL + DAILY_REPORT_START;
            ResponseEntity<String> response = restTemplate.exchange(hostService, HttpMethod.GET, request, String.class);
            HttpStatus responseStatusCode = response.getStatusCode();
            if (responseStatusCode.toString().equals("200"))
            {
            	boolean bMaxTriesComplete = false;
            	boolean bProgressComplete = false;
            	do
            	{
            		ReportStartRequestStatus reportReportStatus = getDailyReportStatus(hostURL);
            		if(reportReportStatus != null)
            		{
            			String strProgress = reportReportStatus.getProgress();	
            			String strErrMessage = reportReportStatus.getErrorMessage();
            			double progress = Double.parseDouble(strProgress);
            			System.out.println(strProgress);
            			System.out.println(strErrMessage);
            			if(strErrMessage.equals("OK"))
            			{
            				//Making Good progress
            				if(progress == 1.0)
            				{
            					bProgressComplete = true;
            					//Now upload file to dropbox
            					Properties properties = PropertiesReader.getProperties();
            					String pdfFileLocation = properties.getProperty("email.pdffilelocation");
            					String pdfFile = properties.getProperty("email.pdffile");
            					
            					Calendar calendar = Calendar.getInstance(Locale.getDefault());
            					Date date = new Date(calendar.getTimeInMillis());
            					SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
            					String fileName = dateFormat.format(date);
            					
            					File f1 = new File(pdfFile);
            					String updatedPDFFileName = "dailyReport-"+ fileName + ".pdf";
            					File f2 = new File(pdfFileLocation + updatedPDFFileName);
            					f1.renameTo(f2);
            					String dropBoxURL = uploadFileToDropBox(pdfFileLocation, updatedPDFFileName);
            					sendMail(dropBoxURL);
            					purgeReportStatus(hostString);
            				}
            				try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
            			}
            			else
            			{
            				//Error occurred
            				bProgressComplete = true;
            				sendErrorMail(strErrMessage);
            			}
            		}
            		else
            		{
            			throw new PDFReportException("Bad reponse from server, when getting status, "
            					+ "Error Code : " + responseStatusCode.toString());
            		}
            	}while(!bProgressComplete);
            }
            else
            {
            	throw new PDFReportException("Bad reponse from server, Error Code : " + responseStatusCode.toString());
            }
		}
		private static void sendMail(String strDropBoxLink)
		{
			Properties properties = PropertiesReader.getProperties();
			EdgeMailService edgeMailService = new EdgeMailService();
			edgeMailService.sendMailPDF(strDropBoxLink);
		}
		private static void sendErrorMail(String errorMessage)
		{
			Properties properties = PropertiesReader.getProperties();
			EdgeMailService edgeMailService = new EdgeMailService();
			edgeMailService.sendMailError(errorMessage);
		}
		private static String uploadFileToDropBox(String fileLocation, String fileName) throws DbxException, IOException
		{
			String dropBoxLink = null;
			String dropBoxAccessToken = PropertiesReader.getProperties().getProperty("dailyreport.dropboxAccessToken");
			String dropBoxLocation = PropertiesReader.getProperties().getProperty("dailyreport.dropboxLocation");
			DropBoxConnector dropBoxConnector = new DropBoxConnector();
			dropBoxConnector.setdropBoxAccessToken(dropBoxAccessToken);
			dropBoxConnector.establishConnection();
			dropBoxConnector.uploadFile(dropBoxLocation,fileLocation,fileName);
			dropBoxLink = dropBoxConnector.getSharedLinks(dropBoxLocation, fileName);
			System.out.println(dropBoxLink);
			return dropBoxLink;
		}
	 	public static void generateDailyReport(String hostURL) throws PDFReportException, DbxException, IOException
	    {
	 		ReportStartRequestStatus reportStartStatus = getDailyReportStatus(hostURL);
	 		if(reportStartStatus != null){
	 			if(!reportStartStatus.getStatus().equals("NOT_STARTED"))
	 			{
	 				logger.error("Last report generation triggered was not completed " + reportStartStatus.toString());
	 				purgeReportStatus(hostURL);
	 			}
	 			startReportGeneration(hostURL);
	 		}
	 		else
	 		{
	 			throw new PDFReportException("Bad reponse from server");
	 		}
	    }
		@Override
		public void run() {
			try {
				generateDailyReport(hostString);
			} catch (PDFReportException e) {
				String errorTrace = PDFExceptionUtils.getStackTrace(e);
				logger.error(e.getMessage());
				logger.error(errorTrace);
				sendErrorMail(errorTrace);
			} catch (DbxException e) {
				String errorTrace = PDFExceptionUtils.getStackTrace(e);
				logger.error(e.getMessage());
				logger.error(errorTrace);
				sendErrorMail(errorTrace);
			} catch (IOException e) {
				String errorTrace = PDFExceptionUtils.getStackTrace(e);
				logger.error(e.getMessage());
				logger.error(errorTrace);
				sendErrorMail(errorTrace);
			}catch (Exception e) {
				String errorTrace = PDFExceptionUtils.getStackTrace(e);
				logger.error(e.getMessage());
				logger.error(errorTrace);
				sendErrorMail(errorTrace);
			}
		}
	 	
}
