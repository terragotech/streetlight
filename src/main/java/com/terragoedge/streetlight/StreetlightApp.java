package com.terragoedge.streetlight;

import com.terragoedge.streetlight.service.HistoryService;


public class StreetlightApp {

    public static void main(String[] args) {
        HistoryService historyService = new HistoryService();
        historyService.start();
    }

}
