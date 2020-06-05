package com.terragoedge.streetlight.job;

import com.terragoedge.streetlight.service.ReCommissionService;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;

public class RecommissionJob implements Job {
    private Logger logger = Logger.getLogger(RecommissionJob.class);
    private ReCommissionService reCommissionService = new ReCommissionService();
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("Recommission job started");
        reCommissionService.start();
        logger.info("Recommission job end");
    }
}
