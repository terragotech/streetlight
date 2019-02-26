package com.terragoedge.slvinterface;

import com.terragoedge.slvinterface.service.*;

public class SlvInterfaceApp {

    public static void main(String[] args) {

      while (true) {
            try {
                SlvInterfaceService slvInterfaceService = new SlvInterfaceService();
                while (true) {
                    slvInterfaceService.start();
                    Thread.sleep(60000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}