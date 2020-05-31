package com.sofa.metric.lexical;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.dictionary.Dictionary;

import com.sofa.metric.lexical.parser.URIParser;
import com.sofa.util.OutputFormatter;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.util.CoreMap;

/**
 * This singleton handle the Stanford dictionary and WordNet
 * Its used to analyze decomposed URI to find Part Of Speech, definition and other informations
 * This singleton must be loaded with its loading method before any other call
 * @author Faucheux_C
 */
public class Dictionaries {
	
	/** INTERNAL STATE ================================================== */
	private boolean loaded; // dictionaries loaded
	private boolean versionned; // version found into the path
	private String version;
	private StanfordCoreNLP pipeline; // Stanford
	// lexical analysis
	private LexicalizedParser lexicalizedParser;
	private TokenizerFactory<CoreLabel> tokenizerFactory;
	private TreebankLanguagePack languagePack;
	private GrammaticalStructureFactory factory;
	// informations about current URI 
	private String eep; // entity end point
	private URIParser parser;
	private Map<ArrayList<String>, ArrayList<String>> lemmas;
	private Map<ArrayList<String>, ArrayList<String>> sfposs;
	private Map<ArrayList<String>, ArrayList<POS>> wnposs;
	private Map<ArrayList<String>, GrammaticalStructure> gmstructs;
	
	// base URI
	private String base = new String();
	public void setBaseURI(String base) {
		// keep only the first part of the base (other nodes will be retrieved later)
		int counter = 0;
		for (int i = 0; i < base.length(); i++) {
			char c = base.charAt(i);
			if (c == '/' || c == '\\') {
				counter ++;
			}
			if (counter <=2) {
				this.base += c;
			}
		}
	}
	
	/** SINGLETON ======================================================== */
	private static Dictionaries instance = new Dictionaries();
	public static Dictionaries getInstance() {
		if (!instance.loaded) instance.load(); // load if necessary
		return instance;
	}
	
	private Dictionaries()  {
		try {
			resetInformations(new String());
		} catch (JWNLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		loaded = false;
	}
	
	/** GETTERS ========================================================== */
	public boolean isLoaded() { return loaded; }
	// version
	public boolean uriIsVersionned() { return versionned; }
	public String getURIVersion() { return version; }
	// entity end point
	public String getEEP() { return eep; }
	// Stanford pipeline
	public StanfordCoreNLP getPipeline() { return pipeline; }
	// parser
	public URIParser getParser() { return parser; }
	// lemmas
	public ArrayList<String> getLemmasForNode(ArrayList<String> node) {
		if (lemmas.containsKey(node)) return lemmas.get(node);
		return new ArrayList<String>();
	}
	// Stanford POSs
	public ArrayList<String> getSFPartOfSpeechsForNode(ArrayList<String> node) throws JWNLException {
		// analyzeURI(node.toString());
		if (sfposs.containsKey(node)) return sfposs.get(node);
		return new ArrayList<String>();
	}
	// Wordnet POSs
	public ArrayList<POS> getWNPartOfSpeechsForNode(ArrayList<String> node) throws JWNLException {
		// analyzeURI(node.toString());
		if (wnposs.containsKey(node)) return wnposs.get(node);
		return new ArrayList<POS>();
	}
	// grammatical structures
	public GrammaticalStructure getGrammaticalStructureForNode(ArrayList<String> node) {
		if (gmstructs.containsKey(node)) return gmstructs.get(node);
		return null;
	}
	
	
	/** PUBLIC METHODS =================================================== */
	
	public void load() {
		
		if (!loaded) {
			try {
				// WordNet initialization
				// MacOS-like Configuration
				
				
				//JWNL.initialize(new FileInputStream("/Users/Javier/Eclipse/eclipse-luna-jee-32/workspace/jwnl14-rc2/config/file_properties.xml"));
				
				
				// Windows Initialization
				String path ="C:\\Users\\jeppa\\eclipse-workspace\\linguistic\\linguistic\\lib\\jwnl14-rc2\\config\\file_properties.xml";
				 JWNL.initialize(new FileInputStream(path));


				
				// Stanford pipeline initialization
				Properties props = new Properties();
				props.put("annotators", "tokenize, ssplit, pos, lemma");
				pipeline = new StanfordCoreNLP(props);
				
				// Lexical analyzing
				lexicalizedParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
				tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
				languagePack = new PennTreebankLanguagePack();
				factory = languagePack.grammaticalStructureFactory();
				
				// loading done
				System.out.println("INFO: Dictionaries successfuly loaded");
				loaded = true;
				
			} catch (FileNotFoundException excep){
				System.err.println("Dictionaries : Unable to find the JWNL properties file");
				System.err.println(excep.getMessage());
			} catch (JWNLException excep) {
				System.err.println("Dictionaries : WordNet initialization error");
				System.err.println(excep.getMessage());
			}
		}
	}
	
	/**
	 * Parse, analyzes the given URI and set up the internal informations about this URI
	 * @param uri : the URI to analyze
	 * @throws JWNLException 
	 */
	public void analyzeURI(String eep) throws JWNLException {
		
		this.eep = eep;
		// initialization
		resetInformations(eep);
		
		// process each node
		for (ArrayList<String> node : parser.getNodes()) {
			// recreates the sentence
			String sentence = new String();
			for (String word : node) {
				sentence += word + " ";
			}
			PartOfSpeechAnalyze(node, sentence);
			lexicalAnalyze(node, sentence);
		}
	}
	
	/**
	 * Creates a list of synsets which contains every associated synsets to this word
	 * @param lemma
	 * @param pos
	 * @return
	 */
	public ArrayList<Synset> getSenses(String lemma, POS pos) {
		
		ArrayList<Synset> results = new ArrayList<Synset>();
		
		// retrieves the Wordnet dictionary
		Dictionary dic = Dictionary.getInstance();
		
		try {
			IndexWord index = dic.getIndexWord(pos, lemma);
			
			// retrieves synsets
			if (index != null) {
				Synset[] senses = index.getSenses();
				for (int i = 0; i < senses.length; i++) {
					results.add(senses[i]);
				}
			}
			
		} catch (JWNLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return results;
	}
	
	/**
	 * Checks if a word is a verb by its Wordnet index word
	 * Only word with verb index word and no other will be considered as verbs
	 * @param word
	 * @return
	 */
	public boolean wordIsVerbByWN(String word) {
		
		// Wordnet dictionary
		Dictionary dic = Dictionary.getInstance();
		// checks index words
		IndexWord iVerb = null, iNoun = null, iAdverb = null, iAdjective = null;
		try {
			iVerb = dic.getIndexWord(POS.VERB, word);
			iNoun = dic.getIndexWord(POS.NOUN, word);
			iAdverb = dic.getIndexWord(POS.ADVERB, word);
			iAdjective = dic.getIndexWord(POS.ADJECTIVE, word);
		} catch (JWNLException execpt) {
			execpt.printStackTrace();
		}
		
		if (iVerb != null && iNoun == null && iAdverb == null && iAdjective == null) return true;
		return false;
	}
	
	/**
	 * Get the list of synsets for a given node
	 * @param node
	 * @return the concatenation of the synset list of each word present into the node
	 */
	public ArrayList<Synset> getWNSenses(ArrayList<String> node) {
		
		ArrayList<Synset> result = new ArrayList<Synset>();
		
		// retrieves the Wordnet dictionary
		Dictionary dic = Dictionary.getInstance();
				
		// retrieves the index
		ArrayList<POS> possForNode = wnposs.get(node);
		ArrayList<String> lemmasForNode = lemmas.get(node);
		if(possForNode != null){
			
		for (int i = 0; i < possForNode.size(); i++) {
			
			POS pos = possForNode.get(i);
			String lemma = lemmasForNode.get(i);
	
			// retrieves the index word
			try {
				IndexWord index = dic.getIndexWord(pos, lemma);
				
				// retrieves synsets
				if (index != null) {
					Synset[] senses = index.getSenses();
					for (int j = 0; j < senses.length; j++) {
						result.add(senses[j]);
					}
				}
			} catch (JWNLException execpt) {
				
			}
		}	
		}
		return result;	
	}
	
	/**
	 * Get the list of other synsets for a given node
	 * Those synset will be found for every other WordNet POS than the one found by Stanford
	 * @param node
	 * @return
	 */
	public ArrayList<Synset> getWNOtherSenses(ArrayList<String> node) {
		
		ArrayList<Synset> result = new ArrayList<Synset>();
		
		// retrieves the Wordnet dictionary
		Dictionary dic = Dictionary.getInstance();
		
		// retrieves the index
		ArrayList<POS> possForNode = wnposs.get(node);
		ArrayList<String> lemmasForNode = lemmas.get(node);
		for (int i = 0; i < possForNode.size(); i++) {
			
			POS pos = possForNode.get(i);
			ArrayList<POS> otherPOSs = Dictionaries.getWNOtherPartOfSpeechs(pos);
			String lemma = lemmasForNode.get(i);
	
			// retrieves the index word
			try {
				for (POS otherPOS : otherPOSs) {
					
					IndexWord index = dic.getIndexWord(otherPOS, lemma);
					
					// retrieves synsets
					if (index != null) {
						Synset[] senses = index.getSenses();
						for (int j = 0; j < senses.length; j++) {
							result.add(senses[j]);
						}
					}
				}
				
			} catch (JWNLException execpt) {
				
			}
		}	
		return result;	
	}
	
	/** PRIVATE METHODS ================================================== */
	
	/**
	 * Resets information with a new URI to analyze
	 * @param uri
	 * @throws JWNLException 
	 */
	private void resetInformations(String eep) throws JWNLException {
		
		if (eep.length() > 0) {
			OutputFormatter.getInstance().printDebugInformation("Dictionaries", "New entity end point", 
					"================================================================================");
		}
		
		// looks for version
		String uri = base + eep;
		version = VersionningMetric.filterVersion(uri);
		if (!version.isEmpty()) {
			OutputFormatter.getInstance().printDebugInformation("Dictionaries", "Version found", version);
			uri = uri.replace(version, "");
			versionned = true;
		}
		
		// parse the path
		parser = new URIParser();
		parser.parse(eep); //uri
		if (eep.length() > 0) {
			// DEBUG
			String informations = new String();
			informations += "URI without parameters : " + parser.getURIWithoutParameter() + "\n";
			informations += "Nodes : ";
			for (ArrayList<String> node : parser.getNodes()) {
				informations += "/ ";
				for (String word : node) {
					informations += word + " ";
				}
			}
			informations += "\n";
			informations += "Parameters : ";
			for (String key : parser.getParameters().keySet()) {
				String value = parser.getParameters().get(key);
				informations += key + "=" + value + " ";
			}
			OutputFormatter.getInstance().printDebugInformation("Dictionaries", "Parsing done", informations);
		}
		// empty maps
		lemmas = new HashMap<ArrayList<String>, ArrayList<String>>();
		sfposs = new HashMap<ArrayList<String>, ArrayList<String>>();
		wnposs = new HashMap<ArrayList<String>, ArrayList<POS>>();
		gmstructs = new HashMap<ArrayList<String>, GrammaticalStructure>();
	}
	
	/**
	 * Creates the POS tagging for the given node
	 * @param node
	 * @param sentence
	 */
	private void PartOfSpeechAnalyze(ArrayList<String> node, String sentence) {
		
		// annotates it
		Annotation document = new Annotation(sentence);
		pipeline.annotate(document);
		
		// results for current node
		ArrayList<String> lemma = new ArrayList<String>();
		ArrayList<String> sfpos = new ArrayList<String>();
		ArrayList<POS> wnpos = new ArrayList<POS>();
		
		// retrieves tags
		List<CoreMap> results = document.get(SentencesAnnotation.class);
		for(CoreMap element: results) {
			for (CoreLabel token: element.get(TokensAnnotation.class)) {

				// - LEMMA
				lemma.add(token.lemma());
				// - POS
				String pos = token.get(PartOfSpeechAnnotation.class);  
				sfpos.add(pos);
				wnpos.add(Dictionaries.convertSFPOSToWNPOS(pos));
			}
		}
		
		// add node
		lemmas.put(node, lemma);
		sfposs.put(node, sfpos);
		wnposs.put(node, wnpos);
	}
	
	/**
	 * Creates the grammatical structure for the given node
	 * @param node
	 * @param sentence
	 */
	private void lexicalAnalyze(ArrayList<String> node, String sentence) {

		// tokenize
		List<CoreLabel> coreLabels = tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize();
		// create the grammatical structure
		Tree tree = lexicalizedParser.apply(coreLabels);
		GrammaticalStructure gmstruct = factory.newGrammaticalStructure(tree);
		gmstructs.put(node, gmstruct);
	}
	
	
	/**
	 * Convert a Part Of Speech from Stanford to WordNet
	 * @param pos
	 * @return
	 */
	private static POS convertSFPOSToWNPOS(String pos) {
		
		if (pos.contains("VB")) {
			return POS.VERB;
		} else if (pos.contains("NN")) {
			return POS.NOUN;
		} else if (pos.contains("RB")) {
			return POS.ADVERB;
		} else if (pos.contains("JJ")) {
			return POS.ADJECTIVE;
		} else { // default
			return POS.NOUN;
		}
	}
	
	/**
	 * Create a list of POSs that are not the one given
	 * @param pos
	 * @return
	 */
	private static ArrayList<POS> getWNOtherPartOfSpeechs(POS pos) {
		
		ArrayList<POS> result = new ArrayList<POS>();
		boolean verb = true, adjective = true, adverb = true, noun = true;
		if (pos == POS.VERB) verb = false;
		else if (pos == POS.ADJECTIVE) adjective = false;
		else if (pos == POS.ADVERB) adverb = false;
		else if (pos == POS.NOUN) noun = false;
	
		if (verb) result.add(POS.VERB);
		if (adjective) result.add(POS.ADJECTIVE);
		if (adverb) result.add(POS.ADVERB);
		if (noun) result.add(POS.NOUN);
		
		return result;
	}
}
