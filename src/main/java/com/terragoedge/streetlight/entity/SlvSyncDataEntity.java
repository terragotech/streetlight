package com.terragoedge.streetlight.entity;

import java.util.ArrayList;
import java.util.List;

import com.terragoedge.streetlight.StreetLightData;

public class SlvSyncDataEntity {
	private String macAddress;
	private String blockName;
	private String parentNoteId;
	private String idOnController;
	private String lat;
	private String lng;
	private String replaceNodeQRVal;
	private List<StreetLightData> streetLightDatas = new ArrayList<>();

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getBlockName() {
		return blockName;
	}

	public void setBlockName(String blockName) {
		this.blockName = blockName;
	}

	public String getParentNoteId() {
		return parentNoteId;
	}

	public void setParentNoteId(String parentNoteId) {
		this.parentNoteId = parentNoteId;
	}

	public String getIdOnController() {
		return idOnController;
	}

	public void setIdOnController(String idOnController) {
		this.idOnController = idOnController;
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

	public String getReplaceNodeQRVal() {
		return replaceNodeQRVal;
	}

	public void setReplaceNodeQRVal(String replaceNodeQRVal) {
		this.replaceNodeQRVal = replaceNodeQRVal;
	}
	
	

	public List<StreetLightData> getStreetLightDatas() {
		return streetLightDatas;
	}

	public void setStreetLightDatas(List<StreetLightData> streetLightDatas) {
		this.streetLightDatas = streetLightDatas;
	}

	@Override
	public String toString() {
		return "SlvSyncDataEntity [macAddress=" + macAddress + ", blockName=" + blockName + ", parentNoteId="
				+ parentNoteId + ", idOnController=" + idOnController + ", lat=" + lat + ", lng=" + lng
				+ ", replaceNodeQRVal=" + replaceNodeQRVal + ", streetLightDatas=" + streetLightDatas + "]";
	}

	

}
