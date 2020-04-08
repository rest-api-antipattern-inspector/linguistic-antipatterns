package com.mallet;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.*;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class TextTool {

	private Properties props;
	private StanfordCoreNLP pipeline;
	private Annotation document;
	private String lemma;

	public TextTool() {
		PrintStream err = System.err;
		System.setErr(new PrintStream(new OutputStream() {
			public void write(int b) {
			}
		}));

		props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma");
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
		lemma = "";
		pipeline = new StanfordCoreNLP(props, false);

		System.setErr(err);
	}

	public static boolean isStringNumeric(String str) {
		DecimalFormatSymbols currentLocaleSymbols = DecimalFormatSymbols.getInstance();
		char localeMinusSign = currentLocaleSymbols.getMinusSign();

		if (!Character.isDigit(str.charAt(0)) && str.charAt(0) != localeMinusSign)
			return false;

		boolean isDecimalSeparatorFound = false;
		char localeDecimalSeparator = currentLocaleSymbols.getDecimalSeparator();

		for (char c : str.substring(1).toCharArray()) {
			if (!Character.isDigit(c)) {
				if (c == localeDecimalSeparator && !isDecimalSeparatorFound) {
					isDecimalSeparatorFound = true;
					continue;
				}
				return false;
			}
		}
		return true;
	}

	public ArrayList<String> camelCaseSplit(String s) {
		ArrayList<String> Split = new ArrayList<String>(Arrays.asList(StringUtils.splitByCharacterTypeCamelCase(s)));
		return Split;
	}

	public String lemmatize(String text) {
		String[] mots = text.split("\\b\\s{2,}\\b", -1);
		lemma = "";
		for (int i = 0; i < mots.length; i++) {
			document = pipeline.process(mots[i]);

			for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
				for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
					String word = token.get(CoreAnnotations.TextAnnotation.class);
					lemma += token.get(CoreAnnotations.LemmaAnnotation.class);
				}
			}
		}
		return lemma;
	}

	public String getLemma(String scvWord) {
		String result = "";
		result = lemmatize(scvWord);
		return result;
	}

	public ArrayList<String> FiletoStrings(String path, String fileExt, Boolean joinLines) throws IOException {
		ArrayList<String> result = new ArrayList<String>();
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			File file = listOfFiles[i];
			if (file.isFile() && file.getName().endsWith(fileExt)) {
				BufferedReader in = new BufferedReader(
						new InputStreamReader(new FileInputStream(file.getAbsolutePath()), "UTF8"));
				String str = null;
				while ((str = in.readLine()) != null) {
					if (str != null && !str.isEmpty()) {
						result.add(str);
					}
				}
			}
			// System.out.println(result.toString());
			if (joinLines) {
				String s = StringUtils.join(result, " ");
				result.clear();
				result.add(s);
			}
		}
		return result;
	}

	public HashMap<String, ArrayList<String>> fileToBiDimensionalStringList(String path, String fileExt)
			throws IOException {
		HashMap<String, ArrayList<String>> hMap = new HashMap<String, ArrayList<String>>();

		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			File file = listOfFiles[i];
			if (file.isFile() && file.getName().endsWith(fileExt)) {
				BufferedReader in = new BufferedReader(
						new InputStreamReader(new FileInputStream(file.getAbsolutePath()), "UTF8"));
				String str = null;
				while ((str = in.readLine()) != null) {

					String LeftStr = str.substring(0, str.indexOf("->")).trim();
					ArrayList<String> RightList = new ArrayList<String>(
							Arrays.asList(str.substring(str.indexOf("->") + 2, str.length()).trim().split("/", -1)));
					hMap.put(LeftStr, RightList);
				}

			}
		}

		return hMap;

	}

}
