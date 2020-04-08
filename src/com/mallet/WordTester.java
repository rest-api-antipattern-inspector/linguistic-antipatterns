package com.mallet;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.iterator.StringArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.InstanceList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class WordTester {
	private ParallelTopicModel innerModel;
	private double[] probabilities;

	public WordTester(ParallelTopicModel aModel) {
		this.innerModel = aModel;
	}

	public ArrayList<Double> getValues(String word, ArrayList<Pipe> pipes) {

		InstanceList testing = new InstanceList(new SerialPipes(pipes));

		String[] w = { word };
		testing.addThruPipe(new StringArrayIterator(w));

		TopicInferencer inferencer = innerModel.getInferencer();
		inferencer.setRandomSeed(5);

		double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);

		ArrayList<Double> perc = new ArrayList<Double>();

		for (double d : testProbabilities) {
			BigDecimal bd = new BigDecimal(d);
			BigDecimal size = new BigDecimal((double) 1 / testProbabilities.length);

			if (bd.setScale(4, RoundingMode.HALF_UP).doubleValue() == size.setScale(4, RoundingMode.HALF_UP)
					.doubleValue()) {
				bd = new BigDecimal(0);
			}
			perc.add(bd.setScale(4, RoundingMode.HALF_UP).doubleValue() * 100);
		}
		return perc;

	}
}
