package com.terragoedge.streetlight;

import org.apache.log4j.Logger;

import com.terragoedge.install.StreetLightInstallService;

public class StreetlightApp {
	
	final static Logger logger = Logger.getLogger(StreetlightApp.class);
	
	public static void main(String[] args) {
		StreetLightInstallService streetLightInstallService = new StreetLightInstallService();
		
		while(true){
			try{
				streetLightInstallService.process();
				Thread.sleep(60000);
				
			}catch(Exception e){
				e.printStackTrace();
				logger.error("Error in main", e);
			}
		}
	}
	
	
}
