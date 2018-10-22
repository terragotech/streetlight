package com.terragoedge.streetlight.pdfreport;

import java.util.ArrayList;
import java.util.List;

public class CSVUtils {
	public static int getConditionFieldIndex(List<String>fields, String condtionName)
	{
		int nResult = -1;
		
		if((fields != null) && (condtionName != null))
		{
			int fieldCount = fields.size();
			for(int idx=0;idx<fieldCount;idx++)
			{
				String currElement = fields.get(idx);
				if(currElement.equals(condtionName))
				{
					nResult = idx;
					break;
				}
			}
		}
		return nResult;
	}
	public static List<String> parseFields(String line) {
		StringBuffer strBuffer = new StringBuffer();

		List<String> result = null;
		result = new ArrayList<String>();
		boolean bBeginField = false;

		int inputLength = line.length();
		Character ch1 = '\"';
		Character ch2 = ',';
		boolean lastWasQuotes = false;
		for (int idx = 0; idx < inputLength; idx++) {
			Character ch = line.charAt(idx);
			if (ch.equals(ch1)) {
				if (!bBeginField) {
					bBeginField = true;
				} else {
					bBeginField = false;
					String s1 = strBuffer.toString();
					strBuffer = new StringBuffer();
					result.add(s1);
					// Collect Field Here
					lastWasQuotes = true;
				}
			} else if (ch.equals(ch2) && (bBeginField != true)) {
				// Collect Field Here
				if (!lastWasQuotes) {
					String s2 = strBuffer.toString();
					result.add(s2);
					strBuffer = new StringBuffer();
				} else {
					lastWasQuotes = false;
				}

			} else {
				strBuffer.append(ch);

			}
		}
		String tmp = strBuffer.toString();
		if(tmp.length() > 0)
		{
			result.add(tmp);
		}
		return result;
	}

}


