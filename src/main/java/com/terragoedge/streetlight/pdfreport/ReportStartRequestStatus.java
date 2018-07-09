package com.terragoedge.streetlight.pdfreport;

public class ReportStartRequestStatus {
	private String progress;
	private String status;
	private String errorMessage;
	private String startedTime;
	private String errorCode;
	public String getProgress() {
		return progress;
	}
	public void setProgress(String progress) {
		this.progress = progress;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getStartedTime() {
		return startedTime;
	}
	public void setStartedTime(String startedTime) {
		this.startedTime = startedTime;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	@Override
    public String toString() {
		return "progress=" + progress + ", status=" + status + ", errorMessage=" + errorMessage + " ,startedTime" + startedTime + " ,errorCode" +  errorCode;
	}
}
