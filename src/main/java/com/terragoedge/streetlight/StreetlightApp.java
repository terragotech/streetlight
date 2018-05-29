package com.terragoedge.streetlight;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.terragoedge.streetlight.service.StreetlightChicagoService;

public class StreetlightApp {
	
	final static Logger logger = Logger.getLogger(StreetlightApp.class);
	
	public static int getHoursOfDay(){
		String customDate = PropertiesReader.getProperties().getProperty("amerescousa.custom.date");
		if(customDate != null && customDate.equals("true")){
			String hoursOfDay = PropertiesReader.getProperties().getProperty("amerescousa.reporting.time");
			if(hoursOfDay != null){
				return Integer.parseInt(hoursOfDay);
			}
		}
		//return 17;
        return 11;
	}




	//1525944100172
	public static void main(String[] args) {
		try{
			while(true){
				try{
					Calendar calendar = Calendar.getInstance(Locale.getDefault());
					int hoursOfDay = calendar.get(Calendar.HOUR_OF_DAY);
					System.out.println("hoursOfDay:"+hoursOfDay);
					// 18 and 19
					if(hoursOfDay >= getHoursOfDay() && hoursOfDay < (getHoursOfDay() + 1)){
					//if(hoursOfDay >= 10 && hoursOfDay < 13){
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
