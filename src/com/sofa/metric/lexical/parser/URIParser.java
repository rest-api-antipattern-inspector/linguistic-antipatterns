package com.sofa.metric.lexical.parser;

import java.util.ArrayList;
import java.util.Map;

import net.didion.jwnl.JWNLException;

/**
 * The parse takes a URI and decompose it into a list of nodes
 * Each node is itself a list of strings which represents words
 * The parser retrieves also the path parameters and stocks them into a map of key - value
 * @author Faucheux_C
 */
public class URIParser {
	
	private URIParserCore core;
	private String uri;
	
	
	public URIParser() {
		// create an empty core
		core = new URIParserCore();
	}
	
	public void parse(String uri) throws JWNLException {
		
		// create a new core
		core = new URIParserCore();
		//if(uri.length() > 0){
		//	this.uri = uri.substring(uri.indexOf("/", 7));
		//	uri = this.uri;
		//}
		//else
		this.uri = uri;
		
		URIParserState currentState = new URIParserStateProcessNode();
		URIParserState.State transition = URIParserState.State.UNCHANGED;
		
		for (int i = 0; i < uri.length(); i++) {
			
			char c = uri.charAt(i);
			transition = currentState.processCharacter(c, core);
			switch (transition) {

			case PROCESS_NODE:
				currentState = new URIParserStateProcessNode();
				break;
			case PROCESS_PTAG:
				currentState = new URIParserStateProcessPTag();
				break;
			case PROCESS_PVAL:
				currentState = new URIParserStateProcessPVal();
				break;
			case UNCHANGED:
				break;
				
			case ERROR:
			default:
				i = uri.length();
				break;
			}
		}
		
		core.finish(uri);
	}
	
	public ArrayList<ArrayList<String>> getNodes() {
		return core.getNodes();
	}
	
	public Map<String, String> getParameters() {
		return core.getParameters();
	}
	
	/**
	 * Get the nodes flattened
	 * @return a list of strings - with a unique string per node
	 */
	public ArrayList<String> getFlattenedNode() {
		ArrayList<String> flattenedNodes = new ArrayList<String>();
		for (ArrayList<String> node : core.getNodes()) {
			String flattenedNode = new String();
			for (String word : node) {
				flattenedNode += word;
			}
			flattenedNodes.add(flattenedNode);
		}
		return flattenedNodes;
	}
	
	/** 
	 * Return the uri without parameters
	 * @return
	 */
	public String getURIWithoutParameter() {
		String result = new String();
		for (int i = 0; i < uri.length(); i++) {
			char c = uri.charAt(i);
			if (c != '?') {
				result += c;
			} else {
				return result;
			}
		}
		return result;
	}
	

	/** 
	 * Return the uri with parameters
	 * @return
	 */
	
	public String getURIWithParameter() {
		String result = new String();
		for (int i = 0; i < uri.length(); i++) {
			result += uri.charAt(i);

		}
		return result;
	}
	

	public String toString() {
		return core.toString();
	}
}
