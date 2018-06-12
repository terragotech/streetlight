package com.terragoedge.streetlight;

import com.terragoedge.streetlight.service.InstallDateFix;
import org.apache.log4j.Logger;

import com.terragoedge.streetlight.service.StreetlightChicagoService;

public class StreetlightApp {
	

	public static void main(String[] args) {
		InstallDateFix streetlightChicagoService = new InstallDateFix();
		streetlightChicagoService.run();

	}

}
