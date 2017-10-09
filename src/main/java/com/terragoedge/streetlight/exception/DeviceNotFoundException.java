package com.terragoedge.streetlight.exception;

public class DeviceNotFoundException extends Exception{
	
	public String message;
	
	public DeviceNotFoundException(String message){
		super(message);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	
	

}
