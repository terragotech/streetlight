package com.slvinterface.service;

import com.automation.slvtoedge.services.SlvToEdgeService;

public class ImportSLVData2 {
    public void startImport(String slvDataPath){
        SlvToEdgeService slvToEdgeService = new SlvToEdgeService();

        slvToEdgeService.start(slvDataPath);
    }
}
