package com.terragoedge.streetlight.exception;

public class DeviceUpdationFailedException extends Exception {
	private String message;

	public DeviceUpdationFailedException(String message) {
		super(message);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
