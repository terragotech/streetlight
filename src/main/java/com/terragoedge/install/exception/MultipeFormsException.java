package com.terragoedge.install.exception;

public class MultipeFormsException extends Exception{
	String message = null;

	public MultipeFormsException(String message) {
		super(message);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
