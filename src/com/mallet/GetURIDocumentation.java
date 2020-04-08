/**
 * 
 */
package com.mallet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Francis
 *
 */
public class GetURIDocumentation {

	public static Map<String, String> uriDocumentation = new HashMap();
	
	public static Map<String, String> methodDocumentation = new HashMap();
	
	public void createApiDescription2() throws IOException {

		File folder = new File("./API_DOC_short/" + RestTest.selectedAPI);
		File[] listOfFiles = folder.listFiles();
		String line;
		BufferedReader reader;
		for (File file : listOfFiles) {
			if ((file.isFile()) && (file.getName().contains("txt"))
					&& (!((file.getName().contains("APIIndex")) || (file.getName().contains("APITest"))))) {

				reader = new BufferedReader(new FileReader(RestTest.docPath + RestTest.selectedAPI + "/" + file.getName()));
				try {

					while ((line = reader.readLine()) != null) {

						if(line.length() <= 2)
							continue;
						String[] tmp = line.split(">>");
						
						//System.out.println("current file processing: " + file);
						
						this.uriDocumentation.put(tmp[1].toString(), tmp[2].toString());
						
						this.methodDocumentation.put(tmp[0].toString()+":"+tmp[1].toString(), tmp[2].toString());
						
						if (RestAnalyser.verbose)
							System.out.println(tmp[1].toString() + "->" + tmp[2].toString());
					}

				} finally {
					reader.close();
				}

			}
		}

	}
}
