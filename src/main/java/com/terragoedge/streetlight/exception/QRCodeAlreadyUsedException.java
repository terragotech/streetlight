package com.terragoedge.streetlight.exception;

public class QRCodeAlreadyUsedException extends Exception {
	
	String message = null;
	public QRCodeAlreadyUsedException(String message){
		super(message);
		this.message = message;
	}
	
	
	public String getMessage(){
		return  message;
	}

}
