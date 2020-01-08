package com.terragoedge.streetlight;

import com.terragoedge.streetlight.service.TalkAddressService;
import org.apache.log4j.Logger;

public class StreetlightApp {
	public static void main(String[] args) {
        TalkAddressService talkAddressService = new TalkAddressService();
        talkAddressService.getTalqAddress();
	}

}
