package com.terragoedge.streetlight.service;

public class DailyReportCSV {
	
	private String noteTitle;
	private String context;
	private String contextType;
	private String fixtureCode;
	private String fixtureType;
	private String qrCode;
	private long noteCreatedDateTime;
	
	
	

	public long getNoteCreatedDateTime() {
		return noteCreatedDateTime;
	}

	public void setNoteCreatedDateTime(long noteCreatedDateTime) {
		this.noteCreatedDateTime = noteCreatedDateTime;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getContextType() {
		return contextType;
	}

	public void setContextType(String contextType) {
		this.contextType = contextType;
	}

	public String getFixtureCode() {
		return fixtureCode;
	}

	public void setFixtureCode(String fixtureCode) {
		this.fixtureCode = fixtureCode;
	}

	public String getFixtureType() {
		return fixtureType;
	}

	public void setFixtureType(String fixtureType) {
		this.fixtureType = fixtureType;
	}

	public String getQrCode() {
		return qrCode;
	}

	public void setQrCode(String qrCode) {
		this.qrCode = qrCode;
	}

	public String getNoteTitle() {
		return noteTitle;
	}

	public void setNoteTitle(String noteTitle) {
		this.noteTitle = noteTitle;
	}
	
	
	

}
