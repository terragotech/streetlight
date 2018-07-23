package com.terragoedge.slvinterface.exception;

public class InValidBarCodeException extends Exception {

	String message = null;

	public InValidBarCodeException(String message) {
		super(message);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
