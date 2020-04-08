package com.mallet;

import java.util.*;

public class Topic {
	public ArrayList<TopicWord> getTopicWords() {
		return topicWords;
	}

	private ArrayList<TopicWord> topicWords;
	private double topicDistribution;

	public Topic() {
		this.topicWords = new ArrayList<TopicWord>();
	}

	public ArrayList<String> getTopicWordsString(){
		ArrayList<String> retour = new ArrayList<>();
		for(TopicWord topicWord: topicWords)
		{
			retour.add(topicWord.getRootWord());
		}
	return retour;
	}

	protected void setTopicWords(ArrayList<TopicWord> topicWords) {
		this.topicWords = topicWords;
	}

	public double getTopicDistribution() {
		return topicDistribution;
	}

	public void setTopicDistribution(double topicDistribution) {
		this.topicDistribution = topicDistribution;
	}

	@Override
	public String toString() {
		return "Topic{topicWords=" + topicWords + "\n" + '}';
	}
}
