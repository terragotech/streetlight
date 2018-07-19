package com.terragoedge.slvinterface.exception;

public class DeviceCreationFailedException extends Exception {
	private String message;

	public DeviceCreationFailedException(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
