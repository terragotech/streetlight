package com.terragoedge.streetlight;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import com.terragoedge.streetlight.dao.StreetlightDao;
import com.terragoedge.streetlight.installmaintain.InstallMaintenanceDao;
import com.terragoedge.streetlight.installmaintain.utills.Utils;
import org.apache.log4j.Logger;

import com.terragoedge.streetlight.service.StreetlightChicagoService;
import org.joda.time.DateTime;

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
		//return 13;
        return 17;
	}




	//1525944100172
	public static void main(String[] args) {
		InstallMaintenanceDao installMaintenanceDao = new InstallMaintenanceDao();
		installMaintenanceDao.loadNotesData();
		installMaintenanceDao.doProcess();
		/*try{
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
							StreetlightDaoConnection.getInstance().closeConnection();
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
		}*/
		
		
		
		
	}



	/*public static void main(String[] rg){
        StreetlightDao streetlightDao  =new StreetlightDao();
        Map<String,List<Long>> createdList = new HashMap<>();
        Map<String,String> ttitle = new HashMap<>();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("title,");
        stringBuffer.append("revisionguid,");
        stringBuffer.append("count");
        stringBuffer.append("\n");
        streetlightDao.getResData(createdList,ttitle);
        Set<String> keys = createdList.keySet();
        for(String key : keys){
            List<Long>  sss =  createdList.get(key);
            Collections.sort(sss);
            long start = sss.get(0);
           long end =  sss.get(sss.size() -1);
           if(end - start  < 2000){
               stringBuffer.append(ttitle.get(key));
               stringBuffer.append(",");
               stringBuffer.append(key);
               stringBuffer.append(",");
               stringBuffer.append(sss.size());
               stringBuffer.append("\n");
           }
        }

        logData(stringBuffer.toString(),"dublicate_list.csv");
    }



    private static void logData(String data,String fileName){
        FileOutputStream fileOutputStream = null;
        try{
            fileOutputStream = new FileOutputStream("./report/"+fileName);
            //fileOutputStream = new FileOutputStream("./"+fileName);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.flush();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(fileOutputStream != null){
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/

}
