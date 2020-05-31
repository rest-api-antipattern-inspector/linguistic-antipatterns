/*The main class: */
package com.mallet;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

import com.opencsv.CSVWriter;
import com.sofa.helper.ServiceHelper;
import com.sofa.helper.ServiceHelper.HTTPMethod;
import com.sofa.metric.LexicalResult;
import com.sofa.metric.lexical.Dictionaries;
import com.sofa.metric.lexical.HierarchicalMetrics;
import com.sofa.metric.lexical.LexicalMetrics;
import com.sofa.metric.lexical.parser.URIParser;
import com.sofa.util.OutputFormatter;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

/**
 * Created by Mohamed on 13-Oct-2015.
 */
public class RestTest {
	
	static final ArrayList<String> extensions = createExtensions();
	static final ArrayList<String> crudyWords = createCRUDyWords();
	
	public static List<String> lessCohesiveDocResultAP = new ArrayList();
	public static List<String> lessCohesiveDocResultP = new ArrayList();
	
	public static List<String> contextlessResultAP = new ArrayList();
	public static List<String> contextlessResultP = new ArrayList();
	
	public static List<String> pluralisedResultAP = new ArrayList();
	public static List<String> pluralisedResultP = new ArrayList();
	
	public static List<String> amorphusResultAP = new ArrayList();
	public static List<String> amorphusResultP = new ArrayList();
	
	public static List<String> unversionedResultAP = new ArrayList();
	public static List<String> versionedResultP = new ArrayList();

	public static List<String> crudyURIResultAP = new ArrayList();
	public static List<String> crudyURIResultP = new ArrayList();
	
	public static List<String> nonHierarchyResultAP = new ArrayList();
	public static List<String> nonHierarchyResultP = new ArrayList();
	
	public static List<String> inconsistentdocumentationAP = new ArrayList();
	public static List<String> inconsistentdocumentationP = new ArrayList();
	
    public static String selectedAPI;
    public static String docPath = "API_DOC_short/";

    public static String chooseAPI(ArrayList<String> APIs) {
        int number = 0;
        System.out.println("Choose API:");
        for (String API : APIs) {
            number++;
            System.out.println(number + " " + API);

        }
        Scanner in = new Scanner(System.in);
        int value = 0;
        String API = new String();
        while (value == 0) {
            value = in.nextInt();
            if ((value <= 0) || (value > number)) {
                value = 0;
                System.out.println("Value must be between 0 and " + (number + 1) + " (excluded)");
            } else {
                API = APIs.get(value - 1);
            }

        }

        return API;
    }

    public static ArrayList<String> getAPIs() throws IOException {
        ArrayList<String> APIs = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new FileReader("API_DOC_short/APIList.txt"));
        String line;
        try {

            while ((line = reader.readLine()) != null) {
                APIs.add(line);
            }

        } finally {
            reader.close();
        }
        return APIs;
    }

    public static void main(String[] args) throws IOException, JWNLException {
        long detectionTime = 0;
        CSVWriter summaryCsv = null;
        CSVWriter summaryCoCsv = null;
        Long execTime = System.currentTimeMillis();
        boolean writeheader, writeheaderCo;

        TextTool textTool = new TextTool();

        selectedAPI = chooseAPI(getAPIs());
        writeheader = new File("su1mmary.csv").exists();
        summaryCsv = new CSVWriter(new FileWriter("summary.csv", true), ',', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER);
        if (!writeheader) {
            summaryCsv.writeNext(new String[]{"API", "# Pattern Detected"});
        }

        writeheaderCo = new File("cohSummary.csv").exists();
        summaryCoCsv = new CSVWriter(new FileWriter("cohSummary.csv", true), ',', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER);
        if (!writeheaderCo) {
            summaryCoCsv.writeNext(new String[]{"API", "# Pattern Detected"});
        }
        summaryCoCsv.close();
        summaryCsv.close();

        ArrayList<String> stopword = textTool.FiletoStrings("stopword", "en.txt", false);


        ArrayList<String> apiSpecificStopWords = textTool.FiletoStrings("stopword", selectedAPI + ".txt", false);
        stopword.addAll(apiSpecificStopWords);
        System.out.println(Arrays.toString(stopword.toArray()));

        RestAnalyser restAnalyser = new RestAnalyser(selectedAPI);

        ArrayList<String> stopwordRemove = textTool.FiletoStrings("stopword/remove", selectedAPI + ".txt", false);
        HashMap<String, ArrayList<String>> acronyms = textTool.fileToBiDimensionalStringList("acronyms/",
                selectedAPI + ".txt");

        restAnalyser.setVerbose(false);
        restAnalyser.setIsLemm(true);
        restAnalyser.setStopWords(stopword);
        restAnalyser.setNoStopWords(stopwordRemove);
        restAnalyser.setAcronyms(acronyms);

        restAnalyser.readAllAPIDescription();

        int Topics = restAnalyser.obtainNumberOfTopics();
        restAnalyser.CreateTopicModel(Topics);
        restAnalyser.printTopicModel();
        restAnalyser.saveTopicModelCSV();
  
        
        Scanner reader = new Scanner(System.in);  // Reading from System.in
        System.out.println("1:AmorphousURI 2:CRUDyURI 3:NonHierarchicalNodes 4:PluralisedNodes 5:UnversionedURIs 6:LessCohesiveDoc 7:ContextlessResource 8:InconsistentDoc 9:All");
        int n = reader.nextInt(); // Scans the next token of the input as an int.
        
        long startTime = System.nanoTime();
        
        if(n==1){
        	/* Detection of AmorphousURI Antipattern */
        	detectAmorphousURI();
        	writeOutput(selectedAPI+"-"+"AmorphousURI.txt", amorphusResultAP, amorphusResultP);
        }
        
        else if(n==2){
        	/* Detection of CRUDyURI Antipattern */
        	detectCRUDyURI();
        	writeOutput(selectedAPI+"-"+"CRUDyURI.txt", crudyURIResultAP, crudyURIResultP);
        }
        
        else if(n==3){
        	/* Detection of NonHierarchicalNodes Antipattern */
        	detectNonHierarchicalNodes();
        	writeOutput(selectedAPI+"-"+"NonHierarchicalNodes.txt", nonHierarchyResultAP, nonHierarchyResultP);
        }
        
        else if(n==4){
        	/* Detection of PluralisedNodes Antipattern */
        	detectPluralisedNodes();
        	writeOutput(selectedAPI+"-"+"PluralisedNodes.txt", pluralisedResultAP, pluralisedResultP);
        }
        
        else  if(n==5){
        	/* Detection of UnversionedURIs Antipattern */
        	detectUnversionedURIs(restAnalyser, execTime, summaryCsv, detectionTime);
        	writeOutput(selectedAPI+"-"+"UnversionedURIs.txt", unversionedResultAP, versionedResultP);
        }
        
        else if(n==6){
        	/*  Less Cohesive Documentation Antipattern  */
        	detectLessCohesiveDocumentation(stopword, stopwordRemove, acronyms, restAnalyser, detectionTime, summaryCoCsv);
        	writeOutput(selectedAPI+"-"+"LessCohesiveDoc.txt", lessCohesiveDocResultAP, lessCohesiveDocResultP);
        }
        
        else  if(n==7){
        	/* Detection of Contextless Resource Antipattern */
        	detectContextlessResource(restAnalyser, execTime, summaryCsv, detectionTime);
        	writeOutput(selectedAPI+"-"+"ContextlessResource.txt", contextlessResultAP, contextlessResultP);
        }
        
        else if(n==8){
        	/* Detection of Inconsistent Documentation Antipattern */
        	detectInconsistentDocumentation(stopword, stopwordRemove, acronyms, restAnalyser, detectionTime, summaryCoCsv);
        	writeOutput(selectedAPI+"-"+"InconsistentDoc.txt", inconsistentdocumentationAP, inconsistentdocumentationP);
        }
        
        else if(n==9){
        	detectAmorphousURI(); writeOutput(selectedAPI+"-"+"AmorphousURI.txt", amorphusResultAP, amorphusResultP);
        	detectCRUDyURI(); writeOutput(selectedAPI+"-"+"CRUDyURI.txt", crudyURIResultAP, crudyURIResultP);
        	detectNonHierarchicalNodes(); writeOutput(selectedAPI+"-"+"NonHierarchicalNodes.txt", nonHierarchyResultAP, nonHierarchyResultP);
        	detectPluralisedNodes(); writeOutput(selectedAPI+"-"+"PluralisedNodes.txt", pluralisedResultAP, pluralisedResultP);
        	//detectUnversionedURIs(restAnalyser, execTime, summaryCsv, detectionTime); writeOutput(selectedAPI+"-"+"UnversionedURIs.txt", unversionedResultAP, versionedResultP);
        	//detectLessCohesiveDocumentation(stopword, stopwordRemove, acronyms, restAnalyser, detectionTime, summaryCoCsv); writeOutput(selectedAPI+"-"+"LessCohesiveDoc.txt", lessCohesiveDocResultAP, lessCohesiveDocResultP);
        	detectContextlessResource(restAnalyser, execTime, summaryCsv, detectionTime); writeOutput(selectedAPI+"-"+"ContextlessResource.txt", contextlessResultAP, contextlessResultP);
        	//detectInconsistentDocumentation(stopword, stopwordRemove, acronyms, restAnalyser, detectionTime, summaryCoCsv); writeOutput(selectedAPI+"-"+"InconsistentDoc.txt", inconsistentdocumentationAP, inconsistentdocumentationP);
        	
        }
        else{
        	System.out.println("Invaid input...");
        	System.exit(0);
        }
        
        System.err.println("... ...done... ...");
        
        long endTime = System.nanoTime();
        
        System.out.println("TOTAL DETECTION TIME: " + (double)(endTime - startTime)/1000000);
    }

	private static void writeOutput(String FNAME, List<String> Antipattern, List<String> Pattern) throws IOException {

		BufferedWriter bw = new BufferedWriter (new FileWriter(FNAME));

		bw.write ("***Antipattern***" + "\n");
		bw.write ("Count: " + Antipattern.size() + "\n");
		
		for (String line : Antipattern) {
			bw.write (line + "\n");
		}
		
		bw.write ("\n");
		bw.write ("***Pattern***" + "\n");
		bw.write ("Count: " + Pattern.size() + "\n");
		for (String line : Pattern) {
			bw.write (line + "\n");
		}
		
		bw.close ();
		
	}

	private static void detectInconsistentDocumentation(ArrayList<String> stopword, ArrayList<String> stopwordRemove, HashMap<String, ArrayList<String>> acronyms, RestAnalyser restAnalyser, long detectionTime, CSVWriter summaryCoCsv) throws IOException {

		String post[] = {"delete", "deletes", "update", "modify", "query", "list", "lists", "check", "checks", "verify", "get", "gets"};
		String delete[] = {"get", "gets", "find", "finds", "search", "check", "list", "verify", "get", "gets"};
		String put[] = {"delete", "deletes","creates", "finds", "create", "find", "search", "checks", "lists", "check", "list"};
		String get[] = {"delete", "deletes", "updates", "update", "creates", "create"};
		
		long execStartTime = System.currentTimeMillis();
        System.err.println("Detection of Inconsistent Documentation Antipattern: ");
        RestAnalyser restAnalyser2 = new RestAnalyser(selectedAPI);
        // get all the documentations in a Map
        GetURIDocumentation getDoc = new GetURIDocumentation();
        getDoc.createApiDescription2();
        restAnalyser2.setVerbose(false);
        restAnalyser2.setIsLemm(true);
        restAnalyser2.setStopWords(stopword);
        restAnalyser2.setNoStopWords(stopwordRemove);
        restAnalyser2.setAcronyms(acronyms);
        long execEndTime = 0;
        //long executionTimeTotal = 0, detectiontime, detectionTimeTotal = 0;
        // read each uri and its documentation...
        int result;
        for (String key : GetURIDocumentation.methodDocumentation.keySet()) {
        	
        	String documentation = GetURIDocumentation.methodDocumentation.get(key).trim();
        	String words[] = documentation.split(" ");
        	
        	String keys[] = key.split(":");
        	
        	if(keys[0].toString().toLowerCase().trim().equals("get")){
        		if(Arrays.asList(get).contains(words[0].trim().toLowerCase())){
        			inconsistentdocumentationAP.add(keys[0] + " " + keys[1].trim() + " " + GetURIDocumentation.methodDocumentation.get(key).trim());
        		}
        		else
        			inconsistentdocumentationP.add(keys[0] + " " + keys[1].trim() + " " + GetURIDocumentation.methodDocumentation.get(key).trim());
        	}
        		
        	else if(keys[0].toString().toLowerCase().trim().equals("delete")){
        		if(Arrays.asList(delete).contains(words[0].trim().toLowerCase())){
        			inconsistentdocumentationAP.add(keys[0] + " " + keys[1].trim() + " " + GetURIDocumentation.methodDocumentation.get(key).trim());
        		}
        		else
        			inconsistentdocumentationP.add(keys[0] + " " + keys[1].trim() + " " + GetURIDocumentation.methodDocumentation.get(key).trim());
        	}
        		
        	else if(keys[0].toString().toLowerCase().trim().equals("put")){
        		if(Arrays.asList(put).contains(words[0].trim().toLowerCase())){
        			inconsistentdocumentationAP.add(keys[0] + " " + keys[1].trim() + " " + GetURIDocumentation.methodDocumentation.get(key).trim());
        		}
        		else
        			inconsistentdocumentationP.add(keys[0] + " " + keys[1].trim() + " " + GetURIDocumentation.methodDocumentation.get(key).trim());
        	}
        		
        	else if(keys[0].toString().toLowerCase().trim().equals("post")){
        		if(Arrays.asList(post).contains(words[0].trim().toLowerCase())){
        			inconsistentdocumentationAP.add(keys[0] + " " + keys[1].trim() + " " + GetURIDocumentation.methodDocumentation.get(key).trim());
        		}
        		else
        			inconsistentdocumentationP.add(keys[0] + " " + keys[1].trim() + " " + GetURIDocumentation.methodDocumentation.get(key).trim());
        	}
        	
        	else{
        		inconsistentdocumentationP.add(keys[0] + " " + keys[1].trim() + " " + GetURIDocumentation.methodDocumentation.get(key).trim());
        	}
        	
        }
        CSVWriter writerco = null;
        try {
            writerco = new CSVWriter(new FileWriter(restAnalyser2.getCohesivresultsFile()), ',');
            writerco.writeAll(restAnalyser2.getCsvCohesiveResultsLines());
            writerco.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        detectionTime = restAnalyser.getAPDectTime() + restAnalyser.getNDectTime() + restAnalyser.getPDectTime();
        System.out.println("Total Execution Time for Inconsistent Documentation AP: " + (execEndTime - execStartTime) / 1000 + "s");
        System.out.println("Total Detection Time for Inconsistent Documentation AP: " + detectionTime / 1000 + "s");
        try {
            summaryCoCsv = new CSVWriter(new FileWriter("cohSummary.csv", true), ',', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER);
            summaryCoCsv.writeNext(new String[]{selectedAPI, restAnalyser2.getCohesivePatternCount()});
            summaryCoCsv.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}

	private static void detectContextlessResource(RestAnalyser restAnalyser, Long execTime, CSVWriter summaryCsv, long detectionTime) throws IOException {
		System.err.println("Detection of Contextless Resource Antipattern for " + selectedAPI);
        BufferedReader reader = new BufferedReader(new FileReader(docPath + selectedAPI + "/APITest.txt"));
        String URI;
        try {
        	int result = 0;
            while ((URI = reader.readLine()) != null) {
                result = restAnalyser.URIContextualAnalysis(URI);
            
            if(result == 1)
            	contextlessResultAP.add(URI);
            else if(result == 0)
            	contextlessResultP.add(URI);
            }
            execTime = System.currentTimeMillis() - execTime;
            CSVWriter writer = null;
            try {
                writer = new CSVWriter(new FileWriter(restAnalyser.getContextresultsFile()), ',');
                writer.writeAll(restAnalyser.getCsvResultsLines());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            reader.close();
        }
        try {

            summaryCsv = new CSVWriter(new FileWriter("summary.csv", true), ',', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER);

            summaryCsv.writeNext(new String[]{selectedAPI, restAnalyser.getContextualPatternCount()});
            summaryCsv.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        detectionTime = restAnalyser.getAPDectTime() + restAnalyser.getNDectTime() + restAnalyser.getPDectTime();
        System.out.println("Total Execution Time for Contextless Resource Antipattern: " + execTime / 1000 + "s");
        System.out.println("Total Detection Time for Contextless Resource Antipattern: " + detectionTime / 1000 + "s");
    System.err.println("##################################################################################################\n");	
		
	}

	private static void detectLessCohesiveDocumentation(ArrayList<String> stopword, ArrayList<String> stopwordRemove, HashMap<String, ArrayList<String>> acronyms, RestAnalyser restAnalyser, long detectionTime, CSVWriter summaryCoCsv) throws IOException {
		long execStartTime = System.currentTimeMillis();
        System.err.println("Detection of Less Cohesive Documentation Antipattern: ");
        RestAnalyser restAnalyser2 = new RestAnalyser(selectedAPI);
        // get all the documentations in a Map
        GetURIDocumentation getDoc = new GetURIDocumentation();
        getDoc.createApiDescription2();
        restAnalyser2.setVerbose(false);
        restAnalyser2.setIsLemm(true);
        restAnalyser2.setStopWords(stopword);
        restAnalyser2.setNoStopWords(stopwordRemove);
        restAnalyser2.setAcronyms(acronyms);
        long execEndTime = 0;
        //long executionTimeTotal = 0, detectiontime, detectionTimeTotal = 0;
        // read each uri and its documentation...
        int result;
        for (String key : GetURIDocumentation.uriDocumentation.keySet()) {
            //execTime = System.currentTimeMillis();
            String documentation = GetURIDocumentation.uriDocumentation.get(key);
            // obtain the numner of topics...
            int topics = StringUtils.split(SplitCamelCase.splitCamelCase(key.trim()), "-:{}<>/_)(").length;
            // create the topic model for each URI's documentation...
            restAnalyser2.CreateTopicModel2(topics, key, documentation);
            // create the topic model for each URI's documentation...
            restAnalyser2.printTopicModel2(key);
            // create the topic model for each URI's documentation...
            restAnalyser2.saveTopicModelCSV2(key, documentation);

            //execTime = System.currentTimeMillis() - execTime;
            //executionTimeTotal +=  execTime;
            //detectiontime = System.currentTimeMillis();
            execEndTime = System.currentTimeMillis();
            result = restAnalyser2.LessCohesiveDocumentationAnalysis(topics, key, documentation);
            
            if(result == 1)
            	lessCohesiveDocResultAP.add(key.trim() + " " + documentation);
            else if(result == 0)
            	lessCohesiveDocResultP.add(key.trim() + " " + documentation);
            
            //detectiontime = System.currentTimeMillis() - detectiontime;
            //detectionTimeTotal += detectiontime;
        }
        CSVWriter writerco = null;
        try {
            writerco = new CSVWriter(new FileWriter(restAnalyser2.getCohesivresultsFile()), ',');
            writerco.writeAll(restAnalyser2.getCsvCohesiveResultsLines());
            writerco.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        detectionTime = restAnalyser.getAPDectTime() + restAnalyser.getNDectTime() + restAnalyser.getPDectTime();
        System.out.println("Total Execution Time for Less Cohesive Documentation AP: " + (execEndTime - execStartTime) / 1000 + "s");
        System.out.println("Total Detection Time for Less Cohesive Documentation AP: " + detectionTime / 1000 + "s");
        try {
            summaryCoCsv = new CSVWriter(new FileWriter("cohSummary.csv", true), ',', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER);
            summaryCoCsv.writeNext(new String[]{selectedAPI, restAnalyser2.getCohesivePatternCount()});
            summaryCoCsv.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}

	private static void detectUnversionedURIs(RestAnalyser restAnalyser, Long execTime, CSVWriter summaryCsv, long detectionTime) throws IOException {
		
		System.err.println("Detection of UnversionedURIs Antipattern for " + selectedAPI);
        BufferedReader reader3 = new BufferedReader(new FileReader(docPath + selectedAPI + "/APITest.txt"));
        String URI3;
        try {

         while ((URI3 = reader3.readLine()) != null) {
        	 
        	 String regex = ".*v1.*";
        	 String regex1 = ".*v0.*";
        	 String regex2 = ".*v2.*";
        	 String regex3 = ".*v3.*";
        	 String regex4 = ".*v3.*";
        	 String regex5 = ".*/v/.*";
        	 String regex6 = ".*api-version=.*";
        	 
        	 boolean matches = Pattern.matches(regex, URI3); boolean matches1 = Pattern.matches(regex1, URI3); 
        	 boolean matches2 = Pattern.matches(regex2, URI3); boolean matches3 = Pattern.matches(regex3, URI3); 
        	 boolean matches4 = Pattern.matches(regex4, URI3); boolean matches5 = Pattern.matches(regex5, URI3); 
        	 boolean matches6 = Pattern.matches(regex6, URI3);
        	 
     		if (matches || matches1 || matches2 || matches3 || matches4 || matches5 || matches6 ) {
    			String ver = " [Version found.] ";
    			versionedResultP.add(URI3.trim() + ver);
    		}
     		else
     			unversionedResultAP.add(URI3.trim() + " [No version found.] ");     		
         	}
         }
        finally {
        	reader3.close();
        }
      	
	}

	private static void detectPluralisedNodes() throws IOException, JWNLException {

System.err.println("Detection of PluralisedNodes Antipattern for " + selectedAPI);
        
        BufferedReader reader4 = new BufferedReader(new FileReader(docPath + selectedAPI + "/" + selectedAPI + ".txt"));
        
        URIParser parser = Dictionaries.getInstance().getParser();
		LexicalResult.Reliability reliability = new LexicalResult.Reliability();
		
		try {

			String line;
			while ((line = reader4.readLine()) != null) {

				if(line.length() <= 2)
					continue;
				String[] tmp = line.split(">>");
				
				boolean goodType = false;
				ServiceHelper.HTTPMethod httpMethod = null;
				if(tmp[0].replaceAll(" ", "").equalsIgnoreCase("GET"))
					httpMethod = HTTPMethod.GET;
				if(tmp[0].replaceAll(" ", "").equalsIgnoreCase("POST"))
					httpMethod = HTTPMethod.POST;
				if(tmp[0].replaceAll(" ", "").equalsIgnoreCase("PUT"))
					httpMethod = HTTPMethod.PUT;
				if(tmp[0].replaceAll(" ", "").equalsIgnoreCase("DELETE"))
					httpMethod = HTTPMethod.DELETE;
				
				//POST or DELETE Method
				if (httpMethod == ServiceHelper.HTTPMethod.PUT || httpMethod == ServiceHelper.HTTPMethod.DELETE)
				{
					// pluralised last node
					String comment  = " [Pluralized last node with PUT|DELETE method.] ";
					
			        String nodes[] = tmp[1].split(Pattern.quote("/"));
			        int i=0;
			        for(String st : nodes){
			        	nodes[i] = st.replaceAll("[^a-zA-Z0-9]", "");
			        	i++;
			        }
			        
			        ArrayList<String> splittedNodes = new ArrayList<>();
			        
			        for(String str : nodes){
			        	ArrayList<String> tmp1 = new ArrayList<String>(Arrays.asList(str.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")));
			        	splittedNodes.addAll(tmp1);
			        }
			        
					if (splittedNodes.size() > 0) {
						ArrayList<String> lastNode = new ArrayList<String>(Arrays.asList(splittedNodes.get((splittedNodes.size())-1)));
						
						if (LexicalMetrics.nodeIsPlural(lastNode, reliability)) {
							goodType = false;
							
						}
						else{
							goodType = true;
						}
						if(!goodType) {
							pluralisedResultAP.add(tmp[0]+ " " + tmp[1].trim() + comment);
						} else {
							pluralisedResultP.add(tmp[0]+ " " + tmp[1].trim() + comment);
						}
					}
					
					
					
				}
				else if(httpMethod == ServiceHelper.HTTPMethod.POST)
				{
					// pluralised last node
					String comment  = " [Singular last node with POST method.] ";
					
					String nodes[] = tmp[1].split(Pattern.quote("/"));
			        int i=0;
			        for(String st : nodes){
			        	nodes[i] = st.replaceAll("[^a-zA-Z0-9]", "");
			        	i++;
			        }
			        
			        ArrayList<String> splittedNodes = new ArrayList<>();
			        
			        for(String str : nodes){
			        	ArrayList<String> tmp1 = new ArrayList<String>(Arrays.asList(str.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")));
			        	splittedNodes.addAll(tmp1);
			        }
			        
					if (splittedNodes.size() > 1) {
						ArrayList<String> lastNode = new ArrayList<String>(Arrays.asList(splittedNodes.get((splittedNodes.size())-1)));
						
						if(splittedNodes.size() > 0){
							if (LexicalMetrics.nodeIsPlural(lastNode, reliability)) {
								goodType = true;
							}
							else
								goodType = false;
						}
						if(!goodType) {
							pluralisedResultAP.add(tmp[0]+ " " + tmp[1].trim() + comment);
						} else {
							pluralisedResultP.add(tmp[0]+ " " + tmp[1].trim() + comment);
						}
					}
					
				}
			}

		} finally {
			reader4.close();
		}
		
	}

	private static void detectNonHierarchicalNodes() throws IOException, JWNLException {
		BufferedReader reader2 = new BufferedReader(new FileReader(docPath + selectedAPI + "/APITest.txt"));
        String URI2;
        try {

         while ((URI2 = reader2.readLine()) != null) {
        	 
             String nodes[] = URI2.split(Pattern.quote("/"));
             int i=0;
             for(String st : nodes){
             	nodes[i] = st.replaceAll("[^a-zA-Z0-9]", "");
             	i++;
             }
             
             ArrayList<String> splittedNodes = new ArrayList<>();
             
             for(String str : nodes){
             	ArrayList<String> tmp = new ArrayList<String>(Arrays.asList(str.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")));
             	splittedNodes.addAll(tmp);
             }
        	 
             LexicalResult.Reliability reliability = new LexicalResult.Reliability();
             String resultInformation = "";
             
     		// for each node
     		double hierarchies = 0;
     		boolean goodType = true;
     		boolean Pdetection=true, APdetection=false;
     		
            if (splittedNodes.size() >= 2) {
     			for (int j = 0; j < splittedNodes.size() - 1; j++) {
     				
     				String nodeA = splittedNodes.get(j);
     				String nodeB = splittedNodes.get(j+1);
     				
     			 if((nodeA != null && !nodeA.equals("")) && (nodeB != null && !nodeB.equals("")))
     			 {	
     				String relationType = HierarchicalMetrics.reversedHierarchy(new ArrayList<String>(Arrays.asList(nodeA)), new ArrayList<String>(Arrays.asList(nodeB)));
     				
    				if (!relationType.equals("")) {
    					
    					/** REVERSED HIERARCHY **/
    					APdetection = true;
    					OutputFormatter.getInstance().printDebugInformation("NonHierarchicalNodes", "Reversed hierarchy", nodeA + "\n" + nodeB);
    					resultInformation += "Reversed hierarchy of type " + relationType + " detected between " + nodeA + " and " + nodeB + ". ";
    					break;
    					
    				} else if (HierarchicalMetrics.specializationHierachy(new ArrayList<String>(Arrays.asList(nodeA.split(","))), new ArrayList<String>(Arrays.asList(nodeB.split(","))))) {
    					/** SPECIALIZATION HIERARCHY **/
    					Pdetection = true;
    					//OutputFormatter.getInstance().printDebugInformation("NonHierarchicalNodes", "Specialization hierarchy", nodeA + "\n" + nodeB);
    					resultInformation += "Specialization hierarchy detected between " + nodeA + " and " + nodeB + ". ";
    					hierarchies++; // increment the chain length
    					
    				} 
    				reliability.multiplyDetectionReliability(LexicalResult.Reliability.AMBIGUOUS_DETECTION);
     			 }
     			}
     			
     			if(Pdetection || APdetection){
     				
    				double max = splittedNodes.size() - 1;
    				if(APdetection){
    					if (hierarchies == 0) {
    						goodType = false;
    						resultInformation += hierarchies + " hierarchical relations were detected out of " + max;		
    					}
    				}
    				else if(Pdetection){
    					if (hierarchies >= 1) {
    						goodType = true;
    						resultInformation += hierarchies + " hierarchical relations were detected out of " + max;		
    					}
    				} 
    			}     			
             }
            
			if(!goodType) {
				nonHierarchyResultAP.add(URI2.trim() + resultInformation);
			} else {
				nonHierarchyResultP.add(URI2.trim() + resultInformation);
			}
         }
        }
        finally {
        	reader2.close();
        }
	}

	private static void detectCRUDyURI() throws IOException {
		System.err.println("Detection of CRUDyURI Antipattern for " + selectedAPI);
        BufferedReader reader1 = new BufferedReader(new FileReader(docPath + selectedAPI + "/APITest.txt"));
        String URI1;
        try {
        while ((URI1 = reader1.readLine()) != null) {
            	
        boolean goodType = false;	 
        String comment = null;   
        String nodes[] = URI1.split(Pattern.quote("/"));
        int i=0;
        for(String st : nodes){
        	nodes[i] = st.replaceAll("[^a-zA-Z0-9]", "");
        	i++;
        }
        
        ArrayList<String> splittedNodes = new ArrayList<>();
        
        for(String str : nodes){
        	ArrayList<String> tmp = new ArrayList<String>(Arrays.asList(str.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")));
        	splittedNodes.addAll(tmp);
        }
        
        boolean loop = false;
		

		boolean apFound= false;
		for (String nodeWord : splittedNodes) {	
			for (String crudyWord : crudyWords) {
				if (nodeWord.toLowerCase().equals(crudyWord.toLowerCase())) {
					goodType = false;
					//comment = "CRUDy word detected : " + crudyWord;
					apFound = true;
					comment = comment + " [" + crudyWord + " foud] ";
					break;
				}
				else{
					//comment = "No CRUDy word detected : ";
					goodType = true;
				}
				
			}
			if(apFound)
				break;
	
        
            }
		
		if(!goodType) {
			crudyURIResultAP.add(URI1.trim() + comment);
		} else{
			crudyURIResultP.add(URI1.trim() + comment);
		}
		
         }
        }finally {
        	reader1.close();
        }
		
	}

	private static void detectAmorphousURI() throws IOException {

		System.err.println("Detection of AmorphousURI Antipattern for " + selectedAPI);
        int foundAP;
        BufferedReader reader0 = new BufferedReader(new FileReader(docPath + selectedAPI + "/APITest.txt"));
        String URI0;
        try {

            while ((URI0 = reader0.readLine()) != null) {
            	
            	String comment = null;
            	
            	foundAP = 0;
                if(URI0.contains("%5F".toLowerCase()) || URI0.contains("%5F")){
                	foundAP = 1;
                	comment = comment + " [underscore found] ";
                }
                char c = URI0.charAt(URI0.length()-1);
                if (c == '/' || c == '\\'){ 
                	foundAP = 1;
                	comment = comment + " [trailing slash found] ";
                }
                
        		for (String extension : extensions) {
        			if (URI0.contains(extension.toLowerCase()) || URI0.contains(extension.toUpperCase())) {
        				foundAP = 1;
        				comment = comment + " [extension found] ";
        			}	
        		}
        		
        		/*int detections = 0;
        		
        		String separator = "/";
        		String nodes[] = URI0.split(Pattern.quote(separator));
        		
        		for (String node : nodes) {
        			
        			for (int i = 0; i < node.length(); i++) {
        					int beg;
        					if (i == 0) 
        						beg = 0;
        					else beg = 1;
        					for (int j = beg; j < node.length(); j++) {
        						c = node.charAt(j);
        						if (LexicalMetrics.characterIsUpperCase(c)) {
        							// detection
        							detections++;
        							break;
        							
        						}
        					}
        				
        			}
        		}
        		
        		
        		if (detections > 0){
        			foundAP = 1;
        			comment = comment + " [upper case resource found] ";
        		}
        		*/
        		
        		if(foundAP == 1)
        			amorphusResultAP.add(URI0.trim() + comment);
        		else
        			amorphusResultP.add(URI0.trim() + comment);
            }
            
        } finally {
            reader0.close();
        }
		
	}

	private static ArrayList<String> createExtensions() {
		ArrayList<String> result = new ArrayList<String>();
		result.add(".json");
		result.add(".html");
		result.add(".pdf");
		result.add(".txt");
		result.add(".xml");
		result.add(".jpg");
		result.add(".jpeg");
		result.add(".png");
		result.add(".gif");
		result.add(".csv");
		result.add(".htm");
		result.add(".zip");
		return result;
	}
	
	static private ArrayList<String> createCRUDyWords() {
		
		ArrayList<String> result = new ArrayList<String>();
		result.add("create"); result.add("make"); result.add("write");
		
		result.add("read"); result.add("search"); result.add("show"); result.add("take");
		
		result.add("delete"); result.add("destroy"); result.add("cancel"); result.add("remove");
		
		result.add("update"); result.add("copy"); result.add("move"); result.add("upgrade"); result.add("notify");		
		
		return result;
	}
}// main
