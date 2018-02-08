package com.terragoedge.streetlight.exception;

public class QRCodeNotMatchedException extends Exception {

	String idOnController = null;
	String macAddress = null;

	public QRCodeNotMatchedException(String idOnController, String macAddress) {
		super(idOnController);
		this.idOnController = idOnController;
		this.macAddress = macAddress;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public String getIdOnController() {
		return idOnController;
	}

	public void setIdOnController(String idOnController) {
		this.idOnController = idOnController;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

}
