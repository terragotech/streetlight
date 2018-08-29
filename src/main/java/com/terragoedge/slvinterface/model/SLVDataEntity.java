package com.terragoedge.slvinterface.model;

import java.util.ArrayList;
import java.util.List;

public class SLVDataEntity {
	private String idOnController;
	private String controllerStrId;
	private String macAddress;
	private String luminareCode;
	private String newMacAddress;
	private String lat;
	private String lng;
	private List<Object> paramsList = new ArrayList<>();
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
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = lat;
	}
	public String getLng() {
		return lng;
	}
	public void setLng(String lng) {
		this.lng = lng;
	}
	public List<Object> getParamsList() {
		return paramsList;
	}
	public void setParamsList(List<Object> paramsList) {
		this.paramsList = paramsList;
	}
	@Override
	public String toString() {
		return "SLVDataEntity [idOnController=" + idOnController + ", controllerStrId=" + controllerStrId
				+ ", macAddress=" + macAddress + ", luminareCode=" + luminareCode + ", newMacAddress=" + newMacAddress
				+ ", lat=" + lat + ", lng=" + lng + ", paramsList=" + paramsList + "]";
	}
}
