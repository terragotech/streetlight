package com.terragoedge.streetlight.pdfreport;

import java.io.PrintWriter;
import java.io.StringWriter;

public class PDFExceptionUtils {
	 public static String getStackTrace(Exception ex) {
	        StringWriter errorMessage = new StringWriter();
	        ex.printStackTrace(new PrintWriter(errorMessage));
	        return errorMessage.toString();
	    }
}

