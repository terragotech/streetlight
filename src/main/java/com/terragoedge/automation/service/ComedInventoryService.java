package com.terragoedge.automation.service;

import com.terragoedge.Test;
import com.terragoedge.automation.model.EdgeFormInfo;
import com.terragoedge.automation.model.InventoryResult;
import com.terragoedge.slvinterface.model.EdgeFormData;
import com.terragoedge.slvinterface.service.InventoryAutomationService;
import com.terragoedge.slvinterface.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ComedInventoryService extends AbstractInventoryService {
    InventoryAutomationService inventoryAutomationService = null;
    private static ExecutorService reportExecutor = Executors.newFixedThreadPool(10);

    public ComedInventoryService() {
        inventoryAutomationService = new InventoryAutomationService();
    }

    public void run() {
        int i=0;
        String inventoryHandlingFormGuid = "0b513f6e-452e-4397-84db-18d9deac61d7";
        String nodeFormGuid = "4010b3b0-14ef-4762-bdf7-e03306c852d3";
        String path = "./inventorydata.csv";
        //List<String> macAddressList = new ArrayList<>();
        //macAddressList.add("00135005007F1C2D");
        List<String> macAddressList = getCsvToEntity(path);
        System.out.println("Total Records : " + macAddressList.size());
        List<Future<InventoryResult>> inventoryResultList = new ArrayList<>();
        for (String macAddress : macAddressList) {
            System.out.println("Processed mac: " + macAddress);
            System.out.println("Total processed : " + i);
            Future<InventoryResult> inventoryResultFuture = reportExecutor.submit(new InventoryAutomationService(inventoryHandlingFormGuid, nodeFormGuid, macAddress));
            inventoryResultList.add(inventoryResultFuture);
        }
        List<InventoryResult> inventoryLists = new ArrayList<>();
        for (Future<InventoryResult> future : inventoryResultList) {
            try {
                i++;
                inventoryLists.add(future.get());
                System.out.println(i);
            } catch (InterruptedException | ExecutionException ex) {
            }
        }
        reportExecutor.shutdown();
        String outputFile = "./inventorydata_verifiedData.csv";
        if (inventoryLists.size() > 0)
            System.out.println("processed macaddress size : " + inventoryLists.size());
        Utils.writeInventoryData(inventoryLists, outputFile);
    }

}
