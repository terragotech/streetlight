package com.terragoedge.slvinterface.exception;

public class QRCodeAlreadyUsedException extends Exception {
	
	String message = null;
	String macAddress = null;
	public QRCodeAlreadyUsedException(String message,String macAddress){
		super(message);
		this.message = message;
		this.macAddress = macAddress;
	}
	
	
	public String getMacAddress() {
		return macAddress;
	}



	public String getMessage(){
		return  message;
	}

}
