package com.terragoedge.streetlight;

import org.apache.log4j.Logger;

import com.terragoedge.streetlight.service.StreetlightService;

public class StreetlightApp {
	
	final static Logger logger = Logger.getLogger(StreetlightApp.class);
	
	public static void main(String[] args) {
		StreetlightService streetlightService = new StreetlightService();
		while(true){
			try{
				streetlightService.run();
				Thread.sleep(60000);
				
			}catch(Exception e){
				logger.error("Error in main", e);
			}
		}
	}

}
