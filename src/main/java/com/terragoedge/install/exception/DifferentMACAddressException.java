package com.terragoedge.install.exception;

public class DifferentMACAddressException extends Exception{
	

	String message = null;

	public DifferentMACAddressException(String message) {
		super(message);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
