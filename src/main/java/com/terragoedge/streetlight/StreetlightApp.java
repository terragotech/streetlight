package com.terragoedge.streetlight;

import com.terragoedge.streetlight.service.StreetlightChicagoService;


public class StreetlightApp {

	public static void main(String[] args) {
	    while (true){
            StreetlightChicagoService streetlightChicagoService = null;
            try{
                streetlightChicagoService = new StreetlightChicagoService();
                streetlightChicagoService.run();

                //streetlightChicagoService.loadDevices();

                Thread.sleep(30000);
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                streetlightChicagoService.closeConnection();
            }
        }
	}

}
