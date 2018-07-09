package com.terragoedge.streetlight.pdfreport;

public class PDFReportException extends Exception{
	private String message;
	PDFReportException(String message){
		this.message = message;
	}
	public String getMessage()
	{
		return message;
	}
}
