package com.slvinterface.main;

import com.slvinterface.service.*;
import org.apache.log4j.Logger;

public class SLVApp {

    private static final Logger logger = Logger.getLogger(SLVApp.class);
    public static void main(String[] r){
        try{


            InBoundInterface inBoundInterface = new PrismInboundInterface2();
            inBoundInterface.startProcessing();
            /*if(r.length > 0) {
                inBoundInterface.updateNotes(r[0]);

            }
            else
            {
                System.out.println("Insufficient arguments");
            }*/


        }catch (Exception e){
            e.printStackTrace();
        }


    }
}
