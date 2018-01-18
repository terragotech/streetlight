package com.terragoedge.streetlight;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.terragoedge.streetlight.service.StreetlightChicagoService;

public class StreetlightApp {
	
	final static Logger logger = Logger.getLogger(StreetlightApp.class);
	
	public static void main(String[] args) {
		/*Calendar calendar = Calendar.getInstance(Locale.getDefault());
		calendar.set(Calendar.HOUR_OF_DAY, 00);
		calendar.set(Calendar.MINUTE, 00);
		calendar.set(Calendar.SECOND, 00);
		System.out.println(calendar.getTime().getTime());
		System.out.println(System.currentTimeMillis());*/
		StreetlightChicagoService streetlightChicagoService = new StreetlightChicagoService();
		try {
			streetlightChicagoService.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
