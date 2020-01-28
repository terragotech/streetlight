package com.terragoedge.streetlight.exception;

public class InValidSLNumberException extends Exception {
	String message = null;
	public InValidSLNumberException(String message){
		super(message);
		this.message = message;
	}
	
	
	public String getMessage(){
		return  message;
	}

}
