package com.terragoedge.streetlight.pdfreport;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


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
		private static void startReportGeneration(String hostURL) throws PDFReportException
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
            					sendMail();
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
		private static void sendMail()
		{
			Properties properties = PropertiesReader.getProperties();
			String pdfFile = properties.getProperty("email.pdffile");
			EdgeMailService edgeMailService = new EdgeMailService();
			edgeMailService.sendMailPDF(pdfFile);
		}
	 	public static void generateDailyReport(String hostURL) throws PDFReportException
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
			}
		}
	 	
}
