package com.terragoedge.streetlight;

import com.terragoedge.streetlight.service.FailureReportService;


public class StreetlightApp {
	

	public static void main(String[] args) {

        FailureReportService failureReportService = new FailureReportService();
        failureReportService.run();



	}

}
