package com.terragoedge.streetlight.exception;

public class NoValueException extends Exception{
	
	String message = null;
	
	public NoValueException(String message){
		super(message);
		this.message = message;
	}
	
	
	public String getMessage(){
		return  message;
	}


}
