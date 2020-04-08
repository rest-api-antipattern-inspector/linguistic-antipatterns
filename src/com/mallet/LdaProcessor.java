package com.mallet;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveNonAlpha;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.FileIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

public class LdaProcessor {
    private int nbrtopword = 15;
    private int numTopics;
    private Pipe pipe;
    private String sourceTextFolder;
    private Topic aTopic;
    private ArrayList<String> sourceDocs;
    private String sourceText;
    private ArrayList<Document> documents;
    private ArrayList<Topic> aTopicList = new ArrayList<Topic>();
    private ArrayList<Topic> topics = new ArrayList<Topic>();
    private InstanceList globalInstance;
    private ArrayList pipeList = new ArrayList();
    private TokenSequenceRemoveStopwords stopwords;
    private boolean processLemm;
    private TopicWord topicWord;

    public LdaProcessor() {

        stopwords = new TokenSequenceRemoveStopwords();
        stopwords.setCaseSensitive(false);
        stopwords.setMarkDeletions(false);
        sourceDocs = new ArrayList<String>();
        buildDefaultPipe();
        pipe = new SerialPipes(pipeList);
    }

    public void addSourceDoc(String doc) {
        sourceDocs.add(doc);

    }

    public void clearSourceDocs() {
        sourceDocs.clear();

    }

    public ArrayList<Topic> getaTopicList() {
        return topics;
    }

    public ArrayList<ArrayList<String>> getTopicStringList() {
        ArrayList<String> l = new ArrayList<>();
        ArrayList<ArrayList<String>> TopicText = new ArrayList<ArrayList<String>>();
        for (Topic topic : topics) {
            TopicText.add(topic.getTopicWordsString());

        }

        return TopicText;
    }

    public void setProcessLemm(boolean processLemm) {
        this.processLemm = processLemm;
    }

    public void RemoveStopWords(ArrayList<String> stopWordSkiped) {

        if (stopWordSkiped.size() == 0) {
            return;
        }

        stopwords.removeStopWords(stopWordSkiped.toArray(new String[stopWordSkiped.size()]));

        pipeList.clear();
        buildDefaultPipe();
        pipeList.add(3, stopwords);
        pipe = new SerialPipes(pipeList);

        // setSourceText(this.sourceText);

    }

    public void AddStopWords(ArrayList<String> stopWordFile) {
        if (stopWordFile.size() == 0) {
            return;
        }
        stopwords.addStopWords(stopWordFile.toArray(new String[stopWordFile.size()]));

        pipeList.clear();
        buildDefaultPipe();
        pipeList.add(3, stopwords);
        pipe = new SerialPipes(pipeList);

        // setSourceText(this.sourceText);
    }

    public void setSourceText(String sourceText) {
        sourceText = sourceText.replaceAll("[^a-zA-Z]", " ");
        sourceText = sourceText.replaceAll("\\b\\w{1,2}\\b", " ");
        sourceText = sourceText.replaceAll("\\s{1,}", " ");

        if (this.processLemm) {
            this.sourceText = getLemm(sourceText);
        } else {
            this.sourceText = sourceText;
        }

        ArrayList<Instance> instanceArrayList = new ArrayList<Instance>();
        instanceArrayList.add(new Instance(this.sourceText, null, "Original Text", null));

        globalInstance = new InstanceList(pipe);
        globalInstance.addThruPipe(instanceArrayList.iterator());

    }

    public void createGlobalInstances() {

        globalInstance = new InstanceList(pipe);

        Integer i = 0;
        for (String doc : sourceDocs) {

            doc = doc.replaceAll("[^a-zA-Z]", " ");
            doc = doc.replaceAll("\\b\\w{1,2}\\b", " ");
            doc = doc.replaceAll("\\s{1,}", " ");
            if (this.processLemm)
                doc = getLemm(doc);
            ArrayList<Instance> instanceArrayList = new ArrayList<Instance>();
            instanceArrayList.add(new Instance(doc, null, "Original Text" + i.toString(), null));
            globalInstance.addThruPipe(instanceArrayList.iterator());
        }

    }

    protected String getLemm(String srcText) {
        TextTool textTool = new TextTool();
        String[] mots = srcText.split(" ");
        String rSourceText = "";
        for (int i = 0; i < mots.length; i++) {

            String lemmatize = textTool.lemmatize(mots[i]);
            if (lemmatize == null || lemmatize.length() == 0) {
                lemmatize = "{" + mots[i] + "}";
            }
            rSourceText = rSourceText + lemmatize + " ";
        }
        return rSourceText;
    }

    public ArrayList<Document> getDocuments() {
        return documents;
    }

    public void setNbrtopword(int nbrtopword) {
        this.nbrtopword = nbrtopword;
    }

    public int getNbrtopword() {
        return nbrtopword;
    }

    public void setNumTopics(int anumTopics) {
        this.numTopics = anumTopics;
    }

    public void processText() throws IOException {

        PrintStream err = System.err;
        System.setErr(new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        }));

        documents = new ArrayList<>();

		/*
         * if (this.processLemm) { this.sourceText = getLemm(this.sourceText); }
		 */

        // setSourceText(this.sourceText);
        // addSourceDoc(this.sourceText);
        createGlobalInstances();
        double alpha = 50.0 / numTopics;
        ParallelTopicModel model = new ParallelTopicModel(numTopics, alpha, 0.01);
        // ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0,
        // 0.01);
        model.setRandomSeed(5);
        model.setNumThreads(1);
        model.setNumIterations(1500);
        model.addInstances(globalInstance);
        model.estimate();

        WordTester wt = new WordTester(model);

        wt.getValues("hellooo", pipeList);

        Alphabet dataAlphabet = globalInstance.getDataAlphabet();
        double[] topicDistribution = model.getTopicProbabilities(0);

        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();

        for (int topicnbr = 0; topicnbr < numTopics; topicnbr++) {
            Iterator<IDSorter> sorterIterator = topicSortedWords.get(topicnbr).iterator();
            ArrayList<TopicWord> topicWordArrayList;
            int rank = 0;

            topicWordArrayList = new ArrayList<>();
            while (sorterIterator.hasNext() && rank < nbrtopword) {
                IDSorter idCountPair = sorterIterator.next();
                topicWord = new TopicWord();
                topicWord.setWord((String) dataAlphabet.lookupObject(idCountPair.getID()));
                topicWord.setWordWeight(idCountPair.getWeight());
                topicWordArrayList.add(topicWord);
                rank++;
            }

            aTopic = new Topic();
            aTopic.setTopicWords(topicWordArrayList);
            aTopic.setTopicDistribution(topicDistribution[topicnbr]);
            aTopicList.add(aTopic);
        }
        Document d = new Document();

        d.setFileName("!!");
        d.setTopics(aTopicList);
        topics = aTopicList;

        documents.add(d);
        aTopicList = new ArrayList<Topic>();
        System.setErr(err);
    }

    private void buildDefaultPipe() {
        pipeList.add(new Input2CharSequence("UTF-8"));
        Pattern tokenPattern = Pattern.compile("[\\p{L}\\p{N}_]+");
        pipeList.add(new CharSequence2TokenSequence(tokenPattern));
        pipeList.add(new TokenSequenceRemoveStopwords());
        pipeList.add(new TokenSequenceRemoveNonAlpha());
        pipeList.add(new TokenSequenceLowercase());
        pipeList.add(new TokenSequence2FeatureSequence());
    }

    private InstanceList readDirectory(File directory) {
        return readDirectories(new File[]{directory});
    }

    private InstanceList readDirectories(File[] directories) {
        FileIterator iterator = new FileIterator(directories, new TxtFilter(), FileIterator.LAST_DIRECTORY);

        InstanceList instances = new InstanceList(pipe);

        instances.addThruPipe(iterator);

        return instances;
    }

    private String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, Charset.defaultCharset());
    }

    private List<String> DiffStopWords(List<String> sourceList, List<String> listToRemove) {

        List<String> union = new ArrayList(sourceList);
        union.addAll(listToRemove);

        List<String> intersection = new ArrayList(sourceList);
        intersection.retainAll(listToRemove);

        List<String> symmetricDifference = new ArrayList(union);
        symmetricDifference.removeAll(intersection);

        return symmetricDifference;
    }

    public String malletWordSet() {
        return globalInstance.getDataAlphabet().toString();
    }

    private class TxtFilter implements FileFilter {
        public boolean accept(File file) {
            return file.toString().endsWith(".csv");
        }
    }

}
