package com.terragoedge.streetlight;

import com.terragoedge.streetlight.service.SlvService;


public class StreetlightApp {
	

	public static void main(String[] args) {
		SlvService slvService = new SlvService();
		slvService.start();
		/*InstallDateFix streetlightChicagoService = new InstallDateFix();
		streetlightChicagoService.run();*/

	}

}
