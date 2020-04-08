package com.sofa.metric.lexical;

import java.util.ArrayList;

import java.util.Map;

import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;



import com.sofa.metric.LexicalResult;

/**
 * Provides static methods to analyze tags from URIs
 * @author Charlie Faucheux
 *
 */
public class LexicalMetrics {

	/**
	 * Checks if a node is an ID
	 * An ID must be a series of numbers and only numbers
	 * @param tag
	 * @param reliability
	 * @return
	 */
	public static boolean nodeIsID(ArrayList<String> node, LexicalResult.Reliability reliability) {
		
		if (node.size() == 1) {
			String tag = node.get(0);
			if (tag.length() == 0) return false;
			for (int i = 0; i < tag.length(); i++) {
				if (!characterIsNumeric(tag.charAt(i))) return false;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if a list of parameters contain an ID value
	 * @param parameters
	 * @param reliability
	 * @return
	 */
	public static boolean parametersContainID(Map<String, String> parameters, LexicalResult.Reliability reliability) {
		
		for (String key : parameters.keySet()) {
			if (key.contains("id") ||
				key.contains("ID")) return true;
		}
		return false;
	}
	
	/**
	 * Checks if a node is a plural 
	 * @param node
	 * @param reliability
	 * @return
	 */
	public static boolean nodeIsPlural(ArrayList<String> node, LexicalResult.Reliability reliability) {
		
				
		int delta=1;
		
		//If last word in the node is a file extension (xml, json, or txt) we consider te previous one
		if ((node.get(node.size()-1).contentEquals("xml")) || (node.get(node.size()-1).contentEquals("json")) || (node.get(node.size()-1).contentEquals("txt")))
			delta=2;
		
		
		
		
		ArrayList<String> sfposs = Dictionaries.getInstance().getSFPartOfSpeechsForNode(node);
		if (sfposs.size() > 0) {
			// retrieves the word to be considered
			String lastPos = sfposs.get(sfposs.size() - delta);
			if ((lastPos.equals("NNPS") || lastPos.equals("NNS")) && !lastPos.equals("NN") && !lastPos.equals("NNP")) 
				return true;
		}
		
		return false;
	}
	
	/**
	 * Checks if a node is a singular 
	 * @param node
	 * @param reliability
	 * @return
	 */
	public static boolean nodeIsSingular(ArrayList<String> node, LexicalResult.Reliability reliability) {
		ArrayList<String> sfposs = Dictionaries.getInstance().getSFPartOfSpeechsForNode(node);
		if (sfposs.size() > 0) {
			// retrieves the last word
			String lastPos = sfposs.get(sfposs.size() - 1);
			if (lastPos.equals("NNP") || lastPos.equals("NN")) return true;
		}
		return false;
	}
	
	/**
	 * Checks if a character is numeric
	 * @param c
	 * @return
	 */
	public static boolean characterIsNumeric(char c) {
		if (c >= '0' && c <= '9') return true;
		return false;
	}
	
	/**
	 * Checks if a character is upper case
	 * @param c
	 * @return
	 */
	public static boolean characterIsUpperCase(char c) {
		if (c >= 'A' && c <= 'Z') return true;
		return false;
	}

	public static boolean characterIsLowerCase(char c) {
		if (c >= 'a' && c <= 'z') return true;
		return false;
	}
	
	/**
	 * Checkis if a word is composed with only upper case letters
	 * @param word
	 * @return
	 */
	public static boolean wordIsUpperCase(String word) {
		for (int i = 0; i < word.length(); i++) {
			char c = word.charAt(i);
			if (!characterIsUpperCase(c)) return false;
		}
		return true;
	}
	
	public static boolean wordIsLowerCase(String word) {
		for (int i = 0; i < word.length(); i++) {
			char c = word.charAt(i);
			if (!characterIsLowerCase(c)) return false;
		}
		return true;
	}
	
	/**
	 * Checks if a node contains any synonym of the given word
	 * @param node
	 * @param lemma
	 * @param pos
	 * @return
	 */
	public static boolean nodeContainsSynonym(ArrayList<String> node, String lemma, POS pos, LexicalResult.Reliability reliability) {
		
		ArrayList<Synset> nodeSenses = Dictionaries.getInstance().getWNSenses(node);
		ArrayList<Synset> wordSenses = Dictionaries.getInstance().getSenses(lemma, pos);
		
		for (Synset sense : nodeSenses) {
			if (wordSenses.contains(sense)) return true;
		}
		
		// checks other POSs
		nodeSenses = Dictionaries.getInstance().getWNOtherSenses(node);
		for (Synset sense : nodeSenses) {
			if (wordSenses.contains(sense)) {
				reliability.multiplyReadingReliability(LexicalResult.Reliability.REPROCESSED_READING);
				return true;
			}
		}
		return false;
	}

}
