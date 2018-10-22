package com.terragoedge.streetlight.pdfreport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TextFileWriter {
	File file = null;
	FileWriter fileWriter = null;
	BufferedWriter bufferedWriter = null;
	public void openFile(String fileName,boolean bAppend) throws IOException
	{
		file = new File(fileName);
		if(bAppend)
		{
			fileWriter = new FileWriter(file,true);
		}
		else
		{
			fileWriter = new FileWriter(file);
		}
		bufferedWriter = new BufferedWriter(fileWriter);
	}
	public void writeLine(String dataString) throws IOException
	{
		
		bufferedWriter.write(dataString);
	}
	public void closeFile()
	{
		if(bufferedWriter != null)
		{
			try {
				bufferedWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}

