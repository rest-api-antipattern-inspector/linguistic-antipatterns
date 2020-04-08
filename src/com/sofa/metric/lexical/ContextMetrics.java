package com.sofa.metric.lexical;

import java.util.ArrayList;
import java.util.List;

import net.didion.jwnl.data.Synset;

import com.sofa.metric.LexicalResult;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public class ContextMetrics {
	
	public static boolean nodeAreInContext(ArrayList<String> nodeA, ArrayList<String> nodeB, LexicalResult.Reliability reliability) {
		
		StopWords stop_words = new StopWords();
		
		// check left > right
		ArrayList<String> sensesA = getSensesWords(Dictionaries.getInstance().getWNSenses(nodeA));
				
		for(int i=0; i<sensesA.size(); i++){
			if(stop_words.isStopword(sensesA.get(i).toLowerCase().toString()) || 
					stop_words.isStemmedStopword(sensesA.get(i).toLowerCase().toString()))
				sensesA.remove(i);
		}
		
		for (String wordB : nodeB) {
			if (sensesA.contains(wordB)) {
				return true;
			}
		}
		// check right > left
		ArrayList<String> sensesB = getSensesWords(Dictionaries.getInstance().getWNSenses(nodeB));
		
		for(int i=0; i<sensesB.size(); i++){
			if(stop_words.isStopword(sensesB.get(i).toLowerCase().toString()) || 
					stop_words.isStemmedStopword(sensesB.get(i).toLowerCase().toString()))
				sensesB.remove(i);
		}
		
		for (String wordA : nodeA) {
			if (sensesB.contains(wordA)) {
				return true;
			}
		}
		
		// check senses combination
		for (String senseA : sensesA) {
			if (sensesB.contains(senseA)) {
				return true;
			}
		}
		
		for (String senseB : sensesB) {
			if (sensesA.contains(senseB)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Extract words from synsets gloss
	 * @param senses
	 * @return
	 */
	private static ArrayList<String> getSensesWords(ArrayList<Synset> senses) {
		ArrayList<String> result = new ArrayList<String>();
		for (Synset synset : senses) {
			String gloss = synset.getGloss();
			
			// annotates it
			Annotation document = new Annotation(gloss);
			Dictionaries.getInstance().getPipeline().annotate(document);
			
			// retrieves interesting words
			List<CoreMap> results = document.get(SentencesAnnotation.class);
			for(CoreMap element: results) {
				for (CoreLabel token: element.get(TokensAnnotation.class)) {
	
					String pos = token.get(PartOfSpeechAnnotation.class);  
					if (StanfordPOS.getDiscriminantPOS().contains(pos)) {
						result.add(token.lemma());
					}
				}
			}
			
			//System.out.println("synset: " + synset.toString());
			
			//System.out.println("sense words: ");
			//for(int i=0; i<result.size(); i++)
				//System.out.print(result.get(i) + " " );
			//System.out.println();System.out.println();
		}
		
		
		return result;
	}
	
	
	
}
