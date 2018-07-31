package com.terragoedge.slvinterface;

import com.terragoedge.slvinterface.service.SlvInterfaceService;
import com.terragoedge.slvinterface.service.TalkAddressService;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;

public class CronJobService {

    @Scheduled(cron = "${cron.expression.talqaddress}")
    public void startTalqAddress() {
     /*   try {
            TalkAddressService talkAddressService = new TalkAddressService();
            talkAddressService.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Method startTalqAddress  10 seconds. Current time is :: " + new Date());
        System.out.println("Executed");
  */  }

    @Scheduled(cron = "${cron.expression.slvinterface}")
    public void startSLVinterface() {
        try {
            System.out.println("Cronjob called");
            SlvInterfaceService slvInterfaceService = new SlvInterfaceService();
            slvInterfaceService.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Method startSLVinterface  1 minutes. Current time is :: " + new Date());
        System.out.println("Executed");
    }

}
