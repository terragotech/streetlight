package com.terragoedge;

import com.terragoedge.streetlight.service.ReCommissionService;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReCommissionMain {
    public static void main(String[] args) {
        final ReCommissionService reCommissionService = new ReCommissionService();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);

        Executor executor = Executors.newScheduledThreadPool(1);

        ((ScheduledExecutorService) executor).scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                reCommissionService.start();
            }
        },(calendar.getTimeInMillis() - (new Date().getTime())), 86400000, TimeUnit.MILLISECONDS );
    }
}
