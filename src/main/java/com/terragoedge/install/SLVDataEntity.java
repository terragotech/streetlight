package com.terragoedge.install;

public class SLVDataEntity {

	private String idOnController;
	private String controllerStrId;
	private String macAddress;
	private String luminareCode;
	private String newMacAddress;

	public String getIdOnController() {
		return idOnController;
	}

	public void setIdOnController(String idOnController) {
		this.idOnController = idOnController;
	}

	public String getControllerStrId() {
		return controllerStrId;
	}

	public void setControllerStrId(String controllerStrId) {
		this.controllerStrId = controllerStrId;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getLuminareCode() {
		return luminareCode;
	}

	public void setLuminareCode(String luminareCode) {
		this.luminareCode = luminareCode;
	}

	public String getNewMacAddress() {
		return newMacAddress;
	}

	public void setNewMacAddress(String newMacAddress) {
		this.newMacAddress = newMacAddress;
	}

	@Override
	public String toString() {
		return "SLVDataEntity [idOnController=" + idOnController + ", controllerStrId=" + controllerStrId
				+ ", macAddress=" + macAddress + ", luminareCode=" + luminareCode + ", newMacAddress=" + newMacAddress
				+ "]";
	}

}
