package com.terragoedge.slvinterface.model;

import java.util.ArrayList;
import java.util.List;

public class DeviceMacAddress {

	private String errorCode;
	private Object errorCodeLabel;
	private Object message;
	private String status;
	private Boolean statusError;
	private Boolean statusOk;
	private List<Value> value = new ArrayList<>();

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public Object getErrorCodeLabel() {
		return errorCodeLabel;
	}

	public void setErrorCodeLabel(Object errorCodeLabel) {
		this.errorCodeLabel = errorCodeLabel;
	}

	public Object getMessage() {
		return message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Boolean getStatusError() {
		return statusError;
	}

	public void setStatusError(Boolean statusError) {
		this.statusError = statusError;
	}

	public Boolean getStatusOk() {
		return statusOk;
	}

	public void setStatusOk(Boolean statusOk) {
		this.statusOk = statusOk;
	}

	public List<Value> getValue() {
		return value;
	}

	public void setValue(List<Value> value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "DeviceMacAddress [errorCode=" + errorCode + ", errorCodeLabel=" + errorCodeLabel + ", message="
				+ message + ", status=" + status + ", statusError=" + statusError + ", statusOk=" + statusOk
				+ ", value=" + value + "]";
	}

}
