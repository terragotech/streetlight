package com.terragoedge.streetlight;

import org.apache.log4j.Logger;

import com.terragoedge.streetlight.service.StreetlightChicagoService;

public class StreetlightApp {
	
	final static Logger logger = Logger.getLogger(StreetlightApp.class);
	
	public static void main(String[] args) {
		StreetlightChicagoService streetlightChicagoService = new StreetlightChicagoService();
		while(true){
			try{
				streetlightChicagoService.run();
				Thread.sleep(60000);
				
			}catch(Exception e){
				logger.error("Error in main", e);
			}
		}

	}

}
