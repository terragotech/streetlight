package com.terragoedge.streetlight.service;

import java.util.Objects;

public class DailyReportCSV {

	private String noteTitle;
	private String context;
	private String fixtureQrScan;
	private String fixtureType;
	private String qrCode;
	private int noteId;
	private String lat;
	private String lng;
	private String createdBy;
	private long createddatetime;
	private String existingNodeMACAddress;
	private String newNodeMACAddress;
	private String isReplaceNode = "No";
	
	private String nodeMACAddress;
	private boolean isQuickNote;


	private String macAddressNoteTitle;
	private String macAddressDub;

	public String getMacAddressDub() {
		return macAddressDub;
	}

	public void setMacAddressDub(String macAddressDub) {
		this.macAddressDub = macAddressDub;
	}

	public String getExistingNodeMACAddress() {
		return existingNodeMACAddress != null ? existingNodeMACAddress : "";
	}

	public void setExistingNodeMACAddress(String existingNodeMACAddress) {
		this.existingNodeMACAddress = existingNodeMACAddress;
	}

	public String getNewNodeMACAddress() {
		return newNodeMACAddress != null ? newNodeMACAddress : "";
	}

	public void setNewNodeMACAddress(String newNodeMACAddress) {
		this.newNodeMACAddress = newNodeMACAddress;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
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



	public String getNoteTitle() {
		return noteTitle;
	}

	public void setNoteTitle(String noteTitle) {
		this.noteTitle = noteTitle;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getFixtureQrScan() {
		return fixtureQrScan != null ? fixtureQrScan : "";
	}

	public void setFixtureQrScan(String fixtureQrScan) {
		this.fixtureQrScan = fixtureQrScan;
	}

	public String getFixtureType() {
		return fixtureType != null ? fixtureType : "";
	}

	public void setFixtureType(String fixtureType) {
		this.fixtureType = fixtureType;
	}

	public String getQrCode() {
		return qrCode != null ? qrCode : "";
	}

	public void setQrCode(String qrCode) {
		this.qrCode = qrCode;
	}

	public int getNoteId() {
		return noteId;
	}

	public void setNoteId(int noteId) {
		this.noteId = noteId;
	}


	public String getMacAddressNoteTitle() {
		return macAddressNoteTitle;
	}

	public void setMacAddressNoteTitle(String macAddressNoteTitle) {
		this.macAddressNoteTitle = macAddressNoteTitle;
	}

	public long getCreateddatetime() {
		return createddatetime;
	}

	public void setCreateddatetime(long createddatetime) {
		this.createddatetime = createddatetime;
	}


	public String getIsReplaceNode() {
		return isReplaceNode;
	}

	public void setIsReplaceNode(String isReplaceNode) {
		this.isReplaceNode = isReplaceNode;
	}

	public String getNodeMACAddress() {
		return nodeMACAddress;
	}

	public void setNodeMACAddress(String nodeMACAddress) {
		this.nodeMACAddress = nodeMACAddress;
	}

	public boolean isQuickNote() {
		return isQuickNote;
	}

	public void setQuickNote(boolean isQuickNote) {
		this.isQuickNote = isQuickNote;
	}

	@Override
	public String toString() {
		return "DailyReportCSV [noteTitle=" + noteTitle + ", context=" + context + ", fixtureQrScan=" + fixtureQrScan
				+ ", fixtureType=" + fixtureType + ", qrCode=" + qrCode + ", noteId=" + noteId + ", lat=" + lat
				+ ", lng=" + lng + ", createdBy=" + createdBy + ", createddatetime=" + createddatetime
				+ ", existingNodeMACAddress=" + existingNodeMACAddress + ", newNodeMACAddress=" + newNodeMACAddress
				+ ", isReplaceNode=" + isReplaceNode + ", nodeMACAddress=" + nodeMACAddress + ", isQuickNote="
				+ isQuickNote + ", macAddressNoteTitle=" + macAddressNoteTitle + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DailyReportCSV that = (DailyReportCSV) o;
		return Objects.equals(noteTitle, that.noteTitle);
	}

	@Override
	public int hashCode() {

		return Objects.hash(noteTitle);
	}
}
