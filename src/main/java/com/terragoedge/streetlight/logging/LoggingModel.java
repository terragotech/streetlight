package com.terragoedge.streetlight.logging;

public class LoggingModel {
	
	private String processedNoteId;
	private String status;
	private String errorDetails;
	private String createdDatetime;
	private String noteName;
	private String existingNodeMACaddress;
	private String newNodeMACaddress;
	private String isReplaceNode;
	private boolean isQuickNote;
	private String idOnController;
	private String macAddress;
	private boolean isNoteAlreadySynced;




	public boolean isNoteAlreadySynced() {
		return isNoteAlreadySynced;
	}
	public void setNoteAlreadySynced(boolean flag) {
		this.isNoteAlreadySynced = flag;
	}
	public String getProcessedNoteId() {
		return processedNoteId;
	}
	public void setProcessedNoteId(String processedNoteId) {
		this.processedNoteId = processedNoteId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getErrorDetails() {
		return errorDetails;
	}
	public void setErrorDetails(String errorDetails) {
		this.errorDetails = errorDetails;
	}
	public String getCreatedDatetime() {
		return createdDatetime;
	}
	public void setCreatedDatetime(String createdDatetime) {
		this.createdDatetime = createdDatetime;
	}
	public String getNoteName() {
		return noteName;
	}
	public void setNoteName(String noteName) {
		this.noteName = noteName;
	}
	public String getExistingNodeMACaddress() {
		return existingNodeMACaddress;
	}
	public void setExistingNodeMACaddress(String existingNodeMACaddress) {
		this.existingNodeMACaddress = existingNodeMACaddress;
	}
	public String getNewNodeMACaddress() {
		return newNodeMACaddress;
	}
	public void setNewNodeMACaddress(String newNodeMACaddress) {
		this.newNodeMACaddress = newNodeMACaddress;
	}
	public String getIsReplaceNode() {
		return isReplaceNode;
	}
	public void setIsReplaceNode(String isReplaceNode) {
		this.isReplaceNode = isReplaceNode;
	}
	public boolean getIsQuickNote() {
		return isQuickNote;
	}
	public void setIsQuickNote(boolean isQuickNote) {
		this.isQuickNote = isQuickNote;
	}
	public String getIdOnController() {
		return idOnController;
	}
	public void setIdOnController(String idOnController) {
		this.idOnController = idOnController;
	}
	public String getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	
	
	

}
