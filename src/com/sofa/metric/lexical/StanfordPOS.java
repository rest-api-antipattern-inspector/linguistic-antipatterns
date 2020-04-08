package com.sofa.metric.lexical;

import java.util.ArrayList;

public class StanfordPOS {

	private static ArrayList<String> discriminantPOS = null;
	public static ArrayList<String> getDiscriminantPOS() {
		
		if (discriminantPOS == null) {
			discriminantPOS = new ArrayList<String>();
			discriminantPOS.add("JJ");
			discriminantPOS.add("JJR");
			discriminantPOS.add("JJS");
			discriminantPOS.add("NN");
			discriminantPOS.add("NNP");
			discriminantPOS.add("NNS");
			discriminantPOS.add("NNPS");
			discriminantPOS.add("RB");
			discriminantPOS.add("RBR");
			discriminantPOS.add("RBS");
			discriminantPOS.add("VB");
			discriminantPOS.add("VBD");
			discriminantPOS.add("VBG");
			discriminantPOS.add("VBN");
			discriminantPOS.add("VBP");
			discriminantPOS.add("VBZ");
		}
		
		return discriminantPOS;
	}
	
	/**
	 * Remove non-discriminant word from the given node
	 * Each word will be in lower case
	 * @param node
	 * @return
	 */
	public static ArrayList<String> getNodeDiscriminants(ArrayList<String> node) {
		ArrayList<String> result = new ArrayList<String>();
		int index = 0;
		for (String pos : Dictionaries.getInstance().getSFPartOfSpeechsForNode(node)) {
			if (discriminantPOS.contains(pos)) {
				result.add(node.get(index).toLowerCase());
			}
			index++;
		}
		return result;
	}
	
}
