
package com.mallet;

import com.opencsv.CSVWriter;
import de.linguatools.disco.CorruptConfigFileException;
import de.linguatools.disco.DISCO;
import de.linguatools.disco.WrongWordspaceTypeException;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;


public class RestAnalyser {
    public static boolean verbose = true;
    LdaProcessor newldaProcessor = new LdaProcessor();
    private ArrayList<String> apiDescription;
    private double sementicDistance;
    private double sementicThreshold;
    private ArrayList<Topic> topics;
    private boolean isLemm;
    private int nbrTopics;
    private ArrayList<String> stopWords;
    private ArrayList<String> noStopWords;
    private HashMap<String, ArrayList<String>> acronyms;
    private ArrayList<String[]> csvResultsLines;
    private ArrayList<String[]> csvCohesiveResultsLines;
    private TextTool textTool;
    private LdaProcessor ldaProcessor;
    private String contexttracesFile;
    private String cohesivtracesFile;
    private String contextresultsFile;
    private String cohesivresultsFile;

    public java.lang.String getContextualPatternCount() {
        return Integer.toString(contextualPatternCount);
    }

    public void setContextualPatternCount(int contextualPatternCount) {
        this.contextualPatternCount = contextualPatternCount;
    }

    private int contextualPatternCount;

    public String getCohesivePatternCount() {
        return Integer.toString(cohesivePatternCount);
    }

    public void setCohesivePatternCount(int cohesivePatternCount) {
        this.cohesivePatternCount = cohesivePatternCount;
    }

    private int cohesivePatternCount;
    private double threshold = 0.1;
    private long APDectTime;
    private long PDectTime;
    private long NDectTime;
    private DISCO disco;

    public RestAnalyser(String api) {
        APDectTime = 0L;
        PDectTime = 0L;
        NDectTime = 0L;
        contextualPatternCount = 0;
        cohesivePatternCount = 0;

        noStopWords = new ArrayList<String>();
        stopWords = new ArrayList<String>();
        textTool = new TextTool();
        this.ldaProcessor = new LdaProcessor();
        apiDescription = new ArrayList<String>();
        System.err.println("Creating DISCO Distributional Similarity Calculator");
        try {
            disco = new DISCO("/Users/jeshag/eclipse-workspace/enwiki-20130403-sim-lemma-mwl-lc", false);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CorruptConfigFileException e) {
            e.printStackTrace();
        }
        System.err.println("DISCO Object Created");
        System.out.flush();
        System.err.flush();
        InitialiseTraceFile();
        InitialiseResultsFile();
    }

    public ArrayList<String[]> getCsvResultsLines() {
        return csvResultsLines;
    }

    public ArrayList<String[]> getCsvCohesiveResultsLines() {
        return csvCohesiveResultsLines;
    }

    public String getContexttracesFile() {
        return contexttracesFile;
    }

    public String getContextresultsFile() {
        return contextresultsFile;
    }

    // methods for the less cohesive documentation antipattern:

    public String getCohesivresultsFile() {
        return cohesivresultsFile;
    }

    public ArrayList<Topic> CreateTopicModel2(int nbrTopics, String uri, String docs) {

        System.err.println("Creating LDA Topic Model for URI: " + uri);

        RestAnalyserResult result = null;

        if (GetURIDocumentation.uriDocumentation.size() == 0) {
            return null;
        }

        newldaProcessor.addSourceDoc(docs);
        newldaProcessor.AddStopWords(stopWords);
        newldaProcessor.RemoveStopWords(noStopWords);
        newldaProcessor.setProcessLemm(isLemm);
        newldaProcessor.setNumTopics(nbrTopics);
        newldaProcessor.setNbrtopword(30);

        try {
            newldaProcessor.processText();
        } catch (IOException e) {
            e.printStackTrace();

        }

        return newldaProcessor.getaTopicList();

    }

    public void printTopicModel2(String uri) {
        topics = this.newldaProcessor.getaTopicList();
        int i = 0;
        for (Topic topic : topics) {
            System.out.println("Topic " + i + " for URI " + uri);
            for (TopicWord topicWord : topic.getTopicWords()) {
                System.out.println(topicWord.getWord().toString());
            }

            i++;
        }
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

    }

    public void saveTopicModelCSV2(String uri, String docs) {
        File apiDirectory = new File(".uri_doc/topic_models/" + RestTest.selectedAPI + uri.replaceAll("/", "-"));
        if (!apiDirectory.exists()) {
            // creates a new folder
            apiDirectory.mkdir();
        }
        if (apiDirectory.isDirectory()) {
            // Date date = new Date();
            // DateFormat day = new SimpleDateFormat("yy-MM-dd");
            // DateFormat hour = new SimpleDateFormat("HH-mm");
            String fileName = ".uri_doc/topic_models/" + RestTest.selectedAPI + "/" + "topics_"
                    + uri.replaceAll("/", "-")// day.format(date) + "_"
                    // + hour.format(date)
                    + ".csv";
            try {

                // creates the file
                CSVWriter writer = new CSVWriter(new FileWriter(fileName), ',');
                String[] line = new String[11];
                int i = 0;
                for (Topic topic : topics) {

                    line[0] = "Topic " + i;
                    int j = 1;
                    for (TopicWord topicWord : topic.getTopicWords()) {
                        line[j] = topicWord.getWord().toString();
                        j++;
                    }
                    i++;
                    writer.writeNext(line);
                }
                writer.close();

            } catch (IOException except) {

            }

        }
    }

    private void InitialiseTraceFile() {

        String tracesFolder = System.getProperty("user.dir") + "/traces/";
        File apiDirectory = new File(tracesFolder + RestTest.selectedAPI);
        if (!apiDirectory.exists()) {
            // creates a new folder
            apiDirectory.mkdirs();
        }
        if (apiDirectory.isDirectory()) {
            Date date = new Date();
            DateFormat day = new SimpleDateFormat("yy-MM-dd");
            DateFormat hour = new SimpleDateFormat("HH");
            contexttracesFile = tracesFolder + RestTest.selectedAPI + "/" + "traces_" + day.format(date) + "_"
                    + hour.format(date) + ".csv";
//            try {
//
//                // creates the file
//                this.contexttracesFile = new CSVWriter(new FileWriter(fileName), ',');
//                System.err.println("Traces file created");
//
//            } catch (IOException except) {
//                System.err.println("Error creating traces file");
//            }
        }

    }

    private void InitialiseResultsFile() {

        File apiDirectory = new File("./results/" + RestTest.selectedAPI + "/Context/");

        csvResultsLines = new ArrayList<String[]>();
        csvCohesiveResultsLines = new ArrayList<String[]>();
        String[] resultsLine = new String[7];
        resultsLine[0] = "Uri";
        resultsLine[1] = "Contextual?";
        resultsLine[2] = "Topic Number";
        resultsLine[3] = "Similarity";
        resultsLine[4] = "Topic wordset";
        resultsLine[5] = "Detection Time (ms)";
        resultsLine[6] = "Detection Time";
        csvResultsLines.add(resultsLine);
        csvCohesiveResultsLines.add(new String[]{"Uri","Cohesive?", "Topic Number", "Similarity", "Topic wordset", "Detection Time (ms)", "Detection Time"});
        if (!apiDirectory.exists()) {
            // creates a new folder
            apiDirectory.mkdirs();
        }
        apiDirectory = new File("./results/" + RestTest.selectedAPI + "/Cohesive/");
        if (!apiDirectory.exists()) {
            // creates a new folder
            apiDirectory.mkdirs();
        }
        if (apiDirectory.isDirectory()) {
            Date date = new Date();
            DateFormat day = new SimpleDateFormat("yy-MM-dd");
            DateFormat hour = new SimpleDateFormat("HH");
            contextresultsFile = "./results/" + RestTest.selectedAPI + "/Context/" + "results_" + day.format(date) + "_"
                    + hour.format(date) + ".csv";
            cohesivresultsFile = "./results/" + RestTest.selectedAPI + "/Cohesive/" + "CohesiveResults_" + day.format(date) + "_"
                    + hour.format(date) + ".csv";
        }

    }

    public void setVerbose(boolean verbose) {
        RestAnalyser.verbose = verbose;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public int uriSize(String Uri) {

        Uri = Uri.substring(Uri.indexOf(" ") + 1, Uri.length());

        if (Uri.indexOf("?") >= 0)
            Uri = Uri.substring(0, Uri.indexOf("?"));

        if (Uri.indexOf(".") >= 0)
            Uri = Uri.substring(0, Uri.indexOf("."));

        ArrayList<String> splitURI = new ArrayList<>(
                Arrays.asList(StringUtils.split(SplitCamelCase.splitCamelCase(Uri.trim()), "-:{}<>/_)(")));
        return splitURI.size();

    }

    public ArrayList<String> getUriNodes(String Uri) throws IOException {
        if (verbose)
            System.out.println(Uri);
        //Uri = Uri.substring(Uri.indexOf(" ") + 1, Uri.length());

        if (Uri.indexOf("?") >= 0)
            Uri = Uri.substring(0, Uri.indexOf("?"));

        if (Uri.indexOf(".") >= 0)
            Uri = Uri.substring(0, Uri.indexOf("."));

        // Alternative implementation controlling the nodes to add
        ArrayList<String> uriNodes = new ArrayList<>();
        ArrayList<String> nodesToRemove = new ArrayList<>();


        while (Uri.indexOf("_") > 0) {
            Uri = Uri.substring(0, Uri.indexOf("_")) + "/" + Uri.substring(Uri.indexOf("_") + 1, Uri.length());
        }

        ArrayList<String> tmp = new ArrayList<>(Arrays.asList(StringUtils.split(SplitCamelCase.splitCamelCase(Uri.trim()), "-:{}<>/_)(")));
       
        ArrayList<String> splitURI = new ArrayList<>();
        
        for(String nodes: tmp){
        	String[] tmp1 = nodes.split(" ");
        	List<String> temp = Arrays.asList(tmp1);
        	splitURI.addAll(temp);
        }

        for (String Node : splitURI) {
            if (acronyms.containsKey(Node.trim())) {
                uriNodes.addAll(acronyms.get(Node.trim()));
            } else {           	
                if ((disco.frequency(Node.trim()) == 0)) {
                    ArrayList<String> splitNodes = textTool.camelCaseSplit(Node.trim());
                    if(splitNodes.size() > 1) {
                    for (String splitNode : splitNodes)
                        uriNodes.add(splitNode.toLowerCase().trim());
                    } else {
                    	String word = Node.trim();
                    	String wordSingular = "";
                    	// Check if word might be plural
                    	if (word.charAt(word.length() - 1) == 's') {
                    		wordSingular = word.substring(0, word.length() - 1);
                    	};
                    	// If word not exist see if singular word exist
                    	if(disco.frequency(word) > 0 || disco.frequency(wordSingular) > 0) {
                    		uriNodes.add(word);
                    	} else {
                    		// For each character in the word
                    		for(int i = 0; i < word.length(); i++) {
                    			// If word is not found after checking all
                    			// characters, add word to node list
                    			if (i == word.length() - 1) {
                    				uriNodes.add(word);
                    				break;
                    			}
                    			// Break the word in two, 
                    			String subWord1 = word.substring(0, i + 1);
                    			String subWord2 = word.substring((i + 1), word.length());

                    			// If the first word is not found in the dictionary 
                    			// continue loop.
                    			if (disco.frequency(subWord1.trim()) == 0) {
                    				continue;
                    			}
                    			
                    			// If first and second words are found, add both 
                    			// to nod list and end the loop
                    			if(disco.frequency(subWord2.trim()) > 0) {
                    				uriNodes.add(subWord1);
                    				uriNodes.add(subWord2);
                    				break;
                    			}
                    		}
                    	}
                    }
                } else {
                    uriNodes.add(Node.toLowerCase().trim());
                }
            }

        }

        for (String node : uriNodes) {

            if (node.trim().length() == 0) {
                nodesToRemove.add(node);
            } else {
                if (((stopWords.contains(node.trim())) && (!noStopWords.contains(node.trim()))) || (textTool.isStringNumeric(node.trim()))) {
                    nodesToRemove.add(node.trim());

                }
            }

        }
        uriNodes.removeAll(nodesToRemove);

        return uriNodes;

    }

	/*
     * public String getApiDescription() { return apiDescription; }
	 */

    public void addApiDescription(String apiDescription) {
        this.apiDescription.add(apiDescription);
        if (verbose)
            System.out.println(apiDescription);
    }

    public void readAllAPIDescription() throws IOException {

        File folder = new File("./API_DOC_short/" + RestTest.selectedAPI);
        File[] listOfFiles = folder.listFiles();
        String line;
        BufferedReader reader;
        for (File file : listOfFiles) {
            if ((file.isFile()) && (file.getName().contains("txt"))
                    && (!((file.getName().contains("APIIndex")) || (file.getName().contains("APITest"))))) {

                StringBuilder stringBuilder = new StringBuilder();

                reader = new BufferedReader(
                        new FileReader(RestTest.docPath + RestTest.selectedAPI + "/" + file.getName()));
                try {

                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    addApiDescription(stringBuilder.toString());

                } finally {
                    reader.close();
                }

                // System.out.println("Processing Content: " + file.getName());
            }
        }

    }

    public int obtainNumberOfTopics() throws IOException {
        System.err.println("Calculating number of topics for " + RestTest.selectedAPI + " at " + RestTest.docPath
                + RestTest.selectedAPI + "/APIIndex.txt");
        System.err.flush();
        System.out.flush();
        BufferedReader reader = new BufferedReader(
                new FileReader(RestTest.docPath + RestTest.selectedAPI + "/APIIndex.txt"));
        ArrayList<String> APITopics = new ArrayList<String>();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.substring(line.indexOf(" ") + 1, line.length());
                if (verbose)
                    System.out.println(line);
                ArrayList<String> nodes = new ArrayList<String>(
                        Arrays.asList(StringUtils.split(SplitCamelCase.splitCamelCase(line.trim()), "-:{}<>/_)(")));

                if (!APITopics.contains(nodes.get(0).trim())) {

                    APITopics.add(nodes.get(0).trim());
                }

            }
        } finally {
            reader.close();
        }

        for (String APITopic : APITopics) {
            System.out.println("APITopic: " + APITopic);
        }
        System.out.println(APITopics.size() + " Topics detected");
        System.err.flush();
        System.out.flush();
        return APITopics.size();
    }

    public double getSementicDistance() {
        return sementicDistance;
    }

    public void setSementicDistance(double sementicDistance) {
        this.sementicDistance = sementicDistance;
    }

    public double getSementicThreshold() {
        return sementicThreshold;
    }

    public void setSementicThreshold(double sementicThreshold) {
        this.sementicThreshold = sementicThreshold;
    }

    public ArrayList<Topic> getTopics() {
        return topics;
    }

    public void setTopics(ArrayList<Topic> topics) {
        this.topics = topics;
    }

    public boolean isLemm() {
        return isLemm;
    }

    public void setIsLemm(boolean isLemm) {
        this.isLemm = isLemm;
    }

    public int getNbrTopics() {
        return nbrTopics;
    }

    public void setNbrTopics(short nbrTopics) {
        this.nbrTopics = nbrTopics;
    }

    public ArrayList<String> getStopWords() {
        return stopWords;
    }

    public void setStopWords(ArrayList<String> stopWords) {
        this.stopWords = stopWords;
    }

    public ArrayList<String> getNoStopWords() {
        return noStopWords;
    }

    public void setNoStopWords(ArrayList<String> noStopWords) {
        this.noStopWords = noStopWords;
    }

    public ArrayList<Topic> CreateTopicModel(int nbrTopics) {
        System.err.println("Creating LDA Topic Model");
        this.nbrTopics = nbrTopics;
        RestAnalyserResult result = null;

        if (apiDescription == null || apiDescription.size() == 0) {
            return null;
        }

        // (JGH) with this it passes more than one description
        for (String description : apiDescription) {
            this.ldaProcessor.addSourceDoc(description);
        }

        this.ldaProcessor.AddStopWords(stopWords);
        this.ldaProcessor.RemoveStopWords(noStopWords);
        this.ldaProcessor.setProcessLemm(isLemm);
        this.ldaProcessor.setNumTopics(nbrTopics);

        try {
            this.ldaProcessor.processText();
        } catch (IOException e) {
            e.printStackTrace();

        }

        return this.ldaProcessor.getaTopicList();

    }

    public void printTopicModel() {
        topics = this.ldaProcessor.getaTopicList();
//        int i = 0;
//        for (Topic topic : topics) {
//            System.out.println("--------------  Topic " + i + " ----------------------------");
//            for (TopicWord topicWord : topic.getTopicWords()) {
//                System.out.println(topicWord.getWord().toString());
//            }
//
//            i++;
//        }
//        System.out.println("-----------------------------------------------------");

    }

    public void saveTopicModelCSV() {
        File apiDirectory = new File("./topic_models/" + RestTest.selectedAPI);
        if (!apiDirectory.exists()) {
            // creates a new folder
            apiDirectory.mkdirs();
        }
        if (apiDirectory.isDirectory()) {
            Date date = new Date();
            DateFormat day = new SimpleDateFormat("yy-MM-dd");
            DateFormat hour = new SimpleDateFormat("HH");
            String fileName = "./topic_models/" + RestTest.selectedAPI + "/" + "topics_" + day.format(date) + "_"
                    + hour.format(date) + ".csv";
            try {
                // creates the file
                CSVWriter writer = new CSVWriter(new FileWriter(fileName), ',');
                String[] line = new String[ldaProcessor.getNbrtopword()+1];
                int i = 0;
                for (Topic topic : topics) {
                    line[0] = "Topic " + i;
                    int j = 1;
                    for (TopicWord topicWord : topic.getTopicWords()) {
                        line[j] = topicWord.getWord().toString();
                        j++;
                    }
                    i++;
                    writer.writeNext(line);
                }
                writer.close();

            } catch (IOException except) {
            }

        }
    }

    public int URIContextualAnalysis(String Uri) throws IOException {

        Long dectTime = System.currentTimeMillis();
        topics = this.ldaProcessor.getaTopicList();
        ArrayList<ArrayList<String>> TopicText = ldaProcessor.getTopicStringList();


        // System.out.println("URI: "+Uri);
        if(Uri.equals("/users/<userID>/properties"))
        	System.out.println();
        ArrayList<String> UriNodes = getUriNodes(Uri);
        String[] traceUri = new String[1];
        traceUri[0] = Uri;
//        contexttracesFile.writeNext(traceUri);
        String[] resultsLine = new String[7];
        if ((UriNodes.size() >= 1) && Uri.length() >= 1) {
            float[][] results_matrix = new float[UriNodes.size()][nbrTopics];
            try {

                float sim = 0;
                int node = 0;
                for (String s : UriNodes) {
                    String[] tracesLine = new String[nbrTopics + 1];
                    tracesLine[0] = s;
                    if (verbose) {
                        System.out.println(
                                "-------------- Node:" + textTool.getLemma(s) + "  ----------------------------");
                    }
                    int numTopic = 0;
                    for (Topic topic : topics) {
                        float max_sim = 0;
                        if (verbose)
                            System.out.println("--------- " + textTool.getLemma(s) + "  vs Topic: " + numTopic
                                    + "------------------");
                        for (TopicWord topicWord : topic.getTopicWords()) {
                            try {
                                // sim =
                                // disco.semanticSimilarity(textTool.getLemma(s),
                                // topicWord.getRootWord(),
                                // DISCO.SimilarityMeasure.COSINE);
                                if (textTool.getLemma(s).equals(topicWord.getRootWord()))
                                    sim = 2;
                                
                                else{
                                    sim = disco.secondOrderSimilarity(textTool.getLemma(s), topicWord.getRootWord());
                                    System.err.println( textTool.getLemma(s) + "\t" + topicWord.getRootWord() + "\t" + sim);
                                }

                            } catch (WrongWordspaceTypeException e) {
                                e.printStackTrace();
                            }
                            if (verbose)
                                System.out.println(topicWord.getRootWord() + " : " + sim);
                            if (sim > threshold) {
                                if (sim > max_sim)
                                    max_sim = sim;
                            }

                        }
                        if (max_sim > threshold) {
                            results_matrix[node][numTopic] = max_sim;

                        } else
                            results_matrix[node][numTopic] = (float) 0.0;
                        tracesLine[numTopic + 1] = Float.toString(results_matrix[node][numTopic]);
                        if (verbose) {
                            System.out.println("\nMax Result Topic " + numTopic + ":      " + max_sim + "\n");

                        }
                        numTopic++;
                    }
                    node++;
//                    tracesWriter.writeNext(tracesLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            boolean same_context = false;
            float highestSimilarity = -3.0F;
            int best_topic = -1;
            for (int i = 0; i < nbrTopics; i++) {
                same_context = true;

                float averageSimilarity = 0.0F;

                for (int j = 0; j < UriNodes.size(); j++) {
                    averageSimilarity += results_matrix[j][i];
                    if (results_matrix[j][i] == (float) 0.0)
                        same_context = false;
                    // else
                    // same_context = true;

                }
                averageSimilarity = averageSimilarity / UriNodes.size();
                if (same_context) {
                    if (averageSimilarity > highestSimilarity) {
                        highestSimilarity = averageSimilarity;
                        best_topic = i + 1;
                    }
                }

            }

            dectTime = System.currentTimeMillis() - dectTime;
            
            System.out.println("Matrix values for: " + Uri);
            
            for(int i=0; i < results_matrix.length; i++)
            	System.out.println(Arrays.toString(results_matrix[i]));
            
            if (best_topic == -1) {
                System.out.println("ContextLess: No Context detected " + UriNodes.toString() + " in URI: " + Uri);
                resultsLine[0] = Uri;
                resultsLine[1] = highestSimilarity > threshold ? "Yes" : "No";
                resultsLine[2] = "No";
                resultsLine[3] = "0.0";
                resultsLine[4] = "N/A";
                resultsLine[5] = "N/A";
                resultsLine[6] = ((Float) (0.0001F * dectTime)).toString();
                csvResultsLines.add(resultsLine);
                APDectTime += dectTime;
                
                return 1;

            } else {
                System.out.println("Contextual Nodes: Context detected for nodes: " + UriNodes.toString()
                        + " in Topic: " + (best_topic) + " with average similarity between nodes =" + highestSimilarity
                        + " in URI: " + Uri);
                resultsLine[0] = Uri;
                resultsLine[1] = highestSimilarity > threshold ? "Yes" : "No";
                resultsLine[2] = ((Integer) (best_topic)).toString();
                resultsLine[3] = ((Float) highestSimilarity).toString();
                resultsLine[4] = TopicText.get(best_topic - 1).toString();
                resultsLine[5] = dectTime.toString();
                resultsLine[6] = ((Float) (0.0001F * dectTime)).toString();
                csvResultsLines.add(resultsLine);
                PDectTime += dectTime;
                contextualPatternCount++;
                return 0;
            }
            
            
        }
		return -1;
    }

    public HashMap<String, ArrayList<String>> getAcronyms() {
        return acronyms;
    }

    public void setAcronyms(HashMap<String, ArrayList<String>> acronyms) {
        this.acronyms = acronyms;
    }

    public long getAPDectTime() {
        return APDectTime;
    }

    public void setAPDectTime(long APDectTime) {
        this.APDectTime = APDectTime;
    }

    public long getPDectTime() {
        return PDectTime;
    }

    public void setPDectTime(long PDectTime) {
        this.PDectTime = PDectTime;
    }

    public long getNDectTime() {
        return NDectTime;
    }

    public void setNDectTime(long NDectTime) {
        this.NDectTime = NDectTime;
    }

    public int LessCohesiveDocumentationAnalysis(int numTopics, String Uri, String docs) {

        // System.err.println("Detection of Less Cohesive Documentation
        // Antipattern: ");
        Long dectTime = System.currentTimeMillis();
//        topics = this.newldaProcessor.getaTopicList();

        String[] UriNodes = null;
        UriNodes = StringUtils.split(SplitCamelCase.splitCamelCase(Uri.trim()), "-:{}<>/_)( ");
        String[] traceUri = new String[1];
        String [] text = StringUtils.split(docs," ");
        ArrayList<String> lemmText = new ArrayList<>();
        traceUri[0] = Uri;
        newldaProcessor.setNbrtopword(25);
        ArrayList<ArrayList<String>> TopicText =  newldaProcessor.getTopicStringList();
        String[] resultsLine = new String[7];

		/*
         * int i=0; for (String node:UriNodes) { traceNodes[i]=node; i++; }
		 * contexttracesFile.writeNext(traceNodes);
		 */

        for (String word : text) {
            lemmText.add(textTool.getLemma(word));
        }
        java.lang.String corpus = StringUtils.join(lemmText.toArray(), " ");
        if ((UriNodes.length >= 1)
                && StringUtils.split(SplitCamelCase.splitCamelCase(Uri.trim()), "-:{}<>/_)(").length >= 1) {
            float[][] results_matrix = new float[UriNodes.length][numTopics];
            try {

                float sim = 0;
                int node = 0;
                for (String s : UriNodes) {
                    String[] tracesLine = new String[numTopics + 1];
                    tracesLine[0] = s;
                    if (verbose) {
                        System.out.println(
                                "-------------- Node:" + textTool.getLemma(s) + "  ----------------------------");
                    }
                    int numTopic = 0;
//                    for (Topic topic : topics) {
//                        float max_sim = 0;
//                        if (verbose)
//                            System.out.println("--------- " + textTool.getLemma(s) + "  vs Topic: " + numTopic
//                                    + "------------------");
                    float max_sim = 0;
                    for (String topicWord : text) {
                            try {
                                // sim =
                                // disco.semanticSimilarity(textTool.getLemma(s),
                                // topicWord.getRootWord(),
                                // DISCO.SimilarityMeasure.COSINE);
                                if (textTool.getLemma(s).equals(textTool.getLemma(topicWord)))
                                    sim = 2;
                                else
                                    sim = disco.secondOrderSimilarity(textTool.getLemma(s), textTool.getLemma(topicWord));

                            } catch (WrongWordspaceTypeException e) {
                                e.printStackTrace();
                            }
                            if (verbose)
                                System.out.println(textTool.getLemma(topicWord) + " : " + sim);
                            if (sim > threshold) {
                                if (sim > max_sim)
                                    max_sim = sim;
                            }

                        }
                        if (max_sim > threshold) {
                            results_matrix[node][numTopic] = max_sim;
                        } else
                            results_matrix[node][numTopic] = (float) 0.0;
                        tracesLine[numTopic + 1] = Float.toString(results_matrix[node][numTopic]);
                        if (verbose) {
                            System.out.println("\nMax Result Topic " + numTopic + ":      " + max_sim + "\n");

                        }
                        numTopic++;
                    //}

                    node++;
//                    csvCohesiveResultsLines.add(tracesLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            boolean same_context;
            float highestSimilarity = -3.0F;
            int best_topic = -1;
            for (int i = 0; i < numTopics; i++) {
                same_context = true;

                float averageSimilarity = 0.0F;

                for (int j = 0; j < UriNodes.length; j++) {
                    averageSimilarity += results_matrix[j][i];
                    if (results_matrix[j][i] == (float) 0.0)
                        same_context = false;

                }
                averageSimilarity = averageSimilarity / UriNodes.length;
                if (same_context) {
                    if (averageSimilarity > highestSimilarity) {
                        highestSimilarity = averageSimilarity;
                        best_topic = i + 1;

                    }
                }
            }

            dectTime = System.currentTimeMillis() - dectTime;
            if (best_topic == -1) {
                System.err.println("***Less Cohesive Documentation detected " + " between URI: " + Uri
                        + "and Documentation: [" + docs + "] ");
                resultsLine[0] = Uri;
                resultsLine[1] = "No";
                resultsLine[2] = "N/A";
                resultsLine[3] = ((Float) highestSimilarity).toString();
                resultsLine[4] = docs;
                resultsLine[5] = dectTime.toString();
                resultsLine[6] = ((Float) (0.0001F * dectTime)).toString();
                csvCohesiveResultsLines.add(resultsLine);

                APDectTime += dectTime;

                return 1;
            } else {
                System.err.println("Cohesive Documentation detected " + "between URI: " + Uri + "and Documentation: ["
                        + docs + "] " + " in Topic: " + best_topic + " with average similarity between nodes ="
                        + highestSimilarity);
                resultsLine[0] = Uri;
                resultsLine[1] = highestSimilarity > threshold ? "Yes" : "No";
                resultsLine[2] = ((Integer) (best_topic)).toString();
                resultsLine[3] = ((Float) highestSimilarity).toString();
                resultsLine[4] = docs;
                resultsLine[5] = dectTime.toString();
                resultsLine[6] = ((Float) (0.0001F * dectTime)).toString();

                csvResultsLines.add(resultsLine);

                csvCohesiveResultsLines.add(resultsLine);
                PDectTime += dectTime;
                cohesivePatternCount++;
                return 0;
            }
        }
		return -1;

    }
}
