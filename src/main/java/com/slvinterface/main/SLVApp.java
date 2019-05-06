package com.slvinterface.main;

import com.slvinterface.service.SLVInterfaceService;
import com.slvinterface.service.SurreySLVInterface;

public class SLVApp {

    public static void main(String[] r){
        try{
            SLVInterfaceService slvInterfaceService = new SurreySLVInterface();
            slvInterfaceService.run();
        }catch (Exception e){
            e.printStackTrace();
        }


    }
}
