package com.mallet;

import java.util.ArrayList;

public class Document {
	private ArrayList<Topic> topics;
	private String fileName;

	public Document() {
		topics = new ArrayList<Topic>();
	}

	public ArrayList<Topic> getTopics() {
		return topics;
	}

	protected void setTopics(ArrayList<Topic> atopics) {
		this.topics.clear();
		this.topics.addAll(atopics);
	}

	public String getFileName() {
		return fileName;
	}

	protected void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
