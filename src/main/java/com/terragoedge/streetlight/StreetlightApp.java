package com.terragoedge.streetlight;

import com.terragoedge.streetlight.service.InstallDateFix;
import com.terragoedge.streetlight.service.SlvService;
import com.terragoedge.streetlight.service.StreetlightChicagoService;
import com.terragoedge.streetlight.service.TalkAddressService;


public class StreetlightApp {


	public static void main(String[] args) {
		// SlvService slvService = new SlvService();
		// slvService.start();

/*
		InstallDateFix streetlightChicagoService = new InstallDateFix();
		streetlightChicagoService.run();
*/


		while (true){
		    try{
		      //  TalkAddressService talkAddressService = new TalkAddressService();
		      //  talkAddressService.getTalqAddress();
               StreetlightChicagoService streetlightChicagoService = new StreetlightChicagoService();
                streetlightChicagoService.run();
                Thread.sleep(50000);
                break;
            }catch (Exception e){
		        e.printStackTrace();
            }
        }

	}

}
