package com.terragoedge.install.exception;

public class InValidFormException extends Exception {

	String message = null;

	public InValidFormException(String message) {
		super(message);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
