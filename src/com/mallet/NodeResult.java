package com.mallet;

public class NodeResult {
	private double semanticDistance;
	private String node;
	private String matchedWord;

	public NodeResult() {
	}

	public String getMatchedWord() {
		return matchedWord;
	}

	public void setMatchedWord(String matchedWord) {
		this.matchedWord = matchedWord;
	}

	public double getSemanticDistance() {
		return semanticDistance;
	}

	public void setSemanticDistance(double semanticDistance) {
		this.semanticDistance = semanticDistance;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}
}
