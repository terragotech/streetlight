package com.terragoedge.streetlight;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.terragoedge.streetlight.service.StreetlightChicagoService;

public class StreetlightApp {
	
	final static Logger logger = Logger.getLogger(StreetlightApp.class);
	
	public static void main(String[] args) {
		StreetlightChicagoService streetlightChicagoService = new StreetlightChicagoService();
		try {
			streetlightChicagoService.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main1(String[] args) {
		try{
			while(true){
				try{
					Calendar calendar = Calendar.getInstance(Locale.getDefault());
					int hoursOfDay = calendar.get(Calendar.HOUR_OF_DAY);
					System.out.println("hoursOfDay:"+hoursOfDay);
					// 18 and 19
					if(hoursOfDay >= 18 && hoursOfDay < 19){
						File file  = new File("./report/pid");
						if(!file.exists()){
							System.out.println("File is not present.");
							StreetlightChicagoService streetlightChicagoService = new StreetlightChicagoService();
							try {
								streetlightChicagoService.run();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}else{
							System.out.println("File is present.");
						}
						
					}else{
						File file  =new File("./report/pid");
						if(file.exists()){
							System.out.println("File deleted.");
							file.delete();
						}
					}
					Thread.sleep(600000);
				}catch(Exception e){
					e.printStackTrace();
				}
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		
		
	}

}
