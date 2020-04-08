package com.mallet;

/**
 * Created by Mohamed on 23-Oct-2015.
 */
public class RestAnalyserResult {
	private String[] topics;
	private boolean isContextual;
	private NodeResult nodeResult;

	public RestAnalyserResult() {
	}

	public String[] getTopics() {
		return topics;
	}

	public void setTopics(String[] topics) {
		this.topics = topics;
	}

	public boolean isContextual() {
		return isContextual;
	}

	public void setIsContextual(boolean isContextual) {
		this.isContextual = isContextual;
	}

	public NodeResult getNodeResult() {
		return nodeResult;
	}

	public void setNodeResult(NodeResult nodeResult) {
		this.nodeResult = nodeResult;
	}

}
