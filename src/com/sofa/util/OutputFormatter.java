package com.sofa.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVWriter;

//import com.sofa.helper.ServiceHelper;
import com.sofa.metric.LexicalResult;
import com.sofa.motifs.AMotif;
import com.sofa.motifs.Motif;


/**
 * The output formatter is used to handle the formatted output
 * which contains every tests results
 * @author Charlie Faucheux
 */
public class OutputFormatter {
	
	/**
	 * Description of a pattern
	 */
	private class Pattern implements Comparable<Pattern> {
		public AMotif.Type type;
		public String title;
		public Pattern(AMotif motif) {
			this.type = motif.getType();
			this.title = motif.getTitle();
		}
		
		public String toString() {
			String prefix = new String();
			if (type == AMotif.Type.PATTERN) prefix = "[P]";
			else prefix = "[AP]";
			return prefix + " " + title;
		}

		@Override
		public int compareTo(Pattern pattern) {
			return title.compareTo(pattern.title);
		}
	}
	
	/**
	 * Description of a call
	 */
	private class Call {
		// http method used
		public String method;
		// end point
		public String endpoint;
		// number of call
		public int number;
		// number of detections for each pattern
		public ArrayList<Integer> detections;
		// reliabilites for detections
		public ArrayList<ArrayList<Double>> reliabilities;
		
		/**
		 * Initializes the call structure
		 * @param method : name of the call method
		 * @param patternNb : number of pattern to check
		 */
		
		/**
		 * Calculates the average reliability for a given pattern
		 * @param index
		 * @return
		 */
		public double calculAverageReliability(int index) {
			double sum = 0;
			for (Double reliability : reliabilities.get(index)) {
				sum += reliability;
			}
			double nbDetections = reliabilities.get(index).size();
			if (nbDetections > 0) 
				return 1.0 - (sum / nbDetections);
			return 0;
		}
		
	}
	
	
	// attributes
	private String api;
	private ArrayList<Pattern> patterns; // list of patterns
	private Map<String, Pattern> patternsByName;
	private Map<String, Call> calls; // keys are method path
	private DecimalFormat formatter; // double formatter
	
	// singleton
	private OutputFormatter() {
		patterns = new ArrayList<Pattern>();
		patternsByName = new HashMap<String, Pattern>();
		calls = new HashMap<String, Call>();
		formatter = new DecimalFormat("#0");
	}
	private static OutputFormatter instance = new OutputFormatter();
	public static OutputFormatter getInstance() {
		return instance;
	}
	
	private boolean debugMode = true;
	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}
	
	/**
	 * Creates a key for a given response
	 * @param reponse
	 * @return
	 */
	
	/**
	 * Print information for debug if the DEBUG_MODE is set to true
	 * @param className
	 * @param title
	 * @param information
	 */
	public void printDebugInformation(String className, String title, String informations) {
		if (debugMode) {
			System.out.println("DEBUG : " + className + " - " + title);
			System.out.println(informations);
			System.out.println();
		}
	}
	
	/**
	 * Set the name of the api which is tested
	 * @param api
	 */
	public void setAPIName(String api) {
		this.api = api;
	}
	
	/**
	 * Add a pattern to the list of checked pattern
	 * Each checked pattern must be added before any matching adding !
	 * @param pattern
	 */
	public void addPattern(Motif motif) {
		Pattern pattern = new Pattern(motif.getImpl());
		patterns.add(pattern);
		patternsByName.put(pattern.title, pattern);
	}

	/**
	 * Return a list which contains all patterns of the given type
	 * The list is sorted
	 * @return
	 */
	/*
	private ArrayList<Pattern> getPatternsSorted(AMotif.Type type) {
		ArrayList<Pattern> results = new ArrayList<Pattern>();
		for (Pattern pattern : patterns) {
			if (pattern.type == type) {
				results.add(pattern);
			}
		}
		Collections.sort(results);
		return results;
	}
	*/
	/**
	 * Return a list which contains patterns first and anti-patterns after
	 * @return
	 */
	private ArrayList<Pattern> getAllPatternsSorted() {
		ArrayList<Pattern> result = new ArrayList<Pattern>();
		//result.addAll(getPatternsSorted(AMotif.Type.PATTERN));
		//result.addAll(getPatternsSorted(AMotif.Type.ANTIPATTERN));
		return result;
	}
	
	
	
	/**
	 * Add a matching for a couple response - pattern
	 * The add test method MUST be called before adding matchings
	 * @param response
	 * @param patternName
	 * @param reliability
	 */
	
	/**
	 * Calculates the avarage availibity of every calls for a given pattern
	 * @param index
	 * @return
	 */
	private double calculAverageReliabilityForPattern(int index) {
		double sum = 0;
		double nbDetections = 0;
		for (String key : calls.keySet()) {
			Call call = calls.get(key);
			if (call.detections.get(index) > 0) {
				sum += call.calculAverageReliability(index) * call.reliabilities.get(index).size();
				nbDetections += call.reliabilities.get(index).size();
			}
		}
		if (nbDetections > 0) 
			return sum / nbDetections;
		return 0;
	}
	
	/**
	 * Calculates the number of calls
	 * @return
	 */
	private int calculTotalCalls() {
		int sum = 0;
		for (String key : calls.keySet()) {
			Call call = calls.get(key);
			sum += call.number;
		}
		return sum;
	}
	
	/**
	 * Calculates the number of detections for a given pattern
	 * @param index
	 * @return
	 */
	private int calculDetectionsForPattern(int index, String pattern) {
		int sum = 0;

		for (String key : calls.keySet()) {
			Call call = calls.get(key);
			Integer tmp;
			
			//	/videos/rate
			if(pattern.contains("Tidy") && call.detections.get(index) < 4 )
				tmp = 0*call.number;
			else if(pattern.contains("Tidy") && call.detections.get(index) == 4 )
				tmp = 1*call.number;
			else if(pattern.contains("Tidy") && call.detections.get(index) > 4 )
				tmp = 1*call.detections.get(index)/4;
			else if(pattern.contains("Amorphous") && call.detections.get(index) >= 1 )
				tmp = 1*call.number;
			else if(pattern.contains("Amorphous") && call.detections.get(index) == 0 )
				tmp = 0*call.number;
			else
				tmp = call.detections.get(index);
			sum += tmp;
		}
		return sum;
	}
	
	// folder where are saved results
	private final String rootDirectory = "results";
	/**
	 * Save results for the current API
	 * Results are saved in a directory named like the API, in the "results" folder
	 * If the directory doesnt exist, it will be created
	 */
	public void saveAPIResults() {

		// checking for existence of results for this API
		File apiDirectory = new File(rootDirectory + "/" + api); 
		if (!apiDirectory.exists()) { 
			// creates a new folder
			apiDirectory.mkdir();
		}

		if (apiDirectory.isDirectory()) {
			
			// create the date for file name
			Date date = new Date();
			DateFormat day = new SimpleDateFormat("yy-MM-dd"); 
			DateFormat hour = new SimpleDateFormat("HH-mm"); 
			String fileName = rootDirectory + "/" + api + "/" + day.format(date) + "_" + hour.format(date) + ".csv";
			try {
				
				// creates the file
				CSVWriter writer = new CSVWriter(new FileWriter(fileName), ',');
				
				// print detections
				printTable(writer, TableType.DETECTIONS);
				
				String[] line = new String[1];
				writer.writeNext(line);
				
				// print reliabilities
				printTable(writer, TableType.RELIABILITIES);
				
				writer.close();
				
			} catch (IOException except) {
				
			}
		}
	}
	
	private enum TableType {
		DETECTIONS, RELIABILITIES;
	}
	/**
	 * Print a result table of detections or of reliabilities
	 * @param type
	 */
	private void printTable(CSVWriter writer, TableType type) {
		
		int index;
		
		// number of column in the results tab
		int colNb = 3 + patterns.size();
		
		/** Table title */
		String[] title = new String[colNb];
		title[0] = "HTTP Method";
		title[1] = "Entity End Point";
		title[2] = "# of Tests";
		index = 3;
		// Add patterns
		for (Pattern pattern : getAllPatternsSorted()) {
			title[index] = pattern.toString();
			index++;
		}
		writer.writeNext(title);
		
		/** Calls */
		for (String key : calls.keySet()) {
			
			String[] result = new String[colNb];
			Call call = calls.get(key);
			// set the HTTP method
			result[0] = call.method;
			// set the entity end point
			result[1] = call.endpoint;
			// set the number of calls
			result[2] = (new Integer(call.number)).toString();
			// set the matchings
			index = 3;
			// patterns
			for (Pattern pattern : getAllPatternsSorted()) {
				int patternIndex = patterns.indexOf(pattern);
				String detections = new String();
				// number of detections
				if (type == TableType.DETECTIONS) {
					Integer tmp;
					if(pattern.title.equals("Tidy URI") && call.detections.get(patternIndex) == 4)
						tmp = Integer.parseInt("1")*call.number;
					else if(pattern.title.equals("Tidy URI") && call.detections.get(patternIndex) < 4)
						tmp = Integer.parseInt("0")*call.number;		
					else if(pattern.title.equals("Tidy URI") && call.detections.get(index) > 4 )
						tmp = 1*call.detections.get(index)/4;
					else if(pattern.title.equals("Amorphous URI") && call.detections.get(patternIndex) >= 1)
						tmp = Integer.parseInt("1")*call.number;					
					else if(pattern.title.equals("Amorphous URI") && call.detections.get(patternIndex) == 0)
						tmp = Integer.parseInt("0")*call.number;					
					else
						tmp = call.detections.get(patternIndex);
					detections += tmp;
				// average reliability
				} else {
					if (call.detections.get(patternIndex) > 0) {
						if(call.calculAverageReliability(patternIndex) >  0.0)
							detections += formatter.format(call.calculAverageReliability(patternIndex)*100) + "%";
						else
							detections += "-";
							
					} else {
						detections += "-";
					}
				}
				
				result[index] = detections;
				index++;
			}
			writer.writeNext(result);
		}
		
		/** Total */
		String[] total = new String[colNb];
		if (type == TableType.DETECTIONS) {
			total[0] = "Detections for";
		} else {
			total[0] = "Soundness for";
		}
		total[1] = api;

		// total calls
		total[2] = (new Integer(calculTotalCalls())).toString();
		// total detections for each pattern and average reliability
		index = 3;
		for (Pattern pattern : getAllPatternsSorted()) {
			int patternIndex = patterns.indexOf(pattern);
			String resume = new String();
						
			int detections = calculDetectionsForPattern(patternIndex, pattern.title);
			// total of detections
			if (type == TableType.DETECTIONS) {
				resume += detections;
			// average reliability
			} else {
				if (detections > 0) {
					// add the average reliability
					if(calculAverageReliabilityForPattern(patternIndex) > 0.0 )
						resume += formatter.format(calculAverageReliabilityForPattern(patternIndex) * 100) + "%";
					else
						resume += "-";
				} else {
					resume += "-";
				}
			}
			total[index] = resume;
			index++;
		}
		writer.writeNext(total);
	}
	
	/**
	 * Print the results on the system output
	 */
	public enum PrintDetails {
		FULL,
		SUMS,
		NONE;
	}
	public void printAPIResults(PrintDetails details) {
		
		if (details != PrintDetails.NONE) {
			//System.out.println("------------------------------");
			//System.out.println("Results for the API : " + api);
		}
		
		if (details == PrintDetails.FULL) {
			
			for (String key : calls.keySet()) {
				Call call = calls.get(key);
				//System.out.println("------------------------------");
				// information
				//System.out.println("HTTP Method : " + call.method);
				//System.out.println("Entity End Point : " + call.endpoint);
				//System.out.println("# of Tests : " + call.number);
				// matchings
				for (Pattern pattern : getAllPatternsSorted()) {
					int index = patterns.indexOf(pattern);
					String detection = new String();
					// pattern name
					detection += "\t" + pattern + " : ";
					if (call.detections.get(index) > 0) {
						/** OLDER VERSION
						// number of detections
						detection += call.detections.get(index) + " ";
						// average
						detection += "(" + formatter.format(call.calculAverageReliability(index)) + ")";
						**/
						/** NEW VERSION : display only detection reliability **/
						int tmp;
						if(pattern.title.contains("Tidy") && call.detections.get(index) < 4 )
							tmp = 0*call.number;
						else if(pattern.title.contains("Tidy") && call.detections.get(index) == 4 )
							tmp = 1*call.number;
						else if(pattern.title.contains("Tidy") && call.detections.get(index) > 4 )
							tmp = 1*call.detections.get(index)/4;
						else if(pattern.title.contains("Amorphous") && call.detections.get(index) >= 1 )
							tmp = 1*call.number;
						else if(pattern.title.contains("Amorphous") && call.detections.get(index) == 0 )
							tmp = 0*call.number;
						else
							tmp = call.detections.get(index);
						
						detection += tmp + " - " + formatter.format(call.calculAverageReliability(index) * 100) + "%";

						//System.out.println(detection);
					}
				}	
			}
			
			System.out.println("------------------------------");
		}
		
		if (details == PrintDetails.FULL || details == PrintDetails.SUMS) {
			
			if (details == PrintDetails.SUMS) System.out.println("------------------------------");
		
			// Sums
			System.out.println("Total detections");
			for (Pattern pattern : getAllPatternsSorted()) {
				int index = patterns.indexOf(pattern);
				int detections = calculDetectionsForPattern(index, pattern.title);
				if (detections > 0) {
					
					System.out.println(
							// pattern name
							pattern + " : " + 
							/** OLDER VERSION
							// number of detections
							detections + 
							// average
							" (" + formatter.format(calculAverageReliabilityForPattern(index)) + ")");
							**/
							/** NEW VERSION : display only detection reliability **/
							detections + " - " + formatter.format(calculAverageReliabilityForPattern(index) * 100) + "%");
				}
			}
			System.out.println("Calls : " + calculTotalCalls());
			System.out.println("------------------------------");
		}
	}
	
	/**
	 * Deletes the result folders
	 */
	public void deleteResults() {
		
		File folder = new File(rootDirectory);
		if(folder.exists()) {
			File[] apis = folder.listFiles();
			for(int i = 0 ; i < apis.length ; i++) {
				if(apis[i].isDirectory()) {
					File[] results = apis[i].listFiles();
					for(int j = 0 ; j < results.length ; j++) {
						results[j].delete();
					}
				}
				apis[i].delete();
			}
		}
	}
}
