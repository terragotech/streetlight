package com.terragoedge.streetlight;

import com.terragoedge.streetlight.job.RecommissionJob;
import com.terragoedge.streetlight.service.StreetlightChicagoService;
import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Calendar;
import java.util.Date;


public class StreetlightApp {
    private static final Logger logger = Logger.getLogger(StreetlightApp.class);
    private static boolean isReportProcessed = false;


	public static void main(String[] args) {
        startRecommissionJob();
	    while (true){
            StreetlightChicagoService streetlightChicagoService = null;
            try{
                streetlightChicagoService = new StreetlightChicagoService();
                streetlightChicagoService.run();

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                int hours = calendar.get(Calendar.HOUR_OF_DAY);

                if(hours == Integer.valueOf(PropertiesReader.getProperties().getProperty("com.installation.exception.report.time"))){

                    if(!isReportProcessed){
                        logger.info("Existing MAC Address Report process starts.");
                        streetlightChicagoService.generateInstallationRemovedExceptionReport();
                        logger.info("Existing MAC Address Report process Ends.");
                        isReportProcessed = true;
                    }
                }else{
                    if(isReportProcessed){
                        isReportProcessed = false;
                    }
                }
                Thread.sleep(30000);
            }catch (Exception e){
               logger.error("Error in main",e);
            }finally {
                streetlightChicagoService.closeConnection();
            }
       }
	}

	private static void startRecommissionJob(){
        try {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            Scheduler scheduler = schedulerFactory.getScheduler();
            scheduler.start();
            JobDetail recommissionJob = JobBuilder.newJob(RecommissionJob.class).build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .startNow()
                    .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(13,0)).build();

            scheduler.scheduleJob(recommissionJob,trigger);
        }catch (Exception e){
            logger.error("Error while startRecommissionJob: ",e);
        }
    }

}
