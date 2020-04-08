package com.sofa.metric.lexical.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sofa.util.Launcher;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.dictionary.Dictionary;


public class URIParserCore {
	// error flag
	private boolean error;
	public boolean getError() {
		return error;
	}
	
	// current node
	private String currentValue;
	public String getCurrentValue() {
		return currentValue;
	}
	public char getCurrentValueLastChar() {
		return currentValue.charAt(currentValue.length()-1);
	}
	public void removeCurrentValueLastChar() {
		currentValue = currentValue.substring(0, currentValue.length() - 1);
	}
	
	private ArrayList<String> currentNode;
	public ArrayList<String> getCurrentNode() {
		return currentNode;
	}
	// parameters
	private String currentPTag;
	private String currentPVal;
	
	// parsing result
	private ArrayList<ArrayList<String>> nodes;
	public ArrayList<ArrayList<String>> getNodes() {
		return nodes;
	}
	private Map<String, String> parameters;
	public Map<String, String> getParameters() {
		return parameters;
	}
	
	// initialization
	public URIParserCore() {
		error = false;
		currentValue = new String();
		currentNode = new ArrayList<String>();
		currentPTag = new String();
		currentPVal = new String();
		
		nodes = new ArrayList<ArrayList<String>>();
		parameters = new HashMap<String, String>();
	}
	
	/**
	 * Updates the current value with a character
	 * @param c
	 */
	public void updateValue(char c) {
		currentValue += c;
	}
	/**
	 * Puts the current value in the current node and reset the current value
	 * @throws JWNLException 
	 */
	public void newValue() throws JWNLException {
		if (currentValue.length() > 1){
			IndexWord[] arrayWords = Dictionary.getInstance().lookupAllIndexWords(currentValue).getIndexWordArray();
			if( arrayWords.length > 0 )
				currentNode.add(currentValue);
			//add the substrings to the currentNode if it is a word...
			else{
				String tmp = currentValue.substring(0, 2), newString;
				boolean found=false;
				for(int i=2; i<currentValue.length(); i++){
					tmp = tmp.concat(currentValue.substring(i, i+1));
					arrayWords =  Dictionary.getInstance().lookupAllIndexWords(tmp).getIndexWordArray();
					if(arrayWords.length > 0 ){
						currentNode.add(tmp);
						found = true;
					}
					if(found){
						if(Dictionary.getInstance().lookupAllIndexWords(currentValue.substring(i+1, currentValue.length())).getIndexWordArray().length > 0)
						{
							currentNode.add(currentValue.substring(i+1, currentValue.length()));
							break;
						}
						if(i<currentValue.length()-2){
							i+=1;
							tmp = currentValue.substring(i, i+2);
							i++;
							found =false;
						}
						else
							break;
					}
				}
			}
			}
		currentValue = new String();
	}
	/**
	 * Puts the current node in the nodes list and reset the current node
	 * The current value is also put into the current node before
	 * @throws JWNLException 
	 */
	public void newNode() throws JWNLException {
		newValue();
				
		if (currentNode.size() > 0){ 
			
			ArrayList<String> currentNodeTMP = new ArrayList<String>();
			for(int i=0; i<currentNode.size(); i++){
				
				if(Launcher.abbrv.containsKey(currentNode.get(i).toString().toLowerCase())){
					
					currentNodeTMP.add(Launcher.abbrv.get(currentNode.get(i).toString().toLowerCase()));
					
				}
				else
					currentNodeTMP.add(currentNode.get(i));
			
			}
				
			currentNode = currentNodeTMP;
			
			nodes.add(currentNode);
			
		}
		currentNode = new ArrayList<String>();
	}
	
	/**
	 * Updates the current parameter tag with a character
	 * @param c
	 */
	public void updatePTag(char c) {
		currentPTag += c;
	}
	/**
	 * Updates the current parameter value with a character
	 * @param c
	 */
	public void updatePVal(char c) {
		currentPVal += c;
	}
	/**
	 * Puts the current parameter value in the parameters map with the current parameter tag as key
	 * Resets the current parameter tag and value
	 * @param c
	 */
	public void newParameter() {
		if (currentPTag.length() > 0 && currentPVal.length() > 0) parameters.put(currentPTag, currentPVal);
		currentPTag = new String();
		currentPVal = new String();
	}
	
	public void finish(String uri) throws JWNLException {
		if (currentValue.length() > 0) {
			newValue();
		}
		if (currentNode.size() > 0) {
			newNode();
		}
		if (currentPTag.length() > 0 && currentPVal.length() > 0) {
			newParameter();
		}
		
		// remove two first nodes
		//if (nodes.size() > 1 && uri.contains("bestbuy")) {
		//	nodes.remove(0);
		//}
	}
	
	public String toString() {
		
		String result = new String();
		for (ArrayList<String> node : nodes) {
			for (String word : node) {
				result += word + " ";
			}
			result += "/ ";
		}
		System.out.print("? ");
		for (String key : parameters.keySet()) {
			String val = parameters.get(key);
			result += key + " = " + val + " & ";
		}
		result += "\n";
		return result;
	}
}