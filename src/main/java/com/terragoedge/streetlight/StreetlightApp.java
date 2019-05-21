package com.terragoedge.streetlight;

import com.terragoedge.streetlight.service.StreetlightChicagoService;
import com.terragoedge.streetlight.service.TalkAddressService;
import org.springframework.http.HttpMethod;

import java.util.Calendar;
import java.util.Date;


public class StreetlightApp {
private static boolean isReportProcessed = false;
	public static void main(String[] args) {
        Calendar calendar = Calendar.getInstance();
	    while (true){
            StreetlightChicagoService streetlightChicagoService = null;
            try{
                streetlightChicagoService = new StreetlightChicagoService();
                streetlightChicagoService.run();
                calendar.setTime(new Date());
                int hours = calendar.get(Calendar.HOUR_OF_DAY);
                if(hours == Integer.valueOf(PropertiesReader.getProperties().getProperty("com.existing.mac.validation.failure.report.time"))){
                    if(!isReportProcessed){
                        String fileUploadUrl = PropertiesReader.getProperties().getProperty("com.existing.mac.validation.failure.report.url");
                        streetlightChicagoService.edgeSlvserverCall(fileUploadUrl);
                        isReportProcessed = true;
                    }
                }else{
                    if(isReportProcessed){
                        isReportProcessed = false;
                    }
                }
                Thread.sleep(30000);
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                streetlightChicagoService.closeConnection();
            }
       }
	}

}
