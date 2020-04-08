package com.sofa.motifs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

//import com.sofa.RESTPadManager;
//import com.sofa.helper.ServiceHelper;
import com.sofa.helper.Tools;
import com.sofa.metric.LexicalResult;

public abstract class AMotif {
	
	/**
	 * A motif could be a pattern or an anti-pattern
	 * @author Charlie Faucheux
	 */
	public enum Type {
		PATTERN, ANTIPATTERN;
	}
	
	// constants
	private final String TRACES_FOLDER = "traces/";
	private final String ALL_TRACES_FOLDER = "tracesAll/";
	private final String STATS_FOLDER = "traces/stats/";
	private final String ALL_STATS_FOLDER = "tracesAll/stats/";

	 // motif attributes
	private Type type;
	private String title;
	protected List<LexicalResult> results;
	public static List<LexicalResult> allResult;

	
	/**
	 * Constructor, set the type and the title of the motif
	 * @param type
	 * @param title
	 */
	public AMotif(Type type, String title) {
		this.type = type;
		this.title = title;
		results = new ArrayList<LexicalResult>(); // empty list by default
		allResult = new ArrayList<LexicalResult>(); // empty list by default
	}
	
	// getters
	public Type getType() { return type; }
	public String getTitle() { return title; }
	
	/**
	 * Set the list of results
	 * This method must be implemented to associated metric results to a pattern/anti-pattern
	 * @param results
	 */
	protected abstract void setResults();
	
	/**
	 * Detection of the pattern, by checking the associated metric results
	 */
	
	
	/**
	 * Retrieves the pattern results
	 * @return
	 */
	public List<LexicalResult> getResults() {
		return results;
	}
	
	/**
	 * Traces and Stats utilities
	 * @param badSmells
	 * @param httpMethod
	 * @return
	 */
	
	
	/**
	 * Traces and Stats utilities
	 * @param badSmells
	 * @return
	 */
	protected int countBadSmells(List<LexicalResult> badSmells) {
		return badSmells.size();
	}



	

	
	/**
	 * Write traces for this motif in the traces directory
	 * @param compositeName
	 * @param motifName
	 * @param count
	 */
	protected void writeInTraces(String compositeName, String motifName, String content) {
		
		// checking for existence of traces for this API
		String tracesDirectory = TRACES_FOLDER + compositeName;
		File apiDirectory = new File(tracesDirectory); 
		if (!apiDirectory.exists()) { 
			// creates a new folder
			apiDirectory.mkdir();
		}
				
		Tools.writeFile(content, tracesDirectory + "/" + compositeName + " - " + motifName + ".txt");
		
		/* unused - see the OutputFormatter
		if(RESTPadManager.debugDetail == DebugDetail.FULL)
			System.out.println(content);*/
	}
	
	/**
	 * Write stats for this motif in the stats directory
	 * @param compositeName
	 * @param motifName
	 * @param count
	 */
	protected void writeInStats(String compositeName, String motifName, int count) {
		
		// checking for existence of stats for this API
		String statsDirectory = STATS_FOLDER + compositeName;
		File apiDirectory = new File(statsDirectory); 
		if (!apiDirectory.exists()) { 
			// creates a new folder
			apiDirectory.mkdir();
		}
				
		String content = compositeName + "::" + motifName + " => " + count + "\n";
		Tools.writeFile(content, statsDirectory + "/" + compositeName + ".txt", true);
		
		/* unused - see the OutputFormatter
		if(RESTPadManager.debugDetail == DebugDetail.STATS)
			System.out.println(content); */
	}
	
	
protected void writeInAllTraces(String compositeName, String motifName, String content) {
		
		// checking for existence of traces for this API
		String tracesDirectory = ALL_TRACES_FOLDER + compositeName;
		File apiDirectory = new File(tracesDirectory); 
		if (!apiDirectory.exists()) { 
			// creates a new folder
			apiDirectory.mkdir();
		}
				
		Tools.writeFile(content, tracesDirectory + "/" + compositeName + " - " + motifName + ".txt");
	}
	
	/**
	 * Write stats for this motif in the stats directory
	 * @param compositeName
	 * @param motifName
	 * @param count
	 */
	protected void writeInAllStats(String compositeName, String motifName, int count) {
		
		// checking for existence of stats for this API
		String statsDirectory = ALL_STATS_FOLDER + compositeName;
		File apiDirectory = new File(statsDirectory); 
		if (!apiDirectory.exists()) { 
			// creates a new folder
			apiDirectory.mkdir();
		}
				
		String content = compositeName + "::" + motifName + " => " + count + "\n";
		Tools.writeFile(content, statsDirectory + "/" + compositeName + ".txt", true);
	}
	
	
	protected String extractNameOfClass(Class<?> classe) {
		String[] name = classe.getName().split("\\."); 
		return name[name.length-1];
	}
	
}
