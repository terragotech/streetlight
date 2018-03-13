package com.terragoedge.install.exception;

public class SLNumberException extends Exception {
	String message = null;

	public SLNumberException(String message) {
		super(message);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
