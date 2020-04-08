package com.mallet;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.pipe.iterator.FileIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.*;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class TopicModel {

	public static final int NBRTOPWORD = 10;
	public static final int NUM_TOPICS = 1;

	public static void main(String[] args) throws Exception {

		// Begin by importing documents from text to feature sequences

		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		// Pipes: lowercase, tokenize, remove stopwords, map to features
		pipeList.add(new CharSequenceLowercase());
		pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
		pipeList.add(new TokenSequenceRemoveStopwords(new File("stopword/en.txt"), "UTF-8", false, false, false));
		pipeList.add(new TokenSequence2FeatureSequence());

		File[] directories = new File[] { new File("sample") };
		FileIterator iterator;
		iterator = new FileIterator(directories, new TxtFilter(), FileIterator.STARTING_DIRECTORIES);

		InstanceList instances = new InstanceList(new SerialPipes(pipeList));

		Reader fileReader = new InputStreamReader(new FileInputStream(new File("sample/equipartition_theorem.txt")),
				"UTF-8");
		// instances.addThruPipe(new CsvIterator(fileReader,
		// Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
		// 3, 2, 1));
		instances.addThruPipe(iterator);

		Formatter outSum = new Formatter(new StringBuilder(), Locale.US);
		String Sumr = "";

		for (int ii = 0; ii < instances.size(); ii++) {
			InstanceList instanceList = new InstanceList();
			instanceList.add(instances.get(ii));
			ParallelTopicModel model = new ParallelTopicModel(NUM_TOPICS, 1.0, 0.01);

			model.addInstances(instanceList);

			model.setNumThreads(1);
			model.setNumIterations(200);
			model.estimate();

			// Show the words and topics in the first instance

			// The data alphabet maps word IDs to strings
			Alphabet dataAlphabet = instances.getDataAlphabet();

			FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
			LabelSequence topics = model.getData().get(0).topicSequence;

			Formatter out = new Formatter(new StringBuilder(), Locale.US);
			for (int position = 0; position < tokens.getLength(); position++) {
				out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)),
						topics.getIndexAtPosition(position) + 1);
			}
			// System.out.println(out);
			// System.out.println();

			// Estimate the topic distribution of the first instance,
			// given the current Gibbs state.
			double[] topicDistribution = model.getTopicProbabilities(0);

			// Get an array of sorted sets of word ID/count pairs
			ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();

			// Show top 5 words in topics with proportions for the first
			// document
			for (int topic = 0; topic < NUM_TOPICS; topic++) {
				Iterator<IDSorter> sorterIterator = topicSortedWords.get(topic).iterator();

				outSum.format("|%-24s|\t%-8d|\t%-16.3f|\t", instanceList.get(0).getName(), topic + 1,
						topicDistribution[topic]);
				int rank = 0;
				while (sorterIterator.hasNext() && rank < NBRTOPWORD) {
					IDSorter idCountPair = sorterIterator.next();
					outSum.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()),
							idCountPair.getWeight());
					rank++;
				}
				outSum.format("\n");
				Sumr = Sumr + outSum.toString();
			}

			// Create a new instance with high probability of topic 0
			StringBuilder topicZeroText = new StringBuilder();
			Iterator<IDSorter> sorterIterator = topicSortedWords.get(0).iterator();

			int rank = 0;
			while (sorterIterator.hasNext() && rank < NBRTOPWORD) {
				IDSorter idCountPair = sorterIterator.next();
				topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
				rank++;
			}
		}
		System.out.format("|%-24s|\t%-8s|\t%-16s|\t%-48s\n", "Ressource", "Topic#", "Distribution", "Topic");
		System.out.println(Sumr);

		// Create a new instance named "test instance" with empty target and
		// source fields.
		// InstanceList testing = new InstanceList(instances.getPipe());
		// testing.addThruPipe(new Instance(topicZeroText.toString(), null,
		// "test instance", null));
		//
		// TopicInferencer inferencer = model.getInferencer();
		// double[] testProbabilities =
		// inferencer.getSampledDistribution(testing.get(0), 10, 1, NBRTOPWORD);
		// System.out.println("0\t" + testProbabilities[0]);
	}

	static class TxtFilter implements FileFilter {
		public boolean accept(File file) {
			return file.toString().endsWith(".txt");
		}
	}

}