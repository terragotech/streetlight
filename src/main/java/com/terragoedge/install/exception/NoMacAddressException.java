package com.terragoedge.install.exception;

public class NoMacAddressException extends Exception{
	String message = null;

	public NoMacAddressException(String message) {
		super(message);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
