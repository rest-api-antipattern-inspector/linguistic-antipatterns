package com.mallet;

import java.util.ArrayList;
import java.util.HashMap;

public class TopicWord {

	private String rootWord;
	private double wordWeight;
	private String word;
	private double rootWordWeight;
	private TextTool textTool;

	private HashMap<String, ArrayList<String>> wordSynList;

	public TopicWord() {
		textTool = new TextTool();
		wordSynList = new HashMap<String, ArrayList<String>>();
	}

	public String getWord() {
		return word;
	}

	protected void setWord(String word) {
		this.word = word;
		this.rootWord = textTool.lemmatize(word);

	}

	public String getRootWord() {
		return rootWord;
	}

	public double getRootWordWeight() {
		return rootWordWeight;
	}

	public double getWordWeight() {
		return wordWeight;
	}

	protected void setWordWeight(double wordWeight) {
		this.wordWeight = wordWeight;
		this.rootWordWeight = wordWeight;
	}

	@Override
	public String toString() {
		return "word='" + word + '\'' + ", rootWord='" + rootWord + '\'' + ", wordWeight=" + wordWeight + '}' + "\n";
	}

	public ArrayList<String> getSynSetList(String Word) {

		return wordSynList.get(textTool.getLemma(Word));
	}

	@Override
	public boolean equals(Object o) {
		boolean result = false;
		TextTool textTool = new TextTool();
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		TopicWord topicWord = (TopicWord) o;
		String[] words = this.word.split("_:{}/");
		if (words.length > 0) {
			String tw = textTool.lemmatize(words[0]);
			result = topicWord.getWord().equalsIgnoreCase(tw);
			if (!result && words.length > 1) {
				tw = textTool.lemmatize(words[1]);
				result = topicWord.getWord().equalsIgnoreCase(tw);
			}
		}
		return result;
	}

}