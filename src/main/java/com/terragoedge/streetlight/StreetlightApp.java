package com.terragoedge.streetlight;

import com.terragoedge.streetlight.service.StreetlightChicagoService;
import com.terragoedge.streetlight.service.TalkAddressService;
import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;

import java.util.Calendar;
import java.util.Date;


public class StreetlightApp {
    private static final Logger logger = Logger.getLogger(StreetlightApp.class);
private static boolean isReportProcessed = false;
	public static void main(String[] args) {
        Calendar calendar = Calendar.getInstance();
	    while (true){
            StreetlightChicagoService streetlightChicagoService = null;
            try{
                streetlightChicagoService = new StreetlightChicagoService();
                streetlightChicagoService.run();

                Thread.sleep(30000);
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                streetlightChicagoService.closeConnection();
            }
       }
	}

}
