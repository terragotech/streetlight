package com.terragoedge.streetlight;

import com.terragoedge.streetlight.service.StreetlightChicagoService;
import com.terragoedge.streetlight.service.TalkAddressService;


public class StreetlightApp {

	public static void main(String[] args) {
//	    while (true){
            TalkAddressService talkAddressService = null;
//            StreetlightChicagoService streetlightChicagoService = null;
            try{
                talkAddressService = new TalkAddressService();
                talkAddressService.getTalqAddress();
//                streetlightChicagoService = new StreetlightChicagoService();
//                streetlightChicagoService.run();

                //streetlightChicagoService.loadDevices();

//                Thread.sleep(30000);
            }catch (Exception e){
                e.printStackTrace();
            }finally {
//                streetlightChicagoService.closeConnection();
            }
//        }
	}

}
