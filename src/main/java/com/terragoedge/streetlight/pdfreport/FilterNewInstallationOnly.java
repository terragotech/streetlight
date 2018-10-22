package com.terragoedge.streetlight.pdfreport;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import  com.terragoedge.streetlight.pdfreport.CSVUtils;

public class FilterNewInstallationOnly {
	public static void applyOperation(String inputFile, String outputFile) throws IOException
	{
		
		
		try {
			TextFileWriter textFileWriter = new TextFileWriter();
			TextFileReader textFileReader = new TextFileReader();
			textFileReader.openFile(inputFile);
			textFileWriter.openFile(outputFile, false);
			List<String> csvHeaders = null;
			String inputLine = null;
			// List of Fields to check
			int MACAddressIdx = -1;
			int ENMACAddressIdx = -1;
			int NNMACAddressIdx = -1;
			
			// End of List of Fields to check
			boolean bHeaderRead = false;
			 while ((inputLine = textFileReader.readLine()) != null) {
	            if (!bHeaderRead) {
	            	csvHeaders = CSVUtils.parseFields(inputLine);
	            	MACAddressIdx = CSVUtils.getConditionFieldIndex(csvHeaders, "MAC Address");
	            	ENMACAddressIdx = CSVUtils.getConditionFieldIndex(csvHeaders,"Existing Node MAC Address");
	            	NNMACAddressIdx = CSVUtils.getConditionFieldIndex(csvHeaders,"New Node MAC Address");
	            	textFileWriter.writeLine(inputLine);
	            	textFileWriter.writeLine("\n");
	            	bHeaderRead = true;
	            }
	            else
	            {
	            	List<String> fields = CSVUtils.parseFields(inputLine);
	            	String macAddressInstalled = fields.get(MACAddressIdx);
	            	String macAddressExisting = fields.get(ENMACAddressIdx);
	            	String macAddressNew = fields.get(NNMACAddressIdx);
	            	if(macAddressInstalled != null){
	            		if(!macAddressInstalled.equals(""))
	            		{
	            			if(macAddressExisting != null)
	            			{
	            				if(macAddressNew != null)
	            				{
	            					if(macAddressExisting.equals("") && macAddressNew.equals(""))
	            					{
	            						textFileWriter.writeLine(inputLine);
	            		            	textFileWriter.writeLine("\n");
	            					}
	            				}
	            			}
	            		}
	            	}
	            	
	            }
			 }//end while
			 textFileReader.closeFile();
			 textFileWriter.closeFile();
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
	}
}
