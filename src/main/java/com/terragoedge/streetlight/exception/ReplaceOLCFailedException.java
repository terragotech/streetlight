package com.terragoedge.streetlight.exception;

public class ReplaceOLCFailedException extends Exception {

	String message;

	public ReplaceOLCFailedException(String message) {
		super(message);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
