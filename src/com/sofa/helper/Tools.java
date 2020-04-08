package com.sofa.helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sofa.util.NoCaseMap;

public class Tools {


	public static <T> String listJoin(String separator, List<T> list) {
		StringBuilder out = new StringBuilder();
		int i = 0;
		for (Object o : list)
		{
			i++;
			out.append(o.toString());
			if(list.size() > i)
				out.append(separator);
		}
		return out.toString();
	}


	public static void writeFile(String fileContent, String filePathOutput, boolean append) {
		try {
			FileWriter fileWriter;
			File f = new File(filePathOutput);
			File parent = f.getParentFile();
			if(!parent.exists())
				parent.mkdirs();

			fileWriter = new FileWriter(filePathOutput, append);

			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(fileContent);

			bufferedWriter.flush();
			bufferedWriter.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static void writeFile(String fileContent, String filePathOutput) {
		writeFile(fileContent, filePathOutput, false);
	}

	public static int firstDigit(int i) {
		List<Integer> digits = new ArrayList<Integer>();
		while(i > 0) {
			digits.add(i % 10);
			i /= 10;
		}
		return digits.get(digits.size() - 1);
	}

	

}
